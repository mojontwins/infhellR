package net.minecraft.client.gui;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.world.World;

public class GuiGameOver extends GuiScreen {
	public void initGui() {
		this.controlList.clear();
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 72, "Respawn"));
		this.controlList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 96, "Title menu"));
		if(this.mc.session == null) {
			((GuiButton)this.controlList.get(1)).enabled = false;
		}

	}

	protected void keyTyped(char c, int keyCode) {
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 0) {
			;
		}

		if(guiButton.id == 1) {
			this.mc.thePlayer.respawnPlayer();
			this.mc.displayGuiScreen((GuiScreen)null);
		}

		if(guiButton.id == 2) {
			this.mc.clearWorld((World)null);
			this.mc.displayGuiScreen(new GuiMainMenu());
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		this.drawCenteredString(this.fontRenderer, "Game over!", this.width / 2 / 2, 30, 0xFFFFFF);
		GL11.glPopMatrix();
		this.drawCenteredString(this.fontRenderer, "Score: \247e" + this.mc.thePlayer.getScore(), this.width / 2, 100, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}
}
