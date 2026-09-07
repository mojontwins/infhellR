package net.minecraft.game.command.worldedit;

import net.minecraft.game.command.CommandBase;

import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.world.World;
import net.minecraft.game.worldedit.WorldEdit;

public abstract class CommandWorldEdit extends CommandBase {
	public BlockPos pointingAt;
	
	public boolean checkCorners(World world) {
		if(!WorldEdit.checkCorners()) {
			this.theCommandSender.printMessage(world, "Set points first!");
			return false;
		}
		
		return true;
	}
	
}
