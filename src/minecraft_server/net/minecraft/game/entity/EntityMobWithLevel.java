package net.minecraft.game.entity;

import net.minecraft.game.world.World;

/**
 * A humanoid mob whose strength (and equipment) scales with a level value,
 * synced to clients through the data watcher so it survives SMP.
 */
public class EntityMobWithLevel extends EntityHumanoid implements IMobWithLevel {
	public EntityMobWithLevel(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		// Level, stored in the data watcher so it survives SMP.
		this.dataWatcher.addObject(Datawatchers.DW_LEVEL, (byte)0);
	}

	public EntityMobWithLevel(World world, int level) {
		super(world);
		this.setLvl(level);
	}

	public int getLvl() {
		return (int)this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_LEVEL);
	}

	public void setLvl(int level) {
		this.dataWatcher.updateObject(Datawatchers.DW_LEVEL, (byte)level);
	}

	public int getLevel() {
		return this.getLvl();
	}

	@Override
	public void setLevel(int level) {
		this.setLvl(level);
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
