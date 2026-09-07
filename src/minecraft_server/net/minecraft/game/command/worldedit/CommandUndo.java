package net.minecraft.game.command.worldedit;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.worldedit.WorldEdit;

public class CommandUndo extends CommandWorldEdit {

	@Override
	public String getString() {
		return "undo";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		if(WorldEdit.hasUndo) {
			this.theCommandSender.printMessage(theWorld, "Undoing to " + WorldEdit.undoOrigin);
			WorldEdit.undo(theWorld);
			
			return 1;
		}
		
		this.theCommandSender.printMessage(theWorld, "Nothing to undo");
		return 0;
	}

	@Override
	public String getHelp() {
		return "Undoes last action.\nReturns: 1 on success.";
	}

}
