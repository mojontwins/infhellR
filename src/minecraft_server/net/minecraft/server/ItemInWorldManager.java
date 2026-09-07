package net.minecraft.server;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.network.packet.Packet53BlockChange;

/**
 * Manages block interactions (hit, dig, harvest) and item usage for a player
 * on the server side. Each player has their own ItemInWorldManager instance,
 * which coordinates the block-breaking progress, harvest checks,
 * and server-side item-consumption logic.
 */
public class ItemInWorldManager {

	/** The server-side World this manager operates in. */
	private WorldServer world;

	/** The player whose interactions this manager processes. */
	public EntityPlayer thisPlayer;

	/** Number of ticks elapsed since the start of block breaking. */
	private int blockBreakProgress;

	/** Coordinates of the currently targeted block for breaking. */
	private int targetBlockX;
	private int targetBlockY;
	private int targetBlockZ;

	/** Current damage dealt to the block being broken. */
	private int curBlockDamage;

	/** True while the player is actively breaking a block. */
	private boolean isBreaking;

	/** Coordinates of the last block that started being broken. */
	private int lastBreakX;
	private int lastBreakY;
	private int lastBreakZ;

	/** Block damage value when the last break started. */
	private int lastBreakDamage;

	/**
	 * Creates a new manager bound to a specific world.
	 *
	 * @param worldServer1 the WorldServer instance
	 */
	public ItemInWorldManager(WorldServer worldServer1) {
		this.world = worldServer1;
	}

	/**
	 * Called each tick to advance block-breaking progress.
	 * If the block damage reaches the threshold, the block is harvested.
	 */
	public void updateBlockRemoving() {
		++this.curBlockDamage;
		if (this.isBreaking) {
			int delta = this.curBlockDamage - this.lastBreakDamage;
			int blockID = this.world.getBlockId(this.lastBreakX, this.lastBreakY, this.lastBreakZ);
			int meta = this.world.getBlockMetadata(this.lastBreakX, this.lastBreakY, this.lastBreakZ);
			if (blockID != 0) {
				Block block = Block.blocksList[blockID];
				float progress = block.blockStrength(this.thisPlayer, meta) * (float) (delta + 1);
				if (progress >= 1.0F) {
					this.isBreaking = false;
					this.blockHarvested(this.lastBreakX, this.lastBreakY, this.lastBreakZ);
				}
			} else {
				this.isBreaking = false;
			}
		}
	}

	/**
	 * Handles the player starting to break a block at the given coordinates.
	 * In creative mode, the block is immediately broken. Otherwise, the
	 * breaking progress is initialized.
	 */
	public void blockClicked(int x, int y, int z, int side) {
		if (this.thisPlayer.isCreative) {
			if (!this.world.onBlockHit((EntityPlayer) null, x, y, z, side)) {
				this.blockHarvested(x, y, z);
			}
		} else {
			this.world.onBlockHit((EntityPlayer) null, x, y, z, side);
			this.blockBreakProgress = this.curBlockDamage;
			int blockID = this.world.getBlockId(x, y, z);
			int meta = this.world.getBlockMetadata(x, y, z);
			if (blockID > 0) {
				Block.blocksList[blockID].onBlockClicked(this.world, x, y, z, this.thisPlayer);
			}
			if (blockID > 0 && Block.blocksList[blockID].blockStrength(this.thisPlayer, meta) >= 1.0F) {
				this.blockHarvested(x, y, z);
			} else {
				this.targetBlockX = x;
				this.targetBlockY = y;
				this.targetBlockZ = z;
			}
		}
	}

	/**
	 * Called while the player continues to hold the break button on a block.
	 * If the block damage reaches 70% (or player is creative), the block is harvested.
	 */
	public void blockRemoving(int x, int y, int z) {
		if (x == this.targetBlockX && y == this.targetBlockY && z == this.targetBlockZ) {
			int delta = this.curBlockDamage - this.blockBreakProgress;
			int blockID = this.world.getBlockId(x, y, z);
			int meta = this.world.getBlockMetadata(x, y, z);
			if (blockID != 0) {
				Block block = Block.blocksList[blockID];
				float progress = block.blockStrength(this.thisPlayer, meta) * (float) (delta + 1);
				if (progress >= 0.7F || this.thisPlayer.isCreative) {
					this.blockHarvested(x, y, z);
				} else if (!this.isBreaking) {
					this.isBreaking = true;
					this.lastBreakX = x;
					this.lastBreakY = y;
					this.lastBreakZ = z;
					this.lastBreakDamage = this.blockBreakProgress;
				}
			}
		}
	}

	/** Removes the block at the given coordinates and fires destruction callbacks. */
	public boolean removeBlock(int x, int y, int z) {
		Block block = Block.blocksList[this.world.getBlockId(x, y, z)];
		int meta = this.world.getBlockMetadata(x, y, z);
		boolean removed = this.world.setBlockWithNotify(x, y, z, 0);
		if (block != null && removed) {
			block.onBlockDestroyedByPlayer(this.world, x, y, z, meta);
		}
		return removed;
	}

	/**
	 * Handles full block harvest: plays break sound, drops items,
	 * applies silk touch / fortune, and sends a block-change packet
	 * to the player (or all players in non-creative mode).
	 */
	public boolean blockHarvested(int x, int y, int z) {
		int blockID = this.world.getBlockId(x, y, z);
		int meta = this.world.getBlockMetadata(x, y, z);
		this.world.playAuxSFXAtEntity(this.thisPlayer, 2001, x, y, z, blockID + this.world.getBlockMetadata(x, y, z) * 256);
		boolean removed = this.removeBlock(x, y, z);
		if (this.thisPlayer.isCreative) {
			((EntityPlayerMP) this.thisPlayer).playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, this.world));
		} else {
			ItemStack heldStack = this.thisPlayer.getCurrentEquippedItem();
			if (heldStack != null) {
				heldStack.onDestroyBlock(blockID, x, y, z, this.thisPlayer);
				if (heldStack.stackSize == 0) {
					heldStack.onItemDestroyedByUse(this.thisPlayer);
					this.thisPlayer.destroyCurrentEquippedItem();
				}
			}
			if (removed && this.thisPlayer.canHarvestBlock(Block.blocksList[blockID], meta)) {
				if (!this.thisPlayer.isCreative) {
					// Silk touch: gold tools have silver touch
					if (heldStack != null && heldStack.getItem() != null && heldStack.getItem().silkTouch) {
						Block.blocksList[blockID].silkTouchBlock(this.world, x, y, z, meta);
					} else {
						Block.blocksList[blockID].harvestBlock(this.world, this.thisPlayer, x, y, z, meta);
					}
				}
				((EntityPlayerMP) this.thisPlayer).playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, this.world));
			}
		}
		return removed;
	}

	/** Handles right-click item usage, swapping stacks if the item changed. */
	public boolean itemUsed(EntityPlayer entityPlayer1, World world2, ItemStack itemStack3) {
		int originalSize = itemStack3.stackSize;
		int originalDamage = itemStack3.itemDamage;
		ItemStack resultStack = itemStack3.useItemRightClick(world2, this.thisPlayer);
		if (resultStack != itemStack3 || resultStack != null && resultStack.stackSize != originalSize) {
			this.thisPlayer.inventory.mainInventory[this.thisPlayer.inventory.currentItem] = resultStack;
			if (this.thisPlayer.isCreative) {
				resultStack.stackSize = originalSize;
				resultStack.setItemDamage(originalDamage);
			}
			if (resultStack.stackSize == 0) {
				this.thisPlayer.inventory.mainInventory[this.thisPlayer.inventory.currentItem] = null;
			}
			return true;
		} else {
			return false;
		}
	}

	/** Tries to activate a block with the held item, or falls back to item use. */
	public boolean activeBlockOrUseItem(EntityPlayer entityPlayer1, World world2, ItemStack itemStack3, int x, int y, int z, int side,
			float xWithinFace, float yWithinFace, float zWithinFace, byte shift) {
		int blockID = world2.getBlockId(x, y, z);
		return blockID > 0 && Block.blocksList[blockID].blockActivated(world2, x, y, z, entityPlayer1) && shift != 1
				? true
				: (itemStack3 == null ? false : itemStack3.useItem(entityPlayer1, world2, x, y, z, side, xWithinFace, yWithinFace, zWithinFace));
	}
}
