package net.minecraft.game.world.block;

public class ColorizerWater {
	private static int[] waterBuffer = new int[65536];

	public static void setColorRamp(int[] i0) {
		setWaterBuffer(i0);
	}

	public static int[] getWaterBuffer() {
		return waterBuffer;
	}

	public static void setWaterBuffer(int[] waterBuffer) {
		ColorizerWater.waterBuffer = waterBuffer;
	}
}
