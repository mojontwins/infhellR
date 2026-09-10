package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.tree.EnumTreeType;
import net.minecraft.game.Direction;
import net.minecraft.game.world.World;

public class WorldGenWillow extends WorldGenMojon {
	private int height = 5;

	private static final int leavesID = Block.leaves.blockID;
	private static final int leavesMeta = EnumTreeType.WILLOW.getLeafMetadata();;
	private static final int trunkID = Block.wood.blockID;
	private static final int trunkMeta = 0;
	
	public WorldGenWillow(int height) {
		this(height, false);
	}
	
	public WorldGenWillow(int height, boolean withNotify) {
		super(withNotify);
		this.height = height;
	}

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		
		// Check if tree can grow here
		if(!this.validGround(world, x0, y0 - 1, z0)) return false;
		if(y0 + this.height > Chunk.SECTION_HEIGHT - 2) return false;
		
		// Make trunk
		
		int radius = 3;
		
		BlockPos src = new BlockPos().set(x0, y0, z0);
		BlockPos dst = new BlockPos().set(
				x0 + rand.nextInt((radius << 1) + 1) - radius,
				this.height + y0,
				z0 + rand.nextInt((radius << 1) + 1) - radius
		);
		
		this.bresenham(world, src, dst, trunkID, trunkMeta);
		
		// Make crown
		
		for(int y = 0; y <= 1; y ++) {
			this.squareTreeLayer(world, rand, dst.x, dst.y + y, dst.z, 3, leavesID, leavesMeta);
		}
		dst.move(Direction.UP);
	
		// Throw branches
		
		BlockPos branchCursor = new BlockPos();
		int amp, mulX, mulZ;

		for(int j = 0; j <= 1; j ++) {

			for(int direction : Direction.HORZ_PLANE) {
				// Start here
				branchCursor.set(dst);

				// Move away from the center
				branchCursor.move(direction, 2);

				// Paint down
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);

				branchCursor.move(Direction.DOWN);
				amp = rand.nextInt(2);
				this.setBlockIfEmpty(world,
						branchCursor.x + Direction.offsetX[direction] * amp,
						branchCursor.y,
						branchCursor.z + Direction.offsetZ[direction] * amp, leavesID, leavesMeta);

				// Move sideways, at random.
				int sideWays = Direction.getCW(direction);
				int amount = rand.nextInt(3) - 1;
				int vx = Direction.offsetX[sideWays] * amount;
				int vz = Direction.offsetZ[sideWays] * amount;

				int vx2 = vx; if(rand.nextBoolean()) vx2 += vx;
				int vz2 = vx; if(rand.nextBoolean()) vz2 += vz;

				branchCursor.move(Direction.DOWN).move(direction);
				this.setBlockIfEmpty(world, branchCursor.x + vx, branchCursor.y, branchCursor.z + vz, leavesID, leavesMeta);
				branchCursor.move(Direction.DOWN);
				this.setBlockIfEmpty(world, branchCursor.x + vx, branchCursor.y, branchCursor.z + vz, leavesID, leavesMeta);

				int branchL = 2 + rand.nextInt(3); for(int i = 0; i < branchL; i ++) {
					branchCursor.move(Direction.DOWN);
					this.setBlockIfEmpty(world, branchCursor.x + vx2, branchCursor.y, branchCursor.z + vz2, leavesID, leavesMeta);
				}
			}

			for(int direction : Direction.HORZ_PLANE_DIAGONALS) {
				mulX = rand.nextInt(2);
				mulZ = 1 - mulX;

				// Start here
				branchCursor.set(dst);

				// Move away from the center
				branchCursor.x += Direction.offsetX[direction] * (mulX + 1);
				branchCursor.z += Direction.offsetZ[direction] * (mulZ + 1);

				// Paint down
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);

				// Move away from the center
				branchCursor.x += Direction.offsetX[direction];
				branchCursor.z += Direction.offsetZ[direction];

				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);
				this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);

				// Random
				branchCursor.x += Direction.offsetX[direction] * rand.nextInt(2);
				branchCursor.z += Direction.offsetZ[direction] * rand.nextInt(2);
				int branchL = 2 + rand.nextInt(3); for(int i = 0; i < branchL; i ++) {
					this.setBlockIfEmpty(world, branchCursor.move(Direction.DOWN), leavesID, leavesMeta);
				}
			}

		}

		return true;
	}

}
