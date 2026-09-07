package net.minecraft.game.world;

import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.ChunkProviderHell;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.biome.BiomeGenBase;

/**
 * WorldProvider for the Nether (dimension -1).
 */
public class WorldProviderHell extends WorldProvider {
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 1.0D, 0.0D);
		this.isNether = true;
		this.isHellWorld = true;
		this.hasNoSky = true;
		this.worldType = -1;
	}

	public Vec3D getFogColor(float celestialAngle, float partialTick, boolean bloodMoon) {
		return Vec3D.createVector((double)0.2F, (double)0.03F, (double)0.03F);
	}

	protected void generateLightBrightnessTable() {
		float minBrightness = 0.1F;

		for(int level = 0; level <= 15; ++level) {
			float darkness = 1.0F - (float)level / 15.0F;
			this.lightBrightnessTable[level] = (1.0F - darkness) / (darkness * 3.0F + 1.0F) * (1.0F - minBrightness) + minBrightness;
		}

	}

	public IChunkProvider getChunkProvider() {
		return new ChunkProviderHell(this.worldObj, this.worldObj.getRandomSeed());
	}

	public boolean canCoordinateBeSpawn(int x, int z) {
		int topId = this.worldObj.getFirstUncoveredBlock(x, z);
		return topId == Block.bedrock.blockID ? false : (topId == 0 ? false : Block.opaqueCubeLookup[topId]);
	}

	public float calculateCelestialAngle(long worldTime, float partialTick) {
		return 0.5F;
	}

	public boolean canRespawnHere() {
		return false;
	}
}
