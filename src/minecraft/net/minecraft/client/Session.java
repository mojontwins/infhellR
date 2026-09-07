package net.minecraft.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.game.world.block.Block;

/**
 * Represents an authenticated player session (local or online).
 *
 * <p>Stores the username, session token, and server password parameter needed for
 * multiplayer connections. The static {@link #registeredBlocksList} is used by the
 * multiplayer protocol to track which block types are known to the server.</p>
 */
public class Session {
    /** Blocks registered with the server on the current session. */
    public static List<Block> registeredBlocksList = new ArrayList<Block>();
    /** Player username. */
    public String username;
    /** Session token used for online multiplayer authentication. */
    public String sessionId;
    /** Optional server password parameter for authentication. */
    public String mpPassParameter;

    public Session(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    static {
        registeredBlocksList.add(Block.stone);
        registeredBlocksList.add(Block.cobblestone);
        registeredBlocksList.add(Block.brick);
        registeredBlocksList.add(Block.dirt);
        registeredBlocksList.add(Block.planks);
        registeredBlocksList.add(Block.wood);
        registeredBlocksList.add(Block.leaves);
        registeredBlocksList.add(Block.torchWood);
        registeredBlocksList.add(Block.stairSingle);
        registeredBlocksList.add(Block.glass);
        registeredBlocksList.add(Block.cobblestoneMossy);
        registeredBlocksList.add(Block.sapling);
        registeredBlocksList.add(Block.plantYellow);
        registeredBlocksList.add(Block.plantRed);
        registeredBlocksList.add(Block.mushroomBrown);
        registeredBlocksList.add(Block.mushroomRed);
        registeredBlocksList.add(Block.sand);
        registeredBlocksList.add(Block.gravel);
        registeredBlocksList.add(Block.sponge);
        registeredBlocksList.add(Block.cloth);
        registeredBlocksList.add(Block.oreCoal);
        registeredBlocksList.add(Block.oreIron);
        registeredBlocksList.add(Block.oreGold);
        registeredBlocksList.add(Block.blockSteel);
        registeredBlocksList.add(Block.blockGold);
        registeredBlocksList.add(Block.bookShelf);
        registeredBlocksList.add(Block.tnt);
        registeredBlocksList.add(Block.obsidian);
    }
}
