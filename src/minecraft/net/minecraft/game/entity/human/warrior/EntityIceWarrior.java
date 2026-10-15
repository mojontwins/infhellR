package net.minecraft.game.entity.human.warrior;

import java.util.List;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.animal.EntityAnimal;
import net.minecraft.game.entity.human.base.EntityArmoredHumanBase;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

/**
 * Levelled ice-themed melee humanoid. It hunts visible survival players and
 * also preys on nearby animals.
 */
public class EntityIceWarrior extends EntityArmoredHumanBase implements IMobWithLevel, ISentient {
	public EntityIceWarrior(World world) {
		super(world);
		this.texture = "/mob/ice.png";
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		super.configureAttributesBasedOnLevel();

		// Ice warriors wield blunt weapons instead of the base class's swords.
		switch(this.getLvl()) {
		case 0:
			this.setHeldItem(new ItemStack(Item.battleWood, 1));
			break;
		case 1:
			this.setHeldItem(new ItemStack(Item.maceSteel, 1));
			break;
		case 2:
			this.setHeldItem(new ItemStack(Item.hammerSteel, 1));
			break;
		case 3:
			this.setHeldItem(new ItemStack(Item.maceGold, 1));
			break;
		case 4:
			this.setHeldItem(new ItemStack(Item.maceDiamond, 1));
			break;
		default:
			this.setHeldItem(new ItemStack(Item.axeGold, 1));
			break;
		}
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		if(player != null && !player.isCreative && this.canEntityBeSeen(player)) {
			return player;
		}

		List<Entity> nearbyAnimals = this.worldObj.getEntitiesWithinAABB(EntityAnimal.class, this.boundingBox.expand(16.0D, 4.0D, 16.0D));
		for(Entity entity : nearbyAnimals) {
			EntityLiving animal = (EntityLiving)entity;
			if(this.isValidTarget(animal)) {
				return animal;
			}
		}

		return null;
	}

	/** A valid animal target must be alive and overlap this mob vertically. */
	public boolean isValidTarget(EntityLiving target) {
		if(target == null) return false;
		if(target == this) return false;
		if(!target.isEntityAlive()) return false;
		if(target.boundingBox.minY >= this.boundingBox.maxY || target.boundingBox.maxY <= this.boundingBox.minY) return false;
		return true;
	}

	@Override
	public void setArmorTexture(int level) {
		switch(level) {
		case 1:
			this.armorTexture = "/armor/chain";
			break;
		case 2:
			this.armorTexture = "/armor/ice_iron";
			break;
		case 3:
			this.armorTexture = "/armor/ice_gold";
			break;
		case 4:
			this.armorTexture = "/armor/ice_diamond";
			break;
		default:
			this.armorTexture = "/armor/ice_cloth";
		}
	}

	@Override
	protected String getHurtSound() {
		return "mob.ice.hurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.ice.hurt";
	}

	@Override
	protected String getLivingSound() {
		return "mob.ice.idle";
	}
}
