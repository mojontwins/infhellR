package net.minecraft.game.entity.human.base;

import net.minecraft.game.entity.EntityMobWithLevel;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Base for the levelled, weapon-carrying humanoids drawn with the human model
 * and an armour overlay (pirates, ice warriors). It owns only the overlay
 * contract and the level hook; the swing animation and held item now live on
 * {@link net.minecraft.game.entity.EntityHumanoid}, and the level-dependent
 * armour statistics live on {@link EntityArmoredHumanBase}.
 */
public class EntityHumanWarrior extends EntityMobWithLevel implements IHostileToPlayer {
	/** Number of armour layers currently drawn by the renderer (0 = none, 4 = full). */
	public int armor = 4;

	/** Texture prefix used for the armour overlay, e.g. {@code "/armor/iron"}. */
	public String armorTexture = "/armor/cloth";

	/** Level the mob was last configured for; used to detect SMP level sync. */
	public int origLvl = 0xff;

	/** Whether this humanoid may be removed by the natural despawn timer. */
	public boolean despawn = false;

	/** Whether the model draws its "ears" layer. */
	public boolean renderEars = false;

	public EntityHumanWarrior(World world) {
		super(world);
		this.attackStrength = 4;
	}

	@Override
	public void setLevel(int level) {
		this.setLvl(level);
		this.configureAttributesBasedOnLevel();
	}

	/**
	 * Hook fired whenever the mob's level changes. This base has no
	 * level-dependent behaviour; armoured humans override it to choose their
	 * equipment and recompute their stats.
	 */
	public void configureAttributesBasedOnLevel() {
	}

	@Override
	public void onUpdate() {
		// In SMP the level arrives after construction, so reconfigure when it differs.
		if(this.origLvl != this.getLvl()) {
			this.configureAttributesBasedOnLevel();
		}

		super.onUpdate();
	}

	/**
	 * Hook fired every living tick to advance the armour overlay. This base
	 * never loses armour; armoured humans override it.
	 */
	public void updateArmor() {
	}

	@Override
	public void onLivingUpdate() {
		this.updateArmor();
		super.onLivingUpdate();
	}

	@Override
	protected String getLivingSound() {
		return "mob.zombie";
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	@Override
	public void dropFewItems() {
		int level = this.getLvl();

		// Every humanoid drops a few apples.
		int appleCount = this.rand.nextInt(3);
		for(int i = 0; i < appleCount; ++i) {
			this.dropItem(Item.appleRed.shiftedIndex, 1);
		}

		// Higher-level humanoids also drop the ore matching their tier.
		int oreId = -1;
		if(level == 2) {
			oreId = Item.ingotIron.shiftedIndex;
		} else if(level == 3) {
			oreId = Item.ingotGold.shiftedIndex;
		} else if(level == 4) {
			oreId = Item.diamond.shiftedIndex;
		}

		if(oreId >= 0) {
			int oreCount = this.rand.nextInt(3);
			for(int i = 0; i < oreCount; ++i) {
				this.dropItem(oreId, 1);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setInteger("level", this.getLvl());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.setLvl(tag.getInteger("level"));
		this.configureAttributesBasedOnLevel();
	}

	@Override
	protected void despawnEntity() {
		// Stay around until death unless natural despawn was explicitly enabled.
		if(this.despawn || this.isDead) {
			super.despawnEntity();
		}
	}

	@Override
	protected int getDropItemId() {
		return Item.feather.shiftedIndex;
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();

		// Swing whenever a target comes within melee reach.
		if(!this.isSwinging && this.entityToAttack != null && this.entityToAttack.getDistanceToEntity(this) < 3.0F) {
			this.swingItem();
		}
		this.updateSwingAnimation();

		// Forget a target once it dies.
		if(this.entityToAttack != null && !this.entityToAttack.isEntityAlive()) {
			this.entityToAttack = null;
		}
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox)
				&& this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0
				&& !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
}
