package net.jxta.id.UUID;

import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;

/**
 * A general purpose JXTA ID Format implementing all of the six standard ID
 * Types. It was originally created for the Java 2 SE reference implementation.
 * The 'uuid' format uses randomly generated UUIDs as the mechanism for
 * generating canonical values for the ids it provides.
 * <p/>
 * <p/>For IDs constructed using "seed" varient constructors, the first 16
 * bytes of the seed are used literally as the UUID value. The value is masked
 * to make it a valid version 4 IETF varient UUID.
 *
 * @see net.jxta.id.ID
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#ids" target="_blank">JXTA Protocols Specification : IDs</a>
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#refimpls-ids-jiuft" target="_blank">JXTA Protocols Specification : UUID ID Format</a>
 */
public class IDFormat {

    /**
     * Log4J Logger
     */
    private static final transient Logger LOG = Logger.getInstance(IDFormat.class.getName());

    /**
     * number of bytes in the byte array
     */
    public final static int IdByteArraySize = 64;

    /**
     * The size of a UUID in bytes
     */
    public final static int uuidSize = 16;

    /**
     * The size of the flags field
     */
    public final static int flagsSize = 1;

    /**
     * Location of the type field within the flags field
     */
    public final static int flagsIdTypeOffset = IDFormat.flagsSize - 1;

    /**
     * Type value for Codat
     */
    public final static byte flagCodatID = 0x01;

    /**
     * Type value for PeerGroup
     */
    public final static byte flagPeerGroupID = 0x02;

    /**
     * Type value for Peer
     */
    public final static byte flagPeerID = 0x03;

    /**
     * Type value for Pipe
     */
    public final static byte flagPipeID = 0x04;

    /**
     * Type value for ModuleClass
     */
    public final static byte flagModuleClassID = 0x05;

    /**
     * Type value for ModuleSpec
     */
    public final static byte flagModuleSpecID = 0x06;

    /**
     * Location of ID flags within byte array.
     */
    public final static int flagsOffset = IDFormat.IdByteArraySize - IDFormat.flagsSize;

    /**
     * Our local version of the world Peer Group ID. We need this for cases
     * where we have to make ids which are in the world peer group. We only
     * use this ID for those cases and never return this ID.
     */
    public static final PeerGroupID worldPeerGroupID = new PeerGroupID(
            new UUID(0x5961626164616261L, 0x4A78746150325033L)); //YabadabaJXTAP2P!

    /**
     * Our local version of the net Peer Group ID. We need this for cases
     * where we have to make ids which are in the net peer group. We only
     * use this ID for those cases and never return this ID.
     */
    public static final PeerGroupID defaultNetPeerGroupID = new net.jxta.peergroup.PeerGroupID(
            new UUID(0x5961626164616261L, 0x4E50472050325033L)); //YabadabaNPG P2P!

    /**
     * This table maps our local private versions of the well known ids to the
     * globally known version.
     */
    final static Object[] [] wellKnownIDs = {
            {net.jxta.peergroup.PeerGroupID.worldPeerGroupID, worldPeerGroupID},
            {net.jxta.peergroup.PeerGroupID.defaultNetPeerGroupID, defaultNetPeerGroupID}
    };

    /**
     * The instantiator for this ID Format which is used by the IDFactory.
     */
    public static final Instantiator INSTANTIATOR = new Instantiator();

    /**
     * This class cannot be instantiated.
     */
    protected IDFormat() {
    }

    /**
     * Translate from well known ID to our locally encoded versions.
     *
     * @param input the id to be translated.
     * @return the translated ID or the input ID if no translation was needed.
     */
    static ID translateFromWellKnown(ID input) {
        for (int eachWellKnown = 0; eachWellKnown < wellKnownIDs.length; eachWellKnown++) {
            ID aWellKnown = (ID) wellKnownIDs[eachWellKnown][0];

            if (aWellKnown.equals(input))
                return (ID) wellKnownIDs[eachWellKnown][1];
        }

        return input;
    }

    /**
     * Translate from locally encoded versions to the well known versions.
     *
     * @param input the id to be translated.
     * @return the translated ID or the input ID if no translation was needed.
     */

    public static ID translateToWellKnown(ID input) {
        for (int eachWellKnown = 0; eachWellKnown < wellKnownIDs.length; eachWellKnown++) {
            ID aLocalEncoding = (ID) wellKnownIDs[eachWellKnown][1];

            if (aLocalEncoding.equals(input))
                return (ID) wellKnownIDs[eachWellKnown][0];
        }

        return input;
    }


    /**
     * Public member which returns a URI of the ID.
     *
     * @param uniqueValue the unique portion of the ID
     * @return the URI
     */
    public static URI toURI(String uniqueValue) {
        return URI.create(ID.URIEncodingName + ":" + ID.URNNamespace + ":" + uniqueValue);
    }
}
