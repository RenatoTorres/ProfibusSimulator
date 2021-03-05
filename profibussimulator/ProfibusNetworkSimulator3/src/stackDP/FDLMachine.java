/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackDP;

import common.framestructure.ProfibusFrame;
import common.framestructure.ProfibusServices;
import common.framestructure.Service;
import exceptions.ProfibusFrameException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author renato_veiga
 */
public class FDLMachine extends Thread {
    public static final byte FDL_OFFLINE_0             = 0;
    public static final byte FDL_LISTEN_TOKEN_1        = 1;
    public static final byte FDL_ACTIVE_IDLE_2         = 2;
    public static final byte FDL_CLAIM_TOKEN_3         = 3;
    public static final byte FDL_USE_TOKEN_4           = 4;
    public static final byte FDL_AWAIT_DATA_RESPONSE_5 = 5;
    public static final byte FDL_CHECK_ACCESS_TIME_6   = 6;
    public static final byte FDL_PASS_TOKEN_7          = 7;
    public static final byte FDL_CHECK_TOKEN_PASS_8    = 8;
    public static final byte AWAIT_STATUS_RESPONSE_9   = 9;

    protected final byte LAS_STATION_OFFLINE                = 0;
    protected final byte LAS_STATION_NOT_LOGICAL_TOKEN_RING = 1;
    protected final byte LAS_STATION_IN_LOGICAL_TOKEN_RING  = 2;
    
    protected byte fdl_machine_state = FDL_OFFLINE_0;
    protected byte last_state        = FDL_OFFLINE_0;

    private DPLineTCP DPChannel;
    
    protected Timer TTOTimer;    
    private int Tsl;
    private int MasterAddr;

    int ttrcycles = 0;
    byte[] LAS = new byte[126];
    int tempSA = 0;
    int temDA = 0;

    
    FDLMachine(DPLineTCP dpl)
    {
        DPChannel = dpl;
    }
    
    @Override
    public void run( )
    {
        
        //PON
        //PowerOnResetFDL( );
        
        switch( fdl_machine_state )
        {
            case FDL_OFFLINE_0:   //state 0
                //Starting-up
                setupConfiguration( );
                tempSA    = 0;
                temDA     = 0;
                ttrcycles = 0;
                
                fdl_machine_state = FDL_LISTEN_TOKEN_1;
                
                TTOTimer.setTimerValueAndStart( CalculateTTO() );
                break;
                
            case FDL_LISTEN_TOKEN_1:  //state 1
                //Stay here waiting for timeout or for token cicle
                fdl_machine_state = FDL_LISTEN_TOKEN_1;

                try {
                    DPChannel.waitInputTelegram( );
                } catch (IOException ex) {
                    Logger.getLogger(FDLMachine.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if( DPChannel.isNewResponse( ) == false )
                {
                    //NO RESPONSE
                    //wait timer
                    ttrcycles = 0;
                    
                    if( TTOTimer.isEnd() == true )
                    {
                        //No activity - GO to Claim Token
                        fdl_machine_state = FDL_CLAIM_TOKEN_3;
                    }
                }
                else
                {
                    //ANY RESPONSE FOUND! -> GO ANALYSE
                    
                    //communication running
                    ProfibusFrame pf = null;

                    try {
                        pf = DPChannel.getInputFrame();
                    } catch (ProfibusFrameException ex) {
                        Logger.getLogger(FDLMachine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    int sourceAddr = pf.getSourceAddr();
                    int destAddr   = pf.getDestAddr();

                    if( pf.IsToken() == true  )
                    {
                        BuildLAS( pf );
                        
                        //Compute TTR cycles
                        if( tempSA == sourceAddr && temDA == destAddr )
                        {
                            //Wait for 2 same token frames
                            ttrcycles += 1;
                        }
                    }
                    else
                    {
                        if( sourceAddr == MasterAddr )
                        {
                            //Response Frame with same MASTER ADDRESS
                            //DUPLICATED MASTER ADDRESS
                            //GO OFFLINE
                            fdl_machine_state = FDL_OFFLINE_0;                            
                        }
                        else if( destAddr == MasterAddr )
                        {
                            ResponseReqFDLStatus( pf );
                        }
                    }
                    
                    //After 2 complete TTR cycles
                    if( ttrcycles >= 2 )
                    {
                        //Wait for 2 identical cycles of token rotation 
                        //and go to FDL_ACTIVE_IDLE_2
                        TTOTimer.setTimerValueAndStart( CalculateTTO() );
                        fdl_machine_state = FDL_ACTIVE_IDLE_2;
                    }
                }
                break;
                
            case FDL_ACTIVE_IDLE_2:  //state 2
                //
                //fdl_machine_state = FDL_ACTIVE_IDLE_2;
                
                //Em caso de erro ou comando do usuario
                //if( UserCommand() == EXIT_FROM_LOGICAL_TOKEN_RING)
                //fdl_machine_state = FDL_LISTEN_TOKEN_1;
                
                try {
                    DPChannel.waitInputTelegram( );
                } catch (IOException ex) {
                    Logger.getLogger(FDLMachine.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if( DPChannel.isNewResponse( ) == false )
                {
                    //NO RESPONSE
                    //wait timer
                    if( TTOTimer.isEnd() == true )
                    {
                        //No activity - GO to Claim Token
                        fdl_machine_state = FDL_CLAIM_TOKEN_3;
                    }
                }
                else
                {
                     //Monitor token pass to myself and request fdl status

                }
                
                //Em caso de timeout
                fdl_machine_state = FDL_CLAIM_TOKEN_3;
                fdl_machine_state = FDL_USE_TOKEN_4;
                break;
                
            case FDL_CLAIM_TOKEN_3:  //state 3
                //
                fdl_machine_state = FDL_USE_TOKEN_4;
                fdl_machine_state = FDL_PASS_TOKEN_7;
                break;
                
            case FDL_USE_TOKEN_4:  //state 4
                //
                fdl_machine_state = FDL_USE_TOKEN_4;
                fdl_machine_state = FDL_CHECK_ACCESS_TIME_6;
                fdl_machine_state = FDL_AWAIT_DATA_RESPONSE_5;
                fdl_machine_state = FDL_ACTIVE_IDLE_2;
                break;
                
            case FDL_AWAIT_DATA_RESPONSE_5:  //state 4
                //
                fdl_machine_state = FDL_AWAIT_DATA_RESPONSE_5;
                fdl_machine_state = FDL_USE_TOKEN_4;
                fdl_machine_state = FDL_ACTIVE_IDLE_2;
                break;
                
            case FDL_CHECK_ACCESS_TIME_6:  //state 6
                //
                fdl_machine_state = FDL_USE_TOKEN_4;
                fdl_machine_state = FDL_PASS_TOKEN_7;
                break;
                
            case FDL_PASS_TOKEN_7:  //state 7
                //
                fdl_machine_state = FDL_CHECK_TOKEN_PASS_8;
                fdl_machine_state = FDL_CHECK_ACCESS_TIME_6;
                fdl_machine_state = FDL_USE_TOKEN_4;
                fdl_machine_state = FDL_LISTEN_TOKEN_1;
                fdl_machine_state = FDL_AWAIT_DATA_RESPONSE_5;
                break;
                
            case FDL_CHECK_TOKEN_PASS_8:  //state 8
                //
                fdl_machine_state = FDL_CHECK_TOKEN_PASS_8;
                fdl_machine_state = FDL_ACTIVE_IDLE_2;
                fdl_machine_state = FDL_PASS_TOKEN_7;
                break;
                
            case AWAIT_STATUS_RESPONSE_9:  //state 9
                //
                fdl_machine_state = FDL_AWAIT_DATA_RESPONSE_5;
                fdl_machine_state = FDL_ACTIVE_IDLE_2;
                fdl_machine_state = FDL_PASS_TOKEN_7;
                break;
        }
    }

    private void setupConfiguration() {
        Tsl = 200;  //Tbit
    }
    
    private int CalculateTTO( )
    {
        return 6*Tsl + 2*MasterAddr*Tsl;
    }

    public void Power_on() throws ProfibusFrameException, IOException {
        start();
    }

    private void ResponseReqFDLStatus(ProfibusFrame pf) {
        //Verify if frame is Request FDL Status
        if( pf.IsReq() == true )
        {
            if( pf.getFrameService() == Service.PB_REQ_FDL_STATUS )
            {
                //Request FDL Status to myself
                //Response now
                int sourceAddr = pf.getSourceAddr();
                int destAddr   = pf.getDestAddr();
                ProfibusFrame pfr = ProfibusServices.SendServiceRequest(sourceAddr, destAddr, new Service(Service.PB_RES_FDL_STATUS) );
                //TODO - modify pfr to READY TO ENTER IN LOGICAL TOKEN RING
                //pfr.

                try {
                    //Send to line
                    DPChannel.setOutputFrame(pfr);
                } catch (IOException ex) {
                    Logger.getLogger(FDLMachine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                //TODO - Response with RESOURCE UNAVAIABLE

            }
        }
        else
        {
            //Response Frame to destination with same MASTER ADDRESS
            //DUPLICATED MASTER ADDRESS
            //GO OFFLINE
            fdl_machine_state = FDL_OFFLINE_0;                                  
        }
    }

    private void BuildLAS(ProfibusFrame pf) {
        int sourceAddr = pf.getSourceAddr();
        int destAddr   = pf.getDestAddr();
        if( (sourceAddr < 126) && (LAS[sourceAddr] == LAS_STATION_OFFLINE) )
        {
            //Store SA into LAS list
            LAS[ sourceAddr ] = LAS_STATION_IN_LOGICAL_TOKEN_RING;
            tempSA = sourceAddr;
            temDA  = destAddr;
        }

    }
}
