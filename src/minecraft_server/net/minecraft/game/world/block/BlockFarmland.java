package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockFarmland extends Block {
	protected BlockFarmland(int blockID) {
		super(blockID, Material.ground);
		this.blockIndexInTexture = 87;
		this.setTickOnLoad(true);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
		this.setLightOpacity(255);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBoxFromPool((double)(x + 0), (double)(y + 0), (double)(z + 0), (double)(x + 1), (double)(y + 1), (double)(z + 1));
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		return side == 1 && metadata > 0 ? this.blockIndexInTexture - 1 : (side == 1 ? this.blockIndexInTexture : 2);
	}

	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
		if(random5.nextInt(5) == 0) {
			if(!this.isWaterNearby(world1, i2, i3, i4) && !world1.canBlockBeRainedOn(i2, i3 + 1, i4)) {
				int i6 = world1.getBlockMetadata(i2, i3, i4);
				if(i6 > 0) {
					world1.setBlockMetadataWithNotify(i2, i3, i4, i6 - 1);
				} else if(!this.isCropsNearby(world1, i2, i3, i4)) {
					world1.setBlockWithNotify(i2, i3, i4, Block.dirt.blockID);
				}
			} else {
				world1.setBlockMetadataWithNotify(i2, i3, i4, 7);
			}
		}

	}

	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		if(world.rand.nextInt(4) == 0) {
			world.setBlockWithNotify(x, y, z, Block.dirt.blockID);
		}

	}

	private boolean isCropsNearby(World world, int x, int y, int z) {
		byte b5 = 0;

		for(int i6 = x - b5; i6 <= x + b5; ++i6) {
			for(int i7 = z - b5; i7 <= z + b5; ++i7) {
				if(world.getBlockId(i6, y + 1, i7) == Block.crops.blockID) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isWaterNearby(World world, int x, int y, int z) {
		for(int i5 = x - 4; i5 <= x + 4; ++i5) {
			for(int i6 = y; i6 <= y + 1; ++i6) {
				for(int i7 = z - 4; i7 <= z + 4; ++i7) {
					if(world.getBlockMaterial(i5, i6, i7) == Material.water) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		super.onNeighborBlockChange(world, x, y, z, id);
		Material material6 = world.getBlockMaterial(x, y + 1, z);
		if(material6.isSolid()) {
			world.setBlockWithNotify(x, y, z, Block.dirt.blockID);
		}

	}

	public int idDropped(int metadata, Random rand) {
		return Block.dirt.idDropped(0, rand);
	}
	
	public boolean canGrowPlants() {
		return true;
	}
}
