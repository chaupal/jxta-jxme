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
package net.jxta.impl.endpoint.router;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import net.jxta.document.*;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.protocol.AccessPointAdvertisement;
import net.jxta.protocol.RouteAdvertisement;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;

/**
 * Message element Router. This element is added to every
 * message to carry route information for the EndpointRouter service
 */

public class EndpointRouterMessage {

    /**
     * Log4j Category
     */
    private static final Logger LOG = Logger.getInstance(EndpointRouterMessage.class.getName());

    public static final String MESSAGE_NS = "jxta";
    public static final String MESSAGE_NAME = "EndpointRouterMsg";
    public static final String Name = "jxta:ERM";

    public static final String SrcTag = "Src";
    public static final String DestTag = "Dest";
    public static final String LastHopTag = "Last";
    public static final String GatewayForwardTag = "Fwd";
    public static final String GatewayReverseTag = "Rvs";

    private String srcAddress = null; // PeerID-based EndpointAddress
    private String destAddress = null; // PeerID-based EndpointAddress
    private String lastHop = null; // Plain PeerID
    private List forwardGateways = null;
    private List forwardCache = null;
    private List reverseGateways = null;
    private List reverseCache = null;
//    private Vector forwardGateways = null;
//    private Vector forwardCache = null;
//    private Vector reverseGateways = null;
//    private Vector reverseCache = null;
    private RouteAdvertisement radv = null;

    // A flag that represents the existence of data.  Which is
    // different from all fields being empty.
    private boolean rmExists = false;

    // A flag that tells us that the message is not uptodate compared to
    // This object.
    private boolean rmDirty = false;

    // Keep tied to one and only one message.
    private final Message message;

    // Cache the element. At the minimum it simplifies removal.
    private MessageElement rmElem = null;

    public boolean msgExists() {
        return rmExists;
    }

    public boolean isDirty() {
        return rmDirty;
    }

    public EndpointRouterMessage(Message message, boolean removeMsg) {

        this.message = message;

        try {
            rmElem = message.getMessageElement(MESSAGE_NS, MESSAGE_NAME);
            if (rmElem == null) {
                return;
            }

            // We have an element, but until we read it, no data to
            // match (rmExists == false). If the data cannot be read
            // from the element, the element is scheduled for removal.
            rmDirty = true;

            // If we have been instructed so, do not parse any existing
            // element, and leave it marked for removal from the message
            // as if it were invalid.
            if (removeMsg) {
                return;
            }

            StructuredTextDocument doc = (StructuredTextDocument) StructuredDocumentFactory.newStructuredDocument(rmElem.getMimeType(),
                    rmElem.getStream());

            Enumeration each = null;
            TextElement e = null;

            each = doc.getChildren();
            if (!each.hasMoreElements()) {
                // results in rmExists being false.
                return;
            }

            while (each.hasMoreElements()) {
                try {
                    e = (TextElement) each.nextElement();

                    if (e.getName().equals(SrcTag)) {
                        srcAddress = e.getTextValue();
                        continue;
                    }

                    if (e.getName().equals(DestTag)) {
                        destAddress = e.getTextValue();
                        continue;
                    }

                    if (e.getName().equals(LastHopTag)) {
                        lastHop = e.getTextValue();
                        continue;
                    }

                    if (e.getName().equals(GatewayForwardTag)) {
                        for (Enumeration eachXpt = e.getChildren(); eachXpt.hasMoreElements();) {

                            if (forwardGateways == null) {
                                forwardGateways = new ArrayList();
                            }
                            if (forwardCache == null) {
                                forwardCache = new ArrayList();
                            }
                            TextElement aXpt = (TextElement) eachXpt.nextElement();
                            AccessPointAdvertisement xptAdv = (AccessPointAdvertisement)
                                    AdvertisementFactory.newAdvertisement(aXpt);

                            forwardGateways.add(xptAdv);
                            forwardCache.add(aXpt); // Save the original element.
                        }
                        continue;
                    }

                    if (e.getName().equals(GatewayReverseTag)) {
                        for (Enumeration eachXpt = e.getChildren(); eachXpt.hasMoreElements();) {

                            if (reverseGateways == null) {
                                reverseGateways = new ArrayList();
                            }
                            if (reverseCache == null) {
                                reverseCache = new ArrayList();
                            }
                            TextElement aXpt = (TextElement) eachXpt.nextElement();
                            AccessPointAdvertisement xptAdv = (AccessPointAdvertisement)
                                    AdvertisementFactory.newAdvertisement(aXpt);

                            reverseGateways.add(xptAdv);
                            reverseCache.add(aXpt); // Save the original element
                        }
                        continue;
                    }

                    if (e.getName().equals(RouteAdvertisement.getAdvertisementType())) {
                        radv = (RouteAdvertisement)
                                AdvertisementFactory.newAdvertisement(e);
                    }
                } catch (Exception ee) {// keep going
                }
            }
            // XXX 20040929 bondolo Should be doing validation here.

            // All parsed ok, we're in sync.
            rmExists = true;
            rmDirty = false;

        } catch (Exception eee) {// give up. The dirty flag will get the element removed
            // from the message (if there was one) and we'll report
            // there was none.
        }
    }

    public void updateMessage() {

        if (!rmDirty) {
            return;
        }

        if (!rmExists) {

            // The change was to remove it.
            // If there was an rmElem, remove it and make sure to remove
            // all of them. We may have found one initialy but there may be
            // several. (just a sanity check for outgoing messages).

            while (rmElem != null) {
                message.removeMessageElement(MESSAGE_NS, rmElem);
                rmElem = message.getMessageElement(MESSAGE_NS, MESSAGE_NAME);
            }

            if (rmElem != null) {
                message.removeMessageElement(MESSAGE_NS, rmElem);
            }
            rmElem = null;
            rmDirty = false;
            return;
        }

        // The element was either created or changed. Replace whatever
        // if anything was in the message

        StructuredTextDocument doc = (StructuredTextDocument)
                StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, Name);

        if (doc instanceof Attributable) {
            ((Attributable) doc).addAttribute("xmlns:jxta", "http://jxta.org");
        }

        Element e = null;

        if (srcAddress != null) {
            e = doc.createElement(SrcTag, srcAddress);
            doc.appendChild(e);
        }

        if (destAddress != null) {
            e = doc.createElement(DestTag, destAddress);
            doc.appendChild(e);
        }
        rmElem = new TextDocumentMessageElement(MESSAGE_NAME, doc, null);
        message.replaceMessageElement(MESSAGE_NS, rmElem);

        rmDirty = false;
    }

    public void setSrcAddress(EndpointAddress a) {
        rmExists = true;
        rmDirty = true;
        srcAddress = a.toString();
    }

    public EndpointAddress getSrcAddress() {
        return new EndpointAddress(srcAddress);
    }

    public void setDestAddress(EndpointAddress a) {
        rmExists = true;
        rmDirty = true;
        destAddress = a.toString();
    }

    public EndpointAddress getDestAddress() {
        return new EndpointAddress(destAddress);
    }

    public void setLastHop(String p) {
        rmExists = true;
        rmDirty = true;
        lastHop = p;
    }

    public String getLastHop() {
        return lastHop;
    }

    public void setForwardHops(List v) {
        rmExists = true;
        rmDirty = true;
        forwardGateways = v;
        forwardCache = null;
    }

    public List getForwardHops() {
        return forwardGateways;
    }

    public void prependReverseHop(AccessPointAdvertisement ap) {
        rmExists = true;
        rmDirty = true;
        if (reverseGateways == null) {
            reverseGateways = new ArrayList();
            reverseCache = new ArrayList();
        }

        reverseGateways.add(0, ap);

        if (reverseCache == null) {
            return;
        }

        // if we still have a cache (we where able to keep it conistent, update it

        StructuredTextDocument apDoc = (StructuredTextDocument) ap.getDocument(MimeMediaType.XMLUTF8);

        reverseCache.add(0, apDoc);
    }

    // Do not call this routine lightly: it blasts the cache.
    public void setReverseHops(List v) {
        rmExists = true;
        rmDirty = true;

        // No inplace changes allowed, we need to keep the cache
        // consistent: clone

        if (v == null) {
            reverseGateways = null;
        } else {

            reverseGateways = v;
        }

        // Not worth updating the cache. Blast it.
        reverseCache = null;
    }

    public List getReverseHops() {

        if (reverseGateways == null) {
            return null;
        }

        // No inplace changes allowed, we need to keep the cache
        // consistent: clone

        return reverseGateways;
    }


    public RouteAdvertisement getRouteAdv() {
        return radv;
    }

    public void setRouteAdv(RouteAdvertisement radv) {
        rmExists = true;
        rmDirty = true;
        this.radv = radv;
    }

    // Used only for debugging
    public String display() {
        StringBuffer msgInfo = new StringBuffer("Process Incoming : ");

        msgInfo.append("\n\tsrc=");
        msgInfo.append((srcAddress != null) ? srcAddress : "none");
        msgInfo.append("\n\tdest== ");
        msgInfo.append((destAddress != null) ? destAddress : "none");
        msgInfo.append("\n\tlastHop= ");
        msgInfo.append((lastHop != null) ? lastHop : "none");
        msgInfo.append("\n\tembedded radv= ");
        msgInfo.append(radv != null ? radv.display() : "none");

        if (forwardGateways != null) {
            msgInfo.append("    Forward Hops:");
            for (int i = 0; i < forwardGateways.size(); ++i) {
                try {
                    msgInfo.append("   [" + i + "] ");
                    msgInfo.append(((AccessPointAdvertisement)
                            forwardGateways.get(i)).getPeerID());
                } catch (Exception ez1) {
                    break;
                }
            }
        }

        if (reverseGateways != null) {
            msgInfo.append("    Reverse Hops:");
            for (int i = 0; i < reverseGateways.size(); ++i) {
                try {
                    msgInfo.append("   [" + i + "] ");
                    msgInfo.append(((AccessPointAdvertisement)
                            forwardGateways.get(i)).getPeerID());
                } catch (Exception ez1) {
                    break;
                }
            }
        }

        return msgInfo.toString();
    }

    // This will ensure that all older elements will be removed from
    // the message in case they do not get replaced by new ones before
    // updateMsg is called.

    public void clearAll() {

        if (rmExists) {
            rmDirty = true;

            srcAddress = null;
            destAddress = null;
            lastHop = null;
            forwardGateways = null;
            reverseGateways = null;
            radv = null;
            rmExists = false;
        }
    }
}
