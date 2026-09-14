package net.minecraft.game.world.terrain.generate.feature.icepalace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.biome.BiomeGenTaiga;
import net.minecraft.game.world.biome.BiomeGenTundra;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.FeatureDynamicSchematic;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;

public class FeatureIcePalace extends FeatureDynamicSchematic {
	private static final int palaceHeight = 64;
	private List<FeatureAABB> pieceAABBs = new ArrayList<FeatureAABB> ();
	private List<PalacePiecePosition> corridorPositions = new ArrayList<PalacePiecePosition> ();
	private List<PalacePiecePosition> topPositions = new ArrayList<PalacePiecePosition> ();
	private Random pieceRand;
	private int y0 = 64;
	private int highestRoomY = 0;
	
	private static final int towerSeparation = 6;
	
	private static final Class<?>[] selectablePieces = new Class<?>[] {
		PalacePieceRoom1.class,
		PalacePieceRoom2.class,
		PalacePieceRoom3.class,
		PalacePieceRoom4.class,
		PalacePieceRoom5.class,
		PalacePieceRoom6.class,
		PalacePieceGarden.class,
		PalacePieceLibrary.class,
		PalacePieceJail.class
	};

	public FeatureIcePalace(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		if(this.featureProvider.chunkProvider instanceof ChunkProviderSky) {
			int height = this.world.getLandSurfaceHeightValue(this.centerX, this.centerZ);
			if(height < 1 || height > 64) return;
			
			this.y0 = height;
		}
		
		super.setup(world, rand, biome, chunkX, chunkZ);
	}

	@Override
	public int getFeatureHeight() {
		return palaceHeight;
	}

	@Override
	public void generateSchematic(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.pieceRand = new Random(world.getRandomSeed() + chunkX * 25117 + chunkZ * 151121);
		
		// Fill the whole schematic with -1s
		this.fillSchematic((short)-1);
		
		// Start in the center
		int cx = this.schematic.length >> 1;
		int cz = this.schematic[0].length >> 1;
		int cy = 0;
		
		// Make a half spherical hole
		int radius = 24;
		int radiusSq = radius * radius;
		for(int x = 0; x <= radius; x ++) {
			int xx = x * x;
			for(int z = 0; z <= radius; z++) {
				int zz = z * z;
				for(int y = 0; y <= radius; y ++) {
					if(xx + zz + y * y < radiusSq) {
						this.schematic[cx + x][cz + z][radius - y] = 0;
						this.schematic[cx - x][cz + z][radius - y] = 0;
						this.schematic[cx + x][cz - z][radius - y] = 0;
						this.schematic[cx - x][cz - z][radius - y] = 0;
					}
				}
			}
		}
		
		// Create base, place and store aabb
		PalacePiece base = new PalacePieceBase(false, this);
		base.place(cx, cy, cz);
		this.pieceAABBs.add(base.aabb);
		base.drawPiece(schematic);
		
		// Start generating on top of base
		cy += base.height;
		this.generateSchematicRecursive(cx, cy, cz, this.getNewPiece(this.pieceRand));
		
		// Add tower top. 
		// First find the toppest top: this will become the boss chamber.
		int toppestIndex = 0;
		int toppestY = 0;
		for(int i = 0; i < this.topPositions.size(); i ++) {
			if(this.topPositions.get(i).y > toppestY) {
				toppestY = this.topPositions.get(i).y;
				toppestIndex = i;
			}
		}
		
		// Draw all top pieces
		for(int i = 0; i < this.topPositions.size(); i ++) {
			PalacePiecePosition topPosition = this.topPositions.get(i);
			PalacePiece top = (i == toppestIndex) ? new PalacePieceBossChamber(topPosition.rotated, this) : new PalacePieceNormalTop(topPosition.rotated, this);
			top.place(topPosition.x, topPosition.y, topPosition.z);
			top.drawPiece(schematic);
		}
		
		// Add corridors
		Iterator<PalacePiecePosition> iterator = this.corridorPositions.iterator();
		while(iterator.hasNext()) {
			PalacePiecePosition corridorPosition = iterator.next();
			PalacePiece corridor = new PalacePieceCorridor(corridorPosition.rotated, this);
			corridor.place(corridorPosition.x, corridorPosition.y, corridorPosition.z);
			corridor.drawPiece(schematic);
		}
		
		System.out.println ("Palace @ " + this.centerX + " " + this.centerZ);
	}
	
	private void generateSchematicRecursive(int x, int y, int z, PalacePiece newPiece) {
		if(y > this.highestRoomY) this.highestRoomY = y;
		
		// First find if this tower can grow up or we must register a top
		PalacePiece nextPiece = this.getNewPiece(pieceRand);
		
		if(y + newPiece.height + nextPiece.height <= 56 && this.pieceRand.nextInt(6) != 0) {
			// Spawn new piece up: would next piece fit?
			
			nextPiece.place(x, y + newPiece.height, z);
			if(!this.collideWithAny(nextPiece.aabb)) {
				
				// Draw this piece
				newPiece.place(x, y, z);
				this.pieceAABBs.add(newPiece.aabb);
				newPiece.drawPiece(schematic);
				
				// Recurse up
				this.generateSchematicRecursive(x, y + newPiece.height, z, nextPiece);
				
				// Spawn new pieces sideways. This assumes the normal tower radius is 4
				for(int i = 0; i < 2; i ++) {
					PalacePiece piece = this.getNewPiece(this.pieceRand);
					int newPieceChance = this.pieceRand.nextInt(6);
					PalacePiecePosition PalacePiecePosition;
					
					switch(newPieceChance) {
					case 1:
						// Spawn +x;
						if (this.attemptToPlaceAndRecurr(x + 4 + towerSeparation + 4, y, z, piece)) {	
							// Add connection +X
							PalacePiecePosition = new PalacePiecePosition(x + 4 + (towerSeparation >> 1), y - 1, z, false);
							this.corridorPositions.add(PalacePiecePosition);
						}
						break;
					case 2:
						// Spawn -x
						if(this.attemptToPlaceAndRecurr(x - 4 - towerSeparation - (piece.width >> 1), y, z, piece)) {				
							// Add connection -x
							PalacePiecePosition = new PalacePiecePosition(x - 4 - (towerSeparation >> 1), y - 1, z, false);
							this.corridorPositions.add(PalacePiecePosition);
						}
						break;
					case 3:
						// Spawn +z;
						if(this.attemptToPlaceAndRecurr(x, y, z + 4 + towerSeparation + 4, piece)) {
							// Add connection +z
							PalacePiecePosition = new PalacePiecePosition(x, y - 1, z + 4 + (towerSeparation >> 1), true);
							this.corridorPositions.add(PalacePiecePosition);
						}
						break;
					case 4:
						// Spawn -z;
						if(this.attemptToPlaceAndRecurr(x, y, z - 4 - towerSeparation - 4, piece)) {
							// Add connection -z
							PalacePiecePosition = new PalacePiecePosition(x, y - 1, z - 4 - (towerSeparation >> 1), true);
							this.corridorPositions.add(PalacePiecePosition);
						}
						break;
					}
				}
				
				// Done
				return;
			}
		} 
		
		// Everything failed, so this becomes a tower top.
		
		// We'll store the top position and also preemptively add a 11x7x11 aabb to the list
		// But we are not drawing anything *at this stage*.
		newPiece = new PalacePieceNormalTop(false, this);
		newPiece.place(x, y, z);
		this.pieceAABBs.add(newPiece.aabb);
		this.topPositions.add(new PalacePiecePosition(x, y, z, false));
	}
	
	private boolean attemptToPlaceAndRecurr(int x, int y, int z, PalacePiece piece) {
		piece.place(x, y, z);
		if(!this.collideWithAny(piece.aabb) && this.aabb.containsFully(piece.aabb)) {
			// Generate a new tower.
			this.generateSchematicRecursive(x, y, z, piece);
			
			// Add bottom of the new tower.
			PalacePiece underPiece = new PalacePieceUnderTower(true, this);
			underPiece.place(x, y - 7, z);
			this.pieceAABBs.add(underPiece.aabb);
			underPiece.drawPiece(this.schematic);
			
			return true;
		}
		return false;
	}
	
	private boolean collideWithAny(FeatureAABB aabb) {
		Iterator<FeatureAABB> iterator = this.pieceAABBs.iterator();
		while(iterator.hasNext()) {
			if(iterator.next().collidesWith(aabb)) return true;
		}
		
		return false;
	}
	
	private PalacePiece getNewPiece(Random rand) {
		PalacePiece piece = null;
		try {
			boolean rotated = rand.nextBoolean();
			piece = (PalacePiece) selectablePieces[rand.nextInt(selectablePieces.length)].getConstructor(new Class[] {Boolean.TYPE, FeatureIcePalace.class}).newInstance(new Object[] {rotated, this});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return piece;
	}

	@Override
	public int getY0() {
		return this.y0;
	}

	@Override
	public int getFeatureRadius() {
		return 2;
	}

	@Override
	public int getSpawnChance() {
		return 16;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX,
			int chunkZ) {
		return biome instanceof BiomeGenTundra || biome instanceof BiomeGenGlacier || (biome instanceof BiomeGenTaiga && world.isOceanChunk(chunkX, chunkZ));
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		this.drawPieceOnGeneration(chunkX, chunkZ, chunk);
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		this.drawSpecialBlocksForChunk(world, rand, chunkX, chunkZ);
	}
	
	@Override
	public void onSpecialBlockSet(int x, int y, int z, int id, int meta) {
		switch(id) {
		case 52:
			TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)this.world.getBlockTileEntity(x, y, z);
			if(mobSpawner != null) {
				mobSpawner.setMobID("Ice"); 
			
				int level = 1 + ((this.highestRoomY - 64) * y / 4);
				if(level > 4) level = 4;
				
				this.world.setBlockMetadata(x, y, z, 8 | level);
			}
			break;
		case 54:
			TileEntityChest chest = (TileEntityChest)this.world.getBlockTileEntity(x, y, z);
			if(chest != null && chest.getSizeInventory() > 0) {
				int ni = 4 + this.world.rand.nextInt(4);
				int level = this.world.rand.nextInt(10);
				
				for(int i = 0; i < ni; ++i) {
					ItemStack itemStack = this.getTreasure(level, this.world.rand);
					chest.setInventorySlotContents(i, itemStack);
				}			
			}
			if(this.world.isAirBlock(x, y - 1, z)) {
				this.world.setBlock(x, y - 1, z, Block.planks.blockID);
			}
			break;
		case 151:
			// Boss!
			TileEntityMobSpawnerOneshot bossSpawner = (TileEntityMobSpawnerOneshot)this.world.getBlockTileEntity(x, y, z);
			if(bossSpawner != null) {
				bossSpawner.setMobID("IceBoss");
				
				this.world.setBlockMetadata(x, y, z, 4);
			}
			break;
		}
	}
	
	// Return a possible ice treasure.
	protected ItemStack getTreasure(int level, Random rand) {
		// level should be rand 0-9
		
		if(level < 4) {
			switch(rand.nextInt(6)) {
			case 0:
				return new ItemStack(Item.helmetRags, rand.nextInt(4) + 1);
			case 1:
				return new ItemStack(Item.bucketEmpty);
			case 2:
				return new ItemStack(Item.bread);
			case 3:
				return new ItemStack(Item.saddle);
			case 5:
			default:
				return new ItemStack(Block.torchWood, rand.nextInt(16) + 1);
			case 4:
				return new ItemStack(Item.wheat, rand.nextInt(3) + 1);
			}
		} else if(level < 9) {
			switch(rand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getTreasure(1, rand);
			case 5:
				return new ItemStack(Item.plateRags);
			case 6:
				return new ItemStack(Item.swordSteel);
			case 7:
				return new ItemStack(Item.legsRags);
			default:
				return new ItemStack(Item.swordGold);
			}
		} else {
			switch(rand.nextInt(8)) {
			case 0:
				return new ItemStack(Item.maceDiamond);
			case 1:
				return new ItemStack(Item.nametagSimple);
			case 2:
				return this.getTreasure(2, rand);
			case 3:
				return new ItemStack(Item.maceSteel);
			case 7:
			default:
				return new ItemStack(Item.diamond);
			case 4:
				return new ItemStack(Item.appleGold);
			case 5:
				return new ItemStack(Item.diamond);
			case 6:
				return new ItemStack(Item.ruby);
			}
		} 
	}
	
	@Override
	public int minimumSeparation() {
		return 8;
	}

}
