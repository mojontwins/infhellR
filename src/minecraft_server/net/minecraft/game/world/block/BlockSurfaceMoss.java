package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.EnumSkyBlock;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

/**
 * Surface moss: a thin, walk-through patch that grows on the top face of
 * {@link Block#canGrowMoss() suitable blocks}. It is one texel (1/16 of a
 * block) high, non-opaque, and rendered like a carpet/snow slab via the
 * normal block renderer.
 *
 * <p>Spread (random tick) mirrors mushroom behaviour: it creeps to a random
 * neighbouring cell (x/z/y within one block) that is air, lit only by block
 * light below 7 (sunlight does not inhibit it) and has suitable ground. It
 * never spreads during summer.</p>
 */
public class BlockSurfaceMoss extends Block {
	/** Inverse of the per-random-tick spread probability (1 in N). Tune for testing. */
	private static final int MOSS_SPREAD_CREEP_CHANCE = 100;

	protected BlockSurfaceMoss(int blockID, int blockIndexInTexture) {
		super(blockID, blockIndexInTexture, Material.plants);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
		this.setTickOnLoad(true);
		this.setLightValue(0);
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	@Override
	public void setBlockBoundsForItemRender() {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block groundBlock = Block.blocksList[world.getBlockId(x, y - 1, z)];
		return groundBlock != null && groundBlock.canGrowMoss();
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		if(y < 0 || y >= 128) return false;
		Block groundBlock = Block.blocksList[world.getBlockId(x, y - 1, z)];
		return groundBlock != null && groundBlock.canGrowMoss();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		super.onNeighborBlockChange(world, x, y, z, blockID);
		this.checkBlockStay(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		this.checkBlockStay(world, x, y, z);

		if(Seasons.currentSeason == Seasons.SUMMER) return;
		if(rand.nextInt(MOSS_SPREAD_CREEP_CHANCE) != 0) return;

		int spreadX = x + rand.nextInt(3) - 1;
		int spreadY = y + rand.nextInt(3) - 1;
		int spreadZ = z + rand.nextInt(3) - 1;

		if(!world.isAirBlock(spreadX, spreadY, spreadZ)) return;
		if(world.getSavedLightValue(EnumSkyBlock.Block, spreadX, spreadY, spreadZ) >= 7) return;
		if(!this.canBlockStay(world, spreadX, spreadY, spreadZ)) return;

		world.setBlockWithNotify(spreadX, spreadY, spreadZ, this.blockID);
	}

	private void checkBlockStay(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
			world.setBlockWithNotify(x, y, z, 0);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return 0;
	}
}