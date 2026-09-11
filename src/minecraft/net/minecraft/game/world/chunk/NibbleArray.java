package net.minecraft.game.world.chunk;

import java.util.Arrays;

/**
 * A packed 4-bit-per-cell array holding one 16x16x16 subchunk's worth of light values.
 *
 * <p>Each {@code NibbleArray} addresses a single subchunk. Coordinates are <b>subchunk-local</b>:
 * {@code x}, {@code yLocal} and {@code z} each range 0-15 and map to a cell index via
 * {@code x << 8 | z << 4 | yLocal} (equivalent to {@code (x*16 + z)*16 + yLocal}). Every two
 * 4-bit cells share one byte in the backing {@link #data} array, so an array sized for the full
 * 4096 cells of a subchunk occupies just 2048 bytes.</p>
 */
public class NibbleArray {
	public final byte[] data;

	/**
	 * Creates a nibble array sized for {@code cellCount} 4-bit cells.
	 *
	 * @param cellCount number of cells; the backing array holds {@code cellCount >> 1} bytes.
	 */
	public NibbleArray(int cellCount) {
		this.data = new byte[cellCount >> 1];
	}

	/**
	 * Wraps an existing backing byte array.
	 *
	 * @param backingBytes pre-allocated nibble storage; must be at least one byte per two cells.
	 */
	public NibbleArray(byte[] backingBytes) {
		this.data = backingBytes;
	}

	/**
	 * Reads the 4-bit value at the given subchunk-local cell.
	 *
	 * @param x      subchunk-local X (0-15)
	 * @param yLocal subchunk-local Y (0-15)
	 * @param z      subchunk-local Z (0-15)
	 * @return the 4-bit cell value (0-15)
	 */
	public int getNibble(int x, int yLocal, int z) {
		int index = x << 8 | z << 4 | yLocal;
		final byte value = this.data[index >>> 1];
		return ((value >>> ((index & 1) << 2)) & 0xF);
	}

	/**
	 * Writes the given 4-bit value into the specified subchunk-local cell.
	 *
	 * @param x      subchunk-local X (0-15)
	 * @param yLocal subchunk-local Y (0-15)
	 * @param z      subchunk-local Z (0-15)
	 * @param value  the 4-bit value to store (0-15)
	 */
	public void setNibble(int x, int yLocal, int z, int value) {
		int index = x << 8 | z << 4 | yLocal;
		final int shift = (index & 1) << 2;
		final int i = index >>> 1;
		this.data[i] = (byte)((this.data[i] & (0xF0 >>> shift)) | (value << shift));
	}

	public boolean isValid() {
		return this.data != null;
	}

	/**
	 * Expands the packed nibbles into one byte per cell (low nibble of each byte holds the value).
	 *
	 * @return a byte array of {@code data.length * 2} entries, one per cell.
	 */
	public byte[] asByteArray() {
		byte[] result = new byte[this.data.length * 2];
		int idx = 0;
		for(int i = 0; i < this.data.length; i ++) {
			result[idx++] = (byte)(this.data[i] & 15);
			result[idx++] = (byte)((this.data[i] >> 4) & 15);
		}

		return result;
	}

	/**
	 * Sets every cell in this array to the given 4-bit value. Used to pre-fill a freshly allocated
	 * sky nibble array with full brightness (0xF) before the light engine re-computes ground truth.
	 *
	 * @param value the 4-bit value (0-15) written to all cells.
	 */
	public void setAll(int value) {
		final byte packed = (byte)((value & 0xF) << 4 | (value & 0xF));
		Arrays.fill(this.data, packed);
	}
}