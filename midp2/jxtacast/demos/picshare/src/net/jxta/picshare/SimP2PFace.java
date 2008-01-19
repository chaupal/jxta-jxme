/*
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
 * information on Project JXTA, please see <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: SimP2PFace.java,v 1.6 2006/03/07 20:44:52 tra Exp $
 *
 */

package net.jxta.picshare;

import java.util.*;

import net.jxta.discovery.*;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.impl.protocol.PeerGroupAdv;
import net.jxta.impl.protocol.PeerAdv;
import net.jxta.impl.id.UUID.*;
import net.jxta.document.*;

/*
 * SimP2PFace: Simulated peer-to-peer protocol interface.
 *
 *              Class used to simulate JXTA functions.  Can be used to run the
 *              demo without JXTA, on a standalone machine.  No net connection
 *              needed.
 *
 */
public class SimP2PFace implements P2PFace {

    static final protected String groupNames[] = {"ChowHounds", "Giants", "Help Wanted"};
    static final protected String rootPeerNames[] = {"Trillin", "Barry", "JollyGreen",
                                                     "Heidi", "JoeBob", "TheyMightBe",
                                                     "RollyStokers", "Cat", "TempJobs"};

    static final protected String grp0PeerNames[] = {"Trillin", "JollyGreen", "Heidi"};
    static final protected String grp1PeerNames[] = {"Barry", "JollyGreen", "TheyMightBe",
                                                     "RollyStokers"};
    static final protected String grp2PeerNames[] = {"JoeBob", "TempJobs"};

    protected PeerGroup    rootGroup;     // NetPeerGroup simulation.
    protected PeerGroupAdvertisement rootGroupAdv;  // "NetPeerGroup" advertisement.
    protected PeerAdv myPeerAdv;          // Peer advertisement about me.
    protected Vector groups;              // Peer groups that we've "found".   PeerGroupAdvertisement objects.
    protected Vector joinedGroups;        // Peer groups that we've "joined".  JoinedGroup objects.
    protected Vector rootPeers;           // Peers in the root group.

    protected PeerAdv.Instantiator peerAdvInst;  // Instantiator of PeerAdv objects.


    public SimP2PFace() {

        joinedGroups = new Vector(10);

        // Create a simulation of the "NetPeerGroup".
        rootGroupAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());
        rootGroupAdv.setName("NetPeerGroup");
        rootGroupAdv.setPeerGroupID(new PeerGroupID());

        // Create my peer advertisement.
        peerAdvInst = new PeerAdv.Instantiator();
        myPeerAdv = (PeerAdv)peerAdvInst.newInstance();
        myPeerAdv.setName("Sam");
        myPeerAdv.setPeerID(new PeerID((PeerGroupID)rootGroupAdv.getPeerGroupID()));
        myPeerAdv.setPeerGroupID(rootGroupAdv.getPeerGroupID());

        // Create the root peer group.
        rootGroup = new SimPeerGroup(rootGroupAdv, myPeerAdv);

        // Create some simulated peer groups.
        PeerGroupAdvertisement group;
        groups = new Vector(5);
        for (int i = 0; i < groupNames.length; i++) {
            group = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                    PeerGroupAdvertisement.getAdvertisementType());
            group.setName(groupNames[i]);
            group.setPeerGroupID(new PeerGroupID());
            groups.add(group);
        }

        // Add some peers into the NetPeerGroup.
        rootPeers = new Vector(rootPeerNames.length);
        createPeers(rootPeerNames, rootGroupAdv, rootPeers);
    }


    /** Return my own peer name. */
    public String getMyPeerName() {
        return myPeerAdv.getName();
    }


    /** Return advertisement for the default (initial) peer group. */
    public PeerGroupAdvertisement getDefaultAdv() {
        return rootGroupAdv;
    }


    /** Return advertisement for my peer. */
    public PeerAdvertisement getMyPeerAdv() {
        return myPeerAdv;
    }


    /** Return the default (initial) peer group.
     */
    public PeerGroup getDefaultGroup() {
        return rootGroup;
    }


    /** Launch peer group discovery.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     */
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener) {

        // Trigger the listener, so it will pick up the groups we've
        // already defined.  The DiscoThread will trigger the listener after
        // a few seconds, to simulate a network delay.
        //
        DiscoThread thread = new DiscoThread(discoListener);
        thread.start();
    }


    /** Launch peer discovery, for the specified group.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param group         - peer group for which to find peers.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     */
    public void discoverPeers(String targetPeerId,
                              PeerGroupAdvertisement group,
                              DiscoveryListener discoListener) {

        // Trigger the listener, so it will pick up the peers we've
        // defined.  The DiscoThread will trigger the listener after
        // a few seconds, to simulate a network delay.
        //
        DiscoThread thread = new DiscoThread(discoListener);
        thread.start();
    }


    /** Launch advertisement discovery, for the specified group.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param group         - peer group for which to find advertisements.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     *  @param attribute     - Limit responses to advertisements with this attribute/value pair.
     *                         Set to null to place no limit.
     *  @param value         - See 'attribute', above.
     */
    public void discoverAdvertisements(String targetPeerId,
                                       PeerGroupAdvertisement group,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value) {

        // Trigger the listener, so it will pick up the advs we've
        // defined.  The DiscoThread will trigger the listener after
        // a few seconds, to simulate a network delay.
        //
        DiscoThread thread = new DiscoThread(discoListener);
        thread.start();
    }


    /** Return an enerator to an array of PeerGroupAdvertisement objects
     *  representing the groups known so far.
     *  Note this doesn't include the default "NetPeerGroup".
     */
    public Enumeration getKnownGroups() {
        return groups.elements();
    }


    /** Return an enerator to an array of PeerAdvertisement objects
     *  representing the peers known so far, that are members of the specified
     *  peer group.
     */
    public Enumeration getKnownPeers(PeerGroupAdvertisement group) {

        // If it's the root group adv, return the root peer set.
        if (group.getPeerGroupID().equals(rootGroupAdv.getPeerGroupID()))
            return rootPeers.elements();

        // If it's a group that we've joined, return its peer set.
        JoinedGroup joinedGroup = findJoinedGroup(group);
        if (joinedGroup != null)
            return joinedGroup.peers.elements();

        return null;
    }


    /** Return an enerator to an array of Advertisement objects
     *  representing the advs known so far, that were created within the
     *  specified peer group.  The list can be narrowed to advs matching
     *  an attribute/value pair.
     *
     *  @param group     - peer group for which to retrieve advertisements.
     *  @param attribute - Limit responses to advertisements with this attribute/value pair.
     *                     Set to null to place no limit.
     *  @param value     - See 'attribute', above.
     */
    public Enumeration getKnownAdvertisements(PeerGroupAdvertisement group,
                                              String attribute,
                                              String value) {

        // For now just return a list of the peer advertisements for this group,
        // and ignore the attr/value spec.
        return getKnownPeers(group);
    }


    /** Join the specified peergroup.
     *  @param  groupAdv      Advertisement of the group to join.
     *  @param  beRendezvous  If true, act as a rendezvous for this group.
     *  @return PeerGroup if we were successfully able to join the group, or
     *          if we had already joined it.
     *          null if we were unable to join the group.
     */
    public PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv,
                                   boolean beRendezvous) {

        // If it's the root group adv, it's already joined by default.
        if (groupAdv.getPeerGroupID().equals(rootGroupAdv.getPeerGroupID()))
            return rootGroup;

        // See if we've already joined this group.
        JoinedGroup joinedGroup = findJoinedGroup(groupAdv);
        if (joinedGroup != null)
            return joinedGroup.peerGroup;

        // Create the group, and add some peers to it.
        SimPeerGroup newGroup = new SimPeerGroup(groupAdv, myPeerAdv);
        joinedGroup = new JoinedGroup(groupAdv, newGroup);

        // Add different peers, depending on the group.
        // FIXME - with this system, the peers don't have the same peer ID's
        // as the peers with the same name in the NetPeerGroup.
        if (groupAdv.getName().equals(groupNames[0]))
            createPeers(grp0PeerNames, groupAdv, joinedGroup.peers);
        else if (groupAdv.getName().equals(groupNames[1]))
            createPeers(grp1PeerNames, groupAdv, joinedGroup.peers);
        else if (groupAdv.getName().equals(groupNames[2]))
            createPeers(grp2PeerNames, groupAdv, joinedGroup.peers);

        // Add the new group to our list of joined groups.
        joinedGroups.add(joinedGroup);

        return newGroup;
    }


    /** Create and join a new PeerGroup.  Also publishes the group
     *  advertisement.
     *
     *  @param  groupName     Name for the new group.
     *  @param  description   Group description.
     *  @param  beRendezvous  If true, act as a rendezvous for this group.
     *  @return The new peer group if successful, otherwise null.
     */
    public PeerGroup createNewGroup(String groupName, String description,
                                    boolean beRendezvous) {

        PeerGroupAdvertisement adv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());
        adv.setName(groupName);
        adv.setPeerGroupID(new PeerGroupID());
        groups.add(adv);

        return joinPeerGroup(adv, beRendezvous);
    }


    /** Is the indicated peer a rendezvous? */
    public boolean isRendezvous(PeerAdvertisement peerAdv) {
        return false;  // FIXME
    }


    /** Create some fake peers in the specified group, using the supplied
     *  array of peer names, into the supplied vector.
     */
    protected void createPeers(String[] names, PeerGroupAdvertisement group,
                               Vector peers) {

        PeerAdvertisement peer;
        for (int i = 0; i < names.length; i++) {
            peer = (PeerAdv)peerAdvInst.newInstance();
            peer.setPeerGroupID(group.getPeerGroupID());
            peer.setPeerID(new PeerID((PeerGroupID)group.getPeerGroupID()));
            peer.setName(names[i]);

            peers.add(peer);
        }
    }


    /** Search our array of joined groups for the requested group.
     *  @return JoinedGroup, or null if not found.
     */
    protected JoinedGroup findJoinedGroup(PeerGroupAdvertisement groupAdv) {

        JoinedGroup group = null;

        // Step thru the groups we've created, looking for one that has the
        // same peergroup ID as the requested group advertisement.
        //
        Enumeration myGroups = joinedGroups.elements();
        while (myGroups.hasMoreElements()) {
            group = (JoinedGroup)myGroups.nextElement();

            // If these match, we found it.
            if (group.groupAdv.getPeerGroupID().equals(groupAdv.getPeerGroupID()))
                return group;
        }

        // Didn't find it.
        return null;
    }


    /** Class to hold a peergroup that we've joined, along with a list of
     *  peers that we've 'discovered'.
     */
    class JoinedGroup extends Object {

        public PeerGroupAdvertisement groupAdv;
        public PeerGroup peerGroup;
        public Vector peers;

        public JoinedGroup (PeerGroupAdvertisement groupAdv, PeerGroup peerGroup) {
            this.groupAdv = groupAdv;
            this.peerGroup = peerGroup;
            peers = new Vector(10);
        }
    }


    /** Thread to simulate the discovery process.  In the simulation,
     *  its only job is to implement a time delay before triggering
     *  the discovery listener, to simulate the network search time.
     *  A separate thread is needed because we don't want to hold up
     *  the main thread's UI & animation.
     */
    class DiscoThread extends Thread {

        DiscoveryListener listener;

        public DiscoThread(DiscoveryListener listener) {
            this.listener = listener;
        }

        public void run() {
            try {sleep(5000);} catch (Exception e) {}
            if (listener != null)
                listener.discoveryEvent(null);
        }
    }
}
