package net.minecraft.game.item;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockBed;

public class ItemLunarWand extends Item {

	public ItemLunarWand(int i1) {
		super(i1);

		this.displayOnCreativeTab = CreativeTabs.tabTools;
	}

	/**
	 * Right-clicking an unenchanted bed enchants both halves with the lunar wand.
	 * Enchanted beds are the only ones that actually skip the night when slept in.
	 */
	public boolean onItemUse(ItemStack itemStack1, EntityPlayer entityPlayer2, World world3, int i4, int i5, int i6, int i7) {
		if(world3.getBlockId(i4, i5, i6) != Block.blockBed.blockID) {
			return false;
		}

		int metadata = world3.getBlockMetadata(i4, i5, i6);
		if(BlockBed.isBedSleepable(metadata)) {
			return false;
		}

		int direction = BlockBed.getDirectionFromMetadata(metadata);
		int otherX;
		int otherZ;
		if(BlockBed.isBlockFootOfBed(metadata)) {
			otherX = i4 - BlockBed.headBlockToFootBlockMap[direction][0];
			otherZ = i6 - BlockBed.headBlockToFootBlockMap[direction][1];
		} else {
			otherX = i4 + BlockBed.headBlockToFootBlockMap[direction][0];
			otherZ = i6 + BlockBed.headBlockToFootBlockMap[direction][1];
		}

		world3.setBlockMetadataWithNotify(i4, i5, i6, metadata | BlockBed.META_SLEEPABLE);
		if(world3.getBlockId(otherX, i5, otherZ) == Block.blockBed.blockID) {
			world3.setBlockMetadataWithNotify(otherX, i5, otherZ, world3.getBlockMetadata(otherX, i5, otherZ) | BlockBed.META_SLEEPABLE);
		}

		for(int i = 0; i < 7; ++i) {
			world3.spawnParticle("largesmoke",
				(double)i4 + world3.rand.nextDouble(),
				(double)i5 + world3.rand.nextDouble(),
				(double)i6 + world3.rand.nextDouble(),
				0.0D, 0.0D, 0.0D);
		}

		world3.playSoundEffect((double)i4 + 0.5D, (double)i5 + 0.5D, (double)i6 + 0.5D, "random.drr", 1.0F, 0.25F);
		world3.playSoundEffect((double)i4 + 0.5D, (double)i5 + 0.5D, (double)i6 + 0.5D, "random.breath", 0.5F, 0.15F);

		entityPlayer2.triggerAchievement(AchievementList.magicBed);

		return true;
	}

}
