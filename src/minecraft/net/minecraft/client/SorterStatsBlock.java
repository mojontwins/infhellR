package net.minecraft.client;

import java.util.Comparator;
import net.minecraft.client.gui.GuiSlotStatsBlock;
import net.minecraft.client.gui.GuiStats;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatCrafting;
import net.minecraft.game.achievements.StatList;

public class SorterStatsBlock implements Comparator<Object> {
	final GuiStats statsGUI;
	final GuiSlotStatsBlock slotStatsBlockGUI;

	public SorterStatsBlock(GuiSlotStatsBlock slotGui, GuiStats statsGui) {
		this.slotStatsBlockGUI = slotGui;
		this.statsGUI = statsGui;
	}

	public int sortByStat(StatCrafting left, StatCrafting right) {
		int leftId = left.getItemId();
		int rightId = right.getItemId();
		StatBase leftStat = null;
		StatBase rightStat = null;
		if (this.slotStatsBlockGUI.sectionId == 2) {
			leftStat = StatList.mineBlockStatArray[leftId];
			rightStat = StatList.mineBlockStatArray[rightId];
		} else if (this.slotStatsBlockGUI.sectionId == 0) {
			leftStat = StatList.objectCraftStats[leftId];
			rightStat = StatList.objectCraftStats[rightId];
		} else if (this.slotStatsBlockGUI.sectionId == 1) {
			leftStat = StatList.objectUseStats[leftId];
			rightStat = StatList.objectUseStats[rightId];
		}

		if (leftStat != null || rightStat != null) {
			if (leftStat == null) return 1;
			if (rightStat == null) return -1;

			int leftValue = GuiStats.getStatsFileWriter(this.slotStatsBlockGUI.guiStats).writeStat(leftStat);
			int rightValue = GuiStats.getStatsFileWriter(this.slotStatsBlockGUI.guiStats).writeStat(rightStat);
			if (leftValue != rightValue) {
				return (leftValue - rightValue) * this.slotStatsBlockGUI.sectionHighlightState;
			}
		}
		return leftId - rightId;
	}

	public int compare(Object a, Object b) {
		return this.sortByStat((StatCrafting) a, (StatCrafting) b);
	}
}
