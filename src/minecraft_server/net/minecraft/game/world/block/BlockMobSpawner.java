package net.minecraft.game.world.block;

import java.util.Random;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.material.Material;

public class BlockMobSpawner extends BlockContainer {
	public boolean oneShot = false; 
	
	protected BlockMobSpawner(int id, int blockIndex, boolean oneShot) {
		super(id, blockIndex, oneShot ? Material.spawner : Material.rock);
		this.oneShot = oneShot;
		if(oneShot) this.blockIndexInTexture = 253;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		if(this.oneShot) return null;
		return super.getCollisionBoundingBoxFromPool(world1, i2, i3, i4);
	}

	protected TileEntity getBlockEntity() {
		if(this.oneShot) {
			return new TileEntityMobSpawnerOneshot();
		} else {
			return new TileEntityMobSpawner();
		}
	}

	public int idDropped(int metadata, Random rand) {
		return 0;
	}

	public int quantityDropped(Random rand) {
		return 0;
	}

	public boolean isOpaqueCube() {
		return false;
	}
	
	public boolean seeThrough() {
		return true; 
	}
}
