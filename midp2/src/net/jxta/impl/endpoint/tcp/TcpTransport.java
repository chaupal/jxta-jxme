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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collection;
import com.sun.java.util.collections.Collections;
import com.sun.java.util.collections.Iterator;
import com.sun.java.util.collections.List;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Attributable;
import net.jxta.document.Attribute;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.TextElement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.EndpointService;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.MessageReceiver;
import net.jxta.endpoint.MessageSender;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.MessengerEvent;
import net.jxta.endpoint.MessengerEventListener;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.impl.endpoint.EndpointServiceImpl;
import net.jxta.impl.endpoint.LoopbackMessenger;
import net.jxta.impl.endpoint.WireFormatMessageFactory;
import net.jxta.impl.endpoint.msgframing.MessagePackageHeader;
import net.jxta.impl.protocol.TCPAdv;
import net.jxta.impl.util.TimeUtils;
import net.jxta.impl.util.TimerThreadNamer;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.Module;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.TransportAdvertisement;
import net.jxta.util.LimitInputStream;
import net.jxta.util.WatchedStream;
import net.jxta.util.java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * This class implements the TCP Transport Protocol
 *
 * @see net.jxta.endpoint.MessageTransport
 * @see net.jxta.endpoint.MessageSender
 * @see net.jxta.endpoint.MessageReceiver
 * @see net.jxta.endpoint.EndpointService
 * @see <a href="http://spec.jxta.org/v1.0/docbook/JXTAProtocols.html#trans-tcpipt">JXTA Protocols Specification : Standard JXTA Transport Bindings</a>
 */
public class TcpTransport implements Runnable, Module, MessageSender, MessageReceiver {

    /**
     * Log4j category
     */
    private static final Logger LOG = Logger.getInstance(TcpTransport.class.getName());

    /**
     * The size of the buffer that we use to store message data being sent.
     * Make it the maximum size of a message. (smaller is permitted, though).
     */
    static final int SendBufferSize = 64 * 1024; // 64 KBytes

    /**
     * The number of bytes that we write to/read from a socket in a single write/read call.
     * Keep this reasonably small since it defines the precision at which we monitor
     * progress. With 8K, any write/read will complete in at most 1 second on a 64Kbit/s connection.
     * Watched Stream start worrying after 10 seconds, so this will still work for connections
     * as slow as 6Kbit/s. Note that we monitor progress of read only when we know data is expected.
     */
    static final int ChunkSize = 8 * 1024; // 8 KBytes

    /**
     * The buffer size that we instruct TCP to use for incoming data.
     * One full message.
     */
    static final int RecvBufferSize = 64 * 1024; // 64 KBytes

    // Note: We do not rely on Socket timeout. Java's implementation is crap.
    // These time outs are used to control the behaviour of WatchedInputStream
    // and WatchedOutputStream. These are Filters that monitor their progress.

    /**
     * Amount of time our input stream will wait for any kind of progress
     * on input before declaring that input has stalled. This one is for
     * when we're just waiting for a message to come in. So we'll close
     * the connection if it seems to be unused for quite a long time.
     * WatchedOutputStream breaks writes in small enough chunks that progress
     * can be reliably measured with a resolution of a few seconds.
     */
    static final int LongTimeout = 30 * (int) TimeUtils.AMINUTE;

    /**
     * Amount of time our input stream will wait for any kind of progress
     * on input before declaring that input has stalled. This one is for
     * when we know data should be comming.
     * Amount of time our output stream will wait for any kind of progress
     * during a write() before declaring that the output has stalled.
     */
    static final int ShortTimeout = 10 * (int) TimeUtils.ASECOND;

    /**
     * The amount of time the socket "lingers" after we close it locally.
     * Linger enables the remote socket to finish receiving any pending data
     * at its own rate.
     */
    // xxx: consider referencing a common resource
    //static final int              LingerDelay = 2 * (int) TimeUtils.AMINUTE;
    static final int LingerDelay = 2 * 60;

    /**
     * The amount of time we wait for a connection to be established (effective with jdk 1.4 only).
     * We go get the same system property than URLconnection and default to 20 seconds.
     */
    static int connectionTimeOut = 20 * (int) TimeUtils.ASECOND;

    static final int MaxAcceptCnxBacklog = 50; // Java's default is 50

    // Connections that are watched often - io in progress
    List ShortCycle = Collections.synchronizedList(new ArrayList());

    // Connections that are watched rarely - idle or waiting for input
    List LongCycle = Collections.synchronizedList(new ArrayList());

    private String serverName = null;
    private List publicAddresses = new ArrayList();
    private EndpointAddress publicAddress = null;
    private MessageElement msgSrcAddrElement = null;

    private String interfaceAddressStr;
    protected InetAddress usingInterface;
    private int serverSocketPort;
    private IncomingUnicastServer unicastServer = null;

    private boolean isClosed = false;

    
    private boolean               allowMulticast = true;
    private String                multicastAddress = "224.0.1.85";
    private int                   multicastPortNb = 1234;
    private int                   multicastPacketSize = 16384;
    private EndpointAddress       mAddress = null;
    private InetAddress           propagateInetAddress;
    private int                   propagatePort;
    private int                   propagateSize;
    private Thread                multicastThread = null;
   // private MulticastSocket multicastSocket = null;

    protected PeerGroup group = null;
    protected EndpointService endpoint = null;

    private static final String protocolName = "tcp";

    private boolean publicAddressOnly = false;

    private MessengerEventListener messengerEventListener = null;

    final Timer connectionWatchTimer;

    /**
     * TimerTask used to watch over connections.
     */
    static class Watcher extends TimerTask {
        private Collection watchList;

        public Watcher(Collection watchList) {
            this.watchList = watchList;
        }

        public void run() {
            try {
                WatchedStream[] allStreams = (WatchedStream[]) watchList.toArray(new WatchedStream[watchList.size()]);
                int len = allStreams.length;

                for (int i = 0; i < len; i++) {
                    allStreams[i].watch();
                }
            } catch (Throwable all) {
                if (LOG.isEnabledFor(Priority.FATAL)) {
                    LOG.fatal("Uncaught Throwable in thread :" + Thread.currentThread().getName(), all);
                }
            }
        }
    }

    /**
     * Construct a new TCPTransport instance
     */
    public TcpTransport() {
        try {
            String connectTOStr = System.getProperty("sun.net.client.defaultConnectTimeout");

            if (connectTOStr != null) {
                int i = Integer.parseInt(connectTOStr);
                connectionTimeOut = i;
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("Could not parse system property: sun.net.client.defaultConnectTimeout");
            }

            // Keep the default
        }

        connectionWatchTimer = new Timer();
        connectionWatchTimer.schedule(new TimerThreadNamer("TCP Transport Connection Timer"), 0);

        // Setup the timer for the two connection watch lists.
        connectionWatchTimer.schedule(new Watcher(LongCycle), LongTimeout, LongTimeout);
        connectionWatchTimer.schedule(new Watcher(ShortCycle), ShortTimeout, ShortTimeout);
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

        if (target instanceof TcpTransport) {
            TcpTransport likeMe = (TcpTransport) target;

            if (!getProtocolName().equals(likeMe.getProtocolName())) {
                return false;
            }

            // FIXME 20020630 bondolo@jxta.org Compare the multicasts.

            Iterator myAddrs = publicAddresses.iterator();
            Iterator itsAddrs = likeMe.publicAddresses.iterator();

            while (myAddrs.hasNext()) {
                if (!itsAddrs.hasNext()) {
                    return false;
                } // it has fewer than i do.

                EndpointAddress mine = (EndpointAddress) myAddrs.next();
                EndpointAddress its = (EndpointAddress) itsAddrs.next();

                if (!mine.equals(its)) {
                    return false;
                }       // content didnt match
            }
            return (!itsAddrs.hasNext()); // ran out at the same time?
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getPublicAddress().hashCode();
    }

    /**
     * Initialization of the TcpTransport (called by Platform)
     */
    public void init(PeerGroup g, ID assignedID, Advertisement impl) throws PeerGroupException {

        group = g;
        endpoint = g.getEndpointService();

        try {
            ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) impl;
            ConfigParams configAdv = g.getConfigAdvertisement();
            // Get out invariable parameters from the implAdv
            Element param = implAdv.getParam();

            if (param != null) {
                Enumeration list = param.getChildren("Proto");

                if (list.hasMoreElements()) {
                    TextElement pname = (TextElement) list.nextElement();
                    // TODO The transport should now it's name!!!
                    //protocolName = pname.getTextValue();
                }
            }

            // Get our peer-defined parameters in the configAdv
            param = configAdv.getServiceParam(assignedID);

            Enumeration tcpChilds = param.getChildren(TransportAdvertisement.getAdvertisementType());

            // get the TransportAdv
            if (tcpChilds.hasMoreElements()) {
                param = (Element) tcpChilds.nextElement();
                Attribute typeAttr = ((Attributable) param).getAttribute("type");

                if (!TCPAdv.getAdvertisementType().equals(typeAttr.getValue())) {
                    throw new IllegalArgumentException("transport adv is not a " + TCPAdv.getAdvertisementType());
                }

                if (tcpChilds.hasMoreElements()) {
                    throw new IllegalArgumentException("Multiple transport advs detected for " + assignedID);
                }
            } else {
                throw new IllegalArgumentException(TransportAdvertisement.getAdvertisementType() + " could not be located.");
            }

            Advertisement paramsAdv = null;

            try {
                paramsAdv = AdvertisementFactory.newAdvertisement((TextElement) param);
            } catch (NoSuchElementException notThere) {
            }

            if (!(paramsAdv instanceof TCPAdv)) {
                throw new IllegalArgumentException("Provided Advertisement was not a " + TCPAdv.getAdvertisementType());
            }

            TCPAdv adv = (TCPAdv) paramsAdv;

            // determine the local interface to use. If the user specifies
            // one, use that. Otherwise, use the all the available interfaces.
            interfaceAddressStr = adv.getInterfaceAddress();
            if (interfaceAddressStr != null) {
                usingInterface = new InetAddress(interfaceAddressStr);
            } else {
                usingInterface = InetAddress.ANYADDRESS;
            }

            serverName = adv.getServer();

            // Even when server is not enabled, we use the serverSocketPort
            // as a discriminant for the simulated network partitioning,
            // human readable messages, and a few things of that sort.

            serverSocketPort = adv.getPort();

            // should we expose other than a public address if one was
            // specified ?
            publicAddressOnly = adv.getPublicAddressOnly();

            // Start the servers

            if (adv.isServerEnabled()) {
                unicastServer = new IncomingUnicastServer(this, usingInterface, serverSocketPort, adv.getStartPort(), adv.getEndPort());
                InetAddress boundAddresss = usingInterface;
                // XXX bondolo 20040628 Save the port back as a preference to TCPAdv
                // Build the publicAddresses
                // First in the list is the "public server name". We don't try to
                // resolve this since it might not be resolvable in the context
                // we are running in, we just assume it's good.
                if (serverName != null) {
                    // use speced server name.
                    EndpointAddress newAddr = new EndpointAddress(protocolName, serverName + ":" + serverSocketPort, null, null);
                    publicAddresses.add(newAddr);
                }

                // then add the rest of the local interfaces as appropriate
                // Unless we find an non-loopback interface, we're in local
                // only mode.
                boolean localOnly = true;

                if (usingInterface.equals(InetAddress.ANYADDRESS)) {
                } else {
                    // use speced interface
                    if (!usingInterface.isLoopbackAddress()) {
                        localOnly = false;
                    }

                    String hostAddress = usingInterface.getAddress();
                    EndpointAddress newAddr = new EndpointAddress(protocolName,
                            hostAddress + ":" + serverSocketPort,
                            null, null);

                    // Add public address:
                    // don't add it if its already in the list
                    // don't add it if we have a hand-set public address
                    // and the publicAddressOnly property is set.
                    if (!(serverName != null && publicAddressOnly)) {
                        if (!publicAddresses.contains(newAddr)) {
                            publicAddresses.add(newAddr);
                        }
                    }
                }

                // If the only available interface is LOOPBACK,
                // then make sure we use only that (that includes
                // resetting the outgoing/listening interface
                // from ANYADDRESS to LOOPBACK).

                if (localOnly) {
                    usingInterface = InetAddress.LOOPBACK;
                    publicAddresses.clear();

                    String hostAddress = usingInterface.getAddress();
                    EndpointAddress pubAddr = new EndpointAddress(protocolName,
                            hostAddress + ":" + serverSocketPort,
                            null, null);
                    publicAddresses.add(pubAddr);
                }

                // Set the "prefered" public address. This is the address we
                // will use for identifying outgoing requests.
                publicAddress = (EndpointAddress) publicAddresses.get(0);

            } else {

                // Only the outgoing interface matters.
                // Verify that ANY interface does not in fact mean
                // LOOPBACK only. If that's the case, we want to make
                // that explicit, so that consistency checks regarding
                // the allowed use of that interface work properly.

                if (usingInterface.equals(InetAddress.ANYADDRESS)) {
                    usingInterface = InetAddress.LOOPBACK;
                }

                // The "public" address is just an internal label
                // it is not usefull to anyone outside.
                // IMPORTANT: we set the port to zero, to signify that this
                // address is not realy usable. This means that the
                // TCP restriction port HACK will NOT be consistent in stopping
                // multicasts if you do not enable incoming connections.
                String hostAddress = usingInterface.getAddress();
                publicAddress = new EndpointAddress(protocolName, hostAddress + ":" + serverSocketPort, null, null);
            }

            msgSrcAddrElement = new StringMessageElement(EndpointServiceImpl.MESSAGE_SOURCE_NAME, publicAddress.toString(), (MessageElement) null);

            
            //  Get the multicast configuration.
            allowMulticast = adv.getMulticastState();
             if (allowMulticast) {
                   multicastAddress = adv.getMulticastAddr();
                   multicastPortNb = new Integer(adv.getMulticastPort()).intValue();
                   multicastPacketSize = new Integer(adv.getMulticastSize()).intValue();
                   mAddress = new EndpointAddress(protocolName, multicastAddress + ":" + Integer.toString(multicastPortNb), null, null);
            
            // Create the multicast input socket
                   propagatePort = multicastPortNb;
                   propagateSize = multicastPacketSize;
                   propagateInetAddress = new InetAddress(multicastAddress);
            try {
                            } catch (Exception soe) {
                         LOG.error("Could not join multicast group, setting Multicast off");
                          allowMulticast = false;
                            }
                            if (allowMulticast) {
                                multicastThread = new Thread(this, "TCP Multicast Server Listener");
                                multicastThread.start();
                            }
                        }

            // We're fully ready to function.
            messengerEventListener = endpoint.addMessageTransport(this);

        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.ERROR)) {
                LOG.error("Initialization exception", e);
            }

            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("FIXME: there may be threads that need killing.");
            }
            throw new PeerGroupException("Initialization exception : " + e.getMessage());
        }

        if (messengerEventListener == null) {
            throw new PeerGroupException("Transport registration refused");
        }

        // Cannot start before registration, we could be announcing new messengers while we
        // do not exist yet ! (And get an NPE because we do not have the messenger listener set).

        if (unicastServer != null) {
            if (!unicastServer.start()) {
                throw new PeerGroupException("Unable to start TCP Unicast Server");
            }
        }

        // Tell tell the world about our configuration.
        if (LOG.isEnabledFor(Priority.INFO)) {
            StringBuffer configInfo = new StringBuffer("Configuring TCP Transport : " + assignedID);

            configInfo.append("\n\tGroup Params:");
            configInfo.append("\n\t\tGroup: " + group.getPeerGroupName());
            configInfo.append("\n\t\tGroup ID: " + group.getPeerGroupID());
            configInfo.append("\n\t\tPeer ID: " + group.getPeerID());

            configInfo.append("\n\tFrom Adv:");
            configInfo.append("\n\t\tProtocol: " + protocolName);
            configInfo.append("\n\t\tPublic address: " + (serverName == null ? "(unspecified)" : serverName));
            configInfo.append("\n\t\tInterface address: " + (interfaceAddressStr == null ? "(unspecified)" : interfaceAddressStr));
            configInfo.append("\n\t\tMulticast State: " + (allowMulticast ? "Enabled" : "Disabled"));

            configInfo.append("\n\tConfiguration :");
            if (null != unicastServer) {
                if (-1 == unicastServer.getStartPort()) {
                    configInfo.append("\n\t\tUnicast Server Bind Addr: " + usingInterface.getAddress() + ":" + serverSocketPort);
                } else {
                    configInfo.append(
                            "\n\t\tUnicast Server Bind Addr: " + usingInterface.getAddress() + ":" + serverSocketPort + " [" + unicastServer.getStartPort()
                                    + "-" + unicastServer.getEndPort() + "]");
                }
                configInfo.append("\n\t\tUnicast Server Bound Addr: " + unicastServer.getLocalSocketAddress());
            } else {
                configInfo.append("\n\t\tUnicast Server : disabled");
            }

            configInfo.append("\n\t\tPublic Addresses: ");
            configInfo.append("\n\t\t\tDefault Endpoint Addr : " + publicAddress);

            Iterator eachPublic = publicAddresses.iterator();

            while (eachPublic.hasNext()) {
                EndpointAddress anAddr = (EndpointAddress) eachPublic.next();

                configInfo.append("\n\t\t\tEndpoint Addr : " + anAddr);
            }

            if (LOG.isEnabledFor(Priority.INFO)) {
                LOG.info(configInfo);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized int startApp(String[] arg) {

        isClosed = false;

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void stopApp() {

        if (isClosed) {
            return;
        }

        isClosed = true;

        endpoint.removeMessageTransport(this);

        if (unicastServer != null) {
            unicastServer.stop();
            unicastServer = null;
        }
        connectionWatchTimer.cancel();

        // Close all watched streams. The input threads will kill the
        // connections.

        WatchedStream[] allStreams = (WatchedStream[]) ShortCycle.toArray(new WatchedStream[ShortCycle.size()]);

        for (int i = 0, len = allStreams.length; i < len; i++) {
            try {
                allStreams[i].close();
            } catch (IOException ignored) {
            }
        }

        allStreams = (WatchedStream[]) LongCycle.toArray(new WatchedStream[LongCycle.size()]);
        for (int i = 0, len = allStreams.length; i < len; i++) {
            try {
                allStreams[i].close();
            } catch (IOException ignored) {
            }
        }

        // There should be nothing left, but just for completeness...
        ShortCycle.clear();
        LongCycle.clear();

        // Accelerated GC (or so some say).
        endpoint = null;
        group = null;
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolName() {
        return "tcp";
    }

    // BT for direct channell
    static public String getProto() { return "tcp";}
   

    /**
     * {@inheritDoc}
     */
    public EndpointAddress getPublicAddress() {
        return (EndpointAddress) publicAddress.clone();
    }

    /**
     * {@inheritDoc}
     */
    public EndpointService getEndpointService() {
        return (EndpointService) endpoint.getInterface();
    }

    /**
     * {@inheritDoc}
     */
    public Object transportControl(Object operation, Object Value) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getPublicAddresses() {
        return publicAddresses.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnectionOriented() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowsRouting() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Messenger getMessenger(EndpointAddress dst, Object hintIgnored) {

        if (null == dst) {
            throw new IllegalArgumentException("Null addr");
        }
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Creating a messenger to :"+dst.toString());
        }
        System.out.println("Creating a messenger to :"+dst.toString());
        EndpointAddress plainAddr = new EndpointAddress(dst, null, null);

        if (!plainAddr.getProtocolName().equals(getProtocolName())) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("getMessenger: cannot make messenger for protocol: " + plainAddr.getProtocolName());
            }
            return null;
        }

        // XXX: the following is a work around in order to
        // strip out peers that advertise their loopback IP address (127.0.0.1)
        // as an EndpointAddress
        // lomax@jxta.org
        // jice@jxta.org: make an exception if we're configured with *only* the
        // loopback address, in order to allow interface-less machines
        // to be used for devellopment. During boot the loopback address
        // is allowed to be one of the public addresses, only if it is the
        // only address we have.
        // So, if the destination is one of our addresses
        // including loopback, it is okay to return a loopback messenger.
        // Else, if the address is a loopback address connection will be
        // denied by TcpConnection if it turns out to be a loopback
        // address and local addresses are not the loopback singleton.

        // check for loopback addresses
        if (publicAddresses.contains(plainAddr)) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("getMessenger: return LoopbackMessenger for addr : " + dst);
            }
            return new LoopbackMessenger(endpoint, getPublicAddress(), dst,
                    new EndpointAddress("jxta", group.getPeerID().getUniqueValue().toString(), null, null));
        }

        // Not an *authorized* connection to self, then TcpConnection will
        // check that this is indeed a connection to non-self. It is more
        // efficient to test it there, where the address is converted from
        // its string form already.
        try {
            // Right now we do not want to "announce" outgoing messengers because they get pooled and so must
            // not be grabbed by a listener. If "announcing" is to be done, that should be by the endpoint
            // and probably with a subtely different interface.
            TcpMessenger m = new TcpMessenger(dst, this);

            m.start();
            return m;
        } catch (Throwable caught) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("getMessenger: could not get messenger for " + dst, caught);
                } else {
                    LOG.warn("getMessenger: could not get messenger for " + dst + "/" + caught.getMessage());
                }
            }
            caught.printStackTrace();
            return null;
        }
    }

    /**
     * Handles incoming multicasts.
     */
    public void run() {
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Multicast disabled");
        }

    }

    /**
     * Handle a byte buffer from a multi-cast. This assumes that processing of
     * the buffer is lightweight. Formerly there used to be a delegation to
     * worker threads. The way queuing works has changed though and it should
     * be ok to do the receiver right on the server thread.
     *
     * @param buffer the buffer to process.
     */
    public void processMulticast(byte[] buffer, int size) {
        if (!allowMulticast) {
            return;
        }

        long messageReceiveBeginTime = 0;

        try {
            if (size < 4) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("processMulticast : damaged multicast discarded");
                }
                return;
            }

            if (('J' != buffer[0]) || ('X' != buffer[1]) || ('T' != buffer[2]) || ('A' != buffer[3])) {
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("processMulticast : damaged multicast discarded");
                }
                return;
            }

            InputStream inputStream = new ByteArrayInputStream(buffer, 4, size - 4);
            MessagePackageHeader header = new MessagePackageHeader(inputStream);
            MimeMediaType msgMime = header.getContentTypeHeader();
            long msglength = header.getContentLengthHeader();
            // FIXME 20020730 bondolo@jxta.org Do something with content-coding here.
            // read the message!
            Message msg = WireFormatMessageFactory.fromWire(new LimitInputStream(inputStream,
                    msglength),
                    msgMime,
                    null);
            // Give the message to the EndpointService Manager
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("processMulticast : handing multicast message to EndpointService" + msg);
            }

            // Demux the message for the upper layers.
            endpoint.demux(msg);
        } catch (Throwable e) {

            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("processMulticast : discard incoming multicast message - exception ", e);
            }
            // Just discard the message. Multicast are not reliable
        }
    }

    /**
     * (@inheritdoc}
     */
    public boolean isPropagateEnabled() {
        return allowMulticast;
    }

    /**
     * (@inheritdoc}
     */
    public boolean isPropagationSupported() {
        return false;
    }

    /**
     * Propagates a TransportMessage on this EndpointProtocol
     * <p/>
     * <p/>Synchronizing to not allow concurrent IP multicast: this
     * naturally bounds the usage of ip-multicast boolean be linear and not
     * exponantial.
     *
     * @param message   the TransportMessage to be propagated
     * @param pName     the name of a service
     * @param pParams   parameters for this message.
     * @param prunePeer (ignored)
     * @throws IOException thrown if the message could not be sent for some reason.
     */
    public synchronized void propagate(Message message,
                                       String pName,
                                       String pParams,
                                       String prunePeer)
            throws IOException {
        if (!allowMulticast) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Multicast disabled, returning");
            }
        }
    }

    /**
     * Ping a remote host.
     * <p/>
     * This implementation tries to open a connection, and after tests the
     * result. Note if there is already an open connection, no new connection
     * is actually created.
     *
     * @param addr the endpoint addresss to ping
     * @return true if the address is reachable, otherwise false.
     */
    public boolean ping(EndpointAddress addr) {
        boolean result = false;
        EndpointAddress endpointAddress;
        long pingStartTime = 0;

        try {
            // Too bad that this one will not get pooled. On the other hand ping is
            // not here too stay.
            // Note the connection receive thread is not started so that messenger goes away real quick.
            endpointAddress = new EndpointAddress(addr, null, null);
            TcpMessenger tcpMessenger = new TcpMessenger(endpointAddress, this);
            result = true;
        } catch (Throwable e) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("failure pinging " + addr.toString(), e);
            }

        }
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("ping to " + addr.toString() + " == " + result);
        }

        return result;
    }


    void messengerReadyEvent(Messenger newMessenger, EndpointAddress connAddr) {
        messengerEventListener.messengerReady(new MessengerEvent(this, newMessenger, connAddr));
    }
}
