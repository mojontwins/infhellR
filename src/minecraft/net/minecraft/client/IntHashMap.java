package net.minecraft.client;

import java.util.HashSet;
import java.util.Set;

/**
 * A hash map keyed by primitive {@code int} values, avoiding the autoboxing overhead
 * of {@link java.util.HashMap}&lt;Integer, V&gt;.
 *
 * <p>Uses open-addressing with a singly-linked list chain for collision resolution.
 * Hash computation applies XOR-shifting to improve distribution for Minecraft's
 * entity/block ID keys.</p>
 *
 * <p>This class is used for {@link net.minecraft.network.NetHandler} and other places
 * where primitive int keys are needed without the boxing overhead.</p>
 *
 * @param <V> the value type stored in the map
 */
public class IntHashMap<V> {
    /** The hash table buckets. */
    @SuppressWarnings("unchecked")
    private transient IntHashMapEntry<V>[] slots = new IntHashMapEntry[16];
    /** Number of key-value pairs currently stored. */
    private transient int count;
    /** Threshold at which to resize the table. */
    private int threshold = 12;
    /** Load factor — table is resized when count exceeds {@code capacity * growFactor}. */
    private final float growFactor = 0.75F;
    /** Incremented on every structural modification (insert/remove). */
    private transient volatile int versionStamp;
    /** Set of all keys currently in the map, maintained for external iteration. */
    private Set<Integer> keySet = new HashSet<Integer>();

    /**
     * Computes a hash for the given integer key using a shift-based hash function
     * to improve distribution.
     */
    private static int computeHash(int key) {
        key ^= key >>> 20 ^ key >>> 12;
        return key ^ key >>> 7 ^ key >>> 4;
    }

    /**
     * Maps a hash value to a bucket index using a bitmask.
     * Works correctly when {@code slotCount} is a power of two.
     */
    private static int getSlotIndex(int hash, int slotCount) {
        return hash & slotCount - 1;
    }

    /**
     * Looks up the value associated with the given key.
     *
     * @param key the key to search for
     * @return the value, or {@code null} if not found
     */
    public Object lookup(int key) {
        int hash = computeHash(key);
        for (IntHashMapEntry<V> e = this.slots[getSlotIndex(hash, this.slots.length)]; e != null; e = e.nextEntry) {
            if (e.hashEntry == key) {
                return e.valueEntry;
            }
        }
        return null;
    }

    /**
     * Returns true if the map contains the given key.
     *
     * @param key the key to check
     */
    public boolean containsItem(int key) {
        return this.lookupEntry(key) != null;
    }

    /**
     * Returns the map entry for the given key, or {@code null}.
     *
     * @param key the key to look up
     * @return the entry, or {@code null}
     */
    final IntHashMapEntry<V> lookupEntry(int key) {
        int hash = computeHash(key);
        for (IntHashMapEntry<V> e = this.slots[getSlotIndex(hash, this.slots.length)]; e != null; e = e.nextEntry) {
            if (e.hashEntry == key) {
                return e;
            }
        }
        return null;
    }

    /**
     * Inserts or updates a key-value pair.
     *
     * @param key   the integer key
     * @param value the value to associate
     */
    public void addKey(int key, V value) {
        this.keySet.add(key);
        int hash = computeHash(key);
        int slot = getSlotIndex(hash, this.slots.length);
        for (IntHashMapEntry<V> e = this.slots[slot]; e != null; e = e.nextEntry) {
            if (e.hashEntry == key) {
                e.valueEntry = value;
                return;
            }
        }
        ++this.versionStamp;
        this.insert(hash, key, value, slot);
    }

    private void grow(int newCapacity) {
        IntHashMapEntry<V>[] oldSlots = this.slots;
        int oldLen = oldSlots.length;
        if (oldLen == 1073741824) {
            this.threshold = Integer.MAX_VALUE;
        } else {
            @SuppressWarnings("unchecked")
            IntHashMapEntry<V>[] newSlots = new IntHashMapEntry[newCapacity];
            this.copyTo(newSlots);
            this.slots = newSlots;
            this.threshold = (int)(newCapacity * this.growFactor);
        }
    }

    private void copyTo(IntHashMapEntry<V>[] newSlots) {
        IntHashMapEntry<V>[] oldSlots = this.slots;
        for (int i = 0; i < oldSlots.length; ++i) {
            IntHashMapEntry<V> e = oldSlots[i];
            if (e != null) {
                oldSlots[i] = null;
                IntHashMapEntry<V> next;
                do {
                    next = e.nextEntry;
                    int slot = getSlotIndex(e.slotHash, newSlots.length);
                    e.nextEntry = newSlots[slot];
                    newSlots[slot] = e;
                    e = next;
                } while (next != null);
            }
        }
    }

    /**
     * Removes and returns the value associated with the given key.
     *
     * @param key the key to remove
     * @return the removed value, or {@code null}
     */
    public Object removeObject(int key) {
        this.keySet.remove(key);
        IntHashMapEntry<V> e = this.removeEntry(key);
        return e == null ? null : e.valueEntry;
    }

    final IntHashMapEntry<V> removeEntry(int key) {
        int hash = computeHash(key);
        int slot = getSlotIndex(hash, this.slots.length);
        IntHashMapEntry<V> prev = this.slots[slot];
        IntHashMapEntry<V> e = prev;
        while (e != null) {
            IntHashMapEntry<V> next = e.nextEntry;
            if (e.hashEntry == key) {
                ++this.versionStamp;
                --this.count;
                if (prev == e) {
                    this.slots[slot] = next;
                } else {
                    prev.nextEntry = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }
        return e;
    }

    /** Removes all entries from the map. */
    public void clearMap() {
        ++this.versionStamp;
        for (int i = 0; i < this.slots.length; ++i) {
            this.slots[i] = null;
        }
        this.count = 0;
    }

    private void insert(int hash, int key, V value, int slot) {
        IntHashMapEntry<V> e = this.slots[slot];
        this.slots[slot] = new IntHashMapEntry<V>(hash, key, value, e);
        if (this.count++ >= this.threshold) {
            this.grow(2 * this.slots.length);
        }
    }

    /** @return the set of all keys in the map. */
    public Set<Integer> getKeySet() {
        return this.keySet;
    }

    /** @return the raw hash for the given key (for use by callers that need the hash value). */
    static int getHash(int key) {
        return computeHash(key);
    }

    /** @return the current structural-modification version stamp. */
    public int getVersionStamp() {
        return this.versionStamp;
    }

    /** Sets the structural-modification version stamp. */
    public void setVersionStamp(int versionStamp) {
        this.versionStamp = versionStamp;
    }
}
