/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: CmTest.java,v 1.4 2005/06/14 05:53:28 hamada Exp $
 */
package net.jxta.impl.cm;

import net.jxta.peergroup.PeerGroupID;
import net.jxta.id.IDFactory;
import net.jxta.id.ID;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.io.ByteArrayInputStream;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.peer.PeerID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.textui.TestRunner;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocument;
import net.jxta.document.Element;
import net.jxta.document.AdvertisementFactory;

/**
 *  A CmTest unit test
 */
public class CmTest extends TestCase {

    private final static int ITERATIONS = 10;

    private static Cm cm = null;

    private final static String[] dirname = {"Peers", "Groups", "Adv"};
    private static boolean failed = false;
    private final static PeerGroupID pgID = (PeerGroupID) IDFactory.newPeerGroupID();

    private List queue = Collections.synchronizedList(new ArrayList());

    private static Random random = new Random();

    /**
     *  Constructor for the CmTest object
     *
     *@param  testName  test name
     */
    public CmTest(String testName) {
        super(testName);

        synchronized (CmTest.class) {
            if (null == cm) {
                cm = new Cm(true);
            }
        }

    }

    /**
     *  Description of the Method
     *
     *@param  expired  Description of the Parameter
     */
    public void testCreatePeer() {
        boolean expired = false;
        ID advID = null;
        String advName = null;

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            PeerAdvertisement adv = generatePeerAdv(i);
            advID = adv.getID();
            advName = advID.getUniqueValue().toString();

            try {
                if (!expired) {
                    cm.save(dirname[0], advName, adv);
                } else {
                    cm.save(dirname[0], advName, adv, 1, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to create Peer Adv: " + e.getMessage());
            }
        }
        System.out.println("Completed Creation of " + ITERATIONS +
                " PeerAdvertisements in: " +
                (System.currentTimeMillis() - t0) / 1000 +
                " seconds");
    }

    /**
     *  Description of the Method
     *
     *@param  expired  Description of the Parameter
     */
    public void testCreatePipe() {
        boolean expired = false;
        ID advID = null;
        String advName = null;
        StructuredDocument doc = null;

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            PipeAdvertisement adv = generatePipeAdv(i);
            advID = adv.getID();
            if (advID == null || advID.equals(ID.nullID)) {
                advName = Cm.createTmpName(doc);
            } else {
                advName = advID.getUniqueValue().toString();
            }
            try {
                if (!expired) {
                    cm.save(dirname[2], advName, adv);
                } else {
                    cm.save(dirname[2], advName, adv, 1, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to create Pipe Adv: " + e.getMessage());
            }
        }

        System.out.println("Completed Creation of " + ITERATIONS +
                " PipeAdvertisements in: " +
                (System.currentTimeMillis() - t0) / 1000 +
                " seconds");
    }

    /**
     *  Description of the Method
     */
    public void testDeletePeer() {
        ArrayList advNameList = new ArrayList(ITERATIONS);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            PeerAdvertisement adv = generatePeerAdv(i);
            String advName = adv.getID().getUniqueValue().toString();
            try {
                cm.save(dirname[0], advName, adv);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to create Peer Adv: " + e.getMessage());
            }
            advNameList.add(advName);
        }

        // randomize the list to make deletion a little more unpredictable
        Collections.shuffle(advNameList);

        for (int i = 0; i < ITERATIONS; i++) {
            try {
                cm.remove(dirname[0], (String) advNameList.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to delete Peer Adv: " + e.getMessage());
            }
        }

        Vector searchResults = null;
        try {
            searchResults = cm.search(dirname[0], "Name", "*", ITERATIONS, null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to search Peer Adv: " + e.getMessage());
        }

        // alway start unit test with an empty cm (rm -r .jxta)
        assertTrue("remove failed for " + searchResults.size(),
                searchResults.size() == 0);

        System.out.println("Completed Creation+Deletion of " + ITERATIONS +
                " PeerAdvertisements in: " +
                (System.currentTimeMillis() - t0) / 1000 +
                " seconds");
    }

    /**
     *  {@inheritDoc}
     *
     *@param  message  Description of the Parameter
     */
    public static void fail(String message) {
        failed = true;
        junit.framework.TestCase.fail(message);
    }

    /**
     *  Description of the Method
     *
     *@param  i  Description of the Parameter
     */
    private void findPeerAdv(int i) {
        long t0 = System.currentTimeMillis();
        try {
            Vector searchResults =
                    cm.search(dirname[0], "Name", "CmTestPeer" + i, 1, null);
            assertNotNull("Null search result", searchResults);
            Enumeration result = searchResults.elements();
            assertNotNull("Null search enumerator", result);
            assertTrue("empty Search Result for query attr=Name value=CmTestPeer" + i,
                    result.hasMoreElements());
            while (result.hasMoreElements()) {
                    String val = (String) ((PeerAdvertisement) result.nextElement()).getName();
                    assertTrue("Name mismatch ", val.equals("CmTestPeer" + i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("findPeerAdv failed: " + e.getMessage());
        }
        System.out.println("findPeerAdv retrieved CmTestPeer" + i + " in: " +
                (System.currentTimeMillis() - t0) +
                " ms");
    }

    /**
     *  Description of the Method
     *
     *@param  i  Description of the Parameter
     */
    private void findPeerAdvContains(int i) {
        /*
         *  to make things more interesting, we remove the first digit from the
         *  queryString if it is longer than 2 digits.
         */
        String queryString = Integer.toString(i);
        if (queryString.length() > 2) {
            queryString = queryString.substring(1, queryString.length());
        }

        long t0 = System.currentTimeMillis();
        Vector searchResults = null;
        try {
            searchResults = cm.search(dirname[0], "Name", "*" + queryString + "*", 10, null);
            assertNotNull("Null search result", searchResults);
            Enumeration result = searchResults.elements();
            assertNotNull("Null search enumerator", result);
            assertTrue("Enumerator empty", result.hasMoreElements());
            while (result.hasMoreElements()) {
                ByteArrayInputStream dataStream =
                        (ByteArrayInputStream) result.nextElement();
                StructuredDocument doc =
                        StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, dataStream);
                Enumeration en = doc.getChildren("Name");
                while (en.hasMoreElements()) {
                    String val = (String) ((Element) en.nextElement()).getValue();
                    System.out.println("Contains: Queried for *" + queryString + "*, found: " + val);
                    assertTrue("result returned " + val +
                            " does not contain " + queryString, val.indexOf(queryString) != -1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("findPeerAdvContains failed: " + e.getMessage());
        }
        System.out.println("Contains: retrieved " + searchResults.size() +
                " entries in: " +
                (System.currentTimeMillis() - t0) + " ms");
    }

    /**
     *  Description of the Method
     *
     *@param  i  Description of the Parameter
     */
    private void findPeerAdvEndswith(int i) {
        /*
         *  to make things more interesting, we remove the first digit from the
         *  id if it is longer than 2 digits.
         */
        String queryString = Integer.toString(i);
        if (queryString.length() > 2) {
            queryString = queryString.substring(1, queryString.length());
        }

        long t0 = System.currentTimeMillis();
        Vector searchResults = null;
        try {
            searchResults = cm.search(dirname[0], "Name", "*" + queryString, 10, null);
            assertNotNull("Null search result", searchResults);
            Enumeration result = searchResults.elements();
            assertNotNull("Null search enumerator", result);
            assertTrue("Enumerator empty", result.hasMoreElements());
            while (result.hasMoreElements()) {
                ByteArrayInputStream dataStream =
                        (ByteArrayInputStream) result.nextElement();
                StructuredDocument doc =
                        StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, dataStream);
                Enumeration en = doc.getChildren("Name");
                while (en.hasMoreElements()) {
                    String val = (String) ((Element) en.nextElement()).getValue();
                    System.out.println("EndsWith: Queried for *" + queryString + ", found: " + val);
                    assertTrue("result returned " + val +
                            " does not end with " + queryString,
                            val.endsWith(queryString));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("findPeerEndsWith failed: " + e.getMessage());
        }
        System.out.println("EndsWith: retrieved " + searchResults.size() +
                " entries in: " +
                (System.currentTimeMillis() - t0) + " ms");
    }

    /**
     *  Description of the Method
     *
     *@param  i  Description of the Parameter
     */
    private void findPeerAdvStartswith(int i) {
        /*
         *  to make things more interesting, we remove the last digit from the
         *  queryString if it is longer than 2 digits.
         */
        String queryString = Integer.toString(i);
        if (queryString.length() > 2) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }

        long t0 = System.currentTimeMillis();
        Vector searchResults = null;
        try {
            searchResults = cm.search(dirname[0], "Name", "CmTestPeer" + queryString + "*", 10, null);
            assertNotNull("Null search result", searchResults);
            Enumeration result = searchResults.elements();
            assertNotNull("Null search enumerator", result);
            assertTrue("Enumerator empty", result.hasMoreElements());
            while (result.hasMoreElements()) {
                ByteArrayInputStream dataStream =
                        (ByteArrayInputStream) result.nextElement();
                StructuredDocument doc =
                        StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, dataStream);
                Enumeration en = doc.getChildren("Name");
                while (en.hasMoreElements()) {
                    String val = (String) ((Element) en.nextElement()).getValue();
                    System.out.println("StartsWith: Queried for CmTestPeer" + queryString + "*, found: " + val);
                    assertTrue("result returned " + val +
                            " does not start with CmTestPeer" + queryString,
                            val.startsWith("CmTestPeer" + queryString));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("findPeerAdvStartsWith failed: " + e.getMessage());
        }
        System.out.println("StartsWith: retrieved " + searchResults.size() +
                " entries in: " +
                (System.currentTimeMillis() - t0) + " ms");
    }

    /**
     *  Description of the Method
     *
     *@param  number  Description of the Parameter
     *@return         Description of the Return Value
     */
    private PeerAdvertisement generatePeerAdv(int number) {
        try {
            PeerAdvertisement peerAdv = (PeerAdvertisement)
                    AdvertisementFactory.newAdvertisement(PeerAdvertisement.getAdvertisementType());
            peerAdv.setPeerGroupID(pgID);
            peerAdv.setPeerID((PeerID) IDFactory.newPeerID(pgID));
            peerAdv.setName("CmTestPeer" + number);
            return peerAdv;
        } catch (Exception e) {
            e.printStackTrace();
            fail("generatePeerAdv failed: " + e.getMessage());
        }
        return null;
    }

    /**
     *  Description of the Method
     *
     *@param  number  Description of the Parameter
     *@return         Description of the Return Value
     */
    private PipeAdvertisement generatePipeAdv(int number) {
        try {
            PipeAdvertisement adv = (PipeAdvertisement)
                    AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
            adv.setPipeID((PipeID) IDFactory.newPipeID(pgID));
            adv.setName("CmTestPipe" + number);
            adv.setType(PipeService.UnicastType);
            return adv;
        } catch (Exception e) {
            e.printStackTrace();
            fail("generatePipeAdv failed: " + e.getMessage());
        }
        return null;
    }

    /**
     *  The main program to test Cm
     *
     *@param  argv           The command line arguments
     *@exception  Exception  Description of the Exception
     */
    public static void main(String[] argv) throws Exception {
        try {
            TestRunner.run(suite());
        } finally {
            synchronized (CmTest.class) {
                if (null != cm) {
                    cm.stop();
                    cm = null;
                }
            }
        }

        System.err.flush();
        System.out.flush();
    }

    /**
     *  Description of the Method
     */
    public void testSearchPeer() {
        boolean expired = false;
        ID advID = null;
        String advName = null;
        for (int i = 0; i < ITERATIONS; i++) {
            PeerAdvertisement adv = generatePeerAdv(i);
            advID = adv.getID();
            advName = advID.getUniqueValue().toString();

            try {
                if (!expired) {
                    cm.save(dirname[0], advName, adv);
                } else {
                    cm.save(dirname[0], advName, adv, 1, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to create Peer Adv: " + e.getMessage());
            }
        }
        long t0 = System.currentTimeMillis();
        Vector entries = cm.getEntries(dirname[0], false);
        assertTrue("empty keys", entries.size() != 0);
        System.out.println("getEntries retrieved " + entries.size() +
                " peers in: " +
                (System.currentTimeMillis() - t0) / 1000 +
                " seconds");

        for (int i = 0; i < ITERATIONS; i++) {
            findPeerAdv(i);
        }

        t0 = System.currentTimeMillis();
        Vector searchResults = cm.search(dirname[0], null, null, 10000, null);
        System.out.println("non-existent test should find 0, found: " +
                searchResults.size());
        System.out.println("retrieved " + searchResults.size() + " records in: " +
                (System.currentTimeMillis() - t0) +
                " ms");

        int threshold = 10;
        Vector expirations = new Vector();
        Vector results = cm.getRecords(dirname[0], threshold, expirations, false);
        assertTrue("cm.getRecords failed", threshold == results.size());
        System.out.println("Testing Query for non-existent records");
        results = cm.getRecords(dirname[1], threshold, expirations, false);
        assertTrue("cm.getRecords(dirname[1]) should not return results", results.size() == 0);
        System.out.println("End Testing Query for non-existent records");
    }

    /**
     *  A unit test suite for JUnit
     *
     *@return    The test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(CmTest.class);
        return suite;
    }
}

