package net.jxta.id.UUID;

import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;

public class ModuleSpecID extends net.jxta.platform.ModuleSpecID {

    /**
     * Log4J categorgy
     */
    private static final transient Logger LOG = Logger.getInstance(ModuleSpecID.class.getName());

    private final static int moduleClassIdOffset = 0;
    private final static int moduleSpecIdOffset = net.jxta.id.UUID.IDFormat.uuidSize;
    private final static int padOffset = ModuleSpecID.moduleSpecIdOffset + IDFormat.uuidSize;
    private final static int padSize = IDFormat.flagsOffset - ModuleSpecID.padOffset;

    /**
     * The id data
     */
    protected IDBytes id;

    /**
     * Constructor. Used only internally.
     */
    protected ModuleSpecID() {
        super();
        id = new IDBytes();
        id.bytes[IDFormat.flagsOffset + IDFormat.flagsIdTypeOffset] = IDFormat.flagModuleSpecID;
    }

    /**
     * Intializes contents from provided ID.
     *
     * @param id the ID data
     */
    protected ModuleSpecID(IDBytes id) {
        super();
        this.id = id;
    }

    /**
     * Creates a ModuleSpecID in a given class, with a given class unique id.
     * A UUID of a class and another UUID are provided.
     *
     * @param classUUID the class to which this will belong.
     * @param specUUID  the unique id of this spec in that class.
     * @since JXTA  1.0
     */
    protected ModuleSpecID(UUID classUUID, UUID specUUID) {

        this();
        id.longIntoBytes(ModuleSpecID.moduleClassIdOffset,
                classUUID.getMostSignificantBits());
        id.longIntoBytes(ModuleSpecID.moduleClassIdOffset + 8,
                classUUID.getLeastSignificantBits());

        id.longIntoBytes(ModuleSpecID.moduleSpecIdOffset,
                specUUID.getMostSignificantBits());
        id.longIntoBytes(ModuleSpecID.moduleSpecIdOffset + 8,
                specUUID.getLeastSignificantBits());
    }

    /**
     * See {@link net.jxta.id.IDFactory.Instantiator#newModuleSpecID(net.jxta.platform.ModuleClassID)}.
     */
    public ModuleSpecID(ModuleClassID classID) {
        this(classID.getClassUUID(), UUIDFactory.newUUID());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }

        if (target instanceof ModuleSpecID) {
            ModuleSpecID msidTarget = (ModuleSpecID) target;

            if (!getIDFormat().equals(msidTarget.getIDFormat()))
                return false;

            if (id == msidTarget.id)
                return true;

            boolean result = id.equals(msidTarget.id);

            // if true then we can have the two ids share the id bytes
            if (result)
                msidTarget.id = id;

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
    public net.jxta.platform.ModuleClassID getBaseClass() {
        return new ModuleClassID(getClassUUID(), new UUID(0L, 0L));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOfSameBaseClass(net.jxta.platform.ModuleClassID classId) {
        return getClassUUID().equals(((ModuleClassID) classId).getClassUUID());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOfSameBaseClass(net.jxta.platform.ModuleSpecID specId) {
        return getClassUUID().equals(((ModuleSpecID) specId).getClassUUID());
    }

    /**
     * get the class' unique id
     *
     * @return UUID module class' unique id
     * @since JXTA 1.0
     */
    protected UUID getClassUUID() {
        UUID result =
                new UUID(id.bytesIntoLong(ModuleSpecID.moduleClassIdOffset),
                        id.bytesIntoLong(ModuleSpecID.moduleClassIdOffset + 8));

        return result;
    }

    /**
     * get the spec unique id
     *
     * @return UUID module spec unique id
     * @since JXTA 1.0
     */
    protected UUID getSpecUUID() {
        UUID result =
                new UUID(id.bytesIntoLong(ModuleSpecID.moduleSpecIdOffset),
                        id.bytesIntoLong(ModuleSpecID.moduleSpecIdOffset + 8));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public URI toURI() {
        return IDFormat.toURI((String) getUniqueValue());
    }
}
