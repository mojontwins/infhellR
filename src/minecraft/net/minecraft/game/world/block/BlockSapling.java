package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.tree.EnumTreeType;

public class BlockSapling extends BlockFlower implements IBlockWithSubtypes {
	public String[] names = new String [] {
		"Oak",
		"Baobab",
		"Cypress",
		"Fir",
		"Jungle",
		"Mangrove",
		"Taiga",
		"fancy",
		"Willow",
		"Shrub"
	};
	
	protected BlockSapling(int i1, int i2) {
		super(i1, i2);
		this.setMyBlockBounds();
	}
	
	public void setMyBlockBounds() {
		float f3 = 0.4F;
		this.setBlockBounds(0.5F - f3, 0.0F, 0.5F - f3, 0.5F + f3, f3 * 2.0F, 0.5F + f3);

		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}
	
	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
		if(!world1.isRemote) {
			super.updateTick(world1, i2, i3, i4, random5);
			if(world1.getBlockLightValue(i2, i3 + 1, i4) >= 10 && random5.nextInt(20) == 0) {
				int i6 = world1.getBlockMetadata(i2, i3, i4);
				if((i6 & 8) == 0) {
					world1.setBlockMetadataWithNotify(i2, i3, i4, i6 | 8);
				} else {
					this.growTree(world1, i2, i3, i4, random5);
				}
			}

		}
	}

	public void growTree(World world, int x, int y, int z, Random rand) {
		int saplingId = world.getBlockId(x, y, z);
		int meta = world.getBlockMetadata(x, y, z) & 0xf0;
		
		EnumTreeType tree = EnumTreeType.findTreeTypeFromSapling(new BlockState(Block.blocksList[saplingId], meta));
		WorldGenerator worldGen = tree.getGen(rand);
		
		if (tree.needsFourSaplings) {
			// Check for 4 saplings
			for(int dx = 0; dx >= -1; dx --) {
				for(int dz = 0; dz >= -1; dz --) {
					if(
						this.sameSapling(world, x + dx, y, z + dz, saplingId, meta) &&
						this.sameSapling(world, x + dx + 1, y, z + dz, saplingId, meta) &&
						this.sameSapling(world, x + dx, y, z + dz + 1, saplingId, meta) && 
						this.sameSapling(world, x + dx + 1, y, z + dz + 1, saplingId, meta)) {
						
						world.setBlock(x + dx, y, z + dz, 0);
						world.setBlock(x + dx + 1, y, z + dz, 0);
						world.setBlock(x + dx, y, z + dz + 1, 0);
						world.setBlock(x + dx + 1, y, z + dz + 1, 0);
						
						if(worldGen == null || !worldGen.generate(world, rand, x + dx, y, z + dz)) {
							world.setBlockAndMetadata(x + dx, y, z + dz, saplingId, meta);
							world.setBlockAndMetadata(x + dx + 1, y, z + dz, saplingId, meta);
							world.setBlockAndMetadata(x + dx, y, z + dz + 1, saplingId, meta);
							world.setBlockAndMetadata(x + dx + 1, y, z + dz + 1, saplingId, meta);
				}

					break;
				}
			}
		}
			} else {
			world.setBlock(x, y, z, 0);
			if(worldGen == null || !worldGen.generate(world, rand, x, y, z)) {
				world.setBlockAndMetadata(x, y, z, saplingId, meta);
			}
		}

	}

	public boolean sameSapling(World world, int x, int y, int z, int saplingId, int meta) {
		return world.getBlockId(x, y, z) == saplingId &&
				world.getBlockMetadata(x, y, z) == meta;
	}

	protected int damageDropped(int i1) {
		return i1 & 0xf0;
	}
	
	@Override
	public int getRenderType() {
		return 1;
	}
	
	@Override
	public String inventoryOverlayString(int meta) {
		return this.names[meta >> 4].substring(0, 1);
	}

	@Override
	public String getNameFromMeta(int meta) {
		return "sapling." + this.names[meta >> 4];
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.blockIndexInTexture;
	}
	
	@Override
	public void getSubBlocks(int id, CreativeTabs tab, List<ItemStack> list) {
		for(int i = 0; i < this.names.length; i ++) {
			list.add(new ItemStack(id, 1, i << 4));
		}
	}
}
