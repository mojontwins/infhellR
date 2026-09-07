package net.minecraft.client;

public class MouseFilter {
	private float accumulatedDelta;
	private float lastSmoothed;
	private float rejectedMovement;

	public float smooth(float value, float smoothingFactor) {
		this.accumulatedDelta += value;
		value = (this.accumulatedDelta - this.lastSmoothed) * smoothingFactor;
		this.rejectedMovement += (value - this.rejectedMovement) * 0.5F;
		if (value > 0.0F && value > this.rejectedMovement || value < 0.0F && value < this.rejectedMovement) {
			value = this.rejectedMovement;
		}
		this.lastSmoothed += value;
		return value;
	}
}
