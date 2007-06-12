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
 * $Id: CloseTest.java,v 1.1 2002/02/25 23:25:47 oic Exp $
 *
 */

/**
 * Unit tests for PeerNetwork.close().
 */

package net.jxta.midp.test.peernetwork;

import java.io.*;
import java.net.*;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

public final class CloseTest {

    private PeerNetwork peer = null;
    private String peerName = "jxta peer";
    private String relayUrl = null;
    private String pipeName = "jxta pipe";
    private byte[] persistentState = null;
    private int listenQueryId = 0;
    private int closeQueryId = 0;

    private String testName = null;
    private static int count = 0;

    public void runTest() {
	testPositive("existing pipe name", pipeName, null, "JxtaUnicast");
	testNegative("non-existing pipe name", "somepipe", null, "JxtaUnicast");
	testNegative("non-matching pipe type", pipeName, null, "sometype");
    }

    public void testPositive(String title, String name, String id, String type) {
	count++;
	System.out.println("\n********** Test " + count + " positive: " + title + " **********");

	try {
	    closeQueryId = peer.close(name, id, type);
	    System.out.println("close query ID: " + closeQueryId);

	    System.out.println("** Test " + count + " PASSED **");
	} catch (IOException e) {
	    System.out.println(e);
	    System.out.println("** Test " + count + " FAILED **");
	} catch (Exception e) {
	    System.out.println(e);
	    System.out.println("** Test " + count + " FAILED **");
	}
    }

    public void testNegative(String title, String name, String id, String type) {
	count++;
	System.out.println("\n********** Test " + count + " negative: " + title + " **********");

	try {
	    closeQueryId = peer.close(name, id, type);
	    System.out.println("close query ID: " + closeQueryId);

	    System.out.println("** Test " + count + " FAILED **");
	} catch (IOException e) {
	    System.out.println(e);
	    System.out.println("** Test " + count + " PASSED **");
	} catch (Exception e) {
	    System.out.println(e);
	    System.out.println("** Test " + count + " PASSED **");
	}
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
	System.out.println("relay url: " + relayUrl);

	try {
	    peer = PeerNetwork.createInstance(peerName);
	    persistentState = peer.connect(relayUrl, null);
	    System.out.println("persistent state: " + persistentState);

	    listenQueryId = peer.listen(pipeName, null, "JxtaUnicast");
	    System.out.println("listen query ID: " + listenQueryId);
	} catch (IOException e) {
	    System.out.println(e);
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    public void tearDown() {
    }

    public CloseTest(String name) {
	testName = name;
	System.out.println("Test name: " + testName);
    }

    static public void main(String args[]) {
	CloseTest test = new CloseTest("CloseTest");
	test.setUp();
	test.runTest();
	test.tearDown();
    }
}

