package net.minecraft.client.render.tileentity;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityPiston;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.world.block.BlockPistonBase;

public class TileEntityRendererPiston extends TileEntitySpecialRenderer {
	private RenderBlocks renderBlocks;

	public void func_31070_a(TileEntityPiston piston, double x, double y, double z, float partialTicks) {
		Block storedBlock = Block.blocksList[piston.getStoredBlockID()];
		if(storedBlock != null && piston.getProgress(partialTicks) < 1.0F) {
			Tessellator tessellator = Tessellator.instance;
			this.bindTextureByName("/terrain.png");
			RenderHelper.disableStandardItemLighting();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			if(Minecraft.isAmbientOcclusionEnabled()) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
			} else {
				GL11.glShadeModel(GL11.GL_FLAT);
			}

			tessellator.startDrawingQuads();
			tessellator.setTranslation((double)((float)x - (float)piston.xCoord + piston.func_31017_b(partialTicks)), (double)((float)y - (float)piston.yCoord + piston.func_31014_c(partialTicks)), (double)((float)z - (float)piston.zCoord + piston.func_31013_d(partialTicks)));
			tessellator.setColorOpaque(1, 1, 1);
			if(storedBlock == Block.pistonExtension && piston.getProgress(partialTicks) < 0.5F) {
				this.renderBlocks.renderPistonExtensionAllFaces(storedBlock, piston.xCoord, piston.yCoord, piston.zCoord, false);
			} else if(piston.func_31012_k() && !piston.getExtending()) {
				Block.pistonExtension.setHeadTexture(((BlockPistonBase)storedBlock).getPistonExtensionTexture());
				this.renderBlocks.renderPistonExtensionAllFaces(Block.pistonExtension, piston.xCoord, piston.yCoord, piston.zCoord, piston.getProgress(partialTicks) < 0.5F);
				Block.pistonExtension.clearHeadTexture();
				tessellator.setTranslation((double)((float)x - (float)piston.xCoord), (double)((float)y - (float)piston.yCoord), (double)((float)z - (float)piston.zCoord));
				this.renderBlocks.renderPistonBaseAllFaces(storedBlock, piston.xCoord, piston.yCoord, piston.zCoord);
			} else {
				this.renderBlocks.renderBlockAllFaces(storedBlock, piston.xCoord, piston.yCoord, piston.zCoord);
			}

			tessellator.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
			RenderHelper.enableStandardItemLighting();
		}

	}

	public void cacheSpecialRenderInfo(World world) {
		this.renderBlocks = new RenderBlocks(world);
	}

	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		this.func_31070_a((TileEntityPiston)tileEntity, x, y, z, partialTicks);
	}
}
