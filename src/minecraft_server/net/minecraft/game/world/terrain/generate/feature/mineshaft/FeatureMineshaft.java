package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.structure.FeatureStructure;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * Abandoned mineshaft. Buried corridor networks; the gate is the vanilla
 * {@code nextInt(100) == 0} roll biased away from spawn (the {@code nextInt(80) < max(|x|,
 * |z|)} term keeps them out of the world-origin area).
 */
public class FeatureMineshaft extends FeatureStructure {
	public FeatureMineshaft(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public StructureRoomPool createRoomPool() {
		return new MineshaftRoomPool();
	}

	@Override
	public StructurePiece createRoot(Random rand, BiomeGenBase biome) {
		return new MineRoomPiece(rand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
	}

	@Override
	protected void placeVertically(World world, Random rand) {
		this.markAvailableHeight(rand, 10);
	}

	@Override
	public int getSpawnChance() {
		// The real roll lives in shouldSpawn (it depends on the coordinates); this keeps
		// the provider's generic chance roll neutral.
		return MAXCHANCE;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return rand.nextInt(100) == 0 && rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
	}
}
