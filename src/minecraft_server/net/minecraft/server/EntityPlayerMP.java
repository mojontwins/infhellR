package net.minecraft.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.trading.Currency;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.TradingRecipeList;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.container.Container;
import net.minecraft.game.container.ContainerChest;
import net.minecraft.game.container.ContainerDispenser;
import net.minecraft.game.container.ContainerFurnace;
import net.minecraft.game.container.ContainerTrader;
import net.minecraft.game.container.ContainerWorkbench;
import net.minecraft.game.container.ICrafting;
import net.minecraft.game.container.IInventory;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.container.SlotCrafting;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EnumAction;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityDispenser;
import net.minecraft.game.world.block.tileentity.TileEntityFurnace;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet100OpenWindow;
import net.minecraft.network.packet.Packet101CloseWindow;
import net.minecraft.network.packet.Packet103SetSlot;
import net.minecraft.network.packet.Packet104WindowItems;
import net.minecraft.network.packet.Packet105UpdateProgressbar;
import net.minecraft.network.packet.Packet17Sleep;
import net.minecraft.network.packet.Packet18Animation;
import net.minecraft.network.packet.Packet200Statistic;
import net.minecraft.network.packet.Packet22Collect;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet38EntityStatus;
import net.minecraft.network.packet.Packet39AttachEntity;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet42RemoveEntityEffect;
import net.minecraft.network.packet.Packet51MapChunk;
import net.minecraft.network.packet.Packet5PlayerInventory;
import net.minecraft.network.packet.Packet8UpdateHealth;
import net.minecraft.network.packet.Packet94FreezeLevel;
import net.minecraft.network.packet.Packet99SetCreativeMode;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.entity.player.EnumStatus;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.item.ItemMapBase;

/**
 * Server-side representation of a player in multiplayer.
 * Extends EntityPlayer with networking, crafting synchronization,
 * and server-specific state management (invulnerability ticks,
 * chunk loading, window management).
 */
public class EntityPlayerMP extends EntityPlayer implements ICrafting {

	/** The network handler that sends/receives packets for this player. */
	public NetServerHandler playerNetServerHandler;

	/** Reference to the MinecraftServer instance. */
	public MinecraftServer mcServer;

	/** Manages block interaction state (breaking, item usage). */
	public ItemInWorldManager itemInWorldManager;

	/** Last known position for chunk loading management. */
	public double managedPosX;
	public double managedPosZ;

	/** Chunks that have been sent to the player. */
	public List<ChunkCoordIntPair> loadedChunks = new LinkedList<ChunkCoordIntPair>();

	/** Chunks the player is subscribed to for updates. */
	public Set<ChunkCoordIntPair> listeningChunks = new HashSet<ChunkCoordIntPair>();

	/** Last health value sent to the client (for detecting changes). */
	private int lastHealth = -99999999;

	/** Last freeze level sent to the client. */
	private int lastFreezeLevel = -99999999;

	/** Server-side invulnerability timer after teleport/respawn. */
	private int ticksOfInvuln = 60;

	/** Cached equipment slots for network synchronization. */
	private ItemStack[] playerInventory = new ItemStack[]{null, null, null, null, null};

	/** Next window ID to assign. */
	private int currentWindowId = 0;

	/** True when the client is only changing stack quantities (not slot changes). */
	public boolean isChangingQuantityOnly;

	/**
	 * Creates a new multiplayer player entity.
	 *
	 * @param minecraftServer1  the MinecraftServer instance
	 * @param world2            the world the player spawns in
	 * @param string3           the player's username
	 * @param itemInWorldManager4 the item interaction manager for this player
	 */
	public EntityPlayerMP(MinecraftServer minecraftServer1, World world2, String string3, ItemInWorldManager itemInWorldManager4) {
		super(world2);
		itemInWorldManager4.thisPlayer = this;
		this.itemInWorldManager = itemInWorldManager4;
		ChunkCoordinates spawn = world2.getSpawnPoint();
		int spawnX = spawn.posX;
		int spawnZ = spawn.posZ;
		int spawnY = spawn.posY;
		if (!world2.worldProvider.hasNoSky) {
			spawnX += this.rand.nextInt(20) - 10;
			spawnY = world2.findTopSolidBlock(spawnX, spawnZ);
			spawnZ += this.rand.nextInt(20) - 10;
		}
		this.setLocationAndAngles((double) spawnX + 0.5D, (double) spawnY, (double) spawnZ + 0.5D, 0.0F, 0.0F);
		this.mcServer = minecraftServer1;
		this.stepHeight = 0.0F;
		this.username = string3;
		this.yOffset = 0.0F;
	}

	/** Changes the world this player belongs to (on dimension change). */
	public void setWorldHandler(World world) {
		super.setWorld(world);
		this.itemInWorldManager = new ItemInWorldManager((WorldServer) world);
		this.itemInWorldManager.thisPlayer = this;
	}

	/** Syncs the crafting inventory with the client's GUI. */
	public void sendUpdateTimeAndWeather() {
		this.craftingInventory.onCraftGuiOpened(this);
	}

	/** Returns the cached equipment inventory. */
	public ItemStack[] getInventory() {
		return this.playerInventory;
	}

	/** Resets the player's eye height offset. */
	protected void resetHeight() {
		this.yOffset = 0.0F;
	}

	/** Returns the player's eye height (fixed at 1.62 blocks). */
	public float getEyeHeight() {
		return 1.62F;
	}

	/**
	 * Called each tick. Updates block-breaking progress, equipment
	 * synchronization, and inventory tracking.
	 */
	public void onUpdate() {
		this.itemInWorldManager.updateBlockRemoving();
		--this.ticksOfInvuln;
		this.craftingInventory.updateCraftingResults();

		// Sync equipment changes to nearby players.
		for (int slot = 0; slot < 5; ++slot) {
			ItemStack equipped = this.getEquipmentInSlot(slot);
			if (equipped != this.playerInventory[slot]) {
				this.mcServer.getEntityTracker(this.dimension).sendPacketToTrackedPlayers(this,
						new Packet5PlayerInventory(this.entityId, slot, equipped));
				this.playerInventory[slot] = equipped;
			}
		}
	}

	/** Returns the item in the given equipment slot. */
	public ItemStack getEquipmentInSlot(int slot) {
		return slot == 0 ? this.inventory.getCurrentItem() : this.inventory.armorInventory[slot - 1];
	}

	/** Handles player death: records death location, drops items. */
	public void onDeath(Entity entity) {
		this.rememberCoordinates();
		this.setDeadManChest();
		this.inventory.dropAllItems();
	}

	/** Checks PvP rules and invulnerability before allowing damage. */
	public boolean attackEntityFrom(Entity entity, int damage) {
		if (this.ticksOfInvuln > 0) {
			return false;
		} else {
			if (!this.mcServer.pvpOn) {
				if (entity instanceof EntityPlayer) {
					return false;
				}
				if (entity instanceof EntityArrow) {
					EntityArrow arrow = (EntityArrow) entity;
					if (arrow.shootingEntity instanceof EntityPlayer) {
						return false;
					}
				}
			}
			return super.attackEntityFrom(entity, damage);
		}
	}

	/** Checks the server's PvP setting. */
	protected boolean isPVPEnabled() {
		return this.mcServer.pvpOn;
	}

	/** Heals the player (delegates to parent). */
	public void heal(int amount) {
		super.heal(amount);
	}

	/**
	 * Called each tick after the base entity update.
	 * Handles chunk sending, portal logic, and health/freeze broadcasts.
	 */
	public void onUpdateEntity(boolean sendChunkPackets) {
		super.onUpdate();

		// Send map data for carried maps to the client.
		for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
			ItemStack stack = this.inventory.getStackInSlot(i);
			if (stack != null && Item.itemsList[stack.itemID].hasContents()
					&& this.playerNetServerHandler.getNumChunkDataPackets() <= 2) {
				Packet mapPacket = ((ItemMapBase) Item.itemsList[stack.itemID]).s_func_28022_b(stack, this.worldObj, this);
				if (mapPacket != null) {
					this.playerNetServerHandler.sendPacket(mapPacket);
				}
			}
		}

		// Send chunk data from the pending queue.
		if (sendChunkPackets && !this.loadedChunks.isEmpty()) {
			ChunkCoordIntPair chunk = (ChunkCoordIntPair) this.loadedChunks.get(0);
			if (chunk != null) {
				boolean shouldSend = false;
				if (this.playerNetServerHandler.getNumChunkDataPackets() < 4) {
					shouldSend = true;
				}
				if (shouldSend) {
					WorldServer world = this.mcServer.getWorldManager(this.dimension);
					this.loadedChunks.remove(chunk);
					this.playerNetServerHandler.sendPacket(new Packet51MapChunk(chunk.chunkXPos * 16, 0, chunk.chunkZPos * 16, 16, Chunk.SECTION_HEIGHT, 16, world));
					List<TileEntity> tiles = world.getTileEntityList(chunk.chunkXPos * 16, 0, chunk.chunkZPos * 16,
							chunk.chunkXPos * 16 + 16, Chunk.SECTION_HEIGHT, chunk.chunkZPos * 16 + 16);
					for (int i = 0; i < tiles.size(); ++i) {
						this.getTileEntityInfo((TileEntity) tiles.get(i));
					}
				}
			}
		}

		// Portal logic: handle dimension teleportation.
		if (this.inPortal) {
			if (this.mcServer.propertyManagerObj.getBooleanProperty("allow-nether", true)) {
				if (this.craftingInventory != this.inventorySlots) {
					this.closeScreen();
				}
				if (this.ridingEntity != null) {
					this.mountEntity(this.ridingEntity);
				} else {
					this.timeInPortal += 0.0125F;
					if (this.timeInPortal >= 1.0F) {
						this.timeInPortal = 1.0F;
						this.timeUntilPortal = 10;
						this.mcServer.configManager.sendPlayerToOtherDimension(this);
					}
				}
				this.inPortal = false;
			}
		} else {
			if (this.timeInPortal > 0.0F) {
				this.timeInPortal -= 0.05F;
			}
			if (this.timeInPortal < 0.0F) {
				this.timeInPortal = 0.0F;
			}
		}

		if (this.timeUntilPortal > 0) {
			--this.timeUntilPortal;
		}

		// Broadcast health changes.
		if (this.health != this.lastHealth) {
			this.playerNetServerHandler.sendPacket(new Packet8UpdateHealth(this.health));
			this.lastHealth = this.health;
		}

		// Broadcast freeze level changes.
		if (this.freezeLevel != this.lastFreezeLevel) {
			this.playerNetServerHandler.sendPacket(new Packet94FreezeLevel(this.freezeLevel));
			this.lastFreezeLevel = this.freezeLevel;
		}
	}

	/** Sends a tile entity description packet to the player. */
	private void getTileEntityInfo(TileEntity tile) {
		if (tile != null) {
			Packet packet = tile.getDescriptionPacket();
			if (packet != null) {
				this.playerNetServerHandler.sendPacket(packet);
			}
		}
	}

	/** Placeholder for living update logic. */
	public void onLivingUpdate() {
		super.onLivingUpdate();
	}

	/** Notifies nearby players when this player picks up an item or arrow. */
	public void onItemPickup(Entity entity, int count) {
		if (!entity.isDead) {
			EntityTracker tracker = this.mcServer.getEntityTracker(this.dimension);
			if (entity instanceof EntityItem) {
				tracker.sendPacketToTrackedPlayers(entity, new Packet22Collect(entity.entityId, this.entityId));
			}
			if (entity instanceof EntityArrow) {
				tracker.sendPacketToTrackedPlayers(entity, new Packet22Collect(entity.entityId, this.entityId));
			}
		}
		super.onItemPickup(entity, count);
		this.craftingInventory.updateCraftingResults();
	}

	/** Sets creative mode and notifies the client. */
	public void setCreativeMode(boolean creativeMode) {
		this.isCreative = creativeMode;
		this.playerNetServerHandler.sendPacket(new Packet99SetCreativeMode(creativeMode));
	}

	/** Broadcasts an arm swing animation to nearby players. */
	public void swingItem() {
		if (!this.isSwinging) {
			this.swingProgressInt = -1;
			this.isSwinging = true;
			EntityTracker tracker = this.mcServer.getEntityTracker(this.dimension);
			tracker.sendPacketToTrackedPlayers(this, new Packet18Animation(this, 1));
		}
	}

	public void s_func_22068_s() {
	}

	/** Handles player sleeping in a bed and broadcasts the sleep event. */
	public EnumStatus sleepInBedAt(int x, int y, int z) {
		EnumStatus result = super.sleepInBedAt(x, y, z);
		if (result == EnumStatus.OK) {
			EntityTracker tracker = this.mcServer.getEntityTracker(this.dimension);
			Packet17Sleep sleepPacket = new Packet17Sleep(this, 0, x, y, z);
			tracker.sendPacketToTrackedPlayers(this, sleepPacket);
			this.playerNetServerHandler.teleportTo(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.playerNetServerHandler.sendPacket(sleepPacket);
		}
		return result;
	}

	/** Wakes the player from sleep and broadcasts the wake event. */
	public void wakeUpPlayer(boolean immediate, boolean updateWorld, boolean setSpawn) {
		if (this.isPlayerSleeping()) {
			EntityTracker tracker = this.mcServer.getEntityTracker(this.dimension);
			tracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 3));
		}
		super.wakeUpPlayer(immediate, updateWorld, setSpawn);
		if (this.playerNetServerHandler != null) {
			this.playerNetServerHandler.teleportTo(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}
	}

	/** Handles mounting a vehicle and sends attach/teleport packets. */
	public void mountEntity(Entity entity) {
		super.mountEntity(entity);
		this.playerNetServerHandler.sendPacket(new Packet39AttachEntity(this, this.ridingEntity));
		this.playerNetServerHandler.teleportTo(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
	}

	public boolean hitGround(double d, boolean onGround) {
		return false;
	}

	public void handleFalling(double d, boolean onGround) {
		super.hitGround(d, onGround);
	}

	/** Returns the next available window ID. */
	private void getNextWindowId() {
		this.currentWindowId = this.currentWindowId % 100 + 1;
	}

	/** Opens the crafting (workbench) GUI. */
	public void displayWorkbenchGUI(int x, int y, int z) {
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new Packet100OpenWindow(this.currentWindowId, 1, "Crafting", 9));
		this.craftingInventory = new ContainerWorkbench(this.inventory, this.worldObj, x, y, z);
		this.craftingInventory.windowId = this.currentWindowId;
		this.craftingInventory.onCraftGuiOpened(this);
	}

	/** Opens a chest GUI. */
	public void displayGUIChest(IInventory inventory) {
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new Packet100OpenWindow(this.currentWindowId, 0, inventory.getInvName(), inventory.getSizeInventory()));
		this.craftingInventory = new ContainerChest(this.inventory, inventory);
		this.craftingInventory.windowId = this.currentWindowId;
		this.craftingInventory.onCraftGuiOpened(this);
	}

	/** Opens a furnace GUI. */
	public void displayGUIFurnace(TileEntityFurnace furnace) {
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new Packet100OpenWindow(this.currentWindowId, 2, furnace.getInvName(), furnace.getSizeInventory()));
		this.craftingInventory = new ContainerFurnace(this.inventory, furnace);
		this.craftingInventory.windowId = this.currentWindowId;
		this.craftingInventory.onCraftGuiOpened(this);
	}

	/** Opens a dispenser GUI. */
	public void displayGUIDispenser(TileEntityDispenser dispenser) {
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new Packet100OpenWindow(this.currentWindowId, 3, dispenser.getInvName(), dispenser.getSizeInventory()));
		this.craftingInventory = new ContainerDispenser(this.inventory, dispenser);
		this.craftingInventory.windowId = this.currentWindowId;
		this.craftingInventory.onCraftGuiOpened(this);
	}

	/** Opens the NPC trading GUI. */
	public void displayGUITrading(InventoryPlayer playerInventory, ITrader trader) {
		this.getNextWindowId();
		this.craftingInventory = new ContainerTrader(this.inventory, trader, this.worldObj);
		this.craftingInventory.windowId = this.currentWindowId;
		this.craftingInventory.onCraftGuiOpened(this);
		this.playerNetServerHandler.sendPacket(new Packet100OpenWindow(this.currentWindowId, 6, trader.getTraderName(),
				trader.getCurrency() == Currency.currencyEmerald ? 0 : 1));

		TradingRecipeList recipes = trader.getRecipes(this);
		if (recipes != null) {
			try {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(bytes);
				out.writeInt(this.currentWindowId);
				recipes.writeRecipiesToStream(out);
				this.playerNetServerHandler.sendPacket(new Packet250CustomPayload("MC|TrList", bytes.toByteArray()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Notifies the client of a slot change in a crafting container. */
	public void updateCraftingInventorySlot(Container container, int slot, ItemStack stack) {
		if (!(container.getSlot(slot) instanceof SlotCrafting)) {
			if (!this.isChangingQuantityOnly) {
				this.playerNetServerHandler.sendPacket(new Packet103SetSlot(container.windowId, slot, stack));
			}
		}
	}

	public void s_func_28017_a(Container container) {
		this.updateCraftingInventory(container, container.getInventoryStacks());
	}

	/** Sends the full inventory state to the client. */
	public void updateCraftingInventory(Container container, List<ItemStack> stacks) {
		this.playerNetServerHandler.sendPacket(new Packet104WindowItems(container.windowId, stacks));
		this.playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, this.inventory.getItemStack()));
	}

	/** Sends a progress bar update for a container (e.g., furnace burn time). */
	public void updateCraftingInventoryInfo(Container container, int progressBar, int value) {
		this.playerNetServerHandler.sendPacket(new Packet105UpdateProgressbar(container.windowId, progressBar, value));
	}

	public void onItemStackChanged(ItemStack stack) {
	}

	/** Closes the current GUI and reopens the inventory. */
	public void closeScreen() {
		this.playerNetServerHandler.sendPacket(new Packet101CloseWindow(this.craftingInventory.windowId));
		this.closeCraftingGui();
	}

	/** Syncs the held item to the client. */
	public void updateHeldItem() {
		if (!this.isChangingQuantityOnly) {
			this.playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, this.inventory.getItemStack()));
		}
	}

	/** Closes the crafting GUI and reverts to the player's main inventory. */
	public void closeCraftingGui() {
		this.craftingInventory.onCraftGuiClosed(this);
		this.craftingInventory = this.inventorySlots;
	}

	/** Sets movement input values received from the client. */
	public void setMovementType(float strafe, float forward, boolean jumping, boolean sneaking, float pitch, float yaw) {
		this.moveStrafing = strafe;
		this.moveForward = forward;
		this.isJumping = jumping;
		this.setSneaking(sneaking);
		this.rotationPitch = pitch;
		this.rotationYaw = yaw;
	}

	/** Sends a stat update to the client, splitting large values into 100-unit chunks. */
	public void addStat(StatBase stat, int amount) {
		if (stat != null) {
			if (!stat.isIndependent) {
				while (amount > 100) {
					this.playerNetServerHandler.sendPacket(new Packet200Statistic(stat.statId, 100));
					amount -= 100;
				}
				this.playerNetServerHandler.sendPacket(new Packet200Statistic(stat.statId, amount));
			}
		}
	}

	/** Dismounts from any vehicle and wakes up if sleeping. */
	public void s_func_30002_A() {
		if (this.ridingEntity != null) {
			this.mountEntity(this.ridingEntity);
		}
		if (this.riddenByEntity != null) {
			this.riddenByEntity.mountEntity(this);
		}
		if (this.sleeping) {
			this.wakeUpPlayer(true, false, false);
		}
	}

	/** Resets health tracking (called on respawn). */
	public void s_func_30001_B() {
		this.lastHealth = -99999999;
	}

	/** Sends a chat message to the player (with translation). */
	public void addChatMessage(String message) {
		StringTranslate translator = StringTranslate.getInstance();
		String translated = translator.translateKey(message);
		this.playerNetServerHandler.sendPacket(new Packet3Chat(translated));
	}

	/** Sends the item-use-finished packet. */
	@Override
	protected void onItemUseFinish() {
		System.out.println("Send itemUseFinish");
		this.playerNetServerHandler.sendPacket(new Packet38EntityStatus(this.entityId, (byte) 9));
		super.onItemUseFinish();
	}

	/** Broadcasts eating animation when a food item is used. */
	@Override
	public void setItemInUse(ItemStack stack, int duration) {
		super.setItemInUse(stack, duration);
		if (stack != null && stack.getItem() != null && stack.getItem().getItemUseAction(stack) == EnumAction.eat) {
			System.out.println("Send packet18Animation 5");
			EntityTracker tracker = this.mcServer.getEntityTracker(this.dimension);
			tracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 5));
		}
	}

	@Override
	public void func_6420_o() {
	}

	/** Broadcasts a new status effect to the player. */
	@Override
	public void onNewStatusEffect(StatusEffect effect) {
		super.onNewStatusEffect(effect);
		this.playerNetServerHandler.sendPacket(new Packet41EntityEffect(this.entityId, effect));
	}

	@Override
	public void onChangedStatusEffect(StatusEffect effect) {
		super.onNewStatusEffect(effect);
		this.playerNetServerHandler.sendPacket(new Packet41EntityEffect(this.entityId, effect));
	}

	@Override
	public void onFinishedStatusEffect(StatusEffect effect) {
		super.onNewStatusEffect(effect);
		this.playerNetServerHandler.sendPacket(new Packet42RemoveEntityEffect(this.entityId, effect));
	}
}