/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackDP;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author renato_veiga
 */
public class DPLine {
    CommPort commPort;
    private SerialWriter Sender;
    private SerialReader Receiver;
    public byte InputBuffer[] = new byte[255];
    public byte InputSize = 0;
    private boolean systemAvaiable = false;
    private boolean bIsNewResponse = false;
    
    public boolean IsNewResponse( )
    {
        return bIsNewResponse;
    }
    
    public void setNewResponse(boolean isnew )
    {
        bIsNewResponse = isnew;
    }
    
    public byte[] getInputTelegram( )
    {
        setNewResponse(false);
        return Receiver.getTrimBuffer( );
    }
        
    public void setOutputTelegram( byte[] output )
    {
        Sender.setOutput( output );
    }
    
    public boolean isSystemAvaiable( )
    {
        return systemAvaiable;
    }

    public static String[] listPorts( )
    {
        int i=0,k=0;
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if( portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
                System.out.println("SerialPort "+ String.valueOf(i)+": " + portIdentifier.getName() );
                i+=1;
            }
        }
        

        String[] SerialPorts = new String[i];
        
        java.util.Enumeration<CommPortIdentifier> portEnum2 = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum2.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum2.nextElement();
            if( portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
                SerialPorts[k] = portIdentifier.getName();
                k+=1;
            }
        }
        
        return SerialPorts;
    }
    
    public int connect ( String portName ) throws Exception
    {
        
        listPorts();
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
            return 0;
        }
        else
        {
            commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.setInputBufferSize(255);
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_EVEN);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                               
                Sender = new SerialWriter( out );
                Sender.start();
                
                Receiver = new SerialReader( in );
                serialPort.addEventListener( Receiver );
                serialPort.notifyOnDataAvailable( true );
                
                systemAvaiable = true;
                System.out.println("Ready to WORK!");
                return -1;
            }
            else
            {
                System.out.println("Only serial ports are handled!!!");
                return 0;
            }
        }     
    }
    
    public void disconnect( )
    {
        commPort.close();
    }

    boolean isActivity() {
        return true;
    }

    public boolean IsReceivingResponse() {
        return Receiver.IsOnReceiverLoop();
    }
    
    /**
     * Handles the input coming from the serial port. A new line character
     * is treated as the end of a block in this example. 
     */
    public class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private int buffer_len=0;
        private byte[] buffer = new byte[255];
        boolean bWaitStartFrame = true;
        private int uiframetype;
        final int SD1 = 0x10;
        final int SD2 = 0x68;
        final int SD4 = 0xDC;
        final int  SC = 0xE5;
        int SD2_LE = 0;
        int SD2_HEADER_TRAILER_SIZE = 6;
        byte[] InputTelegram;
        boolean isonloop = false;
        TelegramReader tr = new TelegramReader();
        
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
            try
            {
                while ( ((data = in.read()) > -1) )
                {
                    isonloop = true;
                    EnqueueCaracter( data );
                }
                isonloop = false;
             }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }             
        }
        
        void EnqueueCaracter( int data ) throws IOException
        {
            tr.EnqueueCaracter( data );
            if (tr.isNewResponse() == true) 
            {
                setNewResponse(true);
                InputTelegram = new byte[tr.getTelegramLen()];
                System.arraycopy(tr.getTelegram(), 0, InputTelegram, 0, tr.getTelegramLen());
            }
        }

//        void EnqueueCaracter( int data ) throws IOException
//        {
//            if( bWaitStartFrame == true )
//            {
//                //Waiting for new frame
//                if( (data == SD2) || (data == SD1) || (data == SD4) || (data == SC) )
//                {
//                    bWaitStartFrame = false;
//                    buffer[buffer_len++] = (byte) data;
//                }
//
//                switch(data)
//                {
//                    case SD1:
//                        uiframetype = SD1;
//                        break;
//
//                    case SD2:
//                        uiframetype = SD2;
//                        SD2_LE = 0;
//                        break;
//
//                    case SD4:
//                        uiframetype = SD4;
//                        break;
//
//                    case SC:
//                        uiframetype = SC;
//                        bWaitStartFrame = true;
//                        FinishFrameResponse( );
//                        break;
//
//                    default:
//                        //ignore data
//                        buffer_len = 0;
//                        break;
//                }
//
//            }
//            else
//            {
//                //Its not new frame - only enqueue data according...
//                buffer[buffer_len++] = (byte) data;
//                switch(uiframetype)
//                {
//                    case SD1:
//                        if( buffer_len >= 6 )
//                        {
//                            bWaitStartFrame = true;
//                            buffer_len = 6;
//                            FinishFrameResponse( );
//                        }
//                        break;
//
//                    case SD2:
//                        if( (buffer_len > 1) && (SD2_LE == 0) )
//                        {
//                            SD2_LE = data;                                    
//                        }
//                        else if( (buffer_len > 1) && (SD2_LE != 0) ) 
//                        {
//                            if( buffer_len >= (SD2_LE + SD2_HEADER_TRAILER_SIZE) )
//                            {
//                                bWaitStartFrame = true;
//                                buffer_len = (SD2_LE + SD2_HEADER_TRAILER_SIZE);
//                                FinishFrameResponse(  );
//                            }
//                        }
//                        break;
//
//                    case SD4:
//                        if( buffer_len >= 3 )
//                        {
//                            bWaitStartFrame = true;
//                            buffer_len = 3;
//                            FinishFrameResponse(  );
//                        }
//                        break;
//                }
//            }
//        }
//        
//        
//        public void FinishFrameResponse(  ) throws IOException
//        {
//            //in.close();
//            InputTelegram = new byte[buffer_len];
//            System.arraycopy(buffer, 0, InputTelegram, 0, buffer_len);
//            setNewResponse(true);
//        }
        
        public byte[] getTrimBuffer( )
        {
            return InputTelegram;
        }

        private boolean IsOnReceiverLoop() {
            return isonloop;
        }

    }

    /** */
    public class SerialWriter extends Thread
    {
        OutputStream out;
        public byte OutputBuffer[] = new byte[255];
        int OutputSize = 0;
        
        public synchronized void setOutput(byte[] b)
        {
            System.arraycopy(b, 0, OutputBuffer, 0, b.length);
            OutputSize = b.length;
            notify();
        }
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            while( true )
            {
                try {
                    SendFrame( );
                } catch (InterruptedException ex) {
                    Logger.getLogger(DPLine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        public synchronized void SendFrame( ) throws InterruptedException
        {
            while( OutputSize == 0 )
                wait();
            
            try
            {                
                int priority = getPriority();
                setPriority(MAX_PRIORITY);
                
                this.out.write(OutputBuffer, 0, OutputSize);
                
                setPriority(priority);
                
                OutputSize = 0;

                System.out.println("Frame sent!");
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
    
}
