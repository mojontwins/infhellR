package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.item.ItemDoor;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;

/**
 * One room / corridor / stairwell of a {@link FeatureStructure}.
 *
 * <p>A piece owns a box in world coordinates ({@link #box}) plus an orientation
 * ({@link #coordBaseMode}, 0..3). It knows how to draw itself into the world limited to
 * a chunk-clip box ({@link #generate}) and how to attach its neighbours
 * ({@link #buildConnections}).
 *
 * <p>All coordinates passed to the placement helpers are LOCAL to the piece (0-based
 * relative to the box); the helpers translate them through {@link #getXWithOffset} and
 * friends and only write when the resulting world position falls inside the clip box.
 * That is what keeps a multi-chunk structure from ever writing into un-generated
 * neighbours.
 */
public abstract class StructurePiece {
	protected FeatureAABB box;
	protected int coordBaseMode;
	protected int pieceType;
	protected FeatureStructure owner;

	protected StructurePiece(int pieceType) {
		this.pieceType = pieceType;
		this.coordBaseMode = -1;
	}

	public void setOwner(FeatureStructure owner) {
		this.owner = owner;
	}

	public FeatureStructure getOwner() {
		return this.owner;
	}

	public FeatureAABB getBox() {
		return this.box;
	}

	public int getPieceType() {
		return this.pieceType;
	}

	public float getMostlySolidPercent() {
		return 0.6F;
	}

	/**
	 * Attaches child pieces by calling back into {@link FeatureStructure#requestNextPiece}.
	 * {@code parent} is the piece that spawned this one (may be {@code null} for the root).
	 */
	public abstract void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand);

	/**
	 * Draws the parts of this piece that fall inside {@code chunkBox}.
	 *
	 * @return true to keep the piece, false to discard it (e.g. it sits in liquid)
	 */
	public abstract boolean generate(World world, Random rand, FeatureAABB chunkBox, boolean mostlySolid);

	public static StructurePiece findIntersecting(List<StructurePiece> pieces, FeatureAABB box) {
		Iterator<StructurePiece> iterator = pieces.iterator();
		StructurePiece piece;
		do {
			if(!iterator.hasNext()) {
				return null;
			}
			piece = iterator.next();
		} while(piece.getBox() == null || !piece.getBox().collidesWith(box));

		return piece;
	}

	/* ------------------------------------------------------------------ *
	 *  Local -> world coordinate transforms
	 * ------------------------------------------------------------------ */

	protected int getXWithOffset(int x, int z) {
		switch(this.coordBaseMode) {
		case 0:
		case 2:
			return this.box.x1 + x;
		case 1:
			return this.box.x2 - z;
		case 3:
			return this.box.x1 + z;
		default:
			return x;
		}
	}

	protected int getYWithOffset(int y) {
		return this.coordBaseMode == -1 ? y : y + this.box.y1;
	}

	protected int getZWithOffset(int x, int z) {
		switch(this.coordBaseMode) {
		case 0:
			return this.box.z1 + z;
		case 1:
		case 3:
			return this.box.z1 + x;
		case 2:
			return this.box.z2 - z;
		default:
			return z;
		}
	}

	/** Rotates a metadata value (rails, doors, stairs, ladders, buttons) for this piece. */
	protected int getMetadataWithOffset(int blockID, int meta) {
		if(blockID == Block.rail.blockID) {
			if(this.coordBaseMode == 1 || this.coordBaseMode == 3) {
				return meta == 1 ? 0 : 1;
			}
		} else if(blockID != Block.doorWood.blockID && blockID != Block.doorSteel.blockID) {
			if(blockID != Block.stairCompactCobblestone.blockID && blockID != Block.stairCompactPlanks.blockID) {
				if(blockID == Block.ladder.blockID) {
					if(this.coordBaseMode == 0) {
						if(meta == 2) {
							return 3;
						}
						if(meta == 3) {
							return 2;
						}
					} else if(this.coordBaseMode == 1) {
						if(meta == 2) {
							return 4;
						}
						if(meta == 3) {
							return 5;
						}
						if(meta == 4) {
							return 2;
						}
						if(meta == 5) {
							return 3;
						}
					} else if(this.coordBaseMode == 3) {
						if(meta == 2) {
							return 5;
						}
						if(meta == 3) {
							return 4;
						}
						if(meta == 4) {
							return 2;
						}
						if(meta == 5) {
							return 3;
						}
					}
				} else if(blockID == Block.button.blockID) {
					if(this.coordBaseMode == 0) {
						if(meta == 3) {
							return 4;
						}
						if(meta == 4) {
							return 3;
						}
					} else if(this.coordBaseMode == 1) {
						if(meta == 3) {
							return 1;
						}
						if(meta == 4) {
							return 2;
						}
						if(meta == 2) {
							return 3;
						}
						if(meta == 1) {
							return 4;
						}
					} else if(this.coordBaseMode == 3) {
						if(meta == 3) {
							return 2;
						}
						if(meta == 4) {
							return 1;
						}
						if(meta == 2) {
							return 3;
						}
						if(meta == 1) {
							return 4;
						}
					}
				}
			} else if(this.coordBaseMode == 0) {
				if(meta == 2) {
					return 3;
				}
				if(meta == 3) {
					return 2;
				}
			} else if(this.coordBaseMode == 1) {
				if(meta == 0) {
					return 2;
				}
				if(meta == 1) {
					return 3;
				}
				if(meta == 2) {
					return 0;
				}
				if(meta == 3) {
					return 1;
				}
			} else if(this.coordBaseMode == 3) {
				if(meta == 0) {
					return 2;
				}
				if(meta == 1) {
					return 3;
				}
				if(meta == 2) {
					return 1;
				}
				if(meta == 3) {
					return 0;
				}
			}
		} else if(this.coordBaseMode == 0) {
			if(meta == 0) {
				return 2;
			}
			if(meta == 2) {
				return 0;
			}
		} else {
			if(this.coordBaseMode == 1) {
				return meta + 1 & 3;
			}
			if(this.coordBaseMode == 3) {
				return meta + 3 & 3;
			}
		}

		return meta;
	}

	/* ------------------------------------------------------------------ *
	 *  Block placement / query
	 * ------------------------------------------------------------------ */

	protected void placeBlock(World world, int blockID, int metadata, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ)) {
			world.setBlockAndMetadata(worldX, worldY, worldZ, blockID, metadata);
		}
	}

	protected void placeBlockIfNextToSolid(World world, int blockID, int metadata, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ)) {
			if(
					world.getBlockId(worldX - 1, worldY, worldZ) != 0 ||
					world.getBlockId(worldX + 1, worldY, worldZ) != 0 ||
					world.getBlockId(worldX, worldY, worldZ - 1) != 0 ||
					world.getBlockId(worldX, worldY, worldZ + 1) != 0 ||
					world.getBlockId(worldX, worldY - 1, worldZ) != 0 ||
					world.getBlockId(worldX, worldY + 1, worldZ) != 0
			) {
				world.setBlockAndMetadata(worldX, worldY, worldZ, blockID, metadata);
			}
		}
	}

	protected void placeBlockNoVertClip(World world, int blockID, int metadata, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInsideHorizontally(worldX, worldY, worldZ)) {
			world.setBlockAndMetadata(worldX, worldY, worldZ, blockID, metadata);
		}
	}

	protected int getBlockId(World world, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		return !clip.isVecInside(worldX, worldY, worldZ) ? 0 : world.getBlockId(worldX, worldY, worldZ);
	}

	protected Material getBlockMaterial(World world, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		return !clip.isVecInside(worldX, worldY, worldZ) ? Material.air : world.getBlockMaterial(worldX, worldY, worldZ);
	}

	protected int getBlockIdNoVertClip(World world, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		return !clip.isVecInsideHorizontally(worldX, worldY, worldZ) ? -1 : world.getBlockId(worldX, worldY, worldZ);
	}

	protected boolean canBlockSeeTheSky(World world, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		return !clip.isVecInside(worldX, worldY, worldZ) ? false : world.canBlockSeeTheSky(worldX, worldY, worldZ);
	}

	/* ------------------------------------------------------------------ *
	 *  Fill helpers
	 * ------------------------------------------------------------------ */

	protected void fillWithBlocks(World world, FeatureAABB clip, int x1, int y1, int z1, int x2, int y2, int z2, int edgeBlock, int innerBlock, boolean onlyReplaceExisting) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0) {
						if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
							this.placeBlock(world, innerBlock, 0, x, y, z, clip);
						} else {
							this.placeBlock(world, edgeBlock, 0, x, y, z, clip);
						}
					}
				}
			}
		}
	}

	protected void fillWithBlocksIfRock(World world, FeatureAABB clip, int x1, int y1, int z1, int x2, int y2, int z2, int edgeBlock, int innerBlock, boolean onlyReplaceExisting) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					Material material = this.getBlockMaterial(world, x, y, z, clip);
					if(material == Material.rock || material == Material.clay) {
						if(!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0) {
							if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
								this.placeBlock(world, innerBlock, 0, x, y, z, clip);
							} else {
								this.placeBlock(world, edgeBlock, 0, x, y, z, clip);
							}
						}
					}
				}
			}
		}
	}

	protected void fillWithBlockVariation(World world, FeatureAABB clip, int x1, int y1, int z1, int x2, int y2, int z2, int mainBlock, int variationBlock, Random rand, int chance, boolean onlyReplaceExisting) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0) {
						this.placeBlock(world, rand.nextInt(chance) == 0 ? variationBlock : mainBlock, 0, x, y, z, clip);
					}
				}
			}
		}
	}

	protected void fillWithRandomizedBlocks(World world, FeatureAABB clip, int x1, int y1, int z1, int x2, int y2, int z2, boolean onlyReplaceExisting, Random rand, StructureBlockSelector selector) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0) {
						selector.select(rand, x, y, z, y == y1 || y == y2 || x == x1 || x == x2 || z == z1 || z == z2);
						this.placeBlock(world, selector.getSelectedBlockId(), selector.getSelectedMetadata(), x, y, z, clip);
					}
				}
			}
		}
	}

	protected void randomlyFillWithBlocks(World world, FeatureAABB clip, Random rand, float chance, int x1, int y1, int z1, int x2, int y2, int z2, int edgeBlock, int innerBlock, boolean onlyReplaceExisting) {
		for(int y = y1; y <= y2; ++y) {
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					if(rand.nextFloat() <= chance && (!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0)) {
						if(y != y1 && y != y2 && x != x1 && x != x2 && z != z1 && z != z2) {
							this.placeBlock(world, innerBlock, 0, x, y, z, clip);
						} else {
							this.placeBlock(world, edgeBlock, 0, x, y, z, clip);
						}
					}
				}
			}
		}
	}

	protected void randomlyPlaceBlock(World world, FeatureAABB clip, Random rand, float chance, int x, int y, int z, int blockID, int meta) {
		if(rand.nextFloat() < chance) {
			this.placeBlock(world, blockID, meta, x, y, z, clip);
		}
	}

	protected void randomlyPlaceBlockIfNextToSolid(World world, FeatureAABB clip, Random rand, float chance, int x, int y, int z, int blockID, int meta) {
		if(rand.nextFloat() < chance) {
			this.placeBlockIfNextToSolid(world, blockID, meta, x, y, z, clip);
		}
	}

	/** Fills a solid ellipsoid inscribed in the given local box. */
	protected void randomlyRareFillWithBlocks(World world, FeatureAABB clip, int x1, int y1, int z1, int x2, int y2, int z2, int blockID, boolean onlyReplaceExisting) {
		float sizeX = (float)(x2 - x1 + 1);
		float sizeY = (float)(y2 - y1 + 1);
		float sizeZ = (float)(z2 - z1 + 1);
		float centerX = (float)x1 + sizeX / 2.0F;
		float centerZ = (float)z1 + sizeZ / 2.0F;

		for(int y = y1; y <= y2; ++y) {
			float normalizedY = (float)(y - y1) / sizeY;

			for(int x = x1; x <= x2; ++x) {
				float normalizedX = ((float)x - centerX) / (sizeX * 0.5F);

				for(int z = z1; z <= z2; ++z) {
					float normalizedZ = ((float)z - centerZ) / (sizeZ * 0.5F);
					if(!onlyReplaceExisting || this.getBlockId(world, x, y, z, clip) != 0) {
						float distance = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;
						if(distance <= 1.05F) {
							this.placeBlock(world, blockID, 0, x, y, z, clip);
						}
					}
				}
			}
		}
	}

	protected void clearBlocksUpwards(World world, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ)) {
			while(!world.isAirBlock(worldX, worldY, worldZ) && worldY < 255) {
				world.setBlockAndMetadata(worldX, worldY, worldZ, 0, 0);
				++worldY;
			}
		}
	}

	protected void fillBlocksDownwards(World world, int blockID, int metadata, int x, int y, int z, FeatureAABB clip) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ)) {
			while((world.isAirBlock(worldX, worldY, worldZ) || world.getBlockMaterial(worldX, worldY, worldZ).getIsLiquid()) && worldY > 1) {
				world.setBlockAndMetadata(worldX, worldY, worldZ, blockID, metadata);
				--worldY;
			}
		}
	}

	protected void placeLootChest(World world, FeatureAABB clip, Random rand, int x, int y, int z, LootEntry[] loot, int stacks) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ) && world.getBlockId(worldX, worldY, worldZ) != Block.chest.blockID) {
			world.setBlockWithNotify(worldX, worldY, worldZ, Block.chest.blockID);
			TileEntityChest chest = (TileEntityChest)world.getBlockTileEntity(worldX, worldY, worldZ);
			if(chest != null) {
				LootEntry.fillChest(rand, loot, chest, stacks);
			}
		}
	}

	protected void placeDoor(World world, FeatureAABB clip, Random rand, int x, int y, int z, int meta) {
		int worldX = this.getXWithOffset(x, z);
		int worldY = this.getYWithOffset(y);
		int worldZ = this.getZWithOffset(x, z);
		if(clip.isVecInside(worldX, worldY, worldZ)) {
			ItemDoor.placeDoorBlock(world, worldX, worldY, worldZ, meta, Block.doorWood);
		}
	}

	/* ------------------------------------------------------------------ *
	 *  Terrain probes
	 * ------------------------------------------------------------------ */

	/** Fraction of opaque blocks inside the piece box (clipped), true if above threshold. */
	protected boolean isMostlySolid(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int y1 = Math.max(this.box.y1 - 1, clip.y1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int y2 = Math.min(this.box.y2 + 1, clip.y2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int solid = 0;
		int total = 0;
		for(int x = x1; x < x2; ++x) {
			for(int y = y1; y < y2; ++y) {
				for(int z = z1; z < z2; ++z) {
					++total;
					if(world.isBlockOpaqueCube(x, y, z)) {
						++solid;
					}
				}
			}
		}

		return solid > (float)total * this.getMostlySolidPercent();
	}

	/** Fraction of opaque blocks inside the clipped piece box. */
	protected float getFractionSolid(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int y1 = Math.max(this.box.y1 - 1, clip.y1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int y2 = Math.min(this.box.y2 + 1, clip.y2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int solid = 0;
		int total = 0;
		for(int x = x1; x < x2; ++x) {
			for(int y = y1; y < y2; ++y) {
				for(int z = z1; z < z2; ++z) {
					++total;
					if(world.isBlockOpaqueCube(x, y, z)) {
						++solid;
					}
				}
			}
		}

		return (float)solid / (float)total;
	}

	/** Fraction of the top layer of the clipped piece box that can see the sky. */
	protected float getFractionExposed(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int y2 = Math.min(this.box.y2 + 1, clip.y2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int exposed = 0;
		int total = 0;
		for(int x = x1; x < x2; ++x) {
			for(int z = z1; z < z2; ++z) {
				++total;
				if(world.canBlockSeeTheSky(x, y2, z)) {
					++exposed;
				}
			}
		}

		return (float)exposed / (float)total;
	}

	/** True if any block on the six faces of the clipped piece box is a liquid. */
	protected boolean hasLiquidInBox(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int y1 = Math.max(this.box.y1 - 1, clip.y1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int y2 = Math.min(this.box.y2 + 1, clip.y2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int x;
		int y;
		int z;
		int id;
		for(x = x1; x <= x2; ++x) {
			for(z = z1; z <= z2; ++z) {
				id = world.getBlockId(x, y1, z);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}

				id = world.getBlockId(x, y2, z);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}
			}
		}

		for(x = x1; x <= x2; ++x) {
			for(y = y1; y <= y2; ++y) {
				id = world.getBlockId(x, y, z1);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}

				id = world.getBlockId(x, y, z2);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}
			}
		}

		for(z = z1; z <= z2; ++z) {
			for(y = y1; y <= y2; ++y) {
				id = world.getBlockId(x1, y, z);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}

				id = world.getBlockId(x2, y, z);
				if(id > 0 && Block.blocksList[id].blockMaterial.getIsLiquid()) {
					return true;
				}
			}
		}

		return false;
	}

	/** True if the entire floor under the clipped piece box is non-opaque (free air). */
	protected boolean isAirBelow(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int y = clip.y1 - 1;

		for(int x = x1; x <= x2; ++x) {
			for(int z = z1; z <= z2; ++z) {
				Block block = Block.blocksList[world.getBlockId(x, y, z)];
				if(block != null && block.isOpaqueCube()) {
					return false;
				}
			}
		}

		return true;
	}

	/** Fraction of the single floor layer just below the clipped piece box that is non-opaque. */
	protected float getAirRatioBelow(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int y = Math.max(this.box.y1 - 1, clip.y1);

		int open = 0;
		int total = 0;
		for(int x = x1; x <= x2; ++x) {
			for(int z = z1; z <= z2; ++z) {
				Block block = Block.blocksList[world.getBlockId(x, y, z)];
				if(block == null || !block.isOpaqueCube()) {
					++open;
				}
				++total;
			}
		}

		return (float)open / (float)total;
	}

	/** Fraction of the six "walls" of the clipped piece box that is empty air. */
	protected float getAirRatioInWalls(World world, FeatureAABB clip) {
		int x1 = Math.max(this.box.x1 - 1, clip.x1);
		int y1 = Math.max(this.box.y1 - 1, clip.y1);
		int z1 = Math.max(this.box.z1 - 1, clip.z1);
		int x2 = Math.min(this.box.x2 + 1, clip.x2);
		int y2 = Math.min(this.box.y2 + 1, clip.y2);
		int z2 = Math.min(this.box.z2 + 1, clip.z2);

		int open = 0;
		int total = 0;
		int id;

		for(int x = x1; x <= x2; ++x) {
			for(int z = z1; z <= z2; ++z) {
				id = world.getBlockId(x, y1, z);
				if(id == 0) {
					++open;
				}
				++total;

				id = world.getBlockId(x, y2, z);
				if(id == 0) {
					++open;
				}
				++total;
			}
		}

		for(int x = x1; x <= x2; ++x) {
			for(int y = y1; y <= y2; ++y) {
				id = world.getBlockId(x, y, z1);
				if(id == 0) {
					++open;
				}
				++total;

				id = world.getBlockId(x, y, z2);
				if(id == 0) {
					++open;
				}
				++total;
			}
		}

		for(int z = z1; z <= z2; ++z) {
			for(int y = y1; y <= y2; ++y) {
				id = world.getBlockId(x1, y, z);
				if(id == 0) {
					++open;
				}
				++total;

				id = world.getBlockId(x2, y, z);
				if(id == 0) {
					++open;
				}
				++total;
			}
		}

		return (float)open / (float)total;
	}
}
