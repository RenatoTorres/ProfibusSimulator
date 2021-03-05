/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package class2MasterComm;

import common.framestructure.Service;
import stackDP.DPLineTCP;

/**
 *
 * @author Renato Veiga
 */
public class CommandServices {
    static public String COMPORT = new String( );
    static public String SerialServerIP= new String( );
    static public String BaudRate= new String( );
    static public float fSlotTime = 0;

    static void setConnectionPrm(String commPort, String SerialIP, String Baud, float fSlot )
    {
        COMPORT         = commPort;
        SerialServerIP  = SerialIP;
        BaudRate        = Baud;
        fSlotTime       = fSlot;
    }
    
    static public ProprietaryExecuteCommand ConnectAndExecute(DPLineTCP dpTCP, int MasterAddr, int SlaveAddr, byte[] frame ) throws Exception
    {
        ProprietaryExecuteCommand pe = ConnectAndNotExecute(dpTCP, MasterAddr, SlaveAddr, frame );
        pe.start();
        
        return pe;
    }

    static public ProprietaryExecuteCommand ConnectAndNotExecute(DPLineTCP dpTCP, int MasterAddr, int SlaveAddr, byte[] frame ) throws Exception {
        
        String SlotTime       = String.valueOf(fSlotTime);
        
        if( !dpTCP.isConnected() )
            dpTCP.connect( SerialServerIP, 30000, COMPORT, BaudRate, SlotTime, ((Integer) MasterAddr).toString() );

        ProprietaryExecuteCommand pe = new ProprietaryExecuteCommand(dpTCP, 
                MasterAddr, 
                SlaveAddr, 
                frame);

        return pe;
    }        
    
    
    static public ProfibusExecuteCommand ConnectAndExecute(DPLineTCP dpTCP, int MasterAddr, int SlaveAddr, Service service ) throws Exception
    {
        ProfibusExecuteCommand pe = ConnectAndNotExecute(dpTCP, MasterAddr, SlaveAddr, service );
        pe.start();
        
        return pe;
}

    static public ProfibusExecuteCommand ConnectAndNotExecute(DPLineTCP dpTCP, int MasterAddr, int SlaveAddr, Service service ) throws Exception {
        
        String SlotTime       = String.valueOf(fSlotTime);
        
        if( !dpTCP.isConnected() )
            dpTCP.connect( SerialServerIP, 30000, COMPORT, BaudRate, SlotTime, ((Integer) MasterAddr).toString() );

        ProfibusExecuteCommand pe = new ProfibusExecuteCommand(dpTCP, 
                MasterAddr, 
                SlaveAddr, 
                service);
        
        return pe;
    }    
    
}
