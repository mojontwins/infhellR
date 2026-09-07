package net.minecraft.game.world.terrain.generate.city;

import java.util.Random;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;


public class BuildingShoppe1 extends BuildingSchematic {
	
	@Override
	public String getSchematic() {
		return "/resources/schematics/building/shoppe1.schematic";
	}
	
	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ, Chunk chunk) {
		if(rand.nextInt(3) != 0) return;
		
		int x0 = chunkX * 16;
		int z0 = chunkZ * 16;
		
		// Generate spider spawner, cobwebs and a treasure chest.
		this.addChest(world, rand, x0 + 7, chunk.buildingY0 + 6, z0 + 9);
		this.addSpawner(world, rand, x0 + 7, chunk.buildingY0 + 7, z0 + 9, rand.nextBoolean() ? "Spider" : "SwarmSpider");
		
		for(int x = 3; x <= 11; x ++) {
			for(int z = 6; z <= 11; z ++) {
				for(int y = 6; y <= 8; y ++) {
					int xx = x0 + x, yy = chunk.buildingY0 + y, zz = z0 + z;
					if(rand.nextInt(4) != 0 && world.getBlockId(xx, yy, zz) == 0) {
						world.setBlock(xx, yy, zz, Block.web.blockID);
					}
				}
			}
		}
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
				return new ItemStack(Item.swordSteel);
			case 4:
				return new ItemStack(Item.bow);
			case 5:
				return new ItemStack(Item.axeSteel);
			case 6:
				return new ItemStack(Item.fishingRod);
			}
		} else {
			switch(rand.nextInt(8)) {
			case 0:
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
