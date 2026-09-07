package net.minecraft.game.world.terrain.generate.amazonvillage;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.World;

public class BuildingAmazonWell extends BuildingAmazon {

	public BuildingAmazonWell(World world, boolean rotated) {
		super(world, rotated);
	} 

	@Override
	protected int buildingWidth() {
		return 5;
	}

	@Override
	protected int buildingLength() {
		return 5;
	}
	
	@Override
	protected int buildingHeight() {
		return 4;
	}
	
	@Override
	public void generate() {

	}

	@Override
	public void build() {
		this.terraform(this.buildingHeight());
			
		int y1 = - 16; if(y1 < 16) y1 = 16;
		int y2 = - 1;
		
		for(int x = 1; x <= 3; x ++) {
			for(int y = y1; y <= y2; y ++) {
				for(int z = 1; z <= 3; z ++) {
					this.setBlockRelative(x, y, z, Block.waterStill.blockID, 0);
				}
			}
		}
		
		this.setBlockColumnRelative(0, 1, new int[] {98});
		this.setBlockColumnRelative(0, 2, new int[] {98, -2, 85, 17 | (12 << 8)});
		this.setBlockColumnRelative(0, 3, new int[] {98});
		this.setBlockColumnRelative(1, 0, new int[] {98});
		this.setBlockColumnRelative(1, 2, new int[] {-3, 0, 17 | (12 << 8)});
		this.setBlockColumnRelative(1, 4, new int[] {98});
		this.setBlockColumnRelative(2, 0, new int[] {98});
		this.setBlockColumnRelative(2, 2, new int[] {-3, 0, 17 | (12 << 8), 50 | (5 << 8)});
		this.setBlockColumnRelative(2, 4, new int[] {98});
		this.setBlockColumnRelative(3, 0, new int[] {98});
		this.setBlockColumnRelative(3, 2, new int[] {-3, 0, 17 | (12 << 8)});
		this.setBlockColumnRelative(3, 4, new int[] {98});
		this.setBlockColumnRelative(4, 1, new int[] {98});
		this.setBlockColumnRelative(4, 2, new int[] {98, -2, 85, 17 | (12 << 8)});
		this.setBlockColumnRelative(4, 3, new int[] {98});

	}

}
