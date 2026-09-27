# 256-Tall Terrain Generation Buffers

Branch: `256-tall-block-meta-arrays`

## Objective

Raise the transient terrain-generation buffers from 16x16x128 to 16x16x256 so that terrain
generators may legally write the entire column, then flatten them into the 16 runtime
subchunks without allocating subchunks that no longer need storage.

Concretely:

1. `ChunkProviderGenerate.provideChunk` allocates **65 536-byte** block-id and metadata
   buffers (16 x 256 x 16) instead of 32 768, indexed `x << 12 | z << 8 | y`.
2. `Chunk.loadFlatBlocks` slices **all 16** sections, not just the lower 8.
3. Flattening allocates only the contiguous prefix of subchunks that can hold content
   (see *Invariant* below). Sections above that prefix are never allocated.
4. **Generated terrain for y 0-127 must remain bit-identical.** Only storage strides change;
   every vertical loop bound, height limit, clip and RNG call stays exactly as it is.

Not assumed: the upper 128 layers are empty. Future feature generators are expected to build
above y 127, so the flattening scan must consider all 16 sections, and the flat buffers must be
able to carry high-altitude construction. The `y == 128` ceilings that exist in the generators
today are preserved only to keep this change's output identical; they are the natural place to
raise later.

## Invariant: contiguous-prefix materialization

`Chunk` materializes subchunks as a **contiguous prefix** `[0, topmost]`, where `topmost` is the
highest section index containing at least one non-air block. Sections above `topmost` stay
`null`.

The scan is **exhaustive over all 16 sections** and runs **top-down**, stopping at the first
non-air section. Once that section is found, everything below it is allocated.

```
section 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
          [------- null / implicit --------]  [ allocated ]
                                        topmost
```

Why a prefix rather than a per-section test: a `null` subchunk reads as sky-light 15, block
light 0 and air. That implicit state is only correct if the section is genuinely open to the
sky. With a prefix, a `null` section has nothing anywhere below it and an entirely empty column
above it, so the implicit values are correct by construction. A per-section rule would leave a
`null` section sandwiched between content below and an opaque roof above, and that pocket would
render fully lit.

Metadata alone never materializes a subchunk. Air carries no metadata worth keeping, and
allocating for it would defeat the purpose.

### Consumers re-checked against the new invariant

`subchunkCount` is no longer a constant 8 for a fresh chunk; it is `topmost + 1` and varies per
chunk (typically 5-6 with a surface around y 64, up to 16 for high construction). Every reader
was audited:

| Site | Behaviour under the new invariant |
|---|---|
| `Chunk.recomputeEmptyFlags:323` | Already computes `subchunkCount = highestMaterialized + 1`, i.e. `topmost + 1`. No change needed. |
| `Chunk.generateSkylightMap:448-453` | Resets `lightLevel = 15` at a null section. Now correct by construction instead of by luck. No change needed. |
| `Chunk.getSavedLightValue:777` | Null-guards, returns 15 sky / 0 block. Correct. |
| `Chunk.setLightValue:786` | Null-guards, no-op. Correct. |
| `Chunk.getChunkData:1244/1278/1306` | Null-guards; serializes null as block 0, block-light 0, sky `0xF`. Correct. |
| `World.randomTick:2281` | Iterates `getSubchunkCount()` but already `continue`s on `isSubchunkEmpty`, so the dropped eager sections 5-7 cost no probes. The comment claiming "10 * 8 = 80" is stale and gets corrected. |
| `WorldRenderer:218` | Iterates all 16 with `isSubchunkEmpty`. Correct. |
| `ChunkProviderClient.prepareChunk:36` | Feeds all-zero buffers; `topmost = -1` so nothing is materialized and `subchunkCount` stays 0. The `0xFF` skylight sentinel loop becomes dead and is removed. |
| `ChunkLoader:308-327` | Persists and replays the exact `SubchunkMask`, so the prefix round-trips to disk. Correct. |
| `StarlightEngine` | Reads a null nibble array as sky 15 / block 0, per the Anvil-native reference. Correct. |

## New requirement this exposes: client/server symmetry

`Chunk.setChunkData:1117-1137` currently derives its `materialize[]` mask per section from the
incoming block plane alone. With the server switching to a contiguous prefix, a mid-prefix all-air
section would arrive carrying real (dark) sky light while the client chose not to materialize it,
leaving the client reading sky 15. That divergence already exists in weaker form today; the new
rule widens it.

Fix: mirror the top-down contiguity in `setChunkData`, **scoped to the sections the packet
actually covers**. Scoping matters because `PlayerInstance.onUpdate:151-159` sends genuine
sub-volume windows to clients that have not loaded the chunk, and unconditionally materializing
`0..topmost` there would create out-of-range sections holding zero (dark) light. For the
dominant full-column packet (`EntityPlayerMP:257`, y 0..256) the scoped result is exactly
`0..topmost`.

The fully general fix is an explicit `SubchunkMask` in the `Packet51MapChunk` wire layout so the
client materializes precisely what the server holds. Noted, out of scope.

## Verified call graph

`ChunkProviderGenerate.provideChunk` (`ChunkProviderGenerate.java:354`):

1. `new byte[32768]` x2 -> `Chunk(world, blockArray, metadata, ...)`. The same arrays stay
   reachable as `chunk.blocks` / `chunk.data` and are read through the flat fallback.
2. `generateTerrain` - `<<11` / `<<7`, `indexInBlockArray += chunkHeight` (128 = z stride).
3. `underwaterGenerator.generate(..., blockArray)` - 1 239 index sites.
4. `featureProvider.getNearestFeatures(chunkX, chunkZ, chunk)`.
5. `oceanRavineGenerator` / `caveGenerator` / `mineshaftMesaGenerator` / `mineshaftGenerator` /
   `strongholdGenerator`.
6. `BuildingHighway.generate(..., blockArray)` - 4 148 **literal** indices.
7. `buildOnChunk` -> `MapGenCity.generate(..., chunk.blocks, chunk.data)` (still pre-slice).
8. `ravineGenerator`, then `replaceBlocksForBiome` (including the biome subclasses).
9. `chunk.loadFlatBlocks(blockArray, metadata)` (`:446`), then `chunk.generateSkylightMap()`.

Parallel 32 768-byte paths that share the same loader:

- `ChunkProviderHell.provideChunk:197` plus `generateTerrain:51` and `replaceBlocksForBiome:139`.
- `ChunkProviderClient.prepareChunk:37` (all-zero buffers plus the `0xFF` sentinel).
- `ChunkLoader:355` - the **legacy 128-tall save migration**, gated on
  `flatBlocks.length == 128 * 16 * 16` at `:340`, which then feeds *saved* light planes into
  `skyLightMap[section]` for sections 0-7. This one stays 128-high.

`Chunk` flat-buffer touch points: `loadFlatBlocks:206`, `exportFlatBlocks128:246`,
`exportFlatData128:264`, `importFlatBlocks128:291`, `getBlockID:516`, `getBlockMetadata:751`,
the wrapping constructor at `:162`, the fields at `:63-65`, `FLAT_SECTION_COUNT:55`, and the
class javadoc at `:40-45`.

`MapGenCity.markOrProcessChunk` uses `exportFlatBlocks128` / `exportFlatData128` /
`importFlatBlocks128` to stage in-world terrain edits before re-importing them.

## Index translation rules

Mechanical, stride-only. Nothing in this table touches a vertical domain.

| Old | New |
|---|---|
| `x << 11 \| z << 7 \| y` | `x << 12 \| z << 8 \| y` |
| `(x + (xSection << 3)) << 11 \| ... << 7 \| y` | `<< 12` / `<< 8` |
| `z << 7` | `z << 8` |
| `index += 128` (z stride) | `index += 256` |
| `chunkidx += 2048` (x stride) | `chunkidx += 4096` |
| `chunkHeight = 128` used as the flat z stride | `256` |
| `(k1 * 16 + k2) * 128 + l` | `... * 256 + l` |
| `new byte[32768]` | `new byte[65536]` |
| `(x << 4 \| z) << 7` (flat column base) | `(x << 4 \| z) << 8` |
| `BuildingHighway` literal indices (all multiples of 128) | multiplied by 2 |

Explicitly **unchanged**, to preserve generation output:

- `for (y = 127; y >= 0; --y)`, `y == 127`, `y >= 127 - ...`, `y <= 0 + ...`
- `y < 128` clips in `BlockArrayUtils`, `MapGenCaves`, `FeatureDynamicSchematic`
- `rand.nextInt(128)`, `lavaLevel = 64`, `seaLevel = 64`
- the 32-entry `ySection` density loops in `generateTerrain` / `justGenerateForHeight`
- `128 >> 4` for the legacy 128-tall loader

## Implementation steps

### 1. `Chunk.java` - storage and loader core

- `loadFlatBlocks` (`:206`): top-down exhaustive scan of all 16 sections for the first non-air
  one; materialize `0..topmost`; copy with `flatColumnBase = (x << 4 | z) << 8` and
  `subchunkColumnBase = (x << 4 | z) << 4`; `recomputeEmptyFlags()`; drop the flat buffers.
- Add a shared `private static boolean flatSectionHasContent(byte[] flat, int section)` that
  scans 16 columns x 16 local-y bytes, so the rule is stated once and reused by the loader and
  the importer.
- Add `loadFlatBlocks128(byte[], byte[])` for the `ChunkLoader` legacy branch. Under the new
  rule some of sections 0-7 may stay `null`, so the legacy light copy at `:371-381` and
  `:387-397` needs a `plane == null` guard, keeping `hasSkyPlanes` / `hasBlockPlanes` set so a
  null section stays a relight candidate exactly as on the fresh path.
- Widen and rename the city staging pair to `exportFlatBlocks()` / `exportFlatData()` /
  `importFlatBlocks()` at 65 536 bytes with the `<< 8` base, applying the same contiguous-prefix
  rule so a lower all-air section beneath a non-empty one is still materialized.
- Flat fallbacks `getBlockID:516` and `getBlockMetadata:751`: relax
  `y < (SECTION_HEIGHT >> 1)` to `y < SECTION_HEIGHT` and re-stride the index. Required, because
  `biome.generate(rand, 64, chunk)` (`:415`) and `featureProvider.getNearestFeatures` (`:399`)
  read the flat buffers before slicing.
- Refresh the "128-high" javadoc on the class, the `blocks` / `data` fields and the wrapping
  constructor.

### 2. Providers

- `ChunkProviderGenerate`: `new byte[65536]` x2 (`:358-359`); `generateTerrain:209` and
  `specialCarving:337` re-strided; the z stride `chunkHeight` becomes 256. `justGenerateForHeight`
  is untouched.
- `ChunkProviderSky.generateTerrain`: `chunkHeight` 128 -> 256 (`:30`), index at `:55`, stride at
  `:67`.
- `ChunkProviderHell`: `new byte[65536]` x2 (`:197-198`); `generateTerrain:51` and
  `replaceBlocksForBiome:139`.

### 3. Generators

- `MapGenUnderwater` (1 239 sites), `MapGenCity` (170 plus `raiseTerrain` / `flattenTerrain`
  `idx += 128` -> 256), `FurniturePieces` (112), `MapGenRavine` (2, including
  `(k1 * 16 + k2) * 128 + l` -> `* 256`), `MapGenCaves` (2, keeping the `y < 128` clip),
  `BuildingSchematic` (`z << 7` -> `<< 8`, x stride `2048` -> `4096`), `BuildingDynamic`,
  `CityBlockData`, `BlockArrayUtils:21,25`.
- Features: `FeatureDynamicSchematic` (`:188` index; the `y == 128` breaks at `:204` and `:234`
  are the ceiling a future high-altitude feature would raise), `FeatureSlimeBossLair` (6),
  `FeatureSinkHole`, `FeatureTest`, `FeatureHollowHill` (`index += 128` -> 256, keeping
  `nextInt(128)`), `FeatureSphereTest` via `BlockArrayUtils`.
- Biomes: `BiomeGenBase`, `BiomeGenGlacier`, `BiomeGenMesa:81`, `BiomeGenThickForest` - index only.
- `BuildingHighway`: multiply all 4 148 literal indices by 2. Each is `(col * 16) * 128` with
  `col = x * 16 + z`, so doubling is an exact coordinate-preserving rewrite. Verified
  mechanically: every matched literal is a multiple of 128.

### 4. Client

- `ChunkProviderClient.prepareChunk`: drop the two 32 768 buffers and the `loadFlatBlocks` call -
  an all-zero column has `topmost = -1` - and drop the now-dead `0xFF` sentinel fill (`:43-47`),
  which a `null` section already satisfies.
- `Chunk.setChunkData`: window-scoped contiguity after the pass-1 block scan.

### 5. Verification

- Exhaustive greps for leftovers: `<< *11`, `<< *7`, `32768`, `+= 128`, `\* 128`, `2048`. Every
  surviving hit must be an intentional vertical-domain use.
- Headless golden probe: generate chunks spanning ocean, city, underground and feature chunks;
  hash the 65 536-byte flat arrays *and* the final 16 subchunk planes plus the height maps; assert
  byte-identical output versus a pre-change baseline, and assert
  `sectionBlocks[s] != null <=> s <= topmost`.
- `javac` client and server, then `build.bat`, then `check_parity.py`.
- In-game: new world, confirm terrain matches the pre-change world at y 0-127, and confirm caves
  and overhangs are not lit through.
