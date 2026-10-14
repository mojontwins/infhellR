package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.structure.StructurePieceTreasure;

/**
 * Factory and loot table for mineshaft pieces.
 *
 * <p>Vanilla had two nearly identical static factories ({@code StructureMineshaftPieces} and
 * {@code StructureMineshaftMesaPieces}). This class folds them into one small hierarchy so
 * the recursion, loot and piece placement are written once; a variant only overrides the
 * stair-roll threshold and (optionally) which corridor class to build. Mirrors the vanilla
 * logic: each call rolls a piece type, asks it for a valid footprint and - when one is
 * found - recurses into it.
 */
public class MineshaftPieces {
	private static final StructurePieceTreasure[] LOOT = new StructurePieceTreasure[]{
		new StructurePieceTreasure(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new StructurePieceTreasure(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new StructurePieceTreasure(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new StructurePieceTreasure(Item.dyePowder.shiftedIndex, 4, 4, 9, 5),
		new StructurePieceTreasure(Item.diamond.shiftedIndex, 0, 1, 2, 3),
		new StructurePieceTreasure(Item.nametagSimple.shiftedIndex, 0, 1, 2, 3),
		new StructurePieceTreasure(Item.coal.shiftedIndex, 0, 3, 8, 10),
		new StructurePieceTreasure(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new StructurePieceTreasure(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 1),
		new StructurePieceTreasure(Block.rail.blockID, 0, 4, 8, 1),
		new StructurePieceTreasure(Item.seeds.shiftedIndex, 0, 2, 4, 10),
	};

	/** The world this factory's pieces are built in (passed on to each created piece). */
	protected final World world;

	/** Roll (0-99) at or above which a corridor becomes a stairwell. Cross is always >= 80. */
	private final int stairsThreshold;

	public MineshaftPieces(World world, int stairsThreshold) {
		this.world = world;
		this.stairsThreshold = stairsThreshold;
	}

	protected MineshaftComponent createCorridor(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentCorridor(this.world, componentType, rand, box, direction);
	}

	protected MineshaftComponent createCross(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentCross(this.world, componentType, rand, box, direction);
	}

	protected MineshaftComponent createStairs(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentStairs(this.world, componentType, rand, box, direction);
	}

	protected MineshaftComponent getRandomComponent(List<MineshaftComponent> components, Random rand, int x, int y, int z, int direction, int componentType) {
		int roll = rand.nextInt(100);
		StructureBoundingBox box;
		if(roll >= 80) {
			box = MineshaftComponentCross.findValidPlacement(components, rand, x, y, z, direction);
			if(box != null) {
				return this.createCross(componentType, rand, box, direction);
			}
		} else if(roll >= this.stairsThreshold) {
			box = MineshaftComponentStairs.findValidPlacement(components, rand, x, y, z, direction);
			if(box != null) {
				return this.createStairs(componentType, rand, box, direction);
			}
		} else {
			box = MineshaftComponentCorridor.findValidPlacement(components, rand, x, y, z, direction);
			if(box != null) {
				return this.createCorridor(componentType, rand, box, direction);
			}
		}

		return null;
	}

	/**
	 * Creates the next piece if it would fit. The 80-block bound and the depth cap are
	 * measured against the START ROOM (passed down through every branch), exactly like
	 * the vanilla generator.
	 */
	public MineshaftComponent getNextComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand, int x, int y, int z, int direction, int depth) {
		if(depth > 8) {
			return null;
		}
		if(Math.abs(x - startRoom.getBoundingBox().minX) > 80 || Math.abs(z - startRoom.getBoundingBox().minZ) > 80) {
			return null;
		}

		MineshaftComponent component = this.getRandomComponent(components, rand, x, y, z, direction, depth + 1);
		if(component != null) {
			component.setPieces(this);
			components.add(component);
			component.buildComponent(startRoom, components, rand);
		}

		return component;
	}

	public StructurePieceTreasure[] getLoot() {
		return LOOT;
	}
}
