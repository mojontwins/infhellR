package net.minecraft.game.world.terrain.generate.feature.stronghold;

/**
 * The kind of doorway a stronghold piece opens onto its neighbour: an open arch, a wooden
 * door, iron grates or a locked iron door. Port of the vanilla {@code EnumDoor}.
 */
public enum StrongholdDoor {
	OPENING,
	WOOD_DOOR,
	GRATES,
	IRON_DOOR;
}
