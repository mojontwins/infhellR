package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class GuiButton extends Gui {
	public int width;
	public int height;
	public int xPosition;
	public int yPosition;
	public String displayString;
	public int id;
	public boolean enabled;
	public boolean drawButton;

	public String toolTip = null;
	
	public GuiButton(int id, int x, int y, String label) {
		this(id, x, y, 200, 20, label);
	}

	public GuiButton(int id, int x, int y, int width, int height, String label) {
		this.width = 200;
		this.height = 20;
		this.enabled = true;
		this.drawButton = true;
		this.id = id;
		this.xPosition = x;
		this.yPosition = y;
		this.width = width;
		this.height = height;
		this.displayString = label;
	}

	protected int getHoverState(boolean isHovered) {
		byte state = 1;
		if(!this.enabled) {
			state = 0;
		} else if(isHovered) {
			state = 2;
		}

		return state;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if(this.drawButton) {
			FontRenderer fontRenderer = mc.fontRenderer;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/gui.png"));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			
			boolean hover = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			
			int hs = this.getHoverState(hover);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hs * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hs * 20, this.width / 2, this.height);
			
			this.mouseDragged(mc, mouseX, mouseY);
			
			if(!this.enabled) {
				this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, -6250336);
			} else if(hover) {
				this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 16777120);
			} else {
				this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 14737632);
			}

		}
	}

	protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
	}

	public void mouseReleased(int mouseX, int mouseY) {
	}

	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
		return this.enabled && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
	}
}
