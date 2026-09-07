package net.minecraft.game.entity.status;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;

public class StatusEffect {
	public int statusID; 	// Status ID of this effect.
	public int duration; 	// Time before the effect fades out.
	public int amplifier; 	// An effect amplifier. 
	
	public StatusEffect(int statusID, int duration, int amplifier) {
		this.statusID = statusID;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public StatusEffect(StatusEffect statusEffect) {
		this.statusID = statusEffect.statusID;
		this.duration = statusEffect.duration;
		this.amplifier = statusEffect.amplifier;
	}
	
	public void writeStatusEffectToNBT(NBTTagCompound compoundTag) {
		compoundTag.setInteger("StatusID", this.statusID);
		compoundTag.setInteger("Duration", this.duration);
		compoundTag.setInteger("Amplifier", this.amplifier);
	}
	
	public StatusEffect(NBTTagCompound compoundTag) {
		this.statusID = compoundTag.getInteger("StatusID");
		this.duration = compoundTag.getInteger("Duration");
		this.amplifier = compoundTag.getInteger("Amplifier");
	}
	
	/*
	 * This method is to be called from EntityLiving's `onEntityUpdate`
	 * returns false when the effect has finished.
	 */
	public boolean onUpdate (EntityLiving entityLiving) {
		Status status = Status.statusTypes [statusID];
		if (duration > 0) {
			if (status.isReady(duration, amplifier)) {
				status.performEffect(entityLiving, amplifier, duration);
				if(status.isOneShot()) return false;
			}
			duration --;
			return true;
		} else {
			status.onCompleted(entityLiving, amplifier);
			return false;
		}
	}
	
	public void combine (StatusEffect statusEffect) {
		// Update this with statusEffect's values.
		this.duration += statusEffect.duration;
		if (this.amplifier < statusEffect.amplifier) this.amplifier = statusEffect.amplifier;
	}
	
	public boolean equals (Object obj) {
		if (!(obj instanceof StatusEffect)) return false;
		
		StatusEffect statusEffect = (StatusEffect) obj;
		return statusEffect.statusID == this.statusID && statusEffect.duration == this.duration && statusEffect.amplifier == this.amplifier;
	}
}
