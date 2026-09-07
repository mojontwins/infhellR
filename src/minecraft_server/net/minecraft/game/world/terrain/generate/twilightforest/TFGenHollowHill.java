package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.World;

public class TFGenHollowHill extends TFGenerator {
	int hsize;
	int radius;
	int sn;
	int mg;
	int tc;
	int hx;
	int hy;
	int hz;
	Random hillRNG;

	public TFGenHollowHill(int size) {
		this.hsize = size;
		this.radius = (this.hsize * 2 + 1) * 8 - 6;
		// Original b1.7.3 method
		/*
		int area = (int)(Math.PI * (double)this.radius * (double)this.radius);
		this.sn = area / 8;
		*/
		// r1.2.5 method
		int[] sna = new int[]{0, 128, 256, 512};
		this.sn = sna[this.hsize];
		int[] mga = new int[]{0, 2, 5, 10};
		this.mg = mga[this.hsize];
		int[] tca = new int[]{0, 2, 6, 12};
		this.tc = tca[this.hsize];
	}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		// When used in the overworld, y = 64!
		
		this.worldObj = world;
		this.hx = x;
		this.hy = y; 				// In the overworld, 64
		this.hz = z;
		this.hillRNG = rand;

		int i;
		int[] dest;
		for(i = 0; i < this.mg; ++i) {
			dest = this.getCoordsInHill2D();
			this.placeMobSpawner(dest[0], this.hy + rand.nextInt(4), dest[1]);
		}

		for(i = 0; i < this.tc; ++i) {
			dest = this.getCoordsInHill2D();
			this.placeTreasureChest(dest[0], this.hy + rand.nextInt(4), dest[1]);
		}

		// Note how all stalag[ti|mi]tes are generated from y+1, which happens to be 65 in the overworld!
		// As we may have terrain under 64, water covered, this has to be taken in account in TFGenCaveStalactite.
		
		for(i = 0; i < this.sn; ++i) {
			dest = this.getCoordsInHill2D();
			TFGenCaveStalactite stalag = TFGenCaveStalactite.makeRandomOreStalactite(rand, this.hsize);
			stalag.generate(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		for(i = 0; i < this.sn; ++i) {
			dest = this.getCoordsInHill2D();
			(new TFGenCaveStalactite(Block.stone.blockID, rand.nextDouble(), true)).generate(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		for(i = 0; i < this.sn; ++i) {
			dest = this.getCoordsInHill2D();
			(new TFGenCaveStalactite(Block.stone.blockID, rand.nextDouble() * 0.7D, false)).generate(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		if(this.hsize == 3) {
			int[] i9 = this.getEmptyCoordsInHill(this.hy + 10, 20);
			this.placeWraithSpawner(i9[0], this.hy + 10, i9[1]);
			i9 = this.getEmptyCoordsInHill(this.hy + 10, 20);
			this.placeWraithSpawner(i9[0], this.hy + 10, i9[1]);
		}
		
		return true;
	}

	boolean isInHill(int cx, int cz) {
		int dx = this.hx - cx;
		int dz = this.hz - cz;
		int dist = (int)Math.sqrt((double)(dx * dx + dz * dz));
		return dist < this.radius;
	}

	int[] getCoordsInHill2D() {
		int rx;
		int rz;
		do {
			rx = this.hx + this.hillRNG.nextInt(2 * this.radius) - this.radius;
			rz = this.hz + this.hillRNG.nextInt(2 * this.radius) - this.radius;
		} while(!this.isInHill(rx, rz));

		int[] coords = new int[]{rx, rz};
		return coords;
	}

	int[] getEmptyCoordsInHill(int ry, int rad) {
		int rx;
		int rz;
		int whatsThere;
		do {
			rx = this.hx + this.hillRNG.nextInt(2 * rad) - rad;
			rz = this.hz + this.hillRNG.nextInt(2 * rad) - rad;
			whatsThere = this.worldObj.getBlockId(rx, ry, rz);
		} while(!this.isInHill(rx, rz) || whatsThere != 0);

		int[] coords = new int[]{rx, rz};
		return coords;
	}

	protected boolean placeMobSpawner(int dx, int dy, int dz) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Block.mobSpawner.blockID);
		TileEntityMobSpawner ms = (TileEntityMobSpawner)this.worldObj.getBlockTileEntity(dx, dy, dz);
		if(ms != null) {
			ms.setMobID(this.getMobID(this.hsize));
		}
		this.fillWithBlocksDownwads(dx, dy - 1, dz, Block.cobblestone.blockID, 0);

		return true;
	}
	
	protected boolean placeWraithSpawner(int dx, int dy, int dz) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Block.mobSpawnerOneshot.blockID);
		TileEntityMobSpawnerOneshot ms = (TileEntityMobSpawnerOneshot)this.worldObj.getBlockTileEntity(dx, dy, dz);
		if(ms != null) {
			ms.setMobID("TwilightWraith");
		}

		return true;
	}

	
	protected String getMobID(int level) {
		return level == 1 ? this.getLevel1Mob() : (level == 2 ? this.getLevel2Mob() : (level == 3 ? this.getLevel3Mob() : "Spider"));
	}

	public String getLevel1Mob() {
		switch(this.hillRNG.nextInt(10)) {
		case 0:
		case 1:
		case 2:
			return "SwarmSpider";
		case 3:
		case 4:
		case 5:
			return "Spider";
		case 6:
		case 7:
			return "Zombie";
		case 8:
			//return "Silverfish";
		case 9:
			return "Redcap";
		default:
			return "Spider";
		}
	}

	public String getLevel2Mob() {
		switch(this.hillRNG.nextInt(10)) {
		case 0:
		case 1:
		case 2:
			return "Redcap";
		case 3:
		case 4:
		case 5:
			return "Zombie";
		case 6:
		case 7:
			return "Skeleton";
		case 8:
			return "Spider";
		case 9:
			return "SwarmSpider";
		default:
			return "Redcap";
		}
	}

	public String getLevel3Mob() {
		switch(this.hillRNG.nextInt(11)) {
		case 0:
		case 1:
		case 2:
			return "SwarmSpider";
		case 3:
		case 4:
		case 5:
			return "Skeleton";
		case 6:
		case 7:
			return "Spider";
		case 8:
			return "Creeper";
		case 9:
		case 10:
			return "TwilightWraith";
		default:
			return "Husk";
		}
	}

	protected boolean placeTreasureChest(int dx, int dy, int dz) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Block.chest.blockID);
		TileEntityChest tec = (TileEntityChest)this.worldObj.getBlockTileEntity(dx, dy, dz);
		if(tec != null && tec.getSizeInventory() > 0) {
			int ni = this.hillRNG.nextInt(4) + this.hillRNG.nextInt(4) + 2;

			for(int i = 0; i < ni; ++i) {
				tec.setInventorySlotContents(i, this.getTreasure(this.hsize));
			}
		}
		this.fillWithBlocksDownwads(dx, dy - 1, dz, Block.cobblestone.blockID, 0);

		return true;
	}

	protected ItemStack getTreasure(int level) {
		if(level == 1) {
			switch(this.hillRNG.nextInt(6)) {
			case 0:
				return new ItemStack(Item.ingotIron, this.hillRNG.nextInt(4) + 1);
			case 1:
				return new ItemStack(Item.bucketEmpty);
			case 2:
				return new ItemStack(Item.bread);
			case 3:
				return new ItemStack(Item.ruby);
			case 5:
				return new ItemStack(Item.emerald);
			case 4:
				return new ItemStack(Item.wheat, this.hillRNG.nextInt(3) + 1);
			default:
				return new ItemStack(Block.torchWood, this.hillRNG.nextInt(16) + 1);
			}
		} else if(level == 2) {
			switch(this.hillRNG.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getTreasure(1);
			case 3:
				return new ItemStack(Item.ruby);
			case 7:
				return new ItemStack(Item.emerald);
			case 4:
				return new ItemStack(Item.ingotGold, this.hillRNG.nextInt(6) + 1);
			case 5:
				return new ItemStack(Item.saddle);
			case 6:
				return new ItemStack(Item.dyePowder, this.hillRNG.nextInt(10) + 1, this.hillRNG.nextInt(16));
			default:
				return new ItemStack(Item.bowlSoup);
			}
		} else if(level == 3) {
			switch(this.hillRNG.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getTreasure(2);
			case 3:
				return new ItemStack(Item.emerald);
			case 7:
				return new ItemStack(Item.ruby);
			case 4:
				return new ItemStack(Item.appleGold);
			case 5:
				return new ItemStack(Block.sponge);
			case 6:
				return new ItemStack(Item.saddle);
			default:
				return new ItemStack(Item.diamond);
			}
		} else {
			return new ItemStack(Item.coal, this.hillRNG.nextInt(16) + 1);
		}
	}
}
