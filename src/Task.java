/*====================================================================*\

Task.java

Task class.

\*====================================================================*/


// IMPORTS


import java.io.File;

import java.util.List;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.TaskCancelledException;

import uk.org.blankaspect.gui.ProgressView;

import uk.org.blankaspect.iff.Chunk;
import uk.org.blankaspect.iff.ChunkFilter;

//----------------------------------------------------------------------


// TASK CLASS


abstract class Task
    extends uk.org.blankaspect.util.Task
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // COMPRESS TASK CLASS


    public static class Compress
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public Compress( List<InputOutput> inputsOutputs,
                         ChunkFilter[]     chunkFilters,
                         boolean           recursive )
        {
            this.inputsOutputs = inputsOutputs;
            this.chunkFilters = chunkFilters;
            this.recursive = recursive;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                App.getInstance( ).compress( inputsOutputs, chunkFilters, recursive );
            }
            catch ( TaskCancelledException e )
            {
                setException( e, false );
            }

            // Terminate other task threads
            setCancelled( true );

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private List<InputOutput>   inputsOutputs;
        private ChunkFilter[]       chunkFilters;
        private boolean             recursive;

    }

    //==================================================================


    // EXPAND TASK CLASS


    public static class Expand
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public Expand( List<InputOutput> inputsOutputs,
                       boolean           recursive )
        {
            this.inputsOutputs = inputsOutputs;
            this.recursive = recursive;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                App.getInstance( ).expand( inputsOutputs, recursive );
            }
            catch ( TaskCancelledException e )
            {
                setException( e, false );
            }

            // Terminate other task threads
            setCancelled( true );

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private List<InputOutput>   inputsOutputs;
        private boolean             recursive;

    }

    //==================================================================


    // VALIDATE TASK CLASS


    public static class Validate
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public Validate( List<InputOutput> inputsOutputs,
                         boolean           recursive )
        {
            this.inputsOutputs = inputsOutputs;
            this.recursive = recursive;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            App.getInstance( ).validate( inputsOutputs, recursive );

            // Terminate other task threads
            setCancelled( true );

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private List<InputOutput>   inputsOutputs;
        private boolean             recursive;

    }

    //==================================================================


    // WRITE COMPRESSED FILE TASK CLASS


    public static class WriteCompressed
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteCompressed( FileProcessor fileProcessor,
                                File          file,
                                byte[]        privateData )
        {
            this.fileProcessor = fileProcessor;
            this.file = file;
            this.privateData = privateData;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                fileProcessor.writeCompressedFile( file, privateData );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FileProcessor   fileProcessor;
        private File            file;
        private byte[]          privateData;

    }

    //==================================================================


    // WRITE EXPANDED FILE TASK CLASS


    public static class WriteExpanded
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteExpanded( FileProcessor  fileProcessor,
                              File           file,
                              AudioFileKind  fileKind,
                              List<Chunk>    chunks )
        {
            this.fileProcessor = fileProcessor;
            this.file = file;
            this.fileKind = fileKind;
            this.chunks = chunks;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                fileProcessor.writeExpandedFile( file, fileKind, chunks );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FileProcessor   fileProcessor;
        private File            file;
        private AudioFileKind   fileKind;
        private List<Chunk>     chunks;

    }

    //==================================================================


    // WRITE CONFIGURATION TASK CLASS


    public static class WriteConfig
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteConfig( File file )
        {
            this.file = file;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                AppConfig.getInstance( ).write( file );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private File    file;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private Task( )
    {
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void setInfo( String str )
    {
        ProgressView progressView = getProgressView( );
        if ( progressView != null )
            progressView.setInfo( str );
    }

    //------------------------------------------------------------------

    public static void setInfo( String str,
                                File   file )
    {
        ProgressView progressView = getProgressView( );
        if ( progressView != null )
            progressView.setInfo( str, file );
    }

    //------------------------------------------------------------------

    public static void setProgress( double value )
    {
        ProgressView progressView = getProgressView( );
        if ( progressView != null )
            progressView.setProgress( 0, value );
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
