package net.minecraft.game.world.block;

public class StepSoundSlime extends StepSound {
	public StepSoundSlime(String string1, float f2, float f3) {
		super(string1, f2, f3);
	}
	
	public String getBreakSound() {
		return "mob.slime";
	}

	public String getStepSound() {
		return "mob.slime";
	}	
}
