package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.material.Material;

public class BlockOre extends Block {
	public BlockOre(int i1, int i2) {
		super(i1, i2, Material.rock);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int idDropped(int i1, Random random2) {
		if(this.blockID == Block.oreCoal.blockID) return Item.coal.shiftedIndex;
		if(this.blockID == Block.oreDiamond.blockID) return Item.diamond.shiftedIndex;
		if(this.blockID == Block.oreLapis.blockID) return Item.dyePowder.shiftedIndex;
		if(this.blockID == Block.oreEmerald.blockID) return Item.emerald.shiftedIndex;
		if(this.blockID == Block.oreRuby.blockID) return Item.ruby.shiftedIndex;
		return this.blockID;
	}

	public int quantityDropped(Random random1) {
		if(this.blockID == Block.oreLapis.blockID) return 4 + random1.nextInt(5);
		if(this.blockID == Block.oreEmerald.blockID) return 1 + random1.nextInt(2);
		if(this.blockID == Block.oreRuby.blockID) return 1 + random1.nextInt(2);
		return this.blockID == Block.oreLapis.blockID ? 4 + random1.nextInt(5) : 1;
	}

	protected int damageDropped(int i1) {
		return this.blockID == Block.oreLapis.blockID ? 4 : 0;
	}
	
	// Softlocked for b1.2
	public boolean softLocked() {
		return this.blockID == Block.oreLapis.blockID;
	}
}
