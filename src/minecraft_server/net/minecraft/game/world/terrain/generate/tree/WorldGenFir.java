package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.WorldGenMojon;
import net.minecraft.game.world.World;

public class WorldGenFir extends WorldGenMojon {
	
	private static final int leavesID = Block.leaves.blockID;
	private static final int leavesMeta = EnumTreeType.FIR.getLeafMetadata();;
	private static final int trunkID = Block.wood.blockID;
	private static final int trunkMeta = 0;
	
	public int height = 6;
	public boolean large = false;
	
	public WorldGenFir(int height, boolean large) {
		this(height, large, false);
	}
	
	public WorldGenFir(int height, boolean large, boolean withNotify) {
		super(withNotify);
		this.height = height;
		this.large = large;
	}
	
	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		
		if(this.large) {
			// Big fir version
			
			if(y0 + this.height > Chunk.SECTION_HEIGHT - 2) return false;
			
			// Check if tree fits / valid soil
			
			if(!this.validGround(world, x0, y0 - 1, z0)) return false;
			if(!this.validGround(world, x0, y0 - 1, z0 + 1)) return false;
			if(!this.validGround(world, x0 + 1, y0 - 1, z0)) return false;
			if(!this.validGround(world, x0 + 1, y0 - 1, z0 + 1)) return false;
			
			for(int y = 0; y < this.height + 18; y ++) {
				for(int x = 0; x <= 1; x ++) {
					for(int z = 0; z <= 1; z ++) {
						if(world.isBlockOpaqueCube(x0 + x, y0 + y, z0 + z)) return false;
					}
				}
			}
			
			for(int y = 0; y < 18; y ++) {
				for(int i = 0; i <= 5; i ++) {
					for(int j = 0; j <= i; j ++) {
						if(world.isBlockOpaqueCube(x0 - 5 + i, y0 + y, z0 - i)) return false;
						if(world.isBlockOpaqueCube(x0 - 5 + i, y0 + y, z0 + 1 + i)) return false;
						if(world.isBlockOpaqueCube(x0 + 6 - i, y0 + y, z0 - i)) return false;
						if(world.isBlockOpaqueCube(x0 + 6 - i, y0 + y, z0 + 1 + i)) return false;
					}
				}
			}
			
			// Draw
			
			int trunkTop = y0 + this.height;
			
			// Layers
			
			for(int y = 0; y < 3; y ++) {
				this.squareTreeLayer(world, rand, x0, trunkTop + 18 + y, z0, 2, leavesID, leavesMeta);
				this.diamondTreeLayer(world, rand, x0, trunkTop + 15 + y, z0, 4, leavesID, leavesMeta);
				this.diamondTreeLayer(world, rand, x0, trunkTop + 12 + y, z0, 6, leavesID, leavesMeta);
			}
			
			// Platforms
		
			this.diamondTreeLayer(world, rand, x0, trunkTop + 9, z0, 6, leavesID, leavesMeta);
			this.diamondTreeLayer(world, rand, x0, trunkTop + 8, z0, 8, leavesID, leavesMeta);
			this.diamondTreeLayer(world, rand, x0, trunkTop + 5, z0, 8, leavesID, leavesMeta);
			this.diamondTreeLayer(world, rand, x0, trunkTop + 4, z0, 10, leavesID, leavesMeta);
			this.diamondTreeLayer(world, rand, x0, trunkTop + 1, z0, 10, leavesID, leavesMeta);
			this.diamondTreeLayer(world, rand, x0, trunkTop + 0, z0, 12, leavesID, leavesMeta);
			
			// Trunk
			
			for(int y = y0; y < trunkTop + 18; y ++) {
				this.setBlockWithMetadata(world, x0, y, z0, trunkID, trunkMeta);
				this.setBlockWithMetadata(world, x0, y, z0 + 1, trunkID, trunkMeta);
				this.setBlockWithMetadata(world, x0 + 1, y, z0, trunkID, trunkMeta);
				this.setBlockWithMetadata(world, x0 + 1, y, z0 + 1, trunkID, trunkMeta);
			}
			
			return true;
		} else {
			// Normal fir version
			
			this.height -= 3; if(this.height < 3) this.height = 3;
			if(y0 + this.height > Chunk.SECTION_HEIGHT - 2) return false;
			
			// Check if tree fits / valid soil
			if(!this.validGround(world, x0, y0 - 1, z0)) return false;
			
			for(int y = y0; y < y0 + this.height + 5; y ++) {
				if(world.isBlockOpaqueCube(x0, y, z0)) return false;
			}
			
			for(int y = y0 + this.height; y < y0 + this.height + 5; y ++) {
				for(int i = 0; i < 5; i ++) {
					if(world.isBlockOpaqueCube(x0 + i - 2, y, z0)) return false;
					if(world.isBlockOpaqueCube(x0, y, z0 + i - 2)) return false;
				}

				for(int x = x0 - 1; x <= x0 + 1; x ++) {
					for(int z = z0 - 1; z <= z0 + 1; z ++) {
						if(world.isBlockOpaqueCube(x, y, z)) return false;
					}
				}
			}
			
			// Tree fits, draw all tree layers
			
			int treeTop = y0 + this.height + 5;
			
			// Top cone
			
			for(int y = 0; y < 2; y ++) {
				this.setBlockIfEmpty(world, x0, treeTop + y, z0, leavesID, leavesMeta);	
				this.crossTreeLayer(world, rand, x0, treeTop - 1 - y, z0, 3, leavesID, leavesMeta);
			}
			
			// Platforms
			
			this.crossTreeLayer(world, rand, x0, treeTop - 4, z0, 3, leavesID, leavesMeta);
			this.crossTreeLayer(world, rand, x0, y0 + this.height, z0, 5, leavesID, leavesMeta);
			this.squareTreeLayer(world, rand, x0, y0 + this.height, z0, 3, leavesID, leavesMeta);
			
			// Trunk
			for(int y = y0; y < y0 + this.height + 5; y ++) {
				this.setBlockWithMetadata(world, x0, y, z0, trunkID, trunkMeta);
			}
			
			return true;
		}
		
	}

}
