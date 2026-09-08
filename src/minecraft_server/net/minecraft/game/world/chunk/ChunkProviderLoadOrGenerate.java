package net.minecraft.game.world.chunk;

import java.io.IOException;
import net.minecraft.game.IProgressUpdate;

import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldChunkManager;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.biome.BiomeGenBase;

public class ChunkProviderLoadOrGenerate implements IChunkProvider {
	private Chunk blankChunk;
	private IChunkProvider chunkProvider;
	private IChunkLoader chunkLoader;
	private Chunk[] chunks;
	private World worldObj;
	int lastQueriedChunkXPos;
	int lastQueriedChunkZPos;
	private Chunk lastQueriedChunk;
	private int curChunkX;
	private int curChunkY;

	public void setCurrentChunkOver(int i1, int i2) {
		this.curChunkX = i1;
		this.curChunkY = i2;
	}

	public boolean canChunkExist(int i1, int i2) {
		byte b3 = 15;
		return i1 >= this.curChunkX - b3 && i2 >= this.curChunkY - b3 && i1 <= this.curChunkX + b3 && i2 <= this.curChunkY + b3;
	}

	public boolean chunkExists(int chunkX, int chunkZ) {
		if(!this.canChunkExist(chunkX, chunkZ)) {
			return false;
		} else if(chunkX == this.lastQueriedChunkXPos && chunkZ == this.lastQueriedChunkZPos && this.lastQueriedChunk != null) {
			return true;
		} else {
			int i3 = chunkX & 31;
			int i4 = chunkZ & 31;
			int i5 = i3 + i4 * 32;
			Chunk chunk = this.chunks[i5];
			
			return chunk != null && (chunk == this.blankChunk || chunk.isAtLocation(chunkX, chunkZ));
		}
	}

	public Chunk prepareChunk(int i1, int i2) {
		return this.provideChunk(i1, i2);
	}

	public Chunk provideChunk(int chunkX, int chunkZ) {
		if(chunkX == this.lastQueriedChunkXPos && chunkZ == this.lastQueriedChunkZPos && this.lastQueriedChunk != null) {
			return this.lastQueriedChunk;
		} else if(!this.worldObj.findingSpawnPoint && !this.canChunkExist(chunkX, chunkZ)) {
			return this.blankChunk;
		} else {
			int i3 = chunkX & 31;
			int i4 = chunkZ & 31;
			int i5 = i3 + i4 * 32;
			if(!this.chunkExists(chunkX, chunkZ)) {
				if(this.chunks[i5] != null) {
					this.chunks[i5].onChunkUnload();
					this.saveChunk(this.chunks[i5]);
					this.saveExtraChunkData(this.chunks[i5]);
				}

				Chunk chunk6 = this.getChunkAt(chunkX, chunkZ);
				if(chunk6 == null) {
					if(this.chunkProvider == null) {
						chunk6 = this.blankChunk;
					} else {
						chunk6 = this.chunkProvider.provideChunk(chunkX, chunkZ);
						chunk6.removeUnknownBlocks();
					}
				}

				this.chunks[i5] = chunk6;
				chunk6.doNothing();
				if(this.chunks[i5] != null) {
					this.chunks[i5].onChunkLoad();
					this.chunks[i5].initLightingForRealNotJustHeightmap();
				}

				if( !this.chunks[i5].isTerrainPopulated && 
					this.chunkExists(chunkX + 1, chunkZ + 1) && 
					this.chunkExists(chunkX, chunkZ + 1) && 
					this.chunkExists(chunkX + 1, chunkZ)
				) {
					this.populate(this, chunkX, chunkZ);
				}

				if( this.chunkExists(chunkX - 1, chunkZ) && 
					!this.provideChunk(chunkX - 1, chunkZ).isTerrainPopulated && 
					this.chunkExists(chunkX - 1, chunkZ + 1) && 
					this.chunkExists(chunkX, chunkZ + 1) 
					//&& this.chunkExists(chunkX - 1, chunkZ)
				) {
					this.populate(this, chunkX - 1, chunkZ);
				}

				if( this.chunkExists(chunkX, chunkZ - 1) && 
					!this.provideChunk(chunkX, chunkZ - 1).isTerrainPopulated && 
					this.chunkExists(chunkX + 1, chunkZ - 1) && 
					// this.chunkExists(chunkX, chunkZ - 1) && 
					this.chunkExists(chunkX + 1, chunkZ)
				) {
					this.populate(this, chunkX, chunkZ - 1);
				}

				if( this.chunkExists(chunkX - 1, chunkZ - 1) && 
					!this.provideChunk(chunkX - 1, chunkZ - 1).isTerrainPopulated && 
					// this.chunkExists(chunkX - 1, chunkZ - 1) && 
					this.chunkExists(chunkX, chunkZ - 1) && 
					this.chunkExists(chunkX - 1, chunkZ)
				) {
					this.populate(this, chunkX - 1, chunkZ - 1);
				}
			}

			this.lastQueriedChunkXPos = chunkX;
			this.lastQueriedChunkZPos = chunkZ;
			this.lastQueriedChunk = this.chunks[i5];
			return this.chunks[i5];
		}
	}
	
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		return this.provideChunk(chunkX, chunkZ);
	}

	private Chunk getChunkAt(int posX, int posZ) {
		if(this.chunkLoader == null) {
			return this.blankChunk;
		} else {
			try {
				Chunk chunk3 = this.chunkLoader.loadChunk(this.worldObj, posX, posZ);
				if(chunk3 != null) {
					chunk3.lastSaveTime = this.worldObj.getWorldTime();
					
					if(chunk3.biomeGenCache == null) {
						BiomeGenBase [] biomesForGeneration = null;
						biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(biomesForGeneration, posX * 16, posZ * 16, 16, 16);
						
						// A reference of the biomeGenCache object
						chunk3.biomeGenCache = biomesForGeneration;
						
						// Seed the per-column climate caches from the same ramp pass.
						WorldChunkManager manager = this.worldObj.getWorldChunkManager();
						if(manager.temperature != null) {
							chunk3.setClimateCache(manager.temperature, manager.humidity);
						}
					}
				}

				return chunk3;
			} catch (Exception exception4) {
				exception4.printStackTrace();
				return this.blankChunk;
			}
		}
	}

	private void saveExtraChunkData(Chunk chunk) {
		if(this.chunkLoader != null) {
			try {
				this.chunkLoader.saveExtraChunkData(this.worldObj, chunk);
			} catch (Exception exception3) {
				exception3.printStackTrace();
			}

		}
	}

	private void saveChunk(Chunk chunk) {
		if(this.chunkLoader != null) {
			try {
				chunk.lastSaveTime = this.worldObj.getWorldTime();
				this.chunkLoader.saveChunk(this.worldObj, chunk);
			} catch (IOException iOException3) {
				iOException3.printStackTrace();
			}

		}
	}

	public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
		Chunk chunk4 = this.provideChunk(chunkX, chunkZ);
		if(!chunk4.isTerrainPopulated) {
			chunk4.isTerrainPopulated = true;
			if(this.chunkProvider != null) {
				this.chunkProvider.populate(chunkProvider, chunkX, chunkZ);
				chunk4.setChunkModified();
			}
		}

	}

	public boolean saveChunks(boolean flag, IProgressUpdate progressUpdate) {
		int i3 = 0;
		int i4 = 0;
		int i5;
		if(progressUpdate != null) {
			for(i5 = 0; i5 < this.chunks.length; ++i5) {
				if(this.chunks[i5] != null && this.chunks[i5].needsSaving(flag)) {
					++i4;
				}
			}
		}

		i5 = 0;

		for(int i6 = 0; i6 < this.chunks.length; ++i6) {
			if(this.chunks[i6] != null) {
				if(flag && !this.chunks[i6].neverSave) {
					this.saveExtraChunkData(this.chunks[i6]);
				}

				if(this.chunks[i6].needsSaving(flag)) {
					this.saveChunk(this.chunks[i6]);
					this.chunks[i6].isModified = false;
					++i3;
					if(i3 == 2 && !flag) {
						return false;
					}

					if(progressUpdate != null) {
						++i5;
						if(i5 % 10 == 0) {
							progressUpdate.setLoadingProgress(i5 * 100 / i4);
						}
					}
				}
			}
		}

		if(flag) {
			if(this.chunkLoader == null) {
				return true;
			}

			this.chunkLoader.saveExtraData();
		}

		return true;
	}

	public boolean unload100OldestChunks() {
		if(this.chunkLoader != null) {
			this.chunkLoader.chunkTick();
		}

		return this.chunkProvider.unload100OldestChunks();
	}

	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return "ChunkCache: " + this.chunks.length;
	}
}
