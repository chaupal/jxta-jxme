/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
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
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
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
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: ChatPanel.java,v 1.17 2005/06/09 17:17:31 hamada Exp $
 *
 */

package net.jxta.j2me.demo.chat;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.TextListener;
import java.awt.event.TextEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.awt.FileDialog;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.image.ImageObserver;
import java.awt.Image;
import java.awt.Dimension;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

public class ChatPanel extends Panel implements ActionListener, 
        ItemListener, KeyListener, Runnable {

    public static final boolean DEBUG = true;
    public static final boolean WARN = true;

    public static final String TITLE_BUDDY     = "Chat";
    public static final String TITLE_SETTINGS  = "Settings";
    public static final String TITLE_ADMIN     = "Buddies";
    public static final String TITLE_ADD       = "Find";

    public static final String BUTTON_BUDDIES = "Buddies...";
    public static final String BUTTON_SETTINGS = "Settings...";
    public static final String BUTTON_REMOVE = "Remove";
    public static final String BUTTON_ADDING    = "Add...";
    public static final String BUTTON_DONE   = "Done";
    public static final String BUTTON_SEARCH   = "Search";
    public static final String BUTTON_CANCEL   = "Cancel";
    public static final String BUTTON_ADD    = "Add";
    public static final String BUTTON_SEND   = "Send";
    public static final String BUTTON_IMAGE   = "Image";
    public static final String BUTTON_SEND_IMAGE   = "Send Image";
    public static final String BUTTON_BYE    = "Bye";
    public static final String BUTTON_OK    = "Ok";

    // default colors
    public static final Color DEFAULT_PANEL_FG = null; // Color.blue;
    public static final Color DEFAULT_PANEL_BG = null; // Color.green;
    public static final Color DEFAULT_TEXTCOMPONENT_BG = null; // Color.red;
    public static final Color DEFAULT_BUTTON_BG = null; // Color.orange;

    static final String[] CANNED_PHRASES = { "Hello!", "Goodbye!", 
                                             "I'm Home.", "I'm at Work.", 
                                             "Where are you?", "What time?", 
                                             "Where?", "------------", 
                                             "Customize...", "Send picture..." };
    static final int NUMBER_PHRASES = 7;

    public static final String DEFAULT_RELAY = "http://192.18.37.36:9700";

    public static final String CHAT_USERNAME_PREFIX = "JxtaTalkUserName.";

    public static final String PIPE_TYPE_UNICAST = "JxtaUnicast";
    public static final String PIPE_TYPE_PROPAGATE = "JxtaPropagate";

    static final String ID_PREFIX       = "urn:jxta:uuid-";
    static final String WORLD_GROUP_ID  = "59616261646162614A78746150325033";
    static final String NET_GROUP_ID    = "59616261646162614E50472050325033";

    static final String GROUP_ID_SUFFIX = "02";
    static final String PEER_ID_SUFFIX  = "03";
    static final String PIPE_ID_SUFFIX  = "04";

    static final int UUID_START_INDEX = 37;
    static final int UUID_END_INDEX = UUID_START_INDEX + 32;

    static final String IP2PGRP_NAME = "IP2PGRP";
    static final String IP2PGRP_ID = ID_PREFIX + NET_GROUP_ID + 
                                     //"D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1" + 
                                     "D1D1D1D1D1D141D191D191D1D1D1D1D1" + 
                                     PIPE_ID_SUFFIX;
    static final String IP2PGRP_TYPE = PIPE_TYPE_PROPAGATE;
    static final String IP2PGRP_GROUP = "NetPeerGroup";
    static final Destination IP2PGRP = new Destination(IP2PGRP_NAME, 
                                                       IP2PGRP_ID, 
                                                       IP2PGRP_TYPE, 
                                                       IP2PGRP_GROUP);

    static final String PICSHAREGRP_NAME = "PicShare";
    static final String PICSHAREGRP_ID = ID_PREFIX + NET_GROUP_ID + 
                                         //"50696353686172652D5B46696C654361" +
                                         "5069635368614265AD5B86696C654361" +
                                         PIPE_ID_SUFFIX;
    static final String PICSHAREGRP_TYPE = PIPE_TYPE_PROPAGATE;
    static final String PICSHAREGRP_GROUP = "NetPeerGroup";
    static final Destination PICSHAREGRP = new Destination(PICSHAREGRP_NAME, 
                                                           PICSHAREGRP_ID, 
                                                           PICSHAREGRP_TYPE, 
                                                           PICSHAREGRP_GROUP);
    // Message element names.
    final static String MESSAGE_TYPE = "MessageType";         // See list of types below.
    final static String SENDER_NAME  = "JxtaTalkSenderName";  // The sending peer name.
    final static String SENDER_ID    = "SenderID";  // The sending peer name.

    final static String FILE_KEY     = "FileKey";      // Unique key for this file transaction.
    final static String FILE_NAME    = "FileName";     // File name (no path).
    final static String FILE_SIZE    = "FileSize";     // File size.
    final static String BLOCK_NUM    = "BlockNum";     // Large files are sent in blocks.
    final static String TOTAL_BLOCKS = "TotalBlocks";  // Total number of blocks in the file.
    final static String BLOCK_SIZE   = "BlockSize";    // The size of one block.
    final static String DATA_BLOCK   = "DataBlock";    // One block of file data.

    static  FileDialog openDlg;
    private PeerNetwork peer;
    private Destination user;
    private String peerId = "";
    private Thread thread = null;
    private Destination currentBuddy = null;
    private Vector buddyList = new Vector();
    private Vector resultList = new Vector();
    private boolean isMessengerPanel = false;

    // The Chat Widget Panels
    private BuddyPanel buddyPanel;
    private BuddyAdminPanel buddyAdminPanel;
    private BuddyAddPanel buddyAddPanel;
    private MessengerPanel messengerPanel;
    private SettingsPanel settingsPanel;
    private ImageViewPanel imagePanel;

    public ChatPanel() {
        buddyPanel = new BuddyPanel();
        buddyAdminPanel = new BuddyAdminPanel();
        buddyAddPanel = new BuddyAddPanel();
        messengerPanel = new MessengerPanel();
        settingsPanel = new SettingsPanel();
        imagePanel = new ImageViewPanel();

        setRelay(DEFAULT_RELAY);

        user = new Destination("", "", PIPE_TYPE_UNICAST, "");

        // set the color
        if (DEFAULT_PANEL_FG != null) {
            setForeground(DEFAULT_PANEL_FG);
        }

        if (DEFAULT_PANEL_BG != null) {
            setBackground(DEFAULT_PANEL_BG);
        }

        setLayout(new BorderLayout());

        buddyPanel.buddyList.addItemListener(this);
        buddyPanel.buddiesButton.addActionListener(this);
        buddyPanel.settingsButton.addActionListener(this);

        messengerPanel.sendButton.addActionListener(this);
        messengerPanel.imageSendButton.addActionListener(this);
        messengerPanel.imageButton.addActionListener(this);
        messengerPanel.byeButton.addActionListener(this);
        messengerPanel.inputField.addKeyListener(this);

        buddyAdminPanel.removeButton.addActionListener(this);
        buddyAdminPanel.addButton.addActionListener(this);
        buddyAdminPanel.doneButton.addActionListener(this);

        buddyAddPanel.searchButton.addActionListener(this);
        buddyAddPanel.cancelButton.addActionListener(this);
        buddyAddPanel.addButton.addActionListener(this);

        settingsPanel.doneButton.addActionListener(this);
        settingsPanel.doneButton.setEnabled(false);

        imagePanel.okButton.addActionListener(this);

        addBuddy(IP2PGRP);
        addBuddy(PICSHAREGRP);

        selectPanel(settingsPanel, TITLE_SETTINGS);
    }

    public ChatPanel(String relay, String peerId, 
                     Destination user, Vector buddies) {
        this();

        setUser(user);
        setRelay(relay);
        setPeerId(peerId);

        for (Enumeration e = buddies.elements(); e.hasMoreElements(); ) {
            addBuddy((Destination)e.nextElement());
        }

        if (user.name.length() == 0) {
            selectPanel(settingsPanel, TITLE_SETTINGS);
        } else {
            settingsPanel.doneButton.setEnabled(true);
            selectPanel(buddyPanel, TITLE_BUDDY);

            connect();
        }
    }
    public void actionPerformed(ActionEvent evt) {
        if (DEBUG) {
            System.out.println("actionPerformed " + evt);
        }

        // actions from the Buddy Panel
        if (evt.getSource() == buddyPanel.buddiesButton) {
            selectPanel(buddyAdminPanel, TITLE_ADMIN);
        } else if (evt.getSource() == buddyPanel.settingsButton) {
            selectPanel(settingsPanel, TITLE_SETTINGS);

        // actions from the Messenger Panel
        } else if (evt.getSource() == messengerPanel.sendButton) {
            sendMessage();
        } else if (evt.getSource() == messengerPanel.imageSendButton) {
            openDlg.show();
            if (openDlg.getFile() != null){
                try {
                    sendImage (new File(openDlg.getDirectory(), openDlg.getFile()));
                } catch (Exception e ){
                    if (DEBUG) {
                        System.out.println ("Couldn't Send Image");
                    }
                }
            }
        } else if (evt.getSource() == messengerPanel.byeButton) {
            if (PIPE_TYPE_PROPAGATE.equals(currentBuddy.type)) {
                try {
                    // start listening on group pipe
                    peer.close(currentBuddy.id);

                    thread.interrupt();
                } catch (Exception e) {
                    if (DEBUG) {
                        System.out.println("status: could not close group pipe");
                    }
                }
            }

            messengerPanel.logArea.setText("");
            messengerPanel.inputField.setText("");

            selectPanel(buddyPanel, TITLE_BUDDY);

        // actions from the Buddy Admin Panel
        } else if (evt.getSource() == buddyAdminPanel.removeButton) {
            int[] indexes = buddyAdminPanel.buddyList.getSelectedIndexes();

            for (int i = indexes.length-1; i >= 0; i--) {
                if (DEBUG) {
                    System.out.println("remove buddy at index " + indexes[i]);
                }
                buddyPanel.buddyList.remove(indexes[i]);
                buddyAdminPanel.buddyList.remove(indexes[i]);
                buddyList.removeElementAt(indexes[i]);
            }
        } else if (evt.getSource() == buddyAdminPanel.addButton) {
            selectPanel(buddyAddPanel, TITLE_ADD);
        } else if (evt.getSource() == buddyAdminPanel.doneButton) {
            selectPanel(buddyPanel, TITLE_BUDDY);

        // actions from the Buddy Add Panel
        } else if (evt.getSource() == buddyAddPanel.searchButton) {
            search();
        } else if (evt.getSource() == buddyAddPanel.cancelButton) {
            selectPanel(buddyAdminPanel, TITLE_ADMIN);
        } else if (evt.getSource() == buddyAddPanel.addButton) {
            int[] indexes = buddyAddPanel.resultList.getSelectedIndexes();

            for (int i = 0; i < indexes.length; i++) {
                addBuddy((Destination)resultList.elementAt(indexes[i]));
            }

            selectPanel(buddyAdminPanel, TITLE_ADMIN);

        // actions from the Settings Panel
        } else if (evt.getSource() == settingsPanel.doneButton) {
            user.name = settingsPanel.userField.getText();
            setUser(user);

            setRelay(settingsPanel.relayField.getText());

            selectPanel(buddyPanel, TITLE_BUDDY);

            connect();

        } else if (evt.getSource() == imagePanel.okButton) {
            selectPanel(messengerPanel, "Chat group " + currentBuddy.name);
        } else if (evt.getSource() == messengerPanel.imageButton) {
            selectPanel(imagePanel, "Picshare with " + currentBuddy.name);
        }
    }

    public void itemStateChanged(ItemEvent evt) {
        if (DEBUG) {
            System.out.println("itemStateChanged " + evt);
        }

        if (evt.getSource() == buddyPanel.buddyList) {
            currentBuddy = getBuddy(buddyPanel.buddyList.getSelectedIndex());

            if (DEBUG) {
                System.out.println("currentBuddy name=" + currentBuddy.name + 
                                   " id=" + currentBuddy.id + 
                                   " type=" + currentBuddy.type + 
                                   " group=" + currentBuddy.group);
            }

            if (PIPE_TYPE_PROPAGATE.equals(currentBuddy.type)) {
                try {
                    peer.create(PeerNetwork.PIPE, 
                                CHAT_USERNAME_PREFIX+currentBuddy.name, 
                                currentBuddy.id, 
                                currentBuddy.type);
                    // start listening on group pipe
                    peer.listen(currentBuddy.id);

                    selectPanel(messengerPanel, "Chat group " + currentBuddy.name);
                } catch (Exception e) {
                    if (WARN) {
                        System.err.println("status: could not listen to group pipe");
                    }
                }
            } else {
                selectPanel(messengerPanel, "Chat with " + currentBuddy.name);
            }

            int index = buddyPanel.buddyList.getSelectedIndex();
            if (index != -1) {
                buddyPanel.buddyList.deselect(index);
            }

            // make sure we are listen to our user pipe
            try {
                // start listening on user pipe
                peer.create(PeerNetwork.PIPE, 
                            CHAT_USERNAME_PREFIX+user.name, 
                            user.id, 
                            user.type);
                peer.listen(user.id);

                thread.interrupt();
            } catch (Exception e) {
                if (WARN) {
                    System.err.println("status: could not listen to user pipe");
                }
            }

        }
    }

    public void keyTyped(KeyEvent evt) {}
    public void keyReleased(KeyEvent evt) {}
    public void keyPressed(KeyEvent evt) {
        if (evt.getSource() == messengerPanel.inputField && 
            evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMessage();
        }
    }

    private void selectPanel(Panel selectedPanel, String title) {
        Container parent = getParent();

        isMessengerPanel = selectedPanel == messengerPanel;

        if (DEBUG) {
            System.out.println("parent = " + parent);
        }
        if (parent instanceof Frame) {
            if (DEBUG) {
                System.out.println("parent is a frame");
            }
            ((Frame)parent).setTitle(title);
        }

        removeAll();

        add(selectedPanel, BorderLayout.CENTER);

        if (getParent() != null) {
            getParent().validate();
        }
    }

    static class BuddyPanel extends Panel {
        List buddyList = new List();
        Button buddiesButton = new Button(BUTTON_BUDDIES);
        Button settingsButton = new Button(BUTTON_SETTINGS);

        BuddyPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(buddyList, c);

            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(buddiesButton, c);

            c.gridx = 1;
            gridbag.setConstraints(settingsButton, c);

            add(buddyList);
            add(buddiesButton);
            add(settingsButton);

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                buddyList.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                buddiesButton.setBackground(DEFAULT_BUTTON_BG);
                settingsButton.setBackground(DEFAULT_BUTTON_BG);
            }
        }
    }

    static class BuddyAdminPanel extends Panel {
        List buddyList = new List(2, true);
        Button removeButton = new Button(BUTTON_REMOVE);
        Button addButton = new Button(BUTTON_ADDING);
        Button doneButton = new Button(BUTTON_DONE);

        BuddyAdminPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 3;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(buddyList, c);

            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(removeButton, c);

            c.gridx = 1;
            gridbag.setConstraints(addButton, c);

            c.gridx = 2;
            gridbag.setConstraints(doneButton, c);

            add(buddyList);
            add(removeButton);
            add(addButton);
            add(doneButton);

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                buddyList.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                removeButton.setBackground(DEFAULT_BUTTON_BG);
                addButton.setBackground(DEFAULT_BUTTON_BG);
                doneButton.setBackground(DEFAULT_BUTTON_BG);
            }
        }
    }

    static class BuddyAddPanel extends Panel {
        TextField queryField = new TextField();
        Button searchButton = new Button(BUTTON_SEARCH);
        List resultList = new List(2, true);
        Button cancelButton = new Button(BUTTON_CANCEL);
        Button addButton = new Button(BUTTON_ADD);

        BuddyAddPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(queryField, c);

            c.gridwidth = 2;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(searchButton, c);

            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(resultList, c);

            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(cancelButton, c);

            c.gridx = 1;
            gridbag.setConstraints(addButton, c);

            add(queryField);
            add(searchButton);
            add(resultList);
            add(cancelButton);
            add(addButton);

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                queryField.setBackground(DEFAULT_TEXTCOMPONENT_BG);
                resultList.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                searchButton.setBackground(DEFAULT_BUTTON_BG);
                cancelButton.setBackground(DEFAULT_BUTTON_BG);
                addButton.setBackground(DEFAULT_BUTTON_BG);
            }
        }
    }

    static class MessengerPanel extends Panel implements ItemListener {
        Choice textChoice = new Choice();
        TextArea logArea = new TextArea("", 4, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
        TextField inputField = new TextField("", 10);
        Button sendButton = new Button(BUTTON_SEND);
        Button imageButton = new Button(BUTTON_IMAGE);
        Button imageSendButton = new Button(BUTTON_SEND_IMAGE);
        Button byeButton = new Button(BUTTON_BYE);

        MessengerPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 4;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(logArea, c);

            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(textChoice, c);
            gridbag.setConstraints(inputField, c);

            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(sendButton, c);

            c.gridx = 1;
            gridbag.setConstraints(imageSendButton, c);

            c.gridx = 2;
            gridbag.setConstraints(imageButton, c);

            c.gridx = 3;
            gridbag.setConstraints(byeButton, c);

            add(logArea);
            add(textChoice);
            add(inputField);
            add(sendButton);
            add(imageSendButton);
            add(imageButton);
            add(byeButton);

            logArea.setEditable(false);
            textChoice.addItemListener(this);

            for (int i = 0; i < CANNED_PHRASES.length; i++) {
                textChoice.add(CANNED_PHRASES[i]);
            }

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                textChoice.setBackground(DEFAULT_TEXTCOMPONENT_BG);
                logArea.setBackground(DEFAULT_TEXTCOMPONENT_BG);
                inputField.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                sendButton.setBackground(DEFAULT_BUTTON_BG);
                imageButton.setBackground(DEFAULT_BUTTON_BG);
                byeButton.setBackground(DEFAULT_BUTTON_BG);
            }

           // Create a file open dialog, for selecting images to send.
           openDlg = new FileDialog(new Frame(), "Select an image to send", FileDialog.LOAD);
           openDlg.setDirectory(".");
           //openDlg.setFilenameFilter(new ImageFilenameFilter());
            
        }

        public void itemStateChanged(ItemEvent evt) {
            if (DEBUG) {
                System.out.println("itemStateChanged " + evt);
            }

            if (evt.getSource() == textChoice) {
                int index = textChoice.getSelectedIndex();

                if (index < NUMBER_PHRASES) {
                    String text = inputField.getText();
                    int start = inputField.getSelectionStart();
                    int end = inputField.getSelectionEnd();

                    if (DEBUG) {
                        System.out.println("start=" + start + " end=" + end);
                        System.out.println("start=" + text.substring(0, start) + 
                                           " end=" + text.substring(end));
                    }

                    inputField.setText(text.substring(0, start) + 
                                       textChoice.getSelectedItem() +
                                       text.substring(end));
                }
            }
        }
    }

    static class SettingsPanel extends Panel implements TextListener {
        TextField userField = new TextField();
        Label userLabel = new Label("username");
        TextField relayField = new TextField();
        Label relayLabel = new Label("relay");
        Button doneButton = new Button(BUTTON_DONE);

        SettingsPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 1;
            gridbag.setConstraints(userLabel, c);

            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(userField, c);

            c.gridx = 0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(relayLabel, c);

            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(relayField, c);

            c.gridwidth = 1;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(doneButton, c);

            add(userField);
            add(userLabel);
            add(relayField);
            add(relayLabel);
            add(doneButton);

            userField.addTextListener(this);

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                userField.setBackground(DEFAULT_TEXTCOMPONENT_BG);
                relayField.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                doneButton.setBackground(DEFAULT_BUTTON_BG);
            }
        }

        public void textValueChanged(TextEvent evt) {
            if (userField.getText().length() > 0) {
                if (!doneButton.isEnabled()) {
                    doneButton.setEnabled(true);
                }
            } else {
                if (doneButton.isEnabled()) {
                    doneButton.setEnabled(false);
                }
            }
        }
    }

    static class ImageViewPanel extends Panel {
        ImagePanel imagePanel = new ImagePanel(this);
        Label imageLabel = new Label();
        Button okButton = new Button(BUTTON_OK);

        ImageViewPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);

            c.gridx = 0;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridbag.setConstraints(imageLabel, c);

            c.gridy = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(imagePanel, c);

            c.gridy = 2;
            c.gridwidth = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(okButton, c);

            add(imageLabel);
            add(imagePanel);
            add(okButton);

            if (DEFAULT_TEXTCOMPONENT_BG != null) {
                imageLabel.setBackground(DEFAULT_TEXTCOMPONENT_BG);
                imagePanel.setBackground(DEFAULT_TEXTCOMPONENT_BG);
            }

            if (DEFAULT_BUTTON_BG != null) {
                okButton.setBackground(DEFAULT_BUTTON_BG);
            }
        }

        public void setImage(byte[] data, String name) {
            if (data != null) {
                imagePanel.setImage(data);
                imageLabel.setText(name);
            }
        }
    }

    static class ImagePanel extends Panel implements ImageObserver {

        Panel parent = null;
        Image image = null;
        int width = 0;
        int height = 0;

        public ImagePanel(Panel parent) {
            this.parent = parent;
            setBackground (Color.white);
        }

        public void setImage(byte[] data) {
            image = java.awt.Toolkit.getDefaultToolkit().createImage(data);
            width = 0;
            height = 0;
            if (parent != null) {
		parent.repaint();
            }

            repaint();

        }

        public void paint(java.awt.Graphics g) {
            if (image != null) {
                Dimension d = getSize();

                g.drawImage(image, 
                            (d.width - width)/2, 
                            (d.height - height)/2, 
                            this);
            }else if (DEBUG) {
                System.out.println ("image is null....");
            }

        }
	
        public boolean imageUpdate(java.awt.Image img,
                                   int infoflags,
                                   int x,
                                   int y,
                                   int width,
                                   int height) {

	    if ((infoflags & ImageObserver.ALLBITS)!=0) {
                 this.width = width;
                 this.height = height;
	    
                 if (parent != null) {
		     parent.repaint();
                 }
                 repaint();
	    
	    }
            return true;
        }
	
    }

    public int getBuddyCount() {
        return buddyList.size();
    }

    public Destination getBuddy(int index) {
        return (Destination)buddyList.elementAt(index);
    }

    protected void addBuddy(Destination buddy) {
        if (!"".equals(buddy.name) && !buddyList.contains(buddy)) {
            buddyList.addElement(buddy);
            buddyPanel.buddyList.add(buddy.name);
            buddyAdminPanel.buddyList.add(buddy.name);
        }
    }

    public Destination getUser() {
        if (user == null) {
            return new Destination(null, null, PIPE_TYPE_UNICAST, "");
        } else {
            return user;
        }
    }

    protected void setUser(Destination user) {
        this.user = user;
        settingsPanel.userField.setText(user.name);
        if (user.name.length() > 0) {
            settingsPanel.doneButton.setEnabled(true);
        } else {
            settingsPanel.doneButton.setEnabled(false);
        }

        addBuddy(user);
    }

    public String getRelay() {
        return settingsPanel.relayField.getText();
    }

    protected void setRelay(String relay) {
        settingsPanel.relayField.setText(relay);
    }

    public String getPeerId() {
        if (peerId == null) {
            return "";
        } else {
            return peerId;
        }
    }

    protected void setPeerId(String peerId) {
        this.peerId = peerId;

        if (peerId.length() >= UUID_END_INDEX) {
            user.id = ID_PREFIX + NET_GROUP_ID + 
                      peerId.substring(UUID_START_INDEX, 
                                       UUID_END_INDEX) + 
                      PIPE_ID_SUFFIX;

            if (DEBUG) {
                System.out.println("user.id = " + user.id);
            }
        }
    }

    public void run() {

        String peerId = getPeerId();
        byte[] state = null;
        if (peerId != null) {
            state = peerId.getBytes();
        }

        try {
            // connect to the relay
            state = peer.connect(getRelay(), state);
        } catch (Exception e) {
            disconnect();
            if (WARN) {
                System.err.println("status: could not connect");
            }
            return ;
        }

        setPeerId(new String(state));

        if (DEBUG) System.out.println("status: connected");

        try {
            // start listening on user pipe
            peer.create(PeerNetwork.PIPE, 
                        CHAT_USERNAME_PREFIX+user.name, 
                        user.id, 
                        user.type);
            peer.listen(user.id);
        } catch (Exception e) {
            if (DEBUG) System.out.println("status: could not listen to user pipe");
        }

        if (getParent() != null) {
            getParent().validate();
        }

        while (peer != null) {
            Message m = null;
            try {
                m = peer.poll(1000);
            } catch(IOException e) {
                if (WARN) {
                    System.err.println("IOException in PeerNetwork.poll");
                }
            }

            if (m == null) {
                if (DEBUG) {
                    System.out.println("sleep for 5 seconds");
                }
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                    if (WARN) {
                        System.err.println("InterruptedException in polling thread");
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println("handle message");
                }

                handleMessage(m);
            }
        }

        thread = null;
    }
    
    private void sendImage (File file) throws IOException {
        if (DEBUG) {
            System.out.println ("sending image");
        }

        int blocksSent = 0;

        // Allocate space to store the file data.
        byte [] imageData = new byte[(int)file.length()];

        // Read the file into memory.
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(imageData, 0, imageData.length);
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String keyStr = getPeerId() + "+" + file.getName() + "+" + 
                        String.valueOf(System.currentTimeMillis());

        Element [] elm = new Element[11];
        elm[0] = new Element(SENDER_NAME,
                             user.name.getBytes(), null, null);
        elm[1] = new Element("GrpName", 
                             currentBuddy.group.getBytes(), null, null);
        elm[2] = new Element(MESSAGE_TYPE, 
                             "FILE".getBytes(), null, null);
        elm[3] = new Element(FILE_NAME, 
                             file.getName().getBytes(), null, null);
        elm[4] = new Element(FILE_SIZE, 
                             String.valueOf(imageData.length).getBytes(), null, null);
        elm[5] = new Element(FILE_KEY, 
                             keyStr.getBytes(), null, null);
        elm[6] = new Element(BLOCK_SIZE, 
                             String.valueOf(2000).getBytes(), null, null);
        elm[7] = new Element(DATA_BLOCK, 
                             imageData, null, null);
        elm[8] = new Element(BLOCK_NUM, 
                             String.valueOf(0).getBytes(), null, null);
        elm[9] = new Element(TOTAL_BLOCKS, 
                             String.valueOf(1).getBytes(), null, null);
        elm[10] = new Element(SENDER_ID, 
                             getPeerId().getBytes(), null, null);

        Message m = new Message(elm);

        peer.send(currentBuddy.id, m);

        thread.interrupt();
    }

    private void saveImageToFile (byte[] image, String fileName) {
        if (DEBUG) {
            System.out.println ("Saving to file: " + fileName);
        }
        if (image == null || fileName == null) {
            //System.out.println (fileName + " not saved ****");
            //if (image == null) {
                //System.out.println ("Image is NULL");
                //Thread.dumpStack();
	    //}
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream (fileName);
            BufferedOutputStream bos = new BufferedOutputStream (fos);

            bos.write (image, 0, image.length);
            bos.flush();
            fos.close();

            if (DEBUG) {
                System.out.println ("Saved to file: " + fileName);
            }
        } catch (Exception e) {
	    e.printStackTrace();
            return;
        }
    }

    private String picShareKey = null;
    private Object[] picShareDataBlocks = null;

    private void handleMessage(Message m) {
        String sender = null;
        String message = null;

        String pipeName = null;
        String pipeId = null;
        String pipeType = null;

        String fileKey = null;
        String fileName = null;
        int totalBlocks = 0;
        int blockSize = 0;
        int blockNum = 0;
        int fileSize = 0;
        byte[] dataBlock = null;
        boolean isDisplayable = true;

        for (int i = 0; i < m.getElementCount(); i++) {
            Element element = m.getElement(i);
            String fullName = element.getNameSpace() + ":" + element.getName();

            if (":MessageType".equals(fullName)){
                String msgType = new String (element.getData());
                
                // ACK message has no other data, so just return
                if ("FILE_ACK".equals(msgType)) {
                    System.out.println ("Received ACK");
                    return;
                }
            }

            if (":JxtaTalkSenderName".equals(fullName)) {
                sender = new String(element.getData());;
            } else if (":JxtaTalkSenderMessage".equals(fullName)) {
                message = new String(element.getData());;
            } else if ("proxy:name".equals(fullName)) {
                String dataStr = new String(element.getData());
                if (dataStr.startsWith(CHAT_USERNAME_PREFIX)) {
                    pipeName = dataStr.substring(CHAT_USERNAME_PREFIX.length());
                } else {
                    pipeName = new String(element.getData());;
                }
            } else if ("proxy:id".equals(fullName)) {
                pipeId = new String(element.getData());;
            } else if ("proxy:arg".equals(fullName)) {
                pipeType = new String(element.getData());;

            // PicShare block
            } else if (":TotalBlocks".equals(fullName)) {
                try {
                    totalBlocks = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if (":BlockSize".equals(fullName)) {
                try {
                    blockSize = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if (":BlockNum".equals(fullName)) {
                try {
                    blockNum = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if (":FileSize".equals(fullName)) {
                try {
                    fileSize = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if (":FileName".equals(fullName)) {
                fileName = new String(element.getData());
            } else if (":FileKey".equals(fullName)) {
                fileKey = new String(element.getData());
            } else if (":DataBlock".equals(fullName)) {
                dataBlock = element.getData();
                isDisplayable = false;
            }
            if (DEBUG) {
                if (isDisplayable) {
                    String dataStr = new String(element.getData());
                    System.out.println("["+i+"] "+ fullName + " = " + dataStr);
                }
            }
            isDisplayable = true;
        }

        if (sender != null && message != null) {
            log(sender + "> " + message);
        } else if (pipeName != null && pipeId != null && pipeType != null) {
            Destination newBuddy = new Destination(pipeName, pipeId, pipeType, "");
            if (! resultList.contains(newBuddy)) {
                buddyAddPanel.resultList.add(newBuddy.name);
                resultList.addElement(newBuddy);
            }
        } else if (fileKey != null) {
	    if (DEBUG) {
		System.out.println ("Getting in file handling");
            }

            // Is start of a new Image?
            if (! fileKey.equals(picShareKey) || picShareDataBlocks == null) {
                picShareKey = fileKey;
                picShareDataBlocks = new Object[totalBlocks];
            }

            // save this block of data
            if (blockNum < picShareDataBlocks.length) {
                picShareDataBlocks[blockNum] = dataBlock;
            }

            if (picShareDataBlocks.length == 1) {
                // single block image
                imagePanel.setImage(dataBlock, fileName);
                log("[ image: " + fileName + " ]");

                if (isMessengerPanel) {
                    selectPanel(imagePanel, "Picshare with " + currentBuddy.name);
                }
                saveImageToFile (dataBlock, fileName);
            } else {
                // check to see if we have all of the blocks
                int i = 0;
                for (; i < picShareDataBlocks.length && picShareDataBlocks[i] != null; i++) {
                }

                if (i == picShareDataBlocks.length) {
                    // all of the blocks are available, copy into one block
                    byte[] bigBlock = new byte[fileSize];
                    int idx = 0;

                    for (i = 0; i < picShareDataBlocks.length; i++) {
                        byte[] block = (byte[])picShareDataBlocks[i];
                        for (int j = 0; j < block.length; j++) {
                            bigBlock[idx++] = block[j];
                        }
                    }

                    imagePanel.setImage(bigBlock, fileName);
                    log("[ image: " + fileName + " ]");

                    if (isMessengerPanel) {
                        selectPanel(imagePanel, "Picshare with " + currentBuddy.name);
                    }
                    saveImageToFile (bigBlock, fileName);
                }
            }
        }
    }

    private synchronized void connect() {

        if (peer != null) {
            peer = null;
        }

        if (thread != null) {
            if (DEBUG) {
                System.out.println("status: already connected, disconnecting");
            }
            thread.interrupt();
            try {
                thread.join();
            } catch (Exception e) {
                if (WARN) {
                    System.err.println("status: InterruptedException in join()");
                }
            }
            if (DEBUG) {
                System.out.println("status: disconnected");
            }
        }

        if (DEBUG) {
            System.out.println("status: connecting");
        }

        peer = PeerNetwork.createInstance(user.name);

        // start the polling thread
        thread = new Thread(this);
        thread.start();
    }

    private void cancel() {
        disconnect();
    }

    private synchronized void disconnect() {
        peer = null;

        if (DEBUG) {
            System.out.println("status: disconnected");
        }

        if (thread != null) {
            thread.interrupt();
        }
    }

    private void search() {
        String query = buddyAddPanel.queryField.getText();

        if (query == null || "".equals(query)) {
            query = "*";
        }
        query = CHAT_USERNAME_PREFIX + query;

        try {
            if (DEBUG) {
                System.out.println("start search for " + query);
            }

            buddyAddPanel.resultList.removeAll();
            resultList.removeAllElements();

            peer.search(PeerNetwork.PIPE, "Name", query, 1);

            thread.interrupt();
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println("Could not start search");
            }
        }
    }

    private void sendMessage() {
        String message = messengerPanel.inputField.getText();

        // send the message to the buddy
        try {
            send(message);
            if (! PIPE_TYPE_PROPAGATE.equals(currentBuddy.type)) {
                log(currentBuddy.name + "> " + message);
            }
            messengerPanel.inputField.setText("");
        } catch (IOException e) {
            log("COULD NOT SEND MESSAGE");
        }
    }

    private void send(String message) throws IOException {
        Element[] elm = new Element[3];
        elm[0] = new Element("JxtaTalkSenderName", 
                             user.name.getBytes(), null, null);
        elm[1] = new Element("JxtaTalkSenderMessage", 
                             message.getBytes(), null, null);
        elm[2] = new Element("GrpName", 
                             currentBuddy.group.getBytes(), null, null);

        Message m = new Message(elm);

        peer.send(currentBuddy.id, m);

        thread.interrupt();
    }

    private synchronized void log(String message) {
        messengerPanel.logArea.appendText(message + "\n");
    }

    static class Destination {
        String name;
        String id;
        String type;
        String group;

        Destination(String name, String id, String type, String group) {
            if (name == null) {
                this.name = "";
            } else {
                this.name = name;
            }

            if (id == null) {
                this.id = "";
            } else {
                this.id = id;
            }

            if (type == null) {
                this.type = "";
            } else {
                this.type = type;
            }

            if (group == null) {
                this.group = "";
            } else {
                this.group = group;
            }
        }

        void write(DataOutputStream out) throws IOException {
            out.writeUTF(name);
            out.writeUTF(id);
            out.writeUTF(type);
            out.writeUTF(group);
        }

        public boolean equals(Object obj) {
            if (DEBUG) {
                System.out.println("Destination.equals()");
            }

            if (obj instanceof Destination) {
                Destination dest = (Destination)obj;
                if (name.equals(dest.name) && 
                    id.equals(dest.id) && 
                    type.equals(dest.type) && 
                    group.equals(dest.group)) {
                        return true;
                    }
            }

            return false;
        }

        static Destination read(DataInputStream in) throws IOException {
            String name = in.readUTF();
            String id = in.readUTF();
            String type = in.readUTF();
            String group = in.readUTF();

            return new Destination(name, id, type, group);
        }
    }
}
