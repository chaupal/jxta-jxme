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
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;

/**
 * This class implements the Module Implemenation Advertisement according to
 * the schema defined by the JXTA Core Specification. <p/>
 * <p/>
 * <pre>
 * &lt;xs:complexType name="MIA">
 *   &lt;xs:sequence>
 *     &lt;xs:element name="MSID" type="jxta:JXTAID"/>
 *     &lt;xs:element name="Comp" type="xs:anyType"/>
 *     &lt;xs:element name="Code" type="xs:anyType"/>
 *     &lt;xs:element name="PURI" type="xs:anyURI" minOccurs="0"/>
 *     &lt;xs:element name="Prov" type="xs:string" minOccurs="0"/>
 *     &lt;xs:element name="Desc" type="xs:anyType" minOccurs="0"/>
 *     &lt;xs:element name="Parm" type="xs:anyType" minOccurs="0"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 *
 * @see net.jxta.document.Advertisement
 * @see net.jxta.protocol.ModuleImplAdvertisement
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#advert-mia>
 *      target='_blank'>JXTA Protocols Specification - Advertisements : Module
 *      Implementation Advertisement</a>
 */
public class ModuleImplAdv extends ModuleImplAdvertisement {

    /**
     * Log4J Logger
     */
    private final static Logger LOG = Logger.getInstance(ModuleImplAdv.class.getName());

    /**
     * Constructor for the ModuleImplAdv object
     */
    public ModuleImplAdv() {
        super();
    }

    /**
     * Constructor for the ModuleImplAdv object
     *
     * @param root Description of the Parameter
     */
    public ModuleImplAdv(Element root) {
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

        ID moduleID = getModuleSpecID();
        if ((null == moduleID) || (moduleID.equals(ID.nullID))) {
            throw new IllegalArgumentException("Module Spec ID was not initialized by advertisement");
        }

        if (null == getCode() || (0 == getCode().length())) {
            throw new IllegalArgumentException("Code was not initialized by advertisement");
        }

        Element compat = getCompat();
        if (null == compat) {
            throw new IllegalArgumentException("Compatibility statement was not initialized by advertisement");
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

        // sanity check time!

        ID moduleID = getModuleSpecID();
        if ((null == moduleID) || (moduleID.equals(ID.nullID))) {
            throw new IllegalStateException("Module Spec ID is not initialized.");
        }

        if (null == getCode() || (0 == getCode().length())) {
            throw new IllegalStateException("Code is not initialized.");
        }

        Element compat = getCompatPriv();
        if (null == compat) {
            throw new IllegalStateException("Compatibility statement is not initialized.");
        }

        // create the document

        Element e;

        e = adv.createElement(msidTag, getModuleSpecID().toString());
        adv.appendChild(e);

        // Copy the compat document as an element of adv.
        StructuredDocumentUtils.copyElements(adv, adv, compat, compTag);

        if (getCode() != null) {
            e = adv.createElement(codeTag, getCode());
            adv.appendChild(e);
        }

        if (getUri() != null) {
            e = adv.createElement(uriTag, getUri());
            adv.appendChild(e);
        }

        if (getProvider() != null) {
            e = adv.createElement(provTag, getProvider());
            adv.appendChild(e);
        }

        if (getDescription() != null) {
            e = adv.createElement(descTag, getDescription());
            adv.appendChild(e);
        }

        Element param = getParamPriv();
        // Copy the param document as an element of adv.
        if (param != null) {
            // Force the element to be named "Parm" even if that is not
            // the name of paramDoc.
            StructuredDocumentUtils.copyElements(adv, adv, param, paramTag);
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

        String nm = elem.getName();

        if (nm.equals(msidTag)) {
            setModuleSpecID((ModuleSpecID) ID.create(URI.create(elem.getTextValue())));
            return true;
        }

        if (nm.equals(compTag)) {
            // setCompat keeps a copy a stand alone document.
            setCompat(elem);
            return true;
        }

        if (nm.equals(codeTag)) {
            setCode(elem.getTextValue());
            return true;
        }

        if (nm.equals(uriTag)) {
            setUri(elem.getTextValue());
            return true;
        }

        if (nm.equals(provTag)) {
            setProvider(elem.getTextValue());
            return true;
        }

        if (nm.equals(descTag)) {
            setDescription(elem.getTextValue());
            return true;
        }

        if (nm.equals(paramTag)) {
            // setParam keep a copy of this element as a standalone
            // document.
            setParam(elem);
            return true;
        }

        return false;
    }

    /**
     * Create new instances of ModuleImplAdv
     */
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        /**
         * {@inheritDoc}
         *
         * @return The advertisementType value
         */
        public String getAdvertisementType() {
            return ModuleImplAdv.getAdvertisementType();
        }

        /**
         * {@inheritDoc}
         *
         * @return Description of the Return Value
         */
        public Advertisement newInstance() {
            return new ModuleImplAdv();
        }

        /**
         * {@inheritDoc}
         *
         * @param root Description of the Parameter
         * @return Description of the Return Value
         */
        public Advertisement newInstance(Element root) {
            return new ModuleImplAdv(root);
        }
    }
}
