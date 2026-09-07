package net.minecraft.game.world.terrain.generate.city;

import java.util.Random;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;


public class BuildingGraveyard extends BuildingSchematic {

	@Override
	public String getSchematic() {
		return "/resources/schematics/building/graveyard.schematic";
	}
	
	@Override	
	public void populate(World world, Random rand, int chunkX, int chunkZ, Chunk chunk) {
		int x0 = chunkX * 16;
		int z0 = chunkZ * 16;
		
		// Chests
		int numChests = 1 + rand.nextInt(2);
		int numSpawners = 2 + rand.nextInt(2);
		
		for(int i = 0; i < numChests; i ++) {
			int row = rand.nextInt(3);
			int x = this.getXforRow(rand, row);		
			this.addChest(world, rand, x0 + x, chunk.buildingY0, z0 + 11 - row * 4);
		}
		
		int x, row;
		int dontTrySoHard = 0;
		for(int i = 0; i < numSpawners; i ++) {
			do {
				row = rand.nextInt(3);
				x = this.getXforRow(rand, row);			
			} while (world.getBlockId(x0 + x, chunk.buildingY0, z0 + 11 - row * 4) != Block.grass.blockID && dontTrySoHard ++ < 10);
			this.addSpawner(world, rand, x0 + x, chunk.buildingY0, z0 + 11 - row * 4, "Skeleton");
		}
	}
		
	public int getXforRow(Random rand, int row) {
		int x = 0;
		switch(row) {
			case 0: x = 2 + 2 * rand.nextInt(6); break;
			case 1: x = 5 + 2 * rand.nextInt(5); break;
			case 2: x = 2 + 2 * rand.nextInt(5); break;
		}
		return x;
	}
	
	@Override
	protected ItemStack getTreasure(int level, Random rand) {
		// level should be rand 0-9
		
		if(level < 4) {
			switch(rand.nextInt(6)) {
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
			case 1:
				return new ItemStack(Item.nametagSimple);
			case 0:
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
