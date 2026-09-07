package net.minecraft.client.gui;

import net.minecraft.game.StringTranslate;
import net.minecraft.network.packet.Packet0KeepAlive;
import net.minecraft.client.NetClientHandler;

public class GuiDownloadTerrain extends GuiScreen {
	private NetClientHandler netHandler;
	private int updateCounter = 0;

	public GuiDownloadTerrain(NetClientHandler netClientHandler) {
		this.netHandler = netClientHandler;
	}

	protected void keyTyped(char c, int keyCode) {
	}

	public void initGui() {
		this.controlList.clear();
	}

	public void updateScreen() {
		++this.updateCounter;
		if(this.updateCounter % 20 == 0) {
			this.netHandler.addToSendQueue(new Packet0KeepAlive());
		}

		if(this.netHandler != null) {
			this.netHandler.processReadPackets();
		}

	}

	protected void actionPerformed(GuiButton guiButton) {
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackground(0);
		StringTranslate translator = StringTranslate.getInstance();
		this.drawCenteredString(this.fontRenderer, translator.translateKey("multiplayer.downloadingTerrain"), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
