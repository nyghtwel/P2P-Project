/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.Arrays;

/**
 *
 * @author mythai
 */
public class Converter {
    private static Converter instance;

    public Converter() {
    
    }
    
    public static Converter getInstance(){
        if (instance == null)
            instance = new Converter();
        return instance;
    }
    
    // insert integer value to 4 specific bytes of array, start at startIndex    
    public void integerTo4Bytes(int value, byte[] arrays, int startIndex){
        arrays[startIndex] = (byte)(value >> 24);
        arrays[startIndex + 1] = (byte)(value >> 16);
        arrays[startIndex + 2] = (byte)(value >> 8);
        arrays[startIndex + 3] = (byte)(value);
    }
    
    public int getIntegerFrom4Bytes(byte[] array, int startIndex){
        int r = array[startIndex] << 24 
                | (array[startIndex + 1] & 0xFF) << 16 
                | (array[startIndex + 2] & 0xFF) << 8 
                | (array[startIndex + 3] & 0xFF);
        return r;
    }
    
    public void booleanArray2Bytes(boolean[] bitfield, byte[] array, int startIndex){
        byte b = 0;
        int index = startIndex;
        for (int i=0; i<bitfield.length; i++){
            if (bitfield[i]){
                b = (byte) (b | 0x1);
            }
            
            if (i%8 == 7){ // add byte
                array[index] = b;
                index ++;
            }
            b = (byte) ((b << 1) & 0xFF);
        }
        
        if (bitfield.length % 8 > 0){
            b = (byte)((b << (7 - bitfield.length % 8)) & 0xFF);
            array[index] = b;
        }
    }
    
    // used to convert bitfield msg to bitfield boolean array, length is length of boolean array
    public boolean[] bytes2BooleanArray(byte[] array, int startIndex, int length){
        boolean[] bits = new boolean[length];
        Arrays.fill(bits, false);
        
        int index = startIndex;
        byte b = array[index];
        int count = 0;
        while (count < length){
            bits[count] =  ((b & 0x80) >> 7) > 0;
            count++;
            b = (byte) ((b << 1) & 0xFF);
            if (count % 8 == 0 && count < length){
                index ++;
                b = array[index];
            }
        }
        
        return bits;
    }
    
}
