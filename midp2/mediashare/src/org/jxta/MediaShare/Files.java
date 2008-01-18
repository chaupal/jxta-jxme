// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;

public class Files {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //

    //
    // Constructors
    //

    public Files() {
    }

    //
    // My Methods
    //

    public Vector getFileInfo()
            throws Exception {
        int idx;
        Vector files = new Vector();

        listDir(0, files, "/", FileSystemRegistry.listRoots());

        return (files);
    }

    public void listDir(int indent, Vector files, String prefix, Enumeration en) {
        String fullName;
        while (en.hasMoreElements()) {
            String fileName = (String) en.nextElement();
            fullName = prefix + fileName;
            try {
                FileConnection fc = (FileConnection) Connector.open("file://" + fullName, Connector.READ);
                if (fc.exists())
                    listFile(indent, files, fc);
            } catch (Exception ex) {
            }

        }
    }

    public void listFile(int indent, Vector files, FileConnection fc) {
        String path = fc.getPath();
        String name = fc.getName();

        if (fc.isDirectory()) {
            //strs.addElement(indentStr + path + name + " +");
            try {
                listDir(indent + 2, files, path + name, fc.list());
            } catch (Exception ex) {
            }
        } else if (name.toLowerCase().endsWith(".jpg"))
            files.addElement(fc);
    }

    public String strIndent(int n) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < n; ++i)
            buf.append(" ");
        return (buf.toString());
    }
}
