package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;

public class BlockGrass extends Block implements IBlockWithSubtypes {
	protected BlockGrass(int blockID) {
		super(blockID, Material.grass);
		this.blockIndexInTexture = 3;
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public boolean canGrowMoss() {
		return true;
	}

	public int getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		int meta = blockAccess.getBlockMetadata(x, y, z);
		
		if(meta == 1) {
		
			switch(side) {
				case 0: return 2;
				case 1: return 0;
				default: 
					Block block = Block.blocksList[blockAccess.getBlockId(x, y + 1, z)];
					
					if(block != null && (
							(block.blockMaterial == Material.snow || block.blockMaterial == Material.builtSnow) ||
							(block.getRenderType() == 111 && blockAccess.getBlockMetadata(x, y + 1, z) > 0)
						)
					) {
						return 14 * 16 + 1;
					}
					
					return 0;
			}
		}
		
		switch(side) {
			case 0: return 2;
			case 1: return 0;
			default: 
				Block block = Block.blocksList[blockAccess.getBlockId(x, y + 1, z)];
				
				if(block == null) return 3;
				
				if(block.blockMaterial == Material.snow || block.blockMaterial == Material.builtSnow) return 68;
				if(block.getRenderType() == 111 && blockAccess.getBlockMetadata(x, y + 1, z) > 0) return 68;
				
				return 3;
		}
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		if(meta == 1) return 0; 
		return this.getBlockTextureFromSide(side);
	}
	
	@Override
	public int getBlockTextureFromSide(int side) {
		switch(side) {
			case 0: return 2;
			case 1: return 0;
			default: return 3;
		}
	}
	
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);
			int blockIDAbove = world.getBlockId(x, y + 1, z);
			
			if(meta == 1) {
				int blockBelow = world.getBlockId(x, y - 1, z);
				if(blockBelow == Block.dirt.blockID) {
					world.setBlock(x, y - 1, z, Block.grass.blockID);
				} else if (blockBelow == 0) {
					world.setBlockMetadata(x, y, z, 0);
				}
			}
			
			if(world.getBlockLightValue(x, y + 1, z) < 4 && Block.lightOpacity[blockIDAbove] > 2) {
				
				if(blockIDAbove == Block.grass.blockID && world.getBlockMetadata(x, y + 1, z) == 1) {
					return;
				}
				
				if(rand.nextInt(4) != 0) {
					return;
				}

				world.setBlockWithNotify(x, y, z, Block.dirt.blockID);
			} else if(world.getBlockLightValue(x, y + 1, z) >= 9) {
				int xx = x + rand.nextInt(3) - 1;
				int yy = y + rand.nextInt(5) - 3;
				int zz = z + rand.nextInt(3) - 1;
				int blockID = world.getBlockId(xx, yy + 1, zz);
				if(world.getBlockId(xx, yy, zz) == Block.dirt.blockID && world.getBlockLightValue(xx, yy + 1, zz) >= 4 && Block.lightOpacity[blockID] <= 2) {
					world.setBlockAndMetadataWithNotify(xx, yy, zz, Block.grass.blockID, meta);
				}
			}

			if(Seasons.currentSeason == Seasons.AUTUMN && world.isAirBlock(x, y + 1, z) && world.isUnderLeaves(x, y + 1, z)) {
				world.setBlock(x, y + 1, z, Block.leafPile.blockID);
			}
		}
	}

	public int idDropped(int i1, Random random2) {
		return Block.dirt.idDropped(0, random2);
	}
	
	public boolean canGrowPlants() {
		return true;
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 2; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
    	if (y <= 0) return;
    	
    	int blockID = world.getBlockId(x, y, z);
    	if (blockID != this.blockID) return;
    	
    	int meta = world.getBlockMetadata(x, y, z);
    	int blockBelow = world.getBlockId(x, y - 1, z);
    	int blockAbove = world.getBlockId(x, y + 1, z);
    	
    	if (meta == 1 && blockBelow != this.blockID) {
    		world.setBlockMetadata(x, y, z, 0);
    	} 
    	
    	if (blockAbove != this.blockID && (Block.opaqueCubeLookup[blockAbove] || blockAbove == Block.dirtPath.blockID)) {
    		world.setBlock(x, y, z, Block.dirt.blockID);
    		this.onNeighborBlockChange(world, x, y - 1, z, this.blockID);
    	}
    	
    	if (blockAbove == this.blockID) {
    		if (meta == 1) {
	    		world.setBlockMetadata(x, y, z, 0);
	    		world.setBlockMetadata(x, y + 1, z, 1);
    		} else {
    			world.setBlock(x, y, z, Block.dirt.blockID);
    		}
    		
    		this.onNeighborBlockChange(world, x, y - 1, z, neighborBlockID);
    	}
    }

	@Override
	public String getNameFromMeta(int meta) {
		return "grass";
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return meta == 0 ? 3 : 0;
	}
}
