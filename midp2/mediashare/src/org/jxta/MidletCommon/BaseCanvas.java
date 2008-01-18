// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MidletCommon;

import java.util.Vector;
import java.io.InputStream;
import javax.microedition.lcdui.*;

public class BaseCanvas extends Canvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    // titleFont
    private final static int titleFont_face = Font.FACE_MONOSPACE;
    private final static int titleFont_style = Font.STYLE_BOLD;
    private final static int titleFont_size = Font.SIZE_LARGE;
    // labelFont
    private final static int labelFont_face = Font.FACE_PROPORTIONAL;
    private final static int labelFont_style = Font.STYLE_BOLD;
    private final static int labelFont_size = Font.SIZE_SMALL;
    // dataFont
    private final static int dataFont_face = Font.FACE_MONOSPACE;
    private final static int dataFont_style = Font.STYLE_PLAIN;
    private final static int dataFont_size = Font.SIZE_SMALL;
    // listFont
    private final static int listFont_face = Font.FACE_MONOSPACE;
    private final static int listFont_style = Font.STYLE_BOLD;
    private final static int listFont_size = Font.SIZE_LARGE;

    //
    // Protected Stuff
    //
    protected BaseMidlet midlet;            // needed to get height
    protected final static int titleFont_color = 0xff0000;
    protected final static int labelFont_color = 0xf9ed2e;
    protected final static int dataFont_color = 0xffffff;
    protected final static int listFont_color = 0xffffff;
    protected int dataOffset = 0;
    protected int line2height;

    protected static Font titleFont = null;
    protected static Font labelFont = null;
    protected static Font dataFont = null;
    protected static Font listFont = null;
    protected String myTitle;

    //
    // Constructors
    //
    public BaseCanvas(CommandListener listener, String titleStr, BaseMidlet midlet) {
        this(listener, titleStr, midlet, 0);
    }

    public BaseCanvas(CommandListener listener, String titleStr, BaseMidlet midlet,
                      int offset) {
        setCommandListener(listener);
        loadFonts();
        line2height = titleFont.getHeight();
        midlet = midlet;
        myTitle = titleStr;
        dataOffset = offset;
    }

    //
    // BaseCanvas Set Methods
    //
    public void setMyTitle(String str) {
        myTitle = str;
        return;
    }

    public String getMyTitle() {
        return (myTitle);
    }

    public static Image loadImage(InputStream is) {
        Image ret = null;
        try {
            ret = Image.createImage(is);
        } catch (java.io.IOException e) {
        }
        return (ret);
    }

    public static Image loadImage(String location) {
        Image ret = null;
        try {
            ret = Image.createImage(location);
        } catch (java.io.IOException e) {
        }
        return (ret);
    }

    //
    // BaseCanvas Methods
    //
    public synchronized void loadFonts() {
        if (titleFont == null)
            titleFont = Font.getFont(titleFont_face, titleFont_style,
                    titleFont_size);

        if (labelFont == null)
            labelFont = Font.getFont(labelFont_face, labelFont_style,
                    labelFont_size);

        if (dataFont == null)
            dataFont = Font.getFont(dataFont_face, dataFont_style,
                    dataFont_size);

        if (listFont == null)
            listFont = Font.getFont(listFont_face, listFont_style,
                    listFont_size);
    }

    public void clearAndDrawStrings(Graphics g, int x, int y, int w,
                                    int h, int fg, int bg, String str1, String str2) {
        clear(g, bg, x, y, w, h);
        if (str1 != null) {
            g.setColor(fg);
            g.drawString(str1, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void drawStrings(Graphics g, int x, int y, int w,
                            int h, int fg, int bg, String str1, String str2) {
        if (str1 != null) {
            g.setColor(fg);
            g.drawString(str1, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void drawStringShadow(Graphics g, int x, int y, String str) {
        if (str != null) {
            g.setColor(0x00ffffff);   // white
            g.drawString(str, x + 1, y + 1, Graphics.TOP | Graphics.LEFT);

            g.setColor(0x00000000);   // black
            g.drawString(str, x, y, Graphics.TOP | Graphics.LEFT);
        }
        return;
    }

    public void drawStringShadowRight(Graphics g, int x, int y, String str) {
        int width;
        if (str != null) {
            width = g.getFont().stringWidth(str);
            drawStringShadow(g, getWidth() - width - x, y, str);
        }
    }

    public void drawStringsRight(Graphics g, int x, int y, int w,
                                 int h, int fg, int bg, String str1) {
        int width;
        if (str1 != null) {
            width = g.getFont().stringWidth(str1);
            g.setColor(fg);
            g.drawString(str1, x + w - width, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    protected void drawForeground(Graphics g) {
    }

    protected void drawTitle(Graphics g) {
        if (myTitle != null) {
            g.setColor(titleFont_color);
            g.setFont(titleFont);
            g.drawString(myTitle, dataOffset, 0, Graphics.TOP | Graphics.LEFT);
        }
    }

    //
    // Draw the background image
    //
    protected void paint(Graphics g) {
        drawBackground(g);
        drawForeground(g);
        drawTitle(g);
    }

    protected void drawBackground(Graphics g) {
        clear(g, 0xffffff);
    }

    protected void clear(Graphics g, int bgcolor) {
        clear(g, bgcolor, 0, 0, getWidth(), getHeight());
    }

    protected void clear(Graphics g, int color, int x1, int y1, int w, int h) {
        g.setColor(color);
        g.fillRect(x1, y1, w, h);
    }

    public int getHeight() {
        int ret;

        if (midlet != null)
            ret = super.getHeight() - midlet.getCommandHeight();
        else
            ret = super.getHeight();

        return (ret);
    }

    public void drawStatus(Graphics g, int count) {
        int half = getHeight() / 2;

        g.setFont(Font.getDefaultFont());
        g.setColor(0xff0000);
        g.drawString("" + count, count * 8, half, Graphics.TOP | Graphics.LEFT);
    }

    //
    // There are two ways to format the text:
    //
    // 1) Do a word at a time until we hit the edge.
    //
    // 2) Do a binary search on the string to fit, then do a binary search
    //    on the remainder.
    //
    // I'll do 1) for sentences with white space, then back off to 2) 
    //
    public void formatText(Graphics g, String text, int x, int y, int width,
                           int height, Font font) {
        int cnt = 0;
        int offset = 0;
        int nlIdx = 0;
        int yIdx = 0;
        String line;
        //
        // Loop through the "lines"
        //
        for (offset = 0; nlIdx != -1 && offset < text.length();
             offset = nlIdx + 1) {
            if ((nlIdx = text.indexOf('\n', offset)) == -1)
                line = text.substring(offset);
            else
                line = text.substring(offset, nlIdx);

            ++cnt;
            yIdx += formatLine(g, line, x, y + yIdx, width, height - yIdx, font);
        }
    }

    //
    // Word at a time
    // returns lines used
    //
    public static int formatLine(Graphics g, String line, int x, int y,
                                 int width, int height, Font font) {
        int cnt;
        int offset;
        int wIdx;
        int xIdx;
        int yIdx;
        int len;
        int outLen;
        int lineHeight = font.getHeight();
        String word;
        String outWord;
        //
        // Loop through the "lines"
        //
        cnt = 0;
        wIdx = 0;
        xIdx = 0;
        yIdx = 0;
        if (lineHeight > height)
            return (0);

        for (offset = 0; wIdx != -1 && offset < line.length(); offset = wIdx + 1) {
            if ((wIdx = line.indexOf(' ', offset)) == -1)
                word = line.substring(offset);
            else
                word = line.substring(offset, wIdx);

            ++cnt;

            len = font.stringWidth(word);
            if (xIdx == 0) {
                outWord = word;
                outLen = len;
            } else {
                outWord = " " + word;
                outLen = font.stringWidth(outWord);
            }

            if (xIdx + outLen < width) {
                g.drawString(outWord, x + xIdx, y + yIdx, Graphics.TOP | Graphics.LEFT);
                xIdx += outLen;
            } else {
                yIdx += lineHeight;
                if (yIdx + lineHeight > height)
                    return (yIdx);
                g.drawString(word, x, y + yIdx, Graphics.TOP | Graphics.LEFT);
                xIdx = len;
            }
        }
        return (yIdx);
    }

    //
    // There are two ways to format the text:
    //
    // 1) Do a word at a time until we hit the edge.
    //
    // 2) Do a binary search on the string to fit, then do a binary search
    //    on the remainder.
    //
    // I'll do 1) for sentences with white space, then back off to 2) 
    //
    // take an input string and convert it to a list of strings - one per
    // line.  Newlines in the input string creates a "paragraph" - a
    // empty line.
    //
    public Vector formatTextStrings(String inputText, int x, int y, int width,
                                    Font font) {
        int offset = 0;
        int nlIdx = 0;
        int yIdx = 0;
        String line;
        Vector vec = new Vector();
        //
        // Loop through the "lines"
        //
        String text = inputText.replace('^', '\n');
        text.trim();
        for (offset = 0; nlIdx != -1 && offset < text.length();
             offset = nlIdx + 1) {
            if ((nlIdx = text.indexOf('\n', offset)) == -1)
                line = text.substring(offset);
            else
                line = text.substring(offset, nlIdx);

            yIdx += formatLineStrings(line, x, y + yIdx, width, font, vec);
            vec.addElement("");
        }
        return (vec);
    }

    //
    // Word at a time
    // returns lines used
    //
    public static int formatLineStrings(String line, int x, int y, int width,
                                        Font font, Vector vec) {
        int cnt;
        int offset;
        int wIdx;
        int xIdx;
        int yIdx;
        int len;
        int outLen;
        int lineHeight = font.getHeight();
        String word;
        String outWord;
        String currentLine = null;
        //
        // Loop through the "lines"
        //
        cnt = 0;
        wIdx = 0;
        xIdx = 0;
        yIdx = 0;
        for (offset = 0; wIdx != -1 && offset < line.length(); offset = wIdx + 1) {
            if ((wIdx = line.indexOf(' ', offset)) == -1)
                word = line.substring(offset);
            else
                word = line.substring(offset, wIdx);

            ++cnt;
            len = font.stringWidth(word);
            if (xIdx == 0) {
                if (currentLine != null)
                    vec.addElement(currentLine);

                currentLine = new String();

                outWord = word;
                outLen = len;
            } else {
                outWord = " " + word;
                outLen = font.stringWidth(outWord);
            }


            if (xIdx + outLen < width) {
                currentLine = currentLine + outWord;
                xIdx += outLen;
            } else {
                yIdx += lineHeight;

                if (currentLine != null)
                    vec.addElement(currentLine);

                currentLine = new String();

                currentLine = currentLine + word;
                xIdx = len;
            }
        }
        if (currentLine != null && currentLine.length() > 0)
            vec.addElement(currentLine);

        return (yIdx);
    }

    //
    //
    public int formatTextRegion(Graphics g, Vector text, int x, int y,
                                int width, int height, Font font, int offset) {
        int lineHeight = font.getHeight();
        int idx;
        int yIdx = 0;

        for (idx = offset; idx < text.size() && yIdx < height; ++idx, yIdx += lineHeight) {
            g.drawString((String) text.elementAt(idx), x, y + yIdx, Graphics.TOP | Graphics.LEFT);
        }
        return (idx);
    }
}
