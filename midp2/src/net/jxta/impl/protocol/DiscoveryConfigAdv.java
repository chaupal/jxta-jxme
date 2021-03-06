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
package net.jxta.impl.protocol;

import com.sun.java.util.collections.Collections;
import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.Map;
import net.jxta.document.*;
import net.jxta.id.ID;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;

/**
 * Contains parameters for configuration of the Reference Implemenation
 * Rendezvous Service. <p/>
 * <p/>
 * <pre><code>
 * <p/>
 * </code></pre>
 */
public final class DiscoveryConfigAdv extends ExtendableAdvertisement {

    private final static String FORWARD_ALWAYS_REPLICA = "forwardAlwaysReplica";
    private final static String FORWARD_BELOW_TRESHOLD = "forwardBelowThreshold";
    private final static String LOCAL_ONLY = "localOnly";

    /**
     * Log4J Logger
     */
    private final static Logger LOG = Logger.getInstance(DiscoveryConfigAdv.class.getName());

    /**
     * Our DOCTYPE
     */
    private final static String advType = "jxta:DiscoConfigAdv";

    private final static String[] fields = {};

    /**
     * If true, the discovery service will always forward queries to the
     * replica peer even if there are local responses, unless the replica peer
     * is the local peer.
     */
    private boolean forwardAlwaysReplica = false;

    /**
     * If true, the discovery service will always forward queries if the number
     * of local responses is below the specified threshold. The threshold may
     * be reduced by the number of local responses before forwarding. NOTE: not
     * yet implemented.
     */
    private boolean forwardBelowTreshold = false;

    /**
     * localOnly discovery.
     */
    private boolean localOnly = false;

    /**
     * Use the Instantiator through the factory
     */
    DiscoveryConfigAdv() {
    }

    /**
     * Use the Instantiator through the factory
     *
     * @param root Description of the Parameter
     */
    DiscoveryConfigAdv(Element root) {
        if (!XMLElement.class.isInstance(root)) {
            throw new IllegalArgumentException(getClass().getName() + " only supports XLMElement");
        }

        XMLElement doc = (XMLElement) root;

        String doctype = doc.getName();

        String typedoctype = "";
        Attribute itsType = doc.getAttribute("type");
        if (null != itsType) {
            typedoctype = itsType.getValue();
        }

        if (!doctype.equals(getAdvertisementType()) && !getAdvertisementType().equals(typedoctype)) {
            throw new IllegalArgumentException("Could not construct : "
                    + getClass().getName() + "from doc containing a " + doc.getName());
        }

        Enumeration eachAttr = doc.getAttributes();

        while (eachAttr.hasMoreElements()) {
            Attribute aDiscoAttr = (Attribute) eachAttr.nextElement();
            String name = aDiscoAttr.getName();
            boolean flag = "true".equals(aDiscoAttr.getValue().trim());

            if (FORWARD_ALWAYS_REPLICA.equals(name)) {
                forwardAlwaysReplica = flag;
            } else if (FORWARD_BELOW_TRESHOLD.equals(name)) {
                forwardBelowTreshold = flag;
            } else if (LOCAL_ONLY.equals(name)) {
                localOnly = flag;
            } else {
                if (LOG.isEnabledFor(Priority.WARN)) {
                    LOG.warn("Unhandled Attribute: " + name);
                }
            }
        }
    }

    /**
     * Make a safe clone of this DiscoveryConfigAdv.
     *
     * @return Object A copy of this DiscoveryConfigAdv
     */
    public Object clone() {
        return new DiscoveryConfigAdv();
    }

    /**
     * {@inheritDoc}
     *
     * @return The advType value
     */
    public String getAdvType() {
        return getAdvertisementType();
    }

    /**
     * {@inheritDoc}
     *
     * @return The advertisementType value
     */
    public static String getAdvertisementType() {
        return advType;
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
     * {@inheritDoc}
     *
     * @param encodeAs Description of the Parameter
     * @return The document value
     */
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredDocument adv = (StructuredDocument) super.getDocument(encodeAs);

        if (adv instanceof Attributable) {
            Attributable attrDoc = (Attributable) adv;

            // Do not output if false. It is the default value.
            if (forwardAlwaysReplica) {
                attrDoc.addAttribute(FORWARD_ALWAYS_REPLICA, "true");
            }
            if (forwardBelowTreshold) {
                attrDoc.addAttribute(FORWARD_BELOW_TRESHOLD, "true");
            }
            if (localOnly) {
                attrDoc.addAttribute(FORWARD_BELOW_TRESHOLD, "true");
            }
        }
        return adv;
    }

    /**
     * True if this discovery service will forward queries to the replica peer
     * in all cases, rather than only in the absence of local responses.
     *
     * @return The current setting.
     */
    public boolean getForwardAlwaysReplica() {
        return forwardAlwaysReplica;
    }

    /**
     * True if this discovery service will forward queries when the number of
     * local responses is below the specified treshold, rather than only in the
     * absence of local responses.
     *
     * @return The current setting.
     */
    public boolean getForwardBelowTreshold() {
        return forwardBelowTreshold;
    }

    /**
     * {@inheritDoc}
     *
     * @return The iD value
     */
    public ID getID() {
        return ID.nullID;
    }

    /**
     * {@inheritDoc}
     *
     * @return The indexFields value
     */
    public String[] getIndexFields() {
        return fields;
    }

    /**
     * {@inheritDoc}
     *
     * @return The indexMap value
     */
    public final Map getIndexMap() {
        return Collections.unmodifiableMap(new HashMap());
    }

    /**
     * True if this discovery service performs only local discovery.
     *
     * @return The current setting.
     */
    public boolean getLocalOnly() {
        return localOnly;
    }

    /**
     * Specifies if this discovery service will forward queries to the replica
     * peer in all cases, rather than only in the absence of local responses.
     *
     * @param newvalue The new forwardAlwaysReplica value
     */
    public void setForwardAlwaysReplica(boolean newvalue) {
        forwardAlwaysReplica = newvalue;
    }

    /**
     * Specifies if this discovery service will forward queries when the number
     * of local responses is below the specified treshold, rather than only in
     * the absence of local responses.
     *
     * @param newvalue The new forwardBelowTreshold value
     */
    public void setForwardBelowTreshold(boolean newvalue) {
        forwardBelowTreshold = newvalue;
    }

    /**
     * Specifies if this discovery service will perform local only discovery.
     *
     * @param newvalue The new localOnly value
     */
    public void setLocalOnly(boolean newvalue) {
        localOnly = newvalue;
    }

    /**
     * Instantiator for DiscoveryConfigAdv
     */
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        /**
         * {@inheritDoc}
         *
         * @return The advertisementType value
         */
        public String getAdvertisementType() {
            return advType;
        }

        /**
         * {@inheritDoc}
         *
         * @return Description of the Return Value
         */
        public Advertisement newInstance() {
            return new DiscoveryConfigAdv();
        }

        /**
         * {@inheritDoc}
         *
         * @param root Description of the Parameter
         * @return Description of the Return Value
         */
        public Advertisement newInstance(Element root) {
            return new DiscoveryConfigAdv(root);
        }
    }
}
