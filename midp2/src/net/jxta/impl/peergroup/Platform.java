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
package net.jxta.impl.peergroup;

import com.sun.java.util.collections.HashMap;
import net.jxta.document.Advertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.TextElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.ModuleImplAdvertisement;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.IOException;

/**
 * Provides the implementation for the World Peer Group. <p/>
 * <p/>
 * Key differences from regular groups are:
 * <ul>
 * <li> Provides a mechanism for peer group configuration parameter and for
 * reconfiguration via a plugin configurator.</li>
 * <li> Ensures that only a single instance of the World Peer Group exists
 * within the context of the current classloader.</li>
 * </ul>
 */
public class Platform extends StdPeerGroup {

    /**
     * Log4J Logger
     */
    private final static Logger LOG = Logger.getInstance(Platform.class.getName());

    /**
     */
    private ModuleImplAdvertisement allPurposeImplAdv = null;

    /**
     * If <code>true</code> then initialization has been completed. Declared
     * <code>static</code> to ensure that only one instance exists.
     */
    private static boolean initialized = false;


    /**
     * Default constructor
     */
    public Platform() {
    }


    /**
     * Returns the all purpose peer group implementation advertisement that is
     * most usefull when called in the context of the platform group: the
     * description of an infrastructure group. This definition is always the
     * same and has a well known ModuleSpecID. It includes the basic service,
     * high-level transports and the shell for main application. It differs
     * from the one returned by StdPeerGroup only in that it includes the
     * high-level transports (and different specID, name and description, of
     * course). However, in order to avoid confusing inheritance schemes (class
     * hierarchy is inverse of object hierarchy) other possible dependency
     * issues, we just redefine it fully, right here. The user must remember to
     * change the specID if the set of services protocols or applications is
     * altered before use.
     *
     * @return ModuleImplAdvertisement The new peergroup impl adv.
     * @throws Exception Description of the Exception
     */
    public ModuleImplAdvertisement getAllPurposePeerGroupImplAdvertisement()
            throws Exception {

        // Build it only the first time; then clone it.
        if (allPurposeImplAdv != null) {
            return allPurposeImplAdv;
        }

        // Make a new impl adv
        // For now, use the well know NPG naming, it is not
        // identical to the allPurpose PG because we use the class
        // ShadowPeerGroup which initializes the peer config from its
        // parent.
        ModuleImplAdvertisement implAdv =
                mkImplAdvBuiltin(PeerGroup.refNetPeerGroupSpecID,
                        ShadowPeerGroup.class.getName(),
                        "Default NetPeerGroup reference implementation.");

        TextElement paramElement = (TextElement) implAdv.getParam();
        StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv();
        ModuleImplAdvertisement moduleAdv;

        // set the services
        HashMap services = new HashMap();

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refEndpointSpecID,
                        "net.jxta.impl.endpoint.EndpointServiceImpl",
                        "Reference Implementation of the Endpoint Service");
        services.put(PeerGroup.endpointClassID, moduleAdv);

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refResolverSpecID,
                        "net.jxta.impl.resolver.ResolverServiceImpl",
                        "Reference Implementation of the Resolver Service");
        services.put(PeerGroup.resolverClassID, moduleAdv);
/*
        moduleAdv =
                mkImplAdvBuiltin(PSEMembershipService.pseMembershipSpecID,
                "net.jxta.impl.membership.pse.PSEMembershipService",
                "Reference Implementation of the PSE Membership Service");
        services.put(PeerGroup.membershipClassID, moduleAdv);

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refAccessSpecID,
                "net.jxta.impl.access.always.AlwaysAccessService",
                "Always Access Service");
        services.put(PeerGroup.accessClassID, moduleAdv);
*/
        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refDiscoverySpecID,
                        "net.jxta.impl.discovery.DiscoveryServiceImpl",
                        "Reference Implementation of the Discovery Service");
        services.put(PeerGroup.discoveryClassID, moduleAdv);

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refRendezvousSpecID,
                        "net.jxta.impl.rendezvous.RendezVousServiceImpl",
                        "Reference Implementation of the Rendezvous Service");
        services.put(PeerGroup.rendezvousClassID, moduleAdv);

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refPipeSpecID,
                        "net.jxta.impl.pipe.PipeServiceImpl",
                        "Reference Implementation of the Pipe Service");
        services.put(PeerGroup.pipeClassID, moduleAdv);
/*
        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refPeerinfoSpecID,
                "net.jxta.impl.peer.PeerInfoServiceImpl",
                "Reference Implementation of the Peerinfo Service");
        services.put(PeerGroup.peerinfoClassID, moduleAdv);
*/
        paramAdv.setServices(services);

        // High-level Transports.
        HashMap protos = new HashMap();

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refRouterProtoSpecID,
                        "net.jxta.impl.endpoint.router.EndpointRouter",
                        "Reference Implementation of the Router Message Transport");
        protos.put(PeerGroup.routerProtoClassID, moduleAdv);
        paramAdv.setProtos(protos);
        // Pour our newParamAdv in implAdv
        paramElement = (TextElement) paramAdv.getDocument(MimeMediaType.XMLUTF8);
        implAdv.setParam(paramElement);
        allPurposeImplAdv = implAdv;
        return implAdv;
    }


    /**
     * {@inheritDoc}
     */
    protected synchronized void initFirst(PeerGroup nullParent, ID assignedID, Advertisement impl)
            throws PeerGroupException {

        if (initialized) {
            LOG.fatal("You cannot initialize more than one World PeerGroup!");
            throw new PeerGroupException("You cannot initialize more than one World PeerGroup!");
        }

        ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) impl;

        if (nullParent != null) {
            if (LOG.isEnabledFor(Priority.ERROR)) {
                LOG.error("World PeerGroup cannot be instantiated with a parent group!");
            }

            throw new PeerGroupException("World PeerGroup cannot be instantiated with a parent group!");
        }

        // if we weren't given a module impl adv then make one from scratch.
        if (null == implAdv) {
            try {
                // Build the platform's impl adv.
                implAdv = mkPlatformImplAdv();
            } catch (Throwable e) {
                if (LOG.isEnabledFor(Priority.FATAL)) {
                    LOG.fatal("Fatal Error making Platform Impl Adv", e);
                }
                throw new PeerGroupException("Fatal Error making Platform Impl Adv: " + e.getMessage());
            }
        }


        ConfigParams adv = (ConfigParams) ConfigurationFactory.newPlatformConfig();

        setConfigAdvertisement(adv);
        // Initialize the group.
        super.initFirst(null, PeerGroupID.worldPeerGroupID, implAdv);
        // Publish our own adv.
        try {
            publishGroup("World PeerGroup", "Standard World PeerGroup Reference Implementation");
        } catch (IOException e) {
            throw new PeerGroupException("Failed to publish World Peer Group Advertisement: " + e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @throws PeerGroupException Description of the Exception
     */
    protected synchronized void initLast() throws PeerGroupException {
        // Nothing special for now, but we might want to move some steps
        // from initFirst, in the future.
        super.initLast();

        initialized = true;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @throws Exception Description of the Exception
     */
    protected ModuleImplAdvertisement mkPlatformImplAdv() throws Exception {

        // Start building the implAdv for the platform intself.
        ModuleImplAdvertisement platformDef =
                mkImplAdvBuiltin(PeerGroup.refPlatformSpecID,
                        "World PeerGroup",
                        "Standard World PeerGroup Reference Implementation");

        // Build the param section now.
        StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv();
        HashMap protos = new HashMap();
        HashMap services = new HashMap();
        HashMap apps = new HashMap();
        // Build ModuleImplAdvs for each of the modules
        ModuleImplAdvertisement moduleAdv;

        // Do the Services

        // "Core" Services
        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refEndpointSpecID,
                        "net.jxta.impl.endpoint.EndpointServiceImpl",
                        "Reference Implementation of the Endpoint service");
        services.put(PeerGroup.endpointClassID, moduleAdv);
        // Do the protocols

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refTcpProtoSpecID,
                        "net.jxta.impl.endpoint.tcp.TcpTransport",
                        "Reference Implementation of the TCP Message Transport");
        protos.put(PeerGroup.tcpProtoClassID, moduleAdv);
        // Do the Apps

        moduleAdv =
                mkImplAdvBuiltin(PeerGroup.refStartNetPeerGroupSpecID,
                        "net.jxta.impl.peergroup.StartNetPeerGroup",
                        "Start Net Peer Group");
        apps.put(applicationClassID, moduleAdv);

        paramAdv.setServices(services);
        paramAdv.setProtos(protos);
        paramAdv.setApps(apps);

        // Pour the paramAdv in the platformDef
        platformDef.setParam
                ((StructuredDocument)
                        paramAdv.getDocument(MimeMediaType.XMLUTF8));

        return platformDef;
    }


    /**
     * {@inheritDoc}
     */
    public void stopApp() {
        super.stopApp();

        initialized = false;
    }
}
