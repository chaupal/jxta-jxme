/*
 *
 * $Id: Message.java,v 1.13 2006/06/09 19:14:24 hamada Exp $
 *
 * Copyright (c) 2001-2006 Sun Microsystems, Inc.  All rights reserved.
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
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class represents a JXTA Message. A JXTA Message is composed of
 * several {@link net.jxta.j2me.Element}s. The Elements can be in any
 * order, but certain elements are reserved for use by the JXTA
 * Network. These private Elements use a private namespace.<p>
 * 
 * It also defines convenience methods for accessing commonly-used
 * properties for handling responses to the asynchronous operations
 * defined in {@link net.jxta.j2me.PeerNetwork}.<p>
 *
 * This is an immutable class.
 */

public final class Message {

    public static final String DEFAULT_NAME_SPACE = "";
    public static final String JXTA_NAME_SPACE = "jxta";
    public static final String PROXY_NAME_SPACE = "proxy";
    public static final String DEFAULT_MIME_TYPE = "application/x-jxta-msg";

    public static final String REQUESTID_TAG = "requestId";
    public static final String TYPE_TAG = "type";
    public static final String NAME_TAG = "name";
    public static final String ID_TAG = "id";
    public static final String ARG_TAG = "arg";
    public static final String ATTRIBUTE_TAG = "attr";
    public static final String VALUE_TAG = "value";
    public static final String THRESHOLD_TAG = "threshold";

    public static final String REQUEST_TAG = "request";
    public static final String RESPONSE_TAG = "response";
    public static final String ERROR_TAG = "error";

    public static final String REQUEST_JOIN = "join";
    public static final String REQUEST_CREATE = "create";
    public static final String REQUEST_SEARCH = "search";
    public static final String REQUEST_LISTEN = "listen";
    public static final String REQUEST_CLOSE = "close";
    public static final String REQUEST_SEND = "send";

    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_ERROR = "error";
    public static final String RESPONSE_INFO = "info";
    public static final String RESPONSE_RESULT = "result";
    public static final String RESPONSE_MESSAGE = "data";

    private static final String JXTA_MESSAGE_HEADER = "jxmg";
    private static final int MESSAGE_VERSION = 0;

    private static Hashtable static_ns2id = new Hashtable(2);
    private static Hashtable static_id2ns = new Hashtable(2);

    private static final ByteCounterOutputStream 
        byteCounter = new ByteCounterOutputStream();
    private static final DataOutputStream 
        dataCounter = new DataOutputStream(byteCounter);

    /** 
     * An empty Message to send when we have no outgoing message. This
     * helps maintain a persistent connection to an HTTP relay.
     */
    public static final Message EMPTY = new Message();

    private Element[] elements = null;
    private int nextNameSpaceId = 0;

    static {
        int nextNameSpaceId = 0;
        Integer id = new Integer(nextNameSpaceId++);
        static_ns2id.put(DEFAULT_NAME_SPACE, id);
        static_id2ns.put(id, DEFAULT_NAME_SPACE);
        
        id = new Integer(nextNameSpaceId++);
        static_ns2id.put(JXTA_NAME_SPACE, id);
        static_id2ns.put(id, JXTA_NAME_SPACE);
    }

    /**
     * Construct an empty Message, without any Elements.
     */
    private Message() {
        elements = new Element[0];
    }

    /**
     * Construct a Message from an array of Elements. The supplied
     * Elements are passed along as-is to the relay. Typically, these
     * Elements would hold application data. Internally, JXTA for J2ME
     * may add its own Elements to the Message for routing and other
     * purposes.
     *
     * @param elms an array of elements
     */
    public Message(Element[] elms) {
        // make a defensive copy of the Element array
        elements = new Element[elms.length];
        for (int i=0; i < elms.length; i++) {
            elements[i] = elms[i];
        }
    }

    /**
     * Return the number of Elements contained in this Message. Usage:
     *
     * <p><code>
     * for (int i=0; i < msg.getElementCount(); i++) { <br>
     * &nbsp;&nbsp;Element el = msg.getElement(i); <br>
     * &nbsp;&nbsp;... <br>
     * }
     * </code></p>
     * 
     * @return the number of Elements in this Message.
     */
    public int getElementCount() {
        return elements.length;
    }

    /**
     * Return the Element contained in this Message at the specified
     * index. Usage:
     *
     * <p><code>
     * for (int i=0; i < msg.getElementCount(); i++) { <br>
     * &nbsp;&nbsp;Element el = msg.getElement(i); <br>
     * &nbsp;&nbsp;... <br>
     * }
     * </code></p>
     *
     * @param index specifies the index of the Element to be returned
     *
     * @return the Elements in this Message at the specified index.
     *
     * @throws ArrayOutOfBoundsException if the specified index is out
     * of bounds (negative or greater than {@link #getElementCount})
     */
    public Element getElement(int index) {
        return elements[index];
    }

    void write(DataOutputStream dos) 
        throws IOException {

        // write message signature
        for (int i=0; i < JXTA_MESSAGE_HEADER.length(); i++) {
            dos.writeByte(JXTA_MESSAGE_HEADER.charAt(i));
        }

        // write message version
        dos.writeByte(MESSAGE_VERSION);

        // calculate namespace table indices

        /* there can be at most elements.length namespaces. We specify
           the Hashtable size because mostly, element count will be
           around 3-5 and using the default Hashtable size of 11 would
           be quite wasteful */
        Hashtable ns2id = new Hashtable(elements.length);
        int nsId = nextNameSpaceId;
        for (int i=0; i < elements.length; i++) {
            String ns = elements[i].getNameSpace();
            if (static_ns2id.get(ns) == null && 
                ns2id.get(ns) == null) {
                ns2id.put(ns, new Integer(nsId++));
            }
        }

        // write message name spaces
        dos.writeShort(ns2id.size());
        Enumeration nse = ns2id.keys();
        while(nse.hasMoreElements()) {
            String ns = (String) nse.nextElement();
            writeString(dos, ns);
        }

        // write message element count
        int elementCount = getElementCount();
        dos.writeShort(elementCount);
        for (int i=0; i < elementCount; i++) {
            // write message elements
            getElement(i).write(dos, static_ns2id, ns2id);
        }
    }

    static Message read(DataInputStream dis) 
        throws IOException {

        // read message signature
        for (int i=0; i < JXTA_MESSAGE_HEADER.length(); i++) {
            if (dis.readByte() != JXTA_MESSAGE_HEADER.charAt(i)) {
                throw new IOException("Message header not found");
            }
        }

        // read message version
        int version = dis.readByte();
        if (version != MESSAGE_VERSION) {
            throw new IOException("Message version mismatch: expected " +
                                  MESSAGE_VERSION + ", got " + version);
        }

        // read message name spaces
        int nsCount = dis.readShort();
        String[] nameSpaces = new String[nsCount];
        for (int i=0; i < nsCount; i++) {
            String ns = readString(dis);
            nameSpaces[i] = ns;
        }

        // read message element count
        int elementCount = dis.readShort();
        Element[] elms = new Element[elementCount];

        // create name space indices
        Hashtable id2ns = new Hashtable(nsCount);
        for (int i=0; i < nsCount; i++) {
            id2ns.put(new Integer(i), nameSpaces[i]);
        }

        for (int i=0; i < elementCount; i++) {
            // read message elements
            elms[i] = Element.read(dis, static_id2ns, id2ns);
        }

        Message msg = new Message(elms);
        return msg;
    }
    
    static String readString(DataInputStream dis)
        throws IOException {

        int len = dis.readShort();
        if (len < 0) {
            throw new IOException("Negative string length in message");
        }
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes);
    }
    
    static void writeString(DataOutputStream dos, String s)
        throws IOException {

        dos.writeShort(s.length());
        dos.write(s.getBytes());
    }

    /**
     * Returns the size in bytes of this Message.
     */
    public int getSize() {
        synchronized(byteCounter) {
            byteCounter.reset();
            synchronized(dataCounter) {
                try {
                    write(dataCounter);
                } catch (IOException ex) {
                    throw new RuntimeException("ByteCounter should never " +
                                               " throw an IOException");
                }
            }
            return byteCounter.size();
        }
    }
}

