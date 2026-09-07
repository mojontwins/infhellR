package net.minecraft.game;

import net.minecraft.game.entity.player.EntityPlayer;

public interface IPlayerFileData {
	void writePlayerData(EntityPlayer entityPlayer1);

	void readPlayerData(EntityPlayer entityPlayer1);
}
