/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stackDP;

/**
 *
 * @author renato
 */
public class TelegramReader {
    private int buffer_len=0;
    private byte[] buffer = new byte[255];
    boolean bWaitStartFrame = true;
    private int uiframetype = 0;
    byte[] InputTelegram;

    static final int SD1 = 0x10;
    static final int SD2 = 0x68;
    static final int SD4 = 0xDC;
    static final int  SC = 0xE5;
    int SD2_LE = 0;
    static final int SD2_HEADER_TRAILER_SIZE = 6;
    private boolean bnewresponse = false;
    
    public final boolean DEBUG_ON = true;

    
    void TelegramReader()
    {
        buffer_len=0;
        bWaitStartFrame = true;
        bnewresponse = false;
        SD2_LE = 0;
        uiframetype = 0;
    }
    
    void EnqueueCaracter( int data )
    {
        if(DEBUG_ON) System.out.println("TelegramReader: EnqueueCaracter(" + Integer.toString(data) + ") SD1: " + Integer.toString(SD1) + " SD2: " + Integer.toString(SD2) + " SD4: " + Integer.toString(SD4) + " SC: " + Integer.toString(SC) );
        if( bWaitStartFrame == true )
        {
            //Waiting for new frame
            if( data < 0 ) data += 256;
            if( (data == SD2) || (data == SD1) || (data == SD4) || (data == SC) )
            {
                bWaitStartFrame = false;
                buffer[buffer_len++] = (byte) data;
                if(DEBUG_ON) System.out.println("TelegramReader: New Frame Start(" + Integer.toString(data) + ")");
            }

            switch(data)
            {
                case SD1:
                    uiframetype = SD1;
                    break;

                case SD2:
                    uiframetype = SD2;
                    SD2_LE = 0;
                    break;

                case SD4:
                    uiframetype = SD4;
                    break;

                case SC:
                    uiframetype = SC;
                    bWaitStartFrame = true;
                    FinishFrameResponse( );
                    break;

                default:
                    //ignore data
                    buffer_len = 0;
                    break;
            }

        }
        else
        {
            //Its not new frame - only enqueue data according...
            buffer[buffer_len++] = (byte) data;
            switch(uiframetype)
            {
                case SD1:
                    if( buffer_len >= 6 )
                    {
                        bWaitStartFrame = true;
                        buffer_len = 6;
                        FinishFrameResponse( );
                    }
                    break;

                case SD2:
                    if( (buffer_len > 1) && (SD2_LE == 0) )
                    {
                        SD2_LE = data;                                    
                    }
                    else if( (buffer_len > 1) && (SD2_LE != 0) ) 
                    {
                        if( buffer_len >= (SD2_LE + SD2_HEADER_TRAILER_SIZE) )
                        {
                            bWaitStartFrame = true;
                            buffer_len = (SD2_LE + SD2_HEADER_TRAILER_SIZE);
                            FinishFrameResponse(  );
                        }
                    }
                    break;

                case SD4:
                    if( buffer_len >= 3 )
                    {
                        bWaitStartFrame = true;
                        buffer_len = 3;
                        FinishFrameResponse(  );
                    }
                    break;
            }
        }
    }


    public void FinishFrameResponse(  )
    {
        InputTelegram = new byte[buffer_len];
        System.arraycopy(buffer, 0, InputTelegram, 0, buffer_len);
        if(DEBUG_ON) System.out.println("TelegramReader: Frame Finished!");
        setNewResponse(true);
    }
    
    public int getTelegramLen()
    {
        return buffer_len;
    }
    
    public byte[] getTelegram( )
    {
        setNewResponse(false);
        return InputTelegram;
    }

    protected void setNewResponse(boolean b) {
        bnewresponse = b;
    }
    
    public boolean isNewResponse() {
            return bnewresponse;
    }
}
