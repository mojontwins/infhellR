# Special Beds — Lunar Wand sleep mechanic

Post-mortem for the "enchanted bed" feature. Implemented across client
(`src/minecraft`) and server (`src/minecraft_server`) mirrors; all touched
shared files remain byte-identical between the two trees.

## Design

- **Plain beds no longer skip the night.** Sleeping in a normal bed lays the
  player down, fades the screen and shows the MP-style *Leave Bed* screen
  (`GuiSleepMP`, now also in single-player), but the world keeps ticking and
  dawn arrives normally. A plain bed does **not** set the respawn point.
- **Enchanted beds skip the night.** A bed whose metadata has bit `16`
  (`BlockBed.META_SLEEPABLE`) behaves like a vanilla bed: when all players are
  asleep in enchanted beds, `World.tick()` jumps the time to the next dawn,
  spawns are skipped, and the player's respawn point is set.
- **Lunar Wand** enchants a bed. Right-clicking an unenchanted bed with the
  wand sets bit `16` on both halves, plays the crying-obsidian style effects
  (7 `largesmoke`, `random.drr`, `random.breath`) and unlocks the `magicBed`
  achievement.
- **Enchanted beds glitter.** An enchanted bed periodically emits a `glowdust`
  particle (`BlockBed.randomDisplayTick`), the same particle used by
  `EntityGlowdustFX`.
- **Breaking an enchanted bed destroys the enchantment** — it always drops a
  plain bed (item damage `0`).

## Recipe

The wand takes one music record. Because only two records exist, two shaped
recipes were registered (`RecipesMine.addRecipes`), one per record:

```
" Dg"
" dD"
"G  "

D = Block.blockDiamond
g = Block.glowStone
G = Block.blockGold
d = Item.record13  (recipe 1)  /  Item.recordCat  (recipe 2)
```

Output: `Item.lunarWand` (id `1033`).

## Achievements

All four already existed in `AchievementList` with lang keys; three had no
trigger and were wired up here:

| Achievement     | Trigger                                                              |
|-----------------|----------------------------------------------------------------------|
| `lunarWand`     | Craft the wand — `SlotCrafting.onPickupFromSlot`                     |
| `magicBed`      | Enchant a bed — `ItemLunarWand.onItemUse`                            |
| `hireAmazon`    | Tame an Amazon — `EntityAmazon` (replaces the old `currency` grant)  |
| `dressedInRags` | Full rag set (cap + shirt + trousers) — `EntityPlayer.onUpdate`      |

`dressedInRags` uses a new `wearingFullRags()` helper (all three pieces). It
does **not** change the pre-existing `dressedInRags()` partial-set cold bonus,
which still accepts helmet + (plate **or** legs).

## Changes by file

### `game/world/block/BlockBed.java` (client + server)
- `blockActivated`: resolve the clicked half to the foot half **before** the
  `isRemote` short-circuit. If the held item is `Item.lunarWand` and the bed is
  not yet sleepable, `return false` so the item's `onItemUse` handles the
  interaction (this is required on both sides by
  `blockActivated(...) && shift != 1 ? true : itemStack.useItem(...)`).
  Removed the old `if(!isBedSleepable(i6)) return false;` refusal, so any bed
  can now be slept in (skip is gated later in `World`).
- `damageDropped`: always returns `0` (never preserves the enchant on drop).
- `randomDisplayTick`: new; emits `glowdust` for sleepable beds.

### `game/entity/player/EntityPlayer.java` (client + server)
- New `isPlayerSleepingInEnchantedBed()` — sleeping and the foot-block metadata
  has `META_SLEEPABLE`.
- Dawn auto-wake now passes
  `this.isPlayerSleepingInEnchantedBed()` as `setSpawn`, so a plain bed never
  overwrites the respawn point at dawn.
- New `wearingFullRags()`; `onUpdate` (`!isRemote`, every 20 ticks) triggers
  `dressedInRags`.

### `game/world/World.java` (client + server)
- `isAllPlayersFullyAsleep()` now requires each player to be
  `isPlayerFullyAsleep() && isPlayerSleepingInEnchantedBed()`. This is the
  single gate that decides whether `World.tick()` skips to dawn, so plain beds
  simply never satisfy it.

### `game/item/ItemLunarWand.java` (client + server)
- Implemented `onItemUse`: verify the target is a bed, compute the partner half
  from `BlockBed.headBlockToFootBlockMap`, OR `META_SLEEPABLE` into both
  metadata values via `setBlockMetadataWithNotify`, emit the effects, and
  trigger `magicBed`. `displayOnCreativeTab = CreativeTabs.tabTools`.

### `game/item/Item.java` (client + server)
- `lunarWand` is now `new ItemLunarWand(1033)...` instead of a plain `Item`.

### `game/item/recipe/RecipesMine.java` (client + server)
- Added the two wand recipes next to the crying-obsidian-wand recipe.

### `game/container/SlotCrafting.java` (client + server)
- Added a `Item.lunarWand` branch granting `lunarWand`.

### `game/world/terrain/generate/feature/amazonvillage/EntityAmazon.java` (client + server)
- Taming now triggers `AchievementList.hireAmazon` instead of `currency`.

### `server/NetServerHandler.java`
- `handleEntityAction` state `3` (client asks to leave bed) now passes
  `this.playerEntity.isPlayerSleepingInEnchantedBed()` as `setSpawn`, so a
  manual leave from a plain bed does not set respawn.

### `client/Minecraft.java`
- Removed `&& this.theWorld.isRemote` from the auto screen-switch, so
  `GuiSleepMP` is shown in single-player as well.

### `client/gui/GuiSleepMP.java`
- Added `doesGuiPauseGame() => false` (matches `GuiScreen`; the world must keep
  ticking for the enchanted-bed skip and for plain-bed dawn).
- `func_22115_j()` (Leave Bed) now falls back to a local
  `wakeUpPlayer(false, true, isPlayerSleepingInEnchantedBed())` when the local
  player is not an `EntityClientPlayerMP` (i.e. single-player).

## Timing / sync notes

- The enchant is written on both halves with
  `setBlockMetadataWithNotify`, which propagates to MP clients through
  `Packet53BlockChange`.
- `worldObj.isRemote` is `false` in single-player (SP uses a plain `World`, not
  an integrated server), so `isAllPlayersFullyAsleep()` runs there.
- Because `SleepManager`/skip logic is server-authoritative, MP clients predict
  the metadata change locally in `ItemLunarWand.onItemUse` and the server
  reconciles it.

## Known caveats / follow-ups

- **`hireAmazon` parent chain.** `hireAmazon`'s parent is `currency`, whose only
  grant was the Amazon-taming code just replaced. If `currency` is never
  obtained elsewhere, the hire-Amazon toast may not display even though the
  stat is granted. Accepted intentionally during design; revisit if the toast
  not appearing becomes a problem.
- **Not compiled in this checkout.** `build.bat` is hardcoded to
  `D:\Cosas\InfHell\infhell-release` and the `lib/` jars are absent from this
  working tree, so a `build.bat` / javac run could not be executed here.
  Compile on the real build machine (or in Eclipse) before shipping.
  Parity was instead verified by MD5 across the client/server mirrors
  (all touched shared files match; `EntityPlayer.java`'s two-blank-line diff is
  pre-existing).

## Manual test checklist

1. Craft the wand using each record variant → `lunarWand` toast.
2. Right-click an unenchanted bed with the wand → effects, glowdust starts,
   `magicBed` toast.
3. Sleep in a plain bed → lay down, *Leave Bed* screen, no night skip, no
   respawn change; leave with the button / Esc.
4. Sleep in an enchanted bed → night skips, respawn set, wake at dawn.
5. Break an enchanted bed → drops a plain bed (no glowdust when replaced).
6. Tame an Amazon → `hireAmazon`.
7. Equip full rags → `dressedInRags`.
