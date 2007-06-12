/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
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
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: Chat.java,v 1.7 2002/09/21 01:07:47 kuldeep Exp $
 *
 */

package net.jxta.j2me.demo.chat;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Chat extends WindowAdapter {
    public static final boolean DEBUG = false;
    public static final boolean WARN = true;

    public static final int CURRENT_DATA_VERSION = 0;
    public static final String DEFAULT_FILENAME = "chat.ser";

    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 200;

    private File preferenceFile;
    private Rectangle bounds = new Rectangle();

    private Frame frame;
    private ChatPanel chat;

    private String peerId;

    public Chat(String filename) {
        // create the chat frame and initialize it
        frame = new Frame("Chat");
        frame.setBackground (Color.white);
        frame.addWindowListener(this);

        frame.setLayout(new GridLayout(1, 1));

        if (filename != null) {
            preferenceFile = new File(filename);
        }

        if (preferenceFile == null) {
            preferenceFile = new File(DEFAULT_FILENAME);
        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        // check for saved preferences
        try {
            chat = load(new DataInputStream(new FileInputStream(preferenceFile)));
        } catch (IOException e) {
            // create a new chat widget
            chat = new ChatPanel();

            bounds = new Rectangle((screen.width - DEFAULT_WIDTH) / 2, 
                                   (screen.height - DEFAULT_HEIGHT) / 2, 
                                   DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        if (bounds.width > screen.width) {
            bounds.width = screen.width;
        }
        if (bounds.height > screen.height) {
            bounds.height = screen.height;
        }

        // add the chatWidget and initialize the frame
        frame.add(chat);
        frame.pack();

        frame.setBounds(bounds);
        frame.setVisible(true);
    }

    public void windowClosing(WindowEvent evt) {
        Window window = evt.getWindow();
        if (window == frame) {
            try {
                save(new DataOutputStream(new FileOutputStream(preferenceFile)));
            } catch (IOException e) {
                if (WARN) System.err.println("could not save preference file");
            }
        }

        // close the frame and exit
        window.setVisible(false);
        window.dispose();
        System.exit(0);
    }

    private ChatPanel load(DataInputStream in) throws IOException {
        // first int is the version
        int version = in.readInt();

        // make sure the version is compatiable
        if (version != CURRENT_DATA_VERSION) {
            throw new IOException("persistent data version is not compatable");
        }

        // load preference bounds
        bounds = new Rectangle(in.readInt(), 
                               in.readInt(), 
                               in.readInt(), 
                               in.readInt());

        // load the relay Url
        String relay = in.readUTF();

        // load the peerId
        String peerId = in.readUTF();

        // load the pipe
        ChatPanel.Destination user = ChatPanel.Destination.read(in);

        // get the number of buddies
        int number = in.readInt();

        // read the buddy list
        Vector buddies = new Vector();
        for (int i = 0; i < number; i++) {
            ChatPanel.Destination buddy = ChatPanel.Destination.read(in);
            buddies.addElement(buddy);
        }

        return new ChatPanel(relay, peerId, user, buddies);
    }

    private void save(DataOutputStream out) throws IOException {
        // first int is the version
        out.writeInt(CURRENT_DATA_VERSION);

        // save bounds
        bounds = frame.getBounds();
        out.writeInt(bounds.x);
        out.writeInt(bounds.y);
        out.writeInt(bounds.width);
        out.writeInt(bounds.height);

        // save the relay Url
        out.writeUTF(chat.getRelay());

        // save the peerId
        out.writeUTF(chat.getPeerId());

        // save the pipe
        chat.getUser().write(out);

        // save the number of buddies
        out.writeInt(chat.getBuddyCount());

        // write the buddy list
        for (int i = 0; i < chat.getBuddyCount(); i++) {
            chat.getBuddy(i).write(out);
        }
    }

    static public void main(String args[]) {
        if (args.length > 0) {
            new Chat(args[0]);
        } else {
            new Chat(null);
        }
    }
}
