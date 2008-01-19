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
 * $Id: PicShare.java,v 1.10 2005/04/26 19:06:50 hamada Exp $
 *
 */

/*****************************************************************************
 *
 * PicShare release history
 *
 * Version numbers below correspond to the PicShare.version string, not to the
 * CVS check-in ID.
 *
 * 1.00  03/18/02  Beta release.  There was an unversioned alpha on 03/09.
 * 1.01  03/20/02  Run peer as rdv in joined groups.  Sort group and
 *                 peer lists.  Keep dupes out of group list.  Frame icon.
 * 1.02  03/23/02  Add exit button, remove chat button.
 *                 Reset status line when file operation reaches 100%.
 *                 Background of transparent areas in images is now same as
 *                 canvas background.
 *                 Made sort routine case-insensitive.
 *                 Give app the focus when a file is dropped in.
 * 1.03  04/09/02  The file transmission class has changed its name from
 *                 "FileCast" to "JxtaCast".  It's in its own package now.
 * 1.04  04/10/02  New frames: Inlaid and Miami.  Show frame image on options panel.
 * 1.05  01/11/03  Use the JXTA_HOME system property.
 * 1.06  01/28/03  Create the "reconf" file in the JXTA_HOME dir during reconfig.
 * 1.07  04/05/03  Beef up peer and group advt publishing after creating or
 *                 joining.  Add more error reporting.  Log version at startup.
 * 2.00  04/07/03  Tested with JXTA 2.0 platform, tag JXTA_2_0_Stable_20030301.
 *                 Changed startup image, from bullseye to milk drop.
 *
 *****************************************************************************/

package net.jxta.picshare;

// Java imports.

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.JxtaCastEvent;
import net.jxta.jxtacast.JxtaCastEventListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/*
 * PicShare: JXTA JxtaCast Demo application
 *
 *           Picture sharing over a peer-to-peer network.
 *           Images are broadcast from the sending peer to all other PicShare users
 *           currently in the peer group.
 *
 */
public class PicShare extends JPanel implements JxtaCastEventListener,
        FileDropTargetListener {

    // PicShare version number.
    // Note JxtaCast has its own version number, and it is the JxtaCast
    // version number that is sent in the messages.
    //
    public final static String version = "2.00";

    P2PFace p2p;          // Class encapsulating the peer-2-peer networking protocols.
    JxtaCast jxtaCast;     // Class for sending/receiving multi-cast files.
    String jxtaHomeDir;  // Location of the JXTA file caches.

    RendezvousListener rdvListener;   // Listen for rendezvous connections.
    PicShareAdvDiscovery advDiscovery;  // Find other PicShare users.

    // Known peers running PicShare in the current group.
    // The PeerId is the key, and the peer name is the value.
    Hashtable knownPeers = new Hashtable(50);

    // Known rendezvous connections.
    // The PeerId is the key, and last known rdv status is the value,
    // as an Integer object.
    Hashtable<String, Integer> knownRdvs = new Hashtable<String, Integer>(10);

    // Main window controls.
    JFrame frame;           // On-screen controls.
    JProgressBar status;          // Status bar.
    BtnListener btnListener;     // Listener for most buttons in the app;

    final static String defaultStatusStr = "Waiting for picture.";


    // Picture tab  controls.
    JToolBar tools;           // Tool bar.
    JButton btnSaveAs;       // Tool bar: Save As button.
    JTextArea captionDisp;     // Picture caption area.
    PictureFrameDisplay imgView;  // The image view area.

    // Toolbar button text.
    final static String exitStr = "Exit program";
    final static String sendStr = "Send picture";
    final static String saveAsStr = "Save as";
    final static String chatStr = "Chat";
    final static String firstStr = "First";
    final static String prevStr = "Prev";
    final static String nextStr = "Next";
    final static String lastStr = "Last";


    // Peer Group tab controls.
    JLabel currPeerGroup;
    JTextArea peerList;
    JLabel rdvMsg;
    final static String rdvMsgStr = "Rendezvous connections: ";

    // Peer Group tab button text.
    final static String changeGroupStr = "Change group";
    final static String newGroupStr = "Create new group";
    final static String refreshStr = "Refresh";


    // Options tab controls.
    JCheckBox ckboxCaption;            // User option for whether to ask for captions.
    SizeableImageDisplay optFrameImg;  // Frame chooser display.

    // Options tab button text.
    final static String carvedFrameStr = "Carved";
    final static String giltFrameStr = "Gilt";
    final static String inlaidFrameStr = "Inlaid";
    final static String miamiFrameStr = "Miami";
    final static String noFrameStr = "None";
    final static String configStr = "JXTA platform configuration";

    // Oft-used file dialogs.  We keep them, instead of creating them fresh
    // each time, so they can maintain their state (current directory, etc.)
    FileDialog openDlg;
    FileDialog saveDlg;

    // A collection of all the files we've sent or received during this run.
    // The vector will contain FilePackage objects.
    //
    Vector<FilePackage> filePackages;
    int currFilePkg = 0;

    // temp
    int chatMsgNum;


    public static void main(String s[]) {

        System.out.println("\nPicShare v" + version);
        System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
        System.out.println();

        PicShare picShare = new PicShare();
        picShare.init();
    }


    /**
     * Initialize the p2p system, and create the UI.
     */
    protected void init() {

        // Collection of files sent or received during this run.
        filePackages = new Vector<FilePackage>(100);

        // Set a default directory for loading and storing picture files.
        // If the JXTA_HOME property has been set, create a "pictures" dir
        // under that.  Otherwise, create it under the current dir.
        jxtaHomeDir = System.getProperty("JXTA_HOME");
        if (jxtaHomeDir == null)
            jxtaHomeDir = "." + File.separator + ".jxta" + File.separator;
        else if (!jxtaHomeDir.endsWith(File.separator))
            jxtaHomeDir += File.separator;

        String fileSaveLoc = jxtaHomeDir + "pictures";
        File dirTest = new File(fileSaveLoc);
        try {
            dirTest.mkdirs();
            fileSaveLoc += File.separator;
        } catch (SecurityException ex) {
            ex.printStackTrace();
            fileSaveLoc = "." + File.separator;
        }

        rdvListener = createRdvListener();

        // Should we run JXTA or the simulation?  It's controlled by the
        // JXTA.SIMULATION system property.  The simulation is handy for testing
        // the UI, without starting JXTA.  Under the simulation, the jxtaCast
        // object will be null, so there are serveral places in the code
        // that test for that.
        //
        String runSim = System.getProperty("JXTA.SIMULATION");
        if (runSim != null && runSim.equals("true"))
            p2p = new SimP2PFace();
        else {
            p2p = new JxtaP2PFace(rdvListener);

            // The JxtaCast object handles broadcast file transfers and side chats.
            jxtaCast = new JxtaCast(p2p.getMyPeerAdv(), p2p.getDefaultGroup(), "PicShare");
            JxtaCast.logEnabled = true;
            jxtaCast.fileSaveLoc = fileSaveLoc;
            jxtaCast.addJxtaCastEventListener(this);
        }

        initGUI(fileSaveLoc);

        // Use discovery to find other peers running PicShare in this group.
        // JxtaCast will work without discovery.  We're doing this just so
        // we can show a list of PicShare peers to the user.
        //
        // At this point we probably haven't connected to any rendezvous peers
        // yet, but we can still discover peers on the local subnet.
        //
        advDiscovery = new PicShareAdvDiscovery(p2p.getDefaultGroup());
        advDiscovery.loadFromCache();
        advDiscovery.launchDiscovery(null);

        // Launch peer group discovery.  No listener, we'll just load the list
        // from the cache when we want to show it to the user.
        p2p.discoverGroups(null, null);

        // It's possible that rdv events came in while we were
        // finishing the init process.  Refresh the display just in case.
        updateRdvDisplay();
    }


    /**
     * Create listener for rendezvous events.
     */
    RendezvousListener createRdvListener() {

        // Create a rendezvous listener.
        return new RendezvousListener() {
            public void rendezvousEvent(RendezvousEvent event) {

                if (event.getType() == RendezvousEvent.RDVCONNECT ||
                        event.getType() == RendezvousEvent.RDVRECONNECT ||
                        event.getType() == RendezvousEvent.RDVDISCONNECT ||
                        event.getType() == RendezvousEvent.RDVFAILED) {

                    // Add this rdv to our list, or update his status if he's
                    // already there.
                    knownRdvs.put(event.getPeer(), event.getType());

                    // If we haven't created the advDiscovery obj yet, bail.
                    if (advDiscovery == null)
                        return;

                    // If we've connected to a new rdv.  Launch discovery,
                    // so we can see any peers and groups this rdv knows.
                    if (event.getType() == RendezvousEvent.RDVCONNECT) {
                        advDiscovery.launchDiscovery(event.getPeer());
                        p2p.discoverGroups(event.getPeer(), null);
                    }

                    // Update the UI with the number of active connections.
                    updateRdvDisplay();
                }
            }
        };
    }


    /**
     * Perform user interface startup.
     */
    void initGUI(String fileSaveLoc) {

        // Set look and feel for the currently running platform.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exc) {
            System.err.println("Error loading L&F: " + exc);
        }

        setPreferredSize(new Dimension(410, 507));

        // Component layout management.
        setLayout(new BorderLayout());

        // Create a listener for application buttons (on panels and toolbars).
        btnListener = new BtnListener();

        // Create the tab control.
        JTabbedPane tabber = new JTabbedPane();
        tabber.add("Picture", createPictureTabPanel());
        tabber.add("Peer group", createPeerGroupTabPanel());
        tabber.add("Options", createOptionsTabPanel());
        tabber.add("About", createAboutTabPanel());
        add(tabber, "Center");

        // Status bar.
        status = new JProgressBar();
        status.setString(defaultStatusStr);
        status.setStringPainted(true);
        status.setMaximum(100);
        status.setValue(0);
        add(status, "South");

        // Main window frame.
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("picshare32.gif"));
        String title = "PicShare";
        if (p2p instanceof SimP2PFace)
            title += " Simulation";
        frame = new JFrame(title + " - " + p2p.getMyPeerName());
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitProgramCommand();
            }
        });
        frame.getContentPane().add("Center", this);
        frame.pack();
        frame.setLocation(200, 100);
        frame.setIconImage(icon);
        frame.setVisible(true);

        // Create a file open dialog, for selecting images to send.
        openDlg = new FileDialog(frame, "Select a picture file to send", FileDialog.LOAD);
        openDlg.setDirectory(fileSaveLoc);
        openDlg.setFilenameFilter(new ImageFilenameFilter());

        // Create a file save-as dialog, for saving received images.
        saveDlg = new FileDialog(frame, "Save file as", FileDialog.SAVE);
        saveDlg.setDirectory(fileSaveLoc);
    }


    /**
     * Create the panel for the picture display tab.
     * <p/>
     * Contains a toolbar, the picture and frame display area, and the
     * picture caption display.
     */
    Component createPictureTabPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Make the toolbar.
        tools = new JToolBar();
        tools.setFloatable(false);
        tools.setBorderPainted(false);
        panel.add(tools, "North");

        // Exit button.
        ImageIcon imgExit = new ImageIcon(getClass().getResource("exit.gif"));
        JButton btnExit = new JButton(imgExit);
        btnExit.setMnemonic('x');
        btnExit.setActionCommand(exitStr);
        btnExit.addActionListener(btnListener);
        btnExit.setHorizontalAlignment(SwingConstants.LEFT);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setMargin(new Insets(0, 0, 0, 5));
        btnExit.setToolTipText("Exit PicShare.");
        tools.add(btnExit);

        // Send button.
        ImageIcon imgSend = new ImageIcon(getClass().getResource("open.gif"));
        JButton btnSend = new JButton(imgSend);
        btnSend.setMnemonic('s');
        btnSend.setActionCommand(sendStr);
        btnSend.addActionListener(btnListener);
        btnSend.setHorizontalAlignment(SwingConstants.LEFT);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setMargin(new Insets(0, 0, 0, 5));
        btnSend.setToolTipText("Open and send a picture.");
        tools.add(btnSend);

        // Save-as button.
        ImageIcon imgSaveAs = new ImageIcon(getClass().getResource("saveAs.gif"));
        btnSaveAs = new JButton(imgSaveAs);
        btnSaveAs.setMnemonic('a');
        btnSaveAs.setActionCommand(saveAsStr);
        btnSaveAs.addActionListener(btnListener);
        btnSaveAs.setHorizontalAlignment(SwingConstants.LEFT);
        btnSaveAs.setBorderPainted(false);
        btnSaveAs.setFocusPainted(false);
        btnSaveAs.setMargin(new Insets(0, 0, 0, 5));
        btnSaveAs.setToolTipText("Save picture.");
        btnSaveAs.setEnabled(false);
        tools.add(btnSaveAs);

/* Chat button removed until we get the chat window coded.

        // Chat button.
        ImageIcon imgChat = new ImageIcon(getClass().getResource("chat.gif"));
        JButton btnChat = new JButton(imgChat);
        btnChat.setMnemonic('c');
        btnChat.setActionCommand(chatStr);
        btnChat.addActionListener(btnListener);
        btnChat.setHorizontalAlignment(SwingConstants.LEFT);
        btnChat.setBorderPainted(false);
        btnChat.setFocusPainted(false);
        btnChat.setMargin(new Insets(0, 0, 0, 5));
        btnChat.setToolTipText("Open the chat window.");
        tools.add(btnChat);
*/

        // First button.
        tools.addSeparator();
        ImageIcon imgFirst = new ImageIcon(getClass().getResource("first.gif"));
        JButton btnFirst = new JButton(imgFirst);
        btnFirst.setMnemonic(KeyEvent.VK_HOME);
        btnFirst.setActionCommand(firstStr);
        btnFirst.addActionListener(btnListener);
        btnFirst.setHorizontalAlignment(SwingConstants.LEFT);
        btnFirst.setBorderPainted(false);
        btnFirst.setFocusPainted(false);
        btnFirst.setMargin(new Insets(0, 0, 0, 5));
        btnFirst.setToolTipText("Show the first picture.");
        tools.add(btnFirst);

        // Prev button.
        ImageIcon imgPrev = new ImageIcon(getClass().getResource("prev.gif"));
        JButton btnPrev = new JButton(imgPrev);
        btnPrev.setMnemonic(KeyEvent.VK_LEFT);
        btnPrev.setActionCommand(prevStr);
        btnPrev.addActionListener(btnListener);
        btnPrev.setHorizontalAlignment(SwingConstants.LEFT);
        btnPrev.setBorderPainted(false);
        btnPrev.setFocusPainted(false);
        btnPrev.setMargin(new Insets(0, 0, 0, 5));
        btnPrev.setToolTipText("Show the previous picture.");
        tools.add(btnPrev);

        // Next button.
        ImageIcon imgNext = new ImageIcon(getClass().getResource("next.gif"));
        JButton btnNext = new JButton(imgNext);
        btnNext.setMnemonic(KeyEvent.VK_RIGHT);
        btnNext.setActionCommand(nextStr);
        btnNext.addActionListener(btnListener);
        btnNext.setHorizontalAlignment(SwingConstants.LEFT);
        btnNext.setBorderPainted(false);
        btnNext.setFocusPainted(false);
        btnNext.setMargin(new Insets(0, 0, 0, 5));
        btnNext.setToolTipText("Show the next picture.");
        tools.add(btnNext);

        // Last button.
        ImageIcon imgLast = new ImageIcon(getClass().getResource("last.gif"));
        JButton btnLast = new JButton(imgLast);
        btnLast.setMnemonic(KeyEvent.VK_END);
        btnLast.setActionCommand(lastStr);
        btnLast.addActionListener(btnListener);
        btnLast.setHorizontalAlignment(SwingConstants.LEFT);
        btnLast.setBorderPainted(false);
        btnLast.setFocusPainted(false);
        btnLast.setMargin(new Insets(0, 0, 0, 5));
        btnLast.setToolTipText("Open the last window.");
        tools.add(btnLast);

        // Picture display area.
        imgView = new PictureFrameDisplay(this);
        panel.add(imgView, "Center");

        // Picture caption text display.
        captionDisp = new JTextArea(3, 80);
        captionDisp.setLineWrap(true);
        captionDisp.setWrapStyleWord(true);
        captionDisp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        captionDisp.setEditable(false);
        JScrollPane captionScrollPane = new JScrollPane(captionDisp);
        captionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(captionScrollPane, "South");

        return panel;
    }


    /**
     * Create the panel for the Peer Group tab.
     */
    Component createPeerGroupTabPanel() {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Current peer group name display.
        String currGroupName;
        if (jxtaCast != null)
            currGroupName = jxtaCast.getPeerGroup().getPeerGroupAdvertisement().getName();
        else
            currGroupName = p2p.getDefaultAdv().getName();
        currPeerGroup = new JLabel(currGroupName);
        currPeerGroup.setFont(new Font("SansSerif", Font.BOLD, 20));
        currPeerGroup.setForeground(Color.blue);
        Border b1 = BorderFactory.createEtchedBorder();
        Border b2 = BorderFactory.createTitledBorder(b1, "Peer group");
        Border b3 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border b4 = BorderFactory.createCompoundBorder(b2, b3);
        currPeerGroup.setBorder(b4);
        panel.add(currPeerGroup);
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(currPeerGroup, gbc);

        // Peer list.
        JLabel label = new JLabel("Peers running PicShare:");
        panel.add(label);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbl.setConstraints(label, gbc);

        peerList = new JTextArea();
        peerList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        peerList.setEditable(false);
        JScrollPane peerScrollPane = new JScrollPane(peerList);
        peerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(peerScrollPane);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbl.setConstraints(peerScrollPane, gbc);

        // Rendezvous connection message.
        rdvMsg = new JLabel(rdvMsgStr + "0");
        panel.add(rdvMsg);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(rdvMsg, gbc);

        // Group of buttons down the right side of the panel.
        JPanel btns = new JPanel();
        GridBagLayout gblBtns = new GridBagLayout();
        btns.setLayout(gblBtns);
        GridBagConstraints gbcBtns = new GridBagConstraints();
        gbcBtns.anchor = GridBagConstraints.NORTHWEST;
        gbcBtns.fill = GridBagConstraints.HORIZONTAL;
        gbcBtns.insets = new Insets(0, 0, 5, 0);

        // Change Group button.
        ImageIcon imgGroup = new ImageIcon(getClass().getResource("group.gif"));
        JButton btnGroup = new JButton("    " + changeGroupStr, imgGroup);
        btnGroup.setMnemonic('c');
        btnGroup.setActionCommand(changeGroupStr);
        btnGroup.addActionListener(btnListener);
        btnGroup.setToolTipText("Join a different peer group.");
        btnGroup.setHorizontalAlignment(SwingConstants.LEFT);
        btns.add(btnGroup);
        gblBtns.setConstraints(btnGroup, gbcBtns);

        // Create New Group button.
        ImageIcon imgNewGroup = new ImageIcon(getClass().getResource("newGroup.gif"));
        JButton btnNewGroup = new JButton("    " + newGroupStr, imgNewGroup);
        btnNewGroup.setMnemonic('n');
        btnNewGroup.setActionCommand(newGroupStr);
        btnNewGroup.addActionListener(btnListener);
        btnNewGroup.setToolTipText("Create and join a new peer group.");
        btnNewGroup.setHorizontalAlignment(SwingConstants.LEFT);
        btns.add(btnNewGroup);
        gbcBtns.gridx = 0;
        gbcBtns.gridy = 1;
        gblBtns.setConstraints(btnNewGroup, gbcBtns);

        // Refresh button.
        ImageIcon imgRefresh = new ImageIcon(getClass().getResource("refresh.gif"));
        JButton btnRefresh = new JButton("    " + refreshStr, imgRefresh);
        btnRefresh.setMnemonic('r');
        btnRefresh.setActionCommand(refreshStr);
        btnRefresh.addActionListener(btnListener);
        btnRefresh.setToolTipText("Rediscover peers and groups.");
        btnRefresh.setHorizontalAlignment(SwingConstants.LEFT);
        btns.add(btnRefresh);
        gbcBtns.gridx = 0;
        gbcBtns.gridy = 2;
        gblBtns.setConstraints(btnRefresh, gbcBtns);

        // Add the button group panel to the main panel.
        panel.add(btns);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(18, 10, 0, 0);
        gbl.setConstraints(btns, gbc);

        return panel;
    }


    /**
     * Create the panel for the Options tab.
     */
    Component createOptionsTabPanel() {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Frame choice radio buttons box.
        JRadioButton carvedBtn = new JRadioButton(carvedFrameStr);
        carvedBtn.setMnemonic('c');
        carvedBtn.setActionCommand(carvedFrameStr);
        carvedBtn.addActionListener(btnListener);

        JRadioButton giltBtn = new JRadioButton(giltFrameStr);
        giltBtn.setMnemonic('g');
        giltBtn.setActionCommand(giltFrameStr);
        giltBtn.addActionListener(btnListener);

        JRadioButton inlaidBtn = new JRadioButton(inlaidFrameStr);
        inlaidBtn.setMnemonic('i');
        inlaidBtn.setActionCommand(inlaidFrameStr);
        inlaidBtn.addActionListener(btnListener);
        inlaidBtn.setSelected(true);

        JRadioButton miamiBtn = new JRadioButton(miamiFrameStr);
        miamiBtn.setMnemonic('m');
        miamiBtn.setActionCommand(miamiFrameStr);
        miamiBtn.addActionListener(btnListener);

        JRadioButton noneBtn = new JRadioButton(noFrameStr);
        noneBtn.setMnemonic('n');
        noneBtn.setActionCommand(noFrameStr);
        noneBtn.addActionListener(btnListener);

        ButtonGroup group = new ButtonGroup();
        group.add(carvedBtn);
        group.add(giltBtn);
        group.add(inlaidBtn);
        group.add(miamiBtn);
        group.add(noneBtn);

        JPanel frameBtns = new JPanel();
        frameBtns.setLayout(new BoxLayout(frameBtns, BoxLayout.Y_AXIS));
        frameBtns.add(carvedBtn);
        frameBtns.add(giltBtn);
        frameBtns.add(inlaidBtn);
        frameBtns.add(miamiBtn);
        frameBtns.add(noneBtn);

        // Frame image display.
        JPanel optFrameImgPanel = new JPanel();
        optFrameImgPanel.setLayout(new BorderLayout());
        optFrameImgPanel.setPreferredSize(new Dimension(150, 120));
        optFrameImgPanel.setMaximumSize(new Dimension(150, 120));
        optFrameImgPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        optFrameImg = new SizeableImageDisplay();
        optFrameImg.setVisible(false);  // Bug work-around, show later.
        optFrameImg.setImage(getClass().getResource("inlaid-frame.jpg"));
        optFrameImgPanel.add(optFrameImg, BorderLayout.CENTER);

        // Frame chooser control: holds frame radio button group and frame display.
        JPanel frameChooser = new JPanel();
        frameChooser.add(frameBtns);
        frameChooser.add(Box.createHorizontalStrut(30));
        frameChooser.add(optFrameImgPanel);
        Border b1 = BorderFactory.createEtchedBorder();
        Border b2 = BorderFactory.createTitledBorder(b1, "Picture frame");
        Border b3 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border b4 = BorderFactory.createCompoundBorder(b2, b3);
        frameChooser.setBorder(b4);
        panel.add(frameChooser);
        gbl.setConstraints(frameChooser, gbc);

        // Captions checkbox.
        panel.add(Box.createVerticalStrut(20));
        ckboxCaption = new JCheckBox("Ask for a caption when sending a picture.", true);
        ckboxCaption.setMnemonic('a');
        panel.add(ckboxCaption);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbl.setConstraints(ckboxCaption, gbc);

        // JXTA Config button.
        panel.add(Box.createVerticalStrut(20));
        ImageIcon imgConfig = new ImageIcon(getClass().getResource("config.gif"));
        JButton configBtn = new JButton("    " + configStr, imgConfig);
        configBtn.setMnemonic('j');
        configBtn.setActionCommand(configStr);
        configBtn.addActionListener(btnListener);
        configBtn.setToolTipText("Show the JXTA platform configuration dialog " +
                "next time you start PicShare.");
        panel.add(configBtn);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbl.setConstraints(configBtn, gbc);

        Component filler = Box.createHorizontalGlue();
        panel.add(filler);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbl.setConstraints(filler, gbc);

        // There's a bug that causes the option panel's mini-frame display to be
        // drawn on the wrong tab panel.  So we'll wait until this tab is
        // displayed before showing it.  This isn't right yet.  The repaint()
        // call below isn't displaying the frame image.  It finally shows up
        // when the user clicks somewhere.
        ComponentListener bugfix = new ComponentListener() {

            public void componentShown(ComponentEvent e) {
                if (!optFrameImg.isVisible()) {
                    optFrameImg.setVisible(true);
                    optFrameImg.repaint();
                }
            }

            /**
             * Not used, but needed for ComponentListener support.
             */
            public void componentResized(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
        };
        panel.addComponentListener(bugfix);

        return panel;
    }


    /**
     * Create the panel for the About tab.
     */
    Component createAboutTabPanel() {

        JLabel text;
        Font fixed = new Font("Monospaced", Font.PLAIN, 12);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // App name.
        text = new JLabel("JXTA PicShare");
        text.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 40));
        text.setForeground(Color.blue);
        panel.add(text);

        // App tagline.
        text = new JLabel("Peer-to-peer image broadcasting");
        text.setFont(new Font("SansSerif", Font.PLAIN, 16));
        text.setForeground(Color.blue);
        panel.add(text);

        // Copyright.
        panel.add(new JLabel("Copyright \u00A9 2002, 2003, Sun Microsystems, Inc."));
        panel.add(Box.createVerticalStrut(30));

        // Version information box.
        JPanel versionInfo = new JPanel();
        versionInfo.setLayout(new BoxLayout(versionInfo, BoxLayout.Y_AXIS));
        Border bOut = BorderFactory.createEtchedBorder();
        Border bIn = BorderFactory.createEmptyBorder(10, 10, 10, 60);
        versionInfo.setBorder(BorderFactory.createCompoundBorder(bOut, bIn));
        text = new JLabel("PicShare version: " + version);
        text.setFont(fixed);
        versionInfo.add(text);
        text = new JLabel("JxtaCast version: " + JxtaCast.version);
        text.setFont(fixed);
        versionInfo.add(text);
        panel.add(versionInfo);
        panel.add(Box.createVerticalGlue());

        // Contact info.
        text = new JLabel("Project JXTA:      http://www.jxta.org");
        text.setFont(fixed);
        panel.add(text);
        text = new JLabel("JxtaCast/PicShare: http://jxtacast.jxta.org");
        text.setFont(fixed);
        panel.add(text);

        return panel;
    }


    /**
     * Open a new file for sending.
     */
    void openFile(File file) {

        // See if the user wants to add a caption.
        String caption = null;
        if (ckboxCaption.isSelected()) {

            // If a file has been dropped in, this program doesn't have focus yet.
            // If the caption dlg comes up without focus, it won't accept keyboard
            // input until the user clicks on it.  How can we make it grab focus?
            frame.toFront();

            JOptionPane capDlgPane = new JOptionPane("Would you like to add a caption " +
                    "to this picture?", JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION);
            capDlgPane.setWantsInput(true);
            JDialog capDlg = capDlgPane.createDialog(frame, "Send picture - " + file.getName());
            capDlg.show();

            // Bail out if user hit Cancel, or closed dialog without choosing yes/no.
            if (capDlgPane.getValue() == null)
                return;
            if (capDlgPane.getValue() instanceof Integer) {
                if ((Integer) capDlgPane.getValue() == JOptionPane.YES_OPTION)
                    caption = (String) capDlgPane.getInputValue();
                else if (((Integer) capDlgPane.getValue()) == JOptionPane.NO_OPTION)
                    caption = null;
                else
                    return;
            } else {
                // The only time we're getting a non-Integer back is when user presses
                // return after entering text, instead of hitting a button.  So take this
                // as a "Yes" response.
                caption = (String) capDlgPane.getInputValue();
            }
        }

        // Create a FilePackage and add it to our collection.
        FilePackage pkg = new FilePackage(file, caption);
        filePackages.add(pkg);
        btnSaveAs.setEnabled(true);

        // Let's wait until the image is finished loading before we start sending
        // it out to the peers.  We want to see it displayed in our window before
        // we see the progress meter start to show the sending progress for it.
        // Also it'll strain the system less if we don't have two threads trying
        // to load the same image file from disk at the same time.
        //
        // So, we call a func that will trigger the image to start loading, and
        // we'll get updates thru the imageUpdate() func.  When the image is
        // completely loaded, we'll start the send process from there.
        //
        // If we've already sent or received this image once, we've already got
        // it loaded and can do the send now.  Image.getWidth() will return -1
        // if the image hasn't loaded yet.  (There's an in-between state, where
        // the image will return the width, yet still isn't completely loaded.
        // Let's not worry about that for now.)
        //
        if (pkg.image.getWidth(this) > -1)
            sendFile(pkg);

        // Put a message on the status line.
        status.setString("Loading " + pkg.filename + "...");
        status.setValue(0);
    }


    /**
     * Send out the file.
     */
    void sendFile(FilePackage pkg) {

        pkg.transType = FilePackage.SEND;
        pkg.fileSent = true;

        // Show the image.
        displayFile(pkg);

        // Send the file out to the peers.
        if (jxtaCast != null)
            jxtaCast.sendFile(pkg.file, pkg.caption);
    }


    /**
     * Display the image and information from the specified file package.
     */
    void displayFile(FilePackage pkg) {
        currFilePkg = filePackages.indexOf(pkg);
        imgView.setImage(pkg.image);
        String capText = pkg.sender + ": " + pkg.filename;
        if (pkg.caption != null)
            capText = capText.concat("\n" + pkg.caption);
        captionDisp.setText(capText);
        captionDisp.setCaretPosition(0);
    }


    public boolean imageUpdate(Image image, int flags, int x, int y, int w, int h) {

        // Is the image finished loading?  Not if the following test is true...
        if ((flags & ALLBITS) == 0)
            return true;

        // Step thru our list of FilePackages.  If we find the one containing
        // this image, and it hasn't been sent out yet, send it now.
        //
        Enumeration<FilePackage> elements = filePackages.elements();
        FilePackage pkg;

        while (elements.hasMoreElements()) {

            pkg = elements.nextElement();

            if (pkg.image == image) {
                if (pkg.transType == FilePackage.SEND && !pkg.fileSent)
                    sendFile(pkg);

                break;
            }
        }

        return true;
    }


    /**
     * JxtaCastEventListener interface support.  Receive status updates on file
     * transmission progress.  File is complete when JxtaCastEvent.percentDone is 100.
     */
    public void jxtaCastProgress(JxtaCastEvent e) {

        // Compose a progress message.  Format the percentage done as a percent
        // value, no decimal places.  If it rounds up to 100%, show it as 99%.
        //
        String action = (e.transType == JxtaCastEvent.SEND) ? "Sending " : "Receiving ";
        String pctStr = NumberFormat.getPercentInstance().format(e.percentDone / 100);
        if (pctStr.equals("100%") && e.percentDone < 100)
            pctStr = "99%";
        status.setString(action + e.filename + "  " + pctStr);
        status.setValue((int) e.percentDone);

        // If a file we've been receiving is complete, add it to our list of files,
        // and display it.
        if (e.transType == JxtaCastEvent.RECV && e.percentDone == 100) {
            FilePackage pkg = new FilePackage(e);
            filePackages.add(pkg);
            btnSaveAs.setEnabled(true);
            displayFile(pkg);
            if (pkg.caption.equals("Print")) {
                System.out.println("Printing :"+pkg.filename);
                print(pkg);
            }
        }

        // It's possible to receive messages from peers we haven't discovered yet,
        // so treat this as discovery.
        //
        addKnownPeer(e.senderId, e.sender, true);

        // If we're done, reset the status line to the default message.
        if (e.percentDone == 100) {
            status.setString(defaultStatusStr);
            status.setValue(0);
        }
    }

    final String PNGMIME = "image/png";
    final String JPEGMIME = "image/jpeg";
    final String GIFMIME = "image/gif";

    void print(FilePackage pkg) {

        try {

            if (pkg.image == null) {
                return;
            }
            InetAddress address = InetAddress.getByName("192.18.190.50");
            Socket socket = new Socket(address, 9100);
            OutputStream out = socket.getOutputStream();
            InputStream is = new FileInputStream(new File(pkg.filepath + pkg.filename));
            String mime = null;
            String name = pkg.filename;
            if (name.toLowerCase().contains("jpg") || name.toLowerCase().contains("jpg")) {
                mime = JPEGMIME;
            } else if (name.toLowerCase().contains("png")) {
                mime = PNGMIME;
            } else if (name.toLowerCase().contains("gif")) {
                mime = GIFMIME;
            }
            printJob(is, out, mime);
            out.flush();
            socket.close();
        } catch (Exception e) {
        }

    }

    private void printJob(InputStream is, OutputStream os, String mime) {

        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        DocFlavor flavor = null;
        try {
            flavor = new DocFlavor.INPUT_STREAM(mime);
        } catch (IllegalArgumentException iae) {
            return;
        }
        StreamPrintServiceFactory factories[] =
                StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, "application/postscript");
        if (factories.length > 0) {
            PrintService service = factories[0].getPrintService(os);

            DocPrintJob job = service.createPrintJob();
            DocAttributeSet das = new HashDocAttributeSet();
            Doc doc = new SimpleDoc(is, flavor, das);
            try {
                job.print(doc, pras);
            } catch (PrintException pe) {
            }
        }
    }

    /**
     * From the FileDropTarget interface: a file has been dropped into PicShare's
     * image view area.
     */
    public void fileDropped(File file) {
        openFile(file);
    }


    /**
     * Quit the program.
     */
    public void exitProgramCommand() {
        System.exit(0);
    }


    /**
     * Copy the currently displayed file into a user-selected location and filename.
     */
    public void fileSaveAsCommand() {

        // Figure out what file we're supposed to save.
        if (filePackages.isEmpty())
            return;
        FilePackage pkg = filePackages.elementAt(currFilePkg);
        if (pkg == null)
            return;

        // Ask the user where to save it.
        saveDlg.setFile(pkg.filename);
        saveDlg.show();
        if (saveDlg.getFile() == null)
            return;

        // Copy the file.
        try {
            // Make sure we're not trying to save on top of ourselves.
            File srcFile = new File(pkg.filepath, pkg.filename);
            File dstFile = new File(saveDlg.getDirectory(), saveDlg.getFile());
            if (srcFile.getCanonicalPath().equals(dstFile.getCanonicalPath()))
                return;

            byte fdata[] = new byte[1024 * 10];

            FileInputStream fis = new FileInputStream(srcFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            FileOutputStream fos = new FileOutputStream(dstFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = bis.read(fdata, 0, fdata.length);
            while (bytesRead > 0) {
                bos.write(fdata, 0, bytesRead);
                bytesRead = bis.read(fdata, 0, fdata.length);
            }

            bos.flush();
            fos.close();
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Switch to a new peer group.
     * Show the user a list of the groups, and join the one she picks.
     */
    public void changeGroupCommand() {

        Vector grpNames = new Vector(100);
        Hashtable<String, PeerGroupAdvertisement> grpAdvs = new Hashtable<String, PeerGroupAdvertisement>(100);
        PeerGroupAdvertisement adv;
        PeerGroupAdvertisement adv2;
        String name;
        Object obj;

        // Build a list of the known peer groups.
        // First, add the root peer group. Then add any other groups we've
        // discovered, loading them from the cache.
        //
        // We're actually building two collections.  The Hashtable stores the
        // advertisements, using the group name as a key.  The Vector holds
        // just the names, to be used with the GUI list.
        //
        adv = p2p.getDefaultAdv();
        grpNames.add(adv.getName());
        grpAdvs.put(adv.getName(), adv);

        Enumeration en = p2p.getKnownGroups();
        while (en.hasMoreElements()) {

            obj = en.nextElement();
            if (obj instanceof PeerGroupAdvertisement) {
                adv = (PeerGroupAdvertisement) obj;
                name = adv.getName();

                // In the case of duplicate group names, check if it's also a
                // duplicate ID, in which case we can skip it.  Otherwise, add
                // a space to the name of one, to make it unique.                
                if (grpAdvs.containsKey(name)) {
                    adv2 = grpAdvs.get(name);
                    if (adv.getID().equals(adv2.getID()))
                        continue;
                    else
                        name += " ";
                }

                grpNames.add(name);
                grpAdvs.put(name, adv);
            }
        }

        // Now create a GUI selection list of the groups.  Sort the names list
        // first.  (Does JList really provide no way to display in sorted order?)
        sort(grpNames);
        JList list = new JList(grpNames);
        JScrollPane scrollPane = new JScrollPane(list);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Build an options dialog to display the list.
        ImageIcon icon = new ImageIcon(getClass().getResource("group.gif"));
        JOptionPane grpDlgPane = new JOptionPane(scrollPane,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon);
        JDialog grpDlg = grpDlgPane.createDialog(frame, "Select a peer group");
        grpDlg.show();

        // Bail out if user hit Cancel, or closed dialog without choosing yes/no.
        if (grpDlgPane.getValue() == null)
            return;
        if (grpDlgPane.getValue() instanceof Integer) {
            if ((Integer) grpDlgPane.getValue() == JOptionPane.OK_OPTION) {

                // The user selected an item in the list.
                // Find the group adv in the hashtable based on the selected
                // name, and then join the group.
                //
                String selName = (String) list.getSelectedValue();
                if (selName != null) {
                    adv = grpAdvs.get(selName);
                    joinGroup(adv);
                }
            }
        }
    }


    /**
     * Create and join a new peer group.
     */
    public void createNewGroupCommand() {

        String grpName;

        // Ask for a new group name.
        ImageIcon icon = new ImageIcon(getClass().getResource("newGroup.gif"));
        JOptionPane newDlgPane = new JOptionPane("What would you like to name " +
                "this group?", JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION, icon);
        newDlgPane.setWantsInput(true);
        JDialog newDlg = newDlgPane.createDialog(frame, "Create Peer Group");
        newDlg.show();

        // Bail out if user hit Cancel, or closed dialog without choosing OK.
        if (newDlgPane.getValue() == null)
            return;
        if (newDlgPane.getValue() instanceof Integer) {
            if ((Integer) newDlgPane.getValue() == JOptionPane.OK_OPTION) {
                grpName = (String) newDlgPane.getInputValue();
                createNewGroup(grpName);
            }
        } else {
            // The only time we're getting a non-Integer back is when user presses
            // return after entering text, instead of hitting a button.  So take this
            // as an "OK" response.
            grpName = (String) newDlgPane.getInputValue();
            createNewGroup(grpName);
        }
    }


    /**
     * Refresh our known peers and peer groups, with new discovery.
     */
    public void refreshPeersAndGroupsCommand() {

        // Refesh the set of known peers, with new discovery.
        advDiscovery.refresh();
        updateRdvDisplay();

        // Refresh the set of known groups, with new discovery.
        if (jxtaCast != null) {
            DiscoveryService disco = jxtaCast.getPeerGroup().getDiscoveryService();
            try {
                disco.flushAdvertisements(null, DiscoveryService.GROUP);
            } catch (Exception e) {
            }
        }
        p2p.discoverGroups(null, null);
    }


    /**
     * Create and join a new peer group.
     *
     * @param grpName Name for the new peer group.
     * @return Created peer group if successful, otherwise null.
     */
    public PeerGroup createNewGroup(String grpName) {

        PeerGroup group = null;

        // Validate the group name.
        if (grpName == null)
            return null;
        String newGrpName = grpName.trim();
        if (newGrpName.length() < 1)
            return null;

        group = p2p.createNewGroup(newGrpName, "PicShare v" + version, true);
        if (group == null)
            return null;

        joinGroup(group);

        return group;
    }


    /**
     * Join a different peer group.
     *
     * @param grpAdv Advertisement of the group to join.
     * @return PeerGroup if successful, otherwise null.
     */
    public synchronized PeerGroup joinGroup(PeerGroupAdvertisement grpAdv) {

        // Check to see if this is the group we're already in.
        PeerGroupAdvertisement currAdv = advDiscovery.group.getPeerGroupAdvertisement();
        if (grpAdv.getID().equals(currAdv.getID()))
            return null;

        // Join the group.  (Or just retreive it, if it's been joined
        // before.  P2PFace handles that.  If we fail, we'll just stay
        // in the current group.
        PeerGroup group = p2p.joinPeerGroup(grpAdv, true);
        if (group == null)
            return null;

        return joinGroup(group);
    }


    /**
     * Join a different peer group.
     *
     * @param group The group to join.
     * @return PeerGroup if successful, otherwise null.
     */
    public synchronized PeerGroup joinGroup(PeerGroup group) {

        PeerGroupAdvertisement grpAdv = group.getPeerGroupAdvertisement();

        // Update the peer group tab display.
        currPeerGroup.setText(grpAdv.getName());

        if (jxtaCast != null)
            jxtaCast.setPeerGroup(group);

        // Tell our peer discovery about the new group.  This will also trigger an
        // update of our gui peer list.
        //
        advDiscovery.setPeerGroup(group);
        advDiscovery.loadFromCache();
        advDiscovery.launchDiscovery(null);

        return group;
    }


    /**
     * Add the specified peer to our list of known peers.
     *
     * @param peerId   Peer Id string.
     * @param peerName Peer name.
     * @param updateUI If true, update the peer display.
     * @return true if the new peer was added.  Returns false if the
     *         peer was already in the list.
     */
    public boolean addKnownPeer(String peerId, String peerName, boolean updateUI) {

        if (!knownPeers.containsKey(peerId)) {
            knownPeers.put(peerId, peerName);
            if (updateUI == true)
                updatePeerDisplay();

            return true;
        }

        return false;
    }


    /**
     * Rebuild the Peer Group panel's peer list from the current collection
     * of peers.
     */
    public synchronized void updatePeerDisplay() {

        String peersStr = "";

        // Build a list in a vector first, so we can sort it.
        // (They're in a hashtable now...)
        Vector vec = new Vector(knownPeers.size());
        Enumeration en = knownPeers.elements();
        while (en.hasMoreElements()) {
            vec.addElement(en.nextElement());
        }

        // Now sort, and unroll the vector into a string with line breaks.
        sort(vec);
        en = vec.elements();
        while (en.hasMoreElements()) {
            peersStr += (String) en.nextElement() + "\n";
        }

        peerList.setText(peersStr);
    }


    /**
     * Update the UI with the number of active rendezvous connections.
     */
    public synchronized void updateRdvDisplay() {

        int activeCount = 0;
        Integer rdvStatus;
        Enumeration<Integer> en = knownRdvs.elements();
        while (en.hasMoreElements()) {
            rdvStatus = en.nextElement();
            if (rdvStatus == RendezvousEvent.RDVCONNECT ||
                    rdvStatus == RendezvousEvent.RDVRECONNECT) {
                activeCount++;
            }
        }
        rdvMsg.setText(rdvMsgStr + activeCount);
    }


    // Sort the vector, according to the string representations of its objects.
    // Sorts in ascending order, case insensitive.
    public static void sort(Vector vec) {

        synchronized (vec) {

            // Ye olde bubble sort.
            Object a;
            Object b;
            String aStr;
            String bStr;
            for (int i = 0; i < vec.size() - 1; i++) {
                for (int j = i + 1; j < vec.size(); j++) {

                    a = vec.elementAt(i);
                    b = vec.elementAt(j);
                    aStr = a.toString().toLowerCase();
                    bStr = b.toString().toLowerCase();
                    if (aStr.compareTo(bStr) > 0) {
                        vec.setElementAt(b, i);
                        vec.setElementAt(a, j);
                    }
                }
            }
        }
    }


    /**
     * An ActionListener that listens to the buttons.
     */
    class BtnListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals(exitStr)) {

                // -- Exit Button -- //
                exitProgramCommand();
            } else if (command.equals(sendStr)) {

                // -- Send Button -- //
                openDlg.show();
                if (openDlg.getFile() != null) {
                    openFile(new File(openDlg.getDirectory(), openDlg.getFile()));
                }
            } else if (command.equals(saveAsStr)) {

                // -- Save As Button -- //
                fileSaveAsCommand();
            } else if (command.equals(chatStr)) {

                // -- Chat Button -- //

                // For now, just send a test message out, to see if peers
                // have got they ears on.
                if (jxtaCast != null)
                    jxtaCast.sendChatMsg("Chat message test: " + chatMsgNum++);

                // Temp, handy for determining desired startup size.
                // System.out.println(frame.getBounds().toString());
            } else if (command.equals(firstStr)) {

                // -- First Button -- //
                if (!filePackages.isEmpty()) {
                    FilePackage pkg = filePackages.firstElement();
                    displayFile(pkg);
                }
            } else if (command.equals(prevStr)) {

                // -- Prev Button -- //
                if (!filePackages.isEmpty() && currFilePkg > 0) {
                    FilePackage pkg = filePackages.elementAt(--currFilePkg);
                    displayFile(pkg);
                }
            } else if (command.equals(nextStr)) {

                // -- Next Button -- //
                if (!filePackages.isEmpty() && currFilePkg < filePackages.size() - 1) {
                    FilePackage pkg = filePackages.elementAt(++currFilePkg);
                    displayFile(pkg);
                }
            } else if (command.equals(lastStr)) {

                // -- Last Button -- //
                if (!filePackages.isEmpty()) {
                    FilePackage pkg = filePackages.lastElement();
                    displayFile(pkg);
                }
            } else if (command.equals(changeGroupStr)) {

                // -- Peer group tab's Change Group Button -- //
                changeGroupCommand();
            } else if (command.equals(newGroupStr)) {

                // -- Peer group tab's Create New Group Button -- //
                createNewGroupCommand();
            } else if (command.equals(refreshStr)) {

                // -- Peer group tab's Refresh Button -- //
                refreshPeersAndGroupsCommand();
            } else if (command.equals(carvedFrameStr)) {

                // -- Option tab's "Carved" radio button -- //
                imgView.setFrame(PictureFrameDisplay.FRAME_CARVED);
                optFrameImg.setImage(getClass().getResource("carved-frame.jpg"));
            } else if (command.equals(giltFrameStr)) {

                // -- Option tab's "Gilt" radio button -- //
                imgView.setFrame(PictureFrameDisplay.FRAME_GILT);
                optFrameImg.setImage(getClass().getResource("gilt-frame.jpg"));
            } else if (command.equals(inlaidFrameStr)) {

                // -- Option tab's "Inlaid" radio button -- //
                imgView.setFrame(PictureFrameDisplay.FRAME_INLAID);
                optFrameImg.setImage(getClass().getResource("inlaid-frame.jpg"));
            } else if (command.equals(miamiFrameStr)) {

                // -- Option tab's "Miami" radio button -- //
                imgView.setFrame(PictureFrameDisplay.FRAME_MIAMI);
                optFrameImg.setImage(getClass().getResource("miami-frame.jpg"));
            } else if (command.equals(noFrameStr)) {

                // -- Option tab's "None" radio button -- //
                imgView.setFrame(PictureFrameDisplay.FRAME_NONE);
                optFrameImg.setImage((Image) null);
            } else if (command.equals(configStr)) {

                // -- Option tab's Config Button -- //

                String title = "Reconfigure JXTA";
                String msg = "Do you want to reconfigure the JXTA platform?\n" +
                        "Press 'Yes', and the configuration dialog will\n" +
                        "appear the next time you start PicShare.\n\n";

                int answer = JOptionPane.showConfirmDialog(frame, msg, title,
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {

                    // We request reconfiguration by writing a file named "reconf" to
                    // the startup directory.
                    try {
                        FileOutputStream fos = new FileOutputStream(jxtaHomeDir + "reconf");
                        fos.write(0);
                        fos.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * Class used to find other PicShare users known in the peer group.
     * <p/>
     * We're using advertisement discovery instead of peer discovery,
     * because we only care about other peers who are running PicShare.
     * <p/>
     * We'll find PicShare users by looking for JxtaCast's back-channel
     * pipe advertisements.  The pipe adv name contains a prefix that we
     * can use to narrow the search.  The rest of the name contains the
     * user's peer name and ID.
     * <p/>
     * Discovered peers are placed in the PicShare.knownPeers collection,
     * and we'll call PicShare.updatePeerDisplay() to refresh the gui.
     */
    class PicShareAdvDiscovery implements DiscoveryListener {

        PeerGroup group;


        PicShareAdvDiscovery(PeerGroup group) {
            this.group = group;
        }


        /**
         * Change to a new peer group.  We stop listening to discovery on
         * the old group, and clear the display.
         */
        public void setPeerGroup(PeerGroup newGroup) {

            DiscoveryService disco = group.getDiscoveryService();
            if (disco != null)
                disco.removeDiscoveryListener(this);

            group = newGroup;

            // Empty the collection, then put ourselves back in.
            knownPeers.clear();
            addKnownPeer(p2p.getMyPeerAdv().getPeerID().toString(),
                    p2p.getMyPeerName(), true);
        }


        /**
         * Load advs from the cache.  They may be there from prior runs.
         */
        public void loadFromCache() {

            boolean peerAdded = false;

            // Put ourselves in the collection.
            peerAdded = addKnownPeer(p2p.getMyPeerAdv().getPeerID().toString(),
                    p2p.getMyPeerName(), false);

            PeerGroupAdvertisement pgAdv = group.getPeerGroupAdvertisement();
            String searchStr = "";

            // Get list of advs from the cache, but restrict them to ones with
            // our JxtaCast back-channel pipe prefix.
            //
            if (jxtaCast != null)
                searchStr = jxtaCast.getBackChannelPipePrefix() + "*";

            // Step thru the list, adding the peers to the collection.
            Enumeration en = p2p.getKnownAdvertisements(pgAdv, "Name", searchStr);
            while (en.hasMoreElements()) {
                if (addPeer((Advertisement) en.nextElement()))
                    peerAdded = true;
            }

            if (peerAdded)
                updatePeerDisplay();
        }


        /**
         * Start a new discovery.
         *
         * @param targetPeerId Limit the discovery to responses from this peer.
         *                     Use to aim a disco request at a specific rdv.
         *                     Set to null to disco with all peers.
         */
        public void launchDiscovery(String targetPeerId) {

            PeerGroupAdvertisement pgAdv = group.getPeerGroupAdvertisement();
            String searchStr = "";

            // Discover other PicShare users by looking for their JxtaCast 
            // back-channel pipe advertisements.  Narrow the search by
            // using the pipe name prefix plus a wildcard.
            // The full pipe name also contains the peer name and ID.
            //
            if (jxtaCast != null)
                searchStr = jxtaCast.getBackChannelPipePrefix() + "*";

            p2p.discoverAdvertisements(targetPeerId, pgAdv, this, "Name", searchStr);
        }


        /**
         * Flush the cache, and launch a new discovery.
         */
        public void refresh() {

            // Empty the collection, then put ourselves back in.
            knownPeers.clear();
            addKnownPeer(p2p.getMyPeerAdv().getPeerID().toString(),
                    p2p.getMyPeerName(), true);

            // Flush any cached advertisements for this group.
            DiscoveryService disco = group.getDiscoveryService();
            if (disco != null) {
                try {
                    disco.flushAdvertisements(null, DiscoveryService.ADV);
                } catch (Exception e) {
                }
            }

            launchDiscovery(null);
        }


        /**
         * Receive disco results.  This func may be called multiple times
         * after discovery is launched.  It may be called by several threads
         * simultaneously.
         */
        public void discoveryEvent(DiscoveryEvent ev) {

            // The simulator returns a null event...
            if (ev == null)
                return;

            boolean peerAdded = false;
            Advertisement adv = null;
            PipeAdvertisement pipeAdv = null;
            String pipeName;
            String peerName;
            String peerID;
            String str = null;

            // Each discovery response contains the set of advertisements
            // matching our search criteria that is known by the responding
            // peer.  We look thru these reponses and extract peer information
            // from them.
            DiscoveryResponseMsg res = ev.getResponse();
            Enumeration en = res.getResponses();
            while (en.hasMoreElements()) {

                try {
                    str = (String) en.nextElement();

                    // Create an advertisement object from each element.
                    // Due to our search criteria, they should all be
                    // PipeAdvertisement objects, but cast carefully anyway.
                    //
                    adv = AdvertisementFactory.newAdvertisement(new MimeMediaType("text/xml"),
                            new ByteArrayInputStream(str.getBytes()));

                    if (addPeer(adv))
                        peerAdded = true;
                } catch (Exception e) {
                    System.out.println("Error parsing ADV discovery response element.");
                    e.printStackTrace();
                }
            }

            if (peerAdded == true)
                updatePeerDisplay();
        }


        /**
         * Extract a peer name and ID from the given advertisement, which should be
         * a pipeAdv for a JxtaCast back-channel pipe.  If we can successfully get
         * the peer info, we'll add the peer to PicShare's collection of known peers.
         *
         * @return true if a new peer was added to the list.  A false return doesn't
         *         necessarily mean that we couldn't extrat the peer info.  It will
         *         also return false if the peer was already in our known peers list.
         */
        boolean addPeer(Advertisement adv) {

            String peerName;
            String peerID;
            String pipeName;
            PipeAdvertisement pipeAdv;
            boolean peerAdded = false;

            try {
                if (adv instanceof PipeAdvertisement) {
                    pipeAdv = (PipeAdvertisement) adv;
                    pipeName = pipeAdv.getName();

                    // System.out.println(" Pipe name = " + pipeName);
                    // System.out.println("      type = " + pipeAdv.getType());

                    // Check the pipe name prefix, to make sure our filter
                    // criteria worked.
                    if (pipeName != null &&
                            pipeName.startsWith(jxtaCast.getBackChannelPipePrefix())) {

                        // JxtaCast knows how to extract the peer name and ID from
                        // the pipe adv name, since it put it there.
                        peerName = JxtaCast.getPeerNameFromBackChannelPipeName(pipeName);
                        peerID = JxtaCast.getPeerIdFromBackChannelPipeName(pipeName);

                        // System.out.println("Discovered PicShare peer: " +
                        //     peerName + "  -  " + peerID);

                        // Add the peer to our collection.
                        peerAdded = addKnownPeer(peerID, peerName, false);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error parsing ADV discovery response element.");
                e.printStackTrace();
            }

            return peerAdded;
        }
    }


    /**
     * FilenameFilter for our open FileDialog: only display .jpg, .gif, and .png files.
     * This won't work under Windows, using Sun's Java.  Oh well.
     */
    class ImageFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {

            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") ||
                    lower.endsWith(".gif") ||
                    lower.endsWith(".png");

        }
    }


    // A collection of info representing one file transaction, either sending
    // or receiving.  Most of the info we want is already in the JxtaCastEvent
    // class, so we'll just extend that.
    //
    class FilePackage extends JxtaCastEvent {

        File file;
        Image image;
        boolean fileSent;


        /**
         * Construct a package for a file that we're going to send.
         */
        FilePackage(File file, String caption) {

            // Set the base class vars.
            transType = SEND;
            filename = file.getName();
            filepath = file.getParent();
            sender = p2p.getMyPeerName();
            senderId = p2p.getMyPeerAdv().getPeerID().toString();
            percentDone = 100;

            if (caption != null)
                this.caption = new String(caption);

            this.file = file;
            image = Toolkit.getDefaultToolkit().getImage(file.getPath());
            fileSent = false;
        }


        /**
         * Construct a package for a file that we've received.
         */
        FilePackage(JxtaCastEvent e) {

            // Copy the base class vars from the event.
            transType = e.transType;
            filename = new String(e.filename);
            filepath = new String(e.filepath);
            sender = new String(e.sender);
            senderId = new String(e.senderId);
            percentDone = e.percentDone;

            if (e.caption != null)
                caption = new String(e.caption);

            image = Toolkit.getDefaultToolkit().getImage(filepath + filename);
            fileSent = false;
        }
    }
}
