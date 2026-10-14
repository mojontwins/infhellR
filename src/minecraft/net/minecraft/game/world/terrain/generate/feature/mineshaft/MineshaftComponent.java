package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.WeightedRandom;
import net.minecraft.game.WeightedRandomChoice;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.structure.StructurePieceTreasure;

/**
 * One piece (room, corridor, cross or stairwell) of a mineshaft.
 *
 * <p>This is the Feature-system counterpart of the vanilla {@code StructureComponent}.
 * The layout logic (placement, orientation bookkeeping, recursion) is kept identical,
 * but drawing is split across the two feature phases:
 *
 * <ul>
 *   <li>{@link #generate(MineshaftWriter, Random)} lays the <b>structural</b> blocks
 *       (floor, walls, cleared volumes, supports) during chunk generation - it must not
 *       depend on neighbouring chunks being loaded.</li>
 *   <li>{@link #populate(MineshaftWriter, Random)} adds the <b>world-dependent and
 *       special</b> blocks (arches vs. chains, rails, torches, cobwebs, chests and
 *       spawners) during the population stage, where the full world is available.</li>
 * </ul>
 *
 * <p>Both phases are driven by a per-piece {@link Random} seeded from the piece's
 * footprint, so a piece draws the exact same blocks no matter which chunk (or how many
 * times) it is asked to draw for.
 */
public abstract class MineshaftComponent {
	/** The world the shaft is being built in (available to pieces that need terrain queries at construction). */
	protected final World world;
	protected StructureBoundingBox boundingBox;
	protected int coordBaseMode = -1;
	protected int componentType;

	/**
	 * The variant factory that created this piece (regular or mesa). Used by
	 * {@link #buildComponent} to spawn children and by the decorative pass to read the
	 * variant's loot table. Set by {@link #setPieces} right after construction.
	 */
	protected MineshaftPieces pieces;

	protected MineshaftComponent(World world, int componentType) {
		this.world = world;
		this.componentType = componentType;
	}

	public StructureBoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public int getComponentType() {
		return this.componentType;
	}

	public void setPieces(MineshaftPieces pieces) {
		this.pieces = pieces;
	}

	/** Fraction of the piece footprint that must be opaque for it to count as "mostly solid". */
	public float getMostlySolidPercent() {
		return 0.6F;
	}

	/** Recursively grows the shaft by attaching child pieces. */
	public abstract void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand);

	/** Structural pass: runs during chunk generation, writes into the flat chunk buffers. */
	public abstract void generate(MineshaftWriter writer, Random rand);

	/** Decorative/special pass: runs during chunk population, writes into the world. */
	public void populate(MineshaftWriter writer, Random rand) {
	}

	public static MineshaftComponent findIntersecting(List<MineshaftComponent> components, StructureBoundingBox candidate) {
		Iterator<MineshaftComponent> iterator = components.iterator();
		MineshaftComponent component;
		do {
			if(!iterator.hasNext()) {
				return null;
			}
			component = iterator.next();
		} while(component.getBoundingBox() == null || !component.getBoundingBox().intersectsWith(candidate));
		return component;
	}

	// ------------------------------------------------------------------
	// Coordinate translation (copied verbatim from StructureComponent)
	// ------------------------------------------------------------------

	protected int getXWithOffset(int x, int z) {
		switch(this.coordBaseMode) {
		case 0:
		case 2:
			return this.boundingBox.minX + x;
		case 1:
			return this.boundingBox.maxX - z;
		case 3:
			return this.boundingBox.minX + z;
		default:
			return x;
		}
	}

	protected int getYWithOffset(int y) {
		return this.coordBaseMode == -1 ? y : y + this.boundingBox.minY;
	}

	protected int getZWithOffset(int x, int z) {
		switch(this.coordBaseMode) {
		case 0:
			return this.boundingBox.minZ + z;
		case 1:
		case 3:
			return this.boundingBox.minZ + x;
		case 2:
			return this.boundingBox.maxZ - z;
		default:
			return z;
		}
	}

	protected int getMetadataWithOffset(int blockID, int metadata) {
		if(blockID == Block.rail.blockID) {
			if(this.coordBaseMode == 1 || this.coordBaseMode == 3) {
				return metadata == 1 ? 0 : 1;
			}
		} else if(blockID != Block.doorWood.blockID && blockID != Block.doorSteel.blockID) {
			if(blockID == Block.stairCompactCobblestone.blockID || blockID == Block.stairCompactPlanks.blockID) {
				if(this.coordBaseMode == 0) {
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

	// ------------------------------------------------------------------
	// Block access
	// ------------------------------------------------------------------

	protected void placeBlock(MineshaftWriter writer, int blockID, int metadata, int x, int y, int z) {
		writer.setBlock(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z), blockID, metadata);
	}

	protected void placeBlockNoVertClip(MineshaftWriter writer, int blockID, int metadata, int x, int y, int z) {
		writer.setBlock(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z), blockID, metadata);
	}

	protected int getBlockID(MineshaftWriter writer, int x, int y, int z) {
		return writer.getBlockID(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
	}

	protected int getBlockIDNoClip(MineshaftWriter writer, int x, int y, int z) {
		return writer.getBlockIDNoClip(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
	}

	protected boolean canBlockSeeTheSky(MineshaftWriter writer, int x, int y, int z) {
		return writer.canBlockSeeTheSky(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
	}

	// ------------------------------------------------------------------
	// Fill helpers
	// ------------------------------------------------------------------

	protected void fill(MineshaftWriter writer, int x1, int y1, int z1, int x2, int y2, int z2, int blockID1, int blockID2, boolean onlyReplaceSolid) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplaceSolid || this.getBlockID(writer, x, y, z) != 0) {
						if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
							this.placeBlock(writer, blockID2, 0, x, y, z);
						} else {
							this.placeBlock(writer, blockID1, 0, x, y, z);
						}
					}
				}
			}
		}
	}

	protected void fillVariation(MineshaftWriter writer, int x1, int y1, int z1, int x2, int y2, int z2, int commonID, int rareID, Random rand, int rarity, boolean onlyReplaceSolid) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplaceSolid || this.getBlockID(writer, x, y, z) != 0) {
						this.placeBlock(writer, rand.nextInt(rarity) == 0 ? rareID : commonID, 0, x, y, z);
					}
				}
			}
		}
	}

	protected void randomFill(MineshaftWriter writer, Random rand, float chance, int x1, int y1, int z1, int x2, int y2, int z2, int blockID1, int blockID2, boolean onlyReplaceSolid) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(rand.nextFloat() <= chance && (!onlyReplaceSolid || this.getBlockID(writer, x, y, z) != 0)) {
						if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
							this.placeBlock(writer, blockID2, 0, x, y, z);
						} else {
							this.placeBlock(writer, blockID1, 0, x, y, z);
						}
					}
				}
			}
		}
	}

	protected void randomBlock(MineshaftWriter writer, Random rand, float chance, int x, int y, int z, int blockID, int metadata) {
		if(rand.nextFloat() < chance) {
			this.placeBlock(writer, blockID, metadata, x, y, z);
		}
	}

	protected void randomRareFill(MineshaftWriter writer, int x1, int y1, int z1, int x2, int y2, int z2, int blockID, boolean onlyReplaceSolid) {
		float f11 = (float)(x2 - x1 + 1);
		float f12 = (float)(y2 - y1 + 1);
		float f13 = (float)(z2 - z1 + 1);
		float cx = (float)x1 + f11 / 2.0F;
		float cz = (float)z1 + f13 / 2.0F;

		for(int y = y1; y <= y2; ++y) {
			float fy = (float)(y - y1) / f12;
			for(int x = x1; x <= x2; ++x) {
				float fx = ((float)x - cx) / (f11 * 0.5F);
				for(int z = z1; z <= z2; ++z) {
					float fz = ((float)z - cz) / (f13 * 0.5F);
					if(!onlyReplaceSolid || this.getBlockID(writer, x, y, z) != 0) {
						float dist = fx * fx + fy * fy + fz * fz;
						if(dist <= 1.05F) {
							this.placeBlock(writer, blockID, 0, x, y, z);
						}
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Special blocks (population phase only)
	// ------------------------------------------------------------------

	protected void createTreasureChest(MineshaftWriter writer, Random rand, int x, int y, int z, StructurePieceTreasure[] loot, int numberOfStacks) {
		int xAbs = this.getXWithOffset(x, z);
		int yAbs = this.getYWithOffset(y);
		int zAbs = this.getZWithOffset(x, z);
		if(!writer.isInChunk(xAbs, zAbs)) {
			return;
		}

		World world = writer.getWorld();
		if(world.getBlockId(xAbs, yAbs, zAbs) != Block.chest.blockID) {
			world.setBlockWithNotify(xAbs, yAbs, zAbs, Block.chest.blockID);
			TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(xAbs, yAbs, zAbs);
			if(chest != null) {
				fillTreasureChest(rand, loot, chest, numberOfStacks);
			}
		}
	}

	private static void fillTreasureChest(Random rand, StructurePieceTreasure[] loot, TileEntityChest chest, int numberOfStacks) {
		for(int i = 0; i < numberOfStacks; ++i) {
			StructurePieceTreasure treasure = (StructurePieceTreasure) WeightedRandom.getRandomItem(rand, (WeightedRandomChoice[]) loot);
			int count = treasure.minItemStack + rand.nextInt(treasure.maxItemStack - treasure.minItemStack + 1);
			if(Item.itemsList[treasure.itemID].getItemStackLimit() >= count) {
				chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), new ItemStack(treasure.itemID, count, treasure.itemMetadata));
			} else {
				for(int j = 0; j < count; ++j) {
					chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), new ItemStack(treasure.itemID, 1, treasure.itemMetadata));
				}
			}
		}
	}

	protected void placeSpawner(MineshaftWriter writer, int x, int y, int z, String mobID) {
		int xAbs = this.getXWithOffset(x, z);
		int yAbs = this.getYWithOffset(y);
		int zAbs = this.getZWithOffset(x, z);
		if(!writer.isInChunk(xAbs, zAbs)) {
			return;
		}

		World world = writer.getWorld();
		world.setBlockWithNotify(xAbs, yAbs, zAbs, Block.mobSpawner.blockID);
		TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getBlockTileEntity(xAbs, yAbs, zAbs);
		if(spawner != null) {
			spawner.setMobID(mobID);
		}
	}

	// ------------------------------------------------------------------
	// Environment queries (usable only in the population phase, which owns a world)
	//
	// These are the Feature-system port of the vanilla StructureComponent environment
	// helpers. The vanilla versions took the chunk bounding box explicitly; here the
	// chunk box is derived from the writer (see MineshaftWriter.getChunkBox) so the
	// piece only ever inspects / writes the chunk it is currently being drawn into.
	// ------------------------------------------------------------------

	/** Bounds of the piece's footprint clipped against the chunk box being drawn. */
	protected int environmentMinX(StructureBoundingBox chunkBox) {
		return Math.max(this.boundingBox.minX - 1, chunkBox.minX);
	}

	protected int environmentMinY(StructureBoundingBox chunkBox) {
		return Math.max(this.boundingBox.minY - 1, chunkBox.minY);
	}

	protected int environmentMinZ(StructureBoundingBox chunkBox) {
		return Math.max(this.boundingBox.minZ - 1, chunkBox.minZ);
	}

	protected int environmentMaxX(StructureBoundingBox chunkBox) {
		return Math.min(this.boundingBox.maxX + 1, chunkBox.maxX);
	}

	protected int environmentMaxY(StructureBoundingBox chunkBox) {
		return Math.min(this.boundingBox.maxY + 1, chunkBox.maxY);
	}

	protected int environmentMaxZ(StructureBoundingBox chunkBox) {
		return Math.min(this.boundingBox.maxZ + 1, chunkBox.maxZ);
	}

	/** True when any of the six "walls" of the piece region contains a liquid. */
	protected boolean isLiquidInChunk(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int x1 = this.environmentMinX(chunkBox);
		int y1 = this.environmentMinY(chunkBox);
		int z1 = this.environmentMinZ(chunkBox);
		int x2 = this.environmentMaxX(chunkBox);
		int y2 = this.environmentMaxY(chunkBox);
		int z2 = this.environmentMaxZ(chunkBox);

		for(int x = x1; x <= x2; ++x) {
			for(int z = z1; z <= z2; ++z) {
				if(isLiquid(world, x, y1, z) || isLiquid(world, x, y2, z)) {
					return true;
				}
			}
		}

		for(int x = x1; x <= x2; ++x) {
			for(int y = y1; y <= y2; ++y) {
				if(isLiquid(world, x, y, z1) || isLiquid(world, x, y, z2)) {
					return true;
				}
			}
		}

		for(int z = z1; z <= z2; ++z) {
			for(int y = y1; y <= y2; ++y) {
				if(isLiquid(world, x1, y, z) || isLiquid(world, x2, y, z)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean isLiquid(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y, z);
		return id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid();
	}

	/** Fraction of the piece region (clipped to the chunk) made of opaque cubes. */
	protected float getFractionSolid(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int solid = 0;
		int total = 0;
		for(int x = this.environmentMinX(chunkBox); x < this.environmentMaxX(chunkBox); ++x) {
			for(int y = this.environmentMinY(chunkBox); y < this.environmentMaxY(chunkBox); ++y) {
				for(int z = this.environmentMinZ(chunkBox); z < this.environmentMaxZ(chunkBox); ++z) {
					++total;
					if(world.isBlockOpaqueCube(x, y, z)) {
						++solid;
					}
				}
			}
		}
		return (float) solid / (float) total;
	}

	/** Whether the piece region is mostly opaque (used by the mesa's solid-ground gate). */
	protected boolean isMostlySolid(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int solid = 0;
		int total = 0;
		for(int x = this.environmentMinX(chunkBox); x < this.environmentMaxX(chunkBox); ++x) {
			for(int y = this.environmentMinY(chunkBox); y < this.environmentMaxY(chunkBox); ++y) {
				for(int z = this.environmentMinZ(chunkBox); z < this.environmentMaxZ(chunkBox); ++z) {
					++total;
					if(world.isBlockOpaqueCube(x, y, z)) {
						++solid;
					}
				}
			}
		}
		return solid > (float) total * this.getMostlySolidPercent();
	}

	/** Fraction of the piece footprint (clipped to the chunk) whose top column sees the sky. */
	protected float getFractionExposed(MineshaftWriter writer, StructureBoundingBox chunkBox) {
		World world = writer.getWorld();
		int x1 = this.environmentMinX(chunkBox);
		int z1 = this.environmentMinZ(chunkBox);
		int x2 = this.environmentMaxX(chunkBox);
		int z2 = this.environmentMaxZ(chunkBox);
		int y = this.environmentMaxY(chunkBox);

		int exposed = 0;
		int total = 0;
		for(int x = x1; x < x2; ++x) {
			for(int z = z1; z < z2; ++z) {
				++total;
				if(world.canBlockSeeTheSky(x, y, z)) {
					++exposed;
				}
			}
		}
		return (float) exposed / (float) total;
	}

	/**
	 * Clears the given region, but only where the existing block is natural rock/clay -
	 * the mesa shaft carves its tunnel through solid ground instead of through air.
	 */
	protected void fillIfRock(MineshaftWriter writer, StructureBoundingBox chunkBox, int x1, int y1, int z1, int x2, int y2, int z2, int blockID1, int blockID2, boolean onlyReplaceSolid) {
		World world = writer.getWorld();
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					Material material = this.getBlockMaterial(writer, x, y, z, chunkBox);
					if(material == Material.rock || material == Material.clay) {
						if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
							this.placeBlock(writer, blockID2, 0, x, y, z);
						} else {
							this.placeBlock(writer, blockID1, 0, x, y, z);
						}
					}
				}
			}
		}
	}

	/** Reads the material of a cell, reporting air outside the chunk box (matching vanilla). */
	protected Material getBlockMaterial(MineshaftWriter writer, int x, int y, int z, StructureBoundingBox chunkBox) {
		int xAbs = this.getXWithOffset(x, z);
		int yAbs = this.getYWithOffset(y);
		int zAbs = this.getZWithOffset(x, z);
		if(!chunkBox.isVecInside(xAbs, yAbs, zAbs)) {
			return Material.air;
		}
		return writer.getWorld().getBlockMaterial(xAbs, yAbs, zAbs);
	}

	/** Places a block only when one of its six neighbours is non-air (mesa web placement). */
	protected void placeBlockIfNextToSolid(MineshaftWriter writer, StructureBoundingBox chunkBox, int blockID, int metadata, int x, int y, int z) {
		int xAbs = this.getXWithOffset(x, z);
		int yAbs = this.getYWithOffset(y);
		int zAbs = this.getZWithOffset(x, z);
		if(!chunkBox.isVecInside(xAbs, yAbs, zAbs)) {
			return;
		}

		World world = writer.getWorld();
		if(world.getBlockId(xAbs - 1, yAbs, zAbs) != 0
				|| world.getBlockId(xAbs + 1, yAbs, zAbs) != 0
				|| world.getBlockId(xAbs, yAbs, zAbs - 1) != 0
				|| world.getBlockId(xAbs, yAbs, zAbs + 1) != 0
				|| world.getBlockId(xAbs, yAbs - 1, zAbs) != 0
				|| world.getBlockId(xAbs, yAbs + 1, zAbs) != 0) {
			writer.setBlock(xAbs, yAbs, zAbs, blockID, metadata);
		}
	}

	protected void randomBlockIfNextToSolid(MineshaftWriter writer, StructureBoundingBox chunkBox, Random rand, float chance, int x, int y, int z, int blockID, int metadata) {
		if(rand.nextFloat() < chance) {
			this.placeBlockIfNextToSolid(writer, chunkBox, blockID, metadata, x, y, z);
		}
	}
}
