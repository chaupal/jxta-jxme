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
 * Sample code illustrating PeerNetwork.search()
 */
package tutorial;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

/** 
 * This example illustrates how to use the JXTA for J2ME API to search
 * for Pipes on the JXTA network.
 *
 * It creates an instance of 
 * {@link PeerNetwork PeerNetwork}, connects to the JXTA
 * network, creates a pipe and issues a search request looking for
 * this pipe, and then polls for responses to this search request.<p>
 * 
 * The usage for running the application is:
 * <pre>
 *     mySearch [-relay <i>relay</i>]
 * </pre>
 *
 * where <tt>relay</tt> is the URL of the JXTA for J2ME relay hosts
 * (for example, <tt>http://209.25.154.233:9700</tt>). If no relay is
 * specified on the command line, the application attempts to use the
 * localhost as the relay.
 * 
 * Sample output when this application is run: 
 * <pre>
 * relay url: http://209.25.154.233:9700
 * create request id: 0
 * pipe search query id: 1
 * start polling...
 * </pre>
 * <a name="response-0"><i>response-0:</i></a>
 * <pre>
 * 0 "proxy:response" "text/plain;charset=UTF-8" dlen=7 "success"
 * 1 "proxy:requestId" "application/octet-stream" dlen=1 "0"
 * 2 "proxy:type" "text/plain;charset=UTF-8" dlen=4 "PIPE"
 * 3 "proxy:name" "text/plain;charset=UTF-8" dlen=12 "mySearchPipe"
 * 4 "proxy:id" "text/plain;charset=UTF-8" dlen=80 
 *    "urn:jxta:uuid-59616261646162614E50472050325033B95DA21366214E26A6849948F4DCAE5304"
 * 5 "proxy:arg" "text/plain;charset=UTF-8" dlen=11 "JxtaUnicast"
 * 6 "jxta:EndpointSourceAddress" "text/plain;charset=UTF-8" dlen=78 
 *   "jxta://uuid-59616261646162614A787461503250330AB02835ADAD123AA362808AC812CF2E03"
 * 7 "jxta:EndpointDestinationAddress" "text/plain;charset=UTF-8" dlen=46 
 *   "http://0.0.0.0:0/EndpointService:jxta-NetGroup"
 * </pre>
 * <a name="response-1"><i>response-1:</i></a>
 * <pre>
 * 0 "proxy:response" "text/plain;charset=UTF-8" dlen=6 "result"
 * 1 "proxy:requestId" "application/octet-stream" dlen=1 "1"
 * 2 "proxy:type" "text/plain;charset=UTF-8" dlen=4 "PIPE"
 * 3 "proxy:name" "text/plain;charset=UTF-8" dlen=12 "mySearchPipe"
 * 4 "proxy:id" "text/plain;charset=UTF-8" dlen=80 
 *   "urn:jxta:uuid-59616261646162614E50472050325033B95DA21366214E26A6849948F4DCAE5304"
 * 5 "proxy:arg" "text/plain;charset=UTF-8" dlen=11 "JxtaUnicast"
 * 6 "jxta:EndpointSourceAddress" "text/plain;charset=UTF-8" dlen=78 
 *   "jxta://uuid-59616261646162614A787461503250330AB02835ADAD123AA362808AC812CF2E03"
 * 7 "jxta:EndpointDestinationAddress" "text/plain;charset=UTF-8" dlen=46 
 *   "http://0.0.0.0:0/EndpointService:jxta-NetGroup"
 * </pre>
 *
 * In this example, we received two messages:
 *
 * <ol>
 *
 * <li><a href="#response-0">response-0</a> is in response to
 * <code>requestID==0</code>, the <code>create()</code> request. The
 * first six elements in this message use the proxy name space. The
 * first element returns 
 * {@link net.jxta.j2me.Message#RESPONSE_SUCCESS success}, 
 * indicating that the pipe was successfully created.</li>. The next five elements
 * return information about the pipe that was created (name, id, type,
 * etc.). The last two elements were appended by JXTA and are used for
 * routing; these elements use the JXTA name space.</li>
 *
 * <li><a href="#response-1">response-1</a> is in response to
 * <code>requestID==1</code>, the <code>search()</code> request. The
 * first element returns
 * {@link net.jxta.j2me.Message#RESPONSE_RESULT result},
 * indicating that this message
 * contains the results of the search request.  Elements 2-5 contain
 * information about the pipe that was found (name, id, type, etc.)
 * </li>
 *
 * </ol>
 */

public final class mySearch {
    
    private static final String USAGE =
        "\nUsage: mySearch [-relay relay]" +
        "\n" +
        "options:\n" +
        "  -relay    http relay URL\n";

    private static final String PEER_NAME = "mySearch";
    private static final String PIPE_NAME = "mySearchPipe";
    private static final String PIPE_TYPE = PeerNetwork.UNICAST_PIPE;

    private PeerNetwork peerNetwork = null;
    private String relayUrl = null;
    private byte[] persistentState = null;
    private int createQueryId = 0;
    private int pipeSearchQueryId = 0;
    
    /**
     * Constructor for the mySearch object.
     *
     * First, create a new Peer with the specified name. Although names do
     * not need to be unique, it is better for MIDP clients if they are.
     *
     * <pre>
     *    peerNetwork = PeerNetwork.createInstance(PEER_NAME)
     * </pre>
     *
     * Then, connect to the JXTA relay using:
     * <pre> 
     *     persistentState = peerNetwork.connect(relayUrl, persistentState);
     * </pre>
     *
     * A connection to a relay needs to be established before any other
     * operations can be invoked. In this example, we do not use
     * persistentState again. However, in a real application, this value
     * should be persisted and passed back on subsequent calls to
     * <code>connect()</code> so that the peer can be identified and it
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
     * error occurred.  This response message will contain an element with
     * name "requestID" which matches createQueryId. A separate Element in
     * this Message with name "response" will contain one of the
     * following:
     * {@link net.jxta.j2me.Message#RESPONSE_SUCCESS success}, 
     * {@link net.jxta.j2me.Message#RESPONSE_ERROR error},
     * {@link net.jxta.j2me.Message#RESPONSE_INFO info},
     * {@link net.jxta.j2me.Message#RESPONSE_RESULT result}, or 
     * {@link net.jxta.j2me.Message#RESPONSE_MESSAGE message}. For
     * example, if the call to <code>create()</code> successfully creates
     * the Pipe, we would receive a message with elements similar to the
     * following:
     *
     * <p></p>
     * <table border="0" bgcolor="#e0e0ff">
     * <tr>
     *   <th>Element Index</th>
     *   <th>Name Space</th>
     *   <th>Name</th>
     *   <th>Data</th>
     * </tr>
     * <tr>
     *   <td>0</td>
     *   <td>proxy</td>
     *   <td>response</td>
     *   <td>success</td>
     * </tr>
     * <tr>
     *   <td>1</td>
     *   <td>proxy</td>
     *   <td>requestID</td>
     *   <td>5</td>
     * </tr>
     * <tr>
     *   <td colspan=4><i>(additional elements deleted for brevity)</i></td>
     * </tr>
     * </table>
     * <p></p>
     *
     * We then send a search request to the JXTA Proxy looking for the
     * pipe we just created:
     *
     * <pre>
     *    pipeSearchQueryId = peerNetwork.search(PeerNetwork.PIPE, "Name", PIPE_NAME, 1); 
     * </pre>
     *
     * We pass four arguments to <code>search()</code>:
     *
     * <ul>
     *
     * <li>String type - The type of item to search for; this can be
     * {@link net.jxta.j2me.PeerNetwork#PEER PeerNetwork.PEER},
     * {@link net.jxta.j2me.PeerNetwork#GROUP PeerNetwork.GROUP}, or
     * {@link net.jxta.j2me.PeerNetwork#PIPE PeerNetwork.PIPE}, or
     * {@link net.jxta.j2me.PeerNetwork#PIPE PeerNetwork.ADV}.
     *
     * <li>String attribute - Name of the attribute to search
     * for. This is one of the fields that Advertisements
     * are indexed on.
     *
     * <li>String query - An expression specifying the items to search for
     *
     * <li>String threshold the maximum number of responses
     * allowed from any one peer
     *
     * </ul>
     *
     * In this example, we are searching for the newly created pipe. We
     * will receive asynchronous responses from the JXTA Proxy Service
     * when (and if) it locates the entities for which we're searching.
     *
     * Finally, we loop forever and poll for responses that were sent to
     * this peer:
     *
     * <pre>
     *    Message m = peerNetwork.poll(1);
     * </pre>
     *
     * This method takes one argument, which is the timeout in
     * milliseconds. (A timeout value of 0 would indicate that the poll
     * operation should wait forever.) <p> For each message that is
     * received, we loop through all elements in this message and print
     * their contents. We should expect to see status and informational
     * Messages in response to our call to <code>listen()</code>, as well
     * as one or more messages containing the results of our search query.
     * 
     * @param  relay     URL of the relay host
     * @throws IOException
     */

    public mySearch(String relay) throws IOException {
        this.relayUrl = relay;

        System.out.println("relay url: " + relayUrl);
        
        peerNetwork = PeerNetwork.createInstance(PEER_NAME);
        // Create a peer and have it connect to the relay.
        // A connection to relay is required before any other
        // operations can be invoked.
        persistentState = peerNetwork.connect(relayUrl, persistentState);
            
        // Create a Unicast pipe named after the peer; PipeID will
        // be returned asynchronously in a response message
        createQueryId = peerNetwork.create(PeerNetwork.PIPE, PIPE_NAME, null, PIPE_TYPE);
        System.out.println("create request id: " + createQueryId);
            
        // Send a search request looking for the pipe
        pipeSearchQueryId = peerNetwork.search(PeerNetwork.PIPE, 
                                               "Name", PIPE_NAME, 1);
        System.out.println("pipe search query id: " + pipeSearchQueryId);

        System.out.println("start polling...");

        // Now poll for all messages addressed to us.  We should
        // receive messages containing the status and responses to
        // peerNetwork's create() and search() requests
        while (true) {

            // do not use a timeout of 0, 0 means block forever
            Message m = peerNetwork.poll(1);
            if (m != null) {

                // display details of each element
                for (int i = 0; i < m.getElementCount(); i++ ) {
                    Element e = m.getElement(i);
                    System.out.println(i + " " + 
                                       e.toString() + " " + 
                                       "\"" + new String(e.getData()) + "\"");
                }
                System.out.println();
            }
        }
    }

    /** 
     * Gets the relay URL and calls mySearch().
     * 
     * First parses the command line to see if the <code>-relay</code>
     * option was specified. If the relay was not specified, tries to
     * use the IP address of the localhost. <p>
     * 
     * If we get a relay URL, either from the command line or by using
     * the localhost IP address, we pass this URL to the call to
     * create a new mySearch object. This constructor performs all the
     * interesting work in this example: creating the JXTA components,
     * connecting to the JXTA network, and performing the search
     * operation.
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
                System.err.println("Cannot determine IP address of localhost: " + 
                                   "Must use the -relay option" + USAGE);
                return;
            }
        }

        try {
            mySearch test = new mySearch(relay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

