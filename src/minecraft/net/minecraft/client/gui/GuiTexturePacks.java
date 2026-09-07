package net.minecraft.client.gui;

import java.io.File;

import net.minecraft.client.Minecraft;

import org.lwjgl.Sys;
import net.minecraft.game.StringTranslate;

public class GuiTexturePacks extends GuiScreen {
	protected GuiScreen guiScreen;
	private int refreshTimer = -1;
	private String fileLocation = "";
	private GuiTexturePackSlot guiTexturePackSlot;

	public GuiTexturePacks(GuiScreen guiScreen) {
		this.guiScreen = guiScreen;
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.add(new GuiSmallButton(5, this.width / 2 - 154, this.height - 48, translator.translateKey("texturePack.openFolder")));
		this.controlList.add(new GuiSmallButton(6, this.width / 2 + 4, this.height - 48, translator.translateKey("gui.done")));
		this.mc.texturePackList.updateAvaliableTexturePacks();
		this.fileLocation = (new File(Minecraft.getMinecraftDir(), "texturepacks")).getAbsolutePath();
		this.guiTexturePackSlot = new GuiTexturePackSlot(this);
		this.guiTexturePackSlot.registerScrollButtons(this.controlList, 7, 8);
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id == 5) {
				Sys.openURL("file://" + this.fileLocation);
			} else if(guiButton.id == 6) {
				this.mc.renderEngine.refreshTextures();
				this.mc.displayGuiScreen(this.guiScreen);
			} else {
				this.guiTexturePackSlot.actionPerformed(guiButton);
			}

		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
	}

	protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
		super.mouseMovedOrUp(mouseX, mouseY, button);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.guiTexturePackSlot.drawScreen(mouseX, mouseY, partialTicks);
		if(this.refreshTimer <= 0) {
			this.mc.texturePackList.updateAvaliableTexturePacks();
			this.refreshTimer += 20;
		}

		StringTranslate translator = StringTranslate.getInstance();
		this.drawCenteredString(this.fontRenderer, translator.translateKey("texturePack.title"), this.width / 2, 16, 0xFFFFFF);
		this.drawCenteredString(this.fontRenderer, translator.translateKey("texturePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void updateScreen() {
		super.updateScreen();
		--this.refreshTimer;
	}

	static Minecraft getMinecraft(GuiTexturePacks guiTexturePacks) {
		return guiTexturePacks.mc;
	}

	static FontRenderer getFontRenderer(GuiTexturePacks guiTexturePacks) {
		return guiTexturePacks.fontRenderer;
	}

}
