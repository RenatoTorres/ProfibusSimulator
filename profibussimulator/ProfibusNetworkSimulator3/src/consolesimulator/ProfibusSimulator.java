/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package consolesimulator;


import MDBAccess.PBCfg;
import common.framestructure.ProfibusFrame;
import exceptions.ProfibusFrameException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import view.fdl.FDLAnalysis.FDLTelegramsTable;
/**
 *
 * @author Renato Veiga Torres
 */
public class ProfibusSimulator extends Thread {

    private int RefreshRate;
    ProfibusFrame CurrentRequest, CurrentResponse;
    private JTextArea outputWindow = null;
    private FDLTelegramsTable outputTable = null;
    private boolean stopThisThread;
    private int tq;
    private int whosnext = 0;
    public ProfibusStationSimulator SM;
    ArrayList SenderQueue;
    private boolean MasterRequest = true;
//    private SlavesPanel LocalLL;
    private int order = 0;
    private Date TimeNow = new Date();
    private float fTStamp = 0;
    private boolean bLastResponseValid = false;
    private Date globalDate;
    private Long globalTS;
    private boolean bLastRequestIsTokenForItself = false;
    static protected float fTLastFramePropagationTime = 0;
    
    public boolean isRunningEnable = true;

    public ProfibusSimulator( )
    {
        SM = new ProfibusStationSimulator( );
        
        SenderQueue = new ArrayList( );

        setStopThisThread(false);
        RefreshRate = 10;
        tq = 0;

        SM.Init( );
    }

    public ProfibusSimulator(PBCfg pbcfg)
    {
        SM = new ProfibusStationSimulator( pbcfg );
        
        SenderQueue = new ArrayList( );

        setStopThisThread(false);
        RefreshRate = 10;
        tq = 0;

        SM.Init( );
    }
    
    public void run()
    {
        globalDate = new Date( );
        globalTS   = new Long (globalDate.getTime());
        fTStamp    = 0;//globalTS.floatValue();
        
        while( isRunningEnable ) {
            if( isStopThisThread() == true )  {
                yield( );
            }
            else  {
                try {
                    BuildNextTelegram();
                } catch (ProfibusFrameException ex) {
                    Logger.getLogger(ProfibusSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try  {
                sleep( getRefreshRate() );
            } 
            catch (InterruptedException ex) {
                Logger.getLogger( ProfibusSimulator.class.getName() ).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void BuildNextTelegram( ) throws ProfibusFrameException
    {
        SimulateNextTelegram();
        SetFDLTelegramsTable(CurrentRequest);
        SetFDLTelegramsTable(CurrentResponse);
    }

    private void SimulateNextTelegram() throws ProfibusFrameException
    {
      CurrentRequest  = null;
      CurrentResponse = null;

      CurrentRequest = SM.MasterSendRequest( );
      SM.SlaveReceiveRequest( CurrentRequest );
      GenerateRequestTimeStamp( CurrentRequest );
      EnqueueFrame( CurrentRequest );

      //Used to calculate timestamp
      bLastRequestIsTokenForItself = IsTokenForItself( CurrentRequest );

      CurrentResponse = SM.SlaveSendResponse( );
      SM.MasterReceiveResponse( CurrentResponse );

      if( CurrentResponse != null ) {
        GenerateResponseTimeStamp( CurrentResponse );
        bLastResponseValid = true;
      }
      else {
          bLastResponseValid = false;
      }

      //if( frame.getFrameService() == Service.PB_RES_GET_DIAG );
      //  System.out.println("Diag Response!!!");

      
      EnqueueFrame( CurrentResponse );
    }

    protected String InsertSpaces( String in )
    {
        String out="";
        for(int i=0; i < in.length(); i+=2)
        {
            out+=in.substring(i, i+2)+" ";
        }
        return out;
    }

    protected void EnqueueFrame(ProfibusFrame p )
    {
        if( p != null ) {
            if( SenderQueue.size() < 100 )
                SenderQueue.add( tq++, p );
            order++;
        }
    }

    protected String DequeueFrame( )
    {
        if( SenderQueue.isEmpty() == false )
        {
            tq--;
            return ((ProfibusFrame) SenderQueue.remove( 0 )).toString();
        }
        else
            return "";
    }

    public String getNextFrame( )
    {
        return DequeueFrame( );
    }

    public boolean IsFrameAvaiable( )
    {
        return !SenderQueue.isEmpty();
    }

    public void notifyStop( )
    {
        setStopThisThread(true);
    }

    public void notifyStart( )
    {
        if( this.isAlive() == true )
            setStopThisThread(false);
        else
            this.start();
    }

    public void setOutputWindow(JTextArea jt)
    {
        outputWindow = jt;
    }

    /**
     * @return the stopThisThread
     */
    public boolean isStopThisThread() {
        return stopThisThread;
    }

    /**
     * @param stopThisThread the stopThisThread to set
     */
    public void setStopThisThread(boolean stopThisThread) {
        this.stopThisThread = stopThisThread;
    }

    /**
     * @return the RefreshRate
     */
    public int getRefreshRate() {
        return RefreshRate;
    }

    /**
     * @param RefreshRate the RefreshRate to set
     */
    public void setRefreshRate(int RefreshRate) {
        this.RefreshRate = RefreshRate;
    }

    public int getFrameBufferLength() {
        return SenderQueue.size();
    }

//     public void RegisterLiveListAction(SlavesPanel LiveListPanel) {
//        LocalLL = LiveListPanel;
//    }

    /**
     * @return the outputTable
     */
    public FDLTelegramsTable getOutputTable() {
        return outputTable;
    }

    /**
     * @param outputTable the outputTable to set
     */
    public void setOutputTable(FDLTelegramsTable outputTable) {
        this.outputTable = outputTable;
    }

    public void SetSlaveOnline(Integer integer, boolean b) {
        SM.SetSlaveOnline(integer,  b);
    }

    private void SetFDLTelegramsTable(ProfibusFrame p) {
        FDLTelegramsTable fdl;
        
        if( p == null )
            return;

        String s = p.toString();

        if( s != null )
        {
            s = InsertSpaces( s ) +"\n";
            fdl = getOutputTable();
            fdl.setProfidoctorPackage( p );
            System.out.println( s );
        }
    }

    private void GenerateRequestTimeStamp(ProfibusFrame frame_request) {
        float bitTimeMs  = SM.getCurrentMaster( frame_request.getSourceAddr() ).getBitTimeMs( );
        float SlotTimeMs = SM.getCurrentMaster( frame_request.getSourceAddr() ).getSlotTime() * bitTimeMs;
        float setupTime  = SM.getCurrentMaster( frame_request.getDestAddr() ).getSetupTime( ) * bitTimeMs;
        float quietTime  = SM.getCurrentMaster( frame_request.getDestAddr() ).getQuietTime( ) * bitTimeMs;

        //Normative Parts - Part 4 - Timer Operation
        float TSET    = setupTime;   //TSET: Setup Time - tempo de reacao a uma interrupcao ou ao inicio da recepcao.
        float TQUI    = quietTime;   //TQUI: Quiet time - usado quando ha repetidores (antigo)
        float minTSDR;               //minTSDR: Tempo minimo para resposta de uma estacao (aqui nao ha)
        float TSDI;                  //Tempo minimo para uma nova requisicao do mestre (request ou token)
        float TTD     = 0.5f*bitTimeMs;       //TTD: Transmission delay time - used for distance delay;

        //MinTSDR - minimo tempo para uma nova pergunta do mestre
        minTSDR = SM.getCurrentMaster( frame_request.getSourceAddr() ).getMinTSDR( ) * bitTimeMs;
        TSDI = minTSDR;

        //TSYN - Synchronization Time
        float TSyn = (33 * bitTimeMs );

        //TSM - Safety Margin TSM
        float TSM = 2*bitTimeMs + 2*TSET + TQUI;

        //TIDLE 1 is Max of TSYN + TSM, minTSDR, TSDI
        float TID1 = Max(TSyn + TSM, minTSDR, TSDI);

        //NOTE VEIGA - IMPORTANT!!!
        //This is for current FRAME - but we need to use last one and
        //store this value for next frame calculation
        float TDataCurrentFrame = frame_request.getNumBits( ) * bitTimeMs;
        
        //Add time to timestamp;
        //If NO last response, TSlot must be included on TStamp
        if( ( (bLastResponseValid == false) && (bLastRequestIsTokenForItself == false) ) || (SM.Master.retryProcess == true)  )
            fTStamp += fTLastFramePropagationTime + SlotTimeMs;
        else
            fTStamp += fTLastFramePropagationTime + TID1 + TTD;
        
        fTLastFramePropagationTime = TDataCurrentFrame;
                
        frame_request.setTimeFrame( fTStamp );
    }


    private void GenerateResponseTimeStamp(ProfibusFrame frame_response) {
        float bitTimeMs = SM.getCurrentMaster( frame_response.getDestAddr() ).getBitTimeMs( );
        float setupTime = SM.getCurrentMaster( frame_response.getDestAddr() ).getSetupTime( ) * bitTimeMs;
        float quietTime = SM.getCurrentMaster( frame_response.getDestAddr() ).getQuietTime( ) * bitTimeMs;
        float MaxTSDR   = SM.getCurrentMaster(frame_response.getDestAddr() ).getMaxTSDR() * bitTimeMs;

        float TSET    = setupTime;   //TSET: Setup Time - tempo de reacao a uma interrupcao ou ao inicio da recepcao.
        float TQUI    = quietTime;   //TQUI: Quiet time - usado quando ha repetidores (antigo)
        float TTD     = 0.5f*bitTimeMs;       //TTD: Transmission delay time - used for distance delay;

        //TSYN - Synchronization Time
        float TSyn = (33 * bitTimeMs );

        //TSM - Safety Margin TSM
        float TSM = 2*bitTimeMs; // //2*TSET + TQUI;

        //TIDLE 1 is Max of TSYN + TSM, minTSDR, TSDI
        float TID2 = Max(TSyn + TSM, MaxTSDR, 0);

        //TODO: TEST !!!!!!!!!!!!!!!!!!!
        TID2 = SM.getCurrentMaster(frame_response.getDestAddr() ).getMinTSDR() * bitTimeMs;
        
        //NOTE VEIGA - IMPORTANT!!!
        //This is for current FRAME - but we need to use last one and
        //store this value for next frame calculation
        float TData = frame_response.getNumBits( ) * bitTimeMs;

        //Compute slave time to response
        fTStamp += fTLastFramePropagationTime + TID2 + TTD;
        
        fTLastFramePropagationTime = TData;

        frame_response.setTimeFrame( fTStamp );
    }

    private float Max(float f1, float f2, float f3) {
        if( f1 > f2 ) {
            if( f1 > f3)
                return f1;
            else
                return f3;
        }
        else {
            if( f2 > f3 )
                return f2;
            else
                return f3;
        }
    }

    private boolean IsTokenForItself(ProfibusFrame req) {
        if( (req.getSourceAddr() == req.getDestAddr()) && (req.IsToken() == true) )
            return true;
        else
            return false;
    }
    
    public ProfibusSlaveCollection getSlaveCollection( )
    {
        return SM.getSlaveSimulationCollection();
    }

    public boolean isSlaveOnline(int address) {
        if( SM.isSlaveCollection( ) == true && (SM.getSimulatedSlave(address) != null) )
            return SM.getSimulatedSlave(address).isOnline();
        else
            return false;
    }

    public void setSlaveVoltage(int address, int newvalue) {
        if( SM.isSlaveCollection( ) == true )
            SM.getSimulatedSlave(address).setVoltage( newvalue );
    }

    public int getSlaveVoltage(int address) {
        if( SM.isSlaveCollection( ) == true )
            return SM.getSimulatedSlave(address).getVoltage( );
        else
            return ProfibusSlave.DEFAULT_VOLTAGE;
    }

    public boolean getReadableSignal(int address) {
        if( SM.isSlaveCollection( ) == true )
            return SM.getSimulatedSlave(address).isReadableSignal();
        else
            return true;
    }

    public int getSlavePhyProblem(int address) {
        if( SM.isSlaveCollection( ) == true )
            return SM.getSimulatedSlave(address).getSlavePhyProblem(address);
        else
            return -1;
    }
    
    public void setSlavePhyProblem(int address, int problem) {
        if( SM.isSlaveCollection( ) == true )
            SM.getSimulatedSlave(address).setSlavePhyProblem(problem);
    }

    public void setReadable(int address, boolean selected) {
        if( SM.isSlaveCollection( ) == true )
            SM.getSimulatedSlave(address).setReadableSignal(selected);
    }

    public void notifyFinish() {
        isRunningEnable = false;

        try {
            finalize();
        } catch (Throwable ex) {
            Logger.getLogger(ProfibusSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
