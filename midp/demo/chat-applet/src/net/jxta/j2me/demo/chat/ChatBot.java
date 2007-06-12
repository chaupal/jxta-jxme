package net.jxta.j2me.demo.chat;

import java.io.IOException;
import java.util.Calendar;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

public class ChatBot implements Runnable{

    public static final boolean DEBUG = false;
    public static final boolean WARN = true;

    public static final int CURRENT_DATA_VERSION = 0;


    static final String[] CANNED_PHRASES = { "Hello! Everyone", "Its fun to chat!", 
                                             "Where are you?", "I'm at Work.", 
                                             "I love chatting with you all", "What time?", 
                                             "Where?",  
                                             "Anyone from cyberland?", "Send me your pics..." };
    static final String[] CANNED_MESSAGES = { "Welcome aboard!", "I am proud to be with you!", 
                                             "Want to have a party?", "Enjoy trip down the memory lane.", 
                                             "I love chatting with you all", "What time?", 
                                             "Where?", "I am enjoying your company", 
                                             "This is very exciting!", "Send your photograph..." };
    static int numberPhrases;


    static final Calendar cal = Calendar.getInstance();

    public static final String CHAT_USERNAME_PREFIX = "JxtaTalkUserName.";

    public static final String PIPE_TYPE_PROPAGATE = "JxtaPropagate";

    static final String ID_PREFIX       = "urn:jxta:uuid-";
    static final String NET_GROUP_ID    = "59616261646162614E50472050325033";

    static final String PIPE_ID_SUFFIX  = "04";

    static final String GROUP_NAME = "IP2PGRP";
    static final String GROUP_ID = "D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1D1";

    // default parameters
    private String relay   = "http://192.18.37.36:9700";
    private String bgColor = "white";
    private String fgColor = "black";
    private String groupName  = GROUP_NAME; // 
    private String groupID = GROUP_ID;
    private int    sleepTime = 2000;   // 5 secs
    private int    interval  = 2000*60; // 2 minutes
    private String trigger = "Funny";
 
    private String completeGroupID = ID_PREFIX + NET_GROUP_ID + GROUP_ID + 
                                     PIPE_ID_SUFFIX;

    private PeerNetwork peer;
    private String userName = "ChatBot";
    private String peerId = "";
    private Thread thread = null;

    private boolean connected = false;
    private boolean connectInitiated = false;

    public ChatBot (String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-interval") &&
                i+1 < args.length) {
                String intervalStr = args[++i];
                try {
                    interval = Integer.parseInt(intervalStr);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid value: " + intervalStr);
                }
            } else if (args[i].equalsIgnoreCase("-relay") &&
                i+1 < args.length) {
                relay = args[++i];
            } else if (args[i].equalsIgnoreCase("-group") &&
                i+1 < args.length) {
                groupName = args[++i];
            } else if (args[i].equalsIgnoreCase("-groupid") &&
                i+1 < args.length) {
                groupID = args[++i];
                setGroupID(groupID);
            } else if (args[i].equalsIgnoreCase("-name") &&
                i+1 < args.length) {
                userName = args[++i];
            } else if (args[i].equalsIgnoreCase("-trigger") &&
                i+1 < args.length) {
                trigger = args[++i];
            }  else {
                System.out.println("Error parsing arguments");
                return;
            }
        }
        numberPhrases = CANNED_PHRASES.length;
        initiateConnect();
        poll();
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

    public void poll() {
        boolean notAnnounced = true;
        startPolling();
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
                    System.out.println("sleep for " + sleepTime/1000 + " seconds");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    if (WARN) {
                        System.err.println("InterruptedException in polling thread");
                    }
                }
            } else {
                handleMessage(m);
            }
            if (notAnnounced) {
                try {
                    System.out.println ("Announcing joining");
	            send (userName + " has joined ***");
                    notAnnounced = false;
                } catch (Throwable t) {
                    t.printStackTrace ();
                }
            }
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
            log(sender + "> " + message + "  <" + cal.getTime() + ">");
            parseMessage (sender, message);
        } 
    }

    private void initiateConnect() {

        if (peer == null) {
            peer = PeerNetwork.createInstance(userName);
        }

        if (connected || connectInitiated) {
            return;
        }

        connectInitiated = true;
    }

    private boolean connect() throws Throwable {
        connectInitiated = false;
        String url = getRelayURL();
       
        if (DEBUG) {
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
        }

        return true;
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
    }

    private synchronized void log(String message) {
        System.out.println (message);
    }
    
    static int nextMsg = 0;

    private void parseMessage (String sender, String message) {
	String chat = message.toLowerCase();
        try {
	    if (chat.indexOf ("joined ***") >= 0) {
		send (sender + ", welcome to " + getGroupName() + " chat world");
            } else if ( chat.indexOf (trigger) >= 0) {
    		send (CANNED_MESSAGES[nextMsg++]);
		if (nextMsg == CANNED_MESSAGES.length)
		    nextMsg = 0;
            }
        } catch (Throwable t) {}
    }
        

    public static void main(String[] args) {

        ChatBot bot = new ChatBot(args);
    }

   private void startPolling() {
        thread = new Thread(this);
        thread.start();
    }


    public void run() {
        int next = 0;
        while (peer != null) {
            try {
                Thread.sleep(interval);
            } catch(InterruptedException e) {
                if (WARN) {
                     System.err.println("InterruptedException in polling thread");
		}
            }
            try {
                send (CANNED_PHRASES[next++]);
            } catch (Throwable t) {
                 t.printStackTrace ();
            }
            if (next == numberPhrases) {
		next = 0;
            }
	}
    }
}
