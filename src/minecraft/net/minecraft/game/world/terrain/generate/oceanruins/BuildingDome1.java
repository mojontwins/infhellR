package net.minecraft.game.world.terrain.generate.oceanruins;

import net.minecraft.game.world.World;

public class BuildingDome1 extends BuildingOceanRuin {

	public BuildingDome1(World world, boolean rotated) {
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
		this.setBlockColumnRelative(0, 1, new int[] {-2, 13});
		this.setBlockColumnRelative(0, 2, new int[] {13});
		this.setBlockColumnRelative(0, 3, new int[] {13});
		this.setBlockColumnRelative(0, 4, new int[] {-2, 13});
		this.setBlockColumnRelative(0, 5, new int[] {13});
		this.setBlockColumnRelative(1, 0, new int[] {-2, 13});
		this.setBlockColumnRelative(1, 1, new int[] {-3, 13});
		this.setBlockColumnRelative(1, 2, new int[] {13, -3, 98});
		this.setBlockColumnRelative(1, 3, new int[] {13, 98, 4, -3, 98, -1});
		this.setBlockColumnRelative(1, 4, new int[] {13, -3, 98});
		this.setBlockColumnRelative(1, 5, new int[] {13});
		this.setBlockColumnRelative(1, 6, new int[] {13});
		this.setBlockColumnRelative(2, 0, new int[] {13});
		this.setBlockColumnRelative(2, 1, new int[] {13, -3, 98});
		this.setBlockColumnRelative(2, 2, new int[] {13});
		this.setBlockColumnRelative(2, 3, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(2, 4, new int[] {13});
		this.setBlockColumnRelative(2, 5, new int[] {13, -3, 98});
		this.setBlockColumnRelative(2, 6, new int[] {-2, 13});
		this.setBlockColumnRelative(3, 0, new int[] {13});
		this.setBlockColumnRelative(3, 1, new int[] {13, -2, 98, 4, -2, 98, -1});
		this.setBlockColumnRelative(3, 2, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(3, 3, new int[] {-2, 0, -4, -1, 147});
		this.setBlockColumnRelative(3, 4, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(3, 5, new int[] {13, 98, 4, -3, 98, -1});
		this.setBlockColumnRelative(3, 6, new int[] {-3, 13});
		this.setBlockColumnRelative(4, 0, new int[] {13});
		this.setBlockColumnRelative(4, 1, new int[] {13, -3, 98});
		this.setBlockColumnRelative(4, 2, new int[] {13});
		this.setBlockColumnRelative(4, 3, new int[] {13, -5, -1, 98});
		this.setBlockColumnRelative(4, 4, new int[] {13});
		this.setBlockColumnRelative(4, 5, new int[] {13, 98, -2, 4});
		this.setBlockColumnRelative(4, 6, new int[] {13});
		this.setBlockColumnRelative(5, 0, new int[] {13});
		this.setBlockColumnRelative(5, 1, new int[] {13});
		this.setBlockColumnRelative(5, 2, new int[] {13, 98, 4, 98});
		this.setBlockColumnRelative(5, 3, new int[] {13, -5, 98, -1});
		this.setBlockColumnRelative(5, 4, new int[] {13, -3, 98});
		this.setBlockColumnRelative(5, 5, new int[] {-2, 13});
		this.setBlockColumnRelative(6, 1, new int[] {13});
		this.setBlockColumnRelative(6, 2, new int[] {13});
		this.setBlockColumnRelative(6, 3, new int[] {13});
		this.setBlockColumnRelative(6, 5, new int[] {13});
		
		// Special block: 3, 0, 3, 250, 0
		this.addChestRelative(3, 0, 3);
		
		// Special block: 3, 1, 3, 251, 0
		this.addSpawnerRelative(3, 1, 3, "Triton");
	}

}
