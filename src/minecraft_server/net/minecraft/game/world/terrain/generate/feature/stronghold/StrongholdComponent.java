package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.feature.StructurePieceBlockSelector;
import net.minecraft.game.world.terrain.generate.feature.StructurePieceTreasure;

/**
 * One piece (corridor, room, crossing, stairwell, library, ...) of a stronghold.
 *
 * <p>This is the Feature-system counterpart of the vanilla {@code ComponentStronghold}.
 * It reuses the mineshaft API - the {@link MineshaftWriter} block layer and every generic
 * helper on {@link MineshaftComponent} (fills, coordinate translation, chests, environment
 * probes) - and adds only the stronghold-specific bits: the stone-brick block selector
 * fill, the doorways, the air-ratio gates and the recursion into {@link StrongholdPieces}.
 *
 * <p>Unlike the mineshaft (which lays most of its blocks straight into the chunk's flat
 * generation buffers), a stronghold is entirely world-dependent: every piece decides
 * whether to draw at all from the world around it, so ALL drawing happens during the
 * population phase. {@link #addComponentParts} mirrors the vanilla gate - returning
 * {@code false} disables the piece (vanilla removed it from the structure start).
 */
public abstract class StrongholdComponent extends MineshaftComponent {
	/** The selection factory that built this piece (weighted table + recursion entry point). */
	protected StrongholdPieces factory;

	/** Set once {@link #addComponentParts} has vetoed the piece, so it is never retried. */
	private boolean disabled;

	protected StrongholdComponent(World world, int componentType) {
		super(world, componentType);
	}

	public void setFactory(StrongholdPieces factory) {
		this.factory = factory;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	/** A stronghold piece writes nothing during the generation phase; see the class comment. */
	@Override
	public void generate(MineshaftWriter writer, Random rand) {
	}

	@Override
	public void populate(MineshaftWriter writer, Random rand) {
		if(this.disabled) {
			return;
		}
		StructureBoundingBox chunkBox = writer.getChunkBox();
		if(this.boundingBox == null || !this.boundingBox.intersectsWith(chunkBox)) {
			return;
		}
		if(!this.addComponentParts(writer, rand, chunkBox)) {
			this.disabled = true;
		}
	}

	/**
	 * Draws this piece into the world. Returns false to disable the piece entirely
	 * (matching the vanilla {@code StructureStart.generateStructure} removal).
	 */
	protected abstract boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox);

	// ------------------------------------------------------------------
	// Block substitution gate
	//
	// The piece draws its masonry first and its doors, openings and decor on top of it,
	// so every write goes through canReplaceBlock: air and opted-in terrain may be
	// written, the stronghold's own blocks may be rewritten, everything else - tree
	// trunks and leaves overflowing from neighbouring chunks, player builds - is kept.
	// ------------------------------------------------------------------

	/** The blocks a stronghold piece places itself. */
	private static final int[] ownBlockIDs = new int[] {
		StrongholdPieces.BRICKS_ID,
		StrongholdPieces.BRICKS_ALT1_ID,
		StrongholdPieces.BRICKS_ALT2_ID,
		StrongholdPieces.IRON_FENCE_ID,
		Block.planks.blockID,
		Block.bookShelf.blockID,
		Block.fence.blockID,
		Block.ladder.blockID,
		Block.torchWood.blockID,
		Block.stairSingle.blockID,
		Block.stairDouble.blockID,
		Block.stairCompactCobblestone.blockID,
		Block.stairCompactPlanks.blockID,
		Block.rail.blockID,
		Block.web.blockID,
		Block.doorWood.blockID,
		Block.doorSteel.blockID,
		Block.button.blockID
	};

	private static boolean isOwnBlock(int blockID) {
		for(int i = 0; i < ownBlockIDs.length; ++i) {
			if(ownBlockIDs[i] == blockID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether this piece may substitute whatever currently sits in the given piece-local
	 * cell. Air is always writable (nothing is substituted); any other block must have
	 * opted in through {@link Block#replaceableByFeature} or be part of the stronghold's
	 * own palette, so foreign blocks - above all the tree blocks that overflow into the
	 * chunk from its neighbours - are never overwritten.
	 */
	protected boolean canReplaceBlock(MineshaftWriter writer, int x, int y, int z) {
		Block existingBlock = Block.blocksList[this.getBlockID(writer, x, y, z)];
		return existingBlock == null || existingBlock.replaceableByFeature || isOwnBlock(existingBlock.blockID);
	}

	@Override
	protected void placeBlock(MineshaftWriter writer, int blockID, int metadata, int x, int y, int z) {
		if(this.canReplaceBlock(writer, x, y, z)) {
			super.placeBlock(writer, blockID, metadata, x, y, z);
		}
	}

	@Override
	protected void createTreasureChest(MineshaftWriter writer, Random rand, int x, int y, int z, StructurePieceTreasure[] loot, int numberOfStacks) {
		if(!this.canReplaceBlock(writer, x, y, z)) {
			return;
		}
		super.createTreasureChest(writer, rand, x, y, z, loot, numberOfStacks);
	}

	@Override
	protected void placeSpawner(MineshaftWriter writer, int x, int y, int z, String mobID) {
		if(!this.canReplaceBlock(writer, x, y, z)) {
			return;
		}
		super.placeSpawner(writer, x, y, z, mobID);
	}

	// ------------------------------------------------------------------
	// Stronghold fill helper (built on the inherited placeBlock / getBlockID)
	// ------------------------------------------------------------------

	/** Fills with a stone selector: boundary cells are bricks, interior cells vary. */
	protected void fillWithRandomizedBlocks(MineshaftWriter writer, StructureBoundingBox chunkBox, int x1, int y1, int z1, int x2, int y2, int z2, boolean onlyReplace, Random rand, StructurePieceBlockSelector selector) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplace || this.getBlockID(writer, x, y, z) != 0) {
						selector.selectBlocks(rand, x, y, z, y == y1 || y == y2 || x == x1 || x == x2 || z == z1 || z == z2);
						this.placeBlock(writer, selector.getSelectedBlockId(), selector.getSelectedBlockMetaData(), x, y, z);
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Mob spawners
	// ------------------------------------------------------------------

	/** The hostile mobs a stronghold spawner may be set to. */
	protected static final String[] SPAWNER_MOBS = new String[] {"Zombie", "Skeleton", "Spider"};

	/**
	 * A deterministic RNG for a piece's special blocks (spawners), seeded from the piece
	 * footprint. It must NOT reuse the piece Random: the block-fill passes above draw a
	 * number of values that depends on which chunk is being drawn (their solid-block gate
	 * is clipped to that chunk), so reusing it would let a spawner land on different spots
	 * - or spawn more than once - across the chunks the piece spans.
	 */
	protected Random specialRandom() {
		StructureBoundingBox box = this.boundingBox;
		long seed = 0x9E3779B97F4A7C15L;
		seed = seed * 31L + box.minX;
		seed = seed * 31L + box.minY;
		seed = seed * 31L + box.minZ;
		seed = seed * 31L + box.maxX;
		seed = seed * 31L + box.maxY;
		seed = seed * 31L + box.maxZ;
		return new Random(seed);
	}

	/** Places a mob spawner set to a random Zombie / Skeleton / Spider. */
	protected void placeRandomMobSpawner(MineshaftWriter writer, Random rand, int x, int y, int z) {
		this.placeSpawner(writer, x, y, z, SPAWNER_MOBS[rand.nextInt(SPAWNER_MOBS.length)]);
	}

	// ------------------------------------------------------------------
	// Environment gates (population phase only)
	//
	// Both clip the piece footprint against the current chunk box exactly like the
	// vanilla StructureComponent helpers (which received the chunk box explicitly).
	// ------------------------------------------------------------------

	/** Fraction of non-opaque blocks on the row immediately below the piece footprint. */
	protected float getAirRatioBelow(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int x1 = this.environmentMinX(chunkBox);
		int z1 = this.environmentMinZ(chunkBox);
		int x2 = this.environmentMaxX(chunkBox);
		int z2 = this.environmentMaxZ(chunkBox);
		int y = Math.max(this.boundingBox.minY - 1, chunkBox.minY);

		int total = 0;
		int nonOpaque = 0;
		for(int x = x1; x <= x2; ++x) {
			for(int z = z1; z <= z2; ++z) {
				Block block = Block.blocksList[world.getBlockId(x, y, z)];
				if(block == null || !block.isOpaqueCube()) {
					++nonOpaque;
				}
				++total;
			}
		}
		return (float) nonOpaque / (float) total;
	}

	/** Fraction of air in the six "walls" bounding the piece footprint. */
	protected float getAirRatioInWalls(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int x1 = this.environmentMinX(chunkBox);
		int y1 = this.environmentMinY(chunkBox);
		int z1 = this.environmentMinZ(chunkBox);
		int x2 = this.environmentMaxX(chunkBox);
		int y2 = this.environmentMaxY(chunkBox);
		int z2 = this.environmentMaxZ(chunkBox);

		int total = 0;
		int air = 0;
		int x;
		int y;
		int z;
		for(x = x1; x <= x2; ++x) {
			for(z = z1; z <= z2; ++z) {
				if(world.getBlockId(x, y1, z) == 0) ++air;
				++total;
				if(world.getBlockId(x, y2, z) == 0) ++air;
				++total;
			}
		}
		for(x = x1; x <= x2; ++x) {
			for(y = y1; y <= y2; ++y) {
				if(world.getBlockId(x, y, z1) == 0) ++air;
				++total;
				if(world.getBlockId(x, y, z2) == 0) ++air;
				++total;
			}
		}
		for(z = z1; z <= z2; ++z) {
			for(y = y1; y <= y2; ++y) {
				if(world.getBlockId(x1, y, z) == 0) ++air;
				++total;
				if(world.getBlockId(x2, y, z) == 0) ++air;
				++total;
			}
		}
		return (float) air / (float) total;
	}

	// ------------------------------------------------------------------
	// Metadata / doors / recursion (ported from ComponentStronghold + StructureComponent)
	// ------------------------------------------------------------------

	@Override
	protected int getMetadataWithOffset(int blockID, int metadata) {
		if(blockID == Block.rail.blockID) {
			if(this.coordBaseMode == 1 || this.coordBaseMode == 3) {
				return metadata == 1 ? 0 : 1;
			}
		} else if(blockID != Block.doorWood.blockID && blockID != Block.doorSteel.blockID) {
			if(blockID != Block.stairCompactCobblestone.blockID && blockID != Block.stairCompactPlanks.blockID) {
				if(blockID == Block.ladder.blockID) {
					if(this.coordBaseMode == 0) {
						if(metadata == 2) return 3;
						if(metadata == 3) return 2;
					} else if(this.coordBaseMode == 1) {
						if(metadata == 2) return 4;
						if(metadata == 3) return 5;
						if(metadata == 4) return 2;
						if(metadata == 5) return 3;
					} else if(this.coordBaseMode == 3) {
						if(metadata == 2) return 5;
						if(metadata == 3) return 4;
						if(metadata == 4) return 2;
						if(metadata == 5) return 3;
					}
				} else if(blockID == Block.button.blockID) {
					if(this.coordBaseMode == 0) {
						if(metadata == 3) return 4;
						if(metadata == 4) return 3;
					} else if(this.coordBaseMode == 1) {
						if(metadata == 3) return 1;
						if(metadata == 4) return 2;
						if(metadata == 2) return 3;
						if(metadata == 1) return 4;
					} else if(this.coordBaseMode == 3) {
						if(metadata == 3) return 2;
						if(metadata == 4) return 1;
						if(metadata == 2) return 3;
						if(metadata == 1) return 4;
					}
				}
			} else if(this.coordBaseMode == 0) {
				if(metadata == 2) return 3;
				if(metadata == 3) return 2;
			} else if(this.coordBaseMode == 1) {
				if(metadata == 0) return 2;
				if(metadata == 1) return 3;
				if(metadata == 2) return 0;
				if(metadata == 3) return 1;
			} else if(this.coordBaseMode == 3) {
				if(metadata == 0) return 2;
				if(metadata == 1) return 3;
				if(metadata == 2) return 1;
				if(metadata == 3) return 0;
			}
		} else if(this.coordBaseMode == 0) {
			if(metadata == 0) return 2;
			if(metadata == 2) return 0;
		} else if(this.coordBaseMode == 1) {
			return metadata + 1 & 3;
		} else if(this.coordBaseMode == 3) {
			return metadata + 3 & 3;
		}
		return metadata;
	}

	protected StrongholdDoor getRandomDoor(Random rand) {
		switch(rand.nextInt(5)) {
		case 0:
		case 1:
		default:
			return StrongholdDoor.OPENING;
		case 2:
			return StrongholdDoor.WOOD_DOOR;
		case 3:
			return StrongholdDoor.GRATES;
		case 4:
			return StrongholdDoor.IRON_DOOR;
		}
	}

	/**
	 * Carves a doorway into the wall of this piece. Like the vanilla version it bails out
	 * unless the row below the door is solid and the row three above is not fully open.
	 */
	protected void placeDoor(MineshaftWriter writer, Random rand, StrongholdDoor door, int x, int y, int z) {
		if(this.getBlockID(writer, x, y - 1, z) == 0
				|| this.getBlockID(writer, x + 1, y - 1, z) == 0
				|| this.getBlockID(writer, x + 2, y - 1, z) == 0) {
			return;
		}
		if(this.getBlockID(writer, x, y + 3, z) == 0
				&& this.getBlockID(writer, x + 1, y + 3, z) == 0
				&& this.getBlockID(writer, x + 2, y + 3, z) == 0) {
			return;
		}

		switch(door) {
		case WOOD_DOOR:
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 1, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y, z);
			this.placeBlock(writer, Block.doorWood.blockID, 0, x + 1, y, z);
			this.placeBlock(writer, Block.doorWood.blockID, 8, x + 1, y + 1, z);
			break;
		case GRATES:
			this.placeBlock(writer, 0, 0, x + 1, y, z);
			this.placeBlock(writer, 0, 0, x + 1, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x, y, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x + 1, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x + 2, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x + 2, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, x + 2, y, z);
			break;
		case IRON_DOOR:
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 1, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y + 2, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y + 1, z);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, x + 2, y, z);
			this.placeBlock(writer, Block.doorSteel.blockID, 0, x + 1, y, z);
			this.placeBlock(writer, Block.doorSteel.blockID, 8, x + 1, y + 1, z);
			this.placeBlock(writer, Block.button.blockID, this.getMetadataWithOffset(Block.button.blockID, 4), x + 2, y + 1, z + 1);
			this.placeBlock(writer, Block.button.blockID, this.getMetadataWithOffset(Block.button.blockID, 3), x + 2, y + 1, z - 1);
			break;
		case OPENING:
		default:
			this.fill(writer, x, y, z, x + 2, y + 2, z, 0, 0, false);
			break;
		}
	}

	protected StrongholdComponent getNextComponentNormal(StrongholdComponentStairs2 start, List<MineshaftComponent> components, Random rand, int offsetX, int offsetY) {
		switch(this.coordBaseMode) {
		case 0:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, this.coordBaseMode, this.getComponentType());
		case 1:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType());
		case 2:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, this.coordBaseMode, this.getComponentType());
		case 3:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType());
		default:
			return null;
		}
	}

	protected StrongholdComponent getNextComponentX(StrongholdComponentStairs2 start, List<MineshaftComponent> components, Random rand, int offsetY, int offsetZ) {
		switch(this.coordBaseMode) {
		case 0:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, 1, this.getComponentType());
		case 1:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, 2, this.getComponentType());
		case 2:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, 1, this.getComponentType());
		case 3:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, 2, this.getComponentType());
		default:
			return null;
		}
	}

	protected StrongholdComponent getNextComponentZ(StrongholdComponentStairs2 start, List<MineshaftComponent> components, Random rand, int offsetY, int offsetZ) {
		switch(this.coordBaseMode) {
		case 0:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, 3, this.getComponentType());
		case 1:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, 0, this.getComponentType());
		case 2:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, 3, this.getComponentType());
		case 3:
			return this.factory.getNextValidComponent(start, components, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, 0, this.getComponentType());
		default:
			return null;
		}
	}

	protected static boolean canStrongholdGoDeeper(StructureBoundingBox box) {
		return box != null && box.minY > 10;
	}
}
