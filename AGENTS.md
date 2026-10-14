# InfHell Development

## Project Overview

This is **InfHell 2**, a heavily modified Minecraft Beta 1.7.3 with many backported features from later versions and original additions. The codebase is large (~1,500+ Java files) with third-party mod integrations (Twilight Forest, Mo'Creatures, Better Dungeons, etc.).

## Directory Structure

```
infhell-release/
├── src/                     # Main source code
│   ├── minecraft/           # Client code
│   │   ├── net/minecraft/
│   │   │   ├── client/        # Client-only classes (controller, render, gui, model, etc.)
│   │   │   │   ├── json/      # J_* JSON parser classes (Mojang JSon)
│   │   │   │   ├── model/     # Entity models
│   │   │   │   ├── render/    # Rendering (entity/, tileentity/, texture/, camera/)
│   │   │   │   ├── sound/     # SoundManager, CodecMus, MusInputStream
│   │   │   │   └── network/   # (reserved for future client-specific networking)
│   │   │   ├── game/          # Shared game classes
│   │   │   │   ├── physics/
│   │   │   │   ├── world/     # + biome/, block/, chunk/, material/, path/, terrain/
│   │   │   │   │              # terrain.generate/ (tree, city, bo3, structure, amazonvillage, etc.)
│   │   │   │   ├── entity/    # + animal/, misc/, monster/, player/, projectile/, status/, ai/, helpers/, item/
│   │   │   │   ├── command/   # + worldedit/
│   │   │   │   ├── trading/
│   │   │   │   └── worldedit/
│   │   │   ├── network/       # Common networking: DataWatcher, NetHandler, NetworkManager, NetworkMasterThread, NetworkReaderThread, NetworkWriterThread, ThreadMonitorConnection
│   │   │   ├── isom/
│   │   │   └── nbt/           # NBT classes
│   │   ├── resources/         # bo3, schematics, sounds, unusedbo3 (bundled in jar)
│   │   ├── seasons/           # Custom seasons textures (bundled in jar)
│   │   ├── title/             # Custom title screen assets (bundled in jar)
│   │   └── shaders/           # Shader assets
│   └── minecraft_server/    # Server code (mirrors client structure)
│       ├── net/minecraft/
│       │   ├── server/        # MinecraftServer.java (server entry)
│       │   ├── game/          # Same as client
│       │   ├── isom/
│       │   ├── nbt/
│       │   └── network/       # (mirrors client)
│       └── resources/         # bo3 files for server (bundled in minecraft_server.jar)
├── src-inf2010625-1/       # Reference: infdev 20100625-1 (target package organization)
├── workspace/              # Eclipse workspace
│   ├── Client/             # Client Eclipse project
│   ├── Server/             # Server Eclipse project
│   └── NBTSchematics2BlockArray/, RegExpTests/
├── jars/                   # Vanilla minecraft.jar, minecraft_server.jar, resources
├── lib/                    # LWJGL, JInput, minecraft.jar libs
│   ├── client/
│   └── server/
├── conf/start/Start.java   # Main class entry point
├── build/, bin/, reobf/    # Build output directories
└── backup_pre_reorg/       # Backup of src/ before reorganization (can be deleted)
```

## Development Workflow

- Work, compile, and test in **Eclipse** (workspace is at `workspace/`)
- Client project: `workspace/Client/`
- Server project: `workspace/Server/`
- Entry point: `net.minecraft.client.Minecraft` (client), `net.minecraft.server.MinecraftServer` (server)
- Currently uses `conf/start/Start.java` with reflection to set minecraftDir

## Reorganization Status

The flat `net/minecraft/src/` package has been **reorganized** into proper subpackages matching the `src-inf2010625-1/` reference structure. This was done via Python scripts (`backup_pre_reorg/` contains a pre-reorg backup if needed).

### Package structure summary (post-reorg)

**Shared (`net.minecraft.game.*`)**
- `game.physics.*` — AABB, Vec3D, Raycast, etc.
- `game.world.*` — World, chunks, blocks, biomes, materials, terrain
- `game.world.terrain.generate.*` — MapGenCity, WorldGen base, feature generators
- `game.world.terrain.generate.tree.*` — Tree/shrub/streetlight generators
- `game.world.terrain.generate.city.*` — Building + subclasses + `CityBlockData` + `CityBitmaps`
- `game.world.terrain.generate.bo3.*` — `WorldGenBo3Tree.java` + BO3 structures
- `game.world.terrain.generate.amazonvillage.*` — Amazon village `EntityAmazon`
- `game.entity.*` — Entity hierarchy + item, recipe subpkgs
- `game.command.*` — Commands + worldedit
- `game.trading.*` — Trading system
- `game.worldedit.*` — WorldEdit (client/server split)

**Client-only (`net.minecraft.client.*`)**
- `client.Minecraft` — main client
- `client.gui.*` — Screens, widgets, container
- `client.render.*` — Renderer pipeline
- `client.render.entity.*` — `RenderAmazon`, etc.
- `client.model.*` — `ModelAmazon`, etc.
- `client.sound.*` — `SoundManager`, `CodecMus`, `MusInputStream`
- `client.json.*` — 48 `J_*` Mojang JSOn parser classes
- `client.MoreResourcesInstaller` — Custom resource installer
- `client.GameSettings`, `client.GameSettingsValues` (was in client, now in `game`)
- `client.Seasons`, `client.Version` (was in client, now in `game`)

**Common networking (`net.minecraft.network.*`)**
- `network.DataWatcher` — entity state sync
- `network.NetHandler` — abstract packet handler
- `network.NetworkManager` — TCP packet dispatch
- `network.NetworkMasterThread`, `NetworkReaderThread`, `NetworkWriterThread` — I/O threads
- `network.ThreadMonitorConnection` — connection watchdog

Note: `Packet*.java` classes are currently in `net.minecraft.game.*` and reference `NetHandler` via import. Future move planned: `net.minecraft.network.packet.*` (see cleanup plan below).

**Server-only (`net.minecraft.server.*`)**
- `server.MinecraftServer` — main server entry
- `server.network.*` — `NetServerHandler`, `NetLoginHandler` (will move to `net.minecraft.server.network.*` in cleanup phase)

## Scripts Used

All scripts are in `C:\Users\na_th\AppData\Local\Temp\opencode\`:
- `fix_all_imports.py` - usage-based import adder (1940 imports, 755 files)
- `fix_mod_imports.py` - fix mod file imports for entity subpackages
- `fix_wrong_imports.py` - fix import paths pointing to wrong packages
- `fix_from_javac.py` / `fix_from_javac2.py` - parse javac errors and add imports
- `add_missing_imports.py` - earlier version (3863 imports, 1050 files)
- `fix_j_imports.py` - add J_ class inter-imports after move to `client.json`
- `fix_enum_import.py` - add EnumJsonNodeType import to J_ files

Backup at `backup_pre_reorg/src/` if reorganization needs to be reverted.

## Project Goals

### 1. ~~Reorganize Code into Packages~~ ✅ DONE

All classes (core Minecraft + third-party mods) reorganized into `net.minecraft.*` packages following infdev conventions. Both client and server compile with ZERO errors.

### 2. ~~Standalone minecraft.jar Export~~ ✅ DONE

`build.bat` compiles client + server and exports both jars:

**Output:**
- `minecraft.jar` — client (`Main-Class: net.minecraft.client.Minecraft`)
- `minecraft_server.jar` — dedicated server (`Main-Class: net.minecraft.server.MinecraftServer`)

**Contents:**
- Compiled `.class` files from `src/minecraft/` and `src/minecraft_server/`
- Audio libs: `paulscode/sound/*`, `com/jcraft/jorbis/*`, `com/jcraft/jogg/*` (extracted from vanilla `lib/client/minecraft.jar`)
- Vanilla resources: textures, lang files (extracted from vanilla `lib/client/minecraft.jar`)
- Custom resources: `bo3/`, `schematics/`, `sounds/`, `unusedbo3/` (from `src/minecraft/resources/`)
- Custom `title/` and `seasons/` assets (from `src/minecraft/title/`, `src/minecraft/seasons/`)
- Server `bo3/` resources (from `src/minecraft_server/resources/`)
- No natives/LWJGL (provided by Minecraft launcher)
- No sound/music files (streamed from `jars/resources/` at runtime)

**Usage:** Run `build.bat` from the project root.

### 3. ~~Bug Fixes (Priority: MEDIUM)~~ — ongoing
- Tree generation status tracked in `trees.md`
- Command system details in `commands.md`
- Check `changelog.txt` for recent fixes and known issues
- Fixed pre-existing `Empty2` constructor mismatch in `SaveConverterMcRegion` / `ChunkFolderPattern` / `ChunkFilePattern` (server) by switching to no-arg `new ChunkFolderPattern()` / `new ChunkFilePattern()`.

### 4. Cleanup & Refactoring Plan (Priority: LOW, ongoing)

**Goal:** After reorganization, clean up the obfuscated-style naming (e.g. `i1`, `j_`, `func_27290_a`, `field_27292_a`) introduced by the upstream Mojang obfuscation. This is a multi-step, low-risk pass to make the codebase readable and maintainable.

**Scope per step** (incremental, one package at a time, tested after each):
1. **Rename local variables & parameters** — `i1`, `i2`, `j1`, `string1`, `object1` → descriptive names.
2. **Rename private fields** — `field_27292_a` → semantic name (e.g. `value`, `next`, `parent`).
3. **Rename methods** — `func_27290_a` → semantic name (e.g. `append`, `build`, `visit`).
4. **Tighten access modifiers** — make truly private members `private` (some are over-promoted to `public` from the reorg).
5. **Reorganize remaining misplaced classes** — see list below.

**Already-completed cleanup moves** (folded into the reorg):
- `ColorizerFoliage`/`Grass`/`Water` → `game.world.block`
- `CodecMus` → `client.sound`
- `MusInputStream` → `client.sound`
- `ModelAmazon` → `client.model`
- `RenderAmazon` → `client.render.entity`
- `MoreResourcesInstaller` → `client`
- `ThreadSleepForever`, `Seasons`, `Version` → `game`
- `GameSettingsValues` → `game`
- `ChatAllowedCharacters` → `game`
- 48 `J_*` JSON classes → `client.json`
- `EnumJsonNodeType` left in `game` (re-exported to J_ files via import)
- `DataWatcher`, `NetHandler`, `NetworkManager`, `NetworkMasterThread`, `NetworkReaderThread`, `NetworkWriterThread`, `ThreadMonitorConnection` → `network` (both client & server)

**Naming-rename backlog** (no package moves, just identifiers):
- J_ classes: `func_27xxx_a/b` → semantic (e.g. `J_JsonListenerToJdomAdapter.func_27290_a` → `startObject`)
- `Packet` fields (`field_27292_a`) → semantic
- MapGenCity: rename `processEdgeChunk` parameter names; `drawRoofStairs` locals
- Block/item field names (`blockID` → `blockId`, `blockIndexInTexture` already correct)

**Conventions for renames:**
- One package per step
- After each package: run `build.bat` to confirm zero errors
- Keep client/server identical (where they should be)
- Preserve public API names that other mod code or runtime assets rely on
- For pre-obfuscation names, consult `src-inf2010625-1/` infdev reference for historical names
- When in doubt, ask before renaming — keep change atomic and reversible via `backup_pre_reorg/`

## Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| Minecraft | `net/minecraft/client/Minecraft.java` | Main client entry |
| MinecraftServer | `net/minecraft/server/MinecraftServer.java` | Server entry |
| World | `net/minecraft/game/world/World.java` | World management |
| EntityPlayer | `net/minecraft/game/entity/player/EntityPlayer.java` | Player entity |
| Block | `net/minecraft/game/world/block/Block.java` | Block registry |
| Item | `net/minecraft/game/item/Item.java` | Item registry |
| NetHandler | `net/minecraft/network/NetHandler.java` | Abstract packet handler |
| NetClientHandler | `net/minecraft/client/NetClientHandler.java` | Client packet dispatch |
| NetServerHandler | `net/minecraft/server/NetServerHandler.java` | Server packet dispatch |
| NetworkManager | `net/minecraft/network/NetworkManager.java` | TCP I/O |
| DataWatcher | `net/minecraft/network/DataWatcher.java` | Entity state sync |
| RenderEngine | `net/minecraft/client/render/RenderEngine.java` | Texture management |
| EntityRenderer | `net/minecraft/client/render/EntityRenderer.java` | Entity rendering |
| RenderGlobal | `net/minecraft/client/render/RenderGlobal.java` | World rendering |
| RenderBlocks | `net/minecraft/client/render/RenderBlocks.java` | Block rendering |
| MapGenCity | `net/minecraft/game/world/terrain/generate/MapGenCity.java` | City generation |
| CityBlockData | `net/minecraft/game/world/terrain/generate/city/CityBlockData.java` | Per-block data for MapGenCity |
| CityBitmaps | `net/minecraft/game/world/terrain/generate/city/CityBitmaps.java` | Bitmap constants for MapGenCity |

## Important Configuration Files

- `workspace/Client/.classpath` — Client build path
- `workspace/Client/.project` — Eclipse project config
- `conf/start/Start.java` — Custom launcher (uses reflection for minecraftDir)
- `jars/minecraft.jar` — Vanilla jar (contains resources, sound, META-INF)
- `lib/client/minecraft.jar` — LWJGL-bound vanilla jar for compilation
- `build.bat` — Build script (compile + jar packaging + resource bundling)

## Working with This Project

- **Compile in Eclipse**: Use the Client or Server project in workspace
- **Test in launcher**: Export jar, place in versions folder, use with appropriate launcher profile
- **Resource files**:
  - `src/minecraft/resources/` — bo3, schematics, sounds, unusedbo3 (bundled in client jar)
  - `src/minecraft_server/resources/` — bo3 (bundled in server jar)
  - `src/minecraft/title/` — custom title screen (bundled in client jar)
  - `src/minecraft/seasons/` — custom season textures (bundled in client jar)
  - `jars/resources/` — runtime sound/music streaming source
- **Client-Server parity**: classes that exist both in Client and Server must be identical, save for `WorldEdit.java` (client & server have different implementations) and `Minecraft.java` vs `MinecraftServer.java` (which are themselves the different entry points).
- **Parity verification**: run `C:\Users\na_th\AppData\Local\Temp\opencode\check_parity.py` after structural changes to confirm only intentional diffs remain.
- **Deobfuscation**: every file touched that still uses obfuscated style naming needs to have identifiers renamed to have readable names.

## Work State


## Work State

### Structure→Feature reimplementation (mineshaft, mineshaftmesa, stronghold)
Reimplementing structures backported from release vanilla minecraft (package `net.minecraft.game.world.terrain.generate.structure`) as Features for the FeatureProvider system (package `net.minecraft.game.world.terrain.generate.feature`). Each structure must be user-tested and confirmed before moving on; the original structure hook is unhooked (not deleted) once its Feature is validated.

**Decision: extend `FeatureProvider` to a shared `MAX_FEATURE_RADIUS` constant.** The provider's per-chunk scan and population scan used a hardcoded ring of 3; they now use `MAX_FEATURE_RADIUS = 8` (set in `FeatureProvider`), enough for the mineshaft (corridors can reach ~7 chunks from the origin room). `collectNearestFeatures`/`populateFeatures` loops and the mineshaft's `getFeatureRadius()` use it. This raises per-chunk scan work (sum of (2i+1)^2 for i=0..8 = 1938 cheap iterations, vs 84 before) but gate results are cached in `noFeatureChunks`.

**Decision: "two-step faithful-ish" split.** Plan the deterministic skeleton in `setup` (no world reads); lay normal/shell blocks in `generate` (writes directly into the chunk's flat buffers `chunk.blocks`/`chunk.data` at index `x<<12 | z<<8 | y`, the FeatureHollowHill pattern); resolve world-dependent decor (arches vs. hanging chains) AND specials (chests, spawners, torches, webs, rails) in `populate` (via `MineshaftWriter` -> `chunk.setBlockIDWithMetadata` / `world`). Each piece gets a deterministic per-piece seed so it draws identically regardless of which chunk (or how many times) it is rendered for; the vanilla shared RNG stream across components is intentionally given up.

**Mineshaft — DONE and CONFIRMED in-game** (user: "mineshafts spawn correctly, and are structurally correct and everything works as intended"). Temporary `[MS]`/`Shaft Chest` diagnostics have since been removed.
- New self-contained package `feature.mineshaft`: `FeatureMineshaft`, `MineshaftWriter` (chunk-flat generate writer vs. world populate writer), `MineshaftComponent` base (offset helpers + `fill`/`fillVariation`/`randomFill`/`randomBlock`/`randomRareFill`/`createTreasureChest`/`placeSpawner`), `MineshaftComponentRoom`/`Corridor`/`Cross`/`Stairs`, `MineshaftPieces` (factory + loot table).
- `FeatureMineshaft extends Feature` (NOT `FeatureDynamicSchematic`), `isGeneratedAfterCaves() == true`, spawn gate = vanilla rule (`rand.nextInt(80) < max(|cX|,|cZ|)` with `getSpawnChance()` = `MAXCHANCE/100`).
- `setup` ports `StructureStart.updateBoundingBox` + `markAvailableHeight(world, rand, 10)` so the shaft stays buried.
- Registered LAST in `FeatureProvider` (features are first-match exclusive; earlier broad-biome gates would preempt it — only `FeatureSlimeBossLair` is broad, at 1/4096, so preemption is negligible).
- Original mineshaft unhooked: `ChunkProviderGenerate` `mineshaftGenerator.generate` (~421) and `mineshaftGenerator.generateStructuresInChunk` (~994) commented; mesa and stronghold hooks initially left intact, mesa since unhooked (see below).
- Client and server copies byte-identical; both compile clean via the raw `javac` commands in `build.bat` (note: `build.bat` ends with `pause`, so run the javac lines directly for CI-style checks).
- **BUG FOUND & FIXED (separation veto):** `FeatureMineshaft` did not override `minimumSeparation()`, so it inherited `Feature.getFeatureRadius()` = `MAX_FEATURE_RADIUS` = 8. `FeatureProvider.getFeatureForChunkCoords` rejects a feature if ANY already-placed feature is within `separation` chunks, so with an 8-chunk keep-out (~289 nearby cells) some other feature was nearly always in range — the shaft passed the spawn gate and printed `Feature ...FeatureMineshaft @ x z`, then was silently rejected before `setup`/`generate`/`populate` (hence no blocks, no chests). Other features use tiny separations (hollow hill 1, volcano 3, stone arch 0). Fix: `FeatureMineshaft.minimumSeparation()` returns `0` (shafts may overlap other structures, like vanilla). NOTE the side effect: other features' own larger separations (ice palace 8, big ship/amazon/ocean/slime 16) will now treat dense shafts as blockers — acceptable but watch for suppressed rarities.
**Mineshaftmesa — IMPLEMENTED, awaiting in-game test:**
- Reuses the mineshaft package. `MineshaftPieces` was refactored into an instance factory: static `REGULAR = new MineshaftPieces(70)`, constructor takes the stair roll threshold (`>= t`), protected `createCorridor/createCross/createStairs`, and public `getNextComponent`/`getLoot`. `MineshaftComponent` got a `protected MineshaftPieces pieces` field + `setPieces()` so children are built by the correct variant.
- **World is threaded into every piece constructor.** `MineshaftComponent` now holds `protected final World world` and its constructor takes it; every subclass (room/corridor/cross/stairs + mesa variants) and both factories (`MineshaftPieces(World, int)`, `MineshaftPiecesMesa(World)`) pass it along. `MineshaftPieces.REGULAR` / `MineshaftPiecesMesa.INSTANCE` static singletons were removed (a factory is now per-feature, holding the world); `FeatureMineshaft.setup` uses `new MineshaftPieces(this.world, 70)` and `FeatureMineshaftMesa.setup` uses `new MineshaftPiecesMesa(this.world)`.
- **Mesa room base Y** is now terrain-derived: `world.getLandSurfaceHeightValue(x, z) - 5` (was `66 + rand(32)`), so the room is embedded near the local surface instead of floating above thin mesas (the old base frequently sat in air, so `isMostlySolid` failed and the room was never drawn). `setup` reads the height map, which is available because `ChunkProviderGenerate.provideChunk` calls `chunk.generateLandSurfaceHeightMap()` (~379) before `collectNearestFeatures` (~393).
- A diagnostic `System.out.println("[MesaMS] @ cx cy cz")` (room bounding-box center) is left in `FeatureMineshaftMesa.setup` to locate shafts in-game; remove once confirmed.
- `MineshaftPiecesMesa extends MineshaftPieces` (`super(world, 60)`) overrides all three `create*` to the mesa pieces.
- New mesa pieces (`MineshaftComponentMesaRoom`/`MesaCross`/`MesaStairs`/`MesaCorridor`), all `.generate()` no-ops that draw in `populate()` (the mesa needs the world for every decision). Room base is terrain-derived (see above); body otherwise identical to regular. Cross gate = `isLiquid || fracSolid < 0.4 || fracExposed > 0.5`; room/stairs gate = `isLiquid || !isMostlySolid`; corridor is a faithful port of `ComponentMineshaftMesaCorridor` (env-aware: `fillIfRock`, arches vs. hanging chains, plank floor, rails, webs, chests, spider spawner).
- `FeatureMineshaftMesa extends Feature`: `getFeatureRadius()=MAX_FEATURE_RADIUS`, `getSpawnChance()=MAXCHANCE`, `minimumSeparation()=0`, `shouldSpawn` = `biome instanceof BiomeGenMesa && rand.nextInt(50)==0`, `isGeneratedAfterCaves()=true`, `setup` builds the mesa room (no `markAvailableHeight`), `generate` empty, `populate` draws every intersecting piece with `new Random(pieceSeed(component))`.
- Env helpers ported onto `MineshaftComponent` (`isLiquidInChunk`, `getFractionSolid`, `isMostlySolid`, `getFractionExposed`, `fillIfRock`, `getBlockMaterial`, `placeBlockIfNextToSolid`, `randomBlockIfNextToSolid`) reading through `MineshaftWriter` + `getChunkBox()`; they mirror vanilla `StructureComponent` bounds exactly (chunk box = `chunkX<<4 .. +15`, y 0..255; vanilla used y 1..512 — the difference never affects the intersections).
- Registered `FeatureMineshaftMesa` immediately BEFORE `FeatureMineshaft` (mesa gate is specific, so it must not be preempted by the broad regular gate).
- Old mesa hooks commented in `ChunkProviderGenerate`: `mineshaftMesaGenerator.generate` (~420) and `mineshaftMesaGenerator.generateStructuresInChunk` (~995); stronghold hooks left intact.
- Client and server copies byte-identical; both compile clean via the raw `javac` lines in `build.bat`.
- **BUG FOUND & FIXED during compile:** `MineshaftPiecesMesa` initially did not override `createCross`/`createStairs`, so the mesa cross/stairs were unreferenced (javac never even compiled them) and the mesa would have used the regular cross/stairs — whose blocks are only emitted during `generate`, which the mesa leaves empty (silent no-op). Fixed by overriding both to the mesa pieces.

**Remaining:** user test of the mesa shaft, then stronghold (same package/method pattern). After each, ask the user to test in-game before proceeding.

**Repo note (state discovered this session):** the last commit `351220d "Structures ... gone, 1st step"` already contains an OLDER, now-superseded feature implementation (`MineRoomPiece`, `MineCorridorPiece`, `MineshaftRoomPool`, `MineshaftLoot`, a shared `feature.structure` framework with `FeatureStructure`/`StructurePiece`/`StructureRoomPool`, and `mineshaftmesa`/`stronghold` Feature packages). The working tree DELETES all of those (they show as `D` in `git status`) and replaces the mineshaft with the new self-contained `MineshaftComponent*`/`MineshaftWriter`/`MineshaftPieces` design; the working-tree `FeatureProvider` drops the mesa/stronghold registrations accordingly. So `mineshaftmesa`/`stronghold` Features must be reimplemented from scratch (the old committed versions are a reference but were deliberately removed).

## Next Move
1. Ask the user to test the mineshaftmesa Feature in-game: find a mesa biome, confirm above-ground mesa shafts generate with their arches/chains, plank floors, rails, webs, chests and spider spawners. The old mesa generator is unhooked, so any mesa shaft seen is the new Feature.
2. If confirmed, proceed to reimplement `stronghold` as a Feature using the same package/method pattern.
3. If mineshaft frequency feels too low, consider moving `registerFeature(FeatureMineshaft.class)` earlier in the registration order.

## Code Conventions

- All touched source code must have their variables renamed to have meaningful names.
- All classes (core and third-party mods) are in `net.minecraft.*` packages
- Server-side code mirrors client structure in `minecraft_server/`
- Some classes/methods made public for cross-package access (original Minecraft had package-private access)
- Common/shared code goes in `net.minecraft.game.*` (or `net.minecraft.network.*` for shared network)
- Client-only code (rendering, GUI, models) goes in `net.minecraft.client.*`
- Server-only code goes in `net.minecraft.server.*`
- Subpackages: `entity.animal`, `entity.monster`, `entity.player`, `entity.projectile`, `entity.ai`, `world.biome`, `world.terrain.generate.tree`, `world.terrain.generate.structure`, `gui.container.creativetab`
- **`mc` field convention**: `GuiScreen` subclasses use `this.mc` to reference the parent `Minecraft` instance (NOT renamed to `minecraft` to avoid cascading changes across 50+ subclasses). Classes that control their own lifecycle (`GameSettings`, `LoadingScreenRenderer`) renamed to `minecraft`.

## Communication Tone & Rules
- **No Flattery or Praise:** Avoid sycophancy, compliments, and filler adjectives (e.g., "excellent choice," "great job").
- **Zero Manners:** Skip greetings, pleasantries, apologies, and concluding remarks. Start immediately with the answer or action.
- **Omit Needless Words:** Keep responses concise, direct, and down-to-the-point. Do not explain obvious logic or write conversational transitions.
- **Direct Disagreement:** If an assumption or code snippet is wrong, state it plainly and correct it immediately without softening language.
