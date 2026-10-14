package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * A mesa mineshaft corridor.
 *
 * <p>Unlike the regular corridor - which lays a stone/cobblestone shell during chunk
 * generation - the mesa corridor is built entirely during population, because every one
 * of its decisions needs the surrounding world:
 *
 * <ul>
 *   <li>the whole piece is skipped when the chunk region holds liquid, is too
 *       air/exposed, or (per section) the sky is visible;</li>
 *   <li>its tunnel is carved only through natural rock/clay ({@link #fillIfRock});</li>
 *   <li>its supports switch between arches and hanging chains based on whether there is
 *       intact ground above;</li>
 *   <li>its floor planks and (rare) spider spawner are gated on exposure.</li>
 * </ul>
 *
 * <p>This is a faithful port of the vanilla {@code ComponentMineshaftMesaCorridor}.
 */
public class MineshaftComponentMesaCorridor extends MineshaftComponentCorridor {
	public MineshaftComponentMesaCorridor(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int direction) {
		super(world, componentType, rand, boundingBox, direction);
	}

	/** No structural pass: the mesa draws everything in the world phase (see {@link #populate}). */
	@Override
	public void generate(MineshaftWriter writer, Random rand) {
	}

	@Override
	public void populate(MineshaftWriter writer, Random rand) {
		StructureBoundingBox chunkBox = writer.getChunkBox();
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return;
		}
		if(this.getFractionSolid(writer, chunkBox) < 0.4F || this.getFractionExposed(writer, chunkBox) > 0.5F) {
			return;
		}

		int length = this.sectionCount * 5 - 1;
		this.fillIfRock(writer, chunkBox, 0, 0, 0, 2, 2, length, 0, 0, false);
		if(this.hasSpiders) {
			this.randomFill(writer, rand, 0.6F, 0, 0, 0, 2, 1, length, Block.web.blockID, 0, false);
		}

		for(int section = 0; section < this.sectionCount; ++section) {
			int midZ = 2 + section * 5;

			// Arch when a ceiling still exists above, otherwise hang chains from the roof.
			if(this.getBlockIDNoClip(writer, 0, 3, midZ) != 0
					|| this.getBlockIDNoClip(writer, 1, 3, midZ) != 0
					|| this.getBlockIDNoClip(writer, 2, 3, midZ) != 0) {
				this.fill(writer, 0, 0, midZ, 0, 1, midZ, Block.fence.blockID, 0, false);
				this.fill(writer, 2, 0, midZ, 2, 1, midZ, Block.fence.blockID, 0, false);
				this.placeBlock(writer, Block.planks.blockID, 0, 0, 2, midZ);
				this.placeBlock(writer, Block.stairSingle.blockID, 2 | 8, 1, 2, midZ);
				this.placeBlock(writer, Block.planks.blockID, 0, 2, 2, midZ);
			} else {
				if(this.getBlockIDNoClip(writer, 0, -1, midZ) != 0 && !this.canBlockSeeTheSky(writer, 0, 1, midZ)) {
					this.placeBlock(writer, Block.fence.blockID, 0, 0, 0, midZ);
					int y = 1;
					while(this.getBlockIDNoClip(writer, 0, y, midZ) == 0) {
						this.placeBlockNoVertClip(writer, Block.chain.blockID, 0, 0, y, midZ);
						++y;
					}
				}

				if(this.getBlockIDNoClip(writer, 2, -1, midZ) != 0 && !this.canBlockSeeTheSky(writer, 2, 1, midZ)) {
					this.placeBlock(writer, Block.fence.blockID, 0, 2, 0, midZ);
					int y = 1;
					while(this.getBlockIDNoClip(writer, 2, y, midZ) == 0) {
						this.placeBlockNoVertClip(writer, Block.chain.blockID, 0, 2, y, midZ);
						++y;
					}
				}
			}

			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.1F, 0, 2, midZ - 1, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.1F, 2, 2, midZ - 1, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.1F, 0, 2, midZ + 1, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.1F, 2, 2, midZ + 1, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 0, 2, midZ - 2, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 2, 2, midZ - 2, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 0, 2, midZ + 2, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 2, 2, midZ + 2, Block.web.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 0, 2, midZ + 2, Block.torchWood.blockID, 0);
			this.randomBlockIfNextToSolid(writer, chunkBox, rand, 0.05F, 2, 2, midZ + 2, Block.torchWood.blockID, 0);

			if(rand.nextInt(100) == 0) {
				this.createTreasureChest(writer, rand, 2, 0, midZ - 1, this.pieces.getLoot(), 3 + rand.nextInt(4));
			}
			if(rand.nextInt(100) == 0) {
				this.createTreasureChest(writer, rand, 0, 0, midZ + 1, this.pieces.getLoot(), 3 + rand.nextInt(4));
			}

			if(this.hasSpiders && this.getFractionExposed(writer, chunkBox) < 0.4F) {
				if(!this.spawnerPlaced) {
					int randZ = midZ - 1 + rand.nextInt(3);
					int xAbs = this.getXWithOffset(1, randZ);
					int zAbs = this.getZWithOffset(1, randZ);
					if(writer.isInChunk(xAbs, zAbs)) {
						this.spawnerPlaced = true;
						this.placeSpawner(writer, 1, 0, randZ, "Spider");
					}
				}

				if(this.spawnerPlaced) {
					for(int y = 0; y <= 2; ++y) {
						for(int x = 0; x <= 2; ++x) {
							for(int z = 0; z <= 4; ++z) {
								if(this.getBlockID(writer, x, y, z) == 0) {
									this.placeBlock(writer, Block.web.blockID, 0, x, y, z);
								}
							}
						}
					}
				}
			}
		}

		// Lay a plank floor wherever the natural floor is missing and the sky is not visible.
		for(int x = 0; x <= 2; ++x) {
			for(int z = 0; z <= length; ++z) {
				if(this.getBlockID(writer, x, -1, z) == 0 && !this.canBlockSeeTheSky(writer, x, 3, z)) {
					this.placeBlock(writer, Block.planks.blockID, 0, x, -1, z);
				}
			}
		}

		if(this.hasRails) {
			for(int z = 0; z <= length; ++z) {
				int below = this.getBlockID(writer, 1, -1, z);
				if(below > 0 && Block.opaqueCubeLookup[below]) {
					this.randomBlock(writer, rand, 0.7F, 1, 0, z, Block.rail.blockID, this.getMetadataWithOffset(Block.rail.blockID, 0));
				}
			}
		}
	}
}
