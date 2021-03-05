/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import common.ConversionsAdvanced;
import java.net.*;
import java.io.*;

public class Client extends Thread {

    public void run( ) {
        try {

            String host = "127.0.0.1";
            Socket socket = new Socket(host, 1250);
            byte[] buf = new byte[742];
            int i = 0;

            DataOutputStream ostream = new DataOutputStream( socket.getOutputStream() );
            DataInputStream istream  = new DataInputStream ( socket.getInputStream()  );

            System.out.println("Client port " + Integer.toString(socket.getLocalPort())+" start");

            while (socket.isConnected()) {
                
                istream.read(buf);
                
                System.out.println("MESSAGE " + Integer.toString(i++) + " RECEIVED - Size: " + Integer.toString(buf.length));
                System.out.println( ConversionsAdvanced.toStringFromStream(buf, " "));
                System.out.println(" ");

                yield( );
            }
            System.out.println("Client port " + Integer.toString(socket.getLocalPort())+" finish");
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}