/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package class2MasterComm;

import common.framestructure.ProfibusFrame;
import common.framestructure.Service;
import java.util.logging.Level;
import java.util.logging.Logger;
import stackDP.DPLineTCP;
import view.fdl.FDLAnalysis.FDLTelegramsTable;

/**
 *
 * @author Renato Veiga
 */
public class CommandServicesInterface {

    
static public byte[] getLiveList(DPLineTCP dpTCP, int masterAddress )
{
    int masteraddr = masterAddress;
    int slaveaddr=0;
    ProprietaryExecuteCommand pe;
    byte[] frame_content = new byte[1];
    byte [] ll = null;
    
    try {
        frame_content[0] = (byte) 0xA1;

        pe = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, frame_content );

        if( pe.IsNewResponse() == true )
            ll = pe.getLiveList();

    } catch (Exception ex) {
        Logger.getLogger(CommandServicesInterface.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return ll;
}
    

static public int getMasterStatus(DPLineTCP dpTCP, int masterAddr)
{
    int masteraddr = masterAddr;
    int MasterCondition=0;
    int slaveaddr=0;
    ProprietaryExecuteCommand pe;
    byte[] frame_content = new byte[1];

    try {
            frame_content[0] = (byte) 0xA2;

            pe = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, frame_content );

            if( pe.IsNewResponse() == true ) {
                MasterCondition = pe.getMasterStatus( );
            }
                
            } catch (Exception ex) {
                Logger.getLogger(FDLTelegramsTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return MasterCondition;
}

static public ProfibusFrame getSlaveDiag(DPLineTCP dpTCP, int masterAddr, int slaveAddr)
{
    int masteraddr = Integer.valueOf( masterAddr );
    int slaveaddr  = Integer.valueOf( slaveAddr );
    ProfibusFrame resframe = null;

    try {
        ProfibusExecuteCommand pres = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, new Service(Service.PB_REQ_GET_DIAG) );

        if( pres.IsNewResponse() == true )
                resframe = pres.getFrameResponse();
    
    } catch (Exception ex) {
        Logger.getLogger(CommandServicesInterface.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return resframe;
}

static public ProfibusFrame getSlaveCfg(DPLineTCP dpTCP, int masterAddr, int slaveAddr)
{
    int masteraddr = Integer.valueOf( masterAddr );
    int slaveaddr  = Integer.valueOf( slaveAddr );
    ProfibusFrame resframe = null;

    try {
        ProfibusExecuteCommand pres = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, new Service(Service.PB_REQ_GET_CFG) );

        if( pres.IsNewResponse() == true )
                resframe = pres.getFrameResponse();
    
    } catch (Exception ex) {
        Logger.getLogger(CommandServicesInterface.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return resframe;
}

static public ProfibusFrame ReadInputs(DPLineTCP dpTCP, int masterAddr, int slaveAddr)
{
    int masteraddr = Integer.valueOf( masterAddr );
    int slaveaddr  = Integer.valueOf( slaveAddr );
    ProfibusFrame resframe = null;

    try {
        ProfibusExecuteCommand pres = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, new Service(Service.PB_REQ_RD_INPUTS) );

        if( pres.IsNewResponse() == true )
                resframe = pres.getFrameResponse();
    
    } catch (Exception ex) {
        Logger.getLogger(CommandServicesInterface.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return resframe;
}

static public ProfibusFrame ReadOutputs(DPLineTCP dpTCP, int masterAddr, int slaveAddr)
{
    int masteraddr = Integer.valueOf( masterAddr );
    int slaveaddr  = Integer.valueOf( slaveAddr );
    ProfibusFrame resframe = null;

    try {
        ProfibusExecuteCommand pres = CommandServices.ConnectAndExecute(dpTCP, masteraddr, slaveaddr, new Service(Service.PB_REQ_RD_OUTPUTS) );

        if( pres.IsNewResponse() == true )
                resframe = pres.getFrameResponse();
    
    } catch (Exception ex) {
        Logger.getLogger(CommandServicesInterface.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return resframe;
}
    
}
