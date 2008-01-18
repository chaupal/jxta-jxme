// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import javax.microedition.lcdui.*;

public class WelcomeCanvas extends BaseBgCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private final static String sunLogoStr = "/sunlogo_sm.png";
    private final static String motLogoStr = "/moto.png";
    private static Image sunLogo = null;
    private static Image motLogo = null;
    private String midletTitle;
    private Display display;
    private int linePointer = 0;

    private static final int SunRed = 80;
    private static final int SunGreen = 79;
    private static final int SunBlue = 191;

    private static final int MotRed = 10;
    private static final int MotGreen = 127;
    private static final int MotBlue = 164;

    //
    // Constructors
    //
    public WelcomeCanvas(CommandListener listener, String titleStr,
                         BaseMidlet midlet) {
        super(listener, titleStr, midlet);
        midletTitle = "MediaShare vers " + midlet.getVersion();
        display = midlet.getDisplay();
        loadLogos();
    }

    public synchronized void loadLogos() {
        if (sunLogo == null)
            sunLogo = loadImage(sunLogoStr);

        if (motLogo == null)
            motLogo = loadImage(motLogoStr);

        return;
    }

    protected void drawTitle(Graphics g) {
        return;
    }

    protected void paint(Graphics g) {
        int x = 0;
        int y = 0;
        int w = 0;

        super.paint(g);

        gradient2(g, MotRed, MotGreen, MotBlue, SunRed, SunGreen, SunBlue);

        g.drawImage(motLogo, 0, 0, Graphics.TOP | Graphics.LEFT);

        // BOTTOM
        //g.setColor(0x594fbf);	// Sun BLUE
        y = getHeight() - sunLogo.getHeight();
        //g.fillRect(0, y, getWidth(), sunLogo.getHeight());
        g.drawImage(sunLogo, 0, y, Graphics.TOP | Graphics.LEFT);

        g.setColor(0xdddddd);
        formatText(g, "E. Mike Durbin", sunLogo.getWidth() + 4, y + 1, getWidth() - sunLogo.getWidth() - 2,
                sunLogo.getHeight() - 2, dataFont);

        // MIDDLE
        g.setFont(titleFont);
        g.setColor(titleFont_color);

        w = titleFont.stringWidth(myTitle) + 4;
        x = (getWidth() - w) / 2;
        fgDrawStrings(g, x, getHeight() / 2, w, titleFont.getHeight() + 2,
                titleFont_color, myTitle);

        if (linePointer != 0) {
            g.setColor(0x00ff00);
            g.drawLine(0, linePointer, getWidth(), linePointer);
        }
    }

    private int rgbColor(int red, int green, int blue) {
        return (((red & 0xff) << 16) |
                ((green & 0xff) << 8) |
                (blue & 0xff));
    }

    private void gradient(Graphics g, int sRed, int sGreen, int sBlue, int fRed, int fGreen, int fBlue) {
        int idx;
        int color;
        int height = getHeight();
        int width = getWidth();
        int red = sRed;
        int green = sGreen;
        int blue = sBlue;

        for (idx = 0; idx < height; ++idx) {
            red = sRed + (((fRed - sRed) * idx) / height);
            green = sGreen + (((fGreen - sGreen) * idx) / height);
            blue = sBlue + (((fBlue - sBlue) * idx) / height);

            color = rgbColor(red, green, blue);

            g.setColor(color);
            g.drawLine(0, idx, width, idx);
        }

    }

    private void gradient2(Graphics g, int r1, int g1, int b1, int r2, int g2, int b2) {
        //a linear gradient.
        int height = getHeight();
        int width = getWidth();
        int sub = 1;
        int winc = width / sub;
        int y;
        int x1;
        int x2;
        int total = height * sub;
        int current;

        for (int idx = 0; idx < height; idx++) {
            y = idx;
            for (int cnt = 0; cnt < sub; ++cnt) {
                x1 = cnt * winc;
                x2 = x1 + winc - 1;
                current = (idx * sub) + cnt;

                gline(x1, y, x2, y, current, total,
                        g, r1, g1, b1, r2, g2, b2);
            }
        }
        return;
    }

    private void gline(int x1, int y1, int x2, int y2, int inc, int total, Graphics g,
                       int r1, int g1, int b1, int r2, int g2, int b2) {
        int red = (r2 * inc / total) + r1 - (r1 * inc / total);
        int green = (g2 * inc / total) + g1 - (g1 * inc / total);
        int blue = (b2 * inc / total) + b1 - (b1 * inc / total);
        int color = rgbColor(red, green, blue);

        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
        return;
    }
}