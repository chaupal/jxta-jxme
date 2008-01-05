package net.jxta.id.UUID;

import net.jxta.peergroup.PeerGroupID;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;

import java.io.InputStream;

/**
 * An implementation of the {@link net.jxta.codat.CodatID} ID Type.
 */
//public class CodatID extends net.jxta.codat.CodatID {
public class CodatID extends net.jxta.codat.CodatID {

    /**
     * Log4J categorgy
     */
    private static final transient Logger LOG = Logger.getInstance(CodatID.class.getName());

    /**
     * size of a SHA1 hash. I would use MessageDigest.getDigestLength, but
     * possible exceptions make it difficult to do.
     */
    protected final static int hashSize = 20;

    /**
     * Location of the group id in the byte array.
     */
    protected final static int groupIdOffset = 0;

    /**
     * Location of the randomly chosen portion of the id within the byte array.
     */
    protected final static int idOffset = CodatID.groupIdOffset + net.jxta.id.UUID.IDFormat.uuidSize;

    /**
     * Location of the hash value portion of the id within the byte array.
     */
    protected final static int codatHashOffset = CodatID.idOffset + IDFormat.uuidSize;

    /**
     * Location of the begining of pad (unused space) within the byte array.
     */
    protected final static int padOffset = CodatID.codatHashOffset + CodatID.hashSize;

    /**
     * Size of the pad.
     */
    protected final static int padSize = IDFormat.flagsOffset - CodatID.padOffset;

    /**
     * The id data
     */
    protected IDBytes id;

    /**
     * Internal constructor
     */
    protected CodatID() {
        super();
        id = new IDBytes();
        id.bytes[IDFormat.flagsOffset + IDFormat.flagsIdTypeOffset] = IDFormat.flagCodatID;
    }

    /**
     * Intializes contents from provided bytes.
     *
     * @param id the ID data
     */
    protected CodatID(IDBytes id) {
        super();
        this.id = id;
    }


    protected CodatID(UUID groupUUID, UUID idUUID) {
        this();

        id.longIntoBytes(CodatID.groupIdOffset,
                groupUUID.getMostSignificantBits());
        id.longIntoBytes(CodatID.groupIdOffset + 8,
                groupUUID.getLeastSignificantBits());

        id.longIntoBytes(CodatID.idOffset,
                idUUID.getMostSignificantBits());
        id.longIntoBytes(CodatID.idOffset + 8,
                idUUID.getLeastSignificantBits());
    }

    /**
     * See {@link net.jxta.id.IDFactory.Instantiator#newCodatID(net.jxta.peergroup.PeerGroupID)}.
     */
    public CodatID(PeerGroupID groupID) {
        this(groupID.getUUID(), UUIDFactory.newUUID());
    }

    /**
     * See {@link net.jxta.id.IDFactory.Instantiator#newCodatID(net.jxta.peergroup.PeerGroupID,byte[])}.
     */
    public CodatID(PeerGroupID groupID, byte [] seed) {
        this();

        UUID groupUUID = groupID.getUUID();

        id.longIntoBytes(
                CodatID.groupIdOffset, groupUUID.getMostSignificantBits());
        id.longIntoBytes(
                CodatID.groupIdOffset + 8, groupUUID.getLeastSignificantBits());

        for (int copySeed = Math.min(IDFormat.uuidSize, seed.length) - 1;
             copySeed >= 0;
             copySeed--)
            id.bytes[copySeed + CodatID.idOffset] = seed[copySeed];

        // make it a valid UUID
        id.bytes[CodatID.idOffset + 6] &= 0x0f;
        id.bytes[CodatID.idOffset + 6] |= 0x40; /* version 4 */
        id.bytes[CodatID.idOffset + 8] &= 0x3f;
        id.bytes[CodatID.idOffset + 8] |= 0x80; /* IETF variant */
        id.bytes[CodatID.idOffset + 10] &= 0x3f;
        id.bytes[CodatID.idOffset + 10] |= 0x80; /* multicast bit */
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }

        if (target instanceof CodatID) {
            CodatID codatTarget = (CodatID) target;

            if (!getIDFormat().equals(codatTarget.getIDFormat()))
                return false;

            if (id == codatTarget.id)
                return true;

            boolean result = id.equals(codatTarget.id);

            // if true then we can have the two ids share the id bytes
            if (result)
                codatTarget.id = id;

            return result;
        } else
            return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String getIDFormat() {
        return IDFormat.INSTANTIATOR.getSupportedIDFormat();
    }

    /**
     * {@inheritDoc}
     */
    public Object getUniqueValue() {
        return getIDFormat() + "-" + (String) id.getUniqueValue();
    }

    /**
     * {@inheritDoc}
     */
    public net.jxta.id.ID getPeerGroupID() {
        UUID groupUUID = new UUID(
                id.bytesIntoLong(CodatID.groupIdOffset),
                id.bytesIntoLong(CodatID.groupIdOffset + 8));

        PeerGroupID groupID = new PeerGroupID(groupUUID);

        // convert to the generic world PGID as necessary
        return IDFormat.translateToWellKnown(groupID);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStatic() {
        for (int eachHashByte = CodatID.codatHashOffset;
             eachHashByte < (CodatID.padOffset); eachHashByte++)
            if (0 != id.bytes[eachHashByte])
                return true;

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public URI toURI() {
        return IDFormat.toURI((String) getUniqueValue());
    }
}
