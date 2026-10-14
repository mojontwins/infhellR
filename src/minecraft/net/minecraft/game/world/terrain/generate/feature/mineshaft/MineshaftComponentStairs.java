package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * A mineshaft stairwell: a simple descending carved void connecting two levels.
 */
public class MineshaftComponentStairs extends MineshaftComponent {
	public MineshaftComponentStairs(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int direction) {
		super(world, componentType);
		this.coordBaseMode = direction;
		this.boundingBox = boundingBox;
	}

	public static StructureBoundingBox findValidPlacement(List<MineshaftComponent> components, Random rand, int x, int y, int z, int direction) {
		StructureBoundingBox box = new StructureBoundingBox(x, y - 5, z, x, y + 2, z);
		switch(direction) {
		case 0:
			box.maxX = x + 2;
			box.maxZ = z + 8;
			break;
		case 1:
			box.minX = x - 8;
			box.maxZ = z + 2;
			break;
		case 2:
			box.maxX = x + 2;
			box.minZ = z - 8;
			break;
		case 3:
			box.maxX = x + 8;
			box.maxZ = z + 2;
		}

		return findIntersecting(components, box) != null ? null : box;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		int componentType = this.getComponentType();
		switch(this.coordBaseMode) {
		case 0:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, componentType);
			break;
		case 1:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, 1, componentType);
			break;
		case 2:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, componentType);
			break;
		case 3:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, 3, componentType);
		}
	}

	@Override
	public void generate(MineshaftWriter writer, Random rand) {
		this.fill(writer, 0, 5, 0, 2, 7, 1, 0, 0, false);
		this.fill(writer, 0, 0, 7, 2, 2, 8, 0, 0, false);

		for(int step = 0; step < 5; ++step) {
			this.fill(writer, 0, 5 - step - (step < 4 ? 1 : 0), 2 + step, 2, 7 - step, 2 + step, 0, 0, false);
		}
	}
}
