/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MDBAccess;

import MDBAccess.PBCfg.DeviceCfg;
import java.sql.*;
import java.util.HashMap;

/**
 *
 * @author Renato Veiga
 */
public class PBFile 
{
    Connection connection = null;
    int num_devices = 0;
    public PBCfg pbcfg = new PBCfg( );
    private String myFileName;
    private String myShortFileName;

    public PBFile(String filename)
    {
        try {
            //String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=";
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            //File f = new File(filename);
            //connection = DriverManager.getConnection(url + filename, "anonymous" /*username*/, "guest" /*password*/);
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String url = "jdbc:ucanaccess://";
            connection = DriverManager.getConnection(url + filename);

            ReadMDBFile();
            
            myFileName = new String( filename );

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error!");
        }
    }
    
    public PBCfg getConfiguration( ) {
        return pbcfg;
    }
    
    public HashMap<Integer,DeviceCfg> getDeviceMap( ) {
        return pbcfg.DeviceList;
    }

    public void ReadMDBFile() throws SQLException {
        
        ReadBusParameters( );
        
        ReadNumberOfStations( );
        
        ReadMasterInfo( );
        
        ReadDeviceList( );
        
        connection.close();
    }

    protected void ReadNumberOfStations() throws SQLException {
        //Get number of devices
        String query_num_dev = "SELECT COUNT(*) FROM PROFIBUS";
        Statement st_num_dev = connection.createStatement();
        ResultSet rs_num_dev = st_num_dev.executeQuery(query_num_dev);
        Integer num_devices = rs_num_dev.next() ? new Integer(rs_num_dev.getInt(1)) : 0;
        System.out.println("PBFile - Number of stations: " + String.valueOf(num_devices) );
        System.out.println(" " );
        
        pbcfg.num_devices = num_devices;
    }

    private void ReadMasterInfo() throws SQLException {
        String query_master_info = "SELECT PROFIBUS.Station_Address, PROFIBUS.Info_Text, PROFIBUS.Dev_Type_Text, PROFIBUS.Vendor_Name, PROFIBUS.GsdFile FROM PROFIBUS WHERE PROFIBUS.Station_Address = PROFIBUS.Master_System;";

        //Get Master information
        Statement st_master_info = connection.createStatement();
        ResultSet rs_master_info = st_master_info.executeQuery(query_master_info);
        if( rs_master_info.next() ) {
            String tag = "Master";
            String vendor = rs_master_info.getString(4);
            String model = rs_master_info.getString(3);
            int addr = rs_master_info.getInt(1);

            System.out.println("PBFile - Master Address..: " + addr );
            System.out.println("PBFile - Master Vendor: "    + vendor );
            System.out.println("PBFile - Master Model.: "    + model );
            System.out.println(" " );
            
            pbcfg.addMaster( "Master", vendor, model, addr );            
        }
    }

    private void ReadDeviceList() throws SQLException {
        String query_device_info = "SELECT PROFIBUS.Station_Address, PROFIBUS.Info_Text, PROFIBUS.Dev_Type_Text, PROFIBUS.Vendor_Name, PROFIBUS.GsdFile FROM PROFIBUS WHERE PROFIBUS.Station_Address <> PROFIBUS.Master_System;";
        String query_ident_num   = "SELECT DESCRIPT.IdentNumber FROM DESCRIPT";
        
        //Get Device List
        Statement st_device_info = connection.createStatement();
        ResultSet rs_device_info = st_device_info.executeQuery(query_device_info);
        
        //Get Device Ident Number List
        Statement st_ident_num = connection.createStatement();
        ResultSet rs_ident_num = st_ident_num.executeQuery(query_ident_num);
      
        while (rs_device_info.next() && rs_ident_num.next()) {
            String tag    = rs_device_info.getString(2);
            String vendor = rs_device_info.getString(4);
            String model  = rs_device_info.getString(3);
            String gsd    = rs_device_info.getString(5);
            int addr      = rs_device_info.getInt(1);
            int ident     = rs_ident_num.getInt(1);            
            
            System.out.println("PBFile - Slave Address.......: " + addr );
            System.out.println("PBFile - Slave Tag...: " + tag );
            System.out.println("PBFile - Slave Vendor: " + vendor );
            System.out.println("PBFile - Slave Model.: " + model );
            System.out.println("PBFile - Slave GSD...: " + gsd );
            System.out.println("PBFile - Slave IdentNumber: " + ident );
            
            pbcfg.addSlave( tag, vendor, model, gsd, ident, addr );
            
            AddModulesInfo( addr );
        }
    }

    private void AddModulesInfo( int addr ) throws SQLException {
        String query_device_module  = "SELECT * FROM DESCRIPT WHERE DESCRIPT.StationAddress = ";
        String query_device_address = "SELECT * FROM ADD_TAB WHERE Station_Address = ";
        String query_device_module_f  = null;
        String query_device_address_f = null;

        //Get ModuleIndentifier List
        query_device_module_f  = query_device_module  + String.valueOf( addr ) + ";";
        query_device_address_f = query_device_address + String.valueOf( addr ) + ";";

        System.out.println( query_device_module_f );
        Statement st_module_info = connection.createStatement();
        ResultSet rs_module_info = st_module_info.executeQuery(query_device_module_f);

        //Get number of modules of this device
        int moduleCount = 0;

        if( rs_module_info.next() )
            moduleCount = rs_module_info.getByte(11);

        pbcfg.DeviceList.get(addr).num_modules = moduleCount;

        //Get Modules info
        System.out.println( query_device_address_f );
        Statement st_address_info = connection.createStatement();
        ResultSet rs_address_info = st_address_info.executeQuery(query_device_address_f);
        
        int intotal  = 0;
        int outtotal = 0;

        String DeviceIdentifierString = new String( );
        while (rs_address_info.next() && moduleCount > 0) {  
            int inlen  = rs_address_info.getInt("Input_Data_Length");;
            int outlen = rs_address_info.getInt("Output_Data_Length");
            
            int identifierCount = rs_address_info.getInt("Ident_Count");
            String ModuleModel  = "";//rs_address_info.getString("Modul_Name");
            String Tag          = "";//rs_address_info.getString("SymbolName");
            int moduleIndex     = rs_address_info.getInt("Modul_Index");
            
            System.out.println("PBFile - ("+ String.valueOf( addr ) + ") Adding Module: " + Tag);

            //Get Configuration WORDS
           String ModuleIdentifierString = new String();
            if (identifierCount > 0) {
                for (int i = 1; i <= identifierCount; i++) {
                    int identifier = rs_address_info.getInt("Ident_" + String.valueOf(i));
                    ModuleIdentifierString += "0x" + Integer.toHexString(identifier).toUpperCase();
                    ModuleIdentifierString += ",";
                }
            }
            
            if( moduleIndex > 1 )
                Tag += Tag + "_" + String.valueOf(moduleIndex);
            
            //Add module to device
            pbcfg.DeviceList.get(addr).addModule(inlen, outlen, Tag, ModuleModel, ModuleIdentifierString);
            
            intotal  += inlen;
            outtotal += outlen;
            DeviceIdentifierString += ModuleIdentifierString;
        }          

        String preCfg = new String();
        String AString[] = DeviceIdentifierString.split(",");
        
        for(int i=0; i < AString.length; i++)
        {
            String sTemp = AString[i].replace("0x", "");
            if( (sTemp.length() < 2) && (sTemp.length() > 0) )
            {
                AString[i] = "0" + AString[i];
            }
            
            preCfg = preCfg + AString[i];
        }

        preCfg = preCfg.replace("0x", "");
        if( ( (preCfg.length() % 2) != 0) && (preCfg.length() > 0) )
            preCfg = "0" + preCfg;
                    
        pbcfg.DeviceList.get(addr).cfg       = preCfg;
        pbcfg.DeviceList.get(addr).INLength  = intotal;
        pbcfg.DeviceList.get(addr).OUTLength = outtotal;

        System.out.println("PBFile - Number of Modules: " + moduleCount );
        System.out.println("PBFile - Identifiers......: " + DeviceIdentifierString + "(" + preCfg + ")" );
        System.out.println("PBFile - Total INput  Length: " + intotal );
        System.out.println("PBFile - Total OUTput Length: " + outtotal );
        System.out.println(" ");
    }

    private void ReadBusParameters() throws SQLException {
        //Get number of devices
        String query_bus_param = "SELECT * FROM BUS_DP_TAB";
        Statement st_bus_param = connection.createStatement();
        ResultSet rs_bus_param = st_bus_param.executeQuery(query_bus_param);

        long baudRate =0; 
        int SlotTime =0;
        int MinTSDR =0;
        int MaxTSDR =0;
        int TQui =0;
        int TSet =0;
        long TTR =0;
        int GAP =0;
        int HSA =0;
        int Max_Retry =0;
        int Min_Slave_Interval =0;
        int Poll_Timeout =0;
        int Data_Control_Time =0;
        long Watchdog =0;
        
        
        if( rs_bus_param.next() ) {
            baudRate           = DecodeBaudRate( rs_bus_param.getInt("Baudrate") );
            SlotTime           = rs_bus_param.getInt("Tsl");
            MinTSDR            = rs_bus_param.getInt("Min_Tsdr");
            MaxTSDR            = rs_bus_param.getInt("Max_Tsdr");
            TQui               = rs_bus_param.getInt("Tqui");
            TSet               = rs_bus_param.getInt("Tset");
            
            TTR               = rs_bus_param.getLong("Ttr");
            GAP                = rs_bus_param.getInt("Gap");
            HSA                = rs_bus_param.getInt("Hsa");
            Max_Retry          = rs_bus_param.getInt("Max_Retry_Limit");
            
            Min_Slave_Interval = rs_bus_param.getInt("Min_Slave_Intervall");
            Poll_Timeout       = rs_bus_param.getInt("Poll_Timeout");
            Data_Control_Time  = rs_bus_param.getInt("Data_Control_Time");
            Watchdog          = rs_bus_param.getLong("Watchdog");
        }            
            
        System.out.println("PBFile - BUS PARAMETERS");
        System.out.println("PBFile - BaudRate: " + String.valueOf(baudRate) );
        System.out.println("PBFile - SlotTime: " + String.valueOf(SlotTime) );
        System.out.println("PBFile - MinTSDR: " + String.valueOf(MinTSDR) );
        System.out.println("PBFile - MaxTSDR: " + String.valueOf(MaxTSDR) );
        System.out.println("PBFile - Quiet Time: " + String.valueOf(TQui) );
        System.out.println("PBFile - Setup Time: " + String.valueOf(TSet) );
        System.out.println(" " );        
        System.out.println("PBFile - TTR: " + String.valueOf(TTR) );
        System.out.println("PBFile - GAP: " + String.valueOf(GAP) );
        System.out.println("PBFile - Max_Retry: " + String.valueOf(Max_Retry) );
        System.out.println("PBFile - HSA: " + String.valueOf(HSA) );
        System.out.println(" " );
        System.out.println("PBFile - Min_Slave_Interval: " + String.valueOf(Min_Slave_Interval) );
        System.out.println("PBFile - Poll_Timeout: " + String.valueOf(Poll_Timeout) );
        System.out.println("PBFile - Data_Control_Time: " + String.valueOf(Data_Control_Time) );
        System.out.println("PBFile - Watchdog: " + String.valueOf(Watchdog) );
        System.out.println(" " );

        pbcfg.SetBusParameter( 
                baudRate,
                SlotTime,
                MinTSDR,
                MaxTSDR,
                TQui,
                TSet,
                TTR,
                GAP,
                HSA,
                Max_Retry,
                Min_Slave_Interval,
                Poll_Timeout,
                Data_Control_Time,
                Watchdog );
    }

    private long DecodeBaudRate(int aInt) {
        long lret = 0;
        switch( aInt ) {
            case 0: lret=9600;      break;
            case 1: lret=19200;     break;
            case 11: lret=45450;     break;
            case 2: lret=93750;     break;
            case 3: lret=187500;    break;
            case 4: lret=500000;    break;
            case 6: lret=1500000;   break;
            case 7: lret=3000000;   break;
            case 8: lret=6000000;   break;
            case 9: lret=12000000;  break;
        }
        return lret;
    }

    public String getFileName() {
        return myFileName;
    }

    public String getShortFileName( )
    {
        return myShortFileName;
    }
    
    public void setShortFileName(String name) {
        myShortFileName = new String(name);
    }
}
