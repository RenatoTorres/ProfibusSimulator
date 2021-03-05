/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SlaveDialog;
import consolesimulator.ProfibusSimulator;
import consolesimulator.ProfibusSimulatorSingleton;
import consolesimulator.ProfibusSlave;


/**
 *
 * @author Veiga
 */
public class DiagAndCfgStationInterface {
    
    ProfibusSlave pbs;
    
    public DiagAndCfgStationInterface(int addr)
    {
        ProfibusSimulator pbsim = ProfibusSimulatorSingleton.getInstance();
        pbs = pbsim.getSlaveCollection().getSlave(addr);
    }
    
    public byte[] getSlaveDiagBits( )
    {
        return pbs.getDiagBits();
    }
    
    public void modifySlaveDiagAndCfg(byte[] bits ) {
        pbs.setDiagBits( bits );
        pbs.triggerNewDiag( );
    }

    int getSlavesMaster() {
        return (int) pbs.getMasterConfigurator( );
    }

    String getSlaveIdentNumber() {
        return pbs.getIdentNumberString();
    }

    public String getConfig() {
        return pbs.getCfgString();
    }

    public String getIdent() {
        return getSlaveIdentNumber();
    }

    public void setNativeCfg(byte[] hexStringToByteArray) {
        pbs.setNativeCfg( hexStringToByteArray );
    }

    public void setIdentNumber(byte[] hexStringToByteArray) {
        pbs.setIdentNumber( hexStringToByteArray );
    }

    public void modifySlaveDiagAndCfg(byte[] bits, String SConfig, String SIdent) 
    {
        pbs.setDiagBits( bits );
        pbs.OnChangeConfiguration( SConfig );
        pbs.OnChangeIdentNumber( SIdent );
        pbs.triggerNewDiag( );
    }
    
    
    
}
