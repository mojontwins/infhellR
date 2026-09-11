package net.minecraft.client.gui;

import net.minecraft.game.StringTranslate;
import net.minecraft.client.EnumOptions;
import net.minecraft.client.GameSettings;

public class GuiVideoSettings extends GuiScreen {
	private GuiScreen parentGuiScreen;
	protected String screenTitle = "Video Settings";
	private GameSettings guiGameSettings;
	private static EnumOptions[] videoOptions = new EnumOptions[]{
			EnumOptions.DISPLAY_MODES,
			EnumOptions.GRAPHICS, 
			
			EnumOptions.GAMMA,
			EnumOptions.AO_LEVEL,
			
			EnumOptions.RENDER_DISTANCE, 
			EnumOptions.FRAMERATE_LIMIT, 
			
			EnumOptions.CLEAR_WATERS, 
			EnumOptions.COLOURED_ATHMOSPHERICS,
			
			EnumOptions.FOV,
			EnumOptions.VIEW_BOBBING, 
			
			EnumOptions.GUI_SCALE, 		
			EnumOptions.HAND,
			
			EnumOptions.MIPMAP_LEVEL, 		
			EnumOptions.MIPMAP_TYPE,
						
			EnumOptions.ADVANCED_OPENGL,
			EnumOptions.USE_VBO,
			
	};

	public GuiVideoSettings(GuiScreen parentGuiScreen, GameSettings gameSettings) {
		this.parentGuiScreen = parentGuiScreen;
		this.guiGameSettings = gameSettings;
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.screenTitle = translator.translateKey("options.videoTitle");
		int buttonIndex = 0;
		EnumOptions[] optionsArray = videoOptions;
		int optionsLen = optionsArray.length;

		for(int i = 0; i < optionsLen; ++i) {
			EnumOptions option = optionsArray[i];
			if(!option.isFloat()) {
				this.controlList.add(new GuiSmallButton(option.getOrdinal(), this.width / 2 - 155 + buttonIndex % 2 * 160, 32 + 21 * (buttonIndex >> 1), option, this.guiGameSettings.getKeyBinding(option)));
			} else {
				this.controlList.add(new GuiSlider(option.getOrdinal(), this.width / 2 - 155 + buttonIndex % 2 * 160, 32 + 21 * (buttonIndex >> 1), option, this.guiGameSettings.getKeyBinding(option), this.guiGameSettings.getOptionFloatValue(option)));
			}

			++buttonIndex;
		}

		this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 170, translator.translateKey("gui.done")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id < 100 && guiButton instanceof GuiSmallButton) {
				this.guiGameSettings.setOptionValue(((GuiSmallButton)guiButton).returnEnumOptions(), 1);
				guiButton.displayString = this.guiGameSettings.getKeyBinding(EnumOptions.getEnumOptions(guiButton.id));
			}

			if(guiButton.id == 200) {
				this.mc.gameSettings.saveOptions();
				this.mc.displayGuiScreen(this.parentGuiScreen);
			}

			ScaledResolution scaledRes = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			int scaledWidth = scaledRes.getScaledWidth();
			int scaledHeight = scaledRes.getScaledHeight();
			this.setWorldAndResolution(this.mc, scaledWidth, scaledHeight);
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 16, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
