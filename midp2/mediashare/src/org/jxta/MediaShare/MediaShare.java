// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import java.util.Vector;

import org.jxta.MidletCommon.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

//import javax.microedition.io.file.*;

public class MediaShare extends BaseMidlet implements JxtaGui {
    //
    // Private Stuff
    //
    private static final String DEFAULT_POLL_INTERVAL = "1";
    private Command exitCommand;
    private Command startCommand;
    private Command backCommand;
    private Command configCommand;
    private WelcomeCanvas welcomeScreen;
    private PictureList pictureList;
    private PictureDisplay pictureDisplay;
    private ConnectingForm connectingForm;
    private IncomingAlert incomingAlert;
    private IncomingDisplay incomingDisplay;
    private ShareList shareList;
    private BuddyList buddyList;
    private ConfigForm configForm;
    private JxtaThread jxta;
    private Files mediaFiles;        // FILES
    private Object currentPicture;
    private String relayHost = null;
    private String identity;
    private int relayPort;
    private int pollInterval;
    private Thread jxtaThread;
    private RecordStore recordStore;
    private int recordId;

    //
    // Constructors
    //
    public MediaShare() {
        super();

        readConfig();
    }

    protected void init() {
        super.init();


        mediaFiles = new Files();            // FILES

        exitCommand = new Command("Exit", Command.EXIT, 1);
        startCommand = new Command("Start", Command.ITEM, 1);
        backCommand = new Command("Back", Command.BACK, 2);
        configCommand = new Command("Config", Command.ITEM, 1);

        welcomeScreen = new WelcomeCanvas(this, "Welcome", this);
        welcomeScreen.addCommand(exitCommand);
        welcomeScreen.addCommand(configCommand);

        configForm = new ConfigForm(this, "Configure", this);
        configForm.addCommand(backCommand);

        pictureList = new PictureList(this, "Picture List", this);
        //pictureList = new PictureList(this, "Picture List", this, mediaFiles);
        pictureList.addCommand(backCommand);

        pictureDisplay = new PictureDisplay(this, "Picture Display", this);
        pictureDisplay.addCommand(backCommand);

        incomingDisplay = new IncomingDisplay(this, "Incoming Picture", this);
        incomingDisplay.addCommand(backCommand);

        shareList = new ShareList(this, "Share List", this);
        shareList.addCommand(backCommand);

        buddyList = new BuddyList(this, "Buddy List", this);
        buddyList.addCommand(backCommand);

        connectingForm = new ConnectingForm();

        incomingAlert = new IncomingAlert(this);

        try {
            recordStore = RecordStore.openRecordStore("MediaShare", true);

            readRecordStore();
        }
        catch (Exception ex) {
            showExceptionAlert("Record Store Exception", ex);
        }

        // do this after the user hits 'start''
        //jxtaThread.start();

        return;
    }

    public void startApp()
            throws MIDletStateChangeException {
        super.startApp();       // calls init()

        if (relayHost != null)
            welcomeScreen.addCommand(startCommand);

        setCurrent(welcomeScreen);
    }

    //
    // CommandListener Methods
    //
    public void commandAction(Command command, Displayable displayable) {
        super.commandAction(command, displayable);

        if (command.getCommandType() != Command.EXIT &&
                command.getCommandType() != Command.BACK) {
            try {
                if (displayable instanceof WelcomeCanvas)
                    commandWelcomeScreen(command, displayable);

                else if (displayable instanceof PictureList)
                    commandPictureList(command, displayable);

                else if (displayable instanceof PictureDisplay)
                    commandPictureDisplay(command, displayable);

                else if (displayable instanceof IncomingDisplay)
                    commandIncomingDisplay(command, displayable);

                else if (displayable instanceof ShareList)
                    commandShareList(command, displayable);

                else if (displayable instanceof ShareList)
                    commandShareList(command, displayable);

                else if (displayable instanceof BuddyList)
                    commandBuddyList(command, displayable);

                else if (displayable instanceof IncomingAlert)
                    commandIncomingAlert(command, displayable);

                else if (displayable instanceof ConfigForm)
                    commandConfigForm(command, displayable);

                else {
                    showMessageAlert("Unknown command:" + command);
                }
            } catch (Exception ex) {
                showExceptionAlert("command = " + command.getLabel(), ex);
                ex.printStackTrace();
            }
        }
    }

    //
    // MediaShare Methods
    //
    private void commandWelcomeScreen(Command command, Displayable displayable) {

        String cmd = command.getLabel();
        if (cmd.equals("Start")) {
            jxta = new JxtaThread(identity, relayHost, relayPort, pollInterval, this);
            jxtaThread = new Thread(jxta);
            jxtaThread.start();
            connectingForm.setRelayHost(relayHost);
            display.setCurrent(connectingForm);
        } else if (cmd.equals("Config")) {
            configForm.setData(relayHost, relayPort, identity);
            display.setCurrent(configForm);
        }
    }

    private void commandPictureList(Command command, Displayable displayable) {
        Object obj;
        String cmd = command.getLabel();
        if (cmd.equals("Select")) {
            currentPicture = pictureList.getSelected();

            pictureDisplay.setPicture(currentPicture);
            setCurrent(pictureDisplay);
        }
    }

    private void commandPictureDisplay(Command command, Displayable displayable) {
        String cmd = command.getLabel();
        if (cmd.equals("Share")) {
            shareList.getBuddies(jxta);
            setCurrent(shareList);
        } else if (cmd.equals("Print")) {
            buddyList.setMyTitle("Printers");
            buddyList.getBuddies(jxta, Jxta.PRINTERS);
            setCurrent(buddyList);
        }
    }

    private void commandIncomingDisplay(Command command, Displayable displayable) {
        String cmd = command.getLabel();
        if (cmd.equals("Share"))
            setCurrent(shareList);

        else if (cmd.equals("Print")) {
            buddyList.setMyTitle("Printers");
            buddyList.getBuddies(jxta, Jxta.PRINTERS);
            setCurrent(buddyList);
        }
    }


    private void commandShareList(Command command, Displayable displayable) {
        String cmd = command.getLabel();
        if (cmd.equals("Select")) {
            buddyList.setMyTitle(shareList.getSelectedString());
            buddyList.getBuddies(jxta, shareList.getSelectedIndex());
            setCurrent(buddyList);
        }
    }

    private void commandBuddyList(Command command, Displayable displayable) {
        String cmd = command.getLabel();
        if (cmd.equals("Select")) {
            jxta.sendPicture(currentPicture, null);
            setPreviousScreen(pictureDisplay);
        }
    }

    private void commandConfigForm(Command command, Displayable displayable) {
        String cmd = command.getLabel();
        if (cmd.equals("Save")) {
            relayHost = configForm.getRelayHost();
            relayPort = configForm.getRelayPort();
            identity = configForm.getIdentity();
            saveRecordStore();
            welcomeScreen.addCommand(startCommand);
        }
        restoreCurrent();
    }

    private byte[] incomingData;
    private String incomingCaption;
    private String incomingSender;

    private void commandIncomingAlert(Command command, Displayable displayable) {
        String cmd = command.getLabel();

        if (cmd.equals("Accept")) {
            // we saved 'data', 'caption', and 'sender' before we put up the alert box;
            pictureDisplay.createImage(incomingData, incomingCaption, incomingSender);
            setCurrent(pictureDisplay);
        } else
            restoreCurrent();
    }

    public String getAppPropertyDefault(String key, String defaultValue) {
        String value = getAppProperty(key);
        if (value == null)
            value = defaultValue;

        return (value);
    }

    private void readConfig() {
        String tmp;
        tmp = getAppPropertyDefault("PollInterval", DEFAULT_POLL_INTERVAL);
        pollInterval = Integer.parseInt(tmp);
    }

    //
    // JxtaGui methods
    //
    public void alert(AlertType alert) {
        playSound(alert);
    }

    public void setImage(byte[] data, String caption, String sender) {
        //
        // First pop up an alert.  If the user accepts it, then display the picture.
        //
        incomingDisplay.createImage(data, caption, sender);
        setCurrent(incomingDisplay);
    }

    public void showMessage(String err, String msg, AlertType alert, int duration) {
        showMessageAlert(err + ": " + msg);
    }

    public void connected() {
        try {
            Image img;
            String imgName;

            Vector vec = mediaFiles.getFileInfo();    // FILES
            imgName = "/buspic.jpg";
            img = Image.createImage(imgName);
            vec.addElement(new ImageContainer(img, imgName));

            imgName = "/santa-4.jpg";
            img = Image.createImage(imgName);
            vec.addElement(new ImageContainer(img, imgName));

            pictureList.setFiles(vec);
        } catch (Exception ex) {
        }

        setCurrent(pictureList);
    }

    public String getMessage() {
        return ("0");
    }

    public void setTick(int tick) {
        connectingForm.setTick(tick);
    }

    //////////////////////////////////////////////////////////////////////////
    // Record Store Management
    //
    public void readRecordStore() {
        int rid;
        int num;
        int got = 0;
        String recordStr;
        //
        // We store the RelayHost, RelayPort, and Identity in on record:
        //	host:port:identity
        //
        num = 0;
        try {
            num = recordStore.getNumRecords();
        } catch (Exception e) {
            showExceptionAlert("Problem with getNumRecords()", e);
        }

        try {
            RecordEnumeration em = recordStore.enumerateRecords(null, null, false);
            while (em.hasNextElement()) {
                try {
                    rid = em.nextRecordId();
                    try {
                        recordStr = new String(recordStore.getRecord(rid));
                        // parse the record
                        if (parseRecord(recordStr)) {
                            recordId = rid;
                            return;
                        } else
                            deleteRecord(rid);
                    } catch (Exception e) {
                        showExceptionAlert("Problem reading record", e);
                        deleteRecord(rid);
                    }
                } catch (Exception e) {
                    showExceptionAlert("Problem getting next record id", e);
                }
            }
        }
        catch (Exception e) {
            showExceptionAlert("Problem getting enumeration", e);
        }
    }

    public boolean parseRecord(String str) {
        int idx1;
        int idx2;
        int idx3;
        String str1;
        String str2;
        String str3;

        if ((idx1 = str.indexOf(":")) != -1) {
            str1 = str.substring(0, idx1);
            if ((idx2 = str.indexOf(":", idx1 + 1)) != -1) {
                str2 = str.substring(idx1 + 1, idx2);
                str3 = str.substring(idx2 + 1);
                relayHost = str1;
                relayPort = Integer.parseInt(str2);
                identity = str3;

                return (true);
            }
        }
        return (false);
    }

    private void deleteRecord(int rid) {
        // Don't know what this is - REMOVE IT
        if (rid != 0) {
            try {
                recordStore.deleteRecord(rid);
            }
            catch (Exception e) {
                showExceptionAlert("Problem deleting record", e);
            }
        }
    }

    private int saveRecord(String str) {
        byte[] recordData = null;
        int recordLen = 0;

        recordData = str.getBytes();
        recordLen = recordData.length;

        if (recordId == 0) {
            try {
                recordId = recordStore.addRecord(recordData, 0, recordLen);
            }
            catch (Exception e) {
                showExceptionAlert("Problem adding record [" + str + "]", e);
            }
        } else {
            try {
                recordStore.setRecord(recordId, recordData, 0, recordLen);
            } catch (Exception e) {
                showExceptionAlert("Problem setting record [" +
                        recordId + "][" + str + "]", e);
            }
        }
        return (recordId);
    }

    public void saveRecordStore() {
        String rec = relayHost + ":" + relayPort + ":" + identity;
        recordId = saveRecord(rec);
    }

    //
    // This sould never get called unless the record store gets corrupted
    //
    public void deleteAllRecords() {
        int rid;
        int num;

        num = 0;
        try {
            num = recordStore.getNumRecords();
        }
        catch (Exception e) {
            showExceptionAlert("Problem with getNumRecords()!", e);
        }

        try {
            RecordEnumeration em = recordStore.enumerateRecords(null, null, false);
            while (em.hasNextElement()) {
                try {
                    deleteRecord(em.nextRecordId());
                } catch (Exception e) {
                    showExceptionAlert("Problem getting next record id!", e);
                }
            }
        }
        catch (Exception e) {
            showExceptionAlert("Problem getting enumeration!", e);
        }
    }

    public void dumpAllRecords() {
        int rid;
        int num;
        byte[] recordData;

        num = 0;
        try {
            num = recordStore.getNumRecords();
        } catch (Exception e) {
            showExceptionAlert("Problem with getNumRecords()!", e);
        }

        try {
            RecordEnumeration em = recordStore.enumerateRecords(null, null, false);
            while (em.hasNextElement()) {
                try {
                    rid = em.nextRecordId();
                    try {
                        recordData = (recordStore.getRecord(rid));
                    } catch (Exception e) {
                        showExceptionAlert("Problem reading record!", e);
                        deleteRecord(rid);
                    }
                }
                catch (Exception e) {
                    showExceptionAlert("Problem getting next record id!", e);
                }
            }
        } catch (Exception e) {
            showExceptionAlert("Problem getting enumeration!", e);
        }
    }
}
