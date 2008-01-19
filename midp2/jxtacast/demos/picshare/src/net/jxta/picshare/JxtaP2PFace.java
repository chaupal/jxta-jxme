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
 * $Id: JxtaP2PFace.java,v 1.5 2005/04/26 19:06:50 hamada Exp $
 *
 */

package net.jxta.picshare;

import javax.swing.*;
import java.util.*;
import java.io.IOException;

import net.jxta.discovery.*;
import net.jxta.document.MimeMediaType;
import net.jxta.exception.PeerGroupException;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.*;
import net.jxta.protocol.*;
import net.jxta.rendezvous.*;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.Advertisement;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;



/*
 * JxtaP2PFace: JXTA peer-to-peer protocol interface.
 *
 *              Class used to access JXTA functionality.  This puts most of
 *              JXTA related code here, separate from the UI.  (Some error
 *              reporting is done through Swing message boxes, however.)
 *
 */
public class JxtaP2PFace implements P2PFace {

    protected PeerGroup rootGroup;
    protected Vector joinedGroups;  // Holds PeerGroup objects that we've joined.


    /** Constructor - Starts JXTA.
     *
     *  @param rdvListener - can be null if you don't want rdv event notification.
     */
    public JxtaP2PFace(RendezvousListener rdvListener) {

        // Start JXTA.
        try {
            // Create, and start the default jxta NetPeerGroup.
            rootGroup = PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException e) {
            // Very bad trouble: bail out.
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(-1);
        }

        // If we've been given a Rendezvous listener, add it to the 
        // NetPeerGroup.
        rootGroup.getRendezVousService().addListener(rdvListener);

        joinedGroups = new Vector(20);
        joinedGroups.add(rootGroup);
    }


    /** Return my own peer name. */
    public String getMyPeerName() {
        return rootGroup.getPeerAdvertisement().getName();
    }


    /** Return advertisement for the default (initial) peer group. */
    public PeerGroupAdvertisement getDefaultAdv() {
        return rootGroup.getPeerGroupAdvertisement();
    }


    /** Return advertisement for my peer. */
    public PeerAdvertisement getMyPeerAdv() {
        return rootGroup.getPeerAdvertisement();
    }


    /** Return the default (initial) peer group. */
    public PeerGroup getDefaultGroup() {
        return rootGroup;
    }


    /** Launch peer group discovery.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     */
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener) {

        DiscoveryService disco = rootGroup.getDiscoveryService();
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.GROUP,
                                             null,
                                             null,
                                             discoListener);
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

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
        PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return;

        DiscoveryService disco = pg.getDiscoveryService();
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.PEER,
                                             null,
                                             null,
                                             discoListener);
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

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
        PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return;

        DiscoveryService disco = pg.getDiscoveryService();
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.ADV,
                                             attribute,
                                             value,
                                             discoListener);
        thread.start();
    }


    /** Return an enerator to an array of PeerGroupAdvertisement objects
     *  representing the groups known so far.
     *  Note this doesn't include the default "NetPeerGroup".
     */
    public Enumeration getKnownGroups() {

        Enumeration en = null;
        DiscoveryService disco = rootGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.GROUP, null, null);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        return en;
    }


    /** Return an enerator to an array of PeerAdvertisement objects
     *  representing the peers known so far, that are members of the specified
     *  peer group.
     */
    public Enumeration getKnownPeers(PeerGroupAdvertisement group) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
        PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return null;

        Enumeration en = null;
        DiscoveryService disco = pg.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.PEER, null, null);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        return en;
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

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
        PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return null;

        Enumeration en = null;
        DiscoveryService disco = pg.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.ADV, attribute, value);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        return en;
    }


    /** Join the specified PeerGroup.
     *  @param  groupAdv      Advertisement of the group to join.
     *  @param  beRendezvous  If true, act as a rendezvous for this group.
     *  @return PeerGroup if we were successfully able to join the group, or
     *          if we had already joined it.
     *          null if we were unable to join the group.
     */
    public synchronized PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv,
                                                boolean beRendezvous) {

        // See if it's a group we've already joined.
        PeerGroup newGroup = findJoinedGroup(groupAdv);
        if (newGroup != null)
            return newGroup;

        // Join the group.  This is done by creating a PeerGroup object for
        // the group and initializing it with the group advertisement.
        //
        try {
            newGroup = rootGroup.newGroup(groupAdv);
        } catch (PeerGroupException e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "Unable to join group.\n" +
                                          e.toString(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        authenticateMembership(newGroup);

        // We'll be a rendezvous in the new group, if requested.
        if (beRendezvous)
            newGroup.getRendezVousService().startRendezVous();

        // Advertise that we've joined this group.
        DiscoveryService disco = newGroup.getDiscoveryService();
        try {
            // Publish our advertisements.  Is all of this really needed?
            disco.publish(newGroup.getPeerGroupAdvertisement());
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        // Add the new group to our list of joined groups.
        joinedGroups.add(newGroup);

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

        PeerGroup pg;               // new peer group
        PeerGroupAdvertisement adv; // advertisement for the new peer group
        
        try {
            // Create a new all purpose peergroup.
            ModuleImplAdvertisement implAdv =
                rootGroup.getAllPurposePeerGroupImplAdvertisement();
            
            pg = rootGroup.newGroup(null,         // Assign new group ID
                                    implAdv,      // The implem. adv
                                    groupName,    // The name
                                    description); // Helpful descr.
        }
        catch (Exception e) {
            System.out.println("Group creation failed with " + e.toString());
            JOptionPane.showMessageDialog(null, "Unable to create group.\n" +
                                          e.toString(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        authenticateMembership(pg);

        // We'll be a rendezvous in the new group, if requested.
        if (beRendezvous)
            pg.getRendezVousService().startRendezVous();

        // Advertise that we've created this group.
        DiscoveryService disco = rootGroup.getDiscoveryService();
        try {
            // Not sure how much of this is needed; this might be overkill.
            disco.publish(pg.getPeerGroupAdvertisement());
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        // Add the new group to our list of joined groups.
        joinedGroups.add(pg);

        return pg;
    }


    /** Authenticate membershipe in a peer group.
     *  @return true if successful.
     */
    public boolean authenticateMembership(PeerGroup group) {

        StructuredDocument creds = null;
        try {
            // Generate the credentials for the Peer Group.
            AuthenticationCredential authCred = 
                new AuthenticationCredential(group, null, creds);

            // Get the MembershipService from the peer group.
            MembershipService membership = group.getMembershipService();

            // Get the Authenticator from the Authentication creds.
            Authenticator auth = membership.apply(authCred);

            // Check if everything is okay to join the group.
            if (auth.isReadyForJoin()) {
                Credential myCred = membership.join(auth);
            }
            else {
                System.out.println("Failure: unable to join group.  " +
                                   "Authenticator not ready.");
                JOptionPane.showMessageDialog(null, "Unable to join group.\n" +
                                              "Authenticator not ready.", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e) {
            System.out.println("Failure in authentication.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to authenticate group membership.\n" +
                                          e.toString(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }


    /** Is the indicated peer a rendezvous? */
    public boolean isRendezvous(PeerAdvertisement peerAdv) {

        // Find the PeerGroup object for this group.
        PeerGroup group = findJoinedGroup(peerAdv.getPeerGroupID());
        if (group == null)
            return false;

        // Are we checking for our own peer?  If so, we can just ask the
        // PeerGroup object if we are a rendezvous.
        if (peerAdv.getPeerID().equals(rootGroup.getPeerAdvertisement().getPeerID()))
            return group.isRendezvous();

        // Get the RendezVousService from the PeerGroup.
        RendezVousService rdv = (RendezVousService)group.getRendezVousService();
        if (rdv == null)
            return false;

        // Get a list of the connected rendezvous peers for this group, and
        // search it for the requested peer.
        //
        PeerID peerID = null;
        Enumeration rdvs = null;
        rdvs = rdv.getConnectedRendezVous();
        while (rdvs.hasMoreElements()) {
            try {
                peerID = (PeerID)rdvs.nextElement();
                if (peerID.equals(peerAdv.getPeerID()))
                    return true;
            } catch (Exception e) {}
        }

        // We'll search the disconnected rendezvous, also.
        rdvs = rdv.getDisconnectedRendezVous();
        while (rdvs.hasMoreElements()) {
            try {
                peerID = (PeerID)rdvs.nextElement();
                if (peerID.equals(peerAdv.getPeerID()))
                    return true;
            } catch (Exception e) {}
        }

        // Didn't find it, the peer isn't a rendezvous.
        return false;
    }


    /** Search our array of joined groups for the requested group.
     *  @return PeerGroup, or null if not found.
     */
    protected PeerGroup findJoinedGroup(PeerGroupAdvertisement groupAdv) {
        return findJoinedGroup(groupAdv.getPeerGroupID());
    }


    /** Search our array of joined groups for the requested group.
     *  @return PeerGroup, or null if not found.
     */
    protected PeerGroup findJoinedGroup(PeerGroupID groupID) {

        PeerGroup group = null;

        // Step thru the groups we've created, looking for one that has the
        // same peergroup ID as the requested group.
        //
        Enumeration myGroups = joinedGroups.elements();
        while (myGroups.hasMoreElements()) {
            group = (PeerGroup)myGroups.nextElement();

            // If these match, we found it.
            if (group.getPeerGroupID().equals(groupID))
                return group;
        }

        // Didn't find it.
        return null;
    }


    /** Thread to perform the discovery process.
     *  A separate thread is needed because we don't want to hold up
     *  the main thread's UI & animation.  The JXTA remote discovery call
     *  can take several seconds.
     */
    class DiscoThread extends Thread {

        DiscoveryService disco;
        int type;
        String targetPeerId;
        String attribute;
        String value;
        DiscoveryListener discoListener;

        public DiscoThread(DiscoveryService disco,
                           String targetPeerId,
                           int type,
                           String attribute,
                           String value,
                           DiscoveryListener discoListener) {
            this.disco = disco;
            this.type = type;
            this.discoListener = discoListener;

            if (targetPeerId != null)
                this.targetPeerId = new String(targetPeerId);
            if (attribute != null)
                this.attribute = new String(attribute);
            if (value != null)
                this.value = new String(value);
        }

        public void run() {
            disco.getRemoteAdvertisements(targetPeerId, type, attribute,
                                          value, 10, discoListener);
        }
    }
}
