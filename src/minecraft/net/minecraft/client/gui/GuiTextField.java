package net.minecraft.client.gui;

import net.minecraft.game.ChatAllowedCharacters;

public class GuiTextField extends Gui {
	private final FontRenderer fontRenderer;
	private final int xPos;
	private final int yPos;
	private final int width;
	private final int height;
	private String text;
	private int maxStringLength;
	private int cursorCounter;
	public boolean isFocused = false;
	public boolean isEnabled = true;
	private GuiScreen parentGuiScreen;
	private boolean canLoseFocus = true;
	private boolean enableBackgroundDrawing = true;
	
	public GuiTextField(GuiScreen parentGuiScreen, FontRenderer fontRenderer, int x, int y, int width, int height, String text) {
		this.parentGuiScreen = parentGuiScreen;
		this.fontRenderer = fontRenderer;
		this.xPos = x;
		this.yPos = y;
		this.width = width;
		this.height = height;
		this.setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void updateCursorCounter() {
		++this.cursorCounter;
	}

	public boolean textboxKeyTyped(char c, int keyCode) {
		if(this.isEnabled && this.isFocused) {
			if(c == 9) {
				this.parentGuiScreen.selectNextField();
				return true;
			}

			if(c == 22) {
				String clipboard = GuiScreen.getClipboardString();
				if(clipboard == null) {
					clipboard = "";
				}

				int maxPaste = 32 - this.text.length();
				if(maxPaste > clipboard.length()) {
					maxPaste = clipboard.length();
				}

				if(maxPaste > 0) {
					this.text = this.text + clipboard.substring(0, maxPaste);
				}
				
				return true;
			}

			if(keyCode == 14 && this.text.length() > 0) {
				this.text = this.text.substring(0, this.text.length() - 1);
				return true;
			}

			if(ChatAllowedCharacters.allowedCharacters.indexOf(c) >= 0 && (this.text.length() < this.maxStringLength || this.maxStringLength == 0)) {
				this.text = this.text + c;
				return true;
			}

		}
		
		return false;
	}

	public void mouseClicked(int mouseX, int mouseY, int button) {
		boolean inside = this.isEnabled && mouseX >= this.xPos && mouseX < this.xPos + this.width && mouseY >= this.yPos && mouseY < this.yPos + this.height;
		if (this.canLoseFocus) {
			this.setFocused(inside);
		}
	}

	public void setFocused(boolean focused) {
		if(focused && !this.isFocused) {
			this.cursorCounter = 0;
		}

		this.isFocused = focused;
	}

	public void drawTextBox() {
		if (this.isEnableBackgroundDrawing()) {
			this.drawRect(this.xPos - 1, this.yPos - 1, this.xPos + this.width + 1, this.yPos + this.height + 1, -6250336);
			this.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.height, 0xFF000000);
		}
		
		int x = this.enableBackgroundDrawing ? this.xPos + 4 : this.xPos;
		int y = this.enableBackgroundDrawing ? this.yPos + (this.height - 8) / 2 : this.yPos;
		
		int c1 = this.enableBackgroundDrawing ? 0xE0E0E0 : 0x222222;
		int c2 = this.enableBackgroundDrawing ? 0x707070 : 0x222222;
		
		if(this.isEnabled) {
			boolean showCursor = this.isFocused && this.cursorCounter / 6 % 2 == 0;
			this.drawString(this.fontRenderer, this.text + (showCursor ? "_" : ""), x, y, c1);
		} else {
			this.drawString(this.fontRenderer, this.text, x, y, c2);
		}

	}

	public void setMaxStringLength(int max) {
		this.maxStringLength = max;
	}

	public void setCanLoseFocus(boolean canLose) {
		this.canLoseFocus = canLose;
	}
	
	public boolean isEnableBackgroundDrawing() {
		return this.enableBackgroundDrawing;
	}

	public void setEnableBackgroundDrawing(boolean enabled) {
		this.enableBackgroundDrawing = enabled;
	}
}
