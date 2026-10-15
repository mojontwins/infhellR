package net.minecraft.game.entity.human.base;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

/**
 * Base for armoured, levelled humanoids.
 *
 * <p>The mob's weapon and armour texture scale with its level, and its armour
 * overlay degrades as it loses health, so tougher (higher {@link #armorIndex})
 * humans keep more visible armour for longer.</p>
 */
public class EntityArmoredHumanBase extends EntityHumanWarrior {
	/** Heavier armour raises both the health pool and the number of armour layers. */
	public int armorIndex = 1;

	public EntityArmoredHumanBase(World world) {
		super(world);
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		this.setArmorTexture(this.getLvl());

		// Pick the weapon matching the mob's level.
		switch(this.getLvl()) {
		case 0:
			this.setHeldItem(new ItemStack(Item.swordWood, 1));
			break;
		case 1:
			this.setHeldItem(new ItemStack(Item.swordStone, 1));
			break;
		case 2:
			this.setHeldItem(new ItemStack(Item.swordSteel, 1));
			break;
		case 3:
			this.setHeldItem(new ItemStack(Item.swordGold, 1));
			break;
		case 4:
			this.setHeldItem(new ItemStack(Item.swordDiamond, 1));
			break;
		default:
			this.setHeldItem(new ItemStack(Item.swordGold, 1));
			break;
		}

		// Scale stats with the world difficulty and the mob's level.
		if(this.worldObj != null) {
			this.armorIndex += this.getLvl() + this.worldObj.difficultySetting;

			if(this.worldObj.difficultySetting == 2) {
				++this.attackStrength;
			}

			if(this.worldObj.difficultySetting == 3) {
				this.attackStrength += 3;
			}

			this.attackStrength += this.getLvl();
		}

		this.origLvl = this.getLvl();
		this.health = this.getFullHealth();
	}

	/** Chooses the armour overlay texture for the given level. */
	public void setArmorTexture(int level) {
		switch(level) {
		case 1:
			this.armorTexture = "/armor/chain";
			break;
		case 2:
			this.armorTexture = "/armor/iron";
			break;
		case 3:
			this.armorTexture = "/armor/gold";
			break;
		case 4:
			this.armorTexture = "/armor/diamond";
			break;
		default:
			this.armorTexture = "/armor/cloth";
		}
	}

	@Override
	public int getFullHealth() {
		return 20 + this.armorIndex * 4;
	}

	@Override
	public void updateArmor() {
		// Shed one armour layer for each quarter of the armour-scaled health lost.
		int fullHealth = this.getFullHealth();
		int armorFloor = this.armorIndex;

		if(this.health < fullHealth - armorFloor * 4) {
			this.armor = 0;
		} else if(this.health < fullHealth - armorFloor * 3) {
			this.armor = 1;
		} else if(this.health < fullHealth - armorFloor * 2) {
			this.armor = 2;
		} else if(this.health < fullHealth - armorFloor) {
			this.armor = 3;
		}
	}
}
