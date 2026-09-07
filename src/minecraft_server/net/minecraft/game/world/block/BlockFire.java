package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDarkForest;

public class BlockFire extends Block {
	private int[] chanceToEncourageFire = new int[256];
	private int[] abilityToCatchFire = new int[256];

	protected BlockFire(int i1, int i2) {
		super(i1, i2, Material.fire);
		this.setTickOnLoad(true);
	}

	public void initializeBlock() {
		this.setBurnRate(Block.planks.blockID, 5, 20);
		this.setBurnRate(Block.fence.blockID, 5, 20);
		this.setBurnRate(Block.stairCompactPlanks.blockID, 5, 20);
		this.setBurnRate(Block.wood.blockID, 5, 5);
		this.setBurnRate(Block.chippedWood.blockID, 5, 5);
		this.setBurnRate(Block.hollowLog.blockID, 5, 5);
		this.setBurnRate(Block.leaves.blockID, 30, 60);
		this.setBurnRate(Block.bookShelf.blockID, 30, 20);
		this.setBurnRate(Block.tnt.blockID, 15, 100);
		this.setBurnRate(Block.tallGrass.blockID, 60, 100);
		this.setBurnRate(Block.cloth.blockID, 30, 60);
	}

	private void setBurnRate(int i1, int i2, int i3) {
		this.chanceToEncourageFire[i1] = i2;
		this.abilityToCatchFire[i1] = i3;
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
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

	public int quantityDropped(Random random1) {
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
			int i6 = world.getBlockMetadata(x, y, z);
			if(i6 < 15) {
				world.setBlockMetadata(x, y, z, i6 + rand.nextInt(3) / 2);
			}

			world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
			if(!superFuelBeneath && !this.canNeighborBurn(world, x, y, z)) {
				if(!world.isBlockNormalCube(x, y - 1, z) || i6 > 3) {
					world.setBlockWithNotify(x, y, z, 0);
				}

			} else if(!superFuelBeneath && !this.canBlockCatchFire(world, x, y - 1, z) && i6 == 15 && rand.nextInt(4) == 0) {
				world.setBlockWithNotify(x, y, z, 0);
			} else {
				if(i6 % 2 == 0 && i6 > 2) {
					this.tryToCatchBlockOnFire(world, x + 1, y, z, 300, rand, i6);
					this.tryToCatchBlockOnFire(world, x - 1, y, z, 300, rand, i6);
					this.tryToCatchBlockOnFire(world, x, y - 1, z, 250, rand, i6);
					this.tryToCatchBlockOnFire(world, x, y + 1, z, 250, rand, i6);
					this.tryToCatchBlockOnFire(world, x, y, z - 1, 300, rand, i6);
					this.tryToCatchBlockOnFire(world, x, y, z + 1, 300, rand, i6);
	
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

	private void tryToCatchBlockOnFire(World world, int x, int y, int z, int chance, Random rand, int dist) {
		int i7 = this.abilityToCatchFire[world.getBlockId(x, y, z)];
		if(world.raining() || world.snowing()) i7 >>= 2;
		if(world.getBiomeGenAt(x, z).isHumid()) i7 >>= 1;
		if(rand.nextInt(chance) < i7) {
			boolean z8 = world.getBlockId(x, y, z) == Block.tnt.blockID;
			if(rand.nextInt(2) == 0) {
				world.setBlockWithNotify(x, y, z, this.blockID);
			} else {
				world.setBlockWithNotify(x, y, z, 0);
			}

			if(z8) {
				Block.tnt.onBlockDestroyedByPlayer(world, x, y, z, 1);
			}
		}

	}
	
	// Beta 1.6 code
	/*
	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
		boolean z6 = world1.getBlockId(i2, i3 - 1, i4) == Block.bloodStone.blockID;
		if(!this.canPlaceBlockAt(world1, i2, i3, i4)) {
			world1.setBlockWithNotify(i2, i3, i4, 0);
		}

		if(!z6 && world1.raining() && (world1.canBlockBeRainedOn(i2, i3, i4) || world1.canBlockBeRainedOn(i2 - 1, i3, i4) || world1.canBlockBeRainedOn(i2 + 1, i3, i4) || world1.canBlockBeRainedOn(i2, i3, i4 - 1) || world1.canBlockBeRainedOn(i2, i3, i4 + 1))) {
			world1.setBlockWithNotify(i2, i3, i4, 0);
		} else {
			int i7 = world1.getBlockMetadata(i2, i3, i4);
			if(i7 < 15) {
				world1.setBlockMetadata(i2, i3, i4, i7 + random5.nextInt(3) / 2);
			}

			world1.scheduleBlockUpdate(i2, i3, i4, this.blockID, this.tickRate());
			if(!z6 && !this.canNeighborBurn(world1, i2, i3, i4)) {
				if(!world1.isBlockNormalCube(i2, i3 - 1, i4) || i7 > 3) {
					world1.setBlockWithNotify(i2, i3, i4, 0);
				}

			} else if(!z6 && !this.canBlockCatchFire(world1, i2, i3 - 1, i4) && i7 == 15 && random5.nextInt(4) == 0) {
				world1.setBlockWithNotify(i2, i3, i4, 0);
			} else {
				this.tryToCatchBlockOnFire(world1, i2 + 1, i3, i4, 300, random5, i7);
				this.tryToCatchBlockOnFire(world1, i2 - 1, i3, i4, 300, random5, i7);
				this.tryToCatchBlockOnFire(world1, i2, i3 - 1, i4, 250, random5, i7);
				this.tryToCatchBlockOnFire(world1, i2, i3 + 1, i4, 250, random5, i7);
				this.tryToCatchBlockOnFire(world1, i2, i3, i4 - 1, 300, random5, i7);
				this.tryToCatchBlockOnFire(world1, i2, i3, i4 + 1, 300, random5, i7);

				for(int i8 = i2 - 1; i8 <= i2 + 1; ++i8) {
					for(int i9 = i4 - 1; i9 <= i4 + 1; ++i9) {
						for(int i10 = i3 - 1; i10 <= i3 + 4; ++i10) {
							if(i8 != i2 || i10 != i3 || i9 != i4) {
								int i11 = 100;
								if(i10 > i3 + 1) {
									i11 += (i10 - (i3 + 1)) * 100;
								}

								int i12 = this.getChanceOfNeighborsEncouragingFire(world1, i8, i10, i9);
								if(i12 > 0) {
									int i13 = (i12 + 40) / (i7 + 30);
									if(i13 > 0 && random5.nextInt(i11) <= i13 && (!world1.raining() || !world1.canBlockBeRainedOn(i8, i10, i9)) && !world1.canBlockBeRainedOn(i8 - 1, i10, i4) && !world1.canBlockBeRainedOn(i8 + 1, i10, i9) && !world1.canBlockBeRainedOn(i8, i10, i9 - 1) && !world1.canBlockBeRainedOn(i8, i10, i9 + 1)) {
										int i14 = i7 + random5.nextInt(5) / 4;
										if(i14 > 15) {
											i14 = 15;
										}

										world1.setBlockAndMetadataWithNotify(i8, i10, i9, this.blockID, i14);
									}
								}
							}
						}
					}
				}

			}
		}
	}

	private void tryToCatchBlockOnFire(World world1, int i2, int i3, int i4, int i5, Random random6, int i7) {
		int i8 = this.abilityToCatchFire[world1.getBlockId(i2, i3, i4)];
		if(random6.nextInt(i5) < i8) {
			boolean z9 = world1.getBlockId(i2, i3, i4) == Block.tnt.blockID;
			if(random6.nextInt(i7 + 10) < 5 && !world1.canBlockBeRainedOn(i2, i3, i4)) {
				int i10 = i7 + random6.nextInt(5) / 4;
				if(i10 > 15) {
					i10 = 15;
				}

				world1.setBlockAndMetadataWithNotify(i2, i3, i4, this.blockID, i10);
			} else {
				world1.setBlockWithNotify(i2, i3, i4, 0);
			}

			if(z9) {
				Block.tnt.onBlockDestroyedByPlayer(world1, i2, i3, i4, 1);
			}
		}

	}
	*/

	private boolean canNeighborBurn(World world1, int i2, int i3, int i4) {
		return this.canBlockCatchFire(world1, i2 + 1, i3, i4) ? true : (this.canBlockCatchFire(world1, i2 - 1, i3, i4) ? true : (this.canBlockCatchFire(world1, i2, i3 - 1, i4) ? true : (this.canBlockCatchFire(world1, i2, i3 + 1, i4) ? true : (this.canBlockCatchFire(world1, i2, i3, i4 - 1) ? true : this.canBlockCatchFire(world1, i2, i3, i4 + 1)))));
	}

	private int getChanceOfNeighborsEncouragingFire(World world1, int i2, int i3, int i4) {
		byte b5 = 0;
		if(!world1.isAirBlock(i2, i3, i4)) {
			return 0;
		} else {
			int i6 = this.getChanceToEncourageFire(world1, i2 + 1, i3, i4, b5);
			i6 = this.getChanceToEncourageFire(world1, i2 - 1, i3, i4, i6);
			i6 = this.getChanceToEncourageFire(world1, i2, i3 - 1, i4, i6);
			i6 = this.getChanceToEncourageFire(world1, i2, i3 + 1, i4, i6);
			i6 = this.getChanceToEncourageFire(world1, i2, i3, i4 - 1, i6);
			i6 = this.getChanceToEncourageFire(world1, i2, i3, i4 + 1, i6);
			return i6;
		}
	}

	public boolean isCollidable() {
		return false;
	}

	public boolean canBlockCatchFire(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return this.chanceToEncourageFire[iBlockAccess1.getBlockId(i2, i3, i4)] > 0;
	}

	public int getChanceToEncourageFire(World world1, int i2, int i3, int i4, int i5) {
		int i6 = this.chanceToEncourageFire[world1.getBlockId(i2, i3, i4)];
		return i6 > i5 ? i6 : i5;
	}

	public boolean canPlaceBlockAt(World world1, int i2, int i3, int i4) {
		return world1.isBlockNormalCube(i2, i3 - 1, i4) || this.canNeighborBurn(world1, i2, i3, i4);
	}

	public void onNeighborBlockChange(World world1, int i2, int i3, int i4, int i5) {
		if(!world1.isBlockNormalCube(i2, i3 - 1, i4) && !this.canNeighborBurn(world1, i2, i3, i4)) {
			world1.setBlockWithNotify(i2, i3, i4, 0);
		}
	}

	public void onBlockAdded(World world1, int i2, int i3, int i4) {
		if(world1.getBlockId(i2, i3 - 1, i4) != Block.obsidian.blockID || !Block.portal.tryToCreatePortal(world1, i2, i3, i4)) {
			if(!world1.isBlockNormalCube(i2, i3 - 1, i4) && !this.canNeighborBurn(world1, i2, i3, i4)) {
				world1.setBlockWithNotify(i2, i3, i4, 0);
			} else {
				world1.scheduleBlockUpdate(i2, i3, i4, this.blockID, this.tickRate());
			}
		}
	}

	public void randomDisplayTick(World world1, int i2, int i3, int i4, Random random5) {
		if(random5.nextInt(24) == 0) {
			world1.playSoundEffect((double)((float)i2 + 0.5F), (double)((float)i3 + 0.5F), (double)((float)i4 + 0.5F), "fire.fire", 1.0F + random5.nextFloat(), random5.nextFloat() * 0.7F + 0.3F);
		}

		int i6;
		float f7;
		float f8;
		float f9;
		if(!world1.isBlockNormalCube(i2, i3 - 1, i4) && !Block.fire.canBlockCatchFire(world1, i2, i3 - 1, i4)) {
			if(Block.fire.canBlockCatchFire(world1, i2 - 1, i3, i4)) {
				for(i6 = 0; i6 < 2; ++i6) {
					f7 = (float)i2 + random5.nextFloat() * 0.1F;
					f8 = (float)i3 + random5.nextFloat();
					f9 = (float)i4 + random5.nextFloat();
					world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world1, i2 + 1, i3, i4)) {
				for(i6 = 0; i6 < 2; ++i6) {
					f7 = (float)(i2 + 1) - random5.nextFloat() * 0.1F;
					f8 = (float)i3 + random5.nextFloat();
					f9 = (float)i4 + random5.nextFloat();
					world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world1, i2, i3, i4 - 1)) {
				for(i6 = 0; i6 < 2; ++i6) {
					f7 = (float)i2 + random5.nextFloat();
					f8 = (float)i3 + random5.nextFloat();
					f9 = (float)i4 + random5.nextFloat() * 0.1F;
					world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world1, i2, i3, i4 + 1)) {
				for(i6 = 0; i6 < 2; ++i6) {
					f7 = (float)i2 + random5.nextFloat();
					f8 = (float)i3 + random5.nextFloat();
					f9 = (float)(i4 + 1) - random5.nextFloat() * 0.1F;
					world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
				}
			}

			if(Block.fire.canBlockCatchFire(world1, i2, i3 + 1, i4)) {
				for(i6 = 0; i6 < 2; ++i6) {
					f7 = (float)i2 + random5.nextFloat();
					f8 = (float)(i3 + 1) - random5.nextFloat() * 0.1F;
					f9 = (float)i4 + random5.nextFloat();
					world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
				}
			}
		} else {
			for(i6 = 0; i6 < 3; ++i6) {
				f7 = (float)i2 + random5.nextFloat();
				f8 = (float)i3 + random5.nextFloat() * 0.5F + 0.5F;
				f9 = (float)i4 + random5.nextFloat();
				world1.spawnParticle("largesmoke", (double)f7, (double)f8, (double)f9, 0.0D, 0.0D, 0.0D);
			}
		}

	}
}
