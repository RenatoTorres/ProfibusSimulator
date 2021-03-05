/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.framestructure;

/**
 *
 * @author emossin
 */
public class Service {
    public static final byte PB_NULL                            = 0;
    public static final byte PB_REQ_TOKEN_PASS                  = 1;
    public static final byte PB_REQ_FDL_STATUS                  = 2;
    public static final byte PB_RES_FDL_STATUS                  = 3;
    public static final byte PB_REQ_DATA_EXCHANGE               = 4;
    public static final byte PB_RES_DATA_EXCHANGE               = 5;
    public static final byte PB_REQ_DATA_EXCHANGE_ONLY_INPUTS   = 6;
    public static final byte PB_RES_DATA_EXCHANGE_ONLY_OUTPUTS  = 7;
    public static final byte PB_REQ_GET_DIAG                    = 8;
    public static final byte PB_RES_GET_DIAG                    = 9;
    public static final byte PB_REQ_SET_PRM                     = 10;
    public static final byte PB_RES_SET_PRM                     = 11;
    public static final byte PB_REQ_CHK_CFG                     = 12;
    public static final byte PB_RES_CHK_CFG                     = 13;
    public static final byte PB_REQ_RD_INPUTS                   = 14;
    public static final byte PB_RES_RD_INPUTS                   = 15;
    public static final byte PB_RES_RD_INPUTS_EMPTY             = 16;
    public static final byte PB_REQ_RD_OUTPUTS                  = 17;
    public static final byte PB_RES_RD_OUTPUTS                  = 18;
    public static final byte PB_RES_RD_OUTPUTS_EMPTY            = 19;
    public static final byte PB_REQ_GET_CFG                     = 20;
    public static final byte PB_RES_GET_CFG                     = 21;
    public static final byte PB_SET_SLAVE_ADDR                  = 22;
    public static final byte PB_REQ_GLOBAL_CONTROL              = 23;

    private byte servicecode;
    byte SSAP;
    byte DSAP;

    public Service(byte ffunction) {
        this.servicecode = ffunction;
        function2SAP();
    }

    public Service(byte DestSAP, byte SourceSAP) {
        this.SSAP = SourceSAP;
        this.DSAP = DestSAP;
        SAP2function();
    }


    @Override
    public String toString() {
        switch( servicecode )
        {
            case PB_NULL:                            return "No Function";
            case PB_REQ_TOKEN_PASS:                  return "Token Passing";
            case PB_REQ_FDL_STATUS:                  return "Request FDL Status";
            case PB_RES_FDL_STATUS:                  return "FDL Status Response";
            case PB_REQ_DATA_EXCHANGE:               return "Data Exchange";
            case PB_RES_DATA_EXCHANGE:               return "Data Exchange";
            case PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:   return "Data Exchange";
            case PB_RES_DATA_EXCHANGE_ONLY_OUTPUTS:  return "Data Exchange";
            case PB_REQ_GET_DIAG:                    return "Get Diagnostics Request";
            case PB_RES_GET_DIAG:                    return "Slave Diagnostics Response";
            case PB_REQ_SET_PRM:                     return "Set Parameter Request";
            case PB_RES_SET_PRM:                     return "Set Parameter Response";
            case PB_REQ_CHK_CFG:                     return "Check Configuration Request";
            case PB_RES_CHK_CFG:                     return "Check Configuration Response";
            case PB_REQ_RD_INPUTS:                   return "Read Inputs Request";
            case PB_RES_RD_INPUTS:                   return "Read Inputs Response";
            case PB_REQ_RD_OUTPUTS:                  return "Read Outputs Request";
            case PB_RES_RD_OUTPUTS:                  return "Read Outputs Response";
            case PB_REQ_GET_CFG:                     return "Get Configuration Request";
            case PB_RES_GET_CFG:                     return "Get Configuration Response";
            case PB_SET_SLAVE_ADDR:                  return "Set Slave Address Request";
            case PB_REQ_GLOBAL_CONTROL:              return "Request Global Control";                
            default:                                 return "No Function";
        }
    }

    public boolean equals(byte ffunction) {
        boolean ret = false;
        if (this.servicecode == ffunction) {
            ret = true;
        }
        return ret;
    }

    public byte getService(){
        return this.servicecode;
    }


    /**
     * @param servicecode the servicecode to set
     */
    public void setFfunction(byte DestSAP, byte SourceSAP) {
        SSAP = SourceSAP;
        DSAP = DestSAP;
        
        SAP2function();
    }

    public byte getDSAP( )
    {
        return this.DSAP;
    }

    public byte getSSAP( )
    {
        return this.SSAP;
    }


    protected void function2SAP()
    {
        switch( servicecode )
        {
            case PB_NULL:
            case PB_REQ_TOKEN_PASS:
            case PB_REQ_FDL_STATUS:
            case PB_RES_FDL_STATUS:
            case PB_RES_SET_PRM:
            case PB_RES_CHK_CFG:
            case PB_REQ_DATA_EXCHANGE:
            case PB_RES_DATA_EXCHANGE:
            case PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
            case PB_RES_DATA_EXCHANGE_ONLY_OUTPUTS:
            default:
                SSAP = 0x00;
                DSAP = 0x00;
                break;

            case PB_REQ_GET_DIAG:   SSAP = 0x3E; DSAP = 0x3C; break;
            case PB_RES_GET_DIAG:   SSAP = 0x3C; DSAP = 0x3E; break;
            case PB_REQ_SET_PRM:    SSAP = 0x3E; DSAP = 0x3D; break;
            case PB_REQ_CHK_CFG:    SSAP = 0x3E; DSAP = 0x3E; break;
            case PB_REQ_RD_INPUTS:  SSAP = 0x3E; DSAP = 0x38; break;
            case PB_RES_RD_INPUTS:  SSAP = 0x38; DSAP = 0x3E; break;
            case PB_REQ_RD_OUTPUTS: SSAP = 0x3E; DSAP = 0x39; break;
            case PB_RES_RD_OUTPUTS: SSAP = 0x39; DSAP = 0x3E; break;
            case PB_REQ_GET_CFG:    SSAP = 0x3E; DSAP = 0x3B; break;
            case PB_RES_GET_CFG:    SSAP = 0x3B; DSAP = 0x3E; break;
            case PB_SET_SLAVE_ADDR: SSAP = 0x3E; DSAP = 0x37; break;
            case PB_REQ_GLOBAL_CONTROL: SSAP = 0x3E; DSAP = 0x3A; break;
        }
    }

    protected void SAP2function() {
        if( (SSAP == 0x00) || (DSAP == 0x00) ) {
            servicecode = PB_NULL;
        }
        else
        {
            if( SSAP == 0x3E ) {
                switch( DSAP ) {
                    case 0x3C: servicecode = PB_REQ_GET_DIAG;     break;
                    case 0x3D: servicecode = PB_REQ_SET_PRM;      break;
                    case 0x3E: servicecode = PB_REQ_CHK_CFG;      break;
                    case 0x38: servicecode = PB_REQ_RD_INPUTS;    break;
                    case 0x39: servicecode = PB_REQ_RD_OUTPUTS;   break;
                    case 0x3B: servicecode = PB_REQ_GET_CFG;      break;
                    case 0x37: servicecode = PB_SET_SLAVE_ADDR;   break;
                    case 0x3A: servicecode = PB_REQ_GLOBAL_CONTROL; break;
                }
            }
            else {
                switch( SSAP ) {
                    case 0x3C: servicecode = PB_RES_GET_DIAG;     break;
                    case 0x38: servicecode = PB_RES_RD_INPUTS;    break;
                    case 0x39: servicecode = PB_RES_RD_OUTPUTS;   break;
                    case 0x3B: servicecode = PB_RES_GET_CFG;      break;
                }
            }
        }
    }

    boolean isSAPFound() {
        return ( (SSAP != 0) && (DSAP != 0) );
    }

    void setFfunction(byte b) {
        servicecode = b;
        function2SAP();
    }

    void setSSAP(byte SourceSAP) {
        SSAP = SourceSAP;
        SAP2function();
    }

    void setDSAP(byte DestSAP ) {
        DSAP = DestSAP;
        SAP2function();
    }
}
