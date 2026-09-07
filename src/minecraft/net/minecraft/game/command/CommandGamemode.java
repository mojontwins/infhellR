package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandGamemode extends CommandBase {

	@Override
	public String getString() {
		return "gamemode";
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		if(thePlayer == null) return 0;
		
		String gameMode = tokens [1];
		
		int res = thePlayer.isCreative ? 1 : 0;
		
		if ("0".equals(gameMode) || "survival".equals(gameMode)) {
			if (thePlayer.isCreative) this.theCommandSender.printMessage(theWorld, "Game mode changed to survival");
			thePlayer.isCreative = false;
			thePlayer.isFlying = false;
			res = 0;
		} else if ("1".equals(gameMode) || "creative".equals(gameMode)) {
			if (!thePlayer.isCreative) this.theCommandSender.printMessage(theWorld, "Game mode changed to creative");
			thePlayer.isCreative = true;
			res = 1;
		}
		
		return res;
	}

	@Override
	public String getHelp() {
		return "Sets the game mode for the current player\n/gamemode 0|1|survival|creative [<username>]\nReturns: game mode (0 or 1)";
	}

}
