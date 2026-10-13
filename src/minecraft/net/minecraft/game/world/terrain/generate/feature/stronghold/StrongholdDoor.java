package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.Random;

/**
 * The four kinds of doorway a stronghold room can open with. Kept as a tiny enum so a room
 * can remember which door it rolled without carrying the raw ordinal around.
 */
public enum StrongholdDoor {
	OPENING,
	WOOD_DOOR,
	GRATES,
	IRON_DOOR;

	/** Rolls a door kind the way the original 0..4 split does (open is twice as likely). */
	public static StrongholdDoor random(Random rand) {
		switch(rand.nextInt(5)) {
		case 2:
			return WOOD_DOOR;
		case 3:
			return GRATES;
		case 4:
			return IRON_DOOR;
		default:
			return OPENING;
		}
	}
}
