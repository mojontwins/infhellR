package net.minecraft.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.game.ChatAllowedCharacters;
import net.minecraft.game.MathHelper;
import net.minecraft.game.container.Container;
import net.minecraft.game.container.Slot;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityCreature;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.game.world.block.tileentity.TileEntityCommandBlock;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.container.ContainerTrader;
import net.minecraft.network.NetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet0KeepAlive;
import net.minecraft.network.packet.Packet101CloseWindow;
import net.minecraft.network.packet.Packet102WindowClick;
import net.minecraft.network.packet.Packet103SetSlot;
import net.minecraft.network.packet.Packet106Transaction;
import net.minecraft.network.packet.Packet107CreativeSetSlot;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.network.packet.Packet13PlayerLookMove;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet15Place;
import net.minecraft.network.packet.Packet16BlockItemSwitch;
import net.minecraft.network.packet.Packet18Animation;
import net.minecraft.network.packet.Packet19EntityAction;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet255KickDisconnect;
import net.minecraft.network.packet.Packet27Position;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.network.packet.Packet7UseEntity;
import net.minecraft.network.packet.Packet91UpdateCommandBlock;
import net.minecraft.network.packet.Packet93UpdateAnimalName;
import net.minecraft.network.packet.Packet97SetInventorySlot;
import net.minecraft.network.packet.Packet99SetCreativeMode;
import net.minecraft.network.packet.Packet9Respawn;

/**
 * Handles network communication between the server and a single connected player.
 * This is the central hub for processing all incoming packets from a client and
 * dispatching them to appropriate game systems (movement, inventory, chat, etc.).
 * 
 * This handler also implements anti-cheat measures including:
 * <ul>
 *   <li>Position validation (anti-speedhack, anti-teleport)</li>
 *   <li>Flying detection (anti-fly)</li>
 *   <li>Stance checks (ensuring player's head/feet positions are physically valid)</li>
 *   <li>Movement distance limiting</li>
 *   <li>Block interaction range validation</li>
 * </ul>
 * 
 * The handler maintains state about the player's last confirmed position, keepalive
 * status, and pending inventory transactions to ensure synchronized state between
 * client and server.
 * 
 * @see NetHandler
 * @see ICommandListener
 * @see EntityPlayerMP
 * @see MinecraftServer
 */

public class NetServerHandler extends NetHandler implements ICommandListener {
    /** Server-wide logger for NetServerHandler-related events. */
    public static Logger logger = Logger.getLogger("Minecraft");

    /** The network manager that handles the underlying TCP connection to this player. */
    public NetworkManager netManager;

    /** True once this connection has been closed (player kicked or disconnected). */
    public boolean connectionClosed = false;

    /** Reference to the running MinecraftServer instance. */
    private MinecraftServer mcServer;

    /** The player entity associated with this network connection. */
    private EntityPlayerMP playerEntity;

    /** Counter incremented on each handlePackets() call. Used to throttle keepalive packets. */
    private int keepAliveTicksSent;

    /** Tick counter recorded when last packet was sent to the client. */
    private int keepAliveTicksReceived;

    /** Number of consecutive ticks the player has been in air. Used to detect flying cheats. */
    private int playerInAirTime;

    /** Cooldown (in ticks) before inventory changes can propagate to the client. */
    private int inventoryChangeCooldown = 0;

    /** Cooldown (in ticks) for the creative-mode item-drop spam protection. */
    private int creativeCooldown = 0;

    /** Last server-confirmed player X position (used for movement validation). */
    private double lastConfirmedPosX;

    /** Last server-confirmed player Y position (used for movement validation). */
    private double lastConfirmedPosY;

    /** Last server-confirmed player Z position (used for movement validation). */
    private double lastConfirmedPosZ;

    /**
     * Flag indicating whether the player's reported position is trusted.
     * Set to false after a server teleport, then becomes true again once the player
     * confirms the new position via Packet10Flying.
     */
    private boolean positionValid = true;

    /**
     * Tracks pending transaction IDs per crafting inventory window. When the server
     * rejects a client-side slot click (itemStack mismatch), it sends a transaction
     * packet and records the expected response. If the client confirms, the inventory
     * is unlocked; if not, the server reverts the inventory state.
     */
    private Map<Integer, Short> pendingTransactionByWindow = new HashMap<Integer, Short>();

    /**
     * Constructs a new NetServerHandler for the given player.
     *
     * @param minecraftServer the running MinecraftServer instance
     * @param networkManager  the network manager for this connection
     * @param entityPlayerMP  the player entity to bind this handler to
     */
    public NetServerHandler(MinecraftServer minecraftServer, NetworkManager networkManager,
            EntityPlayerMP entityPlayerMP) {
        this.mcServer = minecraftServer;
        this.netManager = networkManager;
        networkManager.setNetHandler(this);
        this.playerEntity = entityPlayerMP;
        entityPlayerMP.playerNetServerHandler = this;
    }

    /**
     * Per-tick packet processing entry point. Reads queued incoming packets and
     * sends keepalive packets if the client has not responded in a while.
     * Also decrements the inventory and creative-mode cooldowns.
     */
    public void handlePackets() {
        // Drain any queued inbound packets first.
        this.netManager.processReadPackets();

        // If more than 20 ticks have passed since we sent the last packet, send a keepalive
        // to confirm the client is still responsive.
        if (this.keepAliveTicksSent - this.keepAliveTicksReceived > 20) {
            this.sendPacket(new Packet0KeepAlive());
        }

        // Tick down the cooldowns so inventory/creative changes can resume.
        if (this.inventoryChangeCooldown > 0) {
            --this.inventoryChangeCooldown;
        }

        if (this.creativeCooldown > 0) {
            --this.creativeCooldown;
        }
    }

    /**
     * Disconnects the player with the given reason. Sends a kick packet, broadcasts
     * a chat message, and removes the player from the player list.
     *
     * @param reason the disconnect reason shown to the client
     */
    public void kickPlayer(String reason) {
        // Close any open screens and notify the client.
        this.playerEntity.closeScreen();
        this.sendPacket(new Packet255KickDisconnect(reason));
        this.netManager.serverShutdown();

        // Broadcast to all remaining players that this player has left.
        this.mcServer.configManager
                .sendPacketToAllPlayers(new Packet3Chat("\u00a7e" + this.playerEntity.username + " left the game."));
        this.mcServer.configManager.playerLoggedOut(this.playerEntity);
        this.connectionClosed = true;
    }

    /**
     * Handles Packet27Position (movement input: strafe/forward/sneak/jump + look angles).
     * Updates the player's movement inputs which are consumed on the next flying packet.
     *
     * @param packet the movement input packet
     */
    public void handlePosition(Packet27Position packet) {
        this.playerEntity.setMovementType(packet.getStrafeMovement(), packet.getForwardMovement(),
                packet.isSneaking(), packet.isInJump(), packet.getPitchRotation(),
                packet.getYawRotation());
    }

    /**
     * Handles Packet10Flying (player position/look updates every tick).
     * This is the core anti-cheat entry point: validates stance, position bounds,
     * movement speed, collision-corrected position, and flying state.
     *
     * @param packet the flying packet from the client
     */
    public void handleFlying(Packet10Flying packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        double oldPosY;
        // After a server-side teleport we ignore the client's position until they
        // report a position matching the teleport target.
        if (!this.positionValid) {
            oldPosY = packet.yPosition - this.lastConfirmedPosY;
            if (packet.xPosition == this.lastConfirmedPosX && oldPosY * oldPosY < 0.01D
                    && packet.zPosition == this.lastConfirmedPosZ) {
                this.positionValid = true;
            }
        }

        if (this.positionValid) {
            double newX;
            double newY;
            double newZ;
            double stanceDelta;
            // === Riding-entity branch ===
            // When the player is mounted, we apply movement deltas in mount-local
            // space and re-anchor the player to the mount's frame.
            if (this.playerEntity.ridingEntity != null) {
                float yaw = this.playerEntity.rotationYaw;
                float pitch = this.playerEntity.rotationPitch;
                this.playerEntity.ridingEntity.updateRiderPosition();
                double oldX = this.playerEntity.posX;
                double oldY = this.playerEntity.posY;
                double oldZ = this.playerEntity.posZ;
                double mountMotionX = 0.0D;
                double mountMotionZ = 0.0D;
                if (packet.rotating) {
                    yaw = packet.yaw;
                    pitch = packet.pitch;
                }

                // (-999, -999, -999) is the magic "no movement" sentinel for vehicle riding.
                if (packet.moving && packet.yPosition == -999.0D
                        && packet.stance == -999.0D) {
                    mountMotionX = packet.xPosition;
                    mountMotionZ = packet.zPosition;
                }

                this.playerEntity.onGround = packet.onGround;
                this.playerEntity.onUpdateEntity(true);
                this.playerEntity.moveEntity(mountMotionX, 0.0D, mountMotionZ);
                this.playerEntity.setPositionAndRotation(oldX, oldY, oldZ, yaw, pitch);
                this.playerEntity.motionX = mountMotionX;
                this.playerEntity.motionZ = mountMotionZ;
                if (this.playerEntity.ridingEntity != null) {
                    worldServer.s_func_12017_b(this.playerEntity.ridingEntity, true);
                }

                if (this.playerEntity.ridingEntity != null) {
                    this.playerEntity.ridingEntity.updateRiderPosition();
                }

                this.mcServer.configManager.serverUpdateMountedMovingPlayer(this.playerEntity);
                this.lastConfirmedPosX = this.playerEntity.posX;
                this.lastConfirmedPosY = this.playerEntity.posY;
                this.lastConfirmedPosZ = this.playerEntity.posZ;
                worldServer.updateEntity(this.playerEntity);
                return;
            }

            // === Sleeping branch ===
            // A sleeping player can't move; ignore all movement input.
            if (this.playerEntity.isPlayerSleeping()) {
                this.playerEntity.onUpdateEntity(true);
                this.playerEntity.setPositionAndRotation(this.lastConfirmedPosX, this.lastConfirmedPosY, this.lastConfirmedPosZ,
                        this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                worldServer.updateEntity(this.playerEntity);
                return;
            }

            // === Normal movement branch ===
            oldPosY = this.playerEntity.posY;
            // Snapshot the current server-side position as the "last confirmed" before
            // applying the client's reported position.
            this.lastConfirmedPosX = this.playerEntity.posX;
            this.lastConfirmedPosY = this.playerEntity.posY;
            this.lastConfirmedPosZ = this.playerEntity.posZ;
            newX = this.playerEntity.posX;
            newY = this.playerEntity.posY;
            newZ = this.playerEntity.posZ;
            float newYaw = this.playerEntity.rotationYaw;
            float newPitch = this.playerEntity.rotationPitch;
            // (-999, -999, -999) standing position with -999 stance means "no position update".
            if (packet.moving && packet.yPosition == -999.0D && packet.stance == -999.0D) {
                packet.moving = false;
            }

            if (packet.moving) {
                newX = packet.xPosition;
                newY = packet.yPosition;
                newZ = packet.zPosition;
                stanceDelta = packet.stance - packet.yPosition;
                // Anti-cheat stance check: stance (eyes - feet) must be between 0.1 and 1.65
                // (the player can't be shorter than 0.1 nor taller than 1.65 blocks).
                if (!this.playerEntity.isPlayerSleeping() && (stanceDelta > 1.65D || stanceDelta < 0.1D)) {
                    this.kickPlayer("Illegal stance");
                    logger.warning(this.playerEntity.username + " had an illegal stance: " + stanceDelta);
                    return;
                }

                // Sanity check: positions outside this range would overflow the world.
                if (Math.abs(packet.xPosition) > 3.2E7D || Math.abs(packet.zPosition) > 3.2E7D) {
                    this.kickPlayer("Illegal position");
                    return;
                }
            }

            if (packet.rotating) {
                newYaw = packet.yaw;
                newPitch = packet.pitch;
            }

            this.playerEntity.onUpdateEntity(true);
            this.playerEntity.ySize = 0.0F;
            this.playerEntity.setPositionAndRotation(this.lastConfirmedPosX, this.lastConfirmedPosY, this.lastConfirmedPosZ, newYaw, newPitch);
            if (!this.positionValid) {
                return;
            }

            // Compute intended movement delta and run the speed check.
            stanceDelta = newX - this.playerEntity.posX;
            double deltaX = newY - this.playerEntity.posY;
            double deltaZ = newZ - this.playerEntity.posZ;
            double moveDistanceSq = stanceDelta * stanceDelta + deltaX * deltaX + deltaZ * deltaZ;
            // Anti-speedhack: a non-creative player can't move more than 10 blocks per tick.
            if (moveDistanceSq > 100.0D && !this.playerEntity.isCreative) {
                logger.warning(this.playerEntity.username + " moved too quickly!");
                this.kickPlayer("You moved too quickly :( (Hacking?)");
                return;
            }

            // Apply the movement. Compare bounding-box-in-air before and after the move
            // to detect movement through solid blocks.
            float collisionMargin = 0.0625F;
            boolean wasInAir = worldServer.getCollidingBoundingBoxes(this.playerEntity,
                    this.playerEntity.boundingBox.copy().getInsetBoundingBox((double) collisionMargin, (double) collisionMargin, (double) collisionMargin))
                    .size() == 0;
            this.playerEntity.moveEntity(stanceDelta, deltaX, deltaZ);
            stanceDelta = newX - this.playerEntity.posX;
            deltaX = newY - this.playerEntity.posY;
            // Snap very small vertical movements to 0 (gravity / jump artifacts).
            if (deltaX > -0.5D || deltaX < 0.5D) {
                deltaX = 0.0D;
            }

            deltaZ = newZ - this.playerEntity.posZ;
            moveDistanceSq = stanceDelta * stanceDelta + deltaX * deltaX + deltaZ * deltaZ;
            boolean movedWrongly = false;
            // If the player's movement was significantly clipped by world geometry,
            // they were trying to phase through a block: revert and warn.
            if (moveDistanceSq > 0.0625D && !this.playerEntity.isPlayerSleeping()) {
                movedWrongly = true;
                logger.warning(this.playerEntity.username + " moved wrongly!");
                System.out.println("Got position " + newX + ", " + newY + ", " + newZ);
                System.out.println("Expected " + this.playerEntity.posX + ", " + this.playerEntity.posY + ", "
                        + this.playerEntity.posZ);
            }

            // Anchor to the player-reported position and re-test for solid collision.
            this.playerEntity.setPositionAndRotation(newX, newY, newZ, newYaw, newPitch);
            boolean isInAir = worldServer.getCollidingBoundingBoxes(this.playerEntity,
                    this.playerEntity.boundingBox.copy().getInsetBoundingBox((double) collisionMargin, (double) collisionMargin, (double) collisionMargin))
                    .size() == 0;
            // If the player was in air before the move but is colliding now (or moved wrongly),
            // the move was invalid: revert to the last confirmed position.
            if (wasInAir && (movedWrongly || !isInAir) && !this.playerEntity.isPlayerSleeping()) {
                this.teleportTo(this.lastConfirmedPosX, this.lastConfirmedPosY, this.lastConfirmedPosZ, newYaw, newPitch);
                return;
            }

            // === Anti-fly check ===
            // Sample a thin box 0.55 blocks below the player's feet: if it doesn't
            // intersect any block, the player must be standing in mid-air.
            AxisAlignedBB feetCheckBB = this.playerEntity.boundingBox.copy()
                    .expand((double) collisionMargin, (double) collisionMargin, (double) collisionMargin).addCoord(0.0D, -0.55D, 0.0D);
            if (!this.mcServer.allowFlight && !worldServer.getIsAnyNonEmptyBlock(feetCheckBB)
                    && !this.playerEntity.isCreative) {
                if (deltaX >= -0.03125D) {
                    ++this.playerInAirTime;
                    // If they have been in air for more than 80 ticks (4 seconds), kick them.
                    if (this.playerInAirTime > 80) {
                        logger.warning(this.playerEntity.username + " was kicked for floating too long!");
                        this.kickPlayer("Flying is not enabled on this server");
                        return;
                    }
                }
            } else {
                this.playerInAirTime = 0;
            }

            this.playerEntity.onGround = packet.onGround;
            this.mcServer.configManager.serverUpdateMountedMovingPlayer(this.playerEntity);
            this.playerEntity.handleFalling(this.playerEntity.posY - oldPosY, packet.onGround);
        }
    }

    /**
     * Force-teleports the player to the given position and notifies the client.
     * Marks the position as invalid so that incoming flying packets are gated until
     * the client confirms the new position.
     *
     * @param x     the new X coordinate
     * @param y     the new Y coordinate
     * @param z     the new Z coordinate
     * @param yaw   the new yaw rotation
     * @param pitch the new pitch rotation
     */
    public void teleportTo(double x, double y, double z, float yaw, float pitch) {
        this.positionValid = false;
        this.lastConfirmedPosX = x;
        this.lastConfirmedPosY = y;
        this.lastConfirmedPosZ = z;
        this.playerEntity.setPositionAndRotation(x, y, z, yaw, pitch);
        this.playerEntity.playerNetServerHandler
                .sendPacket(new Packet13PlayerLookMove(x, y + (double) 1.62F, y, z, yaw, pitch, false));
    }

    /**
     * Handles Packet14BlockDig: player breaking blocks, dropping items, and stopping item use.
     * Applies spawn protection rules (OPs bypass the 16-block spawn radius protection).
     *
     * @param packet the block dig packet (status 0=start, 1=cancel, 2=finish, 3=arm swing, 4=drop item, 5=stop using)
     */
    public void handleBlockDig(Packet14BlockDig packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        if (packet.status == 4) {
            this.playerEntity.dropCurrentItem();
        } else if (packet.status == 5) {
            this.playerEntity.stopUsingItem();
        } else {
        // Determine if the player is exempt from spawn protection (not in the overworld, or is an OP).
        boolean isOp = worldServer.disableSpawnProtection = worldServer.worldProvider.worldType != 0
                || this.mcServer.configManager.isOp(this.playerEntity.username);
        boolean startedDigging = false;
        // Status 0 = start digging, 1 = cancel digging, 2 = finish digging, 3 = arm swing.
        if (packet.status == 0) {
            startedDigging = true;
        }

        if (packet.status == 2) {
            startedDigging = true;
        }

        int x = packet.xPosition;
        int y = packet.yPosition;
        int z = packet.zPosition;
        if (startedDigging) {
            // Reject dig attempts from more than 6 blocks away.
            double distSq = this.playerEntity.posX - ((double) x + 0.5D);
            double d10 = this.playerEntity.posY - ((double) y + 0.5D);
            double d12 = this.playerEntity.posZ - ((double) z + 0.5D);
            double distanceSq = distSq * distSq + d10 * d10 + d12 * d12;
            if (distanceSq > 36.0D) {
                return;
            }
        }

            ChunkCoordinates spawnPoint = worldServer.getSpawnPoint();
            int spawnDistX = (int) MathHelper.abs((float) (x - spawnPoint.posX));
            int spawnDistZ = (int) MathHelper.abs((float) (z - spawnPoint.posZ));
            if (spawnDistX > spawnDistZ) {
                spawnDistZ = spawnDistX;
            }

            if (packet.status == 0) {
                if (spawnDistZ <= 16 && !isOp) {
                    this.playerEntity.playerNetServerHandler
                            .sendPacket(new Packet53BlockChange(x, y, z, worldServer));
                } else {
                    this.playerEntity.itemInWorldManager.blockClicked(x, y, z, packet.face);
                }
            } else if (packet.status == 2) {
                this.playerEntity.itemInWorldManager.blockRemoving(x, y, z);
                if (worldServer.getBlockId(x, y, z) != 0) {
                    this.playerEntity.playerNetServerHandler
                            .sendPacket(new Packet53BlockChange(x, y, z, worldServer));
                }
            } else if (packet.status == 3) {
                double d11 = this.playerEntity.posX - ((double) x + 0.5D);
                double d13 = this.playerEntity.posY - ((double) y + 0.5D);
                double d15 = this.playerEntity.posZ - ((double) z + 0.5D);
                double d17 = d11 * d11 + d13 * d13 + d15 * d15;
                if (d17 < 256.0D) {
                    this.playerEntity.playerNetServerHandler
                            .sendPacket(new Packet53BlockChange(x, y, z, worldServer));
                }
            }

            worldServer.disableSpawnProtection = false;
        }
    }

    /**
     * Handles Packet15Place: player right-clicking blocks (placing items, using items, activating blocks).
     * Enforces the 64-block reach limit and spawn protection radius.
     *
     * @param packet the place packet containing target block coordinates and cursor offsets
     */
    public void handlePlace(Packet15Place packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        ItemStack currentItem = this.playerEntity.inventory.getCurrentItem();
        boolean sentBlockChange = false;

        int x = packet.xPosition;
        int y = packet.yPosition;
        int z = packet.zPosition;
        int direction = packet.direction;
        float xWithinFace = packet.xWithinFace;
        float yWithinFace = packet.yWithinFace;
        float zWithinFace = packet.zWithinFace;	
        byte shift = packet.shift;
                
        // Determine if the player is exempt from spawn protection.
        boolean isOp = worldServer.disableSpawnProtection = worldServer.worldProvider.worldType != 0
                || this.mcServer.configManager.isOp(this.playerEntity.username);
        // Direction 255 is the "use item in air" packet (right-click with empty hand direction).
        if (packet.direction == 255) {
            if (currentItem == null) {
                return;
            }

            this.playerEntity.itemInWorldManager.itemUsed(this.playerEntity, worldServer, currentItem);
        } else {
            ChunkCoordinates spawnPoint = worldServer.getSpawnPoint();
            int spawnDistX = (int) MathHelper.abs((float) (x - spawnPoint.posX));
            int spawnDistZ = (int) MathHelper.abs((float) (z - spawnPoint.posZ));
            if (spawnDistX > spawnDistZ) {
                spawnDistZ = spawnDistX;
            }

            if (this.positionValid && this.playerEntity.getDistanceSq((double) x + 0.5D, (double) y + 0.5D,
                    (double) z + 0.5D) < 64.0D && (spawnDistZ > 16 || isOp)) {
                this.playerEntity.itemInWorldManager.activeBlockOrUseItem(this.playerEntity, worldServer, currentItem,
                        x, y, z, direction, xWithinFace, yWithinFace, zWithinFace, shift);
            }

            sentBlockChange = true;
        }
        
        if (sentBlockChange) {
            this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, worldServer));
            if (direction == 0) {
                --y;
            }

            if (direction == 1) {
                ++y;
            }

            if (direction == 2) {
                --z;
            }

            if (direction == 3) {
                ++z;
            }

            if (direction == 4) {
                --x;
            }

            if (direction == 5) {
                ++x;
            }

            this.playerEntity.playerNetServerHandler.sendPacket(new Packet53BlockChange(x, y, z, worldServer));
        }

        currentItem = this.playerEntity.inventory.getCurrentItem();
        if (currentItem != null && currentItem.stackSize == 0) {
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
            currentItem = null;
        }

        if (currentItem == null || currentItem.getMaxItemUseDuration() == 0) {
            this.playerEntity.isChangingQuantityOnly = true;
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack
                .copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
            Slot currentSlot = this.playerEntity.craftingInventory.findCurrentItem(this.playerEntity.inventory,
                this.playerEntity.inventory.currentItem);
            this.playerEntity.craftingInventory.updateCraftingResults();
            this.playerEntity.isChangingQuantityOnly = false;
            if (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), packet.itemStack)) {
                this.sendPacket(new Packet103SetSlot(this.playerEntity.craftingInventory.windowId, currentSlot.id,
                        this.playerEntity.inventory.getCurrentItem()));
            }
        }

        worldServer.disableSpawnProtection = false;
    }

    /**
     * Called when the network layer reports an I/O error or other unrecoverable error.
     * Logs the disconnect and removes the player from the player list.
     *
     * @param message description of the error
     * @param params  additional formatting parameters
     */
    public void handleErrorMessage(String message, Object[] params) {
        logger.info(this.playerEntity.username + " lost connection: " + message);
        this.mcServer.configManager
                .sendPacketToAllPlayers(new Packet3Chat("\u00a7e" + this.playerEntity.username + " left the game."));
        this.mcServer.configManager.playerLoggedOut(this.playerEntity);
        this.connectionClosed = true;
    }

    /**
     * Catch-all for packets that the server does not have a specific handler for.
     * Logs a warning and kicks the player with a "Protocol error" message.
     *
     * @param packet the unexpected packet
     */
    public void registerPacket(Packet packet) {
        logger.warning(this.getClass() + " wasn\'t prepared to deal with a " + packet.getClass());
        this.kickPlayer("Protocol error, unexpected packet");
    }

    /**
     * Queues a packet for sending to the client and updates the keepalive timestamp
     * to mark that the client just received something from the server.
     *
     * @param packet the packet to send
     */
    public void sendPacket(Packet packet) {
        this.netManager.addToSendQueue(packet);
        this.keepAliveTicksReceived = this.keepAliveTicksSent;
    }

    /**
     * Handles Packet16BlockItemSwitch: player switching the held item slot (hotbar scroll or number key).
     * Validates the slot index is within bounds.
     *
     * @param packet the item switch packet
     */
    public void handleBlockItemSwitch(Packet16BlockItemSwitch packet) {
        if (packet.id >= 0 && packet.id <= InventoryPlayer.mainInventoryWidth()) {
            this.playerEntity.inventory.currentItem = packet.id;
        } else {
            logger.warning(this.playerEntity.username + " tried to set an invalid carried item");
        }
    }

    /**
     * Handles Packet3Chat: player chat messages. Validates message length, allowed
     * characters, and dispatches to either command handling or public broadcast.
     *
     * @param packet the chat packet containing the message
     */
    public void handleChat(Packet3Chat packet) {
        String message = packet.message;
        if (message.length() > 100) {
            this.kickPlayer("Chat message too long");
        } else {
            message = message.trim();

            for (int i = 0; i < message.length(); ++i) {
                if (ChatAllowedCharacters.allowedCharacters.indexOf(message.charAt(i)) < 0) {
                    this.kickPlayer("Illegal characters in chat");
                    return;
                }
            }

            if (message.startsWith("/")) {
                this.handleSlashCommand(message);
            } else {
                message = "<" + this.playerEntity.username + "> " + message;
                logger.info(message);
                this.mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat(message));
            }
        }
    }

    private void handleSlashCommand(String command) {
        if (command.toLowerCase().startsWith("/me ")) {
            command = "* " + this.playerEntity.username + " " + command.substring(command.indexOf(" ")).trim();
            logger.info(command);
            this.mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat(command));
        } else if (command.toLowerCase().startsWith("/kill")) {
            this.playerEntity.attackEntityFrom((Entity) null, 1000);
        } else if (command.toLowerCase().startsWith("/tell ")) {
            String[] parts = command.split(" ");
            if (parts.length >= 3) {
                command = command.substring(command.indexOf(" ")).trim();
                command = command.substring(command.indexOf(" ")).trim();
                command = "\u00a77" + this.playerEntity.username + " whispers " + command;
                logger.info(command + " to " + parts[1]);
                if (!this.mcServer.configManager.sendPacketToPlayer(parts[1], new Packet3Chat(command))) {
                    this.sendPacket(new Packet3Chat("\u00a7cThere\'s no player by that name online."));
                }
            }
        } else {
            String commandName;
            if (this.mcServer.configManager.isOp(this.playerEntity.username)) {
                commandName = command.substring(1);
                logger.info(this.playerEntity.username + " issued server command: " + commandName);
                this.mcServer.addCommand(commandName, this);
            } else {
                commandName = command.substring(1);
                logger.info(this.playerEntity.username + " tried command: " + commandName);
            }
        }
    }

    /**
     * Handles Packet18Animation: arm swing / damage animation. Currently only the
     * swing-arm animation (state 1) is honored server-side.
     *
     * @param packet the animation packet
     */
    public void handleAnimation(Packet18Animation packet) {
        if (packet.animate == 1) {
            this.playerEntity.swingItem();
        }
    }

    /**
     * Handles Packet19EntityAction: player state changes (sneak, wake up, sprint).
     * Waking up from a bed resets the position-valid flag to prevent desync.
     *
     * @param packet the entity action packet (state 1=sneak, 2=stop sneak, 3=wake up, 4=sprint, 5=stop sprint)
     */
    public void handleEntityAction(Packet19EntityAction packet) {
        if (packet.state == 1) {
            this.playerEntity.setSneaking(true);
        } else if (packet.state == 2) {
            this.playerEntity.setSneaking(false);
        } else if (packet.state == 3) {
            this.playerEntity.wakeUpPlayer(false, true, true);
            this.positionValid = false;
        } else if (packet.state == 4) {
            this.playerEntity.setSprinting(true);
        } else if (packet.state == 5) {
            this.playerEntity.setSprinting(false);
        } 
    }

    /**
     * Handles Packet255KickDisconnect: client-initiated disconnect.
     * Tears down the network connection.
     *
     * @param packet the kick packet (normally unused)
     */
    public void handleKickDisconnect(Packet255KickDisconnect packet) {
        this.netManager.networkShutdown("disconnect.quitting", new Object[0]);
    }

    /**
     * Returns the count of chunk data packets sent to this client. Used by the
     * server console for diagnostics.
     *
     * @return number of chunk-data packets sent to this client
     */
    public int getNumChunkDataPackets() {
        return this.netManager.getNumChunkDataPackets();
    }

    /**
     * Sends a server-side chat message to this player as a grey ("info") message.
     * Used by command implementations to respond to players.
     *
     * @param message the message text to send
     */
    public void log(String message) {
        this.sendPacket(new Packet3Chat("\u00a77" + message));
    }

    /**
     * Returns the username of the player associated with this handler.
     * Part of the ICommandListener interface.
     *
     * @return the player's username
     */
    public String getUsername() {
        return this.playerEntity.username;
    }

    /**
     * Handles Packet7UseEntity: attacking or interacting with another entity.
     * Validates line-of-sight and range (must be within 6 blocks).
     *
     * @param packet the use-entity packet
     */
    public void handleUseEntity(Packet7UseEntity packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        Entity entity = worldServer.getEntityByID(packet.targetEntity);
        if (entity != null && this.playerEntity.canEntityBeSeen(entity)
                && this.playerEntity.getDistanceSqToEntity(entity) < 36.0D) {
            if (packet.isLeftClick == 0) {
                this.playerEntity.useCurrentItemOnEntity(entity);
            } else if (packet.isLeftClick == 1) {
                this.playerEntity.attackTargetEntityWithCurrentItem(entity);
            }
        }
    }

    /**
     * Handles Packet9Respawn: client request to respawn after death.
     * If the player is dead, recreates their entity in the overworld.
     *
     * @param packet the respawn packet
     */
    public void handleRespawn(Packet9Respawn packet) {
        if (this.playerEntity.health <= 0) {
            this.playerEntity = this.mcServer.configManager.recreatePlayerEntity(this.playerEntity, 0);
        }
    }

    /**
     * Handles Packet101CloseWindow: player closing an inventory window.
     *
     * @param packet the close-window packet
     */
    public void handleCloseWindow(Packet101CloseWindow packet) {
        this.playerEntity.closeCraftingGui();
    }

    /**
     * Handles Packet102WindowClick: player clicking a slot in an inventory window.
     * Manages the two-phase click protocol (server validates, then client confirms
     * via Packet106Transaction). If the server detects a mismatch, the inventory is
     * reverted to the pre-click state.
     *
     * @param packet the window click packet
     */
    public void handleWindowClick(Packet102WindowClick packet) {
        if (this.playerEntity.craftingInventory.windowId == packet.window_Id
                && this.playerEntity.craftingInventory.getCanCraft(this.playerEntity)) {
            ItemStack returnedItem = this.playerEntity.craftingInventory.slotClick(packet.inventorySlot,
                    packet.mouseClick, packet.packetBoolean, this.playerEntity);
            if (ItemStack.areItemStacksEqual(packet.itemStack, returnedItem)) {
                this.playerEntity.playerNetServerHandler.sendPacket(
                        new Packet106Transaction(packet.window_Id, packet.action, true));
                this.playerEntity.isChangingQuantityOnly = true;
                this.playerEntity.craftingInventory.updateCraftingResults();
                this.playerEntity.updateHeldItem();
                this.playerEntity.isChangingQuantityOnly = false;
            } else {
                this.pendingTransactionByWindow.put(this.playerEntity.craftingInventory.windowId, packet.action);
                this.playerEntity.playerNetServerHandler.sendPacket(
                        new Packet106Transaction(packet.window_Id, packet.action, false));
                this.playerEntity.craftingInventory.setCanCraft(this.playerEntity, false);
                List<ItemStack> slotContents = new ArrayList<ItemStack>();

                for (int i = 0; i < this.playerEntity.craftingInventory.inventorySlots.size(); ++i) {
                    slotContents.add(((Slot) this.playerEntity.craftingInventory.inventorySlots.get(i)).getStack());
                }

                this.playerEntity.updateCraftingInventory(this.playerEntity.craftingInventory, slotContents);
            }
        }
    }

    /**
     * Handles Packet107CreativeSetSlot: creative-mode inventory management.
     * Validates slot index and item properties, then places the item in the
     * player's inventory or drops it if the slot is invalid.
     *
     * @param packet the creative slot packet
     */
    public void handleCreativeSetSlot(Packet107CreativeSetSlot packet) {
        if (this.playerEntity.isCreative) {
            boolean isOffhandSlot = packet.slot < 0;
            ItemStack itemStack = packet.itemStack;
            boolean isMainInventory = packet.slot >= 36 && packet.slot < 36 + InventoryPlayer.mainInventoryWidth();
            boolean isValidItem = itemStack == null || itemStack.itemID < Item.itemsList.length && itemStack.itemID >= 0 && Item.itemsList[itemStack.itemID] != null;
            boolean isValidStack = itemStack == null || itemStack.getItemDamage() >= 0 && itemStack.getItemDamage() >= 0 && itemStack.stackSize <= 64 && itemStack.stackSize > 0;
            if (isMainInventory && isValidItem && isValidStack) {
                if (itemStack == null) {
                    this.playerEntity.inventorySlots.putStackInSlot(packet.slot, (ItemStack) null);
                } else {
                    this.playerEntity.inventorySlots.putStackInSlot(packet.slot, itemStack);
                }

                this.playerEntity.inventorySlots.setCanCraft(this.playerEntity, true);
            } else if (isOffhandSlot && isValidItem && isValidStack && this.creativeCooldown < 200) {
                this.creativeCooldown += 20;
                EntityItem droppedItem = this.playerEntity.dropPlayerItem(itemStack);
                if (droppedItem != null) {
                    droppedItem.makeOld();
                }
            }
        }
    }

    /**
     * Handles Packet106Transaction: client acknowledgment of a server-initiated
     * inventory transaction. If the transaction was previously rejected by the server
     * and is now confirmed by the client, the crafting inventory is unlocked.
     *
     * @param packet the transaction packet
     */
    public void handleTransaction(Packet106Transaction packet) {
        Short pendingAction = (Short) this.pendingTransactionByWindow.get(this.playerEntity.craftingInventory.windowId);
        if (pendingAction != null && packet.shortWindowId == pendingAction.shortValue()
                && this.playerEntity.craftingInventory.windowId == packet.windowId
                && !this.playerEntity.craftingInventory.getCanCraft(this.playerEntity)) {
            this.playerEntity.craftingInventory.setCanCraft(this.playerEntity, true);
        }
    }

    /**
     * Handles Packet91UpdateCommandBlock: player editing a command block's command text.
     * Validates that the target block is a command block, then updates it.
     *
     * @param packet the command-block update packet
     */
    public void handleUpdateCommandBlock(Packet91UpdateCommandBlock packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        
        int x = packet.x; 
        int y = packet.y;
        int z = packet.z;
        
        if (worldServer.blockExists(x, y, z)) {
            TileEntity tileEntity = worldServer.getBlockTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCommandBlock) {
                TileEntityCommandBlock commandBlock = (TileEntityCommandBlock) tileEntity;
                commandBlock.command = packet.command;
                
                commandBlock.onInventoryChanged();
                worldServer.markBlockNeedsUpdate(x, y, z);
            }
        }
    }

    /**
     * Handles Packet130UpdateSign: player editing a sign's four text lines.
     * Validates that the sign is editable and that each line contains only allowed characters.
     *
     * @param packet the sign update packet
     */
    public void handleUpdateSign(Packet130UpdateSign packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        if (worldServer.blockExists(packet.xPosition, packet.yPosition,
                packet.zPosition)) {
            TileEntity tileEntity = worldServer.getBlockTileEntity(packet.xPosition,
                    packet.yPosition, packet.zPosition);
            if (tileEntity instanceof TileEntitySign) {
                TileEntitySign sign = (TileEntitySign) tileEntity;
                if (!sign.getIsEditAble()) {
                    this.mcServer.logWarning(
                            "Player " + this.playerEntity.username + " just tried to change non-editable sign");
                    return;
                }
            }

            int lineIdx;
            int charIdx;
            for (lineIdx = 0; lineIdx < 4; ++lineIdx) {
                boolean lineValid = true;
                if (packet.signLines[lineIdx].length() > 15) {
                    lineValid = false;
                } else {
                    for (charIdx = 0; charIdx < packet.signLines[lineIdx].length(); ++charIdx) {
                        if (ChatAllowedCharacters.allowedCharacters
                                .indexOf(packet.signLines[lineIdx].charAt(charIdx)) < 0) {
                            lineValid = false;
                        }
                    }
                }

                if (!lineValid) {
                    packet.signLines[lineIdx] = "!?";
                }
            }

            if (tileEntity instanceof TileEntitySign) {
                int signX = packet.xPosition;
                int signY = packet.yPosition;
                int signZ = packet.zPosition;
                TileEntitySign signTile = (TileEntitySign) tileEntity;

                for (int i = 0; i < 4; ++i) {
                    signTile.signText[i] = packet.signLines[i];
                }

                signTile.s_func_32001_a(false);
                signTile.onInventoryChanged();
                worldServer.markBlockNeedsUpdate(signX, signY, signZ);
            }
        }
    }

    /**
     * Handles Packet93UpdateAnimalName: player naming a creature.
     * Validates that the entity exists and is a creature before applying the name.
     *
     * @param packet the animal-name update packet
     */
    public void handleUpdateAnimalName(Packet93UpdateAnimalName packet) {
        WorldServer worldServer = this.mcServer.getWorldManager(this.playerEntity.dimension);
        Entity entity = worldServer.getEntityByID(packet.entityId);
        if (entity == null || !(entity instanceof EntityCreature)) {
            System.out.println("Received name " + packet.name + " for non existing creature " + packet.entityId);
        } else {
            ((EntityCreature) entity).setName(packet.name);
        }
    }

    /**
     * Handles Packet250CustomPayload: custom mod-specific channel payloads.
     * Supports the "MC|TrSel" channel for villager-trader recipe selection.
     *
     * @param packet the custom payload packet
     */
    public void handleCustomPayload(Packet250CustomPayload packet) {
        if ("MC|TrSel".equals(packet.channel)) {
            try {
                DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(packet.data));
                int recipeIndex = dataInput.readInt();
                Container container = this.playerEntity.craftingInventory;

                if (container instanceof ContainerTrader) {
                    ((ContainerTrader) container).setCurrentRecipeIndex(recipeIndex);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } 
    }

    /**
     * Handles Packet99SetCreativeMode: server-initiated creative/survival mode toggle.
     * Used by the /gamemode command to switch a player's mode without a respawn.
     *
     * @param packet the creative mode toggle packet
     */
    public void handleSetCreative(Packet99SetCreativeMode packet) {
        this.playerEntity.isCreative = packet.isCreative;
    }

    /**
     * Handles Packet97SetInventorySlot: server-side direct inventory slot override.
     * Used by commands like /give to place items directly in the player's inventory.
     *
     * @param packet the set-inventory-slot packet
     */
    public void handleSetInventorySlot(Packet97SetInventorySlot packet) {
        ItemStack itemStack = new ItemStack(packet.itemID, packet.itemAmount, packet.itemDamage);
        this.playerEntity.inventory.setInventorySlotContents(packet.slot, itemStack);
        System.out.println("Put " + itemStack + " into slot " + packet.slot);
    }

    /**
     * Indicates that this is the server-side handler. Used to differentiate from
     * the client-side NetClientHandler when shared code needs to distinguish them.
     *
     * @return true (always, since this is the server-side handler)
     */
    public boolean isServerHandler() {
        return true;
    }
}
