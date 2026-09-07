package net.minecraft.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

public class GuiButtonTrading extends net.minecraft.client.gui.GuiButton {
	private boolean isForward;
	
	public GuiButtonTrading(int id, int x, int y, boolean isForward) {
		super(id, x, y, 12, 19, "");
		this.isForward = isForward;
	}
	
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if(this.drawButton) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.renderEngine.getTexture("/gui/trading.png"));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			boolean mouseHover = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int v = 0;
			int u = 176;
			
			if(!this.enabled) {
				u += this.width * 2;
			} else if(mouseHover) {
				u += this.width;
			}
			
			if(!this.isForward) {
				v += this.height;
			}
			
			this.drawTexturedModalRect(this.xPosition, this.yPosition, u, v, this.width, this.height);
		}
	}
}
