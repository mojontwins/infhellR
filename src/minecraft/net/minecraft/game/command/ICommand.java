package net.minecraft.game.command;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public interface ICommand {
	public String getString();
	
	public int getMinParams();
	
	public int execute(String [] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer);
	
	public String getHelp();
	
	public CommandBase withCommandSender(ICommandSender commandSender);
}
