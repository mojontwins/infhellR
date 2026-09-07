package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenEucalyptus1 extends WorldGenerator {
	private int height = 0;
	private int span = 5;
	private int branches = 8;
	
	private int woodId = Block.wood.blockID;
	private int woodMeta = 0;
	private int leavesId = Block.leaves.blockID;
	private int leavesMeta = 2;
	
	// Adapted from Enhanced Biomes
	// TODO: Fix meta for horizontal branches!
	
	public WorldGenEucalyptus1() {
		// TODO Auto-generated constructor stub
	}
	
	public WorldGenEucalyptus1(int height, int span, int branches) {
		this.height = height;
		this.span = span;
		this.branches = branches;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (this.height == 0) height = 8 + rand.nextInt(4);
		
		Block block = Block.blocksList[world.getBlockId(x, y - 1, z)];
		if (block == null || !block.canGrowPlants()) return false;

		for(int i = 0; i < span; i++) {
			for(int k = -span; k < span + 1; k++) {
				for(int j = -span; j < span + 1; j++) {
					if(world.isBlockOpaqueCube(x + k, y + height + i,  z + j)) return false;
				}
			}
		}

		for(int p = -2; p < 3; p++) {
			for(int r = -1; r < 2; r++) {
				setBlockIfEmpty(world, x + p, y + height + span + 1, z + r, this.leavesId, this.leavesMeta);
				setBlockIfEmpty(world, x + r, y + height + span + 1, z + p, this.leavesId, this.leavesMeta);
			}
		}

		for(int p = -1; p < 2; p++) {
			for(int r = -1; r < 2; r++) {
				setBlockIfEmpty(world, x + r, y + height + span + 2, z + p, this.leavesId, this.leavesMeta);
			}
		}

		for(int a = 0; a < branches; a++) {
			int disX = (rand.nextInt((span * 2) + 1)) - span;
			int disY = rand.nextInt(span + 1);
			int disZ = (rand.nextInt((span * 2) + 1)) - span;

			int posX = x + disX;
			int posY = y + height - 1 + disY;
			int posZ = z + disZ;

			for(int p = -2; p < 3; p++) {
				for(int r = -1; r < 2; r++) {
					setBlockIfEmpty(world, posX + p, posY, posZ + r, this.leavesId, this.leavesMeta);
					setBlockIfEmpty(world, posX + r, posY, posZ + p, this.leavesId, this.leavesMeta);
				}
			}

			for(int p = -1; p < 2; p++) {
				for(int r = -1; r < 2; r++) {
					world.setBlockAndMetadata(posX + r, posY + 1, posZ + p, this.leavesId, this.leavesMeta);
				}
			}

			int orientation = disX < disZ ? 4 : 12;
			if(disY > disX && disY > disZ) {
				orientation = 0;
			} 
			
			for(int b = 0; b < span; b++) {
				int rx = disX * (b + 1) / span;
				int ry = disY * (b + 1) / span;
				int rz = disZ * (b + 1) / span;

				world.setBlockAndMetadata(x + rx, y + height - 1 + ry, z + rz, this.woodId, orientation | this.woodMeta);
			}

			world.setBlockAndMetadata(posX, posY, posZ, this.woodId, this.woodMeta);
		}

		for(int i = 0; i < height + span + 2; i++) {
			world.setBlockAndMetadata(x, y + i, z, this.woodId, this.woodMeta);
		}

		return true;
	}

}
