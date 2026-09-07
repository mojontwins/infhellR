package net.minecraft.game.entity;

import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.animal.EntityAnimal;

public enum EnumCreatureType {
	monster(IMob.class, 150, Material.air, false),
	creature(EntityAnimal.class, 30, Material.air, true),
	waterCreature(IWaterMob.class, 5, Material.water, true);

	private final Class<?> creatureClass;
	private final int maxNumberOfCreature;
	private final Material creatureMaterial;
	private final boolean isPeacefulCreature;

	private EnumCreatureType(Class<?> class3, int i4, Material material5, boolean z6) {
		this.creatureClass = class3;
		this.maxNumberOfCreature = i4;
		this.creatureMaterial = material5;
		this.isPeacefulCreature = z6;
	}

	public Class<?> getCreatureClass() {
		return this.creatureClass;
	}

	public int getMaxNumberOfCreature() {
		return this.maxNumberOfCreature;
	}

	public Material getCreatureMaterial() {
		return this.creatureMaterial;
	}

	public boolean getPeacefulCreature() {
		return this.isPeacefulCreature;
	}
}
