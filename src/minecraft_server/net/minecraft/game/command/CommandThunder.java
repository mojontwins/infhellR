package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandThunder extends CommandBase {

	public CommandThunder() {
	}

	@Override
	public String getString() {
		return "thunder";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		theWorld.getWorldInfo().setThunderTime(0);
		return 0;
	}

	@Override
	public String getHelp() {
		return "Toggles thunderstorm on<->off\nReturns: 0";
	}

}
