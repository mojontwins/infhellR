package net.minecraft.client.gui;

import net.minecraft.game.StringTranslate;

public class GuiDisconnected extends GuiScreen {
	private String errorMessage;
	private String errorDetail;

	public GuiDisconnected(String messageKey, String detailKey, Object... formatArgs) {
		StringTranslate translator = StringTranslate.getInstance();
		this.errorMessage = translator.translateKey(messageKey);
		if(formatArgs != null) {
			this.errorDetail = translator.translateKeyFormat(detailKey, formatArgs);
		} else {
			this.errorDetail = translator.translateKey(detailKey);
		}

	}

	public void updateScreen() {
	}

	protected void keyTyped(char c, int keyCode) {
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, translator.translateKey("gui.toMenu")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 0) {
			this.mc.displayGuiScreen(new GuiMainMenu());
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.errorMessage, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
		this.drawCenteredString(this.fontRenderer, this.errorDetail, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
