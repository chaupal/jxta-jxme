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
package net.jxta.util.java.io;

import java.io.IOException;
import java.io.Writer;

public abstract class FilterWriter extends Writer {

    /**
     * The underlying character-output stream.
     */
    protected Writer out;

    /**
     * Create a new filtered writer.
     *
     * @param out a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    /**
     * Write a single character.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public void write(int c) throws IOException {
        out.write(c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param cbuf Buffer of characters to be written
     * @param off  Offset from which to start reading characters
     * @param len  Number of characters to be written
     * @throws IOException If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    /**
     * Write a portion of a string.
     *
     * @param str String to be written
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     * @throws IOException If an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    /**
     * Flush the stream.
     *
     * @throws IOException If an I/O error occurs
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Close the stream.
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        out.close();
    }

}
