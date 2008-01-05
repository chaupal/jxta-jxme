package net.jxta.id.UUID;

import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.util.java.net.URI;
import net.jxta.util.java.net.URISyntaxException;

/**
 * The instantiator for the UUID ID Format.
 * <p/>
 * <p/>For "seed" varient constructors, the first 16 bytes of the seed are used
 * literally as the UUID value. The value is masked to make it a valid version 4
 * IETF varient UUID.
 */
public class Instantiator implements IDFactory.URIInstantiator {

    /**
     * Our ID Format
     */
    final static String UUIDEncoded = "uuid";

    /**
     * {@inheritDoc}
     */
    public String getSupportedIDFormat() {
        return UUIDEncoded;
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.codat.CodatID newCodatID(net.jxta.peergroup.PeerGroupID groupID) {
        PeerGroupID peerGroupID = (PeerGroupID) net.jxta.id.UUID.IDFormat.translateFromWellKnown(groupID);

        return new CodatID(peerGroupID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.codat.CodatID newCodatID(net.jxta.peergroup.PeerGroupID groupID, byte [] seed) {
        PeerGroupID peerGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(groupID);

        return new CodatID(peerGroupID, seed);
    }


    /**
     * {@inheritDoc}
     */
    public net.jxta.peergroup.PeerGroupID newPeerGroupID() {
        return new PeerGroupID();
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.peergroup.PeerGroupID newPeerGroupID(byte [] seed) {
        return new PeerGroupID(seed);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.peergroup.PeerGroupID newPeerGroupID(net.jxta.peergroup.PeerGroupID parent) {
        PeerGroupID parentGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(parent);

        return new PeerGroupID(parentGroupID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.peergroup.PeerGroupID newPeerGroupID(net.jxta.peergroup.PeerGroupID parent, byte [] seed) {
        PeerGroupID parentGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(parent);

        return new PeerGroupID(parentGroupID, seed);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.peer.PeerID newPeerID(net.jxta.peergroup.PeerGroupID groupID) {
        PeerGroupID peerGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(groupID);

        return new PeerID(peerGroupID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.peer.PeerID newPeerID(net.jxta.peergroup.PeerGroupID groupID, byte [] seed) {
        PeerGroupID peerGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(groupID);

        return new PeerID(peerGroupID, seed);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.pipe.PipeID newPipeID(net.jxta.peergroup.PeerGroupID groupID) {
        PeerGroupID peerGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(groupID);

        return new PipeID(peerGroupID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.pipe.PipeID newPipeID(net.jxta.peergroup.PeerGroupID groupID, byte [] seed) {
        PeerGroupID peerGroupID = (PeerGroupID) IDFormat.translateFromWellKnown(groupID);

        return new PipeID(peerGroupID, seed);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.platform.ModuleClassID newModuleClassID() {
        return new ModuleClassID();
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.platform.ModuleClassID newModuleClassID(net.jxta.platform.ModuleClassID classID) {
        return new ModuleClassID((ModuleClassID) classID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.platform.ModuleSpecID newModuleSpecID(net.jxta.platform.ModuleClassID classID) {
        return new ModuleSpecID((ModuleClassID) classID);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.id.ID fromURI(URI source) throws URISyntaxException {

        // check the protocol
        if (!net.jxta.id.ID.URIEncodingName.equalsIgnoreCase(source.getScheme()))
            throw new URISyntaxException(source.toString(), "URI scheme was not as expected.");

        String decoded = source.getSchemeSpecificPart();

        int colonAt = decoded.indexOf(':');

        // There's a colon right?
        if (-1 == colonAt)
            throw new URISyntaxException(source.toString(), "URN namespace was missing.");

        // check the namespace
        if (!net.jxta.id.ID.URNNamespace.equalsIgnoreCase(decoded.substring(0, colonAt)))
            throw new URISyntaxException(source.toString(), "URN namespace was not as expected. (" +
                    net.jxta.id.ID.URNNamespace + "!=" + decoded.substring(0, colonAt) + ")");
        // skip the namespace portion and the colon
        decoded = decoded.substring(colonAt + 1);

        return fromURNNamespaceSpecificPart(decoded);
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.id.ID fromURNNamespaceSpecificPart(String source) throws URISyntaxException {
        int dashAt = source.indexOf('-');

        // there's a dash, right?
        if (-1 == dashAt)
            throw new URISyntaxException(source, "URN Encodingtype was missing.");

        if (!source.substring(0, dashAt).equals(getSupportedIDFormat()))
            throw new URISyntaxException(source, "JXTA ID Format was not as expected.");

        // skip the dash
        source = source.substring(dashAt + 1);

        // check that the length is even
        if (0 != (source.length() % 2))
            throw new URISyntaxException(source, "URN contains an odd number of chars");

        // check that the length is long enough
        if (source.length() < 2)
            throw new URISyntaxException(source, "URN does not contain enough chars");

        // check that id is short enough
        if (IDFormat.IdByteArraySize < (source.length() % 2))
            throw new URISyntaxException(source, "URN contains too many chars");
        net.jxta.id.ID result = null;
        IDBytes id = new IDBytes();

        try {
            // do the primary portion.
            for (int eachByte = 0; eachByte < ((source.length() / 2) - IDFormat.flagsSize); eachByte++) {
                int index = eachByte * 2;
                String twoChars = source.substring(index, index + 2);
                id.bytes[eachByte] = (byte) Integer.parseInt(twoChars, 16);
            }

            // do the flags
            for (int eachByte = IDFormat.flagsOffset; eachByte < IDFormat.IdByteArraySize; eachByte++) {
                int index = source.length() - (IDFormat.IdByteArraySize - eachByte) * 2;
                String twoChars = source.substring(index, index + 2);
                id.bytes[eachByte] = (byte) Integer.parseInt(twoChars, 16);
            }
        } catch (NumberFormatException caught) {
            throw new URISyntaxException(source, "Invalid Character in JXTA URI");
        }

        switch (id.bytes[IDFormat.flagsOffset + IDFormat.flagsIdTypeOffset]) {
            case IDFormat.flagCodatID :
                result = new CodatID(id);
                break;
            case IDFormat.flagPeerGroupID :
                result = new PeerGroupID(id);
                result = (PeerGroupID) IDFormat.translateToWellKnown(result);
                break;
            case IDFormat.flagPeerID :
                result = new PeerID(id);
                break;
            case IDFormat.flagPipeID :
                result = new PipeID(id);
                break;
            case IDFormat.flagModuleClassID :
                result = new ModuleClassID(id);
                break;
            case IDFormat.flagModuleSpecID :
                result = new ModuleSpecID(id);
                break;
            default :
                throw new URISyntaxException(source, "JXTA ID Type not recognized");
        }

        return result;
    }
}
