/*====================================================================*\

PrivateData.java

Private data class.

\*====================================================================*/


// IMPORTS


import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.ArrayList;
import java.util.List;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.iff.Chunk;
import uk.org.blankaspect.iff.ChunkFilter;
import uk.org.blankaspect.iff.FormFile;
import uk.org.blankaspect.iff.Id;

import uk.org.blankaspect.util.NumberUtilities;

//----------------------------------------------------------------------


// PRIVATE DATA CLASS


class PrivateData
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int SOURCE_KIND_SIZE    = 2;
    private static final    int ADLER32_SIZE        = 4;
    private static final    int NUM_CHUNKS_SIZE     = 4;
    private static final    int HEADER_SIZE         = SOURCE_KIND_SIZE + ADLER32_SIZE + NUM_CHUNKS_SIZE;

    private static final    int BLOCK_SIZE  = 1 << 12;  // 4096

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // ERROR IDENTIFIERS


    private enum ErrorId
        implements AppException.Id
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        MALFORMED_DATA
        ( "The private data is malformed." ),

        INVALID_DATA
        ( "The private data is invalid." ),

        UNRECOGNISED_SOURCE_FILE_KIND
        ( "The private data belong to an unrecognised kind of audio file." ),

        INVALID_NUM_CHUNKS
        ( "The number of chunks specified in the private data is invalid." ),

        INCORRECT_ADLER32
        ( "The checksum of the private data is incorrect." );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ErrorId( String message )
        {
            this.message = message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : AppException.Id interface
    ////////////////////////////////////////////////////////////////////

        public String getMessage( )
        {
            return message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  message;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // SOURCE CHUNK CLASS


    private static class SourceChunk
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private SourceChunk( Id  id,
                             int size )
        {
            this.id = id;
            this.size = size;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        Id  id;
        int size;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // CHUNK READER CLASS


    private class Reader
        implements FormFile.ChunkReader
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Reader( ChunkFilter filter )
        {
            this.filter = filter;
            compressor = new Deflater( Deflater.BEST_COMPRESSION );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : FormFile.ChunkReader interface
    ////////////////////////////////////////////////////////////////////

        public void beginReading( RandomAccessFile raFile,
                                  Id               typeId,
                                  int              size )
        {
            sourceChunks.clear( );
            compressedDataBlocks.clear( );
            outBuffer = new byte[BLOCK_SIZE];
            outOffset = 0;
            compressor.reset( );
        }

        //--------------------------------------------------------------

        public void read( RandomAccessFile raFile,
                          Id               id,
                          int              size )
            throws AppException, IOException
        {
            if ( Util.indexOf( id, sourceKind.getCriticalIds( ) ) < 0 )
            {
                if ( filter.accept( id ) )
                {
                    sourceChunks.add( new SourceChunk( id, size ) );
                    if ( size > 0 )
                    {
                        byte[] buffer = new byte[size];
                        raFile.readFully( buffer );
                        compressor.setInput( buffer );
                        updateCompressedData( );
                    }
                }
            }
            else
                sourceChunks.add( new SourceChunk( id, 0 ) );
        }

        //--------------------------------------------------------------

        public void endReading( RandomAccessFile raFile )
        {
            compressor.finish( );
            updateCompressedData( );
            if ( outOffset > 0 )
            {
                byte[] buffer = new byte[outOffset];
                System.arraycopy( outBuffer, 0, buffer, 0, buffer.length );
                compressedDataBlocks.add( buffer );
            }
            adler32 = compressor.getAdler( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private void updateCompressedData( )
        {
            while ( true )
            {
                int length = compressor.deflate( outBuffer, outOffset, outBuffer.length - outOffset );
                if ( length == 0 )
                    break;
                outOffset += length;
                if ( outOffset >= outBuffer.length )
                {
                    compressedDataBlocks.add( outBuffer );
                    outBuffer = new byte[BLOCK_SIZE];
                    outOffset = 0;
                }
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private ChunkFilter filter;
        private byte[]      outBuffer;
        private int         outOffset;
        private Deflater    compressor;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public PrivateData( AudioFileKind sourceKind )
    {
        this.sourceKind = sourceKind;
        sourceChunks = new ArrayList<>( );
        compressedDataBlocks = new ArrayList<>( );
    }

    //------------------------------------------------------------------

    public PrivateData( byte[] data )
        throws AppException
    {
        sourceChunks = new ArrayList<>( );
        compressedDataBlocks = new ArrayList<>( );
        set( data );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public AudioFileKind getSourceKind( )
    {
        return sourceKind;
    }

    //------------------------------------------------------------------

    public int getNumChunks( )
    {
        return sourceChunks.size( );
    }

    //------------------------------------------------------------------

    public List<Id> getAncillaryIds( )
    {
        List<Id> ids = new ArrayList<>( );
        for ( SourceChunk element : sourceChunks )
        {
            if ( Util.indexOf( element.id, sourceKind.getCriticalIds( ) ) < 0 )
                ids.add( element.id );
        }
        return ids;
    }

    //------------------------------------------------------------------

    public byte[] getCompressedData( )
    {
        // Allocate buffer for data
        int length = HEADER_SIZE + sourceChunks.size( ) * Chunk.HEADER_SIZE;
        for ( byte[] data : compressedDataBlocks )
            length += data.length;
        byte[] buffer = new byte[length];

        // Set header in buffer
        int offset = 0;
        NumberUtilities.intToBytesBE( sourceKind.ordinal( ), buffer, offset, SOURCE_KIND_SIZE );
        offset += SOURCE_KIND_SIZE;
        NumberUtilities.intToBytesBE( adler32, buffer, offset, ADLER32_SIZE );
        offset += ADLER32_SIZE;
        NumberUtilities.intToBytesBE( sourceChunks.size( ), buffer, offset, NUM_CHUNKS_SIZE );
        offset += NUM_CHUNKS_SIZE;

        // Set list of chunks in buffer
        for ( SourceChunk element : sourceChunks )
        {
            element.id.put( buffer, offset );
            offset += Id.SIZE;
            NumberUtilities.intToBytesBE( element.size, buffer, offset, Chunk.SIZE_SIZE );
            offset += Chunk.SIZE_SIZE;
        }

        // Set compressed chunk data in buffer
        for ( byte[] data : compressedDataBlocks )
        {
            System.arraycopy( data, 0, buffer, offset, data.length );
            offset += data.length;
        }

        return buffer;
    }

    //------------------------------------------------------------------

    public Reader getReader( ChunkFilter filter )
    {
        return new Reader( filter );
    }

    //------------------------------------------------------------------

    public void setChunk( int   index,
                          Chunk chunk )
    {
        int offset = 0;
        for ( int i = 0; i < index; ++i )
            offset += sourceChunks.get( i ).size;
        chunk.set( sourceChunks.get( index ).id, expandedData, offset, sourceChunks.get( index ).size );
    }

    //------------------------------------------------------------------

    private void set( byte[] data )
        throws AppException
    {
        // Parse header
        if ( data.length < HEADER_SIZE )
            throw new AppException( ErrorId.MALFORMED_DATA );

        int offset = 0;
        int sourceKindIndex = NumberUtilities.bytesToIntBE( data, offset, SOURCE_KIND_SIZE );
        offset += SOURCE_KIND_SIZE;
        if ( (sourceKindIndex < 0) || (sourceKindIndex >= AudioFileKind.values( ).length) )
            throw new AppException( ErrorId.UNRECOGNISED_SOURCE_FILE_KIND );
        sourceKind = AudioFileKind.values( )[sourceKindIndex];

        int adler32 = NumberUtilities.bytesToIntBE( data, offset, ADLER32_SIZE );
        offset += ADLER32_SIZE;

        int numChunks = NumberUtilities.bytesToIntBE( data, offset, NUM_CHUNKS_SIZE );
        offset += NUM_CHUNKS_SIZE;
        if ( numChunks < 0 )
            throw new AppException( ErrorId.INVALID_NUM_CHUNKS );

        if ( data.length < HEADER_SIZE + numChunks * Chunk.HEADER_SIZE )
            throw new AppException( ErrorId.MALFORMED_DATA );

        // Parse list of chunks
        int length = 0;
        for ( int i = 0; i < numChunks; ++i )
        {
            Id id = new Id( data, offset );
            offset += Id.SIZE;
            int size = NumberUtilities.bytesToIntBE( data, offset, Chunk.SIZE_SIZE );
            offset += Chunk.SIZE_SIZE;
            sourceChunks.add( new SourceChunk( id, size ) );
            length += size;
        }

        // Expand compressed data
        expandedData = new byte[length];
        Inflater inflater = new Inflater( );
        inflater.setInput( data, offset, data.length - offset );
        try
        {
            length = inflater.inflate( expandedData );
        }
        catch ( DataFormatException e  )
        {
            throw new AppException( ErrorId.INVALID_DATA );
        }

        // Validate data
        if ( !inflater.finished( ) || (length < expandedData.length) )
            throw new AppException( ErrorId.INVALID_DATA );
        if ( inflater.getAdler( ) != adler32 )
            throw new AppException( ErrorId.INCORRECT_ADLER32 );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private AudioFileKind       sourceKind;
    private int                 adler32;
    private List<SourceChunk>   sourceChunks;
    private List<byte[]>        compressedDataBlocks;
    private byte[]              expandedData;

}

//----------------------------------------------------------------------
