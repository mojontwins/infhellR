package net.minecraft.game.item.recipe;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class RecipesMine {
	public void addRecipes(CraftingManager craftingManager) {
		craftingManager.addRecipe(new ItemStack(Item.ironWire, 6), new Object[] {"#", "#", '#', Item.ingotIron}); 
		craftingManager.addRecipe(new ItemStack(Block.ironWall, 5), new Object[] {"# #", " # ", "# #", '#', Item.ironWire});
		craftingManager.addRecipe(new ItemStack(Block.woodenSpikes), new Object[] {"# #", " # ", "# #", '#', Item.stick});
		craftingManager.addRecipe(new ItemStack(Block.barbedWire, 8), new Object[] {"###", "# #", "###", '#', Item.ironWire});
		craftingManager.addRecipe(new ItemStack(Block.blockCoal, 1), new Object[] {"###", "###", "###", '#', Item.coal});
		craftingManager.addRecipe(new ItemStack(Item.mail, 2), new Object[] {"##", "##", '#', Item.ironWire});
		craftingManager.addRecipe(new ItemStack(Item.coal, 9), new Object[] {"#", '#', Block.blockCoal});
		craftingManager.addRecipe(new ItemStack(Item.slingshot, 1), new Object[] {"#-#", " # ", '#', Item.stick, '-', Item.silk});
		craftingManager.addRecipe(new ItemStack(Block.cobblestone, 1), new Object[] {"###", "###", "###", '#', Item.pebble});
		// craftingManager.addRecipe(new ItemStack(Block.glowStone, 1), new Object[]{"##", "##", Character.valueOf('#'), Item.lightStoneDust});
		craftingManager.addRecipe(new ItemStack(Block.driedKelpBlock, 1), new Object[] {"###", "###", "###", '#', Item.driedKelp});
		craftingManager.addRecipe(new ItemStack(Item.driedKelp, 9), new Object[] {"#", '#', Block.driedKelpBlock});
		/*
		craftingManager.addRecipe(new ItemStack(Block.stairSingle, 3, 1), new Object[]{"###", '#', Block.cobblestone});
		craftingManager.addRecipe(new ItemStack(Block.stairSingle, 3, 2), new Object[]{"###", '#', Block.cobblestoneMossy});
		craftingManager.addRecipe(new ItemStack(Block.stairSingle, 3, 3), new Object[]{"###", '#', Block.planks});
		craftingManager.addRecipe(new ItemStack(Block.stairSingle, 3, 4), new Object[]{"###", '#', Block.dirt});
		craftingManager.addRecipe(new ItemStack(Block.stairSingle, 3, 5), new Object[]{"###", '#', Block.glass});
		*/
		craftingManager.addRecipe(new ItemStack(Block.slimeBlock, 1), new Object[]{"###", "###", "###", '#', Item.slimeBall});
		craftingManager.addRecipe(new ItemStack(Item.slimeBall, 9), new Object[]{"#", '#', Block.slimeBlock});
		craftingManager.addRecipe(new ItemStack(Block.fleshBlock, 1),  new Object[]{"###", "###", "###", '#', Item.rottenFlesh});
		craftingManager.addRecipe(new ItemStack(Block.hayStack, 1), new Object[]{"###", "###", "###", '#', Item.wheat});
		craftingManager.addRecipe(new ItemStack(Item.wheat, 9), new Object[]{"#", '#', Block.hayStack});
		craftingManager.addRecipe(new ItemStack(Block.stoneBricks, 4, 0), new Object[]{"##", "##", '#', Block.stone});
		craftingManager.addShapelessRecipe(new ItemStack(Block.wood, 1), new Object[] {new ItemStack(Block.chippedWood, 1, -1), new ItemStack(Block.hollowLog, 1, -1)});
		craftingManager.addShapelessRecipe(new ItemStack(Block.planks,3), new Object[] {new ItemStack(Block.chippedWood, 1, -1)});
		craftingManager.addShapelessRecipe(new ItemStack(Block.planks,1), new Object[] {new ItemStack(Block.hollowLog, 1, -1)});
		
		craftingManager.addRecipe(new ItemStack(Block.classicPistonBase, 1), new Object[]{" X ", "X#X", "XXX", 'X', Item.ingotIron, '#', Item.redstone});
		craftingManager.addRecipe(new ItemStack(Block.classicStickyPistonBase, 1), new Object[]{" X ", "X#X", "XXX", 'X', Item.ingotGold, '#', Item.redstone});
		
		craftingManager.addRecipe(new ItemStack(Block.divingHelmet), new Object[] {"CRC", "C C", "cGc", 'C', Block.blockCopper, 'c', Item.ingotCopper, 'R', Item.redstone, 'G', Block.glass});
		craftingManager.addShapelessRecipe(new ItemStack(Block.streetLantern, 1), new Object[] {new ItemStack(Block.streetLanternBroken, 1), new ItemStack(Block.torchWood, 1)});
		
		craftingManager.addRecipe(new ItemStack(Item.cryingObsidianWand), new Object[] {" CD", " sC", "s  ", 'C', Block.cryingObsidian, 'D', Item.diamond, 's', Item.stick});
		craftingManager.addRecipe(new ItemStack(Block.fenceIron, 16), new Object[]{"###", "###", '#', Item.ingotIron});
		craftingManager.addRecipe(new ItemStack(Block.thinGlass, 16), new Object[]{"###", "###", '#', Block.glass});
		craftingManager.addRecipe(new ItemStack(Item.wheat, 1), new Object[]{"###", "###", '#', Item.straw});
		
		craftingManager.addRecipe(new ItemStack(Item.pirateCrown), new Object[] {"GGG", "RgE", "SSS", 'G', Item.ingotGold, 'R', Item.ruby, 'E', Item.emerald, 'S', Item.pirateSigil, 'g', Block.blockGold});
		
		craftingManager.addRecipe(new ItemStack(Block.boneBlock, 1), new Object[]{"###", "###", "###", '#', Item.bone});
		craftingManager.addShapelessRecipe(new ItemStack(Item.dyePowder,9), new Object[] {new ItemStack(Block.boneBlock, 1)});

		craftingManager.addRecipe(new ItemStack(Block.obsidian, 1), new Object[]{"###", "###", "###", '#', Item.obsidianShard});
		craftingManager.addShapelessRecipe(new ItemStack(Item.obsidianShard,9), new Object[] {new ItemStack(Block.obsidian, 1)});
		
		craftingManager.addRecipe(new ItemStack(Item.recoveryCompass, 1), new Object[]{"###", "#C#", "###", '#', Item.obsidianShard, 'C', Item.compass});
		craftingManager.addRecipe(new ItemStack(Block.thinPlanks, 6), new Object[]{"#", "#", "#", '#', Block.planks});
		
		craftingManager.addRecipe(new ItemStack(Block.cementPowder, 8), new Object[] {" ##", "##*", "***", '#', Block.sand, '*', Block.gravel}); 
		craftingManager.addRecipe(new ItemStack(Block.glass, 1, 1), new Object[] {" # ", "#G#", " # ", '#', Item.stick, 'G', new ItemStack(Block.glass, 1, 0)});
		craftingManager.addRecipe(new ItemStack(Block.glass, 1, 2), new Object[] {" # ", "#G#", " # ", '#', Item.ironWire, 'G', new ItemStack(Block.glass, 1, 0)});
		
		craftingManager.addRecipe(new ItemStack(Block.cement, 1, 1), new Object[] {" # ", "#G#", " # ", '#', Item.stick, 'G', new ItemStack(Block.cement, 1, 0)});
		craftingManager.addRecipe(new ItemStack(Block.cement, 1, 2), new Object[] {" # ", "#G#", " # ", '#', Item.ironWire, 'G', new ItemStack(Block.cement, 1, 0)});
		
		craftingManager.addRecipe(new ItemStack(Block.stoneBricks, 4, 1), new Object[] {"##", "##", '#', new ItemStack(Block.stoneBricks, 1, 0)} );
	}
}
