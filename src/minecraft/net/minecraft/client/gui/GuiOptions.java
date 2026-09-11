package net.minecraft.client.gui;

import net.minecraft.game.StringTranslate;
import net.minecraft.client.EnumOptions;
import net.minecraft.client.GameSettings;

public class GuiOptions extends GuiScreen {
	private GuiScreen parentScreen;
	protected String screenTitle = "Options";
	private GameSettings options;
	private static EnumOptions[] relevantOptions = new EnumOptions[]{EnumOptions.MUSIC, EnumOptions.SOUND, EnumOptions.INVERT_MOUSE, EnumOptions.SENSITIVITY, EnumOptions.DIFFICULTY, EnumOptions.MELTBUILD};

	public GuiOptions(GuiScreen parentScreen, GameSettings gameSettings) {
		this.parentScreen = parentScreen;
		this.options = gameSettings;
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.screenTitle = translator.translateKey("options.title");
		int buttonIndex = 0;
		EnumOptions[] optionsArray = relevantOptions;
		int optionsLen = optionsArray.length;

		for(int i = 0; i < optionsLen; ++i) {
			EnumOptions option = optionsArray[i]; 
			if(!option.isFloat()) {
				this.controlList.add(new GuiSmallButton(option.getOrdinal(), this.width / 2 - 155 + buttonIndex % 2 * 160, this.height / 6 + 24 * (buttonIndex >> 1), option, this.options.getKeyBinding(option)));
			} else {
				this.controlList.add(new GuiSlider(option.getOrdinal(), this.width / 2 - 155 + buttonIndex % 2 * 160, this.height / 6 + 24 * (buttonIndex >> 1), option, this.options.getKeyBinding(option), this.options.getOptionFloatValue(option)));
			}

			++buttonIndex;
		}

		this.controlList.add(new GuiButton(101, this.width / 2 - 100, this.height / 6 + 96 + 12, translator.translateKey("options.video")));
		this.controlList.add(new GuiButton(100, this.width / 2 - 100, this.height / 6 + 120 + 12, translator.translateKey("options.controls")));
		this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 170, translator.translateKey("gui.done")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id < 100 && guiButton instanceof GuiSmallButton) {
				this.options.setOptionValue(((GuiSmallButton)guiButton).returnEnumOptions(), 1);
				guiButton.displayString = this.options.getKeyBinding(EnumOptions.getEnumOptions(guiButton.id));
			}

			if(guiButton.id == 101) {
				this.mc.gameSettings.saveOptions();
				this.mc.displayGuiScreen(new GuiVideoSettings(this, this.options));
			}

			if(guiButton.id == 100) {
				this.mc.gameSettings.saveOptions();
				this.mc.displayGuiScreen(new GuiControls(this, this.options));
			}

			if(guiButton.id == 200) {
				this.mc.gameSettings.saveOptions();
				this.mc.displayGuiScreen(this.parentScreen);
			}

		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
