package net.minecraft.game.entity.status;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;

public class StatusInstantDamage extends Status {

	public StatusInstantDamage(int id, boolean isBadEffect) {
		super(id, isBadEffect);
		this.particleColor = 0xFF0000;
	}
	
	@Override
	public void performEffect (EntityLiving entityLiving, int amplifier, int duration) {
		entityLiving.attackEntityFrom((Entity)null, 6);
	}
	
	@Override
	public boolean isReady (int tick, int amplifier) {
		return true;
	}
	
	@Override
	public boolean isOneShot() {
		return true;
	}
}
