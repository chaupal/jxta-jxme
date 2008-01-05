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
package net.jxta.peergroup;

import net.jxta.exception.JxtaError;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public final class PeerGroupFactory {

    private final static Logger LOG = Logger.getInstance(PeerGroupFactory.class.getName());

    /**
     * Platform (World) Peer Group instances will be created as instances of this class.
     */
    private Class platformClass = null;

    /**
     * Peer Group instances, other than the Platform (World) Peer Group will be
     * created as instances of this class.
     */
    private Class stdPeerGroupClass = null;

    /**
     * The ID of the network peer group.
     */
    private PeerGroupID netPGID = null;

    /**
     * The name of the network peer group.
     */
    private String netPGName = null;

    /**
     * The description of the network peer group.
     */
    private String netPGDesc;

    /**
     * The class which will be instantiated to configure the Platform Peer
     * Group.
     */
    private Class configurator;

    /**
     * Singleton which holds configuration parameters.
     */
    private final static PeerGroupFactory factory = new PeerGroupFactory();

    /**
     * Read the configuration parameters for the Peer Group factory.
     */
    private PeerGroupFactory() {
        String platformPGClassName = "net.jxta.impl.peergroup.Platform";
        String stdPGClassName = "net.jxta.impl.peergroup.StdPeerGroup";

        netPGID = ConfigurationFactory.getInfrastructureID();
        netPGName = "NetPeerGroup";
        netPGDesc = "default Net Peer Group";

        try {
            try {
                // Name, desc, and ID must all be set or not at all.
                // If one is missing we exception out and do none.
                // else, we set them all.
                try {
                    String idTmpStr = "jxta-NetGroup".trim();
                    PeerGroupID idTmp;

                    if (!idTmpStr.startsWith("jxta:")) {
                        idTmp = (PeerGroupID) IDFactory.fromURI(new URI(ID.URIEncodingName + ":" + ID.URNNamespace + ":" + idTmpStr));
                    } else {
                        if (LOG.isEnabledFor(Priority.WARN)) {
                            LOG.warn("Custom Net Peer Group ID is in deprecated form--remove 'jxta:'");
                        }
                        idTmp = (PeerGroupID) IDFactory.fromURI(new URI(ID.URIEncodingName + ":" + idTmpStr));
                    }
                    String nameTmp = "NetPeerGroup";
                    String descTmp = "default Net Peer Group";

                    netPGID = idTmp;
                    netPGName = nameTmp;
                    netPGDesc = descTmp;
                } catch (Exception ignored) {
                    ;
                }
            } catch (Exception nevermind) {
                ;
            }

            platformClass = Class.forName(platformPGClassName);
            stdPeerGroupClass = Class.forName(stdPGClassName);

        } catch (Throwable e) {
            LOG.fatal("Could not initialize platform and standard peer group classes", e);
        }
    }


    /**
     * Static Method to initialize the Platform peer group class
     *
     * @param c The Class which will be instantiated for the World Peer Group
     */
    public static void setPlatformClass(Class c) {
        if (!net.jxta.peergroup.PeerGroup.class.isAssignableFrom(c)) {
            throw new ClassCastException("Not a valid PeerGroup class");
        }

        factory.platformClass = c;
    }

    /**
     * Static Method to initialize the std peer group class
     *
     * @param c Class to use for for general peer group creation.
     */
    public static void setStdPeerGroupClass(Class c) {
        if (!net.jxta.peergroup.PeerGroup.class.isAssignableFrom(c)) {
            throw new ClassCastException("Not a valid PeerGroup class");
        }

        factory.stdPeerGroupClass = c;
    }

    /**
     * Static Method to initialize the net peer group description.
     *
     * @param desc The description to use for the net peer group.
     * @since JXTA 2.1.1
     */
    public static void setNetPGDesc(String desc) {
        factory.netPGDesc = desc;
    }

    /**
     * Static Method to initialize the net peer group name.
     *
     * @param name The name to use for the net peer group.
     * @since JXTA 2.1.1
     */
    public static void setNetPGName(String name) {
        factory.netPGName = name;
    }

    /**
     * Static Method to initialize the net peer group ID.
     *
     * @param id The id to use for the net peer group.
     * @since JXTA 2.1.1
     */
    public static void setNetPGID(PeerGroupID id) {
        factory.netPGID = id;
    }

    /**
     * Static Method to create a new peer group instance.
     * <p/>
     * <p/>After beeing created the init method needs to be called, and
     * the startApp() method may be called, at the invoker's discretion.
     *
     * @return PeerGroup instance of a new PeerGroup
     */
    public static PeerGroup newPeerGroup() {
        try {
            return (PeerGroup) factory.stdPeerGroupClass.newInstance();
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.WARN))
                LOG.warn("Failed to construct peer group", e);

            throw new JxtaError("Failed to construct peer group", e);
        }
    }

    /**
     * Instantiates the Platform Peer Group.
     * <p/>
     * <p/>The {@link PeerGroup#init(PeerGroup,ID,net.jxta.document.Advertisement)} method is
     * called automatically. The {@link PeerGroup#startApp(String[])} method
     * is left for the invoker to call if appropriate.
     * <p/>
     * <p/>Invoking this method amounts to creating an instance of JXTA.
     * <p/>
     * <p/>Since JXTA stores its persistent state in the local filesystem
     * relative to the initial current directory, it is unadvisable to
     * start more than one instance with the same current directory.
     *
     * @return PeerGroup instance of a new Platform
     */
    public static PeerGroup newPlatform() {

        PeerGroup plat;
        try {
            plat = (PeerGroup) factory.platformClass.newInstance();
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.FATAL))
                LOG.fatal("Could not instantiate Platform", e);
            e.printStackTrace();
            throw new JxtaError("Could not instantiate Platform", e);
        }

        try {
            plat.init(null, PeerGroupID.worldPeerGroupID, null);

            return (PeerGroup) plat.getInterface();
        } catch (RuntimeException e) {
            LOG.fatal("newPlatform failed", e);

            // rethrow
            throw e;
        }
        catch (Exception e) {
            // should be all other checked exceptions
            LOG.fatal("newPlatform failed", e);

            // Simplify exception scheme for caller: any sort of problem wrapped
            // in a PeerGroupException.
            throw new JxtaError("newPlatform failed", e);
        }
    }


    /**
     * Instantiates the net peer group using the provided platform peer group.
     *
     * @param ppg The platform group.
     * @return PeerGroup The default netPeerGroup
     */
    public static PeerGroup newNetPeerGroup(PeerGroup ppg) throws PeerGroupException {

        try {
            // Build the group based on our config.
            PeerGroup newPg = ppg.newGroup(factory.netPGID,
                    ppg.getAllPurposePeerGroupImplAdvertisement(), // Platform knows what to do
                    factory.netPGName,
                    factory.netPGDesc);

            return newPg;
        } catch (PeerGroupException failed) {
            LOG.fatal("newNetPeerGroup failed", failed);

            // rethrow
            throw failed;
        }
        catch (RuntimeException e) {
            LOG.fatal("newNetPeerGroup failed", e);

            // rethrow
            throw e;
        }
        catch (Exception e) {
            // should be all other checked exceptions
            LOG.fatal("newNetPeerGroup failed", e);

            // Simplify exception scheme for caller: any sort of problem wrapped
            // in a PeerGroupException.
            throw new PeerGroupException("newNetPeerGroup failed", e);
        }
    }

    /**
     * Instantiates the platform peergroup and then instantiates the net peer
     * group. This simplifies the method by which applications can start JXTA.
     *
     * @return The newly instantiated net peer group.
     */
    public static PeerGroup newNetPeerGroup() throws PeerGroupException {
        // create the  default Platform Group.
        PeerGroup platformGroup = newPlatform();
        try {
            PeerGroup npg = newNetPeerGroup(platformGroup);
            return npg;
        }
        finally {
            platformGroup.unref();
        }
    }
}
