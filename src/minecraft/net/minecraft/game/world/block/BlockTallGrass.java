package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;

public class BlockTallGrass extends BlockFlower implements IBlockWithSubtypes {
	// Metadata:
	// DTTTSSSS -> S = Snow logged
	// |\_/---- -> T = Grass type
	// `------- -> D = Double size
	
	public static int[] tallGrassColor = new int [] {
		0xFFFFFF,
		0xF3EFC1, // 0xFAF3A4, 
		0xA76E1F
	};
	
	protected BlockTallGrass(int var1, int var2) {
		super(var1, var2);
		this.setMyBlockBounds();
	}

	@Override 
	public void setMyBlockBounds() {
		float var3 = 0.4F;
		this.setBlockBounds(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 0.8F, 0.5F + var3);
	}

	@Override
	public int idDropped(int meta, Random random2) {
		meta >>= 4;		// Bits 4-7  
		meta &= 7; 		// Ignore bit 7 (which marks "double height"

		if(meta == 0) {
			return random2.nextInt(8) == 0 ? Item.seeds.shiftedIndex : -1;
		} else if(meta < 3) {
			return random2.nextInt(8) == 0 ? Item.straw.shiftedIndex : -1;
		} else {
			return -1;
		}
	}
	
	@Override
	protected int damageDropped(int meta) {
		//meta >>= 4;		// Bits 4-7  
		//meta &= 7; 		// Ignore bit 7 (which marks "double height"
		return 0;
	}
	
	@Override
	public int getRenderType() {
		return 111;
	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return ((meta >> 4) & 7) == 0 ? 13 * 16 + 13 : 15 * 16 + 10;
	}
	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return this.getRenderColor(blockAccess.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta) {
		return tallGrassColor[(meta >> 4) & 7];
	}
	
	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return this.canBlockStay(world, x, y, z, world.getBlockMetadata(x, y, z));
	}
	
	@Override
	public boolean canBlockStay(World world, int x, int y, int z, int metadata) {
		int grassType = (metadata >> 4) & 7;
		
		switch(grassType) {
		case 0: 
			return super.canBlockStay(world, x, y, z);
			
		default:
			int blockID = world.getBlockId(x, y - 1, z);
			Block block = Block.blocksList[blockID];
			
			if(block != null && block.canGrowPlants()) return true;
			if(blockID == Block.sand.blockID || blockID == Block.terracotta.blockID || blockID == Block.stainedTerracotta.blockID) return true;
			
			return false;
		}
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 3; i ++) {
			par3List.add(new ItemStack(par1, 1, i << 4));
		}
		
		for(int i = 0; i < 3; i ++) {
			par3List.add(new ItemStack(par1, 1, 128 | (i << 4)));
		}
	}
    
    @Override
	protected boolean canThisPlantGrowOnThisBlockID(int i1) {
		Block block = Block.blocksList[i1];
		return block != null && (i1 == Block.sand.blockID || block.canGrowPlants());
    }
    
	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == Item.shears.shiftedIndex) {
			this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.tallGrass, 1, world.getBlockMetadata(x, y, z) & 0xf0));
			world.setBlockWithNotify(x, y, z, 0);
			world.playSoundEffect((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, this.stepSound.getStepSound(), (this.stepSound.getVolume() + 1.0F) / 8.0F, this.stepSound.getPitch() * 0.5F);
			return true;
		}
		
		return false;
	}

	@Override
	public String getNameFromMeta(int meta) {
		return (meta & 128) != 0 ? "verytallgrass" : "tallgrass";
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.getBlockTextureFromSideAndMetadata(0, meta);
	}
	
	@Override
	public int getEncouragementToFire() {
		return 60;
	}

	@Override
	public int getAbilityToCatchFire() {
		return 100;
	}
}
