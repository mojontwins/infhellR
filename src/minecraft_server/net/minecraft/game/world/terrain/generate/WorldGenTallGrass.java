package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockFlower;

public class WorldGenTallGrass extends WorldGenerator {
	private int tallGrassID;
	private int tallGrassMetadata;

	public WorldGenTallGrass(int i1, int i2) {
		this.tallGrassID = i1;
		this.tallGrassMetadata = i2;
	}

	public boolean generate(World world1, Random rand, int x0, int y0, int z0) {
		int meta = this.tallGrassMetadata;
		/*for(int i11 = 0; ((i11 = world1.getBlockId(x0, y0, z0)) == 0 || i11 == Block.leaves.blockID) && y0 > 0; --y0) {
		}*/

		for(int attempts = 0; attempts < 32; ++attempts) {
			int x = x0 + rand.nextInt(8) - rand.nextInt(8);
			int y = y0 + rand.nextInt(4) - rand.nextInt(4);
			int z = z0 + rand.nextInt(8) - rand.nextInt(8);
			
			if (
					this.tallGrassMetadata < 0 && 
					(world1.isAirBlock(x, y, z) || world1.getBlockId(x, y, z) == Block.layeredSand.blockID)
			) {
				meta = -1;
				int blockIDBelow = world1.getBlockId(x, y - 1, z);
				if (blockIDBelow > 0) {
					if (blockIDBelow == Block.sand.blockID) {
						if (rand.nextBoolean ()) meta = 0x10;
					} else if (blockIDBelow == Block.terracotta.blockID || blockIDBelow == Block.stainedTerracotta.blockID) {
						meta = 0x20;
					} else {
						Block block = Block.blocksList[blockIDBelow];
						if (block != null && block.canGrowPlants()) meta = 0;
					}
					
					if (meta >= 0) world1.setBlockAndMetadata(x, y, z, this.tallGrassID, meta);
				}
			} else {			
				if(world1.isAirBlock(x, y, z) && ((BlockFlower)Block.blocksList[this.tallGrassID]).canBlockStay(world1, x, y, z, this.tallGrassMetadata)) {
					world1.setBlockAndMetadata(x, y, z, this.tallGrassID, meta);
				}
			}
		}

		return true;
	}
}
