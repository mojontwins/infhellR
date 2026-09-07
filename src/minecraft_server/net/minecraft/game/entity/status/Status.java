package net.minecraft.game.entity.status;

import net.minecraft.game.entity.EntityLiving;

public class Status {
	public String name;
	public static final Status statusTypes [] = new Status [256];
	public static int latestId = 1;
	public boolean isBadEffect;
	public int id;
	public int particleColor = 0xFFFFFF;
	public boolean showParticles = true;
	
	// Default status
	public static Status statusPoisoned = new StatusPoisoned(getNewStatusId(), true);
	public static Status statusSlowness = new StatusSlowness(getNewStatusId(), true);
	public static Status statusAutoHealing = new StatusAutoHealing(getNewStatusId(), false);
	public static Status statusInstantDamage = new StatusInstantDamage(getNewStatusId(), true);
	public static Status statusDizzy = new StatusDizzy(getNewStatusId(), true);
	public static Status statusBlind = new StatusBlind(getNewStatusId(), true);
	public static Status statusJumpy = new StatusJumpy(getNewStatusId(), true);
	
	/*
	 * Class constructor needs an id and a boolean isBadEffect.
	 */
	public Status(int id, boolean isBadEffect) {
		this.id = id;
		this.isBadEffect = isBadEffect;
		statusTypes [id] = this;
	}
	
	/*
	 * Handy sequencer you can use to get free Status IDs.
	 */
	public static int getNewStatusId () {
		return (latestId < 256) ? latestId ++ : -1;		
	}

	/*
	 * Give it a proper name for the registry
	 */
	public Status setName(String name) {
		this.name = name;
		return this;
	}
	
	/*
	 * Override this method with your own to perform the effect
	 */
	public void performEffect(EntityLiving entityLiving, int amplifier, int duration) {    	
	}
	
	/*
	 *  Override this method with your own to decide if the effect is ready
	 */
	public boolean isReady(int tick, int amplifier) {    	
		return true;
	}
	
	/*
	 *  Override this method with your own to decide if the effect is applicable to this entity
	 */
	public boolean isApplicableTo(EntityLiving entityLiving) {
		return true;
	}
	
	/*
	 * Do this before turning the status effect off
	 */
	public void onCompleted(EntityLiving entityLiving, int amplifier) {
	}
	
	/*
	 * Return true if the effect should run just once no matter what
	 */
	public boolean isOneShot() {
		return false;
	}
}
