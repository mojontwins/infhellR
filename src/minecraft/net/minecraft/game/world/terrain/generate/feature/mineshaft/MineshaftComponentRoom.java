package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * The central starting room of a mineshaft. Corridors, stairs and crosses are hung off
 * all four of its walls; the openings are saved so the room can carve them through its
 * own floor/ceiling blocks.
 */
public class MineshaftComponentRoom extends MineshaftComponent {
	private final LinkedList<StructureBoundingBox> childOpenings = new LinkedList<StructureBoundingBox>();

	public MineshaftComponentRoom(World world, int componentType, Random rand, int x, int z) {
		this(world, componentType, rand, x, z, 50);
	}

	/**
	 * @param baseY the floor height of the room; regular shafts bury it at y=50, the mesa
	 *        variant passes its own above-ground base (see MineshaftComponentMesaRoom).
	 */
	public MineshaftComponentRoom(World world, int componentType, Random rand, int x, int z, int baseY) {
		super(world, componentType);
		this.boundingBox = new StructureBoundingBox(x, baseY, z, x + 7 + rand.nextInt(6), baseY + 4 + rand.nextInt(6), z + 7 + rand.nextInt(6));
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		int componentType = this.getComponentType();
		int verticalJitter = this.boundingBox.getYSize() - 3 - 1;
		if(verticalJitter <= 0) {
			verticalJitter = 1;
		}

		int offset;
		MineshaftComponent child;
		StructureBoundingBox childBox;
		for(offset = 0; offset < this.boundingBox.getXSize(); offset += 4) {
			offset += rand.nextInt(this.boundingBox.getXSize());
			if(offset + 3 > this.boundingBox.getXSize()) {
				break;
			}

			child = this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + offset, this.boundingBox.minY + rand.nextInt(verticalJitter) + 1, this.boundingBox.minZ - 1, 2, componentType);
			if(child != null) {
				childBox = child.getBoundingBox();
				this.childOpenings.add(new StructureBoundingBox(childBox.minX, childBox.minY, this.boundingBox.minZ, childBox.maxX, childBox.maxY, this.boundingBox.minZ + 1));
			}
		}

		for(offset = 0; offset < this.boundingBox.getXSize(); offset += 4) {
			offset += rand.nextInt(this.boundingBox.getXSize());
			if(offset + 3 > this.boundingBox.getXSize()) {
				break;
			}

			child = this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX + offset, this.boundingBox.minY + rand.nextInt(verticalJitter) + 1, this.boundingBox.maxZ + 1, 0, componentType);
			if(child != null) {
				childBox = child.getBoundingBox();
				this.childOpenings.add(new StructureBoundingBox(childBox.minX, childBox.minY, this.boundingBox.maxZ - 1, childBox.maxX, childBox.maxY, this.boundingBox.maxZ));
			}
		}

		for(offset = 0; offset < this.boundingBox.getZSize(); offset += 4) {
			offset += rand.nextInt(this.boundingBox.getZSize());
			if(offset + 3 > this.boundingBox.getZSize()) {
				break;
			}

			child = this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY + rand.nextInt(verticalJitter) + 1, this.boundingBox.minZ + offset, 1, componentType);
			if(child != null) {
				childBox = child.getBoundingBox();
				this.childOpenings.add(new StructureBoundingBox(this.boundingBox.minX, childBox.minY, childBox.minZ, this.boundingBox.minX + 1, childBox.maxY, childBox.maxZ));
			}
		}

		for(offset = 0; offset < this.boundingBox.getZSize(); offset += 4) {
			offset += rand.nextInt(this.boundingBox.getZSize());
			if(offset + 3 > this.boundingBox.getZSize()) {
				break;
			}

			child = this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + rand.nextInt(verticalJitter) + 1, this.boundingBox.minZ + offset, 3, componentType);
			if(child != null) {
				childBox = child.getBoundingBox();
				this.childOpenings.add(new StructureBoundingBox(this.boundingBox.maxX - 1, childBox.minY, childBox.minZ, this.boundingBox.maxX, childBox.maxY, childBox.maxZ));
			}
		}
	}

	@Override
	public void generate(MineshaftWriter writer, Random rand) {
		this.fillVariation(writer, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ, Block.dirt.blockID, Block.adobe.blockID, rand, 2, true);
		this.fill(writer, this.boundingBox.minX, this.boundingBox.minY + 1, this.boundingBox.minZ, this.boundingBox.maxX, Math.min(this.boundingBox.minY + 3, this.boundingBox.maxY), this.boundingBox.maxZ, 0, 0, false);
		for(StructureBoundingBox opening : this.childOpenings) {
			this.fill(writer, opening.minX, opening.maxY - 2, opening.minZ, opening.maxX, opening.maxY, opening.maxZ, 0, 0, false);
		}
		this.randomRareFill(writer, this.boundingBox.minX, this.boundingBox.minY + 4, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, 0, false);
	}
}
