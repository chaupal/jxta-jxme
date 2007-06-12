/************************************************************************
 *
 * $Id: HttpMessenger.java,v 1.4 2002/09/18 00:28:34 akhil Exp $
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
 * WARRANTIES,INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
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

// this class provides a DoJa implementation of the HTTP message
// protocol with the relay

// this should be in the net.jxta.j2me.cldc package, but it needs to
// access Message.read and Message.write from net.jxta.j2me, which are
// package-private, hence this hack
package net.jxta.j2me;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;

import com.nttdocomo.io.HttpConnection;
import com.nttdocomo.io.ConnectionException;

/**
 * Provides a messaging service for the JXTA for MIDP peer. Facilities
 * to send and receive messages are provided. Message receiving is
 * based on establishing a relationship with a JXTA relay server.
 */
final class HttpMessenger {

    /** the relay that we are connecting to **/
    private String relayUrl = null;

    /** the local peer's peerid **/
    private String peerId = null;

    /** the current lease length **/
    private long lease = 0;

    HttpMessenger() {
    }

    private HttpConnection httpConnectionCreate(String command) 
        throws IOException {

	String urlQueryString = null;
        if (peerId != null) {
            urlQueryString = "/relay?x-jxta-command=" + command + 
                             "&x-jxta-client=" + peerId;
	}
        else {
            urlQueryString = "/relay?x-jxta-command=" + command; 
        }

        HttpConnection conn = (HttpConnection) 
            Connector.open(relayUrl + urlQueryString, Connector.READ_WRITE);
        conn.setRequestMethod(HttpConnection.POST);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        return conn;
    }

    private void httpConnectionConnect(HttpConnection conn)
        throws IOException {

        if (conn.getResponseCode() != HttpConnection.HTTP_OK &&
            conn.getResponseCode() != 100) {
            throw new IOException("HTTP Error: " + 
                                  conn.getResponseCode() + " " +
                                  conn.getResponseMessage());
        }

        String slease = conn.getHeaderField("x-jxta-lease");
        if (slease != null) {
            try {
                lease = Long.parseLong(slease);
            } catch (NumberFormatException e) {
                lease = 0;
                throw new IOException("Could not parse lease: " + slease);
            }
        }

        // will be returned by the obtainLease command
        String optionalPeerId = conn.getHeaderField("x-jxta-client");
        if (optionalPeerId != null) {
            peerId = optionalPeerId;
        }
    }

    /** connects to the relay service and hands back the peer id **/
    synchronized String connect(String relayUrl, String persistedPeerId) 
        throws IOException {

        if (relayUrl == null) {
            throw new IOException("No relay URL specified");
        }
        this.relayUrl = relayUrl;

        peerId = persistedPeerId;

        HttpConnection conn = null;
        try {
            conn = httpConnectionCreate("obtainLease");
	    conn.connect();
            httpConnectionConnect(conn);
        } catch (ConnectionException ex) {
            throw new IOException("Connect: " + ex.getStatus());
        } catch (Throwable t) {
            throw new IOException("Connect: " + t.getMessage());
	} finally {
            if (conn != null) {
                conn.close();
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
        HttpConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            conn = httpConnectionCreate("poll&x-jxta-timeout=" + 
					Integer.toString(timeout));

	    if (outgoing != Message.EMPTY) {
                dos = conn.openDataOutputStream();
                outgoing.write(dos);
                dos.close();
                dos = null;
	    }

	    conn.connect();
            if (conn.getLength() == 0) {
                return null;
            }

            dis = conn.openDataInputStream();
            msg = Message.read(dis);
        } catch (ConnectionException ex) {
            throw new IOException("Connect: " + ex.getMessage());
	} catch (Throwable t){
            throw new IOException("Connect: " + t.getMessage());
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return msg;
    }
}
