package net.minecraft.game.world.block;

public class BlockArrayUtils {
	private byte[] data;	
	int chunkX;
	int chunkZ;
	
	int cX1, cX2;
	int cZ1, cZ2;
	
	public BlockArrayUtils(byte[] data, int chunkX, int chunkZ) {
		this.data = data;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		
		this.cX1 = chunkX << 4; this.cX2 = this.cX1 + 15;
		this.cZ1 = chunkZ << 4; this.cZ2 = this.cZ1 + 15;
	}
	
	public void setBlockWithClipping(int x, int y, int z, byte b) {
		if(x >= 0 && x < 16 && y >= 0 && y < 128 && z >= 0 && z < 16) this.data[x << 11 | z << 7 | y] = b;
	}
	
	public void setBlockWithClippingAbsolute(int x, int y, int z, byte b) {
		if(x >= this.cX1 && x <= this.cX2 && y >= 0 && y < 128 && z >= this.cZ1 && z <= this.cZ2)  this.data[(x - this.cX1) << 11 | (z - this.cZ1) << 7 | y] = b;
	}
	
	public boolean withinSphere(int x, int y, int z, int radius) {
		return x * x + y * y + z * z <= radius * radius;
	}
	
	public boolean drawSphere(int x0, int y0, int z0, int radius, int blockID) {
		byte b = (byte)blockID;
		
		// Calculate an octant, replicate for other octants.
		for(int x = 0; x <= radius; x ++) {
			for(int z = 0; z <= radius; z++) {
				for(int y = 0; y <= radius; y ++) {
					if(withinSphere(x, y, z, radius)) {
						this.setBlockWithClipping(x0 + x, y0 + y, z0 + z, b);
						this.setBlockWithClipping(x0 - x, y0 + y, z0 + z, b);
						this.setBlockWithClipping(x0 + x, y0 + y, z0 - z, b);
						this.setBlockWithClipping(x0 - x, y0 + y, z0 - z, b);
						this.setBlockWithClipping(x0 + x, y0 - y, z0 + z, b);
						this.setBlockWithClipping(x0 - x, y0 - y, z0 + z, b);
						this.setBlockWithClipping(x0 + x, y0 - y, z0 - z, b);
						this.setBlockWithClipping(x0 - x, y0 - y, z0 - z, b);
					}
				}
			}
		}
		
		return true;
	}
	
	/*
	 * Draws a sphere around absolute coordinates.
	 * Only draws blocks inside this chunk (i.e. clipped to chunk)
	 */
	public boolean drawSphereAbsolute(int x0, int y0, int z0, int radius, int blockID) {
		byte b = (byte)blockID;
		
		// Calculate an octant, replicate for other octants.
		for(int x = 0; x <= radius; x ++) {
			for(int z = 0; z <= radius; z++) {
				for(int y = 0; y <= radius; y ++) {
					if(withinSphere(x, y, z, radius)) {
						this.setBlockWithClippingAbsolute(x0 + x, y0 + y, z0 + z, b);
						this.setBlockWithClippingAbsolute(x0 - x, y0 + y, z0 + z, b);
						this.setBlockWithClippingAbsolute(x0 + x, y0 + y, z0 - z, b);
						this.setBlockWithClippingAbsolute(x0 - x, y0 + y, z0 - z, b);
						this.setBlockWithClippingAbsolute(x0 + x, y0 - y, z0 + z, b);
						this.setBlockWithClippingAbsolute(x0 - x, y0 - y, z0 + z, b);
						this.setBlockWithClippingAbsolute(x0 + x, y0 - y, z0 - z, b);
						this.setBlockWithClippingAbsolute(x0 - x, y0 - y, z0 - z, b);
					}
				}
			}
		}	
		
		return true;
	}
}
