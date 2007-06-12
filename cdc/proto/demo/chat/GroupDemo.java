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
 *  $Id: GroupDemo.java,v 1.10 2005/09/15 02:13:30 hamada Exp $
 */
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.rendezvous.RendezVousService;
/**
 * The intent of this demo is to illustrate platform configuration
 * and creation. Then it goes onto illustrating how use a propagate
 * for group chat.
 */

public class GroupDemo implements PipeMsgListener, RendezvousListener {

    static PeerGroup netPeerGroup = null;
    static PeerGroup myJXTA = null;
    private final static String SenderMessage = "JxtaTalkSenderMessage";
    private final static String SenderName = "JxtaTalkSenderName";
    private final static String SENDERGROUPNAME = "GrpName";
    private final static String MYJXTA2ID = "urn:jxta:uuid-1989101414002000081518002844102602";

    private RendezVousService rendezvous;
    private PipeService pipeService;
    private DiscoveryService discovery;
    private PipeAdvertisement pipeAdv;
    private InputPipe input = null;
    private OutputPipe output = null;
    private Map names = new HashMap();

    /**
     *  main
     *
     * @param  args  command line args
     */
    public static void main(String args[]) {
        GroupDemo groupDemo = new GroupDemo();
        try {
            groupDemo.startJxta();
            myJXTA = groupDemo.joinMyJXTA();
            groupDemo.start(myJXTA);
            groupDemo.printMemStat();
        } catch (IOException io) {
            io.printStackTrace();
        }
        synchronized (groupDemo) {
            //The intention with the wait is to ensure the app continues to run
            try {
                groupDemo.wait();
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
        }
    }


    /**
     *  Starts this demo's services (mainly the chat)
     *
     * @param  group            The PeerGroup to start in
     * @exception  IOException  if an I/O occurs
     */
    private void start(PeerGroup group) throws IOException {
        PipeAdvertisement padv = getMyJxtaPipeAdv(group);
        pipeService = group.getPipeService();
        rendezvous = group.getRendezVousService();
        // register for rendezvous events
        rendezvous.addListener(this);
        discovery = netPeerGroup.getDiscoveryService();

        //Create the input pipe with this app as the message listener for this pipe
        input = pipeService.createInputPipe(padv, this);
        //This pipe is a propagated pipe, therefore also bind to it
        output = pipeService.createOutputPipe(padv, 1);
        if (rendezvous.isConnectedToRendezVous()) {
            System.out.println("Connected to a rendezvous");
            Enumeration rdvs = rendezvous.getConnectedRendezVous();
            while (rdvs.hasMoreElements()) {
                ID pid = (PeerID) rdvs.nextElement();
                System.out.println("Connected to :" + idToName(pid));
            }
        }
        sendMessage("Greetings MyJXTA2 World");
    }


    /**
     * Starts jxta
     *
     * @exception  IOException  Description of the Exception
     */
    private void startJxta() throws IOException {
        try {
            // Set the peer name
            ConfigurationFactory.setName("JXME.GroupDemo");
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
        } catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Display messages as they arrive, if the message contains the string "jxme"
     * respond with a greeting
     *
     * @param  event  Message event
     */

    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg = null;
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


    /**
     *  Sends a datagram
     *
     * @param  gram  message to send
     */
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
    private PipeAdvertisement getMyJxtaPipeAdv(PeerGroup group) {

        byte[] preCookedPID = {
                                  (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                  (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                  (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
                                  (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1
                              };

        PipeID id = (PipeID) IDFactory.newPipeID(group.getPeerGroupID(), preCookedPID);
        PipeAdvertisement pipeAdv = (PipeAdvertisement)
                                    AdvertisementFactory.newAdvertisement(
                                        PipeAdvertisement.getAdvertisementType());
        pipeAdv.setPipeID(id);
        // the name really does not matter here, only for illustration
        pipeAdv.setName("JxtaTalkUserName.IP2PGRP");
        pipeAdv.setType(PipeService.PropagateType);
        return pipeAdv;
    }


    /**
     *  rendezvousEvent the rendezvous event
     *
     * @param  event  rendezvousEvent
     */
    public void rendezvousEvent(RendezvousEvent event) {
        if (event.getType() == event.RDVCONNECT ||
                event.getType() == event.RDVRECONNECT) {
            System.out.println("NetPeerGroup rendezvous connection");
            Enumeration rdvs = netPeerGroup.getRendezVousService().getConnectedRendezVous();
            while (rdvs.hasMoreElements()) {
                ID pid = (PeerID) rdvs.nextElement();
                System.out.println("Rendezvous : [" + idToName(pid)+"]");
            }
            System.out.println("MyJXTA rendezvous connection");
            rdvs = myJXTA.getRendezVousService().getConnectedRendezVous();
            while (rdvs.hasMoreElements()) {
                ID pid = (PeerID) rdvs.nextElement();
                System.out.println("Rendezvous : [" + idToName(pid)+"]");
            }

        }
        sendMessage("Hello MyJXTA2 world");
        sendMessage("You can ping me by addressing messages to me");
    }

    /**
     *  Creates the MyJXTA2 PeerGroupAdvertisment, and publishes ModuleImplAdvertisement for the group 
     *
     * @param  group  Parent peer group
     * @param  name   group name
     * @return        A PeerGroupAdvertisement ready to be joined
     */
    private PeerGroupAdvertisement createGroupAdv(PeerGroup group, String name) {
        try {
            DiscoveryService discovery = group.getDiscoveryService();
            ModuleImplAdvertisement groupImplAdv =
                group.getAllPurposePeerGroupImplAdvertisement();
            PeerGroupAdvertisement adv = (PeerGroupAdvertisement)
                                         AdvertisementFactory.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
            adv.setName(name);
            adv.setModuleSpecID(groupImplAdv.getModuleSpecID());
            PeerGroupID id = null;
            id = (PeerGroupID) IDFactory.fromURI(new URI(MYJXTA2ID));
            adv.setPeerGroupID(id);
            adv.setDescription("created by jxme");
            discovery.publish(adv);
            discovery.remotePublish(adv);
            discovery.publish(groupImplAdv);
            discovery.remotePublish(groupImplAdv);
            return adv;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     *  Joins the MyJXTA2 PeerGroup
     *
     * @return    The PeerGroup object
     */
    public PeerGroup joinMyJXTA() {
        try {
            PeerGroupAdvertisement adv = createGroupAdv(netPeerGroup, "MyJXTA2");
            myJXTA = netPeerGroup.newGroup(adv);
            RendezVousService rendezvous = myJXTA.getRendezVousService();
            // register for rendezvous events
            rendezvous.addListener(this);
            if (rendezvous.isConnectedToRendezVous()) {
                System.out.println("Connected to a rendezvous");
            }
            return myJXTA;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *  Maps a peerid to a name
     *
     * @param  id  PeerID of interest
     * @return     Peer name
     */
    private String idToName(ID id) {

        String idstring = id.toString();
        String name = (String) names.get(id);
        if (null != name) {
            return name;
        }
        try {
            Enumeration res;
            if (id instanceof PeerID) {
                res = discovery.getLocalAdvertisements(DiscoveryService.PEER, "PID", idstring);
                if (res.hasMoreElements()) {
                    name = ((PeerAdvertisement) res.nextElement()).getName();
                }
            } else if (id instanceof PeerGroupID) {
                res = discovery.getLocalAdvertisements(DiscoveryService.GROUP, "GID", idstring);
                if (res.hasMoreElements()) {
                    name = ((PeerGroupAdvertisement) res.nextElement()).getName();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {}

                res = discovery.getLocalAdvertisements(DiscoveryService.PEER, "PID", idstring);
                if (res.hasMoreElements()) {
                    name = ((PeerAdvertisement) res.nextElement()).getName();
                }

            }
        } catch (IOException failed) {}
                   
        if (null != name) {
            names.put(id, name);
        } else {
            name = "["+id+"]";
        }
        return name;
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

