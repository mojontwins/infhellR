package net.minecraft.game.world;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.EntityBlockEntity;
import net.minecraft.game.world.block.BlockStairs;
import net.minecraft.game.world.block.BlockStep;

public class ChunkCache implements IBlockAccess {
	private int chunkX;
	private int chunkZ;
	private int originBlockX;
	private int originBlockZ;
	private Chunk[][] chunkArray;
	private World worldObj;

	public ChunkCache(World world1, int i2, int i3, int i4, int i5, int i6, int i7) {
		this.worldObj = world1;
		this.chunkX = i2 >> 4;
		this.chunkZ = i4 >> 4;
		int i8 = i5 >> 4;
		int i9 = i7 >> 4;
		this.chunkArray = new Chunk[i8 - this.chunkX + 1][i9 - this.chunkZ + 1];

		for(int i10 = this.chunkX; i10 <= i8; ++i10) {
			for(int i11 = this.chunkZ; i11 <= i9; ++i11) {
				this.chunkArray[i10 - this.chunkX][i11 - this.chunkZ] = world1.getChunkFromChunkCoords(i10, i11);
			}
		}

		this.originBlockX = this.chunkX << 4;
		this.originBlockZ = this.chunkZ << 4;
	}

	public int getBlockId(int i1, int i2, int i3) {
		if(i2 < 0) {
			return 0;
		} else if(i2 >= 128) {
			return 0;
		} else {
			if((i1 & ~15) == this.originBlockX && (i3 & ~15) == this.originBlockZ) {
				Chunk originChunk = this.chunkArray[0][0];
				return originChunk == null ? 0 : originChunk.getBlockID(i1 & 15, i2, i3 & 15);
			}

			int i4 = (i1 >> 4) - this.chunkX;
			int i5 = (i3 >> 4) - this.chunkZ;
			if(i4 >= 0 && i4 < this.chunkArray.length && i5 >= 0 && i5 < this.chunkArray[i4].length) {
				Chunk chunk6 = this.chunkArray[i4][i5];
				return chunk6 == null ? 0 : chunk6.getBlockID(i1 & 15, i2, i3 & 15);
			} else {
				return 0;
			}
		}
	}

	public TileEntity getBlockTileEntity(int i1, int i2, int i3) {
		int i4 = (i1 >> 4) - this.chunkX;
		int i5 = (i3 >> 4) - this.chunkZ;
		return this.chunkArray[i4][i5].getChunkBlockTileEntity(i1 & 15, i2, i3 & 15);
	}
	

	public EntityBlockEntity getBlockEntity(int x, int y, int z) {
		int i4 = (x >> 4) - this.chunkX;
		int i5 = (z >> 4) - this.chunkZ;
		return this.chunkArray[i4][i5].getChunkBlockEntity(x & 15, y, z & 15);
	}

	public float getBrightness(int i1, int i2, int i3, int i4) {
		int i5 = this.getLightValue(i1, i2, i3);
		if(i5 < i4) {
			i5 = i4;
		}

		return this.worldObj.worldProvider.lightBrightnessTable[i5];
	}

	public int getLightBrightnessForSkyBlocks(int i1, int i2, int i3, int i4) {
		int i5 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i1, i2, i3);
		int i6 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, i1, i2, i3);
		if(i6 < i4) {
			i6 = i4;
		}

		return i5 << 20 | i6 << 4;
	}

	public float getLightBrightness(int i1, int i2, int i3) {
		return this.worldObj.worldProvider.lightBrightnessTable[this.getLightValue(i1, i2, i3)];
	}

	public int getLightValue(int i1, int i2, int i3) {
		return this.getLightValueExt(i1, i2, i3, true);
	}

	public int getLightValueExt(int i1, int i2, int i3, boolean z4) {
		if(i1 >= -32000000 && i3 >= -32000000 && i1 < 32000000 && i3 <= 32000000) {
			int i5;
			int i6;
			if(z4) {
				i5 = this.getBlockId(i1, i2, i3);
				if(i5 == Block.stairSingle.blockID || i5 == Block.tilledField.blockID || i5 == Block.stairCompactPlanks.blockID || i5 == Block.stairCompactCobblestone.blockID) {
					i6 = this.getLightValueExt(i1, i2 + 1, i3, false);
					int i7 = this.getLightValueExt(i1 + 1, i2, i3, false);
					int i8 = this.getLightValueExt(i1 - 1, i2, i3, false);
					int i9 = this.getLightValueExt(i1, i2, i3 + 1, false);
					int i10 = this.getLightValueExt(i1, i2, i3 - 1, false);
					if(i7 > i6) {
						i6 = i7;
					}

					if(i8 > i6) {
						i6 = i8;
					}

					if(i9 > i6) {
						i6 = i9;
					}

					if(i10 > i6) {
						i6 = i10;
					}

					return i6;
				}
			}

			if(i2 < 0) {
				return 0;
			} else if(i2 >= 128) {
				i5 = 15 - this.worldObj.getSkylightSubtracted();
				if(i5 < 0) {
					i5 = 0;
				}

				return i5;
			} else {
				i5 = (i1 >> 4) - this.chunkX;
				i6 = (i3 >> 4) - this.chunkZ;
				return this.chunkArray[i5][i6].getBlockLightValue(i1 & 15, i2, i3 & 15, this.worldObj.getSkylightSubtracted());
			}
		} else {
			return 15;
		}
	}

	public int getBlockMetadata(int i1, int i2, int i3) {
		if(i2 < 0) {
			return 0;
		} else if(i2 >= 128) {
			return 0;
		} else {
			if((i1 & ~15) == this.originBlockX && (i3 & ~15) == this.originBlockZ) {
				return this.chunkArray[0][0].getBlockMetadata(i1 & 15, i2, i3 & 15);
			}

			int i4 = (i1 >> 4) - this.chunkX;
			int i5 = (i3 >> 4) - this.chunkZ;
			return this.chunkArray[i4][i5].getBlockMetadata(i1 & 15, i2, i3 & 15);
		}
	}

	public Material getBlockMaterial(int i1, int i2, int i3) {
		int i4 = this.getBlockId(i1, i2, i3);
		Block block = Block.blocksList[i4];
		return block == null ? Material.air : block.blockMaterial;
	}

	public WorldChunkManager getWorldChunkManager() {
		return this.worldObj.getWorldChunkManager();
	}

	public boolean isBlockOpaqueCube(int i1, int i2, int i3) {
		Block block4 = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return block4 == null ? false : block4.isOpaqueCube();
	}

	public boolean isBlockNormalCube(int i1, int i2, int i3) {
		Block block4 = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return block4 == null ? false : block4.blockMaterial.getIsSolid() && block4.renderAsNormalBlock();
	}
	
	public boolean isAirBlock(int i1, int i2, int i3) {
		Block block4 = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return block4 == null;
	}
	
	public int getSkyBlockTypeBrightness(EnumSkyBlock enumSkyBlock1, int x, int y, int z) {
		if(y < 0) {
			y = 0;
		}

		if(y >= 256) {
			y = 255;
		}

		if(y >= 0 && y < 256) {
			int blockID = this.getBlockId(x, y, z);
			if(Block.useNeighborBrightness[blockID]) {

				// Add: Upside down blocks must consider different shit
				Block block = Block.blocksList[blockID];
				int bTB;

				if(
						((block instanceof BlockStep) && (this.getBlockMetadata(x, y, z) & 8) != 0) ||
						((block instanceof BlockStairs) && (this.getBlockMetadata(x, y, z) & 4) != 0)
				){
					bTB = this.getSpecialBlockBrightness(enumSkyBlock1, x, y - 1, z);
				} else {
					bTB = this.getSpecialBlockBrightness(enumSkyBlock1, x, y + 1, z);
		}

				int bE = this.getSpecialBlockBrightness(enumSkyBlock1, x + 1, y, z);
				int bW = this.getSpecialBlockBrightness(enumSkyBlock1, x - 1, y, z);
				int bN = this.getSpecialBlockBrightness(enumSkyBlock1, x, y, z + 1);
				int bS = this.getSpecialBlockBrightness(enumSkyBlock1, x, y, z - 1);
				if(bE > bTB) {
					bTB = bE;
				}

				if(bW > bTB) {
					bTB = bW;
				}

				if(bN > bTB) {
					bTB = bN;
				}

				if(bS > bTB) {
					bTB = bS;
				}

				return bTB;
			} else {
				int cx = (x >> 4) - this.chunkX;
				int cz = (z >> 4) - this.chunkZ;
				return this.chunkArray[cx][cz].getSavedLightValue(enumSkyBlock1, x & 15, y, z & 15);
			}
		} else {
			return enumSkyBlock1.defaultLightValue;
		}
	}

	public int getSpecialBlockBrightness(EnumSkyBlock enumSkyBlock1, int i2, int i3, int i4) {
		if(i3 < 0) {
			i3 = 0;
		}

		if(i3 >= 256) {
			i3 = 255;
		}

		if(i3 >= 0 && i3 < 256) {
			int i5 = (i2 >> 4) - this.chunkX;
			int i6 = (i4 >> 4) - this.chunkZ;
			return this.chunkArray[i5][i6].getSavedLightValue(enumSkyBlock1, i2 & 15, i3, i4 & 15);
		} else {
			return enumSkyBlock1.defaultLightValue;
		}
	}
}
