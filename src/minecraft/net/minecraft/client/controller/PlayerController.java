package net.minecraft.client.controller;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class PlayerController {
	protected final Minecraft mc;
	public boolean isInTestMode = false;

	public PlayerController(Minecraft minecraft1) {
		this.mc = minecraft1;
	}

	public void onWorldChange(World world1) {
	}

	public void clickBlock(int i1, int i2, int i3, int i4) {
		this.mc.theWorld.onBlockHit(this.mc.thePlayer, i1, i2, i3, i4);
		this.sendBlockRemoved(i1, i2, i3, i4);
	}

	public boolean sendBlockRemoved(int i1, int i2, int i3, int i4) {
		World world5 = this.mc.theWorld;
		Block block6 = Block.blocksList[world5.getBlockId(i1, i2, i3)];
		world5.playAuxSFX(2001, i1, i2, i3, block6.blockID + world5.getBlockMetadata(i1, i2, i3) * 256);
		int i7 = world5.getBlockMetadata(i1, i2, i3);
		boolean z8 = world5.setBlockWithNotify(i1, i2, i3, block6 != null && block6.blockMaterial == Material.water ? Block.waterStill.blockID : 0);
		if(block6 != null && z8) {
			block6.onBlockDestroyedByPlayer(world5, i1, i2, i3, i7);
		}

		return z8;
	}

	public void sendBlockRemoving(int i1, int i2, int i3, int i4) {
	}

	public void resetBlockRemoving() {
	}

	public void setPartialTime(float f1) {
	}

	public float getBlockReachDistance() {
		return 5.0F;
	}

	public boolean sendUseItem(EntityPlayer entityPlayer1, World world2, ItemStack itemStack3) {
		int i4 = itemStack3.stackSize;
		ItemStack itemStack5 = itemStack3.useItemRightClick(world2, entityPlayer1);
		if(itemStack5 != itemStack3 || itemStack5 != null && itemStack5.stackSize != i4) {
			entityPlayer1.inventory.mainInventory[entityPlayer1.inventory.currentItem] = itemStack5;
			if(itemStack5.stackSize == 0) {
				entityPlayer1.inventory.mainInventory[entityPlayer1.inventory.currentItem] = null;
			}

			return true;
		} else {
			return false;
		}
	}

	public void flipPlayer(EntityPlayer entityPlayer1) {
	}

	public void updateController() {
	}

	public boolean shouldDrawHUD() {
		return true;
	}

	public void func_6473_b(EntityPlayer entityPlayer1) {
	}

	public boolean sendPlaceBlock(EntityPlayer entityPlayer, World world, ItemStack itemStack, int x, int y, int z, int face, float xWithinFace, float yWithinFace, float zWithinFace, byte shift) {
		int i8 = world.getBlockId(x, y, z);
		return i8 > 0 && Block.blocksList[i8].blockActivated(world, x, y, z, entityPlayer) && shift != 1 ? true : (itemStack == null ? false : itemStack.useItem(entityPlayer, world, x, y, z, face, xWithinFace, yWithinFace, zWithinFace));
	}

	public EntityPlayer createPlayer(World world1) {
		return new EntityPlayerSP(this.mc, world1, this.mc.session, world1.worldProvider.worldType);
	}

	public void interactWithEntity(EntityPlayer entityPlayer1, Entity entity2) {
		entityPlayer1.useCurrentItemOnEntity(entity2);
	}

	public void attackEntity(EntityPlayer entityPlayer1, Entity entity2) {
		entityPlayer1.attackTargetEntityWithCurrentItem(entity2);
	}

	public ItemStack windowClick(int i1, int i2, int i3, boolean z4, EntityPlayer entityPlayer5) {
		return entityPlayer5.craftingInventory.slotClick(i2, i3, z4, entityPlayer5);
	}

	public void onCraftGuiClosed(int i1, EntityPlayer entityPlayer2) {
		entityPlayer2.craftingInventory.onCraftGuiClosed(entityPlayer2);
		entityPlayer2.craftingInventory = entityPlayer2.inventorySlots;
	}
	
	public void onStoppedUsingItem(EntityPlayer entityPlayer1) {
		entityPlayer1.stopUsingItem();
	}
	
	// Mine
	
	public int sendSetItemStack(Item item, int itemDamage) {
		InventoryPlayer inventory = this.mc.thePlayer.inventory;
		int stackSize = 1;
		
		if(
			inventory.mainInventory[inventory.currentItem] != null && 
			inventory.mainInventory[inventory.currentItem].itemID == item.shiftedIndex && 
			inventory.mainInventory[inventory.currentItem].itemDamage == itemDamage
		) {
			stackSize = inventory.mainInventory[inventory.currentItem].stackSize + 1;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			stackSize = item.getItemStackLimit();
		}

		inventory.mainInventory[inventory.currentItem] = new ItemStack(item, stackSize, itemDamage);
		this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
	
		return stackSize;
	}

	public void sendSlotPacket(ItemStack itemStack, int var5) {
	}

	public void sendSetItemStackCreative(ItemStack itemStack) {
	}

}
