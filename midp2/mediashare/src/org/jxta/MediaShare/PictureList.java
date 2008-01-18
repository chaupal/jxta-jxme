// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;        // FILES

public class PictureList extends ListCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private Files mediaFiles;            // FILES
    private Vector pics;

    //
    // Constructors
    //
    public PictureList(CommandListener listener, String titleStr,
                       BaseMidlet midlet) {
        super(listener, titleStr, midlet);
    }

    //
    // take a vector of FileConnections and/or ImagesContainers
    //

    public void setFiles(Vector vec) {
        FileConnection fc;            // FILES
        ImageContainer ic;
        String[] strs = new String[vec.size()];
        Object obj;

        pics = vec;

        for (int idx = 0; idx < pics.size(); ++idx) {
            obj = pics.elementAt(idx);
            if (obj instanceof ImageContainer) {
                ic = (ImageContainer) obj;
                strs[idx] = ic.name;
            }
            /* FILES */
            else {
                fc = (FileConnection) pics.elementAt(idx);
                strs[idx] = fc.getName();
            }
            /* FILES */
        }
        setArray(strs);
    }

    public Object getSelected() {
        return (pics.elementAt(getSelectedIndex()));
    }
}
