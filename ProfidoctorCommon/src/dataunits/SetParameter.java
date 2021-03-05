/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataunits;

/**
 *
 * @author renato_veiga
 */
public class SetParameter  extends GeneralDataUnit {
    //Octet 1
    private boolean reserved0;
    private boolean reserved1;
    private boolean reserved2;
    private boolean WATCHDOG;
    private boolean Freeze;
    private boolean Sync;
    private boolean Unlock;
    private boolean Lock;

    //Octet 2...7
    private byte WD_FACTOR1;
    private byte WD_FACTOR2;
    private byte minTSDR;
    private byte IdentNumber_High;
    private byte IdentNumber_Low;
    private byte Group_Ident;
    private byte[] Data_Unit;

    public SetParameter(  ) {
        
    }

    public SetParameter( byte[] DU) {
        Data_Unit = new byte[7];
        System.arraycopy(DU, 0,Data_Unit,0,(DU.length < 8) ? DU.length : 7 );
        Decode( Data_Unit );
    }

    byte[] Encode( ) {

        Data_Unit = new byte[7];

        //Octet 1
        Data_Unit[0] |= (isWATCHDOG() == true)  ? 0x08 : 0x00;
        Data_Unit[0] |= (isFreeze() == true)    ? 0x10 : 0x00;
        Data_Unit[0] |= (isSync() == true)      ? 0x20 : 0x00;
        Data_Unit[0] |= (isUnlock() == true)    ? 0x40 : 0x00;
        Data_Unit[0] |= (isLock() == true)      ? 0x80 : 0x00;

        //Octet 2...7
        Data_Unit[1] = getWD_FACTOR1();
        Data_Unit[2] = getWD_FACTOR2();
        Data_Unit[3] = getMinTSDR();
        Data_Unit[4] = getIdentNumber_High();
        Data_Unit[5] = getIdentNumber_Low();
        Data_Unit[6] = getGroup_Ident();

        return Data_Unit;
    }

    void Decode( byte[] Data_Unit ) {
        //Octet 1
        WATCHDOG = ((Data_Unit[0] & 0x08) != 0) ? true : false;
        Freeze   = ((Data_Unit[0] & 0x10) != 0) ? true : false;
        Sync     = ((Data_Unit[0] & 0x20) != 0) ? true : false;
        Unlock   = ((Data_Unit[0] & 0x40) != 0) ? true : false;
        Lock     = ((Data_Unit[0] & 0x80) != 0) ? true : false;

        //Octet 2..7
        WD_FACTOR1       = Data_Unit[1];
        WD_FACTOR2       = Data_Unit[2];
        minTSDR          = Data_Unit[3];
        IdentNumber_High = Data_Unit[4];
        IdentNumber_Low  = Data_Unit[5];
        Group_Ident      = Data_Unit[6];
    }

    /**
     * @return the reserved0
     */
    public boolean isReserved0() {
        return reserved0;
    }

    public int getIdentNumber() {
        return IdentNumber_High * 256 + IdentNumber_Low;
    }
    
    /**
     * @return the reserved1
     */
    public boolean isReserved1() {
        return reserved1;
    }

    /**
     * @return the reserved2
     */
    public boolean isReserved2() {
        return reserved2;
    }

    /**
     * @return the WATCHDOG
     */
    public boolean isWATCHDOG() {
        return WATCHDOG;
    }

    /**
     * @param WATCHDOG the WATCHDOG to set
     */
    public void setWATCHDOG(boolean WATCHDOG) {
        this.WATCHDOG = WATCHDOG;
    }

    /**
     * @return the Freeze
     */
    public boolean isFreeze() {
        return Freeze;
    }

    /**
     * @param Freeze the Freeze to set
     */
    public void setFreeze(boolean Freeze) {
        this.Freeze = Freeze;
    }

    /**
     * @return the Sync
     */
    public boolean isSync() {
        return Sync;
    }

    /**
     * @param Sync the Sync to set
     */
    public void setSync(boolean Sync) {
        this.Sync = Sync;
    }

    /**
     * @return the Unlock
     */
    public boolean isUnlock() {
        return Unlock;
    }

    /**
     * @param Unlock the Unlock to set
     */
    public void setUnlock(boolean Unlock) {
        this.Unlock = Unlock;
    }

    /**
     * @return the Lock
     */
    public boolean isLock() {
        return Lock;
    }

    /**
     * @param Lock the Lock to set
     */
    public void setLock(boolean Lock) {
        this.Lock = Lock;
    }

    /**
     * @return the WD_FACTOR1
     */
    public byte getWD_FACTOR1() {
        return WD_FACTOR1;
    }

    /**
     * @param WD_FACTOR1 the WD_FACTOR1 to set
     */
    public void setWD_FACTOR1(byte WD_FACTOR1) {
        this.WD_FACTOR1 = WD_FACTOR1;
    }

    /**
     * @return the WD_FACTOR2
     */
    public byte getWD_FACTOR2() {
        return WD_FACTOR2;
    }

    /**
     * @param WD_FACTOR2 the WD_FACTOR2 to set
     */
    public void setWD_FACTOR2(byte WD_FACTOR2) {
        this.WD_FACTOR2 = WD_FACTOR2;
    }

    /**
     * @return the minTSDR
     */
    public byte getMinTSDR() {
        return minTSDR;
    }

    /**
     * @param minTSDR the minTSDR to set
     */
    public void setMinTSDR(byte minTSDR) {
        this.minTSDR = minTSDR;
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

    /**
     * @param IdentNumber_Low the IdentNumber_Low to set
     */
    public void setIdentNumber_Low(byte IdentNumber_Low) {
        this.IdentNumber_Low = IdentNumber_Low;
    }

    /**
     * @return the Group_Ident
     */
    public byte getGroup_Ident() {
        return Group_Ident;
    }

    /**
     * @param Group_Ident the Group_Ident to set
     */
    public void setGroup_Ident(byte Group_Ident) {
        this.Group_Ident = Group_Ident;
    }


    public byte[] getData_Unit( ) {
        return Encode();
    }

}
