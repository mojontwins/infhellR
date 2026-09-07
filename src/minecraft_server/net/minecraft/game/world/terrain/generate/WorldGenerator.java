package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockFluid;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.block.BlockIce;
import net.minecraft.game.world.block.BlockState;

public abstract class WorldGenerator {
	public abstract boolean generate(World world, Random rand, int x, int y, int z);

	public void setScale(double scaleX, double scaleY, double scaleZ) {
	}

	public boolean blockIsCube(World world, int x, int y, int z) {
		if (world.isBlockOpaqueCube(x, y, z))
			return true;
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		return (block instanceof BlockIce) || (block instanceof BlockFluid);
	}

	public void setBlockIfEmpty(World world, int x, int y, int z, int blockID) {
		if (0 == world.getBlockId(x, y, z))
			world.setBlock(x, y, z, blockID);
	}

	public void setBlockIfEmpty(World world, int x, int y, int z, int blockID, int meta) {
		if (0 == world.getBlockId(x, y, z))
			world.setBlockAndMetadata(x, y, z, blockID, meta);
	}

	public boolean isValidSoilForTree(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if (block == null)
			return false;
		return block.canGrowPlants();
	}

	// Helpers from extraBiomesXXL

	public boolean check2x2Trunk(int x, int y, int z, int height, BlockState logs, World world, boolean inWater) {
		if (inWater) {
			for (int y1 = y + 1; y1 < y + height; y1++) {
				Block b00 = world.getBlock(x, y1, z);
				Block b10 = world.getBlock(x + 1, y1, z);
				Block b01 = world.getBlock(x, y1, z + 1);
				Block b11 = world.getBlock(x + 1, y1, z + 1);
				if (b00 != null && !(b00.blockMaterial == Material.water) && !this.isReplaceable(b00))
					return false;
				if (b01 != null && !(b01.blockMaterial == Material.water) && !this.isReplaceable(b01))
					return false;
				if (b10 != null && !(b10.blockMaterial == Material.water) && !this.isReplaceable(b10))
					return false;
				if (b11 != null && !(b11.blockMaterial == Material.water) && !this.isReplaceable(b11))
					return false;
			}
		} else {
			for (int y1 = y + 1; y1 < y + height; y1++) {
				if (!world.isAirBlock(x, y1, z))
					return false;
				if (!world.isAirBlock(x + 1, y1, z))
					return false;
				if (!world.isAirBlock(x, y1, z + 1))
					return false;
				if (!world.isAirBlock(x + 1, y1, z + 1))
					return false;
			}
		}

		return true;
	}

	public boolean place2x2Trunk(int x, int y, int z, int height, BlockState logs, World world) {
		for (int y1 = y; y1 < y + height; y1++) {
			world.setBlockAndMetadata(x, y1, z, logs);
			world.setBlockAndMetadata(x + 1, y1, z, logs);
			world.setBlockAndMetadata(x, y1, z + 1, logs);
			world.setBlockAndMetadata(x + 1, y1, z + 1, logs);
		}

		return true;
	}

	private boolean isReplaceable(Block block) {
		return block == null || !block.isOpaqueCube() || block.blockID == Block.leaves.blockID;
	}

	public boolean checkLeafCluster(World world, int x, int y, int z, int height, int radius) {
		for (int layer = -height; layer <= height; layer++) {
			if (!this.checkLeavesCircle(x, y + layer, z, radius * Math.cos(layer / (height / 1.3)), world))
				return false;
		}

		return true;
	}

	public void generateLeafCluster(World world, int x, int y, int z, int height, int radius, BlockState leaves) {
		for (int layer = -height; layer <= height; layer++) {
			this.placeLeavesCircle(x, y + layer, z, radius * Math.cos(layer / (height / 1.3)), leaves, world);
		}
	}

	public boolean checkLeavesCircle(double x, int y, double z, double r, World world) {
		double dist = r * r;

		for (double z1 = Math.floor(-r); z1 < r + 1; z1++) {
			for (double x1 = Math.floor(-r); x1 < r + 1; x1++) {
				int x2 = (int) (x1 + x);
				int z2 = (int) (z1 + z);

				final Block block = world.getBlock(x2, y, z2);

				if (((x1 * x1) + (z1 * z1)) <= dist) {
					if (block != null && block.isOpaqueCube())
						return false;
				}
			}
		}

		return true;
	}

	public void placeLeavesCircle(double x, int y, double z, double r, BlockState leaves, World world) {
		double dist = r * r;

		for (double z1 = Math.floor(-r); z1 < r + 1; z1++) {
			for (double x1 = Math.floor(-r); x1 < r + 1; x1++) {
				int x2 = (int) (x1 + x);
				int z2 = (int) (z1 + z);

				final Block block = world.getBlock(x2, y, z2);

				if ((((x1 * x1) + (z1 * z1)) <= dist) && this.isReplaceable(block)) {
					world.setBlockAndMetadata(x2, y, z2, leaves);
				}
			}
		}
	}

	public boolean checkBlockLine(int[] start, int[] end, BlockState logs, World world) {
		if (start.length != 3 || end.length != 3)
			return false;

		// Get the direction vector
		int[] direction = { start[0] - end[0], start[1] - end[1], start[2] - end[2] };
		if (Math.abs(direction[2]) > Math.abs(direction[1]) && Math.abs(direction[2]) > Math.abs(direction[0])) {
			// We are going to use the y axis as our major axis
			if (direction[2] >= 0) {
				for (int z = start[2]; z >= end[2]; z--) {
					double m = (z - start[2]) / (double) direction[2];
					int x = (int) (start[0] + (direction[0] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			} else {
				for (int z = start[2]; z <= end[2]; z++) {
					double m = (z - start[2]) / (double) direction[2];
					int x = (int) (start[0] + (direction[0] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			}
		} else if (Math.abs(direction[0]) > Math.abs(direction[1])) {
			// Treverse along the x axis
			if (direction[0] >= 0) {
				for (int x = start[0]; x >= end[0]; x--) {
					double m = (x - start[0]) / (double) direction[0];
					int z = (int) (start[2] + (direction[2] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			} else {
				for (int x = start[0]; x <= end[0]; x++) {
					double m = (x - start[0]) / (double) direction[0];
					int z = (int) (start[2] + (direction[2] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			}
		} else {
			// We will use the y axis as our major axis
			if (direction[1] >= 0) {
				for (int y = start[1]; y >= end[1]; y--) {
					double m = (y - start[1]) / (double) direction[1];
					int x = (int) (start[0] + (direction[0] * m));
					int z = (int) (start[2] + (direction[2] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			} else {
				for (int y = start[1]; y <= end[1]; y++) {
					double m = (y - start[1]) / (double) direction[1];
					int x = (int) (start[0] + (direction[0] * m));
					int z = (int) (start[2] + (direction[2] * m));
					if (!world.isAirBlock(x, y, z))
						return false;
				}
			}
		}

		return true;
	}

	public boolean placeBlockLine(int[] start, int[] end, BlockState log, World world) {
		int logBlock = log.getBlock().blockID;
		int logMeta = log.getMetadata();

		if (start.length != 3 || end.length != 3)
			return false;

		// Get the direction vector
		int[] direction = { start[0] - end[0], start[1] - end[1], start[2] - end[2] };
		if (Math.abs(direction[2]) > Math.abs(direction[1]) && Math.abs(direction[2]) > Math.abs(direction[0])) {
			// We are going to use the y axis as our major axis
			if (direction[2] >= 0) {
				for (int z = start[2]; z >= end[2]; z--) {
					double m = (z - start[2]) / (double) direction[2];
					int x = (int) (start[0] + (direction[0] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta | 4);
				}
			} else {
				for (int z = start[2]; z <= end[2]; z++) {
					double m = (z - start[2]) / (double) direction[2];
					int x = (int) (start[0] + (direction[0] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta | 4);
				}
			}
		} else if (Math.abs(direction[0]) > Math.abs(direction[1])) {
			// Treverse along the x axis
			if (direction[0] >= 0) {
				for (int x = start[0]; x >= end[0]; x--) {
					double m = (x - start[0]) / (double) direction[0];
					int z = (int) (start[2] + (direction[2] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta | 12);
				}
			} else {
				for (int x = start[0]; x <= end[0]; x++) {
					double m = (x - start[0]) / (double) direction[0];
					int z = (int) (start[2] + (direction[2] * m));
					int y = (int) (start[1] + (direction[1] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta | 12);
				}
			}
		} else {
			// We will use the y axis as our major axis
			if (direction[1] >= 0) {
				for (int y = start[1]; y >= end[1]; y--) {
					double m = (y - start[1]) / (double) direction[1];
					int x = (int) (start[0] + (direction[0] * m));
					int z = (int) (start[2] + (direction[2] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta);
				}
			} else {
				for (int y = start[1]; y <= end[1]; y++) {
					double m = (y - start[1]) / (double) direction[1];
					int x = (int) (start[0] + (direction[0] * m));
					int z = (int) (start[2] + (direction[2] * m));
					if (world.isAirBlock(x, y, z))
						world.setBlockAndMetadata(x, y, z, logBlock, logMeta);
				}
			}
		}

		return true;
	}

	public boolean placeKnee(int x, int y, int z, int height, int direction, BlockState logs, BlockState knees,
			World world) {
		int logBlock = logs.getBlock().blockID;
		int logMeta = logs.getMetadata();

		if (direction > 3)
			return false;

		int orientation = 0;

		switch (direction) {
		case 0:
			orientation = 8;
			break;
		case 1:
			orientation = 12;
			break;
		case 2:
			orientation = 8;
			break;
		case 3:
			orientation = 12;
			break;
		default:
			break;
		}

		for (int y1 = y - 1; y1 > 1; y1--) {
			Block block = world.getBlock(x, y1, z);
			if (!this.isReplaceable(block))
				break;

			// If there is an air block here place a root log
			world.setBlockAndMetadata(x, y1, z, logBlock, logMeta);
		}

		for (int y1 = y; y1 < y + height - 1; y1++) {
			world.setBlockAndMetadata(x, y1, z, logBlock, logMeta);
		}

		// Place the knee on top
		world.setBlockAndMetadata(x, y + height - 1, z, knees.getBlock().blockID, knees.getMetadata() | orientation);

		return true;
	}

}
