/************************************************************************
 *
 * $Id: ChatPanel.java,v 1.18 2002/09/21 00:58:47 akhil Exp $
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

package net.jxta.doja.demo.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.nttdocomo.util.Timer;
import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.Component;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.Panel;
import com.nttdocomo.ui.ListBox;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.TextBox;
import com.nttdocomo.ui.Label;
import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.ShortTimer;
import com.nttdocomo.ui.SoftKeyListener;
import com.nttdocomo.ui.ComponentListener;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaSound;
import com.nttdocomo.ui.MediaListener;
import com.nttdocomo.ui.MediaPresenter;
import com.nttdocomo.ui.AudioPresenter;
import com.nttdocomo.ui.VisualPresenter;
//import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.UIException;

 import javax.microedition.io.Connector;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

public class ChatPanel extends Panel
     implements SoftKeyListener, ComponentListener, Runnable {

    private static final boolean DEBUG = true;
    private static final boolean QUANTIFY = false;

    private static final int DEFAULT_POLL_INTERVAL = 1;
    private static final int CONFIG_OFFSET = 9000;
    private static final int BUDDY_LIST_OFFSET = CONFIG_OFFSET + 250;

    // for various reasons BINARY_DATA needs to be the first
    private static final String SP = "scratchpad:///0";
    private static final String BINARY_DATA = SP + ";pos=0";
    private static final String CONFIG_RECORD = SP + ";pos=" + CONFIG_OFFSET;
    private static final String BUDDYLIST_RECORD = SP + ";pos=" + BUDDY_LIST_OFFSET;
    private static final String CHAT_USERNAME_PREFIX = "JxtaTalkUserName.";
    
    public static final String PIPE_TYPE_UNICAST = "JxtaUnicast";
    public static final String PIPE_TYPE_PROPAGATE = "JxtaPropagate";
    
    static final String ID_PREFIX       = "urn:jxta:uuid-";
    static final String PIPE_ID_SUFFIX  = "04";
    static final String NET_GROUP_ID    = "59616261646162614E50472050325033";    

    private static final String TALKNAME_PREFIX = "JxtaTalkUserName.";
    private static final String INSTANTP2P_GROUPNAME = "IP2PGRP";
    private static final String INSTANTP2P_PIPEID = ID_PREFIX + NET_GROUP_ID + 
        "D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1" + PIPE_ID_SUFFIX;
    private static final String PICSHARE_GROUPNAME = "PicShare";
    private static final String PICSHARE_PIPEID = ID_PREFIX + NET_GROUP_ID +
        "50696353686172652D5B46696C654361" + PIPE_ID_SUFFIX;

    private static final String pipe_type = PIPE_TYPE_PROPAGATE;

    private static AudioPresenter theAP = AudioPresenter.getAudioPresenter();

    private static VisualPresenter theVP = null;

    private Panel mainPanel = null;
    private Panel replyForm = null;
    private MediaPlayer mediaPlayer;

    private TextBox tbIdentity = null;
    private TextBox tbSentMsg = null;
    private ListBox lbRcvdMsg = null;

    private PeerNetwork peer = null;
    private String currentBuddy = "IP2PGRP"; 
    private String relayURL;
    private Hashtable buddyIds = new Hashtable();
    private Font font = null;
    private Vector messages;

    private byte[] state = new byte[0];
    private int pollInterval = DEFAULT_POLL_INTERVAL;
    private Thread pollThread = null;
    private boolean stopPolling = false;
    private boolean connectInitiated = false;
    private boolean connected = false;
    private boolean sendPending = false;

    private final int DEFAULT_SCROLL;
    private final int HEIGHT, WIDTH, LINEHEIGHT;
    private ListBox buddyList = null;

    private Panel imagePanel;
    private ListBox imgList = null;
    private String savedImages[] = new String [8];
    private String binaryFileName = null;
    private byte[] binaryData = null;

    public ChatPanel (String sourceURL){
        if (DEBUG) {
            System.out.println ("SourceURL: " + sourceURL);
        }

        // Parse the sourceURL. For DoJa, we do not need getFile() 
        // portion of the URL. We do not need to validate thr URL 
        // as its done by the DoJa engine.
        //        
        // get index of "//" as part of protocol
        int n = sourceURL.indexOf('/') + 2;

        // get index of first "/" after "://"
        n = sourceURL.indexOf ('/', n);
        if (n > 0) {
            relayURL = sourceURL.substring(0, n);
        } else {
            relayURL = sourceURL;
        }

        if (DEBUG) {
            System.out.println ( "relayURL: " + relayURL);
        }

        // initialize all the class finals.
        font = Font.getDefaultFont ();
        HEIGHT = getHeight();
        WIDTH = getWidth();
        LINEHEIGHT = font.getBBoxHeight ("fg");
        DEFAULT_SCROLL = HEIGHT/LINEHEIGHT -2;

        messages = new Vector(DEFAULT_SCROLL);
    
        readConfig();
        setTitle ("Identity");
        add(new Label("Name: " ));
        add(tbIdentity);
        
        setSoftLabel(SOFT_KEY_1, "Exit");
        setSoftLabel(SOFT_KEY_2, "OK");
        setComponentListener(this);
        setSoftKeyListener(this);

        mediaPlayer = new MediaPlayer(this);
        imgList = new ListBox(ListBox.RADIO_BUTTON);

    }

    public void softKeyPressed(int index) {}

    public void softKeyReleased(int index) {
        if (index == SOFT_KEY_1) {
            // If the current displayed panel is ChatPanel 
            // or mainPanel -> SOFT_KEY_1 corresponds to "Exit"
            if (Display.getCurrent() == replyForm ||
                Display.getCurrent() == imagePanel) {
                 // If the current displayed panel is a sendPanel 
                // or mainPanel -> SOFT_KEY_1 corresponds to "Back".
                // So, display the mainPanel
                Display.setCurrent(mainPanel);
            } else if (Display.getCurrent() == mediaPlayer) {
                // save image
                saveImage();
            } else {
                terminate();
            }
        } 
        else if (index == SOFT_KEY_2) {
            if (connected & Display.getCurrent() == mainPanel) {
                sendPanel();
            } else if (Display.getCurrent() == replyForm) {
                if (send()) {
                    Display.setCurrent (mainPanel);
                }
            }else if (Display.getCurrent() == this){
                // starts app and initiates connection
                initiateConnect();
            } else if (Display.getCurrent() == imagePanel) {
                sendImage (imgList.getSelectedIndex()+1);
            } else {
                if (Display.getCurrent()== mediaPlayer) {
                    try {
                        theAP.stop();
                    } catch (Throwable t) {}
                }
                Display.setCurrent (mainPanel);
            }
        } 
    }

    public void componentAction (Component c, int type, int param){
        if (c == tbSentMsg){
            if (send()) {
                Display.setCurrent (mainPanel);
            }
        } else if (c == buddyList) {
            String buddy = buddyList.getItem(buddyList.getSelectedIndex());
            if (buddy.equals("Send Image")) {
                displayImgList();
                return;
            }

            if (buddy.equals(currentBuddy)) {
                return;
            }
            // close the current pipe and start listening on a new propagate pipe
            try {
                closePipe(currentBuddy);
            } catch (IOException ex) {
               showAlert(Dialog.DIALOG_ERROR, 
                         "closePipe", 
                         "Error closing pipe: " + ex.getMessage());
            }

            currentBuddy = buddy;

            try {
                listen(currentBuddy);
            } catch (Throwable ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Connect", 
                      "Error joining group: " + buddy + " " + ex.getMessage());
            }
        } else if (c == tbIdentity){
            //starts app and initiates connection
            initiateConnect();
        }
    }

    private void createMainPanel() {
        mainPanel = new Panel ();
        buddyList = new ListBox (ListBox.CHOICE);
        buddyList.append("IP2PGRP");
        buddyList.append("PicShare");
        buddyList.append("Send Image");
        lbRcvdMsg = new ListBox (ListBox.SINGLE_SELECT, DEFAULT_SCROLL);

        mainPanel.add(buddyList);
        mainPanel.add(lbRcvdMsg);

        mainPanel.setSoftLabel(SOFT_KEY_1, "Exit");
        mainPanel.setSoftLabel(SOFT_KEY_2, "Send");
        mainPanel.setComponentListener(this);
        mainPanel.setSoftKeyListener(this);
    }

    private void startApp() {
        stopPolling = false;
        pollThread = new Thread(this);
        pollThread.start();
    }
    
    private void pauseApp() {
        stopPolling = true;
        pollThread = null;
    }

    public void terminate() {
        if (DEBUG) {
            System.out.println ("Exiting....");
        }
        stopPolling = true;
        pollThread = null;

        storeConfig();
        peer = null;
        Chat.app.terminate();
    }

    private void displayImgList() {
        if (imagePanel == null) {
            imagePanel = new Panel();
            imagePanel.setTitle ("Image List");
            imagePanel.add(imgList);
            imagePanel.setSoftLabel(SOFT_KEY_1, "Back");
            imagePanel.setSoftLabel(SOFT_KEY_2, "Send");
            imagePanel.setSoftKeyListener (this);
        }
        Display.setCurrent (imagePanel);
    }

    private void saveImage() {
        int i;

        // start saving from 10K position as the first 10K are used as temp 
        // storage for current image
        for (i = 1; i < savedImages.length; i++){
            if (savedImages[i] == null) {
                break;
            }
        }
        if (i >= savedImages.length) {
            System.out.println ("Scratchpad is full");
            return;
        }

        try {
            DataOutputStream dosSP = Connector.openDataOutputStream(SP + ";pos=" + i*10000);
            dosSP.writeShort(binaryData.length);
            dosSP.write(binaryData);
            dosSP.close();
        } catch (Exception e) {
            showAlert(Dialog.DIALOG_WARNING, 
                      "processBinaryData", 
                      "Corrupted data");
            return;  
        }
        savedImages[i] = binaryFileName;
        imgList.append(binaryFileName);
    }


    private void sendImage (int index) {
        DataInputStream disSP = null;
        byte[] data = null;
        try {
            disSP = Connector.openDataInputStream (SP + ";pos=" + index*10000);
            int dataLen = disSP.readShort();
            data = new byte[dataLen];
            disSP.readFully (data);
            disSP.close();
            if (DEBUG) {
                System.out.println("Read image: dataLen=" + dataLen);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (disSP != null) {
                    disSP.close();
                }
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }

        Element [] elm = new Element[9];
        elm[0] = new Element("JxtaTalkSenderName", 
                             tbIdentity.getText().getBytes(), 
                             null, null);
        // for compatibility with myJXTA aka InstantP2P
        elm[1] = new Element("GrpName", 
                             "NetPeerGroup".getBytes(), 
                             null, null);
        elm[2] = new Element("MessageType", 
                             "FILE".getBytes(), null, null);
        elm[3] = new Element("FileName", 
                             savedImages[index].getBytes(), null, null);
        elm[4] = new Element("FileSize", 
                             String.valueOf(data.length).getBytes(), null, null);
        elm[5] = new Element("FileKey", 
                             savedImages[index].getBytes(), null, null);
        //elm[6] = new Element("BlockSize", 
        //                     String.valueOf(9000).getBytes(), null, null);
        elm[6] = new Element("DataBlock", 
                             data, null, null);
        elm[7] = new Element("BlockNum", 
                             String.valueOf(1).getBytes(), null, null);
        elm[8] = new Element("TotalBlocks", 
                             String.valueOf(1).getBytes(), null, null);

        Message m = new Message(elm);
        try {
            String chatBuddy = TALKNAME_PREFIX + currentBuddy;
            String pipeId = (String) buddyIds.get(currentBuddy);
            peer.send(chatBuddy, pipeId, pipe_type, m);
        } catch (IOException ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "SendImage", 
                      "Error sending message: " + ex.getMessage());
            return;
        }
    }

    private void sendPanel() {
        if (currentBuddy == null) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Send", 
                      "Please first select a buddy to chat with");
            return;
        }

        if (replyForm == null) {
            replyForm = new Panel();
            replyForm.setTitle("Send/Reply");
            tbSentMsg = new TextBox(null, 
				    40,
                                    DEFAULT_SCROLL,
                                    TextBox.DISPLAY_ANY);
            tbSentMsg.setEditable(true);
            replyForm.add(tbSentMsg);
            replyForm.setSoftLabel(SOFT_KEY_1, "Back");
            replyForm.setSoftLabel (SOFT_KEY_2, "Send");
            replyForm.setComponentListener(this);
            replyForm.setSoftKeyListener(this);
        }

        Display.setCurrent(replyForm);
    }

    private void readConfig() {
        String prop = null;

        tbIdentity = new TextBox(prop == null ? "" : prop,
                                 20,
                                 1,
                                 TextBox.DISPLAY_ANY);
        DataInputStream disSP = null;
        try {
            disSP = Connector.openDataInputStream (CONFIG_RECORD);
            int dataLen = disSP.readInt();
            byte [] data = new byte[dataLen];
            disSP.readFully (data);

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            tbIdentity.setText(dis.readUTF());
            int stateLen = dis.readShort();
            state = new byte[stateLen];
            dis.readFully(state);

            dis.close();
            bais.close();
            if (DEBUG) {
                System.out.println("Read config: identity=" + tbIdentity.getText() +
                                   " groupChat=" +
                                   " state=" + state.toString() +
                                   " stateLen=" + stateLen);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (disSP != null) {
                    disSP.close();
                }
            } catch (Exception ex) {
                if (DEBUG) {
                    System.out.println(ex);
                }
            }
        }
    }

    private void storeConfig() {

        DataOutputStream dosSP = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
            dos.writeUTF(tbIdentity.getText());
            dos.writeShort(state.length);
            dos.write(state);
            dos.close();
            byte [] data = baos.toByteArray();

            dosSP = Connector.openDataOutputStream(CONFIG_RECORD);
            dosSP.writeInt(data.length);
            dosSP.write (data);

            if (DEBUG) {
                System.out.println("Saved config: identity=" + tbIdentity.getText() +
                                   " groupChat=" +
                                   " stateLen=" + state.length);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                System.out.println(ex);
            }
        } finally {
            try {
                if (dosSP != null) {
                    dosSP.close();
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

    private boolean send() {
        if (peer == null || !connected) {
            initiateConnect();
            sendPending = true;
            return false;
        }

        String msg = tbSentMsg.getText();
        Element[] elm = new Element[3];
        elm[0] = new Element("JxtaTalkSenderName", 
                             tbIdentity.getText().getBytes(), 
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
            peer.send(chatBuddy, pipeId, pipe_type, m);
        } catch (IOException ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Send", 
                      "Error sending message: " + ex.getMessage());
            return false;
        }

        return true;
    }

    private void initiateConnect() {
	createMainPanel();
        startApp();

        if (peer == null) {
            peer = PeerNetwork.createInstance(tbIdentity.getText());
        }

        if (connected || connectInitiated) {
            return;
        }

        connectInitiated = true;
        // we will perform the actual operation in the poll thread
    }


    private void listen (String buddy) throws IOException {

         String chatIdentity = TALKNAME_PREFIX + buddy;

         String pipeId = null;
         if (INSTANTP2P_GROUPNAME.equals(buddy)) {
             // listen on myJXTA's well-known pipe id if talking to it
             pipeId = INSTANTP2P_PIPEID;
         } else if (PICSHARE_GROUPNAME.equals(buddy)) {
             // listen on PicShare's well-known pipe id if talking to it
             pipeId = PICSHARE_PIPEID;
         }

         if (DEBUG) {
             System.out.println("Listening on " + chatIdentity);
         }
         peer.listen(chatIdentity, pipeId, pipe_type);
    }

    private boolean connect() {
        connectInitiated = false;
        String url = relayURL;
        if (DEBUG) {
            System.out.println("Connect():Connecting to " + url);
        }

        try {
            long startTime = 0;
            long endTime = 0;
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
            if (DEBUG) {
                System.out.println("Connected " + connected + "...");
            }

            listen(currentBuddy);

            int size = buddyList.getItemCount();
            for (int i=0; i < size; i++) {
                String buddy = buddyList.getItem(i);
                peer.search(PeerNetwork.PIPE, TALKNAME_PREFIX + buddy);
            }
        } catch (Throwable ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Connect", 
                      "Error connecting to relay: " + ex.getMessage());
            return false;
        }

        return true;
    }

    private void closePipe (String buddy) throws IOException {
	String chatIdentity = TALKNAME_PREFIX + buddy;  
	String pipeId = null;

	if (INSTANTP2P_GROUPNAME.equals(buddy)) {
	    // close myJXTA's well-known pipe id if talking to it
	    pipeId = INSTANTP2P_PIPEID;
	} else if (PICSHARE_GROUPNAME.equals(buddy)) {
	    // close on PicShare's well-known pipe id if talking to it
	    pipeId = PICSHARE_PIPEID;
	}

        peer.close(chatIdentity, pipeId, pipe_type);
        if (DEBUG) {
	    System.out.println("Closed " + chatIdentity);
        }
    }

    private void disconnect() {
        try {
            closePipe(currentBuddy);
            connected = false;
        } catch (IOException ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Disconnect", 
                      "Error connecting to relay: " + ex.getMessage());
        }
        peer = null;
    }

    
    // a class to increment the value of a Gauge everytime it is run
    static class Gauge extends Canvas {
        private final static int PADDING_X = 2;  // for title
        private final static int PADDING_Y = 4; // for title
        private final static int MAX_BARS  = 10; // number of progress bars
        private static ChatPanel midlet;

        private int ticks = MAX_BARS;
        private final int barWidth;              // Width of each Bar
        private final int offset;                // Offset for first bar

        Gauge(ChatPanel chat) {
            midlet = chat;
            // width of each bar to bve drawn
            barWidth = midlet.WIDTH/(MAX_BARS*2);
            // offset is required to center align all the bars.
            offset = (midlet.WIDTH - (barWidth*MAX_BARS*2))/2;

            setSoftLabel(SOFT_KEY_1, "Exit");
        }

        public void paint (Graphics g){
            // Clear up the complete screen when all the bars are drawn
            if (ticks == MAX_BARS) {
                // initializing to zero as we don't want to compute modulo value
	        ticks = 0;
		//int baseLine = font.getBBoxHeight("Connecting");

                // clear up the whole screen
                g.clearRect (0, 0, midlet.WIDTH, midlet.HEIGHT);
                // draw TITLE string. DoJa Emulator draws string at the baseline
                g.drawString ("Connecting", PADDING_X, midlet.LINEHEIGHT);
                // draw line under the title string
                g.drawLine (0, 
                            midlet.LINEHEIGHT+PADDING_Y, 
                            midlet.WIDTH, 
                            midlet.LINEHEIGHT+PADDING_Y);

                for (int i=0; i<MAX_BARS; i++) {
                    // draw progress bars - unfilled
                    g.drawRect (i*2*barWidth+offset, 
                                midlet.HEIGHT/2, 
                                barWidth, 
                                20);
                }
            }
            // Fill only the required bar
            g.fillRect (ticks*2*barWidth+offset, 
                        midlet.HEIGHT/2, 
                        barWidth, 
                        20);
	}

        public void processEvent(int type, int param) {
            if (param == 21){
                midlet.terminate();
            } else {
               ++ticks;
               repaint();
            }
        }
    }

    public void run() {
        if (DEBUG) {
            System.out.println("starting poll thread");
        }

        while (!stopPolling) {
            if (!connected && connectInitiated) {
                ShortTimer timer = null;
                Gauge updater = new Gauge(this);
                Display.setCurrent(updater);
		try {
		    timer = ShortTimer.getShortTimer(updater, 99, 1000, true);
                    timer.start();
		} catch (Exception e) {
                    timer.stop();
		}
                try {
                    connect();
                    if (DEBUG) {
                        System.out.println ("Connected...");
                    }
                } catch (Throwable ex){
                    showAlert(Dialog.DIALOG_ERROR, 
                      "run", 
                      "Error connecting to relay: " + ex.getMessage());

		}

                if (sendPending) {
                    try {
                        send();
                    } finally {
                        sendPending = false;
                    }
                }
                Display.setCurrent(mainPanel);
            }

            try {
                poll();
            } catch (Throwable t) {
                showAlert(Dialog.DIALOG_ERROR, 
                          "Poll", 
                          "Error processing message: " + t.getMessage());
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

    private void poll() {
        if (peer == null || !connected) {
            // not yet connected
            return;
        }

        Message msg = null;
        try {
            long startTime = 0;
            long endTime = 0;
            if (QUANTIFY) {
                startTime = System.currentTimeMillis();
            }
            // timeout must not be zero: zero means block forever
            if (peer != null) {
                msg = peer.poll(1);
            }
            if (QUANTIFY) {
                endTime = System.currentTimeMillis();
                System.out.println("poll took " +
                                   Long.toString(endTime-startTime));
            }
        } catch (IOException ex) {
            showAlert(Dialog.DIALOG_ERROR, 
                      "Poll", 
                      "Error polling relay: " + ex.getMessage());
            return;
        }

        if (msg == null) {
            return;
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

            int size = buddyList.getItemCount();
            for (int i=0; i < size; i++) {
                String buddy = buddyList.getItem(i);
                if (buddy.equals(name) &&
                    id != null && !"".equals(id)) {
                    buddyIds.put(name, id);
                    break;
                }
            }
        }

        String sender = null;
        String message = null;

        String binaryCaption = null;

        boolean isDisplayable = true;
        for (int i=0; i < msg.getElementCount(); i++) {
            el = msg.getElement(i);
            if ("JxtaTalkSenderName".equals(el.getName())) {
                sender = new String(el.getData());
            } else if ("JxtaTalkSenderMessage".equals(el.getName())) {
                message = new String(el.getData());
            } else if ("Caption".equals(el.getName())) {
                binaryCaption = new String(el.getData());
            } else if ("FileName".equals(el.getName())) {
                binaryFileName = new String(el.getData());
            } else if ("DataBlock".equals(el.getName())) {
                binaryData = el.getData();
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

        if (binaryData != null) {
            processBinaryData (binaryData, binaryFileName, binaryCaption, sender);
        }

        if (sender != null && message != null) {
            if (sender.indexOf(TALKNAME_PREFIX) >= 0) {
                sender = sender.substring(TALKNAME_PREFIX.length());
            }
            String displayedMsg = sender + "> " + message + "\n";
            if (messages.size() >= DEFAULT_SCROLL) {
                if (DEBUG) {
		    System.out.println ("List size: " + messages.size());
                }
                messages.removeElementAt(0);
	    }
            messages.addElement (displayedMsg);
            displayString (messages);

        }
    }

    private void displayString (Vector messages) {
        int width = lbRcvdMsg.getWidth();

        lbRcvdMsg.removeAll();

        // DoJa doesn't provide StringItem
        for (int i = 0; i < messages.size(); i++) {
             String msg = (String)messages.elementAt(i);
             int len = msg.length();

             if (len > 0) {
                int lineBreak = font.getLineBreak (msg, 0, msg.length(), width);

                int offset = 0;
                while (offset < len) {
                    lbRcvdMsg.append (msg.substring(offset));
                    offset += lineBreak;
                }
            }
	}
    }

    private void processBinaryData (byte[] data, 
                                    String fileName, 
                                    String caption, 
                                    String sender) {

        if (data.length > CONFIG_OFFSET){
             showAlert(Dialog.DIALOG_WARNING, 
                       "processBinaryData", 
                       "Picture too big");
             return;
        }
        try {
            DataOutputStream dosSP = Connector.openDataOutputStream(BINARY_DATA);
            dosSP.write(data);
            dosSP.close();
        } catch (Exception e) {
            showAlert(Dialog.DIALOG_WARNING, 
                      "processBinaryData", 
                      "Corrupted data");
            return;  
        }

        if (DEBUG) {
            System.out.println("Saved data: caption=" + caption + " sender=" + sender);
        }

        caption = caption == null ? fileName : caption;
        String ext = fileName.substring(fileName.lastIndexOf('.')+1);
        if (ext.equals("mid") || ext.equals("midi") || ext.equals("mfi")) {
            mediaPlayer.createClip(caption);
        } else {
            mediaPlayer.createImage (caption);
        }
        Display.setCurrent (mediaPlayer);
    }


    void showAlert(int type, String title, String message) {
        if (DEBUG) {
            System.out.println ("ALERTS: " + message);
        }
        Dialog dialog = new Dialog(type, title);
        dialog.setText(message);
        dialog.show();
    }


    private static class MediaPlayer extends Panel implements MediaListener {

        private String caption;
 
        public MediaPlayer (ChatPanel midlet){
            setTitle ("JXME Media Player");
            setSoftLabel(SOFT_KEY_1, "Save");
            setSoftLabel(SOFT_KEY_2, "OK");
            setSoftKeyListener((SoftKeyListener)midlet);
        }

        public void createClip (String caption) {
            this.caption = caption;

            MediaSound mediaSound = null;
            try {
                mediaSound = MediaManager.getSound(SP);
                mediaSound.use();
            } catch (UIException e) {
                if (DEBUG) {
                    System.out.println (e.getStatus());
                }
            } catch (Throwable ce) {

               // Handle networking problem here
		if (DEBUG) {
		    System.out.println ("use: " + ce);
		}
            }
            theAP.setSound(mediaSound);
            theAP.setMediaListener(this); 
            theAP.play();
        }

        public void createImage (String caption) {
            this.caption = caption;

            MediaImage mediaImage = null;
            try {
                mediaImage = MediaManager.getImage(SP);
                mediaImage.use();
            } catch (UIException e) {
                if (DEBUG) {
                    System.out.println (e.getStatus());
                }
            } catch (Throwable ce) {

               // Handle networking problem here
		if (DEBUG) {
		    System.out.println ("use: " + ce);
		}
            }
            if (theVP == null) {
                theVP = new VisualPresenter();
                add(theVP);
                theVP.setMediaListener(this); 
            }else {
                // we need to stop the previous animation
                theVP.stop();
            }
            theVP.setImage(mediaImage);
            theVP.play();
        }

        /**
         * MediaListener to listen any Media(AUDIO/VISUAL) EVENT so that <code>play</code>
         * and <code>stop</code> of Audio/VisdualPresenter can be called if necessary.
         * For example, to implement "looping" sound.
         */
        public void mediaAction(MediaPresenter source, int type, int param) {

            if (source == theVP) {
                switch (type) {

                // We receive event telling us Visual has finished playing.
                case VisualPresenter.VISUAL_COMPLETE :
                    // uncomment the following if a loop is desired
                    // theVP.play();
                    setTitle ("complete");
                    break;

                // We receive event telling us Visual has just begin playing.
                // Show that to the screen
                case VisualPresenter.VISUAL_PLAYING :
                    setTitle ("Playing: " + caption);
                    break;

                // We receive event telling us Visual has stopped.
                // Show that to the screen (may not have enough time
                // to stay in the screen if play is called right
                // after this), but we do receive this event.
                case VisualPresenter.VISUAL_STOPPED :
                    setTitle ("Stopped");
                    break;
                }
            } else if (source == theAP) {
                switch (type) {

                // We receive event telling us Audio has finished playing.
                case AudioPresenter.AUDIO_COMPLETE :
                    setTitle ("complete");
                    break;

                // We receive event telling us Audio has just begin playing.
                // Show that to the screen
                case AudioPresenter.AUDIO_PLAYING :
                    setTitle ("Playing: " + caption);
                    break;

                // We receive event telling us Audio has stopped.
                // Show that to the screen (may not have enough time
                // to stay in the screen if play is called right
                // after this), but we do receive this event.
                case AudioPresenter.AUDIO_STOPPED :
                    setTitle ("Stopped");
                    break;
                }
            }
        }
    }
}
