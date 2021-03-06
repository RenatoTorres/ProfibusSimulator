/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataunits;

/**
 *
 * @author renato_veiga
 */
public class SlaveDiagnostic extends GeneralDataUnit{

    private boolean Station_Non_Existent;
    private boolean Station_Not_Ready;
    private boolean Cfg_Fault;
    private boolean Ext_Diag;
    private boolean Not_Supported;
    private boolean Invalid_Slave_Response;
    private boolean Prm_Fault;
    private boolean Master_Lock;
    private boolean Prm_Req;
    private boolean Stat_Diag;
    private boolean DP;
    private boolean WD_On;
    private boolean Freeze_Mode;
    private boolean Sync_Mode;
    private boolean reserved1;
    private boolean Deactivated;
    private boolean reserved2[];
    private boolean Ext_Diag_Overflow;
    private byte Master_Addr;
    private byte IdentNumber_High;
    private byte IdentNumber_Low;
    public static final byte STATION_NON_EXISTENT = 0;
    public static final byte STATION_NOT_READY = 1;
    public static final byte CFG_FAULT = 2;
    public static final byte EXT_DIAG = 3;
    public static final byte NOT_SUPPORTED = 4;
    public static final byte INVALID_SLAVE_RESPONSE = 5;
    public static final byte PRM_FAULT = 6;
    public static final byte MASTER_LOCK = 7;
    public static final byte PRM_REQ = 8;
    public static final byte STAT_DIAG = 9;
    public static final byte DP_BIT = 10;
    public static final byte WD_ON = 11;
    public static final byte FREEZE_MODE = 12;
    public static final byte SYNC_MODE = 13;
    public static final byte RESERVED1 = 14;
    public static final byte DEACTIVATED = 15;
    public static final String[][] DiagBitDescription = {
        {"Station Non Existent",
            "Station not Ready",
            "Configuration Fault",
            "Extended Diagnostics Existent",
            "Not Supported",
            "Invalid Slave Response",
            "Parametrization Fault",
            "Master Lock",
            "Parametrization Request",
            "Static Diagnostic",
            "DP BIT",
            "Watchdog ON",
            "Slave is on freeze mode",
            "Slave is on sync mode",
            "Reserved",
            "Deatcivated"
        },
        { //Station Non Existent
            "This bit is set by the DP-Master if the respective DP-Slave can not be"
            + " reached over the line. If this bit is set the diagnostic bits contains"
            + " the state of the last diagnostic message or the initial value. The DP-Slave"
            + " sets this bit to zero",
            //Station not Ready
            "This bit is set by the DP-Slave if the DP-Slave is not yet ready for"
            + " data transfer",
            //Configuration Fault
            "This bit is set by the DP-Slave as soon as the last received"
            + " configuration data from the DP-master are different from these which"
            + " the DP-Slave has determined",
            //Extended Diagnostics Existent
            "This bit is set by the DP-Slave. It indicates that a diagnostic entry"
            + " exists in the slave specific diagnostic area (Ext_Diag_Data) if the"
            + " bit is set to one. If the bit is set to zero a status message can"
            + " exist in the slave specific diagnostic area (Ext_Diag_Data). The"
            + " meaning of this status message depends on the application and will not"
            + " be fixed in this specification",
            //Not Supported
            "This bit is set by the DP-Slave as soon as a function was requested"
            + " which is not supported from this DP-Slave",
            //Invalid Slave Response
            "This bit is set by the DP-Master as soon as receiving a not plausible"
            + " response from an addressed DP-Slave. The DP-Slave sets this bit to"
            + " zero",
            //Parametrization Fault
            "This bit is set by the DP-Slave if the last parameter frame was"
            + " faulty, e. g. wrong length, wrong Ident_Number, invalid parameters",
            //Master Lock
            "The DP-Slave has been parameterized from another master. This bit is"
            + " set by the DP-Master (class 1), if the address in octet 4 is different"
            + " from 255 and different from the own address. The DP-Slave sets this"
            + " bit to zero",
            //Parametrization Request
            "If the DP-Slave sets this bit the respective DP-Slave shall be"
            + " reparameterized and reconfigured. The bit remains set until"
            + " parameterization is finished. This bit is set by the DP-Slave",
            //Static Diagnostic
            "If the DP-Slave sets this bit the DP-Master shall fetch diagnostic"
            + " informations as long as this bit is reset again. For Example, the "
            + " DP-Slave sets this bit if it is not able to provide valid user data",
            //DP BIT
            "This bit is set to 1 by the DP-Slave",
            //Watchdog ON
            "This bit is set by the DP-Slave as soon as his watchdog control has"
            + " been activated",
            //Slave is on freeze mode
            "This bit is set by the DP-Slave as soon as the respective DP-Slave has"
            + " received the Freeze control command",
            //Slave is on sync mode
            "This bit is set by the DP-Slave as soon as the respective DP-Slave has"
            + " received the Sync control command",
            //reserved
            "This bit is ignored in the Protocol.",
            //Deatcivated
            "This bit is set by the DP-Master as soon as the DP-Slave has been"
            + " marked inactive within the DP-Slave parameter set and has been removed"
            + " from cyclic processing. The DP-Slave sets this bit always to zero"
        }
    };

    public SlaveDiagnostic() {
        Station_Non_Existent = false;
        Station_Not_Ready = true;
        Cfg_Fault = false;
        Ext_Diag = false;
        Not_Supported = false;
        Invalid_Slave_Response = false;
        Prm_Fault = false;
        Master_Lock = false;

        Prm_Req = true;
        Stat_Diag = false;
        DP = true;
        WD_On = false;
        Freeze_Mode = false;
        Sync_Mode = false;
        reserved1 = false;
        Deactivated = false;

        reserved2 = new boolean[7];

        Ext_Diag_Overflow = false;

        Master_Addr = (byte) 0xff;

        IdentNumber_High = 0x00;
        IdentNumber_Low = 0x00;
    }

    public SlaveDiagnostic(byte[] Data_Unit) {
        this();
        Decode(Data_Unit);
    }

    public byte[] getBytes() {
        byte Data_Unit[] = new byte[6];

        //Octet 1
        Data_Unit[0] |= (isStation_Non_Existent() == true) ? 0x01 : 0x00;
        Data_Unit[0] |= (isStation_Not_Ready() == true) ? 0x02 : 0x00;
        Data_Unit[0] |= (isCfg_Fault() == true) ? 0x04 : 0x00;
        Data_Unit[0] |= (isExt_Diag() == true) ? 0x08 : 0x00;
        Data_Unit[0] |= (isNot_Supported() == true) ? 0x10 : 0x00;
        Data_Unit[0] |= (isInvalid_Slave_Response() == true) ? 0x20 : 0x00;
        Data_Unit[0] |= (isPrm_Fault() == true) ? 0x40 : 0x00;
        Data_Unit[0] |= (isMaster_Lock() == true) ? 0x80 : 0x00;

        //Octet 2
        Data_Unit[1] |= (isPrm_Req() == true) ? 0x01 : 0x00;
        Data_Unit[1] |= (isStat_Diag() == true) ? 0x02 : 0x00;
        Data_Unit[1] |= (isDP() == true) ? 0x04 : 0x00;
        Data_Unit[1] |= (isWD_On() == true) ? 0x08 : 0x00;
        Data_Unit[1] |= (isFreeze_Mode() == true) ? 0x10 : 0x00;
        Data_Unit[1] |= (isSync_Mode() == true) ? 0x20 : 0x00;
        Data_Unit[1] |= (isDeactivated() == true) ? 0x80 : 0x00;

        //Octet 3
        Data_Unit[2] |= (isExt_Diag_Overflow() == true) ? 0x80 : 0x00;

        //Octet 4
        Data_Unit[3] = getMaster_Addr();

        //Octet 5 and 6
        Data_Unit[4] = getIdentNumber_High();
        Data_Unit[5] = getIdentNumber_Low();

        return Data_Unit;
    }

    public void setBits(byte[] bits) {
        if (bits.length >= 2) {
            //Octet 1
            Station_Non_Existent = ((bits[0] & 0x01) != 0) ? true : false;
            Station_Not_Ready = ((bits[0] & 0x02) != 0) ? true : false;
            Cfg_Fault = ((bits[0] & 0x04) != 0) ? true : false;
            Ext_Diag = ((bits[0] & 0x08) != 0) ? true : false;
            Not_Supported = ((bits[0] & 0x10) != 0) ? true : false;
            Invalid_Slave_Response = ((bits[0] & 0x20) != 0) ? true : false;
            Prm_Fault = ((bits[0] & 0x40) != 0) ? true : false;
            Master_Lock = ((bits[0] & 0x80) != 0) ? true : false;

            //Octet 2
            Prm_Req = ((bits[1] & 0x01) != 0) ? true : false;
            Stat_Diag = ((bits[1] & 0x02) != 0) ? true : false;
            DP = ((bits[1] & 0x04) != 0) ? true : false;
            WD_On = ((bits[1] & 0x08) != 0) ? true : false;
            Freeze_Mode = ((bits[1] & 0x10) != 0) ? true : false;
            Sync_Mode = ((bits[1] & 0x20) != 0) ? true : false;
            reserved1 = ((bits[1] & 0x40) != 0) ? true : false;
            Deactivated = ((bits[1] & 0x80) != 0) ? true : false;
        }
    }

    public boolean IsSelectedBit(int bitoffset, int byteoffset) {
        byte[] du = getBytes();
        boolean bret = false;
        if (byteoffset < 2 && bitoffset < 8) {
            bret = IsSelectedBit(du[ byteoffset], bitoffset);
        }

        return bret;
    }

    protected boolean IsSelectedBit(byte b, int pos) {
        return ((b & (0x01 << pos)) != 0);
    }

    protected int getNumSelectedBits() {
        byte[] du = getBytes();
        int iret = 0;

        for (int i = 0; i < 8; i++) {
            if (IsSelectedBit(du[0], i)) {
                iret++;
            }

            if (IsSelectedBit(du[1], i)) {
                iret++;
            }
        }

        return iret;
    }

    void Decode(byte[] Data_Unit) {
        //Octet 1
        Station_Non_Existent = ((Data_Unit[0] & 0x01) != 0) ? true : false;
        Station_Not_Ready = ((Data_Unit[0] & 0x02) != 0) ? true : false;
        Cfg_Fault = ((Data_Unit[0] & 0x04) != 0) ? true : false;
        Ext_Diag = ((Data_Unit[0] & 0x08) != 0) ? true : false;
        Not_Supported = ((Data_Unit[0] & 0x10) != 0) ? true : false;
        Invalid_Slave_Response = ((Data_Unit[0] & 0x20) != 0) ? true : false;
        Prm_Fault = ((Data_Unit[0] & 0x40) != 0) ? true : false;
        Master_Lock = ((Data_Unit[0] & 0x80) != 0) ? true : false;

        //Octet 2
        Prm_Req = ((Data_Unit[1] & 0x01) != 0) ? true : false;
        Stat_Diag = ((Data_Unit[1] & 0x02) != 0) ? true : false;
        DP = ((Data_Unit[1] & 0x04) != 0) ? true : false;
        WD_On = ((Data_Unit[1] & 0x08) != 0) ? true : false;
        Freeze_Mode = ((Data_Unit[1] & 0x10) != 0) ? true : false;
        Sync_Mode = ((Data_Unit[1] & 0x20) != 0) ? true : false;
        reserved1 = ((Data_Unit[1] & 0x40) != 0) ? true : false;
        Deactivated = ((Data_Unit[1] & 0x80) != 0) ? true : false;

        //Octet 3
        if ((Data_Unit[1] & 0x7F) != 0x00) {
            reserved2[0] = ((Data_Unit[2] & 0x01) != 0) ? true : false;
            reserved2[1] = ((Data_Unit[2] & 0x02) != 0) ? true : false;
            reserved2[2] = ((Data_Unit[2] & 0x04) != 0) ? true : false;
            reserved2[3] = ((Data_Unit[2] & 0x08) != 0) ? true : false;
            reserved2[4] = ((Data_Unit[2] & 0x10) != 0) ? true : false;
            reserved2[5] = ((Data_Unit[2] & 0x20) != 0) ? true : false;
            reserved2[6] = ((Data_Unit[2] & 0x40) != 0) ? true : false;
        }
        Ext_Diag_Overflow = ((Data_Unit[2] & 0x80) != 0) ? true : false;

        //Octet 4
        Master_Addr = Data_Unit[3];

        //Octet 5 and Octet 6
        IdentNumber_High = Data_Unit[4];
        IdentNumber_Low = Data_Unit[5];
    }

    protected boolean IsSelected(int bitnum) {
        boolean bbitVal = false;

        switch (bitnum) {
            case STATION_NON_EXISTENT:
                bbitVal = Station_Non_Existent;
                break; //critical
            case STATION_NOT_READY:
                bbitVal = Station_Not_Ready;
                break; //critical
            case CFG_FAULT:
                bbitVal = Cfg_Fault;
                break; //critical
            case EXT_DIAG:
                bbitVal = Ext_Diag;
                break; //critical
            case NOT_SUPPORTED:
                bbitVal = Not_Supported;
                break; //critical
            case INVALID_SLAVE_RESPONSE:
                bbitVal = Invalid_Slave_Response;
                break; //critical
            case PRM_FAULT:
                bbitVal = Prm_Fault;
                break; //critical
            case MASTER_LOCK:
                bbitVal = Master_Lock;
                break; //information

            case PRM_REQ:
                bbitVal = Prm_Req;
                break; //critical
            case STAT_DIAG:
                bbitVal = Stat_Diag;
                break; //warning
            case DP_BIT:
                bbitVal = DP;
                break; //ignore
            case WD_ON:
                bbitVal = WD_On;
                break; //info
            case FREEZE_MODE:
                bbitVal = Freeze_Mode;
                break; //info
            case SYNC_MODE:
                bbitVal = Sync_Mode;
                break; //info
            case RESERVED1:
                bbitVal = reserved1;
                break; //ignore
            case DEACTIVATED:
                bbitVal = Deactivated;
                break; //info
        }




        return bbitVal;
    }

    /**
     * @return the Station_Non_Existent
     */
    public boolean isStation_Non_Existent() {
        return Station_Non_Existent;
    }

    /**
     * @param Station_Non_Existent the Station_Non_Existent to set
     */
    public void setStation_Non_Existent(boolean Station_Non_Existent) {
        this.Station_Non_Existent = Station_Non_Existent;
    }

    /**
     * @return the Station_Not_Ready
     */
    public boolean isStation_Not_Ready() {
        return Station_Not_Ready;
    }

    /**
     * @param Station_Not_Ready the Station_Not_Ready to set
     */
    public void setStation_Not_Ready(boolean Station_Not_Ready) {
        this.Station_Not_Ready = Station_Not_Ready;
    }

    /**
     * @return the Cfg_Fault
     */
    public boolean isCfg_Fault() {
        return Cfg_Fault;
    }

    /**
     * @param Cfg_Fault the Cfg_Fault to set
     */
    public void setCfg_Fault(boolean Cfg_Fault) {
        this.Cfg_Fault = Cfg_Fault;
    }

    /**
     * @return the Ext_Diag
     */
    public boolean isExt_Diag() {
        return Ext_Diag;
    }

    /**
     * @param Ext_Diag the Ext_Diag to set
     */
    public void setExt_Diag(boolean Ext_Diag) {
        this.Ext_Diag = Ext_Diag;
    }

    /**
     * @return the Not_Supported
     */
    public boolean isNot_Supported() {
        return Not_Supported;
    }

    /**
     * @param Not_Supported the Not_Supported to set
     */
    public void setNot_Supported(boolean Not_Supported) {
        this.Not_Supported = Not_Supported;
    }

    /**
     * @return the Invalid_Slave_Response
     */
    public boolean isInvalid_Slave_Response() {
        return Invalid_Slave_Response;
    }

    /**
     * @param Invalid_Slave_Response the Invalid_Slave_Response to set
     */
    public void setInvalid_Slave_Response(boolean Invalid_Slave_Response) {
        this.Invalid_Slave_Response = Invalid_Slave_Response;
    }

    /**
     * @return the Prm_Fault
     */
    public boolean isPrm_Fault() {
        return Prm_Fault;
    }

    /**
     * @param Prm_Fault the Prm_Fault to set
     */
    public void setPrm_Fault(boolean Prm_Fault) {
        this.Prm_Fault = Prm_Fault;
    }

    /**
     * @return the Master_Lock
     */
    public boolean isMaster_Lock() {
        return Master_Lock;
    }

    /**
     * @param Master_Lock the Master_Lock to set
     */
    public void setMaster_Lock(boolean Master_Lock) {
        this.Master_Lock = Master_Lock;
    }

    /**
     * @return the Prm_Req
     */
    public boolean isPrm_Req() {
        return Prm_Req;
    }

    /**
     * @param Prm_Req the Prm_Req to set
     */
    public void setPrm_Req(boolean Prm_Req) {
        this.Prm_Req = Prm_Req;
    }

    /**
     * @return the Stat_Diag
     */
    public boolean isStat_Diag() {
        return Stat_Diag;
    }

    /**
     * @param Stat_Diag the Stat_Diag to set
     */
    public void setStat_Diag(boolean Stat_Diag) {
        this.Stat_Diag = Stat_Diag;
    }

    /**
     * @return the DP
     */
    public boolean isDP() {
        return DP;
    }

    /**
     * @param DP the DP to set
     */
    public void setDP(boolean DP) {
        this.DP = DP;
    }

    /**
     * @return the WD_On
     */
    public boolean isWD_On() {
        return WD_On;
    }

    /**
     * @param WD_On the WD_On to set
     */
    public void setWD_On(boolean WD_On) {
        this.WD_On = WD_On;
    }

    /**
     * @return the Freeze_Mode
     */
    public boolean isFreeze_Mode() {
        return Freeze_Mode;
    }

    /**
     * @param Freeze_Mode the Freeze_Mode to set
     */
    public void setFreeze_Mode(boolean Freeze_Mode) {
        this.Freeze_Mode = Freeze_Mode;
    }

    /**
     * @return the Sync_Mode
     */
    public boolean isSync_Mode() {
        return Sync_Mode;
    }

    /**
     * @param Sync_Mode the Sync_Mode to set
     */
    public void setSync_Mode(boolean Sync_Mode) {
        this.Sync_Mode = Sync_Mode;
    }

    /**
     * @return the Deactivated
     */
    public boolean isDeactivated() {
        return Deactivated;
    }

    /**
     * @param Deactivated the Deactivated to set
     */
    public void setDeactivated(boolean Deactivated) {
        this.Deactivated = Deactivated;
    }

    /**
     * @return the Ext_Diag_Overflow
     */
    public boolean isExt_Diag_Overflow() {
        return Ext_Diag_Overflow;
    }

    /**
     * @param Ext_Diag_Overflow the Ext_Diag_Overflow to set
     */
    public void setExt_Diag_Overflow(boolean Ext_Diag_Overflow) {
        this.Ext_Diag_Overflow = Ext_Diag_Overflow;
    }

    /**
     * @return the Diag_Master_Addr
     */
    public byte getDiag_Master_Addr() {
        return Master_Addr;
    }

    /**
     * @param Diag_Master_Addr the Diag_Master_Addr to set
     */
    public void setDiag_Master_Addr(byte Diag_Master_Addr) {
        this.Master_Addr = Diag_Master_Addr;
    }

    /**
     * @return the reserved1
     */
    public boolean isReserved1() {
        return reserved1;
    }

    /**
     * @param reserved1 the reserved1 to set
     */
    public void setReserved1(boolean reserved1) {
        this.reserved1 = reserved1;
    }

    /**
     * @return the reserved2
     */
    public boolean[] getReserved2() {
        return reserved2;
    }

    /**
     * @param reserved2 the reserved2 to set
     */
    public void setReserved2(boolean[] reserved2) {
        this.reserved2 = reserved2;
    }

    /**
     * @return the Master_Addr
     */
    public byte getMaster_Addr() {
        return Master_Addr;
    }
    
    /**
     * @return the Master_Addr
     */
    public int getMaster_Addr_Int() {
        return (Master_Addr < 0 ? 256+Master_Addr : Master_Addr);
    }

    /**
     * @param Master_Addr the Master_Addr to set
     */
    public void setMaster_Addr(byte Master_Addr) {
        this.Master_Addr = Master_Addr;
    }

    /**
     * @return the IdentNumber_High
     */
    public byte getIdentNumber_High() {
        return IdentNumber_High;
    }

    /**
     * @param IdentNumber_High the IdentNumber_High to set
     */
    public void setIdentNumber_High(byte IdentNumber_High) {
        this.IdentNumber_High = IdentNumber_High;
    }

    /**
     * @return the IdentNumber_Low
     */
    public byte getIdentNumber_Low() {
        return IdentNumber_Low;
    }
    
    
    public int getIdentNumber() {
        return (IdentNumber_High < 0 ? 256+IdentNumber_High : IdentNumber_High)*256 + (IdentNumber_Low < 0 ? 256+IdentNumber_Low : IdentNumber_Low);
    }

    /**
     * @param IdentNumber_Low the IdentNumber_Low to set
     */
    public void setIdentNumber_Low(byte IdentNumber_Low) {
        this.IdentNumber_Low = IdentNumber_Low;
    }

    public String getBitShortDescription(byte bitCode) {
        String ret = new String();
        ret = DiagBitDescription[0][bitCode];
        return ret;
    }

    public String getBitDescription(byte bitCode) {
        String ret = new String();
        ret = DiagBitDescription[1][bitCode];
        return ret;
    }

    
    public String[][] getStringDescription() {
        byte[] du = getBytes();
        int k = 0;
        String[][] sDesc = new String[getNumSelectedBits()][2];
        for (int i = 0; i < 8; i++) {
            if (IsSelectedBit(du[0], i)) {
                sDesc[ k ][ 0] = DiagBitDescription[0][i];
                sDesc[ k ][ 1] = DiagBitDescription[1][i];
                k++;
            }
        }

        for (int i = 0; i < 8; i++) {
            if (IsSelectedBit(du[1], i)) {
                sDesc[ k ][ 0] = DiagBitDescription[0][8+i];
                sDesc[ k ][ 1] = DiagBitDescription[1][8+i];
                k++;
            }
        }

        return sDesc;
    }
    
    public String toHTML( )
    {
        String sret = new String();
        String sFilled[][] = getStringDescription();
        
        for(int i=0; i < sFilled.length; i++)
        {
            sret += sFilled[i][0]+" = 1<BR>";
        }
        
        return sret;
    }
}
