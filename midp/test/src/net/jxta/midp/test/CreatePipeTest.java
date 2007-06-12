/************************************************************************
 *
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
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

/**
 * Test for testing PeerNetwork.connect()
 */
package net.jxta.midp.test.peernetwork;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

import javax.microedition.midlet.MIDlet;

    public final class CreatePipeTest extends MIDlet implements CommandListener {
    private static final String TALKNAME_PREFIX = "JxtaTalkUserName.";
    private static final String PIPE_TYPE = PeerNetwork.PROPAGATE_PIPE;
    private static final String INSTANTP2P_PIPEID = "urn:jxta:uuid-" + 
                                                    "59616261646162614E50472050325033" + 
                                                    "D1D1D1D1D1D141D191D191D1D1D1D1D104";

    private static final int MAX_TEXTFIELD_SIZE = 256;

    private Display display = null;
    private Form initForm = null;
    private Form configForm = null;

    private TextField tfRelayHost;
    private TextField tfRelayPort;
    private TextField tfIdentity;
    private TextField tfPolltime;
  
    private PeerNetwork peerNetwork = null;
    private String relayUrl = null;
    private byte[] persistentState = null;

    public CreatePipeTest() throws IOException {
        initForm = new Form("Create Pipe Test");

        Command cmdConnect = new Command("Start Test", Command.SCREEN, 1);
        Command cmdConfig = new Command("Configuration", Command.SCREEN, 2);
        Command cmdDefault = new Command("Defaults", Command.SCREEN, 3);
        Command cmdExit = new Command("Exit", Command.EXIT, 4);
        initForm.addCommand(cmdConnect);
        initForm.addCommand(cmdConfig);
        initForm.addCommand(cmdDefault);
        initForm.addCommand(cmdExit);

        initForm.setCommandListener(this); 

        tfRelayHost = new TextField ("Relay Host:", 
                                     "209.25.154.233",
                                     MAX_TEXTFIELD_SIZE,
                                     TextField.ANY);
        tfRelayPort = new TextField ("Relay Port:", 
                                     "9900",
                                     MAX_TEXTFIELD_SIZE,
                                     TextField.NUMERIC);
        tfIdentity = new TextField ("Identity:", 
                                    "testPeer",
                                    MAX_TEXTFIELD_SIZE,
                                    TextField.ANY);
        tfPolltime = new TextField ("Poll Interval:", 
				    "5000",
                                     MAX_TEXTFIELD_SIZE,
                                     TextField.NUMERIC);


   }

    public void commandAction(Command c, Displayable d) {
        if (c.getCommandType() == Command.EXIT) {
            destroyApp(true);
            notifyDestroyed();
            return;
        } 

        String label = c.getLabel();
        if (d == initForm) {
            if (label.equals("Configuration")) {
                editConfig();
            } else if (label.equals("Start Test")) {
                test();
            } 
        } else if (d == configForm) {
            if (c.getCommandType() == Command.OK) {
                display.setCurrent(initForm);
            } else if (c.getCommandType() == Command.BACK) {
                display.setCurrent(initForm);
            }
        } 
    }

    private void editConfig() {
        if (configForm == null) {
            configForm = new Form("Configuration");
            configForm.append(tfRelayHost);
            configForm.append(tfRelayPort);
            configForm.append(tfIdentity);
            configForm.append(tfPolltime);

            Command cmdOK = new Command("OK", Command.OK, 1);
            Command cmdBack = new Command("Back", Command.BACK, 2);
            configForm.addCommand(cmdOK);
            configForm.addCommand(cmdBack);
            configForm.setCommandListener(this) ;
        }
        display.setCurrent(configForm);
    }

    private void setup () {
       peerNetwork = PeerNetwork.createInstance(tfIdentity.getString());
        // Create a peer and have it connect to the relay.
        // A connection to relay is required before any other
        // operations can be invoked.
        try {
            String host = tfRelayHost.getString();
            int port = 0;

            try {
                port = Integer.parseInt(tfRelayPort.getString());
            } catch (NumberFormatException ex) {
                showAlert("setup", 
                          "Error parsing relay port number: " + 
                          tfRelayPort.getString(),
                          AlertType.ERROR, 
                          Alert.FOREVER,
                          initForm);
                return;
            }
            String relayUrl = "http://" + host + ":" + Integer.toString(port);
            persistentState = peerNetwork.connect(relayUrl, persistentState);
            if (persistentState != null) {
                showAlert("setup", 
                          "Setup Done",
                          AlertType.CONFIRMATION, 
                          Alert.FOREVER,
                          initForm);
            } else {
                showAlert("setup", 
                          "FAILED: connection error",
                          AlertType.INFO, 
                          Alert.FOREVER,
                          initForm);
            }
        } catch (Throwable t) {
                showAlert("setup", 
                          "FAILED: connection error " +
                          t.getMessage(),
                          AlertType.INFO, 
                          Alert.FOREVER,
                          initForm);
        }
    }

    private void test () {
        String pipeName = TALKNAME_PREFIX + tfIdentity.getString();
        String pipeId = INSTANTP2P_PIPEID;
        String pipeType = PIPE_TYPE;
  
        try {
            int createId = peerNetwork.create(PeerNetwork.PIPE, pipeName, pipeId, pipeType);
            String id = findPipeId (createId, pipeName);
            if (id != null) {
                showAlert("test", 
                          "Create Pipe SUCCEEDED",
                          AlertType.INFO, 
                          Alert.FOREVER,
                          initForm);
            }
        } catch (Throwable t) {
            showAlert("test", 
                      "Create Pipe FAILED: " +
                      t.getMessage(),
                      AlertType.ERROR, 
                      Alert.FOREVER,
                      initForm);
        }        
    }

    private String findPipeId(int requestId, String pipeName) 
        throws IOException {

        System.out.println("start polling for pipeId...");
        
        // Now poll for all messages addressed to us. Stop when we
        // find the response to our listen command
        int rid = -1;
        String id = null;
        String type = null;
        String name = null;
        String arg = null;
        String response = null;

        Message msg = null;
        int polltime = 5000;
 
        try {
            polltime = Integer.parseInt(tfPolltime.getString());
        } catch (NumberFormatException ex) {
            showAlert("findPipeId", 
                      "Error parsing poll interval value: " + 
                          tfPolltime.getString(),
                          AlertType.ERROR, 
                          Alert.FOREVER,
                          initForm);
                return null;
            }

        while (true) {
            
            // do not use a timeout of 0, 0 means block forever
            msg = peerNetwork.poll(polltime);
            if (msg == null) {
                continue;
            }
                
            // look for a response to our search query
            for (int i = 0; i < msg.getElementCount(); i++ ) {
                Element e = msg.getElement(i);
                if (Message.PROXY_NAME_SPACE.equals(e.getNameSpace())) {

                    String elementName = e.getName();
                    if (Message.REQUESTID_TAG.equals(elementName)) {
                        String rids = new String(e.getData());
                        try {
                            rid = Integer.parseInt(rids);
                        } catch (NumberFormatException nfx) {
                            System.err.println("Recvd invalid " +
                                               Message.REQUESTID_TAG +
                                               ": " + rids);
                            continue;
                        }
                    } else if (Message.TYPE_TAG.equals(elementName)) {
                        type = new String(e.getData());
                    } else if (Message.NAME_TAG.equals(elementName)) {
                        name = new String(e.getData());
                    } else if (Message.ARG_TAG.equals(elementName)) {
                        arg = new String(e.getData());
                    } else if (Message.ID_TAG.equals(elementName)) {
                        id = new String(e.getData());
                    } else if (Message.RESPONSE_TAG.equals(elementName)) {
                        response = new String(e.getData());
                    }
                }
                System.out.println(i + " " + e.getName() + " " + new String(e.getData()) +
                                   " " + e.getNameSpace());
            }

            System.out.println("requestId:rid: " + requestId + ":" + rid + "\n" + 
                               "response: " + response + "\n" + 
                               "type: " + type + "\n" +
                               "pipeName:name: " + pipeName + ":" + name + "\n" +
                               "pipetype:arg: " + PIPE_TYPE + ":" + arg);
            if (rid == requestId &&
                response.equals("success") &&
                type.equals("PIPE") &&
                name.equals(pipeName) &&
                arg.equals(PIPE_TYPE)) {
                System.out.println("got pipeId");
                return id;
            }
            try {
               Thread.sleep (1000);
            } catch (Throwable t) {}
        }
    }

    /** 
     * Gets the relay URL and calls ConnectTest().
     * 
     * First parses the command line to see if the <code>-relay</code>
     * option was specified. If the relay was not specified, tries to
     * use the IP address of the localhost. <p>
     * 
     * If we get a relay URL, either from the command line or by using
     * the localhost IP address, we pass this URL to the call to
     * create a new ConnectTest object. This constructor creats the 
     * JXTA components. test method connects to the JXTA network.
     */
    public void startApp() {
        display = Display.getDisplay(this);
        // startup in the config screen if the identity is not configured

        System.out.println (tfIdentity.getString());
        if ("".equals(tfIdentity.getString())) {
            editConfig();
        } else {
            display.setCurrent(initForm);
        }
        setup();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        peerNetwork = null;
    }

    private void showAlert(String title, String message, AlertType type, 
                           int timeout, Displayable back) {
        System.out.println ("showAlert: " + message);
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(timeout);
        display.setCurrent(alert, back);
    }
}

