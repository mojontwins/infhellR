package net.minecraft.client.render.tileentity;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.render.RenderEngine;

public abstract class TileEntitySpecialRenderer {
	protected TileEntityRenderer tileEntityRenderer;

	public abstract void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks);

	protected void bindTextureByName(String texturePath) {
		RenderEngine renderEngine = this.tileEntityRenderer.renderEngine;
		renderEngine.bindTexture(renderEngine.getTexture(texturePath));
	}

	public void setTileEntityRenderer(TileEntityRenderer tileEntityRenderer) {
		this.tileEntityRenderer = tileEntityRenderer;
	}

	public void cacheSpecialRenderInfo(World world) {
	}

	public FontRenderer getFontRenderer() {
		return this.tileEntityRenderer.getFontRenderer();
	}
}
