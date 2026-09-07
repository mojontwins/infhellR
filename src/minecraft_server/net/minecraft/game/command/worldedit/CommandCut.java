package net.minecraft.game.command.worldedit;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.worldedit.WorldEdit;

public class CommandCut extends CommandWorldEdit {

	@Override
	public String getString() {
		return "cut";
	}

	@Override
	public int getMinParams() {
		return 0;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		if(this.checkCorners(theWorld)) {
			WorldEdit.cut(theWorld, thePlayer);
			this.theCommandSender.printMessage(theWorld, WorldEdit.clipboardSize() + " blocks cut to the clipboard.");
			
			return WorldEdit.clipboardSize();
		}
		
		return 0;
	}

	@Override
	public String getHelp() {
		return "Cuts the active area to the clipboard.\nReturns: blocks cut.";
	}

}
