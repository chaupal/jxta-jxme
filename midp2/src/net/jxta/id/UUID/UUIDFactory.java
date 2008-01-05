package net.jxta.id.UUID;


import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public final class UUIDFactory {

//    /**
//     *  Log4J Logger
//     */
//    private static final transient Logger LOG = Logger.getLogger(UUIDFactory.class.getName());

    /**
     * The point at which the Gregorian calendar rules are used, measured in
     * milliseconds from the standard epoch.  Default is October 15, 1582
     * (Gregorian) 00:00:00 UTC or -12219292800000L.
     */
    static final long GREGORIAN_MILLIS_OFFSET = 12219292800000L;

    /**
     * offset of this computer relative to utc
     */
    private long utc_offset = 0L;

    /**
     * Time at which we last generated a version 1 UUID in relative
     * milliseconds from 00:00:00.00, 15 October 1582 UTC.
     */
    private long lastTimeSequence = 0L;

    /**
     * Count of how many version 1 UUIDs we have generated at this time
     * sequence value.
     */
    private long inSequenceCounter = 0L;

    /**
     * pseudo random value to prevent clock collisions on the same computer.
     */
    private long clock_seq = 0L;

    /**
     * pseudo random value. If available, this should be seeded with the MAC
     * address of a local network interface.
     */
    private long node = 0L;

    /**
     * Random number generator for UUID generation.
     */
    private Random randNum = null;

    /**
     * We have to catch exceptions from construct of JRandom so we
     * have to init it inline.
     */
    private static UUIDFactory factory = new UUIDFactory();

    /**
     * Generate a new random UUID value. The UUID returned is a version 4 IETF
     * varient random UUID.
     * <p/>
     * <p/>This member must be synchronized because it makes use of shared
     * internal state.
     *
     * @return UUID returns a version 4 IETF varient random UUID.
     */
    public synchronized static net.jxta.id.UUID.UUID newUUID() {

        return newUUID(factory.randNum.nextLong(), factory.randNum.nextLong());
    }

    /**
     * Returns a formatted time sequence field containing the elapsed time in
     * 100 nano units since 00:00:00.00, 15 October 1582. Since the normal
     * clock resolution is coarser than 100 nano than this value, the lower
     * bits are generated in sequence for each call within the same milli.
     *
     * @return time sequence value
     */
    private synchronized long getTimeSequence() {
        long now = (System.currentTimeMillis() - GREGORIAN_MILLIS_OFFSET + utc_offset) * 10000L; // convert to 100 nano units;

        if (now > lastTimeSequence) {
            lastTimeSequence = now;
            // XXX bondolo@jxta.org It might be better to set this to a random
            // value and just watch for rollover. The reason is that there may
            // be more than one instance running on the same computer which is
            // generating UUIDs, but is not excluded by our synchronization.
            // A random value would reduce collisions.
            inSequenceCounter = 0;
        } else {
            inSequenceCounter++;
            if (inSequenceCounter >= 10000L) {
                // we allow the clock to skew forward rather than wait. It's
                // really unlikely that anyone will be continuously generating
                // more than 10k UUIDs per milli for very long.
                lastTimeSequence++;
                inSequenceCounter = 0;
            }
        }

        return (now + inSequenceCounter);
    }

    /**
     * Generate a new UUID value. The UUID returned is a version 1 IETF
     * varient UUID.
     * <p/>
     * <p/>The node value used is currently a random value rather than the
     * normal ethernet MAC address because the MAC address is not directly
     * accessible in to java.
     *
     * @return UUID returns a version 1 IETF varient UUID.
     */
    public static UUID newSeqUUID() {
        long mostSig = 0L, leastSig = 0L;

        long timeSeq = factory.getTimeSequence();

        mostSig |= (timeSeq & 0x0FFFFFFFFL) << 32;
        mostSig |= ((timeSeq >> 32) & 0x0FFFFL) << 16;
        mostSig |= (0x01L) << 12; // version 1;
        mostSig |= ((timeSeq >> 48) & 0x00FFFL);

        leastSig |= (0x02L) << 62; // ietf varient
        leastSig |= ((factory.clock_seq >> 8) & 0x03FL) << 56;
        leastSig |= (factory.clock_seq & 0x0FFL) << 48;
        leastSig |= factory.node & 0x0FFFFFFFFFFFFL;

        return new UUID(mostSig, leastSig);
    }

    /**
     * Generate a new UUID value. The values provided are masked to produce a
     * version 4 IETF varient random UUID.
     *
     * @param bytes the 128 bits of the UUID
     * @return UUID returns a version 4 IETF varient random UUID.
     */
    public static UUID newUUID(byte [] bytes) {
        if (bytes.length != 16)
            throw new IllegalArgumentException("bytes must be 16 bytes in length");

        long mostSig = 0;
        for (int i = 0; i < 8; i++) {
            mostSig = (mostSig << 8) | (bytes[i] & 0xff);
        }

        long leastSig = 0;
        for (int i = 8; i < 16; i++) {
            leastSig = (leastSig << 8) | (bytes[i] & 0xff);
        }

        return newUUID(mostSig, leastSig);
    }


    /**
     * Generate a new UUID value. The values provided are masked to produce a
     * version 3 IETF varient UUID.
     *
     * @param mostSig  High-long of UUID value.
     * @param leastSig Low-long of UUID value.
     * @return UUID returns a version 3 IETF varient random UUID.
     */
    public static UUID newHashUUID(long mostSig, long leastSig) {

        mostSig &= 0xFFFFFFFFFFFF0FFFL;
        mostSig |= 0x0000000000003000L; // version 3
        leastSig &= 0x3FFFFFFFFFFFFFFFL;
        leastSig |= 0x8000000000000000L;  // IETF variant

        return new UUID(mostSig, leastSig);
    }

    /**
     * Generate a new UUID value. The values provided are masked to produce a
     * version 4 IETF varient random UUID.
     *
     * @param mostSig  High-long of UUID value.
     * @param leastSig Low-long of UUID value.
     * @return UUID returns a version 4 IETF varient random UUID.
     */
    public static UUID newUUID(long mostSig, long leastSig) {

        mostSig &= 0xFFFFFFFFFFFF0FFFL;
        mostSig |= 0x0000000000004000L; // version 4
        leastSig &= 0x3FFFFFFFFFFFFFFFL;
        leastSig |= 0x8000000000000000L;  // IETF variant

        leastSig &= 0xFFFF7FFFFFFFFFFFL;
        leastSig |= 0x0000800000000000L;  // multicast bit

        return new UUID(mostSig, leastSig);
    }


    /**
     * Singleton class
     */
    private UUIDFactory() {

        randNum = new Random();

        TimeZone defaultTZ = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(defaultTZ);

        utc_offset = defaultTZ.getRawOffset();

        // Generate a random clock seq
        clock_seq = randNum.nextInt() & 0x03FFL;

        // Generate a random node ID since we can't get the MAC Address
        node = (randNum.nextLong() & 0x0000FFFFFFFFFFFFL);
        node |= 0x0000800000000000L; // mask in the multicast bit since we don't know if its unique.
    }
}
