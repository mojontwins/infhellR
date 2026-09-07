package net.minecraft.game.achievements;

public class StatCrafting extends StatBase {
	private final int itemId;

	public StatCrafting(int i1, String string2, int i3) {
		super(i1, string2);
		this.itemId = i3;
	}

	public int getItemId() {
		return this.itemId;
	}
}
