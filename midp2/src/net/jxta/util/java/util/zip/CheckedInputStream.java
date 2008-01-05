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
package net.jxta.util.java.util.zip;

import net.jxta.util.java.io.FilterInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that also maintains a checksum of the data being read.
 * The checksum can then be used to verify the integrity of the input data.
 *
 * @author David Connelly
 * @version 1.19, 12/19/03
 * @see        Checksum
 */
public class CheckedInputStream extends FilterInputStream {
    private Checksum cksum;

    /**
     * Creates an input stream using the specified Checksum.
     *
     * @param in    the input stream
     * @param cksum the Checksum
     */
    public CheckedInputStream(InputStream in, Checksum cksum) {
        super(in);
        this.cksum = cksum;
    }

    /**
     * Reads a byte. Will block if no input is available.
     *
     * @return the byte read, or -1 if the end of the stream is reached.
     * @throws java.io.IOException if an I/O error has occurred
     */
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            cksum.update(b);
        }
        return b;
    }

    /**
     * Reads into an array of bytes. Will block until some input
     * is available.
     *
     * @param buf the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end
     *         of the stream is reached.
     * @throws IOException if an I/O error has occurred
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        len = in.read(buf, off, len);
        if (len != -1) {
            cksum.update(buf, off, len);
        }
        return len;
    }

    /**
     * Skips specified number of bytes of input.
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error has occurred
     */
    public long skip(long n) throws IOException {
        byte[] buf = new byte[512];
        long total = 0;
        while (total < n) {
            long len = n - total;
            len = read(buf, 0, len < buf.length ? (int) len : buf.length);
            if (len == -1) {
                return total;
            }
            total += len;
        }
        return total;
    }

    /**
     * Returns the Checksum for this input stream.
     *
     * @return the Checksum value
     */
    public Checksum getChecksum() {
        return cksum;
    }
}
