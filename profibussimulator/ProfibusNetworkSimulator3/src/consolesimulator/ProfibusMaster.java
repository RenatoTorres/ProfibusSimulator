/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

import MDBAccess.PBCfg;
import MDBAccess.PBCfg.DeviceCfg;
import common.ConversionsAdvanced;
import exceptions.ProfibusFrameException;
import java.util.ArrayList;
import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import common.framestructure.Service;
import common.framestructure.SD;
import dataunits.SetParameter;
import dataunits.SlaveDiagnostic;
import dataunits.CheckConfiguration;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusMaster {
    int address;
    boolean brunning_cyclic = false;
    int current_cfg_polling = 0;
    int last_cfg_polling    = 0;
    int first_cfg_polling   = 0;
    int current_fdl_polling = 0;
    ProfibusFrame MasterLastRequest = null;
    ConfigurationData[] CfgFile;
    
    int HSA=ProfibusConstants.MAX_NUM_SLAVES;
    public ArrayList SlaveMirror = new ArrayList();
    public ArrayList LiveDevices = new ArrayList();
    
    public byte[][] ConfigurationData;
    
    private boolean bIsMyToken;
    private float _BaudRate;    //bits/sec
    private float _ConfSetupTime;
    private float _ConfQuietTime;
    private float _ConfSlotTime;
    private float _ConfMinTsdr;
    private float _ConfMaxTsdr;
    private float _ConfTTR;
    private float _ConfGAP;
    private float _ConfRetries;
    private float _ConfHSA;
    private float _ConfWdg;
    private int isendGlobalControl = 0;
    public boolean retryProcess = false;
    
    public int numOnline = 0;
    public int numDataExchange = 0;
    
    
    private ProfibusFrame CyclicMachine(int address) {
        ProfibusFrame pfret;

        ProfibusSlave sl = (ProfibusSlave) SlaveMirror.get( address );

        switch( sl.getSlaveStateInMaster() ) {

            case Service.PB_REQ_GET_DIAG:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_GET_DIAG) );
                break;

            case Service.PB_REQ_GET_CFG:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_GET_CFG) );
                break;

            case Service.PB_SET_SLAVE_ADDR:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_SET_SLAVE_ADDR) );
                break;

            case Service.PB_REQ_SET_PRM:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_SET_PRM) );
                pfret.setData_Unit( SetParameterForSlave(sl.getAddress()) );
                break;
                
            case Service.PB_REQ_CHK_CFG:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_CHK_CFG) );
                pfret.setData_Unit( CheckConfigurationForSlave(sl.getAddress()) );
                break;

            case Service.PB_REQ_RD_INPUTS:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_RD_INPUTS) );
                break;

            case Service.PB_REQ_RD_OUTPUTS:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_RD_OUTPUTS) );
                break;

            case Service.PB_REQ_DATA_EXCHANGE:
                int output_size = CfgFile[address].getProfibusCfg().getCfgOutputSize();
                if( output_size != 0 )
                {
                    //Write data
                    pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_DATA_EXCHANGE) );
                    pfret.setData_Unit( new byte[output_size] );
                }
                else
                {
                    pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS) );
                }
                break;

            default:
                pfret = ProfibusServices.SendServiceRequest(this.address, sl.getAddress(), new Service(Service.PB_REQ_GET_DIAG) );
                break;

        }
        
        if( !sl.isAlreadyInCommunication() )
        {
            pfret.setFCV( false );
            pfret.setFCB( true );
            sl.setMasterUseLastFCB( false );
        }
        else
        {
            pfret.setFCV( true );
            pfret.setFCB( sl.getMasterUseLastFCB() );
            sl.setMasterUseLastFCB( !sl.getMasterUseLastFCB() ); //toggle
        }

        return pfret;
    }

    private int getNextCyclicAddress() {
        ProfibusSlave ts;
        int temp_addr = current_cfg_polling;
        int i;

        //Getting next slave to polling
        //1) Manter uma lista dos equipamentos que foram configurados
        //2) A cada vez que entrar nesta rotina, ir para o proximo equipamento
        //   configurado. Verificar se ele esta online e entao retornar seu
        //   enderenco.
        //3) Este endereco devera ser guardado a fim de que na proxima chamada este seja
        //   o inicio da busca. Caso seja atingido 127 e o proximo ainda nao tenha sido encontrado,
        //   reinicia-se a partir do 0.
        //
        //4) Se for retry - mantem mesmo cfg_polling e retorna
        
        if( retryProcess == true )
            return current_cfg_polling;

        //Running for all list
        for( i = (current_cfg_polling+1); i < ProfibusConstants.MAX_ADDRESS; i++) 
        {
            ts = (ProfibusSlave) SlaveMirror.get(i);
            if( (ts.isActiveOnCfg() == true) ) 
            {
                if( /*(ts.isOnline() == true) &&*/ (ts.getAddress() != this.address) ) 
                {
                    current_cfg_polling = i;
                    break;
                }
            }
        }

        if(current_cfg_polling == temp_addr) {
            //127 was reached!
            //Restart from 0
            current_cfg_polling = -1;
        }

        return current_cfg_polling;
    }

    private int getNextReqFDLAddress() {
        ProfibusSlave ts;
        int i;

        if( current_fdl_polling > HSA)
            current_fdl_polling = 0;

        //Procura pelo proximo offline
        for(i=current_fdl_polling; i < (HSA+1); i++ ) {
            ts = (ProfibusSlave) SlaveMirror.get( i );
            if( (i != this.address) && (ts.getSlaveStateInMaster() != Service.PB_REQ_DATA_EXCHANGE) ) {
                current_fdl_polling = i;
                break;
            }
        }

        //Testa se chegou ao fim da lista
        if( current_fdl_polling != i ) {
            current_fdl_polling = 0;

            //Procura pelo proximo offline
            for(i=current_fdl_polling; i < (HSA+1); i++ ) {
                ts = (ProfibusSlave) SlaveMirror.get( i );
                if( (i != this.address) /*&& (ts.isOnline() == false)*/ ) {
                    current_fdl_polling = i;
                    break;
                }
            }
        }

        bIsMyToken = false;

        return current_fdl_polling++;
    }

    public float getBaudRate() {
        return _BaudRate;
    }

    public float getBitTimeMs() {
        return (float) (1000f / _BaudRate);
    }

    public float getSlotTime() {
        return _ConfSlotTime;
    }

    public float getMinTSDR() {
        return _ConfMinTsdr;
    }

    public float getSetupTime() {
        return _ConfSetupTime;
    }

    public float getQuietTime() {
        return _ConfQuietTime;
    }
    
    public float getMaxTSDR( ) {
        return _ConfMaxTsdr;
    }

    public float getGAP( ) {
        return _ConfGAP;
    }
    
    public float getHSA( ) {
        return _ConfHSA;
    }
    
    public float getRetries( ) {
        return _ConfRetries;
    }
    
    public float getTTR( ) {
        return _ConfTTR;
    }
    
    public float getWatchdog( ) {
        return _ConfWdg;
    }
    
    private byte[] SetParameterForSlave(int address) {
        SetParameter prm = new SetParameter();
        prm.setFreeze(false);
        prm.setSync(false);
        prm.setGroup_Ident((byte) 0x00);
        prm.setLock(true);
        prm.setUnlock(false);
        prm.setMinTSDR( (byte) _ConfMinTsdr );
        

        if( _ConfWdg > 0 )
            prm.setWATCHDOG(true);
        else
            prm.setWATCHDOG(false);

        if( _ConfWdg <= 2550 )
        {
            //WDG = WD-Factor1 * WD-Factor2 * 10ms
            //WDG = WD-Factor2 * 10ms
            //WD-Factor2 = WDG / 10;
            prm.setWD_FACTOR1( (byte) 1);
            prm.setWD_FACTOR2( (byte) (_ConfWdg/10) );
        }
        else
        {
            //WDG = WD-Factor1 * WD-Factor2 * 10ms
            //WDG = 0xFF * WD-Factor2 * 10ms
            //WD-Factor2 = WDG / (10*0xFF);
            prm.setWD_FACTOR1( (byte) 0xFA );
            prm.setWD_FACTOR2( (byte) (_ConfWdg/(10*0xFA)) );
        }
        byte IdentHigh = ((ProfibusSlave) SlaveMirror.get(address)).getIdentNumberHigh();
        byte IdentLow = ((ProfibusSlave) SlaveMirror.get(address)).getIdentNumberLow();
        if(IdentHigh != 0 || IdentLow != 0 ) {
            prm.setIdentNumber_High( IdentHigh  );
            prm.setIdentNumber_Low( IdentLow );
        }

        return prm.getData_Unit();
    }

    private byte[] CheckConfigurationForSlave(int address) {
        ProfibusSlave ps = (ProfibusSlave) SlaveMirror.get(address);
        CheckConfiguration cfg = new CheckConfiguration( ps.getNativeCfg() ) ;
        
        return cfg.getData_Unit();
    }

    boolean isSlaveConfiguredStartOnline(int i) {
        return ((ProfibusSlave) SlaveMirror.get(i)).isOnline();
    }

    int getSlaveExtendedDiagSize(int i) {
        return 0;
    }

    public int getNumOnlineSlaves() {
        return numOnline;
    }
    
    public int getNumDataExchangeSlaves( )
    {
        return numDataExchange;
    }

    ProfibusSlave getConfiguredSlave(int i) {
        return (ProfibusSlave) SlaveMirror.get( i );
    }
    

    class LiveList {
        public int SlaveCurrentState;
        public SlaveDiagnostic Diag = new SlaveDiagnostic();
    }
    
    class ConfigurationData {
        byte[] data_file;
        CheckConfiguration cfg = null;
        
        CheckConfiguration getProfibusCfg( )
        {
            if( cfg == null)
            {
                cfg = new CheckConfiguration(data_file);
            }
            return cfg;
        }
    }
    
    public ProfibusMaster(PBCfg pbcfg )
    {
        //Get master Properties from file .pb
        ProfibusSlave ts;
        this.address = pbcfg.getMasterAddress();
        
        LiveDevices.clear( );
        SlaveMirror.clear( );
        for(int i=0; i < ProfibusConstants.MAX_ADDRESS; i++) {
            LiveDevices.add(new LiveList());
            SlaveMirror.add(new ProfibusSlave(i));
            ((LiveList)LiveDevices.get(i)).Diag.setStation_Non_Existent(true);
        }
        
        String isStartOnline;

        //Get baudrates from pbfile
        _BaudRate      = pbcfg.busParameter.baudRate;
        _ConfSlotTime  = pbcfg.busParameter.SlotTime;
        _ConfMinTsdr   = pbcfg.busParameter.MinTSDR;
        _ConfMaxTsdr   = pbcfg.busParameter.MaxTSDR;
        _ConfQuietTime = pbcfg.busParameter.TQui;
        _ConfSetupTime = pbcfg.busParameter.TSet;

        _ConfTTR       = pbcfg.busParameter.TTR;
        _ConfGAP       = pbcfg.busParameter.GAP;
        _ConfRetries   = pbcfg.busParameter.Max_Retry;
        _ConfHSA       = pbcfg.busParameter.HSA;
        _ConfWdg       = pbcfg.busParameter.Watchdog;
        
        //Add devices to simulator structure from PBFile
        //Iteract in pbcfg Device List
        
        Map<Integer,DeviceCfg> map = pbcfg.getDeviceList( );
        Iterator<Map.Entry<Integer,DeviceCfg>> entries = map.entrySet().iterator();
        CfgFile = new ConfigurationData[ProfibusConstants.MAX_ADDRESS-1];
        
        while( entries.hasNext() ) {
            Map.Entry<Integer,DeviceCfg> entry = entries.next();
            Integer addr = entry.getKey();
            DeviceCfg dev = entry.getValue();
            
            if( pbcfg.getMasterAddress() == addr )
                continue;
           
            ts = (ProfibusSlave) SlaveMirror.get( addr );
            ts.setActiveOnCfg(true);
            ts.setSlaveStateInMaster( Service.PB_REQ_GET_DIAG );
            String cfg = dev.cfg;
            ts.setNativeCfg( ConversionsAdvanced.hexStringToByteArray(cfg) );


            
            //Get Identifiers
            CfgFile[addr] = new ConfigurationData();
            CfgFile[addr].data_file = new byte[ts.getNativeCfg().length];
            System.arraycopy(ts.getNativeCfg(), 0, CfgFile[addr].data_file, 0, ts.getNativeCfg().length);

            if( dev.ident_number < 0 )
                dev.ident_number += 65536;
            
            //Get IdentNumber
            if( dev.ident_number != 0)
            {
                String sIdent = Integer.toHexString(dev.ident_number);
                if( (sIdent.length() % 2) != 0 )
                    sIdent = "0"+sIdent;
                ts.setIdentNumber( ConversionsAdvanced.hexStringToByteArray( sIdent ));
                //diagcfg.setIdentNumber( ConversionsAdvanced.hexStringToByteArray( sIdent ) );
            }
            
            ts.setOnline(true);

            System.out.println("Adding Configured Slave: Address " + String.valueOf(addr) + 
                    " IdentNumber: " + String.valueOf( dev.ident_number) + " Configuration: " + dev.cfg);
            
            
            /*rowData.add( dev.name ); //TAG
            rowData.add( dev.INLength ); //Bytes IN
            rowData.add( dev.OUTLength ); //Bytes OUT*/
        }
        
    }

    public ProfibusMaster(int address) {
        String isActive, cfg, srprop, Ident;
        ProfibusSlave ts;
        this.address = address;
        
        LiveDevices.clear( );
        SlaveMirror.clear( );
        for(int i=0; i < ProfibusConstants.MAX_ADDRESS; i++) {
            LiveDevices.add(new LiveList());
            SlaveMirror.add(new ProfibusSlave(i));
            ((LiveList)LiveDevices.get(i)).Diag.setStation_Non_Existent(true);
        }

        Properties props = new Properties();
        String isStartOnline;

        //try retrieve data from file
        try {
            System.out.println(System.getProperty("user.dir"));
            props.load(new FileInputStream("master.properties"));
            _BaudRate  = Float.valueOf( props.getProperty( "BaudRate" ) );
            _ConfSlotTime  = Float.valueOf( props.getProperty( "SlotTime" ) );
            _ConfMinTsdr   = Float.valueOf(props.getProperty( "MinTSDR" ) );
            _ConfMaxTsdr   = Float.valueOf(props.getProperty( "MaxTSDR" ) );
            _ConfQuietTime = Float.valueOf(props.getProperty( "QuietTime" ) );
            _ConfSetupTime = Float.valueOf(props.getProperty( "SetupTime" ) );
            
            _ConfTTR     = Float.valueOf(props.getProperty( "TTR" ) );
            _ConfGAP     = Float.valueOf(props.getProperty( "GAP" ) );
            _ConfRetries = Float.valueOf(props.getProperty( "Retries" ) );
            _ConfHSA     = Float.valueOf(props.getProperty( "HSA" ) );
            _ConfWdg     = Float.valueOf(props.getProperty( "WDG" ) );
            
            
            CfgFile = new ConfigurationData[ProfibusConstants.MAX_ADDRESS-1];
            
            for(int i=0; i < (ProfibusConstants.MAX_ADDRESS-1); i++)
            {
                srprop = "Address" + String.valueOf(i)+ ".isActive";
                isActive = props.getProperty( srprop );
                if(isActive.equals("yes") == true)
                {
                    ts = (ProfibusSlave) SlaveMirror.get(i);
                    ts.setActiveOnCfg(true);
                    ts.setSlaveStateInMaster( Service.PB_REQ_GET_DIAG );
                    
                    srprop = "Address" + String.valueOf(i)+ ".Cfg";
                    cfg = props.getProperty( srprop );
                    ts.setNativeCfg( ConversionsAdvanced.hexStringToByteArray(cfg));
                    CfgFile[i] = new ConfigurationData();
                    CfgFile[i].data_file = new byte[ts.getNativeCfg().length];
                    System.arraycopy(ts.getNativeCfg(), 0, CfgFile[i].data_file, 0, ts.getNativeCfg().length);

                    srprop = "Address" + String.valueOf(i)+ ".IdentNumber";
                    Ident  = props.getProperty( srprop );
                    ts.setIdentNumber( ConversionsAdvanced.hexStringToByteArray(Ident));
                    
                    srprop = "Address" + String.valueOf(i)+ ".StartOnline";
                    isStartOnline  = props.getProperty( srprop );
                    if( isStartOnline.equals("yes") == true ) {
                        ts.setOnline(true);
                    }
                    
                    System.out.println("Adding Configured Slave: Address " + String.valueOf(i) + 
                            " IdentNumber: " + Ident + " Configuration: " + cfg);
                }
            }
        }
        catch(IOException e) {
             e.printStackTrace();
        }

        bIsMyToken = true;

    }

    public ProfibusFrame runMachine( ) throws ProfibusFrameException {
        ProfibusFrame pfret = null;
        int tempaddr;
        
        // Operacao do mestre
        // Realizar todos os comandos ciclicos
        // Procurar por um novo escravo
        // Enviar o comando de token para o proximo mestre
        // 
        if( bIsMyToken == true) {
            tempaddr = getNextCyclicAddress();
            if( tempaddr != -1 ) {
                if( retryProcess == false )
                {
                    pfret = CyclicMachine( tempaddr );
                }
                else
                {
                    pfret = new ProfibusFrame( MasterLastRequest.toString() );
                }
            }
            else 
            {
                pfret = ProfibusServices.SendRequestFDLStatus( getNextReqFDLAddress(), this.address );
            }
        }
        else 
        {
            if( isendGlobalControl != 0 )
            {
                pfret = ProfibusServices.SendServiceRequest( this.address, 127, new Service(Service.PB_REQ_GLOBAL_CONTROL) );
                pfret.setData_Unit(new byte[2]);
                isendGlobalControl = 0;
            }
            else
            {
                pfret = ProfibusServices.SendTokenPass(address, address );
                isendGlobalControl = 1;
                bIsMyToken = true;
            }
        }

        MasterLastRequest = pfret;

        return pfret;
    }

    public boolean isAlive( int i ) {
        return (( (LiveList)LiveDevices.get(i)).Diag.isStation_Non_Existent() == true) ? false : true;
    }

    public void setAlive( int i ) {
        ( (LiveList)LiveDevices.get(i)).Diag.setStation_Non_Existent(false);
    }

    public void setDead( int i ) {
        ( (LiveList)LiveDevices.get(i)).Diag.setStation_Non_Existent(true);
    }

    void ReceiveResponse(ProfibusFrame CurrentPacket) {
        ProfibusSlave sl;
        if( MasterLastRequest.getSD() == SD.SD4 ) {
            //TOKEN PASS
            //Ignore and continue
            return;
        }
        
        if( CurrentPacket == null ) 
        {
            if( (MasterLastRequest != null) && (MasterLastRequest.getDestAddr() < ProfibusConstants.MAX_ADDRESS)) 
            {
               
                sl = (ProfibusSlave) SlaveMirror.get( MasterLastRequest.getDestAddr() );
                
                if( (sl.isOnline() != false) && (sl.getSlaveStateInMaster() == Service.PB_REQ_DATA_EXCHANGE) )
                {
                    if( sl.retries < _ConfRetries )
                    {
                        //Retry not exausted => Start Retry Process
                        sl.retries += 1;
                        retryProcess = true;
                    }
                    else
                    {
                        //Retries exausted => put in offline
                        sl.retries = 0;
                        retryProcess = false;
                        
                        numOnline -= 1;
                        numDataExchange -= 1;

                        sl.setOnline( false );
                        sl.restart();
                    }
                }

            }
            return;
        }
        
        if( CurrentPacket.getSD() != SD.SC ) {
            if( CurrentPacket.getSourceAddr() != MasterLastRequest.getDestAddr() ) {
                //Wrong response address
                //Ignore and return
                return;
            }
            sl = (ProfibusSlave) SlaveMirror.get( CurrentPacket.getSourceAddr() );
        }
        else {
            sl = (ProfibusSlave) SlaveMirror.get( MasterLastRequest.getDestAddr() );
            sl.setAlreadyInCommunication( true );
        }
        
        //In communication => reset retry counter
        retryProcess = false;
        sl.retries = 0;

        if( CurrentPacket.getFrameService() == Service.PB_RES_FDL_STATUS ) 
        {
            //Add Num Online stations
            if( sl.isOnline() == false )
                numOnline += 1;
            
            sl.setOnline( true );
            sl.setSlaveStateInMaster( Service.PB_REQ_GET_DIAG );
        }
        else 
        {
            sl.setAlreadyInCommunication( true );
            switch( sl.getSlaveStateInMaster() ) {
                
                case Service.PB_REQ_GET_DIAG:
                    if( CurrentPacket.getFrameService() == Service.PB_RES_GET_DIAG ) 
                    {
                        SlaveDiagnostic diag = new SlaveDiagnostic( CurrentPacket.getData_Unit() );
                        sl.diag.setPrm_Req( diag.isPrm_Req() );
                        sl.diag.setPrm_Fault( diag.isPrm_Fault() );
                        sl.diag.setCfg_Fault( diag.isCfg_Fault() );
                        sl.diag.setStation_Not_Ready( diag.isStation_Not_Ready() );                        
                        sl.diag.setStation_Non_Existent( diag.isStation_Non_Existent() );
                        
                        if( (diag.isPrm_Req() == true) || (diag.isPrm_Fault() == true) || (diag.isCfg_Fault() == true) ) 
                        {
                            sl.setSlaveStateInMaster( Service.PB_REQ_SET_PRM );
                        }
                        else 
                        {
                            if( diag.isStation_Not_Ready() == false )
                            {
                                sl.setSlaveStateInMaster( Service.PB_REQ_DATA_EXCHANGE );
                                numDataExchange += 1;
                            }
                        }
                    }
                    break;

                case Service.PB_REQ_SET_PRM:
                    sl.setSlaveStateInMaster( Service.PB_REQ_CHK_CFG );
                    break;

                case Service.PB_REQ_CHK_CFG:
                    sl.setSlaveStateInMaster( Service.PB_REQ_GET_DIAG );
                    break;

                case Service.PB_REQ_RD_INPUTS:
                    break;

                case Service.PB_REQ_RD_OUTPUTS:
                    break;

                case Service.PB_REQ_DATA_EXCHANGE:
                   
                    if(CurrentPacket.isResponseHighPriority() == true )
                    {
                        sl.setSlaveStateInMaster(Service.PB_REQ_GET_DIAG);
                        numDataExchange -= 1;
                    }
                    else
                        sl.setSlaveStateInMaster(Service.PB_REQ_DATA_EXCHANGE);
                    break;
            }
        }
    }
}

