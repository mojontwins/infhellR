package net.minecraft.game.world.terrain.generate.city;

import java.util.Random;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;


public class BuildingWitchMansion extends BuildingSchematic {

	@Override
	public String getSchematic() {
		return "/resources/schematics/building/witchmansion.schematic";
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ, Chunk chunk) {
		int x0 = chunkX * 16;
		int z0 = chunkZ * 16;
		
		// Chests
		
		this.addChest(world, rand, x0 + 11, chunk.buildingY0 + 26, z0 + 8);
		this.addChest(world, rand, x0 + 7, chunk.buildingY0 + 21, z0 + 8);
		this.addChest(world, rand, x0 + 7, chunk.buildingY0 + 17, z0 + 13);
		
		// Spawners
		this.addSpawner(world, rand, x0 + 3, chunk.buildingY0 + 20, z0 + 8, "AlphaWitch");
		this.addSpawner(world, rand, x0 + 7, chunk.buildingY0 + 22, z0 + 8, "AlphaWitch");
		this.addSpawner(world, rand, x0 + 7, chunk.buildingY0 + 18, z0 + 13, "AlphaWitch");
	}
	
	@Override
	protected ItemStack getTreasure(int level, Random rand) {
		// level should be rand 0-9
		
		if(level < 4) {
			switch(rand.nextInt(8)) {
			case 0:
				return new ItemStack(Item.ingotIron, rand.nextInt(4) + 1);
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
			case 7:
				return new ItemStack(Item.legsRags);
			case 8:
				return new ItemStack(Item.plateRags);
			}
		} else if(level < 9) {
			switch(rand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getTreasure(1, rand);
			case 3:
			case 7:
			default:
				return new ItemStack(Item.potionPoison);
			case 4:
				return new ItemStack(Item.potionSlowness);
			case 5:
				return new ItemStack(Item.potionAutoHealing);
			case 6:
				return new ItemStack(Item.potionInstantDamage);
			}
		} else {
			switch(rand.nextInt(8)) {
			case 0:
				return new ItemStack(Item.hammerDiamond);
			case 1:
			case 2:
				return this.getTreasure(2, rand);
			case 3:
			case 7:
			default:
				return new ItemStack(Item.diamond);
			case 4:
				return new ItemStack(Item.appleGold);
			case 5:
				return new ItemStack(Block.sponge);
			case 6:
				return new ItemStack(Item.saddle);
			}
		} 
	}
}
