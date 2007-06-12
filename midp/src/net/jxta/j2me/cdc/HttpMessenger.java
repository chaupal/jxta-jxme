/************************************************************************
 *
 * $Id: HttpMessenger.java,v 1.20 2003/08/07 01:38:09 kuldeep Exp $
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
 **********************************************************************/

// this class provides a CDC implementation of the HTTP message
// protocol with the relay

// this should be in the net.jxta.j2me.cdc package, but it needs to
// access Message.read and Message.write from net.jxta.j2me, which are
// package-private, hence this hack
package net.jxta.j2me;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

/**
 * Provides a messaging service for the JXTA for MIDP peer. Facilities
 * to send and receive messages are provided. Message receiving is
 * based on establishing a relationship with a JXTA relay server.
 */
final class HttpMessenger {

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
    private static final int DEFAULT_CONNECT_POLL_TIMEOUT = 0;

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
    private static final int DEFAULT_CLIENT_LEASE = 3600000;

    /**
     * Service IDs
     */
    private static final String RELAY_SERVICE_ID = 
        "uuid-DEADBEEFDEAFBABAFEEDBABE0000000F05"; 

    private static final String ENDPOINT_SERVICE_ID ="EndpointService:jxta-NetGroup";


    private static final String COMMAND_CONNECT = "connect";
    private static final String COMMAND_GET_PID = "pid";
    private static final String UNKNOWN_PID = "unknown-unknown";

    /** the relay that we are connecting to **/
    private String relayUrl = null;

    /** our peerid **/
    private String peerId = null;

    HttpMessenger() {
    }

    private URL constructURL(String initRelayUrl, 
                             String command,
                             int timeout,
                             String pid) throws MalformedURLException {

        String url = initRelayUrl +                       // Relay URL (http://address:port)
                     "/" + pid + 
                     "?" + Integer.toString(timeout) +  // Relay Poll time - keeps connection alive
                     "," + Integer.toString(-1) +         // lazytime out - always -1 for JXME peers
                     "," + initRelayUrl +                 // Relay URL (http://address:port)
                     "/" + ENDPOINT_SERVICE_ID +          // Endpoint Service's ID
                     "/" + RELAY_SERVICE_ID +             // Relay Service's ID
                     "/" + command;                       // actual command for relay service

        if (COMMAND_CONNECT.equals(command)) {
            url += "," + Integer.toString(DEFAULT_CLIENT_LEASE) + 
                   "," + "keep,other";
        } 

        return new URL(url);
    }

    /** connects to the relay service and hands back the peer id **/
    synchronized String connect(String initRelayUrl, String persistedPeerId) 
        throws IOException {

        if (initRelayUrl == null) {
            throw new IOException("No relay URL specified");
        }
        relayUrl = initRelayUrl;

        peerId = persistedPeerId;
        URL relay = null;
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

        HttpURLConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = (HttpURLConnection) relay.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Length", "0");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK &&
                conn.getResponseCode() != 100) {
                throw new IOException("HTTP Error: " + 
                                      conn.getResponseCode() + " " +
                                      conn.getResponseMessage());
            }

            String contentLenStr = conn.getHeaderField("Content-Length");

            //System.out.println ("ConLen: " + contentLenStr);

            if (contentLenStr == null) {
                return null;      // return null as this could be denial of lease.
            }
            if ("0".equals(contentLenStr.trim())) {
                return peerId;
            }

            dis = new DataInputStream(conn.getInputStream());
            Message msg = Message.read(dis);
            boolean hasPidResponse = false;

            for (int i=0; i < msg.getElementCount(); i++) {
                Element el = msg.getElement(i);
                String data = new String(el.getData());

                if (Message.RESPONSE_TAG.equals(el.getName()) &&
                    COMMAND_GET_PID.equals(data)) {
                    hasPidResponse = true;
                    break;
                }
            }

            for (int i=0; hasPidResponse && i < msg.getElementCount(); i++) {
                Element el = msg.getElement(i);
                if ("peerid".equals(el.getName())) {
                    peerId = new String(el.getData());
                    peerId = peerId.substring (peerId.indexOf("uuid"));
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
                conn.disconnect();
            }
        }

        return peerId;
    }

    /**
     * Polls for a message from the relay. This is a blocking call. If a
     * message is not received within the timeout value, null is returned 
     */
    synchronized Message poll(int timeout, Message outgoing) 
        throws IOException {

        Message msg = null;
        HttpURLConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        URL relay = null;
        //System.out.println ("Polling... RelayURL: " + relayUrl);
        try {
            relay = constructURL (relayUrl, "", timeout, peerId);

            conn = (HttpURLConnection) relay.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Length", 
                                    Integer.toString(outgoing.getSize()));
            dos = new DataOutputStream(conn.getOutputStream());
            outgoing.write(dos);
            
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK &&
                conn.getResponseCode() != 100) {
                throw new IOException("HTTP Error: " + 
                                       conn.getResponseCode() + " " +
                                       conn.getResponseMessage());
            }
            String contentLenStr = conn.getHeaderField("Content-Length");
            //System.out.println ("Con Length: " + contentLenStr);
            if (contentLenStr == null) {
                return null;
            }
            if ("0".equals(contentLenStr.trim())) {
                return null;
            }

            dis = new DataInputStream(conn.getInputStream());
            msg = Message.read(dis);
        } finally {
            if (conn != null) {
                conn.disconnect();
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
