package net.minecraft.game.entity.monster;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class EntityPigZombieVolcanoes extends EntityPigZombie {

	public EntityPigZombieVolcanoes(World world1) {
		super(world1);
	}

	@Override
	public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(this.posX);
		int y = MathHelper.floor_double(this.boundingBox.minY);
		int z = MathHelper.floor_double(this.posZ);
		
		boolean s = super.getCanSpawnHere();
		boolean b = this.worldObj.getBlockId(x, y - 1, z) == Block.bloodStone.blockID;
		
		return b && s;
	}
	
}
