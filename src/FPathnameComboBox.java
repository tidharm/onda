/*====================================================================*\

FPathnameComboBox.java

Pathname combo box class.

\*====================================================================*/


// IMPORTS


import uk.org.blankaspect.gui.PathnameComboBox;

//----------------------------------------------------------------------


// PATHNAME COMBO BOX CLASS


class FPathnameComboBox
    extends PathnameComboBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int MAX_NUM_PATHNAMES   = 32;
    private static final    int NUM_COLUMNS         = 40;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public FPathnameComboBox( )
    {
        super( MAX_NUM_PATHNAMES, NUM_COLUMNS );
        setUnixStyle( AppConfig.getInstance( ).isShowUnixPathnames( ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void addObserver( FPathnameComboBox comboBox )
    {
        AppConfig.getInstance( ).addShowUnixPathnamesObserver( comboBox.getField( ) );
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
