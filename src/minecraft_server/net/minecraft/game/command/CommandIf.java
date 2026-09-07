package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandIf extends CommandBase {

	@Override
	public String getString() {
		return "if";
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		int yield = this.toIntWithDefault(tokens [1], 0);
		
		if(yield <= 0) return CommandBase.BREAK_AND_FAIL;
		
		return yield;
	}

	@Override
	public String getHelp() {
		return "/if { commands }\nWill fail and interrupt operation if commands yield 0.\nReturns: { command } on success.";
	}

}
