package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;

public class BlockDeadBush extends BlockFlower {
	protected BlockDeadBush(int i1, int i2) {
		super(i1, i2);
		float f3 = 0.4F;
		this.setBlockBounds(0.5F - f3, 0.0F, 0.5F - f3, 0.5F + f3, 0.8F, 0.5F + f3);
	}

	@Override
	protected boolean canThisPlantGrowOnThisBlockID(int var1) {
		Block block = Block.blocksList[var1];
		if(block != null && block.canGrowPlants()) return true;
		
		if(block != null && block instanceof BlockTerracotta) return true;
		
		return var1 == Block.sand.blockID;
	}
	
	@Override
	public boolean canBlockStay(World world1, int i2, int i3, int i4) {
		return this.canThisPlantGrowOnThisBlockID(world1.getBlockId(i2, i3 - 1, i4));
	}

	public int getBlockTextureFromSideAndMetadata(int i1, int i2) {
		return this.blockIndexInTexture;
	}

	public int idDropped(int var1, Random var2) {
		return Item.stick.shiftedIndex;
	}
	
	public int quantityDropped(Random rand) {
		return rand.nextInt(3);
	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	@Override
	public int getRenderType() {
		return 1;
	}
}
