package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * Renderer for the base (housing) of a piston. Delegates to the shared
 * piston-base geometry routine in {@link RenderBlocks}, always drawn in the
 * non-extended (pushed out) orientation. Note the parity quirk: the client
 * delegates to RenderBlocks while other handlers build geometry locally.
 */
public final class RenderBlockPistonBase implements BlockRenderHandler {
	@Override
	/** Draws the piston base body via renderPistonBase in the un-extended pose. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		return renderBlocks.renderPistonBase(block, x, y, z, false);
	}

	@Override
	/** Renders the piston base as a plain cube when shown on an inventory slot. */
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, 1, brightness);
	}
}