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
package net.jxta.impl.rendezvous;

import net.jxta.id.ID;
import net.jxta.impl.util.TimeUtils;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import net.jxta.protocol.RouteAdvertisement;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.peer.PeerID;
import net.jxta.document.XMLElement;
import net.jxta.document.XMLDocument;
import java.util.Enumeration;
import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.AccessPointAdvertisement;

/**
 * Manages a connection with a client or a rendezvous peer.
 */
public class RdvConnection extends PeerConnection {

    /**
     * Log4J Logger
     */
    private final static transient Logger LOG = Logger.getInstance(RdvConnection.class.getName());
    /**
     * Description of the Field
     */
    protected long beginRenewalAt;
    /**
     * Description of the Field
     */
    protected int cachedModCount = -1;

    /**
     * Description of the Field
     */
    protected PeerAdvertisement cachedPeerAdvertisement = null;

    /**
     * Description of the Field
     */
    protected long leasedTil;

    /**
     * Constructor for the PeerConnection object
     *
     * @param group      group context
     * @param rdvService Description of the Parameter
     * @param peer       Description of the Parameter
     */
    public RdvConnection(PeerGroup group, RendezVousServiceImpl rdvService, RdvAdvertisement radv) {
        super(group, rdvService.endpoint, radv);

        cachedPeerAdvertisement = group.getPeerAdvertisement();
        cachedModCount = cachedPeerAdvertisement.getModCount();
    }

    /**
     * Declare that we are connected.
     *
     * @param padv          Description of the Parameter
     * @param leaseDuration Description of the Parameter
     * @param earlyRenewal  Description of the Parameter
     */
    public void connect(RouteAdvertisement route, long leaseDuration, long earlyRenewal) {
        super.connect(leaseDuration);
        setLease(leaseDuration, earlyRenewal);
        // We will almost certainly need a messenger soon. Get it now.
        getCachedMessenger(route);
    }
    
    /**
     * Description of the Method
     *
     * @param adv Description of the Parameter
     * @return Description of the Return Value
     */
    public final static RouteAdvertisement extractRouteAdv(PeerAdvertisement adv) {

        try {
            // Get its EndpointService advertisement
            XMLElement endpParam = (XMLElement) adv.getServiceParam(PeerGroup.endpointClassID);

            if (endpParam == null) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("No Endpoint Params");
                }
                return null;
            }

            // get the Route Advertisement element
            Enumeration paramChilds = endpParam.getChildren(RouteAdvertisement.getAdvertisementType());
            XMLElement param;

            if (paramChilds.hasMoreElements()) {
                param = (XMLElement) paramChilds.nextElement();
            } else {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("No Route Adv in Peer Adv");
                }
                return null;
            }

            // build the new route
            RouteAdvertisement route = (RouteAdvertisement) AdvertisementFactory.newAdvertisement((XMLElement) param);
            route.setDestPeerID(adv.getPeerID());
            return route;
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("failed to extract radv", e);
            }
        }
        return null;
    }    
    /**
     * Description of the Method
     *
     * @param padv Description of the Parameter
     * @param name Description of the Parameter
     * @return Description of the Return Value
     */
    private static RdvAdvertisement createRdvAdvertisement(PeerID pid, PeerGroupID gid, String addr) {

        try {
            // FIX ME: 10/19/2002 lomax@jxta.org. We need to properly set up the service ID. Unfortunately
            // this current implementation of the PeerView takes a String as a service name and not its ID.
            // Since currently, there is only PeerView per group (all peerviews share the same "service", this
            // is not a problem, but that will have to be fixed eventually.

            // create a new RdvAdvertisement
            RdvAdvertisement rdv = (RdvAdvertisement) AdvertisementFactory.newAdvertisement(RdvAdvertisement.getAdvertisementType());

            rdv.setPeerID(pid);
            rdv.setGroupID(gid);         
            rdv.setName("RDV seed");

            RouteAdvertisement ra = (RouteAdvertisement) 
                AdvertisementFactory.newAdvertisement(RouteAdvertisement.getAdvertisementType());
            ra.setDestPeerID(pid);
            AccessPointAdvertisement ap = (AccessPointAdvertisement) 
             AdvertisementFactory.newAdvertisement(AccessPointAdvertisement.getAdvertisementType());
            ap.addEndpointAddress(addr);
            ra.setDest(ap);
            // Insert it into the RdvAdvertisement.
            rdv.setRouteAdv(ra);

            return rdv;
        } catch (Exception ez) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("Cannot create Local RdvAdvertisement: ", ez);
            }
            return null;
        }
    }
    /**
     * Time at which the lease needs renewal in absolute milliseconds.
     *
     * @return The lease value
     */
    public long getRenewal() {
        return beginRenewalAt;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public synchronized boolean peerAdvertisementHasChanged() {

        boolean changed = false;
        // BT limit what we are sending
        /*PeerAdvertisement currPeerAdv = group.getPeerAdvertisement();
        int currModCount = currPeerAdv.getModCount();

        if ((cachedPeerAdvertisement != currPeerAdv) || (cachedModCount != currModCount)) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug(
                        "PeerAdvertisement has changed :" + "\n\t" + System.identityHashCode(cachedPeerAdvertisement) + " != "
                                + System.identityHashCode(currPeerAdv) + "\n\t" + cachedModCount + " != " + currModCount);
            }
            // If our peer adv has changed, it is possible that our rdv has lost
            // all routes to us. Refresh its knowlege by reconnecting.
            cachedPeerAdvertisement = currPeerAdv;
            cachedModCount = currModCount;
            changed = true;
        } */
        return changed;
        
    }

    /**
     * {@inheritDoc}
     *
     * @param leaseDuration The new lease value
     */
    protected void setLease(long leaseDuration) {
        setLease(leaseDuration, 0);
    }

    /**
     * Set the lease duration in relative milliseconds.
     *
     * @param leaseDuration the lease duration in relative milliseconds.
     * @param earlyRenewal  amount of time in relative milliseconds before
     *                      lease end to begin renewal
     */
    public void setLease(long leaseDuration, long earlyRenewal) {

        if (leaseDuration < earlyRenewal) {
            throw new IllegalArgumentException("Renewal scheduled before begining of lease");
        }
        super.setLease(leaseDuration);
        beginRenewalAt = TimeUtils.toAbsoluteTimeMillis(leaseDuration - earlyRenewal);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() + " / " + Long.toString(TimeUtils.toRelativeTimeMillis(beginRenewalAt));
    }
}
