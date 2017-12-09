/*
package peerprocess.log;
/*
*
*
* @author Alan Liou
*/
/*
public class EventLogger {

	private final LogHelper _logHelper;
	private final String _msgHeader;

//	public Event Logger(int peerId) {
//		this (peerId, LogHelper.getLogger());
//	}
//
	public EventLogger(int peerId, LogHelper LogHelper) {
		_msgHeader = ": Peer " + peerId;
		_logHelper = LogHelper;
	}
//
//	public void peerConnection(int peerId, boolean isConnectingPeer) {
//		final String msg = getLogMsgHeader() + (isConnectingPeer ? " makes a connection to Peer %d.":" is connected from Peer %d.");
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//	public void changeOfPreferredNeighbors(String preferredNeighbors) {
//		final String msg = getLogMsgHeader() + " has preferred neighbors %s.";
//		_logHelper.info(String.format(msg, preferredNeighbors));
//	} 
//
//	public void changeOfOptimisticallyUnchokedNeighbors(String preferredNeighbors) {
//		final String msg = getLogMsgHeader() + " has the optimistically unchoked neighbor %s.";
//		_logHelper.info(String.format(msg, preferredNeighbors));
//	}
//
//	public void unchokeMessage(int peerId) {
//		final String msg = getLogMsgHeader() + "is unchoked by %d.";
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//	public void chokeMessage(int peerId) {
//		final String msg = getLogMsgHeader() + "is chocked by %d.";
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//	public void haveMessage(int peerId, int pieceIndex) {
//		final String msg = getLogMsgHeader() + " received the 'have' message from %d for the piece %d.";
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//	public void interestedMessage(int peerId) {
//		final String msg = getLogMsgHeader() + " received the 'interested' message from %d.";
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//	public void notInterestedMessage(int peerId) {
//		final String msg = getLogMsgHeader() + " received the 'not interested' message from %d.";
//		_logHelper.info(String.format(msg, peerId));
//	}
//
//
//	public void downloadedPieceMessage(int peerId, int pieceIndex, int numOfPieces) {
//		final String msg = getLogMsgHeader() + " has downloaded the piece %d from peer %d. Now the number of pieces it has is %d.";
//		_logHelper.info(String.format(msg, pieceIndex, peerId, numOfPieces));
//	}
//
//	public void downloadedFileMessage() {
//		final String msg = getLogMsgHeader() + " has downloaded the complete file.";
//		_logHelper.info(String.format(msg))
//	}

	private String getLogMsgHeader() {
		return (String.format(_msgHeader));
	}
	
}*/