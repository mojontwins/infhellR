package net.minecraft.game.item.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class FurnaceRecipes {
	private static final FurnaceRecipes smeltingBase = new FurnaceRecipes();
	private Map<Integer,ItemStack> smeltingList = new HashMap<Integer,ItemStack>();

	public static final FurnaceRecipes smelting() {
		return smeltingBase;
	}

	private FurnaceRecipes() {
		this.addSmelting(Block.oreIron.blockID, new ItemStack(Item.ingotIron));
		this.addSmelting(Block.oreGold.blockID, new ItemStack(Item.ingotGold));
		this.addSmelting(Block.oreDiamond.blockID, new ItemStack(Item.diamond));
		this.addSmelting(Block.sand.blockID, new ItemStack(Block.glass));
		this.addSmelting(Item.porkRaw.shiftedIndex, new ItemStack(Item.porkCooked));
		this.addSmelting(Item.fishRaw.shiftedIndex, new ItemStack(Item.fishCooked));
		this.addSmelting(Block.cobblestone.blockID, new ItemStack(Block.stone));
		this.addSmelting(Item.clay.shiftedIndex, new ItemStack(Item.brick));
		this.addSmelting(Block.cactus.blockID, new ItemStack(Item.dyePowder, 1, 2));
		this.addSmelting(Block.wood.blockID, new ItemStack(Item.coal, 1, 1));
		
		// Mine
		this.addSmelting(Item.kelp.shiftedIndex, new ItemStack(Item.driedKelp));
		this.addSmelting(Block.dirt.blockID, new ItemStack(Block.adobe, 4));
		this.addSmelting(Item.chickenRaw.shiftedIndex, new ItemStack(Item.chickenCooked));
		this.addSmelting(Item.beefRaw.shiftedIndex, new ItemStack(Item.beefCooked));
		this.addSmelting(Item.chickenCooked.shiftedIndex, new ItemStack(Item.coal));
		this.addSmelting(Item.porkCooked.shiftedIndex, new ItemStack(Item.coal));
		this.addSmelting(Item.fishCooked.shiftedIndex, new ItemStack(Item.coal));
		this.addSmelting(Item.beefCooked.shiftedIndex, new ItemStack(Item.coal));
		this.addSmelting(Item.egg.shiftedIndex, new ItemStack(Item.friedEgg));
		this.addSmelting(Block.oreCopper.blockID, new ItemStack(Item.ingotCopper));
		this.addSmelting(Block.fenceIron.blockID, new ItemStack(Item.ingotIron));
		this.addSmelting(Block.blockClay.blockID, new ItemStack(Block.terracotta));
		this.addSmelting(Item.rottenFlesh.shiftedIndex, new ItemStack(Item.leather));
	}
	
	public List<FurnaceRecipe> getSmeltingsAsList() {
		List<FurnaceRecipe> result = new ArrayList<FurnaceRecipe> ();
		for(int inputId : this.smeltingList.keySet()) {
			result.add(new FurnaceRecipe(inputId, smeltingList.get(inputId)));
		}
		
		return result;
	}

	public void addSmelting(int i1, ItemStack itemStack2) {
		this.smeltingList.put(i1, itemStack2);
	}

	public ItemStack getSmeltingResult(int i1) {
		return (ItemStack)this.smeltingList.get(i1);
	}

	public Map<Integer,ItemStack> getSmeltingList() {
		return this.smeltingList;
	}
}
