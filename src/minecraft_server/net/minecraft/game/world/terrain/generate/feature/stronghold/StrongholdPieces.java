package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * Instance factory for a single stronghold: the weighted piece table plus the recursive
 * "attach the next piece to this doorway" entry point.
 *
 * <p>Port of the vanilla {@code StructureStrongholdPieces}, but made an <b>instance</b>
 * rather than a pile of static state. The vanilla version kept the piece list, the
 * instance counters and the forced next-piece in static fields that were reset by
 * {@code prepareStructurePieces()} for the one structure being generated at a time. In
 * the Feature system many strongholds can be planned in a session, so each
 * {@code FeatureStronghold} owns its own factory and its own mutable weight objects.
 */
public class StrongholdPieces {
	/** The four stone flavours the stronghold is built from (vanilla {@code MapGenStronghold} statics). */
	public static final int BRICKS_ID = Block.stoneBricks.blockID;
	public static final int BRICKS_META = 0;
	public static final int BRICKS_ALT1_ID = Block.cobblestoneMossy.blockID;
	public static final int BRICKS_ALT1_META = 0;
	public static final int BRICKS_ALT2_ID = Block.stone.blockID;
	public static final int BRICKS_ALT2_META = 0;
	public static final int IRON_FENCE_ID = Block.fenceIron.blockID;
	public static final int IRON_FENCE_META = 0;

	/** The block selector that mixes the stone bricks (shared: it carries no per-call state between draws). */
	private static final StrongholdStones strongholdStones = new StrongholdStones();

	/** Pieces allowed to sit at or above y=64 (the surface band). */
	private static final Class<?>[] surfaceAllowedComponents = new Class<?>[] {
		StrongholdComponentStraight.class,
		StrongholdComponentLeftTurn.class,
		StrongholdComponentRightTurn.class,
		StrongholdComponentCrossing.class,
		StrongholdComponentStairsStraight.class
	};

	protected final World world;
	private List<StrongholdPieceWeight> pieceList;
	private Class<?> strongComponentType;
	private int totalWeight;

	public StrongholdPieces(World world) {
		this.world = world;
		this.prepareStructurePieces();
	}

	public static StrongholdStones getStrongholdStones() {
		return strongholdStones;
	}

	/** (Re)builds the weighted piece list with fresh instance counters. */
	public void prepareStructurePieces() {
		this.pieceList = new ArrayList<StrongholdPieceWeight>();
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentStraight.class, 40, 0, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentPrison.class, 5, 5, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentLeftTurn.class, 20, 0, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentRightTurn.class, 20, 0, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentRoomCrossing.class, 10, 20, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentStairsStraight.class, 5, 25, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentStairs.class, 10, 25, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentCrossing.class, 10, 20, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentChestCorridor.class, 5, 20, false));
		this.pieceList.add(new StrongholdPieceWeight(StrongholdComponentLibrary.class, 10, 10, true));
		this.strongComponentType = null;
	}

	private boolean canAddStructurePieces() {
		boolean any = false;
		this.totalWeight = 0;
		for(StrongholdPieceWeight weight : this.pieceList) {
			this.totalWeight += weight.pieceWeight;
			if(weight.instancesLimit > 0 && weight.instancesSpawned < weight.instancesLimit) {
				any = true;
			}
		}
		return any;
	}

	private StrongholdComponent getStrongholdComponentFromWeightedPiece(Class<?> pieceClass, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int depth) {
		StrongholdComponent component = null;
		if(pieceClass == StrongholdComponentStraight.class) {
			component = StrongholdComponentStraight.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentPrison.class) {
			component = StrongholdComponentPrison.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentLeftTurn.class) {
			component = StrongholdComponentLeftTurn.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentRightTurn.class) {
			component = StrongholdComponentRightTurn.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentRoomCrossing.class) {
			component = StrongholdComponentRoomCrossing.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentStairsStraight.class) {
			component = StrongholdComponentStairsStraight.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentStairs.class) {
			component = StrongholdComponentStairs.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentCrossing.class) {
			component = StrongholdComponentCrossing.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentChestCorridor.class) {
			component = StrongholdComponentChestCorridor.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		} else if(pieceClass == StrongholdComponentLibrary.class) {
			component = StrongholdComponentLibrary.findValidPlacement(this.world, components, rand, x, y, z, coordBaseMode, depth);
		}

		if(component != null) {
			component.setFactory(this);
		}
		return component;
	}

	private StrongholdComponent getNextComponent(StrongholdComponentStairs2 start, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int depth) {
		if(!this.canAddStructurePieces()) {
			return null;
		}

		if(this.strongComponentType != null) {
			StrongholdComponent forced = this.getStrongholdComponentFromWeightedPiece(this.strongComponentType, components, rand, x, y, z, coordBaseMode, depth);
			this.strongComponentType = null;
			if(forced != null) {
				return forced;
			}
		}

		int attempts = 0;
		while(attempts < 5) {
			++attempts;
			int roll = rand.nextInt(this.totalWeight);
			Iterator<StrongholdPieceWeight> iterator = this.pieceList.iterator();
			while(iterator.hasNext()) {
				StrongholdPieceWeight weight = iterator.next();
				roll -= weight.pieceWeight;
				if(roll < 0) {
					if(!weight.canSpawnMoreStructuresOfType(depth) || weight == start.currentPieceWeight) {
						break;
					}

					StrongholdComponent component = this.getStrongholdComponentFromWeightedPiece(weight.pieceClass, components, rand, x, y, z, coordBaseMode, depth);
					if(component != null) {
						// Above y=64 only the "open" pieces are allowed, so the surface
						// version of a stronghold reads as a coherent compound.
						if(y >= 64 && !this.allowedOnSurface(component)) {
							// rejected - try the next weight
						} else {
							++weight.instancesSpawned;
							start.currentPieceWeight = weight;
							if(!weight.canSpawnMoreStructures()) {
								this.pieceList.remove(weight);
							}
							return component;
						}
					}
				}
			}
		}

		StructureBoundingBox corridorBox = StrongholdComponentCorridor.findCorridorPlacement(components, rand, x, y, z, coordBaseMode);
		if(corridorBox != null && corridorBox.minY > 1) {
			StrongholdComponent corridor = new StrongholdComponentCorridor(this.world, depth, rand, corridorBox, coordBaseMode);
			corridor.setFactory(this);
			return corridor;
		}
		return null;
	}

	private boolean allowedOnSurface(StrongholdComponent component) {
		return Arrays.asList(surfaceAllowedComponents).contains(component.getClass());
	}

	/** Grows the stronghold from a doorway: create the next piece and register it. */
	public StrongholdComponent getNextValidComponent(StrongholdComponentStairs2 start, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int depth) {
		if(depth > 50) {
			return null;
		}
		if(Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112) {
			StrongholdComponent component = this.getNextComponent(start, components, rand, x, y, z, coordBaseMode, depth + 1);
			if(component != null) {
				components.add(component);
				start.children.add(component);
			}
			return component;
		}
		return null;
	}

	/** Forces the next created piece to be of the given class (used by the main stairwell). */
	public void setComponentType(Class<?> componentClass) {
		this.strongComponentType = componentClass;
	}
}
