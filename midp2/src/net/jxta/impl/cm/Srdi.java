package net.jxta.impl.cm;

import com.sun.java.util.collections.Iterator;
import com.sun.java.util.collections.SortedSet;
import com.sun.java.util.collections.TreeSet;
import net.jxta.credential.Credential;
import net.jxta.document.StructuredDocument;
import net.jxta.id.ID;
import net.jxta.impl.protocol.ResolverSrdiMsgImpl;
import net.jxta.impl.protocol.SrdiMessageImpl;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverSrdiMsg;
import net.jxta.protocol.SrdiMessage;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.resolver.ResolverService;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * Srdi is a service which provides Srdi functionalities such as :
 * <ul>
 * <li> pushing of Srdi messages to a another peer/propagate</li>
 * <li> replication of an Srdi Message to other peers in a given peerview
 * </li>
 * <li> given an expression Srdi provides a independently calculated starting
 * point</li>
 * <li> Forwarding a ResolverQuery, and taking care of hopCount, random
 * selection</li>
 * <li> registers with the RendezvousService to determine when to share Srdi
 * Entries</li> and whether to push deltas, or full a index</li>
 * <li> provides a SrdiInterface giving to provide a generic srdi message
 * definition</li>
 * </ul>
 * <p/>
 * <p/>
 * If Srdi is started as a thread it performs periodic srdi pushes of indices
 * and also has the ability to respond to rendezvous events. <p/>
 * <p/>
 * ResolverSrdiMessages define a ttl, to indicate to the receiving service
 * whether to replicate such message or not. <p/>
 * <p/>
 * In addition A ResolverQuery defines a hopCount to indicate how many hops a
 * query has been forwarded. This element could be used to detect/stop a query
 * forward loopback hopCount is checked to make ensure a query is not forwarded
 * more than twice.
 *
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#proto-prp"
 *      target="_blank">JXTA Protocols Specification : Peer Resolver Protocol
 *      </a>
 */
public class Srdi implements Runnable, RendezvousListener {

    private final static Logger LOG = Logger.getInstance(Srdi.class.getName());

    // This ought be to configurable/based on a function applied to the rpv size
    /**
     * Description of the Field
     */
    public final static int RPV_REPLICATION_THRESHOLD = 3;
    private long connectPollInterval = 0;
    private Credential credential = null;
    private StructuredDocument credentialDoc = null;

    private PeerGroup group = null;
    private String handlername = null;
    private long pushInterval = 0;

    /**
     * Random number generator used for random result selection
     */
    private static Random random = new Random();
    private volatile boolean republish = true;

    private ResolverService resolver;
    private SrdiInterface srdiService = null;

    private volatile boolean stop = false;

    /**
     * Starts the Srdi Service. wait for connectPollInterval prior to pushing
     * the index if connected to a rdv, otherwise index is as soon as the
     * Rendezvous connect occurs
     *
     * @param group               group context to operate in
     * @param handlername         the SRDI handlername
     * @param srdiService         the service utilizing this Srdi, for purposes
     *                            of callback push entries on events such as rdv connect/disconnect,
     *                            etc.
     * @param srdiIndex           The index instance associated with this
     *                            service
     * @param connectPollInterval initial timeout before the very first push of
     *                            entries in milliseconds
     * @param pushInterval        the Interval at which the deltas are pushed
     *                            in milliseconds
     */
    public Srdi(PeerGroup group,
                String handlername,
                SrdiInterface srdiService,
                long connectPollInterval,
                long pushInterval) {

        this.group = group;
        this.handlername = handlername;
        this.srdiService = srdiService;
        this.connectPollInterval = connectPollInterval;
        this.pushInterval = pushInterval;
        resolver = group.getResolverService();
    }

    /**
     * Forwards a Query to a specific peer hopCount is incremented to indicate
     * this query is forwarded
     *
     * @param peer  peerid to forward query to
     * @param query The query
     */
    public void forwardQuery(Object peer, ResolverQueryMsg query) {

        query.incrementHopCount();
        if (query.getHopCount() > 2) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("hopCount exceeded. Not forwarding query " + query.getHopCount());
            }
            // query has been forwarded too many times
            return;
        }
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Forwarding Query to " + peer);
        }
        resolver.sendQuery(peer.toString(), query);
    }

    /**
     * Forwards a Query to a list of peers hopCount is incremented to indicate
     * this query is forwarded
     *
     * @param peers The peerids to forward query to
     * @param query The query
     */
    public void forwardQuery(Vector peers, ResolverQueryMsg query) {

        query.incrementHopCount();
        if (query.getHopCount() > 2) {
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("hopCount exceeded not forwarding query " + query.getHopCount());
            }
            // query has been forwarded too many times
            return;
        }
        for (int i = 0; i < peers.size(); i++) {
            PeerID peer = (PeerID) peers.elementAt(i);
            String destPeer = peer.toString();
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Forwarding Query to " + destPeer);
            }
            resolver.sendQuery(destPeer, query);
        }
    }

    /**
     * forward srdi message to another peer
     *
     * @param peerid       PeerID to forward query to
     * @param srcPid       The source originator
     * @param primaryKey   primary key
     * @param secondarykey secondary key
     * @param value        value of the entry
     * @param expiration   expiration in ms
     */
    public void forwardSrdiMessage(PeerID peerid,
                                   PeerID srcPid,
                                   String primaryKey,
                                   String secondarykey,
                                   String value,
                                   long expiration) {

        try {
            SrdiMessageImpl srdi = new SrdiMessageImpl(srcPid,
                    // ttl of 0, avoids additional replication
                    0,
                    primaryKey,
                    secondarykey,
                    value,
                    expiration);

            ResolverSrdiMsgImpl resSrdi = new ResolverSrdiMsgImpl(handlername,
                    credential,
                    srdi.toString());
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("[" + group.getPeerGroupName() +
                        " / " + handlername +
                        "] Forwarding a SRDI messsage of type " + primaryKey +
                        " to " + peerid);
            }
            resolver.sendSrdi(peerid.toString(), (ResolverSrdiMsg) resSrdi);
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("Failed forwarding SRDI Message", e);
            }
        }
    }

    /**
     * get the global peerview as the rendezvous service only returns the
     * peerview without the local RDV peer. We need this consistent view for
     * the SRDI index if not each RDV will have a different peerview, off
     * setting the index even when the peerview is stable
     *
     * @return the sorted list
     */
    public Vector getGlobalPeerView() {

        Vector global = new Vector();
        SortedSet set = new TreeSet();

        try {
            // get the local peerview
            Vector rpv = group.getRendezVousService().getLocalWalkView();
            Enumeration eachPVE = rpv.elements();
            while (eachPVE.hasMoreElements()) {
                RdvAdvertisement padv = (RdvAdvertisement) eachPVE.nextElement();
                set.add(padv.getPeerID().toString());
            }
            // add myself
            set.add(group.getPeerID().toString());
            // produce a vector of Peer IDs
            Iterator eachPeerID = set.iterator();

            while (eachPeerID.hasNext()) {
                PeerID id = (PeerID) ID.create(URI.create((String) eachPeerID.next()));
                global.addElement(id);
            }
        } catch (Exception ex) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("Failure generating the global view", ex);
            }
        }
        return global;
    }


    /**
     * Push an SRDI message to a peer ttl is 1, and therefore services
     * receiving this message could choose to replicate this message
     *
     * @param peer peer to push message to, if peer is null it is the message
     *             is propagated
     * @param srdi SRDI message to send
     */
    public void pushSrdi(ID peer, SrdiMessage srdi) {
        try {
            ResolverSrdiMsg resSrdi = new ResolverSrdiMsgImpl(handlername, credential, srdi.toString());
            if (peer == null) {
                resolver.sendSrdi(null, resSrdi);
            } else {
                resolver.sendSrdi(peer.toString(), resSrdi);
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Priority.WARN)) {
                LOG.warn("Failed to send srdi message", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param event Description of the Parameter
     */
    public synchronized void rendezvousEvent(RendezvousEvent event) {

        int theEventType = event.getType();
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Processing " + event);
        }

        switch (theEventType) {

            case RendezvousEvent.RDVCONNECT:
                // This is an initial connection, we need to upload the
                // complete index.
                republish = true;

            case RendezvousEvent.RDVRECONNECT:
                // This is just a renewal of the rdv lease. Nothing special to do.
                notify();
                // wake up the thread now.
                break;
            case RendezvousEvent.CLIENTCONNECT:
            case RendezvousEvent.CLIENTRECONNECT:
            case RendezvousEvent.BECAMERDV:
            case RendezvousEvent.BECAMEEDGE:
                break;
            case RendezvousEvent.RDVFAILED:
            case RendezvousEvent.RDVDISCONNECT:
                republish = true;
                break;
            case RendezvousEvent.CLIENTFAILED:
            case RendezvousEvent.CLIENTDISCONNECT:
                break;
            default:
                if (LOG.isEnabledFor(Priority.WARN)) {
                    LOG.warn("[" + group.getPeerGroupName() + " / " + handlername + "] Unexpected RDV event " + event);
                }
                break;
        }
    }

    /**
     * {@inheritDoc} <p/>
     * <p/>
     * Main processing method for the SRDI Worker thread Send all entries, wait
     * for pushInterval, then send deltas
     */
    public void run() {

        boolean waitingForRdv;
        try {
            while (!stop) {
                waitingForRdv = group.isRendezvous() ||
                        !group.getRendezVousService().isConnectedToRendezVous();
                // upon connection we will have to republish
                republish |= waitingForRdv;
                synchronized (this) {
                    // wait until we stop being a rendezvous or connect to a rendezvous
                    if (waitingForRdv) {
                        try {
                            wait(connectPollInterval);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }

                    if (!republish) {
                        if (LOG.isEnabledFor(Priority.DEBUG)) {
                            LOG.debug("[" + group.getPeerGroupName() +
                                    " / " + handlername +
                                    "] Sleeping for " + pushInterval +
                                    "ms before sending deltas.");
                        }
                        try {
                            wait(pushInterval);
                        } catch (InterruptedException e) {
                            continue;
                        }

                        if (stop) {
                            break;
                        }
                    }
                }
                if (LOG.isEnabledFor(Priority.DEBUG)) {
                    LOG.debug("[" + group.getPeerGroupName() +
                            " / " + handlername + "] Pushing " + (republish ?
                            "all entries" : "deltas"));
                }

                srdiService.pushEntries(republish);
                republish = false;
            }
        } catch (Throwable all) {
            if (LOG.isEnabledFor(Priority.ERROR)) {
                LOG.error("Uncaught Throwable in " +
                        Thread.currentThread().getName() +
                        "[" + group.getPeerGroupName() +
                        " / " + handlername + "]", all);
            }
        }
    }

    /**
     * stop the current running thread
     */
    public synchronized void stop() {

        if (stop) {
            return;
        }
        stop = true;
        group.getRendezVousService().removeListener(this);
        // wakeup and die
        notify();
    }

    /**
     * Interface for pushing entries.
     */
    public interface SrdiInterface {

        /**
         * Pushe SRDI entries.
         *
         * @param all if true then push all entries otherwise just push those
         *            which have changed since the last push.
         */
        void pushEntries(boolean all);
    }
}
