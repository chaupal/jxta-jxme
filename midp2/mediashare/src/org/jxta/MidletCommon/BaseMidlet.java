// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MidletCommon;

import java.util.Stack;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class BaseMidlet extends MIDlet implements CommandListener {
    //
    // Public Stuff
    //
    public int displayCmdHeight;

    //
    // Private Stuff
    //
    private Stack displayStack;
    //
    // Protected Stuff
    //
    protected Display display;
    protected Displayable currentScreen;
    //protected Displayable		previousScreen;
    protected Alert messageAlert;
    protected Alert exceptionAlert;
    protected String version;

    //
    // Screens
    //

    //
    // Constructors
    //

    public BaseMidlet() {
        String str;

        displayStack = new Stack();
        displayCmdHeight = 0;

        if ((str = getAppProperty("APPcmdHeight")) != null)
            displayCmdHeight = Integer.parseInt(str);

        if ((version = getAppProperty("MIDlet-Version")) == null)
            version = "unknown";

        messageAlert = new Alert("Message", null, null, AlertType.INFO);
        messageAlert.setTimeout(Alert.FOREVER);

        exceptionAlert = new Alert("Exception", null, null, AlertType.INFO);
        exceptionAlert.setTimeout(Alert.FOREVER);
    }

    public int getCommandHeight() {
        return (displayCmdHeight);
    }

    public String getVersion() {
        return (version);
    }

    public void startApp()
            throws MIDletStateChangeException {
        init();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

    //
    // CommandListener Methods
    //
    public void commandAction(Command command, Displayable displayable) {
        //
        // These are commands common amoung all screens
        //
        if (command.getCommandType() == Command.EXIT) {
            try {
                destroyApp(false);
                notifyDestroyed();
            }
            catch (Exception e) {
                showExceptionAlert("Exit", e);
            }
        }
        if (command.getCommandType() == Command.BACK)
            setPreviousScreen();

    }

    //////////////////////////////////////////////////////////////////////////
    //
    protected void init() {
        display = Display.getDisplay(this);
    }

    public Display getDisplay() {
        return (display);
    }

    public void setCurrent(Displayable disp) {
        //
        // Some screens are transient (alerts).
        //
        if (disp instanceof Alert == false) {
            displayStack.push(currentScreen);
            //previousScreen = currentScreen;
            currentScreen = disp;
        }

        display.setCurrent(disp);
    }

    public void restoreCurrent() {
        display.setCurrent(currentScreen);
    }

    public void setPreviousScreen() {
        if (displayStack.isEmpty() == false)
            currentScreen = (Displayable) displayStack.pop();

        display.setCurrent(currentScreen);
    }

    public void setPreviousScreen(Displayable screen) {
        Displayable tmpScreen;

        while (displayStack.isEmpty() == false) {
            tmpScreen = (Displayable) displayStack.pop();
            if (tmpScreen == screen) {
                currentScreen = tmpScreen;
                display.setCurrent(currentScreen);
                break;
            }
        }
    }

    public void showMessageAlert(String msg) {
        messageAlert.setString(msg);
        setCurrent(messageAlert);
    }

    public void showExceptionAlert(String msg, Exception e) {
        exceptionAlert.setString(msg + " (" + e + ")");
        setCurrent(exceptionAlert);
    }

    public void playSound(AlertType alert) {
        alert.playSound(display);
    }
}
