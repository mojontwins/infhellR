package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.block.Block;

/**
 * Spike descriptor: block type, size factor and direction. The hill feature
 * plans its spikes with these in the constructor and materializes them from
 * an analytic pocket model during terraform, so the old WorldGenTF generate()
 * path (world block scans) is gone.
 */
public class GeneratorCaveStalactite {
	public double size;
	public int bType;
	public boolean hang;

	public GeneratorCaveStalactite(int blockType, double sizeFactor, boolean down) {
		this.bType = blockType;
		this.size = sizeFactor;
		this.hang = down;
	}

	public static GeneratorCaveStalactite makeRandomOreStalactite(Random rand, int caveSize) {
		int s1;
		if(caveSize >= 3 || caveSize >= 2 && rand.nextInt(5) == 0) {
			s1 = rand.nextInt(6);
			if(s1 == 0) {
				return new GeneratorCaveStalactite(Block.oreDiamond.blockID, rand.nextDouble() * 0.5D, true);
			}

			if(s1 == 1) {
				return new GeneratorCaveStalactite(Block.oreRuby.blockID, rand.nextDouble() * 0.5D, true);
			}
			
			if(s1 == 2) {
				return new GeneratorCaveStalactite(Block.oreEmerald.blockID, rand.nextDouble() * 0.5D, true);
			}
		}

		if(caveSize >= 2 || caveSize >= 1 && rand.nextInt(5) == 0) {
			s1 = rand.nextInt(6);
			if(s1 == 0) {
				return new GeneratorCaveStalactite(Block.oreGold.blockID, rand.nextDouble() * 0.6D, true);
			}

			if(s1 == 1 || s1 == 2) {
				return new GeneratorCaveStalactite(Block.oreRedstone.blockID, rand.nextDouble() * 0.8D, true);
			}
		}

		s1 = rand.nextInt(5);
		if(s1 != 0 && s1 != 1) {
			if(s1 != 2 && s1 != 3) {
				return new GeneratorCaveStalactite(Block.glowStone.blockID, rand.nextDouble() * 0.5D, true);
			} else {
				return new GeneratorCaveStalactite(Block.oreCoal.blockID, rand.nextDouble() * 0.8D, true);
			}
		} else {
			return new GeneratorCaveStalactite(Block.oreIron.blockID, rand.nextDouble() * 0.7D, true);
		}
	}
}
