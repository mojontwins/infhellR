package net.minecraft.client.gui;

import java.util.Date;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.chunk.loader.SaveFormatComparator;
import net.minecraft.client.render.Tessellator;

class GuiWorldSlot extends GuiSlot {
	final GuiSelectWorld parentWorldGui;

	public GuiWorldSlot(GuiSelectWorld parentWorldGui) {
		super(parentWorldGui.mc, parentWorldGui.width, parentWorldGui.height, 32, parentWorldGui.height - 64, 36);
		this.parentWorldGui = parentWorldGui;
	}

	protected int getSize() {
		return GuiSelectWorld.getSize(this.parentWorldGui).size();
	}

	protected void elementClicked(int index, boolean doubleClick) {
		GuiSelectWorld.onElementSelected(this.parentWorldGui, index);
		boolean valid = GuiSelectWorld.getSelectedWorld(this.parentWorldGui) >= 0 && GuiSelectWorld.getSelectedWorld(this.parentWorldGui) < this.getSize();
		GuiSelectWorld.getSelectButton(this.parentWorldGui).enabled = valid;
		GuiSelectWorld.getRenameButton(this.parentWorldGui).enabled = valid;
		GuiSelectWorld.getDeleteButton(this.parentWorldGui).enabled = valid;
		if(doubleClick && valid) {
			this.parentWorldGui.selectWorld(index);
		}

	}

	protected boolean isSelected(int index) {
		return index == GuiSelectWorld.getSelectedWorld(this.parentWorldGui);
	}

	protected int getContentHeight() {
		return GuiSelectWorld.getSize(this.parentWorldGui).size() * 36;
	}

	protected void drawBackground() {
		this.parentWorldGui.drawDefaultBackground();
	}

	protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator) {
		SaveFormatComparator save = (SaveFormatComparator)GuiSelectWorld.getSize(this.parentWorldGui).get(index);
		String name = save.getDisplayName();
		if(name == null || MathHelper.stringNullOrLengthZero(name)) {
			name = GuiSelectWorld.func_22087_f(this.parentWorldGui) + " " + (index + 1);
		}

		String fileInfo = save.getFileName();
		fileInfo = fileInfo + " (" + GuiSelectWorld.getDateFormatter(this.parentWorldGui).format(new Date(save.getLastTimePlayed()));
		long sizeBytes = save.getSizeOnDisk();
		fileInfo = fileInfo + ", " + (float)(sizeBytes / 1024L * 100L / 1024L) / 100.0F + " MB)";
		String convertText = "";
		if(save.getRequiresConversion()) {
			convertText = GuiSelectWorld.func_22088_h(this.parentWorldGui) + " " + convertText;
		}

		this.parentWorldGui.drawString(this.parentWorldGui.fontRenderer, name, x + 2, y + 1, 0xFFFFFF);
		this.parentWorldGui.drawString(this.parentWorldGui.fontRenderer, fileInfo, x + 2, y + 12, 8421504);
		this.parentWorldGui.drawString(this.parentWorldGui.fontRenderer, convertText, x + 2, y + 12 + 10, 8421504);
	}
}
