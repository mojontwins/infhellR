package net.minecraft.game.item;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.block.BlockObsidian;

public class ItemPickaxe extends ItemTool {
	// Effective against these block:metadata pairs (damage -1 = any metadata)
	private static ItemStack[] stacksEffectiveAgainst = new ItemStack[]{
		new ItemStack(Block.cobblestone, 1, -1), 
		new ItemStack(Block.stairDouble, 1, -1), 
		new ItemStack(Block.stairSingle, 1, -1), 
		new ItemStack(Block.stone, 1, -1), 
		new ItemStack(Block.sandStone, 1, -1), 
		new ItemStack(Block.cobblestoneMossy, 1, -1), 
		new ItemStack(Block.oreIron, 1, -1), 
		new ItemStack(Block.blockSteel, 1, -1), 
		new ItemStack(Block.oreCoal, 1, -1), 
		new ItemStack(Block.blockGold, 1, -1), 
		new ItemStack(Block.oreGold, 1, -1), 
		new ItemStack(Block.oreDiamond, 1, -1), 
		new ItemStack(Block.blockDiamond, 1, -1), 
		new ItemStack(Block.ice, 1, -1), 
		new ItemStack(Block.bloodStone, 1, -1), 
		new ItemStack(Block.oreLapis, 1, -1), 
		new ItemStack(Block.blockLapis, 1, -1),
		new ItemStack(Block.stairCompactCobblestone, 1, -1),
		new ItemStack(Block.oreRedstone, 1, -1),
		new ItemStack(Block.oreRedstoneGlowing, 1, -1),
		new ItemStack(Block.oreGlow, 1, -1),
		new ItemStack(Block.oreGlowGlowing, 1, -1),
		new ItemStack(Block.brick, 1, -1),
		new ItemStack(Block.button, 1, -1),
		new ItemStack(Block.doorSteel, 1, -1),
		new ItemStack(Block.pressurePlateStone, 1, -1),
		new ItemStack(Block.ironWall, 1, -1), 
		new ItemStack(Block.streetLanternFence, 1, -1), 
		new ItemStack(Block.barbedWire, 1, -1),
		new ItemStack(Block.mobSpawner, 1, -1),
		new ItemStack(Block.stoneOvenIdle, 1, -1),
		new ItemStack(Block.stoneOvenActive, 1, -1),
		new ItemStack(Block.oreRuby, 1, -1),
		new ItemStack(Block.oreEmerald, 1, -1),
		new ItemStack(Block.stairsBrick, 1, -1),
		new ItemStack(Block.stairsSandStone, 1, -1),
		new ItemStack(Block.stairsStoneBrickSmooth, 1, -1),
		new ItemStack(Block.fenceIron, 1, -1),
		new ItemStack(Block.blockRedstone, 1, -1),
		new ItemStack(Block.classicPiston, 1, -1),
		new ItemStack(Block.classicPistonBase, 1, -1),
		new ItemStack(Block.classicStickyPiston, 1, -1),
		new ItemStack(Block.classicStickyPistonBase, 1, -1),
		new ItemStack(Block.terracotta, 1, -1),
		new ItemStack(Block.stainedTerracotta, 1, -1),
		new ItemStack(Block.cement, 1, -1)
	};

	protected ItemPickaxe(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 2, enumToolMaterial2, stacksEffectiveAgainst, silkTouch);
	}
	
	protected ItemPickaxe(int i1, int damageModifier, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, damageModifier, enumToolMaterial2, stacksEffectiveAgainst, silkTouch);
	}

	public boolean canHarvestBlock(Block block) {
		return (block instanceof BlockObsidian) ? 
			this.toolMaterial.getHarvestLevel() == 3
		: 
			(block != Block.blockDiamond && block != Block.oreDiamond && block != Block.oreRuby && block != Block.oreEmerald ? 
				(block != Block.blockGold && block != Block.oreGold ? 
					(block != Block.blockSteel && block != Block.oreIron && block != Block.oreGlow && block != Block.oreGlowGlowing ? 
						(block != Block.blockLapis && block != Block.oreLapis ? 
							(block != Block.oreRedstone && block != Block.oreRedstoneGlowing ? 
								(block.blockMaterial == Material.rock ? 
									true 
								: 
									block.blockMaterial == Material.iron
								) 
							: 
								this.toolMaterial.getHarvestLevel() >= 2
							) 
						: 
							this.toolMaterial.getHarvestLevel() >= 1
						) 
					: 
						this.toolMaterial.getHarvestLevel() >= 1
					) 
				: 
					this.toolMaterial.getHarvestLevel() >= 2
				) 
			: 
				this.toolMaterial.getHarvestLevel() >= 2
			);
	}
}
