package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.EnumOptions;

public class GuiSlider extends GuiButton {
	public float sliderValue = 1.0F;
	public boolean dragging = false;
	private EnumOptions idFloat = null;

	public GuiSlider(int id, int x, int y, EnumOptions enumOption, String label, float value) {
		super(id, x, y, 150, 20, label);
		this.idFloat = enumOption;
		this.sliderValue = value;
	}

	protected int getHoverState(boolean isHovered) {
		return 0;
	}

	protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
		if(this.drawButton) {
			if(this.dragging) {
				this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
				if(this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if(this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}

				minecraft.gameSettings.setOptionFloatValue(this.idFloat, this.sliderValue);
				this.displayString = minecraft.gameSettings.getKeyBinding(this.idFloat);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
			this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
		}
	}

	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
		if(super.mousePressed(minecraft, mouseX, mouseY)) {
			this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
			if(this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if(this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			minecraft.gameSettings.setOptionFloatValue(this.idFloat, this.sliderValue);
			this.displayString = minecraft.gameSettings.getKeyBinding(this.idFloat);
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	public void mouseReleased(int mouseX, int mouseY) {
		this.dragging = false;
	}
}
