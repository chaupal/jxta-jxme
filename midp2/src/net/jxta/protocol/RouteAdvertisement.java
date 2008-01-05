/*
 *  Copyright (c) 2001-2008 Sun Microsystems, Inc.  All rights
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
 *  $Id: $
 */
package net.jxta.protocol;

import com.sun.java.util.collections.*;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;

import java.util.Vector;

/**
 * This type of advertisement is used to represent a route to a destination
 * peer in the JXTA virtual network. Routes are represented in a generic manner
 * as a sequence of hops to reach the destination. Each hop represent a
 * potential relay peer in the route: <pre> Dest
 *       hop 1
 *       hop 2
 *       hop 3
 *       ....
 *       hop n
 * </pre> <p/>
 * <p/>
 * A route can have as many hops as necessary. Hops are implicitly ordered
 * starting from the hop with the shortest route to reach the destination. If a
 * peer cannot reach directly the dest, it should try to reach in descending
 * order one of the listed hops. Some hops may have the same physical distance
 * to the destination. Some hops may define alternative route <p/>
 * <p/>
 * The destination and hops are defined using an AccessPoint Advertisement as
 * JXTA PeerIDs with a list of optional endpoint addresses. The endpoint
 * addresses defined the physical endpoint addresses that can be used to reach
 * the corresponding hop. The PeerID information is required. The endpoint
 * address information is optional.
 *
 * @see PeerAdvertisement
 * @see AccessPointAdvertisement
 */
public abstract class RouteAdvertisement extends ExtendableAdvertisement {

    /**
     * Description of the Field
     */
    public final static String DEST_PID_TAG = "DstPID";

    /**
     * Destination address
     */
    private AccessPointAdvertisement dest = null;

    /**
     * Destination address
     */
    private PeerID destPeer = null;
    private transient ID hashID = null;

    /**
     * Alternative hops to the destination
     */
    private List hops = Collections.synchronizedList(new ArrayList());
//    private Vector hops = new Vector();
    /**
     * Description of the Field
     */
    protected Map indexMap = new HashMap();

    /**
     * add a new list of EndpointAddresses to the Route Destination access
     * point
     *
     * @param addresses vector of endpoint addresses to add to the destination
     *                  access point. Warning: The vector of endpoint addresses is specified
     *                  as a vector of String. Each string representing one endpoint
     *                  address.
     * @deprecated Use {@link #getDest()} and modify
     *             AccessPointAdvertisement directly.
     */

    public void addDestEndpointAddresses(List addresses) {
        dest.addEndpointAddresses(addresses);
    }

    /**
     * Add a new endpointaddress to a hop
     *
     * @param pid  id of the hop
     * @param addr new endpoint address to add
     */
    public void addEndpointAddressToHop(PeerID pid, EndpointAddress addr) {
        List ea = new ArrayList();
        ea.add(addr.toString());

        AccessPointAdvertisement oldHop = getHop(pid);
        if (oldHop != null && !oldHop.contains(addr)) {
            oldHop.addEndpointAddresses(ea);
            replaceHop(oldHop);
        }
    }

    /**
     * Remove loops from the route advertisement by shortcuting cycle from the
     * route
     *
     * @param route     Description of the Parameter
     * @param localPeer Description of the Parameter
     */
    public static void cleanupLoop(RouteAdvertisement route, PeerID localPeer) {

        // Note: we cleanup all enp addresses except for the last hop (which
        // we use to shorten routes often enough).
        // If we end-up removing the last hop, it means that it is the
        // local peer and thus the route ends up with a size 0.

        List hops = route.getVectorHops();
        List newHops = new ArrayList(hops.size());
        Object lastHop = null;

        // Replace all by PID-only entries, but keep the last hop on the side.
        if (hops.size() > 0) {
            lastHop = hops.get(hops.size() - 1);
        }
        hops = route.getVectorHops();
        // remove cycle from the route
        for (int i = 0; i < hops.size(); i++) {
            int loopAt = newHops.indexOf(hops.get(i));
            if (loopAt != -1) {
                // we found a cycle

                // remove all entries after loopAt
                for (int j = newHops.size(); --j > loopAt;) {
                    newHops.remove(j);
                }
            } else {
                // did not find it so we add it
                newHops.add(hops.get(i));
            }
        }

        // Remove the local peer in the route if we were given one
        if (localPeer != null) {
            for (int i = newHops.size(); --i >= 0;) {
                if (localPeer.equals(((AccessPointAdvertisement) newHops.get(i)).getPeerID())) {
                    // remove all the entries up to that point we
                    // need to keep the remaining of the route from that
                    // point
                    for (int j = 0; j <= i; j++) {
                        newHops.remove(0);
                    }
                    break;
                }
            }
        }

        if (lastHop != null && newHops.size() > 0) {
            newHops.set(newHops.size() - 1, lastHop);
        }

        // update the new hops in the route
        route.setHops(newHops);
    }



    /**
     * Check if the route contains the following hop
     *
     * @param pid peer id of the hop
     * @return boolean true or false if the hop is found in the route
     */

    public boolean containsHop(PeerID pid) {
        for (Iterator it = hops.iterator(); it.hasNext();) {
            AccessPointAdvertisement hop = (AccessPointAdvertisement) it.next();
            PeerID hid = hop.getPeerID();

            if (hid == null) {
                continue;
                //may be null
            }

            if (pid.equals(hid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate a string that displays the route information for logging or
     * debugging purpose
     *
     * @return String return a string containing the route info
     */
    public String display() {

        StringBuffer routeBuf = new StringBuffer();

        routeBuf.append("Route to PID=");

        PeerID peerId = getDest().getPeerID();
        if (peerId == null) {
            routeBuf.append("Null Destination");
        } else {
            routeBuf.append(peerId.toString());
        }

        for (Iterator it = getDest().getEndpointAddresses();
             it.hasNext();)
            try {
                routeBuf.append("\n Addr=").append((String) it.next());
            } catch (ClassCastException ex) {
                routeBuf.append("\n Addr=bad address");
            }

        int i = 1;
        for (Iterator it = getHops(); it.hasNext();) {
            if (i == 1) {
                routeBuf.append("\n Gateways = ");
            }
            peerId = ((AccessPointAdvertisement) it.next()).getPeerID();
            if (peerId == null) {
                routeBuf.append("Null Hop");
            } else routeBuf.append("\n\t[").append(i++).append("] ").append(peerId.toString());
        }
        return routeBuf.toString();
    }

    /**
     * Compare if two routes are equals. Equals means the same number of hops
     * and the same endpoint addresses for each hop and the destination
     *
     * @param target the route to compare against
     * @return boolean true if the route is equal to this route otherwise
     *         false
     */
    public boolean equals(Object target) {

        if (this == target) {
            return true;
        }

        if (!(target instanceof RouteAdvertisement)) {
            return false;
        }

        RouteAdvertisement route = (RouteAdvertisement) target;

        // check each of the hops
        // routes need to have the same size
        if (hops.size() != route.size()) {
            return false;
        }

        int index = 0;
        for (Iterator it = route.getHops(); it.hasNext();) {
            AccessPointAdvertisement hop = (AccessPointAdvertisement) it.next();
            if (!hop.equals(hops.get(index++))) {
                return false;
            }
        }

        if (dest == null && route.getDest() == null) {
            return true;
        }

        if (dest == null || route.getDest() == null) {
            return false;
        }

        // chek the destination
        return dest.equals(route.getDest());
    }

    /**
     * Returns the identifying type of this Advertisement.
     *
     * @return String the type of advertisement
     */
    public static String getAdvertisementType() {
        return "jxta:RA";
    }

    /**
     * {@inheritDoc}
     *
     * @return The baseAdvType value
     */
    public final String getBaseAdvType() {
        return getAdvertisementType();
    }

    /**
     * Returns the destination access point
     *
     * @return AccessPointAdvertisement
     */

    public AccessPointAdvertisement getDest() {
        return dest;
    }

    /**
     * Return the endpoint addresses of the destination
     *
     * @return vector of endpoint addresses as String. <b>This is live
     *         data.</b>
     * @deprecated Use {@link #getDest()} and modify AccessPointAdvertisement
     *             directly.
     */
    public List getDestEndpointAddresses() {
        return dest.getVectorEndpointAddresses();
    }

    /**
     * Returns the route destination Peer ID
     *
     * @return peerID of the destination of the route
     */

    public PeerID getDestPeerID() {
        return destPeer;
    }


    /**
     * Returns the access point for the first hop
     *
     * @return AccessPointAdvertisement first hop
     */

    public AccessPointAdvertisement getFirstHop() {
        if (hops == null || hops.isEmpty()) {
            return null;
        } else {
            return (AccessPointAdvertisement) hops.get(0);
        }
    }


    /**
     * return a hop from the list of hops
     *
     * @param pid peer id of the hop
     * @return accesspointadvertisement of the corresponding hop
     */
    public AccessPointAdvertisement getHop(PeerID pid) {
        for (Iterator it = hops.iterator(); it.hasNext();) {
            AccessPointAdvertisement hop = (AccessPointAdvertisement) it.next();
            PeerID hid = hop.getPeerID();
            if (hid != null) {
                if (pid.toString().equals(hid.toString())) {
                    return hop;
                }
            }
        }
        return null;
    }

    /**
     * Return hop of the route at location index in the hops list
     *
     * @param index in the list of hops
     * @return hop AccessPointAdvertisement of the hops
     */
    public AccessPointAdvertisement getHop(int index) {

        if (index < 0) {
            return null;
        }

        if (index > hops.size() - 1) {
            return null;
        }

        return (AccessPointAdvertisement) hops.get(index);
    }

    /**
     * returns the list of hops
     *
     * @return Enumeration list of hops as AccessPointAdvertisement
     */

    public Iterator getHops() {
        return hops.iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return The iD value
     */
    public synchronized ID getID() {
        if (hashID == null) {
            try {
                // We have not yet built it. Do it now
                // The group id is somewhat redundant since it is already
                // part of the peer ID, but that's the way CodatID want it.
                PeerID pid = dest.getPeerID();
                byte[] seed = getAdvertisementType().getBytes();

                hashID = IDFactory.newCodatID((PeerGroupID) pid.getPeerGroupID(),
                        seed);
            } catch (Exception ez) {
                return ID.nullID;
            }
        }
        return hashID;
    }

    /**
     * {@inheritDoc}
     *
     * @return The indexMap value
     */
    public final Map getIndexMap() {
        return indexMap;
    }

    /**
     * Returns the access point for the last hop
     *
     * @return AccessPointAdvertisement last hop
     */

    public AccessPointAdvertisement getLastHop() {
        if (hops == null || hops.isEmpty()) {
            return null;
        } else {
            if (hops.size() > 0)
                return (AccessPointAdvertisement) hops.get(hops.size() - 1);
            else
                return null;
        }
    }

    /**
     * returns the list of hops
     *
     * @return Vectorlist of hops as AccessPointAdvertisement
     */

    public List getVectorHops() {
        return hops;
    }

    public List getVectorHopsCopy() {
        return Collections.synchronizedList(new ArrayList(hops));
    }

    /**
     * check if the route has a loop
     *
     * @return boolean true or false if the route has a loop
     */

    public boolean hasALoop() {
        // Now check for any other potential loops.
        Vector peers = new Vector();
        for (int i = 0; i < hops.size(); ++i) {
            try {
                PeerID pid = ((AccessPointAdvertisement) hops.get(i)).getPeerID();
                if (pid == null) {
                    return true;
                }
                //bad route
                if (peers.contains(pid)) {
                    // This is a loop.
                    return true;
                } else {
                    peers.addElement(pid);
                }
            } catch (Exception ez1) {
                return true;
            }
        }
        return false;
    }

    /**
     * construct a new route <p/>
     * <p/>
     * <b>WARNING hops may be MODIFIED.</b>
     *
     * @param destPid  Description of the Parameter
     * @param firsthop Description of the Parameter
     * @param hops     Description of the Parameter
     * @return Description of the Return Value
     */
    public static RouteAdvertisement newRoute(PeerID destPid,
                                              PeerID firsthop,
                                              List hops) {

        RouteAdvertisement route = (RouteAdvertisement)
                AdvertisementFactory.newAdvertisement(
                        RouteAdvertisement.getAdvertisementType());

        // set the route destination
        AccessPointAdvertisement ap = (AccessPointAdvertisement)
                AdvertisementFactory.newAdvertisement(
                        AccessPointAdvertisement.getAdvertisementType());

        if (destPid == null) {
            return null;
        }
        // messed up destination
        ap.setPeerID(destPid);
        route.setDest(ap);

        // set the route hops
        for (Iterator it = hops.iterator(); it.hasNext();) {
            ap = (AccessPointAdvertisement) it.next();
            if (ap.getPeerID() == null) {
                return null;
            }
            // bad route
        }
        route.setHops(hops);

        // check if the given first hop is already in the route if not add it
        // (note: we do not expect it to be there, but it is acceptable).
        if (firsthop != null) {
            ap = route.getFirstHop();
            if (ap == null || !ap.getPeerID().equals(firsthop)) {
                ap = (AccessPointAdvertisement)
                        AdvertisementFactory.newAdvertisement(
                                AccessPointAdvertisement.getAdvertisementType());
                ap.setPeerID(firsthop);
                route.setFirstHop(ap);
            }
        }

        return route;
    }

    /**
     * construct a new route, all hops are in the hops parameter.
     *
     * @param destPid Description of the Parameter
     * @param hops    Description of the Parameter
     * @return Description of the Return Value
     */
    public static RouteAdvertisement newRoute(PeerID destPid, List hops) {
        return newRoute(destPid, null, hops);
    }

    /**
     * get the nexthop after the given hop
     *
     * @param pid PeerID of the current hop
     * @return ap AccessPointAdvertisement of the next Hop
     */

    public AccessPointAdvertisement nextHop(PeerID pid) {

        AccessPointAdvertisement nextHop;

        // check if we have a real route
        if ((hops == null) || (hops.size() == 0)) {
            // Empty vector.
            return null;
        }

        // find the index of the route
        int index = 0;
        boolean found = false;
        for (Iterator it = hops.iterator(); it.hasNext();) {
            AccessPointAdvertisement ap = (AccessPointAdvertisement) it.next();
            if (pid.toString().equals(ap.getPeerID().toString())) {
                found = true;
                break;
            }
            index++;
        }

        // check if we found the local peer within the vector

        if (!found) {
            // The peer is not into the vector. Since we have got that
            // message, the best we can do is to send it to the first gateway
            // in the forward path.
            try {
                nextHop = (AccessPointAdvertisement) hops.get(0);
            } catch (Exception ez1) {
                // Should not fail, but if it does, there is not much we can do
                return null;
            }
            return nextHop;
        }
        // Found the peer within the vector of hops. Get the next
        // hop
        try {
            nextHop = (AccessPointAdvertisement) hops.get(index + 1);
        } catch (Exception ez1) {
            // There is no next hop
            return null;
        }
        return nextHop;
    }

    /**
     * remove a list of EndpointAddresses from the Route Destination access
     * point
     *
     * @param addresses vector of endpoint addresses to remove from the
     *                  destination access point. Warning: The vector of endpoint addresses
     *                  is specified as a vector of String. Each string representing one
     *                  endpoint address.
     * @deprecated Use {@link #getDest()} and modify
     *             AccessPointAdvertisement directly.
     */

    public void removeDestEndpointAddresses(Vector addresses) {
        dest.removeEndpointAddresses(addresses);
    }

    /**
     * remove an endpointaddress to a hop
     *
     * @param pid  id of the hop
     * @param addr new endpoint address to remove
     */
    public void removeEndpointAddressToHop(PeerID pid, EndpointAddress addr) {
        Vector ea = new Vector();
        ea.addElement(addr.toString());
        AccessPointAdvertisement oldHop = getHop(pid);
        if (oldHop != null && !oldHop.contains(addr)) {
            oldHop.removeEndpointAddresses(ea);
            if (oldHop.size() > 0) {
                // we still have some endpoint addresses
                replaceHop(oldHop);
            } else {
                removeHop(pid);
            }
        }
    }

    /**
     * remove a hop from the list of hops
     *
     * @param pid peer id of the hop
     * @return boolean true or false if the hop is found in the route
     */
    public boolean removeHop(PeerID pid) {

        // FIXME: This is ridiculous, hops is a vector. We can remove
        // any item, we do not have to through the enum copying items 1 by 1.

        List newHops = new ArrayList();
        for (Iterator it = hops.iterator(); it.hasNext();) {
            AccessPointAdvertisement hop = (AccessPointAdvertisement) it.next();
            PeerID hid = hop.getPeerID();
            if (hid != null) {
                if (pid.toString().equals(hid.toString())) {
                    continue;
                }
            }
            // add the other one
            newHops.add(hop);
        }
        setHops(newHops);
        return true;
    }

    /**
     * replace a hop from the list of hops
     *
     * @param ap accesspointadvertisement of the hop to replace
     */
    public void replaceHop(AccessPointAdvertisement ap) {
        int index = 0;
        for (Iterator it = hops.iterator(); it.hasNext();) {
            AccessPointAdvertisement hop = (AccessPointAdvertisement) it.next();
            PeerID hid = hop.getPeerID();
            if (hid != null) {
                if (ap.getPeerID().toString().equals(hid.toString())) {
                    hops.set(index, ap);
                    return;
                }
            }
        }
    }

    /**
     * Sets the access point of the destination
     *
     * @param ap AccessPointAdvertisement of the destination peer
     */

    public void setDest(AccessPointAdvertisement ap) {
        this.dest = ap;
        if ((null != dest) && (null != dest.getPeerID())) {
            setDestPeerID(dest.getPeerID());
            indexMap.put(DEST_PID_TAG, dest.getPeerID().toString());
        } else {
            indexMap.remove(DEST_PID_TAG);
        }
    }

    /**
     * Set the route destination endpoint addresses
     *
     * @param ea vector of endpoint addresses. Warning: The vector of
     *           endpoint addresses is specified as a vector of String. Each string
     *           representing one endpoint address.
     * @deprecated Use {@link #getDest()} and modify AccessPointAdvertisement
     *             directly.
     */

    public void setDestEndpointAddresses(List ea) {
        dest.setEndpointAddresses(ea);
    }

    /**
     * Sets the route destination peer id
     *
     * @param pid route destination peerID
     */

    public void setDestPeerID(PeerID pid) {
        destPeer = pid;

        if (null != dest) {
            dest.setPeerID(pid);
        }
    }

    /**
     * Sets the access point for the first hop
     *
     * @param ap AccessPointAdvertisement of the first hop
     */

    public void setFirstHop(AccessPointAdvertisement ap) {
        hops.add(0, ap);
    }

    /**
     * sets the list of hops associated with this route
     *
     * @param hopsAccess Enumeration of hops as AccessPointAdvertisement The
     *                   vector of hops is specified as a vector of AccessPoint
     *                   advertisement.
     */
    public void setHops(List hopsAccess) {
        // It is legal to set it to null but it is automatically converted
        // to an empty vector. The member hops is NEVER null.
        hops = hopsAccess != null ? hopsAccess : Collections.synchronizedList(new ArrayList());
    }

    /**
     * Sets the access point for the last hop
     *
     * @param ap AccessPointAdvertisement of the last hop
     */

    public void setLastHop(AccessPointAdvertisement ap) {
        hops.add(ap);
    }

    /**
     * return the length of the route
     *
     * @return int size of the route
     */

    public int size() {
        return hops.size();
    }

    /**
     * Alter the given newRoute (which does not start from here) by using
     * firstLeg, a known route to whence it starts from. So that the complete
     * route goes from here to the end-destination via firstLeg. public static
     * boolean stichRoute(RouteAdvertisement newRoute,
     *
     * @param newRoute Description of the Parameter
     * @param firstLeg Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean stichRoute(RouteAdvertisement newRoute, RouteAdvertisement firstLeg) {
        return stichRoute(newRoute, firstLeg, null);
    }

    /**
     * Alter the given newRoute (which does not start from here) by using
     * firstLeg, a known route to whence it starts from. So that the complete
     * route goes from here to the end-destination via firstLeg also shortcut
     * the route by removing the local peer
     *
     * @param newRoute  Description of the Parameter
     * @param firstLeg  Description of the Parameter
     * @param localPeer Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean stichRoute(RouteAdvertisement newRoute,
                                     RouteAdvertisement firstLeg,
                                     PeerID localPeer) {

        if (newRoute.hasALoop()) {
            return false;
        }

        List hops = newRoute.getVectorHops();
        // Make room
//        hops.ensureCapacity(firstLeg.getVectorHops().size() + 1 + hops.size());

        // prepend the routing peer unless the routing peer happens to be
        // in the route already. That happens if the routing peer is the relay.
        // or if the route does not have a first leg
        PeerID routerPid = firstLeg.getDest().getPeerID();
        if (newRoute.size() == 0 || (!newRoute.getFirstHop().getPeerID().equals(routerPid))) {
            AccessPointAdvertisement ap = (AccessPointAdvertisement)
                    AdvertisementFactory.newAdvertisement(
                            AccessPointAdvertisement.getAdvertisementType());
            // prepend the route with the routing peer.
            ap.setPeerID(routerPid);
            hops.add(0, ap);
        }

        // prepend the rest of the route
        hops.addAll(0, firstLeg.getVectorHops());

        // remove any llop from the root
        cleanupLoop(newRoute, localPeer);
        return true;
    }
}
