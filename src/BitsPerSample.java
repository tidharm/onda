/*====================================================================*\

BitsPerSample.java

Bits per sample enumeration.

\*====================================================================*/


// BITS PER SAMPLE ENUMERATION


enum BitsPerSample
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    _16 ( 16, 4 ),
    _24 ( 24, 5 );

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private BitsPerSample( int numBits,
                           int keyLength )
    {
        this.numBits = numBits;
        this.keyLength = keyLength;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static BitsPerSample forNumBits( int numBits )
    {
        for ( BitsPerSample value : values( ) )
        {
            if ( value.numBits == numBits )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public String toString( )
    {
        return Integer.toString( numBits );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public int getNumBits( )
    {
        return numBits;
    }

    //------------------------------------------------------------------

    public int getKeyLength( )
    {
        return keyLength;
    }

    //------------------------------------------------------------------

    public int getBytesPerSample( )
    {
        return ( numBits >> 3 );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private int numBits;
    private int keyLength;

}

//----------------------------------------------------------------------
