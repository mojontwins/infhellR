package net.minecraft.game.container;

import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.ItemArmor;

public class SlotArmor extends Slot {
	final int armorType;
	final ContainerPlayer inventory;

	public SlotArmor(ContainerPlayer containerPlayer1, IInventory iInventory2, int i3, int i4, int i5, int i6) {
		super(iInventory2, i3, i4, i5);
		this.inventory = containerPlayer1;
		this.armorType = i6;
	}

	public int getSlotStackLimit() {
		return 1;
	}

	public boolean isItemValid(ItemStack itemStack1) {
		return itemStack1.getItem() instanceof ItemArmor ? 
				((ItemArmor)itemStack1.getItem()).armorType == this.armorType 
			: 
				(itemStack1.getItem().shiftedIndex == Block.pumpkin.blockID || itemStack1.getItem().shiftedIndex == Block.divingHelmet.blockID ? 
						this.armorType == 0 
					: 
						false);
	}
}

