package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;

public class BlockLeaves extends BlockLeavesBase {

	// Leaves are special a bit cumbersome, mostly I have planed this mod for 0 hours before starting.
	// Bits 0-2 are used to colorize the block.
	// Bit 3 is, like in vanilla, the "don't decay" flag. When you PLACE leaves, they don't decay.
	// Bits 7-4 are used to decide which tree these leaves are from, so the right sapling can drop.
	
	public static int OAK = 0;
	public static int BAOBAB = 0x10;
	public static int CYPRESS = 0x20;
	public static int FIR = 0x30;
	public static int JUNGLE = 0x40;
	public static int MANGROVE = 0x50;
	public static int TAIGA = 0x60;
	public static int FANCYFIR = 0x70;
	public static int WILLOW = 0x80;
	public static int SHRUB = 0x90;
	
	private int leafTexIndex;
	public static byte canopyDiameter = 32;
	public static int canopyRadius = canopyDiameter / 2;

	int[] surroundings;

	// Change to fancy colors for fancy trees and use metadata 1..7 (0 means "biome
	// controlled")
	public static int[] fixedColors = { 
			0x5BFB3B, // Normal neon green
			0xF6F535, // Yellower for arid biomes
			0x9BE658, // Average normal / yellowish, dying NORMALs
			0x5BFB3B, 0x5BFB3B, 0x5BFB3B, 0x5BFB3B, 
			0x5BFB3B // This will NEVER be used, it means "Seasonal
					 // colorizer"
	};

	protected BlockLeaves(int id, int blockIndex) {
		super(id, blockIndex, Material.leaves, false);
		this.leafTexIndex = blockIndex;
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		// Small optimization: When replaced with leaves or wood, surrounding leaves are
		// NOT affected
		if (blockID == Block.wood.blockID || blockID == Block.leaves.blockID)
			return;
		
		this.onBlockRemovalDo(world, x, y, z);
	}

	public int quantityDropped(Random Random) {
		return Random.nextInt(20) != 0 ? 0 : 1;
	}

	public int idDropped(int i, Random Random) {
		return Random.nextInt(50) == 0 ? Item.appleRed.shiftedIndex : Block.sapling.blockID;
	}

	protected int damageDropped(int meta) {
		// Damage dropped is used in saplings. The top 4 bits in the meta byte
		// are used to represent tree types.
		return meta & 0xf0;
	}
	
	public boolean isOpaqueCube() {
		return !this.graphicsLevel;
	}

	public void setGraphicsLevel(boolean flag) {
		this.graphicsLevel = flag;
		this.blockIndexInTexture = this.leafTexIndex + (flag ? 0 : 1);
	}

	public void onEntityWalking(World world, int i, int j, int k, Entity entity) {
		super.onEntityWalking(world, i, j, k, entity);
	}

	public void onBlockRemoval(World world, int x, int y, int z) {
		int blockID = world.getBlockId(x, y, z);

		// Small optimization: When replaced with leaves or wood, surrounding leaves are
		// NOT affected
		if (blockID == Block.wood.blockID || blockID == Block.leaves.blockID)
			return;
		
		this.onBlockRemovalDo(world, x, y, z);
	}

	public void onBlockRemovalDo(World world, int x, int y, int z) {
		byte radius = 1;
		int range = radius + 1;

		if (world.checkChunksExist(x - range, y - range, z - range, x + range, y + range, z + range)) {
			for (int xx = -radius; xx <= radius; ++xx) {
				for (int yy = -radius; yy <= radius; ++yy) {
					for (int zz = -radius; zz <= radius; ++zz) {
						int i2 = world.getBlockId(x + xx, y + yy, z + zz);
						if (i2 == this.blockID) {
							int j2 = world.getBlockMetadata(x + xx, y + yy, z + zz);
							world.setBlockMetadata(x + xx, y + yy, z + zz, j2 | 8);
						}
					}
				}
			}
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		this.updateTick17(world, x, y, z, random);
	}

	public void updateTick17(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int metadata = world.getBlockMetadata(x, y, z);

			// Is leaf marked to be checked?
			if ((metadata & 8) != 0) {
				if (this.surroundings == null) {
					this.surroundings = new int[32768];
				}

				if (world.checkChunksExist(x - 5, y - 5, z - 5, x + 5, y + 5, z + 5)) {

					for (int xx = -4; xx <= 4; ++xx) {
						for (int yy = -4; yy <= 4; ++yy) {
							for (int zz = -4; zz <= 4; ++zz) {
								Block block = Block.blocksList[world.getBlockId(x + xx, y + yy, z + zz)];

								if (block == null || block.blockMaterial != Material.wood) {
									if (block instanceof BlockLeaves) {
										this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + (zz + 16)] = -2;
									} else {
										this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + (zz + 16)] = -1;
									}
								} else {
									this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + (zz + 16)] = 0;
								}
							}
						}
					}

					for (int density = 1; density <= 4; ++density) {
						for (int xx = -4; xx <= 4; ++xx) {
							for (int yy = -4; yy <= 4; ++yy) {
								for (int zz = -4; zz <= 4; ++zz) {
									if (this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + zz + 16] == density
											- 1) {
										if (this.surroundings[((xx + 16 - 1) << 10) + ((yy + 16) << 5) + zz	+ 16] == -2) {
											this.surroundings[((xx + 16 - 1) << 10) + ((yy + 16) << 5) + zz	+ 16] = density;
										}

										if (this.surroundings[((xx + 16 + 1) << 10) + ((yy + 16) << 5) + zz + 16] == -2) {
											this.surroundings[((xx + 16 + 1) << 10) + ((yy + 16) << 5) + zz	+ 16] = density;
										}

										if (this.surroundings[((xx + 16) << 10) + ((yy + 16 - 1) << 5) + zz	+ 16] == -2) {
											this.surroundings[((xx + 16) << 10) + ((yy + 16 - 1) << 5) + zz	+ 16] = density;
										}

										if (this.surroundings[((xx + 16) << 10) + ((yy + 16 + 1) << 5) + zz	+ 16] == -2) {
											this.surroundings[((xx + 16) << 10) + ((yy + 16 + 1) << 5) + zz	+ 16] = density;
										}

										if (this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5)
												+ (zz + 16 - 1)] == -2) {
											this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5)
													+ (zz + 16 - 1)] = density;
										}

										if (this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + zz + 16 + 1] == -2) {
											this.surroundings[((xx + 16) << 10) + ((yy + 16) << 5) + zz + 16 + 1] = density;
										}
									}
								}
							}
						}
					}
				}

				int l2 = this.surroundings[16912];

				if (l2 >= 0) {
					world.setBlockMetadata(x, y, z, metadata & -9); // Clear bit 3
				} else {
					this.removeLeaves(world, x, y, z);
				}
			}
		}
	}

	public void updateTickBeta(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int metadata = world.getBlockMetadata(x, y, z);

			// Is leaf marked to be checked?
			if ((metadata & 8) != 0) {
				byte radius = 6;
				int range = radius + 1;

				if (this.surroundings == null) {
					this.surroundings = new int[canopyDiameter * canopyDiameter * canopyDiameter];
				}

				int density;
				if (world.checkChunksExist(x - range, y - range, z - range, x + range, y + range, z + range)) {
					density = -radius;

					label112: while (true) {
						int xx;
						int yy;
						int zz;
						if (density > radius) {
							density = 1;

							while (true) {
								if (density > 4) {
									break label112;
								}

								for (xx = -radius; xx <= radius; ++xx) {
									for (yy = -radius; yy <= radius; ++yy) {
										for (zz = -radius; zz <= radius; ++zz) {
											if (this.surroundings[((xx + canopyRadius) << 10)
													+ ((yy + canopyRadius) << 5) + zz + canopyRadius] == density - 1) {
												if (this.surroundings[((xx + canopyRadius - 1) << 10)
														+ ((yy + canopyRadius) << 5) + zz + canopyRadius] == -2) {
													this.surroundings[((xx + canopyRadius - 1) << 10)
															+ ((yy + canopyRadius) << 5) + zz + canopyRadius] = density;
												}

												if (this.surroundings[((xx + canopyRadius + 1) << 10)
														+ ((yy + canopyRadius) << 5) + zz + canopyRadius] == -2) {
													this.surroundings[((xx + canopyRadius + 1) << 10)
															+ ((yy + canopyRadius) << 5) + zz + canopyRadius] = density;
												}

												if (this.surroundings[((xx + canopyRadius) << 10)
														+ ((yy + canopyRadius - 1) << 5) + zz + canopyRadius] == -2) {
													this.surroundings[((xx + canopyRadius) << 10)
															+ ((yy + canopyRadius - 1) << 5) + zz
															+ canopyRadius] = density;
												}

												if (this.surroundings[((xx + canopyRadius) << 10)
														+ ((yy + canopyRadius + 1) << 5) + zz + canopyRadius] == -2) {
													this.surroundings[((xx + canopyRadius) << 10)
															+ ((yy + canopyRadius + 1) << 5) + zz
															+ canopyRadius] = density;
												}

												if (this.surroundings[((xx + canopyRadius) << 10)
														+ ((yy + canopyRadius) << 5) + (zz + canopyRadius - 1)] == -2) {
													this.surroundings[((xx + canopyRadius) << 10)
															+ ((yy + canopyRadius) << 5)
															+ (zz + canopyRadius - 1)] = density;
												}

												if (this.surroundings[((xx + canopyRadius) << 10)
														+ ((yy + canopyRadius) << 5) + zz + canopyRadius + 1] == -2) {
													this.surroundings[((xx + canopyRadius) << 10)
															+ ((yy + canopyRadius) << 5) + zz + canopyRadius
															+ 1] = density;
												}
											}
										}
									}
								}

								++density;
							}
						}

						for (xx = -radius; xx <= radius; ++xx) {
							for (yy = -radius; yy <= radius; ++yy) {
								zz = world.getBlockId(x + density, y + xx, z + yy);
								Block block = Block.blocksList[zz];
								if (block != null && block instanceof BlockLog) {
									this.surroundings[((density + canopyRadius) << 10) + ((xx + canopyRadius) << 5) + yy
											+ canopyRadius] = 0;
								} else if (zz == this.blockID) {
									this.surroundings[((density + canopyRadius) << 10) + ((xx + canopyRadius) << 5) + yy
											+ canopyRadius] = -2;
								} else {
									this.surroundings[((density + canopyRadius) << 10) + ((xx + canopyRadius) << 5) + yy
											+ canopyRadius] = -1;
								}
							}
						}

						++density;
					}
				}

				density = this.surroundings[canopyRadius << 10 | canopyRadius << 5 | canopyRadius];
				if (density >= 0) {
					world.setBlockMetadata(x, y, z, metadata & -9); // Clear bit 3
				} else {
					this.removeLeaves(world, x, y, z);
				}
			}
		}
	}

	private void removeLeaves(World world, int i, int j, int k) {
		this.dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k));
		world.setBlockWithNotify(i, j, k, 0);
	}

	// Not as complex as a colorizer, but allows for some freedom!
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return this.getRenderColor(blockAccess.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta) {
		meta &= 7;
		if (meta == 7)
			return Seasons.getLeavesColorForToday();
		return BlockLeaves.fixedColors[meta];
	}

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 2; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}
    
	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == Item.shears.shiftedIndex) {
			this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.leaves, 1, world.getBlockMetadata(x, y, z)));
			world.setBlockWithNotify(x, y, z, 0);
			world.playSoundEffect((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, this.stepSound.getStepSound(), (this.stepSound.getVolume() + 1.0F) / 8.0F, this.stepSound.getPitch() * 0.5F);
			return true;
		}
		
		return false;
	}
}
