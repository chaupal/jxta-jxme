/*
 *
 * $Id: PeerNetwork.java,v 1.33 2006/06/16 21:34:55 hamada Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
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
*/

package net.jxta.j2me;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * This class is an abstraction for the JXTA Network. It specifies the
 * operations that an application can invoke on the JXTA network.
 */

public final class PeerNetwork {

    /**
     * The group, when not specified, defaults to the
     * <code>NetPeerGroup</code>.
     */
    public static final String DEFAULT_GROUP = "urn:jxta:jxta-NetGroup";

    /**
     * The JXTA Unicast pipe. Use for one-to-one communications with a
     * peer.
     */
    public static final String UNICAST_PIPE = "JxtaUnicast";

    /**
     * The JXTA Propagte pipe. Messages sent to this pipe are
     * propagated to the entire group.
     */
    public static final String PROPAGATE_PIPE = "JxtaPropagate";

    /**
     * Create or Search for a Peer.
     */
    public static final String PEER  = "PEER";

    /**
     * Create or Search for a Group.
     */
    public static final String GROUP = "GROUP";

    /**
     * Create or Search for a Pipe.
     */
    public static final String PIPE  = "PIPE";

    /**
     * Search for other resources
     */
    public static final String ADV  = "ADV";

    private static final String PROXY_SERVICE_NAME = 
        "urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE0000000E05";

    private Vector sendMessageQueue = new Vector();

    /** 
     * The default constructor is private. Use the createInstance
     * factory method instead.
     */
    private PeerNetwork() {
    }

    /**
     * Connect to a relay. A connection to a relay needs to be
     * established before any of the other operations can be invoked.
     *
     * @param url relay URL
     *
     * @param state a byte array that represents the persistent state
     * of a connection to the PeerNetwork. Initially, this would be
     * null. A successful {@link #connect} returns this state
     * information which the application is expected to persist and
     * pass it back to connect, if available.
     *
     * @return see the description for the state parameter
     * 
     * @throws IOException if the relay is not accesible
     */
    public byte[] connect(String url, byte[] state) 
        throws IOException {

        relayUrl = url;

        String persistedPeerId = null;
        if (state != null && state.length > 0) {
            persistedPeerId = new String(state);
        }

        peerId = messenger.connect(url, persistedPeerId);
        if (peerId == null) {
            throw new IOException("Connect did not return a peerId");
        }
        
        return peerId.getBytes();
    }

    /**
     * Ask the proxy on the relay to join a new group so that the
     * application can then create a new instance of the PeerNetwork
     * which would be a member of the new group. Note that the
     * application must wait for a success response from the proxy for
     * the join request and only then attempt to create another
     * instance of the PeerNetwork that would be a member of the new
     * group.
     *
     * <p>
     *
     * Please note that it is required to create a new instance of
     * PeerNetwork with the newGroupId and then re-call connect to 
     * effectively join and communicate within the group 
     * {@link #createInstance(String, String)} and {@link #connect}
     *
     * <p>
     *
     * The proxy can be asked to join multiple groups by issuing this
     * request multiple times. Currently there is no leave command,
     * but the proxy could decide to leave the group if there are no
     * more active clients using that group.
     *
     * @param newGroupId the id of the group to join. This id can be
     * obtained by invoking the {@link #search} command for the group
     * and waiting for the response.
     *
     * @param password the password required to join the group, if one is
     * required. Otherwise, it is ignored. (Note: currently it is always
     * ignored. The proxy can only join null-membership groups.
     *
     * @return query id that can be used to match responses
     *
     * @throws IOException if a communication error occurs with the
     * relay or with the JXTA network
     */
    public int join(String newGroupId, String password) throws IOException {
        if (password == null) {
            password = "";
        }
        if (newGroupId == null) {
            throw new IllegalArgumentException("Group ID may not be null");
        }

        int requestId = getNextRequestId();
        Element[] elm = new Element[4];
        elm[0] = new Element(Message.REQUEST_TAG, 
                             Message.REQUEST_JOIN.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.ID_TAG, 
                             newGroupId.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.ARG_TAG, 
                             password.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.REQUESTID_TAG, 
                             Integer.toString(requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        sendMessage(elm);
        return requestId;
    }

    /**
     * Search for Peers, Groups or Pipes.
     *
     * @param type one of 
     * {@link #PEER}, {@link #GROUP} or {@link #PIPE} or {@link #ADV}
     *
     * @param attr the name of the attribute to search
     * for. This is one of the fields that Advertisements
     * are indexed on, returned by 
     * {@link http://platform.jxta.org/nonav/java/api/net/jxta/document/Advertisement.html#getIndexFields()} 
     * in the JXTA J2SE API. The String <code>Name</code>
     * is usually used to search advertisements by name,
     * for example.
     *
     * @param query an expression specifying the items being searched
     * for and also limiting the scope of items to be returned. This
     * is usually a simple regular expression such as, for example,
     * <code>TicTacToe*</code> to search for all entities with names
     * that begin with TicTacToe.
     *
     * @param threshold the maximum number of responses
     * allowed from any one peer
     *
     * @return query id that can be used to match responses
     *
     * @throws IOException if a communication error occurs with the
     * relay or with the JXTA network
     */
    public int search(String type, String attr, String query, int threshold) 
        throws IOException {

        int requestId = getNextRequestId();
        Element[] elm = new Element[6];
        elm[0] = new Element(Message.REQUEST_TAG, 
                             Message.REQUEST_SEARCH.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.TYPE_TAG, 
                             type.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.ATTRIBUTE_TAG, 
                             attr.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.VALUE_TAG, 
                             query.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[4] = new Element(Message.THRESHOLD_TAG, 
                             Integer.toString(threshold).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[5] = new Element(Message.REQUESTID_TAG, 
                             Integer.toString(requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        sendMessage(elm);
        return requestId;
    }

    /**
     * Create a 
     * {@link #PEER} {@link #GROUP} or {@link #PIPE}
     *
     * @param type one of 
     * {@link #PEER}, {@link #GROUP} or {@link #PIPE}
     *
     * @param name the name of the entity being created. 
     *
     * @param id pre-defined id of the entity bieng created. Can be null
     *
     * @param arg an optional arg depending upon the type of entity
     * being created. For example, for {@link #PIPE}, this would be
     * the type of {@link #PIPE} that is to be created. For example,
     * <code>JxtaUniCast</code> and <code>JxtaPropagate</code> are
     * commonly-used values. This parameter can be <code>null</code>.
     *
     * @return query id that can be used to match responses. 
     *
     * @throws IOException if a communication error occurs with the
     * relay or with the JXTA network
     */
    public int create(String type, String name, String id, String arg) 
        throws IOException {

        int requestId = getNextRequestId();

        int numberElements = 4;

        if (id != null) {
            numberElements++;
        }
        if (arg != null) {
            numberElements++;
        }

        Element[] elm = new Element[numberElements];

        elm[0] = new Element(Message.REQUEST_TAG, 
                             Message.REQUEST_CREATE.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.NAME_TAG, 
                             name.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.REQUESTID_TAG, 
                             Integer.toString(requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.TYPE_TAG, 
                             type.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        int index = 4;

        if (id != null) {
            elm[index++] = new Element(Message.ID_TAG, 
                                      id.getBytes(), 
                                      Message.PROXY_NAME_SPACE, null);
        }

        if (arg != null) {
            elm[index] = new Element(Message.ARG_TAG, 
                                     arg.getBytes(), 
                                     Message.PROXY_NAME_SPACE, null);
        }

        sendMessage(elm);
        return requestId;
    }

    /**
     * Open a Pipe for input.
     *
     * @param id the id of the Pipe.
     *
     * @return query id that can be used to match responses
     *
     * @throws IOException if a communication error occurs with the
     * relay or with the JXTA network
     *
     * @throws IllegalArgumentException if id is null
     */
    public int listen(String id) throws IOException, IllegalArgumentException  {

        return pipeOperation(Message.REQUEST_LISTEN, id, null);
    }

    /**
     * Close an input Pipe.
     *
     * @param id the id of the Pipe.
     *
     * @return query id that can be used to match responses
     *
     * @throws IOException if a communication error occurs with the
     * relay or with the JXTA network
     *
     * @throws IllegalArgumentException if id is null
     */
    public int close(String id) 
        throws IOException, IllegalArgumentException {

        return pipeOperation(Message.REQUEST_CLOSE, id, null);
    }

    /**
     * Send data to the specified Pipe.
     *
     * @param id the peer or pipe id to which data is to be sent. 
     *
     * @param data a {@link Message} containing an array of 
     * {@link Element}s which contain application data that is to 
     * be sent
     *
     * @return query id that can be used to match responses, if any
     *
     * @throws IOException if there is a problem in sending
     *
     * @throws IllegalArgumentException if id is null
     */
    public int send(String id, Message data) 
        throws IOException, IllegalArgumentException {

        return pipeOperation(Message.REQUEST_SEND, id, data);
    }

    private int pipeOperation(String op, String id, Message data) 
        throws IOException, IllegalArgumentException {

        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException ("Pipe ID not specified");
        }

        int requestId = getNextRequestId();

        int numberElements = 3;

        if (data != null) {
            numberElements += data.getElementCount();
        }

        Element[] elm = new Element[numberElements];
        elm[0] = new Element(Message.REQUEST_TAG, 
                             op.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.REQUESTID_TAG, 
                             Integer.toString(requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);

        elm[2] = new Element(Message.ID_TAG, 
                             id.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);

        int index = 3;

        if (data != null) {
            for (int i=0; i < data.getElementCount(); i++) {
                elm[index++] = data.getElement(i);
            }
        }

        sendMessage(elm);
        return requestId;
    }

    /**
     * Poll the relay for messages addressed to this Peer.
     *
     * <p>For optimum performance, it is <em>highly</em> recommended
     * that this method be called repeatedly until it returns
     * <code>null</code>, draining all queued messages before sending
     * out any new messages.</p>
     *
     * @param int timeout time in milliseconds to wait for the
     * response. A timeout of <code>0</code> means wait forever.
     *
     * @return a {@link Message} containing an array of 
     * {@link Element}s containing incoming data. Will return a
     * <code>null</code> if there are no incoming {@link Message}s.
     *
     * @throws IOException if there is a problem in communicating with
     * the relay
     */
    public Message poll(int timeout) throws IOException {

        /* Send an empty message when there are no messages to
           send. This helps maintain persistent connections to the
           relay */
        Message outgoing = Message.EMPTY;

        // if there is a queued message, send it first
        if (!sendMessageQueue.isEmpty()) {
            outgoing = (Message) sendMessageQueue.elementAt(0);
            sendMessageQueue.removeElementAt(0);
        }

	// this is a good time to GC... we will be stuck for a while
	// on IO and we may need room if we have a large incoming
	// message
	System.gc();

        return messenger.poll(timeout, outgoing);
    }

    /**
     * Factory method, used to create an instance of a PeerNetwork.
     *
     * @param peername a name that the user would like to give to
     * this Peer. It need not be unique, but it is better for MIDP
     * clients if it is so.
     *
     * @return an instance of PeerNetwork.
     */
    public static PeerNetwork createInstance(String peername) {

        return new PeerNetwork(peername, DEFAULT_GROUP);
    }

    /**
     * Factory method, used to create an instance of a PeerNetwork. 
     * 
     * @param peername a name that the user would like to give to
     * this Peer. It need not be unique, but it is better for MIDP
     * clients if it is so.
     *
     * @param groupId the id of the group to join.
     *
     * @return an instance of PeerNetwork.
     */
    public static PeerNetwork createInstance(String peername,
                                             String groupId) {

        return new PeerNetwork(peername, groupId);
    }

    /*
     * Implementation starts here
     */

    private static int nextRequestId = -1;

    private String peername = null;
    private String groupId = null;
    private String trimmedGID = null;
    private HttpMessenger messenger = null;
    private String relayUrl = null;
    private String peerId = null;

    private PeerNetwork(String peername, String groupId) {
        if (peername == null) {
            throw new IllegalArgumentException("Peer name must be specified");
        }

        if (groupId == null) {
            throw new IllegalArgumentException("Group id must be specified");
        }

        this.peername = peername;
        this.groupId = groupId;
        int ndx = groupId.indexOf ("urn:jxta:");
        if (ndx != -1) {
            trimmedGID = groupId.substring (ndx + "urn:jxta:".length());
        } else {
            trimmedGID = groupId;
        }
        messenger = new HttpMessenger();
    }

    private void sendMessage(Element[] elm)
        throws IOException {

        if (peerId == null) {
            throw new IOException("Must connect before sending a message");
        }

        String proxyAddr = 
            relayUrl + "/EndpointService:" + trimmedGID + "/" + PROXY_SERVICE_NAME + "/" + groupId;
            //System.out.println ("************" + proxyAddr );
        Element destAddrElem = 
            new Element("EndpointDestinationAddress", 
                        proxyAddr.getBytes(), "jxta", null);

        String sourceAddr = "jxta://" + peerId;
        Element srcAddrElem = 
            new Element("EndpointSourceAddress", 
                        sourceAddr.getBytes(), "jxta", null);

        // we will add two elements to the end of the message
        Element[] elm2 = new Element[elm.length + 2];
        for (int i=0; i < elm.length; i++) {
            elm2[i] = elm[i];
        }

        // add the src and dest address elements
        elm2[elm.length] = destAddrElem;
        elm2[elm.length + 1] = srcAddrElem;

        Message msg = new Message(elm2);
        sendMessageQueue.addElement(msg);
    }

    private static synchronized int getNextRequestId() {
        if (++nextRequestId < 0) {
            nextRequestId = 0;
        }
        return nextRequestId;
    }
}

