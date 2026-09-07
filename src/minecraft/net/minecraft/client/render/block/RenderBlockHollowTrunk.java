package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.RenderBlockHollowLog;
import net.minecraft.game.world.block.Block;
import org.lwjgl.opengl.GL11;

/**
	 * Renders a hollow (pipe-like) tree trunk. Instead of drawing geometry directly it delegates to
	 * {@link RenderBlockHollowLog}, passing up to three distinct textures for bottom, top and sides
	 * (honouring the override texture when set). The in-world pass uses the block metadata to orient the
	 * hollow tube, while the inventory pass calls {@code RenderBlockHollowLog.renderAsItem} to draw an
	 * upright segment centred for the item view.
	 */
	public final class RenderBlockHollowTrunk implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Resolve the per-axis textures, mapping any override texture onto all three faces.
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		int ti_0, ti_1, ti_2;
		if(renderBlocks.overrideBlockTexture >= 0) {
			ti_0 = ti_1 = ti_2 = renderBlocks.overrideBlockTexture;
		} else {
			ti_0 = block.getBlockTextureFromSide(0);
			ti_1 = block.getBlockTextureFromSide(1);
			ti_2 = block.getBlockTextureFromSide(2);
		}
			
		RenderBlockHollowLog.renderBlock(renderBlocks.blockAccess, block, meta, x, y, z, ti_0, ti_1, ti_2);
		return true;
	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		RenderBlockUtil.applyInventoryColor(renderBlocks, block, metadata, brightness);
		RenderBlockHollowLog.renderAsItem(renderBlocks.getBlockAccess(), block, block.getBlockTextureFromSide(0), block.getBlockTextureFromSide(1), block.getBlockTextureFromSide(2), tessellator, 1.0F);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
