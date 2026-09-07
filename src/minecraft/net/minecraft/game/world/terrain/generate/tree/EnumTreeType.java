package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.bo3.WorldGenBo3Tree;
import net.minecraft.game.world.terrain.generate.WorldGenBaobab;
import net.minecraft.game.world.terrain.generate.WorldGenCypress;
import net.minecraft.game.world.terrain.generate.WorldGenFir;
import net.minecraft.game.world.terrain.generate.WorldGenMangrove;
import net.minecraft.game.world.terrain.generate.WorldGenWillow;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.block.BlockLeaves;
import net.minecraft.game.world.block.BlockState;
import net.minecraft.game.world.terrain.generate.WorldGenTaiga1;
import net.minecraft.game.world.terrain.generate.WorldGenTaiga2;


public enum EnumTreeType {
	
	NORMAL("Oak", new BlockState(Block.leaves, BlockLeaves.OAK), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.OAK)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return rand.nextInt(8) == 0 ? new WorldGenBigTree() : new WorldGenTrees();
		}
	},
	
	BAOBAB("Baobab", new BlockState(Block.leaves, BlockLeaves.BAOBAB), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.BAOBAB)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return rand.nextBoolean() ?
					new WorldGenBaobab(2+rand.nextInt(3), true)
				:
					new WorldGenBaobab(4+rand.nextInt(5), true);
		}
	},
	
	CYPRESS("Cypress", new BlockState(Block.leaves, BlockLeaves.CYPRESS), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.CYPRESS)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return rand.nextInt(8) == 0 ? new WorldGenCypress(5+rand.nextInt(5), true) : null;
		}
	},
	
	FIR("Fir", new BlockState(Block.leaves, BlockLeaves.FIR), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.FIR)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return rand.nextBoolean() ?
					new WorldGenPineTree(5+rand.nextInt(5), true)
				:
					new WorldGenFir(3+rand.nextInt(3), false, true);
		}
	},
	
	JUNGLE("Jungle", new BlockState(Block.leaves, BlockLeaves.JUNGLE), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.JUNGLE), true) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return new WorldGenHugeTrees(10 + rand.nextInt(10));
		}
	},
	
	MANGROVE("Mangrove", new BlockState(Block.leaves, BlockLeaves.MANGROVE), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.MANGROVE)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return new WorldGenMangrove(true, true);
		}
	},
	
	TAIGA("Taiga", new BlockState(Block.leaves, BlockLeaves.TAIGA), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.TAIGA)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return rand.nextBoolean() ? 
					new WorldGenTaiga1()
				:
					new WorldGenTaiga2();
		}
	},
	
	FANCYFIR("Fancy", new BlockState(Block.leaves, BlockLeaves.FANCYFIR), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.FANCYFIR)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			WorldGenBo3Tree bo3Tree = new WorldGenBo3Tree().withLeavesMeta(this.leaves.getMetadata());
			if (rand.nextInt (10) != 0) {
				bo3Tree.setTreeName("Spruce:SpruceFirSmall" + (1 + rand.nextInt(15)));
			} else {
				bo3Tree.setTreeName("Spruce:SpruceFir" + (1 + rand.nextInt(21)));
			}
			return bo3Tree;
		}
	},
	
	WILLOW("Willow", new BlockState(Block.leaves, BlockLeaves.WILLOW), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.WILLOW)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return new WorldGenWillow(4 + rand.nextInt(4), true);
		}
	},
	
	SHRUB("Shrub", new BlockState(Block.leaves, BlockLeaves.SHRUB), BlockState.wood, new BlockState(Block.sapling, BlockLeaves.SHRUB)) {
		@Override
		public WorldGenerator getGen(Random rand) {
			return new WorldGenShrub();
		}
	},
	;
	
	public final BlockState leaves;
	public final BlockState wood;
	public final BlockState sapling;
	
	public final String name;
	
	public final boolean needsFourSaplings;
	
	public WorldGenerator getGen(Random rand) {
		return new WorldGenTrees();
	}
	
	public static BlockState getSaplingFromLeaves(BlockState leaves) {
		return new BlockState(Block.sapling, leaves.getMetadata());
	}
	
	public static EnumTreeType findTreeTypeFromLeaves(BlockState leaves) {
		for(EnumTreeType e : EnumTreeType.values()) {
			if(leaves.equals(e.leaves)) {
				return e;
			}
		}
		
		return NORMAL;
	}
	
	public static EnumTreeType findTreeTypeFromSapling(BlockState sapling) {
		for(EnumTreeType e : EnumTreeType.values()) {
			if(sapling.equals(e.sapling)) {
				return e;
			}
		}
		
		return NORMAL;
	}
	
	public static EnumTreeType findTreeTypeFromWood(BlockState wood) {
		for(EnumTreeType e : EnumTreeType.values()) {
			if(wood.equals(e.wood)) {
				return e;
			}
		}
		
		return NORMAL;
	}
	
	EnumTreeType(String name, BlockState leaves, BlockState wood, BlockState sapling) {
		this(name, leaves, wood, sapling, false);
	}
	
	EnumTreeType(String name, BlockState leaves, BlockState wood, BlockState sapling, boolean needsFourSaplings) {
		this.name = name;
		this.leaves = leaves;
		this.wood = wood;
		this.sapling = sapling;
		this.needsFourSaplings = needsFourSaplings;
	}

	public int getLeafMetadata() {
		return this.leaves.getMetadata();
	}
}
