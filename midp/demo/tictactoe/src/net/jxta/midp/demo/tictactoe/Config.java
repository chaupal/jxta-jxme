/************************************************************************
 *
 * $Id: Config.java,v 1.7 2005/06/09 17:17:31 hamada Exp $
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

package net.jxta.midp.demo.tictactoe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import javax.microedition.midlet.MIDlet;

public final class Config extends Form
    implements CommandListener {

    private static final String RECORD_STORE_NAME = "tictactoe-config";
    private static final int RECORD_ID = 1;
    private static final int MAX_TEXTFIELD_SIZE = 256;
    private static final int DEFAULT_POLL_INTERVAL = 0;

    private TextField tfRelayHost = null;
    private TextField tfRelayPort = null;
    private TextField tfIdentity = null;
    private TextField tfPollInterval = null;

    private MIDlet midlet = null;
    private Display display = null;
    private Displayable back = null;

    private int pollInterval = DEFAULT_POLL_INTERVAL;

    public Config(MIDlet midlet, Display display, Displayable back) {
        super("TicTacToe Configuration");
        this.midlet = midlet;
        this.display = display;
        this.back = back;

        readConfig();

        append(tfRelayHost);
        append(tfRelayPort);
        append(tfIdentity);
        append(tfPollInterval);

        Command cmdOK = new Command("OK", Command.OK, 1);
        addCommand(cmdOK);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        String label = c.getLabel();
        if (d == this) {
            if (label.equals("OK")) {
                storeConfig();
                display.setCurrent(back);
                return;
            }
        } 
    }

    private boolean readConfig() {
        String prop = null;

        prop = midlet.getAppProperty("RelayHost");
        tfRelayHost = new TextField("Relay host: ", 
                                    prop == null ? "192.18.37.36" : prop, 
                                    MAX_TEXTFIELD_SIZE,
                                    TextField.ANY);
        prop = midlet.getAppProperty("RelayPort");
        tfRelayPort = new TextField("Relay port: ", 
                                    prop == null ? "9700" : prop,
                                    9,
                                    TextField.NUMERIC);

        prop = midlet.getAppProperty("Identity");
        tfIdentity = new TextField("Identity: ", 
                                   prop == null ? "" : prop,
                                   MAX_TEXTFIELD_SIZE,
                                   TextField.ANY);

        prop = midlet.getAppProperty("PollInterval");
        tfPollInterval = new TextField("Poll interval: ", 
                                       prop == null ? 
                                         Integer.toString(pollInterval) : prop,
                                       MAX_TEXTFIELD_SIZE,
                                       TextField.NUMERIC);

        try {
            RecordStore rs = 
                RecordStore.openRecordStore(RECORD_STORE_NAME, false);
            byte[] data = rs.getRecord(RECORD_ID);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            tfRelayHost.setString(dis.readUTF());
            tfRelayPort.setString(dis.readUTF());
            tfIdentity.setString(dis.readUTF());
            tfPollInterval.setString(dis.readUTF());

            try {
                pollInterval = Integer.parseInt(tfPollInterval.getString());
            } catch (NumberFormatException ex) {
                TicTacToe.showAlert("Read Config", 
				    "Error parsing poll interval: " + 
				    tfPollInterval.getString(),
				    AlertType.WARNING, 
                                    Alert.FOREVER,
				    back);
            }

            System.out.println("Read config:" +
                               " host=" + tfRelayHost.getString() +
                               " port=" + tfRelayPort.getString() + 
                               " identity=" + tfIdentity.getString() +
			       " pollInterval=" + pollInterval);
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }

        return true;
    }

    private void storeConfig() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(tfRelayHost.getString());
            dos.writeUTF(tfRelayPort.getString());
            dos.writeUTF(tfIdentity.getString());
            dos.writeUTF(tfPollInterval.getString());
            byte[] data = baos.toByteArray();
        
            RecordStore rs = 
                RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            int recordId = RECORD_ID;
            try {
                rs.getRecord(recordId);
                rs.setRecord(recordId, data, 0, baos.size());
            } catch (RecordStoreException rex) {
                recordId = rs.addRecord(data, 0, baos.size());
                // assert (recordId == RECORD_ID)
            }
            System.out.println("Saved config:" +
                               " recordId=" + recordId + 
                               " host=" + tfRelayHost.getString() +
                               " port=" + tfRelayPort.getString() + 
                               " identity=" + tfIdentity.getString() +
			       " pollInterval=" + tfPollInterval.getString());
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            try {
                dos.close();
                baos.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    public String getRelayHost() {
        return tfRelayHost.getString();
    }

    public String getRelayPort() {
        return tfRelayPort.getString();
    }

    public String getIdentity() {
        return tfIdentity.getString();
    }

    public int getPollInterval() {
        return pollInterval;
    }
}
