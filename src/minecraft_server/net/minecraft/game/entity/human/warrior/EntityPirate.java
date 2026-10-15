package net.minecraft.game.entity.human.warrior;

import java.util.List;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.human.EntityPirateArcher;
import net.minecraft.game.entity.human.EntityPirateBoss;
import net.minecraft.game.entity.human.base.EntityArmoredHumanBase;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Levelled pirate swordsman. Attacks disguised-aware: it ignores players who
 * are dressed as pirates until it (or another pirate nearby) has been provoked,
 * in which case every pirate within 32 blocks turns hostile.
 */
public class EntityPirate extends EntityArmoredHumanBase implements IMobWithLevel, ISentient {
	protected boolean angryAtPlayer = false;

	public EntityPirate(World world) {
		super(world);
		this.armorIndex = 4;
		this.attackStrength = 8;
		this.texture = "/mob/pirate1.png";
	}

	protected int getMaxTextureVariations() {
		return 3;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte)(1 + this.rand.nextInt(this.getMaxTextureVariations())));
	}

	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(Datawatchers.DW_TYPE, variation);
	}

	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE);
	}

	@Override
	public String getEntityTexture() {
		if(this.getMaxTextureVariations() > 1) {
			return "/mob/pirate" + this.getTextureVariation() + ".png";
		} else {
			return this.texture;
		}
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		super.configureAttributesBasedOnLevel();

		// Pirates wield axes instead of the base class's swords.
		switch(this.getLvl()) {
		case 0:
			this.setHeldItem(new ItemStack(Item.axeWood, 1));
			break;
		case 1:
			this.setHeldItem(new ItemStack(Item.axeStone, 1));
			break;
		case 2:
			this.setHeldItem(new ItemStack(Item.axeSteel, 1));
			break;
		case 3:
			this.setHeldItem(new ItemStack(Item.axeGold, 1));
			break;
		case 4:
			this.setHeldItem(new ItemStack(Item.axeDiamond, 1));
			break;
		default:
			this.setHeldItem(new ItemStack(Item.axeGold, 1));
			break;
		}
	}

	@Override
	public int getFullHealth() {
		return 25 + this.armorIndex * 4;
	}

	@Override
	public void onLivingUpdate() {
		// Occasional cosmetic smoke.
		if(this.rand.nextInt(1000) == 1) {
			for(int i = 0; i < 10; ++i) {
				this.worldObj.spawnParticle("smoke", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D + (double)this.rand.nextFloat(), this.posZ + (double)this.rand.nextFloat() - 0.5D, 0.0D, 0.0D, 0.0D);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		if(this.rand.nextInt(10) == 0) {
			for(int i = 0; i < 10; ++i) {
				this.worldObj.spawnParticle("splash", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D + (double)this.rand.nextFloat(), this.posZ + (double)this.rand.nextFloat() - 0.5D, 0.0D, 0.0D, 0.0D);
			}
		}

		// Being hit by a player angers the whole local pirate crew.
		if(source instanceof EntityPlayer) {
			List<Entity> nearbyEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));
			for(Entity other : nearbyEntities) {
				if(other instanceof EntityPirate) {
					((EntityPirate)other).angryAtPlayer = true;
				} else if(other instanceof EntityPirateArcher) {
					((EntityPirateArcher)other).angryAtPlayer = true;
				} else if(other instanceof EntityPirateBoss) {
					((EntityPirateBoss)other).angryAtPlayer = true;
				}
			}

			this.angryAtPlayer = true;
		}

		return super.attackEntityFrom(source, damage);
	}

	@Override
	public boolean getCanSpawnHere() {
		int blockX = MathHelper.floor_double(this.posX);
		int blockY = MathHelper.floor_double(this.boundingBox.minY);
		int blockZ = MathHelper.floor_double(this.posZ);

		for(int y = blockY - 1; y <= blockY; ++y) {
			Material material = this.worldObj.getBlockMaterial(blockX, y, blockZ);
			int blockId = this.worldObj.getBlockId(blockX, y, blockZ);

			if(this.posY > 60.0D) {
				if(material == Material.wood || material == Material.grass || material == Material.ground || blockId == Block.stoneBricks.blockID) {
					return true;
				}
			}

			if(this.rand.nextInt(4) == 1 && super.getCanSpawnHere()) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected Entity findPlayerToAttack() {
		// If something hit us, retaliate before looking for a fresh target.
		if(this.lastAttackingEntity != null && this.lastAttackingEntity != this.currentTarget) {
			return this.lastAttackingEntity;
		}

		EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		if(player != null && !player.isCreative && this.canEntityBeSeen(player) && (!player.dressedAsAPirate() || this.angryAtPlayer)) {
			return player;
		}

		return null;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		tag.setByte("TextureVariation", this.getTextureVariation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.angryAtPlayer = tag.getBoolean("AngryAtPlayer");
		this.configureAttributesBasedOnLevel();

		byte textureVariation = 1;
		if(tag.hasKey("TextureVariation")) {
			textureVariation = tag.getByte("TextureVariation");
		} else {
			System.out.println("Importing old level, overrode texture variation for " + this.getClass() + " with default.");
		}
		this.setTextureVariation(textureVariation);
	}

	@Override
	public void onDeath(Entity source) {
		if(source instanceof EntityPlayer) {
			((EntityPlayer)source).triggerAchievement(AchievementList.pirateKill);
		}

		super.onDeath(source);
	}
}
