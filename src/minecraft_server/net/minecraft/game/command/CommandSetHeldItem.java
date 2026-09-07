package net.minecraft.game.command;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class CommandSetHeldItem extends CommandBase {

	@Override
	public String getString() {
		return "setHeldItem";
	}

	@Override
	public int getMinParams() {
		return 2;
	}

	// TODO reuse code to make ItemStack with id:meta and / or by name
	
	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		int entityId = this.toIntWithDefault(tokens[1], -1);
		ItemStack itemStack = this.parseItemOrBlock(tokens[2]);
		
		if(entityId >= 0 && itemStack != null && itemStack.itemID > 0) {
			Entity entity = theWorld.getEntityById(entityId);
			if(entity instanceof EntityLiving) {
				((EntityLiving) entity).setHeldItem(itemStack);
			}
		}
		
		return entityId;
	}

	@Override
	public String getHelp() {
		return "Sets the held item for a living entity (if supported)\n/setHeldItem <entityId> <itemId>\nReturns: entityId";
	}

}
