/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import common.Converter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import peerprocess.session.Session;

/**
 *
 * @author mythai
 */
public class Handler{
    
    private static Handler instance;
    
    private DataManager dataManager;
    private FileManager fileManager;
    private PeerLog log;
    private Converter converter;
    
    public static Handler getInstance() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        if (instance == null)
            instance = new Handler();
        return instance;
    }
    
    public Handler() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        dataManager = DataManager.getInstance();
        //log = new PeerLog();  // PeerLog.configure(peerId) to call a logger and configure it w/ a peerId
        fileManager = FileManager.getInstance();
        converter = Converter.getInstance();
        log = PeerLog.getLogger(Constants.PEER_ID);
    }    
    
    // called when there is new connection to this peer
    public synchronized void sessionOpened(Session session) throws Exception {
//        log.info("Open session with " + session.getRemoteAddress());
        String addr = String.valueOf(session.getRemoteAddress()); // format /host:port
        String[] tmp = addr.split(":");
        String host = tmp[0].substring(1);
        int port = Integer.parseInt(tmp[1]);
        if (dataManager.isPeerServer(host, port)){
            sendHandshakingMsg(session);
        }
    }

    // called when there is a new message sent to this peer
    public synchronized void messageReceived(Session session, Object message) throws Exception {
        if (message instanceof byte[]){
            byte[] msg  = (byte[]) message;
            if (msg.length > 19 && (new String(msg).substring(0, 18)).equals("P2PFILESHARINGPROJ")){
                processHandshakingMsg(msg, session);
            } else {
                Constants.MessageType type = Constants.convertByte2MsgType(msg[4]);
                switch(type){ // message type
                    case CHOKE:
                    case UNCHOKE:
                        processChokedMsg(type, session);
                        break;
                    case BITFIELD:
                        processBitfieldMsg(msg, session);
                        break;
                    case HAVE:
                        processHaveMsg(msg, session);
                        break;
                    case INTERESTED:
                    case NOTINTERESTED:
                        processInterestedMsg(type, session);
                        break;
                    case PIECE:
                        processPieceMsg(msg, session);
                        break;
                    case REQUEST:
                        processRequestMsg(msg, session);
                        break;
                    case UNIDENTIFIED:
                        break;
                }
            }
        }
    }
    
    private void sendHandshakingMsg(Session session) throws UnsupportedEncodingException, IOException{
        byte[] msg = new byte[32];
        Arrays.fill(msg, (byte)0);
        
        // initiate header
        byte[] tmp = new String("P2PFILESHARINGPROJ").getBytes("UTF-8");
        for (int i=0; i<tmp.length; i++)
            msg[i] = tmp[i];
        
        // initiate peer ID field
        converter.integerTo4Bytes(Constants.PEER_ID, msg, 28);
                
        session.write(msg);
    }
    
    private void processHandshakingMsg(byte[] msg, Session session) throws IOException{
        int peerId = converter.getIntegerFrom4Bytes(msg, 28);
//        log.info("Receive handshaking message from peer " + peerId);
        dataManager.addSession(peerId, session);
        if (peerId > Constants.PEER_ID){
            log.info("Peer " + Constants.PEER_ID + "is connected from Peer " + peerId);
            sendHandshakingMsg(session);
        } else {
            log.info("Peer " + Constants.PEER_ID + " make a connection to " + peerId);
            sendBitFieldMsg(session);
        }
    }
    
    private void sendBitFieldMsg(Session session) throws IOException{
        // bitfield msg all ways has length 4 (msg length) + 1 (msg type) + ceil(number of fields/8) bytes
        byte[] msg = new byte[5 + (int)Math.ceil(((double)Constants.NUMBER_OF_FIELDS)/8)];
        msg[4] = Constants.convertMsgType2Byte(Constants.MessageType.BITFIELD);
        
        // initiate message length
        converter.integerTo4Bytes((int)Math.ceil(((double)Constants.NUMBER_OF_FIELDS)/8), msg, 0);
        
        // insert current peer bitfield
        boolean[] bitfield = dataManager.getBitfield(Constants.PEER_ID);
        converter.booleanArray2Bytes(bitfield, msg, 5); // insertion starts from index of start byte of message payload 

        session.write(msg);
    }  
    
    private void processBitfieldMsg(byte[] msg, Session session) throws IOException{
        int peerId = dataManager.getPeerId(session);
        
//        log.info("Receive bitfield message from " + peerId);
                
        boolean[] bitField = converter.bytes2BooleanArray(msg, 5, Constants.NUMBER_OF_FIELDS);
        dataManager.addBitField(peerId, bitField);
        
        if (peerId > Constants.PEER_ID)
            sendBitFieldMsg(session);
        
        // TODO send interest or uninterest msg
        boolean isInterested = dataManager.isInterested(peerId);
        sendInterestedMsg(isInterested, session);
    }
    
    private void sendInterestedMsg(boolean isInterested, Session session) throws IOException{
        byte[] msg = new byte[5];
        Arrays.fill(msg, (byte)0);
        
        // initiate msg interest
        msg[4] = Constants.convertMsgType2Byte(isInterested? Constants.MessageType.INTERESTED 
                : Constants.MessageType.NOTINTERESTED);
        
        session.write(msg);
    }
    
    private void processInterestedMsg(Constants.MessageType type, Session session){
        int peerId = dataManager.getPeerId(session);
        dataManager.addInterested(peerId, type == Constants.MessageType.INTERESTED);
        if (type == Constants.MessageType.INTERESTED)
            log.info("Peer " + Constants.PEER_ID + " received 'interested' message from " + peerId);
        else
            log.info("Peer " + Constants.PEER_ID + " received 'not interested' message from " + peerId);
    }
    
    private void processChokedMsg(Constants.MessageType type, Session session) throws IOException{
        int peerId = dataManager.getPeerId(session);
        dataManager.addIsChoked(peerId, type == Constants.MessageType.CHOKE); ; 
//        log.info("Receive choked/unchoked message from " + peerId + " " + type);
        
        if (type == Constants.MessageType.CHOKE)
            log.info("Peer " + Constants.PEER_ID + " is choked by " + peerId);
        else 
            log.info("Peer " + Constants.PEER_ID + " is unchoked by " + peerId);

        if (type == Constants.MessageType.UNCHOKE && !dataManager.isRequestPending(peerId)){
            int field = dataManager.randomSelectFieldToRequest(peerId);
            if (field != -1){
//                log.info("Request field " + field + " from peer " + peerId);
                sendRequestMsg(field, session);
                dataManager.addRequestPending(peerId);
                dataManager.addRequestedField(field);
            }
        }
    }
    
    private void sendRequestMsg(int field, Session session) throws IOException{
        byte[] msg = new byte[9];
        Arrays.fill(msg, (byte)0);
        
        msg[4] = Constants.convertMsgType2Byte(Constants.MessageType.REQUEST);
        Converter.getInstance().integerTo4Bytes(field, msg, 5);
        
        session.write(msg);
    }
    
    public void sendPieceMsg(int field, Session session) throws IOException{
        byte[] data = fileManager.getFieldBytes(field);
        int msgLength = 1 + 4 + data.length; // 1 byte for msg type, 4 bytes for field index, the rest is for data
        
        byte[] msg = new byte[msgLength + 4];
        
        converter.integerTo4Bytes(msgLength, msg, 0);
        msg[4] = Constants.convertMsgType2Byte(Constants.MessageType.PIECE);
        converter.integerTo4Bytes(field, msg, 5);
        
        for (int i=0; i<data.length; i++){
            msg[9 + i] = data[i];
        }
        
        session.write(msg);
    }
    
    private void processRequestMsg(byte[] msg, Session session) throws IOException{
        
        int peerId = dataManager.getPeerId(session);
        int requestField = converter.getIntegerFrom4Bytes(msg, 5);
        
//        log.info("Receive request for field " + requestField + " from peer " + peerId);
        
        if (dataManager.isPeerChoked(peerId)){
//            log.info("Peer " + peerId + " is choked so save request for field " + requestField + " and reply later");
            dataManager.addRequesting(peerId, requestField);
        } else {
//            log.info("Send piece msg for field " + requestField + " to peer " + peerId);
            sendPieceMsg(requestField, session);
        }
    }
    
    public void sendChokeMsg(boolean choke, Session session) throws IOException{
        byte[] msg = new byte[5];
        Arrays.fill(msg, (byte)0);
        msg[4] = Constants.convertMsgType2Byte(choke? Constants.MessageType.CHOKE:Constants.MessageType.UNCHOKE);
        session.write(msg);
    }
    
    private void processPieceMsg(byte[] msg, Session session) throws IOException{
        int peerId = dataManager.getPeerId(session);
        int field = converter.getIntegerFrom4Bytes(msg, 5);
        
//        log.info("Receive piece msg of field " + field + " from peer " + peerId);
        int tmp = dataManager.getCurrentNumberOfFields(Constants.PEER_ID) + 1;
        log.info("Peer " + Constants.PEER_ID + " has downloaded the piece " + field + " from " + peerId 
                + ". Now the number piece it has is " + tmp);


        // remove request pending
        dataManager.removeRequestPending(peerId);
        
        // store data 
        int dataLength = converter.getIntegerFrom4Bytes(msg, 0) - 5; // we dont count 1 byte for type, 4 byte for field index
        byte[] data = new byte[dataLength];
        for (int i=0; i<dataLength; i++){
            data[i] = msg[9 + i];
        }
        fileManager.storeFieldBytes(field, data);
        
        // increase download rate
        dataManager.increaseDownloadRate(peerId);
        
        // save info into data manager
        dataManager.addField(Constants.PEER_ID, field);
        
        // broadcast have msg to all other peers
        broadcastHaveMsg(field);
        
        // continue send request to peer
        if (!dataManager.isChokedByPeer(peerId)){
            int newField = dataManager.randomSelectFieldToRequest(peerId);
            if (newField != -1){
//                log.info("Request field " + newField + " from peer " + peerId);
                sendRequestMsg(newField, session);
                dataManager.addRequestPending(peerId);
                dataManager.addRequestedField(newField);
            }
        }
    }
    
    private void broadcastHaveMsg(int field) throws IOException{
        ArrayList<Session> sessionsList = dataManager.getAllActiveSession();
        byte[] msg = new byte[9];
        msg[4] = Constants.convertMsgType2Byte(Constants.MessageType.HAVE);
        converter.integerTo4Bytes(field, msg, 5);
        
//        log.info("Broadcast have msg, field " + field);
        
        for (int i=0; i<sessionsList.size(); i++){
            Session session = sessionsList.get(i);
            session.write(msg);
        }
    }
    
    private void processHaveMsg(byte[] msg, Session session) throws IOException{
        int peerId = dataManager.getPeerId(session);
        int field = converter.getIntegerFrom4Bytes(msg, 5);
        
        log.info("Peer " + Constants.PEER_ID + " received the 'have' message from " + peerId + " for the piece " + field);
        
        // save info to data manager
        dataManager.addField(peerId, field);
        
        int tmp = dataManager.getCurrentNumberOfFields(peerId);
        if (tmp == Constants.NUMBER_OF_FIELDS)
            log.info("Peer " + peerId + " has downloaded the complete file");
        
        // send interest or uninterest msg
        boolean isInterested = dataManager.isInterested(peerId);
//        log.info("Send message interest to peer " + peerId + isInterested);
        sendInterestedMsg(isInterested, session);
    }
}
