package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDarkForest;

public class BlockFire extends Block {

	protected BlockFire(int i1, int i2) {
		super(i1, i2, Material.fire);
		this.setTickOnLoad(true);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 3;
	}

	public int quantityDropped(Random rand) {
		return 0;
	}

	public int tickRate() {
		return 40;
	}

	// Reinstated old fire spread for b1.5
	public void updateTick(World world, int x, int y, int z, Random rand) {
		int blockIDBeneath = world.getBlockId(x, y - 1, z);
		boolean superFuelBeneath = blockIDBeneath == Block.bloodStone.blockID || blockIDBeneath == Block.blockCoal.blockID;
		if(!this.canPlaceBlockAt(world, x, y, z)) {
			world.setBlockWithNotify(x, y, z, 0);
		}

		if(!superFuelBeneath && (world.raining() || world.snowing()) && (world.canBlockBeRainedOn(x, y, z) || world.canBlockBeRainedOn(x - 1, y, z) || world.canBlockBeRainedOn(x + 1, y, z) || world.canBlockBeRainedOn(x, y, z - 1) || world.canBlockBeRainedOn(x, y, z + 1))) {
			world.setBlockWithNotify(x, y, z, 0);
		} else {
			int fireAge = world.getBlockMetadata(x, y, z);
			if(fireAge < 15) {
				world.setBlockMetadata(x, y, z, fireAge + rand.nextInt(3) / 2);
			}

			world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
			if(!superFuelBeneath && !this.canNeighborBurn(world, x, y, z)) {
				if(!world.isBlockNormalCube(x, y - 1, z) || fireAge > 3) {
					world.setBlockWithNotify(x, y, z, 0);
				}

			} else if(!superFuelBeneath && !this.canBlockCatchFire(world, x, y - 1, z) && fireAge == 15 && rand.nextInt(4) == 0) {
				world.setBlockWithNotify(x, y, z, 0);
			} else {
				if(fireAge % 2 == 0 && fireAge > 2) {
					this.tryToCatchBlockOnFire(world, x + 1, y, z, 300, rand, fireAge);
					this.tryToCatchBlockOnFire(world, x - 1, y, z, 300, rand, fireAge);
					this.tryToCatchBlockOnFire(world, x, y - 1, z, 250, rand, fireAge);
					this.tryToCatchBlockOnFire(world, x, y + 1, z, 250, rand, fireAge);
					this.tryToCatchBlockOnFire(world, x, y, z - 1, 300, rand, fireAge);
					this.tryToCatchBlockOnFire(world, x, y, z + 1, 300, rand, fireAge);
	
					for(int xx = x - 1; xx <= x + 1; ++xx) {
						for(int zz = z - 1; zz <= z + 1; ++zz) {
							for(int yy = y - 1; yy <= y + 4; ++yy) {
								if(xx != x || yy != y || zz != z) {
									BiomeGenBase biome = world.getBiomeGenAt(xx, zz);
									
									// Decrease chance (increasing sample) with distance
									int randSize = 100;
									if(yy > y + 1) {
										randSize += (yy - (y + 1)) * 100;
									}
	
									int chance = this.getChanceOfNeighborsEncouragingFire(world, xx, yy, zz);
									if(world.raining() || world.snowing()) chance >>= 2;
									if(biome.isHumid()) chance >>= 2;
									if(biome instanceof BiomeGenDarkForest) chance >>= 4;
									
									if(chance > 0 && rand.nextInt(randSize) <= chance) {
										if(!(world.raining() || world.snowing()) || !world.canBlockBeRainedOn(xx, yy, zz)) {
											world.setBlockWithNotify(xx, yy, zz, this.blockID); 
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void tryToCatchBlockOnFire(World world, int x, int y, int z, int chance, Random rand, int fireAge) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		int catchFire = block != null ? block.getAbilityToCatchFire() : 0;
		if(world.raining() || world.snowing()) catchFire >>= 2;
		if(world.getBiomeGenAt(x, z).isHumid()) catchFire >>= 1;
		if(rand.nextInt(chance) < catchFire) {
			boolean isTnt = block != null && block.blockID == Block.tnt.blockID;
			if(rand.nextInt(2) == 0) {
				world.setBlockWithNotify(x, y, z, this.blockID);
			} else {
				world.setBlockWithNotify(x, y, z, 0);
			}

			if(isTnt) {
				Block.tnt.onBlockDestroyedByPlayer(world, x, y, z, 1);
			}
		}

	}
	
	private boolean canNeighborBurn(World world, int x, int y, int z) {
		return this.canBlockCatchFire(world, x + 1, y, z) ? true : (this.canBlockCatchFire(world, x - 1, y, z) ? true : (this.canBlockCatchFire(world, x, y - 1, z) ? true : (this.canBlockCatchFire(world, x, y + 1, z) ? true : (this.canBlockCatchFire(world, x, y, z - 1) ? true : this.canBlockCatchFire(world, x, y, z + 1)))));
	}

	private int getChanceOfNeighborsEncouragingFire(World world, int x, int y, int z) {
		if(!world.isAirBlock(x, y, z)) {
			return 0;
		} else {
			int maxChance = this.getChanceToEncourageFire(world, x + 1, y, z, 0);
			maxChance = this.getChanceToEncourageFire(world, x - 1, y, z, maxChance);
			maxChance = this.getChanceToEncourageFire(world, x, y - 1, z, maxChance);
			maxChance = this.getChanceToEncourageFire(world, x, y + 1, z, maxChance);
			maxChance = this.getChanceToEncourageFire(world, x, y, z - 1, maxChance);
			maxChance = this.getChanceToEncourageFire(world, x, y, z + 1, maxChance);
			return maxChance;
		}
	}

	public boolean isCollidable() {
		return false;
	}

	public boolean canBlockCatchFire(IBlockAccess blockAccess, int x, int y, int z) {
		Block block = Block.blocksList[blockAccess.getBlockId(x, y, z)];
		return block != null && block.getEncouragementToFire() > 0;
	}

	public int getChanceToEncourageFire(World world, int x, int y, int z, int currentMax) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		int blockChance = block != null ? block.getEncouragementToFire() : 0;
		return blockChance > currentMax ? blockChance : currentMax;
	}

	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return world.isBlockNormalCube(x, y - 1, z) || this.canNeighborBurn(world, x, y, z);
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		if(!world.isBlockNormalCube(x, y - 1, z) && !this.canNeighborBurn(world, x, y, z)) {
			world.setBlockWithNotify(x, y, z, 0);
		}
	}

	public void onBlockAdded(World world, int x, int y, int z) {
		if(world.getBlockId(x, y - 1, z) != Block.obsidian.blockID || !Block.portal.tryToCreatePortal(world, x, y, z)) {
			if(!world.isBlockNormalCube(x, y - 1, z) && !this.canNeighborBurn(world, x, y, z)) {
				world.setBlockWithNotify(x, y, z, 0);
			} else {
				world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
			}
		}
	}

	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if(rand.nextInt(24) == 0) {
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "fire.fire", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
		}

		int samples;
		float smokeX;
		float smokeY;
		float smokeZ;
		if(!world.isBlockNormalCube(x, y - 1, z) && !Block.fire.canBlockCatchFire(world, x, y - 1, z)) {
			if(Block.fire.canBlockCatchFire(world, x - 1, y, z)) {
				for(samples = 0; samples < 2; ++samples) {
					smokeX = (float)x + rand.nextFloat() * 0.1F;
					smokeY = (float)y + rand.nextFloat();
					smokeZ = (float)z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world, x + 1, y, z)) {
				for(samples = 0; samples < 2; ++samples) {
					smokeX = (float)(x + 1) - rand.nextFloat() * 0.1F;
					smokeY = (float)y + rand.nextFloat();
					smokeZ = (float)z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world, x, y, z - 1)) {
				for(samples = 0; samples < 2; ++samples) {
					smokeX = (float)x + rand.nextFloat();
					smokeY = (float)y + rand.nextFloat();
					smokeZ = (float)z + rand.nextFloat() * 0.1F;
					world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world, x, y, z + 1)) {
				for(samples = 0; samples < 2; ++samples) {
					smokeX = (float)x + rand.nextFloat();
					smokeY = (float)y + rand.nextFloat();
					smokeZ = (float)(z + 1) - rand.nextFloat() * 0.1F;
					world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world, x, y + 1, z)) {
				for(samples = 0; samples < 2; ++samples) {
					smokeX = (float)x + rand.nextFloat();
					smokeY = (float)(y + 1) - rand.nextFloat() * 0.1F;
					smokeZ = (float)z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
				}
			}
		} else {
			for(samples = 0; samples < 3; ++samples) {
				smokeX = (float)x + rand.nextFloat();
				smokeY = (float)y + rand.nextFloat() * 0.5F + 0.5F;
				smokeZ = (float)z + rand.nextFloat();
				world.spawnParticle("largesmoke", (double)smokeX, (double)smokeY, (double)smokeZ, 0.0D, 0.0D, 0.0D);
			}
		}

	}
}