/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.framestructure;

import common.ConversionsAdvanced;

/**
 *
 * @author emossin
 */
public class SD {

    public static final byte SD_NULL = (byte) 0;   //0x00
    public static final byte SD1     = (byte) 16;  //0x10
    public static final byte SD2     = (byte) 104; //0x68
    public static final byte SD3     = (byte) 102; //0x66
    public static final byte SD4     = (byte) 220; //0xDC
    public static final byte SC      = (byte) 229; //0xE5
    byte sdType;

    public SD(byte sdType){
        this.sdType = sdType;
    }

    @Override
    public String toString() {
        String sret = "NULL";

        switch( sdType )
        {
            case SD_NULL:
                sret = "NULL";
                break;

            case SD1:
                sret = "SD1";
                break;

            case SD2:
                sret = "SD2";
                break;

            case SD3:
                sret = "SD3";
                break;

            case SD4:
                sret = "SD4";
                break;

            case SC:
                sret = "SC";
                break;
        }
        return sret;
    }

    public String toStringLong()
    {
        switch( sdType ) {
          case SD_NULL: return "NULL SD";
          case SD1:     return "SD1(10h) - Fixed Data Size Frame";
          case SD2:     return "SD2(68h) - Variable Data Size Frame";
          case SD3:     return "SD3(66h) - Fixed Data Size Frame";
          case SD4:     return "SD4(DCh) - Token Pass";
          case SC:      return "SC(E5h)  - Short ACK";
          default:      return "Unknown SD";
        }
    }

    public boolean equals(byte sdType){
        boolean ret = false;
        if(this.sdType == sdType){
            ret = true;
        }
        return ret;
    }

    byte getSD() {
        return sdType;
    }

    void setSD(byte SDVal) {
        switch( SDVal ) {
          case SD_NULL:
          case SD1:
          case SD2:
          case SD3:
          case SD4:
          case SC:
            sdType = SDVal;
            break;

          default:
              sdType = 0x00;
              break;
        }
    }
}
