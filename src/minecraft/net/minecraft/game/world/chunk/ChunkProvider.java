package net.minecraft.game.world.chunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldChunkManager;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.loader.IChunkLoader;

public class ChunkProvider implements IChunkProvider {
	private Set<Integer> droppedChunksSet = new HashSet<Integer>();
	private Chunk blankChunk;
	private IChunkProvider chunkProvider;
	private IChunkLoader chunkLoader;
	private Map<Integer,Chunk> chunkMap = new HashMap<Integer, Chunk>();
	private List<Chunk> chunkList = new ArrayList<Chunk>();
	private World worldObj;
	private int chunkCheckIndex;

	public ChunkProvider(World world1, IChunkLoader iChunkLoader2, IChunkProvider iChunkProvider3) {
		this.blankChunk = new EmptyChunk(world1, 0, 0);
		this.worldObj = world1;
		this.chunkLoader = iChunkLoader2;
		this.chunkProvider = iChunkProvider3;
	}

	public boolean chunkExists(int i1, int i2) {
		return this.chunkMap.containsKey(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
	}

	public void dropChunk(int i1, int i2) {
		ChunkCoordinates chunkCoordinates3 = this.worldObj.getSpawnPoint();
		int i4 = i1 * 16 + 8 - chunkCoordinates3.posX;
		int i5 = i2 * 16 + 8 - chunkCoordinates3.posZ;
		short s6 = 128;
		if(i4 < -s6 || i4 > s6 || i5 < -s6 || i5 > s6) {
			this.droppedChunksSet.add(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
		}

	}

	public Chunk prepareChunk(int chunkX, int chunkZ) {
		int chunkHash = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
		this.droppedChunksSet.remove(chunkHash);
		Chunk chunk = (Chunk)this.chunkMap.get(chunkHash);
		boolean generated = false;
		if(chunk == null) {
			chunk = this.loadChunkFromFile(chunkX, chunkZ);
			if(chunk == null) {
				if(this.chunkProvider == null) {
					chunk = this.blankChunk;
				} else {
					chunk = this.chunkProvider.provideChunk(chunkX, chunkZ);
					generated = true;
				}
			}

			this.chunkMap.put(chunkHash, chunk);
			this.chunkList.add(chunk);
			if(chunk != null) {
				chunk.onChunkLoad();
				if(generated) chunk.initLightingForRealNotJustHeightmap();
			}

			if(!chunk.isTerrainPopulated && this.chunkExists(chunkX + 1, chunkZ + 1) && this.chunkExists(chunkX, chunkZ + 1) && this.chunkExists(chunkX + 1, chunkZ)) {
				this.populate(this, chunkX, chunkZ);
			}

			if(this.chunkExists(chunkX - 1, chunkZ) && !this.provideChunk(chunkX - 1, chunkZ).isTerrainPopulated && this.chunkExists(chunkX - 1, chunkZ + 1) && this.chunkExists(chunkX, chunkZ + 1) && this.chunkExists(chunkX - 1, chunkZ)) {
				this.populate(this, chunkX - 1, chunkZ);
			}

			if(this.chunkExists(chunkX, chunkZ - 1) && !this.provideChunk(chunkX, chunkZ - 1).isTerrainPopulated && this.chunkExists(chunkX + 1, chunkZ - 1) && this.chunkExists(chunkX, chunkZ - 1) && this.chunkExists(chunkX + 1, chunkZ)) {
				this.populate(this, chunkX, chunkZ - 1);
			}

			if(this.chunkExists(chunkX - 1, chunkZ - 1) && !this.provideChunk(chunkX - 1, chunkZ - 1).isTerrainPopulated && this.chunkExists(chunkX - 1, chunkZ - 1) && this.chunkExists(chunkX, chunkZ - 1) && this.chunkExists(chunkX - 1, chunkZ)) {
				this.populate(this, chunkX - 1, chunkZ - 1);
			}
		}

		
		return chunk;
	}

	public Chunk provideChunk(int i1, int i2) {
		Chunk chunk3 = (Chunk)this.chunkMap.get(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
		return chunk3 == null ? this.prepareChunk(i1, i2) : chunk3;
	}
	
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		return this.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
	}

	private Chunk loadChunkFromFile(int i1, int i2) {
		if(this.chunkLoader == null) {
			return null;
		} else {
			try {
				Chunk chunk3 = this.chunkLoader.loadChunk(this.worldObj, i1, i2);
				if(chunk3 != null) {
					chunk3.lastSaveTime = this.worldObj.getWorldTime();
					if(chunk3.biomeGenCache == null) {
						WorldChunkManager manager = this.worldObj.getWorldChunkManager();
						BiomeGenBase[] biomesForGeneration = manager.loadBlockGeneratorData(null, i1 * 16, i2 * 16, 16, 16);
						chunk3.biomeGenCache = biomesForGeneration;
						if(manager.temperature != null) {
							chunk3.setClimateCache(manager.temperature, manager.humidity);
						}
					}
				}

				return chunk3;
			} catch (Exception exception4) {
				exception4.printStackTrace();
				return null;
			}
		}
	}

	private void saveExtraChunkData(Chunk chunk1) {
		if(this.chunkLoader != null) {
			try {
				this.chunkLoader.saveExtraChunkData(this.worldObj, chunk1);
			} catch (Exception exception3) {
				exception3.printStackTrace();
			}

		}
	}

	private void saveChunk(Chunk chunk1) {
		if(this.chunkLoader != null) {
			try {
				chunk1.lastSaveTime = this.worldObj.getWorldTime();
				this.chunkLoader.saveChunk(this.worldObj, chunk1);
			} catch (IOException iOException3) {
				iOException3.printStackTrace();
			}

		}
	}

	public void populate(IChunkProvider iChunkProvider1, int i2, int i3) {
		Chunk chunk4 = this.provideChunk(i2, i3);
		if(!chunk4.isTerrainPopulated) {
			chunk4.isTerrainPopulated = true;
			if(this.chunkProvider != null) {
				this.chunkProvider.populate(iChunkProvider1, i2, i3);
				chunk4.setChunkModified();
			}
		}

	}

	public boolean saveChunks(boolean z1, IProgressUpdate iProgressUpdate2) {
		int i3 = 0;

		for(int i4 = 0; i4 < this.chunkList.size(); ++i4) {
			Chunk chunk5 = (Chunk)this.chunkList.get(i4);
			if(z1 && !chunk5.neverSave) {
				this.saveExtraChunkData(chunk5);
			}

			if(chunk5.needsSaving(z1)) {
				this.saveChunk(chunk5);
				chunk5.isModified = false;
				++i3;
				if(i3 == 24 && !z1) {
					return false;
				}
			}
		}

		if(z1) {
			if(this.chunkLoader == null) {
				return true;
			}

			this.chunkLoader.saveExtraData();
		}

		return true;
	}

	public boolean unload100OldestChunksOrig() {
		for(int i1 = 0; i1 < 100; ++i1) {
			if(!this.droppedChunksSet.isEmpty()) {
				Integer integer2 = (Integer)this.droppedChunksSet.iterator().next();
				Chunk chunk3 = (Chunk)this.chunkMap.get(integer2);
				chunk3.onChunkUnload();
				this.saveChunk(chunk3);
				this.saveExtraChunkData(chunk3);
				this.droppedChunksSet.remove(integer2);
				this.chunkMap.remove(integer2);
				this.chunkList.remove(chunk3);
			}
		}

		if(this.chunkLoader != null) {
			this.chunkLoader.chunkTick();
		}

		return this.chunkProvider.unload100OldestChunks();
	}

	public boolean unload100OldestChunks() {
		int i1;
		for(i1 = 0; i1 < 100; ++i1) {
			if(!this.droppedChunksSet.isEmpty()) {
				Integer chunkHash = (Integer)this.droppedChunksSet.iterator().next();
				Chunk chunk3 = (Chunk)this.chunkMap.get(chunkHash);
				chunk3.onChunkUnload();
				this.saveChunk(chunk3);
				this.saveExtraChunkData(chunk3);
				this.droppedChunksSet.remove(chunkHash);
				this.chunkMap.remove(chunkHash);
				this.worldObj.evictHeightQuery(chunk3.xPosition, chunk3.zPosition);
				this.chunkList.remove(chunk3);
			}
		}

		for(i1 = 0; i1 < 10; ++i1) {
			if(this.chunkCheckIndex >= this.chunkList.size()) {
				this.chunkCheckIndex = 0;
				break;
			}

			Chunk chunk4 = (Chunk)this.chunkList.get(this.chunkCheckIndex++);
			EntityPlayer entityPlayer5 = this.worldObj.getClosestPlayerHorizontal((double)(chunk4.xPosition << 4) + 8.0D, (double)(chunk4.zPosition << 4) + 8.0D, 288.0D);
			if(entityPlayer5 == null) {
				this.dropChunk(chunk4.xPosition, chunk4.zPosition);
			}
		}

		if(this.chunkLoader != null) {
			this.chunkLoader.chunkTick();
		}

		return this.chunkProvider.unload100OldestChunks();
	}
	
	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return "ServerChunkCache: " + this.chunkMap.size() + " Drop: " + this.droppedChunksSet.size();
	}
}
