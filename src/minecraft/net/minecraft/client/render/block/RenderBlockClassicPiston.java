package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * Renderer for the "classic" (pre-1.1 style) piston block. This is a
 * cross-handler shim: all geometry is delegated to the static
 * {@code RenderBlockClassicPiston} helper in the
 * {@code net.minecraft.client.render.entity} package (a legacy third-party
 * implementation kept for backward compatibility).
 */
public final class RenderBlockClassicPiston implements BlockRenderHandler {
	@Override
	/** Draws the classic piston in-world via the legacy entity-package helper. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		return net.minecraft.client.render.entity.RenderBlockClassicPiston.RenderWorldBlock(renderBlocks, renderBlocks.blockAccess, x, y, z, block, 0);
	}

	@Override
	/** Renders the classic piston on an inventory slot via the legacy helper. */
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		net.minecraft.client.render.entity.RenderBlockClassicPiston.RenderInvBlock(renderBlocks, block, 0, brightness);
	}
}