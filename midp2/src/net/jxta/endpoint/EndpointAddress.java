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
package net.jxta.endpoint;

import com.sun.java.util.collections.UnsupportedOperationException;
import net.jxta.id.ID;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.UnsupportedEncodingException;

public class EndpointAddress {

    /**
     * Log4J Logger
     */
    private static final Logger LOG = Logger.getInstance(EndpointAddress.class.getName());

    /**
     * if true then the address is a url, otherwise its a uri (likely a urn).
     */
    private boolean hierarchical = true;

    /**
     * Describes the method of addressing used by the remainder of the
     * endpoint address.
     */
    private String protocol = null;

    /**
     * Describes the destination entity of this address.
     */
    private String protocolAddress = null;

    /**
     * Describes the service that is the destination.
     */
    private String service = null;

    /**
     * Describes parameters for the service.
     */
    private String serviceParam = null;

    /**
     * number of modifications since initial creation. used in caching of
     * string representation and hashCode.
     */
    private transient volatile int modcount = 0;

    /**
     * mod count at last hash code calc
     */
    private transient int modAtLastHashCalc = -1;

    /**
     * cached calculated hash code.
     */
    private transient int cachedHashCode = 0;

    /**
     * mod count at last string representation creation.
     */
    private transient int modAtLastStringCalc = -1;

    /**
     * Returns an unmodifiable copy of the specified EndpointAddress. This
     * method allows modules to provide users with "read-only" access to
     * an endpoint address without needing to make a clone for each copy
     * returned. Attempts to modify the returned EndpointAddress results
     * in an {@link java.lang.UnsupportedOperationException}.
     */
    private static class UnmodifiableEndpointAddress extends EndpointAddress {

        /**
         * Create a new unmodifiable endpoint address.
         */
        public UnmodifiableEndpointAddress(EndpointAddress address) {
            super();
            super.setProtocolName(address.getProtocolName());
            super.setProtocolAddress(address.getProtocolAddress());
            super.setServiceName(address.getServiceName());
            super.setServiceParameter(address.getServiceParameter());
        }

        /**
         * {@inheritDoc}
         * <p/>
         * <p/>throws an {@link java.lang.UnsupportedOperationException} for every operation.
         */
        public void setProtocolName(String name) {
            throw new UnsupportedOperationException("This EndpointAddress is not modifiable");
        }

        /**
         * {@inheritDoc}
         * <p/>
         * <p/>throws an {@link java.lang.UnsupportedOperationException} for every operation.
         */
        public void setProtocolAddress(String address) {
            throw new UnsupportedOperationException("This EndpointAddress is not modifiable");
        }

        /**
         * {@inheritDoc}
         * <p/>
         * <p/>throws an {@link java.lang.UnsupportedOperationException} for every operation.
         */
        public void setServiceName(String name) {
            throw new UnsupportedOperationException("This EndpointAddress is not modifiable");
        }

        /**
         * {@inheritDoc}
         * <p/>
         * <p/>throws an {@link java.lang.UnsupportedOperationException} for every operation.
         */
        public void setServiceParameter(String param) {
            throw new UnsupportedOperationException("This EndpointAddress is not modifiable");
        }
    }

    /**
     * Returns an unmodifiable clone of the provided EndpointAddress.
     *
     * @param address the address to be cloned.
     * @return the unmodifiable address clone.
     */
    public static EndpointAddress unmodifiableEndpointAddress(EndpointAddress address) {
        return new EndpointAddress.UnmodifiableEndpointAddress(address);
    }

    /**
     * Builds an empty (invalid) Endpoint Address.
     *
     * @deprecated EndpointAddress works better if it is immutable.
     */
    public EndpointAddress() {
    }

    /**
     * Builds an Address from a string
     *
     * @param address the string representation of the address.
     */
    public EndpointAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        parseURI(address);
    }

    /**
     * Create an EndpointAddress whose value is initialized from the provided
     * URI.
     *
     * @param address the URI representation of the address.
     */
    public EndpointAddress(URI address) {
        this(address.toString());
    }

    /**
     * Constructor which builds an address from a byte array containing a UTF-8
     * string.
     *
     * @param bytes byte array containing a UTF-8 string of the endpoint address to be constructed.
     * @deprecated There isn't really ever a good reason to use this since it
     *             has to assume the character encoding.
     */
    public EndpointAddress(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null");
        }

        try {
            parseURI(new String(bytes, "UTF8"));
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException(ex.toString());
        }
    }

    /**
     * Constructor which builds an endpoint address from a base address and
     * replacement service and params
     *
     * @param base         The EndpointAddress on which the new EndpointAddress will be based
     * @param service      provides an alternate service for the new EndpointAddress.
     * @param serviceParam provides and alternate service parameter for the new EndpointAddress
     */
    public EndpointAddress(EndpointAddress base, String service, String serviceParam) {
        if (base == null) {
            throw new IllegalArgumentException("base EndpointAddress must not be null");
        }

        setProtocolName(base.getProtocolName());
        setProtocolAddress(base.getProtocolAddress());
        setServiceName(service);
        setServiceParameter(serviceParam);
    }

    /**
     * Constructor which builds an address the four standard constituent parts.
     *
     * @param address      Describes the destination entity of this address.
     * @param protocol     Describes the method of addressing used by the remainder of the
     *                     endpoint address.
     * @param service      String containing the name of the destination service
     * @param serviceParam String containing the service parameter
     */
    public EndpointAddress(String protocol, String address, String service, String serviceParam) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol must not be null");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        setProtocolName(protocol);
        setProtocolAddress(address);
        setServiceName(service);
        setServiceParameter(serviceParam);
    }

    /**
     * Constructor which builds an address from a standard jxta id and a
     * service and param.
     *
     * @param id           the ID which will be the destination of the endpoint address
     * @param service      String containing the name of the destination service
     * @param serviceParam String containing the service parameter
     */
    public EndpointAddress(ID id, String service, String serviceParam) {
        if (null == id) {
            throw new IllegalArgumentException("id must not be null");
        }

        setProtocolName(ID.URIEncodingName + ":" + ID.URNNamespace);
        setProtocolAddress(id.getUniqueValue().toString());
        setServiceName(service);
        setServiceParameter(serviceParam);
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() {
        EndpointAddress clone = new EndpointAddress(getProtocolName(),
                getProtocolAddress(),
                getServiceName(),
                getServiceParameter());

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }

        if (target instanceof EndpointAddress) {
            EndpointAddress likeMe = (EndpointAddress) target;

            boolean result;

            if ((null == protocol) || (null == protocolAddress)) {
                throw new IllegalStateException("Corrupt EndpointAddress, protocol or address is null");
            }

            result = (hierarchical == likeMe.hierarchical) &&
                    protocol.equalsIgnoreCase(likeMe.protocol) &&
                    protocolAddress.equalsIgnoreCase(likeMe.protocolAddress) &&
                    ((service != null) ? ((likeMe.service != null) && service.equals(likeMe.service)) : (likeMe.service == null)) &&
                    ((serviceParam != null) ? ((likeMe.serviceParam != null) && serviceParam.equals(likeMe.serviceParam)) : (likeMe.serviceParam == null));

            return result;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized int hashCode() {
        if (modcount != modAtLastHashCalc) {
            if ((null == protocol) || (null == protocolAddress)) {
                throw new IllegalStateException("Corrupt EndpointAddress, protocol or address is null");
            }

            cachedHashCode = protocol.toLowerCase().hashCode();
            cachedHashCode += protocolAddress.hashCode() * 5741;   // a prime
            cachedHashCode += ((service != null) ? service.hashCode() : 1) * 7177; // a prime
            cachedHashCode += ((serviceParam != null) ? serviceParam.hashCode() : 1) * 6733; // a prime

            cachedHashCode = (0 == cachedHashCode) ? 1 : cachedHashCode;
            modAtLastHashCalc = modcount;
        }

        return cachedHashCode;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized String toString() {
        String result;

        if ((null == protocol) || (null == protocolAddress)) {
            throw new IllegalStateException("Corrupt EndpointAddress, protocol or address is null");
        }

        StringBuffer newResult = new StringBuffer(protocol.length() + protocolAddress.length() + 64);

        newResult.append(protocol);

        if (hierarchical) {
            newResult.append("://");
        } else {
            newResult.append(':');
        }

        newResult.append(protocolAddress);

        if (null != service) {
            if (hierarchical) {
                newResult.append('/');
            } else {
                newResult.append('#');
            }
            newResult.append(service);

            if (null != serviceParam) {
                newResult.append('/');
                newResult.append(serviceParam);
            }
        }

        result = newResult.toString();
        modAtLastStringCalc = modcount;
        return result;
    }

    /**
     * Return a URI which represents the endpoint address.
     *
     * @return a URI which represents the endpoint address.
     */
    public URI toURI() {
        return URI.create(toString());
    }

    /**
     * Get a byte array containing a UTF-8 representation of the address.
     *
     * @return a byte array containing the bytes of a UTF-8 representation of the endpoint
     *         address.
     * @deprecated There isn't really ever a good reason to use this since it
     *             has to assume the character encoding.
     */
    public byte[] getBytes() {
        try {
            return toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 is built in to all jdk
            RuntimeException failure = new UnsupportedOperationException("Could not get UTF-8 encoding");
            throw failure;
        }
    }

    /**
     * Return a String that contains the name of the protocol
     * contained in the EndpointAddress
     *
     * @return a String containing the protocol name
     */
    public String getProtocolName() {
        return protocol;
    }

    /**
     * Return a String that contains the protocol address contained
     * in the EndpointAddress
     *
     * @return a String containing the protocol address
     */
    public String getProtocolAddress() {
        return protocolAddress;
    }

    /**
     * Return a String that contains the service name contained in
     * the EndpointAddress
     *
     * @return a String containing the service name
     */
    public String getServiceName() {
        return service;
    }

    /**
     * Return a String that contains the service parameter contained
     * in the EndpointAddress
     *
     * @return a String containing the protocol name
     */
    public String getServiceParameter() {
        return serviceParam;
    }

    /**
     * Set the protocol name.
     *
     * @param name String containing the name of the protocol
     * @deprecated EndpointAddress works better if it is immutable.
     */
    public synchronized void setProtocolName(String name) {
        if ((null == name) || (0 == name.length())) {
            throw new IllegalArgumentException("name must be non-null and contain at least one character");
        }

        if (-1 != name.indexOf("/")) {
            throw new IllegalArgumentException("name may not contain '/' character");
        }

        int colonAt = name.indexOf(':');

        if (-1 == colonAt) {
            hierarchical = true;
        } else {
            if (!"urn".equalsIgnoreCase(name.substring(0, colonAt))) {
                throw new IllegalArgumentException("Only urn may contain colon");
            }

            if (colonAt == (name.length() - 1)) {
                throw new IllegalArgumentException("empty urn namespace!");
            }

            hierarchical = false;
        }

        protocol = name;
        modcount++;
    }

    /**
     * Set the protocol address.
     *
     * @param address String containing the peer address.
     * @deprecated EndpointAddress works better if it is immutable.
     */
    public synchronized void setProtocolAddress(String address) {
        if ((null == address) || (0 == address.length())) {
            throw new IllegalArgumentException("address must be non-null and contain at least one character");
        }

        if (-1 != address.indexOf("/")) {
            throw new IllegalArgumentException("address may not contain '/' character");
        }

        protocolAddress = address;
        modcount++;
    }

    /**
     * Set the service name.
     *
     * @param name String containing the name of the destination service
     * @deprecated EndpointAddress works better if it is immutable.
     */
    public synchronized void setServiceName(String name) {
        if (null == name) {
            service = null;
            modcount++;
            return;
        }

        if (-1 != name.indexOf("/")) {
            throw new IllegalArgumentException("service name may not contain '/' character");
        }

        service = name;
        modcount++;
    }

    /**
     * Set the service parameter
     *
     * @param param String containing the service parameter
     * @deprecated EndpointAddress works better if it is immutable.
     */
    public synchronized void setServiceParameter(String param) {
        if (null == param) {
            serviceParam = null;
            modcount++;
            return;
        }

        serviceParam = param;
        modcount++;
    }

    /**
     * parse any EndpointAddress from a URI
     *
     * @param addr endpoint address to parse
     */
    private void parseURI(String addr) {
        int index = addr.indexOf("://");
        if (index == -1) {
            parseURN(addr);
        } else {
            parseURL(addr);
        }
    }

    /**
     * parse an EndpointAddress from a URN
     *
     * @param addr endpoint address to parse
     */
    private void parseURN(String addr) {
        int protocolEnd = addr.indexOf(':');

        if (-1 == protocolEnd) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Address is not a valid URI: " + addr);
            }
            throw new IllegalArgumentException("Address is not a valid URI: " + addr);
        }

        if (!"urn".equalsIgnoreCase(addr.substring(0, protocolEnd))) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Address is unrecognized URI form: " + addr);
            }
            throw new IllegalArgumentException("Address is unrecognized URI form: " + addr);
        }

        if ((addr.length() - 1) == protocolEnd) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Address URN does not have a namespace: " + addr);
            }
            throw new IllegalArgumentException("Address URN does not have a namespace: " + addr);
        }

        // gather the namespace as well.
        int namespaceEnd = addr.indexOf(':', protocolEnd + 1);

        if (-1 == namespaceEnd) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Address URN does not have a namespace: " + addr);
            }
            throw new IllegalArgumentException("Address URN does not have a namespace: " + addr);
        }

        setProtocolName(addr.substring(0, namespaceEnd));

        if ((addr.length() - 1) == namespaceEnd) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Address URN does not have a NSS portion: " + addr);
            }
            throw new IllegalArgumentException("Address URN does not have a NSS portion: " + addr);
        }

        // check for service and param
        int nssEnd = addr.indexOf('#', namespaceEnd + 1);

        if (-1 == nssEnd)
            setProtocolAddress(addr.substring(namespaceEnd + 1));
        else {
            setProtocolAddress(addr.substring(namespaceEnd + 1, nssEnd));

            int serviceEnd = addr.indexOf('/', nssEnd + 1);

            if (-1 == serviceEnd) {
                setServiceName(addr.substring(nssEnd + 1));
            } else {
                setServiceName(addr.substring(nssEnd + 1, serviceEnd));

                setServiceParameter(addr.substring(serviceEnd + 1));
            }
        }
    }

    /**
     * parse and EndpointAddress from a URL
     *
     * @param addr endpoint address to parse
     */
    private void parseURL(String addr) {
        String remainder = null;

        int index = addr.indexOf("://");
        if (index == -1) {
            if (LOG.isEnabledFor(Priority.DEBUG))
                LOG.debug("Address is not in absolute form: " + addr);
            throw new IllegalArgumentException("Address is not in absolute form: " + addr);
        }

        if (0 == index) {
            if (LOG.isEnabledFor(Priority.DEBUG))
                LOG.debug("Protocol is missing: " + addr);
            throw new IllegalArgumentException("Protocol is missing: " + addr);
        }

        try {
            setProtocolName(addr.substring(0, index));
            remainder = addr.substring(index + 3);
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Protocol address is missing: " + addr);
            }
            throw new IllegalArgumentException("Protocol address is missing: " + addr);
        }
        index = remainder.indexOf("/");
        if (index == -1) {
            setProtocolAddress(remainder);
            return;
        }

        setProtocolAddress(remainder.substring(0, index));

        remainder = remainder.substring(index + 1);

        index = remainder.indexOf("/");
        if (index == -1) {
            setServiceName(remainder);
            return;
        }

        setServiceName(remainder.substring(0, index));

        remainder = remainder.substring(index + 1);

        setServiceParameter(remainder);
    }
}
