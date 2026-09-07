package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.world.World;
import net.minecraft.client.NetClientHandler;
import net.minecraft.client.ThreadConnectToServer;

public class GuiConnecting extends GuiScreen {
	private NetClientHandler clientHandler;
	private boolean cancelled = false;

	public GuiConnecting(Minecraft minecraft, String serverAddress, int serverPort) {
		System.out.println("Connecting to " + serverAddress + ", " + serverPort);
		minecraft.clearWorld((World)null);
		(new ThreadConnectToServer(this, minecraft, serverAddress, serverPort)).start();
	}

	public void updateScreen() {
		if(this.clientHandler != null) {
			this.clientHandler.processReadPackets();
		}

	}

	protected void keyTyped(char c, int keyCode) {
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, translator.translateKey("gui.cancel")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 0) {
			this.cancelled = true;
			if(this.clientHandler != null) {
				this.clientHandler.disconnect();
			}

			this.mc.displayGuiScreen(new GuiMainMenu());
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		StringTranslate translator = StringTranslate.getInstance();
		if(this.clientHandler == null) {
			this.drawCenteredString(this.fontRenderer, translator.translateKey("connect.connecting"), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
			this.drawCenteredString(this.fontRenderer, "", this.width / 2, this.height / 2 - 10, 0xFFFFFF);
		} else {
			this.drawCenteredString(this.fontRenderer, translator.translateKey("connect.authorizing"), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
			this.drawCenteredString(this.fontRenderer, this.clientHandler.serverName, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	static public NetClientHandler setNetClientHandler(GuiConnecting guiConnecting, NetClientHandler netClientHandler) {
		return guiConnecting.clientHandler = netClientHandler;
	}

	static public boolean isCancelled(GuiConnecting guiConnecting) {
		return guiConnecting.cancelled;
	}

	static public NetClientHandler getNetClientHandler(GuiConnecting guiConnecting) {
		return guiConnecting.clientHandler;
	}
}
