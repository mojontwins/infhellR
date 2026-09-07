package net.minecraft.game.world.chunk;

import net.minecraft.game.IProgressUpdate;

public interface IChunkProvider {
	boolean chunkExists(int i1, int i2);

	Chunk provideChunk(int i1, int i2);

	Chunk prepareChunk(int i1, int i2);

	void populate(IChunkProvider iChunkProvider1, int i2, int i3);

	boolean saveChunks(boolean z1, IProgressUpdate iProgressUpdate2);

	boolean unload100OldestChunks();

	boolean canSave();

	String makeString();

	Chunk justGenerateForHeight(int i, int j);
}
