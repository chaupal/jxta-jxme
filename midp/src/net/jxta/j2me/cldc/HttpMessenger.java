/*
 * $Id: HttpMessenger.java,v 1.26 2006/06/09 19:14:25 hamada Exp $
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

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Provides a messaging service for the JXTA for MIDP peer. Facilities
 * to send and receive messages are provided. Message receiving is
 * based on establishing a relationship with a JXTA relay server.
 */
final class HttpMessenger {
    private static final String DEFAULT_MIME_TYPE = "application/x-jxta-msg";

    /**
     * The duration that the client is willing to for the
     * connection to block until there is a message to be
     * sent. Setting it to -1 means do not block. Setting
     * it to 0 would mean wait forever, but the server will
     * never comply: it caps it at some tunable value
     * (120000 actually).
     */
    /**
     * Block until Peer ID and Lease are obtained
     */
    private final static int DEFAULT_CONNECT_POLL_TIMEOUT = 0;

    /**
     * The duration that the client is willing to block for
     * more messages on the same connection (using
     * chunking). If set to -1, the connection closes after
     * one message.
     * TBD: Not Used
     */
    //private static final int DEFAULT_CLIENT_WAIT_TIMEOUT = 2000;

    /**
     * Requested lease duration.
     */
    private final static int DEFAULT_CLIENT_LEASE = 3600000;

    private final static String RELAY_SERVICE_ID =
            "uuid-DEADBEEFDEAFBABAFEEDBABE0000000F05";

    private final static String ENDPOINT_SERVICE_ID = "EndpointService:jxta-NetGroup";

    private final static String COMMAND_CONNECT = "connect";
    private final static String COMMAND_GET_PID = "pid";
    private final static String UNKNOWN_PID = "unknown-unknown";

    /**
     * the relay that we are connecting to *
     */
    private String relayUrl = null;

    /**
     * our peerid
     */
    private String peerId = null;

    /**
     *Constructor for the HttpMessenger object
     */
    HttpMessenger() { }

    private String constructURL(String initRelayUrl,
            String command,
            int timeout,
            String pid) {

        String url = initRelayUrl +
        // Relay URL (http://address:port)
        "/" + pid +
                "?" + Integer.toString(timeout) +
        // Relay Poll time - keeps connection alive
        "," + Integer.toString(-1) +
        // lazytime out - always -1 for JXME peers
        "," + initRelayUrl +
        // Relay URL (http://address:port)
        "/" + ENDPOINT_SERVICE_ID +
        // Endpoint Service's ID
        "/" + RELAY_SERVICE_ID +
        // Relay Service's ID
        "/" + command;
        // actual command for relay service

        if (COMMAND_CONNECT.equals(command)) {
            url += "," +
                    Integer.toString(DEFAULT_CLIENT_LEASE) + "," +
                    "keep,other";
        }
        System.out.println("relay: " + url);
        return url;
    }


    /**
     * connects to the relay service and hands back the peer id
     *
     * @param  initRelayUrl     Relay url
     * @param  persistedPeerId  PeerID if any
     * @return                  PeerID as a string
     * @exception  IOException  if an i/o error occurs
     */
    synchronized String connect(String initRelayUrl, String persistedPeerId)
             throws IOException {

        if (initRelayUrl == null) {
            throw new IOException("No relay URL specified");
        }
        relayUrl = initRelayUrl;

        peerId = persistedPeerId;
        String relay = null;
        if (peerId == null) {
            relay = constructURL(initRelayUrl,
                    COMMAND_GET_PID,
                    DEFAULT_CONNECT_POLL_TIMEOUT,
                    UNKNOWN_PID);
        } else {
            relay = constructURL(initRelayUrl,
                    COMMAND_CONNECT,
                    DEFAULT_CONNECT_POLL_TIMEOUT,
                    peerId);
        }

        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = (HttpConnection) Connector.open(relay, Connector.READ_WRITE);
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Content-Length", "0");
            conn.setRequestProperty("Content-Type", DEFAULT_MIME_TYPE);

            if (conn.getResponseCode() != HttpConnection.HTTP_OK &&
                    conn.getResponseCode() != 100) {
                throw new IOException("HTTP Error: " +
                        conn.getResponseCode() + " " +
                        conn.getResponseMessage());
            }

            if (conn.getLength() <= 0) {
                return null;
                // return null as this could be denial of lease.
            }

            dis = conn.openDataInputStream();
            Message msg = Message.read(dis);
            boolean hasPidResponse = false;

            for (int i = 0; i < msg.getElementCount(); i++) {
                Element el = msg.getElement(i);
                String data = new String(el.getData());

                if (Message.RESPONSE_TAG.equals(el.getName()) &&
                        COMMAND_GET_PID.equals(data)) {
                    hasPidResponse = true;
                    break;
                }
            }

            for (int i = 0; hasPidResponse && i < msg.getElementCount(); i++) {
                Element el = msg.getElement(i);
                if ("peerid".equals(el.getName())) {
                    peerId = new String(el.getData());
                    peerId = peerId.substring(peerId.indexOf("uuid"));
                    // now that we have a peerid, connect and get a lease
                    // caution: recursive call
                    if (peerId != null) {
                        connect(initRelayUrl, peerId);
                    }
                    break;
                }
            }
        } finally {
            if (dis != null) {
                dis.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return peerId;
    }


    /**
     * Polls for a message from the relay. This is a blocking call. If a
     * message is not received within the timeout value, null is returned
     *
     * @param  timeout          timeout in millis
     * @param  outgoing         if not empty, message is sent
     * @return                  message received
     * @exception  IOException  if an i/o error occurs
     */
    synchronized Message poll(int timeout, Message outgoing) throws IOException {

        Message msg = null;
        HttpConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        String relay = null;
        System.out.println("Polling Interval: " + timeout);
        try {
            relay = constructURL(relayUrl, "", timeout, peerId);
            conn = (HttpConnection) Connector.open(relay, Connector.READ_WRITE);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Connection", "close");
            if (outgoing == Message.EMPTY) {
                conn.setRequestProperty("Content-Length", "0");
            } else {
                conn.setRequestProperty("Content-Length",
                        Integer.toString(outgoing.getSize()));
                conn.setRequestProperty("Content-Type", DEFAULT_MIME_TYPE);
                //System.out.println ("send Con-Len " + outgoing.getSize());
                dos = conn.openDataOutputStream();
                outgoing.write(dos);
            }
            if (conn.getResponseCode() != HttpConnection.HTTP_OK &&
                    conn.getResponseCode() != 100) {
                throw new IOException("HTTP Error: " +
                        conn.getResponseCode() + " " +
                        conn.getResponseMessage());
            }
            System.out.println("recv Con Length: " + conn.getLength());
            if (conn.getLength() <= 0) {
                return null;
            }

            dis = conn.openDataInputStream();
            msg = Message.read(dis);
        } catch (Throwable t) {
            System.out.println("Error polling: " + t.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }
        }
        return msg;
    }
}

