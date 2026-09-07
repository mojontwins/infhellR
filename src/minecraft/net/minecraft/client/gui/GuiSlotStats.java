package net.minecraft.client.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.game.StringTranslate;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatCrafting;
import net.minecraft.game.item.Item;
import net.minecraft.client.render.Tessellator;

abstract class GuiSlotStats extends GuiSlot {
    protected int currentStatsSection;
    protected List<StatBase> statsList;
    protected Comparator<Object> statsSorter;
    public int sectionId;
    /** State for the section button highlight: 0 = normal, 1 = highlighted, -1 = pressed then released. */
    public int sectionHighlightState;
    final GuiStats prevGuiStats;

    protected GuiSlotStats(GuiStats guiStats) {
        super(GuiStats.getMinecraft(guiStats), guiStats.width, guiStats.height, 32, guiStats.height - 64, 20);
        this.prevGuiStats = guiStats;
        this.currentStatsSection = -1;
        this.sectionId = -1;
        this.sectionHighlightState = 0;
        this.setRenderSelectionBox(false);
        this.setRenderHeader(true, 20);
    }

    protected void elementClicked(int index, boolean doubleClick) {
    }

    protected boolean isSelected(int index) {
        return false;
    }

    protected void drawBackground() {
        this.prevGuiStats.drawDefaultBackground();
    }

    protected void drawButtons(int x, int y, Tessellator tessellator) {
        if (!Mouse.isButtonDown(0)) {
            this.currentStatsSection = -1;
        }

        if (this.currentStatsSection == 0) {
            GuiStats.drawSprite(this.prevGuiStats, x + 115 - 18, y + 1, 0, 0);
        } else {
            GuiStats.drawSprite(this.prevGuiStats, x + 115 - 18, y + 1, 0, 18);
        }

        if (this.currentStatsSection == 1) {
            GuiStats.drawSprite(this.prevGuiStats, x + 165 - 18, y + 1, 0, 0);
        } else {
            GuiStats.drawSprite(this.prevGuiStats, x + 165 - 18, y + 1, 0, 18);
        }

        if (this.currentStatsSection == 2) {
            GuiStats.drawSprite(this.prevGuiStats, x + 215 - 18, y + 1, 0, 0);
        } else {
            GuiStats.drawSprite(this.prevGuiStats, x + 215 - 18, y + 1, 0, 18);
        }

        if (this.sectionId != -1) {
            short texX = 79;
            byte texY = 18;
            if (this.sectionId == 1) {
                texX = 129;
            } else if (this.sectionId == 2) {
                texX = 179;
            }
            if (this.sectionHighlightState == 1) {
                texY = 36;
            }
            GuiStats.drawSprite(this.prevGuiStats, x + texX, y + 1, texY, 0);
        }
    }

    @Override
    protected void drawHeader(int x, int y, Tessellator tessellator) {
        this.currentStatsSection = -1;
        if (x >= 79 && x < 115) {
            this.currentStatsSection = 0;
        } else if (x >= 129 && x < 165) {
            this.currentStatsSection = 1;
        } else if (x >= 179 && x < 215) {
            this.currentStatsSection = 2;
        }
        if (this.currentStatsSection >= 0) {
            this.toggleSection(this.currentStatsSection);
            GuiStats.getMinecraft(this.prevGuiStats).sndManager.playSoundFX("random.click", 1.0F, 1.0F);
        }
    }

    protected final int getSize() {
        return this.statsList.size();
    }

    protected final StatCrafting getCraftingStatAt(int index) {
        return (StatCrafting) this.statsList.get(index);
    }

    protected abstract String getSectionTranslationKey(int sectionIndex);

    protected void drawStatValue(StatCrafting statCrafting, int x, int y, boolean highlight) {
        String value;
        if (statCrafting != null) {
            value = statCrafting.func_27084_a(GuiStats.getStatsFileWriter(this.prevGuiStats).writeStat(statCrafting));
            this.prevGuiStats.drawString(GuiStats.getFontRenderer(this.prevGuiStats), value, x - GuiStats.getFontRenderer(this.prevGuiStats).getStringWidth(value), y + 5, highlight ? 0xFFFFFF : 9474192);
        } else {
            value = "-";
            this.prevGuiStats.drawString(GuiStats.getFontRenderer(this.prevGuiStats), value, x - GuiStats.getFontRenderer(this.prevGuiStats).getStringWidth(value), y + 5, highlight ? 0xFFFFFF : 9474192);
        }
    }

    @Override
    protected void drawFooter(int mouseX, int mouseY) {
        if (mouseY >= this.top && mouseY <= this.bottom) {
            int listIndex = this.getSlotAtMouse(mouseX, mouseY);
            int listLeft = this.prevGuiStats.width / 2 - 92 - 16;
            if (listIndex >= 0) {
                if (mouseX < listLeft + 40 || mouseX > listLeft + 40 + 20) {
                    return;
                }
                StatCrafting stat = this.getCraftingStatAt(listIndex);
                this.drawStatTooltip(stat, mouseX, mouseY);
            } else {
                String tooltipKey = "";
                if (mouseX >= listLeft + 115 - 18 && mouseX <= listLeft + 115) {
                    tooltipKey = this.getSectionTranslationKey(0);
                } else if (mouseX >= listLeft + 165 - 18 && mouseX <= listLeft + 165) {
                    tooltipKey = this.getSectionTranslationKey(1);
                } else {
                    if (mouseX < listLeft + 215 - 18 || mouseX > listLeft + 215) {
                        return;
                    }
                    tooltipKey = this.getSectionTranslationKey(2);
                }
                tooltipKey = ("" + StringTranslate.getInstance().translateKey(tooltipKey)).trim();
                if (tooltipKey.length() > 0) {
                    int tooltipX = mouseX + 12;
                    int tooltipY = mouseY - 12;
                    int tooltipWidth = GuiStats.getFontRenderer(this.prevGuiStats).getStringWidth(tooltipKey);
                    GuiStats.drawGradientRect(this.prevGuiStats, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + 8 + 3, -1073741824, -1073741824);
                    GuiStats.getFontRenderer(this.prevGuiStats).drawStringWithShadow(tooltipKey, tooltipX, tooltipY, -1);
                }
            }
        }
    }

    protected void drawStatTooltip(StatCrafting statCrafting, int mouseX, int mouseY) {
        if (statCrafting != null) {
            Item item = Item.itemsList[statCrafting.getItemId()];
            String tooltip = ("" + StringTranslate.getInstance().translateNamedKey(item.getItemName())).trim();
            if (tooltip.length() > 0) {
                int tooltipX = mouseX + 12;
                int tooltipY = mouseY - 12;
                int tooltipWidth = GuiStats.getFontRenderer(this.prevGuiStats).getStringWidth(tooltip);
                GuiStats.drawGradientRect(this.prevGuiStats, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + 8 + 3, -1073741824, -1073741824);
                GuiStats.getFontRenderer(this.prevGuiStats).drawStringWithShadow(tooltip, tooltipX, tooltipY, -1);
            }
        }
    }

    protected void toggleSection(int sectionIndex) {
        if (sectionIndex != this.sectionId) {
            this.sectionId = sectionIndex;
            this.sectionHighlightState = -1;
        } else if (this.sectionHighlightState == -1) {
            this.sectionHighlightState = 1;
        } else {
            this.sectionId = -1;
            this.sectionHighlightState = 0;
        }
        Collections.sort(this.statsList, this.statsSorter);
    }
}
