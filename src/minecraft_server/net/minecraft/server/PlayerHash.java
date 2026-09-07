package net.minecraft.server;

/**
 * A custom hash table that maps long keys to Object values.
 * Used internally for entity-to-player and similar lookups.
 *
 * The implementation is a separate-chaining hash table that resizes
 * (doubles capacity) when the load factor exceeds the configured threshold.
 * Hashing is based on a mix of XOR and right-shift operations for good
 * distribution on long keys.
 */
public class PlayerHash {

	/** The bucket array. Each slot is the head of a collision chain. */
	private transient PlayerHashEntry[] hashArray = new PlayerHashEntry[16];

	/** Number of entries currently in the table. */
	private transient int numHashElements;

	/** Resize threshold. When the entry count exceeds this, the table is resized. */
	private int capacity = 12;

	/** Load factor (resize at 75% of table size). */
	private final float percentUsable = 0.75F;

	/**
	 * Computes the bucket hash for a long key.
	 * Applies XOR-shift mixing to the high and low 32 bits.
	 */
	private static int getHashedKey(long key0) {
		return hash((int) (key0 ^ key0 >>> 32));
	}

	/**
	 * Bit-mixing hash function based on a sequence of XOR-shift operations.
	 * Provides good distribution across the bucket array.
	 */
	private static int hash(int hash0) {
		hash0 ^= hash0 >>> 20 ^ hash0 >>> 12;
		return hash0 ^ hash0 >>> 7 ^ hash0 >>> 4;
	}

	/**
	 * Computes the bucket index for a hash code.
	 * Uses (hash & (length - 1)) which requires the array length to be a power of 2.
	 */
	private static int getHashIndex(int hash0, int arrayLength1) {
		return hash0 & arrayLength1 - 1;
	}

	/**
	 * Looks up a value by its long key.
	 * Returns null if the key is not present.
	 */
	public Object getValueByKey(long key1) {
		int hash3 = getHashedKey(key1);

		// Walk the chain at the computed bucket, comparing keys.
		for (PlayerHashEntry entry4 = this.hashArray[getHashIndex(hash3, this.hashArray.length)]; entry4 != null; entry4 = entry4.nextEntry) {
			if (entry4.key == key1) {
				return entry4.value;
			}
		}

		return null;
	}

	/**
	 * Inserts a key/value pair, replacing any existing value for the same key.
	 * Triggers a resize if the table exceeds its load factor.
	 */
	public void add(long key1, Object value3) {
		int hash4 = getHashedKey(key1);
		int index5 = getHashIndex(hash4, this.hashArray.length);

		// Check if the key already exists, and update its value in place.
		for (PlayerHashEntry entry6 = this.hashArray[index5]; entry6 != null; entry6 = entry6.nextEntry) {
			if (entry6.key == key1) {
				entry6.value = value3;
			}
		}

		this.createKey(hash4, key1, value3, index5);
	}

	/**
	 * Doubles the bucket array size (or sets capacity to MAX_VALUE if already at 2^30).
	 * Entries are re-hashed and placed in the new array.
	 */
	private void resizeTable(int newSize1) {
		PlayerHashEntry[] oldArray2 = this.hashArray;
		int oldLength3 = oldArray2.length;
		if (oldLength3 == 1073741824) {
			// Already at maximum size (2^30); cap capacity to MAX_VALUE.
			this.capacity = Integer.MAX_VALUE;
		} else {
			PlayerHashEntry[] newArray4 = new PlayerHashEntry[newSize1];
			this.copyHashTableTo(newArray4);
			this.hashArray = newArray4;
			this.capacity = (int) ((float) newSize1 * this.percentUsable);
		}
	}

	/**
	 * Re-hashes all entries from the current bucket array into the new array.
	 * Each entry's hash code is reused (cached) to compute the new bucket.
	 */
	private void copyHashTableTo(PlayerHashEntry[] targetArray1) {
		PlayerHashEntry[] sourceArray2 = this.hashArray;
		int targetLength3 = targetArray1.length;

		for (int i4 = 0; i4 < sourceArray2.length; ++i4) {
			PlayerHashEntry entry5 = sourceArray2[i4];
			if (entry5 != null) {
				// Clear the source bucket before walking the chain.
				sourceArray2[i4] = null;

				PlayerHashEntry nextEntry6;
				do {
					nextEntry6 = entry5.nextEntry;
					int newIndex7 = getHashIndex(entry5.cachedHashCode, targetLength3);
					entry5.nextEntry = targetArray1[newIndex7];
					targetArray1[newIndex7] = entry5;
					entry5 = nextEntry6;
				} while (nextEntry6 != null);
			}
		}
	}

	/**
	 * Removes a key and returns the removed value (or null if absent).
	 */
	public Object remove(long key1) {
		PlayerHashEntry removed3 = this.removeKey(key1);
		return removed3 == null ? null : removed3.value;
	}

	/**
	 * Removes an entry by its key and returns it, or null if not found.
	 * Walks the collision chain at the appropriate bucket.
	 */
	final PlayerHashEntry removeKey(long key1) {
		int hash3 = getHashedKey(key1);
		int index4 = getHashIndex(hash3, this.hashArray.length);
		PlayerHashEntry bucketHead5 = this.hashArray[index4];

		PlayerHashEntry currentEntry6 = bucketHead5;
		PlayerHashEntry currentEntry7;
		for (currentEntry6 = bucketHead5; currentEntry6 != null; currentEntry6 = currentEntry7) {
			currentEntry7 = currentEntry6.nextEntry;
			if (currentEntry6.key == key1) {
				--this.numHashElements;
				if (bucketHead5 == currentEntry6) {
					// Removing the head of the chain.
					this.hashArray[index4] = currentEntry7;
				} else {
					// Bypass this entry.
					bucketHead5.nextEntry = currentEntry7;
				}
				return currentEntry6;
			}
			bucketHead5 = currentEntry6;
		}

		return currentEntry6;
	}

	/**
	 * Inserts a new entry at the head of a bucket and triggers a resize
	 * if the load factor threshold is exceeded.
	 */
	private void createKey(int hash1, long key2, Object value4, int index5) {
		PlayerHashEntry existingEntry6 = this.hashArray[index5];
		this.hashArray[index5] = new PlayerHashEntry(hash1, key2, value4, existingEntry6);
		if (this.numHashElements++ >= this.capacity) {
			this.resizeTable(2 * this.hashArray.length);
		}
	}

	/** Package-private helper used by PlayerHashEntry.hashCode(). */
	static int getHashCode(long key0) {
		return getHashedKey(key0);
	}
}