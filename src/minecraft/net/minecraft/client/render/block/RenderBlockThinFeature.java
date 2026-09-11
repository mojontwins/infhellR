package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
	 * Renders a thin foliage-like feature (e.g. grass/fern tuft). It computes a per-block deterministic hash
	 * from the block coordinates, derives a rotation from the top bits of that hash, and momentarily rotates
	 * the top-face UVs by that amount before rendering the standard block, giving each tuft a slightly
	 * different visual twist. The rotation flag is reset immediately afterwards.
	 */
	public final class RenderBlockThinFeature implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Hash the block position so each block gets a stable pseudo-random rotation.
		long hash = (long)(x * 0x2fc20f) ^ (long)z * 0x6ebfff5L ^ (long)y;
		hash = hash * hash * 0x285b825L + hash * 11L;
		int rotation = (int)(hash >> 16 & 7L);
		renderBlocks.uvRotateTop = rotation & 3;
		RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);	
		renderBlocks.uvRotateTop = 0;
		return true;
	}
	
	@Override
	// Renders a plain cube for the inventory/drop view.
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}
