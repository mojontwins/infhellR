package net.minecraft.game.world.block;

public class StepSound {
	public final String soundString;
	public final float volume;
	public final float pitch;

	public StepSound(String string1, float f2, float f3) {
		this.soundString = string1;
		this.volume = f2;
		this.pitch = f3;
	}

	public float getVolume() {
		return this.volume;
	}

	public float getPitch() {
		return this.pitch;
	}

	public String getBreakSound() {
		return "step." + this.soundString;
	}

	public String getStepSound() {
		return "step." + this.soundString;
	}
}
