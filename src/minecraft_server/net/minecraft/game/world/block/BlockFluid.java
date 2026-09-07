package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.IWaterMob;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public abstract class BlockFluid extends Block {
	protected BlockFluid(int i1, Material material2) {
		super(i1, (material2 == Material.lava ? 14 : 12) * 16 + 13, material2);
		float f3 = 0.0F;
		float f4 = 0.0F;
		this.setBlockBounds(0.0F + f4, 0.0F + f3, 0.0F + f4, 1.0F + f4, 1.0F + f3, 1.0F + f4);
		this.setTickOnLoad(true);
	}

	public int colorMultiplier(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return 0xFFFFFF;
	}

	public static float getPercentAir(int i0) {
		if(i0 >= 8) {
			i0 = 0;
		}

		float f1 = (float)(i0 + 1) / 9.0F;
		return f1;
	}

	public int getBlockTextureFromSide(int i1) {
		return i1 != 0 && i1 != 1 ? this.blockIndexInTexture + 1 : this.blockIndexInTexture;
	}

	protected int getFlowDecay(World world1, int i2, int i3, int i4) {
		return world1.getBlockMaterial(i2, i3, i4) != this.blockMaterial ? -1 : world1.getBlockMetadata(i2, i3, i4);
	}

	protected int getEffectiveFlowDecay(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		if(iBlockAccess1.getBlockMaterial(i2, i3, i4) != this.blockMaterial) {
			return -1;
		} else {
			int i5 = iBlockAccess1.getBlockMetadata(i2, i3, i4);
			if(i5 >= 8) {
				i5 = 0;
			}

			return i5;
		}
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean canCollideCheck(int i1, boolean z2) {
		return z2 && i1 == 0;
	}

	public boolean getIsBlockSolid(IBlockAccess iBlockAccess1, int i2, int i3, int i4, int i5) {
		Material material6 = iBlockAccess1.getBlockMaterial(i2, i3, i4);
		return material6 == this.blockMaterial ? false : (material6 == Material.ice ? false : (i5 == 1 ? true : super.getIsBlockSolid(iBlockAccess1, i2, i3, i4, i5)));
	}

	public boolean shouldSideBeRendered(IBlockAccess iBlockAccess1, int i2, int i3, int i4, int i5) {
		Material material6 = iBlockAccess1.getBlockMaterial(i2, i3, i4);
		return material6 == this.blockMaterial ? false : (material6 == Material.ice ? false : (i5 == 1 ? true : super.shouldSideBeRendered(iBlockAccess1, i2, i3, i4, i5)));
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		return null;
	}

	public int getRenderType() {
		return 4;
	}

	public int idDropped(int i1, Random random2) {
		return 0;
	}

	public int quantityDropped(Random random1) {
		return 0;
	}

	private Vec3D getFlowVector(IBlockAccess blockAccess, int x, int y, int z) {
		Vec3D vector = Vec3D.createVector(0.0D, 0.0D, 0.0D);

		int thisFlowDecay = this.getEffectiveFlowDecay(blockAccess, x, y, z);

		for(int side = 0; side < 4; ++side) {
			int xc = x;
			int zc = z;
			if(side == 0) {
				xc = x - 1;
			}

			if(side == 1) {
				zc = z - 1;
			}

			if(side == 2) {
				++xc;
			}

			if(side == 3) {
				++zc;
			}

			int otherFlowDecay = this.getEffectiveFlowDecay(blockAccess, xc, y, zc);
			int ratio;
			if(otherFlowDecay < 0) {
				if(!blockAccess.getBlockMaterial(xc, y, zc).getIsSolid()) {
					otherFlowDecay = this.getEffectiveFlowDecay(blockAccess, xc, y - 1, zc);
					if(otherFlowDecay >= 0) {
						ratio = otherFlowDecay - (thisFlowDecay - 8);
						vector = vector.addVector((double)((xc - x) * ratio), (double)((y - y) * ratio), (double)((zc - z) * ratio));
					}
				}
			} else if(otherFlowDecay >= 0) {
				ratio = otherFlowDecay - thisFlowDecay;
				vector = vector.addVector((double)((xc - x) * ratio), (double)((y - y) * ratio), (double)((zc - z) * ratio));
			}
		}

		if(blockAccess.getBlockMetadata(x, y, z) >= 8) {
			boolean z13 = false;
			if(z13 || this.getIsBlockSolid(blockAccess, x, y, z - 1, 2)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x, y, z + 1, 3)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x - 1, y, z, 4)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x + 1, y, z, 5)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x, y + 1, z - 1, 2)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x, y + 1, z + 1, 3)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x - 1, y + 1, z, 4)) {
				z13 = true;
			}

			if(z13 || this.getIsBlockSolid(blockAccess, x + 1, y + 1, z, 5)) {
				z13 = true;
			}

			if(z13) {
				vector = vector.normalize().addVector(0.0D, -6.0D, 0.0D);
			}
		}

		vector = vector.normalize();
		
		return vector;
	}

	public void velocityToAddToEntity(World world1, int i2, int i3, int i4, Entity entity5, Vec3D vec3D6) {
		Vec3D vec3D7 = this.getFlowVector(world1, i2, i3, i4);
		vec3D6.xCoord += vec3D7.xCoord;
		if(!(entity5 instanceof IWaterMob)) vec3D6.yCoord += vec3D7.yCoord;
		vec3D6.zCoord += vec3D7.zCoord;
	}

	public int tickRate() {
		return this.blockMaterial == Material.water ? 5 : (this.blockMaterial == Material.lava ? 30 : 0);
	}

	public float getBlockBrightness(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		float f5 = iBlockAccess1.getLightBrightness(i2, i3, i4);
		float f6 = iBlockAccess1.getLightBrightness(i2, i3 + 1, i4);
		return f5 > f6 ? f5 : f6;
	}

	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
		super.updateTick(world1, i2, i3, i4, random5);
	}

	public int getRenderBlockPass() {
		return this.blockMaterial == Material.water ? 1 : 0;
	}

	public void randomDisplayTick(World world1, int i2, int i3, int i4, Random random5) {
		if(this.blockMaterial == Material.water && random5.nextInt(64) == 0) {
			int i6 = world1.getBlockMetadata(i2, i3, i4);
			if(i6 > 0 && i6 < 8) {
				world1.playSoundEffect((double)((float)i2 + 0.5F), (double)((float)i3 + 0.5F), (double)((float)i4 + 0.5F), "liquid.water", random5.nextFloat() * 0.25F + 0.75F, random5.nextFloat() * 1.0F + 0.5F);
			}
		}

		if(this.blockMaterial == Material.lava && world1.getBlockMaterial(i2, i3 + 1, i4) == Material.air && !world1.isBlockOpaqueCube(i2, i3 + 1, i4) && random5.nextInt(100) == 0) {
			double d12 = (double)((float)i2 + random5.nextFloat());
			double d8 = (double)i3 + this.maxY;
			double d10 = (double)((float)i4 + random5.nextFloat());
			world1.spawnParticle("lava", d12, d8, d10, 0.0D, 0.0D, 0.0D);
		}

	}

	public static double func_293_a(IBlockAccess iBlockAccess0, int i1, int i2, int i3, Material material4) {
		Vec3D vec3D5 = null;
		if(material4 == Material.water) {
			vec3D5 = ((BlockFluid)Block.waterMoving).getFlowVector(iBlockAccess0, i1, i2, i3);
		}

		if(material4 == Material.lava) {
			vec3D5 = ((BlockFluid)Block.lavaMoving).getFlowVector(iBlockAccess0, i1, i2, i3);
		}

		return vec3D5.xCoord == 0.0D && vec3D5.zCoord == 0.0D ? -1000.0D : Math.atan2(vec3D5.zCoord, vec3D5.xCoord) - Math.PI / 2D;
	}

	public void onBlockAdded(World world1, int i2, int i3, int i4) {
		this.checkForHarden(world1, i2, i3, i4);
	}

	public void onNeighborBlockChange(World world1, int i2, int i3, int i4, int i5) {
		this.checkForHarden(world1, i2, i3, i4);
	}

	private void checkForHarden(World world, int x, int y, int z) {
		if(world.getBlockId(x, y, z) == this.blockID) {
			if(this.blockMaterial == Material.lava) {
				boolean doHarden = false;
				if(doHarden || world.getBlockMaterial(x, y, z - 1) == Material.water) {
					doHarden = true;
				}

				if(doHarden || world.getBlockMaterial(x, y, z + 1) == Material.water) {
					doHarden = true;
				}

				if(doHarden || world.getBlockMaterial(x - 1, y, z) == Material.water) {
					doHarden = true;
				}

				if(doHarden || world.getBlockMaterial(x + 1, y, z) == Material.water) {
					doHarden = true;
				}

				if(doHarden || world.getBlockMaterial(x, y + 1, z) == Material.water) {
					doHarden = true;
				}

				if(doHarden) {
					int metadata = world.getBlockMetadata(x, y, z);
					if(metadata == 0) {
						if(world.rand.nextInt(100) == 0) {
							world.setBlockWithNotify(x, y, z, Block.cryingObsidian.blockID);
						} else {
							world.setBlockWithNotify(x, y, z, Block.obsidian.blockID);
						}
					} else if(metadata <= 4) {
						world.setBlockWithNotify(x, y, z, Block.cobblestone.blockID);
					}

					this.triggerLavaMixEffects(world, x, y, z);
				}
			}

		}
	}

	protected void triggerLavaMixEffects(World world1, int i2, int i3, int i4) {
		world1.playSoundEffect((double)((float)i2 + 0.5F), (double)((float)i3 + 0.5F), (double)((float)i4 + 0.5F), "random.fizz", 0.5F, 2.6F + (world1.rand.nextFloat() - world1.rand.nextFloat()) * 0.8F);

		for(int i5 = 0; i5 < 8; ++i5) {
			world1.spawnParticle("largesmoke", (double)i2 + Math.random(), (double)i3 + 1.2D, (double)i4 + Math.random(), 0.0D, 0.0D, 0.0D);
		}

	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	public boolean getBlocksMovement(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return this.blockMaterial != Material.lava;
	}
}
