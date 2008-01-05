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
package net.jxta.id;

import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.Map;
import net.jxta.codat.CodatID;
import net.jxta.id.jxta.IDFormat;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.util.ClassFactory;
import net.jxta.util.java.net.URI;
import net.jxta.util.java.net.URISyntaxException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.NoSuchElementException;
public final class IDFactory extends ClassFactory {

    /**
     * Interface for instantiators of IDs. Each ID Format registered with the
     * ID Factory implements a class with this interface.
     */
    public interface Instantiator {

        /**
         * Returns the ID Format value associated with this ID Format
         *
         * @return String containing the ID format value for this format.
         */
        public String getSupportedIDFormat();

        /**
         * Creates a new CodatID Instance. A new random CodatID is created for
         * the provided Peer Group. This type of CodatID can be used as a
         * canonical reference for dynamic content.
         *
         * @param groupID the group to which this content will belong.
         * @return The newly created CodatID.
         * @see net.jxta.codat.Codat
         */
        public CodatID newCodatID(PeerGroupID groupID);

        /**
         * Creates a new CodatID instance. A new CodatID is created for the
         * provided Peer Group. This type of CodatID can be used as a
         * canonical reference for dynamic content.
         * <p/>
         * <p/>This varient of CodatID allows you to create "Well-known" codats
         * within the context of diverse groups. This can be useful for common
         * services that need to do discovery without advertisements or for
         * network organization services.  Because of the potential for ID
         * collisions and the difficulties with maintaining common service
         * interfaces this varient of CodatID should be used with great caution
         * and pre-planning.
         *
         * @param groupID the group to which this content will belong.
         * @param seed    The seed information which will be used in creating the
         *                codatID. The seed information should be at least four bytes in length,
         *                though longer values are better.
         * @return The newly created CodatID.
         * @see net.jxta.codat.Codat
         */
        public CodatID newCodatID(PeerGroupID groupID, byte [] seed);

        /**
         * Creates a new PeerID instance. A new random peer id will be generated.
         * The PeerID will be a member of the provided group.
         *
         * @param groupID the group to which this PeerID will belong.
         * @return The newly created PeerID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerID newPeerID(PeerGroupID groupID);

        /**
         * Creates a new PeerID instance. A new PeerID will be generated.
         * The PeerID will be a member of the provided group.
         *
         * @param groupID the group to which this PeerID will belong.
         * @param seed    The seed information which will be used in creating the
         *                PeerID. The seed information should be at least four bytes in length,
         *                though longer values are better.
         * @return The newly created PeerID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerID newPeerID(PeerGroupID groupID, byte [] seed);

        /**
         * Creates a new PeerGroupID instance. A new random peer group id will be
         * generated. The PeerGroupID will be created using the default ID Format.
         *
         * @return The newly created PeerGroupID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerGroupID newPeerGroupID();

        /**
         * Creates a new PeerGroupID instance. A new PeerGroupID will be
         * generated using the provided seed information. The PeerGroupID will
         * be created using the default ID Format.
         * <p/>
         * <p/>This method allows you to create "Well-known" PeerGroupIDs.
         * This is similar to how the JXTA "World Peer Group" and "Net
         * Peer Group". "Well-known" IDs can be useful for common services
         * that need to do  discovery without advertisements or for network
         * organization  services. Because of the potential for ID collisions
         * and the difficulties with maintaining common service interfaces this
         * varient of PeerGroupID should be used with great caution and
         * pre-planning.
         *
         * @param seed The seed information which will be used in creating the
         *             PeerGroupID. The seed information should be at least four bytes in length,
         *             though longer values are better.
         * @return The newly created PeerGroupID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerGroupID newPeerGroupID(byte [] seed);

        /**
         * Creates a new PeerGroupID instance with the specified parent group.
         * A new random peer group id will be generated.
         *
         * @param parent The group which will be the parent of this group.
         * @return The newly created PeerGroupID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerGroupID newPeerGroupID(PeerGroupID parent);

        /**
         * Creates a new PeerGroupID instance with the specified parent group.
         * A new PeerGroupID will be generated using the provided seed
         * information.
         * <p/>
         * <p/>This method allows you to create "Well-known" PeerGroupIDs.
         * This is similar to how the JXTA "World Peer Group" and "Net
         * Peer Group". "Well-known" IDs can be useful for common services
         * that need to do  discovery without advertisements or for network
         * organization  services. Because of the potential for ID collisions
         * and the difficulties with maintaining common service interfaces this
         * varient of PeerGroupID should be used with great caution and
         * pre-planning.
         *
         * @param parent The group which will be the parent of this group.
         * @param seed   The seed information which will be used in creating the
         *               PeerGroupID. The seed information should be at least four bytes in length,
         *               though longer values are better.
         * @return The newly created PeerGroupID.
         * @see net.jxta.peergroup.PeerGroup
         */
        public PeerGroupID newPeerGroupID(PeerGroupID parent, byte [] seed);

        /**
         * Creates a new PipeID instance. A new random PipeID will be generated.
         *
         * @param groupID The group to which this Pipe ID will belong.
         * @return The newly created PipeID.
         */
        public PipeID newPipeID(PeerGroupID groupID);

        /**
         * Creates a new PipeID instance. A new pipe id will be generated with the
         * provided seed information. The Pipe ID will be a member of the provided
         * group.
         * <p/>
         * <p/>This varient of PipeID allows you to create "Well-known" pipes
         * within the context of diverse groups. This can be useful for common
         * services that need to do discovery without advertisements or for
         * network organization services.  Because of the potential for ID
         * collisions and the difficulties with maintaining common service
         * interfaces this varient of PipeID should be used with great caution
         * and pre-planning.
         *
         * @param groupID the group to which this Pipe ID will belong.
         * @param seed    The seed information which will be used in creating the
         *                pipeID. The seed information should be at least four bytes in length,
         *                though longer values are better.
         * @return the newly created PipeID.
         */
        public PipeID newPipeID(PeerGroupID groupID, byte [] seed);

        /**
         * Creates a new ModuleClassID instance. A new random ModuleClassID will
         * be generated with a zero value role identifier. This form of
         * ModuleClassID is appropriate for cases where the module does not
         * need to be distinguished from other instances of the same Module.
         * The ModuleClassID will be created using the default ID Format.
         *
         * @return The newly created ModuleClassID.
         * @see net.jxta.platform.Module
         */
        public ModuleClassID newModuleClassID();

        /**
         * Creates a new ModuleClassID instance. A new random ModuleClassID will
         * be generated with a a random value role identifier and a base class of
         * the provided ModuleClassID. This form of ModuleClassID is
         * appropriate for cases where it is necessary to distinguish instances
         * of the same service interface.
         *
         * @param baseClass The ModuleClassID which will be used as a base
         *                  class for this new role value instance.
         * @return The newly created ModuleClassID.
         * @see net.jxta.platform.Module
         */
        public ModuleClassID newModuleClassID(ModuleClassID baseClass);

        /**
         * Creates a new  ModuleSpecID instance. A new random ModuleSpecID will
         * be generated.
         *
         * @param baseClass The ModuleClassID which will be used as a base
         *                  class for this new ModuleSpecID.
         * @return The newly created ModuleSpecID.
         * @see net.jxta.platform.Module
         */
        public ModuleSpecID newModuleSpecID(ModuleClassID baseClass);
    }

    /**
     * Extended instantiator which provides for construction from URIs and from
     * scheme specific URN fragments. ID Formats are not required to implement
     * this interface, but doing so will improve performance in many cases. When
     * the deprecated URL based calls are removed these methods will be added
     * to the primary interface and this interface will be deprecated.
     */
    public interface URIInstantiator extends Instantiator {

        /**
         * Construct a new ID instance from a JXTA ID contained in a URI.
         *
         * @param source URI which will be decoded to create a new ID instance.
         * @return ID containing the new ID instance initialized from the source.
         * @throws net.jxta.util.java.net.URISyntaxException
         *          if the URI provided is not a valid,
         *          recognized JXTA URI.
         */
        public ID fromURI(URI source) throws URISyntaxException;

        /**
         * Construct a new ID instance from the scheme specific portion of a jxta
         * URN.
         *
         * @param source the scheme specific portion of a jxta URN.
         * @return ID containing the new ID instance initialized from the source.
         * @throws URISyntaxException if the URI provided is not a valid,
         *                            recognized JXTA URI.
         */
        public ID fromURNNamespaceSpecificPart(String source) throws URISyntaxException;
    }

    /**
     * Log4J Logger
     */
    private static final transient Logger LOG = Logger.getInstance(IDFactory.class.getName());

    /**
     * A map of the ID Formats to instantiators.
     * <p/>
     * <ul>
     * <li>keys are {@link String} of ID Format names</li>
     * <li>values are {@link Instantiator} instances</li>
     * </ul>
     */
    private Map idFormats = new HashMap();

    /**
     * Identifies the ID format to use when creating new ID instances.
     */
    private String idNewInstances = null;

    /**
     * This class is a singleton. This is the instance that backs the
     * static methods.
     */
    private static IDFactory factory = new IDFactory();

    /**
     * Standard Constructor. This class is a singleton so the only constructor
     * is private.
     * <p/>
     * <p/>Registers the pre-defined set of ID sub-classes so that this factory
     * can construct them. Uses net.jxta.impl.config.properties file as the
     * source for settings.
     * <p/>
     * <p/>Example entry from  the file net.jxta.impl.config.properties :
     * <p/>
     * <p/><pre><code>
     * #List of ID types supported.
     * IDInstanceTypes=net.jxta.id.jxta.IDFormat net.jxta.impl.id.UUID.IDFormat net.jxta.impl.id.binaryID.IDFormat
     * <p/>
     * #Default type of ID to use when creating an ID (this should not be changed in most implementations).
     * IDNewInstances=uuid
     * </code></pre>
     */
    private IDFactory() {
        // required format
        registerAssoc(net.jxta.id.jxta.IDFormat.INSTANTIATOR);

        // required by this implementation.
        registerAssoc(net.jxta.impl.id.unknown.IDFormat.INSTANTIATOR);

        // set the default ID Format.
        idNewInstances = "uuid";

        // Register a list of classes for association with an ID type
        registerAssoc(net.jxta.id.UUID.IDFormat.INSTANTIATOR);
    }

    /**
     * Used by ClassFactory methods to get the mapping of ID types to constructors.
     *
     * @return Hashtable the hashtable containing the mappings.
     */
    protected Map getAssocTable() {
        return idFormats;
    }

    /**
     * Used by ClassFactory methods to ensure that all keys used with the mapping are
     * of the correct type.
     *
     * @return Class object of the key type.
     */
    protected Class getClassForKey() {
        return String.class;
    }

    /**
     * Used by ClassFactory methods to ensure that all of the instance classes
     * which register with this factory have the correct base class
     *
     * @return Class object of the key type.
     */
    protected Class getClassOfInstantiators() {
        // we dont require that they be of any particular type since they are
        // factories themselves
        return Object.class;
    }

    /**
     * Register a class with the factory from its class name. We override the
     * standard implementation to get the id type from the class and
     * use that as the key to register the class with the factory.
     *
     * @param className The class name which will be regiestered.
     * @return boolean true if the class was registered otherwise false.
     */
    public boolean registerAssoc(Instantiator instantiator) {
        if (null == instantiator) {
            LOG.error("INSTANTIATOR field is null for class  : " + instantiator.getClass().getName());
            return false;
        }

        String idType = instantiator.getSupportedIDFormat();

        return registerAssoc(idType, instantiator);
    }

    /**
     * Returns a String containing the name of the default ID Format.
     *
     * @return The current default ID Format.
     */
    public static String getDefaultIDFormat() {
        return factory.idNewInstances;
    }

    /**
     * Construct a new ID instance from a JXTA ID contained in a URI.
     *
     * @param source URI which will be decoded to create a new ID instance.
     * @return ID containing the new ID instance initialized from the URI.
     * @throws URISyntaxException If the URI provided is not a valid,
     *                            recognized JXTA URI.
     */
    public static ID fromURI(URI source) throws URISyntaxException {
        ID result = null;

        // check the protocol
        if (!ID.URIEncodingName.equalsIgnoreCase(source.getScheme()))
            throw new URISyntaxException(source.toString(), "URI scheme was not as expected.");

        String decoded = source.getSchemeSpecificPart();

        int colonAt = decoded.indexOf(':');

        // There's a colon right?
        if (-1 == colonAt)
            throw new URISyntaxException(source.toString(), "URN namespace was missing.");

        // check the namespace
        if (!net.jxta.id.ID.URNNamespace.equalsIgnoreCase(decoded.substring(0, colonAt)))
            throw new URISyntaxException(source.toString(), "URN namespace was not as expected. (" +
                    net.jxta.id.ID.URNNamespace + "!=" + decoded.substring(0, colonAt) + ")");
        // skip the namespace portion and the colon
        decoded = decoded.substring(colonAt + 1);

        int dashAt = decoded.indexOf('-');

        // there's a dash, right?
        if (-1 == dashAt)
            throw new URISyntaxException(source.toString(), "URN jxta namespace IDFormat was missing.");

        // get the encoding used for this id
        String format = decoded.substring(0, dashAt);

        Instantiator instantiator;
        try {
            instantiator = (Instantiator) factory.getInstantiator(format);
        } catch (NoSuchElementException itsUnknown) {
            instantiator = (Instantiator) factory.getInstantiator("unknown");
        }

        return ((URIInstantiator) instantiator).fromURNNamespaceSpecificPart(decoded);
    }

    /**
     * Creates a new CodatID Instance. A new random CodatID is created for
     * the provided Peer Group. This type of CodatID can be used as a
     * canonical reference for dynamic content.
     *
     * @param groupID the group to which this content will belong.
     * @return The newly created CodatID.
     * @see net.jxta.codat.Codat
     */
    public static CodatID newCodatID(PeerGroupID groupID) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newCodatID(groupID);
    }

    /**
     * Creates a new CodatID instance. A new CodatID is created for the
     * provided Peer Group. This type of CodatID can be used as a
     * canonical reference for dynamic content.
     * <p/>
     * <p/>This varient of CodatID allows you to create "Well-known" codats
     * within the context of diverse groups. This can be useful for common
     * services that need to do discovery without advertisements or for
     * network organization services.  Because of the potential for ID
     * collisions and the difficulties with maintaining common service
     * interfaces this varient of CodatID should be used with great caution
     * and pre-planning.
     *
     * @param groupID the group to which this content will belong.
     * @param seed    The seed information which will be used in creating the
     *                codatID. The seed information should be at least four bytes in length,
     *                though longer values are better.
     * @return The newly created CodatID.
     * @see net.jxta.codat.Codat
     */
    public static CodatID newCodatID(PeerGroupID groupID, byte [] seed) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newCodatID(groupID, seed);
    }

    /**
     * Creates a new PeerID instance. A new random peer id will be generated.
     * The PeerID will be a member of the provided group.
     *
     * @param groupID the group to which this PeerID will belong.
     * @return The newly created PeerID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerID newPeerID(PeerGroupID groupID) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPeerID(groupID);
    }

    /**
     * Creates a new PeerID instance. A new PeerID will be generated.
     * The PeerID will be a member of the provided group.
     *
     * @param groupID the group to which this PeerID will belong.
     * @param seed    The seed information which will be used in creating the
     *                PeerID. The seed information should be at least four bytes in length,
     *                though longer values are better.
     * @return The newly created PeerID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerID newPeerID(PeerGroupID groupID, byte [] seed) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPeerID(groupID, seed);
    }

    /**
     * Creates a new PeerGroupID instance. A new random peer group id will be
     * generated. The PeerGroupID will be created using the default ID Format.
     *
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID() {
        return newPeerGroupID(factory.idNewInstances);
    }

    /**
     * Creates a new PeerGroupID instance using the specified ID Format.
     * A new random peer group id will be generated.
     *
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID(String idformat) {
        Instantiator instantiator = (Instantiator) factory.getInstantiator(idformat);

        return instantiator.newPeerGroupID();
    }

    /**
     * Creates a new PeerGroupID instance. A new PeerGroupID will be
     * generated using the provided seed information. The PeerGroupID will
     * be created using the default ID Format.
     * <p/>
     * <p/>This method allows you to create "Well-known" PeerGroupIDs.
     * This is similar to how the JXTA "World Peer Group" and "Net
     * Peer Group". "Well-known" IDs can be useful for common services
     * that need to do  discovery without advertisements or for network
     * organization  services. Because of the potential for ID collisions
     * and the difficulties with maintaining common service interfaces this
     * varient of PeerGroupID should be used with great caution and
     * pre-planning.
     *
     * @param seed The seed information which will be used in creating the
     *             PeerGroupID. The seed information should be at least four bytes in length,
     *             though longer values are better.
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID(byte [] seed) {
        return newPeerGroupID(factory.idNewInstances, seed);
    }

    /**
     * Creates a new PeerGroupID instance. A new PeerGroupID will be
     * generated using the provided seed information. The PeerGroupID will
     * be created using the default ID Format.
     * <p/>
     * <p/>This method allows you to create "Well-known" PeerGroupIDs.
     * This is similar to how the JXTA "World Peer Group" and "Net
     * Peer Group". "Well-known" IDs can be useful for common services
     * that need to do  discovery without advertisements or for network
     * organization  services. Because of the potential for ID collisions
     * and the difficulties with maintaining common service interfaces this
     * varient of PeerGroupID should be used with great caution and
     * pre-planning.
     *
     * @param seed The seed information which will be used in creating the
     *             PeerGroupID. The seed information should be at least four bytes in length,
     *             though longer values are better.
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID(String idformat, byte [] seed) {
        Instantiator instantiator = (Instantiator) factory.getInstantiator(idformat);

        return instantiator.newPeerGroupID(seed);
    }

    /**
     * Creates a new PeerGroupID instance with the specified parent group.
     * A new random peer group id will be generated.
     *
     * @param parent The group which will be the parent of this group.
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID(PeerGroupID parent) {
        String useFormat = parent.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPeerGroupID(parent);
    }

    /**
     * Creates a new PeerGroupID instance with the specified parent group.
     * A new PeerGroupID will be generated using the provided seed
     * information.
     * <p/>
     * <p/>This method allows you to create "Well-known" PeerGroupIDs.
     * This is similar to how the JXTA "World Peer Group" and "Net
     * Peer Group". "Well-known" IDs can be useful for common services
     * that need to do  discovery without advertisements or for network
     * organization  services. Because of the potential for ID collisions
     * and the difficulties with maintaining common service interfaces this
     * varient of PeerGroupID should be used with great caution and
     * pre-planning.
     *
     * @param parent The group which will be the parent of this group.
     * @param seed   The seed information which will be used in creating the
     *               PeerGroupID. The seed information should be at least four bytes in length,
     *               though longer values are better.
     * @return The newly created PeerGroupID.
     * @see net.jxta.peergroup.PeerGroup
     */
    public static PeerGroupID newPeerGroupID(PeerGroupID parent, byte [] seed) {
        String useFormat = parent.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPeerGroupID(parent, seed);
    }

    /**
     * Creates a new PipeID instance. A new random PipeID will be generated.
     *
     * @param groupID The group to which this Pipe ID will belong.
     * @return The newly created PipeID.
     */
    public static PipeID newPipeID(PeerGroupID groupID) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPipeID(groupID);
    }

    /**
     * Creates a new PipeID instance. A new pipe id will be generated with the
     * provided seed information. The Pipe ID will be a member of the provided
     * group.
     * <p/>
     * <p/>This varient of PipeID allows you to create "Well-known" pipes
     * within the context of diverse groups. This can be useful for common
     * services that need to do discovery without advertisements or for
     * network organization services.  Because of the potential for ID
     * collisions and the difficulties with maintaining common service
     * interfaces this varient of PipeID should be used with great caution
     * and pre-planning.
     *
     * @param groupID the group to which this Pipe ID will belong.
     * @param seed    The seed information which will be used in creating the
     *                pipeID. The seed information should be at least four bytes in length,
     *                though longer values are better.
     * @return the newly created PipeID.
     */
    public static PipeID newPipeID(PeerGroupID groupID, byte [] seed) {
        String useFormat = groupID.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newPipeID(groupID, seed);
    }

    /**
     * Creates a new ModuleClassID instance. A new random ModuleClassID will
     * be generated with a zero value role identifier. This form of
     * ModuleClassID is appropriate for cases where the module does not
     * need to be distinguished from other instances of the same Module.
     * The ModuleClassID will be created using the default ID Format.
     *
     * @return The newly created ModuleClassID.
     * @see net.jxta.platform.Module
     */
    public static ModuleClassID newModuleClassID() {
        return newModuleClassID(factory.idNewInstances);
    }

    /**
     * Creates a new ModuleClassID instance using the specified ID Format.
     * A new random ModuleClassID will be generated with a zero value role
     * identifier. This form of ModuleClassID is appropriate for cases
     * where the module does not need to be distinguished from other
     * instances of the same Module.
     *
     * @return The newly created ModuleClassID.
     * @see net.jxta.platform.Module
     */
    public static ModuleClassID newModuleClassID(String idformat) {
        Instantiator instantiator = (Instantiator) factory.getInstantiator(idformat);

        return instantiator.newModuleClassID();
    }

    /**
     * Creates a new ModuleClassID instance. A new random ModuleClassID will
     * be generated with a a random value role identifier and a base class of
     * the provided ModuleClassID. This form of ModuleClassID is
     * appropriate for cases where it is necessary to distinguish instances
     * of the same service interface.
     *
     * @param baseClass The ModuleClassID which will be used as a base
     *                  class for this new role value instance.
     * @return The newly created ModuleClassID.
     * @see net.jxta.platform.Module
     */
    public static ModuleClassID newModuleClassID(ModuleClassID baseClass) {
        String useFormat = baseClass.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newModuleClassID(baseClass);
    }

    /**
     * Creates a new  ModuleSpecID instance. A new random ModuleSpecID will
     * be generated.
     *
     * @param baseClass The ModuleClassID which will be used as a base
     *                  class for this new ModuleSpecID.
     * @return The newly created ModuleSpecID.
     * @see net.jxta.platform.Module
     */
    public static ModuleSpecID newModuleSpecID(ModuleClassID baseClass) {
        String useFormat = baseClass.getIDFormat();

        // is the group netpg or worldpg?
        if (IDFormat.INSTANTIATOR.getSupportedIDFormat().equals(useFormat)) {
            useFormat = factory.idNewInstances;
        }

        Instantiator instantiator = (Instantiator) factory.getInstantiator(useFormat);

        return instantiator.newModuleSpecID(baseClass);
    }

}
