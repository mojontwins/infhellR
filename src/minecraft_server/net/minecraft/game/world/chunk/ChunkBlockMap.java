package net.minecraft.game.world.chunk;

import net.minecraft.game.world.block.Block;

public class ChunkBlockMap {
	private static byte[] chunkBlockMap = new byte[256];

	public static void translateBlocks(byte[] b0) {
		for(int i1 = 0; i1 < b0.length; ++i1) {
			b0[i1] = chunkBlockMap[b0[i1] & 255];
		}

	}

	static {
		try {
			for(int i0 = 0; i0 < 256; ++i0) {
				byte b1 = (byte)i0;
				if(b1 != 0 && Block.blocksList[b1 & 255] == null) {
					b1 = 0;
				}

				chunkBlockMap[i0] = b1;
			}
		} catch (Exception exception2) {
			exception2.printStackTrace();
		}

	}
}
