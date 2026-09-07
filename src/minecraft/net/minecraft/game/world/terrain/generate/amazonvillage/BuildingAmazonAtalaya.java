package net.minecraft.game.world.terrain.generate.amazonvillage;

import net.minecraft.game.world.World;

public class BuildingAmazonAtalaya extends BuildingAmazon {

	public BuildingAmazonAtalaya(World world, boolean rotated) {
		super(world, rotated);
	} 

	@Override
	protected int buildingWidth() {
		return 9;
	}

	@Override
	protected int buildingLength() {
		return 9;
	}

	@Override
	protected int buildingHeight() {
		return 16;
	}

	@Override
	public void generate() {
	}

	@Override
	public void build() {
		this.terraform(this.buildingHeight());
		
		this.setBlockColumnRelative(0, 3, new int[] {44});
		this.setBlockColumnRelative(0, 4, new int[] {44});
		this.setBlockColumnRelative(0, 5, new int[] {44});
		this.setBlockColumnRelative(1, 2, new int[] {44});
		this.setBlockColumnRelative(1, 3, new int[] {4});
		this.setBlockColumnRelative(1, 4, new int[] {4, -2, 0, 50 | (2 << 8)});
		this.setBlockColumnRelative(1, 5, new int[] {4});
		this.setBlockColumnRelative(1, 6, new int[] {44});
		this.setBlockColumnRelative(2, 1, new int[] {44});
		this.setBlockColumnRelative(2, 2, new int[] {4, -3, 43, 44, -3, 0, 44, -3, 0, 44});
		this.setBlockColumnRelative(2, 3, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(2, 4, new int[] {4, -2, 0, 17 | (4 << 8), 43, 43 | (3 << 8), -2, 0, 43, 43 | (3 << 8), -2, 0, 43, 43 | (5 << 8), 50 | (5 << 8), 0});
		this.setBlockColumnRelative(2, 5, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(2, 6, new int[] {4, -3, 43, 44, -3, 0, 44, -3, 0, 44});
		this.setBlockColumnRelative(2, 7, new int[] {44});
		this.setBlockColumnRelative(3, 0, new int[] {44});
		this.setBlockColumnRelative(3, 1, new int[] {4});
		this.setBlockColumnRelative(3, 2, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(3, 3, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(3, 4, new int[] {4, -2, 0, 50 | (1 << 8), 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(3, 5, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(3, 6, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(3, 7, new int[] {4});
		this.setBlockColumnRelative(3, 8, new int[] {44});
		this.setBlockColumnRelative(4, 0, new int[] {44});
		this.setBlockColumnRelative(4, 1, new int[] {4});
		this.setBlockColumnRelative(4, 2, new int[] {4, -3, 43 | (5 << 8), 43, 43 | (3 << 8), -2, 0, 43, 43 | (3 << 8), -2, 0, 43, 43 | (5 << 8), 50 | (5 << 8), 0});
		this.setBlockColumnRelative(4, 3, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(4, 4, new int[] {4, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(4, 5, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(4, 6, new int[] {4, -3, 43 | (5 << 8), 43, 43 | (3 << 8), -2, 0, 43, 43 | (3 << 8), -2, 0, 43, 43 | (5 << 8), 50 | (5 << 8), 0});
		this.setBlockColumnRelative(4, 7, new int[] {4});
		this.setBlockColumnRelative(4, 8, new int[] {44});
		this.setBlockColumnRelative(5, 0, new int[] {44});
		this.setBlockColumnRelative(5, 1, new int[] {4});
		this.setBlockColumnRelative(5, 2, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(5, 3, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(5, 4, new int[] {4, -13, 65 | (4 << 8)});
		this.setBlockColumnRelative(5, 5, new int[] {4, -3, 0, 5, -3, 0, 5, -3, 0, 5});
		this.setBlockColumnRelative(5, 6, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(5, 7, new int[] {4});
		this.setBlockColumnRelative(5, 8, new int[] {44});
		this.setBlockColumnRelative(6, 1, new int[] {44});
		this.setBlockColumnRelative(6, 2, new int[] {4, -3, 43, 44, -3, 0, 44, -3, 0, 44});
		this.setBlockColumnRelative(6, 3, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(6, 4, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 50 | (5 << 8), 0});
		this.setBlockColumnRelative(6, 5, new int[] {4, -3, 43 | (5 << 8), 43, -3, 43 | (3 << 8), 43, -3, 43 | (3 << 8), 43, 43 | (5 << 8), 44, 0});
		this.setBlockColumnRelative(6, 6, new int[] {4, -3, 43, 44, -3, 0, 44, -3, 0, 44});
		this.setBlockColumnRelative(6, 7, new int[] {44});
		this.setBlockColumnRelative(7, 2, new int[] {44});
		this.setBlockColumnRelative(7, 3, new int[] {4});
		this.setBlockColumnRelative(7, 4, new int[] {4});
		this.setBlockColumnRelative(7, 5, new int[] {4});
		this.setBlockColumnRelative(7, 6, new int[] {44});
		this.setBlockColumnRelative(8, 3, new int[] {44});
		this.setBlockColumnRelative(8, 4, new int[] {44});
		this.setBlockColumnRelative(8, 5, new int[] {44});
		
		this.addSpawnerOneshotRelative(3, 9, 4, "Amazon");
		this.addSpawnerOneshotRelative(3, 5, 4, "Amazon");
		this.addOwnedChestRelative(3, 13, 4, "Amazon");
	}
}
