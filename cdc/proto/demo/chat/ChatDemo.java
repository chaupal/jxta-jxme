/*
 *  Copyright (c) 2005 Sun Microsystems, Inc.  All rights
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
 *  =========================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.                                      
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: ChatDemo.java,v 1.12 2006/01/07 00:05:49 hamada Exp $
 */
import java.io.IOException;
import java.util.Enumeration;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.rendezvous.RendezVousService;

/**
 * The intent of this demo is to illustrate platform configuration 
 * and creation. Then it goes onto illustrating how use a propagate
 * for group chat.
 *
 */

public class ChatDemo implements PipeMsgListener, RendezvousListener {

    static PeerGroup netPeerGroup = null;
    private final static String SenderMessage = "JxtaTalkSenderMessage";
    private static final String SenderName = "JxtaTalkSenderName";
    private static final String SENDERGROUPNAME = "GrpName";

    private PipeService pipeService;
    private RendezVousService rendezvous;
    private PipeAdvertisement pipeAdv;
    private InputPipe input = null;
    private OutputPipe output = null;
    private DiscoveryService discovery = null;

    /**
     *  main
     *
     * @param  args  command line args
     */
    public static void main(String args[]) {
        ChatDemo chatDemo = new ChatDemo();
        try {
            chatDemo.startJxta();
            chatDemo.start();
            chatDemo.printMemStat();
        } catch (IOException io) {
            io.printStackTrace();
        }
        synchronized(chatDemo) {
            //The intention with the wait is to ensure the app continues to run
            //run.
            try {
                chatDemo.wait();
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
        }
    }

    private void start() throws IOException {
        discovery = netPeerGroup.getDiscoveryService();
        pipeService = netPeerGroup.getPipeService();
        //Create the input pipe with this app as the message listener for this
        //pipe
        input = pipeService.createInputPipe(getMyJxtaPipeAdv(), this);
        //This pipe is a propagated pipe, therefore also bind to it
        output = pipeService.createOutputPipe(getMyJxtaPipeAdv(), 100);
        //Announce our presence
        sendMessage("Hello Ad-Hoc World ");
    }

    /**
     * Starts jxta
     *
     */
    private void startJxta() throws IOException {
        try {
            // Set the peer name
            ConfigurationFactory.setName("JXME jxme JXME jxme JXME jxme");
            ConfigurationFactory.setTCPPortRange(9900, 9910);
            // Configure the platform
            Advertisement config = ConfigurationFactory.newPlatformConfig();
            // save it in the default directory $cwd/.jxta
            ConfigurationFactory.save(config, false);
            // create, and Start the default jxta NetPeerGroup
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
            rendezvous = netPeerGroup.getRendezVousService();
            // register for rendezvous events
            rendezvous.addListener(this);
            if (rendezvous.isConnectedToRendezVous()) {
                System.out.println("Connected to a rendezvous");
            }
            //System.out.println(netPeerGroup.getPeerAdvertisement().toString());
        }
        catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Display messages as they arrive, if the message contains the string "jxme"
     * respond with a greeting
     */

    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg=null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            if (msg == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String senderName = "unknown";

        // Get originator's name
        MessageElement nameEl = msg.getMessageElement(SenderName);
        if (nameEl != null) {
            senderName = nameEl.toString();
        }

        // now the message
        String senderMessage = null;
        MessageElement msgEl = msg.getMessageElement(SenderMessage);
        if (msgEl != null) {
            senderMessage = msgEl.toString();
        } else {
            System.out.println("received an unknown message");
            return;
        }

        // Get message
        if (senderMessage == null) {
            senderMessage = "[empty message]";
        }

        System.out.println(senderName + "> " + senderMessage);
        if (senderMessage.toUpperCase().indexOf("JXME") >= 0 &&
            senderMessage.toUpperCase().indexOf("MEM") < 0) {
            sendMessage("Greetings " + senderName);
        }
        if (senderMessage.toUpperCase().indexOf("JXME") >= 0 &&
            senderMessage.toUpperCase().indexOf("MEM") >= 0) {
            sendMessage(senderName+", my mem stats are :" + getMemStats());
        }
    }

    private void sendMessage(String gram) {
        Message response = new Message();
        //The gram
        response.addMessageElement(new StringMessageElement(SenderMessage,
                                   gram,
                                   null));
        //Our name
        response.addMessageElement(new StringMessageElement(SenderName,
                                   "jxme",
                                   null));
        try {
            //Send the message
            output.send(response);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * This method generates a pipe advertisment which is equivlant to instantp2p,
     * shell, and MyJxta2 propagated pipe
     *
     * @return    The group chat PipeAdvertisement
     */
    private PipeAdvertisement getMyJxtaPipeAdv() {

        byte [] preCookedPID = {
                                   (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                   (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                   (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                   (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1
                               };

        PipeID id= (PipeID) IDFactory.newPipeID(netPeerGroup.getPeerGroupID(), preCookedPID);
        PipeAdvertisement pipeAdv = (PipeAdvertisement)
                                    AdvertisementFactory.newAdvertisement(
                                        PipeAdvertisement.getAdvertisementType());
        pipeAdv.setPipeID(id);
        // the name really does not matter here, only for illustration
        pipeAdv.setName("test");
        pipeAdv.setType(PipeService.PropagateType);
        return pipeAdv;
    }
    /**
     *  rendezvousEvent the rendezvous event
     *
     *@param  event   rendezvousEvent
     */
    public void rendezvousEvent(RendezvousEvent event) {
        if (event.getType() == event.RDVCONNECT ||
                event.getType() == event.RDVRECONNECT) {
            System.out.println("Connected to a rendezvous");
            Enumeration rdvs = rendezvous.getConnectedRendezVous();
            while (rdvs.hasMoreElements()) {
                ID pid = (PeerID) rdvs.nextElement();
                System.out.println("Connected to :"+pid);
            }
        }
        sendMessage("Now that I am part of the network, Hello world again");
    }
    private String getMemStats() {
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        long freeHeap = Runtime.getRuntime().freeMemory();
        long totalHeap = Runtime.getRuntime().totalMemory();
        long maxHeap = Runtime.getRuntime().maxMemory();
        return new String("Free Heap :"+ freeHeap +" Total Heap :"+totalHeap+" Max Heap :"+maxHeap);
    }
    
    private void printMemStat() {
        System.out.println(getMemStats());
    }
}

