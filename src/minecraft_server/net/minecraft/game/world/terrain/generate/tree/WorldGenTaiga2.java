package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class WorldGenTaiga2 extends WorldGenerator {
	// Softlocked for b1.0 - No spruce wood / leaves!
	//private final int treeMetadata = 1;
	private final int treeMetadata = 0;
	private final int leavesMetadata = EnumTreeType.TAIGA.getLeafMetadata();
	
	public boolean generate(World world1, Random random2, int i3, int i4, int i5) {
		int i6 = random2.nextInt(4) + 6;
		int i7 = 1 + random2.nextInt(2);
		int i8 = i6 - i7;
		int i9 = 2 + random2.nextInt(2);
		boolean z10 = true;
		if(i4 >= 1 && i4 + i6 + 1 <= Chunk.SECTION_HEIGHT) {
			int i11;
			int i13;
			int i15;
			int i21;
			for(i11 = i4; i11 <= i4 + 1 + i6 && z10; ++i11) {
				if(i11 - i4 < i7) {
					i21 = 0;
				} else {
					i21 = i9;
				}

				for(i13 = i3 - i21; i13 <= i3 + i21 && z10; ++i13) {
					for(int i14 = i5 - i21; i14 <= i5 + i21 && z10; ++i14) {
						if(i11 >= 0 && i11 < Chunk.SECTION_HEIGHT) {
							i15 = world1.getBlockId(i13, i11, i14);
							if(i15 != 0 && i15 != Block.leaves.blockID) {
								z10 = false;
							}
						} else {
							z10 = false;
						}
					}
				}
			}

			if(!z10) {
				return false;
			} else {
				i11 = world1.getBlockId(i3, i4 - 1, i5);
				if((i11 == Block.grass.blockID || i11 == Block.dirt.blockID) && i4 < Chunk.SECTION_HEIGHT - i6 - 1) {
					world1.setBlock(i3, i4 - 1, i5, Block.dirt.blockID);
					i21 = random2.nextInt(2);
					i13 = 1;
					byte b22 = 0;

					int i16;
					int i17;
					for(i15 = 0; i15 <= i8; ++i15) {
						i16 = i4 + i6 - i15;

						for(i17 = i3 - i21; i17 <= i3 + i21; ++i17) {
							int i18 = i17 - i3;

							for(int i19 = i5 - i21; i19 <= i5 + i21; ++i19) {
								int i20 = i19 - i5;
								if((Math.abs(i18) != i21 || Math.abs(i20) != i21 || i21 <= 0) && !Block.opaqueCubeLookup[world1.getBlockId(i17, i16, i19)]) {
									world1.setBlockAndMetadata(i17, i16, i19, Block.leaves.blockID, this.leavesMetadata);
								}
							}
						}

						if(i21 >= i13) {
							i21 = b22;
							b22 = 1;
							++i13;
							if(i13 > i9) {
								i13 = i9;
							}
						} else {
							++i21;
						}
					}

					i15 = random2.nextInt(3);

					for(i16 = 0; i16 < i6 - i15; ++i16) {
						i17 = world1.getBlockId(i3, i4 + i16, i5);
						if(i17 == 0 || i17 == Block.leaves.blockID) {
							world1.setBlockAndMetadata(i3, i4 + i16, i5, Block.wood.blockID, this.treeMetadata);
						}
					}

					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
}
