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
package net.jxta.impl.endpoint.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.Message;
import net.jxta.id.ID;
import net.jxta.impl.endpoint.WireFormatMessage;
import net.jxta.impl.endpoint.WireFormatMessageFactory;
import net.jxta.impl.endpoint.msgframing.MessagePackageHeader;
import net.jxta.impl.endpoint.msgframing.WelcomeMessage;
import net.jxta.util.WatchedInputStream;
import net.jxta.util.WatchedOutputStream;
import net.jxta.util.LimitInputStream;
import net.jxta.util.java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

class TcpConnection implements Runnable {

    /**
     * Log4J Logger
     */
    private static final Logger LOG = Logger.getInstance(TcpConnection.class.getName());

    private static final MimeMediaType appMsg = new MimeMediaType("application/x-jxta-msg").intern();

    private final TcpTransport proto;

    private EndpointAddress dstAddress = null;
    private EndpointAddress fullDstAddress = null;
    private transient InetAddress inetAddress = null;
    private transient int port = 0;

    private transient volatile boolean closed = false;
    private transient Thread recvThread = null;

    private transient WelcomeMessage myWelcome = null;
    private transient WelcomeMessage itsWelcome = null;

    private transient long lastUsed = System.currentTimeMillis();
    private transient SocketConnection socketCon = null;
    private transient WatchedOutputStream woutputStream = null;
    private transient WatchedInputStream winputStream = null;
    private transient OutputStream outputStream = null;
    private transient InputStream inputStream = null;

    private boolean initiator;
    private long connectionBegunTime;
    private boolean closingDueToFailure = false;

    /**
     * only one outgoing message per connection.
     */
    private final transient Object writeLock = new String("tcp write lock");

    /**
     * Creates a new TcpConnection for the specified destination address.
     *
     * @param destaddr the destination address of this connection.
     * @param p        the transport which this connection is part of.
     * @throws java.io.IOException for failures in creating the connection.
     */
    TcpConnection(EndpointAddress destaddr, TcpTransport p) throws IOException {
        initiator = true;

        proto = p;

        this.fullDstAddress = destaddr;
        this.dstAddress = new EndpointAddress(destaddr, null, null);

        String protoAddr = destaddr.getProtocolAddress();
        int portIndex = protoAddr.lastIndexOf(':');
        if (portIndex == -1) {
            throw new IllegalArgumentException("Invalid Protocol Address (port # missing) ");
        }

        String portString = protoAddr.substring(portIndex + 1);
        try {
            port = Integer.valueOf(portString).intValue();
        } catch (NumberFormatException caught) {
            throw new IllegalArgumentException("Invalid Protocol Address (port # invalid): " + portString);
        }

        // Check for bad port number.
        if ((port <= 0) || (port > 65535)) {
            throw new IllegalArgumentException("Invalid port number in Protocol Address : " + port);
        }

        String hostString = protoAddr.substring(0, portIndex);
        inetAddress = new InetAddress(hostString, port);
        //        inetAddress = InetAddress.getByName(hostString);

        if (LOG.isEnabledFor(Priority.INFO)) {
            LOG.info("New TCP Connection to : " + dstAddress + " / " + inetAddress + ":" + port);
        }

        try {

            String strURL = "socket://" + hostString + ((-1 != port) ? ":" + port : "");
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("********** Connector.open: " + strURL);
            }
            socketCon = (SocketConnection) Connector.open(strURL, Connector.READ_WRITE);
            startSocket();
        } catch (IOException e) {
            // If we failed for any reason, make sure the socket is closed.
            // We're the only one to know about it.
            if (socketCon != null) {
                socketCon.close();
            }
            throw e;
        }
    }

    /**
     * Creates a new connection from an incoming socket
     *
     * @param incSocket    the incoming socket.
     * @param TcpTransport the transport we are working for.
     * @throws IOException for failures in creating the connection.
     */
    TcpConnection(SocketConnection incSocket, TcpTransport p) throws IOException {
        proto = p;
        try {
            if (LOG.isEnabledFor(Priority.INFO)) {
                LOG.info("Connection from " + incSocket.getAddress() + ":" + incSocket.getPort());
            }

            initiator = false;

            inetAddress = new InetAddress(incSocket.getAddress());
            port = incSocket.getPort();

            // Temporarily, our address for inclusion in the welcome message
            // response.
            dstAddress = new EndpointAddress(proto.getProtocolName(), incSocket.getLocalAddress() + ":" + port, null, null);
            fullDstAddress = dstAddress;

            socketCon = incSocket;
            startSocket();

            // The correct value for dstAddr: that of the other party.
            dstAddress = itsWelcome.getPublicAddress();
            fullDstAddress = dstAddress;

        }
        catch (IOException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }

        if (null == target) {
            return false;
        }

        if (target instanceof TcpConnection) {
            TcpConnection likeMe = (TcpConnection) target;
            return getDestinationAddress().equals(likeMe.getDestinationAddress()) && getDestinationPeerID().equals(likeMe.getDestinationPeerID());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected void finalize() {
        closingDueToFailure = false;
        close();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getDestinationPeerID().hashCode() + getDestinationAddress().hashCode();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/>Implementation for debugging.
     */
    public String toString() {
        return super.toString() + ":" + ((null != itsWelcome) ? itsWelcome.getPeerID().toString() : "unknown") + " on address "
                + ((null != dstAddress) ? dstAddress.toString() : "unknown");
    }

    public EndpointAddress getDestinationAddress() {
        return (EndpointAddress) dstAddress.clone();
    }

    public EndpointAddress getConnectionAddress() {
        // Somewhat confusing but destinationAddress is the name of that thing
        // for the welcome message.
        return itsWelcome.getDestinationAddress();
    }

    public ID getDestinationPeerID() {
        return itsWelcome.getPeerID();
    }

    private void startSocket() throws IOException {
        // Set socket options
        socketCon.setSocketOption(SocketConnection.KEEPALIVE, 1);
        socketCon.setSocketOption(SocketConnection.SNDBUF, TcpTransport.ChunkSize);
        socketCon.setSocketOption(SocketConnection.RCVBUF, TcpTransport.RecvBufferSize);
        socketCon.setSocketOption(SocketConnection.LINGER, TcpTransport.LingerDelay);
        socketCon.setSocketOption(SocketConnection.DELAY, 0);

        // Get socket streams
        //outputStream = socketCon.openOutputStream();
        //inputStream = socketCon.openInputStream();

        woutputStream = new WatchedOutputStream(socketCon.openOutputStream(), TcpTransport.ChunkSize);
        woutputStream.setWatchList(proto.ShortCycle);

        winputStream = new WatchedInputStream(socketCon.openInputStream(), TcpTransport.ChunkSize);
        winputStream.setWatchList(proto.LongCycle);
        if ((winputStream == null) || (woutputStream == null)) {
           if (LOG.isEnabledFor(Priority.DEBUG)) {
               LOG.debug("   failed getting streams.");
           }
           throw new IOException("Could not get streams");
        }
        outputStream =   woutputStream;
        inputStream = winputStream;
        myWelcome = new WelcomeMessage(fullDstAddress, proto.getPublicAddress(), proto.group.getPeerID(), false);

        myWelcome.sendToStream(outputStream);
        outputStream.flush();

        // The response should arrive shortly or we bail out.
        inputActive(true);

        itsWelcome = new WelcomeMessage(inputStream);
        inputActive(false);

        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("startSocket : Hello from " + itsWelcome.getPublicAddress() + " [" + itsWelcome.getPeerID() + "]");
        }

        recvThread = new Thread(this);
    }

    protected void start() {
        recvThread.start();
    }

    /**
     * Send message to the remote peer.
     *
     * @param msg the message to send.
     */
    public void sendMessage(Message msg) throws IOException {

        // socket is a stream, only one writer at a time...
        synchronized (writeLock) {
            if (closed) {
                if (LOG.isEnabledFor(Priority.INFO)) {
                    LOG.info("Connection was closed to : " + dstAddress);
                }

                throw new IOException("Connection was closed to : " + dstAddress);
            }

            boolean success = false;
            long sendBeginTime = 0;
            long size = 0;


            try {
                // 20020730 bondolo@jxta.org Do something with content-coding here
                // serialize the message.
                WireFormatMessage serialed = WireFormatMessageFactory.toWire(msg, appMsg, (MimeMediaType[]) null);

                // Build the protocol header
                // Allocate a buffer to contain the message and the header

                MessagePackageHeader header = new MessagePackageHeader();

                header.setContentTypeHeader(serialed.getMimeType());

                size = serialed.getByteLength();
                header.setContentLengthHeader(size);

                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("sendMessage (" + serialed.getByteLength() + ") to " + dstAddress + " via " + inetAddress);
                }

                header.sendToStream(outputStream);
                serialed.sendToStream(outputStream);
                outputStream.flush();

                // all done!
                success = true;
                setLastUsed(System.currentTimeMillis());

            } catch (Throwable failure) {

                if (LOG.isEnabledFor(Priority.INFO)) {
                    LOG.info("tcp send - message send failed for " + inetAddress, failure);
                }
                closingDueToFailure = true;
                close();
            }

        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the background Thread. While the connection is active, takes
     * messages from the queue and send it.
     */
    public void run() {
        try {
            if (LOG.isEnabledFor(Priority.INFO)) {
                LOG.info("tcp receive - starts for " + inetAddress + ":" + port);
            }

            try {
                while (isConnected()) {
                    if (closed) {
                        break;
                    }
                    if (LOG.isEnabledFor(Priority.DEBUG)) {
                        LOG.debug("tcp receive - message starts for " + inetAddress + ":" + port);
                    }
                    // We can stay blocked here for a long time, it's ok.
                    MessagePackageHeader header = new MessagePackageHeader(inputStream);


                    MimeMediaType msgMime = header.getContentTypeHeader();

                    long msglength = header.getContentLengthHeader();

                    // FIXME 20020730 bondolo@jxta.org Do something with content-coding here.
                    if (LOG.isEnabledFor(Priority.DEBUG)) {
                        LOG.debug("tcp receive - message body (" + msglength + ") starts for " + inetAddress);
                    }

                    // read the message!
                    // We have received the header, so, the rest had better
                    // come. Turn the short timeout on.
                    inputActive(true);

                    Message msg;

                    try {
                        msg = WireFormatMessageFactory.fromWire(new LimitInputStream(inputStream, msglength, true), msgMime, null);
                    } catch (IOException failed) {
                        if (LOG.isEnabledFor(Priority.INFO)) {
                            LOG.info("tcp receive - failed reading msg from " + inetAddress + ":" + port);
                        }

                        throw failed;
                    } finally {
                        // We can relax again.
                        inputActive(false);
                    }

                    if (LOG.isEnabledFor(Priority.DEBUG)) {
                        LOG.debug("tcp receive - handing incoming message from " + inetAddress + ":" + port + " to EndpointService");
                    }

                    // Demux the message for the upper layers.
                    proto.endpoint.demux(msg);

                    setLastUsed(System.currentTimeMillis());
                }
            } catch (InterruptedIOException woken) {

                // We have to treat this as fatal since we don't know where
                // in the framing the input stream was at. This should have
                // been handled below.

                closingDueToFailure = true;

                if (LOG.isEnabledFor(Priority.WARN)) {
                    LOG.warn(
                            "tcp receive - Error : read() timeout after " + woken.bytesTransferred + " on connection " + inetAddress
                                    + ":" + port);
                }
            }
            catch (EOFException finished) {
                // The other side has closed the connection
                if (LOG.isEnabledFor(Priority.INFO)) {
                    LOG.info("tcp receive - Connection was closed by " + inetAddress);
                }
            } catch (Throwable e) {
                closingDueToFailure = true;

                if (LOG.isEnabledFor(Priority.WARN)) {
                    LOG.warn("tcp receive - Error on connection " + inetAddress, e);
                }
            } finally {
                if (!closed) {
                    // We need to close the connection down.
                    recvThread = null;
                    close();
                }
            }
        } catch (Throwable all) {
            if (LOG.isEnabledFor(Priority.ERROR)) {
                LOG.error("Uncaught Throwable in thread :" + Thread.currentThread().getName(), all);
            }
        }
    }

    private void closeIOs() {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (Exception ez1) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("could not close inputStream ", ez1);
                }
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (Exception ez1) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("Error : could not close outputStream ", ez1);
                }
            }
        }
        if (socketCon != null) {
            try {
                socketCon.close();
                socketCon = null;
            } catch (Exception ez1) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("Error : could not close socket ", ez1);
                }
            }
        }
    }

    /**
     * Soft close of the connection. Messages can no longer be sent, but any
     * in the queue will be flushed.
     */
    public synchronized void close() {
        if (LOG.isEnabledFor(Priority.INFO)) {
            LOG.info(
                (closingDueToFailure ? "Failure" : "Normal") +
                " close of socket to : " + dstAddress + " / " +
                inetAddress + ":" + port);
            if (LOG.isEnabledFor(Priority.DEBUG) && closingDueToFailure) {
                LOG.debug("stack trace", new Throwable("stack trace"));
            }
        }

        if (!closed) {
            setLastUsed(0); // we idle now. Way idle.
            closeIOs();
            closed = true;
            if (recvThread != null) {
                recvThread.interrupt();
            }
        }
    }

    /**
     * return the current connection status.
     *
     * @param true if there is an active connection to the remote peer,
     *             otherwise false.
     */
    public boolean isConnected() {
        return ((recvThread != null) && (!closed));
    }

    /**
     * Return the absolute time in milliseconds at which this Connection was last used.
     *
     * @return absolute time in milliseconds.
     */
    public long getLastUsed() {
        return lastUsed;
    }

    /**
     * Set the last used time for this connection in absolute milliseconds.
     *
     * @param time absolute time in milliseconds.
     */
    private void setLastUsed(long time) {
        lastUsed = time;
    }

    /**
     * This is called with "true" when the invoker is about to read some
     * input and is not willing to wait for it to come.
     * This is called with "false" when the invoker is about to wait for
     * a long time for input to become available with a potentialy very long
     * blocking read.
     */
     private void inputActive(boolean active) {
         if (active) {
             winputStream.setWatchList(proto.ShortCycle);
         } else {
             winputStream.setWatchList(proto.LongCycle);
         }
     }
}
