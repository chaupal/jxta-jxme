/*
 *
 * $Id: Element.java,v 1.3 2006/06/09 19:14:24 hamada Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 */

package net.jxta.j2me;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This class represents an Element of a JXTA {@link
 * net.jxta.j2me.Message}. A JXTA Message is composed of several
 * Elements.<p>
 *
 * This is an immutable class.
 */

public final class Element {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    // flags
    private static final int HAS_TYPE  = 0x01;

    private static final String JXTA_ELEMENT_HEADER = "jxel";

    private String name = null;
    private byte[] data = null;
    private String nameSpace = null;
    private String mimeType = null;

    /**
     * Construct an Element from its parts.
     *
     * @param name the name of the Element
     *
     * @param data the data that this Element carries. This data is
     * transported across the network as-is.
     *
     * @param nameSpace the name space used by the Element. JXTA
     * messages use the <code>"jxta"</code> namespace. JXTA for J2ME
     * messages use a private namespace. If namespace is
     * <code>null</code>, the default namespace of <code>""</code> is
     * used.
     *
     * @param mimeType the mimeType of the data. If <code>null</code>,
     * the default MIME type of
     * <code>"application/octet-stream"</code> is assumed.
     */
    public Element(String name, byte[] data, 
                   String nameSpace, String mimeType) {
        if (name == null) {
            throw new IllegalArgumentException("Element name cannot be null");
        }
        this.name = name;

        if (data == null) {
            throw new IllegalArgumentException("Element data cannot be null");
        }
        this.data = data;

        if (nameSpace == null) {
            this.nameSpace = Message.DEFAULT_NAME_SPACE;
        } else {
            this.nameSpace = nameSpace;
        }

        if (mimeType == null) {
            this.mimeType = DEFAULT_MIME_TYPE;
        } else {
            this.mimeType = mimeType;
        }
    }

    void write(DataOutputStream dos, 
               Hashtable static_ns2id, Hashtable ns2id) 
        throws IOException {

        // write element signature
        for (int i=0; i < JXTA_ELEMENT_HEADER.length(); i++) {
            dos.writeByte(JXTA_ELEMENT_HEADER.charAt(i));
        }

        // write element name space id

        // initialize nsId to the index of Message.DEFAULT_NAME_SPACE
        int nsId = 0;
        Integer nsIdInt = (Integer) static_ns2id.get(nameSpace);
        if (nsIdInt != null) {
            nsId = nsIdInt.intValue();
        } else {
            nsIdInt = (Integer) ns2id.get(nameSpace);
            if (nsIdInt != null) {
                nsId = nsIdInt.intValue() + static_ns2id.size();
            }
        }
        dos.writeByte(nsId);

        // write element flags
        byte flags = 0;
        if (!DEFAULT_MIME_TYPE.equals(mimeType)) {
            flags |= HAS_TYPE;
        }
        dos.writeByte(flags);

        // write element name
        Message.writeString(dos, name);

        // write element mime type
        if ((flags & HAS_TYPE) != 0) {
            Message.writeString(dos, mimeType);
        }

        // write element data
        dos.writeInt(data.length);
        dos.write(data);
    }

    static Element read(DataInputStream dis, 
                        Hashtable static_id2ns, Hashtable id2ns) 
        throws IOException {

        // read element signature
        for (int i=0; i < JXTA_ELEMENT_HEADER.length(); i++) {
            if (dis.readByte() != JXTA_ELEMENT_HEADER.charAt(i)) {
                throw new IOException("Message element header not found");
            }
        }

        // read element name space id
        int nsId = dis.readByte();
        Integer nsIdInt = new Integer(nsId);
        String ns = (String) static_id2ns.get(nsIdInt);
        if (ns == null) {
            nsId -= static_id2ns.size();
            nsIdInt = new Integer(nsId);
            ns = (String) id2ns.get(nsIdInt);
            if (ns == null) {
                throw new IOException("Namespace not found for id " + nsId);
            }
        }

        // read element flags
        byte flags = dis.readByte();

        // read element name
        String name = Message.readString(dis);

        // read element mime type
        String mimeType = DEFAULT_MIME_TYPE;
        if ((flags & HAS_TYPE) != 0) {
            mimeType = Message.readString(dis);
        }

        // read element data
        int len = dis.readInt();
        byte[] data = new byte[len];
        dis.readFully(data);

        return new Element(name, data, ns, mimeType);
    }

    /**
     * Return the name of the Element.
     *
     * @return the Element name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the namespace used by the Element name.
     *
     * @return the Element namespace
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Return the MIME type of the data in the Element.
     *
     * @return the Element MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Return the data in the Element.
     *
     * @return the Element data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Return a String representation of the Element.
     *
     * @return a string representation of the Element
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("\"");
        sb.append(nameSpace);
        sb.append(":");
        sb.append(name);
        sb.append("\" \"");
        sb.append(mimeType);
        sb.append("\" dlen=");
        sb.append(Integer.toString(data.length));

        return sb.toString();
    }
}

