package net.minecraft.game.entity.monster;

import net.minecraft.game.MathHelper;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.ItemArmor;

public class EntityCityHusk extends EntityHusk {
	public int minArmorTier = 0;
	public int maxArmorTier = 2;
	
	public EntityCityHusk(World world) {
		super(world);
		this.texture = "/mob/cityhusk1.png";
		this.texturePrefix = "cityhusk";
		this.moveSpeed = 0.7F;
		this.attackStrength = 7;
		this.scoreValue = 60;
		
		if(rand.nextBoolean()) {
			for (int k = 0; k < 4; k ++) {
				if(rand.nextBoolean()) {
					int tier = this.minArmorTier + this.worldObj.rand.nextInt (1 + this.maxArmorTier - this.minArmorTier);
					if(tier == 2 && rand.nextInt(4) != 0) tier = 0;
					this.setArmor(k, ItemArmor.getArmorPieceForTier(tier, k));
				}
			}
		}
		
	}
	
	protected int getMaxTextureVariations() {
		return 5;
	}

	@Override
	public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(this.posX);
		int y = MathHelper.floor_double(this.boundingBox.minY);
		int z = MathHelper.floor_double(this.posZ);
		
		if(!this.worldObj.canBlockSeeTheSky(x, y, z)) return false;
		if(y < 60) return false;
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
	
	@Override
	protected int getDropItemId() {
		switch(this.rand.nextInt(30)) {
			case 0:
			case 1: 
			case 2: 
			case 3: 
			case 4:
			case 5: return Item.rottenFlesh.shiftedIndex;
			case 8: return Item.ingotIron.shiftedIndex;
			case 9: return Item.bread.shiftedIndex;
			default: return Block.stone.blockID;
		}
	}
	
	// Only spawns on city chunks
	public boolean isUrban() {
		return true;
	}
	
	@Override
	public int getFullHealth() {
		return 40;
	}
	
}
