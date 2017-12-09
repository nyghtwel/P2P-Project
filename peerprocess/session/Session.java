/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import peerprocess.Handler;

/**
 *
 * @author mythai
 */
public class Session extends Thread{
    private Socket connection;
    private Handler handler;
    private ObjectInputStream in;//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket

    public Session(Socket connection, Handler handler) throws IOException {
        this.connection = connection;
        this.handler = handler;
        
        out =  new ObjectOutputStream(connection.getOutputStream());
        out.flush();
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(connection.getInputStream());
            while (true){
                Object msg = in.readObject();
                handler.messageReceived(this, msg);
            }
        } catch (IOException ex) {
//            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
//            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                close();
            } catch (IOException ex) {
//                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public synchronized void write(byte[] msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ex) {
//            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SocketAddress getRemoteAddress() {
        return connection.getRemoteSocketAddress();
    }
    
    public void close() throws IOException{
        in.close();
        out.close();
        connection.close();
    }
}
