/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author mythai
 */
public class PeerProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // TODO code application logic here
        if (args.length > 0)
            Constants.PEER_ID = Integer.parseInt(args[0]);
        System.setProperty("peerId", String.valueOf(Constants.PEER_ID));
        Constants.readCfg();
        PeerConnection connection = PeerConnection.getInstance();
        connection.start();
    }
    
}
