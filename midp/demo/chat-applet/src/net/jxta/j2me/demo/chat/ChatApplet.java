package net.jxta.j2me.demo.chat;

import java.awt.Toolkit;
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
import java.io.IOException;
import java.awt.Dimension;
import java.util.Vector;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

import java.applet.Applet;

public class ChatApplet extends Applet implements 
       ActionListener, ItemListener, Runnable {

    public static final boolean DEBUG = true;
    public static final boolean WARN = true;

    public static final int CURRENT_DATA_VERSION = 0;

    public static final String BUTTON_SEND   = "Send";

    static final String[] CANNED_PHRASES = { "Hello!", "Goodbye!", 
                                             "I'm Home.", "I'm at Work.", 
                                             "Where are you?", "What time?", 
                                             "Where?", "------------", 
                                             "Customize...", "Send picture..." };
    static int numberPhrases;

    public static final String CHAT_USERNAME_PREFIX = "JxtaTalkUserName.";

    public static final String PIPE_TYPE_PROPAGATE = "JxtaPropagate";

    static final String ID_PREFIX       = "urn:jxta:uuid-";
    static final String NET_GROUP_ID    = "59616261646162614E50472050325033";

    static final String PIPE_ID_SUFFIX  = "04";

    static final String IP2PGRP_NAME = "IP2PGRP";
    static final String IP2PGRP_ID = "D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1";

    // default parameters
    private String relay   = "http://192.18.37.36:9700";
    private String bgColor = "white";
    private String fgColor = "black";
    private String groupName  = IP2PGRP_NAME; // IP2PGRP same as myJXTA
    private String groupID = IP2PGRP_ID;
    private int    sleepTime = 5000;   // 5 secs

    private String completeGroupID = ID_PREFIX + NET_GROUP_ID + IP2PGRP_ID + 
                                     PIPE_ID_SUFFIX;

    private PeerNetwork peer;
    private String userName;
    private String peerId = "";
    private Thread thread = null;
    private boolean isMessengerPanel = false;

    // The Chat Widget Panels
    private Panel messengerPanel;
    private List  buddyList;

    private Vector listVector = new Vector();

    private TextField userField;

    private boolean connected = false;
    private boolean connectInitiated = false;

    public ChatApplet () {
        numberPhrases = CANNED_PHRASES.length;
    }

    public void init () {
    String string;

        string = getParameter ("bgcolor");
        if (string != null && !string.equals ("")) {
	    setBackgroundColor(string);
        }

        string = getParameter ("fgcolor");
        if (string != null && !string.equals ("")) {
            setForegroundColor (string);
        }

        string = getParameter ("relay");
        if (string != null && !string.equals ("")) {
	    setRelayURL (string);
        }

        string = getParameter ("group");
        if (string != null && !string.equals ("")) {
            setGroupName(string);
        }

        string = getParameter ("groupid");
        if (string != null && !string.equals ("")) {
            setGroupID(string);
        }

        string = getParameter ("polltime");
        if (string != null && !string.equals ("")) {
            setPollInterval (Integer.parseInt (string));
        }
    }

    public void start() {
        // set the color
        setBackground (Color.white);
        setForeground (Color.black);

        userField = new TextField(30);
        Label userLabel = new Label("Enter your Name");
        add(userLabel);
        add(userField);
        userField.addActionListener(this);
        buddyList = new List();
        buddyList.setBackground(new Color (0xdd,0xdd,0xdd));
     }

    public void actionPerformed(ActionEvent evt) {
        if (DEBUG) {
            System.out.println("actionPerformed " + evt);
        }

        // actions from the Messenger Panel
        if ((evt.getActionCommand() == BUTTON_SEND) || (evt.getSource() == inputField)) {
            sendMessage();
        // actions from the Settings Panel
        } else if (evt.getSource() == userField) {
            userName = userField.getText();
            if (DEBUG){
                System.out.println ("username is: " + userName);
            }
            initiateConnect();
        } 
    }
    public void itemStateChanged(ItemEvent evt) {
        if (DEBUG) {
             System.out.println("itemStateChanged " + evt);
        }

        if (evt.getSource() == textChoice) {
             int index = textChoice.getSelectedIndex();

             if (index < numberPhrases) {
                  String text = inputField.getText();
                  int start = inputField.getSelectionStart();
                  int end = inputField.getSelectionEnd();

                  inputField.setText(text.substring(0, start) + 
                                     textChoice.getSelectedItem() +
                                     text.substring(end));
            }
        }
    }

    private void setBackgroundColor (String color) {
        bgColor = color;
    }

    private void setForegroundColor (String color) {
        fgColor = color;
    }

    private void setRelayURL (String relayURL) {
        relay = relayURL;
    }

    private String getRelayURL () {
        return relay;
    }

    private void setGroupName (String group) {
        groupName = group;
    }

    private String getGroupName () {
        return groupName;
    }

    private void setGroupID (String groupID) {
        completeGroupID = ID_PREFIX + NET_GROUP_ID + groupID + 
                        PIPE_ID_SUFFIX;
    }

    private String getGroupID (){
        return completeGroupID;
    }

    private void setPollInterval (int time) {
        sleepTime = time;
    }

    private void selectPanel(Panel selectedPanel, String title) {
        removeAll();

        setLayout (new BorderLayout());
        add(buddyList, "West");
        add(selectedPanel, "Center");
        selectedPanel.setVisible(true);
	selectedPanel.setSize(getWidth(), getHeight());
        getParent().validate();
    }

    public void run() {
        boolean notAnnounced = true;

        while (peer != null) {
            if (!connected && connectInitiated) {
                try {
                    connect();
	            if (DEBUG) {
                        System.out.println ("Connected...");
                    }
                } catch (Throwable e){
                    if (DEBUG) {
                        e.printStackTrace();
                    }	
	        }
            }
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
                    System.out.println("sleep for " + sleepTime/1000 + " seconds");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                }
            } else {
                handleMessage(m);
            }
            if (notAnnounced) {
                try {
	            send (userName + " has joined ***");
                    notAnnounced = false;
                } catch (Throwable t) {
                    t.printStackTrace ();
                }
            }
        }

        thread = null;
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

        for (int i = 0; i < m.getElementCount(); i++) {
            Element element = m.getElement(i);
            String fullName = element.getName();
            if (DEBUG) {
                if (element.getData().length < 100) {
                    String dataStr = new String(element.getData());
                    System.out.println("["+i+"] "+ fullName + " = " + dataStr);
                }
            }

            if ("JxtaTalkSenderName".equals(fullName)) {
                sender = new String(element.getData());;
            } else if ("JxtaTalkSenderMessage".equals(fullName)) {
                message = new String(element.getData());;
            } 
            // PicShare block
             else if ("TotalBlocks".equals(fullName)) {
                try {
                    totalBlocks = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if ("BlockSize".equals(fullName)) {
                try {
                    blockSize = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if ("BlockNum".equals(fullName)) {
                try {
                    blockNum = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if ("FileSize".equals(fullName)) {
                try {
                    fileSize = Integer.parseInt(new String(element.getData()));
                } catch (NumberFormatException e) {
                }
            } else if ("FileName".equals(fullName)) {
                fileName = new String(element.getData());
            } else if ("FileKey".equals(fullName)) {
                fileKey = new String(element.getData());
            } else if ("DataBlock".equals(fullName)) {
                dataBlock = element.getData();
            }
        }

        if (sender != null && message != null) {
            log(sender, message);
        } else if (fileKey != null) {

            // Is start of a new Image?
            if (!fileKey.equals(picShareKey) || picShareDataBlocks == null) {
                picShareKey = fileKey;
                picShareDataBlocks = new Object[totalBlocks];
            }

            // save this block of data
            if (blockNum < picShareDataBlocks.length) {
                picShareDataBlocks[blockNum] = dataBlock;
            }

            if (picShareDataBlocks.length == 1) {
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
                }
            }
        }
    }

    private void startPolling() {
        // start the polling thread
        thread = new Thread(this);
        thread.start();
    }

    private void initiateConnect() {
        messengerPanel = MessengerPanel();
        startPolling();

        if (peer == null) {
            peer = PeerNetwork.createInstance(userName);
        }

        if (connected || connectInitiated) {
            return;
        }

        connectInitiated = true;
        // we will perform the actual operation in the poll thread
        selectPanel(messengerPanel, "Chat group " + userName);
    }

    private boolean connect() throws Throwable {
        connectInitiated = false;
        String url = getRelayURL();
       
        if (DEBUG) {
            System.out.println ("BaseURL: " + getCodeBase());
            System.out.println("Connect():Connecting to " + url);
        }
	byte[] state = null;
        try {
	    state = peer.connect(url, state);
            connected = true;
            if (DEBUG) {
                System.out.println("Connected " + connected + "...");
            }

            String chatIdentity = CHAT_USERNAME_PREFIX + getGroupName();
            String pipeId = null;

            if (DEBUG) {
                System.out.println("Listening on " + chatIdentity);
            }
            peer.listen(CHAT_USERNAME_PREFIX + getGroupName(), 
                        getGroupID(), PIPE_TYPE_PROPAGATE);

        } catch (Throwable e) {
	    if (DEBUG) {
                e.printStackTrace ();
            }
            return false;
        }

        return true;
    }

    public void destroy (){
        stop();
    }

    public void stop() {
	if (DEBUG){
	    System.out.println ("Closing Connection");
        }
        try {
            send (userName + " has left ***");
	    peer.close(CHAT_USERNAME_PREFIX+getGroupName(), 
		   getGroupID(), PIPE_TYPE_PROPAGATE);
	} catch ( Exception e ) {
          e.printStackTrace ();
        }
        disconnect();
        super.stop();
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

    private void sendMessage() {
        String message = inputField.getText();

        // send the message to the buddy
        try {
            send(message);
            inputField.setText("");
        } catch (IOException e) {
	    e.printStackTrace ();
            log("", "Reconnecting....");
            disconnect();
            try {
               connect();
            } catch (Throwable t) {}
        }
    }

    private void send(String message) throws IOException {
        Element[] elm = new Element[3];
        elm[0] = new Element("JxtaTalkSenderName", 
                             userName.getBytes(), null, null);

        elm[1] = new Element("JxtaTalkSenderMessage", 
                             message.getBytes(), null, null);
        elm[2] = new Element("GrpName", 
                             getGroupName().getBytes(), null, null);

        Message m = new Message(elm);

        peer.send(CHAT_USERNAME_PREFIX+getGroupName(),
                  getGroupID(), PIPE_TYPE_PROPAGATE, m);

        thread.interrupt();
    }

    private synchronized void log(String sender, String message) {
        logArea.appendText(sender + "> " + message + "\n");
        if(!listVector.contains(sender)) {
		listVector.addElement(sender);
                buddyList.addItem(sender);
        }
    }

    private Choice textChoice = new Choice();
    private TextArea logArea = 
            new TextArea("", -1, -1, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private TextField inputField = new TextField();
    private Button sendButton = new Button(BUTTON_SEND);

    private Panel MessengerPanel() {
        Panel panel = new Panel ();
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        c.gridx = 0;
	c.gridwidth = 5;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(logArea, c);

        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textChoice, c);

        c.gridwidth = 4;
        gridbag.setConstraints(inputField, c);

        c.gridwidth =0; 
        c.gridx = 4;
        c.weightx =0.0;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(sendButton, c);

        panel.add(logArea);
        panel.add(textChoice);
        panel.add(inputField);
        panel.add(sendButton);

        logArea.setEditable(false);
        logArea.setBackground ( new Color(0xe9, 0xee, 0xf9));
        textChoice.addItemListener(this);
        inputField.addActionListener (this);
        sendButton.addActionListener (this);

        for (int i = 0; i < CANNED_PHRASES.length; i++) {
             textChoice.add(CANNED_PHRASES[i]);
        }
         return panel;
    }
}
