package net.minecraft.game.world.terrain.generate.structure.stronghold;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.structure.StructureComponent;

import net.minecraft.game.world.ChunkPosition;
import net.minecraft.game.world.World;

public class ComponentStrongholdStairs2 extends ComponentStrongholdStairs {
	public StructureStrongholdPieceWeight field_35038_a;
	public ArrayList<StructureComponent> field_35037_b = new ArrayList<StructureComponent>();

	public ComponentStrongholdStairs2(World world, int i1, Random random2, int i3, int i4) {
		super(world, 0, random2, i3, i4);
	}

	public ChunkPosition getCenter() {
		return super.getCenter();
	}
}
