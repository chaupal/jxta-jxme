// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import javax.microedition.lcdui.*;

public class BuddyList extends ListCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //

    //
    // Constructors
    //

    public BuddyList(CommandListener listener, String titleStr, BaseMidlet midlet) {
        super(listener, titleStr, midlet);
    }

    public void getBuddies(Jxta jxta, int buddyType) {
        String[] buddies = jxta.getBuddies(buddyType);
        setArray(buddies);
    }
}
