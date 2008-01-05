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
import java.io.InputStream;

public class FilterInputStream extends InputStream {
    /**
     * The input stream to be filtered.
     */
    protected volatile InputStream in;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p/>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     * @throws java.io.IOException if an I/O error occurs.
     * @see FilterInputStream#in
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     * Reads up to <code>byte.length</code> bytes of data from this
     * input stream into an array of bytes. This method blocks until some
     * input is available.
     * <p/>
     * This method simply performs the call
     * <code>read(b, 0, b.length)</code> and returns
     * the  result. It is important that it does
     * <i>not</i> do <code>in.read(b)</code> instead;
     * certain subclasses of  <code>FilterInputStream</code>
     * depend on the implementation strategy actually
     * used.
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         the stream has been reached.
     * @throws IOException if an I/O error occurs.
     * @see FilterInputStream#read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. This method blocks until some input is
     * available.
     * <p/>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         the stream has been reached.
     * @throws IOException if an I/O error occurs.
     * @see FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. The actual number of bytes skipped is
     * returned.
     * <p/>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * Returns the number of bytes that can be read from this input
     * stream without blocking.
     * <p/>
     * This method
     * simply performs <code>in.available()</code> and
     * returns the result.
     *
     * @return the number of bytes that can be read from the input stream
     *         without blocking.
     * @throws IOException if an I/O error occurs.
     * @see FilterInputStream#in
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @throws IOException if an I/O error occurs.
     * @see FilterInputStream#in
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Marks the current position in this input stream. A subsequent
     * call to the <code>reset</code> method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     * <p/>
     * The <code>readlimit</code> argument tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     * <p/>
     * This method simply performs <code>in.mark(readlimit)</code>.
     *
     * @param readlimit the maximum limit of bytes that can be read before
     *                  the mark position becomes invalid.
     * @see FilterInputStream#in
     * @see FilterInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     * <p/>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p/>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit bytes, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @throws IOException if the stream has not been marked or if the
     *                     mark has been invalidated.
     * @see FilterInputStream#in
     * @see FilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * Tests if this input stream supports the <code>mark</code>
     * and <code>reset</code> methods.
     * This method
     * simply performs <code>in.markSupported()</code>.
     *
     * @return <code>true</code> if this stream type supports the
     *         <code>mark</code> and <code>reset</code> method;
     *         <code>false</code> otherwise.
     * @see FilterInputStream#in
     * @see java.io.InputStream#mark(int)
     * @see java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return in.markSupported();
    }
}
