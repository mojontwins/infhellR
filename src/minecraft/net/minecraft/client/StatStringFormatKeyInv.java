package net.minecraft.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.game.IStatStringFormat;

public class StatStringFormatKeyInv implements IStatStringFormat {
	final Minecraft mc;

	public StatStringFormatKeyInv(Minecraft mc) {
		this.mc = mc;
	}

	public String formatString(String text) {
		return String.format(text, new Object[]{Keyboard.getKeyName(GameSettingsKeys.keyBindInventory.keyCode)});
	}
}
