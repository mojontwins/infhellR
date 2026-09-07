package net.minecraft.game.item;

import net.minecraft.game.EnumMovingObjectType;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class ItemLilypad extends ItemBlock {
	public ItemLilypad(int par1) {
		super(par1);
	}
	
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World world2, EntityPlayer entityPlayer3) {
		float f4 = 1.0F;
		float f5 = entityPlayer3.prevRotationPitch + (entityPlayer3.rotationPitch - entityPlayer3.prevRotationPitch) * f4;
		float f6 = entityPlayer3.prevRotationYaw + (entityPlayer3.rotationYaw - entityPlayer3.prevRotationYaw) * f4;
		double d7 = entityPlayer3.prevPosX + (entityPlayer3.posX - entityPlayer3.prevPosX) * (double)f4;
		double d9 = entityPlayer3.prevPosY + (entityPlayer3.posY - entityPlayer3.prevPosY) * (double)f4 + 1.62D - (double)entityPlayer3.yOffset;
		double d11 = entityPlayer3.prevPosZ + (entityPlayer3.posZ - entityPlayer3.prevPosZ) * (double)f4;
		Vec3D vec3D13 = Vec3D.createVector(d7, d9, d11);
		float f14 = MathHelper.cos(-f6 * 0.017453292F - (float)Math.PI);
		float f15 = MathHelper.sin(-f6 * 0.017453292F - (float)Math.PI);
		float f16 = -MathHelper.cos(-f5 * 0.017453292F);
		float f17 = MathHelper.sin(-f5 * 0.017453292F);
		float f18 = f15 * f16;
		float f20 = f14 * f16;
		double d21 = 5.0D;
		Vec3D vec3D23 = vec3D13.addVector((double)f18 * d21, (double)f17 * d21, (double)f20 * d21);
		MovingObjectPosition movingobjectposition = world2.rayTraceBlocks(vec3D13, vec3D23, true);

		if (movingobjectposition == null) {
			return par1ItemStack;
		}

		if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE) {
			int i = movingobjectposition.blockX;
			int j = movingobjectposition.blockY;
			int k = movingobjectposition.blockZ;

			if (world2.getBlockMaterial(i, j, k) == Material.water && world2.getBlockMetadata(i, j, k) == 0 && world2.getBlockId(i, j + 1, k) == 0) {
				world2.setBlockWithNotify(i, j + 1, k, Block.lilyPad.blockID);
				if (!entityPlayer3.isCreative) par1ItemStack.stackSize--;
			}
		}

		return par1ItemStack;
	}  
}
