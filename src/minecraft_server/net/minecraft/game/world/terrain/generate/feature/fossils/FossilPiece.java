package net.minecraft.game.world.terrain.generate.feature.fossils;

public class FossilPiece {
	/*
	 * Very simple representation of an array-contained mini-structure
	 */
	
	/* Schematic is x, z, y*/
	private final short [][][] schematic;
	
	int width, length, height;
	
	public FossilPiece(short [][][] schematic) {
		this.schematic = schematic;
		this.width = this.schematic.length;
		this.length = this.schematic[0].length;
		this.height = this.schematic[0][0].length;
	}

	private int rotateX(int x, int z, boolean rotated) {
		return rotated ? z : x;
	}

	private int rotateZ(int x, int z, boolean rotated) {
		return rotated ? x : z;
	}

	/*
	 * This method draws the piece in the global schematic, centered around (x0, y0, z0),
	 * and possibly rotated.
	 */
	public void draw(short [][][] map, int x0, int y0, int z0, boolean rotated) {
		int cx0 = this.width >> 1;
		int cz0 = this.length >> 1;
		
		int cx = this.rotateX(cx0, cz0, rotated);
		int cz = this.rotateZ(cx0, cz0, rotated);
		
		for(int x = 0; x < this.width; x ++) {
			for(int z = 0; z < this.length; z ++) {
				for(int y = 0; y < this.height; y ++) {
					int xx = this.rotateX(x, z, rotated);
					int zz = this.rotateZ(x, z, rotated);
					short s = this.schematic[x][z][y];
					if(s >= 0) {
						int blockID = s & 0xff;
						if(rotated && blockID == 167) {
							int meta = s >> 8;
							if (meta != 0) {
								meta = meta == 4 ? 12 : 4;
								s = (short)(blockID | meta << 8);
							}
						}
						map[x0 + xx - cx][z0 + zz - cz][y0 + y] = s;
					}
				}
			}
		}
	}
}
