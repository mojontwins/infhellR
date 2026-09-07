package net.minecraft.server;

import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.chunk.loader.ISaveHandler;

/**
 * Extension of WorldServer used for dimension-specific worlds
 * in multiplayer. Shares map storage with the overworld to ensure
 * that map data (e.g., discovered map items) is consistent across
 * all dimensions.
 *
 * Most of its initialization is delegated to WorldServer via super(),
 * with the only difference being that mapStorage is inherited from
 * the overworld's WorldServer.
 */
public class WorldServerMulti extends WorldServer {

	/**
	 * Constructs a new dimension-specific world.
	 *
	 * @param minecraftServer1  the owning MinecraftServer
	 * @param iSaveHandler2     the save handler
	 * @param string3           world name
	 * @param i4                dimension ID (-1 for Nether, 0 for Overworld, 1 for End)
	 * @param worldSettings5    world settings
	 * @param worldServer7      overworld server (provides shared mapStorage)
	 */
	public WorldServerMulti(MinecraftServer minecraftServer1, ISaveHandler iSaveHandler2, String string3, int i4,
			WorldSettings worldSettings5, WorldServer worldServer7) {
		super(minecraftServer1, iSaveHandler2, string3, i4, worldSettings5);
		this.mapStorage = worldServer7.mapStorage;
	}
}