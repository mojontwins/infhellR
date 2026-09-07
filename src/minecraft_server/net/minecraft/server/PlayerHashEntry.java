package net.minecraft.server;

/**
 * Entry in the PlayerHash custom hash table.
 * Each entry stores a long key, an Object value, a reference to the next
 * entry in the chain, and a cached hash code.
 */
class PlayerHashEntry {

	/** The long key used for lookup. */
	final long key;

	/** The associated value. */
	Object value;

	/** Next entry in the collision chain. */
	PlayerHashEntry nextEntry;

	/** Cached hash code (pre-computed from the key). */
	final int cachedHashCode;

	/**
	 * Creates a new entry.
	 *
	 * @param cachedHashCode1  pre-computed hash code
	 * @param key2             the long key
	 * @param value4           the value to store
	 * @param nextEntry5       the next entry in the chain
	 */
	PlayerHashEntry(int cachedHashCode1, long key2, Object value4, PlayerHashEntry nextEntry5) {
		this.value = value4;
		this.nextEntry = nextEntry5;
		this.key = key2;
		this.cachedHashCode = cachedHashCode1;
	}

	/** Returns the key. */
	public final long s_func_736_a() {
		return this.key;
	}

	/** Returns the value. */
	public final Object s_func_735_b() {
		return this.value;
	}

	/**
	 * Checks equality with another PlayerHashEntry.
	 * Two entries are equal if their keys and values are equal.
	 */
	public final boolean equals(Object object1) {
		if (!(object1 instanceof PlayerHashEntry)) {
			return false;
		} else {
			PlayerHashEntry other2 = (PlayerHashEntry) object1;
			Long key3 = this.s_func_736_a();
			Long key4 = other2.s_func_736_a();
			if (key3 == key4 || (key3 != null && key3.equals(key4))) {
				Object value5 = this.s_func_735_b();
				Object value6 = other2.s_func_735_b();
				if (value5 == value6 || (value5 != null && value5.equals(value6))) {
					return true;
				}
			}
			return false;
		}
	}

	/** Returns the cached hash code. */
	public final int hashCode() {
		return PlayerHash.getHashCode(this.key);
	}

	/** Returns a string representation: "key=value". */
	public final String toString() {
		return this.s_func_736_a() + "=" + this.s_func_735_b();
	}
}