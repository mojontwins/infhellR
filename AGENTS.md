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

### What was reorganized
- ~263 classes moved to `net.minecraft.game.*` subpackages (physics, world, entity, item)
- ~250 classes moved to `net.minecraft.client.*` subpackages (render, gui, model, etc.)
- ~13 NBT classes moved to `net.minecraft.nbt`
- 618 mod classes reorganized from `com.*` to `net.minecraft.*` packages following infdev conventions:
  - Entity classes → `net.minecraft.game.entity.animal`, `monster`, `player`, etc.
  - AI classes → `net.minecraft.game.entity.ai`
  - Render classes → `net.minecraft.client.render.entity`
  - Model classes → `net.minecraft.client.model`
  - WorldGen classes → `net.minecraft.game.world.terrain.generate.*`
  - Structure classes → `net.minecraft.game.world.terrain.generate.structure.*`
  - Trading classes → `net.minecraft.game.trading`
  - Command classes → `net.minecraft.game.command.worldedit`
  - Creative tab classes → `net.minecraft.client.gui.container.creativetab`
- All package declarations updated
- ~3500+ import statements updated across 1000+ files
- 24 unused imports removed
- 15+ classes made public for cross-package access
- 6 classes fixed with duplicate `public public` modifiers
- 10+ fields/methods made public
- 2 Path import conflicts fixed
- 1 invalid Java syntax fixed
- FQN references replaced with proper imports
- Fixed `WorldGenCypress`/`WorldGenWillow` (wrong file version used previously)

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

### Client compilation: **ZERO ERRORS**
The client (`src/minecraft/`) compiles cleanly with javac:
```
javac -d build_test -cp "lib/client/*.jar" -sourcepath src/minecraft src/minecraft/net/minecraft/client/Minecraft.java
```
-> 0 errors

### Server compilation: **ZERO ERRORS**
The server (`src/minecraft_server/`) also compiles cleanly:
```
javac -d build_test -cp "lib/server/*.jar" -sourcepath src/minecraft_server src/minecraft_server/net/minecraft/server/MinecraftServer.java
```
-> 0 errors

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
- **Check package structure**: Compare with `src-inf2010625-1/` for reference organization
- **Resource files**:
  - `src/minecraft/resources/` — bo3, schematics, sounds, unusedbo3 (bundled in client jar)
  - `src/minecraft_server/resources/` — bo3 (bundled in server jar)
  - `src/minecraft/title/` — custom title screen (bundled in client jar)
  - `src/minecraft/seasons/` — custom season textures (bundled in client jar)
  - `jars/resources/` — runtime sound/music streaming source
- **Client-Server parity**: classes that exist both in Client and Server must be identical, save for `WorldEdit.java` (client & server have different implementations) and `Minecraft.java` vs `MinecraftServer.java` (which are themselves the different entry points).
- **Parity verification**: run `C:\Users\na_th\AppData\Local\Temp\opencode\check_parity.py` after structural changes to confirm only intentional diffs remain.

## Work State
### Completed
- **256-block-height design plan finalized** (`docs/256_blocks_high.md`, both trees): lazy 16×16×16 subchunks (`NibbleArray` per section), subchunks 0–7 eager (y 0–127) + 8–15 lazily allocated on first write; `null` subchunk = implicit air/skylight 15/blocklight 0; index invariant `x<<11|z<<7|y` → subchunk-local `x<<8|z<<4|yLocal`; `SECTION_HEIGHT=256`; memory: 8 eager = today's footprint exactly, 16 built = 2.0×; terrain gen stays 128-high; random-tick 10 probes/subchunk; save format `Height` + `SubchunkMask` + parallel subchunk lists; renderer scan-skip via `isEmpty` flag tracked. **Starlight adaptation mapped**: our `StarlightEngine` is a flat-128 port (hardcoded `127`/`& 127` enumerated at exact lines in `getBlockState`/`getLightLevel`/`setLightLevel`/`performLightIncrease`/`performLightDecrease`/`checkSkyEmittance`/`initSkylight`/`initBlockLight`/`propagateNeighbourLevels`); shape reference = Anvil-native original at `D:\Cosas\InfdevProject\Main\minecraft_r1.2.5\src\minecraft\ca\spottedleaf\starlight\StarlightEngine.java` (per-section `ExtendedBlockStorage` nibbles, `& 15` local indexes, `> 255` bounds, `initSkylight` at maxY+1=256, sections 15→0, null nibble read = skylight 15 / blocklight 0, null write no-op). Queue encoding unchanged (Y is a 16-bit field in the 28-bit packed coord, `encodeOffsetY=32`). Client/server StarlightEngine differ only in `postLightUpdate` (client calls `world.markBlockNeedsUpdate`; server comments it out) — keep that intentional divergence; `isClientSide` is unused. Entry points: `Chunk.initLightingForRealNotJustHeightmap` (`Chunk.java:959`; callers `ChunkProvider:70`, `ChunkProviderLoadOrGenerate:82`, `SchematicsBigShip:127`) and `Chunk.updateLight` (`Chunk.java:967`, from `setBlockIDWithMetadata`/`setBlockIDAndMetadataColumn`); `relightBlock` is height-map only. Plan captured in Implementation Order (10 steps), Manual Verification, Appendices A/C.
- **Bug fix — 3D blocks dark in inventory** (`RenderItem.java` + `RenderBlockUtil.java`):
  - `drawItemIntoGui`: added `GL11.glDisable(GL_LIGHTING)` at start of 3D block path (line ~138) and `GL11.glEnable(GL_LIGHTING)` before `glPopMatrix()` (line ~161).
  - `renderCubeOnInventory`: per-face shade factors applied (`brightness * shade`), correct order: bottom=1.0F (side 0), top=0.5F (side 1), EW=0.8F, NS=0.6F.
- **`Minecraft.java`**: renamed `converMapToMCRegion` → `convertMapToMCRegion`; `changeWorld1` → `clearWorld`; `changeWorld2` → `transitionToWorld`; updated all 8 call sites in `Minecraft.java`, `NetClientHandler.java`, `GuiConnecting.java`, `GuiGameOver.java`, `GuiIngameMenu.java`.
- **`GameSettings.java`**: full rewrite — class/method javadoc, renamed `mc` → `minecraft`, `MODE` → `AVAILABLE_DISPLAY_MODES`, added missing `import net.minecraft.game.GameSettingsValues`.
- **`EnumOptions.java`**: full rewrite — class/constant javadoc, renamed fields `enumFloat`→`isFloat`, `enumBoolean`→`isBoolean`, `enumString`→`localizedKey`; renamed methods to `isFloat()`, `isBoolean()`, `getLocalizedKey()`, `getOrdinal()`; added `getEnumOptions(int)` refactored to iterate values instead of indexing by ordinal.
- **`KeyBinding.java`**: class/method javadoc, field javadoc added.
- **`Session.java`**: class javadoc, field javadoc added.
- **`GameSettingsKeys.java`**: class javadoc, field javadoc added, `MODES` removed (unused), fixed duplicate `;;` in two static fields.
- **`Timer.java`**: full rewrite — class/method javadoc, field javadoc added, renamed no fields (already descriptive).
- **`OpenGlCapsChecker.java`**: class/method javadoc, renamed `tryCheckOcclusionCapable` → `supportsOcclusionCulling`.
- **`LoadingScreenRenderer.java`**: full rewrite — class/method javadoc, renamed `mc` → `minecraft`, all variable names made descriptive.
- **`MouseHelper.java`**: full rewrite — class/method javadoc, renamed `windowComponent` (already descriptive), field javadoc added.
- **`IntHashMap.java`**: full rewrite — class/method javadoc, generic type `<V>` added, renamed `slots`→`slots`, `growFactor` already descriptive; removed raw-type warnings.
- **`IntHashMapEntry.java`**: full rewrite — class javadoc, generic type `<V>`, `@Override` annotations added.
- **`ScreenShotHelper.java`**: full rewrite — all variable names made descriptive (`file0`→`minecraftDir`, `i1`→`width`, `i2`→`height`, `file3`→`screenshotsDir`, `string4`→`timestamp`, etc.), class/method javadoc added.
- **`Gui.java`**: renamed `func_27100_a` → `drawVerticalAchievementConnector`, `func_27099_b` → `drawHorizontalAchievementConnector`, with full javadoc on both.
- **`GuiAchievements.java`**: updated call sites to use renamed methods from `Gui.java`.
- **`GuiScreen.java`**: full rewrite — class/method javadoc added, all variable names made descriptive (kept `mc` field name to avoid cascading renames in 50+ subclasses).
- **`GuiSlot.java`**: full rewrite — renamed `posZ`→`rowHeight`; `field_25123_p`→`renderSelectionBox`; `field_27262_q`→`renderHeader`; `field_27261_r`→`headerHeight`; `func_27258_a`→`setRenderSelectionBox`; `func_27259_a`→`setRenderHeader`; `func_27255_a`→`drawFooter`; `func_27257_b`→`drawFooter`; `func_27256_c`→`getSlotAtMouse`; added `@Override` on overridden methods; added class-level javadoc.
- **`GuiSlotStats.java`**: renamed `func_27258_a`→`setRenderSelectionBox`; `func_27259_a`→`setRenderHeader`; `func_27255_a`→`drawHeader` (signature adds Tessellator param); `func_27257_b`→`drawFooter`; `func_27256_c`→`getSlotAtMouse`; `func_27266_c`→`toggleSection`; `func_27264_b`→`getCraftingStatAt`; `func_27263_a`→`getSectionTranslationKey`; `func_27265_a`→`drawStatValue`; `func_27267_a`→`drawStatTooltip`; `field_27270_f`→`sectionHighlightState`; added `@Override` on `drawHeader`/`drawFooter`.
- **`GuiSlotStatsGeneral.java`**: renamed `func_27258_a`→`setRenderSelectionBox`; `field_27276_a`→`guiStats`; updated call sites.
- **`GuiSlotStatsBlock.java`**: renamed `func_27264_b`→`getCraftingStatAt`; `func_27265_a`→`drawStatValue`; `func_27263_a`→`getSectionTranslationKey`.
- **`GuiSlotStatsItem.java`**: renamed `func_27264_b`→`getCraftingStatAt`; `func_27265_a`→`drawStatValue`; `func_27263_a`→`getSectionTranslationKey`.
- **`SorterStatsBlock.java`**: renamed `func_27297_a`→`sortByStat`; `field_27270_f`→`sectionHighlightState`.
- **`SorterStatsItem.java`**: renamed `func_27371_a`→`sortByStat`; `field_27270_f`→`sectionHighlightState`.

- **`BlockRenderHandler`** interface — added `renderItemIn3d()` default method returning `true`; 16 flat handlers (`RenderBlockFluid`, `RenderBlockFire`, `RenderBlockTorch`, `RenderBlockRedstoneWire`, `RenderBlockCrops`, `RenderBlockDoor`, `RenderBlockLadder`, `RenderBlockRail`, `RenderBlockLever`, `RenderBlockBed`, `RenderBlockRepeater`, `RenderBlockPane`, `RenderBlockVine`, `RenderBlockLilyPad`, `RenderBlockPlant`, `RenderBlockSnowloggedPlant`) override to `return false`.
- **`RenderBlocks.renderItemIn3d(int)`** — replaced the static int-comparison OR-chain with a delegation to `BlockRenderType.get(renderType).handler().renderItemIn3d()`. No call sites need changing; adding a new render type now only requires `BlockRenderHandler.renderItemIn3d()` on its handler.
- **Bug fix — world lighting inverted (items very dark, no day/night on world blocks)** (`WorldProvider.java`): client `generateLightBrightnessTable()` computed the curve inverted (`brightness[i] = (1-i/15)/((1-i/15)*3+1)` → `table[15]=0.05`, `table[0]=0.29`), so full-bright surfaces rendered nearly black and the lightmap was compressed into a 0.05–0.29 range, flattening day/night. The server copy and `WorldProviderHell` still had the correct vanilla orientation (`table[15]=1.0`), confirming a client-only regression. Fixed numerator to `(1 - darkness)` (`= i/15`), matching `WorldProviderHell.java:30` and the server formula; table now maps level 0→0.05 … 15→1.0. Fixes both `getLightBrightness` (hand/EntityItem blocks) and the lightmap (`updateLightmap`) used by world rendering. Client javac exit 0; parity unchanged (809 identical; `WorldProvider.java` already listed among pre-existing substantive diffs from the earlier javadoc rewrite).
- **Bug fix — held / EntityItem blocks & items darker than intended** (entity renderers): NOT a lightmap issue — the lightmap shading is correct. The real cause: hand items (`ItemRenderer.renderItemInFirstPerson`) and EntityItems (`RenderItem.doRenderItem`) were drawn with **GL_LIGHTING still enabled** (directional `LIGHT0`/`LIGHT1` from `RenderHelper.enableStandardItemLighting` → ambient 0.4 + 0.6 diffuse each), which dims the item texture below its intended color. Every other path disables it: world blocks (`RenderGlobal.renderWorld`), inventory items (`RenderItem.drawItemIntoGui` disables for both 3D and flat paths), map path. Mobs/players keep lighting (they look correct). Fix: (1) `RenderItem.doRenderItem` calls `GL11.glDisable(GL11.GL_LIGHTING)` right after `glPushMatrix()` and re-enables before `glPopMatrix()`; (2) `ItemRenderer.renderItemInFirstPerson` disables GL_LIGHTING around the `this.renderItem(...)` held-item call (line ~296) and re-enables after — scoped so third-person mob/player held items (`renderItem` called from `RenderAmazon`/`RenderBiped`/`RenderHuman`/`RenderPlayer`/`RenderTriton`/`RenderWitch`) are unchanged; (3) passed `1.0F` as the block brightness to `renderBlockOnInventory` in `RenderItem` (3D + flat), `ItemRenderer`, `RenderEntityMeatBlock`, `RenderEntityBlockEntity`, `RenderTNTPrimed`, `RenderMinecart` so no skylightSubtracted double-dim remains (precedent: `EntityFX.java:98`). With lighting off, items are shaded by face-shade × lightmap texel — identical to the inventory. Client javac exit 0; all files client-only, parity unchanged.
- **World.java + collaborators — full readability pass** (javadoc, explanatory comments, de-obfuscation): added class javadoc to `World`; renamed the last two obfuscated method names (`func_4085_a` → `getEntityByClass`, `s_func_32005_b` → `shiftScheduledTimes`) and updated their callers (`Minecraft.java:2078`, server `ConsoleCommandHandler` time command ×2); documented ~210 `World` methods (all that lacked javadoc); renamed every obfuscated parameter/local across the whole file (constructors: `iSaveHandler1`→`saveHandler`, `string2`→`saveName`, `worldProvider3`→`worldProvider`, `par4WorldSettings`→`worldSettings`, `world1`→`sourceWorld`; methods: `i1..i8`→`x/y/z/chunkX/chunkZ`, `z*`→descriptive booleans, `par1Entity`→`entity`, `nBTTagCompound`→`nbtTag`, DDA locals in `rayTraceBlocks`, etc.); variable/hash scan confirms **zero** remaining `i1/par1/func_/var1/entity1`-style tokens. Collaborators also de-obfuscated + commented: `BlockTickScheduler` (`entry`, `dueTickCount`, `existingBlockId`, `updateRadius`), `SkylightTracker` (`celestialAngle`, `lightFactor`, `newSkylightSubtracted`), `AtmosphereCalculator` (`brightness`, `daylightFactor`, `r/g/b`, `attenuationStrength` typo fixed), `Explosion` (3 explanatory comments). `EntityQueryService`/`EntityManager` were already clean. Client + server compile ZERO errors; client/server files byte-identical (parity). Applied via 3 sequential method-scoped subagent passes on the client, then mirrored to server.

- **New feature — `BlockSurfaceMoss` (id 171, texture 196) + `WorldGenSurfaceMoss`** (both trees): thin 1/16-tall walk-through carpet (renderType 0 NORMAL, `Material.plants`, `tabDeco`, auto `ItemBlock`). Ground gate = new `Block.canGrowMoss()` hook (base `false`; `true` in `BlockGrass`, `BlockDirt`, `BlockStone`, `BlockStoneBrick`, `BlockRegolith`, `BlockLog`, and new `BlockCobblestone` — cobble 4 + mossyCobble 48 switched to it; `BlockChippedWood` overrides back to `false`; `BlockDirtPath`/`BlockMycelium` extend plain `Block` so stay `false`). Runtime spread in `updateTick` (mushroom-style, air + block-light < 7 + ground; **rate = `MOSS_SPREAD_CREEP_CHANCE` constant, currently 1 in 100**; no spread in `Seasons.SUMMER`). Chunk hook in `ChunkProviderGenerate.populate` after snow: climate prefilter at chunk centre via new `WorldChunkManager.getTemperatureAndHumidityAt(x,z)` (mirrors `loadBlockGeneratorData` curves with local scratch arrays); if humidity > 0.5 && 0.4 < temp < 0.6, attempts = 1 + (int)((h-0.5)*8); anchor requires strict humidity > 0.6 && 0.4 < temp < 0.6, air above, and is placed at `getLandSurfaceHeightValue` + 1; then chain-spreads 2+rand(4) cells (|y' - anchorY| <= 1, ground + air only, no light/season checks at gen time). Client + server compile ZERO errors; parity 825 identical / 40 substantive (unchanged diff set).
- **New feature — per-column biome/temperature/humidity caches persisted with chunk data** (both trees): `Chunk.temperatureCache`/`humidityCache` (`float[256]`, index `x<<4|z`; lazy-seeded via `refreshCaches()`, which now also fills climate from the same `loadBlockGeneratorData` ramp already computed for biomes) + new `Chunk.getTemperatureAt/getHumidityAt` and `setClimateCache(float[256]|double[])`; new `World.getTemperatureAt/getHumidityAt` (chunk-resident → cached; else single-cell `WorldChunkManager.getTemperatureAndHumidityAt`, no generation side effects); new `BiomeGenBase.getBiomeByCode(int)` reverse lookup (lazy map over `biomeList` keyed by the auto-assigned `biomeCode`, fallback `biomeDefault`). Seeding: `ChunkProviderGenerate.provideChunk` harvests the ramp right after the biome-genCache assignment (~line 289); old-save fallback — conditional on `biomeGenCache == null` — recomputes biome + climate in one pass in **both** load paths (`ChunkProvider.loadChunkFromFile` and `ChunkProviderLoadOrGenerate.getChunkAt`). Persistence in `ChunkLoader.storeChunkInCompound`/`loadChunkIntoWorldFromCompound`: three `byte[256]` NBT arrays — `"Biomes"` (biomeCode per cell), `"Temperature"`/`"Humidity"` (quantized `(byte)(int)(v*255)`, decoded `/255.0f`; only `NBTTagByteArray` exists — no float array tag); any missing array (old worlds) → single full recompute on load. `WorldChunkManager.getTemperatureAndHumidityAt` gained a `tempNoise == null` guard; `WorldChunkManagerHell` overrides it to `{temperatureHell, humidityHell}` so off-dimension reads do not NPE. Moss call sites switched to the World API (`ChunkProviderGenerate.populate` prefilter; `WorldGenSurfaceMoss` anchor). Client + server compile ZERO errors; parity 826 identical / 39 substantive (one pre-existing diff normalized by the ChunkProvider copy; all touched files clean in diff list).

### Completed
- **256-height bug fixes (post-implementation)**: (1) **Pitch-black existing worlds** — `ChunkLoader` legacy migration sliced the column-major flat `SkyLight`/`BlockLight` planes as contiguous section blocks (wrong: each subchunk's 16 local-y nibbles of a column are 8 *strided* bytes at `(x<<10|z<<6|s<<3)`), scrambling light; fixed with per-column copies into section planes. Corrupt new-format files already written by the buggy build self-heal via a new `LightVersion` stamp: saves write `LightVersion=1`, subchunk-format loads lacking it relight once (Starlight) + `isModified`. Legacy files (never written by the buggy build) set `hasLightVersion=true` after correct slicing. (2) **Pillaring capped at 128** — `ItemBlock.onItemUse` hardcoded `y == 127` top-layer guard; now `y >= Chunk.SECTION_HEIGHT - 1` (256 analog keeps layer 255 unbuildable). Applied to both trees, byte-identical; client + server compile ZERO errors.

### Active
- **`World.java` decomposition (mirror of `InfdevProject/Main` reference architecture)**: staged extraction of the monolithic 3426-line `World.java` into collaborator classes, applied identically to client and server, one stage at a time, compile + parity-check after each:
  - **Stage 1 DONE — `EntityQueryService`**: new `net/minecraft/game/world/EntityQueryService.java` (instance collaborator holding the two reusable buffers `collidingBoundingBoxes` + `entitiesWithinAABBExcludingEntity` and the 14 spatial/block/entity query methods). `World` keeps all 14 public delegate entry points; the two buffer fields were removed from `World` and its 3 constructors now build `entityQueryService = new EntityQueryService(this)`. Client + server compile with ZERO errors; parity script reports `World.java` + `EntityQueryService.java` identical between trees.
  - **Stage 2 DONE — `EntityManager`**: new `net/minecraft/game/world/EntityManager.java`. `loadedEntityList`/`unloadedEntityList` moved private into the manager (accessed via `getLoadedEntityList()`); cached mob counters added (`mobCount`/`animalCount`/`waterMobCount` tracked on every add/remove via `updateEntityCountOnAdd/Remove`; new public `getCachedEntityCount(Class)` fast-paths `IMob`/`EntityAnimal`/`IWaterMob`). Moved: `spawnEntityInWorld`, `setEntityDead`, `removePlayer`, `getEntityById`, `countEntities`, `addLoadedEntities`, `unloadEntities`, plus new `sweepUnloaded()` (replaces inline unload sweep in `updateEntities`/`updateEntityList`) and `removeEntityFromWorldList(e,index)` (dead-sweep) and `addIfAbsent` (used by `joinEntityInSurroundings`). `obtainEntitySkin`/`releaseEntitySkin` stay as `protected` virtual hooks on `World` (WorldClient/WorldServer overrides still fire via `world.obtainEntitySkin(...)`). `SpawnerAnimals.canSpawnType` now uses `world.getCachedEntityCount(...)`. External direct readers fixed: `MoCTools` (3 reads), `WorldClient.tick` (`getLoadedEntityList().contains(...)`). Client + server compile ZERO errors; parity identical.
  - **Stage 3 DONE — `BlockTickScheduler`**: new `net/minecraft/game/world/BlockTickScheduler.java`. `scheduledTickTreeSet` (TreeSet, time-ordered) + `scheduledTickSet` (HashSet mirror, O(1) dedup) moved private into the scheduler; moved `scheduleBlockUpdate`, `TickUpdates` (→ `tickUpdates`), and `s_func_32005_b` (→ `shiftScheduledTimes`, which also sets the new world time). `World` keeps the three public delegates (`scheduleBlockUpdate`, `TickUpdates`, `s_func_32005_b`); all block call sites (`Block*TNT`, fire, rails, levers, sand, redstone, etc.) and `ConsoleCommandHandler.time` calls are unchanged. `WorldClient` still overrides `scheduleBlockUpdate`/`TickUpdates` as client no-ops (block ticks arrive via server packets). Removed now-unused `TreeSet` import from `World`. Client + server compile ZERO errors; parity identical (`World.java`/`BlockTickScheduler.java` not in diff list).
  - **Stage 4 DONE — `SkylightTracker`**: new `net/minecraft/game/world/SkylightTracker.java`. The non-Starlight lighting bookkeeping that `World` still owned — the `skylightSubtracted` day/night counter (0 at noon … 11 at night, further dimmed by rain/thunder) and its maintenance — moved into a collaborator: field private + `getSkylightSubtracted()`/`setSkylightSubtracted()` plus `calculateSkylightSubtracted(float)` and `updateSkylightSubtracted(float)` (compute-and-store-if-changed). `World` keeps public delegates `getSkylightSubtracted`/`setSkylightSubtracted`/`calculateSkylightSubtracted` (callers: `WorldClient.tick`, `ZanMinimap`) and `calculateInitialSkylight`; `tick()` now calls `skylightTracker.updateSkylightSubtracted(1.0F)`. External direct field readers/writers converted to accessors: `EntityMob.getCanSpawnHere` (temp darken save/restore), `ChunkCache.getLightValue`, `CanvasIsomPreview.loadWorld`/`setTimeOfDay`, `WorldClient.tick`. Removed dead old-lighting leftovers: the two commented-out code blocks (weather-dimmer variant in `calculateSkylightSubtracted`, `updateAllRenderers` loop in `tick()`). Client + server compile ZERO errors; parity identical (`World.java`/`EntityMob.java`/`SkylightTracker.java` not in diff list; `ChunkCache` stays in its pre-existing import-only slot).

- **Stage 5 DONE — `AtmosphereCalculator`**: new `net/minecraft/game/world/AtmosphereCalculator.java` (both trees). The sky/fog/cloud colour + sun/star brightness helpers moved into a **static** helper: `getSunBrightness(World,float)`, `getStarBrightness(World,float)`, `getSkyColor(World,float)`, `getCloudColor(World,float)`, `getFogColor(World,float)`. `World` keeps the five public delegates as one-line static calls, so all callers (`EntityRenderer.1157/1161/1191`, `RenderGlobal.692/726/732/776/798/836/901`, `WorldProvider.186/234` getSunBrightness, `SkylightTracker.getCelestialAngle`) are unchanged. Preserved `Seasons`/`colouredAthmospherics` overrides exactly (sky colour = `Seasons.getSkyColorForToday()` when `colouredAthmospherics`, else fixed `0x88BBFF`; `getFogColor` still delegates to `worldProvider.getFogColor(angle, tick, isBloodMoon(), colouredAthmospherics)`). The per-instance `cloudColour` field (only ever set to white `16777215L`, never mutated) became a `private static final long CLOUD_COLOUR` and its 3 constructor inits were removed from `World`. Removed the dead commented-out legacy rain/thunder sky-dimming block inside `getSkyColor`. Static class is `final` with a private constructor. Client + server compile ZERO errors; parity identical (`World.java`/`AtmosphereCalculator.java` not in diff list).
  - **Stage 6 DONE — `Explosion`**: JSON parsing, `MapGenCity` etc. — the `Explosion` class was already a standalone collaborator (`net/minecraft/game/world/Explosion.java`, identical in both trees) committed pre-session, and the four factory methods on `World` (`createExplosion`, `createBlockExplosion`, `newExplosion`, `newBlockExplosion`) were already thin delegates. Remaining work was a readability pass on the factories bound to the decomposition: renamed the obfuscated params (`entity1/d2/d4/d6/f8/z9` → `exploder/x/y/z/radius/flaming`, `explosion10` → `explosion`) and added javadoc; behavior unchanged. `WorldServer` overrides of `newExplosion`/`newBlockExplosion` (which broadcast `Packet60Explosion` to nearby players and call `doEffects(false)`) are untouched, per plan. Callers (`BlockBed`, `EntityTNTPrimed`, `EntityCreeper`, `EntityElementalCreeper`, `EntityExplodingZombie`, `EntityFireball`) unchanged. Client + server compile ZERO errors; client/server `World.java` + `Explosion.java` byte-identical.
  - Decomposition stages 1-6 COMPLETE.
  - Not in scope: save/load extraction, `tick()` reordering, aggressive API renaming (API churn OK only if game keeps working).

- **`net.minecraft.client.gui` package cleanup**: partially done — `GuiScreen`, `GuiSlot`, `GuiSlotStats*`, `SorterStats*`, `ScaledResolution` done; remaining GUI files: `GuiButton`, `GuiTextField`, `GuiSlider`, `GuiIngame`, `GuiIngameMenu`, `GuiMainMenu`, `GuiCreateWorld`, `GuiWorldSelection`, `GuiControls`, `GuiOptions`, `GuiVideoSettings`, `GuiChat`, `GuiInventory`, `GuiContainer*`, `GuiDispenser`, `GuiCommandBlock`, `GuiSleeping`, `GuiErrorScreen`, `GuiDisconnected`, `GuiConnecting`, `GuiConflictWarning`, `GuiGameOver`, `GuiWinScreen`, `GuiCredits`, `GuiAchievements`, `GuiStats`, `GuiBeacon`, `GuiHopper`, `GuiBrewingStand`, `GuiChest`, `GuiCrafting`, `GuiEnchantment`, `GuiFurnace`, `GuiRepair`, `GuiMerchant`, `GuiBook`, `GuiScreenHorseInventory`, `GuiYesNo`, `GuiTextField`, `GuiParticle`, `FontRenderer`.

### Blocked
- `(none)`

## Next Move
1. **Implement 256-block world height** per the finalized plan in `docs/256_blocks_high.md` (10-step Implementation Order, Starlight adaptation for the light engine). Task is validated & ready; awaits user go-ahead.
2. When the 256-height task is deferred or done, continue **`net.minecraft.client.gui` package cleanup**: process `GuiButton.java`, `GuiTextField.java`, `GuiSlider.java`, `GuiIngame.java`, `GuiIngameMenu.java`, `GuiMainMenu.java`, and all remaining GUI files.
3. Then move to `net.minecraft.client.render` package.
4. Then `net.minecraft.client.model`, `net.minecraft.client.sound`, `net.minecraft.client.particle`, `net.minecraft.client.effect`, `net.minecraft.client.controller`, `net.minecraft.client.player`.

## Code Conventions

- All classes (core and third-party mods) are in `net.minecraft.*` packages
- Server-side code mirrors client structure in `minecraft_server/`
- Some classes/methods made public for cross-package access (original Minecraft had package-private access)
- Common/shared code goes in `net.minecraft.game.*` (or `net.minecraft.network.*` for shared network)
- Client-only code (rendering, GUI, models) goes in `net.minecraft.client.*`
- Server-only code goes in `net.minecraft.server.*`
- Subpackages: `entity.animal`, `entity.monster`, `entity.player`, `entity.projectile`, `entity.ai`, `world.biome`, `world.terrain.generate.tree`, `world.terrain.generate.structure`, `gui.container.creativetab`
- **`mc` field convention**: `GuiScreen` subclasses use `this.mc` to reference the parent `Minecraft` instance (NOT renamed to `minecraft` to avoid cascading changes across 50+ subclasses). Classes that control their own lifecycle (`GameSettings`, `LoadingScreenRenderer`) renamed to `minecraft`.

