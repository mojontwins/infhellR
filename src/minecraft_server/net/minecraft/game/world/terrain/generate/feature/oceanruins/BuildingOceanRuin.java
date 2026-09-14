package net.minecraft.game.world.terrain.generate.feature.oceanruins;

import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.FeatureBuilding;

public abstract class BuildingOceanRuin extends FeatureBuilding {
	protected Random rand;

	public BuildingOceanRuin(World world, boolean rotated) {
		super(world, rotated);
		this.rand = new Random(world.getRandomSeed());
	}
	
	public void terraform() {		
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
				return new ItemStack(Item.stick, rand.nextInt(16) + 1);
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
				return new ItemStack(Item.swordWood);
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
				return new ItemStack(Item.plateRags);
			case 5:
				return new ItemStack(Item.diamond);
			case 6:
				return rand.nextInt(10) == 0 ? new ItemStack(Item.appleGold) : new ItemStack(Item.pirateSigil);
			}
		} 
	}
	
	protected int getChestNumberOfItems() {
		return this.world.rand.nextInt(3) + 2;
	}
	
	public boolean buildingFitsCheck() {
		return true; //this.y0 + this.buildingHeight() < 64;
	}
}
