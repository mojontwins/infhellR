# Height-Only Chunk Queries: Caching and Avoiding Full Terrain Generation

Status: study / design proposal (no code changes yet).
Applies equally to client and server trees (all involved classes are shared
`net.minecraft.game.*`, so the final implementation must stay byte-identical on
both sides, per project convention).

---

## 1. What the engine does today

### 1.1 The four public entry points

All tidy up in one place: `World.java` (both trees).

| Method | Line | Behaviour when the chunk is NOT loaded |
|---|---|---|
| `World.getLandSurfaceHeightValue(bx,bz)` | 839 | `chunkProvider.justGenerateForHeight(cx,cz)` then `chunk.getLandSurfaceHeightValue(...)` |
| `World.isOceanChunk(cx,cz)` | 850 | `chunkProvider.justGenerateForHeight(cx,cz)` then `chunk.isOcean` |
| `World.isUrbanChunk(cx,cz)` | 861 | `chunkProvider.justGenerateForHeight(cx,cz)` then `chunk.isUrbanChunk` |
| `World.justGenerateForHeight(cx,cz)` | 872 | returns the live chunk if it exists, else `chunkProvider.justGenerateForHeight` |

When the chunk *does* exist, all four short-circuit to the real chunk
(`World.chunkExists` + `getChunkFromChunkCoords`), so the cost below only ever
applies to **not-yet-generated neighbour chunks**.

### 1.2 The provider chain

`World.chunkProvider` is built from the world type:

```
World.getChunkProvider()          → new ChunkProvider(world, loader, worldProvider.getChunkProvider())
WorldProvider.getChunkProvider()  → worldInfo.getTerrainType().getChunkGenerator(world)
WorldType.getChunkGenerator()     → ChunkProviderGenerate            (overworld)
                                 → ChunkProviderSky                  (sky dimension)
```

Dedicated server: `WorldServer.getChunkProvider()` → `ChunkProviderServer`,
whose `justGenerateForHeight(x,z)` delegates straight to
`serverChunkGenerator.justGenerateForHeight` (same `ChunkProviderGenerate`).

**Key fact:** `ChunkProvider.justGenerateForHeight` (the `ChunkProvider`
wrapper) simply forwards to the inner generator:

```java
public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
    return this.chunkProvider.justGenerateForHeight(chunkX, chunkZ);   // ChunkProvider.java:99
}
```

It deliberately bypasses the `chunkMap` cache **and** the on-disk
`loadChunkFromFile`. So there is no memoisation anywhere on the path:
**every height query on an unloaded chunk regenerates the whole thing.**

(For completeness: `ChunkProviderLoadOrGenerate.justGenerateForHeight` is a
full `provideChunk` — it materialises a real chunk and caches it in its
32×32 ring. That class is not on the overworld/server path today, so the
height-only variant below is what matters.)

### 1.3 What `ChunkProviderGenerate.justGenerateForHeight` costs

```java
public Chunk justGenerateForHeight(int chunkX, int chunkZ) {   // ChunkProviderGenerate.java:460
    this.rand.setSeed(...);
    byte[] blockArray = new byte[32768];
    byte[] metadata   = new byte[32768];                       // 2 × 32 KB wasted per call
    Chunk chunk = new Chunk(world, blockArray, metadata, cx, cz);
    chunk.isUrbanChunk = worldChunkManager.isUrbanChunk(cx, cz);
    this.generateTerrain(chunkX, chunkZ, blockArray);          // density lattice + 32768 block writes
    chunk.generateLandSurfaceHeightMap();                      // 16×16 columns × ~128 opacity reads
    chunk.isOcean = this.isOcean;
    return chunk;
}
```

`generateTerrain` (line 168) is the expensive part, in three nested stages:

1. **`initializeNoiseField`** (line 508) — the heavy part:
   - five octave-noise passes over the 5×17×5 lattice plus the 5×5
     `scaleNoise`/`depthNoise` planes;
   - **625 `WorldChunkManager.getBiomeGenAt()` calls** (5×5×25 distance-weighted
     window) and **625 `isUrbanChunk()` calls** to shape the density field from
     biome min/max heights. Each `getBiomeGenAt` is an uncached 1×1
     `loadBlockGeneratorData` noise evaluation.
2. `generateTerrain` inner loop — 32768 per-block trilinear interpolation
   evaluations + block/water/air writes + the `isOcean` column test.
3. `generateLandSurfaceHeightMap` — 256 columns × (up to) 128
   `Block.lightOpacity` reads (top→down).

### 1.4 The repeat-call problem

The four facades are called from many places; several of them hammer the *
same* not-yet-generated chunk repeatedly during one populate():

- `WorldGenSurfaceMoss` (1–3 attempts per chunk, targets can land in the +1
  neighbour chunk) → `getLandSurfaceHeightValue`, ChunkProviderGenerate:1189.
- Biome populators (`BiomeGenSavanna`, `BiomeGenForest`, `BiomeGenHotForest`,
  `BiomeGenDarkForest`, `BiomeGenDesertOutskirts`, …) → a *batch* of
  `getLandSurfaceHeightValue` calls; each new column in an unloaded neighbour
  chunk triggers a full height-only generation.
- Feature placement: `FeatureAmazonVillage.badChunksOnCorners` issues up to
  four `justGenerateForHeight` calls per candidate AABB (same corner chunk
  queried repeatedly across candidate iterations); `FeatureSlimeBossLair`,
  `FeatureSinkHole`, `FeatureShip`, `FeatureBigShip`, `FeatureOceanRuins`,
  `FeatureFossil` all run `isOceanChunk`/`isUrbanChunk` on neighbours.
- `FeatureStoneArch`, `FeatureBuilding`, `FeatureIcePalace`, `FeatureFossil`,
  `ComponentStrongholdStairs`, `CommandTree`, `SinglePlayerCommands` query
  `getLandSurfaceHeightValue` per column.

Each miss is a full `generateTerrain` (noise lattice + biomes + block writes).
Because nothing caches the result, N queries for the same chunk cost ~N full
generations. **This is the lag the user reports.**

---

## 2. Solution 1 — cache the height-only result (recommended, primary)

### 2.1 Design

A tiny bounded LRU cache keyed by chunk coordinates. On a miss, run the normal
`justGenerateForHeight` path once and store a *lightweight* payload:

```java
class TerrainHeightEntry {          // per cached chunk
    final int chunkX, chunkZ;
    final byte[] landSurfaceHeightMap;  // 256 bytes
    final boolean isOcean;
    final boolean isUrbanChunk;
}
```

Trade-off that matters: the cached **Chunk** keeps two 32 KB flat arrays
(64 KB) plus height maps; caching the *payload* costs ≈ 350 bytes/entry.
256 entries ≈ **< 100 KB**, even 1024 entries stay under 400 KB. 64 KB × 1024
would be 64 MB — don't cache whole height-only Chunks unbounded.

Data structure: `LinkedHashMap<Integer, TerrainHeightEntry>` with
`removeEldestEntry`, capacity ≈ 128–512 (tunable constant). Key =
`ChunkCoordIntPair.chunkXZ2Int(cx, cz)` — already used all over the codebase,
and integer keys are cheap and dense.

### 2.2 Where to put it

**Option A (recommended): the four `World` facades.** `World` is already the
single chokepoint every query goes through (Section 1.1), and it covers every
provider chain — `ChunkProvider` wrapper (singleplayer), `ChunkProviderServer`
(dedicated server), `ChunkProviderSky`, `ChunkProviderHell`, and even the
legacy `ChunkProviderLoadOrGenerate` if it ever becomes active. Miss handling:

```java
private TerrainHeightQueryCache heightQueryCache = new TerrainHeightQueryCache();

public int getLandSurfaceHeightValue(int bx, int bz) {
    int cx = bx >> 4, cz = bz >> 4;
    if (this.chunkExists(cx, cz)) {
        return this.getChunkFromChunkCoords(cx, cz).getLandSurfaceHeightValue(bx & 15, bz & 15);
    }
    return this.heightQueryCache.getOrCompute(cx, cz, this).landSurfaceHeightMap[(bz & 15) << 4 | (bx & 15)];
}
```

`isOceanChunk`, `isUrbanChunk` and `justGenerateForHeight` follow the same
pattern. `justGenerateForHeight` is the odd one because it returns a `Chunk`;
`getOrCompute` must therefore also be able to **synthesise a Chunk on a hit**:

```java
Chunk chunk = new Chunk(world, cx, cz);          // cheap, no 32 KB arrays
chunk.landSurfaceHeightMap = entry.landSurfaceHeightMap;
chunk.isOcean   = entry.isOcean;
chunk.isUrbanChunk = entry.isUrbanChunk;
chunk.hasBuilding = false;                        // height-only result never has a building
```

That is contractually fine: today's height-only Chunk exposes exactly
`landSurfaceHeightMap`, `isOcean`, `isUrbanChunk`, `hasBuilding` (always
false — `buildOnChunk` never runs in `justGenerateForHeight`), and nothing
else. All current readers (`FeatureSlimeBossLair`, `FeatureAmazonVillage`,
`getLandSurfaceHeightValue`) only touch those members. Note the returned Chunk
has an all-zero `heightMap` today and will have one after a hit too — do not
"synthesise" a `heightMap` you cannot compute cheaply.

**Option B (equivalent, thematically aligned with the recent World
decomposition): `TerrainHeightQueryCache` as a collaborator owned by World.**
Same as A but packaged as a small class (`game/world/`) holding the map and
the four lookup methods — mirrors the `SkylightTracker`/`BlockTickScheduler`
pattern and keeps `World.java` thin. This is the recommended packaging; the
cache itself could also live on each generator if it is preferred to keep
`World` untouched, at the price of duplicating the logic in
`ChunkProviderGenerate`/`ChunkProviderSky`/`ChunkProviderHell`.

**Consistency guarantee (no invalidation needed):**
- While the real chunk does not exist, the cached base-terrain height is the
  exact value a fresh generation would produce (the generator is
  seed-deterministic).
- As soon as the real chunk *is* generated, the facades short-circuit past the
  cache to the live chunk (`chunkExists`), so a stale entry is never served.
- If the chunk later unloads, the cache returns the *base pre-population*
  height — which is precisely what these queries are defined to answer, and is
  identical to what today's re-generation would compute. Correct by definition,
  and strictly cheaper.
- Bounded memory via LRU eviction; optionally drop the entry inside the chunk
  unload path for tidiness (not required for correctness).

### 2.3 Impact

Eliminates the entire repeated `generateTerrain` cost for the hot patterns in
Section 1.4 (multi-column biome pops, multi-corner feature placement, moss
attempts). The first query for a chunk still pays full cost (unchanged latency
for a true miss). Pure win, no semantic change, no provider changes needed,
both trees identical.

---

## 3. Solution 2 — land surface height without materialising terrain

The land-surface height map is *defined* by the density field alone:

- `generateTerrain` writes **stone exactly where `density > 0`**, water
  (`yy < 64`, density ≤ 0) or air otherwise.
- `generateLandSurfaceHeightMap` scans top→down for the topmost block with
  `Block.lightOpacity ≥ 255` — i.e. the topmost stone.
- Stone is the only opaque block the height-only path ever emits.

Therefore:

> `landSurfaceHeight(x, z) = max{ y in [0,127] : density(x, y, z) > 0 }`

(`isOcean` likewise: `false` iff any column has `density(x, 63, z) > 0`.)

So **yes — the height map can be computed without materialising the block
arrays**, by evaluating the same trilinear-interpolated density and recording
per-column maxima instead of writing bytes.

### 3.1 Option A — exact height-only evaluation (recommended)

Replace the `generateTerrain` + `justGenerateForHeight` body with:

```java
// new method on ChunkProviderGenerate
public Chunk justGenerateForHeight(int cx, int cz) {          // exact
    this.rand.setSeed((long)cx * 341873128712L + (long)cz * 132897987541L);
    Chunk chunk = new Chunk(this.worldObj, cx, cz);           // NO 2×32 KB arrays
    chunk.isUrbanChunk = this.worldObj.getWorldChunkManager().isUrbanChunk(cx, cz);
    byte[] surface = new byte[256];
    boolean ocean = true;

    double[] density = this.initializeNoiseField(this.terrainNoise,
        cx * 4, 0, cz * 4, 5, 17, 5, cx, cz);                 // same lattice as generateTerrain

    // Per-column top-down scan: first yy with density > 0 is the land surface.
    // (Same interpolation formulas as the generateTerrain block loop.)
    for (int z = 0; z < 16; z++) {
        for (int x = 0; x < 16; x++) {
            int top = 0;
            for (int yy = 127; yy >= 0; yy--) {
                ... evaluate interpolated density as in generateTerrain ...
                if (densityAt > 0.0D) { top = yy; break; }
                if (yy == 63 && densityAt > 0.0D) ocean = false;   // or fold into one pass
            }
            surface[z << 4 | x] = (byte) top;
        }
    }
    chunk.landSurfaceHeightMap = surface;
    chunk.isOcean = ocean;
    return chunk;
}
```

Properties:
- **Exact**: numerically identical `landSurfaceHeightMap`, `isOcean`,
  `isUrbanChunk` to today's result (prove with a probe — see §5).
- Cheaper than today's miss: no 2×32 KB allocations, no 32768 block writes,
  no second 32768-entry `lightOpacity` scan, and the per-column scan stops at
  the surface (average terrain height ≈ 64 → roughly **half** the per-block
  interpolation evaluations).
- The **dominant cost remains `initializeNoiseField`** — the five octave
  passes plus the 625 biome distance-weighted samples. Those cannot be removed
  if exactness is required, because the density lattice itself is shaped by
  biome min/max heights and urban flags (lines 555–586). A height-only call
  shares this with full generation; caching (Solution 1) is what kills the
  *repeat* cost; this option only trims each *miss*.

### 3.2 Option B — approximate 2D height (very cheap, non-exact)

Skip the 3D octaves (`mainNoise`, `minLimitNoise`, `maxLimitNoise`) entirely
and derive a column height from the 5×5 biome blend (`avgMinHeight`/
`avgMaxHeight`, the `scaleArray`/`depthArray` planes) that `initializeNoiseField`
already computes. This yields a smooth regional surface estimate at maybe
1–5% of the cost of full generation.

**It will not match the actual generated terrain** (no hills, grooves,
overhangs, floating islands). Only use it for callers that tolerate a coarse
Y (e.g. "rough spawn altitude" previews). Do **not** use it for anything that
anchors geometry on the real cut height — building placement, arches, fossils,
moss anchors all depend on the true surface. Presented for completeness; the
recommendation is Option A.

### 3.3 Option C — orthogonal: make `initializeNoiseField` cheaper for everyone

The 625 uncached `getBiomeGenAt` 1×1 samples per chunk (and per height query)
can be collapsed into one decoded 21×21 biome grid (`loadBlockGeneratorData`
on `(chunkX*16 − 2, chunkZ*16 − 2, 24, 24)`-ish) plus lookups, with the
distance window reading pixels out of that grid. This benefits *both* full
generation and height-only queries. Out of scope for this study beyond the
flavour text; note it so the decision isn't accidentally baked out.

---

## 4. Recommended plan

1. **Solution 1 (cache at the World facade / `TerrainHeightQueryCache`
   collaborator).** Primary fix; directly removes the repeat-call lag with a
   ~350-byte entry cost per queried chunk. No changes to providers, no risk to
   the real-chunk path, deterministic.
2. **Solution 2-A (exact density-only height evaluation).** Secondary
   optimisation that shrinks each *miss*. Requires a verification probe to
   prove byte-equality with today's values before switching.
3. Optionally revisit Option C later as a purely-generator-wide speedup.
4. Keep Solution 2-B out of scope unless a caller explicitly signs up to a
   coarse height.

Both 1 and 2-A are implemented in shared `game.*` classes and must be applied
identically to `src/minecraft` and `src/minecraft_server`.

---

## 5. Verification

- **Equality probe** (extend the existing headless
  `C:\Users\na_th\AppData\Local\Temp\opencode\lightprobe\LightProbe.java`
  harness): for a grid of random chunk coords, assert
  `cache.justGenerateForHeight(cx,cz)` returns `landSurfaceHeightMap` +
  `isOcean` + `isUrbanChunk` that are byte-identical to a fresh
  `generateTerrain`-based generation; then assert two consecutive cached calls
  hit the cache (e.g. a hit counter) and that the real-chunk path still wins
  once the chunk is created.
- `build.bat` for both jars; parity check (client/server byte-identical).
- Manual in-game spot-check: same world seed, compare a populated area's
  buildings/moss/arches against the pre-change build.

## 6. Files touched (predicted)

- `src/minecraft/net/minecraft/game/world/TerrainHeightQueryCache.java` (new)
- `src/minecraft/net/minecraft/game/world/World.java` (four facades: 839,
  850, 861, 872)
- `src/minecraft/net/minecraft/game/world/terrain/ChunkProviderGenerate.java`
  (`justGenerateForHeight`, new density-only scan)
- `src/minecraft/net/minecraft/game/world/chunk/ChunkProviderSky.java` /
  `ChunkProviderHell.java` (same pattern, if their height queries matter)
- mirrors under `src/minecraft_server/`