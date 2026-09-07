package net.minecraft.client.gui;

import net.minecraft.game.StringTranslate;
import net.minecraft.client.GameSettings;
import net.minecraft.client.GameSettingsKeys;

public class GuiControls extends GuiScreen {
	private GuiScreen parentScreen;
	protected String screenTitle = "Controls";
	private GameSettings options;
	private int buttonId = -1;

	public GuiControls(GuiScreen parentScreen, GameSettings gameSettings) {
		this.parentScreen = parentScreen;
		this.options = gameSettings;
	}

	private int leftPos() {
		return this.width / 2 - 155;
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		int left = this.leftPos();

		for(int i = 0; i < GameSettingsKeys.keyBindings.length; ++i) {
			this.controlList.add(new GuiSmallButton(i, left + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 70, 20, this.options.getOptionDisplayString(i)));
		}

		this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 170, translator.translateKey("gui.done")));
		this.screenTitle = translator.translateKey("controls.title");
	}

	protected void actionPerformed(GuiButton guiButton) {
		for(int i = 0; i < GameSettingsKeys.keyBindings.length; ++i) {
			((GuiButton)this.controlList.get(i)).displayString = this.options.getOptionDisplayString(i);
		}

		if(guiButton.id == 200) {
			this.mc.displayGuiScreen(this.parentScreen);
		} else {
			this.buttonId = guiButton.id;
			guiButton.displayString = "> " + this.options.getOptionDisplayString(guiButton.id) + " <";
		}

	}

	protected void keyTyped(char c, int keyCode) {
		if(this.buttonId >= 0) {
			this.options.setKeyBinding(this.buttonId, keyCode);
			((GuiButton)this.controlList.get(this.buttonId)).displayString = this.options.getOptionDisplayString(this.buttonId);
			this.buttonId = -1;
		} else {
			super.keyTyped(c, keyCode);
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
		int left = this.leftPos();

		for(int i = 0; i < GameSettingsKeys.keyBindings.length; ++i) {
			this.drawString(this.fontRenderer, this.options.getKeyBindingDescription(i), left + i % 2 * 160 + 70 + 6, this.height / 6 + 24 * (i >> 1) + 7, -1);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
