# Recreating vanilla `Structures` as `Features`.

## Current status

Package `net.minecraft.game.world.terrain.generate.structure` contains backports of three vanilla minecraft structures: mineshaft, mineshaftmesa and stronghold. Structures are big, complex buildings made of different pieces that are connected in different ways procedurally. The system is conceived so buildings can be "drawn" onto the world chunk by chunk and never overflow to adjacent chunks, which may cause recursion problems. The 'Structure' system works by detecting a nearby structure center during generation, procedurally building it (i.e. combining its pieces) and storing its layout. When chunks that contain portions of the structure are generated, the chunk-sized bit of the structure that corresponds to that chunk is "drawn" onto it during the population stage (hook in `ChunkProviderGenerate.populate`).

Package `net.minecraft.game.world.terrain.generate.feature` contains our own multi-chunk building system. Main class: FeatureProvider. It serves the same purpose: generate big buildings and copy them to the world chunk by chunk, on demand. For each chunk being generated, nearby features are detected and a list of features that would include the current chunk is created. Feature layouts are created when the classes are instantiated. Then the system works in two steps: 1. during the *generation* stage (hooks in `ChunkProviderGenerate.provideChunk`), the corresponding bit of the feature is copied directly to the block/metadata pre-flattened arrays. Most "normal" blocks and air are copied during this stage. 2. During the *population* stage (hook in `ChunkProviderGenerate.populate`), special blocks such as light sources, chests, spawners are copied, call to WorldGenerators, etc.  

## Main goal

The main goal is recreating each of the three vanilla structures (mineshaft, mineshaftmesa, stronghold) as Features.

## Directions

* Work on a structure at a time, so the newly recreated structure can be tested. 
* Every recreated structure must take advantage on how the Feature system work: create the whole layout on class instantiation, copying normal blocks to the world during chunk generation (`Feature.generate`), and then copying special blocks (light sources, chests ...) during chunk population (`Feature.populate').
* All three vanilla structures share the same API. A common API could be constructed for the three new Features, for example to generate chests and spawners or to copy portions of pieces to the chunk during generation and population.
* It is **very important** that the main purpose of structures / features is respected: rendering (or "copying") to the World should be made **chunk by chunk**, writes should never *overflow* to neighbouring chunks.
* Generated code should be readable, and heavily commented. A postmortem should be maintained after each structure is recreated as a feature.
* The new features should have the same set of pieces with the same chance to spawn and connect them in the same way as the original to produce similar buildings that look the same and behave the same.
* It should be easy to, once completely recreated, newly designed pieces (or rooms) can be added to the originals.

Note in AGENTS.md that this is the active line of work now and that every newly recreated structure has to be thoroughly tested by the user and confirmed to work before going to the next structure->feature recreation. When each structure is recreated as a feature, the original structure must be "unhook" (so it doesn't generate), and not deleted until the new feature has been validated.
