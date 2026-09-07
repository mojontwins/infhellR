package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.SorterStatsBlock;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatCrafting;
import net.minecraft.game.achievements.StatList;

public class GuiSlotStatsBlock extends GuiSlotStats {
	public final GuiStats guiStats;

	public GuiSlotStatsBlock(GuiStats guiStats) {
		super(guiStats);
		this.guiStats = guiStats;
		this.statsList = new ArrayList<StatBase>();
		Iterator<StatBase> iterator = StatList.objectMineStats.iterator();

		while(iterator.hasNext()) {
			StatCrafting stat = (StatCrafting)iterator.next();
			boolean hasStats = false;
			int itemId = stat.getItemId();
			if(GuiStats.getStatsFileWriter(guiStats).writeStat(stat) > 0) {
				hasStats = true;
			} else if(StatList.objectUseStats[itemId] != null && GuiStats.getStatsFileWriter(guiStats).writeStat(StatList.objectUseStats[itemId]) > 0) {
				hasStats = true;
			} else if(StatList.objectCraftStats[itemId] != null && GuiStats.getStatsFileWriter(guiStats).writeStat(StatList.objectCraftStats[itemId]) > 0) {
				hasStats = true;
			}

			if(hasStats) {
				this.statsList.add(stat);
			}
		}

		this.statsSorter = new SorterStatsBlock(this, guiStats);
	}

	protected void drawButtons(int x, int y, Tessellator tessellator) {
		super.drawButtons(x, y, tessellator);
		if(this.currentStatsSection == 0) {
			GuiStats.drawSprite(this.guiStats, x + 115 - 18 + 1, y + 1 + 1, 18, 18);
		} else {
			GuiStats.drawSprite(this.guiStats, x + 115 - 18, y + 1, 18, 18);
		}

		if(this.currentStatsSection == 1) {
			GuiStats.drawSprite(this.guiStats, x + 165 - 18 + 1, y + 1 + 1, 36, 18);
		} else {
			GuiStats.drawSprite(this.guiStats, x + 165 - 18, y + 1, 36, 18);
		}

		if(this.currentStatsSection == 2) {
			GuiStats.drawSprite(this.guiStats, x + 215 - 18 + 1, y + 1 + 1, 54, 18);
		} else {
			GuiStats.drawSprite(this.guiStats, x + 215 - 18, y + 1, 54, 18);
		}

	}

		protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator) {
		StatCrafting stat = this.getCraftingStatAt(index);
		int itemId = stat.getItemId();
		GuiStats.drawItemSprite(this.guiStats, x + 40, y, itemId);
		this.drawStatValue((StatCrafting) StatList.objectCraftStats[itemId], x + 115, y, index % 2 == 0);
		this.drawStatValue((StatCrafting) StatList.objectUseStats[itemId], x + 165, y, index % 2 == 0);
		this.drawStatValue(stat, x + 215, y, index % 2 == 0);
	}

	protected String getSectionTranslationKey(int sectionIndex) {
		return sectionIndex == 0 ? "stat.crafted" : (sectionIndex == 1 ? "stat.used" : "stat.mined");
	}
}
