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
package net.jxta.impl.document;

import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.util.CharUtils;
import net.jxta.util.java.io.BufferedWriter;
import net.jxta.util.java.io.StringReader;
import net.jxta.util.java.io.StringWriter;

import java.io.*;

/**
 * This class is an implementation of the StructuredDocument interface using
 * a simplified XML implementation.
 */
public class LiteXMLDocument extends LiteXMLElement implements XMLDocumentCommon {

    private final static class Instantiator implements StructuredDocumentFactory.TextInstantiator {

        // "x-" is a mime-type convention for indicating partial or provisional
        // compliance to a standard
        private static final MimeMediaType[] myTypes = {
                MimeMediaType.XML_DEFAULTENCODING, new MimeMediaType("Text", "x-Xml"), new MimeMediaType("Application", "Xml"),
                new MimeMediaType("Application", "x-Xml"),
        };

        // these are the file extensions which are likely to contain files of
        // the type i like.
        private static final ExtensionMapping[] myExtensions = {
                new ExtensionMapping("xml", myTypes[0]), new ExtensionMapping("xml", null)
        };

        /**
         * Creates new LiteXMLDocumentInstantiator
         */
        public Instantiator() {
        }

        /**
         * {@inheritDoc}
         */
        public MimeMediaType[] getSupportedMimeTypes() {
            return (myTypes);
        }

        /**
         * {@inheritDoc}
         */
        public ExtensionMapping[] getSupportedFileExtensions() {
            return (myExtensions);
        }

        /**
         * {@inheritDoc}
         */
        public StructuredDocument newInstance(MimeMediaType mimeType, String doctype) {
            return new LiteXMLDocument(mimeType, doctype);
        }

        /**
         * {@inheritDoc}
         */
        public StructuredDocument newInstance(MimeMediaType mimeType, String doctype, String value) {
            return new LiteXMLDocument(mimeType, doctype, value);
        }

        /**
         * {@inheritDoc}
         */
        public StructuredDocument newInstance(MimeMediaType mimeType, InputStream source) throws IOException {
            return new LiteXMLDocument(mimeType, source);
        }

        /**
         * {@inheritDoc}
         */
        public StructuredDocument newInstance(MimeMediaType mimeType, Reader source) throws IOException {
            return new LiteXMLDocument(mimeType, source);
        }
    }

    /**
     * The instantiator for instances of our documents.
     */
    public static final StructuredDocumentFactory.TextInstantiator INSTANTIATOR = new Instantiator();

    /**
     * The actual document contents.
     */
    final StringBuffer docContent;

    /**
     * The mimetype of this document.
     */
    private final MimeMediaType mimeType;

    /**
     * Creates new LiteXMLDocument
     */
    LiteXMLDocument(MimeMediaType mimeType, String type) {
        this(mimeType, type, "");
    }

    /**
     * Creates new LiteXMLDocument with a textValue in the root element
     */
    LiteXMLDocument(MimeMediaType mimeType, String type, String textValue) {
        super(null, null);

        parent = this;

        this.mimeType = mimeType;

        docContent = new StringBuffer();

        if (null == textValue) {
            textValue = "";
        }

        StringBuffer seedDoc = new StringBuffer(textValue.length() + 3 * type.length() + 128);

        seedDoc.append("<?xml version=\"1.0\"");

        String charset = mimeType.getParameter("charset");

        if (charset != null) {
            seedDoc.append(" encoding=\"");
            seedDoc.append(charset);
            seedDoc.append("\"");
        }
        seedDoc.append("?>\n");

        seedDoc.append("<!DOCTYPE ");
        seedDoc.append(type);
        seedDoc.append(">\n");

        seedDoc.append('<');
        seedDoc.append(type);
        seedDoc.append('>');

        seedDoc.append(textValue);

        seedDoc.append("</");
        seedDoc.append(type);
        seedDoc.append('>');

        try {
            init(new StringReader(seedDoc.toString()));
        } catch (IOException caught) {
            throw new RuntimeException(caught.getMessage());
        }
    }

    /**
     * Creates new LiteXMLDocument
     */
    LiteXMLDocument(MimeMediaType mimeType, InputStream in) throws IOException {
        super(null, null);

        parent = this;

        this.mimeType = mimeType;

        docContent = new StringBuffer();

        String charset = mimeType.getParameter("charset");

        if (charset == null) {
            init(new InputStreamReader(in));
        } else {
            init(new InputStreamReader(in, charset));
        }
    }

    /**
     * Creates new LiteXMLDocument
     */
    LiteXMLDocument(MimeMediaType mimeType, Reader in) throws IOException {
        super(null, null);

        parent = this;

        this.mimeType = mimeType;

        docContent = new StringBuffer();

        init(in);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {

        try {
            StringWriter stringOut = new StringWriter();

            sendToWriter(stringOut);

            stringOut.close();

            return stringOut.toString();
        } catch (IOException caught) {
            throw new RuntimeException(caught.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public MimeMediaType getMimeType() {
        return mimeType;
    }

    /**
     * {@inheritDoc}
     */
    public String getFileExtension() {
        return TextDocumentCommon.Utils.getExtensionForMime(INSTANTIATOR.getSupportedFileExtensions(), getMimeType());
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.document.Element createElement(Object key) {
        return createElement(key, null);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.document.Element createElement(Object key, Object val) {
        if (!(key instanceof String)) {
            throw new ClassCastException(key.getClass().getName() + " not supported by createElement as key.");
        }

        if ((null != val) && !(val instanceof String)) {
            throw new ClassCastException(val.getClass().getName() + " not supported by createElement as value.");
        }

        return createElement((String) key, (String) val);
    }

    /**
     * {@inheritDoc}
     */
    public TextElement createElement(String name) {
        return createElement(name, null);
    }

    /**
     * {@inheritDoc}
     */
    public TextElement createElement(String name, String val) {
        return new LiteXMLElement(this, name, val);
    }

    /**
     * Create a new text element as a sub-range of this document.
     *
     * @param loc The document range for the new element.
     * @return The newly created element.
     */
    protected TextElement createElement(tagRange loc) {
        return new LiteXMLElement(this, loc);
    }

    /**
     * {@inheritDoc}
     */
    public Reader getReader() {
        return new StringReader(toString());
    }

    /**
     * {@inheritDoc}
     */
    public StructuredDocument getRoot() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getStream() throws IOException {
        String charset = mimeType.getParameter("charset");

        if (charset == null) {
            return new ByteArrayInputStream(toString().getBytes());
        } else {
            return new ByteArrayInputStream(toString().getBytes(charset));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendToWriter(Writer writer) throws IOException {
        String charset = mimeType.getParameter("charset");

        if (charset == null) {
            writer.write("<?xml version=\"1.0\"?>\n");
        } else {
            writer.write("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n");
        }

        charRange result = getDocType(docContent, true);

        if (result.isValid()) {
            writer.write(docContent.toString().substring(result.start, result.end + 1));
            writer.write('\n');
        }

        printNice(writer, -1, true);
    }

    /**
     * {@inheritDoc}
     */
    public void sendToStream(OutputStream stream) throws IOException {
        String charset = mimeType.getParameter("charset");

        Writer osw;

        if (charset == null) {
            osw = new OutputStreamWriter(stream);
        } else {
            osw = new OutputStreamWriter(stream, charset);
        }

        Writer out = new BufferedWriter(osw);

        sendToWriter(out);
        out.flush();
    }

    /**
     * Initialises LiteXMLDocument.
     */
    protected void init(Reader in) throws IOException {
        loc = new tagRange();

        char[] smallBuffer = new char[5000];

        do {
            int readCount = in.read(smallBuffer);

            if (readCount < 0) {
                break;
            }

            if (readCount > 0) {
                docContent.append(smallBuffer, 0, readCount);
            }

        } while (true);

        // startTag will contain the xml declaration
        loc.startTag.start = 0;
        loc.startTag.end = docContent.toString().indexOf(">");

        // body is everything after the xml declaration
        loc.body.start = loc.startTag.end + 1;
        loc.body.end = docContent.length() - 1;

        // end is the end of the doc.
        loc.endTag.start = loc.body.end;
        loc.endTag.end = loc.body.end;

        charRange docType = getDocType(getDocument().docContent, false);

        if (docType.isValid()) {
            loc = getTagRanges(getDocument().docContent, docContent.toString().substring(docType.start, docType.end + 1), loc.body);
        } else {
            loc = getTagRanges(getDocument().docContent, null, loc.body);
        }

        if (!loc.isValid()) {
            throw new RuntimeException("Parsing error in source document.");
        }

        if (!loc.startTag.equals(loc.endTag)) {
            addChildTags(loc.body, this); // now add the subtags
        }

        if (paranoidConsistencyChecking) {
            checkConsistency();
        }
    }

    protected charRange getDocType(final StringBuffer source, boolean wholeElement) {
        final String xmldoctype = "!DOCTYPE";
        charRange result = new charRange();
        int start = 0;
        int end = getDocument().docContent.length() - 1;
        tagRange ranges = getTagRanges(source, xmldoctype, new charRange(start, end));

        if (-1 == start) {
            return result;
        }

        if (!ranges.startTag.isValid()) {
            return result;
        }

        if (wholeElement) {
            result = ranges.startTag;
        } else {
            result.start = ranges.startTag.start + 1 + xmldoctype.length() - 1 + 1;

            while ((result.start < end) && // immediately followed by a delimiter or the end of the tag
                    CharUtils.isWhitespace(source.charAt(result.start))) {
                result.start++;
            }

            result.end = result.start;

            while ((result.end + 1) < end) { // immediately followed by a delimiter or the end of the tag
                char possibleEnd = source.charAt(result.end + 1);

                if (CharUtils.isWhitespace(possibleEnd) || ('/' == possibleEnd) || ('>' == possibleEnd)) {
                    break;
                }
                result.end++;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    LiteXMLDocument getDocument() {
        return this;
    }
}
