package net.minecraft.game.achievements;

import net.minecraft.game.IStatType;

final class StatTypeDistance implements IStatType {
	public String format(int i1) {
		double d3 = (double)i1 / 100.0D;
		double d5 = d3 / 1000.0D;
		return d5 > 0.5D ? StatBase.getDecimalFormat().format(d5) + " km" : (d3 > 0.5D ? StatBase.getDecimalFormat().format(d3) + " m" : i1 + " cm");
	}
}
