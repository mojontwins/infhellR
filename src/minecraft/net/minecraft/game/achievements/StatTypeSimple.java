package net.minecraft.game.achievements;

import net.minecraft.game.IStatType;

final class StatTypeSimple implements IStatType {
	public String format(int i1) {
		return StatBase.getNumberFormat().format((long)i1);
	}
}
