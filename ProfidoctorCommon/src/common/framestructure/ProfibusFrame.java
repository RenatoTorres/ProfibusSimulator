/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common.framestructure;
import common.ConversionsAdvanced;
import common.signals.CurtoCircuitoAB;
import common.signals.ExcessoTerminadores;
import common.signals.FaltaTerminadoresOuCaboQuebrado;
import common.signals.Signal;
import common.signals.SignalType;
import dataunits.GeneralDataUnit;
import dataunits.GeneralDataUnitStatic;
import exceptions.ProfibusFrameException;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe para codificação/decodificação de frames Profibus DP/PA.
 * <br>
 * O objetivo desta classe é oferecer ao usuário uma interface de criação para
 * os pacotes Profibus sem a necessidade de conhecer a codificação do protocolo.
 * Os serviços que podem ser utilizados podem ser encontrados na classe
 * Services.
 * <br>
 * Os codigos presentes ao protocolo estão presentes nas classes SD, Service,
 * FC e PROTO_TYPE.
 *
 *
 * @author Renato da Veiga Torres
 * @version 1.0
 * @see Service
 * @see FC
 * @see SD
 * @see PROTO_TYPE
 */
public class ProfibusFrame
{
    private byte[]  RawData;
    private byte[]  Data_Unit;
    private byte[] Samples;
    private byte SourceAddr;
    private byte DestAddr;
    private byte FCByte;
    private PROTO_TYPE pb_type = PROTO_TYPE.PROFIBUS_DP;
    private Date TimeStamp;
    private float msTime;
    private SD    frameSD;
    private Service frameService;
    private FC frameFC;
    private boolean B6,B5,FCV,FCB;
    private static final int NUM_SAMPLES_PER_BIT = 5;
    private static final float SAMPLE_HIGH_LEVEL = 5.1f;
    private static final float SAMPLE_LOW_LEVEL  = 0.0f;
    private boolean bReadableFrame = true;
    private int iSignalProblem = SignalType.SIGNAL_NO_PROBLEM;
    private float fVoltageLevel = 5.0f;
    private GeneralDataUnit ServiceDataUnit;

    /**
    * Retorna o codigo (protocolo) do tipo do frame (SD).
    * 
    * @return SD code of frame (SD1, SD2, SD3, SD4, SC)
    */
    private byte getSDType() {
        return frameSD.getSD();
    }

    public void setSDType( byte ucval ) {
        frameSD.setSD( ucval );
        Encode( );
    }

    /**
    * Retorna o codigo (protocolo) do tipo do frame (SD).
    *
    * @return SD code of frame (SD1, SD2, SD3, SD4, SC)
    */
    void setStationType(byte b) {
        frameFC.setStationType(b);
    }

    /**
    * Retorna o codigo (protocolo) do tipo do frame (SD).
    *
    * @return SD code of frame (SD1, SD2, SD3, SD4, SC)
    */
    public byte getSD() {
        return frameSD.getSD();
    }

    public FC getFCObject() {
        return frameFC;
    }

    /**
     * @return the Data_Unit
     */
    public byte[] getData_Unit() {
        return Data_Unit;
    }

    /**
     * @param Data_Unit the Data_Unit to set
     */
    public void setData_Unit(byte[] du) {
        Data_Unit = du;
        Encode( );
    }

    public static enum PROTO_TYPE { PROFIBUS_DP, PROFIBUS_PA }

    public int getSamplesPerBit() {
        return NUM_SAMPLES_PER_BIT;
    }

    protected void init()
    {
        frameSD       = new SD(SD.SD_NULL);
        frameService  = new Service(Service.PB_NULL);
        frameFC       = new FC(FC.FC_NULL);
        TimeStamp     = new Date();
    }

    public ProfibusFrame(int address, int destAddr, byte function_service )
    {
        init();
        AssembleFrame( address, destAddr, function_service);
    }

    public ProfibusFrame(SD frameSD ) {
        init( );
        setSDType(frameSD.getSD());
    }

    public void Update( )
    {
        AssembleFrame( getSourceAddr(), getDestAddr(), frameService.getService());
    }

    public ProfibusFrame( String framedata ) throws ProfibusFrameException
    {
        init();
        Decode( ConversionsAdvanced.hexStringToByteArray( framedata ) );
        setTimeStamp( Calendar.getInstance().getTime() );
    }

    public ProfibusFrame( byte[] byteframedata ) throws ProfibusFrameException
    {
        this( ConversionsAdvanced.toStringFromStream( byteframedata ) );
    }


    /**
     * @return the FrameType
     */
    public SD getFrameType() {
        return frameSD;
    }

    /**
     * @return the FrameService
     */
    public byte getFrameFC() {
        return frameFC.getFC();
    }

    /**
     * @return the FrameService
     */
    public String getFrameFCString() {
        return frameFC.toString();
    }


    /**
     * @return the FrameService
     */
    public byte getFrameService() {
        return frameService.getService();
    }

    public String getFrameServiceString() {
        return frameService.toString();
    }


    /**
     * @param FrameService the FrameService to set
     */
    public void setFrameFunction(Service FrameFunction) {
        this.frameService = FrameFunction;
    }

    /**
     * @return the IsReq
     */
    public boolean IsReq() {
        return frameFC.IsReq( );
    }

    public void Encode( )
    {
        Encode( this );
        
    }

    public void Encode( ProfibusFrame pf )
    {
        int fsize, aux=0;
        switch( pf.getSDType() )
        {
            case SD.SD1:
                //10 dest source FCByte <checksum> <0x16h>
                fsize = 6;
                aux = 0;
                RawData = new byte[fsize];
                RawData[aux++] = (byte) SD.SD1;
                RawData[aux++] = DestAddr;
                RawData[aux++] = SourceAddr;

                RawData[aux++] = frameFC.EncodeFC( );
                
                if( pb_type == PROTO_TYPE.PROFIBUS_DP )
                {
                    //CheckSum + End Delimiter (0x16)
                    RawData[aux++] = CheckSumDP(RawData, aux );
                    RawData[aux++] = 0x16;
                }
                else
                {
                    //CheckSum => CRC16

                }
                Samples = BuildSignalSamples( RawData, 3 );
                break;

            case SD.SD2:
                //68 size size 68 dest source FCByte <data> <checksum> <0x16h>
                byte tmpData[] = new byte[255];
                
                tmpData[aux++] = SD.SD2;
                tmpData[aux++] = 0;  //will be filled at end
                tmpData[aux++] = 0;
                tmpData[aux++] = SD.SD2;

                if( isIsSAP() == true )
                {
                    tmpData[aux++] = (byte) (DestAddr   | 0x80);
                    tmpData[aux++] = (byte) (SourceAddr | 0x80);
                    tmpData[aux++] = frameFC.EncodeFC( );
                    tmpData[aux++] = frameService.getDSAP();
                    tmpData[aux++] = frameService.getSSAP();
                }
                else
                {
                    tmpData[aux++] = DestAddr;
                    tmpData[aux++] = SourceAddr;
                    tmpData[aux++] = frameFC.EncodeFC( );
                }

                if( Data_Unit != null ) {
                    for(int i=0; i < Data_Unit.length; i++ )
                    {
                        tmpData[aux++] = Data_Unit[ i ];
                    }
                }
                
                tmpData[ 1 ] = (byte) (aux - 4);    //Length computation
                tmpData[ 2 ] = tmpData[ 1 ];

                if( pb_type == PROTO_TYPE.PROFIBUS_DP )
                {
                    //CheckSum + End Delimiter (0x16)
                    tmpData[aux++] = CheckSumDP( tmpData, aux );
                    tmpData[aux++] = 0x16;
                }
                else
                {
                    //CheckSum => CRC16

                }

                //Copy to RawData with correct size
                RawData = new byte[aux];
                System.arraycopy(tmpData, 0, RawData, 0, aux);
                Samples = BuildSignalSamples( RawData, 3 );
                ServiceDataUnit = GeneralDataUnitStatic.DecodeDataUnit(this);
                break;
            
            case SD.SD3:
                break;

            case SD.SD4:
                //DC dest source
                fsize = 3;
                aux = 0;
                RawData = new byte[fsize];
                RawData[aux++] = SD.SD4;
                RawData[aux++] = DestAddr;
                RawData[aux++] = SourceAddr;

                if( pb_type != PROTO_TYPE.PROFIBUS_DP )
                {
                    //CheckSum => CRC16
                    //TODO: ADAPTAR PARA O TOKEN PARA PROFIBUS PA - FUTURO!
                }
                Samples = BuildSignalSamples( RawData, 3 );
                break;

            case SD.SC:
                RawData = new byte[1];
                RawData[aux++] = SD.SC;
                
                if( pb_type != PROTO_TYPE.PROFIBUS_DP )
                {
                    //CheckSum => CRC16
                }
                Samples = BuildSignalSamples( RawData, 3 );
                break;

            default:
                break;
        }
    }

    int Decode( byte[] inputFrame ) throws ProfibusFrameException
    {
        int iret = 0;
        int inputSize = inputFrame.length;

        //Discard invalid sizes
        if( inputSize < 1 ) {
            return -1;
        }

        setRawData( inputFrame );

        //Decoding Frame Type
        byte sdtype =  (byte) inputFrame[0];

        switch( sdtype )
        {
            case SD.SC:      //short answer
                frameSD.setSD( SD.SC );
                frameService.setFfunction(Service.PB_NULL);
                break;

            case SD.SD1:      //fixed frame size 10 xy zw kf
                frameSD.setSD( SD.SD1 );
                setDestAddr( inputFrame[1] );
                setSourceAddr(  inputFrame[2] );
                
                //Verify between data_exchange_without_outputs and request/response_fdl_status
                //Use FCByte to do this
                frameFC.DecodeFC( inputFrame[3] );

                switch( frameFC.getFC() )
                {
                    case FC.FC_REQ_FDL_STATUS_WITH_REPLY:
                        //Request FDL Status
                        frameService.setFfunction(Service.PB_REQ_FDL_STATUS);
                        break;

                    case FC.FC_RES_FDL_HIGH:
                    case FC.FC_RES_FDL_LOW:
                    case FC.FC_NULL:
                        //Request FDL Status
                        frameService.setFfunction(Service.PB_RES_FDL_STATUS);
                        break;

                    case FC.FC_REQ_SEND_DATA_HIGH:
                        //Data Exchange Request
                        frameService.setFfunction( Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS);
                        break;
                    default:
                     throw new ProfibusFrameException(sdtype, "The value of field FC "
                        + "must be: ????????????????? QUAIS VALORES?");

                }
               
                break;


            case SD.SD2:      //variable frame size 68 xy xy 68 DA SA FC DSAP SSAP....
                frameSD.setSD(SD.SD2);
                setDestAddr(   (byte)(inputFrame[4] & 0x7F) );
                setSourceAddr( (byte)(inputFrame[5] & 0x7F) );

                int LE = inputFrame[1] & 0x7F;
                
                //Get FCByte Byte
                frameFC.DecodeFC( inputFrame[6] );
                
                if( (inputFrame[4] & 0x80) != 0 )
                {
                    //SAP Found
                    frameService.setFfunction( inputFrame[7], inputFrame[8] );
                    //SlaveDiag example
                    //68 12 12 68 |81 A3 08 |3E 3C 02 05 00 FF 07 9A 07 00 00 00 00 00 00| 54 16
                    
                    int k=0;
                    int datalen = LE-3;
                    Data_Unit = new byte[ datalen ];

                    for(int i=9; k < datalen; i++, k++ )
                    {
                        Data_Unit[k] = inputFrame[i];
                    }
                }
                else
                {
                    //Data Exchange!
                    //68 12 12 68 |01 03 08 |00 00 02 05 00 FF 07 9A 07 00 00 00 00 00 00| 54 16
                    if( IsReq() == true )
                        frameService.setFfunction(Service.PB_REQ_DATA_EXCHANGE);
                    else
                        frameService.setFfunction(Service.PB_RES_DATA_EXCHANGE);
                    
                    int k=0;
                    int datalen = LE-3;
                    Data_Unit = new byte[ datalen ];

                    for(int i=7; k < datalen; i++, k++ )
                    {
                        Data_Unit[k] = inputFrame[i];
                    }
                }
                ServiceDataUnit = GeneralDataUnitStatic.DecodeDataUnit(this);
                break;

            case SD.SD4:      //token pass DC xy zw
                frameSD.setSD(SD.SD4);
                setDestAddr(   inputFrame[1] );
                setSourceAddr( inputFrame[2] );
                frameService.setFfunction( Service.PB_REQ_TOKEN_PASS );
                break;

            default:
                throw new ProfibusFrameException(sdtype, "The value of field SD "
                        + "must be: 10H (SD1), 68H (SD2), A2H (SD3) or DCH (SD4) ");
        }
        return iret;
    }

    /**
     * @return the RawData
     */
    public byte[] getRawData() {
        return RawData;
    }

    /**
     * @return the RawData
     */
    public byte getRawData(int i) {
        return RawData[i];
    }

    public String toString( )
    {
        return ConversionsAdvanced.toStringFromStream( RawData );
    }

    public String getDirection( )
    {
        if( IsReq() == true )
            return "Request";
        else
            return "Response";
    }

    /**
     * @param RawData the RawData to set
     */
    public void setRawData(byte[] RawData) {
        this.RawData = RawData;
    }

    /**
     * @return the LevelSamples
     */
    public byte[] getLevelSamples() {
        return Samples;
    }
    
    /**
     * @return the LevelSamples
     */
//    public byte[] getLevelSamplesBytes() {
//        return BuildSignalSamples(RawData, NUM_SAMPLES_PER_BIT);
//    }
    

    /**
     * @param LevelSamples the LevelSamples to set
     */
    public void setLevelSamples(byte[] LevelSamples) {
        this.Samples = LevelSamples;
    }

    /**
     * @return the SourceAddr
     */
    public int getSourceAddr() {
        return SourceAddr;
    }

    /**
     * @param SourceAddr the SourceAddr to set
     */
    public void setSourceAddr(byte SourceAddr) {
        this.SourceAddr = SourceAddr;
    }

    /**
     * @return the DestAddr
     */
    public int getDestAddr() {
        return DestAddr;
    }

    /**
     * @param DestAddr the DestAddr to set
     */
    public void setDestAddr(byte DestAddr) {
        this.DestAddr = DestAddr;
    }

    /**
     * @return the TimeStamp
     */
    public Date getTimeStamp() {
        return TimeStamp;
    }

    public long getMsTimeStamp() {
        return TimeStamp.getTime();
    }

    public float getMsFrameTime( ) {
        return msTime;
    }

    public float getUsFrameTime( ) {
        return msTime*1000;
    }

    public void setTimeFrame( float ts ) {
        msTime = ts;
    }


    /**
     * @param TimeStamp the TimeStamp to set
     */
    public void setTimeStamp(Date TimeStamp) {
        this.TimeStamp = TimeStamp;
    }

    /**
     * @return the IsSAP
     */
    public boolean isIsSAP() {
        return frameService.isSAPFound();
    }

    /**
     * @return the SourceSAP
     */
    public int getSourceSAP() {
        return frameService.getSSAP();
    }

    /**
     * @param SourceSAP the SourceSAP to set
     */
    public void setSourceSAP(byte SourceSAP) {
        frameService.setSSAP( SourceSAP );
    }

    /**
     * @return the DestSAP
     */
    public int getDestSAP() {
        return frameService.getDSAP();
    }


    /**
     * @param DestSAP the DestSAP to set
     */
    public void setDestSAP(byte DestSAP) {
        frameService.setDSAP( DestSAP );
    }

    public byte getFC( )
    {
        return FCByte;
    }

    public byte CheckSumDP(byte ab[], int datalength )
    {
        int sum = 0;

        switch( frameSD.getSD() )
        {
            case SD.SD1:
                //10 dest source FCByte <checksum>
                for(int i=1; (i < 4) && (i < datalength); i++)
                    sum += ConversionsAdvanced.ByteToUnsignedInt( ab[i] );
                break;

            case SD.SD2:
                //68 size size 68 dest source FCByte <data> <checksum>
                for(int i=4; (i < (ab[1]+4)) && (i < datalength); i++)
                    sum += ConversionsAdvanced.ByteToUnsignedInt( ab[i] );

                break;
                
            default:
                break;
        }

        //Short to one byte
        sum = sum % 256;

        return (byte) sum;
    }

    public int getNumSignalSamples( byte rawdata[] )
    {
        return rawdata.length*8*NUM_SAMPLES_PER_BIT;
    }

//    public float[] BuildSignalSamples( byte rawdata[] )
//    {
//        return BuildSignalSamplesVeiga( rawdata, rawdata.length );
//    }
    
    public byte[] BuildSignalSamples( byte rawdata[], int numbytes )
    {
        byte[] sampleArray = null;
        
        //Verify signal type and build like selected
        switch( iSignalProblem )
        {
            case SignalType.SIGNAL_NO_PROBLEM:
            default:
                sampleArray = new Signal().createSamples( fVoltageLevel );
                break;

            case SignalType.SIGNAL_A_B_SHORT_CIRCUIT:
                sampleArray = new CurtoCircuitoAB().createSamples( fVoltageLevel );
                break;
                
            case SignalType.SIGNAL_TERMINATOR_EXCESS:
                sampleArray = new ExcessoTerminadores().createSamples( fVoltageLevel );
                break;
                
            case SignalType.SIGNAL_TERMINATOR_MISSING:
                sampleArray = new FaltaTerminadoresOuCaboQuebrado().createSamples( fVoltageLevel );
                break;
                
            //case SignalType.SIGNAL_CABLE_BREAK:
                //sampleArray = new InterrupcaoLinha().createSamples( fVoltageLevel );
        }
        
        return sampleArray;
    }

//    public float[] BuildSignalSamplesVeiga( byte rawdata[], int numbytes )
//    {
//        byte aux, bitsample;
//        int isize = rawdata.length;
//        int i,j,k,s,t;
//
//        if( isize <= 0 )
//            return null;
//
//        if( isize > numbytes )
//            isize = numbytes;
//
//        Samples = new float[ isize*8*NUM_SAMPLES_PER_BIT ];
//        
//        for(i=0; i < isize; i++)
//        {
//            //aux extrai cada byte do frame
//            aux = rawdata[ i ];
//            s = 8*i*NUM_SAMPLES_PER_BIT;
//
//            for(j=0; j < 8; j++)
//            {
//                //bit sample amostra o bit do mais signifcativo para o menos
//                bitsample = (byte) ( aux & (0x80 >> j) );
//                t = s + j*NUM_SAMPLES_PER_BIT;
//
//                for(k = 0; k < NUM_SAMPLES_PER_BIT; k++)
//                {
//                    Samples[ t + k ] = (bitsample != 0) ? SAMPLE_HIGH_LEVEL : SAMPLE_LOW_LEVEL;
//                }
//
//                //FALTA INCLUIR A PARIDADE E STOPBIT
//            }
//        }
//        return Samples;
//    }

    public int getRawSize( ) {
        if( RawData != null )
            return RawData.length;
        else
            return 1;
    }

    private void AssembleFrame(int address, int destAddr, byte function_service) {
        setSourceAddr( (byte) address );
        setDestAddr( (byte) destAddr );
        frameService.setFfunction(function_service);

        switch( frameService.getService() ) {
            case Service.PB_REQ_TOKEN_PASS:
                frameSD.setSD(SD.SD4);
                break;

            case Service.PB_REQ_FDL_STATUS:
            case Service.PB_RES_FDL_STATUS:
            case Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
                frameSD.setSD(SD.SD1);
            break;

            case Service.PB_REQ_DATA_EXCHANGE:
            case Service.PB_RES_DATA_EXCHANGE:
            case Service.PB_REQ_RD_INPUTS:
            case Service.PB_RES_RD_INPUTS:
            case Service.PB_REQ_RD_OUTPUTS:
            case Service.PB_RES_RD_OUTPUTS:
            case Service.PB_REQ_SET_PRM:
            case Service.PB_REQ_CHK_CFG:
            case Service.PB_REQ_GET_CFG:
            case Service.PB_RES_GET_CFG:
            case Service.PB_REQ_GET_DIAG:
            case Service.PB_RES_GET_DIAG:
            case Service.PB_SET_SLAVE_ADDR:
                frameSD.setSD(SD.SD2);
            break;

            case Service.PB_RES_SET_PRM:
            case Service.PB_RES_CHK_CFG:
            case Service.PB_RES_DATA_EXCHANGE_ONLY_OUTPUTS:
            case Service.PB_RES_RD_INPUTS_EMPTY:
            case Service.PB_RES_RD_OUTPUTS_EMPTY:
                frameSD.setSD(SD.SC);
        }

        switch( frameService.getService() ) {
            case Service.PB_REQ_TOKEN_PASS:
                frameFC.setFC( FC.FC_NULL);
                break;

            case Service.PB_REQ_FDL_STATUS:
                frameFC.setFC( FC.FC_REQ_FDL_STATUS_WITH_REPLY );
                break;

            case Service.PB_REQ_DATA_EXCHANGE_ONLY_INPUTS:
            case Service.PB_REQ_DATA_EXCHANGE:
            case Service.PB_REQ_RD_INPUTS:
            case Service.PB_REQ_RD_OUTPUTS:
            case Service.PB_REQ_SET_PRM:
            case Service.PB_REQ_CHK_CFG:
            case Service.PB_REQ_GET_CFG:
            case Service.PB_REQ_GET_DIAG:
            case Service.PB_SET_SLAVE_ADDR:
                frameFC.setFC( FC.FC_REQ_SEND_DATA_HIGH );
                break;

            case Service.PB_RES_FDL_STATUS:
                frameFC.setFC( FC.FC_NULL);
                break;

            case Service.PB_RES_DATA_EXCHANGE:
            case Service.PB_RES_RD_INPUTS:
            case Service.PB_RES_RD_OUTPUTS:
            case Service.PB_RES_GET_CFG:
            case Service.PB_RES_GET_DIAG:
            case Service.PB_RES_SET_PRM:
            case Service.PB_RES_CHK_CFG:
            case Service.PB_RES_DATA_EXCHANGE_ONLY_OUTPUTS:
            case Service.PB_RES_RD_INPUTS_EMPTY:
            case Service.PB_RES_RD_OUTPUTS_EMPTY:
                frameFC.setFC( FC.FC_RES_FDL_LOW );
                break;
        }

        Encode( );
    }

    public int getNumBits( ) {
        //Return size* 11Tbit
        //No time between bytes
        return 11*(getRawSize());
    }

    public boolean IsToken( ) {
        if( this.getFrameService() == Service.PB_REQ_TOKEN_PASS )
            return true;
        else
            return false;
    }
    
    public boolean getFCB( )
    {
        return FCB;
    }

    public boolean getFCV( )
    {
        return FCV;
    }
    
    public void setFCB( boolean bFCB )
    {
        FCB = bFCB;
        Encode( );
    }
    
    public void setFCV( boolean bFCV )
    {
        FCV = bFCV;
        Encode( );
    }
    
    public void setPriorityResponseHigh( )
    {
        frameFC.setFC( FC.FC_RES_FDL_HIGH );
    }
    
    public boolean isResponseHighPriority( )
    {
        return (frameFC.getFC() == FC.FC_RES_FDL_HIGH);
    }
    
    public void setReadableFrame(boolean value )
    {
        bReadableFrame = value;
    }
    
    public boolean isReadableFrame( )
    {
        return bReadableFrame;
    }
    
    public void setSignalType( int istype )
    {
        iSignalProblem = istype;
        Encode();
    }
    
    public int getSignalType( )
    {
        return iSignalProblem;
    }
    
    public void setVoltageLevel( int fVolt )
    {
        fVoltageLevel = fVolt;
        Encode();
    }
    
    public float getVoltageLevel( )
    {
        return fVoltageLevel;
    }
    
    public GeneralDataUnit getDecodedDataUnit( )
    {
        return ServiceDataUnit;
    }
    
}
