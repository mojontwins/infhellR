package net.minecraft.game.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;

/**
 * Models a block explosion (TNT, creeper, ghast fireball, etc.).
 */
public class Explosion {
	public boolean isFlaming = false;
	private Random rng = new Random();
	private World world;
	public double explosionX;
	public double explosionY;
	public double explosionZ;
	public Entity exploder;
	public float explosionSize;
	public Set<ChunkPosition> destroyedBlockPositions = new HashSet<ChunkPosition>();
	public int blockID;

	public Explosion(World world, Entity entity, double x, double y, double z, float size) {
		this(world, entity, x, y, z, size, 0);
	}

	public Explosion(World world, Entity entity, double x, double y, double z, float size, int blockID) {
		this.world = world;
		this.exploder = entity;
		this.explosionSize = size;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.blockID = blockID;
	}

	public void doExplosion() {
		float originalSize = this.explosionSize;
		int gridSize = 16;

		// Trace rays outward from the centre, sampled across the faces of a 16x16x16 cube shell;
		// the distance each ray travels before running out of force is the explosion's reach.
		for(int ix = 0; ix < gridSize; ++ix) {
			for(int iy = 0; iy < gridSize; ++iy) {
				for(int iz = 0; iz < gridSize; ++iz) {
					if(ix == 0 || ix == gridSize - 1 || iy == 0 || iy == gridSize - 1 || iz == 0 || iz == gridSize - 1) {
						double dx = (double)((float)ix / ((float)gridSize - 1.0F) * 2.0F - 1.0F);
						double dy = (double)((float)iy / ((float)gridSize - 1.0F) * 2.0F - 1.0F);
						double dz = (double)((float)iz / ((float)gridSize - 1.0F) * 2.0F - 1.0F);
						double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
						dx /= len;
						dy /= len;
						dz /= len;
						float remaining = this.explosionSize * (0.7F + this.world.rand.nextFloat() * 0.6F);
						double cx = this.explosionX;
						double cy = this.explosionY;
						double cz = this.explosionZ;

						for(float step = 0.3F; remaining > 0.0F; remaining -= step * 0.75F) {
							int bx = MathHelper.floor_double(cx);
							int by = MathHelper.floor_double(cy);
							int bz = MathHelper.floor_double(cz);
							int blockId = this.world.getBlockId(bx, by, bz);
							if(blockId > 0) {
								remaining -= (Block.blocksList[blockId].getExplosionResistance(this.exploder) + 0.3F) * step;
							}

							if(remaining > 0.0F) {
								this.destroyedBlockPositions.add(new ChunkPosition(bx, by, bz));
							}

							cx += dx * (double)step;
							cy += dy * (double)step;
							cz += dz * (double)step;
						}
					}
				}
			}
		}

		this.explosionSize *= 2.0F;
		int minX = MathHelper.floor_double(this.explosionX - (double)this.explosionSize - 1.0D);
		int maxX = MathHelper.floor_double(this.explosionX + (double)this.explosionSize + 1.0D);
		int minY = MathHelper.floor_double(this.explosionY - (double)this.explosionSize - 1.0D);
		int maxY = MathHelper.floor_double(this.explosionY + (double)this.explosionSize + 1.0D);
		int minZ = MathHelper.floor_double(this.explosionZ - (double)this.explosionSize - 1.0D);
		int maxZ = MathHelper.floor_double(this.explosionZ + (double)this.explosionSize + 1.0D);
		List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getBoundingBoxFromPool((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ));
		Vec3D center = Vec3D.createVector(this.explosionX, this.explosionY, this.explosionZ);

		// Damage and knock back every entity inside the blast, weighted by distance and the entity's
		// degree of exposure (block density) to the blast centre.
		for(int i = 0; i < entities.size(); ++i) {
			Entity entity = (Entity)entities.get(i);
			double dist = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)this.explosionSize;
			if(dist <= 1.0D) {
				double dx = entity.posX - this.explosionX;
				double dy = entity.posY - this.explosionY;
				double dz = entity.posZ - this.explosionZ;
				double d = (double)MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
				dx /= d;
				dy /= d;
				dz /= d;
				double blockDensity = (double)this.world.getBlockDensity(center, entity.boundingBox);
				double impact = (1.0D - dist) * blockDensity;
				entity.attackEntityFrom(this.exploder, (int)((impact * impact + impact) / 2.0D * 8.0D * (double)this.explosionSize + 1.0D));
				entity.motionX += dx * impact;
				entity.motionY += dy * impact;
				entity.motionZ += dz * impact;
			}
		}

		this.explosionSize = originalSize;
		ArrayList<ChunkPosition> blocks = new ArrayList<ChunkPosition>();
		blocks.addAll(this.destroyedBlockPositions);
		if(this.isFlaming) {
			// Flaming explosions ignite roughly a third of the surfaces they destroyed.
			for(int i = blocks.size() - 1; i >= 0; --i) {
				ChunkPosition pos = (ChunkPosition)blocks.get(i);
				int x = pos.x;
				int y = pos.y;
				int z = pos.z;
				int blockAbove = this.world.getBlockId(x, y, z);
				int blockBelow = this.world.getBlockId(x, y - 1, z);
				if(blockAbove == 0 && Block.opaqueCubeLookup[blockBelow] && this.rng.nextInt(3) == 0) {
					this.world.setBlockWithNotify(x, y, z, Block.fire.blockID);
				}
			}
		}

	}

	public void doEffects(boolean doParticles) {
		this.world.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
		ArrayList<ChunkPosition> blocks = new ArrayList<ChunkPosition>();
		blocks.addAll(this.destroyedBlockPositions);

		for(int i = blocks.size() - 1; i >= 0; --i) {
			ChunkPosition pos = (ChunkPosition)blocks.get(i);
			int x = pos.x;
			int y = pos.y;
			int z = pos.z;
			int blockId = this.world.getBlockId(x, y, z);
			if(doParticles) {
				double px = (double)((float)x + this.world.rand.nextFloat());
				double py = (double)((float)y + this.world.rand.nextFloat());
				double pz = (double)((float)z + this.world.rand.nextFloat());
				double dx = px - this.explosionX;
				double dy = py - this.explosionY;
				double dz = pz - this.explosionZ;
				double len = (double)MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
				dx /= len;
				dy /= len;
				dz /= len;
				double speed = 0.5D / (len / (double)this.explosionSize + 0.1D);
				speed *= (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
				dx *= speed;
				dy *= speed;
				dz *= speed;
				this.world.spawnParticle("explode", (px + this.explosionX * 1.0D) / 2.0D, (py + this.explosionY * 1.0D) / 2.0D, (pz + this.explosionZ * 1.0D) / 2.0D, dx, dy, dz);
				this.world.spawnParticle("smoke", px, py, pz, dx, dy, dz);
			}

			if(this.blockID == 0) {
				if(blockId > 0) {
					Block.blocksList[blockId].dropBlockAsItemWithChance(this.world, x, y, z, this.world.getBlockMetadata(x, y, z), 0.3F);
					this.world.setBlockWithNotify(x, y, z, 0);
					Block.blocksList[blockId].onBlockDestroyedByExplosion(this.world, x, y, z);
				}
			} else {
				this.world.setBlockWithNotify(x, y, z, this.blockID);
			}
		}

	}
}
