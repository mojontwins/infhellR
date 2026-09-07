package net.minecraft.game.command.worldedit;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.worldedit.WorldEdit;

public class CommandRotateYCW extends CommandWorldEdit {

	@Override
	public String getString() {
		return "rotatey_cw";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		WorldEdit.rotate_cw();
		return 0;
	}

	@Override
	public String getHelp() {
		return "Rotates the clipboard clockwise around the Y axis";
	}

}
