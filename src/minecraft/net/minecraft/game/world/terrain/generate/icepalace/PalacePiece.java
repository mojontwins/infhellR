package net.minecraft.game.world.terrain.generate.icepalace;

import net.minecraft.game.world.feature.FeatureAABB;

public abstract class PalacePiece {
	private FeatureIcePalace featureIcePalace;
	
	public abstract short[][][] getPiece();
	public int centerX, baseY, centerZ;
	public boolean rotated;
	public int width, height, length;
	public FeatureAABB aabb;
	
	public PalacePiece(boolean rotated, FeatureIcePalace featureIcePalace) {
		this.rotated = rotated;
		this.featureIcePalace = featureIcePalace;
		
		short[][][] piece = this.getPiece();
		this.width = piece.length;
		this.length = piece[0].length;
		this.height = piece[0][0].length;
	}

	public void place(int x, int y, int z) {
		this.centerX = x;
		this.baseY = y;
		this.centerZ = z;
		
		if(this.rotated) {
			this.aabb = new FeatureAABB(
					this.centerX - (this.length >> 1),
					this.baseY,
					this.centerZ - (this.width >> 1),
					this.centerX + (this.length >> 1),
					this.baseY + this.height - 1,
					this.centerZ + (this.width >> 1)
			);
		} else {
			this.aabb = new FeatureAABB(
					this.centerX - (this.width >> 1),
					this.baseY,
					this.centerZ - (this.length >> 1),
					this.centerX + (this.width >> 1),
					this.baseY + this.height - 1,
					this.centerZ + (this.length >> 1)
			);
		}
	}
	
	protected int rotateX(int x, int z) {
		return this.rotated ? z : x;
	}

	protected int rotateZ(int x, int z) {
		return this.rotated ? x : z;
	}
	
	/*
	 * Draws the piece centered. This assumes piece w/h is an odd value.
	 * If it detects chests or spawners, it will add them to the feature's special blocks.
	 */
	public void drawPiece(short[][][] map) {
		int x1 = this.aabb.x1;
		int z1 = this.aabb.z1;
		int y1 = this.aabb.y1;
	
		short[][][] piece = this.getPiece();
		
		for(int x = 0; x < width; x ++) {
			for(int z = 0; z < length; z ++) {
				for(int y = 0; y < height; y ++) {
					short s = piece[x][z][y];
					if(s >= 0) {					
						int blockID = s & 0xff;
						int meta = s >> 8;
			
						// Rotate block?
						if(this.rotated) {
							switch(blockID) {
							case 65: // Stairs 2->3, 4->5
							case 54: // Chests 2->3, 4->5
								meta ^= 1;	break; 	
							}
							s = (short)(blockID | (meta << 8));
						}
						
						// Special blocks: chests and spawners. 
						// Shiny glass (147) is here so it is put out in the populate stage and brightness is handled properly	
						if(blockID == 52 || blockID == 54 || blockID == 147 || blockID == 151) {
							this.featureIcePalace.addSpecialBlock(x1 + this.rotateX(x, z), y1 + y, z1 + this.rotateZ(x, z), blockID, meta);
							map[x1 + this.rotateX(x, z)][z1 + this.rotateZ(x, z)][y1 + y] = 4; // why 4? solid and easy to spot (in case of bugs)
						} else {
							map[x1 + this.rotateX(x, z)][z1 + this.rotateZ(x, z)][y1 + y] = s;
						}
					}
				}
			}
		}
	}
}
