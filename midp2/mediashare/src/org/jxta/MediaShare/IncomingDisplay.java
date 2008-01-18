// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import javax.microedition.lcdui.*;
//import javax.microedition.io.file.*;

public class IncomingDisplay extends PictureDisplay {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private Command storeCommand;

    //
    // Constructors
    //
    public IncomingDisplay(CommandListener listener, String titleStr,
                           BaseMidlet midlet) {
        super(listener, titleStr, midlet);
        storeCommand = new Command("Store", Command.ITEM, 1);
        addCommand(storeCommand);
    }

    public void setSender(String str) {
        sender = str;
    }

    protected void paint(Graphics g) {
        super.paint(g);

        drawStringShadowRight(g, 2, 0, sender);

        return;
    }
}
