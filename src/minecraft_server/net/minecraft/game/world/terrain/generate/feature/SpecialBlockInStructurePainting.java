package net.minecraft.game.world.terrain.generate.feature;

/**
 * A painting planned by a structure's constructor, spawned during populate.
 * No block placement involved; onValidSurface arithmetic is done at plan time
 * (EntityPainting's constructor reads no world state).
 */
public class SpecialBlockInStructurePainting extends SpecialBlockInStructure {
	/** Facing: 0=north, 1=east, 2=south, 3=west (EntityPainting dirInt). */
	public final int direction;
	/** EnumArt title, e.g. "Maze". */
	public final String artTitle;

	public SpecialBlockInStructurePainting(int x, int y, int z, int direction, String artTitle) {
		// blockID 0: no block to place, only the entity to spawn
		super(x, y, z, 0, 0);
		this.direction = direction;
		this.artTitle = artTitle;
	}
}
