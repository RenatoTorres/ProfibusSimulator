/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;

import MDBAccess.PBCfg;
import SlaveDialog.DiagAndCfgStationInterface;
import common.framestructure.ProfibusFrame;
import exceptions.ProfibusFrameException;
import java.util.ArrayList;
import view.fdl.FDLAnalysis.FDLTelegramsTable;

/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusStationSimulator
{
    ArrayList MasterArray;
    ArrayList SlaveArray;
    PBCfg pbcfg;
    
    public ProfibusMaster Master;
    public ProfibusSlaveCollection SlaveSim;

    ProfibusStationSimulator(PBCfg pbcfg) {
        this.pbcfg = pbcfg;
    }

    ProfibusStationSimulator() {
        
    }

    public void Init( )
    {
        
        if( pbcfg != null )
        {
            Init( pbcfg );
        }
        else
        {
            InitOld( );
        }
    }
    
    void InitOld( )
    {
        // Creating components
            MasterArray = new ArrayList( );
            SlaveSim = new ProfibusSlaveCollection( );

            MasterArray.add( 0, new ProfibusMaster( 1 ) );

            Master = (ProfibusMaster) MasterArray.get( 0 );

            for(int i=0; i < ProfibusConstants.MAX_ADDRESS; i++ ) {
                if (Master.isSlaveConfiguredStartOnline( i ) == true )
                {
                    SetSlaveOnline(i, true);
                }

                int extdiagsize = Master.getSlaveExtendedDiagSize( i );
                if( extdiagsize != 0 ) {
                    SetSlaveDiagSize(i, extdiagsize );
                }

            }

    }
    
    void Init(PBCfg pbcfg)
    {
        // Creating components
        MasterArray = new ArrayList( );
        SlaveSim = new ProfibusSlaveCollection( );

        MasterArray.add( 0, new ProfibusMaster( pbcfg ) );

        Master = (ProfibusMaster) MasterArray.get( 0 );
        
        for(int i=0; i < ProfibusConstants.MAX_ADDRESS; i++ ) {
            if (Master.isSlaveConfiguredStartOnline( i ) == true )
            {
                SetSlaveOnline(i, true);
            
                if( FDLTelegramsTable.bMirrorToSimulated == true )
                {
                    //Copy all data of slaves loaded from pbfile to simulated devices
                    //Configure for real device
                    ProfibusSlave pbSimSlave = SlaveSim.getSlave(i);
                    ProfibusSlave pbslave = Master.getConfiguredSlave( i );
                    pbSimSlave.setNativeCfg( pbslave.getNativeCfg( ) );
                    pbSimSlave.setIdentNumber( pbslave.getIdentNumberBytes() );
                }
            }
            
        /*int extdiagsize = Master.getSlaveExtendedDiagSize( i );
        if( extdiagsize != 0 ) {
            SetSlaveDiagSize(i, extdiagsize );
        }*/
        }
    }    

    public ProfibusFrame MasterSendRequest( ) throws ProfibusFrameException
    {
        //Return Request From Master
        return Master.runMachine( );
    }

    public ProfibusFrame SlaveSendResponse()
    {
        return SlaveSim.getFrameResponse();
    }

    public ProfibusSlave getSimulatedSlave(int i)
    {
        return SlaveSim.getSlave( i );
    }

    void SetOnlineList(boolean[] ll)
    {
        for(int i=0; i < ll.length; i++)
            SlaveSim.setOnline(i, ll[i]);
    }

    void MasterReceiveResponse(ProfibusFrame CurrentPacket) {
        Master.ReceiveResponse( CurrentPacket );
    }

    void SlaveReceiveRequest(ProfibusFrame CurrentFrame) {
        SlaveSim.ReceiveRequest(CurrentFrame);
    }

    void SetSlaveOnline(Integer integer, boolean b) {
            SlaveSim.setOnline(integer, b);
    }

    ProfibusMaster getCurrentMaster(int Addr) {
        return (ProfibusMaster) MasterArray.get( 0 );
    }
    
    ProfibusSlaveCollection getSlaveSimulationCollection( )
    {
        return SlaveSim;
    }

    boolean isSlaveCollection() {
        return SlaveSim.isSlaveArray( );
    }

    private void SetSlaveDiagSize(int i, int extdiagsize) {
        SlaveSim.SetSlaveDiagSize(i, extdiagsize);
    }

}
