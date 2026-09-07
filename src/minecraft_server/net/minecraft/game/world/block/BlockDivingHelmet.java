package net.minecraft.game.world.block;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockDivingHelmet extends Block {
	public static int HELMET_TOP_BACK = 11*16+4;
	public static int HELMET_BOTTOM = 11*16+5;
	public static int HELMET_SIDES = 11*16+6;
	public static int HELMET_FRONT = 11*16+7;

	protected BlockDivingHelmet(int i1, int i2) {
		super(i1, i2, Material.iron);
	}

	public void onBlockPlacedBy(World world1, int i2, int i3, int i4, EntityLiving entityLiving5) {
		int i6 = MathHelper.floor_double((double)(entityLiving5.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		if(i6 == 0) {
			world1.setBlockMetadataWithNotify(i2, i3, i4, 2);
		}

		if(i6 == 1) {
			world1.setBlockMetadataWithNotify(i2, i3, i4, 5);
		}

		if(i6 == 2) {
			world1.setBlockMetadataWithNotify(i2, i3, i4, 3);
		}

		if(i6 == 3) {
			world1.setBlockMetadataWithNotify(i2, i3, i4, 4);
		}

	}
	
	public int getBlockTextureFromSide(int side) {
		switch(side) {
		case 0: return HELMET_BOTTOM;
		case 1: return HELMET_TOP_BACK;
		case 3: return HELMET_FRONT;
		case 2: return HELMET_TOP_BACK;
		default: return HELMET_SIDES;
		}
	}
	
	public int getBlockTextureFromSideAndMetadata(int i1, int i2) {
		if(i2 == 0) return this.getBlockTextureFromSide(i1);
		if(i1 == 0) return HELMET_BOTTOM;
		if(i1 == 1) return HELMET_TOP_BACK;
		if(i1 == i2) return HELMET_FRONT;
		if(i1 == 2 && i2 == 3 || i1 == 3 && i2 == 2 || i1 == 4 && i2 == 5 || i1 == 5 && i2 == 4) return HELMET_TOP_BACK;
		return HELMET_SIDES;
	}
	
	public int getRenderBlockPass() {
		return 0;
	}
}
