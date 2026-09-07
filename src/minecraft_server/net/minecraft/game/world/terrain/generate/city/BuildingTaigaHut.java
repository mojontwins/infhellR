package net.minecraft.game.world.terrain.generate.city;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;


public class BuildingTaigaHut extends BuildingDynamic {
	public void generate(int y0, Chunk chunk) {
		System.out.println ("Taiga Hut @ " + (chunk.xPosition * 16) + " " + (chunk.zPosition * 16));
		
		Random rand = new Random(chunk.worldObj.getRandomSeed());
		this.setArrays(chunk.blocks, chunk.data);
		
		int w = 5 + 2 * rand.nextInt(3);
		int l = 5 + 2 * rand.nextInt(4);
		
		int x0 = rand.nextInt(16 - w - 2) + 1;
		int z0 = rand.nextInt(16 - l - 2) + 1;
		y0 = chunk.getLandSurfaceHeightValue(x0 + w / 2, z0 + l / 2) + 1;
		
		// Floor
		for(int x = -1; x <= w; x ++) {
			for(int z = -1; z <= l; z ++) {
				int idx = this.coords2Idx(x0 + x, y0 - 1, z0 + z);
				do {
					this.blocks[idx --] = (byte)Block.gravel.blockID;
				} while(this.blocks[idx] == 0);
			}
		}
		
		// Hollow
		for(int x = 1; x < w - 1; x ++) {
			for(int z = 1; z < l - 1; z ++) {
				int idx = this.coords2Idx(x0 + x, y0, z0 + z);
				this.blocks[idx ++] = 0;
				this.blocks[idx ++] = 0;
				this.blocks[idx ++] = 0;
			}
		}
		
		// Pillars
		int[] pillar = new int[] { 17, 17, 17 };
		this.setBlockIdColumn(x0, y0, z0, pillar);
		this.setBlockIdColumn(x0 + w - 1, y0, z0, pillar);
		this.setBlockIdColumn(x0, y0, z0 + l - 1, pillar);
		this.setBlockIdColumn(x0 + w - 1, y0, z0 + l - 1, pillar);

		// Walls
		int[] wall = new int[] { 4, 5, 5 };
		
		for(int x = 1; x < w - 1; x ++) {
			this.setBlockIdColumn(x + x0, y0, z0, wall);
			this.setBlockIdColumn(x + x0, y0, z0 + l - 1, wall);
		}
		
		for(int z = 1; z < l - 1; z ++) {
			this.setBlockIdColumn(x0, y0, z + z0, wall);
			this.setBlockIdColumn(x0 + w - 1, y0, z + z0, wall);
		}
		
		// Door
		this.setBlockIdAndMetadata(x0 + 2, y0, z0, Block.doorWood.blockID, 3);
		this.setBlockIdAndMetadata(x0 + 2, y0 + 1, z0, Block.doorWood.blockID, 11);
		
		// Windows
		for(int i = 0; i < (l - 3) / 2; i ++) {
			this.setBlockId(x0, y0 + 1, z0 + 2 + i * 2, Block.thinGlass.blockID);
			this.setBlockId(x0 + w - 1, y0 + 1, z0 + 2 + i * 2, Block.thinGlass.blockID);
		}
		
		// Roof
		for(int x = 0; x < w; x ++) {
			// Log
			int y = x < (w + 1) / 2 ? x : w - 1 - x;
			for(int z = 0; z < l; z ++) {
				this.setBlockIdAndMetadata(x0 + x, y0 + 2 + y, z0 + z, Block.wood.blockID, 8);
				
				// Air/planks below
				for(int yy = 0; yy < y; yy ++) {
					this.setBlockId(x0 + x, y0 + 2 + yy, z0 + z, z == 0 || z == l - 1 ? Block.planks.blockID : 0);
				}
			}
		}
		
		// Holes
		if(w > 5) {
			this.setBlockId(x0 + w / 2, y0 + w / 2, z0, Block.thinGlass.blockID);
			this.setBlockId(x0 + w / 2, y0 + w / 2, z0 + l - 1, Block.thinGlass.blockID);
		}
	}

	@Override
	public void populate(World world, Random rand, int x0, int y0, Chunk chunk) {
		// TODO Auto-generated method stub
		
	}
}
