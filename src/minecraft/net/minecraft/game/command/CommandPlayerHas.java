package net.minecraft.game.command;

import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandPlayerHas extends CommandBase {

	@Override
	public String getString() {
		return "playerHas";
	}

	@Override
	public int getMinParams() {
		return 2;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		if(thePlayer == null) {
			this.theCommandSender.printMessage(theWorld, "playerHas: No valid player!");
			return CommandBase.BREAK_AND_FAIL;
		}
		
		ItemStack itemStack = this.parseItemOrBlock(tokens [1]);
		if(itemStack == null) {
			this.theCommandSender.printMessage(theWorld, "playerHas: Wrong block:meta|item:damage");
			return CommandBase.BREAK_AND_FAIL;
		}
		
		InventoryPlayer inventory = thePlayer.inventory;
		if(inventory != null) {
			for(int i = 0; i < inventory.getSizeInventory(); i ++) {
				ItemStack currentItemStack = inventory.getStackInSlot(i);
				if(currentItemStack != null && currentItemStack.isStackEqual(itemStack)) return 1;
			}
		}
		
		return 0;
	}

	@Override
	public String getHelp() {
		return "/playerHas <@p|playerName> <item:damage>|<block:meta>\nCheck if player has item/block\nReturns: 1 on success";
	}

}
