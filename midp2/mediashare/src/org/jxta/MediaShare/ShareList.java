// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import javax.microedition.lcdui.*;

public class ShareList extends ListCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //

    //
    // Constructors
    //

    public ShareList(CommandListener listener, String titleStr, BaseMidlet midlet) {
        super(listener, titleStr, midlet);
    }

    public void getBuddies(Jxta jxta) {
        String[] buddyTypes = jxta.getBuddyTypes();
        setArray(buddyTypes);
    }
}
