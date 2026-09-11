package net.minecraft.game.world;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.game.MathHelper;
import net.minecraft.game.Seasons;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EnumCreatureType;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityCow;
import net.minecraft.game.entity.animal.EntityPig;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.human.EntityAlphaWitch;
import net.minecraft.game.entity.human.EntityCowman;
import net.minecraft.game.entity.human.EntityPigman;
import net.minecraft.game.entity.human.EntityTrader;
import net.minecraft.game.entity.monster.EntityHusk;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.BlockBed;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.path.PathEntity;
import net.minecraft.game.world.path.PathPoint;
import net.minecraft.game.world.path.Pathfinder;

/**
 * Handles passive and monster entity spawning in loaded chunks.
 *
 * <p>The spawning system works in two phases:</p>
 * <ol>
 *   <li><b>Chunk collection:</b> For every player, collect all chunks within an
 *       {@link #CHUNK_SEARCH_RADIUS}-chunk radius as eligible spawn locations.</li>
 *   <li><b>Creature-type iteration:</b> For each {@link EnumCreatureType}, compute
 *       the maximum entity cap and horde size (modified by blood moon and season),
 *       then iterate eligible chunks, pick a biome-weighted mob, find a valid
 *       position, and attempt to spawn a horde.</li>
 * </ol>
 *
 * @see EnumCreatureType
 */
public final class SpawnerAnimals {
	private static final int CHUNK_SEARCH_RADIUS = 8;
	private static final int MAX_SPAWN_HEIGHT = Chunk.SECTION_HEIGHT;
	private static final int SPAWNING_ATTEMPTS_PER_CHUNK = 3;
	private static final int SPAWN_POSITION_RADIUS = 6;
	private static final float MIN_SPAWN_DISTANCE_SQ = 576.0F; // 24 blocks^2
	private static final double PLAYER_DETECTION_RANGE = 24.0D;

	private static Set<ChunkCoordIntPair> eligibleChunksForSpawning = new HashSet<ChunkCoordIntPair>();

	/**
	 * Entity types that spawn near sleeping players during blood moons.
	 */
	protected static final Class<?>[] nightSpawnEntities = new Class[]{
		EntitySpider.class, EntityZombie.class, EntitySkeleton.class
	};

	/**
	 * Attempts to spawn entities of all creature types in all eligible chunks near every player.
	 *
	 * @param world         the world to spawn in
	 * @param despawnCreative true to allow hostile creature spawning (non-peaceful)
	 * @param spawnPeaceful   true to allow peaceful (animal) creature spawning
	 * @return the total number of entities spawned this tick
	 */
	public static final int performSpawning(World world, boolean despawnCreative, boolean spawnPeaceful) {
		if (!despawnCreative && !spawnPeaceful) {
			return 0;
		}

		// Phase 1: Collect all chunks within radius of every player
		collectEligibleChunks(world);

		int totalSpawned = 0;
		ChunkCoordinates worldSpawnPoint = world.getSpawnPoint();

		// Phase 2: For each creature type, try to spawn hordes in eligible chunks
		for (EnumCreatureType creatureType : EnumCreatureType.values()) {
			int maxEntities = computeMaxEntities(creatureType, eligibleChunksForSpawning.size(), world);
			int hordeSize = computeHordeSize(creatureType, world);

			if (!canSpawnType(creatureType, despawnCreative, spawnPeaceful, world, maxEntities)) {
				continue;
			}

			for (ChunkCoordIntPair chunkCoords : eligibleChunksForSpawning) {
				SpawnListEntry mobToSpawn = selectWeightedMob(world, chunkCoords, creatureType);
				if (mobToSpawn == null) {
					continue;
				}

				ChunkPosition spawnPos = findValidSpawnPosition(world, chunkCoords, creatureType);
				if (spawnPos == null) {
					continue;
				}

				int spawnedCount = spawnHorde(
					world, creatureType, mobToSpawn,
					spawnPos.x, spawnPos.y, spawnPos.z,
					hordeSize, worldSpawnPoint
				);

				// If entity instantiation threw an exception, abort spawning entirely
				if (spawnedCount < 0) {
					return totalSpawned;
				}

				// Accumulate once per chunk (not per attempt)
				totalSpawned += spawnedCount;
			}
		}

		return totalSpawned;
	}

	// =========================================================================
	// Helpers for performSpawning
	// =========================================================================

	/**
	 * Collects all chunk coordinates within {@link #CHUNK_SEARCH_RADIUS} of every player.
	 * Results are stored in {@link #eligibleChunksForSpawning}.
	 */
	private static void collectEligibleChunks(World world) {
		eligibleChunksForSpawning.clear();

		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer player = (EntityPlayer) world.playerEntities.get(i);
			int playerChunkX = MathHelper.floor_double(player.posX / 16.0D);
			int playerChunkZ = MathHelper.floor_double(player.posZ / 16.0D);

			for (int dx = -CHUNK_SEARCH_RADIUS; dx <= CHUNK_SEARCH_RADIUS; ++dx) {
				for (int dz = -CHUNK_SEARCH_RADIUS; dz <= CHUNK_SEARCH_RADIUS; ++dz) {
					eligibleChunksForSpawning.add(new ChunkCoordIntPair(dx + playerChunkX, dz + playerChunkZ));
				}
			}
		}
	}

	/**
	 * Computes the maximum number of entities of the given type allowed in the world.
	 *
	 * <p>Base formula: {@code maxPerType * eligibleChunks / 256}.</p>
	 * <p>Modifications:</p>
	 * <ul>
	 *   <li>Monsters: blood moon doubles the cap; spring (+50%) / autumn (-25%).</li>
	 *   <li>Creatures: autumn (+50%) / spring (-25%).</li>
	 * </ul>
	 */
	private static int computeMaxEntities(EnumCreatureType type, int chunkCount, World world) {
		int maxEntities = type.getMaxNumberOfCreature() * chunkCount / 256;

		if (type == EnumCreatureType.monster) {
			if (world.worldInfo.isBloodMoon()) {
				maxEntities *= 2;
			}
			// Season 0 = spring: +50%; Season 2 = autumn: -25%
			if (Seasons.currentSeason == 0) {
				maxEntities = maxEntities + (maxEntities >> 1);
			} else if (Seasons.currentSeason == 2) {
				maxEntities = (maxEntities >> 2) + (maxEntities >> 1);
			}
		} else {
			// Peaceful creatures: inverse season scaling
			if (Seasons.currentSeason == 2) {
				maxEntities = maxEntities + (maxEntities >> 1);
			} else if (Seasons.currentSeason == 0) {
				maxEntities = (maxEntities >> 2) + (maxEntities >> 1);
			}
		}

		return maxEntities;
	}

	/**
	 * Computes the number of entities to attempt spawning per horde.
	 *
	 * <p>Monsters get a base of 6 (4 + 2), peaceful creatures get 4.
	 * Both are doubled during a blood moon.</p>
	 */
	private static int computeHordeSize(EnumCreatureType type, World world) {
		int hordeSize = 4;
		if (type == EnumCreatureType.monster) {
			hordeSize += 2;
		}
		if (world.worldInfo.isBloodMoon()) {
			hordeSize *= 2;
		}
		return hordeSize;
	}

	/**
	 * Checks whether this creature type is eligible to spawn this tick.
	 *
	 * <p>Peaceful creatures only spawn if {@code spawnPeaceful} is true;
	 * hostile creatures only spawn if {@code despawnCreative} is true.
	 * Additionally, the current count must not exceed the cap.</p>
	 */
	private static boolean canSpawnType(EnumCreatureType type, boolean despawnCreative, boolean spawnPeaceful, World world, int maxEntities) {
		if (type.getPeacefulCreature() && !spawnPeaceful) {
			return false;
		}
		if (!type.getPeacefulCreature() && !despawnCreative) {
			return false;
		}
		return world.getCachedEntityCount(type.getCreatureClass()) <= maxEntities;
	}

	/**
	 * Picks a random mob from the biome's spawn list using weighted random selection.
	 *
	 * <p>Urban mobs ( {@link SpawnListEntry#isUrban} ) only spawn in chunks that have
	 * a building or road; if an urban mob is selected in a non-city chunk, the selection
	 * re-rolls.</p>
	 *
	 * @return the selected mob, or {@code null} if the biome has no spawn list for this type
	 */
	private static SpawnListEntry selectWeightedMob(World world, ChunkCoordIntPair chunkCoords, EnumCreatureType creatureType) {
		Chunk chunk = world.getChunkFromChunkCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos);
		BiomeGenBase biome = chunk.getBiomeGenAt(8, 8);
		List<SpawnListEntry> spawnList = biome.getSpawnableList(creatureType);

		if (spawnList == null || spawnList.isEmpty()) {
			return null;
		}

		// Compute total rarity (sum of all spawnRarityRate values)
		int totalRarity = 0;
		for (SpawnListEntry entry : spawnList) {
			totalRarity += entry.spawnRarityRate;
		}

		// Weighted random selection with urban mob re-roll
		SpawnListEntry selected = spawnList.get(0);
		int chance = world.rand.nextInt(totalRarity);

		for (SpawnListEntry candidate : spawnList) {
			chance -= candidate.spawnRarityRate;
			if (chance < 0) {
				// Urban mobs only spawn in cities (chunks with buildings or roads)
				if (!candidate.isUrban || chunk.hasBuilding || chunk.hasRoad) {
					selected = candidate;
					break;
				} else {
					// Re-roll: urban mob selected in non-city chunk, try again
					chance = world.rand.nextInt(totalRarity);
				}
			}
		}

		return selected;
	}

	/**
	 * Samples a random block tuple within a chunk for the given creature type and
	 * checks whether it is a valid spawn position.
	 *
	 * <p>Generates a single random block tuple and checks whether the block is a solid
	 * cube and whether its material matches the creature type's required material
	 * (e.g., air for land mobs, water for water mobs). Returns {@code null} when the
	 * position is invalid so the caller advances to the next eligible chunk; this
	 * mirrors the original spawner, which never re-rolls a single chunk.</p>
	 */
	private static ChunkPosition findValidSpawnPosition(World world, ChunkCoordIntPair chunkCoords, EnumCreatureType creatureType) {
		int x = chunkCoords.chunkXPos * 16 + world.rand.nextInt(16);
		int y = world.rand.nextInt(MAX_SPAWN_HEIGHT);
		int z = chunkCoords.chunkZPos * 16 + world.rand.nextInt(16);

		if (world.isBlockNormalCube(x, y, z)
				|| world.getBlockMaterial(x, y, z) != creatureType.getCreatureMaterial()) {
			return null;
		}

		return new ChunkPosition(x, y, z);
	}

	/**
	 * Attempts to spawn a horde of entities around a base position.
	 *
	 * <p>Performs {@link #SPAWNING_ATTEMPTS_PER_CHUNK} attempts, each scattering
	 * {@code hordeSize} entities within a {@link #SPAWN_POSITION_RADIUS}-block radius.
	 * Each entity is validated for spawn conditions, player proximity
	 * ( {@link #PLAYER_DETECTION_RANGE} blocks), and distance from world spawn
	 * ( {@link #MIN_SPAWN_DISTANCE_SQ} blocks²).</p>
	 *
	 * @return the number of entities successfully spawned, or -1 if an instantiation
	 *         exception occurred (caller should abort spawning)
	 */
	private static int spawnHorde(World world, EnumCreatureType creatureType, SpawnListEntry mobToSpawn,
			int baseX, int baseY, int baseZ, int hordeSize, ChunkCoordinates worldSpawnPoint) {

		int spawnedCount = 0;

		for (int attempt = 0; attempt < SPAWNING_ATTEMPTS_PER_CHUNK; ++attempt) {
			int x = baseX;
			int y = baseY;
			int z = baseZ;

			for (int i = 0; i < hordeSize; ++i) {
				// Scatter position randomly within radius
				x += world.rand.nextInt(SPAWN_POSITION_RADIUS) - world.rand.nextInt(SPAWN_POSITION_RADIUS);
				y += world.rand.nextInt(1) - world.rand.nextInt(1);
				z += world.rand.nextInt(SPAWN_POSITION_RADIUS) - world.rand.nextInt(SPAWN_POSITION_RADIUS);

				if (!canCreatureTypeSpawnAtLocation(creatureType, world, x, y, z)) {
					continue;
				}

				float spawnX = (float) x + 0.5F;
				float spawnY = (float) y;
				float spawnZ = (float) z + 0.5F;

				// Must be far enough from any player
				if (world.getClosestPlayer(spawnX, spawnY, spawnZ, PLAYER_DETECTION_RANGE) != null) {
					continue;
				}

				// Must be far enough from world spawn point
				float dx = spawnX - (float) worldSpawnPoint.posX;
				float dy = spawnY - (float) worldSpawnPoint.posY;
				float dz = spawnZ - (float) worldSpawnPoint.posZ;
				if (dx * dx + dy * dy + dz * dz < MIN_SPAWN_DISTANCE_SQ) {
					continue;
				}

				// Instantiate the entity via reflection
				EntityLiving spawnedEntity;
				try {
					spawnedEntity = (EntityLiving) mobToSpawn.entityClass
						.getConstructor(World.class)
						.newInstance(world);
				} catch (Exception e) {
					e.printStackTrace();
					return -1; // Signal to abort spawning entirely
				}

				spawnedEntity.setLocationAndAngles(
					spawnX, spawnY, spawnZ,
					world.rand.nextFloat() * 360.0F, 0.0F
				);

				if (spawnedEntity.getCanSpawnHere()) {
					++spawnedCount;
					world.spawnEntityInWorld(spawnedEntity);
					creatureSpecificInit(spawnedEntity, world, spawnX, spawnY, spawnZ);

					// If we've hit the per-chunk cap, stop this horde
					if (spawnedCount >= spawnedEntity.getMaxSpawnedInChunk()) {
						break;
					}
				}
			}
		}

		return spawnedCount;
	}

	// =========================================================================
	// Spawn position validation
	// =========================================================================

	/**
	 * Checks whether a creature of the given type can validly spawn at the block position.
	 *
	 * <ul>
	 *   <li>Water creatures: the block must be liquid and the block above must not be solid.</li>
	 *   <li>Land creatures: the block below must be solid, the block itself must not be
	 *       solid or liquid, and the block above must not be solid.</li>
	 * </ul>
	 */
	private static boolean canCreatureTypeSpawnAtLocation(EnumCreatureType type, World world, int x, int y, int z) {
		if (type.getCreatureMaterial() == Material.water) {
			return world.getBlockMaterial(x, y, z).getIsLiquid()
				&& !world.isBlockNormalCube(x, y + 1, z);
		}
		return world.isBlockNormalCube(x, y - 1, z)
			&& !world.isBlockNormalCube(x, y, z)
			&& !world.getBlockMaterial(x, y, z).getIsLiquid()
			&& !world.isBlockNormalCube(x, y + 1, z);
	}

	// =========================================================================
	// creature-specific post-spawn initialisation
	// =========================================================================

	/**
	 * Creates an entity, positions it, and adds it to the world.
	 */
	private static EntityLiving spawnSpecial(EntityLiving host, World world,
			float posX, float posY, float posZ, float rotationYaw, float rotationPitch) {
		host.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
		world.spawnEntityInWorld(host);
		return host;
	}

	/**
	 * Initialises a trader's inventory with their default trading offers.
	 */
	private static void setupTrader(EntityTrader trader, World world, boolean specialTrader) {
		trader.fillTradingRecipeList(world, specialTrader);
	}

	/**
	 * Applies creature-type-specific initialisation after spawning:
	 *
	 * <ul>
	 *   <li><b>Pig:</b> 1/128 chance of husk rider, 1/32 chance of pigman trader rider.</li>
	 *   <li><b>Cow:</b> 1/128 chance of husk rider, 1/32 chance of cowman trader rider.</li>
	 *   <li><b>Spider:</b> 1/128 chance each of skeleton, zombie, alex-zombie, or husk jockey.</li>
	 *   <li><b>Sheep:</b> randomises fleece colour.</li>
	 *   <li><b>Alpha Witch:</b> fills inventory with default items.</li>
	 *   <li><b>IMobWithLevel:</b> sets mob level from block metadata (or random if metadata is 0).</li>
	 * </ul>
	 */
	public static void creatureSpecificInit(EntityLiving host, World world, float posX, float posY, float posZ) {
		if (host instanceof EntityPig) {
			tryMountRider(host, world, posX, posY, posZ,
				128, new EntityHusk(world),
				32,  new EntityPigman(world), true);
		} else if (host instanceof EntityCow) {
			tryMountRider(host, world, posX, posY, posZ,
				128, new EntityHusk(world),
				32,  new EntityCowman(world), true);
		} else if (host instanceof EntitySpider) {
			EntityLiving rider = null;
			switch (world.rand.nextInt(128)) {
				case 0: rider = new EntitySkeleton(world); break;
				case 1: rider = new EntityZombie(world); break;
				case 2: rider = new EntityZombieAlex(world); break;
				case 3: rider = new EntityHusk(world); break;
			}
			if (rider != null) {
				spawnSpecial(rider, world, posX, posY, posZ, host.rotationYaw, 0.0F);
				rider.mountEntity(host);
			}
		} else if (host instanceof EntitySheep) {
			((EntitySheep) host).setFleeceColor(EntitySheep.getRandomFleeceColor(world.rand));
		} else if (host instanceof EntityAlphaWitch) {
			((EntityAlphaWitch) host).fillInventory();
		} else if (host instanceof IMobWithLevel) {
			int metadata = world.getBlockMetadata((int) posX, (int) posY, (int) posZ);
			IMobWithLevel mob = (IMobWithLevel) host;
			int level = metadata == 0 ? world.rand.nextInt(mob.getMaxLevel()) : metadata & 7;
			mob.setLevel(level);
		}
	}

	/**
	 * Attempts to mount a rider entity on a host with two probability tiers.
	 *
	 * @param host          the host animal to mount
	 * @param world         the world
	 * @param posX          X position
	 * @param posY          Y position
	 * @param posZ          Z position
	 * @param chance1       denominator for first rider (1/chance1 probability)
	 * @param rider1        the first rider type
	 * @param chance2       denominator for second rider (1/chance2 probability)
	 * @param rider2        the second rider type
	 * @param isTrader      true if the second rider is a trader that needs inventory setup
	 */
	private static void tryMountRider(EntityLiving host, World world,
			float posX, float posY, float posZ,
			int chance1, EntityLiving rider1,
			int chance2, EntityLiving rider2, boolean isTrader) {
		if (world.rand.nextInt(chance1) == 0) {
			EntityLiving rider = spawnSpecial(rider1, world, posX, posY, posZ, host.rotationYaw, 0.0F);
			rider.mountEntity(host);
		} else if (world.rand.nextInt(chance2) == 0) {
			EntityLiving rider = spawnSpecial(rider2, world, posX, posY, posZ, host.rotationYaw, 0.0F);
			if (isTrader) {
				setupTrader((EntityTrader) rider, world, false);
			}
			rider.mountEntity(host);
		}
	}

	// =========================================================================
	// performSleepSpawning — blood moon night horror spawning
	// =========================================================================

	/**
	 * Spawns hostile night-entities near sleeping players so they can be attacked
	 * and killed as an alternative to rest during a blood moon.
	 *
	 * <p>For each sleeping player, up to 20 attempts are made to:</p>
	 * <ol>
	 *   <li>Pick a random position near the player.</li>
	 *   <li>Find the surface (solid block below).</li>
	 *   <li>Spawn a night mob (spider, zombie, or skeleton).</li>
	 *   <li>Pathfind the mob toward the player's bed.</li>
	 *   <li>If the mob can reach the bed, teleport it there and wake the player.</li>
	 * </ol>
	 *
	 * @param players the list of sleeping players
	 * @return true if any monster was successfully spawned and the wake-up sequence should proceed
	 */
	public static boolean performSleepSpawning(World world, List<EntityPlayer> players) {
		boolean wokeUpAny = false;
		Pathfinder pathfinder = new Pathfinder(world);

		for (EntityPlayer player : players) {
			if (nightSpawnEntities == null || nightSpawnEntities.length == 0) {
				continue;
			}

			boolean wokeUp = false;

			for (int attempt = 0; attempt < 20 && !wokeUp; ++attempt) {
				// Pick a random position near the player
				int bx = MathHelper.floor_double(player.posX) + world.rand.nextInt(32) - world.rand.nextInt(32);
				int bz = MathHelper.floor_double(player.posZ) + world.rand.nextInt(32) - world.rand.nextInt(32);
				int by = MathHelper.floor_double(player.posY) + world.rand.nextInt(16) - world.rand.nextInt(16);
				by = Math.max(1, Math.min(Chunk.SECTION_HEIGHT, by));

				int nightMobIdx = world.rand.nextInt(nightSpawnEntities.length);

				// Find the surface: walk down to the first solid block
				int surfaceY = findSurfaceY(world, bx, by, bz);
				if (surfaceY < 0) {
					continue;
				}

				// Instantiate the night mob
				EntityLiving entity;
				try {
					entity = (EntityLiving) nightSpawnEntities[nightMobIdx]
						.getConstructor(World.class)
						.newInstance(world);
				} catch (Exception e) {
					e.printStackTrace();
					return wokeUpAny;
				}

				float spawnX = (float) bx + 0.5F;
				float spawnY = (float) surfaceY;
				float spawnZ = (float) bz + 0.5F;

				entity.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);

				if (!entity.getCanSpawnHere()) {
					continue;
				}

				// Check if the mob can pathfind to the player's bed
				PathEntity path = pathfinder.createEntityPathTo(entity, player, 32.0F);
				if (path == null || path.pathLength <= 1) {
					continue;
				}

				PathPoint firstPoint = path.getPathPoint();
				boolean nearPlayer =
					Math.abs(firstPoint.xCoord - player.posX) < 1.5D &&
					Math.abs(firstPoint.zCoord - player.posZ) < 1.5D &&
					Math.abs(firstPoint.yCoord - player.posY) < 1.5D;

				if (!nearPlayer) {
					continue;
				}

				// Find the nearest empty bed position
				ChunkCoordinates bedPos = BlockBed.getNearestEmptyChunkCoordinates(
					world,
					MathHelper.floor_double(player.posX),
					MathHelper.floor_double(player.posY),
					MathHelper.floor_double(player.posZ),
					1
				);
				if (bedPos == null) {
					bedPos = new ChunkCoordinates(bx, surfaceY + 1, bz);
				}

				// Teleport mob to the bed and wake the player
				float bedX = (float) bedPos.posX + 0.5F;
				float bedZ = (float) bedPos.posZ + 0.5F;
				entity.setLocationAndAngles(bedX, bedPos.posY, bedZ, 0.0F, 0.0F);
				world.spawnEntityInWorld(entity);
				creatureSpecificInit(entity, world, bedX, bedPos.posY, bedZ);
				player.wakeUpPlayer(true, false, false);
				entity.playLivingSound();
				wokeUpAny = true;
				wokeUp = true;
			}
		}

		return wokeUpAny;
	}

	/**
	 * Finds the surface Y coordinate at (x, z) near the given Y.
	 *
	 * <p>Walks downward from {@code startY} to find a solid block below, then walks
	 * upward to find a valid monster spawn position. Returns -1 if no valid surface
	 * was found.</p>
	 */
	private static int findSurfaceY(World world, int x, int startY, int z) {
		// Walk down to find the first solid block
		int surfaceY = startY;
		while (surfaceY > 2 && !world.isBlockNormalCube(x, surfaceY - 1, z)) {
			--surfaceY;
		}

		// Walk up to find a valid spawn position
		while (!canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, world, x, surfaceY, z)
				&& surfaceY < startY + 16
				&& surfaceY < Chunk.SECTION_HEIGHT) {
			++surfaceY;
		}

		if (surfaceY < startY + 16 && surfaceY < Chunk.SECTION_HEIGHT) {
			return surfaceY;
		}
		return -1;
	}
}
