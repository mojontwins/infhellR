package net.minecraft.game.world.chunk;

public class NibbleArray {
	public final byte[] data;

	public NibbleArray(int i1) {
		this.data = new byte[i1 >> 1];
	}

	public NibbleArray(byte[] b1) {
		this.data = b1;
	}

	public int getNibble(int x, int y, int z) {
		int index = x << 11 | z << 7 | y;
		final byte value = this.data[index >>> 1];
		return ((value >>> ((index & 1) << 2)) & 0xF);
	}

	public void setNibble(int x, int y, int z, int value) {
		int index = x << 11 | z << 7 | y;
		final int shift = (index & 1) << 2;
		final int i = index >>> 1;
		this.data[i] = (byte)((this.data[i] & (0xF0 >>> shift)) | (value << shift));
	}

	public boolean isValid() {
		return this.data != null;
	}
	
	public byte[] asByteArray() {
		byte[] result = new byte[this.data.length * 2];
		int idx = 0;
		for(int i = 0; i < this.data.length; i ++) {
			result[idx++] = (byte)(this.data[i] & 15);
			result[idx++] = (byte)((this.data[i] >> 4) & 15);
		}
		
		return result;
	}
}
