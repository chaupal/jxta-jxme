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
 * $Id: FileDropTarget.java,v 1.3 2002/08/22 08:16:17 jclark Exp $
 *
 */

package net.jxta.picshare;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


/**
 * Simple file drag-n-drop support.  We have one component as the target area,
 * and one listener that will receive notification when a file is dropped into the
 * component.  This version only supports a single file drop.  If multiple files are
 * dropped, the listener will only be told about the first one.  If a directory is
 * dropped, it is ignored.
 */
public class FileDropTarget extends DropTarget {

    public FileDropTarget(Component component, FileDropTargetListener listener) {

        setComponent(component);

        // Add a "middle-man" listener, to catch the drop events, filter out the
        // junk we don't want, and pass the dropped files to the final listener.
        try {
            addDropTargetListener(new FileDropTargetMiddleListener(listener));
        }
        catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }
}


class FileDropTargetMiddleListener implements DropTargetListener {

    FileDropTargetListener listener;


    public FileDropTargetMiddleListener(FileDropTargetListener listener) {

        this.listener = listener;
    }


    public void dragEnter(DropTargetDragEvent e) {
        e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }


    public void dragOver(DropTargetDragEvent e) {
        e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }


    public void dragExit(DropTargetEvent e) {
    }


    public void drop(DropTargetDropEvent e) {

        DropTargetContext targetContext = e.getDropTargetContext();

        boolean outcome = false;

        if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0)
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        else {
            e.rejectDrop();
            return;
        }

        DataFlavor[] dataFlavors = e.getCurrentDataFlavors();
        DataFlavor   transferDataFlavor = null;

        for (int i = 0; i < dataFlavors.length; i++) {
            if (dataFlavors[i].isFlavorJavaFileListType()) {
                transferDataFlavor = dataFlavors[i];
                break;
            }
        }

        if (transferDataFlavor != null) {
            Transferable t  = e.getTransferable();
            java.util.List fileList = null;

            try {
                fileList = (java.util.List)t.getTransferData(transferDataFlavor);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.err.println(ioe.getMessage());
                targetContext.dropComplete(false);
                return;

            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                System.err.println(ufe.getMessage());
                targetContext.dropComplete(false);
                return;
            }

            if (fileList != null) {
                try {
                    File file = (File)fileList.get(0);
                    if (file != null  &&  file.isFile()) {
                        listener.fileDropped(file);
                        outcome = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
                    targetContext.dropComplete(false);
                    return;
                }
            } else
                outcome = false;
        }

        targetContext.dropComplete(outcome);
    }


    public void dragScroll(DropTargetDragEvent e) {
    }


    public void dropActionChanged(DropTargetDragEvent e) {
    }
}
