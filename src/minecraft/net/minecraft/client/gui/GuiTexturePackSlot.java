package net.minecraft.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.texture.TexturePackBase;

class GuiTexturePackSlot extends GuiSlot {
	final GuiTexturePacks parentTexturePackGui;

	public GuiTexturePackSlot(GuiTexturePacks parentTexturePackGui) {
		super(GuiTexturePacks.getMinecraft(parentTexturePackGui), parentTexturePackGui.width, parentTexturePackGui.height, 32, parentTexturePackGui.height - 55 + 4, 36);
		this.parentTexturePackGui = parentTexturePackGui;
	}

	protected int getSize() {
		List<TexturePackBase> list = GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.availableTexturePacks();
		return list.size();
	}

	protected void elementClicked(int index, boolean doubleClick) {
		List<TexturePackBase> list = GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.availableTexturePacks();
		GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.setTexturePack((TexturePackBase)list.get(index));
		GuiTexturePacks.getMinecraft(this.parentTexturePackGui).renderEngine.refreshTextures();
	}

	protected boolean isSelected(int index) {
		List<TexturePackBase> list = GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.availableTexturePacks();
		return GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.selectedTexturePack == list.get(index);
	}

	protected int getContentHeight() {
		return this.getSize() * 36;
	}

	protected void drawBackground() {
		this.parentTexturePackGui.drawDefaultBackground();
	}

	protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator) {
		TexturePackBase pack = (TexturePackBase)GuiTexturePacks.getMinecraft(this.parentTexturePackGui).texturePackList.availableTexturePacks().get(index);
		pack.bindThumbnailTexture(GuiTexturePacks.getMinecraft(this.parentTexturePackGui));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0xFFFFFF);
		tessellator.addVertexWithUV((double)x, (double)(y + height), 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)(x + 32), (double)(y + height), 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)(x + 32), (double)y, 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV((double)x, (double)y, 0.0D, 0.0D, 0.0D);
		tessellator.draw();
		this.parentTexturePackGui.drawString(GuiTexturePacks.getFontRenderer(this.parentTexturePackGui), pack.texturePackFileName, x + 32 + 2, y + 1, 0xFFFFFF);
		this.parentTexturePackGui.drawString(GuiTexturePacks.getFontRenderer(this.parentTexturePackGui), pack.firstDescriptionLine, x + 32 + 2, y + 12, 8421504);
		this.parentTexturePackGui.drawString(GuiTexturePacks.getFontRenderer(this.parentTexturePackGui), pack.secondDescriptionLine, x + 32 + 2, y + 12 + 10, 8421504);
	}
}
