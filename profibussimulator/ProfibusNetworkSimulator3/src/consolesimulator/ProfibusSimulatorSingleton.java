/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

import MDBAccess.PBFile;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusSimulatorSingleton
{
    protected static ProfibusSimulator Sim = null;

    public static ProfibusSimulator getInstance( )
    {
        if( Sim == null  )
        {
            Sim = new ProfibusSimulator( );
        }
        return Sim;
    }
    
    public static ProfibusSimulator getInstanceUsingPBFile(PBFile pbfile)
    {
        if( Sim == null )
        {
            Sim = new ProfibusSimulator( pbfile.pbcfg );
        }
        
        if( Sim.isRunningEnable == false )
        {
            Sim = new ProfibusSimulator( pbfile.pbcfg );
        }
        
        return Sim;
    }
}
