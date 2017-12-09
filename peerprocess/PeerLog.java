/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.LogManager;
import java.util.logging.Logger;    // need to review this library
import java.util.logging.SimpleFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author Alan Liou
 *
 * Main class to log events 
 *  
 *
 */
public class PeerLog {
    
    /*
    public PeerLog(int peerId) {
        this (peerId, LogHelper.getLogger());
    }

    public PeerLog(int peerId, LogHelper LogHelper) {
        _msgHeader = ": Peer " + peerId;
        _logHelper = LogHelper;
    }       
    
        Might get rid of this
    */
    

    private static Logger _l;
    private static PeerLog _log = null;

    public PeerLog() {

    }
    private PeerLog (Logger log) {
        _l = log;
    }

    public void configure (int peerId) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        //Properties properties = new Properties();
        //properties.load(LogHelper.class.getResourceAsStream(CONF));
        Handler handler = new FileHandler ("log_peer_" + peerId + ".log");
        //Formatter formatter = (Formatter) Class.forName(properties.getProperty("java.util.logging.FileHandler.formatter")).newInstance();
        myFormatter formatter = new myFormatter();
        handler.setFormatter(formatter);
        handler.setLevel(Level.INFO);
        _log._l.addHandler(handler);
    }

    public static PeerLog getLogger (int peerId) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        if (_log == null){
            _log = new PeerLog (Logger.getLogger("CNT5106C"));
            _log.configure(peerId);
        }
        return _log;
    }

    public synchronized void conf (String msg) {
        _l.log(Level.CONFIG, msg);
    }

    public synchronized void debug (String msg) {
        _l.log(Level.FINE, msg);
    }

    public synchronized void info (String msg) {
        _l.log (Level.INFO, msg);
    }

    public synchronized void severe (String msg) {
        _l.log(Level.SEVERE, msg);
    }

    public synchronized void warning (String msg) {
        _l.log(Level.WARNING, msg);
    }

    public synchronized void severe (Throwable e) {
        _l.log(Level.SEVERE, stackTraceToString (e));
    }

    public synchronized void warning (Throwable e) {
        _l.log(Level.WARNING, stackTraceToString (e));
    }

    private static String stackTraceToString (Throwable t) {
        final Writer sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
}
class myFormatter extends SimpleFormatter{
    public myFormatter() {super();}
    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
    public String format(LogRecord record){
        if(record.getLevel() == Level.INFO){
            String timeStamp = getCurrentTimeStamp();
            return timeStamp + ": "+ record.getMessage() + "\r\n";
        }else{
            return super.format(record);
        }
    }
}