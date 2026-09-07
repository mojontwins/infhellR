package net.minecraft.client;

import java.util.Comparator;
import net.minecraft.client.gui.GuiSlotStatsItem;
import net.minecraft.client.gui.GuiStats;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatCrafting;
import net.minecraft.game.achievements.StatList;

public class SorterStatsItem implements Comparator<Object> {
	final GuiStats statsGUI;
	final GuiSlotStatsItem slotStatsItemGUI;

	public SorterStatsItem(GuiSlotStatsItem slotGui, GuiStats statsGui) {
		this.slotStatsItemGUI = slotGui;
		this.statsGUI = statsGui;
	}

	public int sortByStat(StatCrafting left, StatCrafting right) {
		int leftId = left.getItemId();
		int rightId = right.getItemId();
		StatBase leftStat = null;
		StatBase rightStat = null;
		if (this.slotStatsItemGUI.sectionId == 0) {
			leftStat = StatList.objectBreakStats[leftId];
			rightStat = StatList.objectBreakStats[rightId];
		} else if (this.slotStatsItemGUI.sectionId == 1) {
			leftStat = StatList.objectCraftStats[leftId];
			rightStat = StatList.objectCraftStats[rightId];
		} else if (this.slotStatsItemGUI.sectionId == 2) {
			leftStat = StatList.objectUseStats[leftId];
			rightStat = StatList.objectUseStats[rightId];
		}

		if (leftStat != null || rightStat != null) {
			if (leftStat == null) return 1;
			if (rightStat == null) return -1;

			int leftValue = GuiStats.getStatsFileWriter(this.slotStatsItemGUI.parentGuiStats).writeStat(leftStat);
			int rightValue = GuiStats.getStatsFileWriter(this.slotStatsItemGUI.parentGuiStats).writeStat(rightStat);
			if (leftValue != rightValue) {
				return (leftValue - rightValue) * this.slotStatsItemGUI.sectionHighlightState;
			}
		}
		return leftId - rightId;
	}

	public int compare(Object a, Object b) {
		return this.sortByStat((StatCrafting) a, (StatCrafting) b);
	}
}
