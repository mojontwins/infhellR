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
 * public {@code scheduleBlockUpdate}/{@code TickUpdates}/{@code shiftScheduledTimes} entry points as
 * thin delegates, so external callers — including {@code WorldClient}'s client-side overrides, which are
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
		NextTickListEntry entry = new NextTickListEntry(x, y, z, blockID);
		byte updateRadius = 8;
		if(this.world.scheduledUpdatesAreImmediate) {
			// Immediate mode (debug/world-edit): fire the tick right away instead of queueing it.
			if(this.world.checkChunksExist(entry.xCoord - updateRadius, entry.yCoord - updateRadius, entry.zCoord - updateRadius, entry.xCoord + updateRadius, entry.yCoord + updateRadius, entry.zCoord + updateRadius)) {
				int existingBlockId = this.world.getBlockId(entry.xCoord, entry.yCoord, entry.zCoord);
				if(existingBlockId == entry.blockID && existingBlockId > 0) {
					Block.blocksList[existingBlockId].updateTick(this.world, entry.xCoord, entry.yCoord, entry.zCoord, this.world.rand);
				}
			}

		} else {
			// Normal mode: enqueue the tick, due tickRate ticks from the current world time.
			if(this.world.checkChunksExist(x - updateRadius, y - updateRadius, z - updateRadius, x + updateRadius, y + updateRadius, z + updateRadius)) {
				if(blockID > 0) {
					entry.setScheduledTime((long)tickRate + this.world.worldInfo.getWorldTime());
				}

				if(!this.scheduledTickSet.contains(entry)) {
					this.scheduledTickSet.add(entry);
					this.scheduledTickTreeSet.add(entry);
				}
			}

		}
	}

	/**
	 * Fires up to 1000 due block ticks per call. When {@code processAll} is false, stops at the first
	 * entry whose scheduled time has not been reached yet. Returns whether more ticks remain queued.
	 */
	boolean tickUpdates(boolean processAll) {
		int dueTickCount = this.scheduledTickTreeSet.size();
		if(dueTickCount != this.scheduledTickSet.size()) {
			throw new IllegalStateException("TickNextTick list out of synch");
		} else {
			if(dueTickCount > 1000) {
				// Never fire more than a fixed batch of ticks per frame.
				dueTickCount = 1000;
			}

			for(int i = 0; i < dueTickCount; ++i) {
				NextTickListEntry entry = (NextTickListEntry)this.scheduledTickTreeSet.first();
				if(!processAll && entry.scheduledTime > this.world.worldInfo.getWorldTime()) {
					// Time-ordered: stop at the first tick that has not come due yet.
					break;
				}

				this.scheduledTickTreeSet.remove(entry);
				this.scheduledTickSet.remove(entry);
				byte updateRadius = 8;
				if(this.world.checkChunksExist(entry.xCoord - updateRadius, entry.yCoord - updateRadius, entry.zCoord - updateRadius, entry.xCoord + updateRadius, entry.yCoord + updateRadius, entry.zCoord + updateRadius)) {
					int existingBlockId = this.world.getBlockId(entry.xCoord, entry.yCoord, entry.zCoord);
					if(existingBlockId == entry.blockID && existingBlockId > 0) {
						Block.blocksList[existingBlockId].updateTick(this.world, entry.xCoord, entry.yCoord, entry.zCoord, this.world.rand);
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

		NextTickListEntry entry;
		for(Iterator<NextTickListEntry> iterator = this.scheduledTickSet.iterator(); iterator.hasNext(); entry.scheduledTime += delta) {
			entry = (NextTickListEntry)iterator.next();
		}

		this.world.setWorldTime(newWorldTime);
	}
}