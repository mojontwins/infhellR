package net.minecraft.client;

import java.util.Arrays;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.LongHashMap;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.EmptyChunk;
import net.minecraft.game.world.chunk.IChunkProvider;

public class ChunkProviderClient implements IChunkProvider {
	private Chunk blankChunk;
	private LongHashMap chunkMapping = new LongHashMap();
	private World worldObj;

	public ChunkProviderClient(World world) {
		this.blankChunk = new EmptyChunk(world, 0, 0);
		this.worldObj = world;
	}

	public boolean chunkExists(int x, int z) {
		return this.chunkMapping.containsItem(ChunkCoordIntPair.chunkXZ2Long(x, z));
	}

	public void unloadChunk(int x, int z) {
		Chunk chunk = this.provideChunk(x, z);
		if (!chunk.getIsChunkRendered()) {
			chunk.onChunkUnload();
		}
		this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Long(x, z));
	}

	public Chunk prepareChunk(int x, int z) {
		byte[] blockData = new byte[32768];
		byte[] metaData = new byte[32768];
		Chunk chunk = new Chunk(this.worldObj, blockData, metaData, x, z);
		Arrays.fill(chunk.skylightMap.data, (byte) -1);
		this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Long(x, z), chunk);
		chunk.isChunkLoaded = true;
		return chunk;
	}

	public Chunk provideChunk(int x, int z) {
		Chunk chunk = (Chunk) this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Long(x, z));
		return chunk == null ? this.blankChunk : chunk;
	}

	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		return this.provideChunk(chunkX, chunkZ);
	}

	public boolean saveChunks(boolean all, IProgressUpdate progress) {
		return true;
	}

	public boolean unload100OldestChunks() {
		return false;
	}

	public boolean canSave() {
		return false;
	}

	public void populate(IChunkProvider provider, int x, int z) {
	}

	public String makeString() {
		return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements();
	}
}
