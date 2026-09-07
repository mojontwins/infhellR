package net.minecraft.client.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class PlayerControllerSP extends PlayerController {
	private int curBlockX = -1;
	private int curBlockY = -1;
	private int curBlockZ = -1;
	private float curBlockDamage = 0.0F;
	private float prevBlockDamage = 0.0F;
	private float blockDestroySoundCounter = 0.0F;
	private int blockHitWait = 0;

	public PlayerControllerSP(Minecraft minecraft1) {
		super(minecraft1);
	}

	public void flipPlayer(EntityPlayer entityPlayer1) {
		entityPlayer1.rotationYaw = -180.0F;
	}

	public boolean sendBlockRemoved(int x, int y, int z, int side) {
		int blockId = this.mc.theWorld.getBlockId(x, y, z);
		int metadata = this.mc.theWorld.getBlockMetadata(x, y, z);
		boolean wasRemoved = super.sendBlockRemoved(x, y, z, side);
		ItemStack itemStack = this.mc.thePlayer.getCurrentEquippedItem();
		boolean canHarvestBlock = this.mc.thePlayer.canHarvestBlock(Block.blocksList[blockId], metadata);
		if(itemStack != null) {
			itemStack.onDestroyBlock(blockId, x, y, z, this.mc.thePlayer);
			if(itemStack.stackSize == 0) {
				itemStack.onItemDestroyedByUse(this.mc.thePlayer);
				this.mc.thePlayer.destroyCurrentEquippedItem();
			}
		}

		if(wasRemoved && canHarvestBlock && !this.mc.thePlayer.isCreative) {
			if (itemStack != null && itemStack.getItem() != null && itemStack.getItem().silkTouch) {
				Block.blocksList[blockId].silkTouchBlock(this.mc.theWorld, x, y, z, metadata);
			} else {
				Block.blocksList[blockId].harvestBlock(this.mc.theWorld, this.mc.thePlayer, x, y, z, metadata);
			}
		}

		return wasRemoved;
	}

	public void clickBlock(int i1, int i2, int i3, int i4) {
		this.mc.theWorld.onBlockHit(this.mc.thePlayer, i1, i2, i3, i4);
		int i5 = this.mc.theWorld.getBlockId(i1, i2, i3);
		int meta = this.mc.theWorld.getBlockMetadata(i1, i2, i3);
		if(i5 > 0 && this.curBlockDamage == 0.0F) {
			Block.blocksList[i5].onBlockClicked(this.mc.theWorld, i1, i2, i3, this.mc.thePlayer);
		}

		if(i5 > 0 && Block.blocksList[i5].blockStrength(this.mc.thePlayer, meta) >= 1.0F) {
			this.sendBlockRemoved(i1, i2, i3, i4);
		}

	}

	public void resetBlockRemoving() {
		this.curBlockDamage = 0.0F;
		this.blockHitWait = 0;
	}

	public void sendBlockRemoving(int i1, int i2, int i3, int i4) {
		if(this.blockHitWait > 0) {
			--this.blockHitWait;
		} else {
			if(i1 == this.curBlockX && i2 == this.curBlockY && i3 == this.curBlockZ) {
				int i5 = this.mc.theWorld.getBlockId(i1, i2, i3);
				int meta = this.mc.theWorld.getBlockMetadata(i1, i2, i3);
				if(i5 == 0) {
					return;
				}

				Block block6 = Block.blocksList[i5];
				this.curBlockDamage += block6.blockStrength(this.mc.thePlayer, meta);
				if(this.blockDestroySoundCounter % 4.0F == 0.0F && block6 != null) {
					this.mc.sndManager.playSound(block6.stepSound.getStepSound(), (float)i1 + 0.5F, (float)i2 + 0.5F, (float)i3 + 0.5F, (block6.stepSound.getVolume() + 1.0F) / 8.0F, block6.stepSound.getPitch() * 0.5F);
				}

				++this.blockDestroySoundCounter;
				if(this.curBlockDamage >= 1.0F || this.mc.thePlayer.isCreative) {
					this.sendBlockRemoved(i1, i2, i3, i4);
					this.curBlockDamage = 0.0F;
					this.prevBlockDamage = 0.0F;
					this.blockDestroySoundCounter = 0.0F;
					this.blockHitWait = 5;
				}
			} else {
				this.curBlockDamage = 0.0F;
				this.prevBlockDamage = 0.0F;
				this.blockDestroySoundCounter = 0.0F;
				this.curBlockX = i1;
				this.curBlockY = i2;
				this.curBlockZ = i3;
			}

		}
	}

	public void setPartialTime(float f1) {
		if(this.curBlockDamage <= 0.0F) {
			this.mc.ingameGUI.damageGuiPartialTime = 0.0F;
			this.mc.renderGlobal.damagePartialTime = 0.0F;
		} else {
			float f2 = this.prevBlockDamage + (this.curBlockDamage - this.prevBlockDamage) * f1;
			this.mc.ingameGUI.damageGuiPartialTime = f2;
			this.mc.renderGlobal.damagePartialTime = f2;
		}

	}

	public float getBlockReachDistance() {
		return 4.0F;
	}

	public void onWorldChange(World world1) {
		super.onWorldChange(world1);
	}

	public void updateController() {
		this.prevBlockDamage = this.curBlockDamage;
		this.mc.sndManager.playRandomMusicIfReady();
	}
}
