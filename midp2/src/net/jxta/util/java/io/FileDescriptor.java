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

/**
 * Instances of the file descriptor class serve as an opaque handle
 * to the underlying machine-specific structure representing an open
 * file, an open socket, or another source or sink of bytes. The
 * main practical use for a file descriptor is to create a
 * <code>FileInputStream</code> or <code>FileOutputStream</code> to
 * contain it.
 * <p/>
 * Applications should not create their own file descriptors.
 *
 * @author Pavani Diwanji
 * @version 1.3, 12/19/03
 * @see     java.io.FileInputStream
 * @see     java.io.FileOutputStream
 * @since JDK1.0
 */
public final class FileDescriptor {

    private int fd;

    private long handle;

    /**
     * Constructs an (invalid) FileDescriptor
     * object.
     */
    public /**/
            FileDescriptor() {
        fd = -1;
        handle = -1;
    }

    private /* */ FileDescriptor(int fd) {
        this.fd = fd;
        handle = -1;
    }

    static {
        initIDs();
    }

    /**
     * A handle to the standard input stream. Usually, this file
     * descriptor is not used directly, but rather via the input stream
     * known as <code>System.in</code>.
     *
     * @see System#in
     */
    public static final FileDescriptor in = standardStream(0);

    /**
     * A handle to the standard output stream. Usually, this file
     * descriptor is not used directly, but rather via the output stream
     * known as <code>System.out</code>.
     *
     * @see System#out
     */
    public static final FileDescriptor out = standardStream(1);

    /**
     * A handle to the standard error stream. Usually, this file
     * descriptor is not used directly, but rather via the output stream
     * known as <code>System.err</code>.
     *
     * @see System#err
     */
    public static final FileDescriptor err = standardStream(2);

    /**
     * Tests if this file descriptor object is valid.
     *
     * @return <code>true</code> if the file descriptor object represents a
     *         valid, open file, socket, or other active I/O connection;
     *         <code>false</code> otherwise.
     */
    public boolean valid() {
        return ((handle != -1) || (fd != -1));
    }

    /**
     * Force all system buffers to synchronize with the underlying
     * device.  This method returns after all modified data and
     * attributes of this FileDescriptor have been written to the
     * relevant device(s).  In particular, if this FileDescriptor
     * refers to a physical storage medium, such as a file in a file
     * system, sync will not return until all in-memory modified copies
     * of buffers associated with this FileDesecriptor have been
     * written to the physical medium.
     * <p/>
     * sync is meant to be used by code that requires physical
     * storage (such as a file) to be in a known state  For
     * example, a class that provided a simple transaction facility
     * might use sync to ensure that all changes to a file caused
     * by a given transaction were recorded on a storage medium.
     * <p/>
     * sync only affects buffers downstream of this FileDescriptor.  If
     * any in-memory buffering is being done by the application (for
     * example, by a BufferedOutputStream object), those buffers must
     * be flushed into the FileDescriptor (for example, by invoking
     * OutputStream.flush) before that data will be affected by sync.
     *
     * @throws SyncFailedException
     *          Thrown when the buffers cannot be flushed,
     *          or because the system cannot guarantee that all the
     *          buffers have been synchronized with physical media.
     * @since JDK1.1
     */
    public native void sync() throws SyncFailedException;

    /* This routine initializes JNI field offsets for the class */
    private static native void initIDs();

    private static native long set(int d);

    private static FileDescriptor standardStream(int fd) {
        FileDescriptor desc = new FileDescriptor();
        desc.handle = set(fd);
        return desc;
    }

}
