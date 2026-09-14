package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.World;

public class BuildingAmazonHut1 extends BuildingAmazon {

	public BuildingAmazonHut1(World world, boolean rotated) {
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
		this.terraform(this.buildingHeight());
		
		this.setBlockColumnRelative(0, 0, new int[] {-4, 17, 5});
		this.setBlockColumnRelative(0, 1, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(0, 2, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(0, 3, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(0, 4, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(0, 5, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(0, 6, new int[] {-4, 17, 5});
		this.setBlockColumnRelative(1, 0, new int[] {98, -3, 43 | (4 << 8), 98});
		this.setBlockColumnRelative(1, 1, new int[] {4, -3, 0, 98});
		this.setBlockColumnRelative(1, 2, new int[] {1, -3, 0, 98});
		this.setBlockColumnRelative(1, 3, new int[] {4, -3, 0, 98});
		this.setBlockColumnRelative(1, 4, new int[] {1, -4, 98});
		this.setBlockColumnRelative(1, 5, new int[] {4, 0, 50 | (3 << 8), 0, 5});
		this.setBlockColumnRelative(1, 6, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(2, 0, new int[] {98, -4, 43 | (4 << 8)});
		this.setBlockColumnRelative(2, 1, new int[] {43, 132, -2, 0, 43 | (4 << 8), -2, 4});
		this.setBlockColumnRelative(2, 2, new int[] {43, 132, -2, 0, 43 | (4 << 8), 4, 67});
		this.setBlockColumnRelative(2, 3, new int[] {43, 132, -2, 0, 43 | (4 << 8), -2, 4});
		this.setBlockColumnRelative(2, 4, new int[] {1, -3, 0, 98});
		this.setBlockColumnRelative(2, 5, new int[] {98, -3, 0, 5});
		this.setBlockColumnRelative(2, 6, new int[] {44, -3, 0, 5});
		this.setBlockColumnRelative(3, 0, new int[] {98, -4, 43 | (4 << 8)});
		this.setBlockColumnRelative(3, 1, new int[] {43, 132, -2, 0, 43 | (4 << 8), 4, 67 | (2 << 8)});
		this.setBlockColumnRelative(3, 2, new int[] {87});
		this.setBlockColumnRelative(3, 3, new int[] {43, 132, 51, 0, 43 | (4 << 8), 4, 67 | (3 << 8)});
		this.setBlockColumnRelative(3, 4, new int[] {98, -3, 0, 98});
		this.setBlockColumnRelative(3, 5, new int[] {4, -3, 0, 5});
		this.setBlockColumnRelative(3, 6, new int[] {44, -3, 0, 5});
		this.setBlockColumnRelative(4, 0, new int[] {98, -4, 43 | (4 << 8)});
		this.setBlockColumnRelative(4, 1, new int[] {43, 132, -2, 0, 43 | (4 << 8), -2, 4});
		this.setBlockColumnRelative(4, 2, new int[] {43, 132, -2, 0, 43 | (4 << 8), 4, 67 | (1 << 8)});
		this.setBlockColumnRelative(4, 3, new int[] {43, 132, -2, 0, 43 | (4 << 8), -2, 4});
		this.setBlockColumnRelative(4, 4, new int[] {1, -3, 0, 98});
		this.setBlockColumnRelative(4, 5, new int[] {1, -3, 0, 5});
		this.setBlockColumnRelative(4, 6, new int[] {44, -3, 0, 5});
		this.setBlockColumnRelative(5, 0, new int[] {98, -3, 43 | (4 << 8), 98});
		this.setBlockColumnRelative(5, 1, new int[] {4, -3, 0, 98});
		this.setBlockColumnRelative(5, 2, new int[] {98, -3, 0, 98});
		this.setBlockColumnRelative(5, 3, new int[] {1, -3, 0, 98});
		this.setBlockColumnRelative(5, 4, new int[] {4, -4, 98});
		this.setBlockColumnRelative(5, 5, new int[] {98, 0, 50 | (3 << 8), 0, 5});
		this.setBlockColumnRelative(5, 6, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 0, new int[] {-4, 17, 5});
		this.setBlockColumnRelative(6, 1, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 2, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 3, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 4, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 5, new int[] {98, 85, -2, 0, 5});
		this.setBlockColumnRelative(6, 6, new int[] {-4, 17, 5});
		
		this.addSpawnerOneshotRelative(3, 1, 5, "Amazon");
		
		this.setBlockRelative(3, 1, 2, Block.fire.blockID, 0);
	}

}
