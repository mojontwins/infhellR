package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * A renderer for one block render-type. Each {@link BlockRenderType} owns exactly one
 * handler; handlers emit geometry directly into the tessellator (usually through the
 * engine's face emitters and {@link RenderBlockUtil} helpers), then restore the engine
 * state they touched so the next block starts clean.
 */
public interface BlockRenderHandler {
	/**
	 * Draws one instance of {@code block} at world position (x, y, z) into the current
	 * tessellator batch.
	 *
	 * @return true if any geometry was emitted
	 */
	boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z);

	/**
	 * Optional inventory/held-item draw for this block type. The default does nothing;
	 * handlers that mimic their world look (e.g. stairs, panes) override it, while flat /
	 * plant-like types stay with the engine's generic path.
	 */
	default void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
	}

	/**
	 * Whether this block type renders as a 3-D model when shown as an inventory item or
	 * held entity (as opposed to a flattened 2-D sprite).
	 *
	 * <p>Returning {@code true} causes {@link RenderBlocks#renderItemIn3d} to direct the
	 * renderer down the 3-D path; returning {@code false} uses the flat sprite.</p>
	 *
	 * <p>Flat types that draw their geometry as crossed squares, single quads, or thin
	 * bars should return {@code false}. Block types with volumetric geometry
	 * (cubes, stairs, fences, etc.) should return {@code true} (the default).</p>
	 */
	default boolean renderItemIn3d() {
		return true;
	}
}