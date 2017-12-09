/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mythai
 */
public class Scheduler {

    private Timer timer;
    
    private static Scheduler instance = null;
    
    public Scheduler() {
        timer = new Timer();
    }
    
    public static Scheduler getInstance(){
        if (instance == null)
            instance = new Scheduler();
        return instance;
    }
    
    public void run() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        DataManager d = DataManager.getInstance();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                     d.selectPreferredNeighbors();
                } catch (IOException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 2000, Constants.UNCHOKING_INTERVAL * 1000);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    d.selectOptimisticUnchokedNeighbor();
                } catch (IOException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 2000, Constants.OPTIMISTIC_UNCHOKING_INTERVAL * 1000);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    d.checkFileStatusOfAllPeers();
                } catch (Exception ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }, 2000, Constants.CHECKING_STATUS_ALL_PEER_INTERVAL * 1000);
    }
    
    public void stop(){
        timer.cancel();
    }
    
}
