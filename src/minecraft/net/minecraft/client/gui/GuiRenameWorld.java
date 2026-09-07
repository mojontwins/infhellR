package net.minecraft.client.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.game.StringTranslate;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.chunk.loader.ISaveFormat;

public class GuiRenameWorld extends GuiScreen {
	private GuiScreen parentGuiScreen;
	private GuiTextField theGuiTextField;
	private final String worldName;

	public GuiRenameWorld(GuiScreen parentGuiScreen, String worldName) {
		this.parentGuiScreen = parentGuiScreen;
		this.worldName = worldName;
	}

	public void updateScreen() {
		this.theGuiTextField.updateCursorCounter();
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		Keyboard.enableRepeatEvents(true);
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, translator.translateKey("selectWorld.renameButton")));
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, translator.translateKey("gui.cancel")));
		ISaveFormat saveLoader = this.mc.getSaveLoader();
		WorldInfo worldInfo = saveLoader.getWorldInfo(this.worldName);
		String currentName = worldInfo.getWorldName();
		this.theGuiTextField = new GuiTextField(this, this.fontRenderer, this.width / 2 - 100, 60, 200, 20, currentName);
		this.theGuiTextField.isFocused = true;
		this.theGuiTextField.setMaxStringLength(32);
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id == 1) {
				this.mc.displayGuiScreen(this.parentGuiScreen);
			} else if(guiButton.id == 0) {
				ISaveFormat saveLoader = this.mc.getSaveLoader();
				saveLoader.renameWorld(this.worldName, this.theGuiTextField.getText().trim());
				this.mc.displayGuiScreen(this.parentGuiScreen);
			}

		}
	}

	protected void keyTyped(char c, int keyCode) {
		this.theGuiTextField.textboxKeyTyped(c, keyCode);
		((GuiButton)this.controlList.get(0)).enabled = this.theGuiTextField.getText().trim().length() > 0;
		if(c == 13) {
			this.actionPerformed((GuiButton)this.controlList.get(0));
		}

	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.theGuiTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		StringTranslate translator = StringTranslate.getInstance();
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.renameTitle"), this.width / 2, this.height / 4 - 60 + 20, 0xFFFFFF);
		this.drawString(this.fontRenderer, translator.translateKey("selectWorld.enterName"), this.width / 2 - 100, 47, 10526880);
		this.theGuiTextField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
