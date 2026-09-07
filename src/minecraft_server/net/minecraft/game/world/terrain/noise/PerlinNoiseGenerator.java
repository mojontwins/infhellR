package net.minecraft.game.world.terrain.noise;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class PerlinNoiseGenerator {
	private final SimplexNoiseGenerator[] noiseLevels;
	private final double coef2;
	private final double coef1;

	public PerlinNoiseGenerator(SkippableRandom rand, List<Integer> octaves) {
		this(rand, new TreeSet<Integer>(octaves));
	}

	private PerlinNoiseGenerator(SkippableRandom rand, SortedSet<Integer> octaves) {
		if (octaves.isEmpty()) {
			throw new IllegalArgumentException("Need some octaves!");
		} else {
			int i = -octaves.first();
			int j = octaves.last();
			int k = i + j + 1;

			if (k < 1) {
				throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
			} else {
				SimplexNoiseGenerator simplexnoisegenerator = new SimplexNoiseGenerator(rand);
				int l = j;
				this.noiseLevels = new SimplexNoiseGenerator[k];

				if (j >= 0 && j < k && octaves.contains(0)) {
					this.noiseLevels[j] = simplexnoisegenerator;
				}

				for (int i1 = j + 1; i1 < k; ++i1) {
					if (i1 >= 0 && octaves.contains(l - i1)) {
						this.noiseLevels[i1] = new SimplexNoiseGenerator(rand);
					} else {
						rand.skip(262);
					}
				}

				if (j > 0) {
					long k1 = (long) (simplexnoisegenerator.func_227464_a_(simplexnoisegenerator.xo,
							simplexnoisegenerator.yo, simplexnoisegenerator.zo) * (double) 9.223372E18F);
					SkippableRandom sharedseedrandom = new SkippableRandom(k1);

					for (int j1 = l - 1; j1 >= 0; --j1) {
						if (j1 < k && octaves.contains(l - j1)) {
							this.noiseLevels[j1] = new SimplexNoiseGenerator(sharedseedrandom);
						} else {
							sharedseedrandom.skip(262);
						}
					}
				}

				this.coef1 = Math.pow(2.0D, (double) j);
				this.coef2 = 1.0D / (Math.pow(2.0D, (double) k) - 1.0D);
			}
		}
	}

	public double noiseAt(double x, double y, boolean useNoiseOffsets) {
		double d0 = 0.0D;
		double d1 = this.coef1;
		double d2 = this.coef2;

		for (SimplexNoiseGenerator simplexnoisegenerator : this.noiseLevels) {
			if (simplexnoisegenerator != null) {
				d0 += simplexnoisegenerator.getValue(x * d1 + (useNoiseOffsets ? simplexnoisegenerator.xo : 0.0D),
						y * d1 + (useNoiseOffsets ? simplexnoisegenerator.yo : 0.0D)) * d2;
			}

			d1 /= 2.0D;
			d2 *= 2.0D;
		}

		return d0;
	}

	public double noiseAt(double x, double y, double z, double p_215460_7_) {
		return this.noiseAt(x, y, true) * 0.55D;
	}

}
