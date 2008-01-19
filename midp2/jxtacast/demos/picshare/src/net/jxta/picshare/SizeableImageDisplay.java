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
 * $Id: SizeableImageDisplay.java,v 1.3 2002/08/22 08:16:20 jclark Exp $
 *
 */

package net.jxta.picshare;

// Java imports.
import java.awt.*;
import java.awt.event.*;
import java.net.*;


/*
 * SizeableImageDisplay: Displays an image scaled to fit within the current size of
 *                       the component.  Re-scales the image if the component size
 *                       changes.
 *
 *                       Optional behavior:
 *
 *                           Preserve the width-to-height aspect ratio of the image.
 *
 *                           Scale down only - if component is larger than the
 *                           original image size we just center the image.
 */
public class SizeableImageDisplay extends Canvas {

    // Options - They'll take effect on the next paint().
    public boolean preserveAspectRatio;
    public boolean scaleDownOnly;

    protected Image   image;
    protected boolean imgPrepped;


    public SizeableImageDisplay() {

        image = null;
        preserveAspectRatio = false;
        scaleDownOnly = false;

        setBackground(Color.white);
    }


    public void setImage(URL url) {
        setImage(Toolkit.getDefaultToolkit().getImage(url));
    }


    public void setImage(Image newImage) {

        image = newImage;
        imgPrepped = false;

        // If image is null, force a repaint and bail.
        if (image == null) {
            repaint();
            return;
        }

        // If the image is already loaded, force a paint.
        if (image.getWidth(this) != -1) {
            Rectangle r = calcImageRect();
            prepareImage(this.image, r.width, r.height, this);
            imgPrepped = true;
            repaint();
            return;
        }

        // The image hasn't finished loading yet...
        // If we're not worried about the aspect ratio or scale limits,
        // we can go ahead and prep the image for drawing at the new size.
        // Otherwise, we can't do that until we know the original image size.
        // The imageUpdate() func will force a repaint when the image is done
        // loading.
        //
        if (!preserveAspectRatio  &&  !scaleDownOnly) {
            prepareImage(image, getSize().width, getSize().height, this);
            imgPrepped = true;
        }
    }


    public void paint(Graphics g) {

        // If we don't have the image yet, just blank out the display.
        if (image == null  ||  image.getWidth(this) == -1) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getSize().width, getSize().height);
            return;
        }

        Rectangle r = calcImageRect();
        g.drawImage(image, r.x, r.y, r.width, r.height, getBackground(), this);
    }


    public boolean imageUpdate(Image image, int flags, int x, int y, int w, int h) {

        // Make sure we're dealing with the current image.
        if (this.image != image)
            return true;
        
        // Prepare a scaled image as soon as we know the original dimensions.
        if (!imgPrepped  &&  (flags & WIDTH) != 0  &&  (flags & HEIGHT) != 0) {
            Rectangle r = calcImageRect();
            prepareImage(this.image, r.width, r.height, this);
            imgPrepped = true;
        }

        // Trigger a repaint when an image has completed loading.
        if ((flags & ALLBITS) != 0)
            repaint();

        return true;
    }


    /**
     * Calculate the desired image dimensions and location, based on the original
     * image size, the current component size, and the selected options.
     */
    protected Rectangle calcImageRect() {

        // Original image size.
        int origW = image.getWidth(this);
        int origH = image.getHeight(this);

        // Current component size.
        int cw = getSize().width;
        int ch = getSize().height;

        Rectangle rect = new Rectangle();

        // First set the dimensions to the current size of the component, unless
        // we're in "scale down only" mode and the dimension in already smaller than
        // the component.
        rect.width  = (scaleDownOnly  &&  cw > origW) ? origW : cw;
        rect.height = (scaleDownOnly  &&  ch > origH) ? origH : ch;

        // If we want to preserve the aspect ratio, adjust now.
        if (preserveAspectRatio) {
        
            float origRat = (float)origW / origH;
            float currRat = (float)rect.width / rect.height;
        
            if (origRat != currRat) {

                // Adjust height to maintain the width.  If that adjustment makes
                // the new height taller than the component, adjust the width
                // instead.
                //
                int newHeight = (int)(rect.width / origRat);
                if (newHeight > ch)
                    rect.width = (int)(rect.height * origRat);
                else
                    rect.height = newHeight;
            }
        }

        // If any dimensions are now smaller than the current component size,
        // center the image location.
        if (rect.width < cw)
            rect.x = (cw - rect.width) / 2;
        if (rect.height < ch)
            rect.y = (ch - rect.height) / 2;

        return rect;
    }
}
