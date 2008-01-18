// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MidletCommon;

import java.util.Vector;
import javax.microedition.lcdui.*;

public class ListCanvas extends BaseBgCanvas {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private CommandListener cmdListener;
    private Command selectCommand;
    private String[] stringArray;

    private int maxLines;
    private int lineHeight;
    private int screenLinePointer;
    private int offsetIntoArray;

    //
    // Lines:
    //  SCREEN					LIST
    //	0   = up arrow (if needed)		n/a
    //	1   = first line			start + n-1
    //	n   = last line (stringArray.length)	start + n-1
    //  n+1 = down arrow (if needed)		n/a
    //
    // Constructors
    //
    public ListCanvas(CommandListener listener, String titleStr,
                      BaseMidlet midlet) {
        super(listener, titleStr, midlet, 15);

        selectCommand = new Command("Select", Command.OK, 2);
        addCommand(selectCommand);

        // font stuff
        lineHeight = listFont.getHeight();
        maxLines = (getHeight() - line2height) / lineHeight;
    }

    public void setCommandListener(CommandListener l) {
        super.setCommandListener(l);
        cmdListener = l;
    }

    public void setArray(Vector vec) {
        String[] strs = new String[vec.size()];
        vec.copyInto(strs);
        setArray(strs);
    }

    public void setArray(String[] strings) {
        offsetIntoArray = 0;
        screenLinePointer = 0;
        stringArray = strings;
        repaint();
    }

    public int getSelectedIndex() {
        return (offsetIntoArray + screenLinePointer);
    }

    public String getSelectedString() {
        return (stringArray[offsetIntoArray + screenLinePointer]);
    }

    public String[] getArray() {
        return (stringArray);
    }

    protected void drawForeground(Graphics g) {
    }

    protected void paint(Graphics g) {
        super.paint(g);

        g.setFont(listFont);
        for (int idx = 0; (offsetIntoArray + idx) < stringArray.length && idx < maxLines; ++idx)
            drawChoice(g, stringArray[offsetIntoArray + idx],
                    null, listFont_color, idx);

    }

    private void drawChoice(Graphics g, String str1, String str2, int fgcolor,
                            int line) {
        int y = (line * lineHeight) + line2height;

        if (line == screenLinePointer) {
            drawForeground(g, dataOffset - 2, y, getWidth(), lineHeight);
            drawStrings(g, dataOffset, y, getWidth(), lineHeight,
                    fgcolor, 0xffffff, str1, str2);
            //clearAndDrawStrings(g, dataOffset, y, getWidth(), lineHeight,
            //	0xffffff, fgcolor, str1, str2);
        } else
            drawStrings(g, dataOffset, y, getWidth(), lineHeight,
                    fgcolor, 0xffffff, str1, str2);
    }

    public void keyPressed(int keyCode) {
        switch (getGameAction(keyCode)) {
            case Canvas.LEFT:
                repaint();
                break;

            case Canvas.UP:
                if (offsetIntoArray + screenLinePointer > 0) {
                    if (screenLinePointer > 0)
                        --screenLinePointer;
                    else
                        --offsetIntoArray;

                    repaint();
                }
                break;

            case Canvas.DOWN:
                if (offsetIntoArray + screenLinePointer < stringArray.length - 1) {
                    if (screenLinePointer < maxLines - 1)
                        ++screenLinePointer;
                    else
                        ++offsetIntoArray;

                    repaint();
                }
                break;

            case Canvas.FIRE:
                cmdListener.commandAction(selectCommand, this);
                break;

            default:
                break;
        }
    }

    public void pointerReleased(int x, int y) {
        //
        // find the line that this x,y pair matches, and highlight it
        // (make it the current line)
        //
        int newLineNo = -1;
        int lineNo = y / lineHeight;

        if (lineNo > 0 && lineNo <= stringArray.length)
            newLineNo = lineNo;

        if (newLineNo != screenLinePointer) {
            screenLinePointer = newLineNo;
            repaint();
        }
    }
}
