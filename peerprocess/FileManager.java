/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peerprocess;

import common.Constants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *
 * @author mythai
 */
public class FileManager {
    
    private static FileManager instance;
    
    private boolean containFile = false; // true if peer contain file AT FIRST

    private String directory;
    
    public FileManager() {
        directory = "peer_" + Constants.PEER_ID + "/";
        File theDir = new File( "peer_" + Constants.PEER_ID);
        if (!theDir.exists())
            theDir.mkdir();
    }
    

    public void setContainFile(boolean containFile) throws IOException {
        this.containFile = containFile;
        if (containFile){
            // split file into small pieces
            byte[] re = Files.readAllBytes(Paths.get(directory + Constants.FILE_NAME));
            for (int i=0; i<Constants.NUMBER_OF_FIELDS; i++){
                // calculate size of this piece, size of last piece could be smaller than constant.piece_size
                int pieceSize = Constants.FILE_SIZE  - i * Constants.PIECE_SIZE;
                if (pieceSize > Constants.PIECE_SIZE)
                    pieceSize = Constants.PIECE_SIZE;
                
                // take only portion bytes from file
                byte[] bytes = new byte[pieceSize];
                for (int j=0; j<pieceSize; j++){
                    bytes[j] = re[i * Constants.PIECE_SIZE + j];
                }
                
                // save to small separate file
                String file_name = directory + "file_part_" + Integer.toString(i) + ".dat";
    	FileOutputStream fos = new FileOutputStream(file_name);
    	fos.write(bytes);
    	fos.close();
            }
        }
    }
    
    public static FileManager getInstance(){
        if (instance == null)
            instance = new FileManager();
        return instance;
    }
    
    public byte[] getFieldBytes(int field) throws IOException{
        // TODO Ravi please write your code here
        String file_name = directory + "file_part_" + Integer.toString(field) + ".dat";
        byte[] re = Files.readAllBytes(Paths.get(file_name));
        return re;
    }
    
    public void storeFieldBytes(int field, byte[] bytes) throws FileNotFoundException, IOException {
        //TODO Ravi's task
        String file_name = directory + "file_part_" + Integer.toString(field) + ".dat";
        FileOutputStream fos = new FileOutputStream(file_name);
        fos.write(bytes);
        fos.close();
    }
    
    // call when this peer recieve all fields
    // Ravi's task
    public void finish() throws IOException{
        if (!containFile){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for(int field = 0; field < Constants.NUMBER_OF_FIELDS; field++) {
                    String file_name = directory + "file_part_" + Integer.toString(field) + ".dat";
                    byte[] current_byte = Files.readAllBytes(Paths.get(file_name));
                    outputStream.write(current_byte);
            }
            byte file_final[] = outputStream.toByteArray();
            FileOutputStream fos = new FileOutputStream(directory + Constants.FILE_NAME);
            fos.write(file_final);
            fos.close();
        }
        
        // remove all small pieces
        for (int i=0; i<Constants.NUMBER_OF_FIELDS; i++){
            File file = new File(directory + "file_part_" + Integer.toString(i) + ".dat");
            file.delete();
        }
    }
    
    
}
