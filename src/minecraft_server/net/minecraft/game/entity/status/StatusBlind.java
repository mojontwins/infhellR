package net.minecraft.game.entity.status;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
public class StatusBlind extends Status {

	public StatusBlind(int id, boolean isBadEffect) {
		super(id, isBadEffect);
		this.particleColor = 0x222222;
	}
	
	@Override
	public void performEffect (EntityLiving entityLiving, int amplifier, int duration) {
		// Decrease half a heart
		// Make slower somehow
		entityLiving.isBlinded = true;
	}
	
	@Override
	public void onCompleted(EntityLiving entityLiving, int amplifier) {
		entityLiving.isBlinded = false;
	}

	@Override
	public boolean isApplicableTo (EntityLiving entityLiving) {
		return (entityLiving instanceof EntityPlayer);
	}
}
