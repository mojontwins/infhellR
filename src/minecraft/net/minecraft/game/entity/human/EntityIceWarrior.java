package net.minecraft.game.entity.human;

import java.util.Iterator;
import java.util.List;


import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.animal.EntityAnimal;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

public class EntityIceWarrior extends EntityHumanBase implements IMobWithLevel, ISentient {

	public EntityIceWarrior(World world) {
		super(world);
		this.texture = "/mob/ice.png";
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		super.configureAttributesBasedOnLevel();
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
		EntityPlayer entityPlayer1 = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		if (
				entityPlayer1 != null && 
				!entityPlayer1.isCreative && 
				this.canEntityBeSeen(entityPlayer1)
		) {
			return entityPlayer1;
		}
		
		List<Entity> list = this.worldObj.getEntitiesWithinAABB(EntityAnimal.class, this.boundingBox.expand(16.0D, 4.0D, 16.0D));
		Iterator<Entity> iterator = list.iterator();		
		while(iterator.hasNext()) {
			EntityLiving entityLiving = (EntityLiving)iterator.next();
			if(this.isValidTarget(entityLiving)) return entityLiving;
		}
		
		return null;
	}
	
	public boolean isValidTarget(EntityLiving entityLiving) {
		if(entityLiving == null) return false;
		
		if(entityLiving == this) return false;
		
		if(!entityLiving.isEntityAlive()) return false;
		
		if(entityLiving.boundingBox.minY >= this.boundingBox.maxY || entityLiving.boundingBox.maxY <= this.boundingBox.minY) return false;
		
		return true;
	}
	
	public void setArmorTexture(int lvl) {
		switch(lvl) {
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
