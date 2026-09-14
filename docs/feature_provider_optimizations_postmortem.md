# FeatureProvider Optimisations — Postmortem

Status: **IMPLEMENTED + VERIFIED** (client & server, byte-identical, `build.bat` exports both jars).
Date: 2026-09-13.

## 1. Motivation

`FeatureProvider.getFeatureForChunkCoords()` ran on **every chunk scan** and:

1. Re-created a fresh feature **instance via reflection** for each of the 11 registered
   classes just to evaluate its gate (`shouldFeatureSpawn`), even when the gate was
   trivially false for the current biome. Each scan could allocate ~11 objects.
2. Re-ran the probe even for chunks already visited (the 7×7 `ring 0..3` sweep overlapped
   cells heavily — a cell on ring `r` is re-visited by every parent loop with `i >= r`).
3. Kept a forever-growing `generatedChunks` set (never evicted).
4. Resolved `minimumSeparation` against **block** coordinates while `minimumSeparation()`
   is declared in **chunks** — the check was effectively "same chunk ± a few blocks",
   not the documented "N chunks apart". Two features 1 chunk apart were allowed.
5. Held every DynamicSchematic's populated `short[][][] schematic` in memory for the whole
   session (a feature keeps a multi-megabyte array after its chunks are long gone).

## 2. Changes made

Both `src/minecraft` and `src/minecraft_server` trees, byte-identical, mirrored exactly.

### Change 1 — probe gate, negative memo (+ `populatedChunks` LRU, `featureList` session-resident)
`net/minecraft/game/world/terrain/generate/feature/FeatureProvider.java`

- **Cached feature constructors** (`featureConstructors`): the reflective `(World, int, int,
  FeatureProvider)` ctor lookup for each registered class happens once (in `registerFeature`)
  instead of per chunk per class.
- **Per-class probe instances** (`probeFeatures`): one long-lived `Feature` per registered
  class reused to evaluate `shouldFeatureSpawn`. Because every `shouldSpawn` body is a pure
  function of `(world, seed, biome, chunkX, chunkZ)` (verified for all 11 classes — see
  §4), the probe's verdict is byte-identical to a fresh allocation.
- **Negative memo** (`noFeatureChunks` LRU, cap 4096): a chunk whose sweep produced no
  feature is remembered; later overlapping requests short-circuit instead of re-running the
  gate.
- **`populatedChunks` LRU** (cap 16384) replaces the unbounded `generatedChunks` HashSet
  (the "already populated" failsafe). The failsafe println is preserved.
- **`featureList` is never evicted** — a feature's origin stays resolveable for the session,
  preserving generation of *older* overlapping chunks (e.g. a bigger ship that drifts into
  already-generated territory).
- `getNearestFeatures` rewritten as a **single 7×7 sweep** (rings 0..3), visiting each cell
  exactly once, preserving the original first-visit creation order (cell first probed at
  ring `max(|dx|,|dz|)` — identical sequence to the original quadruple-nested loops).
- `ensureSchematic()` is called on a feature **before** its `generate` stage and
  `onChunkPopulated()` **after** each `populateFeatures` population pass, so once-a-feature
  schematic rebuild/low-frequency bookkeeping is decoupled from the per-chunk hot path.

### Change 2 — shared built schematic, release/rebuild lifecycle
`Feature.java`, `FeatureDynamicSchematic.java`, `FeatureProvider.java`, `IChunkLoader.java`,
`ChunkLoader.java`, `McRegionChunkLoader.java`, `World.java`

- `FeatureDynamicSchematic.buildSchematic(...)` (new shared helper): clears the special-blocks
  scratch array, computes `totalPopulates` **disk-aware**, then invokes the existing
  `generateSchematic` polymorphically — structure bytes unchanged.
- `totalPopulates` is no longer always `(1 + 2 * getFeatureRadius())²`. The new
  `computedPopulatesRemaining()` counts only the feature's box chunks that are NOT already
  final, where "final" means either resident with `isTerrainPopulated`, or persisted by the
  chunk loader (see below). A world saved mid-generation therefore re-arms to the true
  outstanding count on resume instead of re-counting the full box.
- New `pendingPopulates` counter; `onChunkPopulated()` decrements it and, when it reaches 0,
  **releases** the schematic (`schematic = null`, `specialBlocks = null`) so the multi-megabyte
  array is GC-able. `Feature` gains no-op `ensureSchematic()`/`onChunkPopulated()` defaults.
- **Box-scoped release events (root-cause fix):** `FeatureProvider.populateFeatures` fires
  `populate()` for every feature within the old trivially-true radius check (any origin within
  the ±3 scan), but `onChunkPopulated()` now only fires when the popped chunk is INSIDE the
  feature's own box (|ΔX|, |ΔZ| ≤ radius). Previously the counter was drained by unrelated
  ±3 neighbors, releasing the schematic before its true last draw and forcing rebuild churn.
  `populate()` stays window-scoped (HollowHill etc. still scatter lilies/seaweed/coral across
  the whole scan).
- **Disk-aware arming:** new `IChunkLoader.chunkExists(World, int, int)` default (cache-based,
  `world.chunkExists`) with overrides in the two real loaders — `ChunkLoader` (per-chunk
  `.dat`, pure path compute, no mkdir side effect) and `McRegionChunkLoader` (region `.mcr`,
  `RegionFileCache.getRegionFile(...).isChunkSaved(...)`). `World.getChunkLoader()` exposes the
  saved-chunk probe so `computePopulatesRemaining()` can tell "persisted in an earlier session"
  (never re-populated → no decrement) from "still outstanding" (will pop this session →
  count it). Loader is `null` during construction or without a SaveHandler.
- New `ensureSchematic()` rebuild (re-arms `pendingPopulates = totalPopulates` recomputed
  disk-aware, regenerates via the same seed and biome formula as setup) so a regenerated box
  chunk re-populates and releases again. Called from `generate` paths and from safety guards
  inside `drawPieceOnPopulation`/`drawSpecialBlocksForChunk` so a released feature reaching a
  still-populated chunk re-builds on demand.

### Change 3 — consistency of seed/biome
The seed stays exactly: `world.getRandomSeed() + chunkX * 25117 + chunkZ * 151121` (int
overflow on the chunk term, as in the original). Rebuild (`ensureSchematic`) uses the same
formula with `originChunkX/originChunkZ` and `getBiomeGenAt(centerX, centerZ)`.

### Change 4 — commented the contractual purity requirement
`Feature.shouldSpawn` javadoc now documents that implementations MUST be pure (seeded
`Random`-only, no `world.rand`, no unseeded sources, no state mutation). This is what
makes the shared-probe and the memoisation sound.

### Change 6 — `minimumSeparation` now compared in chunk units (ACCEPTED cost: layout change)
`FeatureProvider.java:235-236` — separation check compares `(centerX >> 4) - (other.centerX >> 4)`
(and Z) against `minimumSeparation()` so the documented "N chunks" is what actually happens.
Features closer than `minimumSeparation` chunks (Chebyshev) now correctly reject each other.

Per the plan, changes were **NOT** wanted / deferred:
- Change 5 (sparse "schematic-cells" schematic representation) — **not selected**, deferred
  as an orthogonal memory optimization.
- Change 7 (feature-type housekeeping / dead `getEffectiveSpawnChance`) — **left out-of-scope**.

## 3. Verification

### 3.1 Compile / parity / build
- Client compile (`Minecraft.java` driven, `lib/client/*` classpath): **0 errors**.
- Server compile (`MinecraftServer.java` driven, `lib/server/minecraft_server.jar`): **0 errors**.
- `check_parity.py` (post Option-A release-counter fix): **830 identical / 37 substantive** —
  the expected diff set (the feature files themselves are byte-identical across trees).
- `build.bat`: both `minecraft.jar` and `minecraft_server.jar` exported successfully.

### 3.2 Determinism proof — `FeatureProbe.java`
A standalone probe (living in `C:\Users\na_th\AppData\Local\Temp\opencode\featureprobe\`)
drives the REAL `SaveConverterMcRegion → World → ChunkProvider` facture pipeline over a fixed
grid (~460 chunks, `SEED = 12345L`) and snapshots `featureList` (origin chunk → feature class)
in both insertion order and sorted order. Modes `golden` / `verify`.

**Critical detour — spawn-probe nondeterminism.** The first golden captures did not repeat:
one stone arch flickered between chunk `0,-8` and `-8,-8` across identical runs.
Root cause: `World`'s constructor calls `getInitialSpawnLocation()`, which rambles using the
**unseeded** `world.rand` (`World.java`: `this.rand = new Random()`), generating/populating a
different random set of chunks every run — pre-placing features that cascade into the grid.
Fixed in the probe by subclassing `World` (`ProbeWorld`) and making `getInitialSpawnLocation()`
a no-op. Golden runs then byte-repeat across separate JVMs.

> Side note: this latent unseeded-rand walk also means *real* worlds have a genuinely
> unpredictable spawn region feature set — expected legacy behaviour, not changed here.

Results (all three builds, same seed/grid, deterministic probe):

| build | features placed | vs original |
|---|---|---|
| original (golden) | 15 | baseline |
| modified minus change 6 (nosep) | 15 | **byte-identical** → changes 1–4 are placement-neutral |
| modified (change 6) | 10 | intended re-layout (arch pack 12 → 7; strict separation) |

The 15→10 delta is exactly the audit contract: changes 1–4 provably leave placement untouched,
and change 6 changes *spacing*, not existence logic (OceanRuins/fossils unaffected).

### 3.3 Release/rebuild functional check
*Superseded 2026-09-14: `ensureSchematic`/`totalPopulates` were removed — the release path is
now dropping the feature from `featureList` (see §9).*
Under the modified build the probe also asserts the lifecycle: a `FeatureStoneArch` whose
schematic was released during population must rebuild deterministically, re-arm
`pendingPopulates` to the disk-aware `totalPopulates` (0 here — the whole box already done),
and be a no-op on a second `ensureSchematic()`.
**PASS** — rebuild prints the identical `Arch @ -120 66 -120`; `pending=0 == total=0`.
Placement remains byte-identical to the modified build (VERIFY PASS), so the release events
being box-scoped does not change any generated blocks.

## 4. Purity audit (why the shared probe / memo are sound)

Every registered feature's `shouldSpawn` was checked against unseeded/time-based/jumping
sources:
- All 11 gates use only biome (`BiomeGenBase`), `world.isOceanChunk`, `world.isUrbanChunk`,
  `world.getLandSurfaceHeightValue`, or the `String biomeKey` comparison.
- Height facades used inside gates are **cache-backed** (`TerrainHeightQueryCache`), so a
  gate never forces live chunk generation.
- `FeatureAmazonVillage` / `FeatureSlimeBossLair` call `world.justGenerateForHeight` **inside
  their shouldSpawn** — via the cached height path only (pure).
- No `shouldSpawn` consumes the passed `Random` in a way that reads `world.rand`; the only
  unseeded `new Random()` calls found are in `FeatureProvider.getEffectiveSpawnChance`
  (dead code) and `FeatureBuilding/FeatureWreck` **generate/populate** paths (block content,
  not placement).

## 5. Trade-offs / risks (accepted)

1. **New-area spacing changes** (change 6): newly generated areas may place fewer, better
   spaced features. Existing chunks are unaffected (`featureList` is session-resident and the
   heights/`featureList` snapshots don't retro-fit separation onto already-placed features).
2. **Massive-session repopulation edge**: `populatedChunks` LRU evicts at 16384 entries; in a
   single session that covers >16k chunk-populations, a *still-unbuilt* feature whose origin
   chunk was evicted could theoretically re-populate. (2026-09-14: un-built features are now
   structurally immune — a feature is only released *with* its chunk fully populated, and
   release removes it from `featureList`, so no draw path can ever hit a released feature;
   see §9.) No evidence in normal play; a future pass could raise the cap if memory allows.
4. **Determinism contract**: the whole optimisation leans on `shouldSpawn` staying pure.
   The base-class javadoc now documents the obligation; any future feature violating it will
   surface as a placement nondeterminism and must be treated as a bug in the feature.

## 6. Evidence artifacts

All under `C:\Users\na_th\AppData\Local\Temp\opencode\featureprobe\`:
- `feature_golden.txt` (original, 15 features), `feature_modified.txt` (change 6, 10 features).
- `original\`, `modified\`, `nosep\` source variants used for the three builds.
- `classes_golden` / `classes_nosep` / `classes_verify` — compiled probe runs.

## 8. Re-entry fix — why a fossil "rendered 3-4 times" in a seemingly new world (2026-09-13)

**Symptom (user report):** a fossil discovered in a brand-new level printed "Fossils @" 3-4
times; a ship printed once. User: "the process that frees the structure is too eager".

**Root cause:** every world entry builds a *fresh* `FeatureProvider` whose `featureList` is
empty. Chunks persisted from an earlier session are never re-populated, but the *origins*
around them live on in the deterministic gate: when any **new** chunk is generated within a
3-chunk ring of a saved feature origin (e.g. re-entering, or afterwards generating chunks a
couple of cells beyond the already-saved area), `getFeatureForChunkCoords` misses both
`featureList` and `noFeatureChunks`, re-runs the (deterministic) gate, passes, and creates a
brand-new feature object — `setup()` → a fresh `generateSchematic()` → another print, plus a
needless multi-hundred-KB schematic build that draws into nothing (its box chunks are final).
Every additional re-entry/log-in prints once more → the 3-4 prints.

Ships stayed at one print because `FeatureShip` is radius 0 (its whole footprint is its
origin cell, whose print only ever happens once at first population) and because its
`minimumSeparation()` of 8 structurally blocks re-creation on later passes.

Reproduced deterministically in `FeatureRejoinProbe` before the fix: an origin re-printed
in consecutive sessions `(-184, 216): phase1=1 phase2=1 phase3=1`, i.e. re-created on every
re-entrance. In-session regeneration (the original bug, fixed in section 2) was a FALSE
lead — save-before-drop makes it impossible; the multi-print is a *re-entry* phenomenon.

**Fix (`FeatureProvider.getFeatureForChunkCoords`, both trees):** after the gate passes and
the real feature instance exists, skip creation when the feature's **entire footprint**
(origin + `getFeatureRadius()` box) is already final — every box chunk resident-and-populated
or already written to disk (`isFeatureFullyBuilt` / `isChunkFinal` helpers, cache + disk
probes only, side-effect free). `null` is returned exactly like a failed gate, so the caller
memoizes the cell in `noFeatureChunks` for the session. A feature whose box is only PARTLY
final (e.g. the frontier of a saved world) is NOT skipped: those outstanding chunks
legitimately need the deterministic rebuild so the structure completes across sessions.

**Why it never changes first-time generation:** during a clean contiguous pass no box chunk
is final until it is populated, and the first chunk within a 1-chunk ring of a feature is
where the gate first fires — at that instant the box cannot be fully final. Verified:
`FeatureProbe verify` vs `feature_modified.txt` stays byte-identical (`VERIFY PASS`,
`RELEASE CHECK PASS`).

**Probe A/B results (`FeatureRejoinProbe`, seed 12345, 33x33 grid):**
- Post-fix: `PASS=11 FAIL=0`; interior fossil `boxFinal=true -> re-created=false`,
  `rejoin-prints=0 OK (no rebuild on re-entry)`; only the 2 border features (boxFinal=false)
  re-created (frontier completion). `featureList` after re-entry probes = 0.
- Pre-fix: `PASS=4 FAIL=4` — every fully-built structure re-created on re-entry, fossil
  `rejoin-prints=1 *** REBUILT ON RE-ENTRY ***`.

Build + parity gates: client & server compile ZERO errors; parity 830 identical / 37
substantive (unchanged; FeatureProvider dropped out of the diff list); `build.bat`
`BUILD_EXIT=0`, both jars exported.

## 9. Schematic-rebuild mechanic REMOVED — release = drop from `featureList` (2026-09-14)

**Directive (user):** "remove the `ensureSchematic` methods completely; this mechanic is not
needed." The release counter already proves a feature is never drawn after its last box
chunk, so keeping a whole rebuild path (seed/biome reproducible schematic allocation) was
pure complexity.

**Design change:** a dynamic-schematic feature is released by *forgetting it*:

- `FeatureDynamicSchematic.onChunkPopulated`: when `pendingPopulates` reaches 0, the schematic
  is freed AND the feature removes itself from the provider (`featureProvider.removeFeature(this)`,
  identity-guarded: only if `featureList.get(originHash) == feature`). Freeing and removal are
  atomic — there is no window where a freed feature is still reachable.
- The cell then short-circuits forever: a later chunk generation that scans it re-runs the
  spawn gate, `isFeatureFullyBuilt` sees every box chunk final and returns `null`, and the
  caller memoizes the cell in `noFeatureChunks` for the session. Nothing can ever re-draw the
  freed schematic because nothing can re-create the feature.
- Removed entirely: `Feature.ensureSchematic()` base no-op; `FeatureDynamicSchematic.ensureSchematic()`
  override; the two `if (this.schematic == null) this.ensureSchematic();` safety nets in
  `drawPieceOnPopulation` / `drawSpecialBlocksForChunk`; the `feature.ensureSchematic()`
  pre-draw call in `getNearestFeatures`; `FeatureIcePalace.ensureSchematic` (the sky-height
  rebuild gate); and the now-dead `totalPopulates` field (`buildSchematic` arms
  `pendingPopulates` directly; `setup()` no longer needs an extra arm).
- The `isFeatureFullyBuilt` re-entry guard (§8) stays: a fully built structure re-created on a
  world re-entry would rebuild the schematic for nothing — admissible to keep blocking.

**Soundness invariant:** a feature is in `featureList` iff it is still drawing. Both provider
draw paths iterate `featureList` (`getNearestFeatures` generate stage, `populateFeatures`
populate stage), and `onChunkPopulated` fires *after* the draw in the same loop, so a drawn
feature always has a live schematic. A frontier feature saved mid-generation is never removed
(it re-arms to its outstanding chunk count on resume and removes only at its true last draw).

**Verification (headless probes):** FeatureProbe golden regenerated
(`feature_golden_removal.txt`, 16 lines, new baseline) → `VERIFY PASS` twice (run-to-run
byte-identical); `RELEASE CHECK PASS` — the 6 resident dynamic-schematic features all have
`pendingPopulates > 0` and a live schematic; completed features are absent. FeatureRejoinProbe
(rewritten to census features from the setup() prints, since completed ones leave
`featureList`): `PASS=1 FAIL=0` — interior fossil built exactly once (`phase1=1`), box fully
final on re-entry → not re-created, `rejoin-prints=0`; the post-re-entry `featureList` holds
only frontier features.

Client & server compile ZERO errors; parity 830 identical / 37 substantive unchanged (all four
files byte-identical across trees); `build.bat` `BUILD_EXIT=0`, both jars exported.

## 7. Files touched

- `src/minecraft/net/minecraft/game/world/terrain/generate/feature/FeatureProvider.java` (+ server mirror)
- `src/minecraft/net/minecraft/game/world/terrain/generate/feature/Feature.java` (+ server mirror)
- `src/minecraft/net/minecraft/game/world/terrain/generate/feature/FeatureDynamicSchematic.java` (+ server mirror)
- `src/minecraft/net/minecraft/game/world/terrain/generate/icepalace/FeatureIcePalace.java` (+ server mirror)
- `C:\Users\na_th\AppData\Local\Temp\opencode\featureprobe\FeatureProbe.java` (probe harness, not shipped)
- `C:\Users\na_th\AppData\Local\Temp\opencode\featureprobe\FeatureRejoinProbe.java` (re-entry regression probe, not shipped)