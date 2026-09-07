package net.minecraft.client.gui;

import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.world.World;

public class GuiIngameMenu extends GuiScreen {
	private int updateCounter2 = 0;
	private int updateCounter = 0;

	public void initGui() {
		this.updateCounter2 = 0;
		this.controlList.clear();
		byte offset = -16;
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + offset, "Save and quit to title"));
		if(this.mc.isRemote()) {
			((GuiButton)this.controlList.get(0)).displayString = "Disconnect";
		}

		this.controlList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + offset, "Back to game"));
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + offset, "Options..."));
		this.controlList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + offset, 98, 20, StatCollector.translateToLocal("gui.achievements")));
		this.controlList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + offset, 98, 20, StatCollector.translateToLocal("gui.stats")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 0) {
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
		}

		if(guiButton.id == 1) {
			this.mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
			if(this.mc.isRemote()) {
				this.mc.theWorld.sendQuittingDisconnectingPacket();
			}

			this.mc.clearWorld((World)null);
			this.mc.displayGuiScreen(new GuiMainMenu());
		}

		if(guiButton.id == 4) {
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		}

		if(guiButton.id == 5) {
			this.mc.displayGuiScreen(new GuiAchievements(this.mc.statFileWriter));
		}

		if(guiButton.id == 6) {
			this.mc.displayGuiScreen(new GuiStats(this, this.mc.statFileWriter));
		}

	}

	public void updateScreen() {
		super.updateScreen();
		++this.updateCounter;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean saving = !this.mc.theWorld.quickSaveWorld(this.updateCounter2++);
		if(saving || this.updateCounter < 20) {
			float progress = ((float)(this.updateCounter % 10) + partialTicks) / 10.0F;
			progress = MathHelper.sin(progress * (float)Math.PI * 2.0F) * 0.2F + 0.8F;
			int grayValue = (int)(255.0F * progress);
			this.drawString(this.fontRenderer, "Saving level..", 8, this.height - 16, grayValue << 16 | grayValue << 8 | grayValue);
		}

		this.drawCenteredString(this.fontRenderer, "Game menu", this.width / 2, 40, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
