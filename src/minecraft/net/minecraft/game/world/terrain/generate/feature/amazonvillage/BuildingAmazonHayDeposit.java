package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import net.minecraft.game.world.World;

public class BuildingAmazonHayDeposit extends BuildingAmazon {
	public BuildingAmazonHayDeposit(World world, boolean rotated) {
		super(world, rotated);
	} 

	@Override
	protected int buildingWidth() {
		return 5;
	}

	@Override
	protected int buildingLength() {
		return 7;
	}

	@Override
	protected int buildingHeight() {
		return 5;
	}
	
	@Override
	public void generate() {
	}

	@Override
	public void build() {
		this.terraform(this.buildingHeight());
		
		this.setBlockColumnRelative(0, 0, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 1, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 2, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 3, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 4, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 5, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(0, 6, new int[] {17 | (4 << 8), -15, 0});
		this.setBlockColumnRelative(1, 0, new int[] {0, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 1, new int[] {156, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 2, new int[] {0, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 3, new int[] {156, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 4, new int[] {156, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 5, new int[] {0, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(1, 6, new int[] {0, 17 | (4 << 8), -14, 0});
		this.setBlockColumnRelative(2, 0, new int[] {-2, 0, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 1, new int[] {156, 0, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 2, new int[] {-2, 156, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 3, new int[] {-2, 156, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 4, new int[] {156, 0, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 5, new int[] {-2, 0, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(2, 6, new int[] {-2, 0, 17 | (4 << 8), -13, 0});
		this.setBlockColumnRelative(3, 0, new int[] {-3, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 1, new int[] {156, -2, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 2, new int[] {156, -2, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 3, new int[] {-3, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 4, new int[] {-3, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 5, new int[] {156, -2, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(3, 6, new int[] {-3, 0, 17 | (4 << 8), -12, 0});
		this.setBlockColumnRelative(4, 0, new int[] {-4, 85, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 1, new int[] {-4, 0, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 2, new int[] {-2, 156, -2, 0, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 3, new int[] {-4, 0, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 4, new int[] {-4, 0, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 5, new int[] {-4, 0, 17 | (4 << 8), -11, 0});
		this.setBlockColumnRelative(4, 6, new int[] {-4, 85, 17 | (4 << 8), -11, 0});
	}

}
