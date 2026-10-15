package net.minecraft.game.entity;

/**
 * Marker for entities that defenders such as the amazons treat as hostile to
 * players and will attack on sight.
 *
 * <p>This is a pure marker: only mobs that genuinely hunt the player implement
 * it. Neutral monsters (for example traders and pig zombies, which only fight
 * back once provoked) simply do not implement it, which is why they are kept
 * out of the {@code EntityZombie} hierarchy.</p>
 */
public interface IHostileToPlayer {
}
