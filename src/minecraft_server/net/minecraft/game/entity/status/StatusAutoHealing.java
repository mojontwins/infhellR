package net.minecraft.game.entity.status;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.monster.EntityZombie;

public class StatusAutoHealing extends Status {
	public StatusAutoHealing(int id, boolean isBadEffect) {
		super(id, isBadEffect);
		this.particleColor = 0xD24343;
	}
	
	@Override
	public void performEffect(EntityLiving entityLiving, int amplifier, int duration) {
		// Increase half a heart - decrease for zombies!
		if(entityLiving instanceof EntityZombie) {
			if(entityLiving.health > 1) {
				entityLiving.attackEntityFrom((Entity)null, 1);
			}
		} else if(entityLiving.health < entityLiving.getFullHealth()) {
			entityLiving.heal(amplifier);
		}
	}
	
	@Override
	public boolean isReady(int tick, int amplifier) {
		// Run every 10 ticks
		return (tick % 10) == 0;
	}

	@Override
	public boolean isApplicableTo(EntityLiving entityLiving) {
		// Zombies can't be poisoned
		return !(entityLiving instanceof EntityZombie);
	}
}
