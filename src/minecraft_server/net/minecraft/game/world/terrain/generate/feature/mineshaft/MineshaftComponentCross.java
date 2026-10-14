package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * A mineshaft crossing: a (possibly two-storey) open hub with corner support columns
 * and a plank ceiling. Its {@code coordBaseMode} stays {@code -1}, so - like the room -
 * it works in absolute coordinates; {@code corridorDirection} only steers how the four
 * branches are hung off it.
 */
public class MineshaftComponentCross extends MineshaftComponent {
	private final int corridorDirection;
	private final boolean isMultipleFloors;

	public MineshaftComponentCross(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int corridorDirection) {
		super(world, componentType);
		this.corridorDirection = corridorDirection;
		this.boundingBox = boundingBox;
		this.isMultipleFloors = boundingBox.getYSize() > 3;
	}

	public static StructureBoundingBox findValidPlacement(List<MineshaftComponent> components, Random rand, int x, int y, int z, int direction) {
		StructureBoundingBox box = new StructureBoundingBox(x, y, z, x, y + 3, z);
		if(rand.nextInt(4) == 0) {
			box.maxY += 4;
		}

		switch(direction) {
		case 0:
			box.minX = x - 1;
			box.maxX = x + 3;
			box.maxZ = z + 4;
			break;
		case 1:
			box.minX = x - 4;
			box.minZ = z - 1;
			box.maxZ = z + 3;
			break;
		case 2:
			box.minX = x - 1;
			box.maxX = x + 3;
			box.minZ = z - 4;
			break;
		case 3:
			box.maxX = x + 4;
			box.minZ = z - 1;
			box.maxZ = z + 3;
		}

		return findIntersecting(components, box) != null ? null : box;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		int componentType = this.getComponentType();
		switch(this.corridorDirection) {
		case 0:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, componentType);
			break;
		case 1:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, componentType);
			break;
		case 2:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, componentType);
			break;
		case 3:
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, componentType);
			this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, componentType);
		}

		if(this.isMultipleFloors) {
			if(rand.nextBoolean()) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 4, this.boundingBox.minZ - 1, 2, componentType);
			}
			if(rand.nextBoolean()) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY + 4, this.boundingBox.minZ + 1, 1, componentType);
			}
			if(rand.nextBoolean()) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + 4, this.boundingBox.minZ + 1, 3, componentType);
			}
			if(rand.nextBoolean()) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 4, this.boundingBox.maxZ + 1, 0, componentType);
			}
		}
	}

	@Override
	public void generate(MineshaftWriter writer, Random rand) {
		if(this.isMultipleFloors) {
			this.fill(writer, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.minY + 2, this.boundingBox.maxZ, 0, 0, false);
			this.fill(writer, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.minY + 2, this.boundingBox.maxZ - 1, 0, 0, false);
			this.fill(writer, this.boundingBox.minX + 1, this.boundingBox.maxY - 2, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, 0, 0, false);
			this.fill(writer, this.boundingBox.minX, this.boundingBox.maxY - 2, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, 0, 0, false);
			this.fill(writer, this.boundingBox.minX + 1, this.boundingBox.minY + 3, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.minY + 3, this.boundingBox.maxZ - 1, 0, 0, false);
		} else {
			this.fill(writer, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, 0, 0, false);
			this.fill(writer, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, 0, 0, false);
		}

		// Corner support columns.
		this.fillVariation(writer, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.minX + 1, this.boundingBox.maxY - 1, this.boundingBox.minZ + 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillVariation(writer, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.minX + 1, this.boundingBox.maxY - 1, this.boundingBox.maxZ - 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillVariation(writer, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.maxY - 1, this.boundingBox.minZ + 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillVariation(writer, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxX - 1, this.boundingBox.maxY - 1, this.boundingBox.maxZ - 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);

		// Plank ceiling, plus a slab "floor" wherever the ground below is empty.
		for(int x = this.boundingBox.minX; x <= this.boundingBox.maxX; ++x) {
			for(int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; ++z) {
				this.placeBlock(writer, Block.planks.blockID, 0, x, this.boundingBox.maxY, z);
				if(this.getBlockID(writer, x, this.boundingBox.minY - 1, z) == 0) {
					this.placeBlock(writer, Block.stairSingle.blockID, 8 | 2, x, this.boundingBox.minY - 1, z);
				}
			}
		}
	}

	@Override
	public void populate(MineshaftWriter writer, Random rand) {
		this.throwChainUp(writer, this.boundingBox.minX, this.boundingBox.minZ);
		this.throwChainUp(writer, this.boundingBox.minX, this.boundingBox.maxZ);
		this.throwChainUp(writer, this.boundingBox.maxX, this.boundingBox.minZ);
		this.throwChainUp(writer, this.boundingBox.maxX, this.boundingBox.maxZ);
	}

	private void throwChainUp(MineshaftWriter writer, int x, int z) {
		if(this.getBlockIDNoClip(writer, x, this.boundingBox.maxY + 1, z) == 0
				&& !this.canBlockSeeTheSky(writer, x, this.boundingBox.maxY + 1, z)) {
			int y = this.boundingBox.maxY + 1;
			while(this.getBlockIDNoClip(writer, x, y, z) == 0) {
				this.placeBlockNoVertClip(writer, Block.chain.blockID, 0, x, y, z);
				++y;
			}
		}
	}
}
