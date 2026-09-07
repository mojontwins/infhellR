package net.minecraft.client.gui;

import org.lwjgl.opengl.GL11;

public class GuiCredits extends GuiScreen {
	private GuiScreen parentScreen;
	
	public GuiCredits(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	public void initGui() {
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height - 34, "Back"));
	}
	
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			this.mc.displayGuiScreen(this.parentScreen);
		}
	}

	public void rightJustifyFrom(FontRenderer fontRenderer, String s, int x, int y, int c) {
		this.drawString(fontRenderer, s, x - fontRenderer.getStringWidth(s), y, c);
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		this.drawCenteredString(this.fontRenderer, "CREDITS", this.width / 4, 5, 16777215);
		GL11.glPopMatrix();
		
		this.drawCenteredString(this.fontRenderer, "Minecraft * InfHell * was imagined by \247ena_th_an", this.width/2, 40, 0xEEEEEE);

		int w = 70;

		this.rightJustifyFrom(this.fontRenderer, "DrZhark:", this.width/2 - w, 60, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "Benimatic:", this.width/2 - w, 70, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "Chocolatin:", this.width/2 - w, 80, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "Nitpick:", this.width/2 - w, 90, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "DirtPiper:", this.width/2 - w, 100, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "Hippoplatimus:", this.width/2 - w, 110, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "BigBang87:", this.width/2 - w, 120, 0xFFFF00);
		this.rightJustifyFrom(this.fontRenderer, "Method:", this.width/2 - w, 130, 0xFFFF00);
		
		w = 60;
		this.drawString(this.fontRenderer, "Goat models, renderers & textures", this.width/2 - w, 60, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Hollow hills and what's inside!", this.width/2 - w, 70, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Pirate ship schematics!", this.width/2 - w, 80, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Awesome textures with an Alpha feel", this.width/2 - w, 90, 0xFFFFFF);
		this.drawString(this.fontRenderer, "NSSS was the main inspiration", this.width/2 - w, 100, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Classic, original, real, cool pistons!", this.width/2 - w, 110, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Hit cows with a stick and you'll see.", this.width/2 - w, 120, 0xFFFFFF);
		this.drawString(this.fontRenderer, "Endless source of wisdom, support & ideas", this.width/2 - w, 130, 0xFFFFFF);
		
		this.drawCenteredString(this.fontRenderer, "Additional misc mob textures by", this.width/2, 150, 0xFFFF00);
		this.drawCenteredString(this.fontRenderer, "Tamano, Gold Miner, mountainRoses, LePoulet", this.width/2, 160, 0xFFFFFF);
		this.drawCenteredString(this.fontRenderer, "Minervo Ionni, Soiboi, Teethguy, ziutek420", this.width/2, 170, 0xFFFFFF);
		
		this.drawCenteredString(this.fontRenderer, "if you feel you should appear on this screen, contact me!", this.width/2, 190, 0xEEEEEE);
		
	
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
