package net.minecraft.game.world;

import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.EntityBlockEntity;

public interface IBlockAccess {
	int getBlockId(int i1, int i2, int i3);

	TileEntity getBlockTileEntity(int i1, int i2, int i3);
	
	EntityBlockEntity getBlockEntity(int x, int y, int z);
	
	float getBrightness(int i1, int i2, int i3, int i4);

	float getLightBrightness(int i1, int i2, int i3);

	int getBlockMetadata(int i1, int i2, int i3);

	Material getBlockMaterial(int i1, int i2, int i3);

	boolean isBlockOpaqueCube(int i1, int i2, int i3);

	boolean isBlockNormalCube(int i1, int i2, int i3);

	WorldChunkManager getWorldChunkManager();

	int getLightBrightnessForSkyBlocks(int i2, int i3, int i4, int i);

	boolean isAirBlock(int i, int j, int z);
}
