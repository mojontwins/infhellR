package net.minecraft.game.entity;

import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

/**
 * Shared base for every human-like mob.
 *
 * <p>A humanoid wields an item in its right hand and plays a short melee swing
 * animation with it. Both the inventory-backed {@link EntityArmoredMob} family
 * and the levelled humanoid family descend from this class, so the swing state
 * and the held-item accessors live here instead of being copied into every mob
 * that happens to look human.</p>
 */
public class EntityHumanoid extends EntityMob {
	/** Number of ticks a full swing lasts. */
	protected static final int SWING_TICKS = 8;

	/** Whether the held item is currently mid-swing. */
	public boolean isSwinging = false;

	/** Tick counter of the current swing (0..{@link #SWING_TICKS}). */
	public int swingProgressInt = 0;

	/** Item held in the right hand, drawn by the renderer. */
	protected ItemStack heldItem;

	public EntityHumanoid(World world) {
		super(world);
	}

	/** Begins a swing from its first frame. */
	public void swingItem() {
		this.swingProgressInt = -1;
		this.isSwinging = true;
	}

	/**
	 * Advances the current swing by one tick and publishes its progress to
	 * {@code swingProgress}, which the renderer reads. Mobs that implement
	 * their own action state should call this once per tick.
	 */
	protected void updateSwingAnimation() {
		if(this.isSwinging) {
			++this.swingProgressInt;
			if(this.swingProgressInt >= SWING_TICKS) {
				this.swingProgressInt = 0;
				this.isSwinging = false;
			}
		} else {
			this.swingProgressInt = 0;
		}

		this.swingProgress = (float)this.swingProgressInt / (float)SWING_TICKS;
	}

	@Override
	public ItemStack getHeldItem() {
		return this.heldItem;
	}

	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.heldItem = itemStack;
		return true;
	}
}
