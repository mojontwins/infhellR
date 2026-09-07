package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.IChunkProvider;

public class MapGenBase {
	protected int range = 8;
	protected Random rand = new Random();

	public void generate(IChunkProvider iChunkProvider, World world, int chunkX, int chunkZ, byte[] blockArray) {
		int range = this.range;
		this.rand.setSeed(world.getRandomSeed());
		long prime1 = this.rand.nextLong() / 2L * 2L + 1L;
		long prime2 = this.rand.nextLong() / 2L * 2L + 1L;

		for(int cx = chunkX - range; cx <= chunkX + range; ++cx) {
			for(int cz = chunkZ - range; cz <= chunkZ + range; ++cz) {
				
				// Call recursiveGenerate for each chunk surronding chunkX, chunkZ in range.				
				this.rand.setSeed((long)cx * prime1 + (long)cz * prime2 ^ world.getRandomSeed());
				this.recursiveGenerate(world, cx, cz, chunkX, chunkZ, blockArray);
			}
		}

	}
	protected void recursiveGenerate(World world1, int i2, int i3, int i4, int i5, byte[] b6) {
	}
	
	public void setHeightMap(byte [] heightMap) {
	}
}
