package net.minecraft.client.player;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.client.GameSettingsKeys;

public class MovementInputFromOptions extends MovementInput {
	private boolean[] movementKeyStates = new boolean[10];

	public MovementInputFromOptions() {
	}

	public void checkKeyForMovementInput(int i1, boolean z2) {
		byte b3 = -1;
		if(i1 == GameSettingsKeys.keyBindForward.keyCode) {
			b3 = 0;
		}

		if(i1 == GameSettingsKeys.keyBindBack.keyCode) {
			b3 = 1;
		}

		if(i1 == GameSettingsKeys.keyBindLeft.keyCode) {
			b3 = 2;
		}

		if(i1 == GameSettingsKeys.keyBindRight.keyCode) {
			b3 = 3;
		}

		if(i1 == GameSettingsKeys.keyBindJump.keyCode) {
			b3 = 4;
		}

		if(i1 == GameSettingsKeys.keyBindSneak.keyCode) {
			b3 = 5;
		}

		if(b3 >= 0) {
			this.movementKeyStates[b3] = z2;
		}

	}

	public void resetKeyState() {
		for(int i1 = 0; i1 < 10; ++i1) {
			this.movementKeyStates[i1] = false;
		}

	}

	public void updatePlayerMoveState(EntityPlayer entityPlayer1) {
		this.moveStrafe = 0.0F;
		this.moveForward = 0.0F;
		if(this.movementKeyStates[0]) {
			++this.moveForward;
		}

		if(this.movementKeyStates[1]) {
			--this.moveForward;
		}

		if(this.movementKeyStates[2]) {
			++this.moveStrafe;
		}

		if(this.movementKeyStates[3]) {
			--this.moveStrafe;
		}

		this.jump = this.movementKeyStates[4];
		this.sneak = this.movementKeyStates[5];
		if(this.sneak) {
			this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
			this.moveForward = (float)((double)this.moveForward * 0.3D);
		}

	}
}
