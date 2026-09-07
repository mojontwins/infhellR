package net.minecraft.game.world.terrain.generate.betterdungeons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.feature.FeatureProvider;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockLever;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.World;

public class BetterDungeons {
	
	private ArrayList<StructureBlockData> specialBlocks = new ArrayList<StructureBlockData> ();
	
	public FeatureProvider featureProvider;

	public BetterDungeons(FeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
	}
		
	public ArrayList<StructureBlockData> getSpecialBlocks() {
		return specialBlocks;
	}

	public void setSpecialBlocks(ArrayList<StructureBlockData> specialBlocks) {
		this.specialBlocks = specialBlocks;
	}

	public BetterDungeons() {
	}

	public void populate(World world, Random rand, int chunkX, int chunkZ, BiomeGenBase biomeGen) {
		// Todo :: Hookup original BD dungeons
	}
	
	public void addSpecialBlock(int x, int y, int z, int blockId, int blockMetadata) {
		this.specialBlocks.add(new StructureBlockData(x, y, z, blockId, blockMetadata));
	}
	
	public void copySpecialBlocks(World world) {
		this.copySpecialBlocksCustom(world, this.specialBlocks);
	}

	public void copySpecialBlocksCustom(World world, List<StructureBlockData> specialBlocks) {
		if(specialBlocks.size() > 0) {
			Iterator<StructureBlockData> iterator = specialBlocks.iterator();
			
			while(iterator.hasNext()) {
				StructureBlockData blockData = iterator.next();
				world.setBlockAndMetadataWithNotify(blockData.x, blockData.y, blockData.z, blockData.blockID, blockData.blockMetadata);
				
				if(blockData.blockID == 69) {
					BlockLever blockLever = (BlockLever)Block.lever;
					blockLever.blockActivated(world, blockData.x, blockData.y, blockData.z, null);
					blockLever.blockActivated(world, blockData.x, blockData.y, blockData.z, null);
				} 
			}
			
			specialBlocks.clear();
		}
	}
	
	public void addChest(Random rand, World world, int x, int y, int z) {
		if(world.getBlockId(x, y - 1, z) == 0) {
			if(world.getBlockId(x, y - 2, z) == 0) return;
			world.setBlock(x, y - 1, z, Block.planks.blockID);
		}
		world.setBlockWithNotify(x, y, z, Block.chest.blockID);

		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x, y, z);
		if(tileEntityChest != null) {
			int numItems = rand.nextInt(10) + 4;
			for(int f = 0; f < numItems; ++f) {
				ItemStack itemStack = this.pickChestLootItem(rand);
				tileEntityChest.setInventorySlotContents(f, itemStack);
			}
		}
	}

	public void addSpawner(Random rand, World world, int x, int y, int z) {
		if(world.getBlockId(x, y - 1, z) == 0) {
			if(world.getBlockId(x, y - 2, z) == 0) return;
			world.setBlock(x, y - 1, z, Block.planks.blockID);
		}
		world.setBlockWithNotify(x, y, z, Block.mobSpawner.blockID);
		TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.getBlockTileEntity(x, y, z);
		tileentitymobspawner.setMobID(this.pickMobSpawner(rand));
	}

	private ItemStack pickChestLootItem(Random rand) {
		switch(rand.nextInt(20)) {
			case 0: return new ItemStack(Item.saddle);
			case 1: return new ItemStack(Item.ingotIron, rand.nextInt(4) + 1);
			case 2: return new ItemStack(Item.bread);
			case 3: return new ItemStack(Item.wheat, rand.nextInt(4) + 1);
			case 4: return new ItemStack(Item.gunpowder, rand.nextInt(4) + 1);
			case 5: return new ItemStack(Item.silk, rand.nextInt(4) + 1);
			case 6: return new ItemStack(Item.bucketEmpty);
			case 7: return (rand.nextInt(100) == 0) ? new ItemStack(Item.appleGold) : null;
			case 8: return (rand.nextInt(2) == 0) ? new ItemStack(Item.emerald, rand.nextInt(4) + 4) : null;
			case 9: return (rand.nextInt(10) == 0) ? new ItemStack(Item.itemsList[Item.record13.shiftedIndex + rand.nextInt(2)]) : null;
			case 10: return new ItemStack(Item.dyePowder, 1, 3);
			case 11: return (rand.nextInt(10) == 0) ? new ItemStack(Item.dyePowder, rand.nextInt(4) + 1) : (rand.nextInt(20) == 0 ? new ItemStack(Item.diamond, rand.nextInt(4) + 1) : null);
			case 12: return (rand.nextInt(10) == 0) ? new ItemStack(Item.ingotGold, rand.nextInt(4) + 1) : null;
			//case 13: return new ItemStack(Item.pebble, rand.nextInt(32) + 32);
			case 14: return (rand.nextInt(2) == 0) ? new ItemStack(Item.emerald, rand.nextInt(4) + 4) : null;
			default: return null; 
		}
		
	}

	private String pickMobSpawner(Random rand) {
		int i = rand.nextInt(12);
		switch(i) {
			case 0: return "Eye";
			case 1: return "Creeper"; 
			case 2: return "FastZombie";
			case 3: return "PoisonSkeleton";
			case 4: return "FatZombie";
			case 5: return "Skeleton"; 
			case 6: return "CaveSpider"; 
			case 7: return "SwordZombie"; 
			case 8: return "Spider"; 
			default: return "Zombie";
		}
	}
	
	public void GenerateStructure(World world, Random random, int i, int j, int k, int type) {
	
	}
	
	public void addCustomBlock(int x, int y, int z, int block, int metadata, World world, int mob) {
		Random rand = new Random();
		
		if(block == 255) {
			if(mob == 0) {
				switch(metadata) {
					case 0:
						this.setOneShotSpawner(world, x, y, z, "Pigman", 4);
						break;
					case 1:
						this.setOneShotSpawner(world, x, y, z, "Pigman", 3);
						break;
					case 2:
						this.setOneShotSpawner(world, x, y, z, "Pigman", 2);
						break;
					case 3:
						this.setOneShotSpawner(world, x, y, z, "PigArcher", 0);
						break;
					case 4:
						this.setOneShotSpawner(world, x, y, z, "PigMage", 4);
						break;
					case 5:
						this.setOneShotSpawner(world, x, y, z, "Pigman", 1);
						break;
					case 6:
						this.setOneShotSpawner(world, x, y, z, "Pigman", 0);
						break;
					default:
						this.setOneShotSpawner(world, x, y, z, "PigArcher", 0);
						break;
				}
			} else if(mob == 1) {
				switch(metadata) {
					case 0:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeleton", 4);
						break;
					case 1:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeleton", 3);
						break;
					case 2:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeleton", 2);
						break;
					case 3:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeletonArcher", rand.nextInt(5));
						break;
					case 4:
						this.setOneShotSpawner(world, x, y, z, "Necromancer", 4);
						break;
					case 5:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeleton", 1);
						break;
					case 6:
						this.setOneShotSpawner(world, x, y, z, "ArmoredSkeleton", 0);
						break;
					default:
						if(metadata < 12) {
							this.setOneShotSpawner(world, x, y, z, "ArmoredSkeletonArcher", metadata - 7);
						}
					}
				} else if(mob == 2) {
					switch(metadata) {
					case 0:
						this.setOneShotSpawner(world, x, y, z, "ZombieArmored", 4);
						break;
					case 1:
						this.setOneShotSpawner(world, x, y, z, "ZombieArmored", 3);
						break;
					case 2:
						this.setOneShotSpawner(world, x, y, z, "ZombieArmored", 2);
						break;
					case 3:
						this.setOneShotSpawner(world, x, y, z, "ZombieArcher", rand.nextInt(5));
						break;
					case 4:
						this.setOneShotSpawner(world, x, y, z, "Liche", 4);
						break;
					case 5:
						this.setOneShotSpawner(world, x, y, z, "ZombieArmored", 1);
						break;
					case 6:
						this.setOneShotSpawner(world, x, y, z, "ZombieArmored", 0);
						break;
					default:
						if(metadata < 12) {
							this.setOneShotSpawner(world, x, y, z, "ZombieArcher", metadata - 7);
						}
				}
			} else if(mob == 3) {
				switch(metadata) {
					case 0: 
						this.setOneShotSpawner(world, x, y, z, "PirateArmored", 4);
						break;
					case 1:
						this.setOneShotSpawner(world, x, y, z, "PirateArmored", 3);
						break;
					case 2:
						this.setOneShotSpawner(world, x, y, z, "PirateArmored", 2);
						break;
					case 3:
						this.setOneShotSpawner(world, x, y, z, "PirateArcher", 0);
						break;
					case 4:
						this.setOneShotSpawner(world, x, y, z, "PirateBoss", 0);
						break;
				}
			} else if(mob == 6) {
				// Twilight Forest themed enemy set #1
				switch(metadata) {
				case 0:
				case 10:
					this.setOneShotSpawner(world, x, y, z, "IceWarrior", 0);
					break;
				case 1:
				case 11:
					this.setOneShotSpawner(world, x, y, z, "IceWarrior", 1);
					break;
				case 2:
				case 12:
					this.setOneShotSpawner(world, x, y, z, "IceWarrior", 2);
					break;
				case 3:
				case 13:
					this.setOneShotSpawner(world, x, y, z, "IceWarrior", 3);
					break;
				case 4:
				case 14:
					this.setOneShotSpawner(world, x, y, z, "IceWarrior", 4);
					break;
				case 15:
					this.setOneShotSpawner(world, x, y, z, "IceMage", 4);
					break;
				default:
					// 5 to 9
					this.setOneShotSpawner(world, x, y, z, "IceArcher", metadata - 5);
					break;
				}
			}
		} else if(block == 254) {
			switch(metadata) {
				case 0:
					this.addTreasure(new Random(), world, x, y, z);
					break;
				case 1:
					this.addFoodChest(new Random(), world, x, y, z);
					break;
				case 2:
					this.addMineralChest(new Random(), world, x, y, z);
					break;
				case 3:
					this.addWeaponChest(new Random(), world, x, y, z);
					break;
			}
		}
		
	}
	
	private boolean setOneShotSpawner(World world, int x, int y, int z, String mobID, int level) {
		world.setBlock(x, y + 1, z, 0); // Added
		world.setBlockAndMetadata(x, y, z, Block.mobSpawnerOneshot.blockID, level);
		TileEntityMobSpawnerOneshot tileEntityMobSpawner = (TileEntityMobSpawnerOneshot)world.getBlockTileEntity(x, y, z);

		if (tileEntityMobSpawner != null) {
			tileEntityMobSpawner.setMobID(mobID);
			return true;
		}
		
		return false;
	}
	
	private boolean addTreasure(Random random, World world, int x, int y, int z) {
		world.setBlock(x, y, z, Block.chest.blockID);
		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x, y, z);
		if(tileEntityChest == null) {
			return false;
		} else {
			int itemsCount = random.nextInt(7) + 1;

			for(int f = 0; f < itemsCount; ++f) {
				int item = random.nextInt(16);
				ItemStack itemStack;
				if(item < 3) {
					itemStack = new ItemStack(Item.diamond, random.nextInt(10) + 1);
				} else if(item < 6) {
					itemStack = new ItemStack(Item.emerald, random.nextInt(10) + 1);
				} else if(item < 9) {
					itemStack = new ItemStack(Item.appleGold, random.nextInt(5) + 1);
				} else if(item == 9) {
					itemStack = new ItemStack(Block.cryingObsidian);
				} else if(item == 13) {
					itemStack = new ItemStack(Item.emerald, random.nextInt(10) + 1);
				} else if(item == 14) {
					itemStack = new ItemStack(Item.nametagSimple);
				} else {
					itemStack = new ItemStack(Item.bowlEmpty);
				}

				tileEntityChest.setInventorySlotContents(f + 1, itemStack);
			}

			tileEntityChest.setInventorySlotContents(0, new ItemStack(2256 + random.nextInt(2), 1, 0));
			return true;
		}
	}

	private boolean addFoodChest(Random random, World world, int x, int y, int z) {
		world.setBlock(x, y, z, Block.chest.blockID);
		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x, y, z);
		if(tileEntityChest == null) {
			return false;
		} else {
			int itemsCount = random.nextInt(10) + 1;

			for(int f = 0; f < itemsCount; ++f) {
				int item = random.nextInt(1000);
				ItemStack itemStack;
				
				if(item < 50) {
					itemStack = new ItemStack(Block.driedKelpBlock, random.nextInt(3) + 1);
				} else if(item < 200) {
					itemStack = new ItemStack(Item.porkRaw, random.nextInt(3) + 1);
				} else if(item < 450) {
					itemStack = new ItemStack(Item.chickenRaw, random.nextInt(3) + 1);
				} else if(item < 700) {
					itemStack = new ItemStack(Item.bread, random.nextInt(3) + 1);
				} else if(item < 960) {
					itemStack = new ItemStack(Item.appleRed, random.nextInt(3) + 1);
				} else if(item < 998) {
					itemStack = new ItemStack(Item.potionInstantDamage, random.nextInt(3) + 1);
				} else if(item < 1000) {
					itemStack = new ItemStack(Item.potionPoison, random.nextInt(2) + 1, random.nextInt(30));
				} else {
					itemStack = new ItemStack(Item.appleGold, random.nextInt(10) + 1);
				}

				tileEntityChest.setInventorySlotContents(f, itemStack);
			}

			return true;
		}
	}

	private boolean addMineralChest(Random random, World world, int x, int y, int z) {
		world.setBlock(x, y, z, Block.chest.blockID);
		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x, y, z);
		if(tileEntityChest == null) {			
			return false;
		} else {
			int itemsCount = random.nextInt(10) + 1;

			for(int f = 0; f < itemsCount; ++f) {
				int item = random.nextInt(1000);
				ItemStack itemStack;
				/*if(item < 50) {
					itemStack = new ItemStack(Item.pirateSigil);
				} else */if(item < 300) {
					itemStack = new ItemStack(Item.ingotIron, random.nextInt(5) + 1);
				} else if(item < 550) {
					itemStack = new ItemStack(Item.ingotGold, random.nextInt(5) + 1);
				} else if(item < 600) {
					itemStack = new ItemStack(Item.redstone, random.nextInt(3) + 1);
				} else if(item < 700) {
					itemStack = new ItemStack(Item.wheat, random.nextInt(10) + 1);
				} else if(item < 900) {
					itemStack = new ItemStack(Item.coal, random.nextInt(10) + 1);
				} else if(item < 999) {
					itemStack = new ItemStack(Item.emerald, random.nextInt(3) + 1);
				} else {
					itemStack = new ItemStack(Item.diamond, random.nextInt(3) + 1);
				}

				tileEntityChest.setInventorySlotContents(f, itemStack);
			}

			return true;
		}
	}

	private boolean addWeaponChest(Random random, World world, int x, int y, int z) {
		world.setBlock(x, y, z, Block.chest.blockID);
		TileEntityChest tileEntityChest = (TileEntityChest)world.getBlockTileEntity(x, y, z);
		if(tileEntityChest == null) {
			return false;
		} else {
			int itemsCount = random.nextInt(7) + 5;

			for(int f = 0; f < itemsCount; ++f) {
				int item = random.nextInt(1000);
				ItemStack itemStack;
				if(item < 10) {
					itemStack = new ItemStack(Item.helmetLeather);
				} else if(item < 30) {
					itemStack = new ItemStack(Item.helmetChain);
				} else if(item < 70) {
					itemStack = new ItemStack(Item.helmetSteel);
				} else if(item < 99) {
					itemStack = new ItemStack(Item.helmetPirate);
				} else if(item < 100) {
					itemStack = new ItemStack(Item.helmetDiamond);
				} else if(item < 110) {
					itemStack = new ItemStack(Item.plateLeather);
				} else if(item < 130) {
					itemStack = new ItemStack(Item.plateChain);
				} else if(item < 170) {
					itemStack = new ItemStack(Item.plateSteel);
				} else if(item < 199) {
					itemStack = new ItemStack(Item.platePirate);
				} else if(item < 200) {
					itemStack = new ItemStack(Item.plateDiamond);
				} else if(item < 210) {
					itemStack = new ItemStack(Item.legsLeather);
				} else if(item < 230) {
					itemStack = new ItemStack(Item.legsChain);
				} else if(item < 260) {
					itemStack = new ItemStack(Item.legsSteel);
				} else if(item < 298) {
					itemStack = new ItemStack(Item.legsPirate);
				} else if(item < 300) {
					itemStack = new ItemStack(Item.legsDiamond);
				} else if(item < 310) {
					itemStack = new ItemStack(Item.bootsLeather);
				} else if(item < 330) {
					itemStack = new ItemStack(Item.bootsChain);
				} else if(item < 350) {
					itemStack = new ItemStack(Item.bootsSteel);
				} else if(item < 399) {
					itemStack = new ItemStack(Item.bootsPirate);
				} else if(item < 400) {
					itemStack = new ItemStack(Item.bootsDiamond);
				} else if(item < 420) {
					itemStack = new ItemStack(Item.helmetChain);
				} else if(item < 430) {
					itemStack = new ItemStack(Item.helmetLeather);
				} else if(item < 455) {
					itemStack = new ItemStack(Item.helmetPirate);
				} else if(item < 499) {
					itemStack = new ItemStack(Item.helmetGold);
				} else if(item < 500) {
					itemStack = new ItemStack(Item.helmetDiamond);
				} else if(item < 520) {
					itemStack = new ItemStack(Item.maceGold);
				} else if(item < 520) {
					itemStack = new ItemStack(Item.battleWood);
				} else if(item < 530) {
					itemStack = new ItemStack(Item.battleSteel);
				} else if(item < 540) {
					itemStack = new ItemStack(Item.battleDiamond);
				} else if(item < 550) {
					itemStack = new ItemStack(Item.hammerSteel);
				} else if(item < 560) {
					itemStack = new ItemStack(Item.knifeSteel);
				} else if(item < 570) {
					itemStack = new ItemStack(Item.swordSteel);
				} else if(item < 580) {
					itemStack = new ItemStack(Item.swordGold);
				} else if(item < 581) {
					itemStack = new ItemStack(Item.swordDiamond);
				} else if(item < 610) {
					itemStack = new ItemStack(Item.bow);
				} else if(item < 710) {
					itemStack = new ItemStack(Item.arrow, random.nextInt(16) + 16);
				} else if(item < 810) {
					itemStack = new ItemStack(Item.slingshot);
				} else if(item < 910) {
					itemStack = new ItemStack(Item.pebble, random.nextInt(32) + 32);	
				} else {
					itemStack = new ItemStack(Block.torchWood, 5 + random.nextInt(25));
				}

				tileEntityChest.setInventorySlotContents(f, itemStack);
			}

			return true;
		}
	}
}
