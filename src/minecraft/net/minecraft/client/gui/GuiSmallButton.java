package net.minecraft.client.gui;
import net.minecraft.client.EnumOptions;

public class GuiSmallButton extends GuiButton {
	private final EnumOptions enumOptions;

	public GuiSmallButton(int id, int x, int y, String label) {
		this(id, x, y, (EnumOptions)null, label);
	}

	public GuiSmallButton(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, label);
		this.enumOptions = null;
	}

	public GuiSmallButton(int id, int x, int y, EnumOptions enumOption, String label) {
		super(id, x, y, 150, 20, label);
		this.enumOptions = enumOption;
	}

	public EnumOptions returnEnumOptions() {
		return this.enumOptions;
	}
}
