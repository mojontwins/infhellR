package net.minecraft.game.command;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.BlockPos;

public interface ICommandSender {
	public void printMessage(World world, String message);

	BlockPos getMouseOverCoordinates();
}
