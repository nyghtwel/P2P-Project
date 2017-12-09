/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import datastructure.Pair;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import peerprocess.session.Session;

/**
 *
 * @author mythai
 */
public class PeerConnection extends Thread{
    private static PeerConnection instance;
    
    private DataManager dataManager;
    private Handler handler;
    
    private ServerSocket acceptor;

    public static PeerConnection getInstance() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        if (instance == null)
            instance = new PeerConnection();
        return instance;
    }
    
    public PeerConnection() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        dataManager = DataManager.getInstance();
        handler = Handler.getInstance();
        dataManager.setup();
        Scheduler.getInstance().run();
    }

    @Override
    public void run() {
        try {
            setupClient();
            setupServer();
        } catch (IOException ex) {
            Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setupServer() throws IOException, Exception {
        acceptor = new ServerSocket(dataManager.getPeerInfo(Constants.PEER_ID).getSecond());
        try{
            while(Constants.NUMBER_OF_ACTIVE_PEERS < Constants.NUMBER_OF_PEERS){
                Socket socket = acceptor.accept();
                Session session = new Session(socket, handler);
                session.start();
                handler.sessionOpened(session);
                Constants.NUMBER_OF_ACTIVE_PEERS ++;
            }
        } finally {
            
        }
        acceptor.close();
    }

    private void setupClient() throws IOException, Exception {
        // actively connect to peers who have smaller id
        Vector<Pair<String, Integer>> listPeerServers = dataManager.getListPeerServers();
        for (int i=0; i<listPeerServers.size(); i++) {
            String host = listPeerServers.get(i).getFirst();
            int port = listPeerServers.get(i).getSecond();
            Socket socket = new Socket(host, port);
            Session session = new Session(socket, handler);
            session.start();
            handler.sessionOpened(session);     
        }
    }
    
    public void close() throws IOException{
//        acceptor.close();
    }
}
