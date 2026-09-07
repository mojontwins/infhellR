package net.minecraft.client.gui;

public class GuiYesNo extends GuiScreen {
	private GuiScreen parentScreen;
	private String message1;
	private String message2;
	private String confirmButtonText;
	private String cancelButtonText;
	private int worldNumber;

	public GuiYesNo(GuiScreen parentScreen, String question, String warning, String confirmText, String cancelText, int worldNumber) {
		this.parentScreen = parentScreen;
		this.message1 = question;
		this.message2 = warning;
		this.confirmButtonText = confirmText;
		this.cancelButtonText = cancelText;
		this.worldNumber = worldNumber;
	}

	public void initGui() {
		this.controlList.add(new GuiSmallButton(0, this.width / 2 - 155 + 0, this.height / 6 + 96, this.confirmButtonText));
		this.controlList.add(new GuiSmallButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.cancelButtonText));
	}

	protected void actionPerformed(GuiButton guiButton) {
		this.parentScreen.deleteWorld(guiButton.id == 0, this.worldNumber);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.message1, this.width / 2, 70, 0xFFFFFF);
		this.drawCenteredString(this.fontRenderer, this.message2, this.width / 2, 90, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
