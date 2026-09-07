package net.minecraft.game.world.terrain.generate.fossils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.feature.FeatureAABB;
import net.minecraft.game.world.feature.FeatureDynamicSchematic;
import net.minecraft.game.world.feature.FeatureProvider;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDesert;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureFossil extends FeatureDynamicSchematic {

	/*
	 * A few skeletons will generate. At random, half of them will be drawn at y=64,
	 * half of them on the surface.
	 */

	// Absolute x0, z0
	int x0Abs, z0Abs;

	// We don't want pieces overwriting pieces, so...
	private List<FeatureAABB> pieceAABBs = new ArrayList<FeatureAABB>();

	public static final FossilPiece fossilPieceSkull1 = new FossilPiece(new short[][][] {
			{ { -1, -1, 167 | (4 << 8), -1, -1 }, { -1, 167, 167, 167, -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167, 167, 167, -1 },
					{ -1, -1, -1, -1, -1 } },
			{ { -1, 167, 167 | (12 << 8), 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, 167, 167, -1, -1 } },
			{ { -1, -1, 167 | (12 << 8), 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, 167, 167 | (12 << 8), -1 } },
			{ { -1, -1, 167 | (12 << 8), 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, 167, 167 | (12 << 8), -1 } },
			{ { -1, 167, 167 | (12 << 8), 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, 167, 167, -1, -1 } },
			{ { -1, -1, -1, -1, -1 }, { -1, 167, 167, 167, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167, 167, 167, -1 },
					{ -1, -1, -1, -1, -1 } } });

	public static final FossilPiece fossilPieceSkull2 = new FossilPiece(new short[][][] {
			{ { -1, -1, -1, -1, -1 }, { -1, 167, 167, 167, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167, 167, 167, -1 }, { -1, -1, -1, -1, -1 } },
			{ { -1, 167, 167, 167, -1 }, { 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, 167, 167, 167, -1 } },
			{ { -1, -1, 167, 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, 167 | (12 << 8), -1, 167 | (12 << 8) } },
			{ { -1, -1, -1, 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, 167 | (12 << 8), 167, -1 } },
			{ { -1, -1, 167, 167 | (12 << 8), -1 }, { -1, -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, -1, 167 | (12 << 8), -1, 167 | (12 << 8) } },
			{ { -1, 167, 167, 167, -1 }, { 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) },
					{ -1, 167, 167, 167, -1 } },
			{ { -1, -1, -1, -1, -1 }, { -1, 167, 167, 167, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167, 167, 167, -1 }, { -1, -1, -1, -1, -1 } } });

	public static final FossilPiece fossilPieceSkull3 = new FossilPiece(new short[][][] {
			{ { -1, 167, 167, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { 167, 167, 167, 167 | (12 << 8) } },
			{ { -1, -1, 167 | (12 << 8), -1 }, { -1, -1, -1, 167 | (12 << 8) }, { 167 | (4 << 8), -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, 167 | (12 << 8) }, { -1, 167 | (12 << 8), -1, 167 | (12 << 8) } },
			{ { -1, -1, 167 | (12 << 8), -1 }, { -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, 167 | (12 << 8) }, { -1, 167 | (12 << 8), 167, 167 | (12 << 8) } },
			{ { -1, -1, 167 | (12 << 8), -1 }, { -1, -1, -1, 167 | (12 << 8) }, { 167 | (4 << 8), -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), -1, -1, 167 | (12 << 8) }, { -1, 167 | (12 << 8), -1, 167 | (12 << 8) } },
			{ { -1, 167, 167, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { 167, 167, 167, 167 | (12 << 8) } } });

	public static final FossilPiece fossilPieceSkull4 = new FossilPiece(new short[][][] {
			{ { -1, -1, -1, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167, 167, -1 } },
			{ { -1, 167, 167, -1 }, { 167 | (4 << 8), -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), 167 | (12 << 8), -1, 167 } },
			{ { -1, -1, 167 | (12 << 8), -1 }, { -1, -1, -1, 167 | (12 << 8) },
					{ 167 | (12 << 8), -1, -1, 167 | (12 << 8) }, { -1, 167 | (12 << 8), 167, -1 } },
			{ { -1, 167, 167, -1 }, { 167 | (4 << 8), -1, -1, 167 | (12 << 8) }, { 167 | (12 << 8), -1, -1, 167 | (12 << 8) },
					{ 167 | (4 << 8), 167 | (12 << 8), -1, 167 } },
			{ { -1, -1, -1, -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 }, { -1, 167 | (4 << 8), 167 | (4 << 8), -1 },
					{ -1, 167, 167, -1 } } });

	public static final FossilPiece fossilPieceBody1 = new FossilPiece(new short[][][] { 
			{ { 167, 167, -1 }, { -1, -1, -1 } },
			{ { -1, -1, 167 | (4 << 8) }, { -1, -1, 167 | (4 << 8) } },
			{ { 167, 167, -1 }, { -1, -1, -1 } } });

	public static final FossilPiece fossilPieceBody2 = new FossilPiece(new short[][][] { 
			{ { -1, 167, 167, -1 }, { -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { -1, -1, -1, 167 | (4 << 8) }, { -1, -1, -1, 167 | (4 << 8) } },
			{ { 167 | (12 << 8), -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { -1, 167, 167, -1 }, { -1, -1, -1, -1 } } });

	public static final FossilPiece fossilPieceBody3 = new FossilPiece(	new short[][][] { 
			{ { 167, 167, 167, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { -1, -1, -1, 167 | (4 << 8) }, { -1, -1, -1, 167 | (4 << 8) } },
			{ { -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1 } },
			{ { 167, 167, 167, 167 | (12 << 8) }, { -1, -1, -1, -1 } } });

	public static final FossilPiece fossilPieceBody4 = new FossilPiece(new short[][][] { 
			{ { -1, 167, 167, 167, -1 }, { -1, -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { -1, -1, -1, -1, 167 | (4 << 8) }, { -1, -1, -1, -1, 167 | (4 << 8) } },
			{ { -1, -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { 167 | (12 << 8), -1, -1, -1, 167 | (12 << 8) }, { -1, -1, -1, -1, -1 } },
			{ { -1, 167, 167, 167, -1 }, { -1, -1, -1, -1, -1 } } });

	public static final FossilPiece[] fossilPieceSkulls = new FossilPiece[] { fossilPieceSkull1, fossilPieceSkull2,
			fossilPieceSkull3, fossilPieceSkull4 };

	public static final FossilPiece[] fossilPieceBodys = new FossilPiece[] { fossilPieceBody1, fossilPieceBody2,
			fossilPieceBody3, fossilPieceBody4 };

	public FeatureFossil(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.x0Abs = (this.originChunkX - this.getFeatureRadius()) << 4;
		this.z0Abs = (this.originChunkZ - this.getFeatureRadius()) << 4;
	}

	@Override
	public int getFeatureRadius() {
		return 1;
	}

	@Override
	public int getSpawnChance() {
		return 22;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX,
			int chunkZ) {
		return (biome instanceof BiomeGenDesert) && !world.isOceanChunk(chunkX, chunkZ);
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		this.drawPieceOnGeneration(chunkX, chunkZ, chunk);
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {

	}

	@Override
	public int getFeatureHeight() {
		return 64;
	}

	@Override
	public void generateSchematic(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		boolean sky = this.featureProvider.chunkProvider instanceof ChunkProviderSky;
		
		// Fill the whole schematic with -1s
		for (short[][] arr1 : this.schematic) {
			for (short[] arr2 : arr1) {
				Arrays.fill(arr2, (short) -1);
			}
		}

		// Feature some skeletons
		int maxSkeletons = 2 + rand.nextInt(4);
		int featureSizeBlocks = (this.getFeatureRadius() * 2 + 1) << 4;

		for (int skeletons = 0; skeletons < maxSkeletons; skeletons++) {
			boolean tryAgain = true;

			while (tryAgain) {
				// Only try again once.
				tryAgain = false;

				FossilPiece skull = fossilPieceSkulls[rand.nextInt(fossilPieceSkulls.length)];
				FossilPiece body = fossilPieceBodys[rand.nextInt(fossilPieceBodys.length)];

				// Calculate tail length / direction
				int tailLength = 5 + rand.nextInt(15);
				boolean direction = rand.nextBoolean();

				// Place skull
				int skullX;
				int skullZ;

				int x1, z1, x2, z2;
				FeatureAABB pieceAABB = null;

				if (direction) {
					// Rotated; towards -x
					skullZ = 5 + rand.nextInt(featureSizeBlocks - 10);
					skullX = 3 + skull.length + 2 * tailLength
							+ rand.nextInt(featureSizeBlocks - skull.length - 2 * tailLength);

					x1 = skullX - skull.length / 2 - 1 - 2 * tailLength;
					x2 = skullX + skull.length / 2;

					z1 = Math.min(skullZ - skull.width / 2, skullZ - body.width / 2);
					z2 = Math.max(skullZ + skull.width / 2, skullZ + body.width / 2);
				} else {
					// Not rotated, Towards -z
					skullX = 5 + rand.nextInt(featureSizeBlocks - 10);
					skullZ = 3 + skull.length + 2 * tailLength
							+ rand.nextInt(featureSizeBlocks - skull.length - 2 * tailLength);

					z1 = skullZ - skull.width / 2 - 1 - 2 * tailLength;
					z2 = skullZ + skull.width / 2;

					x1 = Math.min(skullX - skull.width / 2, skullX - body.width / 2);
					x2 = Math.max(skullX + skull.width / 2, skullX + body.width / 2);
				}

				pieceAABB = new FeatureAABB(x1, 0, z1, x2, 7, z2);

				// Check if it fits
				if (!this.aabb.containsFully(pieceAABB) || this.collideWithAny(pieceAABB)) {
					continue;
				}

				// Draw
				if (rand.nextBoolean() && !sky) {
					skull.draw(this.schematic, skullX, 0, skullZ, direction);

					if (direction) {
						int x = skullX - skull.length / 2 - 1;
						for (int i = 0; i < tailLength; i++) {
							body.draw(this.schematic, x, 0, skullZ, direction);
							x -= 2;
						}
					} else {
						int z = skullZ - skull.length / 2 - 1;
						for (int i = 0; i < tailLength; i++) {
							body.draw(this.schematic, skullX, 0, z, direction);
							z -= 2;
						}
					}
				} else {
					int skullY = this.surfaceHeight(skullX, skullZ);
					
					if(!sky || skullY > 0) {			
						skull.draw(this.schematic, skullX, skullY, skullZ, direction);

					if (direction) {
						int x = skullX - skull.length / 2 - 1;
						for (int i = 0; i < tailLength; i++) {
								skullY = this.surfaceHeight(x, skullZ);
								if(sky && skullY == 0) break;
								
								body.draw(this.schematic, x, skullY, skullZ, direction);
							x -= 2;
						}
					} else {
						int z = skullZ - skull.length / 2 - 1;
						for (int i = 0; i < tailLength; i++) {
								skullY = this.surfaceHeight(skullX, z);
								if(sky && skullY == 0) break;
								
								body.draw(this.schematic, skullX, skullY, z, direction);
							z -= 2;
							}
						}
					}
				}

				// Finally
				this.pieceAABBs.add(pieceAABB);

				break;
			}
		}

		System.out.println("Fossils @ " + this.centerX + " " + this.centerZ);
	}

	private int surfaceHeight(int x, int z) {
		int h = world.getLandSurfaceHeightValue(x + this.x0Abs, z + this.z0Abs) - 64;
		if (h < 0) {
			h = 0;
		}
		if (h > 56) {
			h = 56;
		}
		return h;
	}

	private boolean collideWithAny(FeatureAABB aabb) {
		Iterator<FeatureAABB> iterator = this.pieceAABBs.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().collidesWith(aabb))
				return true;
		}

		return false;
	}

	@Override
	public int getY0() {
		return 64;
	}

}
