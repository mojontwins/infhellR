package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.entity.misc.EntityItem;

public class BlockCrops extends BlockFlower {
	protected BlockCrops(int i1, int i2) {
		super(i1, i2);
		this.blockIndexInTexture = i2;
		this.setTickOnLoad(true);
		float f3 = 0.5F;
		this.setBlockBounds(0.5F - f3, 0.0F, 0.5F - f3, 0.5F + f3, 0.25F, 0.5F + f3);
	}

	protected boolean canThisPlantGrowOnThisBlockID(int blockID) {
		return blockID == Block.tilledField.blockID;
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, x, y, z, rand);
		if(world.getBlockLightValue(x, y + 1, z) >= 8) {
			int i6 = world.getBlockMetadata(x, y, z);
			if(i6 < 7) {
				float f7 = this.getGrowthRate(world, x, y, z);
				//if(rand.nextInt((int)(100.0F / f7)) == 0) {
				if(rand.nextInt((int)(80.0F / f7)) == 0) {
					++i6;
					world.setBlockMetadataWithNotify(x, y, z, i6);
				}
			}
		}

	}

	public void fertilize(World world1, int i2, int i3, int i4) {
		world1.setBlockMetadataWithNotify(i2, i3, i4, 7);
	}

	private float getGrowthRate(World world, int x, int y, int z) {
		float f5 = 1.0F;
		int i6 = world.getBlockId(x, y, z - 1);
		int i7 = world.getBlockId(x, y, z + 1);
		int i8 = world.getBlockId(x - 1, y, z);
		int i9 = world.getBlockId(x + 1, y, z);
		int i10 = world.getBlockId(x - 1, y, z - 1);
		int i11 = world.getBlockId(x + 1, y, z - 1);
		int i12 = world.getBlockId(x + 1, y, z + 1);
		int i13 = world.getBlockId(x - 1, y, z + 1);
		boolean z14 = i8 == this.blockID || i9 == this.blockID;
		boolean z15 = i6 == this.blockID || i7 == this.blockID;
		boolean z16 = i10 == this.blockID || i11 == this.blockID || i12 == this.blockID || i13 == this.blockID;

		for(int i17 = x - 1; i17 <= x + 1; ++i17) {
			for(int i18 = z - 1; i18 <= z + 1; ++i18) {
				int i19 = world.getBlockId(i17, y - 1, i18);
				float f20 = 0.0F;
				if(i19 == Block.tilledField.blockID) {
					f20 = 1.0F;
					if(world.getBlockMetadata(i17, y - 1, i18) > 0) {
						f20 = 3.0F;
					}
				}

				if(i17 != x || i18 != z) {
					f20 /= 4.0F;
				}

				f5 += f20;
			}
		}

		if(z16 || z14 && z15) {
			f5 /= 2.0F;
		}

		return f5;
	}

	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		if(metadata < 0) {
			metadata = 7;
		}

		return this.blockIndexInTexture + metadata;
	}

	public int getRenderType() {
		return 6;
	}

	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int metadata) {
		super.onBlockDestroyedByPlayer(world, x, y, z, metadata);
		
		if(!world.isRemote) {
			for(int i7 = 0; i7 < 3; ++i7) {
				if(world.rand.nextInt(15) <= i7) {
					float f8 = 0.7F;
					float f9 = world.rand.nextFloat() * f8 + (1.0F - f8) * 0.5F;
					float f10 = world.rand.nextFloat() * f8 + (1.0F - f8) * 0.5F;
					float f11 = world.rand.nextFloat() * f8 + (1.0F - f8) * 0.5F;
					EntityItem entityItem12 = new EntityItem(world, (double)((float)x + f9), (double)((float)y + f10), (double)((float)z + f11), new ItemStack(Item.seeds));
					entityItem12.delayBeforeCanPickup = 10;
					world.spawnEntityInWorld(entityItem12);
				}
			}

		}
	}

	public int idDropped(int metadata, Random rand) {		
		return metadata == 7 ? Item.wheat.shiftedIndex : -1;
	}

	public int quantityDropped(Random rand) {
		return 1;
	}
	
	public boolean seeThrough() {
		return true; 
	}
}
