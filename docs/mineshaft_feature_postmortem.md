# Mineshaft → Feature postmortem

Reimplemented the released-vanilla mineshaft (`structure.mineshaft.*`) as a
Feature for the `FeatureProvider` system. The original hooks are unhooked but
not deleted, so a regression can be reverted by uncommenting two lines.

## What was built

New self-contained package `net.minecraft.game.world.terrain.generate.feature.mineshaft`:

| File | Role |
|------|------|
| `FeatureMineshaft` | Feature entry point: gate, `setup`, two-phase `generate`/`populate`. |
| `MineshaftWriter` | Shared block-access layer; flat-buffer writes during generate, `Chunk.setBlockIDWithMetadata`/`World` during populate. |
| `MineshaftComponent` | Abstract piece: box/offset bookkeeping + fill/decoration/special helpers. |
| `MineshaftComponentRoom` / `Corridor` / `Cross` / `Stairs` | The four piece types. |
| `MineshaftPieces` | Piece factory and loot table. |

- `FeatureMineshaft extends Feature` (not `FeatureDynamicSchematic`): the whole layout
  is planned as a handful of piece objects, so there is no large schematic to hold.
- `isGeneratedAfterCaves() == true`: carving runs first, the shaft overwrites it.
- `getFeatureRadius()` returns the new shared `FeatureProvider.MAX_FEATURE_RADIUS` (8).
- Spawn gate: `rand.nextInt(80) < max(abs(cX), abs(cZ))`, with `getSpawnChance()` =
  `MAXCHANCE / 100`, mirroring the vanilla 1-in-100 roll plus the distance curve.

## Key decisions

1. **Shared `MAX_FEATURE_RADIUS`.** The provider's two scans were hardcoded to a ring
   of 3. Mineshaft corridors can reach ~7 chunks from the origin room, so the ring was
   made a public constant (`8`). Scan cost rises from 84 to 1938 loop iterations per
   chunk; the negative verdict cache (`noFeatureChunks`) keeps the *gate* evaluations at
   one per distinct cell.

2. **Two-step faithful-ish split.** `setup` plans the deterministic skeleton with no
   world reads (room, corridors, crosses, stairs, plus the vertical `markAvailableHeight`
   shift). `generate` lays the normal/shell blocks straight into the chunk's flat
   generation buffers during the after-caves pass. `populate` resolves the
   world-dependent decor (arch vs. hanging chain) and the specials (chests, spider
   spawner, torches, webs, rails).

3. **Per-piece seeds.** Each piece draws from `new Random(pieceSeed(piece))` in both
   phases, so a piece writes identical blocks no matter which chunk (or how many times)
   it is asked to draw for. The vanilla behavior of sharing one RNG stream across all
   components of a shaft is intentionally dropped.

## Subtleties worth remembering

- **Coordinate conventions are inconsistent in the source.** `Room` and `Cross` leave
  `coordBaseMode = -1`, so their offset helpers are the identity and they pass
  *absolute* coordinates; `Corridor` and `Stairs` set `coordBaseMode` and pass
  *relative* coordinates. The port preserves this exactly. (Original `ComponentMineshaftCross`
  never assigns `coordBaseMode`, which is why its absolute coordinates work.)
- **Chunk storage changes between phases.** `chunk.blocks`/`chunk.data` are the flat
  256-high buffers during generate (index `x<<12 | z<<8 | y`); after `loadFlatBlocks`
  they are null and writes must go through `setBlockIDWithMetadata`.
- `world.getChunkFromChunkCoords` is only used in populate (chunk resident); generate
  never touches `World`.

## Unhooked hooks

`ChunkProviderGenerate`:
- ~line 420: `mineshaftGenerator.generate(...)` commented.
- ~line 992: `mineshaftGenerator.generateStructuresInChunk(...)` commented.
- `mineshaftMesaGenerator` and `strongholdGenerator` left intact.

## Verification

- Client and server copies byte-identical.
- Both compile clean with the raw `javac` lines from `build.bat` (the script itself
  ends with `pause`, so it is unsuitable for automated checks).
- **Not yet tested in-game** — pending user confirmation.
