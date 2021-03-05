/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MDBAccess;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Renato Veiga
 */
public class PBCfg {
    
    int num_devices = 0;
    public HashMap<Integer,DeviceCfg> DeviceList = new HashMap<Integer, DeviceCfg>();
    public BusParameter busParameter = new BusParameter( );
    int masterAddress = 0;
    
    public PBCfg( ) {

    }
    
    public DeviceCfg getDevice( int addr ) {
        return (DeviceCfg) DeviceList.get( addr );
    }
    

    void addMaster(String tag, String vendor, String model, int addr) {
        DeviceCfg dev = new DeviceCfg();
        dev.address = addr;
        dev.name = tag;
        dev.vendor = vendor;
        dev.model = model;
        dev.bMaster = true;
        
        DeviceList.put(addr, dev);
        
        masterAddress = addr;
    }
    
    void addSlave(String tag, String vendor, String model, String gsd, int ident, int addr) {
        DeviceCfg dev = new DeviceCfg();
        dev.address = addr;
        dev.ident_number = ident;
        dev.name = tag;
        dev.vendor = vendor;
        dev.model = model;
        dev.gsdname = gsd;
        dev.bMaster = false;
                
        DeviceList.put(addr, dev);
    }

    public void SetBusParameter(float baudRate, float SlotTime, float MinTSDR, float MaxTSDR, float TQui, float TSet, float TTR, float GAP, float HSA, float Max_Retry, float Min_Slave_Interval, float Poll_Timeout, float Data_Control_Time, float Watchdog) {
        busParameter.baudRate   = baudRate;
        busParameter.SlotTime   = SlotTime;
        busParameter.MinTSDR    = MinTSDR;
        busParameter.MaxTSDR    = MaxTSDR;
        busParameter.TQui       = TQui;
        busParameter.TSet       = TSet;
        
        busParameter.TTR        = TTR;
        busParameter.GAP        = GAP;
        busParameter.Max_Retry  = Max_Retry;
        busParameter.HSA        = HSA;
        
        busParameter.Poll_Timeout       = Poll_Timeout;
        busParameter.Min_Slave_Interval = Min_Slave_Interval;
        busParameter.Data_Control_Time  = Data_Control_Time;
        busParameter.Watchdog           = Watchdog;
    }

    public int getMasterAddress() {
        return masterAddress;
    }

    public Map<Integer, DeviceCfg> getDeviceList() {
        return DeviceList;
    }
    
    public class DeviceCfg {
        public String name;
        public String vendor;
        public String model;
        public int address;
        public int ident_number;
        public String gsdname;
        public boolean bMaster;

        public String cfg;
        
        public int num_modules;
        public HashMap<String,ModuleIndentifier> ModuleList = new HashMap<String, ModuleIndentifier>();
        
        public int INLength = 0;
        public int OUTLength = 0;
        
        public void addModule(int inLen, int Outlen, String Tag, String Model, String Ident) {
            ModuleIndentifier m = new ModuleIndentifier();
            m.INLength = inLen;
            m.OUTLength = Outlen;
            m.Tag   = Tag;
            m.Model = Model;
            m.Identifier = Ident;
            ModuleList.put(Tag, m);
        }
        
        public ModuleIndentifier getModule( String Tag ) {
            return (ModuleIndentifier) ModuleList.get(Tag);
        }
    }
    
    public class ModuleIndentifier {
        int INLength;
        int OUTLength;
        String Tag;
        String Model;
        String Identifier;
    }
    
    public class BusParameter {
        public float baudRate=0;
        public float SlotTime=0;
        public float MinTSDR=0;
        public float MaxTSDR=0;
        public float TQui=0;
        public float TSet=0;
        public float TTR=0;
        public float GAP=0;
        public float HSA=0;
        public float Max_Retry=0;
        public float Min_Slave_Interval=0;
        public float Poll_Timeout=0;
        public float Data_Control_Time=0;
        public float Watchdog=0;
    }
}
