/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

import java.util.ArrayList;
import common.framestructure.ProfibusFrame;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusSlaveCollection
{
    private ArrayList SlaveArray;
    private ProfibusFrame bufferProfibusFrame = null;
    
    public ProfibusSlaveCollection( )
    {
        //Populating slave array
        SlaveArray  = new ArrayList( );
        for( int i=0; i <  ProfibusConstants.MAX_ADDRESS; i++ )
        {
            SlaveArray.add( i, new ProfibusSlave( i ) );
            ProfibusSlave Slave = (ProfibusSlave) SlaveArray.get( i );
            Slave.run( );
        }
    }

    public void ReceiveRequest(ProfibusFrame sframe)
    {
            int addr = sframe.getDestAddr();
            
            if( addr < ProfibusConstants.MAX_ADDRESS )
            {
                ProfibusSlave sl = (ProfibusSlave) SlaveArray.get( addr );
                sl.ReceiveRequest( sframe );
                bufferProfibusFrame = sl.SendResponse();
            }
    }

    public ProfibusFrame getFrameResponse( )
    {
        ProfibusFrame tmpPf = bufferProfibusFrame;
        bufferProfibusFrame = null;
        return tmpPf;
    }

    /**
     * @param Online the Online to set
     */
    public void setOnline(int addr, boolean Online) {
        ProfibusSlave Slave = (ProfibusSlave) SlaveArray.get( addr );
        Slave.setOnline(Online);
    }

    public ProfibusSlave getSlave(int i) {
        if( i >= 0 && i < 127 )
            return (ProfibusSlave) SlaveArray.get( i );
        else
            return null;
    }

    boolean isSlaveArray() {
        return !SlaveArray.isEmpty();
    }

    void SetSlaveDiagSize(int addr, int extdiagsize) {
        ProfibusSlave Slave = (ProfibusSlave) SlaveArray.get( addr );
        Slave.setExtendedDiagSize( extdiagsize );        
    }


}
