package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenDesertFlowers extends WorldGenerator {
	private int plantBlockId;
	private int plantMeta;
	private int plantMetaAlt;
	
	public WorldGenDesertFlowers(int blockID, int meta, int metaAlt) {
		this.plantBlockId = blockID;
		this.plantMeta = meta;
		this.plantMetaAlt = metaAlt;
	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		for(int i6 = 0; i6 < 64; ++i6) {
			int i7 = x + rand.nextInt(8) - rand.nextInt(8);
			int i8 = y + rand.nextInt(4) - rand.nextInt(4);
			int i9 = z + rand.nextInt(8) - rand.nextInt(8);
			
			int blockIDThis = world.getBlockId(i7, i8, i9);
			int blockIDBeneath = world.getBlockId(i7, i8 - 1, i9);
			
			if(blockIDThis == 0 || blockIDThis == Block.layeredSand.blockID) {
				
				if(this.plantBlockId == Block.tallGrass.blockID && blockIDBeneath == Block.grass.blockID) {
					world.setBlockAndMetadata(i7, i8, i9, Block.tallGrass.blockID, 0);
				} else if(blockIDBeneath == Block.sand.blockID || blockIDBeneath == Block.terracotta.blockID || blockIDBeneath == Block.stainedTerracotta.blockID || blockIDBeneath == Block.dirt.blockID) {
					int meta = rand.nextInt(3) == 0 ? this.plantMetaAlt : this.plantMeta;
					world.setBlockAndMetadata(i7, i8, i9, this.plantBlockId, meta);
				}
			}
		}

		return true;
	}

}
