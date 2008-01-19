/*
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: PictureFrameDisplay.java,v 1.5 2003/04/08 08:13:28 jclark Exp $
 *
 */

package net.jxta.picshare;

// Java imports.
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;


/*
 * PictureFrameDisplay: Displays a picture frame image, with another image (the
 *                      picture) nested inside it.
 */
public class PictureFrameDisplay extends JLayeredPane implements ComponentListener {

    // We've got a couple pre-defined frame options, plus the option of no frame.
    // Use these constants with the setFrame() function.
    public final static int FRAME_NONE   = 0;
    public final static int FRAME_GILT   = 1;
    public final static int FRAME_CARVED = 2;
    public final static int FRAME_INLAID = 3;
    public final static int FRAME_MIAMI  = 4;

    // Percent of the picture frame width and height used by the inner display
    // area for the image.
    double picPctW;
    double picPctH;


    SizeableImageDisplay picFrame;
    SizeableImageDisplay picture;


    /** Constructor.  Provide a listener for drag-n-drop files.
     */
    public PictureFrameDisplay(FileDropTargetListener dropListener) {

        // Create the picture frame display.
        picFrame = new SizeableImageDisplay();
        picFrame.setDropTarget(new FileDropTarget(picFrame, dropListener));

        // Create the inner picture display.
        picture = new SizeableImageDisplay();
        picture.scaleDownOnly = true;
        picture.preserveAspectRatio = true;
        picture.setImage(getClass().getResource("startup.jpg"));
        picture.setDropTarget(new FileDropTarget(picture, dropListener));

        setFrame(FRAME_INLAID);

        // We want to listen for resize events.
        addComponentListener(this);

        // Add them to the pane, with the frame on the bottom.
        add(picture,  -1);
        add(picFrame, -1);
    }


    /** Set the frame to one of the predefined frame images.
     *
     *  @Param   frame   FRAME_NONE, FRAME_GILT, or FRAME_CARVED.
     */
    public void setFrame(int frame) {

        if (frame == FRAME_NONE) {
            picPctW = picPctH = 1;
            picFrame.setImage((Image)null);
        }
        else if (frame == FRAME_GILT) {
            picPctW = 0.74371859296482412060301507537688;
            picPctH = 0.69942196531791907514450867052023;
            picFrame.setImage(getClass().getResource("gilt-frame.jpg"));
        }
        else if (frame == FRAME_CARVED) {
            picPctW = 0.83919597989949748743718592964824;
            picPctH = 0.80635838150289017341040462427746;
            picFrame.setImage(getClass().getResource("carved-frame.jpg"));
        }
        else if (frame == FRAME_INLAID) {
            picPctW = 0.75125628140703517587939698492462;
            picPctH = 0.67052023121387283236994219653179;
            picFrame.setImage(getClass().getResource("inlaid-frame.jpg"));
        }
        else if (frame == FRAME_MIAMI) {
            picPctW = 0.72613065326633165829145728643216;
            picPctH = 0.58381502890173410404624277456647;
            picFrame.setImage(getClass().getResource("miami-frame.jpg"));
        }

        componentResized(null);
    }


    public void setImage(Image image) {
        picture.setImage(image);
    }


    /**
     * ComponentListener support.
     *
     * Listen for window size changes.  When the pane is rezized, we need to
     * change the frame and picture sizes to match, and keep the picture
     * centered in the frame.
     */
    public void componentResized(ComponentEvent e) {

        Rectangle paneBounds  = getBounds();
        Rectangle frameBounds = new Rectangle();
        Rectangle picBounds   = new Rectangle();

        // The picture frame should be the same size as this pane.
        frameBounds.width  = paneBounds.width;
        frameBounds.height = paneBounds.height;
        frameBounds.x = 0;
        frameBounds.y = 0;
        picFrame.setBounds(frameBounds);

        // Use our percentages to calculate the new width and height for the
        // picture image, and then center it.
        //
        picBounds.width  = (int)(paneBounds.width  * picPctW) - 2;
        picBounds.height = (int)(paneBounds.height * picPctH) - 2;
        picBounds.x = ((paneBounds.width - picBounds.width) / 2) + 1;
        picBounds.y = ((paneBounds.height - picBounds.height) / 2) - 2;
        picture.setBounds(picBounds);
    }

    /** Not used, but needed for ComponentListener support. */
    public void componentMoved( ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentShown( ComponentEvent e) {}
}