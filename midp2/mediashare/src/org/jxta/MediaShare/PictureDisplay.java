// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import java.io.InputStream;
import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;            // FILES

public class PictureDisplay extends BaseBgCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private Command shareCommand;
    private Command printCommand;
    private Image picture;
    protected String caption;
    protected String sender;
    protected String fileName;

    //
    // Constructors
    //
    public PictureDisplay(CommandListener listener, String titleStr,
                          BaseMidlet midlet) {
        super(listener, titleStr, midlet);
        shareCommand = new Command("Share", Command.ITEM, 1);
        printCommand = new Command("Print", Command.ITEM, 2);
        addCommand(shareCommand);
        addCommand(printCommand);
    }

    protected void drawTitle(Graphics g) {
        return;
    }

    public void setImage(Image image) {
        picture = image;
        return;
    }

    public void setCaption(String cap) {
        caption = cap;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public void setPicture(Object obj) {
        if (obj instanceof ImageContainer) {
            ImageContainer ic = (ImageContainer) obj;
            setImage(ic.image);
            setCaption(ic.name);
            setFileName(ic.name);
        }
        /* FILES */
        else {
            FileConnection picFile = (FileConnection) obj;
            try {
                InputStream is = picFile.openInputStream();
                setImage(loadImage(is));
                setCaption(picFile.getName());
                setFileName(picFile.getURL());
                is.close();
            } catch (Exception ex) {
                picture = null;
            }
        }
        /* FILES */
    }

    public void createImage(byte[] data, String caption, String sender) {
        this.caption = caption;
        this.sender = sender;
        try {
            picture = Image.createImage(data, 0, data.length);
            repaint();
        } catch (Exception ex) {
        }
    }

    protected void paint(Graphics g) {
        int x = 0;
        int y = 0;
        int w = 0;
        String str;

        super.paint(g);

        if (picture != null) {
            // center the picture if possible
            if (picture.getWidth() < getWidth())
                x = (getWidth() - picture.getWidth()) / 2;

            if (picture.getHeight() < getHeight())
                y = (getHeight() - picture.getHeight()) / 2;

            g.drawImage(picture, x, y, 0);

            drawStringShadow(g, 2, 0, fileName);
            drawStringShadow(g, 2, getHeight() - 15, caption);
        }

        return;
    }
}
