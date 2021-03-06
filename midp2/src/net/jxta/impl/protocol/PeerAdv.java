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

import net.jxta.document.*;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Implementation of {@link PeerAdvertisement} matching the standard JXTA
 * Protocol Specification. It implements Peer Advertisement using the following
 * schema: <pre><tt>
 * &lt;xs:complexType name="PA">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="PID" type="JXTAID"/>
 *     &lt;xs:element name="GID" type="JXTAID"/>
 *     &lt;xs:element name="Name" type="xs:string" minOccurs="0"/>
 *     &lt;xs:element name="Desc" type="xs:anyType" minOccurs="0"/>
 *     &lt;xs:element name="Svc" type="jxta:serviceParams" minOccurs="0" maxOccurs="unbounded"/>
 *   &lt;xs:sequence>
 * &lt;/xs:complexType>
 * </tt> </pre>
 *
 * @see net.jxta.protocol.PeerAdvertisement
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#advert-pa"
 *      target="_blank">JXTA Protocols Specification : Peer Advertisement</a>
 */
public class PeerAdv extends PeerAdvertisement {
    /**
     * Log4J Logger
     */
    private final static Logger LOG = Logger.getInstance(PeerAdv.class.getName());

    /**
     * Private Constructor, use the instantiator
     */
    PeerAdv() {
        super();
    }

    /**
     * Private Constructor, use the instantiator
     *
     * @param root Description of the Parameter
     */
    PeerAdv(Element root) {
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

        Enumeration elements = doc.getChildren();

        while (elements.hasMoreElements()) {
            XMLElement elem = (XMLElement) elements.nextElement();

            if (!handleElement(elem)) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("Unhandled Element: " + elem.toString());
                }
            }
        }

        // Sanity Check!!!

        // sanity check time!
        if (null == getPeerID()) {
            throw new IllegalArgumentException("Peer Advertisement did not contain a peer id.");
        }

        if (null == getPeerGroupID()) {
            throw new IllegalArgumentException("Peer Advertisement did not contain a peer group id.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param encodeAs Description of the Parameter
     * @return The document value
     */
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredDocument adv = (StructuredDocument) super.getDocument(encodeAs);

        PeerID peerID = getPeerID();
        if ((null == peerID) || ID.nullID.equals(peerID)) {
            throw new IllegalStateException("Cannot generate Peer Advertisement with no Peer ID!");
        }
        Element e = adv.createElement(pidTag, peerID.toString());
        adv.appendChild(e);

        PeerGroupID groupID = getPeerGroupID();
        if ((null == groupID) || ID.nullID.equals(groupID)) {
            throw new IllegalStateException("Cannot generate Peer Advertisement with no group ID!");
        } else {
            e = adv.createElement(gidTag, groupID.toString());
            adv.appendChild(e);
        }

        // name is optional
        if (getName() != null) {
            e = adv.createElement(nameTag, getName());
            adv.appendChild(e);
        }

        // desc is optional
        StructuredDocument desc = getDesc();
        if (desc != null) {
            StructuredDocumentUtils.copyElements(adv, adv, desc);
        }

        // service params are optional
        // FIXME: this is inefficient - we force our base class to make
        // a deep clone of the table.
        Hashtable serviceParams = getServiceParams();
        Enumeration classIds = serviceParams.keys();
        while (classIds.hasMoreElements()) {
            ModuleClassID classId = (ModuleClassID) classIds.nextElement();

            Element s = adv.createElement(svcTag);
            adv.appendChild(s);

            e = adv.createElement(mcidTag, classId.toString());
            s.appendChild(e);

            e = (Element) serviceParams.get(classId);
            StructuredDocumentUtils.copyElements(adv, s, e, paramTag);
        }
        return adv;
    }

    /**
     * {@inheritDoc}
     *
     * @return The indexFields value
     */
    public final String[] getIndexFields() {
        return fields;
    }

    /**
     * {@inheritDoc}
     *
     * @param raw Description of the Parameter
     * @return Description of the Return Value
     */
    protected boolean handleElement(Element raw) {

        if (super.handleElement(raw)) {
            return true;
        }

        XMLElement elem = (XMLElement) raw;

        if (elem.getName().equals(pidTag)) {
            setPeerID((PeerID) ID.create(URI.create(elem.getTextValue())));
            return true;
        }

        if (elem.getName().equals(gidTag)) {
            setPeerGroupID((PeerGroupID) ID.create(URI.create(elem.getTextValue())));
            return true;
        }

        if (elem.getName().equals(nameTag)) {
            setName(elem.getTextValue());
            return true;
        }

        if (elem.getName().equals(descTag)) {
            setDesc(elem);
            return true;
        }

        if (elem.getName().equals(svcTag)) {
            Enumeration elems = elem.getChildren();
            ModuleClassID classID = null;
            Element param = null;
            while (elems.hasMoreElements()) {
                XMLElement e = (XMLElement) elems.nextElement();
                if (e.getName().equals(mcidTag)) {
                    classID = (ModuleClassID) ID.create(URI.create(e.getTextValue()));
                    continue;
                }
                if (e.getName().equals(paramTag)) {
                    param = e;
                }
            }
            if (classID != null && param != null) {
                // Add this param to the table. putServiceParam()
                // clones param into a standalone document automatically.
                // (classID gets cloned too).
                putServiceParam(classID, param);
            }
            return true;
        }

        // element was not handled
        return false;
    }

    /**
     * Creates instances of PeerAdvertisement.
     */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /**
         * {@inheritDoc}
         *
         * @return The advertisementType value
         */
        public String getAdvertisementType() {
            return PeerAdvertisement.getAdvertisementType();
        }

        /**
         * {@inheritDoc}
         *
         * @return Description of the Return Value
         */
        public Advertisement newInstance() {
            return new PeerAdv();
        }

        /**
         * {@inheritDoc}
         *
         * @param root Description of the Parameter
         * @return Description of the Return Value
         */
        public Advertisement newInstance(Element root) {
            return new PeerAdv(root);
        }
    }
}
