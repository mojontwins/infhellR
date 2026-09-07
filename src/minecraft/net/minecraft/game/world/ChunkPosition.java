package net.minecraft.game.world;

/**
 * Represents a block-coordinate triple within a chunk.
 *
 * <p>Used as the key type in a variety of world-coordinate collections
 * (e.g. lists of burning blocks, pending block updates, entity positions).</p>
 */
public class ChunkPosition {
    public final int x;
    public final int y;
    public final int z;

    public ChunkPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkPosition)) {
            return false;
        }
        ChunkPosition other = (ChunkPosition) obj;
        return other.x == this.x && other.y == this.y && other.z == this.z;
    }

    @Override
    public int hashCode() {
        return this.x * 8976890 + this.y * 981131 + this.z;
    }
}
