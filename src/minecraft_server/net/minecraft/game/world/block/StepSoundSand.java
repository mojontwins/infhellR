package net.minecraft.game.world.block;

final class StepSoundSand extends StepSound {
	StepSoundSand(String string1, float f2, float f3) {
		super(string1, f2, f3);
	}

	public String getBreakSound() {
		return "step.gravel";
	}
}
