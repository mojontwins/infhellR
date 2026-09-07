package net.minecraft.client.render.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.game.physics.Vec3i;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class TextureRecoveryCompassFX extends TextureCompassFX {

	public TextureRecoveryCompassFX(Minecraft minecraft, int iconIndex) {
		super(minecraft, iconIndex);
	}

	protected Vec3i getColor2() {
		return new Vec3i(20, 100, 100);
	}

	protected Vec3i getColor1() {
		return new Vec3i(20, 255, 255);
	}

	protected ChunkCoordinates pointCompassAt() {
		return this.mc.thePlayer.getPlayerLastDeathCoordinate();
	}
}
