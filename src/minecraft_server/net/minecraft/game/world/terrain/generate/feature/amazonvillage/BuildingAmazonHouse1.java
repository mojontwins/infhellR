package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import net.minecraft.game.world.World;

public class BuildingAmazonHouse1 extends BuildingAmazon {

	public BuildingAmazonHouse1(World world, boolean rotated) {
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
		return 9;
	}
	
	@Override
	public void generate() {
	}

	@Override
	public void build() {
		this.terraform(this.buildingHeight());
		
		this.setBlockColumnRelative(0, 0, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 1, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 2, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 3, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 4, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 5, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 6, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 7, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(0, 8, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 0, new int[] {-6, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 1, new int[] {98, -3, 17, 5, 85, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 2, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 3, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 4, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 5, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 6, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 7, new int[] {98, -3, 17, 5, 85, 17 | (4 << 8)});
		this.setBlockColumnRelative(1, 8, new int[] {-6, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 0, new int[] {-7, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 1, new int[] {98, 85, -2, 0, 5, 85, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 2, new int[] {1, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 3, new int[] {98, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 4, new int[] {1, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 5, new int[] {4, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 6, new int[] {98, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 7, new int[] {98, 85, -2, 0, 5, 85, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(2, 8, new int[] {-7, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 0, new int[] {-8, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 1, new int[] {98, 85, -2, 0, 5, 85, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 2, new int[] {98, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 3, new int[] {98, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 4, new int[] {4, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 5, new int[] {1, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 6, new int[] {1, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 7, new int[] {98, 85, -2, 0, 5, 85, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(3, 8, new int[] {-8, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 0, new int[] {-9, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 1, new int[] {98, 85, -2, 0, 5, 85, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 2, new int[] {1, -3, 0, 5, -4, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 3, new int[] {98, -3, 0, 5, -4, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 4, new int[] {1, -3, 0, 5, -3, 85, 89, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 5, new int[] {1, -3, 0, 5, -4, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 6, new int[] {98, -3, 0, 5, -4, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 7, new int[] {98, -3, 0, 5, 85, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(4, 8, new int[] {67 | (3 << 8), -2, 0, 89, 85, -4, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 0, new int[] {-8, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 1, new int[] {98, 85, -2, 0, 5, 85, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 2, new int[] {4, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 3, new int[] {1, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 4, new int[] {4, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 5, new int[] {1, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 6, new int[] {1, -3, 0, 5, -3, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 7, new int[] {98, 85, -2, 0, 5, 85, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(5, 8, new int[] {-8, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 0, new int[] {-7, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 1, new int[] {98, 85, -2, 0, 5, 85, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 2, new int[] {4, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 3, new int[] {1, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 4, new int[] {98, -6, 65 | (4 << 8), 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 5, new int[] {4, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 6, new int[] {1, -3, 0, 5, -2, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 7, new int[] {98, 85, -2, 0, 5, 85, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(6, 8, new int[] {-7, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 0, new int[] {-6, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 1, new int[] {98, -3, 17, 5, 85, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 2, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 3, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 4, new int[] {98, -6, 17});
		this.setBlockColumnRelative(7, 5, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 6, new int[] {98, 85, -2, 0, 5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 7, new int[] {98, -3, 17, 5, 85, 17 | (4 << 8)});
		this.setBlockColumnRelative(7, 8, new int[] {-6, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 0, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 1, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 2, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 3, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 4, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 5, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 6, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 7, new int[] {-5, 0, 17 | (4 << 8)});
		this.setBlockColumnRelative(8, 8, new int[] {-5, 0, 17 | (4 << 8)});

		this.addSpawnerOneshotRelative(4, 1, 3, "Amazon");
		this.addOwnedChestRelative(2, 5, 4, "Amazon");
	}

}
