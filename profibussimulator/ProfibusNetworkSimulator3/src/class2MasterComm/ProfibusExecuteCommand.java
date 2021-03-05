/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package class2MasterComm;

import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import common.framestructure.Service;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import stackDP.DPLine;
import stackDP.DPLineTCP;

/**
 *
 * @author Renato Veiga
 */
public class ProfibusExecuteCommand {
    
    DPLine dp = null;
    DPLineTCP dpTCP = null;
    ProfibusFrame pfreq;
    ProfibusFrame pfres;
    private final int masterAddr;
    private final int slaveAddr;
    private JTextField FieldOutput = null;
    private JTextArea AreaOutput = null;
    
    public ProfibusExecuteCommand(DPLine dp_channel, int MasterAddr, int SlaveAddr, Service svr )
    {
        dp         = dp_channel;
        masterAddr = MasterAddr;
        slaveAddr  = SlaveAddr;
        pfreq      = ProfibusServices.SendServiceRequest(masterAddr, slaveAddr, svr );
    }    
    
    
    public ProfibusExecuteCommand(DPLineTCP dptcp_channel, int MasterAddr, int SlaveAddr, Service svr )
    {
        dpTCP      = dptcp_channel;
        masterAddr = MasterAddr;
        slaveAddr  = SlaveAddr;
        pfreq      = ProfibusServices.SendServiceRequest(masterAddr, slaveAddr, svr );
    }
    
    public void SetOutputTextField(JTextField tf)
    {
        FieldOutput = tf;
    }
    
    public void SetOutputTextArea(JTextArea ta )
    {
        AreaOutput = ta;
    }
    
    public ProfibusFrame getFrameRequest( )
    {
        return pfreq;
    }

    public ProfibusFrame getFrameResponse( )
    {
        return pfres;
    }
    
    public void start( )
    {
        try {
            while( isSystemAvaiable() == false )
            {
                System.out.println("Waiting setup...");
                Thread.sleep(100);
            }
            
            if(AreaOutput != null) AreaOutput.append("> "+pfreq.toString() +"\n");
            setOutputTelegram( pfreq.getRawData() );
            

            if( IsNewResponse() == true )
            {
                pfres = new ProfibusFrame( getInputTelegram() );
                
                if(AreaOutput != null)
                    AreaOutput.append( "< " + pfres.toString() +"\n" );
                
                if(FieldOutput != null) 
                    FieldOutput.setText( pfres.toString() );
            }
            
        } catch (Exception ex) {
            Logger.getLogger(ProfibusExecuteCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected boolean isSystemAvaiable( )
    {
        if( dp != null )
            return dp.isSystemAvaiable();
        else if( dpTCP != null)
            return dpTCP.isSystemAvaiable();
        else
            return false;
    }

    private void setOutputTelegram(byte[] rawData) throws IOException {
        if( dp != null )
            dp.setOutputTelegram(rawData);
        else if( dpTCP != null)
            dpTCP.setOutputTelegram(rawData);
    }

    protected boolean IsNewResponse() {
        if( dp != null )
            return dp.IsNewResponse();
        else if( dpTCP != null)
            return dpTCP.isNewResponse();
        else
            return false;
    }
    
    protected byte[] getInputTelegram() throws IOException
    {
        if( dp != null )
            return dp.getInputTelegram();
        else if( dpTCP != null)
            return dpTCP.getInputTelegram();
        else
            return null;
    }
            
            
}
