package net.minecraft.game.world;

/**
 * A scheduled block tick entry, stored in the world's sorted tick queue.
 *
 * <p>Each entry records a block position and block ID, and the tick is
 * processed at {@link #scheduledTime}. Entries are ordered by scheduled
 * time first, then by insertion order via {@link #tickEntryID} to ensure
 * deterministic tick processing.</p>
 */
public class NextTickListEntry implements Comparable<Object> {
    private static long nextTickEntryID = 0L;

    public int xCoord;
    public int yCoord;
    public int zCoord;
    public int blockID;

    /** The game-tick at which this block tick should fire. */
    public long scheduledTime;

    /** Monotonically increasing insertion order counter; used as a tiebreaker for equal scheduled times. */
    private long tickEntryID = nextTickEntryID++;

    public NextTickListEntry(int x, int y, int z, int blockID) {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        this.blockID = blockID;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NextTickListEntry)) {
            return false;
        }
        NextTickListEntry other = (NextTickListEntry) obj;
        return this.xCoord == other.xCoord
                && this.yCoord == other.yCoord
                && this.zCoord == other.zCoord
                && this.blockID == other.blockID;
    }

    @Override
    public int hashCode() {
        return ((this.xCoord * 128 * 1024 + this.zCoord * 128 + this.yCoord) * 256 + this.blockID);
    }

    /**
     * Sets the tick at which this entry fires.
     *
     * @param tick the game-tick number
     * @return this entry (for chaining)
     */
    public NextTickListEntry setScheduledTime(long tick) {
        this.scheduledTime = tick;
        return this;
    }

    /**
     * Compares two entries by scheduled time, breaking ties with insertion order.
     *
     * @param other the entry to compare against
     * @return -1, 0, or 1
     */
    public int comparer(NextTickListEntry other) {
        if (this.scheduledTime < other.scheduledTime) return -1;
        if (this.scheduledTime > other.scheduledTime) return 1;
        if (this.tickEntryID < other.tickEntryID) return -1;
        if (this.tickEntryID > other.tickEntryID) return 1;
        return 0;
    }

    @Override
    public int compareTo(Object obj) {
        return this.comparer((NextTickListEntry) obj);
    }
}
