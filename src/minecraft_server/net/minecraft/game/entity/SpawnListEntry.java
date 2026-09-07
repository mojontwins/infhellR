package net.minecraft.game.entity;

public class SpawnListEntry {
	public Class<?> entityClass;
	public int spawnRarityRate;
	public boolean isUrban;
	
	public SpawnListEntry(Class<?> class1, int i2) {
		this(class1, i2, false);
	}

	public SpawnListEntry(Class<?> class1, int i2, boolean b) {
		this.entityClass = class1;
		this.spawnRarityRate = i2;
		this.isUrban = b;
	}
}
