/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common.framestructure;

/**
 *
 * @author renato_veiga
 */

public class FC {
    
    //b7 = 1
    public static final byte FC_NULL                          = (byte) 0x0F;
    public static final byte FC_RESERVED0                     = (byte) 0x40;
    public static final byte FC_RESERVED1                     = (byte) 0x41;
    public static final byte FC_RESERVED2                     = (byte) 0x42;
    public static final byte FC_REQ_SEND_DATA_WITH_ACK_LOW    = (byte) 0x43;
    public static final byte FC_REQ_SEND_DATA_WITH_ACK_HIGH   = (byte) 0x44;
    public static final byte FC_REQ_SEND_DATA_WITH_NO_ACK_LOW = (byte) 0x45;
    public static final byte FC_REQ_SEND_DATA_WITH_NO_ACK_HIGH =(byte) 0x46;
    public static final byte FC_REQ_FDL_STATUS_WITH_REPLY     = (byte) 0x49;
    public static final byte FC_RESERVED3                     = (byte) 0x4A;
    public static final byte FC_RESERVED4                     = (byte) 0x4B;
    public static final byte FC_REQ_SEND_DATA_LOW             = (byte) 0x4C;
    public static final byte FC_REQ_SEND_DATA_HIGH            = (byte) 0x4D;
    public static final byte FC_REQ_IDENT_WITH_REPLY          = (byte) 0x4E;
    public static final byte FC_REQ_LSAP_STATUS_WITH_REPLY    = (byte) 0x4F;

    //b7 = 0
    public static final byte FC_ACK_POS                       = (byte) 0x00;
    public static final byte FC_ACK_NEG_FDL_ERR               = (byte) 0x01;
    public static final byte FC_ACK_NEG_NO_RESOURCE           = (byte) 0x02;
    public static final byte FC_ACK_NEG_NO_SERVICE_ACTIVATED  = (byte) 0x03;
    public static final byte FC_RESERVED5                     = (byte) 0x04;
    public static final byte FC_RESERVED6                     = (byte) 0x05;
    public static final byte FC_RESERVED7                     = (byte) 0x06;
    public static final byte FC_RESERVED8                     = (byte) 0x07;
    public static final byte FC_RES_FDL_LOW                   = (byte) 0x08;
    public static final byte FC_ACK_NEG_NO_RESPONSE           = (byte) 0x09;
    public static final byte FC_RES_FDL_HIGH                  = (byte) 0x0A;
    public static final byte FC_RESERVED9                     = (byte) 0x0B;
    public static final byte FC_RES_FDL_LOW_NO_RESOURCE       = (byte) 0x0C;
    public static final byte FC_RES_FDL_HIGH_NO_RESOURCE      = (byte) 0x0D;
    public static final byte FC_RESERVED10                    = (byte) 0x0E;
    public static final byte FC_RESERVED11                    = (byte) 0x0F;

    public static final byte FC_STN_TYPE_PASSIVE_STATION           = (byte)0x00;
    public static final byte FC_STN_TYPE_ACT_STN_NOT_RDY           = (byte)0x10;
    public static final byte FC_STN_TYPE_ACT_STN_RDY_FOR_TOKEN_RING= (byte)0x20;
    public static final byte FC_STN_TYPE_ACT_STN_IN_TOKEN_RING     = (byte)0x30;

    protected byte res;
    protected boolean SendReq = false;
    protected boolean FCB;
    protected boolean FCV;
    private byte StationType;
    protected byte fcbyte;

    public FC(byte fcf )
    {
        DecodeFC(fcf);
    }

    @Override
    public String toString()
    {
        switch( this.fcbyte ) {
          case FC_NULL:                             return "Not applied";
          case FC_REQ_SEND_DATA_WITH_ACK_LOW:       return "Send Data With Acknowledge low ( SDA )";
          case FC_REQ_SEND_DATA_WITH_ACK_HIGH:      return "Send Data With No Acknowledge low ( SDN )";
          case FC_REQ_SEND_DATA_WITH_NO_ACK_LOW:    return "Send Data With Acknowledge high ( SDA )";
          case FC_REQ_SEND_DATA_WITH_NO_ACK_HIGH:   return "Send Data With No Acknowledge high ( SDN )";
          case FC_REQ_FDL_STATUS_WITH_REPLY:        return "Request FDL-Status with Reply";
          case FC_REQ_SEND_DATA_LOW:                return "Send and Request Data low ( SRD )";
          case FC_REQ_SEND_DATA_HIGH:               return "Send and Request Data high ( SRD )";
          case FC_REQ_IDENT_WITH_REPLY:             return "Request Ident with Reply";
          case FC_REQ_LSAP_STATUS_WITH_REPLY:       return "Request LSAP-Status with Reply";
          case FC_ACK_POS:                          return "ACK positive ( OK )";
          case FC_ACK_NEG_FDL_ERR:                  return "ACK negative, FDL/FMA ½-User Error ( UE )";
          case FC_ACK_NEG_NO_RESOURCE:              return "ACK negative, no resource for send data ( RR )";
          case FC_ACK_NEG_NO_SERVICE_ACTIVATED:     return "ACK negative, no service activated ( RS )";
          case FC_RES_FDL_LOW:                      return "Response FDL/FMA1/2-Data low send data ok ( DL )";
          case FC_ACK_NEG_NO_RESPONSE:              return "ACK negative, no response FDL/FMA1/2-data, send data ok ( NR )";
          case FC_RES_FDL_HIGH:                     return "Response FDL-data high, send data ok ( DH )";
          case FC_RES_FDL_LOW_NO_RESOURCE:          return "Response FDL-data low, no resource for send data ( RDL )";
          case FC_RES_FDL_HIGH_NO_RESOURCE:         return "Response FDL-data high, no resource for send data ( RDH )";
          default:                                  return "Unknown FC";
        }
    }

    public boolean equals(byte fcfunction) {
        boolean ret = false;
        if (this.fcbyte == fcfunction) {
            ret = true;
        }
        return ret;
    }

    public byte getFC(){
        return this.fcbyte;
    }

    boolean IsReq() {
        return SendReq;
    }

    public void setFC(byte b)
    {
        byte code;
        
        fcbyte = b;
        
        //Test future
        byte isreq = (byte) (b & 0x40);
        if( isreq != 0 )
            code = (byte) (b & 0x4F);
        else
            code = b;
        
        switch( code ) {
            case FC_RESERVED0:
            case FC_RESERVED1:
            case FC_RESERVED2:
            case FC_REQ_SEND_DATA_WITH_ACK_LOW:
            case FC_REQ_SEND_DATA_WITH_ACK_HIGH:
            case FC_REQ_SEND_DATA_WITH_NO_ACK_LOW:
            case FC_REQ_SEND_DATA_WITH_NO_ACK_HIGH:
            case FC_REQ_FDL_STATUS_WITH_REPLY:
            case FC_RESERVED3:
            case FC_RESERVED4:
            case FC_REQ_SEND_DATA_LOW:
            case FC_REQ_SEND_DATA_HIGH:
            case FC_REQ_IDENT_WITH_REPLY:
            case FC_REQ_LSAP_STATUS_WITH_REPLY:                
                SendReq = true;
                break;

            case FC_ACK_POS:
            case FC_ACK_NEG_FDL_ERR:
            case FC_ACK_NEG_NO_RESOURCE:
            case FC_ACK_NEG_NO_SERVICE_ACTIVATED:
            case FC_RESERVED5:
            case FC_RESERVED6:
            case FC_RESERVED7:
            case FC_RESERVED8:
            case FC_RES_FDL_LOW:
            case FC_ACK_NEG_NO_RESPONSE:
            case FC_RES_FDL_HIGH:
            case FC_RESERVED9:
            case FC_RES_FDL_LOW_NO_RESOURCE:
            case FC_RES_FDL_HIGH_NO_RESOURCE:
            case FC_RESERVED10:
            case FC_RESERVED11:                
                SendReq = false; 
                break;
        }
    }

    void DecodeFC(byte b) {
        res     = (byte) (b & 0x80);
        SendReq = ((b & 0x40) != 0) ? true : false;
        if( SendReq == true) {
            //b7 = 1
            //Send/Req Frame
            FCB = ((b & 0x20) != 0) ? true : false;
            FCV = ((b & 0x10) != 0) ? true : false;
        }
        else {
            //b7 = 0
            //Acknowledgement/Response frame
            boolean b6 = ((b & 0x20) != 0) ? true : false;
            boolean b5 = ((b & 0x10) != 0) ? true : false;

            if( b6 == false )
            {
             if( b5 == false )
                setStationType(FC_STN_TYPE_PASSIVE_STATION);
             else
                setStationType(FC_STN_TYPE_ACT_STN_NOT_RDY);
            }
            else
            {
             if( b5 == false )
                setStationType(FC_STN_TYPE_ACT_STN_RDY_FOR_TOKEN_RING);
             else
                setStationType(FC_STN_TYPE_ACT_STN_IN_TOKEN_RING);
            }

        }
        //fcbyte = (byte) (b & 0x0F);
        setFC( b );
    }

    public byte EncodeFC( )
    {
        byte ret = 0x00;
        if( IsReq() == true ) {
            //Request Frame
            ret = 0x40;
            ret |= ((FCB == true) ? 0x20 : 0x00) | ((FCV == true) ? 0x10 : 0x00);
            ret |= fcbyte;
        }
        else {
            //Response Frame
            ret = 0x00;
            ret |= fcbyte;
        }
        return ret;
    }

    /**
     * @return the StationType
     */
    public byte getStationType() {
        return StationType;
    }

    /**
     * @param StationType the StationType to set
     */
    public void setStationType(byte StationType) {
        this.StationType = StationType;
    }

    public void SetRequest(boolean b) {
        this.SendReq = b;
    }

    public void setToogleFCB() {
        this.FCV = true;
    }

    void setFCB(boolean bFCB) {
        this.FCB = bFCB;
    }

    void setFCV(boolean bFCV) {
        this.FCV = bFCV;
    }

    boolean getFCB() {
        return this.FCB;
    }

    boolean getFCV() {
        return this.FCV;
    }
}
