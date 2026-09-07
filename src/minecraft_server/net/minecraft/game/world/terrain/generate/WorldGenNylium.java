package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.world.terrain.generate.tree.EnumTreeType;
import net.minecraft.game.Direction;
import net.minecraft.game.world.World;

public class WorldGenNylium extends WorldGenMojon {

	private static final int leavesID = Block.leaves.blockID;
	private static final int leavesMeta = (2 | EnumTreeType.MANGROVE.getLeafMetadata());
	private static final int trunkID = Block.wood.blockID;
	private static final int trunkMeta = 0;
	
	public WorldGenNylium(boolean withNotify) {
		super(withNotify);
	}

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		BlockPos origin = new BlockPos().set(x0, y0, z0);
		
		int height = 9 + rand.nextInt(6);
		
		// Ground check / fits
		
		if(!this.validGround(world, x0, y0 - 1, z0) ||
				y0 + height > world.getWorldHeight() - 1 ||
				world.isBlockOpaqueCube(x0, y0 + height, z0)) {
			return false;
		}
	
		// Main trunk
		this.buildBranch(world, rand, origin, 0, (double)height, 0D, 0D);
		this.setBlockWithMetadata(world, x0, y0 - 1, z0, trunkID, trunkMeta);
		
		// Branches
		int branches = 3 + rand.nextInt(3);
		for (int i = 0; i < branches; i ++) {
			this.buildBranch(world, rand, origin, height - 10 + i, 9D, 2 * Math.PI * i / 3, Math.PI * .2D + rand.nextDouble() * .2D - .1D);
		}
		
		// Roots
		int roots = 3 + rand.nextInt(2);
		double angleOffs = 2 * Math.PI * rand.nextDouble();
		
		for(int i = 0; i < roots; i ++) {
			this.buildRoot(
					world, rand,
					origin,
					angleOffs,
					i
			);
		}
		
		return true;
	}

	private void buildRoot(World world, Random rand, BlockPos origin, double angleOffs, int i) {
		BlockPos src = origin.copy().move(Direction.UP , i + 2);
		BlockPos dst = this.findEndPoint(src, 6.0D, i * 2 * Math.PI / 3, .8D);
		if(world.getBlockMaterial(dst).isSolid()) {
			this.bresenham(world, src, dst, trunkID, trunkMeta);
		}
		
	}

	private void buildBranch(World world, Random rand, BlockPos origin, int height, double length, double angle, double tilt) {
		BlockPos src = origin.copy().move(Direction.UP, height);
		BlockPos dst = this.findEndPoint(src, length, angle, tilt);
		
		this.bresenham(world, src, dst, trunkID, trunkMeta);
		
		for(int direction : Direction.HORZ_PLANE) {
			this.setBlockWithMetadata(world, dst.copy().move(direction), trunkID, trunkMeta);
		}
		
		this.circle(world, dst.copy().move(Direction.DOWN), 3, leavesID, leavesMeta);
		this.circle(world, dst, 4, leavesID, leavesMeta);
		this.circle(world, dst.copy().move(Direction.UP), 2, leavesID, leavesMeta);
	}

	
	
}
