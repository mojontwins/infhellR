package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.EnumSkyBlock;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

/**
 * Ice block: slippery, translucent, and meltable.
 *
 * <p>Two subtypes: metadata 0 is clear ice (melts back into water when brightly lit),
 * metadata 1 is rainbow ice (never melts, tinted per-position from the rainbow palette).
 * Packed ice ({@link BlockIcePacked}) reuses this block's properties but disables ticking
 * so it never melts.
 */
public class BlockIce extends BlockBreakable implements IBlockWithSubtypes {
	/** Display names for the two subtypes (clear / rainbow). */
	private final String[] subtypeNames = new String[] { "ice", "ice.orange" };

	/** Rainbow palette indexed by block position (see {@link #colorMultiplier}). */
	private static final int[] RAINBOW_COLOURS = new int[] {
			0xFF6666, 0xFF66A3, 0xFF66EB, 0xE666FF, 0xA866FF, 0x7066FF, 0x668AFF, 0x66D2FF, 
			0x66FFFA, 0x66FFC4, 0x66FF85, 0x70FF66, 0xADFF66, 0xEEFF66, 0xFFE566, 0xFF9E66
	};

	/** Animation cursor advanced on each {@link #getRenderColor} call so the rainbow cycles. */
	private int rainbowAnimationFrame = 0;

	public BlockIce(int blockID, int blockIndex) {
		super(blockID, blockIndex, Material.ice, false);
		this.slipperiness = 0.98F;
		// Random ticks (which drive melting) run for every loaded chunk.
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	/**
	 * Ice passes its faces to the base class with the side index mirrored (vanilla ice
	 * behaviour), which keeps the translucent edge between two neighbouring ice blocks
	 * consistent with how {@link BlockBreakable} hides inner faces between identical
	 * see-through blocks.
	 */
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return super.shouldSideBeRendered(blockAccess, x, y, z, 1 - side);
	}

	/**
	 * When normal (clear) ice is harvested, its empty spot is flooded with a moving water
	 * block if it still has solid or liquid ground beneath it (it is, after all, frozen
	 * water). Rainbow ice (metadata > 0) just drops away.
	 */
	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
		super.harvestBlock(world, player, x, y, z, metadata);
		if(world.getBlockMetadata(x, y, z) > 0) return;
		Material groundMaterial = world.getBlockMaterial(x, y - 1, z);
		if(groundMaterial.getIsSolid() || groundMaterial.getIsLiquid()) {
			world.setBlockWithNotify(x, y, z, Block.waterMoving.blockID);
		}
	}

	/** Ice drops nothing when broken. */
	@Override
	public int quantityDropped(Random rand) {
		return 0;
	}

	/**
	 * Random-tick melting. A block of ice turns into a static water source when the block
	 * light around it is bright enough - unless any of several "don't melt" conditions hold.
	 *
	 * <p>The guards are ordered cheapest-first so a tick that must not melt returns before
	 * any of the more expensive world lookups run: the block's own metadata and the season
	 * are plain field/array reads, the biome is a chunk-local lookup, and the temperature
	 * is the most expensive (for chunks loaded from an older world it may lazily compute the
	 * column's climate ramp once).
	 */
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		// Rainbow ice (metadata > 0) never melts.
		if(world.getBlockMetadata(x, y, z) > 0) {
			return;
		}
		// Ice never melts in winter.
		if(Seasons.currentSeason == Seasons.WINTER) {
			return;
		}
		// In autumn each melting tick is skipped with 50% probability (freeze forecast).
		if(Seasons.currentSeason == Seasons.AUTUMN && rand.nextBoolean()) {
			return;
		}
		// Ice in a cold biome (tundra, etc.) never melts.
		if(world.getBiomeGenAt(x, z).weather == Weather.cold) {
			return;
		}
		// Ice never melts where the temperature is at or below the freezing point.
		if(world.getTemperatureAt(x, z) <= 0.2F) {
			return;
		}

		// Melting: if the block light here exceeds what the block's own opacity lets
		// through, the ice thaws into a static water source (still dropping nothing).
		if(world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 11 - Block.lightOpacity[this.blockID]) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
			world.setBlockWithNotify(x, y, z, Block.waterStill.blockID);
		}
	}

	/** Ice can be pushed and pulled by pistons. */
	@Override
	public int getMobilityFlag() {
		return 0;
	}
	
	/** Non-opaque: blocks and entities behind ice remain visible through it. */
	public boolean seeThrough() {
		return true; 
	}

	/**
	 * Per-vertex tint: clear ice is white; rainbow ice is tinted with the palette entry
	 * picked from the block's position, giving each placement its own stable colour.
	 */
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		int metadata = blockAccess.getBlockMetadata(x, y, z);
		
		return metadata == 0 
				? 0xFFFFFF
				: RAINBOW_COLOURS[(x + y + z) & 0xF];
	}

	/**
	 * Icon tint used for item/held rendering: advances the shared rainbow animation counter
	 * on every call, returning white for clear ice and the first palette colour for rainbow
	 * ice.
	 */
	@Override
	public int getRenderColor(int metadata) {
		rainbowAnimationFrame = (rainbowAnimationFrame + 1) & 0x0F;
		return metadata == 0 ? 0xFFFFFF : RAINBOW_COLOURS[0];
	}
	
	/** Ice is translucent: rendered in the second (transparency) render pass. */
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	/** Both subtypes (clear + rainbow) are offered in the creative inventory. */
    @Override
    public void getSubBlocks(int blockID, CreativeTabs creativeTabs, List<ItemStack> itemList) {
		for(int i = 0; i < 2; i ++) {
			itemList.add(new ItemStack(blockID, 1, i));
		}
	}

	/** Returns the display name for the given subtype (clear / rainbow). */
	@Override
	public String getNameFromMeta(int metadata) {
		return this.subtypeNames[metadata];
	}

	/** Both subtypes share the same ice texture. */
	@Override
	public int getIndexInTextureFromMeta(int metadata) {
		return this.blockIndexInTexture;
	}
}