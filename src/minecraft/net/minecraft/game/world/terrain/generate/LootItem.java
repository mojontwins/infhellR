package net.minecraft.game.world.terrain.generate;

public class LootItem {
	public int id;
	public int maxQuantity;
	public boolean isRare;
	public int chance;
	
	public LootItem (int id, int maxQuantity, boolean isRare, int chance) {
		this.id = id;
		this.maxQuantity = maxQuantity;
		this.isRare = isRare;
		this.chance = chance;
	}
}
