package net.minecraft.game.achievements;

import net.minecraft.game.IStatStringFormat;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class Achievement extends StatBase {
	public final int displayColumn;
	public final int displayRow;
	public final Achievement parentAchievement;
	private final String achievementDescription;
	private IStatStringFormat statStringFormatter;
	public final ItemStack theItemStack;
	private boolean isSpecial;

	public Achievement(int i1, String string2, int i3, int i4, Item item5, Achievement achievement6) {
		this(i1, string2, i3, i4, new ItemStack(item5), achievement6);
	}

	public Achievement(int i1, String string2, int i3, int i4, Block block5, Achievement achievement6) {
		this(i1, string2, i3, i4, new ItemStack(block5), achievement6);
	}

	public Achievement(int id, String achievementName, int column, int row, ItemStack itemStack5, Achievement parentAchievement) {
		super(5242880 + id, StatCollector.translateToLocal("achievement." + achievementName));
		this.theItemStack = itemStack5;
		this.achievementDescription = StatCollector.translateToLocal("achievement." + achievementName + ".desc");
		this.displayColumn = column;
		this.displayRow = row;
		if(column < AchievementList.minDisplayColumn) {
			AchievementList.minDisplayColumn = column;
		}

		if(row < AchievementList.minDisplayRow) {
			AchievementList.minDisplayRow = row;
		}

		if(column > AchievementList.maxDisplayColumn) {
			AchievementList.maxDisplayColumn = column;
		}

		if(row > AchievementList.maxDisplayRow) {
			AchievementList.maxDisplayRow = row;
		}

		this.parentAchievement = parentAchievement;
	}

	public Achievement setIndependent() {
		this.isIndependent = true;
		return this;
	}

	public Achievement setSpecial() {
		this.isSpecial = true;
		return this;
	}

	public Achievement registerAchievement() {
		super.registerStat();
		AchievementList.achievementList.add(this);
		return this;
	}

	public boolean isAchievement() {
		return true;
	}

	public String getDescription() {
		return this.statStringFormatter != null ? this.statStringFormatter.formatString(this.achievementDescription) : this.achievementDescription;
	}

	public Achievement setStatStringFormatter(IStatStringFormat iStatStringFormat1) {
		this.statStringFormatter = iStatStringFormat1;
		return this;
	}

	public boolean getSpecial() {
		return this.isSpecial;
	}

	public StatBase registerStat() {
		return this.registerAchievement();
	}

	public StatBase initIndependentStat() {
		return this.setIndependent();
	}
}
