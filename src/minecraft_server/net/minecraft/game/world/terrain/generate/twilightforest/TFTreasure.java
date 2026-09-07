package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.World;

public class TFTreasure {
	int type;
	protected TFTreasureTable useless;
	protected TFTreasureTable common;
	protected TFTreasureTable uncommon;
	protected TFTreasureTable rare;
	protected TFTreasureTable ultrarare;
	public static TFTreasure hill1 = new TFTreasure(1);
	public static TFTreasure hill2 = new TFTreasure(2);
	public static TFTreasure hill3 = new TFTreasure(3);
	public static TFTreasure hedgemaze = new TFTreasure(4);
	public static TFTreasure underhill_room = new TFTreasure(5);
	public static TFTreasure underhill_deadend = new TFTreasure(6);
	public static TFTreasure tower_room = new TFTreasure(7);

	public TFTreasure(int i) {
		this.type = i;
		this.useless = new TFTreasureTable();
		this.common = new TFTreasureTable();
		this.uncommon = new TFTreasureTable();
		this.rare = new TFTreasureTable();
		this.ultrarare = new TFTreasureTable();
		this.fill(i);
	}

	public boolean generate(World world, Random rand, int cx, int cy, int cz) {
		boolean flag = true;
		world.setBlockWithNotify(cx, cy, cz, Block.chest.blockID);

		int i;
		for(i = 0; i < 4; ++i) {
			flag &= this.addItemToChest(world, rand, cx, cy, cz, this.getCommonItem(rand));
		}

		for(i = 0; i < 2; ++i) {
			flag &= this.addItemToChest(world, rand, cx, cy, cz, this.getUncommonItem(rand));
		}

		for(i = 0; i < 1; ++i) {
			flag &= this.addItemToChest(world, rand, cx, cy, cz, this.getRareItem(rand));
		}

		return flag;
	}

	public ItemStack getCommonItem(Random rand) {
		return rand.nextInt(4) == 0 ? this.useless.getRandomItem(rand) : this.common.getRandomItem(rand);
	}

	public ItemStack getUncommonItem(Random rand) {
		return this.uncommon.getRandomItem(rand);
	}

	public ItemStack getRareItem(Random rand) {
		return rand.nextInt(4) == 0 ? this.ultrarare.getRandomItem(rand) : this.rare.getRandomItem(rand);
	}

	protected boolean addItemToChest(World world, Random rand, int cx, int cy, int cz, ItemStack itemStack) {
		TileEntityChest chest = (TileEntityChest)world.getBlockTileEntity(cx, cy, cz);
		if(chest != null) {
			int slot = this.findRandomInventorySlot(chest, rand);
			if(slot != -1) {
				chest.setInventorySlotContents(slot, itemStack);
				return true;
			}
		}

		return false;
	}

	protected int findRandomInventorySlot(TileEntityChest chest, Random rand) {
		for(int i = 0; i < 100; ++i) {
			int slot = rand.nextInt(chest.getSizeInventory());
			if(chest.getStackInSlot(slot) == null) {
				return slot;
			}
		}

		return -1;
	}

	protected void fill(int i) {
		this.useless.add((Block)Block.plantRed, 4);
		this.useless.add((Block)Block.plantYellow, 4);
		this.useless.add((Item)Item.feather, 3);
		this.useless.add((Item)Item.seeds, 2);
		this.useless.add((Item)Item.flint, 2);
		this.useless.add((Block)Block.cactus, 2);
		this.useless.add((Block)Block.reed, 4);
		this.useless.add((Block)Block.sand, 4);
		this.rare.add((Block)Block.blockCopper, 1);
		this.rare.add((Item)Item.nametagSimple, 1);
		
		if(i == 1 || i == 2 || i == 3) {
			this.common.add((Item)Item.ingotIron, 4);
			this.common.add((Item)Item.wheat, 4);
			this.common.add((Item)Item.silk, 4);
			this.common.add((Item)Item.bucketEmpty, 1);
			this.uncommon.add((Item)Item.bread, 1);
			this.uncommon.add((Item)Item.gunpowder, 4);
			this.uncommon.add((Item)Item.arrow, 12);
			this.uncommon.add((Block)Block.torchWood, 12);
			this.rare.add((Item)Item.ingotGold, 3);
			this.rare.add((Item)Item.pickaxeSteel, 1);
			this.rare.add((Item)Item.saddle, 1);
			this.ultrarare.add((Item)Item.compass, 1);
			this.ultrarare.add((Item)Item.recordCat, 1);
			this.ultrarare.add((Item)Item.diamond, 1);
			this.rare.add((Item)Item.battleSteel, 1);
			this.rare.add((Item)Item.knifeSteel, 1);
			this.ultrarare.add((Item)Item.maceSteel, 1);
			this.ultrarare.add((Item)Item.hammerSteel, 1);
			
		}

		if(i == 4) {
			this.common.add((Block)Block.planks, 4);
			this.common.add((Block)Block.mushroomBrown, 4);
			this.common.add((Block)Block.mushroomRed, 4);
			this.common.add((Item)Item.wheat, 4);
			this.common.add((Item)Item.silk, 4);
			this.common.add((Item)Item.stick, 6);
			/*
			this.uncommon.add((Item)Item.melon, 4);
			this.uncommon.add((Item)Item.melonSeeds, 4);
			this.uncommon.add((Item)Item.pumpkinSeeds, 4);
			*/
			this.uncommon.add((Item)Item.arrow, 12);
			// this.uncommon.add((Block)TFBlocks.firefly, 4);
			this.rare.add((Block)Block.web, 3);
			// this.rare.add((Item)Item.shears, 1);
			this.rare.add((Item)Item.saddle, 1);
			this.rare.add((Item)Item.bow, 1);
			this.rare.add((Item)Item.appleRed, 2);
			this.ultrarare.add((Item)Item.hoeDiamond, 1);
			this.ultrarare.add((Item)Item.diamond, 1);
			this.ultrarare.add((Item)Item.bowlSoup, 1);
			this.ultrarare.add((Item)Item.appleGold, 1);
		}

		if(i == 5) {
			this.common.add((Item)Item.ingotIron, 4);
			this.common.add((Item)Item.bread, 1);
			this.common.add((Item)Item.wheat, 6);
			this.common.add((Item)Item.gunpowder, 4);
			this.common.add((Item)Item.legsLeather, 1);
			this.common.add((Item)Item.helmetLeather, 1);
			this.common.add((Item)Item.bootsLeather, 1);
			this.common.add((Item)Item.plateLeather, 1);
			this.uncommon.add((Item)Item.legsSteel, 1);
			this.uncommon.add((Item)Item.helmetSteel, 1);
			this.uncommon.add((Item)Item.bootsSteel, 1);
			this.uncommon.add((Item)Item.plateSteel, 1);
			this.uncommon.add((Item)Item.swordSteel, 1);
			this.uncommon.add((Item)Item.battleSteel, 1);
			this.uncommon.add((Item)Item.knifeSteel, 1);
			this.rare.add((Item)Item.maceSteel, 1);
			this.rare.add((Item)Item.hammerSteel, 1);
			this.uncommon.add((Item)Item.axeSteel, 1);
			this.uncommon.add((Item)Item.bow, 1);
			this.rare.add((Item)Item.redstone, 6);
			this.rare.add((Item)Item.lightStoneDust, 4);
			this.rare.add((Block)Block.tnt, 3);
			this.rare.add((Item)Item.porkCooked, 1);
			this.ultrarare.add((Item)Item.saddle, 1);
			this.ultrarare.add((Item)Item.book, 1);
			this.ultrarare.add((Item)Item.painting, 1);
			this.ultrarare.add((Item)Item.appleGold, 1);
			this.ultrarare.add((Item)Item.recordCat, 1);
		}

		if(i == 6) {
			this.common.add((Item)Item.stick, 12);
			this.common.add((Item)Item.coal, 12);
			this.common.add((Item)Item.arrow, 12);
			this.common.add((Item)Item.wheat, 4);
			this.uncommon.add((Item)Item.gunpowder, 4);
			this.uncommon.add((Block)Block.planks, 6);
			this.uncommon.add((Item)Item.leather, 4);
			this.uncommon.add((Item)Item.silk, 4);
			this.uncommon.add((Item)Item.paper, 3);
			this.uncommon.add((Item)Item.bread, 1);
			this.rare.add((Item)Item.ingotIron, 3);
			this.rare.add((Item)Item.redstone, 6);
			this.rare.add((Item)Item.lightStoneDust, 4);
			this.ultrarare.add((Item)Item.book, 1);
			this.ultrarare.add((Item)Item.ingotIron, 10);
			this.ultrarare.add((Block)Block.driedKelpBlock, 1);
			this.uncommon.add((Item)Item.battleSteel, 1);
			this.uncommon.add((Item)Item.knifeSteel, 1);
			this.rare.add((Item)Item.maceSteel, 1);
			this.rare.add((Item)Item.hammerSteel, 1);
			
		}

		if(i == 7) {
			this.common.add((Item)Item.snowball, 6);
			this.common.add((Item)Item.paper, 4);
			this.common.add((Item)Item.arrow, 12);
			this.common.add((Item)Item.feather, 11);
			this.uncommon.add((Item)Item.swordGold, 1);
			this.uncommon.add((Item)Item.pickaxeGold, 1);
			this.uncommon.add((Block)Block.torchRedstoneActive, 4);
			this.uncommon.add((Item)Item.silk, 4);
			this.uncommon.add((Item)Item.book, 1);
			this.uncommon.add((Item)Item.bread, 1);
			this.rare.add((Item)Item.slimeBall, 3);
			this.rare.add((Item)Item.redstone, 6);
			this.rare.add((Item)Item.compass, 1);
			//this.ultrarare.add((Item)Item.enderPearl, 1);
			this.ultrarare.add((Block)Block.obsidian, 4);
			this.ultrarare.add((Block)Block.cryingObsidian, 1);
			this.ultrarare.add((Item)Item.diamond, 1);
			this.uncommon.add((Item)Item.battleSteel, 1);
			this.uncommon.add((Item)Item.knifeSteel, 1);
			this.rare.add((Item)Item.maceDiamond, 1);
			this.rare.add((Item)Item.hammerSteel, 1);
			
		}

	}
}
