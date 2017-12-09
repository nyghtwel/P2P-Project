/*
package peerprocess.log;

import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.FileHandler;

import java.util.logging.SimpleFormatter;


/**
 *
 * @author Alan Liou
 */
 /*
public class LogHelper {
    public static void main(String[] args) {  

        Logger logger = Logger.getLogger("MyLog");  
        FileHandler fh;  

        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("MyLogFile.log");  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

            // the following statement is used to log any messages  
            logger.info("My first log");  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  

        logger.info("Hi How r u?");  

    }
//      public synchronized void conf (String msg) {
//        _l.log(Level.CONFIG, msg);
//    }
//
//    public synchronized void debug (String msg) {
//        _l.log(Level.FINE, msg);
//    }
//
//    public synchronized void info (String msg) {
//        _l.log (Level.INFO, msg);
//    }
//
//    public synchronized void severe (String msg) {
//        _l.log(Level.SEVERE, msg);
//    }
//
//    public synchronized void warning (String msg) {
//        _l.log(Level.WARNING, msg);
//    }

    private static String stackTraceToString (Throwable t) {
        final Writer sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}*/