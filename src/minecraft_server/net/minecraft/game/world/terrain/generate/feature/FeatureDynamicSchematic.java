package net.minecraft.game.world.terrain.generate.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.StructureBlockData;
import net.minecraft.game.world.schematic.Schematic;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.World;

public abstract class FeatureDynamicSchematic extends Feature {	
	/*
	 * Dynamic schematic features have a schematic rendered to a big array on creation.
	 * Array covers the whole feature, horizontally, but may have variable height.
	 * 
	 * This base class adds methods to render a portion of the array in the current chunk
	 * both during generation and population.
	 */
	
	// schematic is [x][z][y]
	public short schematic [][][];
	
	// Store special blocks here to process later
	public List<StructureBlockData> specialBlocks = new ArrayList<StructureBlockData>();
	
	// AABB of the whole structure
	public FeatureAABB aabb;

	/*
	 * How many chunks this feature still needs to draw before it can be released.
	 * Armed to the number of box chunks not already final (see computePopulatesRemaining())
	 * during build, decremented by the provider after each overlapping chunk is
	 * populated. When it reaches zero the feature is finished: the schematic is freed
	 * and the feature is removed from the provider (see onChunkPopulated), so the big
	 * array is never carried around as dead weight.
	 */
	private int pendingPopulates;

	public FeatureDynamicSchematic(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.featureAABB.y1 = this.getY0();
		this.featureAABB.y2 = 127;
	}

	public abstract int getFeatureHeight();

	public abstract void generateSchematic(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ);
	
	public abstract int getY0();
	
	/**
	 * Allocates the raw schematic array, resets its bounding box, records how many
	 * chunks must be populated before it can be freed, and asks the subclass to render
	 * its structure into the array.
	 *
	 * <p>Called exactly once per feature lifetime - from {@code setup()} when the
	 * feature is first created. The provider never needs to rebuild a schematic: once
	 * every overlapping chunk has been drawn the feature is removed from the provider's
	 * list (see {@link #onChunkPopulated(int,int)}), and any later chunk regeneration
	 * near a fully built structure is short-circuited before the spawn gate.
	 */
	private void buildSchematic(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		// The special-block list is (re)built by generateSchematic below.
		this.specialBlocks.clear();
		
		this.schematic = new short[(1 + this.getFeatureRadius() * 2) * 16][(1 + this.getFeatureRadius() * 2) * 16][this.getFeatureHeight()];
		this.aabb = new FeatureAABB(0, 0, 0, this.schematic.length - 1, this.schematic[0][0].length - 1, this.schematic[0].length -1);
		
		// One populate per chunk in the feature's bounding square that is NOT already
		// final. Chunks persisted in a previous session (or already resident with their
		// terrain populated) will never fire a populate pass again, so arming the counter
		// to only the outstanding ones lets it reach zero - and free the feature - at the
		// true last draw even when the world was saved mid-generation.
		this.pendingPopulates = this.computePopulatesRemaining();
		
		this.generateSchematic(world, rand, biome, chunkX, chunkZ);
	}

	/**
	 * Counts how many of this feature's chunks can still fire a populate pass in the
	 * current session; a chunk counts when it is NOT already "done".
	 *
	 * <p>"Done" means the chunk's population stage is final - either it is resident in
	 * the cache with isTerrainPopulated set, or it has been persisted by the chunk loader
	 * (a saved chunk is only written after its populate stage, so its blocks - including
	 * the structure - are final and it will never be populated again). Any chunk that is
	 * not done is exactly one that will be generated and populated again in this session,
	 * decrementing the release counter in {@link #onChunkPopulated(int,int)}.
	 *
	 * <p>Uses only cache and disk probes ({@link World#chunkExists(int,int)} neighbours
	 * the chunk cache and never forces generation), so it is side-effect free: on a brand
	 * new world every box chunk counts, reproducing the previous full-box arming.
	 */
	private int computePopulatesRemaining() {
		int radius = this.getFeatureRadius();
		int boxSize = (1 + radius * 2) * (1 + radius * 2);
		if(this.world == null) {
			return boxSize;
		}
		IChunkLoader chunkLoader = this.world.getChunkLoader();
		int remaining = 0;
		for(int chunkX = this.originChunkX - radius; chunkX <= this.originChunkX + radius; chunkX ++) {
			for(int chunkZ = this.originChunkZ - radius; chunkZ <= this.originChunkZ + radius; chunkZ ++) {
				boolean done = false;
				if(this.world.chunkExists(chunkX, chunkZ)) {
					// Resident: populated implies its (structure) blocks are final.
					done = this.world.getChunkFromChunkCoords(chunkX, chunkZ).isTerrainPopulated;
				} else if(chunkLoader != null && chunkLoader.chunkExists(this.world, chunkX, chunkZ)) {
					// Persisted in an earlier session: never re-populated, so no decrement.
					done = true;
				}
				if(!done) {
					remaining ++;
				}
			}
		}
		return remaining;
	}
	
	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.buildSchematic(world, rand, biome, chunkX, chunkZ);
	}
	
	/**
	 * Counts one populated chunk towards the release counter, and releases this feature
	 * once the LAST overlapping chunk has been drawn.
	 *
	 * <p>Only this feature's own box chunks (origin &plusmn; radius) are counted: the
	 * feature provider computes the box overlap before firing this, so neighboring
	 * unrelated chunks never drain the counter early.
	 *
	 * <p>This is the memory win of the whole feature: a large dynamic-schematic feature
	 * can hold a several-hundred-KB array for its whole radius, but every block it needs
	 * has been copied into actual chunks by the time its final overlapping chunk is
	 * populated - so the array and the feature object are dead weight from then on. They
	 * are dropped together here: the array is freed and the feature removes itself from
	 * the provider's featureList. A later chunk generation near the structure re-runs
	 * the spawn gate, which sees every box chunk already final and short-circuits (see
	 * FeatureProvider.isFeatureFullyBuilt) - so no rebuild is ever needed.
	 */
	@Override
	public void onChunkPopulated(int chunkX, int chunkZ) {
		if(this.schematic != null && this.pendingPopulates > 0) {
			--this.pendingPopulates;
			if(this.pendingPopulates == 0) {
				// Every chunk this feature spans is done: the schematic array and the
				// special-block list are dead weight from here on, and the feature can
				// never be drawn again - drop it from the provider entirely.
				this.schematic = null;
				this.specialBlocks.clear();
				this.featureProvider.removeFeature(this);
			}
		}
	}
	
	protected int getPieceX(int chunkX) {
		return chunkX - this.originChunkX + this.getFeatureRadius();
	}
	
	protected int getPieceZ(int chunkZ) {
		return chunkZ - this.originChunkZ + this.getFeatureRadius();
	}
	
	public void addSpecialBlock(int x, int y, int z, int blockID, int meta, boolean needsSupport) {
		this.specialBlocks.add(new StructureBlockData(x, y, z, blockID, meta, needsSupport));
	}
	
	public void drawPieceOnGeneration(int chunkX, int chunkZ, Chunk chunk) {
		// Piece to draw is:
		int pieceXoffs = this.getPieceX(chunkX) << 4;
		int pieceZoffs = this.getPieceZ(chunkZ) << 4;
		
		int y0 = this.getY0();
		if(y0 < 1) return;
		
		// Decode & copy blocks
		for(int x = 0; x < 16; x ++) {
			for(int z = 0; z < 16; z ++) {
				int y = y0;

				int idx = (x << 11) | (z << 7) | y;
				
				for(int i = 0; i < this.getFeatureHeight(); i ++) {
					short blockAndMeta = this.schematic[pieceXoffs + x][pieceZoffs + z][i];
					
					if(blockAndMeta != -1) {
						byte blockID = (byte)(blockAndMeta & 0xff);
						byte metadata = (byte)(blockAndMeta >> 8);
						
						chunk.blocks[idx] = blockID;
						chunk.data[idx] = metadata;
					}
					
					idx ++;
					y ++;
					
					if(y == 128) break;
				}
			}
		}
	}
	
	public void drawPieceOnPopulation(World world, Random rand, int chunkX, int chunkZ) {
		// Piece to draw is:
		int pieceXoffs = this.getPieceX(chunkX) << 4;
		int pieceZoffs = this.getPieceZ(chunkZ) << 4;
		
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		
		// Decode & copy blocks
		for(int x = 0; x < 16; x ++) {
			for(int z = 0; z < 16; z ++) {
				int y = this.getY0();
				
				for(int i = 0; i < this.getFeatureHeight(); i ++) {
					short blockAndMeta = this.schematic[pieceXoffs + x][pieceZoffs + z][i];
					
					if(blockAndMeta != -1) {
						byte blockID = (byte)(blockAndMeta & 0xff);
						byte metadata = (byte)(blockAndMeta >> 8);
						
						chunk.setBlockIDWithMetadata(x, y, z, blockID, metadata);
					}
					
					y ++;
					
					if (y == 128) break;
				}
			}
		}
	}
	
	// This method is to be called from populate
	public void drawSpecialBlocksForChunk(World world, Random rand, int chunkX, int chunkZ) {
		int pieceXoffs = this.getPieceX(chunkX) << 4;
		int pieceZoffs = this.getPieceZ(chunkZ) << 4;
		int y0 = this.getY0();
		
		Iterator<StructureBlockData> it = this.specialBlocks.iterator();
		while(it.hasNext()) {
			StructureBlockData s = it.next();
			
			if(s.x >= pieceXoffs && s.x < pieceXoffs + 15 && s.z >= pieceZoffs && s.z < pieceZoffs + 16) {
				int xAbs = this.featureAABB.x1 + s.x;
				int zAbs = this.featureAABB.z1 + s.z;
				int yAbs = y0 + s.y;
				
				// Some blocks shouldn't spawn on air.
				if (s.needsSupport && world.isAirBlock(xAbs, yAbs - 1, zAbs)) continue;
				
				world.setBlockAndMetadata(xAbs, yAbs, zAbs, s.blockID, s.blockMetadata);
				this.onSpecialBlockSet(xAbs, yAbs, zAbs, s.blockID, s.blockMetadata);
			}
		}
	}
	
	// You should override these methods accordingly
	public void onSpecialBlockSet(int x, int y, int z, int id, int meta) {
	}
	
	/*
	 * Draws a sphere radius r centered around x0, y0, z0
	 */
	public void sphere(int x0, int y0, int z0, int r, short s) {
		int rSq = r * r;
		for(int x = 0; x < r; x ++) {
			int xx = x * x;
			for(int z = 0; z < r; z ++) {
				int zz = z * z;
				for(int y = 0; y < r; y ++) {
					if(y * y + xx + zz <= rSq) {
						this.schematic[x0 + x][z0 + z][y0 + y] = s;
						this.schematic[x0 - x][z0 + z][y0 + y] = s;
						this.schematic[x0 + x][z0 - z][y0 + y] = s;
						this.schematic[x0 - x][z0 - z][y0 + y] = s;
						this.schematic[x0 + x][z0 + z][y0 - y] = s;
						this.schematic[x0 - x][z0 + z][y0 - y] = s;
						this.schematic[x0 + x][z0 - z][y0 - y] = s;
						this.schematic[x0 - x][z0 - z][y0 - y] = s;
					}
				}
			}
		}
	}
	
	/* 
	 * Fill whole schematic
	 */
	public void fillSchematic(short v) {
		// Fill the whole schematic with v
		for(short[][] arr1: this.schematic) {
			for(short[] arr2: arr1) {
				Arrays.fill(arr2, v);
			}
		}
	}
	
	/* 
	 * Load .schematic file into schematic @ x,y,z
	 */
	public void loadSchematic(String pathspec, int x0, int y0, int z0) {
		// Load schematic
		Schematic fileSchematic = new Schematic(pathspec);
		
		// Copy to main array
		int idx = 0;
		for(int y = 0; y < fileSchematic.getHeight(); y ++) {
			for(int z = 0; z < fileSchematic.getLength(); z ++) {
				for(int x = 0; x < fileSchematic.getWidth(); x ++) {
					this.schematic[x0 + x][y0 + y][z0 + z] = 
							(short) 
							((fileSchematic.getBlocks()[idx] & 0xff) |
							((fileSchematic.getData()[idx] & 0xff) << 8));
					idx ++;
				}
			}
		}
	}
}