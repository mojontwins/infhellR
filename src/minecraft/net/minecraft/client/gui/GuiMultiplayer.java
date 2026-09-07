package net.minecraft.client.gui;

import org.lwjgl.input.Keyboard;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.GameSettingsValues;

public class GuiMultiplayer extends GuiScreen {
	private GuiScreen parentScreen;
	private GuiTextField tbServerAddress;

	public GuiMultiplayer(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	public void updateScreen() {
		this.tbServerAddress.updateCursorCounter();
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		Keyboard.enableRepeatEvents(true);
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, translator.translateKey("multiplayer.connect")));
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, translator.translateKey("gui.cancel")));
		String address = GameSettingsValues.lastServer.replaceAll("_", ":");
		((GuiButton)this.controlList.get(0)).enabled = address.length() > 0;
		this.tbServerAddress = new GuiTextField(this, this.fontRenderer, this.width / 2 - 100, this.height / 4 - 10 + 50 + 18, 200, 20, address);
		this.tbServerAddress.isFocused = true;
		this.tbServerAddress.setMaxStringLength(128);
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id == 1) {
				this.mc.displayGuiScreen(this.parentScreen);
			} else if(guiButton.id == 0) {
				String server = this.tbServerAddress.getText().trim();
				GameSettingsValues.lastServer = server.replaceAll(":", "_");
				this.mc.gameSettings.saveOptions();
				String[] parts = server.split(":");
				if(server.startsWith("[")) {
					int closeBracket = server.indexOf("]");
					if(closeBracket > 0) {
						String ipPart = server.substring(1, closeBracket);
						String portPart = server.substring(closeBracket + 1).trim();
						if(portPart.startsWith(":") && portPart.length() > 0) {
							portPart = portPart.substring(1);
							parts = new String[]{ipPart, portPart};
						} else {
							parts = new String[]{ipPart};
						}
					}
				}

				if(parts.length > 2) {
					parts = new String[]{server};
				}

				this.mc.displayGuiScreen(new GuiConnecting(this.mc, parts[0], parts.length > 1 ? this.parseIntWithDefault(parts[1], 25565) : 25565));
			}

		}
	}

	private int parseIntWithDefault(String text, int defaultValue) {
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	protected void keyTyped(char c, int keyCode) {
		this.tbServerAddress.textboxKeyTyped(c, keyCode);
		if(c == 13) {
			this.actionPerformed((GuiButton)this.controlList.get(0));
		}

		((GuiButton)this.controlList.get(0)).enabled = this.tbServerAddress.getText().length() > 0;
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.tbServerAddress.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		StringTranslate translator = StringTranslate.getInstance();
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, translator.translateKey("multiplayer.title"), this.width / 2, this.height / 4 - 60 + 20, 0xFFFFFF);
		this.drawString(this.fontRenderer, translator.translateKey("multiplayer.info1"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 0, 10526880);
		this.drawString(this.fontRenderer, translator.translateKey("multiplayer.info2"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 9, 10526880);
		this.drawString(this.fontRenderer, translator.translateKey("multiplayer.ipinfo"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 36, 10526880);
		this.tbServerAddress.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
