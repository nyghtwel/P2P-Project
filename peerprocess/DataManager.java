/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import datastructure.Pair;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import peerprocess.session.Session;


/**
 *
 * @author mythai
 */
public class DataManager {
    
    private static DataManager instance;
    
    private HashMap<Integer, Pair<String, Integer>> mapPeers; // map: peerId -> <host, port>
    
    private HashMap<Integer, boolean[]> mapBitFields; // map: peerId -> bitfield
    
    private HashMap<Integer, Boolean> mapChoke; // map: peerId -> true:choke  false: unchoke - which peers this peer choke/unchoke
    
    private HashMap<Integer, Boolean> mapIsChoked; // map: peerId -> true: is choked false: is unchoked - which peers are choking/unchoking this peer
    
    private boolean hasRequested[]; // indicate fields this peer has requested
    
    private HashMap<Integer, Boolean> mapRequestPending; // map: peerId -> true if peer is keeping a request from this peer but still not reply back, false otherwise
    
    private HashMap<Integer, Integer> mapRequesting; // map: peerId -> field that peer is requesting from this peer
    
    private HashMap<Integer, Boolean> mapInterested; // map: peerId -> whether mapped peer is interested in this peer;
    
    private HashMap<Integer, Session> mapSession; // map: peerId -> session 
    
    private HashMap<Integer, Integer> mapDownloadingRate; // map: peerId -> downloading rate (bytes ?) from this peer in one interval
    
    private Timer timer; // use for periodically calculate choke and unchoke friends
    
    private Random r;
    
    private PeerLog log;
    
    private Handler handler;
    
    public static DataManager getInstance() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        if (instance == null)
            instance = new DataManager();
        return instance;
    }
        
    public DataManager() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        mapPeers = new HashMap<>();
        mapBitFields = new HashMap<>();
        mapChoke = new HashMap<>();
        mapIsChoked = new HashMap<>();
        mapInterested = new HashMap<>();
        mapSession = new HashMap<>();
        mapRequesting = new HashMap<>();
        mapRequestPending = new HashMap<>();
        mapDownloadingRate = new HashMap<>();
        readInfo();
        
        r = new Random();
        log = PeerLog.getLogger(Constants.PEER_ID);
        // log.configure(Constants.PEER_ID);
        //int peer_id
    }
    
    public void setup() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        handler = Handler.getInstance();
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    selectPreferredNeighbors();
//                } catch (IOException ex) {
//                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }, 2000, Constants.UNCHOKING_INTERVAL * 1000);
//        
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    selectOptimisticUnchokedNeighbor();
//                } catch (IOException ex) {
//                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }, 2000, Constants.OPTIMISTIC_UNCHOKING_INTERVAL * 1000);
//        
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    checkFileStatusOfAllPeers();
//                } catch (Exception ex) {
//                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
//                } 
//            }
//        }, 2000, Constants.CHECKING_STATUS_ALL_PEER_INTERVAL * 1000);
    }
    
    private void readInfo() throws FileNotFoundException, IOException{
        
        // read PeerInfo,cfg
        BufferedReader buff = new BufferedReader(new FileReader(Constants.PEER_INFO_FILE));
        String s = null;
        while ((s = buff.readLine()) != null){
            // read peer info
            String tmp[] = s.split(" ");
            int peerId = Integer.parseInt(tmp[0]);
            String peerHost = tmp[1];
            int port = Integer.parseInt(tmp[2]);
            mapPeers.put(peerId, new Pair<String, Integer>(peerHost, port));
            
            // initiate peer bitfield
            int containFile = Integer.parseInt(tmp[3]);
            boolean[] bitField = new boolean[Constants.NUMBER_OF_FIELDS];
            Arrays.fill(bitField, containFile > 0);
            mapBitFields.put(peerId, bitField);
            if (peerId == Constants.PEER_ID)
                FileManager.getInstance().setContainFile(containFile > 0);
            
            
            // initiate peer choke
            mapChoke.put(peerId, true);
            
            // initiate every other peers are choking this peer
            mapIsChoked.put(peerId, true);
            
            // initiate interested
            mapInterested.put(peerId, false);
            
            // initiate requesting
//            boolean[] requesting = new boolean[Constants.NUMBER_OF_FIELDS];
//            Arrays.fill(requesting, false);
//            mapRequesting.put(peerId, requesting);
            
            // initiate pending request
            mapRequestPending.put(peerId, false);

            // initiate downloading rate
            mapDownloadingRate.put(peerId, 0);
            
            Constants.NUMBER_OF_PEERS ++;
            if (peerId == Constants.PEER_ID)
                Constants.NUMBER_OF_ACTIVE_PEERS = Constants.NUMBER_OF_PEERS;
        }
        
        buff.close();
        
        // initiate fields this peers has requested
        hasRequested = new boolean[Constants.NUMBER_OF_FIELDS];
        Arrays.fill(hasRequested, false);
        // log.configure(Constants.PEER_ID);
    }
    
    // return list of peers which this peer need to actively connect
    public Vector<Pair<String, Integer>> getListPeerServers(){
        Vector<Pair<String, Integer>> result = new Vector<>();
        ArrayList<Integer> listPeerIds = new ArrayList<>(mapPeers.keySet());
        for(int i=0; i<listPeerIds.size(); i++){
            if (listPeerIds.get(i) < Constants.PEER_ID){
                result.add(mapPeers.get(listPeerIds.get(i)));
            }
        }
        return result;
    }
    
    public Pair<String,Integer> getPeerInfo(int peerId){
        return mapPeers.get(peerId);
    }
    
    public synchronized void addSession(int peerId, Session session){
        mapSession.put(peerId, session);
    }
    
    public synchronized void addRequesting(int peerId, int field){
        mapRequesting.put(peerId, field);
    }
    
    public boolean isPeerServer(String host, int port){
        ArrayList<Pair<String, Integer>> listPeers = new ArrayList<>(mapPeers.values());
        for (int i=0; i<listPeers.size(); i++){
            if (listPeers.get(i).getFirst().equals(host) && listPeers.get(i).getSecond() == port)
                return true;
        }
        return false;
    }
    
    public int getPeerId(String host, int port){
        ArrayList<Integer> listPeerIds = new ArrayList<>(mapPeers.keySet());
        for (int i=0; i<listPeerIds.size(); i++){
            int id = listPeerIds.get(i);
            if (mapPeers.get(id).getFirst().equals(host) && mapPeers.get(id).getSecond() == port){
                return id;
            }
        } 
        
        return -1;
    }
    
    public synchronized int getPeerId(Session session){
        ArrayList<Integer> listPeerIds = new ArrayList<>(mapSession.keySet());
        for (int i=0; i<listPeerIds.size(); i++){
            int id = listPeerIds.get(i);
            if (mapSession.get(id).equals(session))
                return id;
        }
        return -1;
    }
    
    public boolean[] getBitfield(int peerId){
        return mapBitFields.get(peerId);
    }
    
    public void addBitField(int peerId, boolean[] bitfield){
        mapBitFields.put(peerId, bitfield);
    }
    
    public int getCurrentNumberOfFields(int peerId){
        boolean[] field = mapBitFields.get(peerId);
        int count = 0;
        for (int i=0; i<field.length; i++){
            if (field[i]) count++;
        }
        return count;
    }
    
    public boolean isInterested(int peerId){
        // return if current peer is interested in peer which has peerId
        boolean[] bitfieldOfCurrentPeer = mapBitFields.get(Constants.PEER_ID);
        boolean[] bitfieldOfTargetPeer = mapBitFields.get(peerId);
        
        if (bitfieldOfTargetPeer != null){
            for (int i=0; i<Constants.NUMBER_OF_FIELDS; i++){
                if (!bitfieldOfCurrentPeer[i] && bitfieldOfTargetPeer[i] && !hasRequested[i])
                    return true;
            }
        }
        
        return false;
    }
    
    public synchronized void selectPreferredNeighbors() throws IOException{ // concurrent access mapDownloading rate (?)
//        log.info("Start reselect prefered neighbors");
        ArrayList<Integer> peerIds = new ArrayList<>(mapSession.keySet()); // only consider active peers
        // sort peerIds in order of downloading rate descending in previous interval
        for (int i=0; i<peerIds.size(); i++){
            for (int j=i+1; j<peerIds.size(); j++){
                if (mapDownloadingRate.get(peerIds.get(i)) < mapDownloadingRate.get(peerIds.get(j))){
                    int tmp = peerIds.get(i);
                    peerIds.set(i, peerIds.get(j));
                    peerIds.set(j, tmp);
                } else if (mapDownloadingRate.get(peerIds.get(i)) == mapDownloadingRate.get(peerIds.get(j))){
                    // shuffer if 2 peers' rates is equal
                    int tmp = r.nextInt(2);
                    if (tmp == 1){
                        tmp = peerIds.get(i);
                        peerIds.set(i, peerIds.get(j));
                        peerIds.set(j, tmp);
                    }
                }
            }
        }
                
        String preferLog = "";
        for (int i=0; i<peerIds.size(); i++){
            if (i < Constants.NUMBER_OF_PREFERRED_NEIGHBORS)
                preferLog  = preferLog + peerIds.get(i) + ".";
        }
        log.info("Peer " + Constants.PEER_ID + " has the preferred neighbors " + preferLog);
        
        // select top k prefered neighbors, choke neighbors who are currently unchoked
        for (int i=0; i<peerIds.size(); i++){
            int peerId = peerIds.get(i);
            if (i < Constants.NUMBER_OF_PREFERRED_NEIGHBORS){ // this peer get unchoked only when it is currently choked
                if (mapChoke.get(peerId)){                
                    mapChoke.put(peerId, false);
                    // send unchoke msg to peerId
                    Session session = mapSession.get(peerId);
                    if (session != null){
//                        log.info("Prefered Neighbors: Send unchoke msg to peer " + peerId);
                        handler.sendChokeMsg(false, session);
                        if (mapRequesting.containsKey(peerId)){
//                            log.info("Solve pending request: Send piece " + mapRequesting.get(peerId) + " to peer " + peerId);
                            handler.sendPieceMsg(mapRequesting.get(peerId), session);
                            mapRequesting.remove(peerId);
                        }
                    }
                }
            } else { // choke peers that are currenly unchoked
                if (!mapChoke.get(peerId)){
                    mapChoke.put(peerId, true);
                    // send choke msg to peerId
                    Session session = mapSession.get(peerId);
                    if (session != null){
                        handler.sendChokeMsg(true, session);
//                        log.info("Prefered Neighbors: Send choke msg to peer " + peerId);
                    }
                }
            }
            
        }

        // reset map downloading rate
        for (int i=0; i<peerIds.size(); i++)
            mapDownloadingRate.put(peerIds.get(i), 0);
        
    }
    
    public synchronized void selectOptimisticUnchokedNeighbor() throws IOException{
//        log.info("start reselecting optimistic unchoked neighbor");
        ArrayList<Integer> peerIds = new ArrayList<>(mapSession.keySet());
        Vector<Integer> candidate = new Vector<>(); // store ids of choked but interested peers
        for (int i=0; i<peerIds.size(); i++){
            int peerId = peerIds.get(i);
            if (mapChoke.get(peerId) && mapInterested.get(peerId)){
                candidate.add(peerId);
            }
        }
        
        // randomly select optimistical unchoked neighbor
        if (candidate.isEmpty()) return;
        
        int select = r.nextInt(candidate.size());
        int peerId = candidate.get(select);
        mapChoke.put(peerId, false);
        // send choke msg to peerId
        Session session = mapSession.get(peerId);
        if (session != null){
            handler.sendChokeMsg(false, session);
            log.info("Peer " + Constants.PEER_ID + " has the optimistically unchoked neighbor " + peerId);
            if (mapRequesting.containsKey(peerId)){
//                log.info("Solve pending request: Send piece " + mapRequesting.get(peerId) + " to peer " + peerId);
                handler.sendPieceMsg(mapRequesting.get(peerId), session);
                mapRequesting.remove(peerId);
            }
        }
    }
    
    public synchronized void addInterested(int peerId, boolean isInterested){
        mapInterested.put(peerId, isInterested);
    }
    
    public void addIsChoked(int peerId, boolean isChoked){
        mapIsChoked.put(peerId, isChoked);
    }
    
    public boolean isChokedByPeer(int peerId){
        return mapIsChoked.get(peerId);
    }
    
    public synchronized boolean isPeerChoked(int peerId){
        return mapChoke.get(peerId);
    }
    
    public boolean isRequestPending(int peerId){
        return mapRequestPending.get(peerId);
    }
    
    public void addRequestPending(int peerId){
        mapRequestPending.put(peerId, true);
    }
    
    public void removeRequestPending(int peerId){
        mapRequestPending.put(peerId, false);
    }
    
    public void addRequestedField(int field){
        hasRequested[field] = true;
    }
    
    // return -1 if there is no field this peer need, otherwise return index of random selection
    public int randomSelectFieldToRequest(int peerId){
        Vector<Integer> candidate = new Vector<>();
        boolean[] fieldsOfPeer = mapBitFields.get(peerId);
        boolean[] currentFields = mapBitFields.get(Constants.PEER_ID);
        for (int i=0; i<fieldsOfPeer.length; i++){
            if (fieldsOfPeer[i] && !currentFields[i] && !hasRequested[i])
                candidate.add(i);
        }
        if (candidate.isEmpty()) return -1;
        int random = r.nextInt(candidate.size());
        return candidate.get(random);
    }
    
    public void addField(int peerId, int field){
        mapBitFields.get(peerId)[field] = true;
    }
    
    public synchronized void increaseDownloadRate(int peerId){
        int tmp = mapDownloadingRate.get(peerId);
        tmp++;
        mapDownloadingRate.put(peerId, tmp);
    }
    
    public synchronized ArrayList<Session> getAllActiveSession(){
        return new ArrayList<Session>(mapSession.values());
    }
    
    public synchronized void checkFileStatusOfAllPeers() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        boolean isFinished = true;
        ArrayList<Integer> peerIds = new ArrayList<>(mapBitFields.keySet());
        for (int i=0; i<peerIds.size(); i++){
            boolean[] bitfield = mapBitFields.get(peerIds.get(i));
            for (int j=0; j<bitfield.length; j++){
                isFinished &=bitfield[j];
                if (!isFinished) break;
            }
            if (!isFinished) break;
        }
        
        if (isFinished){
            log.info("All peers have received file, process terminates");            
            Scheduler.getInstance().stop();
            FileManager.getInstance().finish();
            
            // close all session, close all socket
            ArrayList<Integer> peerIdsOfSesions = new ArrayList<>(mapSession.keySet());
            for (int i=0; i<peerIdsOfSesions.size(); i++){
                int p = peerIdsOfSesions.get(i);
                if (p > Constants.PEER_ID){
                    Session se = mapSession.get(p);
                    se.close();
                }
            }
//            ArrayList<Session>sessionsList = getAllActiveSession();
//            for (int i=0; i<sessionsList.size(); i++)
//                sessionsList.get(i).close();
            
            PeerConnection.getInstance().close();
            
//            timer.cancel();
        }
    }
}
