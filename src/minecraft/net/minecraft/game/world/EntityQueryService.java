package net.minecraft.game.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockFluid;
import net.minecraft.game.world.material.Material;

/**
 * Stateless-ish query helpers for {@link World} that answer spatial questions about blocks and
 * entities (axis-aligned bounding box collisions, material/liquid containment, block density, and
 * entity-in-AABB lookups).
 *
 * <p>This class owns no game state of its own beyond a pair of reusable result buffers used to
 * avoid per-call allocations on the hot collision and entity-query code paths. {@link World} holds a
 * single instance (field {@code entityQueryService}) and forwards every public query entry point
 * here, so external callers are unchanged.
 */
public final class EntityQueryService {

	/** Reusable output buffer for block collision queries. */
	private final ArrayList<AxisAlignedBB> collidingBoundingBoxes;

	/** Reusable output buffer for "entities within this AABB" queries. */
	private final List<Entity> entitiesWithinAABBExcludingEntity;

	/** The {@link World} this service answers queries against. */
	private final World world;

	/** Constructs a query service backed by the given world. */
	public EntityQueryService(World world) {
		this.world = world;
		this.collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
		this.entitiesWithinAABBExcludingEntity = new ArrayList<Entity>();
	}

	/**
	 * Returns the solid (non-water) collision boxes within the given AABB, excluding any belonging
	 * to liquid blocks. The returned list is a reusable internal buffer and is cleared on each call.
	 */
	public List<AxisAlignedBB> getCollidingBoundingBoxesExcludingWater(Entity entity, AxisAlignedBB aabb) {
		this.collidingBoundingBoxes.clear();
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);

		for(int x = minX; x < maxX; ++x) {
			for(int z = minZ; z < maxZ; ++z) {
				if(this.world.blockExists(x, 64, z)) {
					for(int y = minY - 1; y < maxY; ++y) {
						Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
						if(block != null && !(block instanceof BlockFluid)) {
							block.getCollidingBoundingBoxes(this.world, x, y, z, aabb, this.collidingBoundingBoxes, entity);
						}
					}
				}
			}
		}
		
		return this.collidingBoundingBoxes;
	}

	/**
	 * Returns the collision boxes within the given AABB, including both block collision boxes and
	 * entity bounding/collision boxes overlapping the AABB. The returned list is a reusable internal
	 * buffer and is cleared on each call.
	 */
	public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB aabb) {
		this.collidingBoundingBoxes.clear();
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);

		for(int x = minX; x < maxX; ++x) {
			for(int z = minZ; z < maxZ; ++z) {
				if(this.world.blockExists(x, 64, z)) {
					for(int y = minY - 1; y < maxY; ++y) {
						Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
						if(block != null) {
							block.getCollidingBoundingBoxes(this.world, x, y, z, aabb, this.collidingBoundingBoxes, entity);
						}
					}
				}
			}
		}

		double expand = 0.25D;
		List<Entity> nearby = this.getEntitiesWithinAABBExcludingEntity(entity, aabb.expand(expand, expand, expand));

		for(int i = 0; i < nearby.size(); ++i) {
			AxisAlignedBB entityBox = ((Entity)nearby.get(i)).getBoundingBox();
			if(entityBox != null && entityBox.intersectsWith(aabb)) {
				this.collidingBoundingBoxes.add(entityBox);
			}

			entityBox = entity.getCollisionBox((Entity)nearby.get(i));
			if(entityBox != null && entityBox.intersectsWith(aabb)) {
				this.collidingBoundingBoxes.add(entityBox);
			}
		}

		return this.collidingBoundingBoxes;
	}

	/**
	 * Returns whether the given AABB is free of non-dead entities that block spawning (i.e. whether
	 * it is clear enough for an entity to be placed there).
	 */
	public boolean checkIfAABBIsClear(AxisAlignedBB aabb) {
		List<Entity> entities = this.getEntitiesWithinAABBExcludingEntity((Entity)null, aabb);

		for(int i = 0; i < entities.size(); ++i) {
			Entity entity = (Entity)entities.get(i);
			if(!entity.isDead && entity.preventEntitySpawning) {
				return false;
			}
		}

		return true;
	}

	/** Returns whether any non-null block occupies any block position inside the given AABB. */
	public boolean getIsAnyNonEmptyBlock(AxisAlignedBB aabb) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(aabb.minX < 0.0D) {
			--minX;
		}

		if(aabb.minY < 0.0D) {
			--minY;
		}

		if(aabb.minZ < 0.0D) {
			--minZ;
		}

		for(int x = minX; x < maxX; ++x) {
			for(int y = minY; y < maxY; ++y) {
				for(int z = minZ; z < maxZ; ++z) {
					Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
					if(block != null) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/** Returns whether any liquid block occupies any block position inside the given AABB. */
	public boolean getIsAnyLiquid(AxisAlignedBB aabb) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(aabb.minX < 0.0D) {
			--minX;
		}

		if(aabb.minY < 0.0D) {
			--minY;
		}

		if(aabb.minZ < 0.0D) {
			--minZ;
		}

		for(int x = minX; x < maxX; ++x) {
			for(int y = minY; y < maxY; ++y) {
				for(int z = minZ; z < maxZ; ++z) {
					Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
					if(block != null && block.blockMaterial.getIsLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/** Returns whether the block with the given id occupies any block position inside the AABB. */
	public boolean getIsAnyBlockID(AxisAlignedBB aabb, int blockId) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(aabb.minX < 0.0D) {
			--minX;
		}

		if(aabb.minY < 0.0D) {
			--minY;
		}

		if(aabb.minZ < 0.0D) {
			--minZ;
		}

		for(int x = minX; x < maxX; ++x) {
			for(int y = minY; y < maxY; ++y) {
				for(int z = minZ; z < maxZ; ++z) {
					if(blockId == this.world.getBlockId(x, y, z)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/** Returns whether the given AABB overlaps fire or lava blocks. */
	public boolean isBoundingBoxBurning(AxisAlignedBB aabb) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(this.world.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
			for(int x = minX; x < maxX; ++x) {
				for(int y = minY; y < maxY; ++y) {
					for(int z = minZ; z < maxZ; ++z) {
						int blockId = this.world.getBlockId(x, y, z);
						if(blockId == Block.fire.blockID || blockId == Block.lavaMoving.blockID || blockId == Block.lavaStill.blockID) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Applies the flow velocity of any block of the given material overlapping the AABB to the given
	 * entity, and returns whether such a block was encountered.
	 */
	public boolean handleMaterialAcceleration(AxisAlignedBB aabb, Material material, Entity entity) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(!this.world.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
			return false;
		} else {
			boolean hit = false;
			Vec3D flow = Vec3D.createVector(0.0D, 0.0D, 0.0D);

			for(int x = minX; x < maxX; ++x) {
				for(int y = minY; y < maxY; ++y) {
					for(int z = minZ; z < maxZ; ++z) {
						Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
						if(block != null && block.blockMaterial == material) {
							double surface = (double)((float)(y + 1) - BlockFluid.getPercentAir(this.world.getBlockMetadata(x, y, z)));
							if((double)maxY >= surface) {
								hit = true;
								block.velocityToAddToEntity(this.world, x, y, z, entity, flow);
							}
						}
					}
				}
			}

			if(flow.lengthVector() > 0.0D) {
				flow = flow.normalize();
				double speed = 0.014D;
				entity.motionX += flow.xCoord * speed;
				entity.motionY += flow.yCoord * speed;
				entity.motionZ += flow.zCoord * speed;
			}

			return hit;
		}
	}

	/** Returns whether any block of the given material occupies any position inside the AABB. */
	public boolean isMaterialInBB(AxisAlignedBB aabb, Material material) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);

		for(int x = minX; x < maxX; ++x) {
			for(int y = minY; y < maxY; ++y) {
				for(int z = minZ; z < maxZ; ++z) {
					Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
					if(block != null && block.blockMaterial == material) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Returns whether the given AABB is submerged in the given material and whether the material's
	 * surface (accounting for the flow level metadata) reaches the bottom of the AABB.
	 */
	public boolean isAABBInMaterial(AxisAlignedBB aabb, Material material) {
		int minX = MathHelper.floor_double(aabb.minX);
		int maxX = MathHelper.floor_double(aabb.maxX + 1.0D);
		int minY = MathHelper.floor_double(aabb.minY);
		int maxY = MathHelper.floor_double(aabb.maxY + 1.0D);
		int minZ = MathHelper.floor_double(aabb.minZ);
		int maxZ = MathHelper.floor_double(aabb.maxZ + 1.0D);

		for(int x = minX; x < maxX; ++x) {
			for(int y = minY; y < maxY; ++y) {
				for(int z = minZ; z < maxZ; ++z) {
					Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
					if(block != null && block.blockMaterial == material) {
						int meta = this.world.getBlockMetadata(x, y, z);
						double surface = (double)(y + 1);
						if(meta < 8) {
							surface = (double)(y + 1) - (double)meta / 8.0D;
						}

						if(surface >= aabb.minY) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Returns the fraction (0.0-1.0) of the given AABB (sampled across its extents) that is visible
	 * along the ray from {@code from} toward each sample point, ignoring occluding blocks.
	 */
	public float getBlockDensity(Vec3D from, AxisAlignedBB aabb) {
		double stepX = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
		double stepY = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
		double stepZ = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
		int visible = 0;
		int total = 0;

		for(float fx = 0.0F; fx <= 1.0F; fx = (float)((double)fx + stepX)) {
			for(float fy = 0.0F; fy <= 1.0F; fy = (float)((double)fy + stepY)) {
				for(float fz = 0.0F; fz <= 1.0F; fz = (float)((double)fz + stepZ)) {
					double sx = aabb.minX + (aabb.maxX - aabb.minX) * (double)fx;
					double sy = aabb.minY + (aabb.maxY - aabb.minY) * (double)fy;
					double sz = aabb.minZ + (aabb.maxZ - aabb.minZ) * (double)fz;
					if(this.world.rayTraceBlocks(Vec3D.createVector(sx, sy, sz), from) == null) {
						++visible;
					}

					++total;
				}
			}
		}

		return (float)visible / (float)total;
	}

	/**
	 * Returns all entities whose bounding boxes fall inside the given AABB, excluding a specific
	 * entity. The returned list is a reusable internal buffer and is cleared on each call.
	 */
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB aabb) {
		this.entitiesWithinAABBExcludingEntity.clear();
		int minCX = MathHelper.floor_double((aabb.minX - 2.0D) / 16.0D);
		int maxCX = MathHelper.floor_double((aabb.maxX + 2.0D) / 16.0D);
		int minCZ = MathHelper.floor_double((aabb.minZ - 2.0D) / 16.0D);
		int maxCZ = MathHelper.floor_double((aabb.maxZ + 2.0D) / 16.0D);

		for(int cx = minCX; cx <= maxCX; ++cx) {
			for(int cz = minCZ; cz <= maxCZ; ++cz) {
				if(this.world.chunkExists(cx, cz)) {
					this.world.getChunkFromChunkCoords(cx, cz).getEntitiesWithinAABBForEntity(entity, aabb, this.entitiesWithinAABBExcludingEntity);
				}
			}
		}

		return this.entitiesWithinAABBExcludingEntity;
	}

	/** Returns all entities of the given type whose bounding boxes fall inside the given AABB. */
	public List<Entity> getEntitiesWithinAABB(Class<?> entityClass, AxisAlignedBB aabb) {
		int minCX = MathHelper.floor_double((aabb.minX - 2.0D) / 16.0D);
		int maxCX = MathHelper.floor_double((aabb.maxX + 2.0D) / 16.0D);
		int minCZ = MathHelper.floor_double((aabb.minZ - 2.0D) / 16.0D);
		int maxCZ = MathHelper.floor_double((aabb.maxZ + 2.0D) / 16.0D);
		ArrayList<Entity> result = new ArrayList<Entity>();

		for(int cx = minCX; cx <= maxCX; ++cx) {
			for(int cz = minCZ; cz <= maxCZ; ++cz) {
				if(this.world.chunkExists(cx, cz)) {
					this.world.getChunkFromChunkCoords(cx, cz).getEntitiesOfTypeWithinAAAB(entityClass, aabb, result);
				}
			}
		}

		return result;
	}

	/** Returns the nearest entity of the given type inside the AABB to the given entity (inclusive). */
	public Entity findNearestEntityWithinAABB(Class<?> entityClass, AxisAlignedBB aabb, Entity entity) {
		List<Entity> entities = this.getEntitiesWithinAABB(entityClass, aabb);
		Entity nearest = null;
		double bestDistance = Double.MAX_VALUE;

		for(Entity other : entities) {
			if(other != entity) {
				double distance = entity.getDistanceSqToEntity(other);
				if(distance <= bestDistance) {
					nearest = other;
					bestDistance = distance;
				}
			}
		}

		return nearest;
	}
}
