/*
 * File.java
 *
 * Created on August 28, 2006, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.jxta.jxtacast;

import java.io.*;
import java.util.*;
/**
 *
 * @author tra
 */
public class File {
    
    public StringBuffer buf;
    public static final String separator = "\\";
    public Hashtable fileSystem = null;
    public String path;
    
    public File(String path, Hashtable fileSystem) {
          this.fileSystem=fileSystem;
          this.path = path;
    } 
    
    public int length() {
            byte[] buf = (byte[]) fileSystem.get(path);
            return buf.length;
    }   
    
    public String getName() {
        return path;
    }
    
    public byte[] getData() {
        return ((byte[]) fileSystem.get(path));
    }
    
    public void writeData(byte[] data) {
        fileSystem.put(path, data);
    }
    
}
