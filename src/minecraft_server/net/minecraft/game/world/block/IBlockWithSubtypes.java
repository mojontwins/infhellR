package net.minecraft.game.world.block;

public interface IBlockWithSubtypes {
	public String getNameFromMeta(int meta);
	public int getIndexInTextureFromMeta(int meta);
	public int getRenderColor(int damage);
}
