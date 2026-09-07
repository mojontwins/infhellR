package net.minecraft.client.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.EntityClientPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.game.StringTranslate;
import net.minecraft.network.packet.Packet19EntityAction;
import net.minecraft.client.NetClientHandler;

public class GuiSleepMP extends GuiChat {
	public GuiSleepMP(Minecraft mc) {
		super(mc);
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height - 40, translator.translateKey("multiplayer.stopSleeping")));
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void keyTyped(char c, int keyCode) {
		if(keyCode == 1) {
			this.func_22115_j();
		} else if(keyCode == 28) {
			String trimmedMessage = this.message.trim();
			if(trimmedMessage.length() > 0) {
				this.mc.thePlayer.sendChatMessage(this.message.trim());
			}

			this.message = "";
		} else {
			super.keyTyped(c, keyCode);
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 1) {
			this.func_22115_j();
		} else {
			super.actionPerformed(guiButton);
		}

	}

	private void func_22115_j() {
		if(this.mc.thePlayer instanceof EntityClientPlayerMP) {
			NetClientHandler netClientHandler = ((EntityClientPlayerMP)this.mc.thePlayer).sendQueue;
			netClientHandler.addToSendQueue(new Packet19EntityAction(this.mc.thePlayer, 3));
		}

	}
}
