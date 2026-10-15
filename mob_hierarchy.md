# Mob Hierarchy

Mob/creature entity inheritance as of the current working tree. Each node is
`ClassName` followed by its package (relative to `net.minecraft.game`). Only the
"living" mob side of the entity graph is shown — projectiles, dropped items,
vehicles, AI primitives, status effects, helpers and networking classes are
omitted.

Package roots:
- `net.minecraft.game.entity` — generic entity + humanoid bases (shared client/server)
- `net.minecraft.game.entity.animal` — passive / Mo'Creatures animals
- `net.minecraft.game.entity.monster` — hostile monsters
- `net.minecraft.game.entity.human` — humanoid mobs (by branch)
- `net.minecraft.game.entity.human.base` — shared humanoid equipment/level bases
- `net.minecraft.game.entity.human.warrior` — weapon + armour-overlay humans
- `net.minecraft.game.entity.human.armored` — inventory-backed humans
- `net.minecraft.game.entity.misc` — non-monster / non-animal mobs (Triton, ...)
- `net.minecraft.game.entity.player` — the player

```
Entity                                                              entity
└── EntityLiving                                                   entity
    ├── EntityCreature                                             entity
    │   ├── EntityMob                                              entity
    │   │   ├── EntityHumanoid                                     entity        [common humanoid base: swing + held item]
    │   │   │   ├── EntityMobWithLevel                             entity        [level synced via data watcher]
    │   │   │   │   └── EntityHumanWarrior                         entity.human.base
    │   │   │   │       └── EntityArmoredHumanBase                 entity.human.base
    │   │   │   │           ├── EntityPirate                       entity.human.warrior
    │   │   │   │           └── EntityIceWarrior                   entity.human.warrior
    │   │   │   ├── EntityIceBoss                                  entity.human.warrior   (extends EntityHumanWarrior)
    │   │   │   └── EntityArmoredMob                               entity        [carries InventoryMob: armour + held item]
    │   │   │       ├── EntityZombie                               entity.monster
    │   │   │       │   ├── EntityDrowned                          entity.monster
    │   │   │       │   ├── EntityGiantZombie                      entity.monster
    │   │   │       │   ├── EntityHusk                             entity.monster
    │   │   │       │   │   └── EntityCityHusk                     entity.monster
    │   │   │       │   │       └── EntityToxicZombie              entity.monster
    │   │   │       │   ├── EntityPigZombie                        entity.monster
    │   │   │       │   │   └── EntityPigZombieVolcanoes          entity.monster
    │   │   │       │   ├── EntityZombieAlex                       entity.monster
    │   │   │       │   └── EntityTrader                           entity.human.armored   (abstract)
    │   │   │       │       ├── EntityPigman                       entity.human.armored
    │   │   │       │       └── EntityCowman                       entity.human.armored
    │   │   │       ├── EntitySkeleton                             entity.monster
    │   │   │       │   └── EntityIceSkeleton                      entity.monster
    │   │   │       ├── EntityAlphaWitch                           entity.human.armored
    │   │   │       ├── EntityAmazon                               entity.human.armored
    │   │   │       └── EntityTriton                               entity.misc    (also IWaterMob)
    │   │   ├── EntityCreeper                                      entity.monster
    │   │   │   └── EntityElementalCreeper                         entity.monster
    │   │   ├── EntityExplodingZombie                              entity.monster
    │   │   ├── EntityHauntedCow                                   entity.monster
    │   │   ├── EntitySpider                                       entity.monster
    │   │   │   └── EntityTFSwarmSpider                            entity.monster
    │   │   ├── EntityBuilderZombie                                entity.monster
    │   │   ├── EntityTFRedcap                                     entity.monster
    │   │   ├── EntityPirateArcher                                 entity.human
    │   │   │   └── EntityIceArcher                                entity.human
    │   │   └── EntityPirateBoss                                   entity.human
    │   ├── EntityWaterMob                                         entity
    │   │   └── EntitySquid                                        entity.animal
    │   └── EntityAnimal                                           entity.animal
    │       ├── EntityMoCAnimal                                    entity.animal
    │       │   └── EntityMocAnimalTameable                        entity.animal
    │       │       └── EntityGoat                                 entity.animal
    │       ├── EntityBetaOcelot                                   entity.animal
    │       │   ├── EntityCatBlack                                 entity.animal
    │       │   ├── EntityCatRed                                   entity.animal
    │       │   └── EntityCatSiamese                               entity.animal
    │       ├── EntityChicken                                      entity.animal
    │       │   └── EntityChickenBlack                             entity.animal
    │       ├── EntityCow                                          entity.animal
    │       │   ├── EntityColdCow                                  entity.animal
    │       │   └── EntityMooshroom                                entity.animal
    │       ├── EntityPig                                          entity.animal
    │       ├── EntitySheep                                        entity.animal
    │       └── EntityWolf                                         entity.animal
    ├── EntityPlayer                                               entity.player
    ├── EntityFlying                                               entity
    │   ├── EntityGhast                                            entity.monster
    │   └── EntityTFWraith                                         entity.monster
    ├── EntitySlime                                                entity.monster
    │   └── EntitySecretBoss                                       entity.monster
    └── EntityBlockEntity                                          entity
        └── EntityMeatBlock                                        entity
```

## Notes

- `EntityHumanoid` (in `net.minecraft.game.entity`) is the shared humanoid base
  introduced in the current working tree. It owns `isSwinging` /
  `swingProgressInt` / `heldItem` plus `swingItem()` and
  `updateSwingAnimation()`. Both humanoid branches descend from it.
- `EntityMobWithLevel` and `EntityArmoredMob` no longer extend `EntityMob`
  directly; they extend `EntityHumanoid`.
- The former `EntityHumanBase` was renamed to `EntityHumanWarrior`
  (`entity.human.base`); the thin, level-dependent human split into
  `EntityHumanWarrior` (overlay contract + level hook) and
  `EntityArmoredHumanBase` (level-driven armour/equipment).
- `EntityTrader` is abstract and lives in `entity.human.armored`; it still
  descends from `EntityZombie`.
- `EntityPirateArcher`, `EntityIceArcher` and `EntityPirateBoss` extend
  `EntityMob` directly (they do not yet use the human base) and remain in the
  `entity.human` root package.
