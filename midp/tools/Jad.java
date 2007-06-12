/************************************************************************
 *
 * $Id: Jad.java,v 1.6 2003/03/28 04:56:12 shinyability Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

package net.jxta.j2me.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public final class Jad extends Task {

    // the separators used between key/value pairs in the JAD/JAM files
    private static final String JAD_SEPARATOR = ":";
    private static final String JAM_SEPARATOR = "=";

    private File jarFile = null;
    private File jadFile = null;

    // is the JAD file a JAM file? (used by NTT DoCoMo's iMode)
    private boolean isJam = false;
    private String separator = JAD_SEPARATOR;

    public Jad() {
    }

    public synchronized void setJad(File jadFile) {
        this.jadFile = jadFile;
    }

    public synchronized void setJar(File jarFile) {
        this.jarFile = jarFile;
    }

    public synchronized void execute() throws BuildException {
        try {
            tryExecute();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    private void tryExecute() throws IOException {

        if (!jarFile.exists()) {
            throw new BuildException("Cannot find JAR file: " + 
                                     jarFile.getName());
        }

        if (!jadFile.canRead()) {
            throw new BuildException("Cannot read JAD file: " + 
                                     jadFile.getName());
        }

        if (!jadFile.canWrite()) {
            throw new BuildException("Cannot write to JAD file: " + 
                                     jadFile.getName());
        }

	String jadFileExt = jadFile.getName().toLowerCase();
	if (jadFileExt.endsWith("jam")) {
	    isJam = true;
	    separator = JAM_SEPARATOR;
	}

        Hashtable map = new Hashtable();
        Vector keys = new Vector();

        FileReader fileReader = new FileReader(jadFile);
        BufferedReader br = new BufferedReader(fileReader);
        try {
            for (String line = br.readLine(); 
                 line != null; 
                 line = br.readLine()) {
                int colon = line.indexOf(separator);
                if (colon < 0) {
                    throw new BuildException("Garbled JAD: missing '" +
					     separator + "' in " + line);
                }
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                putInOrder(key, value, map, keys);
            }
        } finally {
            br.close();
        }

        // update or insert if missing
	if (isJam) {
	    putInOrder("AppSize", Long.toString(jarFile.length()),
		       map, keys);

	    // iMode needs a "LastModified" entry in the JAM file
	    Date now = new Date();
	    // the specific time format for iMode
	    // Locale.US is convenient to avoid kanji-characters representing a day of week.
	    SimpleDateFormat df = 
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", java.util.Locale.US);
	    putInOrder("LastModified", df.format(now), map, keys);
	} else {
	    putInOrder("MIDlet-Jar-Size", Long.toString(jarFile.length()),
		       map, keys);
	}

        FileWriter fileWriter = new FileWriter(jadFile);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        try {
            int keySize = keys.size();
            for (int i=0; i < keySize; i++) {
                String key = (String) keys.elementAt(i);
                String value = (String) map.get(key);
                bw.write(key);
                bw.write(separator + " ");
                bw.write(value);
                bw.newLine();
            }
        } finally {
            bw.close();
        }
    }

    /** Update value or insert if missing. Preserves insertion order. */
    private void putInOrder(Object key, Object value, 
                            Hashtable map, Vector keys) {
        if (map.put(key, value) == null) {
            keys.addElement(key);
        }
    }
}
