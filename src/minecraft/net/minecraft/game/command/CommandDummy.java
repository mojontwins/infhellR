package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandDummy extends CommandBase {

	public CommandDummy() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getString() {
		return "dummy";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		return 0;
	}

	@Override
	public String getHelp() {
		return "";
	}

}
