/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackDP;

import common.framestructure.ProfibusFrame;
import exceptions.ProfibusFrameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

    
/**
 *
 * @author renato
 */
public class DPLineTCP {
    public byte InputTelegram[];
    public int InputSize = 0;
    public byte buffer[] = new byte[255];
    public byte LAS[] = new byte[127];
    public int buffer_len;
    private boolean systemAvaiable = false;
    private boolean bIsNewResponse = false;
    private DataInputStream serialTCPin;
    private DataOutputStream serialTCPout;
    private Socket clientSocket = null;
    TelegramReader tr;
    
    protected final boolean DEBUG_ON = true;
    
    private final byte PR_LIVE_LIST     = (byte) 0xA1;
    private final byte PR_MASTER_STATUS = (byte) 0xA2;
    
    public final int MASTER_OFFLINE                                    = 0x00;
    public final int MASTER_NOT_READY_TO_ENTER_IN_LOGICAL_TOKEN_RING   = 0x10;
    public final int MASTER_READY_TO_ENTER_IN_LOGICAL_TOKEN_RING       = 0x20;
    public final int MASTER_ALREADY_IN_LOGICAL_TOKEN_RING              = 0x30;
    
    public int MASTER_STATUS = MASTER_OFFLINE;
    
    
    public void connect ( String address, int portNum, String portName, String baudRate,String timeout, String MasterAddr ) throws Exception
    {
        clientSocket = new Socket(address, portNum);
        
        clientSocket.setTcpNoDelay(true);
        clientSocket.setReceiveBufferSize(255);
        clientSocket.setSoTimeout(5000);
               
        serialTCPout = new DataOutputStream( clientSocket.getOutputStream() );
        serialTCPin  = new DataInputStream(  clientSocket.getInputStream()  );
        
        setupSerial(portName, baudRate, timeout, MasterAddr);
        
    }
    
    protected void setupSerial(String portName, String baudRate, String timeout, String MasterAddr ) throws IOException
    {
        serialTCPout.write( portName.getBytes() );
        serialTCPin.readUnsignedByte();
        
        serialTCPout.write( baudRate.getBytes() );
        serialTCPin.readUnsignedByte();
        
        serialTCPout.write( timeout.getBytes() );
        serialTCPin.readUnsignedByte();
        
        serialTCPout.write( MasterAddr.getBytes() );
        serialTCPin.readUnsignedByte();
        
    }
    
    public void disconnect( ) throws IOException
    {
        clientSocket.close();
    }
    
    protected void waitResponseTelegram( ) throws IOException
    {
        byte[] datain = new byte[256];
        int numread;

        //Wait for reception of sizeinput
        if( isConnected() == true )        
            numread = serialTCPin.read( datain );
        else
            return;
        
        if(DEBUG_ON) System.out.println("[DATA ARRIVED] First Data Size Received: " + Integer.toString(numread));
        
        if( numread > 0 )
        {
            //Get size of message (first byte)
            int framesize = datain[0];
            int insize    = datain[0] + 1;
            if(DEBUG_ON) System.out.println("Frame Size: " + Integer.toString(insize));
            
            if( numread >= insize )
            {
                //all data already read!
                if(DEBUG_ON) System.out.println("Data Size Received TOTAL: " + Integer.toString(numread));
            }
            else
            {
                //Must wait for receive all data
                int offset = 0, i = 0;
                int accum  = numread;
                do
                {
                    offset += numread;
                    i+=1;
                    numread = serialTCPin.read( datain, offset, insize-numread );
                    if(DEBUG_ON) System.out.println("Data Size MORE Received Part("+Integer.toString(i)+") of total("+Integer.toString(insize)+"): " + Integer.toString(numread));
                    accum += numread;

                } while( (accum < insize) && (numread > 0) );
                
            }
        
            if( framesize < 0 )
                framesize += 256;
            
            if( framesize > 0 )
            {
                if( CheckProprietaryFrame(datain[1]) == true )
                    ProcessProprietaryFrame(datain, framesize );
                else
                    EnqueueCaracterFrame( datain, framesize );
            }
                
        }
    }
    
    public void setOutputFrame( ProfibusFrame pf ) throws IOException
    {
        setOutputTelegram( pf.getRawData() );
    }
    
    public void setOutputTelegram( byte[] output ) throws IOException
    {
        bIsNewResponse = false;
        
        //Put size of frame in first byte of frame
        byte[] outputprotocol = EnqueueSize( output );
        
        //Convert byte frame array to String
        byte[] outputformatted = HexByteArrayToString( outputprotocol );
        
        //Write to streamout and force sendo over TCP with flush
        if (isConnected() == true)
        {
            serialTCPout.write( outputformatted, 0, outputformatted.length );
            serialTCPout.flush();
            waitResponseTelegram( );            
        }
    }
    
    public void waitInputTelegram( ) throws IOException
    {
        bIsNewResponse = false;
        
        //Put size of frame in first byte of frame
        byte[] outputprotocol = new byte[1];
        outputprotocol[0] = 0;
        
        //Convert byte frame array to String
        byte[] outputformatted = HexByteArrayToString( outputprotocol );
        
        //Write to streamout and force sendo over TCP with flush
        serialTCPout.write( outputformatted, 0, outputformatted.length );
        serialTCPout.flush();
        
        waitResponseTelegram( );        
    }

    private void EnqueueCaracterFrame(byte[] data, int datasize) throws IOException 
    {
        if(DEBUG_ON) System.out.println("DPLineTCP: New Data to Decode");
        
        tr = new TelegramReader();
        
        //Frame start on byte 1 and finishes on insize
        if(DEBUG_ON) System.out.println("DPLineTCP: Send to decode frame size: " + Integer.toString(datasize) );
        for(int i=1; i < (datasize+1); i++)
        {
            EnqueueCaracter( data[i] );
        }
    }

    protected byte[] EnqueueSize( byte[] bytearray )
    {
        
        byte[] outbyte = new byte[ bytearray.length + 1 ];
        System.arraycopy(bytearray, 0, outbyte, 1, bytearray.length);
        outbyte[0] = (byte) outbyte.length;
        
        return outbyte;
    }
    
    protected byte[] HexByteArrayToString(byte[] bytearray )
    {
        String outputstring = new String();
        
        for(int i=0; i < bytearray.length; i++)
        {
            String singlechar = Integer.toHexString( 0xFF & bytearray[i] );
            if (singlechar.length() == 1)
                outputstring += "0"+Integer.toHexString( 0xFF & bytearray[i] )+" ";
            else
                outputstring += Integer.toHexString( 0xFF & bytearray[i] )+" ";
        }
        outputstring = outputstring.trim();
        
        return outputstring.getBytes();
    }
    
    public byte[] getInputTelegram( ) throws IOException
    {
        return InputTelegram;
    }
    
    public ProfibusFrame getInputFrame( ) throws ProfibusFrameException
    {
        return new ProfibusFrame(InputTelegram);
    }
    
    void EnqueueCaracter( int data ) throws IOException
    {
        if(DEBUG_ON) System.out.println("DPLineTCP: EnqueueCaracter = "+ ((Integer) data).toString( ) );
        tr.EnqueueCaracter( data );
        if ( tr.isNewResponse() == true ) 
        {
            bIsNewResponse = true;
            InputTelegram = new byte[ tr.getTelegramLen() ];
            System.arraycopy( tr.getTelegram(), 0, InputTelegram, 0, tr.getTelegramLen() );
        }
    }
        
    public boolean isSystemAvaiable() {
        return (clientSocket != null) ? true : false;
    }

    public boolean isNewResponse() {
        return bIsNewResponse;
    }

    public boolean isConnected() {
        if( clientSocket != null )
        {
            return ( clientSocket.isConnected() && !clientSocket.isClosed() && !clientSocket.isOutputShutdown()  ) ? true : false;
        }
        else
            return false;
    }

    private boolean CheckProprietaryFrame(byte b) {
        boolean ret = false;
        switch ( b )
        {
            case PR_LIVE_LIST:       //LiveList
            case PR_MASTER_STATUS:   //Master Status
                ret = true;
                break;
            
            default:
                ret = false;
        }
        return ret;
    }

    private void ProcessProprietaryFrame(byte[] datain, int framesize) {
        int i;
        switch( datain[1] )
        {
            case PR_LIVE_LIST:       //LiveList
                for(i=2; (i < framesize) && (i < 256); i += 2 )
                    if( datain[ i ] < 127 )
                        LAS[ datain[ i ] ] = datain[ i+1 ];
                
                bIsNewResponse = true;
                InputTelegram = new byte[ LAS.length ];
                System.arraycopy( LAS, 0, InputTelegram, 0, LAS.length );
                break;

            case PR_MASTER_STATUS:   //Master Status
                bIsNewResponse = true;
                InputTelegram = new byte[ 1 ];
                InputTelegram[0] = datain[2];
                MASTER_STATUS = (int) InputTelegram[0];
                break;
        }
    }
    
    public byte[] getLiveList( )
    {
        return LAS;
    }
    
    public int getMasterStatus( )
    {
        return MASTER_STATUS;
    }
}
