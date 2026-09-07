package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.network.packet.Packet;

public class ItemMapBase extends Item {
	protected ItemMapBase(int i1) {
		super(i1);
	}

	public boolean hasContents() {
		return true;
	}

	public Packet s_func_28022_b(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		return null;
	}
}
