package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.material.Material;

public class BlockCryingObsidian extends Block {

	// Adapted from NSSS
	
	public BlockCryingObsidian(int var1, int var2) {
		super(var1, var2, Material.rock);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == Item.compass.shiftedIndex) {
			if(world.isAirBlock(x, y + 1, z) && world.isAirBlock(x, y + 2, z)) {
				entityPlayer.setPlayerSpawnCoordinate(new ChunkCoordinates(x, y + 1, z));
				entityPlayer.setDontCheckSpawnCoordinates(true);
		 		
				if (!world.isRemote) {
					world.getWorldAccess(0).showString("Spawn point set.");
				}
	
				for(int i = 0; i < 7; ++i) {
					world.spawnParticle("largesmoke", 
						(double)x + world.rand.nextDouble (), 
						(double)y + world.rand.nextDouble (),
						(double)z + world.rand.nextDouble (),
						0.0, 0.0, 0.0);
				}
	
				world.playSoundEffect((double)x, (double)y, (double)z, "random.drr", 1.0F, 0.25F);
				world.playSoundEffect((double)x, (double)y, (double)z, "random.breath", 0.5F, 0.15F);
			} else {
				if (!world.isRemote) {
					world.getWorldAccess(0).showString("Can't set spawn! Top is blocked!");
				}
			}
		}
		return true;
	}
	
	@Override
	public int idDropped(int metadata, Random rand) {
		return Block.cryingObsidian.blockID;
	}
}
