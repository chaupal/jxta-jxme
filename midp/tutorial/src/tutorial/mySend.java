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
 * Sample code illustrating PeerNetwork.send() 
 */
package tutorial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

/**
 * This example illustrates using the JXTA for JXME APIs to send and
 * receive messages.
 *
 * This example creates an instance of 
 * {@link PeerNetwork PeerNetwork} and connects to the JXTA network, creates
 * a pipe, opens it for incomig messages, and issues a search request looking 
 * for this pipe. It next polls for messages until it receives a  response 
 * that the pipe has been found, and then sends a message to this pipe 
 * (essentially talking to itself). 
 * Finally, it polls for messages again and displays the messages it receives.
 * 
 * The usage for running the application is:
 * <pre>
 *     mySend [-relay <i>relay</i>]
 * </pre>
 * where <tt>relay</tt> is the URL of the JXTA for J2ME relay hosts
 * (for example, <tt>http://209.25.154.233:9700</tt>). If no relay is
 * specified on the command line, the application attempts to use the
 * localhost as the relay.
 * 
 * Sample output when this application is run: 
 * <pre>
 * relay url: http://209.25.154.233:9700
 * create request id: 0
 * start polling for pipeId...
 * got pipeId
 * listen request id: 1
 * start polling for pipeId...
 * listening on pipeId
 * send request id: 2
 * start polling for incoming messages...
 * Message from: mySendPeer
 * Message: Hello there
 * </pre>
 */

public class mySend {
    
    private static final String USAGE =
        "\nUsage: mySend [-relay relay]" +
        "\n" +
        "options:\n" +
        "  -relay    http relay URL\n";
    
    private static final String PEER_NAME = "mySendPeer"; 
    private static final String PIPE_NAME = "myPipe";
    private static final String PIPE_TYPE = PeerNetwork.UNICAST_PIPE;
    
    private PeerNetwork peer = null;
    private String relayUrl = null;
    private byte[] persistentState = null;
    private int createQueryId = -1;
    private int listenQueryId = -1;
    private int sendRequestId = -1;
    
    /**
     * Constructor for the mySend object.
     *
     * First, create a new instance of
     * {@link PeerNetwork} with the specified name and connect
     * to the JXTA relay. A connection to the relay needs to be established
     * before any other operations can be invoked.
     * <pre>
     *    peerNetwork = PeerNetwork.createInstance(PEER_NAME);
     *    persistentState = peerNetwork.connect(relayUrl, persistentState);
     * </pre>
     *
     * In this example, we do not use
     * persistentState again. However, in a real application, this value
     * should be persisted and passed back on subsequent calls to
     * {@link net.jxta.j2me.PeerNetwork#connect connect()}
     * so that the peer can be identified and it
     * can receive any messages that have been queued in its absence.
     *
     * Next, we create a Unicast pipe named after the peer:
     *
     * <pre>
     *     createQueryId = peerNeetwork.create(PeerNetwork.PIPE, PIPE_NAME, null, PIPE_TYPE);
     * </pre> 
     *
     * The call to <code>create()</code> takes four arguments:
     * <ul>
     * <li> String type - The type of item to create; this can be
     * {@link net.jxta.j2me.PeerNetwork#PEER PeerNetwork.PEER},
     * {@link net.jxta.j2me.PeerNetwork#GROUP PeerNetwork.GROUP}, or
     * {@link net.jxta.j2me.PeerNetwork#PIPE PeerNetwork.PIPE}.
     * <li> String name - Name of the item. For example Pipe Name
     * <li> String id - predefined id. Can be null.
     * <li> String type - used only for Pipes. Type of pipe (
     * {@link net.jxta.j2me.PeerNetwork#UNICAST_PIPE PeerNetwork.UNICAST_PIPE} or
     * {@link net.jxta.j2me.PeerNetwork#PROPAGATE_PIPE PeerNetwork.PROPAGATE_PIPE})
     * </ul>
     *
     * <p></p>
     * Because we pass a null Pipe ID, a new pipe will be created for
     * us. The Pipe ID for this new pipe will be returned asynchronously
     * in a response message. We will also receive an asynchronous
     * response indicating the status of this call to
     * <code>create()</code> -- whether it completed successfully or an
     * error occurred.  In this example, we are ignoring these responses.
     * <p></p>
     *
     * Next, we have our peer search for this pipe.
     * {@link #findMyPipe} waits until it finds the pipe and then returns
     * the pipe ID. 
     * <pre>
     *     String pipeId = findMyPipe();
     * </pre>
     * <p></p>
     *
     * Next, we open the Unicast pipe for incoming messages:
     * <pre>
     *     listenQueryId = peerNeetwork.listen(pipeId);
     * </pre> 
     *
     * The call to <code>listen()</code> takes only one arguments:
     * <ul>
     * <li> String id - Pipe ID returned asynchronously in response to <code>create</code>
     * </ul>
     *<p></p>
     * We will also receive an asynchronous
     * response indicating the status of this call to
     * <code>listen()</code> -- whether it completed successfully or an
     * error occurred.  In this example, we are ignoring these responses.
     * <p></p>
     *
     * Next, we wait for the <code>listen</code> response and send it a message.
     *  The method {@link #sendMyMessage sendMymessage()} 
     * uses this pipeID to send the message:
     * <pre>
     *     findMyPipe();
     *     sendMyMessage(pipeId);
     * </pre>
     *
     * Finally, we call {@link #recvMessage} to poll for and display the
     * message that was sent to this peer. Because {@link #recvMessage} loops forever
     * polling for messages, this application never exits.
     * <pre>
     *     recvMessage();
     * </pre>
     */

    public mySend(String relay) throws IOException {
        relayUrl = relay;
        
        System.out.println("relay url: " + relayUrl);
        
        // Create a peer and have it connect to the relay.
        // A connection to relay is required before any other
        // operations can be invoked.
        peer = PeerNetwork.createInstance(PEER_NAME);
        persistentState = peer.connect(relayUrl, persistentState);
        
        // Have the peer create a pipe; PipeID will
        // be returned asynchronously in a response message
        createQueryId = peer.create(PeerNetwork.PIPE, PIPE_NAME, null, PIPE_TYPE);
        System.out.println("create request id: " + createQueryId);
        String pipeID = findMyPipe();

        // Have the peer open previously created Unicast pipe for input
        listenQueryId = peer.listen(pipeID);
        System.out.println("listen request id: " + listenQueryId);        
        findMyPipe();

        // Have peer send a Message
        sendMyMessage(pipeID);
        
        // Finally, have the peer poll for Messages sent to it
        recvMessage();
    }
    
    /**
     * Search for a pipe with the given name, waiting
     * (possibly forever) until the pipe is found.<p></p>
     *
     * This method polls for messages addressed to this peer. We should
     * receive messages in response to our call to listen() -- messages
     * providing information about the pipe and indicating if the pipe was
     * successfully created. These messages will contain an Element named
     * {@link net.jxta.j2me.Message#REQUESTID_TAG} whose data matches our
     * searchQueryId. <p></p>
     *
     * In this example, we ignore all messages except for those that match
     * our searchQueryId. When we receive a matching message, we return the
     * pipe id. <p></p>
     *
     * First, we poll for messages that were sent to this peer. This method
     * takes one argument, which is the timout in milliseconds. (A timeout
     * value of 0 would indicate that the poll operation should wait forever):
     * <pre>
     *     msg = peer.poll(1);
     * </pre>
     *
     * For each message that is received, we loop through all elements:
     * <pre>
     *     for (int i = 0; i < msg.getElementCount(); i++) {
     *          Element e = msg.getElement(i);
     * </pre>
     *
     * We are expecting a message in response to our search request. This message
     * will contain elements in the {@link net.jxta.j2me.Message#PROXY_NAME_SPACE}
     * with the following names:
     * <ul>
     * <li>{@link net.jxta.j2me.Message#REQUESTID_TAG} -- The request ID; we're
     * looking for one that matches our searchRequestId.
     * <li>{@link net.jxta.j2me.Message#TYPE_TAG} -- Type of JXTA component; can be
     * {@link net.jxta.j2me.PeerNetwork#PIPE}, {@link net.jxta.j2me.PeerNetwork#PEER},
     * or {@link net.jxta.j2me.PeerNetwork#GROUP}. We're expecting 
     * {@link net.jxta.j2me.PeerNetwork#PIPE}.
     * <li>{@link net.jxta.j2me.Message#NAME_TAG} -- The name of the JXTA component;
     * we're expecting our PIPE_NAME.
     * <li>{@link net.jxta.j2me.Message#ARG_TAG} -- Argument; in this example, we're
     * expecting the type of our  pipe, PIPE_TYPE.
     * <li>{@link net.jxta.j2me.Message#ID_TAG} -- The JXTA ID; in this example,
     * we're expecting the JXTA pipe ID.
     * </ul>
     * <p></p>
     * We first check if this element is using the 
     * {@link net.jxta.j2me.Message#PROXY_NAME_SPACE}:
     * <pre>
     *     if (Message.PROXY_NAME_SPACE.equals(e.getNameSpace()))
     * </pre>
     *
     * If it is, we get the name of this element:
     * <pre>
     *     String elementName = e.getName();
     * </pre>
     * And we check if it matches any of our expected tags. For example, if the
     * name matches {@link net.jxta.j2me.Message#ID_TAG}, we save the data in the
     * variable id:
     * <pre>
     *     else if (Message.ID_TAG.equals(elementName)) {
     *          id = new String(e.getData());
     * </pre>
     * When processing the element with the name
     * {@link net.jxta.j2me.Message#REQUESTID_TAG}, we first save the element data as
     * a String, and then use Integer.parseInt() to generate the integer value.
     * <p></p>
     * When we finish processing all elements in this message, we check if it's a
     * match. If it is, we return the pipe ID; otherwise, we continue polling for
     * messages until we find one that matches.
     * <p></p>
     * @throws IOException
     */
    public String findMyPipe() 
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
        while (true) {
            
            // do not use a timeout of 0, 0 means block forever
            msg = peer.poll(1);
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
            }


            if (rid == createQueryId &&
                response.equals("success") &&
                type.equals("PIPE") &&
                name.equals(PIPE_NAME) &&
                arg.equals(PIPE_TYPE)) {
                System.out.println("got pipeId");
                return id;
            } else if (rid == listenQueryId &&
                response.equals("success")) {
                System.out.println("listening on pipeId");
                return id;
            }
        }
    }
    
    /**
     * Sends a "Hello there" message to the specified Pipe. <p></p>
     *
     * This method first creates an array of two elements. The first is named
     * "mySend:Name" and contains the name of the sending peer. The second
     * is named "mySend:Message" and contains the text message we're sending:
     * <pre>
     *     Element[] elm = new Element[2];
     *     elm[0] = new Element("mySend:Name", PEER_NAME.getBytes(), null, null);
     *     elm[1] = new Element("mySend:Message", "Hello there".getBytes(),
     *                          null, null);
     * </pre>
     *
     * Each call to the constructor
     * {@link net.jxta.j2me.Element#Element Element} takes four arguments:
     * <ul>
     * <li><code>String name</code> -- the name of the element
     * <li><code>byte[] data</code> -- the data that this element carries
     * <li><code>String nameSpace</code> -- the name space used by this element
     * <li><code>String mimeType</code> -- the mimeType of the data (if null, the default
     * MIME type of "application/octet-stream" is assumed.
     * </ul>
     * <p>
     * Next, a message is constructed using these elements, and is sent using
     * {@link net.jxta.j2me.PeerNetwork#send PeerNetwork.send()}:
     * <pre>
     *     Message msg = new Message(elm);
     *     sendRequestId = peer.send(pipeID, msg);
     * </pre>
     * </p>
     * The call to {@link net.jxta.j2me.PeerNetwork#send PeerNetwork.send()}
     * takes two arguments:
     * <ul>
     * <li><code>String id</code> --  the pipe ID to which the message is sent
     * <li><code>Message data</code> -- The message containing an array of elements
     * </ul>
     * <p> </p>
     * @param pipeID The JXTA pipe ID to which we send the message
     * @throws IOException
     */
    public void sendMyMessage(String pipeID) 
        throws IOException, IllegalArgumentException {

        Element[] elm = new Element[2];
        
        // Our Message will contain two elements. Receiver should look for
        // elements with the same names ("mySend:Name" and "mySend:Message")
        elm[0] = new Element("mySend:Name", PEER_NAME.getBytes(), 
                             null, null);
        elm[1] = new Element("mySend:Message", "Hello there".getBytes(), 
                             null, null);
        
        Message msg = new Message(elm);        
        sendRequestId = peer.send(pipeID, msg);
        System.out.println("send request id: " + sendRequestId);
    }
    
    /**
     * Loops forevers, polling for messages and printing out any that
     * contain the expected elements.
     * <p>
     * In this example, we're only interested in the message we just sent.
     * For every message that is received, we loop through and check its elements.
     * We are expecting two elements: one named "mySend:Name" which is the name of
     * the sending peer, and another named "mySend:Message" which is the text
     * message that was sent to us. If the element's name space and name match
     * these values, we display the information. 
     * </p>
     * Any elements that do not exactly match these names are ignored.
     * 
     */
    public void recvMessage() throws IOException {

        System.out.println("start polling for incoming messages...");
        
        Message msg = null;        
        while (true) {
            msg = peer.poll(1);
            if (msg == null) {
                continue;
            }

            for (int i = 0; i < msg.getElementCount(); i++) {
                Element e = msg.getElement(i);
                if ("mySend:Name".equals(e.getName()))
                    System.out.println("Message from: " + 
                                       new String(e.getData()));
                if ("mySend:Message".equals(e.getName()))
                    System.out.println("Message: " +
                                       new String(e.getData()));
            }
        }
    }

 /** 
     * Gets the relay URL and calls {@link #mySend mySend()}.
     * 
     * First parses the command line to see if the <code>-relay</code>
     * option was specified. If the relay was not specified, tries to
     * use the IP address of the localhost. 
     * <p>
     * If we get a relay URL, either from the command line or by using
     * the localhost IP address, we pass this URL to the call to
     * create a new mySend object. This constructor performs all the
     * interesting work in this example: creating the JXTA components,
     * connecting to the JXTA network, searching for the pipe that was
     * created, and sending and receiving messages.
     * </p>
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String relay = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-relay") && i+1 < args.length) {
                relay = args[++i];
            } else if (args[i].equals("-help")) {
                System.err.println(USAGE);
            } else {
                System.err.println("Error parsing arguments" + USAGE);
                return;
            }
        }
        
        // Relay URL was not specified on the command-line, perhaps it
        // is running locally?
        if (relay == null) {
            // get the ip address of localhost
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                if (localhost != null) {
                    relay = "http://" + localhost.getHostAddress() + ":9700";
                }
            } catch (UnknownHostException ex) {
                System.err.println(ex);
                System.err.println("Cannot determine IP address of " +
                                   "localhost. Must use the -relay option" + 
                                   USAGE);
                return;
            }
        }
        
        try {
            mySend test = new mySend(relay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
