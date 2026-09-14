package net.minecraft.game.world.terrain.generate.feature.oceanruins;

import net.minecraft.game.world.World;

public class BuildingBuild2 extends BuildingOceanRuin {

	public BuildingBuild2(World world, boolean rotated) {
		super(world, rotated);
	}

	@Override
	protected int buildingWidth() {
		return 15;
	}

	@Override
	protected int buildingLength() {
		return 15;
	}

	@Override
	protected int buildingHeight() {
		return 12;
	}

	@Override
	public void generate() {
	}

	@Override
	public void build() {
		this.setBlockColumnRelative(0, 3, new int[] {13});
		this.setBlockColumnRelative(0, 9, new int[] {13});
		this.setBlockColumnRelative(0, 10, new int[] {13});
		this.setBlockColumnRelative(0, 11, new int[] {13});
		this.setBlockColumnRelative(0, 13, new int[] {13});
		this.setBlockColumnRelative(1, 2, new int[] {13});
		this.setBlockColumnRelative(1, 3, new int[] {13});
		this.setBlockColumnRelative(1, 4, new int[] {13});
		this.setBlockColumnRelative(1, 5, new int[] {13});
		this.setBlockColumnRelative(1, 8, new int[] {13});
		this.setBlockColumnRelative(1, 9, new int[] {13});
		this.setBlockColumnRelative(1, 10, new int[] {13});
		this.setBlockColumnRelative(1, 11, new int[] {13});
		this.setBlockColumnRelative(1, 12, new int[] {13});
		this.setBlockColumnRelative(2, 2, new int[] {13});
		this.setBlockColumnRelative(2, 3, new int[] {-2, 13});
		this.setBlockColumnRelative(2, 4, new int[] {13, 4, -3, 98});
		this.setBlockColumnRelative(2, 5, new int[] {13, 4, 98, 4, 98});
		this.setBlockColumnRelative(2, 6, new int[] {13, 48, 4, -1, 98});
		this.setBlockColumnRelative(2, 7, new int[] {13, 4, -2, 98, 4});
		this.setBlockColumnRelative(2, 8, new int[] {13, 4, -3, 98});
		this.setBlockColumnRelative(2, 9, new int[] {-2, 13});
		this.setBlockColumnRelative(2, 10, new int[] {-2, 13});
		this.setBlockColumnRelative(2, 11, new int[] {13});
		this.setBlockColumnRelative(2, 12, new int[] {13});
		this.setBlockColumnRelative(3, 0, new int[] {13});
		this.setBlockColumnRelative(3, 1, new int[] {-3, 13});
		this.setBlockColumnRelative(3, 2, new int[] {-2, 13});
		this.setBlockColumnRelative(3, 3, new int[] {13, 4, -2, 98, 4});
		this.setBlockColumnRelative(3, 4, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(3, 5, new int[] {13, -4, -1, 48});
		this.setBlockColumnRelative(3, 6, new int[] {13, 4, -3, -1, 98});
		this.setBlockColumnRelative(3, 7, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(3, 8, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(3, 9, new int[] {13, -3, 98, 48});
		this.setBlockColumnRelative(3, 10, new int[] {-3, 13});
		this.setBlockColumnRelative(3, 11, new int[] {-2, 13});
		this.setBlockColumnRelative(3, 12, new int[] {13});
		this.setBlockColumnRelative(3, 13, new int[] {13});
		this.setBlockColumnRelative(4, 0, new int[] {13});
		this.setBlockColumnRelative(4, 1, new int[] {13});
		this.setBlockColumnRelative(4, 2, new int[] {13, 4, -2, 98});
		this.setBlockColumnRelative(4, 3, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(4, 4, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(4, 5, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(4, 6, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(4, 7, new int[] {13, -5, -1, 4});
		this.setBlockColumnRelative(4, 8, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(4, 9, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(4, 10, new int[] {13, -4, 98});
		this.setBlockColumnRelative(4, 11, new int[] {-2, 13});
		this.setBlockColumnRelative(4, 12, new int[] {13});
		this.setBlockColumnRelative(4, 13, new int[] {13});
		this.setBlockColumnRelative(4, 14, new int[] {13});
		this.setBlockColumnRelative(5, 0, new int[] {13});
		this.setBlockColumnRelative(5, 1, new int[] {13, 4, -2, 98, 4});
		this.setBlockColumnRelative(5, 2, new int[] {-2, 13, -3, -1, 98});
		this.setBlockColumnRelative(5, 3, new int[] {13, 0, -4, -1, 98});
		this.setBlockColumnRelative(5, 4, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(5, 5, new int[] {13, -6, -1, 4});
		this.setBlockColumnRelative(5, 6, new int[] {13, -6, -1, 48});
		this.setBlockColumnRelative(5, 7, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(5, 8, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(5, 9, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(5, 10, new int[] {13, -3, 98, -1, 98});
		this.setBlockColumnRelative(5, 11, new int[] {-2, 13, -3, 98});
		this.setBlockColumnRelative(5, 12, new int[] {13});
		this.setBlockColumnRelative(5, 13, new int[] {13, -2, 98, 165 | (1 << 8)});
		this.setBlockColumnRelative(5, 14, new int[] {13});
		this.setBlockColumnRelative(6, 0, new int[] {13});
		this.setBlockColumnRelative(6, 1, new int[] {13, 98, 4, -2, 98});
		this.setBlockColumnRelative(6, 2, new int[] {-3, 13, -2, -1, 98});
		this.setBlockColumnRelative(6, 3, new int[] {-2, 13, -4, -1, 98});
		this.setBlockColumnRelative(6, 4, new int[] {-2, 13, -5, -1, 98});
		this.setBlockColumnRelative(6, 5, new int[] {13, -7, -1, 43});
		this.setBlockColumnRelative(6, 6, new int[] {13, -7, -1, 98});
		this.setBlockColumnRelative(6, 7, new int[] {13, 98, -6, -1, 43});
		this.setBlockColumnRelative(6, 8, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(6, 9, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(6, 10, new int[] {13, 48, 4, 98, -1, 98});
		this.setBlockColumnRelative(6, 11, new int[] {13, 48, 98, 147, 98});
		this.setBlockColumnRelative(6, 12, new int[] {13});
		this.setBlockColumnRelative(6, 13, new int[] {13});
		this.setBlockColumnRelative(6, 14, new int[] {13});
		this.setBlockColumnRelative(7, 0, new int[] {13});
		this.setBlockColumnRelative(7, 1, new int[] {13, 98, -1, -2, 98});
		this.setBlockColumnRelative(7, 2, new int[] {-2, 13, -3, -1, 98});
		this.setBlockColumnRelative(7, 3, new int[] {0, 13, -4, -1, 98});
		this.setBlockColumnRelative(7, 4, new int[] {-2, 13, -5, -1, 98});
		this.setBlockColumnRelative(7, 5, new int[] {13, -7, -1, 98});
		this.setBlockColumnRelative(7, 6, new int[] {13, -7, -1, 43});
		this.setBlockColumnRelative(7, 7, new int[] {13, -7, -1, 98});
		this.setBlockColumnRelative(7, 8, new int[] {5, -6, -1, 98});
		this.setBlockColumnRelative(7, 9, new int[] {5, -5, -1, 98});
		this.setBlockColumnRelative(7, 10, new int[] {5, -2, -1, 147, -1, 98});
		this.setBlockColumnRelative(7, 11, new int[] {-3, 13, 98, 4});
		this.setBlockColumnRelative(7, 12, new int[] {13});
		this.setBlockColumnRelative(7, 13, new int[] {13});
		this.setBlockColumnRelative(8, 0, new int[] {13});
		this.setBlockColumnRelative(8, 1, new int[] {13, -3, 98, 4});
		this.setBlockColumnRelative(8, 2, new int[] {-3, 13, -2, -1, 98});
		this.setBlockColumnRelative(8, 3, new int[] {-3, 13, -3, -1, 98});
		this.setBlockColumnRelative(8, 4, new int[] {-2, 13, -5, -1, 98});
		this.setBlockColumnRelative(8, 5, new int[] {13, -7, -1, 43});
		this.setBlockColumnRelative(8, 6, new int[] {13, -7, -1, 98});
		this.setBlockColumnRelative(8, 7, new int[] {5, -7, -1, 43});
		this.setBlockColumnRelative(8, 8, new int[] {5, -6, -1, 98});
		this.setBlockColumnRelative(8, 9, new int[] {5, -5, -1, 98});
		this.setBlockColumnRelative(8, 10, new int[] {13, 4, -2, 98, -1, 4});
		this.setBlockColumnRelative(8, 11, new int[] {13, 48, 4, 147, 98});
		this.setBlockColumnRelative(8, 12, new int[] {13});
		this.setBlockColumnRelative(8, 13, new int[] {13});
		this.setBlockColumnRelative(8, 14, new int[] {13});
		this.setBlockColumnRelative(9, 0, new int[] {13});
		this.setBlockColumnRelative(9, 1, new int[] {13, -2, 98, 4, 98});
		this.setBlockColumnRelative(9, 2, new int[] {-4, 13, -1, 98});
		this.setBlockColumnRelative(9, 3, new int[] {13, 0, 13, -3, -1, 4});
		this.setBlockColumnRelative(9, 4, new int[] {-2, 13, -5, -1, 4});
		this.setBlockColumnRelative(9, 5, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(9, 6, new int[] {13, -6, -1, 98});
		this.setBlockColumnRelative(9, 7, new int[] {5, -6, -1, 98});
		this.setBlockColumnRelative(9, 8, new int[] {5, -6, -1, 98});
		this.setBlockColumnRelative(9, 9, new int[] {5, -5, -1, 4});
		this.setBlockColumnRelative(9, 10, new int[] {13, 48, 4, 98, -1, 98});
		this.setBlockColumnRelative(9, 11, new int[] {13, -1, -3, 98});
		this.setBlockColumnRelative(9, 12, new int[] {13});
		this.setBlockColumnRelative(9, 13, new int[] {13, -2, 98, 165 | (1 << 8)});
		this.setBlockColumnRelative(9, 14, new int[] {13});
		this.setBlockColumnRelative(10, 0, new int[] {13});
		this.setBlockColumnRelative(10, 1, new int[] {-2, 13});
		this.setBlockColumnRelative(10, 2, new int[] {13, -4, 98});
		this.setBlockColumnRelative(10, 3, new int[] {-2, 13, -3, -1, 4});
		this.setBlockColumnRelative(10, 4, new int[] {-2, 13, -4, -1, 4});
		this.setBlockColumnRelative(10, 5, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(10, 6, new int[] {5, -5, -1, 98});
		this.setBlockColumnRelative(10, 7, new int[] {5, -5, -1, 98});
		this.setBlockColumnRelative(10, 8, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(10, 9, new int[] {-3, 13, -2, -1, 98});
		this.setBlockColumnRelative(10, 10, new int[] {13, 4, -3, 98});
		this.setBlockColumnRelative(10, 11, new int[] {13});
		this.setBlockColumnRelative(10, 12, new int[] {13});
		this.setBlockColumnRelative(10, 13, new int[] {13});
		this.setBlockColumnRelative(11, 1, new int[] {13});
		this.setBlockColumnRelative(11, 2, new int[] {-2, 13});
		this.setBlockColumnRelative(11, 3, new int[] {13, 98, 43, -2, 98});
		this.setBlockColumnRelative(11, 4, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(11, 5, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(11, 6, new int[] {5, -4, -1, 4});
		this.setBlockColumnRelative(11, 7, new int[] {13, -4, -1, 98});
		this.setBlockColumnRelative(11, 8, new int[] {-3, 13, -2, -1, 98});
		this.setBlockColumnRelative(11, 9, new int[] {13, 4, 13, -2, 98});
		this.setBlockColumnRelative(11, 10, new int[] {-4, 13});
		this.setBlockColumnRelative(11, 11, new int[] {-2, 13});
		this.setBlockColumnRelative(11, 12, new int[] {13});
		this.setBlockColumnRelative(11, 13, new int[] {13});
		this.setBlockColumnRelative(11, 14, new int[] {13});
		this.setBlockColumnRelative(12, 2, new int[] {13});
		this.setBlockColumnRelative(12, 3, new int[] {-2, 13});
		this.setBlockColumnRelative(12, 4, new int[] {13, -3, 98, 4});
		this.setBlockColumnRelative(12, 5, new int[] {13, 98, -1, -2, 98});
		this.setBlockColumnRelative(12, 6, new int[] {13, -4, 98});
		this.setBlockColumnRelative(12, 7, new int[] {13, 98, -1, -2, 98});
		this.setBlockColumnRelative(12, 8, new int[] {13, 4, 13, -2, 98});
		this.setBlockColumnRelative(12, 9, new int[] {-3, 13});
		this.setBlockColumnRelative(12, 10, new int[] {-3, 13});
		this.setBlockColumnRelative(12, 11, new int[] {-2, 13});
		this.setBlockColumnRelative(12, 12, new int[] {13});
		this.setBlockColumnRelative(13, 3, new int[] {13});
		this.setBlockColumnRelative(13, 4, new int[] {13});
		this.setBlockColumnRelative(13, 5, new int[] {13});
		this.setBlockColumnRelative(13, 6, new int[] {13});
		this.setBlockColumnRelative(13, 7, new int[] {-2, 13});
		this.setBlockColumnRelative(13, 8, new int[] {-4, 13});
		this.setBlockColumnRelative(13, 9, new int[] {-2, 13});
		this.setBlockColumnRelative(13, 10, new int[] {13});
		this.setBlockColumnRelative(13, 11, new int[] {13});
		this.setBlockColumnRelative(13, 12, new int[] {13});
		this.setBlockColumnRelative(14, 6, new int[] {13});
		this.setBlockColumnRelative(14, 7, new int[] {13});
		this.setBlockColumnRelative(14, 8, new int[] {-2, 13});
		this.setBlockColumnRelative(14, 9, new int[] {13});
		this.setBlockColumnRelative(14, 10, new int[] {13});

		// Special block: 5, 1, 3, 251, 0
		this.addSpawnerRelative(5, 1, 3, "Triton");
		
		// Special block: 7, 0, 3, 250, 0
		this.addChestRelative(7, 0, 3);
		
		// Special block: 9, 1, 3, 251, 0
		this.addSpawnerRelative(9, 1, 3, "Triton");
	}

}
