# Entity Activation Range — Design Document

## Problem

`World.updateEntities()` ticks every entity in `loadedEntityList` every server tick,
regardless of distance from any player. The expensive parts — AI tasks, pathfinding,
`moveEntity` AABB collision, entity-entity body push — run for all of them.

Blocks already have a player-proximity filter in `updateBlocksAndPlayCaveSounds()`:
only chunks within an 8-chunk square of each player are ticked. Entities have no
equivalent.

**Goal:** only tick entities (AI, movement, collisions) within a configurable radius
of each player. Frozen entities receive only `ticksExisted` incremented, plus an
isolated age-based despawn pass to prevent mob cap starvation.

---

## Current entity lifecycle — relevant pieces

| Method | What it does |
|---|---|
| `Entity.onUpdate()` | Calls `onEntityUpdate()` |
| `Entity.onEntityUpdate()` | Increments `ticksExisted`, updates prevPos, handles fire/water/fall |
| `EntityLiving.onEntityUpdate()` | Calls `super`, then updates hurt timers, deathTime, statusEffects, air |
| `EntityLiving.onUpdate()` | Calls `super.onUpdate()`, then `onLivingUpdate()` |
| `EntityLiving.onLivingUpdate()` | Handles `newPosRotationIncrements` interpolation, body-collision push, AI dispatch, jump, movement |
| `EntityLiving.updateAITasks()` | Calls `despawnEntity()`, target/task/navigator/jump/look AI |
| `EntityLiving.updateEntityActionState()` | Increments `entityAge`, calls `despawnEntity()`, selects random walk |
| `EntityMob.onLivingUpdate()` | Adds `entityAge += 2` when in bright light, then `super` |
| `EntityCreature.updateEntityActionState()` | Pathfinding + attack logic; only falls back to `super` (which calls `despawnEntity`) when no path exists |

**Despawn** lives at `EntityLiving.despawnEntity()` (protected):

```java
// line 956
protected void despawnEntity() {
    EntityPlayer player = worldObj.getClosestPlayerToEntity(this, -1.0);
    if (!canDespawn() || player == null) return;
    double distSq = ...;
    if (distSq > 16384.0)  setEntityDead();        // >128 blocks: instant
    if (entityAge > 600 && rand.nextInt(800) == 0) {
        if (distSq < 1024.0)  entityAge = 0;        // <32 blocks: reset
        else                  setEntityDead();        // 32-128 blocks: age-based
    }
}
```

`canDespawn()` returns `true` by default; overridden to `false` for tamed wolves,
tamed ocelots, EntityAmazon, EntityHumanBase (pirates etc.), and guarded by
`EntityHumanBase.despawn` flag.

**Spawn cap** (`SpawnerAnimals`): `computeMaxEntities` uses only the radius-8
eligible-chunk set, but `canSpawnType` calls `world.countEntities(class)` across
**all** loaded entities — so frozen lingering mobs in the 128-288 block ring inflate
the count and starve near-player spawns.

---

## Proposed design

### Step 1 — Add simulation radius constant

`World.java` — new field:

```java
public int entitySimulationRadiusChunks = 8;
```

Square Chebyshev distance, matching block-tick and spawner radius.

---

### Step 2 — Build active chunk set per tick

Top of `World.updateEntities()`, before the entity loop:

```java
HashSet<Integer> activeChunks = new HashSet<>();
int radius = this.entitySimulationRadiusChunks;
for (int p = 0; p < this.playerEntities.size(); ++p) {
    EntityPlayer player = (EntityPlayer) this.playerEntities.get(p);
    int cx = MathHelper.floor_double(player.posX / 16.0);
    int cz = MathHelper.floor_double(player.posZ / 16.0);
    for (int dx = -radius; dx <= radius; ++dx) {
        for (int dz = -radius; dz <= radius; ++dz) {
            activeChunks.add(ChunkCoordIntPair.chunkXZ2Int(cx + dx, cz + dz));
        }
    }
}
```

Cost: O(players × radius²) per tick. With 8 chunks: 17×17 = 289 inserts per player.
Negligible.

---

### Step 3 — Gate entity updates in the main loop

`World.java` lines 1567–1592 (the `loadedEntityList` iteration):

```java
for (i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
    entity2 = (Entity) this.loadedEntityList.get(i1);

    // Riding-entity validity check — unchanged
    if (entity2.ridingEntity != null) {
        if (!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2)
            continue;
        entity2.ridingEntity.riddenByEntity = null;
        entity2.ridingEntity = null;
    }

    if (!entity2.isDead) {
        boolean active = entity2 instanceof EntityPlayer
            || activeChunks.contains(
                ChunkCoordIntPair.chunkXZ2Int(entity2.chunkCoordX, entity2.chunkCoordZ));

        if (active) {
            this.updateEntity(entity2);       // full tick (unchanged)
        } else {
            // FROZEN — ticksExisted only, plus isolated despawn
            entity2.ticksExisted++;
            tickDespawnOnly(entity2);
        }
    }

    // Dead-entity cleanup — unchanged
    if (entity2.isDead) { ... }
}
```

---

### Step 4 — Isolated despawn pass

New private method on `World`:

```java
private void tickDespawnOnly(Entity entity) {
    if (entity instanceof EntityLiving && !entity.isDead && !this.isRemote) {
        EntityLiving living = (EntityLiving) entity;
        ++living.entityAge;
        living.despawnEntity();
    }
}
```

This replicates the exact logic from `EntityLiving.updateEntityActionState()` line
978 (`++entityAge; despawnEntity()`). All existing distance, age, and `canDespawn()`
overrides remain intact — tamed wolves, pirates, amazons etc. are unaffected.

The `!this.isRemote` guard ensures this only runs server-side; the client never
kills entities — it receives destroy packets from the server.

**What this achieves:** a frozen mob at >128 blocks is instantly despawned (existing
behavior, now also runs for frozen mobs). A frozen mob at 32–128 blocks despawns
when `entityAge > 600` — the exact same threshold vanilla uses. Spawn cap is freed.

---

### Step 5 — Expose `despawnEntity` for `World` to call

`EntityLiving.despawnEntity()` is currently `protected`. Two options:

| Option | Change |
|---|---|
| **(A) Minimal** | Make `despawnEntity()` `public` |
| **(B) Safer** | Add `public void performDespawn()` that delegates to `despawnEntity()`, leave original `protected` |

---

## What freezes vs. what stays active

| Component | Frozen? | Notes |
|---|---|---|
| `ticksExisted` | ✅ Incremented | Exactly as requested |
| `despawnEntity` | ✅ Runs (isolated) | Server-side only, preserves all `canDespawn()` overrides |
| AI tasks / pathfinding | ❌ Skipped | Main CPU win |
| `moveEntity` AABB collisions | ❌ Skipped | Main CPU win |
| Entity-entity body push | ❌ Skipped | Part of `onLivingUpdate` |
| Fire / fall / suffocation | ❌ Skipped | Burning mobs freeze mid-burn, resume on approach |
| Potion status effects | ❌ Skipped | Duration frozen |
| Water/lava handling | ❌ Skipped | — |
| `newPosRotationIncrements` interpolation | ❌ Skipped | Position snapshot frozen in place |
| Weather effects (falling sand, arrows) | ✅ Always ticked | Separate `weatherEffects` loop, never filtered |
| Players | ✅ Always ticked | Excluded from filter via `instanceof EntityPlayer` |
| Tile entities | ✅ Always ticked | Gated separately by chunk-tick system |
| Riding/ridden linkage | ✅ Validated | Same as today; valid mounts stay linked |

**Render correctness:** `prevPosX/Y/Z` is not updated for frozen entities, so
`prev == current` → rendered in place with no interpolation pop. When the player
approaches and the entity resumes, prevPos catches up naturally.

---

## Server vs. client

Apply the filter in the shared `World.updateEntities()` so both benefit:

- **Server:** authoritative; runs despawn pass; tick budget freed.
- **Client:** entities ticked by server packets; frozen client entities still render
  correctly (prevPos held); despawn is `!isRemote`-gated so client never kills.

---

## Edge cases

**Mounted/jockey pairs (skeleton riding spider):**
If the spider is outside the sim radius, both are frozen together. The spider stops
ticking; the skeleton riding it stops ticking (both rely on `onUpdate` chain). When
the player approaches, both resume. No desync because movement is coupled via
`updateRidden()`.

**Burning frozen mobs:**
Fire decrement is in `Entity.onEntityUpdate()`. Frozen path skips `onEntityUpdate`
entirely, so fire never ticks down. A mob frozen mid-burn stays on fire indefinitely
until the player approaches and resumes it. Acceptable — the mob is "paused."

**Tamed wolves / ocelots (`canDespawn() = false`):**
They are never killed by `despawnEntity()` regardless of distance. If they wander
beyond 128 blocks from all players, they freeze indefinitely (never despawn, never
tick). This matches your intent — tamed animals persist until chunk unload.

**Items (`EntityItem`) — needs a decision:**
Frozen items (ground drops) won't age. They persist at the 8–18 chunk ring until
chunks unload at 288 blocks. Two options:
- **(a) Leave frozen:** simplest; items are rare in the band in practice.
- **(b) Standalone item despawn:** increment an age counter, kill when `> 6000`.
  Requires adding a small check in `tickDespawnOnly`.

**EntityAmazon / pirate mobs (`canDespawn() = false`):**
Same as tamed wolves — they persist frozen indefinitely. Their AI was not ticking
in the frozen band anyway before this change (they only tick when players are
near). No behavioral change.

---

## Files to modify

| File | Change |
|---|---|
| `src/minecraft/net/minecraft/game/world/World.java` | Add `entitySimulationRadiusChunks` field; add `activeChunks` set + filter in `updateEntities()`; add `tickDespawnOnly()` private method |
| `src/minecraft/net/minecraft/game/entity/EntityLiving.java` | Make `despawnEntity()` public (option A) or add `performDespawn()` wrapper (option B) |
| `src/minecraft_server/net/minecraft/game/world/World.java` | Identical changes (client/server parity) |

No changes to `Entity`, `EntityCreature`, `EntityMob`, or any subclass — the gate
is entirely in `World.updateEntities()`, keeping the change minimal and reversible.

---

## Open questions

1. **Radius value:** default `8` (matches block ticks / spawner / your original
   proposal) — or do you want it configurable in-game? At 8, frozen mobs are
   invisible to the player if view distance ≤ 8 chunks; at higher render distances
   you'd see stock-still mobs at the edge.

2. **Item despawn:** option (a) or (b) from the edge cases section?

3. **128-block instant despawn:** `despawnEntity()` already kills mobs instantly at
   >128 blocks. All frozen entities are >128 blocks from every player, so they die
   on the very tick they freeze — identical to current vanilla behavior. You
   mentioned wanting pure age-based (softer fade-out). Do you want to keep the
   existing instant-kill, or relax it for frozen mobs specifically (age-only, no
   distance instant-kill)?

4. **Spawn chunk protection:** vanilla has a hard-coded 128-block no-despawn-radius
   around spawn. Currently `despawnEntity` does not enforce this — only the
   chunk-load eviction zone does. Not affected by this change, just noting.

---

## Verification

After implementation:

1. **Compile check:** `javac` both client and server sources (see `AGENTS.md` for
   exact commands). Zero errors.
2. **Entity freeze test:** spawn mobs, walk away beyond 8 chunks, return — mobs should
   be in the same position/state as when you left.
3. **Despawn test:** walk far enough that chunks outside 8 chunks but inside 288
   blocks unload; confirm mobs in the ring despawn on a timed basis, not lingering
   forever.
4. **Spawn cap test:** move far from an area with many mobs, wait, confirm new mobs
   can still spawn nearby (caps are not starved by frozen entities).
5. **Tamed wolf test:** tame a wolf, walk away >8 chunks — wolf should freeze in
   place, not despawn, resume normally on return.
6. **Performance test:** compare tick timing with ~200 loaded entities, half outside
   the sim radius. Expect measurable reduction in `updateEntities` tick cost.
