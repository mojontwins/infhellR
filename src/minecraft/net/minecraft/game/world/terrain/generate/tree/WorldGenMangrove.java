package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.Direction;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.terrain.generate.WorldGenMojon;
import net.minecraft.game.world.World;

public class WorldGenMangrove extends WorldGenMojon {
	
	private static final int leavesID = Block.leaves.blockID;
	private static final int leavesMeta = (2 | EnumTreeType.MANGROVE.getLeafMetadata());
	private static final int trunkID = Block.wood.blockID;
	private static final int trunkMeta = 0;

	public boolean fromSapling;
	
	public WorldGenMangrove(boolean withNotify) {
		super(withNotify);
	}
	
	public WorldGenMangrove(boolean withNotify, boolean fromSapling) {
		super(withNotify);
		this.fromSapling = fromSapling;
	}

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		BlockPos origin = new BlockPos().set(x0, y0, z0);
		
		if(fromSapling) {
			Block beneath = world.getBlock(x0, y0 - 1, z0);
			if(beneath == null || !beneath.canGrowPlants())  return false; 

		} else {
			
			// Discard if not on water
			
			Material materialBeneath = world.getBlockMaterial(x0, y0 - 1, z0);
			Material materialOn = world.getBlockMaterial(x0, y0, z0);
		
			if(materialBeneath != Material.water && materialOn != Material.water) return false; 
		}

		// Main trunk
		this.buildBranch(world, rand, origin, 5, 5 + rand.nextInt(4), 0.0D, 0.0D);
		
		// Branches
		int branches = rand.nextInt(3);
		
		// Start at a random angle
		double angleOffs = 2 * Math.PI * rand.nextDouble();
		
		// Each branch is 1/3 of the circumference appart
		for(int i = 0; i < branches; i ++) {
			this.buildBranch(
					world, rand,
					origin,
					7 + i,
					(double) (6 + rand.nextInt(3)),
					2 * Math.PI * i / 3 + angleOffs,
					0.25 * Math.PI);
		}
		
		// Roots
		int roots = 3 + rand.nextInt(2);
		angleOffs = 2 * Math.PI * rand.nextDouble();
		
		for(int i = 0; i < roots; i ++) {
			this.buildRoot(
					world, rand,
					origin,
					5,
					8.0D,
					0.8 * Math.PI * i + angleOffs,
					0.75 * Math.PI + rand.nextDouble() * .1D
			);
		}
		
		return true;
	}

	private void buildRoot(World world, Random rand, BlockPos origin, int height, double length, double angle, double tilt) {
		BlockPos src = origin.copy().move(Direction.UP, height);
		BlockPos dst = this.findEndPoint(src, length, angle, tilt);
		
		this.bresenham(world, src, dst, trunkID, trunkMeta);
		
		// Expand to ground
		while(dst.y > 8) {
			if(world.getBlockMaterial(dst) != Material.water) break;
			dst.move(Direction.DOWN);
			this.setBlockWithMetadata(world, dst, trunkID, trunkMeta);
		}
	}

	private void buildBranch(World world, Random rand, BlockPos origin, int height, double length, double angle, double tilt) {
		BlockPos src = origin.copy().move(Direction.UP, height);
		BlockPos dst = this.findEndPoint(src, length, angle, tilt);
		
		this.bresenham(world, src, dst, trunkID, trunkMeta);
		
		for(int direction : Direction.HORZ_PLANE) {
			this.setBlockWithMetadata(world, dst.copy().move(direction), trunkID, trunkMeta);
		}
		
		int canopy = 2 + rand.nextInt(3);
		this.circle(world, dst.copy().move(Direction.DOWN), canopy - 1, leavesID, leavesMeta);
		this.circle(world, dst, canopy, leavesID, leavesMeta);
		this.circle(world, dst.copy().move(Direction.UP), canopy - 2, leavesID, leavesMeta);
	}

}
