package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.material.Material;

public class BlockDoor extends Block {
	protected BlockDoor(int i1, Material material2) {
		super(i1, material2);
		this.blockIndexInTexture = 97;
		if(material2 == Material.iron) {
			++this.blockIndexInTexture;
		}

		float f3 = 0.5F;
		float f4 = 1.0F;
		this.setBlockBounds(0.5F - f3, 0.0F, 0.5F - f3, 0.5F + f3, f4, 0.5F + f3);
	}

	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		if(side != 0 && side != 1) {
			int i3 = this.getState(metadata);
			if((i3 == 0 || i3 == 2) ^ side <= 3) {
				return this.blockIndexInTexture;
			} else {
				int i4 = i3 / 2 + (side & 1 ^ i3);
				i4 += (metadata & 4) / 4;
				int i5 = this.blockIndexInTexture - (metadata & 8) * 2;
				if((i4 & 1) != 0) {
					i5 = -i5;
				}

				return i5;
			}
		} else {
			return this.blockIndexInTexture;
		}
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 7;
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		this.setDoorRotation(this.getState(blockAccess.getBlockMetadata(x, y, z)));
	}

	public void setDoorRotation(int metadata) {
		float f2 = 0.1875F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
		if(metadata == 0) {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f2);
		}

		if(metadata == 1) {
			this.setBlockBounds(1.0F - f2, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

		if(metadata == 2) {
			this.setBlockBounds(0.0F, 0.0F, 1.0F - f2, 1.0F, 1.0F, 1.0F);
		}

		if(metadata == 3) {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, f2, 1.0F, 1.0F);
		}

	}

	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		this.blockActivated(world, x, y, z, entityPlayer);
	}

	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if(this.blockMaterial == Material.iron) {
			return true;
		} else {
			int i6 = world.getBlockMetadata(x, y, z);
			if((i6 & 8) != 0) {
				if(world.getBlockId(x, y - 1, z) == this.blockID) {
					this.blockActivated(world, x, y - 1, z, entityPlayer);
				}

				return true;
			} else {
				if(world.getBlockId(x, y + 1, z) == this.blockID) {
					world.setBlockMetadataWithNotify(x, y + 1, z, (i6 ^ 4) + 8);
				}

				world.setBlockMetadataWithNotify(x, y, z, i6 ^ 4);
				world.markBlocksDirty(x, y - 1, z, x, y, z);
				world.playAuxSFXAtEntity(entityPlayer, 1003, x, y, z, 0);
				return true;
			}
		}
	}

	public void onPoweredBlockChange(World world, int x, int y, int z, boolean z5) {
		int i6 = world.getBlockMetadata(x, y, z);
		if((i6 & 8) != 0) {
			if(world.getBlockId(x, y - 1, z) == this.blockID) {
				this.onPoweredBlockChange(world, x, y - 1, z, z5);
			}

		} else {
			boolean z7 = (world.getBlockMetadata(x, y, z) & 4) > 0;
			if(z7 != z5) {
				if(world.getBlockId(x, y + 1, z) == this.blockID) {
					world.setBlockMetadataWithNotify(x, y + 1, z, (i6 ^ 4) + 8);
				}

				world.setBlockMetadataWithNotify(x, y, z, i6 ^ 4);
				world.markBlocksDirty(x, y - 1, z, x, y, z);
				world.playAuxSFXAtEntity((EntityPlayer)null, 1003, x, y, z, 0);
			}
		}
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		int i6 = world.getBlockMetadata(x, y, z);
		if((i6 & 8) != 0) {
			if(world.getBlockId(x, y - 1, z) != this.blockID) {
				world.setBlockWithNotify(x, y, z, 0);
			}

			if(id > 0 && Block.blocksList[id].canProvidePower()) {
				this.onNeighborBlockChange(world, x, y - 1, z, id);
			}
		} else {
			boolean z7 = false;
			if(world.getBlockId(x, y + 1, z) != this.blockID) {
				world.setBlockWithNotify(x, y, z, 0);
				z7 = true;
			}

			if(!world.isBlockNormalCube(x, y - 1, z)) {
				world.setBlockWithNotify(x, y, z, 0);
				z7 = true;
				if(world.getBlockId(x, y + 1, z) == this.blockID) {
					world.setBlockWithNotify(x, y + 1, z, 0);
				}
			}

			if(z7) {
				if(!world.isRemote) {
					this.dropBlockAsItem(world, x, y, z, i6);
				}
			} else if(id > 0 && Block.blocksList[id].canProvidePower()) {
				boolean z8 = world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);
				this.onPoweredBlockChange(world, x, y, z, z8);
			}
		}

	}

	public int idDropped(int metadata, Random rand) {
		return (metadata & 8) != 0 ? 0 : (this.blockMaterial == Material.iron ? Item.doorSteel.shiftedIndex : Item.doorWood.shiftedIndex);
	}

	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3D vector1, Vec3D vector2) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.collisionRayTrace(world, x, y, z, vector1, vector2);
	}

	public int getState(int metadata) {
		return (metadata & 4) == 0 ? metadata - 1 & 3 : metadata & 3;
	}

	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return y >= Chunk.SECTION_HEIGHT - 1 ? false : world.isBlockNormalCube(x, y - 1, z) && super.canPlaceBlockAt(world, x, y, z) && super.canPlaceBlockAt(world, x, y + 1, z);
	}

	public static boolean isOpen(int i0) {
		return (i0 & 4) != 0;
	}

	public int getMobilityFlag() {
		return 1;
	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		int var5 = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
		return (var5 & 4) != 0;
	}
}
