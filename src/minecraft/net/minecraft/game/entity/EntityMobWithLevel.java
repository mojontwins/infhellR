package net.minecraft.game.entity;

import net.minecraft.game.world.World;

public class EntityMobWithLevel extends EntityMob implements IMobWithLevel {
	public EntityMobWithLevel(World world) {
		super(world);
	}
	
	protected void entityInit() {
		super.entityInit();
		
		// Store "lvl" in DWO #16
		this.dataWatcher.addObject(Datawatchers.DW_LEVEL, new Byte((byte) 0));
	}
	
	public int getLvl() {
		return (int)this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_LEVEL);
	}
	
	public void setLvl(int lvl) {
		this.dataWatcher.updateObject(Datawatchers.DW_LEVEL, (byte)lvl);
	}
	
	public EntityMobWithLevel(World world, int level) {
		super(world);
		this.setLvl(level);
	}

	public int getLevel() {
		return this.getLvl();
	}

	public void setLevel(int level) {
		this.setLvl(level);
	}

	public int getMaxLevel() {
		return 5;
	}
}
