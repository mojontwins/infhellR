package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.world.block.Block;

/**
 * Renders plant-type blocks (grass, flowers, etc.) as two crossed X quads. World
 * rendering delegates to {@link RenderBlocks#renderCrossedSquares}; the inventory
 * view also uses the same crossed-squares geometry drawn as a centered quad batch.
 */
public final class RenderBlockPlant implements BlockRenderHandler {
	@Override
	// Draws the block as crossed squares in-world.
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		return renderBlocks.renderCrossedSquares(block, x, y, z);
	}

	@Override
	// Draws crossed squares centered at the origin for the inventory/drop view.
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		RenderBlockUtil.applyInventoryColor(renderBlocks, block, metadata, brightness);
		renderBlocks.renderCrossedSquares(block, metadata, -0.5D, -0.5D, -0.5D);
		tessellator.draw();
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}