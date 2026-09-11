# Extending World Height to 256 Blocks

> **Status:** Planned.
> **Target version:** InfHell 2 (Minecraft Beta 1.7.3 derivative).
> **Adapted from:** `InfdevProject/Main/docs/256_blocks_high.md` (a 20100420 Java 8 refactor).

---

## Overview

This codebase has the same 128-block ceiling as the reference project (y = 0 to 127).
Crucially, **the reference project's starting point was identical to ours**: a
single flat 32 768-byte `byte[]` per chunk, with the same packing
(`x << 11 | z << 7 | y`). The *sectioned* (16 × 16 × 16 subchunk) storage described
in the reference document is the **result** of applying that plan, not a
pre-existing structure. So the reference's flat-array → subchunk conversion is a
direct precedent, and this document targets the **same end-state**: lazy
16 × 16 × 16 subchunks backing a 256-tall (y = 0 to 255) buildable world.

Before the change, both projects use a single flat `byte[]` per chunk — here it is:

```
Chunk {
    byte[]        blocks;          // 16*16*128 = 32768 bytes, flat
    byte[]        data;            // 16*16*128 = 32768 bytes, ONE full byte per block
    NibbleArray   skylightMap;     // 16384 bytes (nibble-paired)
    NibbleArray   blocklightMap;   // 16384 bytes (nibble-paired)
    byte[]        heightMap;              // byte[256] (index z<<4|x)
    byte[]        landSurfaceHeightMap;   // byte[256]
    int           heightMapMinimum;
    List<Entity>[] entities;              // new List[8]  — one per 16-block slab

    // Per-column climate caches (added by Broad climate palette branch):
    // 2D per-chunk, index x<<4|z — independent of the vertical layout.
    float[]       temperatureCache;       // float[256], lazy, persisted as "Temperature"
    float[]       humidityCache;          // float[256], lazy, persisted as "Humidity"
}
```

This codebase also carries **per-column climate caches**: `Chunk.temperatureCache` /
`humidityCache` (`float[256]`, indexed by `x << 4 | z`), lazily seeded from the
`WorldChunkManager` ramp by `refreshCaches()`/`setClimateCache(...)` and read via
`Chunk.getTemperatureAt(x,z)`/`getHumidityAt(x,z)` (used by `WorldGenSurfaceMoss`
and the moss climate gate). They are **2D per-column data independent of the
vertical chunk layout**, so the subchunk conversion does not touch their indexing —
but they **must be preserved through the chunk save/load and the generator's chunk
construction** (they are persisted as `"Biomes"`, `"Temperature"`, `"Humidity"`
`byte[256]` arrays, and `provideChunk` seeds them alongside biome generation). See
**Save format** and **ChunkProviderGenerate** below.

This codebase also differs from the reference in *where* per-block light is
computed: light propagation is handled by **Starlight**
(`ca.spottedleaf.starlight.StarlightEngine`, two instances on `World` —
`blockLight` for emitted light, `skyLight` for skylight), not the reference's
vanilla `relightBlock` light pass. Today it reads and writes single flat
`NibbleArray`s per chunk with hardcoded 128-block bounds. The subchunk conversion
therefore includes adapting Starlight to per-subchunk light planes — see **Light
propagation: Starlight** under Architecture and the `StarlightEngine.java` entry
in **Files That Change**.

**Primary design (this document):** replace the four flat arrays with up-to-16
lazy **subchunks**, exactly as the reference did. Subchunks 0–7 (y 0–127) are
eager; subchunks 8–15 (y 128–255) allocate only on first write. A `null` subchunk
reads as implicit open air, fully lit. This keeps fresh-terrain memory at today's
footprint.

**Fork divergence — the flat arrays are NOT removed (gen-buffer retention).** The
reference assumed generation writes only into *local* scratch buffers. This
codebase does not: many gen-stage consumers mutate the *public* `chunk.blocks` /
`chunk.data` flat fields through the `Chunk` object itself — `MapGenCity.generate(..., chunk.blocks, chunk.data)`
(`ChunkProviderGenerate.buildOnChunk`), `BiomeGenBase.generate(..., chunk)`,
`MapGenUnderwater.setChunk(chunk)`, `FeatureProvider`, `BuildingHighway`,
`BlockArrayUtils`, and the city `Building*` subclasses. Rewriting all of them to
section arrays would be the wide, mechanical churn the reference never documented.
So `byte[] blocks` / `byte[] data` stay as the **flat 128-high generation buffers**
(same type, same `x << 11 | z << 7 | y` indexing), and the subchunk planes are
added next to them as parallel arrays (`sectionBlocks` / `sectionData`,
`skyLightMap` / `blockLightMap`, `isEmpty`, `subchunkCount`, and the
`SECTION_SIZE` / `SECTION_HEIGHT` / `SUBCHUNK_COUNT` constants). `provideChunk`
generates into the flat buffers as today, then **`loadFlatBlocks()` slices them
into subchunks 0–7 and releases them (`this.blocks = this.data = null`)**;
`setBlockID*` / `setBlockMetadata` / `getBlockID` / light accessors /
`getChunkData` / `setChunkData` are **sections-only** after that — no flat/section
sync is ever maintained. The only flat fallback is `getBlockID` reading
`this.blocks` when `this.blocks != null && y < 128`, which keeps the transient
**unsliced** `justGenerateForHeight` chunks (height-query only, never written)
working. Memory still lands at today's footprint: 8 eager subchunks × 12 288 B =
98 304 B once the flat buffers are freed.

A simpler-but-heavier **interim** (grow the flat arrays in place to 65536 bytes)
is documented in **Appendix A** — useful if a small diff is preferred over the
reference's memory footprint, and a stepping-stone to the subchunk model.

---

## Architecture

### Subchunk storage

Each chunk holds up to **16 subchunks** (16 × 16 × 16 cells = 4096 each). A
subchunk is:

```
byte[]        blocks      — 4096 bytes
NibbleArray   data        — 2048 bytes (block metadata, 4 bits per cell)
NibbleArray   skyLight    — 2048 bytes
NibbleArray   blockLight  — 2048 bytes
```

**One difference from the reference:** the reference uses a NibbleArray for block
metadata. This codebase stores metadata as a **full `byte[]`** (one byte per block,
the `"NewFormat"` path in `ChunkLoader`). To match the subchunk model cleanly, the
metadata subchunk plane becomes a `byte[4096]` (full byte per cell) rather than a
`NibbleArray[2048]`, i.e. 4096 bytes per subchunk. Adjust the totals accordingly:

| Subchunk plane | Size |
|----------------|------|
| `blocks`       | 4096 B |
| `data` (full byte) | 4096 B |
| `skyLight` (nibble) | 2048 B |
| `blockLight` (nibble) | 2048 B |
| **Total**      | **12 288 B** |

So this codebase's subchunk is 12 288 B (not the reference's 10 240 B, because of
the full-byte metadata). A fully-built column = 16 × 12 288 = 196 608 B. Today's
flat storage is 32 768 (blocks) + 32 768 (full-byte data) + 16 384 (blocklight) +
16 384 (skylight) = **98 304 B** — i.e. 8 eager subchunks × 12 288 B = 98 304 B is
**exactly today's footprint** (the reference's "8-eager == today" property holds
once the full-byte metadata is accounted for; the reference's 81 920 B figure
assumed nibble metadata). (If compacting `data` to a NibbleArray matters, it
reduces both — but that changes metadata storage throughout; see **Appendix B**.)

Subchunk index `s` covers world y = `s * 16` to `s * 16 + 15`.

| Subchunks | y range    | When allocated |
|-----------|------------|----------------|
| 0–7       | 0–127      | Always, on chunk load |
| 8–15      | 128–255    | Lazily, on first **write** above y=127 |

**Allocation is write-only.** A `null` subchunk reads as block 0 (air), metadata
0, skylight 15, blocklight 0 without allocating. Read-allocation is rejected for
the same reason as the reference: `WorldRenderer` scans all slabs per rebuild, so
allocating on read would materialize the entire top half on the first render pass.

The per-subchunk planes live in parallel arrays on `Chunk`:
`byte[16][] sectionBlocks`, `byte[16][] sectionData` (full-byte metadata —
`NibbleArray[16] data` if ever compacted — see Appendix B), and
`NibbleArray[16] skyLightMap / blockLightMap`, with `boolean[] isEmpty` and
`int subchunkCount` alongside. The flat `blocks` / `data` are kept only as
128-high generation buffers and released once sliced (see the divergence note
under Primary design above).

### Three independent vertical systems

As in the reference, the codebase has three independent "16-block vertical slice"
notions. After the change all three grow from 8 to 16:

| System              | Field / constant            | Before | After |
|---------------------|-----------------------------|--------|-------|
| Block + light storage | `Chunk.subchunks[]`        | (flat) | 16    |
| Entity buckets        | `Chunk.entities[]` (`new List[8]`) | 8 | **16** |
| Render layers        | `RenderGlobal.renderChunksTall` | 8   | **16** |

They are independent structures — no code-level coupling (as in the reference).

### Indexing

Subchunk-local cell index (blocks and full-byte `data`):
```
index = (x << 8) | (z << 4) | yLocal
```
(`NibbleArray` planes use the same 12-bit index internally.)

Chunk-level subchunk lookup:
```
subchunkIdx = y >> 4
yLocal      = y & 15
```

Entity segment index (`entity.posY / 16`) already yields `[0, 15]` for y ∈ [0, 255]
with no change; only the `new List[8]` allocation becomes `new List[16]`.

There is no `TILE_Y_SHIFT` constant in this codebase (the entity/block keying is
already per-chunk `List[16]`), so nothing to adjust there.

### Light propagation: Starlight

This codebase **does not use vanilla light propagation**. Per-block light is
handled by **Starlight** (`ca.spottedleaf.starlight.StarlightEngine`) — two
instances on `World` (`blockLight`, emitted light; `skyLight`, skylight,
`World.java:131–132`). Entry points:

- `Chunk.initLightingForRealNotJustHeightmap` → `initBlockLight`/`initSkylight`
  (`Chunk.java:959`), called on chunk load/generate by `ChunkProvider.java:70`,
  `ChunkProviderLoadOrGenerate.java:82`, and `SchematicsBigShip.java:127`.
- `Chunk.updateLight` → `checkBlockEmittance`/`checkSkyEmittance`
  (`Chunk.java:967`), called from `setBlockIDWithMetadata` /
  `setBlockIDAndMetadataColumn` on every block write where light may change.

`Chunk.relightBlock` (the reference's light pass) is in this codebase only a
**height-map helper** — it never touches the light planes.

Our `StarlightEngine` port is a **flat-128 adaptation**. The reference Anvil
`StarlightEngine`
(`InfdevProject/Main/minecraft_r1.2.5/src/minecraft/ca/spottedleaf/starlight/StarlightEngine.java`)
is already **subchunk-native** and is the shape reference for the adaptation:

| Our flat-128 port (today) | Subchunk adaptation (adopt reference shape) |
|---|---|
| `getNibbleFromCache(chunkX, chunkZ)` — one flat nibble per chunk | per-subchunk nibble (reference `getNibbleFromCache(x, y>>4, z)`), `null` for absent/null subchunk |
| `& 127`, `> 127` in `getBlockState` / `getLightLevel` / `setLightLevel` | section-local `worldY & 15`; world-bounds guard `>= SECTION_HEIGHT` |
| propagation bounds `offY > 127` (increase/`decrease`) | `offY >= SECTION_HEIGHT` |
| `checkSkyEmittance` strike-15 loop over the flat nibble (`currY & 127`) | break at a null nibble — stops at the subchunk boundary (reference ~509–513) |
| `initSkylight` starts at y = 127 | start at `SECTION_HEIGHT` (256); descends through implicit-air null subchunks, first *writes* happen only inside a materialized subchunk |
| `initBlockLight` scans `y = 0..127` (Nether) | `0..SECTION_HEIGHT-1`, nibble fetched per-`y>>4`, null-skipped |
| `propagateNeighbourLevels` section loop `(127 >> 4)` → 0 | `(SECTION_HEIGHT >> 4) - 1` → 0 |

Three invariants to preserve:

1. **Light writes must not allocate subchunks.** `getLightLevel` on a null subchunk
   returns the implicit value (skylight 15 / blocklight 0); `setLightLevel` on a
   null subchunk is a **no-op** (implicit value is already correct). This is
   exactly the reference's null-nibble handling, and it's what keeps
   `initSkylight` from materializing the top half of a fresh chunk.
2. **The queue coordinate encoding needs no change.** Y is a 16-bit field
   (`x | (z << 6) | (y << 12)`, masked to (6+6+16) = 28 bits, `encodeOffsetY = 32`),
   so y ∈ [−32, 65535] already fits; no shift/bit-layout edits.
3. **Direct flat-plane consumers become subchunk-aware:** `ChunkProviderClient`
   (`Arrays.fill(chunk.skylightMap.data, (byte)-1)`, the 0xFF "uninitialised
   skylight" sentinel) and `Chunk.setChunkData`/`getChunkData` (the packet path,
   already being rewritten). `ChunkLoader`'s SkyLight/BlockLight NBT is folded
   into the subchunk save format below.

**Accepted trade-off (same as the reference):** a `null` upper subchunk reads as
fully sky-lit even if an opaque roof was built *above* it inside a materialized
subchunk. Shadowing appears the moment a block is placed inside that subchunk —
Starlight's decrease pass then runs on that `setBlockID`. This keeps the top half
allocation-free and is inherent to "null = implicit fully lit".

Client and server `StarlightEngine.java` must stay in sync except for the
**existing intentional diff**: client `postLightUpdate` calls
`world.markBlockNeedsUpdate(...)`, server has it commented out ("not needed server
side") — keep that exact shape. Both trees also carry an unused `isClientSide`
field (`false`) that does not change.

---

## Performance

### Random block ticks

**Goal:** a crop/sapling/grass cell receives `updateTick` at the same average rate
as today.

Adapt the reference's subchunk-iterating loop to the flat loop currently in
`World.updateBlocksAndPlayCaveSounds` (lines ~2267–2278). Instead of probing the
whole flat chunk with a global budget, iterate each **materialized** subchunk and
perform 10 LCG-local probes per subchunk:
```
local = lcg;  x = (local >> 2) & 15;
              z = (local >> 6) & 15;
              yLocal = (local >> 10) & 15;
worldY = subchunkIdx * 16 + yLocal;
```

|                    | Today        | Fresh terrain (8 subchunks) | Fully built (16 subchunks) |
|--------------------|--------------|-----------------------------|----------------------------|
| Cells per subchunk | —            | 4096                        | 4096                       |
| Probes per chunk   | 80 (flat)    | 10 × 8 = 80                 | 10 × 16 = 160              |
| Pick probability   | 80/32768 = 0.244% | 10/4096 = 0.244%       | 10/4096 = 0.244%           |
| Pick rate          | **identical** | **identical**             | **identical**              |

A fresh chunk still performs exactly 80 probes (the historical budget). Building
upward materializes more subchunks and the probe count scales with them, keeping
the per-cell tick rate constant at any height. No global budget juggling.

### Memory

|                          | Today (flat 32768) | 8 eager subchunks | 16 subchunks (fully built) |
|--------------------------|--------------------|--------------------|---------------------------|
| Blocks + metadata + lights / chunk | 32768 + 32768 + 16384 + 16384 = **98 304 B** | 8 × 12 288 = **98 304 B** | 16 × 12 288 = **196 608 B** |
| Entities / chunk         | 8 × List overhead  | 16 × List overhead | 16 × List overhead |
| 17×17 loaded area        | ~55 MB             | ~55 MB             | ~111 MB |

**Result:** a fresh terrain chunk materializes exactly 8 subchunks at **today's
exact footprint** — 8 × 12 288 = 98 304 B, identical to the current flat layout
(which is 32 768 blocks + 32 768 full-byte metadata + 16 384 + 16 384 lights).
Building upward or loading a saved top half adds 12 288 B per subchunk used.
Memory is never worse than today for equivalent coverage.

> **Note:** the reference quoted 81 920 B for a flat/8-eager chunk because it used
> nibble metadata (10 240 B/subchunk). This codebase's full-byte metadata makes
> both today's flat chunk and an 8-eager subchunk column 98 304 B; the fully-built
> 16-subchunk figure is 196 608 B (the reference quoted 163 840 B). Compact `data`
> to a `NibbleArray` (Appendix B) to hit the reference's exact numbers — at the cost
> of a wider metadata conversion change.

### Rendering

`renderChunksTall = 8` → `16`, exactly as the reference. Each chunk column now has
16 `WorldRenderer` instances instead of 8, each owning 3 GL call lists.

Mitigations (from the reference):
- **Empty-subchunk render shortcut (new).** Today `WorldRenderer.updateRenderer`
  lines ~194–306 already marks both render passes `skipRenderPass = true`, then
  scans all 4096 block cells of the section; only if a block emits geometry does it
  open a display list. So an all-air slab currently compiles to an *empty display
  list* — cheap to call, but the 4096-cell scan still runs on every rebuild. With
  the subchunk model this becomes a real shortcut: add an `isEmpty` flag (or a
  `blockCount`) on each subchunk — set true by `allocateSubchunk`, cleared on the
  first placed/written block, set again when the last block is removed — and before
  the block loop in `updateRenderer`, `if (subchunk == null || subchunk.isEmpty) {
  skipRenderPass[0] = skipRenderPass[1] = true; break; }`. This skips the scan
  entirely for the implicit-air / all-air upper subchunks, keeping the 16-layer
  top half cheap even before the display-list call. (A `null` subchunk is trivially
  empty.) This is a per-subchunk flag win that the reference's separate-arrays v1
  did not implement, and it is the natural candidate to add alongside
  `getSubchunkCount()` in `Chunk` (see **Files That Change → `Chunk`**).
- Frustum culling discards the upper layers of distant chunks when looking
  horizontally.
- `RenderGlobal.markBlocksForUpdate` must compute `chunkY = blockY >> 4` (not
  `blockY % renderChunksTall`) and clamp into `[0, renderChunksTall - 1]` so
  neighbour-of-y=0 edits (y = −1) and edits above the top layer cannot index out of
  bounds or wrap to the bottom (see Decision 5).

---

## Files That Change

All paths are in `src/minecraft` (client) and `src/minecraft_server` (server); the
two trees have byte-identical versions of the shared files, so **apply each change
to both trees and run the parity checker** (`check_parity.py`). Client-only files
(renderer) have no server copy.

### Shared (`game.world...`) — both trees

#### `chunk/NibbleArray.java`

- Index formula: `x << 11 | z << 7 | y` → subchunk-local `x << 8 | z << 4 | yLocal`
  (`cellCount = 4096` per subchunk). The nibble arrays (`skyLightMap`/`blockLightMap`)
  are per-subchunk.
- The full-byte `data` is *not* a NibbleArray here — see `Chunk` below.

#### `chunk/Chunk.java`

**Constants:**
- `SECTION_SIZE` = 16 (public).
- `SECTION_HEIGHT` = 256 (public; world and generators share it).
- `SUBCHUNK_COUNT` = `SECTION_HEIGHT / SECTION_SIZE` = 16.

**Fields (keep the flat gen buffers, add the section arrays):** the flat
`byte[] blocks` / `byte[] data` stay as 128-high generation buffers (gen-stage
consumers write them directly — see the Fork-divergence note); add the
per-subchunk storage:
- `byte[16][] sectionBlocks` / `byte[16][] sectionData` (full-byte metadata,
  **4096 B** per subchunk — preserves the `"NewFormat"` layout) +
  `int subchunkCount`.
- `NibbleArray skylightMap` → `NibbleArray[16] skyLightMap` (2048 B per subchunk);
  `NibbleArray blocklightMap` → `NibbleArray[16] blockLightMap` (2048 B per subchunk).
- **`boolean[] isEmpty` (new):** per-subchunk empty flag driving the render
  scan-skip (see Rendering above); `true` for `null` subchunks, cleared on first
  write, re-set when the last block is removed.
- **`loadFlatBlocks()` (new):** slices the flat `blocks`/`data` into sections
  0–7, then **nulls the flat fields** (`this.blocks = this.data = null`).
  Chunks created via `new Chunk(world, blocks, metadata, x, z)` that are *not*
  sliced (`justGenerateForHeight`) keep their flat buffers for height queries.

**Methods:**
- `getBlockID(x,y,z)`: read-only; a `null` subchunk returns 0 (air). Never allocates.
- `setBlockID*` / `setBlockMetadata` / `setLightValue`: allocate the subchunk on
  first write (`allocateSubchunk`, which also pre-fills a fully-lit skylight plane),
  then correct the height map (`relightBlock`) and queue the **Starlight** update
  (`updateLight` → `checkBlockEmittance` / `checkSkyEmittance`).
- `getBlockMetadata` / `getSavedLightValue` / `getBlockLightValue`: read-only;
  `null` subchunks return metadata 0 / skylight 15 / blocklight 0.
- `relightBlock`: **height-map-only** helper in this codebase — it never touches
  the light planes (per-block light is Starlight). Unchanged logic; update `<<11|<<7`
  indexing to per-subchunk.
- `generateHeightMap()` / `generateLandSurfaceHeightMap()` / `generateSkylightMap()`:
  keep the column-walk over the whole 0–255 span; descends through the (implicit
  air-only) empty top half because air has opacity 0. Height-map seeds
  `heightMapMinimum`/`height` → `SECTION_HEIGHT - 1` (= 255).
- **`isEmpty` flag (render shortcut):** add a `boolean[] isEmpty` (or
  `int[] blockCount`) parallel to `subchunks` — subchunk i is empty when
  `subchunks[i] == null` or its flag is set. Maintain it on `setBlockID`/clear:
  clear on first write into a fresh subchunk, set again when the last block is
  removed. Drives the `WorldRenderer` scan-skip (see **Rendering** mitigation and
  `render/WorldRenderer.java`).
- `addEntity(Entity)` / `removeEntityAtIndex(Entity,int)`: unchanged —
  `entity.posY / 16` already yields `[0, 15]`; only `new List[8]` → `new List[16]`.
- `getSubchunkCount()`: number of materialized subchunks counting from the bottom
  (drives the random-tick loop, Decision 6).
- `setChunkData` / `getChunkData` (byte-buffer packet path): rewrite to fan out
  across subchunk planes instead of the flat `<<11|<<7` indexing. New-format
  packet windows of y up to 256 must map onto subchunks 0–15.

#### `ca.spottedleaf.starlight/StarlightEngine.java` — shared, both trees

Per-block light is computed here (not in vanilla `relightBlock`). The port is
flat-128; apply the subchunk adaptation described in **Architecture → Light
propagation: Starlight** (use the Anvil
`InfdevProject/Main/minecraft_r1.2.5/.../StarlightEngine.java` as the shape
reference):
- `getNibbleFromCache(chunkX, chunkZ)` → `getNibbleFromCache(chunkX, chunkY, chunkZ)`
  returning `subchunk.skylightMap` / `subchunk.blocklightMap` (null for a null/absent
  subchunk) instead of the single flat per-chunk nibble.
- `getBlockState` (170/174): `> 127` and `& 127` → `worldY >= SECTION_HEIGHT` guard
  and full `worldY` passed to `chunk.getBlockID` (Chunk resolves the subchunk).
- `getLightLevel` (181/186) / `setLightLevel` (199): `& 127` → section-local
  `& 15`; world-above guard `> 127` → `>= SECTION_HEIGHT`; null nibble keeps the
  implicit value and setLightLevel stays a no-op for null nibbles (no allocation).
- `performLightIncrease` (299, 305/314) / `performLightDecrease` (360, 367/402):
  `offY > 127` → `offY >= SECTION_HEIGHT`; `& 127` → `& 15`.
- `checkSkyEmittance` (483/494): per-subchunk nibble lookup in the strike-15 loop,
  breaking where the subchunk is null (implicit fully-lit below the boundary).
- `initSkylight` (580): `tryPropagateSkylight(..., 127, ...)` → startY =
  `SECTION_HEIGHT` (256) — descends through nil subchunks and only writes into
  materialized ones.
- `initBlockLight` (601): `y <= 127` → `y < SECTION_HEIGHT`; nibble fetched per
  `y >> 4` with a null skip (Nether only; upper subchunks stay null → implicit 0).
- `propagateNeighbourLevels` (634): `(127 >> 4)` → `(SECTION_HEIGHT >> 4) - 1` = 15;
  `& 127` (684) → `& 15` (per-section nibble).
- Queue encoding: **unchanged** (Y is a 16-bit field supporting y ∈ [−32, 65535]).
- `isClientSide`: currently `false` in both trees; no change.

All three invariants from the Architecture section apply. Apply matching edits to
both trees, preserving the existing intentional `postLightUpdate` divergence
(client calls `world.markBlockNeedsUpdate`, server comments it out); the parity
check should show no *new* substantive diffs here.

#### `chunk/loader/ChunkLoader.java` (NBT serialization)

The per-chunk NBT is written here (`storeChunkInCompound`) and read here
(`loadChunkIntoWorldFromCompound`); it is persisted to **region files** (McRegion
`.mcr` in `region/`) via `McRegionChunkLoader` → `RegionFileCache`/`RegionFile`,
which store the NBT byte arrays verbatim and need **no change**.

Add an explicit height marker. The current format has no height tag — height is
implied by `"Blocks"` array length (32768 B = 128 tall). The reference uses a
`Height` tag; adopt the same for migration, plus a `SubchunkMask` for the sparse
set. See **Save format** below.

#### `World.java`

- All `y >= 128` / `y < 128` / `& 127` guards → `y >= SECTION_HEIGHT`,
  `y < SECTION_HEIGHT`, `& (SECTION_HEIGHT - 1)`. Sites (identical in client &
  server): `getBlockId` (476), `blockExists` (565), `checkChunksExist` (526),
  `isBlockOpaqueCube` (576), `getBlockMetadata` (608), `isBlockNormalCube` (637),
  light queries (752/801/816/902/946), `setChunkData`/`getChunkData` clamps
  (2629/2679).
- `updateBlocksAndPlayCaveSounds`: iterate subchunks with 10 probes each (Decision
  6) instead of the flat 80-probe loop with `& 127`.

#### `ChunkCache.java`

- `getBlockId` (63) / `getBlockMetadata` (121) / `getLightValueExt` (199): `128` →
  `SECTION_HEIGHT`. Subchunk lookups under the hood; no caller-visible API change.

#### `biome/BiomeGenBase.java`

- **Unchanged** generation height: the surface walk and ore-vein heights stay 128.
  `replaceBlocksForBiome` writes into the flat 128-high *generation* buffer
  (index `x<<11|z<<7|y`); raising it would consume extra RNG draws and change the
  RNG order — load-bearing for world generation (same concern as the reference).
  Terrain is produced only in the bottom 128 layers; the top half is player-writable.
- `waterFallMaxHeight = 128` (195) — leave.

#### `terrain/ChunkProviderGenerate.java` (and Hell/Sky providers)

- `provideChunk` keeps its flat 32768-byte (`new byte[32768]`) generation buffer.
  The ordering pitfall from the reference applies: build the chunk empty (to hold
  biomes), run `generateTerrain`/`replaceBlocks` into the flat buffer
  (column-major `x<<11|z<<7|y`), and **only then** slice the buffer into subchunks
  0–7 via `chunk.loadFlatBlocks(blocks)` (or equivalent). The upper 8 subchunks
  stay `null` (air).
- **Preserve the climate caches:** `provideChunk` seeds `chunk.biomeGenCache` and
  (via `setClimateCache`) `temperatureCache`/`humidityCache` from the same
  `loadBlockGeneratorData` ramp right after the biome-cache assignment. The
  subchunk refactor must keep this seeding intact and the caches non-null so the
  `temperatureCache != null && humidityCache != null` save-path in `ChunkLoader`
  continues to persist them.
- Keep `chunkHeight = 128`, `rand.nextInt(128)` height rolls, `y > 0 && y < 128`
  checks unchanged (128-high gen; do not raise).
- `ChunkProviderHell` / `ChunkProviderSky` keep their own 32768 buffers and
  128-rolls; verify no regression (see Manual Verification).

#### `terrain/generate/* — WorldGenerator height guards (gameplay-callable)`

World **generation** stays 128-tall: `provideChunk`, `populate`,
`replaceBlocksForBiome`, `waterFallMaxHeight = 128`, the `rand.nextInt(128)`
height rolls, and the `y > 0 && y < 128` guards keep their 128 bounds — terrain
is produced only in the bottom 128 layers; the top half is player-writable only.
Random Y coordinates drawn at `populate` time therefore stay in `[0, 128)`;
**do not lift them.**

But several `WorldGenerator` subclasses are **also invoked during gameplay**, and
the world is now 256 tall. Sapling / bonemeal growth calls the tree generators
from `BlockSapling.updateTick` (`WorldGenTrees`, `WorldGenBigTree`,
`WorldGenForest`, `WorldGenMushroom`, plus mod variants: cypress, willow, bo3,
amazon, streetlight, …), and those can run **above y = 128** (e.g. a sapling
planted on a platform at y = 200). Audit every gameplay-callable generator for
hardcoded 128 ceilings — `127` / `128` bounds, `> 128` / `& 127` Y-masks, height
clamps, and random Y rolls used for placement / trunk / leaf extents — and lift
them to `Chunk.SECTION_HEIGHT` so growth works anywhere in `[0, 255]`.
Generators used only in the gen/populate stage (ores, caves, ravines,
mineshafts, city, moss, …) keep 128. Growth already flows through
`world.setBlock*` → section-aware `Chunk` accessors, so only the generators'
**internal Y ceilings** need changing.

#### `world/SpawnerAnimals.java`

- (optional behaviour change) `MAX_SPAWN_HEIGHT` 128 → `SECTION_HEIGHT`, and the
  `Math.min(128, by)` (524) / `surfaceY < 128` checks (616, 620) → `SECTION_HEIGHT`,
  so mobs can spawn on player-built high platforms. Flag as a behaviour change.

### Client-only

#### `render/RenderGlobal.java`

- `renderChunksTall = 8` → `16` (line ~257); update the comment at line ~183.
- `markBlocksForUpdate` (lines ~1254–1290): compute `chunkY = blockY >> 4` (was
  `blockY % renderChunksTall`), clamp min/max Y into `[0, renderChunksTall - 1]`.
  This prevents y ≥ 128 from wrapping to the bottom renderer.

#### `render/WorldRenderer.java`

- Uses an internal `renderChunkBlocks` buffer (line ~240) with `(y & 127)`; per
  reference the slab is 16 tall so this should read from the correct subchunk of
  the chunk instead. Confirm the slab loop (`y = posY..posY+16`) pulls from
  `chunk.subchunks[y>>4]` and drop the flat `&127` mask.
- **Add the empty-subchunk scan-skip:** before the block loop in `updateRenderer`
  (today line ~223, the `renderChunkBlocks == null` check), short-circuit on
  `chunk.getSubchunk(y>>4) == null || isEmpty(y>>4)` → set both
  `skipRenderPass[*] = true` and break out of the pass loop. This avoids the
  4096-cell scan for implicit-air / all-air upper subchunks.

#### `client/ChunkProviderClient.java`

- `prepareChunk` (`ChunkProviderClient.java:39`) does
  `Arrays.fill(chunk.skylightMap.data, (byte) -1)` — the 0xFF "uninitialised
  skylight" sentinel that `ChunkLoader`'s `isValid()` recognizes. With subchunk
  light planes this must fan out to each of the 16 subchunk `skylightMap.data`
  arrays; a null subchunk is implicitly fully-lit so untouched upper subchunks
  need no fill.

### Server-only

#### `server/EntityPlayerMP.java`

- Chunk send to a player (lines ~256–258) hardcodes `ySize = 128` in
  `Packet51MapChunk` and the tile-entity Y range `128`. Change both to `SECTION_HEIGHT`
  (256). The wire format already supports this: `Packet51MapChunk.yPosition` is a
  `short`, `ySize` is a `read()+1` byte (up to 256). `World.setChunkData` /
  `getChunkData` clamps also change (see `World.java` above).

### Net/wire

No structural change — the packet encodings already tolerate y ∈ [0, 255]
(`Packet51` y short + ySize up to 256, `Packet52` y `& 255`, `Packet14/15/53` y as
byte 0–255). Update stale "0-127" comments if desired.

---

## Save format

A `Height` tag (as the reference uses) distinguishes old 128 saves from new 256
saves, and a `SubchunkMask` encodes which subchunks are present.

### New format (per chunk)

```
Level {
    xPos, zPos, LastUpdate,
    Height: Int = 256,              -- missing → legacy 128
    SubchunkMask: Short,             -- bit i set → subchunk i present (0x00FF for 8-eager)
    SubchunkBlocks: List[ByteArray(4096)],
    SubchunkData: List[ByteArray(4096)],       -- full-byte metadata (current NewFormat layout)
    SubchunkSkyLight: List[ByteArray(2048)],
    SubchunkBlockLight: List[ByteArray(2048)],
    HeightMap: ByteArray(256), Biomes: ByteArray(256),
    Temperature: ByteArray(256), Humidity: ByteArray(256),   -- per-column climate caches
    TerrainPopulated, Entities, TileEntities
}
```

`SubchunkMask` encodes which subchunks are present (future-proofed for any sparse
subchunk). The five subchunk lists are parallel and **mask-ordered**: each holds
exactly one element per set mask bit in ascending subchunk order (a fresh terrain
chunk has 8 entries, subchunks 0–7). `NBTTagList` cannot hold `null` entries, so
the mask carries the "present" set and the lists omit absent subchunks.

> **Metadata note:** `SubchunkData` keeps the full-byte-per-cell format this
> codebase already uses (the `"NewFormat"` path), so `SubchunkData[i]` is a
> `ByteArray(4096)`. If Appendix B (nibble-compact `data`) is adopted, it becomes
> `ByteArray(2048)` per subchunk instead.

### Legacy migration

`loadChunkIntoWorldFromCompound` detects the format by the `Height` tag:
`getInteger("Height")` returns **0** when absent and 128 for an older save —
either maps to the legacy flat path; `256` selects the subchunk lists. The legacy
path:

1. Read flat `Blocks` (32768 B), `Data` (32768 B full-byte, or old nibble→byte via
   the existing `"NewFormat"` branch), `SkyLight` (16384 B), `BlockLight` (16384 B).
2. Split each into subchunks 0–7: each subchunk gets its 4096 / 2048 B slice.
3. Upper subchunks 8–15 are `null` (air, fully lit).
4. If the height map or any skylight plane is missing (the existing `isValid()` /
   `hasSkyPlanes` path), re-run `Chunk.initLightingForRealNotJustHeightmap` (→
   Starlight `initBlockLight`/`initSkylight`) instead of any vanilla light fill —
   `initSkylight` starts at y = 256, descends through the null subchunks, and only
   writes into the materialized 0–7, so no top-half allocation occurs during
   migration.

Subsequent saves write the new format. `Height` becomes permanent on first
re-save, upgrading a world in place.

---

## Implementation Order

1. **`NibbleArray`** — index formula update to subchunk-local (`x<<8|z<<4|yLocal`).
2. **`Chunk`** — add subchunk storage (`byte[16][] sectionBlocks`,
   `byte[16][] sectionData`, `NibbleArray[16] skyLightMap/blockLightMap`,
   `boolean[] isEmpty`, `int subchunkCount`), `getBlockID`/`setBlockID*` with
   lazy allocation, `loadFlatBlocks()` slice-and-release, `isEmpty` flag
   maintenance, height maps, `getSubchunkCount`, entity buckets `List[16]`,
   `setChunkData`/`getChunkData` fan-out. (Flat `blocks`/`data` retained as gen
   buffers and nulled by `loadFlatBlocks`.)
3. **`ChunkProviderGenerate`** (+ Hell/Sky) — generate into flat buffer, then slice
   into 8 eager subchunks; upper 8 `null`. (Apply the reference's ordering:
   generate *before* slicing.)
4. **`StarlightEngine`** (both trees) — subchunk-native accessors: per-subchunk
   nibbles via `getNibbleFromCache(x, y>>4, z)`, `& 15` local indices,
   `> 127`/`& 127` → `>= SECTION_HEIGHT` bounds (all sites listed under Files That
   Change), `initSkylight` start y = 256, section loop `(SECTION_HEIGHT>>4)-1 → 0`,
   and **no light allocation** on null subchunks (implicit skylight 15 / block 0).
   Shape reference: `InfdevProject/Main/minecraft_r1.2.5/.../StarlightEngine.java`.
5. **`World`** — guards to `SECTION_HEIGHT`; random-tick loop over subchunks.
6. **`ChunkCache`**, **`SpawnerAnimals`**, **`BiomeGenBase`** — constant references.
7. **Gameplay-callable `WorldGenerator` audit** — lift 128-height guards inside
   tree/sapling/bonemeal generators (and any generator reachable from a gameplay
   `updateTick`/event, e.g. `BlockSapling` → `WorldGenTrees`/`WorldGenBigTree`/
   `WorldGenForest`/`WorldGenMushroom` + mod variants) to `SECTION_HEIGHT` so
   growth works above y = 127. Gen-stage-only generators keep 128 (see
   Files That Change).
8. **`ChunkLoader`** — new save format (Height tag + SubchunkMask + lists) and
   legacy migration (light regeneration goes through Starlight — see Save format).
9. **`RenderGlobal`** + **`WorldRenderer`** + **`ChunkProviderClient`** —
   `renderChunksTall = 16`, `chunkY = y >> 4`, per-subchunk reads, empty-subchunk
   scan-skip, skylight-sentinel fan-out (client only).
10. **`EntityPlayerMP`** — server chunk delivery ySize = 256.
11. **Compile both trees** (`build.bat`) and run **`check_parity.py`** — confirm only
    the intended files differ and shared files are byte-identical client/server
    (including both `StarlightEngine.java` copies).

---

## Manual Verification

1. **New world:** place a block at y = 200; confirm it renders in-world and in the
   hand/inventory, and persists across save/reload.
2. **Legacy save:** load an existing 128-high save; fly to y = 140 and confirm it's
   air; verify it is re-saved (upgraded) and still loads. Reload and confirm the
   top half remains air and the bottom is intact.
3. **Tick rate:** plant a sapling and crop; compare growth rate with the 128-height
   baseline (should be identical thanks to the per-subchunk 10-probe budget).
4. **Build up:** stack blocks from y = 127 to y = 200; confirm the render slider
   rebuilds both slabs and `markBlocksForUpdate` invalidates the right renderer (no
   wrap-to-bottom corruption).
5. **Cross-dimension:** confirm Nether (`ChunkProviderHell`) and Sky
   (`ChunkProviderSky`) still generate — they keep their own 128-high buffers; no
   regression from the `Chunk` storage change.
6. **Networking (MP):** client joining a 256-height server sees blocks above
   y = 127 (Packet51 ySize 256 path); multi-block changes at high Y (Packet52)
   apply; block digging/placing above y = 127 round-trips.
7. **Climate caches preserved:** after the chunk storage rework, confirm moss
   (`BlockSurfaceMoss`/`WorldGenSurfaceMoss`) still spreads/seeds per climate —
   i.e. `Chunk.temperatureCache`/`humidityCache` are non-null and persisted, and
   `World.getTemperatureAt`/`getHumidityAt` still return cached values on a loaded
   chunk. Save/reload a climate-gated chunk and verify the `"Temperature"`/
   `"Humidity"` arrays survive.
8. **Lighting (Starlight) across the 128 boundary:**
   - Place a torch at y = 127 on a legacy-loaded area → light propagates up into
     y = 128+ (blocklight path now reaches section 8).
   - Place an opaque block at y = 140 (first block above 128) → skylight below it
     in its subchunk drops to 0 immediately (the `setBlockID` → `updateLight` →
     `checkSkyEmittance` decrease pass); placing a block at y = 130 in a still-nil
     subchunk correctly shades after the write (deferred implicit-light discipline).
   - Nether (`ChunkProviderHell`): lava/glowstone emit; `initBlockLight` scans
     up to 255 with null-skip and produces correct cave darkness.
   - New chunk in a 256 world: `initSkylight` from y = 256 leaves upper subchunks
     `null` (verify memory stays at the 8-eager footprint) while the surface is
     fully lit, and breaks correctly at the first opaque surface block.
9. **Light persistence on save/reload:** place light sources and a shaded structure
   straddling y = 128, save, reload, and confirm light values survive (subchunk sky/
   block-light planes written and read back) and no full re-light is forced.
10. **Gameplay growth above y = 127:** build a platform at y = 200, plant a
    sapling and bone-meal it; a tree grows fully above y = 128, is lit and
    rendered, and persists across save/reload. Repeat for a mushroom and a
    mod-native tree (cypress/willow/bo3) if their saplings exist.

---

## Intentionally Out of Scope

- **World generation above y = 127:** terrain stays in the bottom 128 layers; the
  top half is player-writable only, until a future change adds upper-terrain
  generation (as in the reference).
- **Server-free client dependency constraint:** unchanged — `game.world` never
  references `net.minecraft.client.render`; `renderChunksTall` lives in client
  `RenderGlobal` only.
- **Renderer/entity-bucket allocation skip for empty subchunks:** the *render
  scan-skip* via the `isEmpty` flag is in scope (see Rendering above), but going
  further — skipping the `WorldRenderer` object / entity-bucket allocation entirely
  for all-air subchunks — is a future win on top of lazy allocation, as in the
  reference.
- **A dedicated `SubChunkStorage` class:** per the reference's appendix, do it in
  v2, not v1. v1 keeps three parallel per-subchunk arrays inside `Chunk` to
  minimise churn; extraction to a `SubChunkStorage` is a mechanical transformation
  once the model is proven.

---

## Appendix A: Interim — grow the flat arrays to 65536 (no subchunks)

If a small, reviewable diff matters more than the reference's memory footprint,
grow the flat arrays in place:

- `Chunk.blocks`/`data` → `byte[65536]`; `skyLightMap`/`blockLightMap` → 32768 B.
- Index invariant `x << 11 | z << 7 | y` → `x << 12 | z << 8 | y` (in `Chunk`,
  `NibbleArray`, `WorldRenderer:240`; *not* in the 128-high generation buffers).
- `World` guards → `SECTION_HEIGHT`; random-tick `&127`→`&255` **and** double
  `blocksToTickPerFrame` 80 → 160 so the per-cell rate stays 0.244%.
- **`StarlightEngine` (flat mode):** light planes stay single flat `NibbleArray`s
  (grown to 32768 B); only the height bounds lift — `> 127` → `>= SECTION_HEIGHT`
  and `& 127` → `& 255` in `getBlockState`/`getLightLevel`/`setLightLevel`,
  the increase/decrease propagation guards, `checkSkyEmittance`, `initSkylight`
  (start y = 256), `initBlockLight` (0..255), `propagateNeighbourLevels`
  (`(SECTION_HEIGHT>>4)-1` → 0 sections). No per-subchunk fan-out and no queue
  changes (Y is already a 16-bit field). This is the same bound-lift the subchunk
  plan needs, minus the section-local `& 15` work.
- Entity buckets `List[8]` → `List[16]`; `renderChunksTall` 8 → 16 with the
  `markBlocksForUpdate` `y>>4` fix.
- Generator copies its 128-high flat buffer into the bottom of the 65536 arrays
  instead of aliasing it.
- Save/load: branch on `Blocks.length` (32768 legacy vs 65536 new); materialize
  legacy chunks to 65536 on load so re-save upgrades. Region-file layer unchanged.
- `EntityPlayerMP` chunk send ySize → 256.

**Memory (interim):** 65536 + 65536 + 32768 + 32768 = **196 608 B / chunk** (2.0×
today's 98 304 B) — fresh terrain always pays for the materialized empty top half.

The 65536 flat layout's 16-block slice points are the mechanical gateway to the
subchunk model (the primary plan), so the interim does not throw away work.

---

## Appendix B: Should metadata stay a full byte, or become a NibbleArray?

This codebase already stores block metadata as a **full byte per block**
(`byte[] data`, the `"NewFormat"` save layout), unlike vanilla's 4-bit NibbleArray.
This affects the subchunk size and memory:

| Metadata layout | Subchunk `data` plane | Subchunk total | 8 eager | 16 built |
|-----------------|------------------------|----------------|---------|----------|
| Full byte (current) | `byte[4096]` | **12 288 B** | 98 304 B | 196 608 B |
| Nibble (compacted) | `NibbleArray` (2048 B) | **10 240 B** | 81 920 B | 163 840 B |

With the full-byte metadata, an 8-eager subchunk column (98 304 B) is **exactly**
today's flat footprint, so the height extension carries **no** fresh-terrain memory
cost. The fully-built 16-subchunk column is 196 608 B. Compacting `data` to a
NibbleArray would only shrink today's baseline (to 81 920/163 840 B), but it
reverses the existing full-byte storage decision and touches every metadata
read/write and the save format. **Recommendation:** keep the full-byte metadata and
take the exact-fresh-terrain-footprint benefit of the subchunk model as-is.
Compacting metadata is an orthogonal cleanup, not part of the height extension.

---

## Appendix C: Complete index/height-site inventory

Cross-cutting families in this codebase (both trees identical except render):

**Flat block-plane index `<< 11 | << 7` (128-tall):** `Chunk` (~20 sites),
`NibbleArray.getNibble/setNibble`, `WorldRenderer:240`, `BiomeGenBase:495` (gen
buffer — keep at 128), `ChunkProviderGenerate` (gen buffer — keep at 128). Under
the primary plan these become subchunk-local `<< 8 | << 4 | yLocal` (or are sliced
from the gen buffer for the `<<11|<<7` gen sites).

**Height ceilings `127`/`128`/`& 127`:**
`Chunk` (height-map seeds, `getSavedLightValue` `>127`, `if(y==128) break`),
`World` (guards in `World.java` Decision list, random tick + cave sound `&127`),
`ChunkCache` (63/121/199), `RenderGlobal.renderChunksTall=8`,
`SpawnerAnimals` (128 + `nextInt(128)`), `BiomeGenBase.waterFallMaxHeight=128`,
`ChunkProviderGenerate` (`rand.nextInt(128)` — gen buffer, keep),
`StarlightEngine` (flat-128 port: `>127`/`&127` in `getBlockState` 170/174,
`getLightLevel` 181/186, `setLightLevel` 199, increase/decrease propagation
299/305/314/360/367/402, `checkSkyEmittance` 483/494, `initSkylight` 580,
`initBlockLight` 601, `propagateNeighbourLevels` 634/684 — lift to
`SECTION_HEIGHT` per the Starlight section; the Anvil r1.2.5 reference already has
the `>255`/`&15` shape).

**Array sizes `new byte[32768]` (flat gen buffers):** `ChunkProviderGenerate`
(358/359, 461/462), `ChunkProviderHell` (197/218), `ChunkProviderSky` (94) — all
generation buffers, kept at 128 (Decision 10). Only `Chunk.blocks`/`data` become
subchunks.

**Vertical entity slabs `new List[8]`:** `Chunk` (82) → `16`.

**Server chunk send `ySize=128`:** `EntityPlayerMP` (256–258) → `256`.

**Client unpack/serialization clamps:** `World.setChunkData` (2679),
`World.getChunkData` (2629) → `SECTION_HEIGHT`.
