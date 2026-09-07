package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.World;
public class WorldGenWitchHut extends WorldGenerator {
	public static LootItem possibleLootItems [] = {
		new LootItem (Item.saddle.shiftedIndex, 1, false, 0),
		new LootItem (Item.ingotIron.shiftedIndex, 4, false, 0),
		new LootItem (Item.bread.shiftedIndex, 1, false, 0),
		new LootItem (Item.bucketEmpty.shiftedIndex, 1, false, 0),
		new LootItem (Item.appleGold.shiftedIndex, 1, true, 100),
		new LootItem (Item.diamond.shiftedIndex, 1, true, 25),
		new LootItem (Item.potionAutoHealing.shiftedIndex, 2, true, 10),
		new LootItem (Item.potionInstantDamage.shiftedIndex, 3, true, 5),
		new LootItem (Item.potionPoison.shiftedIndex, 4, true, 5),
		new LootItem (Block.sponge.blockID, 1, false, 0),
		new LootItem (Item.maceSteel.shiftedIndex, 1, true, 25),
	};
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if(
				!this.footHitsTerrain(world, x + 1, y - 1, z + 1) ||
				!this.footHitsTerrain(world, x + 6, y - 1, z + 1) ||
				!this.footHitsTerrain(world, x + 1, y - 1, z + 5) ||
				!this.footHitsTerrain(world, x + 6, y - 1, z + 5)
		) {
			System.out.println ("Witch hut @ " + x + " " + y + " " + z + " Failed no foots hit terrain");
			return false;
		}
		
		// Check if position is correct
		
		// Main body
		for(int xx = 0; xx < 9; xx ++) {
			for(int yy = 2; yy < 7; yy ++ ) {
				for(int zz = 0; zz < 7; zz ++) {
					if(world.isBlockOpaqueCube(xx + x, yy + y, zz + z)) {
						System.out.println ("Witch hut @ " + x + " " + y + " " + z + " Failed no clear rectangle");
						return false;
					}
				}
			}
		}
		
		// Draw
		world.setBlockAndMetadataColumn(x + 0, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 1, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 2, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 3, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 4, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 5, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 0, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 1, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 1, y, z + 1, new int[] {-6, 17, 5});
		world.setBlockAndMetadataColumn(x + 1, y, z + 2, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 1, y, z + 3, new int[] {-2, 0, -2, 5, 85, -2, 5});
		world.setBlockAndMetadataColumn(x + 1, y, z + 4, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 1, y, z + 5, new int[] {-6, 17, 5});
		world.setBlockAndMetadataColumn(x + 1, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 2, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 2, y, z + 1, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 2, y, z + 2, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 2, y, z + 3, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 2, y, z + 4, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 2, y, z + 5, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 2, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 3, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 3, y, z + 1, new int[] {-2, 0, -2, 5, 0, -2, 5});
		world.setBlockAndMetadataColumn(x + 3, y, z + 2, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 3, y, z + 3, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 3, y, z + 4, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 3, y, z + 5, new int[] {-2, 0, -2, 5, 0, -2, 5});
		world.setBlockAndMetadataColumn(x + 3, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 4, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 4, y, z + 1, new int[] {-2, 0, -2, 5, 0, -2, 5});
		world.setBlockAndMetadataColumn(x + 4, y, z + 2, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 4, y, z + 3, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 4, y, z + 4, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 4, y, z + 5, new int[] {-2, 0, -2, 5, 0, -2, 5});
		world.setBlockAndMetadataColumn(x + 4, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 5, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 5, y, z + 1, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 5, y, z + 2, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 5, y, z + 3, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 5, y, z + 4, new int[] {-2, 0, 5, -3, 0, 5});
		world.setBlockAndMetadataColumn(x + 5, y, z + 5, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 5, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 6, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 6, y, z + 1, new int[] {-5, 17, -2, 5});
		world.setBlockAndMetadataColumn(x + 6, y, z + 2, new int[] {-2, 0, 5, -2, 0, -2, 5});
		world.setBlockAndMetadataColumn(x + 6, y, z + 3, new int[] {-2, 0, -5, 5});
		world.setBlockAndMetadataColumn(x + 6, y, z + 4, new int[] {-2, 0, -2, 5, 85, -2, 5});
		world.setBlockAndMetadataColumn(x + 6, y, z + 5, new int[] {-5, 17, -2, 5});
		world.setBlockAndMetadataColumn(x + 6, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 0, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 1, new int[] {-2, 0, 5, 85, -2, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 2, new int[] {-2, 0, 5, -3, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 3, new int[] {-2, 0, 5, -3, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 4, new int[] {-2, 0, 5, -3, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 5, new int[] {-2, 0, 5, 85, -2, 0, 44});
		world.setBlockAndMetadataColumn(x + 7, y, z + 6, new int[] {-6, 0, 44});
		world.setBlockAndMetadataColumn(x + 8, y, z + 0, new int[] {-7, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 1, new int[] {-7, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 2, new int[] {-2, 0, 5, -4, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 3, new int[] {-2, 0, 5, -4, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 4, new int[] {-2, 0, 5, -4, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 5, new int[] {-7, 0});
		world.setBlockAndMetadataColumn(x + 8, y, z + 6, new int[] {-7, 0});
		
		// Extend feet
		this.extendFoot(world, x + 1, y - 1, z + 1);
		this.extendFoot(world, x + 6, y - 1, z + 1);
		this.extendFoot(world, x + 1, y - 1, z + 5);
		this.extendFoot(world, x + 6, y - 1, z + 5);
		
		// Special stuff
		world.setBlock(x + 2, y + 3, z + 4, Block.workbench.blockID);
		world.setBlockWithNotify(x + 2, y + 3, z + 3, Block.chest.blockID);
		
		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x + 2, y + 3, z + 3);
		tileEntityChest.setOwnerEntityType("AlphaWitch");
		for (int j = 0; j < 8 + rand.nextInt(4); j ++) {
			ItemStack var18 = this.pickChestLootItem(rand);
			if(var18 != null) {
				tileEntityChest.setInventorySlotContents(rand.nextInt(tileEntityChest.getSizeInventory()), var18);
			}
		}
		
		world.setBlockWithNotify(x + 3, y + 3, z + 3, Block.mobSpawnerOneshot.blockID);
		TileEntityMobSpawnerOneshot tileEntityMobSpawnerOneshot = (TileEntityMobSpawnerOneshot)world.getBlockTileEntity(x + 3, y + 3, z + 3);
		tileEntityMobSpawnerOneshot.setMobID ("AlphaWitch");
		
		world.setBlockWithNotify(x + 3, y + 3, z + 2, Block.mobSpawnerOneshot.blockID);
		tileEntityMobSpawnerOneshot = (TileEntityMobSpawnerOneshot)world.getBlockTileEntity(x + 3, y + 3, z + 2);
		tileEntityMobSpawnerOneshot.setMobID ("BlackCat");
		
		System.out.println("Witch hut @ " + x + " " + z);
				
		return true;
	}

	void extendFoot(World world, int x, int y, int z) {
		while(y > 1 && !world.isBlockOpaqueCube(x, y, z)) {
			world.setBlock(x, y, z, Block.wood.blockID);
			y --;
		}
	}
	
	boolean footHitsTerrain(World world, int x, int y, int z) {
		while(y > 8 && !world.isBlockOpaqueCube(x, y, z)) {
			y --;
		}
		
		return (y > 8);
	}
	
	private ItemStack pickChestLootItem(Random rand) {	
		// Original code was RUBBISH, let's do this properly. 
		int selection = rand.nextInt (possibleLootItems.length + 2);
		if (selection < possibleLootItems.length) {
			LootItem item = possibleLootItems [selection];
			if (item.isRare && rand.nextInt (item.chance) != 0) return null;
			return new ItemStack (item.id, 1 + rand.nextInt (item.maxQuantity));
		} else return null;
	}
}
