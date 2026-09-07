package net.minecraft.game.world.terrain.generate.amazonvillage;

import java.util.Random;

import net.minecraft.game.world.feature.FeatureBuilding;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

public abstract class BuildingAmazon extends FeatureBuilding {
	public static final int pedestalBlock[] = {
			Block.cobblestone.blockID, Block.gravel.blockID, Block.adobe.blockID, Block.cobblestoneMossy.blockID
	};
	
	public BuildingAmazon(World world, boolean rotated) {
		super(world, rotated);
	} 

	public void terraform(int n) {
		// Raises terrain to y - 1, attempting to create a nice looking pyramid of cobblestone
		
		int y = this.y0 - 1;
		int expansion = 1;
		boolean placedBlock;
		do {
			placedBlock = false;
			for(int x = -expansion; x < this.buildingWidth() + expansion; x ++) {
				for(int z = -expansion; z < this.buildingLength() + expansion; z ++) {
					
					/*
					while(!this.isBlockOpaqueCubeRelative(x, y, z) && y > 1) {
						this.setBlockRelative(x, y, z, pedestalBlock[this.world.rand.nextInt(4)]);
						placedBlock = true;
					}
					*/
					int xr, zr;
					if(this.rotated) {
						xr = this.xAbsolute + z;
						zr = this.zAbsolute + x;
					} else {
						xr = this.xAbsolute + x;
						zr = this.zAbsolute + z;
					}
					
					while(!this.world.isBlockOpaqueCube(xr, y, zr) && y > 1) {
						this.world.setBlock(xr, y, zr, pedestalBlock[this.world.rand.nextInt(4)]);
						placedBlock = true;
					}
					
				}
			}
			y --; if(expansion < 4) expansion ++;
		} while(placedBlock);
	}
	
	// Return a possible amazon treasure.
	protected ItemStack getTreasure(int level, Random rand) {
		// level should be rand 0-9
		
		if(level < 4) {
			switch(rand.nextInt(6)) {
			case 0:
				return new ItemStack(Item.ingotGold, rand.nextInt(4) + 1);
			case 1:
				return new ItemStack(Item.bucketEmpty);
			case 2:
				return new ItemStack(Item.bread);
			case 3:
			case 5:
			default:
				return new ItemStack(Block.torchWood, rand.nextInt(16) + 1);
			case 4:
				return new ItemStack(Item.wheat, rand.nextInt(3) + 1);
			}
		} else if(level < 9) {
			switch(rand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getTreasure(1, rand);
			case 5:
				return new ItemStack(Item.fishingRod);
			case 6:
				return new ItemStack(Item.swordSteel);
			case 7:
				return new ItemStack(Item.hammerSteel);
			default:
				return new ItemStack(Item.swordGold);
			}
		} else {
			switch(rand.nextInt(8)) {
			case 0:
				return new ItemStack(Item.maceDiamond);
			case 1:
				return new ItemStack(Item.nametagSimple);
			case 2:
				return this.getTreasure(2, rand);
			case 3:
				return new ItemStack(Item.maceSteel);
			case 7:
			default:
				return new ItemStack(Item.diamond);
			case 4:
				return new ItemStack(Item.appleGold);
			case 5:
				return new ItemStack(Item.diamond);
			case 6:
				return new ItemStack(Item.ruby);
			}
		} 
	}
	
	public boolean buildingFitsCheck() {
		return this.y0 > 32 && this.y0 + this.buildingHeight() <= 127;
	}
}
