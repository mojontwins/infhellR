package net.minecraft.game.world.terrain.generate.oceanruins;

import net.minecraft.game.world.World;

public class BuildingHut3 extends BuildingOceanRuin {

	public BuildingHut3(World world, boolean rotated) {
		super(world, rotated);
	}

	@Override
	protected int buildingWidth() {
		return 7;
	}

	@Override
	protected int buildingLength() {
		return 7;
	}

	@Override
	protected int buildingHeight() {
		return 7;
	}

	@Override
	public void generate() {
	}

	@Override
	public void build() {
		this.setBlockColumnRelative(0, 0, new int[] {-4, -1, 43});
		this.setBlockColumnRelative(0, 1, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(0, 2, new int[] {13});
		this.setBlockColumnRelative(0, 3, new int[] {13});
		this.setBlockColumnRelative(0, 4, new int[] {13});
		this.setBlockColumnRelative(0, 5, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(0, 6, new int[] {-4, -1, 43});
		this.setBlockColumnRelative(1, 0, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(1, 1, new int[] {98, -6, 43});
		this.setBlockColumnRelative(1, 2, new int[] {-4, 98, 48, 4, -1});
		this.setBlockColumnRelative(1, 3, new int[] {-2, 98, -1, -2, 4});
		this.setBlockColumnRelative(1, 4, new int[] {-3, 98, 4});
		this.setBlockColumnRelative(1, 5, new int[] {98, -6, 43});
		this.setBlockColumnRelative(1, 6, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(2, 0, new int[] {-2, 13});
		this.setBlockColumnRelative(2, 1, new int[] {-4, 98, -2, 4, -1});
		this.setBlockColumnRelative(2, 2, new int[] {5});
		this.setBlockColumnRelative(2, 3, new int[] {5});
		this.setBlockColumnRelative(2, 4, new int[] {5});
		this.setBlockColumnRelative(2, 5, new int[] {-7, 98});
		this.setBlockColumnRelative(2, 6, new int[] {13});
		this.setBlockColumnRelative(3, 0, new int[] {-2, 13});
		this.setBlockColumnRelative(3, 1, new int[] {-2, 98, -1, 98, 4});
		this.setBlockColumnRelative(3, 2, new int[] {13, 0});
		this.setBlockColumnRelative(3, 3, new int[] {13});
		this.setBlockColumnRelative(3, 4, new int[] {5});
		this.setBlockColumnRelative(3, 5, new int[] {5, -2, -1, 98, 4, 98, -1});
		this.setBlockColumnRelative(3, 6, new int[] {13});
		this.setBlockColumnRelative(4, 0, new int[] {13});
		this.setBlockColumnRelative(4, 1, new int[] {-3, 98, 4});
		this.setBlockColumnRelative(4, 2, new int[] {5});
		this.setBlockColumnRelative(4, 3, new int[] {5});
		this.setBlockColumnRelative(4, 4, new int[] {5});
		this.setBlockColumnRelative(4, 5, new int[] {-5, 98, 4, -1});
		this.setBlockColumnRelative(4, 6, new int[] {-2, 13});
		this.setBlockColumnRelative(5, 0, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(5, 1, new int[] {98, -5, 43, -1});
		this.setBlockColumnRelative(5, 2, new int[] {98, 48, 98, 4, 48});
		this.setBlockColumnRelative(5, 3, new int[] {-2, 98, -1, 98, 4});
		this.setBlockColumnRelative(5, 4, new int[] {-2, 98, -4, 4, -1});
		this.setBlockColumnRelative(5, 5, new int[] {98, -5, 43, -1});
		this.setBlockColumnRelative(5, 6, new int[] {13, -3, -1, 43});
		this.setBlockColumnRelative(6, 1, new int[] {13});
		this.setBlockColumnRelative(6, 2, new int[] {-2, 13});
		this.setBlockColumnRelative(6, 3, new int[] {13});
		this.setBlockColumnRelative(6, 4, new int[] {-2, 13});
		this.setBlockColumnRelative(6, 5, new int[] {-3, 13, -1, 43});
		this.setBlockColumnRelative(6, 6, new int[] {-4, -1, 43});

		// Special block: 3, 1, 2, 251, 0
		this.addSpawnerRelative(3, 1, 2, "Triton");
	}

}
