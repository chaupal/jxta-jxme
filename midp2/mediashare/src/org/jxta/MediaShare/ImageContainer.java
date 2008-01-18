// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import javax.microedition.lcdui.*;

public class ImageContainer {
    //
    // Public Stuff
    //
    public Image image;
    public String name;

    //
    // Private Stuff
    //

    //
    // Constructors
    //

    public ImageContainer(Image image, String name) {
        this.image = image;
        this.name = name;
    }
}
