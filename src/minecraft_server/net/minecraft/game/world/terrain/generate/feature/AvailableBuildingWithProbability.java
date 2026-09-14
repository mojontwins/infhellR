package net.minecraft.game.world.terrain.generate.feature;

public class AvailableBuildingWithProbability {
	public Class<?> buildingClass;
	public int probability;
	
	public AvailableBuildingWithProbability(Class<?> buildingClass, int probability) {
		this.buildingClass = buildingClass;
		this.probability = probability;
	}
}
