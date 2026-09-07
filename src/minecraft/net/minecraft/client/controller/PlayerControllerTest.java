package net.minecraft.client.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class PlayerControllerTest extends PlayerController {
	public PlayerControllerTest(Minecraft minecraft1) {
		super(minecraft1);
		this.isInTestMode = true;
	}

	public void func_6473_b(EntityPlayer entityPlayer1) {
		for(int i2 = 0; i2 < 9; ++i2) {
			if(entityPlayer1.inventory.mainInventory[i2] == null) {
				this.mc.thePlayer.inventory.mainInventory[i2] = new ItemStack((Block)Block.blocksList[i2]);
			} else {
				this.mc.thePlayer.inventory.mainInventory[i2].stackSize = 1;
			}
		}

	}

	public boolean shouldDrawHUD() {
		return false;
	}

	public void onWorldChange(World world1) {
		super.onWorldChange(world1);
	}

	public void updateController() {
	}
}
