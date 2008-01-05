package net.jxta.id.UUID;

import com.sun.java.util.collections.Arrays;
import net.jxta.util.java.util.zip.CRC32;
import net.jxta.util.java.util.zip.Checksum;

/**
 * Maintains the internal representation of a 'uuid' JXTA ID.
 *
 * @see net.jxta.id.IDFactory
 * @see net.jxta.impl.id.UUID.IDFormat
 * @see <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#refimpls-ids-jiuft" target="_blank">JXTA Protocols Specification : UUID ID Format</a>
 */
public final class IDBytes {

    /**
     * The bytes.
     */
    public byte [] bytes = null;

    /**
     * if true then we have calculated the hash value for this object.
     */
    protected transient volatile boolean hashIsCached = false;

    /**
     * The cached hash value for this object
     */
    protected transient int cachedHash = 0;

    /**
     * Constructs a new byte representation. This constructor initializes only
     * the flag fields of the ID.
     */
    public IDBytes() {
        this.bytes = new byte[ net.jxta.id.UUID.IDFormat.IdByteArraySize ];
        this.bytes[IDFormat.flagsOffset + IDFormat.flagsIdTypeOffset] = 0;
    }

    /**
     * Compares two IDs for equality.
     *
     * @param target the ID to be compared against.
     * @return boolean true if IDs are equal, false otherwise.
     */
    public boolean equals(Object target) {
        if (this == target)
            return true;

        if (target instanceof IDBytes) {
            return Arrays.equals(bytes, ((IDBytes) target).bytes);
        } else {
            return false;
        }
    }

    /**
     * Public member calculates a hash code for this ID. Used by Hashmaps.
     *
     * @return int Containing the hashcode of this ID.
     */
    public int hashCode() {
        if (!hashIsCached) {
            synchronized (this) {
                // check again since it may have been updated while we were waiting for lock
                if (!hashIsCached) {
                    Checksum crc = new CRC32();

                    crc.update(bytes, 0, bytes.length);

                    cachedHash = (int) crc.getValue();
                    hashIsCached = true;
                }
            }
        }

        return cachedHash;
    }

    /**
     * Returns a string representation of the ID bytes. The bytes are encoded
     * in hex ASCII format with two characters per byte. The pad bytes between
     * the primary id portion and the flags field are ommitted.
     *
     * @return String containting the URI
     */
    public String toString() {
        return getUniqueValue().toString();
    }

    /**
     * Private replacement for toHexString since we need the leading 0 digits.
     * Returns a char array containing byte value encoded as 2 hex chars.
     *
     * @param theByte a byte containing the value to be encoded.
     * @return char[] containing byte value encoded as 2 hex characters.
     */
    private static char[] toHexDigits(byte theByte) {
        final char [] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char result[] = new char[2];

        result[0] = HEXDIGITS[(theByte >>> 4) & 15];
        result[1] = HEXDIGITS[theByte & 15];

        return result;
    }

    /**
     * Return an object containing the unique value of the ID. This object must
     * provide implementations of toString() and hashCode() that are canonical
     * and conisistent from run-to-run given the same input values. Beyond
     * this nothing should be assumed about the nature of this object. For some
     * implementations the object returned may be the same as provided.
     *
     * @return Object which can provide canonical representations of the ID.
     */
    public Object getUniqueValue() {
        StringBuffer encoded = new StringBuffer(144);
        int lastIndex;

        // find the last non-zero index.
        for (lastIndex = IDFormat.flagsOffset - 1; lastIndex > 0; lastIndex--)
            if (0 != bytes[lastIndex])
                break;

        // build the string.
        for (int eachByte = 0; eachByte <= lastIndex; eachByte++) {
            char asHex [] = toHexDigits(bytes[eachByte]);
            encoded.append(asHex);
        }

        // append the last two chars.

        char asHex [] = toHexDigits(bytes[IDFormat.flagsOffset + IDFormat.flagsIdTypeOffset]);
        encoded.append(asHex);

        return encoded.toString();
    }

    /**
     * Insert a long value into the byte array. The long is stored in
     * big-endian order into the byte array begining at the specified index.
     *
     * @param offset location within the byte array to insert.
     * @param value  value to be inserted.
     */
    public void longIntoBytes(int offset, long value) {
        if ((offset < 0) || ((offset + 8) > IDFormat.IdByteArraySize))
            throw new IndexOutOfBoundsException("Bad offset");

        for (int eachByte = 0; eachByte < 8; eachByte ++)
            bytes[eachByte + offset] = (byte) (value >> ((7 - eachByte) * 8L));

        hashIsCached = false;
    }

    /**
     * Return the long value of a portion of the byte array. The long is
     * retrieved in big-endian order from the byte array at the specified
     * offset.
     *
     * @param offset location within the byte array to extract.
     * @return long value extracted from the byte array.
     */
    public long bytesIntoLong(int offset) {
        if ((offset < 0) || ((offset + 8) > IDFormat.IdByteArraySize))
            throw new IndexOutOfBoundsException("Bad offset");

        long result = 0L;

        for (int eachByte = 0; eachByte < 8; eachByte ++)
            result |= ((long) (bytes[eachByte + offset] & 0xff)) << ((7 - eachByte) * 8L);

        return result;
    }
}
