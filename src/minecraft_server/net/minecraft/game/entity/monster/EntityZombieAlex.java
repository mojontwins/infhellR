package net.minecraft.game.entity.monster;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class EntityZombieAlex extends EntityZombie {
	
	public EntityZombieAlex(World world) {
		super(world);
		this.texture = "/mob/zombie_alex1.png";
		this.texturePrefix = "zombie_alex";
		this.moveSpeed = 0.8F;
		this.attackStrength = 3;
		this.scoreValue = 20;
	}
	
	protected int getMaxTextureVariations() {
		return 3;
	}
	
	protected int getDropItemId() {
		switch(this.rand.nextInt(15)) {
			case 0:
			case 1:
			case 2: return Item.rottenFlesh.shiftedIndex;
			case 3: 
			case 4:
			case 5: return Item.appleRed.shiftedIndex;
			case 6: return this.rand.nextInt(50) == 0 ? Item.appleGold.shiftedIndex : Block.blueFlower.blockID;
			default: return Item.feather.shiftedIndex;
		}
	}
}
