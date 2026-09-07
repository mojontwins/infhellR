package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.EntityBlockEntity;
import net.minecraft.game.entity.EntityMeatBlock;

public class BlockMeat extends BlockEntity {

	protected BlockMeat(int id, int blockIndexInTexture, Material material) {
		super(id, blockIndexInTexture, material);
		
		this.displayOnCreativeTab = CreativeTabs.tabMisc;
	}

	@Override
	protected EntityBlockEntity getBlockEntity(World world) {
		return new EntityMeatBlock(world);
	}
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if(world.getBlockMetadata(x, y, z) >= 15) return;
		double xd = x + 0.5D;
		double yd = y + 1.0D;
		double zd = z + 0.5D;
		world.spawnParticle("status_effect", xd, yd, zd, 0.7578125D, 0.44921875D, 0.44921875D);
	}

	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return this.getRenderColor(blockAccess.getBlockMetadata(x, y, z));
	}
	
	@Override
	public int getRenderColor(int meta) {
		byte rb = (byte) (0xff - (meta << 3));
		return rb << 16 | 0xff << 8 | rb;
	}
	
	@Override
	public int idDropped(int meta, Random random2) {
		return -1;
	}
	
	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == Item.rottenFlesh.shiftedIndex) {
			int meta = world.getBlockMetadata(x, y, z) - 2;
			if (meta < 0) meta = 0;
			world.setBlockMetadata(x, y, z, meta);
			EntityMeatBlock entity = (EntityMeatBlock) world.getBlockEntity(x, y, z);
			if (entity != null) {
				entity.meatDuration = meta * 24000 / 15;
				for(int i = 0; i < 15; ++i) {
					world.spawnParticle("status_effect", 
							(double)x + world.rand.nextDouble (), 
							(double)y + world.rand.nextDouble (),
							(double)z + world.rand.nextDouble (),
							0.7578125D, 0.44921875D, 0.44921875D);
				}
				world.playSoundEffect((double)x, (double)y, (double)z, "random.eat", 0.5F, 0.15F);
			} else {
				for(int i = 0; i < 7; ++i) {
					world.spawnParticle("largesmoke", 
							(double)x + world.rand.nextDouble (), 
							(double)y + world.rand.nextDouble (),
							(double)z + world.rand.nextDouble (),
							0.0, 0.0, 0.0);
				}
			}
		}
		return true;
	}
}
