// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import javax.microedition.lcdui.*;

public class IncomingAlert extends Alert {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private Command acceptCommand;
    private Command rejectCommand;

    //
    // Constructors
    //
    public IncomingAlert(CommandListener listener) {
        super("Incoming Picture");
        setCommandListener(listener);
        acceptCommand = new Command("Accept", Command.ITEM, 1);
        rejectCommand = new Command("Reject", Command.ITEM, 2);
        addCommand(acceptCommand);
        addCommand(rejectCommand);
    }

    public void setInfo(String caption, String sender) {
        setString(sender + " is trying to send you a picture called: " + caption);
        return;
    }
}
