package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockLog extends Block {
	/*
	 * Original beta logs use metadata 0, 1, 2 for wood type (NORMAL, birch, spruce);
	 * which means such info can be encoded in the first two bits of metadata.
	 * 
	 * This version has wood types soft-locked but will take account for them
	 * and use bits 2 and 3 for orientation. 2 means horizontal, 3 means X or Z aligned
	 */
	
	protected BlockLog(int i1) {
		super(i1, Material.wood);
		this.blockIndexInTexture = 20;
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	protected BlockLog(int i1, Material m) {
		super(i1, m);
		this.blockIndexInTexture = 20;
	}

	public int quantityDropped(Random random1) {
		return 1;
	}

	public int idDropped(int i1, Random random2) {
		return Block.wood.blockID;
	}

	public void onBlockRemoval(World world1, int i2, int i3, int i4) {
		byte b5 = 4;
		int i6 = b5 + 1;
		if(world1.checkChunksExist(i2 - i6, i3 - i6, i4 - i6, i2 + i6, i3 + i6, i4 + i6)) {
			for(int i7 = -b5; i7 <= b5; ++i7) {
				for(int i8 = -b5; i8 <= b5; ++i8) {
					for(int i9 = -b5; i9 <= b5; ++i9) {
						int i10 = world1.getBlockId(i2 + i7, i3 + i8, i4 + i9);
						if(i10 == Block.leaves.blockID) {
							int i11 = world1.getBlockMetadata(i2 + i7, i3 + i8, i4 + i9);
							if((i11 & 8) == 0) {
								world1.setBlockMetadata(i2 + i7, i3 + i8, i4 + i9, i11 | 8);
							}
						}
					}
				}
			}
		}

	}

	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		int woodType = meta & 3;
		boolean horizontal = (meta & 4) != 0;

		int outTextureIndex;
		int endTextureIndex = 21;
		
		switch(woodType) {
			case 1: outTextureIndex = 116; break;
			case 2: outTextureIndex = 117; break;
			default: outTextureIndex = this.blockIndexInTexture; break;
		}
		
		if (!horizontal) {
			// Vanilla logs:
			return side <= 1 ? endTextureIndex : outTextureIndex;
		} else {
			// Horizontal logs:
			if(side <= 1) return outTextureIndex;
			
			if((meta & 8) != 0) {
				return (side == 4 || side == 5) ? endTextureIndex : outTextureIndex;
			} else {
				return (side == 2 || side == 3) ? endTextureIndex : outTextureIndex;
			}
		}
		
	}
	
	public int getBlockTextureFromSide(int side) {
		return this.getBlockTextureFromSideAndMetadata(side, 0);
	}

	protected int damageDropped(int i1) {
		return i1 & 3;
	}
	
	public void onBlockPlaced(World world, int x, int y, int z, int side) {
		// Make this compatible with beta wood types:
		int meta = world.getBlockMetadata(x, y, z);
		
		// Make horizontal & oriented?
		if (side > 1) {
			meta |= 4;
			if (side > 3) {
				meta |= 8;
			}
		}
		
		world.setBlockMetadata(x, y, z, meta);
	}
    
	public boolean renderAsNormalBlock() {
		return true;
	}
	
	public boolean isOpaqueCube() {
		return true;
	}

	public int getRenderType() {
		return 108;
	}
	
	public boolean canGrowMushrooms() {
		return true;
	}
	
	public boolean canGrowMoss() {
		return true;
	}
}
