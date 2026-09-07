package net.minecraft.game.item.recipe;

import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class RecipesCrafting {
	public void addRecipes(CraftingManager craftingManager1) {
		craftingManager1.addRecipe(new ItemStack(Block.chest), new Object[]{"###", "# #", "###", '#', Block.planks});
		craftingManager1.addRecipe(new ItemStack(Block.stoneOvenIdle), new Object[]{"###", "# #", "###", '#', Block.cobblestone});
		craftingManager1.addRecipe(new ItemStack(Block.workbench), new Object[]{"##", "##", '#', Block.planks});
		craftingManager1.addRecipe(new ItemStack(Block.sandStone), new Object[]{"##", "##", '#', Block.sand});
	}
}
