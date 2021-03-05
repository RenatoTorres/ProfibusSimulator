/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

import common.ConversionsAdvanced;
import common.framestructure.FC;
import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import common.framestructure.Service;
import common.signals.SignalType;
import dataunits.CheckConfiguration;
import dataunits.SetParameter;
import dataunits.SlaveDiagnostic;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusSlave {

    public static final int PROFIBUS_SLAVE_STATE_POWER_UP  = 0;
    public static final int PROFIBUS_SLAVE_STATE_WAIT_PRM  = 1;
    public static final int PROFIBUS_SLAVE_STATE_WAIT_CFG  = 2;
    public static final int PROFIBUS_SLAVE_STATE_DATA_EXCH = 3;
    
    protected int   address;
    protected byte  state;
    protected byte  user_parameter_data;
    private boolean ActiveOnCfg;
    ProfibusFrame PBResFrame;
    private ProfibusFrame resFrame;
    private byte current_state = PROFIBUS_SLAVE_STATE_POWER_UP;
    SlaveDiagnostic diag;
    CheckConfiguration cfg_configured;
    private byte[] Cfg_Data_From_Master;
    private byte[] Cfg_Data_Native;
    private byte[] Prm_Data_From_Master;
    private long CurrentMaxTsdr = 10000;       //10 TBit
    private boolean bAlreadyInCommunicationFCV_FCB = false;
    private boolean bLastFCB = false;
    private boolean Diag_Flag = false;
    protected int input_size = 0;
    protected int output_size = 0;
    
    //Diagnosis and Physical problems support
    public final static int DEFAULT_VOLTAGE = 5;
    protected int iVoltage = DEFAULT_VOLTAGE;
    protected boolean bReadableSignal = true;
    
    private int iPhyProblem = SignalType.SIGNAL_NO_PROBLEM;
    private boolean bSlaveOnConfiguration = false;
    private boolean bMasterUseLastFCB;
    int retries = 0;
    private byte state_in_master = Service.PB_REQ_GET_DIAG;
    private int Master_Configurator_Addr = 255;


    public ProfibusSlave(int i )
    {
        address = i;
        Cfg_Data_Native = new byte[2];
        diag = new SlaveDiagnostic();
        diag.setIdentNumber_High( (byte) 0x00 );
        diag.setIdentNumber_Low( (byte) 0x00 );
        
        this.restart( );
    }


    public void restart( )
    {
        diag.setCfg_Fault( false );
        diag.setDP( true );
        diag.setDeactivated( false );
        diag.setDiag_Master_Addr( (byte) 255 );
        diag.setExt_Diag( false );
        diag.setExt_Diag_Overflow( false );
        diag.setFreeze_Mode( false );
        diag.setInvalid_Slave_Response( false );
        diag.setMaster_Addr( (byte) 255 );
        diag.setMaster_Lock( false );
        diag.setNot_Supported( false );
        diag.setPrm_Fault( false );
        diag.setPrm_Req( true);
        diag.setReserved1( false );
        diag.setStat_Diag( false );
        diag.setStation_Non_Existent( true );
        diag.setStation_Not_Ready( true );
        diag.setSync_Mode( false );
        diag.setWD_On( false );
        
        bAlreadyInCommunicationFCV_FCB = false;
        bLastFCB = false;
        current_state = PROFIBUS_SLAVE_STATE_POWER_UP;
    }

    /**
     * Get the value of address
     *
     * @return the value of address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Set the value of address
     *
     * @param address new value of address
     */
    public void setAddress(int address) {
        this.address = address;
    }

    public void run()
    {
        System.out.print("Starting Slave... \n");
    }

    /**
     * @return the ActiveOnCfg
     */
    public boolean isActiveOnCfg() {
        return ActiveOnCfg;
    }

    /**
     * @param ActiveOnCfg the ActiveOnCfg to set
     */
    public void setActiveOnCfg(boolean ActiveOnCfg) {
        this.ActiveOnCfg = ActiveOnCfg;
    }

    /**
     * @return the Online
     */
    public boolean isOnline() {
        return !diag.isStation_Non_Existent();
    }

    /**
     * @param Online the Online to set
     */
    public void setOnline(boolean Online) {
        diag.setStation_Non_Existent(  !(Online) );
    }

    public void ReceiveRequest(ProfibusFrame fr)
    {
        Service srv;
        
        //For test
        if( (diag.isStation_Non_Existent() == false) && (bReadableSignal == true) )
        {
            if( fr.getFrameService() == Service.PB_REQ_FDL_STATUS )
            {
                resFrame = ProfibusServices.SendResponseFDLStatus(address, fr.getSourceAddr() );
                /* bug in ProfibusServices */
                resFrame.setFC(FC.FC_ACK_POS);
            }
            else
            {
                switch( current_state )
                {
                    case PROFIBUS_SLAVE_STATE_POWER_UP:
                    case PROFIBUS_SLAVE_STATE_WAIT_PRM:
                        current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                        diag.setPrm_Req(true);
                        diag.setWD_On(false);
                        diag.setStation_Not_Ready(true);
                        Master_Configurator_Addr = 255;
                        switch( fr.getFrameService() ) 
                        {
                            case Service.PB_REQ_GET_DIAG:
                                srv = new Service( Service.PB_RES_GET_DIAG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( diag.getBytes() );
                                Diag_Flag = false;
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                break;
                            case Service.PB_REQ_GET_CFG:
                                srv = new Service( Service.PB_RES_GET_CFG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( Cfg_Data_Native );
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                break;
                            case Service.PB_REQ_SET_PRM:
                                resFrame = ProfibusServices.SendSCResponse( );
                                Prm_Data_From_Master = fr.getData_Unit();
                                Master_Configurator_Addr = fr.getSourceAddr();
                                if( VerifySetPrm( ) == true )
                                {
                                    diag.setPrm_Fault(false);
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_CFG;
                                }
                                else
                                {
                                    diag.setPrm_Fault(true);
                                    diag.setPrm_Req(true);
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;                                    
                                }
                                break;
                            case Service.PB_SET_SLAVE_ADDR:
                                resFrame = ProfibusServices.SendSCResponse( );
                                break;
                                
                            case Service.PB_REQ_CHK_CFG:    
                            case Service.PB_REQ_RD_INPUTS:    
                            case Service.PB_REQ_RD_OUTPUTS:
                            case Service.PB_REQ_DATA_EXCHANGE:
                            case Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
                                //Must send RS
                                resFrame = ProfibusServices.SendResponseFDLStatus(address, fr.getSourceAddr() );
                                resFrame.setFC(FC.FC_ACK_NEG_NO_SERVICE_ACTIVATED);
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                break;
                        }
                        break;
                        
                    case PROFIBUS_SLAVE_STATE_WAIT_CFG:    
                        diag.setStation_Not_Ready( true );
                        switch( fr.getFrameService() ) 
                        {
                            case Service.PB_REQ_GET_DIAG:
                                srv = new Service( Service.PB_RES_GET_DIAG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( diag.getBytes() );
                                Diag_Flag = false;
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_CFG;
                                break;
                            case Service.PB_REQ_GET_CFG:
                                srv = new Service( Service.PB_RES_GET_CFG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( Cfg_Data_Native );
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_CFG;
                                break;
                            case Service.PB_REQ_SET_PRM:
                                resFrame = ProfibusServices.SendSCResponse( );
                                Master_Configurator_Addr = resFrame.getSourceAddr();
                                if( VerifySetPrm( ) == true )
                                {
                                    diag.setPrm_Fault(false);
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_CFG;
                                }
                                else
                                {
                                    diag.setPrm_Fault(true);
                                    diag.setPrm_Req(true);
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                }
                                break;
                            case Service.PB_REQ_CHK_CFG:
                                resFrame = ProfibusServices.SendSCResponse( );
                                Cfg_Data_From_Master = fr.getData_Unit();
                                if( VerifyCheckCfg( ) == true )
                                {
                                    diag.setPrm_Req(false);                                    
                                    diag.setCfg_Fault(false);
                                    diag.setStation_Not_Ready(false);
                                    current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                }
                                else
                                {
                                    diag.setCfg_Fault( true );
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;    
                                }
                                break;
                                
                            case Service.PB_REQ_RD_INPUTS:    
                            case Service.PB_REQ_RD_OUTPUTS:
                            case Service.PB_REQ_DATA_EXCHANGE:
                            case Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
                                //Must send RS
                                //Must send RS
                                resFrame = ProfibusServices.SendResponseFDLStatus(address, fr.getSourceAddr() );
                                resFrame.setFC(FC.FC_ACK_NEG_NO_SERVICE_ACTIVATED);
                                current_state = PROFIBUS_SLAVE_STATE_WAIT_CFG;
                                break;
                            
                            case Service.PB_SET_SLAVE_ADDR:
                                resFrame = ProfibusServices.SendSCResponse( );
                                break;
                        }
                        break;
                        
                    case PROFIBUS_SLAVE_STATE_DATA_EXCH:    
                        switch( fr.getFrameService() ) {
                            case Service.PB_REQ_GET_DIAG:
                                srv = new Service( Service.PB_RES_GET_DIAG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( diag.getBytes() );
                                Diag_Flag = false;
                                if( !diag.isCfg_Fault() )
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;    
                                }
                                else
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;                                    
                                }
                                break;

                            case Service.PB_REQ_GET_CFG:
                                srv = new Service( Service.PB_RES_GET_CFG );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( Cfg_Data_Native );
                                current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                break;

                            case Service.PB_REQ_SET_PRM:
                                resFrame = ProfibusServices.SendSCResponse( );
                                Master_Configurator_Addr = resFrame.getSourceAddr();
                                if( VerifySetPrm( ) == true )
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                }
                                else
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                    diag.setPrm_Fault(true);
                                    diag.setPrm_Req(true);
                                }
                                break;

                            case Service.PB_REQ_CHK_CFG:
                                resFrame = ProfibusServices.SendSCResponse( );
                                diag.setPrm_Req(false);
                                diag.setStation_Not_Ready(false);
                                Cfg_Data_From_Master = fr.getData_Unit();
                                if( VerifyCheckCfg( ) == true )
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                }
                                else
                                {
                                    current_state = PROFIBUS_SLAVE_STATE_WAIT_PRM;
                                    diag.setPrm_Fault(true);
                                    diag.setCfg_Fault(true);
                                }
                                break;

                            case Service.PB_REQ_RD_INPUTS:
                                srv = new Service( Service.PB_RES_RD_INPUTS );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( new byte[2] );
                                current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                break;

                            case Service.PB_REQ_RD_OUTPUTS:
                                srv = new Service( Service.PB_RES_RD_OUTPUTS );
                                resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                resFrame.setData_Unit( new byte[2] );
                                current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                break;

                            case Service.PB_REQ_DATA_EXCHANGE:
                            case Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
                                if( input_size == 0)
                                {
                                    resFrame = ProfibusServices.SendSCResponse( );
                                }
                                else
                                {
                                    srv = new Service( Service.PB_RES_DATA_EXCHANGE );
                                    resFrame = ProfibusServices.SendServiceRequest(this.address, fr.getSourceAddr(), srv);
                                    if( Diag_Flag == true )
                                    {
                                        resFrame.setPriorityResponseHigh( );
                                    }

                                    resFrame.setData_Unit( new byte[ input_size ] );
                                }
                                
                                current_state = PROFIBUS_SLAVE_STATE_DATA_EXCH;
                                break;

                            case Service.PB_SET_SLAVE_ADDR:
                                resFrame = ProfibusServices.SendSCResponse( );
                                break;
                        }
                        break;
                }
                bAlreadyInCommunicationFCV_FCB = true;
                bLastFCB = fr.getFCB();
                resFrame.setReadableFrame(bReadableSignal);
                resFrame.setSignalType( iPhyProblem );
                resFrame.setVoltageLevel( iVoltage );
            }
        }
        else 
        {
            resFrame = null;
            restart();
        }

    }

    public ProfibusFrame SendResponse( )
    {
        return resFrame;
    }

    public String toString( )
    {
        if(resFrame!= null)
            return resFrame.toString();
        else
            return null;
    }

    public byte getSlaveState( ) {
        return current_state;
    }

    protected void setSlaveState( byte ucval ) {
        current_state = ucval;
    }

    public void setNativeCfg(byte[] bytearray) {
        Cfg_Data_Native = bytearray;
    }

    public byte[] getNativeCfg( ) {
        return Cfg_Data_Native;
    }


    long getCurrentMaxTsdr() {
        return CurrentMaxTsdr;
    }

    private boolean VerifySetPrm( ) {
        SetParameter pr = new SetParameter( Prm_Data_From_Master );
        boolean bconfigurationOK = true;
        
        if( ( pr.getIdentNumber_High() == getIdentNumberHigh() ) && ( pr.getIdentNumber_Low() == getIdentNumberLow() ) )
        {
            diag.setMaster_Addr( (byte) Master_Configurator_Addr );    
            diag.setWD_On(pr.isWATCHDOG());
        }
        else
        {
            bconfigurationOK = false;
        }
       
        //TODO: For test (use IDENT from SetParameter)
        //diag.setIdentNumber_High( pr.getIdentNumber_High() );
        //diag.setIdentNumber_Low( pr.getIdentNumber_Low() );
        return bconfigurationOK;
    }

    public void setIdentNumber(byte[] ByteArray) {
        diag.setIdentNumber_High( ByteArray[0] );
        diag.setIdentNumber_Low( ByteArray[1] );
    }
    
    public int getIdentNumber( )
    {
        return diag.getIdentNumber();
    }
    
    public byte[] getIdentNumberBytes( )
    {
        byte[] Bret = new byte[2];
        Bret[0] = diag.getIdentNumber_High();
        Bret[1] = diag.getIdentNumber_Low();
        
        return Bret;
    }
    
    public byte getIdentNumberHigh( ) {
        return diag.getIdentNumber_High( );
    }

    public byte getIdentNumberLow( ) {
        return diag.getIdentNumber_Low( );
    }

    private boolean VerifyCheckCfg( ) {
        byte[] b_native = getNativeCfg();
        boolean bconfigurationOK = true;
        
        if( b_native.length == Cfg_Data_From_Master.length )
        {
            for(int i=0; i < b_native.length; i++) {
                if( Cfg_Data_From_Master[i] != b_native[i] )
                {
                    bconfigurationOK = false;
                    break;
                }
            }
        }
        
        if( bconfigurationOK == true )
        {
            cfg_configured = new CheckConfiguration( Cfg_Data_From_Master );
            input_size = cfg_configured.getCfgInputSize();
            output_size = cfg_configured.getCfgOutputSize();
        }
        
        return bconfigurationOK;
    }
    
    public byte[] getDiagBits( )
    {
        return diag.getBytes();
    }
    
    public void setDiagBits(byte[] diagdata)
    {
        diag.setBits(diagdata);
    }

    public void triggerNewDiag() 
    {
        Diag_Flag = true;
    }

    boolean isAlreadyInCommunication() {
        return bAlreadyInCommunicationFCV_FCB;
    }

    void setLastFCB( boolean b )
    {
        bLastFCB = b;
    }
    
    boolean getLastFCB() {
        return bLastFCB;
    }

    public byte getMasterConfigurator() {
        return diag.getMaster_Addr();
    }

    public void setVoltage(int newvalue) {
        iVoltage = newvalue;
    }
    
    public int getVoltage( ) {
        return iVoltage;
    }
    
    public void setReadableSignal(boolean sig ) {
        bReadableSignal = sig;
        if( bReadableSignal == false )
        {
            restart();
        }
    }

    boolean isReadableSignal( ) {
        return bReadableSignal;
    }

    int getSlavePhyProblem(int address) {
        return iPhyProblem;
    }

    void setSlavePhyProblem(int problem) {
        iPhyProblem = problem;
    }

    void setExtendedDiagSize(int extdiagsize) {
        
    }

    void setAlreadyInCommunication(boolean b) {
        bAlreadyInCommunicationFCV_FCB = b;
    }

    void setMasterUseLastFCB(boolean b) {
        bMasterUseLastFCB = b;
    }

    boolean getMasterUseLastFCB() {
        return bMasterUseLastFCB;
    }
    
    public String getIdentNumberString( ) {
        return ConversionsAdvanced.toStringFromStream( getIdentNumberBytes() );
    }
    
    public String getCfgString( ) {
        String r = ConversionsAdvanced.toStringFromStream( getNativeCfg() );
        return r;
    }

    byte getSlaveStateInMaster() {
        return state_in_master;
    }

    void setSlaveStateInMaster(byte pstate_in_master) {
        state_in_master = pstate_in_master;
    }

    public void OnChangeConfiguration(String SConfig) {
        setNativeCfg( ConversionsAdvanced.hexStringToByteArray(SConfig) );
        if( VerifyCheckCfg( ) == false )
        {
            diag.setCfg_Fault( true );
            triggerNewDiag();
        }        
    }

    public void OnChangeIdentNumber(String SIdent) {
        setIdentNumber( ConversionsAdvanced.hexStringToByteArray( SIdent ) );
    }
    
}
