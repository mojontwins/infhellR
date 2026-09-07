package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.game.command.CommandProcessor;
import net.minecraft.game.command.ICommandSender;
import net.minecraft.client.Minecraft;
import net.minecraft.game.ChatAllowedCharacters;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.BlockPos;

public class GuiChat extends GuiScreen implements ICommandSender{
	protected String message = "";
	private int updateCounter = 0;
	private static final String allowedCharacters = ChatAllowedCharacters.allowedCharacters;
	
	private static List<String> previousInputs = new ArrayList<String>();
	private int previousInputsCursor = 0;

	public GuiChat(Minecraft mc) {
		this.mc = mc;
	}
		
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.previousInputsCursor = GuiChat.previousInputs.size();
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void updateScreen() {
		++this.updateCounter;
	}

	protected void keyTyped(char c, int keyCode) {
		if(keyCode == 1) {
			this.mc.displayGuiScreen((GuiScreen)null);
		} else if(keyCode == 200 || keyCode == 208) {
			int curCursor = this.previousInputsCursor;
			if(GuiChat.previousInputs.size() > 0) {
				if(keyCode == 200) {
					curCursor--;
					if(curCursor < 0) curCursor = 0;
				} else {
					curCursor++;
					if(curCursor > GuiChat.previousInputs.size() - 1) curCursor = GuiChat.previousInputs.size() - 1;
				}
				
				if(curCursor != this.previousInputsCursor) {
					this.previousInputsCursor = curCursor;
					this.message = GuiChat.previousInputs.get(previousInputsCursor);
				}
			}
		} else if(keyCode == 28) {
			String trimmedMessage = this.message.trim();
			if(trimmedMessage.length() > 0) {
				if(this.mc.isRemote()) {
					String trimmedMsg = this.message.trim();
					if(!this.mc.lineIsCommand(trimmedMsg)) {
						this.mc.thePlayer.sendChatMessage(trimmedMsg);
					}
				} else {
					CommandProcessor.withCommandSender(this);
					CommandProcessor.executeCommand(this.message.trim(), this.mc.theWorld, null, this.mc.thePlayer, this.mc.thePlayer.getPlayerCoordinates());
				}
				
				GuiChat.previousInputs.add(trimmedMessage);
			}

			this.mc.displayGuiScreen((GuiScreen)null);
		} else {
			if(keyCode == 14 && this.message.length() > 0) {
				this.message = this.message.substring(0, this.message.length() - 1);
			}

			if(allowedCharacters.indexOf(c) >= 0 && this.message.length() < 256) {
				this.message = this.message + c;
			}

		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
		this.drawString(this.fontRenderer, "> " + this.message + (this.updateCounter / 6 % 2 == 0 ? "_" : ""), 4, this.height - 12, 14737632);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(mouseButton == 0) {
			if(this.mc.ingameGUI.field_933_a != null) {
				if(this.message.length() > 0 && !this.message.endsWith(" ")) {
					this.message = this.message + " ";
				}

				this.message = this.message + this.mc.ingameGUI.field_933_a;
				int maxLength = 100;
				if(this.message.length() > maxLength) {
					this.message = this.message.substring(0, maxLength);
				}
			} else {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}

	}

	@Override
	public void printMessage(World world, String message) {
		String[] subMessages = message.split("\n");
		
		for(String subMessage : subMessages) {
			this.mc.ingameGUI.addChatMessage(subMessage);
		}
		
	}
	
	@Override
	public BlockPos getMouseOverCoordinates() {
		if(this.mc.objectMouseOver == null) return null;
		
		return new BlockPos().set(
				this.mc.objectMouseOver.blockX, 
				this.mc.objectMouseOver.blockY, 
				this.mc.objectMouseOver.blockZ
		);
	}
}
