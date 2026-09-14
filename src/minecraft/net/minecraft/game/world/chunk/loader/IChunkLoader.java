package net.minecraft.game.world.chunk.loader;

import java.io.IOException;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;

public interface IChunkLoader {
	Chunk loadChunk(World world1, int i2, int i3) throws IOException;

	void saveChunk(World world1, Chunk chunk2) throws IOException;

	void saveExtraChunkData(World world1, Chunk chunk2) throws IOException;

	void chunkTick();

	void saveExtraData();

	/**
	 * Returns true when the chunk at the given chunk coordinates has already been
	 * persisted by this loader - i.e. its population stage completed and the blocks it
	 * holds (including any feature structures) will never be drawn again by the feature
	 * pipeline.
	 *
	 * <p>FeatureDynamicSchematic uses this to size how many of its chunks still need
	 * drawing after a world was saved mid-generation. The default falls back to the
	 * in-memory chunk cache ({@link World#chunkExists(int,int)}, which never touches the
	 * disk); loaders that index files should override it with a real storage check.
	 */
	default boolean chunkExists(World world, int chunkX, int chunkZ) {
		return world.chunkExists(chunkX, chunkZ);
	}
}
