// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MidletCommon;

import javax.microedition.lcdui.*;

public class BaseBgCanvas extends BaseCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private final static String defaultImageFilename1 = "/b1.png";
    private final static String defaultImageFilename2 = "/b1_fg.png";
    private String bgImageFile;
    private String fgImageFile;


    //
    // Protected Stuff
    //
    protected Image background = null;
    protected Image foreground = null;
    protected int Yoff;
    protected int Xoff;

    //
    // Constructors
    //
    public BaseBgCanvas(CommandListener listener, String titleStr,
                        BaseMidlet midlet) {
        super(listener, titleStr, midlet);
        init(defaultImageFilename1);
    }

    public BaseBgCanvas(CommandListener listener, String titleStr,
                        String imageFile, BaseMidlet midlet) {
        super(listener, titleStr, midlet);
        init(imageFile);
    }

    public BaseBgCanvas(CommandListener listener, String titleStr,
                        BaseMidlet midlet, int off) {
        super(listener, titleStr, midlet, off);
        init(defaultImageFilename1);
    }

    public BaseBgCanvas(CommandListener listener, String titleStr,
                        String imageFile, BaseMidlet midlet, int off) {
        super(listener, titleStr, midlet, off);
        init(imageFile);
    }

    public void init(String file) {
        bgImageFile = file;
        fgImageFile = defaultImageFilename2;
        loadImages();
        Yoff = 0;
        Xoff = 0;
    }

    //
    // BaseBgCanvas Methods
    //
    public synchronized void loadImages() {
        if (background == null)
            background = loadImage(bgImageFile);

        if (foreground == null)
            foreground = loadImage(fgImageFile);
    }

    //
    // X and Y offset must allways be negative or zero
    //
    protected void drawBackground(Graphics g) {
        drawTiledImage(g, background);
    }

    protected void drawForeground(Graphics g, int x, int y, int w, int h) {
        g.setClip(x, y, w, h);
        drawTiledImage(g, foreground);
        g.setClip(0, 0, getWidth(), getHeight());
    }

    public void fgDrawStrings(Graphics g, int x, int y, int w,
                              int h, int fg, String str1) {
        drawForeground(g, x, y, w, h);
        drawStrings(g, x + 1, y + 1, w, h, fg, 0, str1, null);
    }

    protected void drawTiledImage(Graphics g, Image img) {
        int idxY;
        int idxX;

        //
        // Draw the img only if it's visable
        //

        //if ((-Xoff) < img.getWidth())
        for (idxX = -Xoff; idxX < getWidth(); idxX += img.getWidth())
            for (idxY = 0; idxY < getHeight(); idxY += img.getHeight())
                g.drawImage(img, idxX, idxY, Graphics.TOP | Graphics.LEFT);
    }
}
