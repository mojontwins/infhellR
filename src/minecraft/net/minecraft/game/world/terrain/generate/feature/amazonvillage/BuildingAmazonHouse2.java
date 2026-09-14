package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import net.minecraft.game.world.World;

public class BuildingAmazonHouse2 extends BuildingAmazon {

	public BuildingAmazonHouse2(World world, boolean rotated) {
		super(world, rotated);
	}

	@Override
	protected int buildingWidth() {
		return 9;
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
		this.terraform(this.buildingHeight());
		
		this.setBlockColumnRelative(0, 0, new int[] {-4, 17, 5, 17});
		this.setBlockColumnRelative(0, 1, new int[] {4, 85, 0, 17 | (4 << 8), 5, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 2, new int[] {4, 85, 0, 17 | (4 << 8), 5, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 3, new int[] {4, 85, 0, 17 | (4 << 8), 5, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 4, new int[] {4, 85, 0, 17 | (4 << 8), 5, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 5, new int[] {4, 85, 0, 17 | (4 << 8), 5, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 6, new int[] {-4, 17, 5, 17});
		this.setBlockColumnRelative(1, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(1, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(1, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(1, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(1, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(1, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(1, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(2, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(2, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(2, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(2, 3, new int[] {-2, 85, 89, 0, 5});
		this.setBlockColumnRelative(2, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(2, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(2, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(3, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(3, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(3, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(3, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(3, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(3, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(3, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(4, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(4, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(4, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(4, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(4, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(4, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(4, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(5, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(5, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(5, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(5, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(5, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(5, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(5, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(6, 0, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(6, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(6, 2, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(6, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(6, 4, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(6, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(6, 6, new int[] {4, 85, 0, 17 | (12 << 8), 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(7, 0, new int[] {-4, 17, 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(7, 1, new int[] {4, -2, 0, 17 | (4 << 8), 5, -2, 17});
		this.setBlockColumnRelative(7, 2, new int[] {4, -2, 0, 17 | (4 << 8), 5, 85, 5});
		this.setBlockColumnRelative(7, 3, new int[] {-3, 0, 17 | (4 << 8), 5, 85, 5});
		this.setBlockColumnRelative(7, 4, new int[] {4, -2, 0, 17 | (4 << 8), 5, 85, 5});
		this.setBlockColumnRelative(7, 5, new int[] {4, -2, 0, 17 | (4 << 8), 5, -2, 17});
		this.setBlockColumnRelative(7, 6, new int[] {-4, 17, 5, 17 | (12 << 8)});
		this.setBlockColumnRelative(8, 0, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(8, 1, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(8, 2, new int[] {17, 85, 50 | (5 << 8), 0, 5});
		this.setBlockColumnRelative(8, 3, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(8, 4, new int[] {17, 85, 50 | (5 << 8), 0, 5});
		this.setBlockColumnRelative(8, 5, new int[] {-4, 0, 5});
		this.setBlockColumnRelative(8, 6, new int[] {-4, 0, 5});

		this.addSpawnerOneshotRelative(1, 0, 5, "Amazon");
		this.addOwnedChestRelative(1, 0, 1, "Amazon");
	}

}
