# Step 1

Fix as much mappings as possible in the client.

# Step 2 

Manually merge client - server

# Step 3

- Remove unwanted beta gameplay items such as beds
- Remove unwanted blocks
- Remove all stats

## Going backwards

List of stuff I will soft-lock. More stuff I WONT soft-lock is not included.

- b1.7 added shears, pistons and silverfish. Flint & Steel is required to ignite TNT.
- b1.6.6 changed glowstone from glass to stone. 
- b1.6 added trapdoor, maps. Fire was nerfed!
- b1.5 added birch & spruce saplings, cobwebs, detector & powered rails.
- b1.4 added cookies & wolves. Changed the minecraft logo.
- b1.3 added beds & redstone repeaters, cobblestone & wooden & sandstone slabs.
- b1.2 added cake, dispensers, lapislazuli block, lapislazuli ore, note block, sandstone, spruce & birch wood, spruce & birch leaves, coloured wool, bones, bone meal, lapis lazuli, cocoa beans, ink sac, charcoal, dyes, sugar, milkable squids, furnace top texture.
- a1.2.0 added netherrack, soul sand, glowstone, carved pumpkin, jack'o'lantern, nether portal, clock, glowstone dust, cooked & raw fish, ghasts, pigmen, biomes, the nether. Changed terrain generation.
- a1.1.2_01 is our target!

The big step is getting to a1.1.2_01, so this is step 4:

# Step 4

* [X] Replace all biomes with a default biome with parametrized population.
* [X] Remove colourizers.
* [X] Creative mode.
* [X] Fly in creative. Faster than walking. CTRL for speed boost.
* [X] Crafting recipes. 
* [X] New level options: game mode, cheats, crafting recipes.
* [X] Creative mode in the server.
* [X] Port tp/time set/etc to the server.

# Step 5: QOL BASIC stuff before archiving as "all purpose alpha"

* [X] Add face coordinates detection on click.
* [X] Make sure they are sent over to the server in the Packet.
* [X] Add colourizer for whool (free texture space).
* [X] Horizontal logs support. Needs two bits so it allows for 4 wood variants + renderer.
* [X] Change "stay" logic for plants to "canGrowPlants"
* [X] Change "stay" logic for shrooms to "canGrowMushrooms"
* [X] Cache biomes in chunks.
* [X] Remove squid generation.
* [X] Remove sunrise/sunset sky colors.
* [X] Integrate altered status system.

# Step 6: Extras

* [X] Check populate in ChunkProviderSky.
* [X] Make bottom of the world kill you in ChunkProviderSky.
* [X] Bug? Sometimes sky is insta-selected?

# Step 7: Blocks + Items + Block renderers + related

Add all blocks and items, and renderers, and needed files. Don't remember that TF blocks will be added later.

* [X] Blocks.
* [X] Items.
* [X] Block renderers.
* [X] Recipes.
* [X] 100s of miscelaneous stuff

# Infhell2 r0.1 @ this point

Features:

* Sky world type!
* Alpha in beta! Buggy noise generators!
* Therefore... Monoliths!
* Crafting manager!
* Cheats & creative mode with flying!
* CTRL to fly faster!
* All infhell new blocks & recipes!
* Silk touch gold tools!
* Crying obsidian to set your spawn point!
* No sleeping!
* Tons of stuff you won't be able to use (yet)!
* Tons of inner workings you won't experience (yet)!
* Working server! That's why I ditched the original Infhell and started over!

# Step 8: Biomes

* [X] All world generators.
* [X] Biome generators.
* [X] Select biome!
* [X] Terrain modifiers.
* [X] Ravines
* [X] They work in the sky world type.
* [X] Support for armored vanilla mods.

# Infhell2 r0.2 @ this point

* Biomes!
* Secret biomes!
* Dark biomes!
* Biomes in sky worlds!
* Mooshrooms!
* Optimizations!
* Tons of stuff you won't be able to use (yet)!
* Tons of inner workings you won't experience (yet)
* Did I mention Monoliths?

# Step 9: Entities + fixes

* [X] Make black duck hostile. 
* [X] (forgot to) remove fancy grass.
* [X] Spawn all over 8K coordinates.
* [X] Upside down slabs & stairs.
* [X] Which work in SMP.
* [X] All non-mod entities.
* [X] Mod entities.
* [X] Bigger caves generator.
* [X] Bit depth fix for AMD cards.
* [X] Better biome surfaces.
* [X] Wood chippable, hit grass with shovel to make paths
* [X] Silk touch gold tools

# Infhell2 r0.3 @ this point

* Enhanced biome surfaces!
* Lots of entities which you won't see (yet)!
* Find a new way to get food in the desert!
* Upside down slabs & stairs!
* Bigger caves!
* Chip the bark of a trunk with your axe!
* Make dirt paths using a shovel!
* Silk touch gold tools 'cause I love NSSS!

# Step 10: Weather & Seasons!

* [X] Better rapid click support in creative mode. Investigate.
* [X] Armored mobs in dungeons w/tiers
* [X] Custom arrows work in multiplayer? Use normal arrows!
* [X] Weather 
* [X] Seasons
* [X] Witches? plus huts, ofc

Weater: 
	- separate rain, snow, thunderstorms.
	- renderer decides which to rain calling Weather class.
	- rain, snow, thunderstorms correctly stored
	- SMP sends rain, snow, thunderstorms status
	- Snow covers in snow / freezes

Witches:
	- Start with a skeleton. 
	- Get it to render.

Seasons:
	- Time passes.
	- Celestial angle.

Witches:
	- Coded from scratch!
	- Have inventory. When inventory off, stop attacking.
	- Refill inventory on random
	- Hut https://cubical.xyz/project/WevN

- Armored zombies and skeletons in dungeons!
- Slightly better dungeons!
- Seasons! Get cold in winter!
- Biome & seasons based Weather!
- Nasty mobs lurk in the mangrove swamps! 
- Lots of fixes!
- Try the seeds Krakatoa & Perfido!


# Step 11: Cities & highways

* [X] Cities.
* [X] Infinite Highway.
* [X] Mineshafts.
* [X] Make "structures off" turn vanilla structures off.
* [X] Add a "no cities" option for new worlds.
* [X] Both above options in SMP via server.properties.
* [X] Fix mineshafts in the Sky world type (ugly) (what do?)
* [/] Freezing if difficulty = normal or hard.
* [ ] Make sure witch mansions *DO* apear albeit seldomly

Witch homes https://cubical.xyz/project/wywM
Graveyards https://cubical.xyz/project/MeWr

- Cities!
- The Infinite Highway!
- (slightly prettier) Mineshafts!
- Coloured fog & sky depending on seasons!
- Cats!
- Polished all world generators for the Sky world type!
- Model Renderer from early release for better betterness!
- Lots of bugfixes!
- Try the seed Witch!

# Step 12: Map features.

* [X] Re-add pistons but with teh original mod capabilities?
* [X] Options to turn coloured fog/sky off.
* [/] Add new achievements.
* [X] Hollow hills + Benimatic entities.
* [X] Slime caves.
* [X] Think of a new feature. Amazon village? Need to add "tameable" amazons.

I need an abstraction to draw offsetted columns of RLE tiles in a chunk, with clipping.

(XxZxY)
BuildingAmazonHouse1_9x9: https://cubical.xyz/project/jPDb
BuildingAmazonHayDepostit_5x7x5: https://cubical.xyz/project/XnD3
BuildingAmazonWell_5x5 x5: https://cubical.xyz/project/YR5e
BuildingAmazonAtalaya_5x5 x14: https://cubical.xyz/project/3VvZ
BuildignAamzonAtalaya_9x9 x14: https://cubical.xyz/project/Dmng
BuildingAmazonHut_7x7 x7: https://cubical.xyz/project/zDkN
BuildingAmazonHouse2_9x7 x7: https://cubical.xyz/project/Z5eO
BuildingAmazonHut2 7x7 x7: https://cubical.xyz/project/nr7n
BuildingAmazonField 7x7 x3: https://cubical.xyz/project/O28n

- Can you find a Hollow Hills!
- Dig in the direction slime statues look!
- The Lair of the Slime Boss! 
- Get the stairs block upon breaking!
- Get your full boat upon breaking!
- Better, faster, harder boats!
- Classic mod pistons!
- A tribe of brutes you can hire with gems!
- Sheep now regrow their wool!

# Step 13: Lightmaps and chunk unloading!

Use lightmaps - But I don't want to do this rambo. 1st I must understand exactly how lighting works in b1.7.3. Then I will port r1.2.5 lighting + lightmaps. But I will only leave that in IF I get the same or more or maybe just slightly less performance.

So on to the first step - understanding how light works.

Two things to solve:

- How celestial angle affects lighting. What causes all chunks to get invalidated and how new lighting is calculated.
- How block relighting is handled. 

## How celestial angle progess updates light levels.

So on to the first issue. `calculateSkylightSubtracted` gets the celestial angle and produces an integer value which seems to range between 0 and 11.

`calculateSkylightSubtracted` is called on each tick, in `World.tick`. If the returned value changes, it is stored (on `World.skylightSubtracted`) and for all worldAccesses `updateAllRenderers` is called, which in turn marks all chunks dirty.

So all chunks are going to be re-rendered, nice. But how the new `skylightSubtracted` affects them?

Well, now all chunks are marked dirty. their `WorldRenderer` objects are added to `RenderGlobal.worldRenderersToUpdate`, and marked dirty.

In `RenderGlobal.updateRenderers`, the `worldRenderersToUpdate` list is iterated and `updateRenderer` is called for each object.

`WorldRenderer.updateRenderer` creates a `ChunkCache` object. A `RenderBlocks` object is created, passing that `chunkCache` as `IBlockAccess`. 

In `RenderBlocks`, `Block.getBlockBrightness` is called when rendering each block. That calls the passed `IBlockAccess`'s `getBrightness`, which is `ChunkCache's`, which uses `World.skylightSubtracted` in its calculations. 

So this part is solved: when `skylightSubtracted` changes, all chunks are re-rendered, and in that process the (new) `skylightSubtracted` value is taken in account.

** So blocks still have their own light value - the lightmap just smooths this out and (presumably) makes it faster **

## Block relight

A new block is placed. How lights are recalculated? 

### Sky light

If the block being placed is not 100% transparent and it is set above the topmost block in its column, `relightBlock` is called for (x, y + 1, z), which is the block position just above the placed block.

If the block is 100% transparent and placed at exactly the height of the topmost block in its column, `relightBlock` is called for (x, y, z).

In any case, `scheduleLightingUpdate` is called for a range which just contains (x, y, z).

`relightBlock` is almost the same as b1.8+ but the last part changes. It ends up calling `scheduleLightingUpdate`. RelightBlock just changes the light value of blocks vertically in the x, z column starting on y.

`scheduleLightingUpdate` does some checks and enqueues the passed coordinate range in `World.lightingToUpdate`.

### Block light

When a block is placed it is added to `lightingToUpdate` via `scheduleLightingUpdate`, but with type = block.

### `updatingLighting`

`World.updatingLighting` seems to consume `lightingToUpdate`. For each extracted item (of class `MetadataChunkBlock`) it calls `recalculateLighting`.

`recalculateLighting` does its thing lighting blocks and propagating changes horizontally, which ends up calling `World.neighborLightPropagationChanged` which in turn schedules more lighting updates. 

## Updating to new lighting + lightmaps

It's a lot of work but it should be doable and I think it may make things better. I'll start slowly adding new stuff to the Tessellator, then changing `RenderBlocks`, then entity related stuff, then adding the lightmap, then changing how block relighting works. I'm going all the way to r1.2.5 (save for the Anvil stuff which I won't be adding for sure!).

### Lightmaps

MAKE A BRANCH FIRST!

Tesellator / Renderblocks / Lightmaps on! Now commit changes and change lighting! Clouds look extra bright tho, need to compare side to side. Also total darkness should be darker.

### New lighting

I'll try and make a list of the methods that should be removed...

* World's `scheduleLightingUpdate` alongside `lightingUpdatesScheduled`, `lightingToUpdate`, `updatingLighting`.
* World's `neighborLightPropagationChanged`.
* The `MetadataChunkBlock` class.

Let's start doing that and getting a lot of errors. K. Now `Chunk` is plagued with errors. Let's add the new calls and the new methods to `World` and see if it magically works.

Nope. I've changed Chunk and World and relight just doesn't work. Let's work step by step. Fix block light first, then sky light.

[X] Fixed block light
[-] Fixed sky light  <- not quite right. Check all methods again tomorrow (particularly chunks) Better but it seems that original light is off. Recalculation seems right. But original light comes from populate which uses normal set block... something is still missing. **It may be `generateSkylightMap`** It isn't **or maybe horizontal propagation of sky light**
[X] Adjusted lightmaps so nights are dark again!
[X] Check methods that draw RLE'd columns as they may be utterly wrong!

Reverted to classic lights

### Chunk unloading

Done

### Problems with features

Completely rewrote how features work.

### Problem with tile entities

Sometimes reference to tile entity is lost and I get infinite oneshot spawners. I could patch this or maybe check if I can do better modifying `setBlockIdWithMetadata`.

### Mobs that get angry

Never recover, not even if you die. How to fix?

### Minimap

Done, may need to fix some colors

# Step 14: Ocean update Ships!

* [X] Underwater generators
* [X] A ways to breathe underwater and see underwater.
* [X] Ship dungeons + Chocolatin entities.
* [X] Chocolate dungeons.
* [X] Working drowned! (lacking sounds rn)
* [ ] Options to turn seasons off.
* [X] Hang plants from the ceiling of caves.
* [X] Underwater cities - use the same approach as Amazon cities.
* [X] Enhance minimap so it works underwater: don't draw water itself but the actual tile at land surface but with a blue tint.
* [X] Change knockback to float.
* [?] Implement drag-craft.
* [X] Implement screen modes.
* [X] Compare block replacer code with vanilla 'cause I get weird patches.
* [X] Darker nights
* [X] Zombie eyes glow in the dark
* [X] Make toxic / city zombies eyes also glow -> This would need several render passes! Armor pass = 1, eyes pass = 0 so armor (outer) is painted after
* [X] toxic zombies particles are too dark?
* [X] Snow tall grass block. If snow needs to cover tall grass it's replaced by snow tall grass. When hit with a shovel it is replaced by a tall grass block
* [X] Remove beds from ships schematics!
* [X] Hollow hills don't render right.
* [X] Optimized SpawnerAnimals to spawn entities only in existing chunks to reduce amount of loaded chunks.
* [X] You should be able to place fences by other fences even though no solid tile is beneath!
* [X] Amazon Village buildings should not spawn under sea level!

Note that I'm encoding 250 = possible chest, 251 = Spawner.
Also 147 is my custom shiny glass, which is a gold pressure plate in vanilla.

(XxZxY)

Derelict 1 15x15x7 https://cubical.xyz/project/rqNX
Derelict 2 15x15x7 https://cubical.xyz/project/jEWO
Walk 1     15x5x7  https://cubical.xyz/project/lNWe
Dome 1     7x7x7   https://cubical.xyz/project/XR65
Digme      7x7x7   https://cubical.xyz/project/d6WE
Building 1 15x15x12 https://cubical.xyz/project/YO6R
Building 2 15x15x12 https://cubical.xyz/project/0Vlj
Hut 1      7x7x7   https://cubical.xyz/project/P7Y2
Hut 2      7x7x7   https://cubical.xyz/project/EEVW

- Haunted cow recoded from bigbang87's Deadly Monsters mod for MC 1.10.2

- Pirates and pirate ships!
- Haunted cows!
- Copper ore!
- Underwater ruins!
- A means to explore underwater!
- Underwater zombies!
- Optional Minimap!
- Lightmaps!
- More weapons!
- Harsher life enduring blood moons!
- Make a Crying Obsidian wand to respawn!
- Dynamic snow/ice freeze & melt optional, default off.
- Boats obliterate lilypads!
- Dress like a pirate and infiltrate!

https://cdn.discordapp.com/attachments/1123505829941887026/1169749729937203261/2023-10-04_22.51.47.png
https://cdn.discordapp.com/attachments/1123505829941887026/1169749730784448622/2023-10-28_23.21.21.png
https://cdn.discordapp.com/attachments/1123505829941887026/1169749730394374236/2023-10-28_21.19.04.png
https://cdn.discordapp.com/attachments/1123505829941887026/1169749731195494430/2023-11-02_16.53.13.png
https://cdn.discordapp.com/attachments/1123505829941887026/1169749731656859828/2023-11-02_16.54.38.png
https://cdn.discordapp.com/attachments/1123505829941887026/1170114320315711518/2023-11-02_22.29.55.png
https://cdn.discordapp.com/attachments/1123505829941887026/1170114320663859272/2023-11-02_22.30.55.png

# Step 15: (early) trading & biome revamp

## Biome revamp roadmap

* [X] Make metadata 8 bit and modify the loader to understand 4 bit metadata and convert it
* [X] Add clay blocks using 1 texture and a colorizer.
* [X] Move the process of substituting 1 column of chunk data to the BiomeGenBase class.
* [X] Make a Mesa Biome - desert blend to substitute the rocky desert.
* [/] Detect the mesa biome from the mineshaft generator to bring them up and generate above 64
* [X] mesa volcanos should generate mesa layers.
* [X] Very rare palm trees in the desert biome and acacias on the mesa biome.
* [X] Add biome between taiga and tundra which is like "tundra with shrubs"
* [X] Make the colorizer work for tall grass to have yellowish tall grass in desers.
* [X] Generate fossils in deserts. 

## Volcanoes

* [X] Do basic volcano gen
* [-] Add metadata to stone to have darker variants, use in volcanos. OR
* [X] Make volcanos a rocky biome thing.
* [X] volcanic rock? deepslate-like in alpha look?

## The ice palace

* [X] Add "blind" effect.
* [X] Create `EntityArrowWithEffect`
* [X] Replace `EntityArrowBlind` (which doesn't work, currently) with effect arrow.
* [X] Basic layout & recursive stuff
* [X] Add a different, simpler topper and use the big one only for the boss

Piezas:
generalmente de 9x9x4:
	Vacío para trabajar: https://cubical.xyz/project/pD8y
	Room 1: https://cubical.xyz/project/Ymx8
	Room 2: https://cubical.xyz/project/wXPM
	Room 3: https://cubical.xyz/project/8NQj
	Room 4: https://cubical.xyz/project/3xQ8

tall rooms 9x9x7
	Room 5: https://cubical.xyz/project/6EQ2
	Room 6: https://cubical.xyz/project/11QQ
	Garden: https://cubical.xyz/project/RpZ9
	Library: https://cubical.xyz/project/n9XP
	Jail: https://cubical.xyz/project/11RW

Base de 15x15x7: 
	Base: https://cubical.xyz/project/d2DM 

Cúspide de 15x15x7: 
	Boss: https://cubical.xyz/project/7b73
	Normal: https://cubical.xyz/project/Qd9N

Undertower de 9x9x5:
	Under: https://cubical.xyz/project/mypm
	Redo under: https://cubical.xyz/project/11lz
	Redo Redo under: https://cubical.xyz/project/8Npz

Corridor 7x3x3:
	Corridor: https://cubical.xyz/project/8N7X

Guide: https://cubical.xyz/project/V3ZX

## Fossils

Head 1: https://cubical.xyz/project/be2m
Head 2: https://cubical.xyz/project/vlw6
Head 3: https://cubical.xyz/project/eqBQ
Head 4: https://cubical.xyz/project/gBJG
Body 1: https://cubical.xyz/project/2mn2
Body 2: https://cubical.xyz/project/qn20
Body 3: https://cubical.xyz/project/G9Nr
Body 4: https://cubical.xyz/project/Bkxe

World Overhaul Update

- Tall mesas in smooth prairies!
- Menacing glaciers and icebergs!
- Deadly deserts with fossils!
- Abandoned huts in tundras and taigas!
- Volcanoes and stone arches!
- The Ice Palace!
- Smoother biome transitions!
- Better trees!
- Lots of fixes!

## General

* [X] Mobs still spawn inside blocks!!
* [X] Tall grass in mesas
* [X] Darker stone (meta 1)
* [-] Import BD pigmen, or at least a super simple version of them (like beta pirates)
* [X] minidungeon (island) mimicking the Nether inside volcanos - Make sure pigmen spawn!

# Step 16: pigmen & cowmen

* [X] TOO MANY CHUNK UPDATES. Revise main loop, tick loop, snow melting. Decrease rates.
* [ ] Revamp small city buildings
* [ ] More furniture
* [ ] Check all ships have loot chests - inc. galeon! (specially!)
* [ ] Moss block like orig. snow block that spawns on top of things in wet biomes. Spawn around the base of trees and let them propagate like mushrooms.

https://cdn.discordapp.com/attachments/1123505829941887026/1181722104589193336/2023-12-05_23.18.18.png?ex=65821779&is=656fa279&hm=d7b3063405a040d26cc550b824a61cb1d692b683e3fb62c1195b41b0ff1c1b3f&
https://cdn.discordapp.com/attachments/1123505829941887026/1181722105113485432/2023-12-05_23.18.37.png?ex=65821779&is=656fa279&hm=5d37dccbb9365487b34c3c986fc1e78009727a3916fb98d809bda5b1ff757d14&
https://cdn.discordapp.com/attachments/1123505829941887026/1181722105625186386/2023-11-27_22.46.59.png?ex=65821779&is=656fa279&hm=ec3e85b01e94d741513931e1f286baecae9560c5537d0bdb3dda4f1a9a7b470c&
https://cdn.discordapp.com/attachments/1123505829941887026/1181722106019467345/2023-11-24_23.39.59.png?ex=65821779&is=656fa279&hm=70bec3e36643aa17a2c419aa9193af2f64e12e76371cc79175069aad0eb4f207&

The First Before Last Update

- Lots, heaps of bugfixes!
- Better frozen oceans!
- Alpha Shipwrecks!
- Cursed cowmen & pigmen!
- Awesome lush forests!
- Lots of new trees!
- Bigger biomes!
- Optimizations! More fps!
- Better server!

* [X] Shipwrecks for the glacier biome
* [X] Don't generate normal ships in glacier biome!
* [ ] MapGenUnderwater seems to interfere with icebergs (building not present, chest/light present!) - It seems it changes/fucks up land surface height
* [X] Ice warriors from spawners often spawn inside blocks? FIXED IT JANKLY

Wreck 1: https://cubical.xyz/project/ZJ95 9x9x15
Wreck 2: https://cubical.xyz/project/5V8P 9x15x15
Wreck 3: https://cubical.xyz/project/WQzj 9x15x16
Wreck 4: https://cubical.xyz/project/JE5w 15x15x15
Wreck 5: https://cubical.xyz/project/y2MR 9x15x15

* [X] Cleanup BO3 collection. Delete MOST
	BO3 Used:
	* Acacia:AcaciaSavanna
	* Spruce:SpruceFirSmall
	* Spruce:SpruceFir
* [x] Check again weapon values vs. enemies. 
* [X] Mobs auto burn? WTF??
* [X] Redo axis-aligned blocks renderer using normal cubes (complete with release if needed) so they get proper AO smooth lights.
* [X] "Your home bed is missing or obstructed", when dying after setting up spawner.
* [X] City mobs not spawning ?!
* [X] Wood is dropping orientation metadata
* [X] Make meta block such a priority that overrides anything
* [X] Encode all base tree types in the higher 4 bit of leaves so I can have saplings for all tree groups:
	* 0 Normal / Big (random) oak
	* 1 Baobab
	* 2 Cypress
	* 3 Fir (1)
	* 4 Jungle (2)
	* 5 Mangrove
	* 6 Taiga (3)
	* 7 Fancy Fir
	* 8 Willow
	* 9 Shrub
* [X] Modify treegens to use correct metadata in leaves

(1) -> random for WorldGenFir, WorldGenPineTree 
(2) -> 4 saplings: huge tree
(3) -> random for taiga1, taiga2
(4) -> bo3 small spruce fir, bo3 spruce fir

* [/] Bigger biomes - understand better NoiseGenOctaves to increase the size of biomes (probably modifying "exponent")
* [/] Starlight? <- branched and tried, but out of memory... Need to understand the algorithm better.
* [X] Colder world type, subtract 0.5 from T. 
* [X] Chests in ice palaces are empty. 
* [X] Monsters from spawners still spawn inside stuff!!
* [X] Grass with meta = 1 but snow on it what do
* [/] Check one shot spawners in ships AGAIN. -> He quitado el "canspawnhere". A ver qué pasa.
* [X] Furnaces buggy in SMP
* [X] Sometimes clients builds up lag. Suspicion: too many packets arrive and overload the queue - put a limit. Big problem! This happens when new chunks have to be created and passed on to the client. the read size qeue skyrockets and the game freezes.
	Comparing stuff related to this I've realised a couple of bugs to correct but which I don't think are causing the problem. In pack52 I need to use a different enconding as I have 1 byte ID 1 byte metadata and not 12 bits ID 4 bits meta.

	He revisado también los métodos que copian los datos del paquete 51 en los chunks, y he visto que pueden ser copias de "porciones" de chunk. Siempre se llama a generateLandSurfaceHeightMap y ¿es posible que esté asumiendo que siempre hay terrain? No creo pero... -> No es esto, está controlado :-(

	DONT FORGET - This was solved. Problem was... I always forget. Oh! I had ported r1.2.5 netcode and it was keeping track of stuff to update and letting a huge list grow and when it got so big it took lots of time to traverse and that made the client unresponsive which made the list grow even more and etc. Removed it and now the problem is gone.
* [X] Implement some sort of eating animation (use release's or b1.8's).
* [X] Make animals follow you if carrying wheat.
* [X] Give names to animals to make them not despawn. Needs a GUI and some special object? - Uses "interact" from EntityAnimal to detect a name tag in the hand. Displays a simple GUI to add a name. Need to make this work in 
* [X] Give names SMP, also names sent to the client when sending entities. --> Work like signs --> O A LO MEJOR NO HACE FALTA porque es datawatcher...! - Pues o el datawatcher tipo string no funfuña, o no va... Voy a centralizar los # de los datawatcher primero. --> He descubierto que un cambio en los datawatchers funciona server -> clientes, pero no al revés. Necesito lo que estaba haciendo del paquete.
* [X] Goats. 
* [X] Achievement on using bucket to milk cows.
* [X] Achievement on using bucket to milk goats.
* [X] Recovery compass  - Add a special compass which points to your last death (spawn point if never died). + achievement (Craft a recovery compass). Crafting recipe is ** surrounding a compass. What could I use? Obsidian shards? Break obsidian to get 9 shards, use 9 shards to build obsidian block. 
* [X] Recovery compass SMP - Debería transferirse al cliente igual que se transfiere el spawn ? -> Se manda con Packet9Respawn . [ ] TEST 
* [?] Fire in mobs is not 100% bright
* [X] Right click to place on a non opaque block should IGNORE that block
* [X] Re.check dead man's chest
* [X] STARLIGHT WORKING! (or so it seems)

World Overhaul Update

- Tall mesas in smooth prairies!
- Menacing glaciers and icebergs!
- Deadly deserts with fossils!
- Abandoned huts in tundras and taigas!
- Volcanoes and stone arches!
- The Ice Palace!
- Smoother biome transitions!
- Better trees!
- Lots of fixes!

TO DO

* [X] Separate all data stored in GameSettings to a static GameSettingsValues, replicate in Server
* [X] FIX PINK IN MAPS!
* [-] Lightning rod achievemnt is very cheesy and not server-friendly as it requires Minecraft. You can't really fire up achievements from World, think on a solution. NAH. VANQUISHED.
* [X] Meat block should decay over time. Block is ticked. When gauge is 0, block is tinted dark or greenihs and associated entity is killed.
* [ ] Jumpy status effect + [ ] item + [ ] achievement + [X] affects all livings.

MAYBE

* [ ] More city buildings
* [ ] More furniture
* [ ] Moss block like orig. snow block that spawns on top of things in wet biomes. Spawn around the base of trees and let them propagate like mushrooms.
* [X] 4 saplings: huge tree

--

My starlight port is still nor perfect - for new chunks ?

Let's annotate the diferences:

1.- getNibbleFromCache

	Org. returns the nibble array from a SECTION (16x16x16).
	Min. returns the nibble array for the WHOLE CHUNK (16x128x16).	

# Working on 1.0, lots of stuff to fix.

* [X] Eating not working in SMP - Lots of missing backend, redid some, backported to 1.2.5 some.
* [X] Placing does not work, apparently when neighbor notification is needed. Cascading changes are not correctly sent to the clients, i.e. when placing a block in shallow water. In the end what get executed is `ItemBlock`'s call to `setBlockAndMetadataWithNotify`. I need to trace from there, what happens to notified blocks taht change.

	setBlockAndMetadataWithNotify -> notifyBlockChange -> markBlockNeedsUpdate / notifyBlocksOfNeighborChange --> for each block surrounding: onNeighborBlockChange, which may cause setBlock/etc. 

	Need to understand (again?) how block changes are notified to the clients.

	Packet53BlockChange (-> handleBlockChange) ? Nope, Packet only sent when placing / digging.
	
	Packet52MultiBlockChange (-> handleMultiBlockChange) ? called from PlayerInstance.onUpdate

	Well, I made a remark in the code explaining how r1.2.5 works differently by sending the whole chunk. Maybe this has to do with my problem, as I'm using mostly r1.2.5 netcode. But I thought this was purely an anvil thing.

	Let's add some logs and see what I can make of this.

	`numBlocksToUpdate` is updated by `markBlockNeedsUpdate` which is called by `PlayerManager.markBlockNeedsUpdate` which is called by `ServerConfigurationManager.markBlockNeedsUpdate` which is called by `WorldManager.markBlockNeedsUpdate`  which is called by `World`'s own.

	This is very complicated to log as there are blocks marked for update continuously. I'm better of checking against r1.2.5 code again?

	Placing a bucket of water triggers the problem big time as none of the blocks that are updated automaticly get to the client, just the original placed block... and occasionally some surrounding blocks. Strange.

	Should I try and do this the r1.2.5 way?? Later.

	I'm logging what the client receives, and it seems to receive multiblock changes when I trigger them. Maybe the data is wrong?? Maybe not placing correctly?

	I think I've seen it. I was decoding shit incorrectly : _ / Yup ... Solved.

* [X] Maybe there's a problem with how much damage you do, 'cause a power hit to a pig with bare hands KILLS the pig. Which I don't think it's right...

	- GetDamageVsEntity: 1 for bare hand, if not get from item, which is 1, generally, or 
		- Axe: 3 + material (0-4).
		- Pickaxe: 2 + material.
		- Sword: 5 + material (apparently this was 4 + `2*material`)

	- +1 if falling (charged attack), so bare hand charged = 2;

	- Call `victim.attackEntityFrom(this, damage)`

	- In the worst case scenario, it ends up calling `damageEntity(damage)`, wich subtracts damage from health.

	- In r1.2.5 there's a "applyArmorCalculations" and "applyPotionDamageCalculations" modifiers.
	
# Working on 1.0, lots of stuff to fix (0.9r3 -> r4)

* [X] Fix minor problmes reported in Discord.
* [-] Add dynamic lighting using a craftable lanern item if item is on hand. A small mod to Starlight is needed.
* [X] I've changed the way cities interact with terrain again. Change all buildings to hollow out their interior, so bumpy terrain only affects outside the buildings.

# 0.9r5

* Tamed monoliths!
* Added crafting of beds as a cheat!
* Added wooden panes!
* You can sprint, but only on max heath! Player will be crippled on low health (can be disabled)

* [ ] Make sprinting / crippling disableable.
* [X] Don't generate city decorations if base spot is covered.
* [X] Check: sometimes custom buildings are appearing overwriting buildings.

Before I release this I wanna make my life easier in case I decide to keep working on this.

* [X] IBlockWithSubtypes stuff, please. There are too many ItemBlocks.
	* ItemLog - deleted, it's not used.
	* ItemSapling - 
		* Saplings store their tree type in the high 4 bits just like Inslands - but saplings are not named nor have their individual graphics nor appear in creative.
		* Something should be added visually so they can be distinguished in the inventory, as they won't stack if they are of different types, so maybe a letter overlay?
	* ItemLeaves - As is, as it is used to raise bit 8 so leaves don't decay.
	* ItemCoral - Changed.
	* ItemBigFlower - Cool.
	* ItemIce - Great.
	* ItemGrass - Yess.
	* ItemTallGrass - This was harder but OK.
	* ItemGlass - Yay.
	* ItemCement - Clazz.
	* ItemStoneBricks - Neato. Also redid the textures to something more vanilla but with the cobble colors.


* [X] EnumTreeType.
	* This is gonna be hard. Of course, BO3 trees are just decorative and I'll leave these aside for now. What I wanna achieve tho is the leaves/wood/saplings asocciation and the grow tree automation.

	* Well, not that hard - considering I only have to generate these trees:
		* [X] NORMAL "Oak", WorldGenTrees / WorldGenBigTree
		* [X] BAOBAB "Baobab", WorldGenBaobab
		* [X] CYPRESS "Cypress", WorldGenCypress
		* [X] FIR "Fir", WorldGenPineTree, WorldGenFir
		* [X] JUNGLE "Jungle", WorldGenHugeTrees, 4 saplings!
		* [X] MANGROVE "Mangrove", WorldGenMangrove
		* [X] TAIGA "Taiga", WorldGenTaiga1, WorldGenTaiga2
		* [X] FANCYFIR "fancy", genera un BO3 de tipo "Spruce:SpruceFirSmall" o "Spruce:SpruceFir" entre todas las variantes.
		* [X] WILLOW "Willow", WorldGenWillow
		* [X] SHRUB "Shrub", WorldGenShrub

	* So thing is, first check if those 10 gens have been parametrized to include the right metadata in the leaves so they produce the right saplings. And it is done, it seems that I had this pretty much finished.

	* So the ONLY thing to do is adding the NEW, full fledged EnumTreeType and automate teh saplings.

Once that's done, Infhell2 is done.

# 0.10 - it was not done.

* [X] Apply intel fix, 'cause legacyfix doesn't work shit with my custom code.
* [X] Make sure you don't spawn in a city chunk. Maybe not even near? How so?
* [X] Make "normal" city generation less.
* [X] Try to make battle axe less useful when chopping wood (trees).
* [X] Saplings with "I'm growing" meta render snow logged. 
* [X] As there's less ticks per chunk than normally, make saplings grow easier. Soften this a bit:

```
	if(world1.getBlockLightValue(i2, i3 + 1, i4) >= 9 && random5.nextInt(30) == 0) {
```
* [X] No skeletons! Adjust ratios in biomes.

I've seen that city specific mobs are added to the normal lists, then discarded. Maybe a serious overhaul on how different types of mods are selected to be spawned has to be developed, like this:

	- Make the mobs list separate.
	- Discard early. Only process city mobs if chunk is urban, and process normal mobs otherwise. 
	- The spwaner calculates the amount of "chance" added by every mob type in the spawn lists every time! Create a proper constructor that maintains this value when creating the biomes and then uses it!

This is the kind of change that beaks lots of shit, and I will leave it until all the silly bugs are corrected.

* [X] Goats no textures

# 1.0-pre1 - Reorganization, rehash, enhancements

* [X] Huge chunk updates count -> it was the starlight engine.
* [X] New EnumTreeType based trees broke the seasonal colorizer. Need to decide which tree tipes will be colorized and adjust.
* [X] Placed flowers seem to modify the light (slight increase?) of the supporting block, for some reason.
* [X] Redevelop abandoned idea: Moss block like orig. snow block that spawns on top of things in wet biomes. Spawn around the base of trees and let them propagate like mushrooms.
* [X] Optimization idea: instantiate worldgens outside loops in populate.
* [X] Optimiztaion idea: cache temperature / humidity in chunks.
* [X] Climate bias towards hot, high possibility of "single climate seeds", study and solve.
* [ ] Check all ships have loot chests - inc. galeon! (specially!)
* [/] Make trading work SMP
* [X] Check lag in SMP
* [X] Maybe `ChunkProviderGenerate.specialCarving` doesn't make sense anymore!
* [X] Study using cached terrain from justGenerateForHeight for new chunks, if available.
* [X] Control saddled pigs.
* [X] Block halfway between grass and gravel (grass with stones) to make beeutiful shit
* [X] Bottom half of sky box same colour as fog.
* [X] Bed have bit 4 = can sleep, default off.
* [X] Stone spikes in the ice look odd, they should start in the sea bed and protrude.
* [X] Ice palaces melting (spawning in too hot weather)
* [X] Ice palace metadata in stairs is not being laid out to the world (all render with meta = 0).
* [X] Stone brick stairs render with small stones for some orientations (fix the metadata sent to the origin block)
* [X] Bigger ice palace - problem out of bounds  -1864 74 6664
* [X] Select screen resolution = slider
* [ ] Hints in loading screens
* [X] Highway broken 
* [X] Legacy MapGenUnderwater not placing chests / lights on population - removed completely

Sleeping

This is a design for the sleeping mechanic in this mod. Vanilla beds let you sleep on them at night or during thunderstorms. This skips the night or thunderstorms completely. This is too powerful for being so cheap to craft. So I've designed a new mechanic:

* You can craft beds, as usual, with 3 wool, 3 planks, and place them.
* You can sleep on beds, but this WON'T skip the night. You'll lay on the bed, scene will darken, and nothing else. You may get up right normally. It's like when you try to sleep on a bed during multiplayer, but other players are not sleeping. Exactly the same mechanics. You can stay in bed but the world ticks normally and you are still vulnerable to attacks, etc.
* Item.lunarWand is a powerful and rather expensive magic wand that can be crafted with this recipe:

.Dg
.dD
G..

Where D = Diamond block, g = Glowstone block, d = *any* music record, G = Gold block.

* When you right-click the bed with the wand, the same sound & effects as hitting BlockCryingObsidian with a coompass happen, and you enchant the bed and give it the ability to skip the night. If you sleep on an enchanted bed, you skip the night, like sleeping in any bed in Vanilla beta minecraft. 
* When you break an enchanted bed, it loses its enchantment.
* When a bed is enchanted, it emits magic particles "glowdust" at random in the same frequency as fire emits smoke particles.
* When you first craft the lunarWand, you gain the achievement "lunarWand".
* When you first enchant a bed, you gain the achievement "magicBed".

Part of this plan

* When you hire an Amazon, you gain the achievement "hireAmazon".
* When you wear the full set helmetRags, plateRags, legsRags you gain the achievement "dessedInRags"

