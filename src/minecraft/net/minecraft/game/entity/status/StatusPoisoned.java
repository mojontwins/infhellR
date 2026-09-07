package net.minecraft.game.entity.status;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.monster.EntityZombie;

public class StatusPoisoned extends Status {
	public StatusPoisoned(int id, boolean isBadEffect) {
		super(id, true);
		this.particleColor = 0x70B433;
	}
	
	@Override
	public void performEffect (EntityLiving entityLiving, int amplifier, int duration) {
		// Decrease half a heart
		if (entityLiving.health > 1) {
			entityLiving.attackEntityFrom((Entity)null, 1);
		}
	}
	
	@Override
	public boolean isReady (int tick, int amplifier) {
		// Run every 5 ticks
		return (tick % 5) == 0;
	}

	@Override
	public boolean isApplicableTo (EntityLiving entityLiving) {
		// Zombies can't be poisoned
		return !(entityLiving instanceof EntityZombie);
	}
}
