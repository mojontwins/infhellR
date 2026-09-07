package net.minecraft.game.command.worldedit;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.worldedit.WorldEdit;

public class CommandClear extends CommandWorldEdit {

	@Override
	public String getString() {
		return "clear";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		if(this.checkCorners(theWorld)) {
			int cleared = WorldEdit.clear(theWorld);
			this.theCommandSender.printMessage(theWorld, cleared + " blocks clear.");
			
			return cleared;
		}
		
		return 0;
	}

	@Override
	public String getHelp() {
		return "Clears the active area.\nReturns: blocks cleared.";
	}

}
