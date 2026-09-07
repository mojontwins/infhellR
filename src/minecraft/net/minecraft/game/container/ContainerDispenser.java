package net.minecraft.game.container;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.block.tileentity.TileEntityDispenser;

public class ContainerDispenser extends Container {
	private TileEntityDispenser tileEntityDispenser;

	public ContainerDispenser(IInventory iInventory1, TileEntityDispenser tileEntityDispenser2) {
		this.tileEntityDispenser = tileEntityDispenser2;

		int i3;
		int i4;
		for(i3 = 0; i3 < 3; ++i3) {
			for(i4 = 0; i4 < 3; ++i4) {
				this.addSlot(new Slot(tileEntityDispenser2, i4 + i3 * 3, 62 + i4 * 18, 17 + i3 * 18));
			}
		}

		for(i3 = 0; i3 < 3; ++i3) {
			for(i4 = 0; i4 < 9; ++i4) {
				this.addSlot(new Slot(iInventory1, i4 + i3 * 9 + 9, 8 + i4 * 18, 84 + i3 * 18));
			}
		}

		for(i3 = 0; i3 < 9; ++i3) {
			this.addSlot(new Slot(iInventory1, i3, 8 + i3 * 18, 142));
		}

	}

	public boolean canInteractWith(EntityPlayer entityPlayer1) {
		return this.tileEntityDispenser.canInteractWith(entityPlayer1);
	}
}
