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
package net.jxta.document;

import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.Map;
import net.jxta.util.ClassFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * A Factory class for Advertisements. This class abstracts the
 * the implementations used to represent and create advertisements.
 * <p/>
 * <p/>Advertisements are core objects that are used to advertise a Peer, a
 * PeerGroup, a Service, or a PipeServiceImpl. The Advertisement class provides
 * a platform independent representation of core objects that can be exchanged
 * between different implementations (Java, C).
 * <p/>
 * <p/>The AdvertisementFactory extends the ClassFactory to register the various
 * types of adverstisements into a static hastable. The factory is called with
 * the Advertisement type requested to create the corresponding advertisement
 * type.
 * <p/>
 * <p/>The initial set of Advertisement instances is loaded from the java
 * property "net.jxta.impl.config.AdvertisementInstanceTypes"
 * <p/>
 * <p/>It would be nice to have "newAdvertisement" methods for "Document" and
 * "TextDocument", but this is not feasible because "TextElement" got there
 * first.
 *
 * @see Advertisement
 * @see Document
 * @see MimeMediaType
 * @see net.jxta.peergroup.PeerGroup
 * @see net.jxta.protocol.PeerAdvertisement
 * @see net.jxta.protocol.PeerGroupAdvertisement
 * @see net.jxta.protocol.PipeAdvertisement
 */
public class AdvertisementFactory extends ClassFactory {
    /**
     * Log4J categorgy
     */
    private static final Logger LOG = Logger.getInstance(AdvertisementFactory.class.getName());

    /**
     * Interface for instantiators of Advertisements
     */
    public interface Instantiator {

        /**
         * Returns the identifying type of this Advertisement.
         *
         * @return String the type of advertisement
         */
        String getAdvertisementType();

        /**
         * Constructs an instance of {@link Advertisement} matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         *
         * @return The instance of {@link Advertisement}.
         */
        Advertisement newInstance();

        /**
         * Constructs an instance of {@link Advertisement} matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         *
         * @param root Specifies a portion of a @link StructuredDocument} which
         *             will be converted into an Advertisement.
         * @return The instance of {@link Advertisement}.
         */
        Advertisement newInstance(net.jxta.document.Element root);
    }

    /**
     * This class is a singleton. This is the instance that backs the
     * static methods.
     */
    private static AdvertisementFactory factory = new AdvertisementFactory();

    /**
     * This is the map of mime-types and constructors used by
     * <CODE>newStructuredDocument</CODE>.
     */
    private Map encodings = new HashMap();

    /**
     * If true then the pre-defined set of StructuredDocument sub-classes has
     * been registered from the property containing them.
     */
    private boolean loadedProperty = false;

    /**
     * Private constructor. This class is not meant to be instantiated except
     * by itself.
     */
    private AdvertisementFactory() {
    }

    private boolean doLoadProperty() {
        return registerAssoc(new net.jxta.impl.protocol.PeerAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.PlatformConfig.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.PeerGroupAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.TCPAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.HTTPAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.RdvConfigAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.DiscoveryConfigAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.PipeAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.RelayConfigAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.RdvAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.ModuleImplAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.ModuleSpecAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.ModuleClassAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.RouteAdv.Instantiator()) &&
                registerAssoc(new net.jxta.impl.protocol.AccessPointAdv.Instantiator())
                ;
    }

    /**
     * {@inheritDoc}
     */
    protected Map getAssocTable() {
        return encodings;
    }

    /**
     * {@inheritDoc}
     */
    public Class getClassOfInstantiators() {
        // our key is the doctype names.
        return Instantiator.class;
    }

    /**
     * {@inheritDoc}
     */
    public Class getClassForKey() {
        // our key is the doctype names.
        return java.lang.String.class;
    }


    protected boolean registerAssoc(Instantiator instantiator) {
        boolean registeredSomething = false;

        String advType = instantiator.getAdvertisementType();

        try {
            registeredSomething = registerAdvertisementInstance(advType, instantiator);
        }
        catch (Exception all) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Failed to register '" + advType + "'", all);
            }
        }

        return registeredSomething;
    }

    /**
     * Register an instantiator for and advertisement type to allow instances
     * of that type to be created.
     *
     * @param rootType     the identifying value for this advertisement instance
     *                     type
     * @param instantiator the instantiator to use in constructing objects
     *                     of this rootType.
     * @return boolean  true if the rootType type is registered. If there is
     *         already a constructor for this type then false will be returned.
     */
    public static boolean registerAdvertisementInstance(String rootType, Instantiator instantiator) {
        return factory.registerAssoc(rootType, instantiator);
    }

    /**
     * Constructs an instance of {@link Advertisement} matching the type
     * specified by the <CODE>advertisementType</CODE> parameter.
     *
     * @param advertisementType Specifies the type of advertisement to create.
     * @return The instance of {@link Advertisement}.
     * @throws java.util.NoSuchElementException
     *          if there is no matching advertisement type.
     */
    public static Advertisement newAdvertisement(String advertisementType) {
        if (! factory.loadedProperty) {
            factory.loadedProperty = factory.doLoadProperty();
        }

        Instantiator instantiator =
                (Instantiator) factory.getInstantiator(advertisementType);

        return instantiator.newInstance();
    }

    /**
     * Constructs an instance of {@link Advertisement} from the provided
     * <code>InputStream</code>. The content type of the stream is declared via
     * the <code>mimetype</code> parameter.
     *
     * @param mimetype Specifies the mime media type of the stream being read.
     * @param stream   imput stream used to read data to construct the advertisement
     * @return The instance of {@link Advertisement}
     * @throws java.io.IOException           error reading message from input stream
     * @throws java.util.NoSuchElementException
     *                                       if there is no matching advertisement type
     *                                       for the type of document read in.
     * @throws UnsupportedOperationException if the specified mime type is not
     *                                       associated with a text oriented document type.
     */
    public static Advertisement newAdvertisement(MimeMediaType mimetype, InputStream stream)
            throws IOException {

        StructuredTextDocument doc = (StructuredTextDocument)
                StructuredDocumentFactory.newStructuredDocument(mimetype, stream);

        return newAdvertisement(doc);
    }

    /**
     * Constructs an instance of {@link Advertisement} from the provided
     * <code>Reader</code>. The content type of the reader is declared via the
     * <code>mimetype</code> parameter.
     *
     * @param mimetype Specifies the mime media type of the stream being read.
     * @param source   used to read data to construct the advertisement.
     * @return The instance of {@link Advertisement}
     * @throws IOException                   error reading message from input stream
     * @throws java.util.NoSuchElementException
     *                                       if there is no matching advertisement type
     *                                       for the type of document read in.
     * @throws UnsupportedOperationException if the specified mime type is not
     *                                       associated with a text oriented document type.
     */
    public static Advertisement newAdvertisement(MimeMediaType mimetype, Reader source)
            throws IOException {
        StructuredTextDocument doc = (StructuredTextDocument)
                StructuredDocumentFactory.newStructuredDocument(mimetype, source);

        return newAdvertisement(doc);
    }

    /**
     * Constructs an instance of {@link Advertisement} matching the type
     * specified by the <CODE>root</CODE> parameter.
     *
     * @param root Specifies a portion of a StructuredDocument which will be
     *             converted into an Advertisement.
     * @return The instance of {@link Advertisement}.
     * @throws java.util.NoSuchElementException
     *          if there is no advertisement type
     *          matching the type of the root node.
     */
    public static Advertisement newAdvertisement(TextElement root) {
        if (! factory.loadedProperty) {
            factory.loadedProperty = factory.doLoadProperty();
        }

        Instantiator instantiator = null;

        // The base type of the advertisement may be overridden by a type
        // declaration. If this is the case, then we try to use that as the
        // key rather than the root name.
        if (root instanceof Attributable) {
            Attribute type = ((Attributable) root).getAttribute("type");

            if (null != type) {
                try {
                    instantiator = (Instantiator) factory.getInstantiator(type.getValue());
                } catch (NoSuchElementException notThere) {
                    // do nothing, its not fatal
                }
            }
        }

        // Don't have an instantiator for the type attribute, try the root name
        if (null == instantiator) {
            instantiator = (Instantiator) factory.getInstantiator(root.getName());
        }
        return instantiator.newInstance(root);
    }
}
