package net.minecraft.client.gui;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.Tessellator;

public class Gui {
	protected float zLevel = 0.0F;

	/**
	 * Draws a 1-pixel-wide vertical bar in the achievement tree, connecting two achievement nodes.
	 * The bar spans from (x1, y) to (x2, y+1) after swapping to normalize x1 > x2.
	 *
	 * @param x1   first X coordinate
	 * @param x2   second X coordinate
	 * @param y    Y coordinate
	 * @param color packed ARGB color
	 */
	protected void drawVerticalAchievementConnector(int x1, int x2, int y, int color) {
		if (x2 < x1) {
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		this.drawRect(x1, y, x2 + 1, y + 1, color);
	}

	/**
	 * Draws a 1-pixel-wide horizontal bar in the achievement tree, connecting two achievement nodes.
	 * The bar spans from (x, y1) to (x+1, y2) after swapping to normalize y1 > y2.
	 *
	 * @param x     X coordinate
	 * @param y1    first Y coordinate
	 * @param y2    second Y coordinate
	 * @param color packed ARGB color
	 */
	protected void drawHorizontalAchievementConnector(int x, int y1, int y2, int color) {
		if (y2 < y1) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}
		this.drawRect(x, y1 + 1, x + 1, y2, color);
	}

	protected void drawRect(int x1, int y1, int x2, int y2, int color) {
		int temp;
		if(x1 < x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}

		if(y1 < y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		float alpha = (float)(color >> 24 & 255) / 255.0F;
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(red, green, blue, alpha);
		tessellator.startDrawingQuads();
		tessellator.addVertex((double)x1, (double)y2, 0.0D);
		tessellator.addVertex((double)x2, (double)y2, 0.0D);
		tessellator.addVertex((double)x2, (double)y1, 0.0D);
		tessellator.addVertex((double)x1, (double)y1, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}

	protected void drawGradientRect(int x1, int y1, int x2, int y2, int startColor, int endColor) {
		float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
		float startRed = (float)(startColor >> 16 & 255) / 255.0F;
		float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
		float startBlue = (float)(startColor & 255) / 255.0F;
		float endAlpha = (float)(endColor >> 24 & 255) / 255.0F;
		float endRed = (float)(endColor >> 16 & 255) / 255.0F;
		float endGreen = (float)(endColor >> 8 & 255) / 255.0F;
		float endBlue = (float)(endColor & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(startRed, startGreen, startBlue, startAlpha);
		tessellator.addVertex((double)x2, (double)y1, 0.0D);
		tessellator.addVertex((double)x1, (double)y1, 0.0D);
		tessellator.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
		tessellator.addVertex((double)x1, (double)y2, 0.0D);
		tessellator.addVertex((double)x2, (double)y2, 0.0D);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
	}

	public void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		float uScale = 0.00390625F;
		float vScale = 0.00390625F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), (double)this.zLevel, (double)((float)(u + 0) * uScale), (double)((float)(v + height) * vScale));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + height), (double)this.zLevel, (double)((float)(u + width) * uScale), (double)((float)(v + height) * vScale));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), (double)this.zLevel, (double)((float)(u + width) * uScale), (double)((float)(v + 0) * vScale));
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)this.zLevel, (double)((float)(u + 0) * uScale), (double)((float)(v + 0) * vScale));
		tessellator.draw();
	}
	
	public String twoDigits(int d) {
		if (d < 10) return "0" + d;
		else return "" + d;
	}
}
