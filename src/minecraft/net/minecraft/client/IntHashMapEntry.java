package net.minecraft.client;

/**
 * A single entry in an {@link IntHashMap} bucket chain.
 *
 * @param <V> the value type
 */
class IntHashMapEntry<V> {
    /** The lookup key for this entry. */
    final int hashEntry;
    /** The value associated with the key. */
    Object valueEntry;
    /** Next entry in the same hash bucket (linked list chain). */
    IntHashMapEntry<V> nextEntry;
    /** The raw (pre-transform) hash used to compute the bucket index during rehashing. */
    final int slotHash;

    IntHashMapEntry(int slotHash, int hashEntry, V valueEntry, IntHashMapEntry<V> nextEntry) {
        this.valueEntry = valueEntry;
        this.nextEntry = nextEntry;
        this.hashEntry = hashEntry;
        this.slotHash = slotHash;
    }

    /** @return the lookup key for this entry. */
    public final int getHash() {
        return this.hashEntry;
    }

    /** @return the value stored in this entry. */
    public final Object getValue() {
        return this.valueEntry;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof IntHashMapEntry)) {
            return false;
        }
        IntHashMapEntry<?> other = (IntHashMapEntry<?>) obj;
        Integer a = this.getHash();
        Integer b = other.getHash();
        if (a == b || a != null && a.equals(b)) {
            Object va = this.getValue();
            Object vb = other.getValue();
            return va == vb || va != null && va.equals(vb);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return IntHashMap.getHash(this.hashEntry);
    }

    @Override
    public final String toString() {
        return this.getHash() + "=" + this.getValue();
    }
}
