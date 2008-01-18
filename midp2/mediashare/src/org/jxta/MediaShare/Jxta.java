// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import java.util.Hashtable;
import java.util.Random;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import javax.microedition.lcdui.*;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

import javax.microedition.io.file.*;        // FILES

public class Jxta {
    //
    // Public Stuff
    //
    public final static int BUDDIES = 0;
    public final static int GALLERIES = 1;
    public final static int PRINTERS = 2;

    //
    // Private Stuff
    //
    private static final boolean DEBUG = true;
    private static final boolean QUANTIFY = true;
    private static final int DEFAULT_ALERT_TIMEOUT = 5000;
    private static final String TALKNAME_PREFIX = "JxtaTalkUserName.";
    private static final String PICSHARE_GROUPNAME = "PicShare";
    private static final String INSTANTP2P_GROUPNAME = "IP2PGRP";
    private static final String INSTANTP2P_PIPEID = "urn:jxta:uuid-" +
            "59616261646162614E50472050325033" +
            "D1D1D1D1D1D141D191D191D1D1D1D1D104";
    private static final String PICSHARE_PIPEID = "urn:jxta:uuid-" +
            "59616261646162614E50472050325033" +
            "5069635368614265AD5B86696C65436104";

    private byte[] state = new byte[0];
    protected boolean connectInitiated = true;
    protected boolean connected = false;
    private boolean isGroupChat = true;
    private PeerNetwork peer = null;
    private String identity;
    private String currentBuddy = "PicShare";
    private int relayPort;
    private int responseId = -1;
    protected int pollInterval;
    private String relayHost;
    private String replyBuddy = null;
    private Hashtable buddyIds = new Hashtable();
    private List buddyList = null;
    private Hashtable imgCache = new Hashtable();
    private Hashtable imgBlockSize = new Hashtable();
    private Hashtable imgFileSize = new Hashtable();
    private Hashtable imgTotalBlocks = new Hashtable();
    protected JxtaGui gui;
    protected boolean sendPending = false;
    private Random random = new Random();

    private String[] buddyTypes =
            {
                    "My Buddies",
                    "My Galleries"
            };

    private String[] buddies =
            {
                    "Wife's Phone",
                    "Dad's Phone",
                    "Son's Phone",
                    "Daughter's Phone",
                    "Home PC",
                    "My PDA"
            };

    private String[] galleries =
            {
                    "Kodak Easyshare Gallery",
                    "Yahoo Photo Gallery",
                    "Google Photo Gallery",
                    "My Home Gallery",
                    "My Set-Top-Box"
            };

    private String[] printers =
            {
                    "Kodak",
                    "Home",
                    "Work"
            };

    // send() inputs
    String sendFileName;
    byte[] sendFileData;

    //
    // Constructors
    //
    public Jxta(String ident, String host, int port, int pollInt, JxtaGui in_gui) {
        identity = ident;
        relayHost = host;
        relayPort = port;
        pollInterval = pollInt;
        gui = in_gui;

        buddyList = new List("Buddy List", List.IMPLICIT);
        buddyList.append(INSTANTP2P_GROUPNAME, null);
        buddyList.append(PICSHARE_GROUPNAME, null);
    }

    //
    // My Methods
    //
    public String[] getBuddyTypes() {
        return (buddyTypes);
    }

    public String[] getBuddies(int buddyType) {
        if (buddyType == PRINTERS)
            return (printers);
        else if (buddyType == GALLERIES)
            return (galleries);

        return (buddies);
    }

    //
    // Connect to the network
    //
    protected boolean connect()
            throws Exception {
        long startTime;
        long endTime;

        connectInitiated = false;

        String url = "http://" + relayHost + ":" + relayPort;
        if (DEBUG) {
        }
        if (QUANTIFY) {
            startTime = System.currentTimeMillis();
        }

        peer = PeerNetwork.createInstance(identity);
        state = peer.connect(url, state);
        if (QUANTIFY) {
            endTime = System.currentTimeMillis();
        }
        connected = true;

        String chatIdentity = isGroupChat ?
                TALKNAME_PREFIX + currentBuddy :
                TALKNAME_PREFIX + identity;

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

        int createId = peer.create(PeerNetwork.PIPE, chatIdentity, pipeId, pipeType);
        while (createId != responseId) {
            poll();
            try {
                Thread.sleep(pollInterval * 1000);
            } catch (Throwable t) {
            }
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
        return true;
    }

    protected boolean poll()
            throws IOException {
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
                msg = peer.poll(pollInterval * 1000);
            }
            if (QUANTIFY) {
                endTime = System.currentTimeMillis();
            }
        }
        catch (IOException ex) {
            //ex.printStackTrace();
            /*
            showAlert("Poll", 
                      "Error polling relay: " + ex.getMessage(),
                      AlertType.ERROR, 
                      DEFAULT_ALERT_TIMEOUT, 
                      initForm);
             */
            return false;
        }

        if (msg == null) {
            return false;
        }

        Element el = null;
        String name = null;
        String id = null;
        for (int i = 0; i < msg.getElementCount(); i++) {
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
            if (name.indexOf(TALKNAME_PREFIX) >= 0)
                name = name.substring(TALKNAME_PREFIX.length());

            int size = buddyList.size();
            for (int i = 0; i < size; i++) {
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
        int blockSize = 0;
        int fileSize = 0;
        int totalBlocks = 0;

        boolean isDisplayable = true;
        for (int i = 0; i < msg.getElementCount(); i++) {
            el = msg.getElement(i);
            if ("requestId".equals(el.getName())) {
                responseId = Integer.parseInt(new String(el.getData()));
            } else if ("JxtaTalkSenderName".equals(el.getName())) {
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
            } else if ("BlockSize".equals(el.getName())) {
                blockSize = Integer.parseInt(new String(el.getData()));
            } else if ("FileSize".equals(el.getName())) {
                fileSize = Integer.parseInt(new String(el.getData()));
            } else if ("TotalBlocks".equals(el.getName())) {
                totalBlocks = Integer.parseInt(new String(el.getData()));
            }
            isDisplayable = true;
        }

        if (imageData != null) {
            String caption =
                    imageCaption == null ? imageFileName : imageCaption;

            gui.setImage(imageData, caption, sender);
            // Save Image in the cache
            imgCache.put(caption, imageData);
            imgBlockSize.put(caption, Integer.toString(blockSize));
            imgFileSize.put(caption, Integer.toString(fileSize));
            imgTotalBlocks.put(caption, Integer.toString(totalBlocks));
        }

        if (sender != null && message != null) {
            if (sender.indexOf(TALKNAME_PREFIX) >= 0) {
                sender = sender.substring(TALKNAME_PREFIX.length());
            }
            replyBuddy = sender;
            String displayedMsg = sender + "> " + message + "\n";

            // keep the last DEFAULT_SCROLL messages, the rest scroll off
            StringItem si = new StringItem(null, displayedMsg);
            /*
            if (initForm.size() >= DEFAULT_SCROLL) {
                initForm.delete(0);
            }
        
            initForm.append(si);
             */

            gui.alert(AlertType.INFO);
        }

        return true;
    }

    private void initiateConnect() {
        if (peer == null) {
            peer = PeerNetwork.createInstance(identity);
        }

        if (connected || connectInitiated) {
            return;
        }

        connectInitiated = true;
    }

    protected boolean sendPicture(Object obj, String msg) {
        if (obj instanceof ImageContainer)        // FILES
            return (sendPictureImage((ImageContainer) obj, msg));
        else                                        // FILES
            return (sendPictureFile((FileConnection) obj, msg));  // FILES
    }

    protected boolean sendPictureImage(ImageContainer ic, String msg) {
        long fSize = 1;
        byte[] sd;

        InputStream is = ic.image.getClass().getResourceAsStream(ic.name);
        DataInputStream ds = new DataInputStream(is);

        try {
            sd = new byte[ds.available()];
            ds.readFully(sd);
            return (send(ic.name, sd, msg));
        }
        catch (Exception ex) {
            gui.showMessage("Picture Open", "Can't read image: " + ex.getMessage(),
                    AlertType.ERROR, DEFAULT_ALERT_TIMEOUT);
            return (false);
        }
    }

    /* FILES */
    protected boolean sendPictureFile(FileConnection pic, String msg) {
        long fSize;
        byte[] sd;
        String sf;

        sf = pic.getName();

        try {
            fSize = pic.fileSize();
            DataInputStream ds = pic.openDataInputStream();
            sd = new byte[(int) fSize];
            ds.readFully(sd);
            return (send(sf, sd, msg));
        }
        catch (Exception ex) {
            gui.showMessage("Picture Open", "Can't open file: " + ex.getMessage(),
                    AlertType.ERROR, DEFAULT_ALERT_TIMEOUT);

            return (false);
        }
    }
    /* FILES */

    protected boolean send(String sendFileName, byte[] sendFileData, String caption) {
        String msg = null;

        if (peer == null || !connected) {
            initiateConnect();
            sendPending = true;
            return false;
        }
        if (sendFileName == null) {
            // Just message
            Element[] elm = new Element[3];

            // Identity
            elm[0] = new Element("JxtaTalkSenderName",
                    identity.getBytes(), null, null);

            // Message
            elm[1] = new Element("JxtaTalkSenderMessage",
                    msg.getBytes(), null, null);

            // for compatibility with myJXTA aka InstantP2P
            // PeerGroup
            elm[2] = new Element("GrpName",
                    "NetPeerGroup".getBytes(), null, null);

            Message m = new Message(elm);

            try {
                String chatBuddy = TALKNAME_PREFIX + currentBuddy;
                String pipeId = (String) buddyIds.get(currentBuddy);
                String pipeType = isGroupChat ? "JxtaPropagate" : "JxtaUnicast";
                peer.send(pipeId, m);
            } catch (Exception ex) {
                gui.showMessage("Send",
                        "Error sending message: " + ex.getMessage(),
                        AlertType.ERROR, Alert.FOREVER);

                return false;
            }
        } else {
            int count = 0;
            Element[] elm = new Element[11];

            // Caption
            if (caption == null)
                caption = sendFileName;
            elm[0] = new Element("Caption",
                    caption.getBytes(), null, null);

            // Filename
            elm[1] = new Element("FileName",
                    sendFileName.getBytes(), null, null);

            // extract Data Block(s)
            elm[2] = new Element("DataBlock",
                    sendFileData, null, null);

            // MessageType
            elm[3] = new Element("MessageType",
                    "FILE".getBytes(), null, null);

            StringBuffer fKey = new StringBuffer(Integer.toString(random.nextInt()));
            fKey.append(sendFileName);

            // FileKey
            elm[4] = new Element("FileKey",
                    (fKey.toString()).getBytes(), null, null);

            //long bSize = fSize;
            long bSize = 12288;
            //long nBlocks = fSize / bSize;

            String blockSize = "" + bSize;
            String fileSize = "" + sendFileData.length;
            //String numBlocks = "" + nBlocks;
            String numBlocks = "1";

            // BlockSize
            elm[5] = new Element("BlockSize",
                    blockSize.getBytes(), null, null);

            // FileSize
            elm[6] = new Element("FileSize",
                    fileSize.getBytes(), null, null);

            // TotalBlocks
            elm[7] = new Element("TotalBlocks",
                    numBlocks.getBytes(), null, null);

            // BlockNumber
            elm[8] = new Element("BlockNum",
                    "0".getBytes(), null, null);

            // SenderID
            elm[9] = new Element("SenderID",
                    state, null, null);

            // BuddyName
            elm[10] = new Element("JxtaTalkSenderName",
                    currentBuddy.getBytes(), null, null);

            Message m = new Message(elm);
            try {
                String chatBuddy = TALKNAME_PREFIX + currentBuddy;
                String pipeId = (String) buddyIds.get(currentBuddy);
                String pipeType = isGroupChat ? "JxtaPropagate" : "JxtaUnicast";
                peer.send(pipeId, m);
            }
            catch (Exception ex) {
                gui.showMessage("Send",
                        "Error sending message: " + ex.getMessage(),
                        AlertType.ERROR, DEFAULT_ALERT_TIMEOUT);

                return (false);
            }

        }
        return (true);
    }
}
