package net.minecraft.game.world.terrain.generate.city;

import net.minecraft.game.world.block.Block;

/*
 * Low-level chunk block-array helpers used by MapGenCity.
 *
 * Performance note (JIT inlining policy):
 * ----------------------------------------
 * World generation is performance-sensitive in this mod, so it may be
 * tempting to inline the bit-shift index formula and the array writes
 * directly at every call site. We do NOT do that, and here is why:
 *
 *   - The methods in this class are intentionally tiny (well under
 *     HotSpot's ~35-byte bytecode inlining threshold). After roughly
 *     1,500-2,000 invocations, the C2 JIT compiler inlines them
 *     into the caller, producing machine code identical to a hand-
 *     inlined bit-shift.
 *   - The real cost of block placement is the byte-array store
 *     (cache miss / store-buffer cost), not the integer arithmetic
 *     used to compute the index. A 1-cycle shift is invisible next
 *     to a 5-200 cycle array write depending on cache state.
 *   - Inlining these methods at the source level would cost
 *     ~2,000 duplicated expressions across the city generator and
 *     its buildings, with zero runtime benefit once the JIT warms up.
 *
 * If you ever profile and see this code as a hotspot, the right fix
 * is to investigate the array access pattern (cache locality of
 * block[]/data[]/meta[]), not to un-inline these helpers.
 */
public final class CityBlockData {

	private CityBlockData() {
	}

	public static int idx(int x, int y, int z) {
		return x << 11 | z << 7 | y;
	}

	public static void setBlock(byte[] data, int x, int y, int z, byte blockID) {
		data[idx(x, y, z)] = blockID;
	}

	public static void setBlock(byte[] data, byte[] meta, int x, int y, int z, byte blockID, byte blockMeta) {
		int i = idx(x, y, z);
		data[i] = blockID;
		meta[i] = blockMeta;
	}

	public static void setEncoded(byte[] data, byte[] meta, int x, int y, int z, int encoded) {
		int i = idx(x, y, z);
		data[i] = Block.decodeID(encoded);
		meta[i] = Block.decodeMeta(encoded);
	}

	public static byte getBlock(byte[] data, int x, int y, int z) {
		return data[idx(x, y, z)];
	}
}
