package net.minecraft.client.render.block;

/**
 * Registry mapping a block's {@link net.minecraft.game.world.block.Block#getRenderType()}
 * number to the single {@link BlockRenderHandler} that knows how to draw it. The engine
 * ({@link net.minecraft.client.render.RenderBlocks#renderBlockByRenderType}) looks up the
 * entry and delegates so every block type is its own isolated class.
 *
 * <p>Render-type 0 (NORMAL) drives the vast majority of blocks via the shared cube code in
 * {@link RenderBlockUtil}. Types 100-112 are the InfHell custom additions.</p>
 */
public enum BlockRenderType {
	NORMAL(0, new RenderBlockNormal()),
	PLANT(1, new RenderBlockPlant()),
	TORCH(2, new RenderBlockTorch()),
	FIRE(3, new RenderBlockFire()),
	FLUID(4, new RenderBlockFluid()),
	REDSTONE_WIRE(5, new RenderBlockRedstoneWire()),
	CROPS(6, new RenderBlockCrops()),
	DOOR(7, new RenderBlockDoor()),
	LADDER(8, new RenderBlockLadder()),
	RAIL(9, new RenderBlockRail()),
	STAIRS(10, new RenderBlockStairs()),
	FENCE(11, new RenderBlockFence()),
	LEVER(12, new RenderBlockLever()),
	CACTUS(13, new RenderBlockCactus()),
	BED(14, new RenderBlockBed()),
	REPEATER(15, new RenderBlockRepeater()),
	PISTON_BASE(16, new RenderBlockPistonBase()),
	PISTON_EXTENSION(17, new RenderBlockPistonExtension()),
	PANE(18, new RenderBlockPane()),
	VINE(20, new RenderBlockVine()),
	LILYPAD(23, new RenderBlockLilyPad()),
	WALL(100, new RenderBlockWall()),
	BARBED_WIRE(101, new RenderBlockBarbedWire()),
	HOLLOW_TRUNK(102, new RenderBlockHollowTrunk()),
	DIRT_PATH(103, new RenderBlockDirtPath()),
	STREET_LANTERN(104, new RenderBlockStreetLantern()),
	CHIPPED_WOOD(105, new RenderBlockChippedWood()),
	SLIME(106, new RenderBlockSlime()),
	AXIS_ORIENTED(107, new RenderBlockAxisOriented()),
	WOOD_ORIENTED(108, new RenderBlockWoodOriented()),
	CLASSIC_PISTON(109, new RenderBlockClassicPiston()),
	CHAIN(110, new RenderBlockChain()),
	SNOWLOGGED_PLANT(111, new RenderBlockSnowloggedPlant()),
	THIN_FEATURE(112, new RenderBlockThinFeature());

	private static final BlockRenderType[] BY_RENDER_TYPE = new BlockRenderType[113];

	static {
		for(BlockRenderType type : values()) {
			BY_RENDER_TYPE[type.renderType] = type;
		}
	}

	public final int renderType;
	private final BlockRenderHandler handler;

	private BlockRenderType(int renderType, BlockRenderHandler handler) {
		this.renderType = renderType;
		this.handler = handler;
	}

	public BlockRenderHandler handler() {
		return this.handler;
	}

	public static BlockRenderType get(int renderType) {
		BlockRenderType type = renderType >= 0 && renderType < BY_RENDER_TYPE.length ? BY_RENDER_TYPE[renderType] : null;
		return type;
	}
}