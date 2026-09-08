package net.minecraft.game.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.game.world.block.Block;

/**
 * Owns the world's scheduled per-block tick queue: a time-ordered set of {@link NextTickListEntry}
 * entries plus a lookup mirror for O(1) duplicate detection, together with the operations that
 * schedule, fire and time-shift block ticks.
 *
 * <p>Entries are stored in {@link #scheduledTickTreeSet} (ordered by scheduled time, then insertion
 * order) and mirrored in {@link #scheduledTickSet}; the two must stay in sync, verified at the top
 * of {@link #tickUpdates}. Extracted from {@link World} in the refactor; {@code World} keeps its
 * public {@code scheduleBlockUpdate}/{@code TickUpdates}/{@code s_func_32005_b} entry points as thin
 * delegates, so external callers — including {@code WorldClient}'s client-side overrides, which are
 * no-ops because block ticks arrive from the server as packets — are unchanged.
 */
public final class BlockTickScheduler {

	/** The owning world, for the world clock, chunk-existence checks and the per-block {@code updateTick} call. */
	private final World world;

	/** Pending block ticks, ordered by scheduled time then insertion order. */
	private final TreeSet<NextTickListEntry> scheduledTickTreeSet = new TreeSet<NextTickListEntry>();

	/** Mirror of {@link #scheduledTickTreeSet} used for O(1) duplicate detection. */
	private final Set<NextTickListEntry> scheduledTickSet = new HashSet<NextTickListEntry>();

	/** Constructs a block tick scheduler bound to the given world. */
	BlockTickScheduler(World world) {
		this.world = world;
	}

	/**
	 * Queues a block tick for the given position and block ID, due {@code tickRate} ticks from now.
	 * When {@link World#scheduledUpdatesAreImmediate} is set the tick is fired instantly instead.
	 */
	void scheduleBlockUpdate(int x, int y, int z, int blockID, int tickRate) {
		NextTickListEntry nextTickListEntry6 = new NextTickListEntry(x, y, z, blockID);
		byte b7 = 8;
		if(this.world.scheduledUpdatesAreImmediate) {
			if(this.world.checkChunksExist(nextTickListEntry6.xCoord - b7, nextTickListEntry6.yCoord - b7, nextTickListEntry6.zCoord - b7, nextTickListEntry6.xCoord + b7, nextTickListEntry6.yCoord + b7, nextTickListEntry6.zCoord + b7)) {
				int i8 = this.world.getBlockId(nextTickListEntry6.xCoord, nextTickListEntry6.yCoord, nextTickListEntry6.zCoord);
				if(i8 == nextTickListEntry6.blockID && i8 > 0) {
					Block.blocksList[i8].updateTick(this.world, nextTickListEntry6.xCoord, nextTickListEntry6.yCoord, nextTickListEntry6.zCoord, this.world.rand);
				}
			}

		} else {
			if(this.world.checkChunksExist(x - b7, y - b7, z - b7, x + b7, y + b7, z + b7)) {
				if(blockID > 0) {
					nextTickListEntry6.setScheduledTime((long)tickRate + this.world.worldInfo.getWorldTime());
				}

				if(!this.scheduledTickSet.contains(nextTickListEntry6)) {
					this.scheduledTickSet.add(nextTickListEntry6);
					this.scheduledTickTreeSet.add(nextTickListEntry6);
				}
			}

		}
	}

	/**
	 * Fires up to 1000 due block ticks per call. When {@code processAll} is false, stops at the first
	 * entry whose scheduled time has not been reached yet. Returns whether more ticks remain queued.
	 */
	boolean tickUpdates(boolean processAll) {
		int i2 = this.scheduledTickTreeSet.size();
		if(i2 != this.scheduledTickSet.size()) {
			throw new IllegalStateException("TickNextTick list out of synch");
		} else {
			if(i2 > 1000) {
				i2 = 1000;
			}

			for(int i3 = 0; i3 < i2; ++i3) {
				NextTickListEntry nextTickListEntry4 = (NextTickListEntry)this.scheduledTickTreeSet.first();
				if(!processAll && nextTickListEntry4.scheduledTime > this.world.worldInfo.getWorldTime()) {
					break;
				}

				this.scheduledTickTreeSet.remove(nextTickListEntry4);
				this.scheduledTickSet.remove(nextTickListEntry4);
				byte b5 = 8;
				if(this.world.checkChunksExist(nextTickListEntry4.xCoord - b5, nextTickListEntry4.yCoord - b5, nextTickListEntry4.zCoord - b5, nextTickListEntry4.xCoord + b5, nextTickListEntry4.yCoord + b5, nextTickListEntry4.zCoord + b5)) {
					int i6 = this.world.getBlockId(nextTickListEntry4.xCoord, nextTickListEntry4.yCoord, nextTickListEntry4.zCoord);
					if(i6 == nextTickListEntry4.blockID && i6 > 0) {
						Block.blocksList[i6].updateTick(this.world, nextTickListEntry4.xCoord, nextTickListEntry4.yCoord, nextTickListEntry4.zCoord, this.world.rand);
					}
				}
			}

			return this.scheduledTickTreeSet.size() != 0;
		}
	}

	/**
	 * Shifts every pending scheduled time by the world-clock delta so queued ticks keep their offset
	 * when the world time is set externally (e.g. from the console), then applies the new time.
	 */
	void shiftScheduledTimes(long newWorldTime) {
		long delta = newWorldTime - this.world.worldInfo.getWorldTime();

		NextTickListEntry nextTickListEntry6;
		for(Iterator<NextTickListEntry> iterator5 = this.scheduledTickSet.iterator(); iterator5.hasNext(); nextTickListEntry6.scheduledTime += delta) {
			nextTickListEntry6 = (NextTickListEntry)iterator5.next();
		}

		this.world.setWorldTime(newWorldTime);
	}
}