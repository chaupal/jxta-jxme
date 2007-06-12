/************************************************************************
 *
 * $Id: Chat.java,v 1.68 2005/06/09 17:17:30 hamada Exp $
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

package net.jxta.midp.demo.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Gauge;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import javax.microedition.midlet.MIDlet;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

public final class Chat extends MIDlet 
    implements CommandListener, Runnable {

    private static final String RECORD_STORE_NAME = "jxta-midp-chat";
    private static final int CONFIG_RECORD_INDEX = 1;
    private static final int BUDDYLIST_RECORD_INDEX = 2;
    private static final int DEFAULT_POLL_INTERVAL = 1;
    private static final int DEFAULT_ALERT_TIMEOUT = 5000;
    private static final int DEFAULT_SCROLL = 3;

    private static final int MAX_TEXTFIELD_SIZE = 256;

    private static final String TALKNAME_PREFIX = "JxtaTalkUserName.";
    private static final String INSTANTP2P_GROUPNAME = "IP2PGRP";

    private static final String INSTANTP2P_PIPEID = "urn:jxta:uuid-" + 
                                                    "59616261646162614E50472050325033" + 
	                                            //"D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1D104";
                                                    "D1D1D1D1D1D141D191D191D1D1D1D1D104";

    private static final String PICSHARE_GROUPNAME = "PicShare";
    private static final String PICSHARE_PIPEID = "urn:jxta:uuid-" +
        "59616261646162614E50472050325033" +
        "5069635368614265AD5B86696C65436104";
        //"50696353686172652D5B46696C65436104";

    private static final boolean DEBUG = true;
    private static final boolean QUANTIFY = true;

    private Display display = null;
    private Form initForm = null;
    private Form replyForm = null;
    private Form configForm = null;
    private Form addBuddyForm = null;
    private Form editBuddyForm = null;
    private Form connectingForm = null;
    private Form confirmForm = null;
    private List buddyList = null;
    private ImageCanvas imageCanvas = null;

    private TextField tfRelayHost = null;
    private TextField tfRelayPort = null;
    private TextField tfIdentity = null;
    private TextField tfPollInterval = null;
    private TextField tfBuddy = null;
    private TextField tfSentMsg = null;

    private PeerNetwork peer = null;
    private String currentBuddy = null; 
    private String replyBuddy = null; 
    private Hashtable buddyIds = new Hashtable();

    private static final String[] CHAT_MODE_CHOICES = { "Group", "Private" };
    private ChoiceGroup cgChatMode = null;
    private boolean isGroupChat = true;

    private byte[] state = new byte[0];
    private int pollInterval = DEFAULT_POLL_INTERVAL;
    private Thread pollThread = null;
    private boolean stopPolling = false;
    private boolean connectInitiated = false;
    private boolean connected = false;
    private boolean sendPending = false;

    private int responseId = -1;
    public Chat() {
        initForm = new Form("JXTA Chat");

        readBuddies();
        readConfig();
        setupConfirmForm();

        imageCanvas = new ImageCanvas(this);
        Command cmdOK = new Command("OK", Command.OK, 1);
        imageCanvas.addCommand(cmdOK);
        imageCanvas.setCommandListener(this);

        Command cmdSend = new Command("Send", Command.SCREEN, 1);
        Command cmdReply = new Command("Reply", Command.SCREEN, 2);
        Command cmdConnect = new Command("Connect", Command.SCREEN, 3);
        Command cmdBuddies = new Command("Buddy List", Command.SCREEN, 4);
        Command cmdConfig = new Command("Configuration", Command.SCREEN, 5);
        Command cmdDefault = new Command("Defaults", Command.SCREEN, 6);
        Command cmdExit = new Command("Exit", Command.EXIT, 6);
        initForm.addCommand(cmdSend);
        initForm.addCommand(cmdReply);
        initForm.addCommand(cmdConnect);
        initForm.addCommand(cmdBuddies);
        initForm.addCommand(cmdConfig);
        initForm.addCommand(cmdDefault);
        initForm.addCommand(cmdExit);

        initForm.setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getCommandType() == Command.EXIT) {
            destroyApp(true);
            notifyDestroyed();
            return;
        } 

        String label = c.getLabel();
        if (d == initForm) {
            if (label.equals("Send")) {
                sendForm();
            } else if (label.equals("Reply")) {
                reply();
            } else if (label.equals("Configuration")) {
                editConfig();
            } else if (label.equals("Buddy List")) {
                editBuddies();
            } else if (label.equals("Connect")) {
                initiateConnect();
            } else if (label.equals("Defaults")) {
                display.setCurrent(confirmForm);
            }
        } else if (d == configForm) {
            if (c.getCommandType() == Command.OK) {
                int i = cgChatMode.getSelectedIndex();
                String chatMode = cgChatMode.getString(i);
                if (CHAT_MODE_CHOICES[0].equals(chatMode)) {
                    isGroupChat = true;
                } else {
                    isGroupChat = false;
                }
                storeConfig();

                // move to the buddy screen if there is'nt a current buddy
                if (currentBuddy == null) {
                    editBuddies();
                } else {
                    display.setCurrent(initForm);
                }
            } else if (c.getCommandType() == Command.BACK) {
                configForm = null;
                readConfig();
                display.setCurrent(initForm);
            }
        } else if (d == buddyList) {
            if (c.getCommandType() == Command.BACK) {
                display.setCurrent(initForm);
                storeBuddies();
            }

            if (label.equals("Chat")) {
                chatBuddy();
                storeBuddies();
            } else if (label.equals("Add")) {
                addBuddy();
            } else if (label.equals("Edit")) {
                editBuddy();
            } else if (label.equals("Delete")) {
                deleteBuddy();
                storeBuddies();
            }
        } else if (d == editBuddyForm) {
            if (c.getCommandType() == Command.OK) {
                int i = buddyList.getSelectedIndex();
                buddyList.set(i, tfBuddy.getString(), null);
                display.setCurrent(buddyList);
                storeBuddies();
            }
        } else if (d == addBuddyForm) {
            if (c.getCommandType() == Command.OK) {
                buddyList.append(tfBuddy.getString(), null);
                display.setCurrent(buddyList);
                storeBuddies();
            }
        } else if (d == replyForm) {
            if (label.equals("Send")) {
                if (send()) {
                    display.setCurrent(initForm);
                }
            } else if (label.equals("Back")) {
                display.setCurrent(initForm);
            }
        } else if (d == confirmForm) {
            if (c.getCommandType() == Command.OK) {
                resetConfig();
            } else {
                display.setCurrent(initForm);
            }
        } else if (d == imageCanvas) {
            if (c.getCommandType() == Command.OK) {
                display.setCurrent(initForm);
            }
        }
    }

    public void startApp() {
        display = Display.getDisplay(this);

        // startup in the config screen if the identity is not configured
        if ("".equals(tfIdentity.getString())) {
            editConfig();
        } else {
            display.setCurrent(initForm);
        }

        stopPolling = false;
        pollThread = new Thread(this);
        pollThread.start();
    }
    
    public void pauseApp() {
        stopPolling = true;
        pollThread = null;
    }

    public void destroyApp(boolean unconditional) {
        stopPolling = true;
        pollThread = null;

        storeConfig();
        storeBuddies();
        peer = null;
    }

    private void reply() {
        if (replyBuddy == null) {
            showAlert("Reply", 
                      "No record of an incoming message or sender",
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT,
                      initForm);
            return;
        }

        // switch the current buddy only in 1:1 chat
        if (!isGroupChat) {
            currentBuddy = replyBuddy;
            initForm.setTitle(currentBuddy);
            replyBuddy = null;
        }

        boolean isbuddy = false;
        int size = buddyList.size();
        for (int i=0; i < size; i++) {
            String buddy = buddyList.getString(i);
            if (buddy.equals(currentBuddy)) {
                isbuddy = true;
                break;
            }
        }

        // make it convenient to add buddies - automatically add any
        // buddy replied to to our buddy list
        if (!isbuddy) {
            buddyList.append(currentBuddy, null);
            storeBuddies();
        }

        sendForm();
    }

    private void sendForm() {
        if (currentBuddy == null) {
            showAlert("Send", 
                      "Please first select a buddy to chat with",
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT,
                      initForm);
            return;
        }

        if (replyForm == null) {
            replyForm = new Form(currentBuddy);
            tfSentMsg = new TextField(null, 
                                      null, 
                                      4096,
                                      TextField.ANY);
            replyForm.append(tfSentMsg);
            Command cmdSend = new Command("Send", Command.SCREEN, 1);
            Command cmdBack = new Command("Back", Command.BACK, 2);
            replyForm.addCommand(cmdSend);
            replyForm.addCommand(cmdBack);
            replyForm.setCommandListener(this) ;
        }

        replyForm.setTitle(currentBuddy);
        display.setCurrent(replyForm);
    }

    private void editConfig() {
        if (configForm == null) {
            configForm = new Form("Configuration");
            configForm.append(tfRelayHost);
            configForm.append(tfRelayPort);
            configForm.append(tfIdentity);
            configForm.append(tfPollInterval);
            configForm.append(cgChatMode);

            Command cmdOK = new Command("OK", Command.OK, 1);
            Command cmdBack = new Command("Back", Command.BACK, 2);
            configForm.addCommand(cmdOK);
            configForm.addCommand(cmdBack);
            configForm.setCommandListener(this) ;
        }

        if (isGroupChat) {
            cgChatMode.setSelectedIndex(0, true);
        } else {
            cgChatMode.setSelectedIndex(1, true);
        }
        display.setCurrent(configForm);
    }

    private void readConfig() {
        String prop = null;

        prop = getAppProperty("RelayHost");
        tfRelayHost = new TextField("Relay host: ", 
                                    prop == null ? "192.18.37.36" : prop, 
                                    MAX_TEXTFIELD_SIZE,
                                    TextField.ANY);

        prop = getAppProperty("RelayPort");
        tfRelayPort = new TextField("Relay port: ", 
                                    prop == null ? "9700" : prop,
                                    MAX_TEXTFIELD_SIZE,
                                    TextField.NUMERIC);

        prop = getAppProperty("Identity");
        tfIdentity = new TextField("Identity: ", 
                                   prop == null ? "" : prop,
                                   MAX_TEXTFIELD_SIZE,
                                   TextField.ANY);

        prop = getAppProperty("PollInterval");
        tfPollInterval = new TextField("Poll interval: ", 
                                       prop == null ? 
                                         Integer.toString(pollInterval) : prop,
                                       MAX_TEXTFIELD_SIZE,
                                       TextField.NUMERIC);

        cgChatMode = new ChoiceGroup("Chat Mode", Choice.EXCLUSIVE,
                                     CHAT_MODE_CHOICES,
                                     null);
        prop = getAppProperty("ChatMode");
        if ("group".equals(prop)) {
            cgChatMode.setSelectedIndex(0, true);
        } else {
            cgChatMode.setSelectedIndex(1, true);
        }

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, false);
            byte[] data = rs.getRecord(CONFIG_RECORD_INDEX);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            tfRelayHost.setString(dis.readUTF());
            tfRelayPort.setString(dis.readUTF());
            tfIdentity.setString(dis.readUTF());

            tfPollInterval.setString(dis.readUTF());
            try {
                pollInterval = Integer.parseInt(tfPollInterval.getString());
            } catch (NumberFormatException ex) {
                showAlert("Read Config", 
                          "Error parsing poll interval: " + 
                          tfPollInterval.getString(),
                          AlertType.WARNING, 
                          DEFAULT_ALERT_TIMEOUT,
                          initForm);
            }

            isGroupChat = dis.readBoolean();
            if (isGroupChat) {
                cgChatMode.setSelectedIndex(0, true);
            } else {
                cgChatMode.setSelectedIndex(1, true);
            }

            int stateLen = dis.readShort();
            state = new byte[stateLen];
            dis.readFully(state);

            if (DEBUG) {
                System.out.println("Read config: host=" + 
                                   tfRelayHost.getString() +
                                   " port=" + tfRelayPort.getString() + 
                                   " identity=" + tfIdentity.getString() +
                                   " poll=" + tfPollInterval.getString() +
                                   " groupChat=" + isGroupChat +
                                   " stateLen=" + stateLen);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }
    }

    private void storeConfig() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        RecordStore rs = null;
        try {
            dos.writeUTF(tfRelayHost.getString());
            dos.writeUTF(tfRelayPort.getString());
            dos.writeUTF(tfIdentity.getString());
            dos.writeUTF(tfPollInterval.getString());

            dos.writeBoolean(isGroupChat);
            dos.writeShort(state.length);
            dos.write(state);
            dos.close();
            byte[] data = baos.toByteArray();

            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            int recordId = CONFIG_RECORD_INDEX;
            try {
                rs.getRecord(recordId);
                rs.setRecord(recordId, data, 0, baos.size());
            } catch (RecordStoreException rex) {
                recordId = rs.addRecord(data, 0, baos.size());
                // assert (recordId == CONFIG_RECORD_INDEX)
            }

            if (DEBUG) {
                System.out.println("Saved config: recordId=" + recordId + 
                                   " host=" + tfRelayHost.getString() +
                                   " port=" + tfRelayPort.getString() + 
                                   " identity=" + tfIdentity.getString() +
                                   " poll=" + tfPollInterval.getString() +
                                   " groupChat=" + isGroupChat +
                                   " stateLen=" + state.length);
            }
            try {
                pollInterval = Integer.parseInt(tfPollInterval.getString());
            } catch (NumberFormatException ex) {
                showAlert("Saved Config", 
                          "Error parsing poll interval: " + 
                          tfPollInterval.getString(),
                          AlertType.WARNING, 
                          DEFAULT_ALERT_TIMEOUT,
                          initForm);
            }

        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
                dos.close();
                baos.close();
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }
    }

    private void resetConfig() {
        if (connected) {
            disconnect();
        }

        peer = null;
        state = new byte[0];

        try {
            RecordStore.deleteRecordStore(RECORD_STORE_NAME);
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        }

	configForm = null;
	readBuddies();
	readConfig();
	currentBuddy = null;
	editConfig();
    }

    private void setupConfirmForm() {
        confirmForm = new Form("Config reset");
        Command cmdOK = new Command("OK", Command.OK, 1);
        Command cmdBack = new Command("Back", Command.BACK, 2);
	StringItem si = new StringItem(null, 
"This will restore the default configuration and buddy list, continue?");
        confirmForm.addCommand(cmdOK);
        confirmForm.addCommand(cmdBack);
        confirmForm.setCommandListener(this);
        confirmForm.append(si);
    }

    private void editBuddies() {
        display.setCurrent(buddyList);
    }

    private void readBuddies() {
        buddyList = new List("Buddy List", List.IMPLICIT);

        Command cmdChat = new Command("Chat", Command.SCREEN, 1);
        Command cmdAdd = new Command("Add", Command.SCREEN, 2);
        Command cmdEdit = new Command("Edit", Command.SCREEN, 3);
        Command cmdDelete = new Command("Delete", Command.SCREEN, 4);
        Command cmdBack = new Command("Back", Command.BACK, 5);
        buddyList.addCommand(cmdChat);
        buddyList.addCommand(cmdAdd);
        buddyList.addCommand(cmdEdit);
        buddyList.addCommand(cmdDelete);
        buddyList.addCommand(cmdBack);
        buddyList.setCommandListener(this) ;

        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, false);
            byte[] data = rs.getRecord(BUDDYLIST_RECORD_INDEX);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            int size = dis.readShort();
            if (DEBUG) {
                System.out.println("Reading buddies: count=" + size);
            }
            for (int i=0; i < size; i++) {
                String buddy = dis.readUTF();
                buddyList.append(buddy, null);

                String buddyId = dis.readUTF();
                buddyIds.put(buddy, buddyId);

                if (DEBUG) {
                    System.out.println("  Read buddy=\"" + buddy + 
                                       "\" id=\"" + buddyId + "\"");
                }
            }
            currentBuddy = dis.readUTF();
            if (DEBUG) {
                System.out.println("Read current buddy=" + currentBuddy);
            }
            initForm.setTitle(currentBuddy);
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }

        boolean ip2p = false;
        boolean picshare = false;
        int size = buddyList.size();
        for (int i=0; i < size; i++) {
            String buddy = buddyList.getString(i);
            if (INSTANTP2P_GROUPNAME.equals(buddy)) {
                ip2p = true;
            } 
            if (PICSHARE_GROUPNAME.equals(buddy)) {
                picshare = true;
            }
        }

        if (!ip2p) {
            // pre-populate the buddy list with the group name of myJXTA
            // to enable interoperability with myJXTA aka InstantP2P
            buddyList.append(INSTANTP2P_GROUPNAME, null);
        }
        if (!picshare) {
            // pre-populate the buddy list with the group name of PicShare
            buddyList.append(PICSHARE_GROUPNAME, null);
        }
    }

    private void storeBuddies() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        RecordStore rs = null;
        try {
            int size = buddyList.size();
            if (DEBUG) {
                System.out.println("Saving buddies: count=" + size);
            }
            dos.writeShort(size);
            for (int i=0; i < size; i++) {
                String buddy = buddyList.getString(i);
                dos.writeUTF(buddy);
                String buddyId = (String) buddyIds.get(buddy);
                if (buddyId == null) {
                    // not yet discovered
                    buddyId = "";
                }
                dos.writeUTF(buddyId);
                if (DEBUG) {
                    System.out.println("  Saved buddy=\"" + 
                                       buddy + "\" id=\"" + buddyId + "\"");
                }
            }

            // also store the currently selected buddy, if any
            if (currentBuddy != null) {
                dos.writeUTF(currentBuddy);
                if (DEBUG) {
                    System.out.println("Saved current buddy=\"" + 
                                       currentBuddy + "\"");
                }
            } else {
                dos.writeUTF("");
            }
            dos.close();
            byte[] data = baos.toByteArray();

            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            int recordId = BUDDYLIST_RECORD_INDEX;
            try {
                rs.getRecord(recordId);
                rs.setRecord(recordId, data, 0, baos.size());
            } catch (RecordStoreException rex) {
                recordId = rs.addRecord(data, 0, baos.size());
                // assert (recordId == BUDDYLIST_RECORD_INDEX)
            }

            if (DEBUG) {
                System.out.println("Saved buddyList: recordId=" + recordId + 
                                   " count=" + size +
                                   " dlen=" + data.length);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.closeRecordStore();
                }
                dos.close();
                baos.close();
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }
    }

    private void chatBuddy() {
        int i = buddyList.getSelectedIndex();
        String buddy = buddyList.getString(i);
        currentBuddy = buddy;
        initForm.setTitle(currentBuddy);
        display.setCurrent(initForm);
    }

    private void addBuddy() {
        int i = buddyList.getSelectedIndex();

        addBuddyForm = new Form("Add Buddy");
        tfBuddy = new TextField("New Buddy: ", 
                                "", 
                                MAX_TEXTFIELD_SIZE,
                                TextField.ANY);
        addBuddyForm.append(tfBuddy);
        Command cmdOK = new Command("OK", Command.OK, 1);
        addBuddyForm.addCommand(cmdOK);
        addBuddyForm.setCommandListener(this) ;
        display.setCurrent(addBuddyForm);
    }

    private void editBuddy() {
        int i = buddyList.getSelectedIndex();
        String buddy = buddyList.getString(i);

        editBuddyForm = new Form("Edit Buddy");
        tfBuddy = new TextField("Edit Buddy: ", 
                                buddy, 
                                MAX_TEXTFIELD_SIZE,
                                TextField.ANY);
        editBuddyForm.append(tfBuddy);
        Command cmdOK = new Command("OK", Command.OK, 1);
        editBuddyForm.addCommand(cmdOK);
        editBuddyForm.setCommandListener(this) ;
        display.setCurrent(editBuddyForm);
    }

    private void deleteBuddy() {
        int i = buddyList.getSelectedIndex();
        buddyList.delete(i);
    }

    private boolean send() {
        if (peer == null || !connected) {
            initiateConnect();
            sendPending = true;
            return false;
        }

        String msg = tfSentMsg.getString();
        Element[] elm = new Element[3];
        elm[0] = new Element("JxtaTalkSenderName", 
                             tfIdentity.getString().getBytes(), 
                             null, null);
        elm[1] = new Element("JxtaTalkSenderMessage", 
                             msg.getBytes(), 
                             null, null);
        // for compatibility with myJXTA aka InstantP2P
        elm[2] = new Element("GrpName", 
                             "NetPeerGroup".getBytes(), 
                             null, null);
        Message m = new Message(elm);
        try {
            String chatBuddy = TALKNAME_PREFIX + currentBuddy;
            String pipeId = (String) buddyIds.get(currentBuddy);
            String pipeType = isGroupChat ? "JxtaPropagate" : "JxtaUnicast";
            peer.send(pipeId, m);
        } catch (Exception ex) {
            showAlert("Send", 
                      "Error sending message: " + ex.getMessage(),
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT, 
                      initForm);
            return false;
        }

        return true;
    }

    private void initiateConnect() {
        if (peer == null) {
            peer = PeerNetwork.createInstance(tfIdentity.getString());
        }

        if (connected || connectInitiated) {
            return;
        }

        connectInitiated = true;
        // we will perform the actual operation in the poll thread
    }

    private boolean connect() throws Exception {
        connectInitiated = false;
        String host = tfRelayHost.getString();
        int port = 0;
        try {
            port = Integer.parseInt(tfRelayPort.getString());
        } catch (NumberFormatException ex) {
            showAlert("Connect", 
                      "Error parsing relay port number: " + 
                      tfRelayPort.getString(),
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT, 
                      initForm);
            return false;
        }

        String url = "http://" + host + ":" + Integer.toString(port);
        if (DEBUG) {
            System.out.println("Connecting to " + url + "...");
        }

        long startTime, endTime;
        if (QUANTIFY) {
            startTime = System.currentTimeMillis();
        }
        state = peer.connect(url, state);
        if (QUANTIFY) {
            endTime = System.currentTimeMillis();
            System.out.println("connect took " +
                               Long.toString(endTime-startTime));
        }
        connected = true;

        String chatIdentity = isGroupChat ? 
            TALKNAME_PREFIX + currentBuddy : 
            TALKNAME_PREFIX + tfIdentity.getString();
        if (PICSHARE_GROUPNAME.equals(currentBuddy)) {
            chatIdentity = TALKNAME_PREFIX + PICSHARE_GROUPNAME;
        }

        String pipeType = isGroupChat ? "JxtaPropagate" : "JxtaUnicast";

        String pipeId = null;
        if (INSTANTP2P_GROUPNAME.equals(currentBuddy)) {
            // listen on myJXTA's well-known pipe id if talking to it
            pipeId = INSTANTP2P_PIPEID;
        } else if (PICSHARE_GROUPNAME.equals(currentBuddy)) {
            // listen on PicShare's well-known pipe id if talking to it
            pipeId = PICSHARE_PIPEID;
        }

        if (DEBUG) {
            System.out.println("creating pipe" + pipeId);
        }
        int createId = peer.create(PeerNetwork.PIPE, chatIdentity, pipeId, pipeType);
        while (createId != responseId){
            poll();
            if (DEBUG) {
                System.out.println ("createId: resId: " + createId + ":" + responseId);
            }
            try {
                Thread.sleep (pollInterval *1000);
            } catch (Throwable t) {}
        }

        if (DEBUG) {
            System.out.println("Listening on " + pipeId);
        }
        peer.listen(pipeId);
        /*
          int size = buddyList.size();
          for (int i=0; i < size; i++) {
          String buddy = buddyList.getString(i);
          peer.search(PeerNetwork.PIPE, 
          "Name", TALKNAME_PREFIX + buddy, 1);
          }
        */

        display.setCurrent(initForm);
        return true;
    }

    private void disconnect() {
	String chatIdentity = isGroupChat ? 
	    TALKNAME_PREFIX + currentBuddy : 
	    TALKNAME_PREFIX + tfIdentity.getString();
	if (PICSHARE_GROUPNAME.equals(currentBuddy)) {
	    chatIdentity = TALKNAME_PREFIX + PICSHARE_GROUPNAME;
	}

	String pipeType = isGroupChat ? "JxtaPropagate" : "JxtaUnicast";

	String pipeId = null;
	if (INSTANTP2P_GROUPNAME.equals(currentBuddy)) {
	    // close myJXTA's well-known pipe id if talking to it
	    pipeId = INSTANTP2P_PIPEID;
	} else if (PICSHARE_GROUPNAME.equals(currentBuddy)) {
	    // close on PicShare's well-known pipe id if talking to it
	    pipeId = PICSHARE_PIPEID;
	}

        try {
            peer.close(pipeId);
            connected = false;
	    if (DEBUG) {
		System.out.println("Closed " + chatIdentity);
	    }
        } catch (Exception ex) {
            showAlert("Disconnect", 
                      "Error connecting to relay: " + ex.getMessage(),
                      AlertType.ERROR, 
                      Alert.FOREVER, 
                      initForm);
        }
    }

    // a class to increment the value of a Gauge everytime it is run
    static class StatusUpdate extends TimerTask {

        private StringItem elapsed = null;
        private Gauge status = null;
        private int tick = 0;
        private int max = 0;

        StatusUpdate(StringItem elapsed, Gauge status) {
            this.elapsed = elapsed;
            this.status = status;
            max = status.getMaxValue();
        }

        public void run() {
            elapsed.setText(Integer.toString(++tick) + "s");
            status.setValue(tick % max);
        }
    }

    public void run() {
        if (DEBUG) {
            System.out.println("starting poll thread");
        }

        while (!stopPolling) {
            if (!connected && connectInitiated) {
                Form connectingForm = new Form("Connecting...");
                StringItem elapsed = new StringItem("", null);
                connectingForm.append(elapsed);
                Gauge status = new Gauge(null, false, 10, 0);
                connectingForm.append(status);
                display.setCurrent(connectingForm);
                StatusUpdate updater = new StatusUpdate(elapsed, status);
                new Timer().scheduleAtFixedRate(updater, 1000, 1000);
                try {
                    connect();
                    updater.cancel();
                    display.setCurrent(initForm);
                    connectingForm = null;
                } catch (Exception ex) {
		    updater.cancel();
                    showAlert("Connect", 
                              "Error connecting to relay: " + ex.getMessage(),
                              AlertType.ERROR, 
                              Alert.FOREVER, 
                              initForm);
                }
                if (sendPending) {
                    try {
                        send();
                    } finally {
                        sendPending = false;
                    }
                }
            }

            try {
		// keep polling until we drain all queued messages
		while (poll()) {
		}
            } catch (Throwable t) {
		//t.printStackTrace();
                showAlert("Poll", 
                          "Error processing message: " + t.getMessage(),
                          AlertType.ERROR, 
                          DEFAULT_ALERT_TIMEOUT, 
                          initForm);
            }

            try {
                // poll interval is specified in seconds
                Thread.currentThread().sleep(pollInterval * 1000);
            } catch (InterruptedException ignore) {
            }
        }
        if (DEBUG) {
            System.out.println("stopped poll thread");
        }
    }

    private boolean poll() {
        if (peer == null || !connected) {
            // not yet connected
            return false;
        }
        
        Message msg = null;
        try {
            long startTime, endTime;
            if (QUANTIFY) {
                startTime = System.currentTimeMillis();
            }
            // timeout must not be zero: zero means block forever
            if (peer != null) {
                msg = peer.poll(pollInterval*1000);
            }
            if (QUANTIFY) {
                endTime = System.currentTimeMillis();
                System.out.println("poll took " +
                                   Long.toString(endTime-startTime));
            }
        } catch (IOException ex) {
            //ex.printStackTrace();
            showAlert("Poll", 
                      "Error polling relay: " + ex.getMessage(),
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT, 
                      initForm);
            return false;
        }

        if (msg == null) {
            return false;
        }

        Element el = null;
        String name = null;
        String id = null;
        for (int i=0; i < msg.getElementCount(); i++) {
            el = msg.getElement(i);
            if (Message.PROXY_NAME_SPACE.equals(el.getNameSpace())) {
		String elementName = el.getName();
                if (Message.NAME_TAG.equals(elementName)) {
                    name = new String(el.getData());
		} else if (Message.ID_TAG.equals(elementName)) {
                    id = new String(el.getData());
		}
	    }
	}	

        if (name != null) {
            if (name.indexOf(TALKNAME_PREFIX) >= 0) {
                name = name.substring(TALKNAME_PREFIX.length());
            }

            int size = buddyList.size();
            for (int i=0; i < size; i++) {
                String buddy = buddyList.getString(i);
                if (buddy.equals(name) &&
                    id != null && !"".equals(id)) {
                    buddyIds.put(name, id);
                    break;
                }
            }
        }

        String sender = null;
        String message = null;

        String imageCaption = null;
        String imageFileName = null;
        byte[] imageData = null;

        boolean isDisplayable = true;
        for (int i=0; i < msg.getElementCount(); i++) {
            el = msg.getElement(i);
            if ("requestId".equals(el.getName())) {
                responseId = Integer.parseInt(new String(el.getData()));
                System.out.println ("ResponseId: " + responseId);
            }else if ("JxtaTalkSenderName".equals(el.getName())) {
                sender = new String(el.getData());
            } else if ("JxtaTalkSenderMessage".equals(el.getName())) {
                message = new String(el.getData());
            } else if ("Caption".equals(el.getName())) {
                imageCaption = new String(el.getData());
            } else if ("FileName".equals(el.getName())) {
                imageFileName = new String(el.getData());
            } else if ("DataBlock".equals(el.getName())) {
                imageData = el.getData();
                isDisplayable = false;
            }

            if (DEBUG) {
                System.out.print(i + " " + el.getName());
                if (isDisplayable) {
                    System.out.print(" " + new String(el.getData()));
                }
                System.out.println();
            }
            isDisplayable = true;
        }

        if (imageData != null) {
            String caption = 
                imageCaption == null ? imageFileName : imageCaption;
            imageCanvas.createImage(imageData, caption, sender);
	    display.setCurrent(imageCanvas);
        }

        if (sender != null && message != null) {
            if (sender.indexOf(TALKNAME_PREFIX) >= 0) {
                sender = sender.substring(TALKNAME_PREFIX.length());
            }
            replyBuddy = sender;
            String displayedMsg = sender + "> " + message + "\n";

            // keep the last DEFAULT_SCROLL messages, the rest scroll off
            StringItem si = new StringItem(null, displayedMsg);
            if (initForm.size() >= DEFAULT_SCROLL) {
                initForm.delete(0);
            }
            initForm.append(si);

            // pop an alert for a millisecond to get a notification beep
            showAlert("", "", AlertType.INFO, 1, initForm);
        }

	return true;
    }

    private void showAlert(String title, String message, AlertType type, 
                           int timeout, Displayable back) {
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(timeout);
        display.setCurrent(alert, back);
    }

    private static class ImageCanvas extends Canvas
    {
        Image image = null;
        String caption = null;
        String sender = null;

        Chat midlet = null;
        int width = 0;
        int height = 0;
        int lineHeight = 0;

        ImageCanvas(Chat midlet) {
            this.midlet = midlet;

            width = getWidth();
            height = getHeight();
            lineHeight = Font.getDefaultFont().getHeight();
        }

        public void createImage(byte[] data, String caption, String sender) {
            this.caption = caption;
            this.sender = sender;
            try {
                image = Image.createImage(data, 0, data.length);
                repaint();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        public void paint(Graphics g) {
            // clear
            g.setColor(255, 255, 255);
            g.fillRect(0, 0, width, height);
            g.setColor(0, 0, 0);

            if (image != null) {
                int xoff = width / 2;
                int yoff = height / 2;
                g.drawImage(image, xoff, yoff, 
                            Graphics.VCENTER | Graphics.HCENTER);
            }

            if (caption != null) {
                g.drawString(caption, 0, 0, 
                             Graphics.TOP | Graphics.LEFT);
            }

            if (sender != null) {
                g.drawString(sender, 0, height, 
                             Graphics.BOTTOM | Graphics.LEFT);
            }
        }
    }
}
