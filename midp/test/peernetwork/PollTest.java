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
 * $Id: PollTest.java,v 1.2 2002/03/06 20:46:35 oic Exp $
 *
 */

/**
 * Unit positive test for PeerNetwork.poll(). One message is to be sent and
 * polled between two peers. The polled message is then to be verified.
 */

package net.jxta.midp.test.peernetwork;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.jxta.j2me.Element;
import net.jxta.j2me.Message;
import net.jxta.j2me.PeerNetwork;

public final class PollTest {

    static String relayUrl = null;
    static Peer[] peers = null;
    static final int peerNum = 2;
    static int sendNum = 0;
    static int pollNum = 1;
    static int pollNull = 0;
    static int DELAY = 1000;
    static int oneMsg = 1;
    static boolean stopPolling = false;
    static String testName = null;

    public PollTest(String name) {
	testName = name;
	System.out.println("Test name: " + testName);
    }

    public void setUp() {
	try {
	    InetAddress localhost = InetAddress.getLocalHost();
	    if (localhost != null) {
		relayUrl = "http://" + localhost.getHostAddress() + ":9700";
	    }
	} catch (UnknownHostException e) {
	    System.out.println(e);
	}
	peers = new Peer[peerNum];
	for (int i=0; i < peerNum; i++) {
	    peers[i] = new Peer(this, "buddy" + i);
	}
    }

    public void runTest() {
	try {
	    for (int i =0; i < peerNum; i++) {
		peers[i].connect();
	    }
	} catch (Exception e) {
	    System.out.println(e);
	    return;
	}
    }

    public void tearDown() {
    }

    static public void main(String args[]) {
	PollTest test = new PollTest("PollTest");
	test.setUp();
	test.runTest();
	test.tearDown();
    }

    final class Peer implements Runnable {

	private PollTest p2p = null;
	private PeerNetwork peer = null;
	private String selfname = null;
	private final String message = "THIS IS A TEST MESSAGE";

	public Peer(PollTest p2p, String selfname) {
            this.p2p = p2p;
            this.selfname = selfname;

            peer = PeerNetwork.createInstance(selfname);
	}

	public synchronized void connect() throws IOException {
            peer.connect(p2p.relayUrl, null);
            peer.listen(selfname, null, "JxtaUnicast");

            Thread thread = new Thread(this);
            thread.start();
	}

	public void run() {
	    while (!stopPolling) {
		Element[] elm = new Element[2];
		elm[0] = new Element("JxtaTalkSenderName", 
				selfname.getBytes(), null, null);
		elm[1] = new Element("JxtaTalkSenderMessage", 
				message.getBytes(), null, null);
		Message sendMsg = new Message(elm);
		Message pollMsg = null;

		String buddy = "buddy" + p2p.pollNum;
		try {
		    // send only one message
		    if (oneMsg == 1) {
			peer.send(buddy, null, null, sendMsg);
			p2p.sendNum++;
			System.out.println(selfname + " -> " + buddy);
			oneMsg++;
		    }
		    pollMsg = peer.poll(-1);
		} catch(IOException e) {
		    e.printStackTrace();
		}

		if (pollMsg != null) {
		    p2p.pollNum++;
		    checkMessage(pollMsg);
		} else {
		    p2p.pollNull++;
		    if ((p2p.pollNum - 1) >= (p2p.peerNum * 2) || p2p.pollNull >= (p2p.peerNum * 10)) {
			stopPolling = true;
		    }
		}

		try {
		    Thread.sleep(p2p.DELAY);
		} catch(InterruptedException e) {
		}
	    }
	}

	public void checkMessage(Message m) {
	    Element el = null;
	    String sender = null;
	    String msg = null;

	    for (int i=0; i < m.getElementCount(); i++) {
		el = m.getElement(i);
		if ("JxtaTalkSenderName".equals(el.getName())) {
		    sender = new String(el.getData());
		}
		if ("JxtaTalkSenderMessage".equals(el.getName())) {
		    msg = new String(el.getData());
		}
	    }

	    if (sender != null && msg != null) {
		String senderStr = new String(sender);
		String msgStr = new String(msg);
		System.out.println(selfname + " <- " + senderStr);
		System.out.println("    sent message: " + message);
		System.out.println("  polled message: " + msg);
		if (message.equals(msg)) {
		    System.out.println("  " + "PASSED");
		} else {
		    System.out.println("  " + "FAILED");
		}
	    }
	}
    }
}

