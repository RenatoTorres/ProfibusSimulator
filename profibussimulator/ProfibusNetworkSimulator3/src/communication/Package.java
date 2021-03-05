/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import common.Conversions;
import common.framestructure.ProfibusFrame;
import common.framestructure.Service;
import exceptions.ProfibusFrameException;


/**
 *
 * @author eduardo_mossin
 */
public class Package {

    public byte[] getPackage(String FrameData) throws ProfibusFrameException {
//        byte[] bufAux = new byte[1484];

        //Simula 2 mensagens
        //size       | Mensagem Profidoctor | size       | Mensagem Profidoctor
        //<4 bytes>  | <...>                | <4 bytes>  | <...>

        //montando pacote com 3 mensagens profidoctor
        int index = 0;

        //msg 1
        byte[] msg = getMsgProfidoctor( new ProfibusFrame( FrameData ) );

        //SIZE do pacote
        byte[] intByte = Conversions.toByta( msg.length );

        byte[] bufAux = new byte[msg.length + 4];


        bufAux[index++] = intByte[0];
        bufAux[index++] = intByte[1];
        bufAux[index++] = intByte[2];
        bufAux[index++] = intByte[3];

        for(int i = 0; i < msg.length; i++)
        {
            bufAux[index++] = msg[i];
        }

        //SIZE do pacote
        //msg = getMsgProfidoctor( (byte) 31, (float) 3.331331);
//        intByte = Conversions.toByta(msg.length);
//        bufAux[index++] = intByte[0];
//        bufAux[index++] = intByte[1];
//        bufAux[index++] = intByte[2];
//        bufAux[index++] = intByte[3];
//
//        for (int i = 0; i < msg.length; i++) {
//            bufAux[index++] = msg[i];
//        }
        return bufAux;
    }
    

    private byte[] getMsgProfidoctor(ProfibusFrame frame ) {
        //Timestamp | Endereco do device fonte | Endereco do device destino | size msg profibus | Mensagem profibus | Numero de bits amostrados | Bits amostrados
        //<8 bytes> | <1 byte>                 | <1 byte>                   | <1 byte>          | <max 255 bytes>   | <2 bytes>                 | <max 240 KBytes>
        byte[] buf = new byte[738];
        int index = 0;
        
        //field Timestamp de 8 bytes (long)
        long dateL = frame.getTimeStamp().getTime();
        byte[] dateBuf = Conversions.toByta(dateL);

        for(int i=0; i < 8; i++)
        {
            buf[index++] = dateBuf[i];
        }

        //Timestamp em us
        byte[] usBuf = Conversions.toByta( frame.getUsFrameTime() );
        for(int i=0; i < 4; i++)
        {
            buf[index++] = usBuf[i];
        }
        
        //Byte tipo do sinal
        //0 para pacote interpretavel
        //1 para pacote nao reconhecivel
        buf[index++] = (byte) ((frame.isReadableFrame() == true) ? 0x01 : 0x00);

        //field endereco do device fonte
        buf[index++] = (byte) frame.getSourceAddr();

        //field Endereco do device destino
        buf[index++] = (byte) frame.getDestAddr();

        //size msg profibus (SIZE DA MSG)
        buf[index++] = (byte) frame.getRawSize();

        //msg profibus (ONDE TRANSFERE A MSG)
        //ESTÁ USANDO SEMPRE TAMANHO 123
        for(int i=0; i < frame.getRawSize(); i++)
        {
            //valor fixo para testar
            buf[ index++ ] = (byte) frame.getRawData(i);
        }

        //numero de bits amostrados - cada bit contem 30 amostras onde cada
        //amostra é analogica do tamanho float (4 bytes)
        buf[index++] = (byte) frame.getSamplesPerBit();

        byte[] a = frame.getLevelSamples();
        System.arraycopy(a, 0, buf, index, a.length);

//        float[ ] a = frame.getLevelSamples();
//        int total = a.length;
//
//        for(int k=0; k < total; k++, index+=4 )
//        {
//            byte[] floatByte = Conversions.toByta((float) a[k] );
//            buf[index+0] = floatByte[0];
//            buf[index+1] = floatByte[1];
//            buf[index+2] = floatByte[2];
//            buf[index+3] = floatByte[3];
//        }

        return buf;
    }
}
