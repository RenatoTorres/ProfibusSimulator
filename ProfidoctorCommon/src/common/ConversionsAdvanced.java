/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

/**
 *
 * @author Renato Veiga Torres
 */
public class ConversionsAdvanced extends Conversions
{

    public static int ByteToUnsignedInt(byte b)
    {
        return ( ((int) b) < 0) ? (256 + ((int) b)) : ((int) b);
    }

    public static String IntToStringByte( int i )
    {
        String sret = Integer.toHexString(i).toUpperCase();
        return (sret.length() == 1) ? ("0"+sret) : sret;
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public static String toStringFromStream( byte data[] )
    {
        return toStringFromStream(data, "");
    }


    public static String toStringFromStream(byte data[], String div)
    {
        String sret = "";
        String s;
        for(int i=0; i < data.length; i++ )
        {
            s = Integer.toHexString( (int) data[i] );

            //Catch bug 0xFFFFFFDC
            if( s.length() > 2 )
                s = s.substring( s.length()-2, s.length() );

            //But 0 in front of one digits numbers
            if( s.length() < 2 )
                s = "0" + s;

            if( sret.equals("") == true )
                sret += s;
            else
                sret += div + s;
        }
        return sret.toUpperCase();
    }

}
