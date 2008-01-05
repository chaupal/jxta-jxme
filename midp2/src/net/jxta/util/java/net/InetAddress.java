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
package net.jxta.util.java.net;

import org.apache.log4j.Logger;


public class InetAddress {

    private final static Logger LOG = Logger.getInstance(InetAddress.class.getName());

    final static String IPV4ANYADDRESS = "0.0.0.0";
    final static String IPV4LOOPBACK = "127.0.0.1";

    /**
     * Constant which works as the IP "Any Address" value
     */
    public final static InetAddress ANYADDRESS;
    public final static InetAddress ANYADDRESSV4;

    /**
     * Constant which works as the IP "Local Loopback" value;
     */
    public final static InetAddress LOOPBACK;
    public final static InetAddress LOOPBACKV4;

    protected String address = null;
    protected int port = -1;

    /**
     *
     */
    static {
        ANYADDRESSV4 = new InetAddress(IPV4ANYADDRESS);
        ANYADDRESS = ANYADDRESSV4;

        LOOPBACKV4 = new InetAddress(IPV4LOOPBACK);
        LOOPBACK = LOOPBACKV4;
    }

    /**
     * @param a
     * @param p
     */
    public InetAddress(String a, int p) {
        address = a;
        port = p;
    }

    /**
     * @param a
     * @param p
     */
    public InetAddress(String a) {
        int index = a.indexOf(":");

        if (-1 == index)
            address = a;
        else {
            address = a.substring(0, index);

            if (a.length() > index + 1)
                port = Integer.parseInt(a.substring(index + 1));
        }
    }

    /**
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * @return
     */
    public String getPortString() {
        return (-1 != port) ? Integer.toString(port) : "";
    }

    /**
     * @return
     */
    public String toString() {
        return (-1 != port) ? address + ":" + port : address;
    }

    /**
     * @return
     */
    public boolean equals(InetAddress ia) {
        return address.equals(ia.getAddress()) && (port == ia.getPort());
    }

    /**
     * @return
     */
    public boolean isLoopbackAddress() {
        return this.equals(LOOPBACK);
    }
}
