package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandNextMoonBad extends CommandBase {

	public CommandNextMoonBad() {
	}

	@Override
	public String getString() {
		return "nextMoonBad";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		theWorld.nextMoonBad = true;
		this.theCommandSender.printMessage(theWorld, "Next moon will be blood moon");
		return 0;
	}

	@Override
	public String getHelp() {
		return "Makes next night have a blood moon\nReturns: 0";
	}

}
