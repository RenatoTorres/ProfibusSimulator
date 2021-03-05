/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

// server.java
import common.ConversionsAdvanced;
import exceptions.ProfibusFrameException;
import java.net.*;
import java.io.*;

import consolesimulator.ProfibusSimulator;
import consolesimulator.ProfibusSimulatorSingleton;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread  {

    String sIP = "";

    public void run( ) {
        Socket socket = null;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(1250, 0, InetAddress.getByAddress("127.0.0.1", new byte[] {127, 0, 0, 1} ));
            sIP = serverSocket.getInetAddress().toString();
            System.out.println("Server::Aguardando clientes");
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Server::Fechando conexao");
        }

        while( true )
        {
            try {
                socket = serverSocket.accept();
                SenderThread s = new SenderThread( socket );
                s.start();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
          System.out.println("Server::Client conectado...");
        }
    }

    public String getServerIP( )
    {
        return sIP;
    }

    public class SenderThread extends Thread
    {
        Socket lsocket;
        ProfibusSimulator sim;
        DataOutputStream ostream;
        DataInputStream istream;

        public SenderThread(Socket sock) throws IOException
        {
            lsocket = sock;
            sim = ProfibusSimulatorSingleton.getInstance( );
            //sim.setCaptureQueueActivated( true );
            ostream = new DataOutputStream(lsocket.getOutputStream());
            istream = new DataInputStream(lsocket.getInputStream());
        }

        @Override
        public void run( ) {
            while (true) {
                //System.out.println("Create package");

                if( sim.IsFrameAvaiable() == true )
                {
                    System.out.println("Server::Pacotes Aguardando Envio: " + Integer.toString( sim.getFrameBufferLength( ) ));

                    Package packProfidoctor = new Package( );
                    String framedata = sim.getNextFrame( );
                   
                    try {
                        //avoid null data
                        if( framedata == null ) {
                            continue;
                        }
                        else {
                            byte[] buf = null;
                            try {
                                buf = packProfidoctor.getPackage( framedata );
                            } catch (ProfibusFrameException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println( ConversionsAdvanced.toStringFromStream(buf, " "));
                            ostream.write(buf, 0, buf.length);
                            ostream.flush();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        if (lsocket != null) {
                            try {
                                lsocket.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            }
                        }
                }
                yield( );
            }
        }
    }
}
