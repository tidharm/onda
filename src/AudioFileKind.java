/*====================================================================*\

AudioFileKind.java

Audio file enumeration.

\*====================================================================*/


// IMPORTS


import java.io.File;

import uk.org.blankaspect.audio.AiffFile;
import uk.org.blankaspect.audio.AudioFile;
import uk.org.blankaspect.audio.WaveFile;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.iff.Chunk;
import uk.org.blankaspect.iff.Id;
import uk.org.blankaspect.iff.IffChunk;
import uk.org.blankaspect.iff.RiffChunk;

import uk.org.blankaspect.util.StringKeyed;

//----------------------------------------------------------------------


// AUDIO FILE ENUMERATION


enum AudioFileKind
    implements StringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    AIFF
    (
        AudioFile.Kind.AIFF,
        CriticalIds.AIFF
    )
    {
        @Override
        public Chunk createChunk( )
        {
            return new IffChunk( );
        }
    },

    WAVE
    (
        AudioFile.Kind.WAVE,
        CriticalIds.WAVE
    )
    {
        @Override
        public Chunk createChunk( )
        {
            return new RiffChunk( );
        }
    };

    //------------------------------------------------------------------

    private interface CriticalIds
    {
        Id[]    AIFF    =
        {
            AiffFile.AIFF_COMMON_ID,
            AiffFile.AIFF_DATA_ID
        };
        Id[]    WAVE    =
        {
            WaveFile.WAVE_FORMAT_ID,
            WaveFile.WAVE_DATA_ID
        };
    }

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private AudioFileKind( AudioFile.Kind fileKind,
                           Id[]           criticalIds )
    {
        this.fileKind = fileKind;
        this.criticalIds = criticalIds;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static AudioFileKind forKey( String key )
    {
        return forFileKind( AudioFile.Kind.forKey( key ) );
    }

    //------------------------------------------------------------------

    public static AudioFileKind forFileKind( AudioFile.Kind fileKind )
    {
        for ( AudioFileKind value : values( ) )
        {
            if ( value.fileKind == fileKind )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

    public static AudioFileKind forFile( File file )
        throws AppException
    {
        return forFileKind( AudioFile.Kind.forFile( file ) );
    }

    //------------------------------------------------------------------

    public static AudioFileKind forFilename( String filename )
    {
        return forFileKind( AudioFile.Kind.forFilename( filename ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

    public abstract Chunk createChunk( );

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : StringKeyed interface
////////////////////////////////////////////////////////////////////////

    public String getKey( )
    {
        return fileKind.getKey( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public String toString( )
    {
        return fileKind.toString( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public Id[] getCriticalIds( )
    {
        return criticalIds;
    }

    //------------------------------------------------------------------

    public AudioFile createFile( File file )
    {
        return fileKind.createFile( file );
    }

    //------------------------------------------------------------------

    public AudioFile createFile( File file,
                                 int  numChannels,
                                 int  bitsPerSample,
                                 int  sampleRate )
    {
        return fileKind.createFile( file, numChannels, bitsPerSample, sampleRate );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private AudioFile.Kind  fileKind;
    private Id[]            criticalIds;

}

//----------------------------------------------------------------------
