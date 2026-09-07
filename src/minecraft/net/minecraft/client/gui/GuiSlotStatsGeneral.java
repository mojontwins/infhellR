package net.minecraft.client.gui;

import net.minecraft.client.render.Tessellator;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatList;

class GuiSlotStatsGeneral extends GuiSlot {
    private final GuiStats guiStats;

    public GuiSlotStatsGeneral(GuiStats guiStats) {
        super(GuiStats.getMinecraft(guiStats), guiStats.width, guiStats.height, 32, guiStats.height - 64, 10);
        this.guiStats = guiStats;
        this.setRenderSelectionBox(false);
    }

    protected int getSize() {
        return StatList.generalStats.size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
    }

    protected boolean isSelected(int index) {
        return false;
    }

    protected int getContentHeight() {
        return this.getSize() * 10;
    }

    protected void drawBackground() {
        this.guiStats.drawDefaultBackground();
    }

    protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator) {
        StatBase stat = (StatBase) StatList.generalStats.get(index);
        this.guiStats.drawString(GuiStats.getFontRenderer(this.guiStats), stat.statName, x + 2, y + 1, index % 2 == 0 ? 0xFFFFFF : 9474192);
        String value = stat.func_27084_a(GuiStats.getStatsFileWriter(this.guiStats).writeStat(stat));
        this.guiStats.drawString(GuiStats.getFontRenderer(this.guiStats), value, x + 2 + 213 - GuiStats.getFontRenderer(this.guiStats).getStringWidth(value), y + 1, index % 2 == 0 ? 0xFFFFFF : 9474192);
    }
}
