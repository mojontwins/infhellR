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
}
