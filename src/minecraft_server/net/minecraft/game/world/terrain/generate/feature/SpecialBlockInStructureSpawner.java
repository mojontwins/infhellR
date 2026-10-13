package net.minecraft.game.world.terrain.generate.feature;

/**
 * A mob spawner planned by a structure's constructor, flushed during populate.
 * mobID is assigned to the TileEntityMobSpawner after placement.
 */
public class SpecialBlockInStructureSpawner extends SpecialBlockInStructure {
	/** Entity class name for TileEntityMobSpawner.mobID, e.g. "Zombie". */
	public final String mobID;

	public SpecialBlockInStructureSpawner(int x, int y, int z, int blockID, int metadata, String mobID) {
		super(x, y, z, blockID, metadata);
		this.mobID = mobID;
	}
}
