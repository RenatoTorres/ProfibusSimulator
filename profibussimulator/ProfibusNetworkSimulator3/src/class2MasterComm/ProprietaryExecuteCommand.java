/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package class2MasterComm;

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
public class ProprietaryExecuteCommand {
    DPLine dp = null;
    DPLineTCP dpTCP = null;
    byte[] pfreq;
    byte[] pfres;
    private final int masterAddr;
    private final int slaveAddr;
    private JTextField FieldOutput = null;
    private JTextArea AreaOutput = null;
    
    public ProprietaryExecuteCommand( DPLine dp_channel, int MasterAddr, int SlaveAddr, byte[] propFrame )
    {
        dp         = dp_channel;
        masterAddr = MasterAddr;
        slaveAddr  = SlaveAddr;
        pfreq      = new byte[ propFrame.length + 1];
        System.arraycopy(propFrame, 0, pfreq, 0, propFrame.length);
    }    
    
    public ProprietaryExecuteCommand( DPLineTCP dptcp_channel, int MasterAddr, int SlaveAddr, byte[] propFrame )
    {
        dpTCP      = dptcp_channel;
        masterAddr = MasterAddr;
        slaveAddr  = SlaveAddr;
        pfreq      = new byte[ propFrame.length + 1];
        System.arraycopy(propFrame, 0, pfreq, 0, propFrame.length);
    }
    
    public void SetOutputTextField(JTextField tf)
    {
        FieldOutput = tf;
    }
    
    public void SetOutputTextArea(JTextArea ta )
    {
        AreaOutput = ta;
    }
    
    public byte[] getFrameRequest( ) {
        return pfreq;
    }

    public byte[] getFrameResponse( ) {
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
            
            if(AreaOutput != null) AreaOutput.append("> "+"Command Sent!" +"\n");
            setOutputTelegram( pfreq );

            if( IsNewResponse() == true )
            {
                pfres = getInputTelegram();
                
                if(AreaOutput != null)
                    AreaOutput.append( "< " + "Command Response!" +"\n" );
                
                if(FieldOutput != null) 
                    FieldOutput.setText( pfres.toString() );
            }
            
        } catch (Exception ex) {
            Logger.getLogger(ProprietaryExecuteCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected boolean isSystemAvaiable( )
    {
        if( dpTCP != null)
            return dpTCP.isSystemAvaiable();
        else
            return false;
    }

    private void setOutputTelegram(byte[] rawData) throws IOException {
        if( dpTCP != null)
            dpTCP.setOutputTelegram(rawData);
    }

    public boolean IsNewResponse() {
        if( dpTCP != null)
            return dpTCP.isNewResponse();
        else
            return false;
    }
    
    protected byte[] getInputTelegram() throws IOException
    {
        if( dpTCP != null)
            return dpTCP.getInputTelegram();
        else
            return null;
    }
    
    public byte[] getLiveList( )
    {
        return dpTCP.getLiveList();
    }
    
    public int getMasterStatus( )
    {
        return dpTCP.getMasterStatus();
    }
    
}
