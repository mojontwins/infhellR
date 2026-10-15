# EntitySubclass Hierarchy (Corrected)

## Complete Class Hierarchy Overview

```
EntityMob (abstract base class - net/minecraft/game/entity/EntityMob)
├─ EntityHumanBase (human-like entities with level system)
│   ├─ EntityPirate (pirate mob, level 0-4 with armor)
│   │
│   ├─ EntityIceWarrior (ice warrior mob, level 0-4 with armor)
│   │
│   └─ EntityIceBoss (ice boss, level 0-4, fixed HP, unique AI)
│
├─ EntityMob subclasses directly (no level system)
│   ├─ EntityPirateArcher (pirate archer, extends EntityMob)
│   │   └─ EntityIceArcher (ice archer, extends EntityPirateArcher)
│   │
│   └─ EntityPirateBoss (pirate boss, extends EntityMob)
│
└─ EntityZombie-based traders (different hierarchy)
    ├─ EntityTrader (abstract, extends EntityZombie, implements ITrader)
    │   ├─ EntityCowman (extends EntityTrader)
    │   └─ EntityPigman (extends EntityTrader)
    │
    └─ EntityAlphaWitch (extends EntityArmoredMob - separate hierarchy)
```

## Key Class Relationships

### 1. EntityHumanBase Hierarchy (Level + Armor System)
These entities **extend EntityHumanBase** and have the level/armor system:
- **EntityPirate** → level 0-4, progressive armor (20→40 HP)
- **EntityIceWarrior** → level 0-4, ice-themed armor (20→40 HP)
- **EntityIceBoss** → level 0-4, fixed 100 HP, ice attacks, teleport

All three extend `EntityHumanBase` → inherit `EntityMobWithLevel` → have `getLvl()`, `setLvl()`, `getFullHealth()` = `20 + armorType * 5`.

### 2. EntityMob Direct Subclasses (No Level System)
These entities **extend EntityMob directly** and do NOT have the level system:
- **EntityPirateArcher** → extends EntityMob, has bow attacks, NO level/armor
  - `extends EntityMob` at line 19 of EntityPirateArcher.java
- **EntityIceArcher** → extends EntityPirateArcher → EntityMob
  - `extends EntityPirateArcher` at line 10 of EntityIceArcher.java
  - NO level/armor system (inherits the no-level nature from parent)
- **EntityPirateBoss** → extends EntityMob, boss variant
  - Fixed 100 HP, invisible mechanic, teleport
  - NO armor system

### 3. EntityZombie-Based Traders (Separate Hierarchy)
- **EntityTrader** → extends EntityZombie, implements ITrader, ISentient
  - Has trading system, abstract class
  - `extends EntityZombie` at line 19 of EntityTrader.java
- **EntityCowman** → extends EntityTrader
  - `extends EntityTrader` at line 13 of EntityCowman.java
- **EntityPigman** → extends EntityTrader
  - `extends EntityTrader` at line 13 of EntityPigman.java

### 4. EntityAlphaWitch (Separate Hierarchy)
- **EntityAlphaWitch** → extends EntityArmoredMob
  - Different armor system entirely (potion-based)
  - `extends EntityArmoredMob` at line 15 of EntityAlphaWitch.java

## Armor System Distribution

### Entities WITH Level/Armor System (extends EntityHumanBase):
| Entity | Level Range | Max Health | Armor Type |
|--------|-------------|------------|------------|
| EntityPirate | 0-4 | 20→40 | Type = level (0=none, 4=diamond) |
| EntityIceWarrior | 0-4 | 20→40 | Type = level, ice-themed textures |
| EntityIceBoss | 0-4 | 100 (fixed) | Has level but fixed HP |

### Entities WITHOUT Level System:
| Entity | Parent | Level System |
|--------|--------|--------------|
| EntityPirateArcher | EntityMob | NO |
| EntityIceArcher | EntityPirateArcher → EntityMob | NO |
| EntityPirateBoss | EntityMob | NO (fixed 100 HP) |

### Entity Traders:
| Entity | Level System | Armor System |
|--------|--------------|--------------|
| EntityTrader | No (extends EntityZombie) | Trading-based |
| EntityCowman | No | Trading-based |
| EntityPigman | No | Trading-based |

## NBT Serialization by Category

### EntityHumanBase Subclasses (level + armor):
```java
// Common fields across EntityPirate, EntityIceWarrior, EntityIceBoss
NBTTagCompound.writeInt("level", level);              // 0-4
NBTTagCompound.writeInt("armorType", armorType);      // 0=none, 1=leather, 2=chain, 3=iron, 4=diamond
NBTTagCompound.writeBooleanArray("armorPieces", armorPieces); // [boots, helmet, legs, chestplate]
NBTTagCompound.writeInt("health", health);            // current health
NBTTagCompound.writeInt("maxHealth", maxHealth);      // 20 + armorType * 5 (or 100 for boss)
```

### EntityMob Direct Subclasses (no level):
```java
// Common fields across EntityPirateArcher, EntityPirateBoss
NBTTagCompound.writeInt("health", health);            // fixed or dynamic
NBTTagCompound.writeByte("AngryAtPlayer", angryAtPlayer); // for pirates
NBTTagCompound.setByte("TextureVariation", textureVariation); // for variation
```

### EntityTrader Subclasses:
```java
// Common fields across EntityTrader, EntityCowman, EntityPigman
NBTTagCompound.writeInt("traderType", traderType);    // trading tier
NBTTagCompound.writeBoolean("AngryAtPlayer", angryAtPlayer);
NBTTagCompound.writeInt("health", health);            // from EntityZombie base
```

## Implementation Summary

### What Works With Current Code
- **EntityPirate** and **EntityIceWarrior**: Already extend EntityHumanBase → have level system automatically
- **EntityIceBoss**: Extends EntityHumanBase → has level system, setLevel(4) in constructor
- **EntityPirateArcher** and **EntityIceArcher**: Extend EntityMob directly → explicitly DO NOT have level system (archers are unarmored)
- **EntityPirateBoss**: Extends EntityMob directly → boss variant, fixed HP

### What Needs Redesign
- **EntityHumanBase**: Refactor armor system with progressive removal at 80%/60%/40%/20% health thresholds
- **EntityPirate**: Update to use new armor formulas
- **EntityIceWarrior**: Update ice-themed armor
- **EntityIceBoss**: Ensure level logic works with fixed 100 HP
- **Rendering**: Update RenderHuman to conditionally render armor pieces based on health

### What Should NOT Change
- **EntityPirateArcher**: Stays as EntityMob subclass, no level/armor
- **EntityIceArcher**: Stays as EntityPirateArcher subclass, no level/armor  
- **EntityPirateBoss**: Stays as EntityMob subclass, boss mechanics preserved
- **EntityTrader / EntityCowman / EntityPigman**: Separate hierarchy, trading system

## Migration Path for Armor Redesign

1. **Phase 1**: Refactor EntityHumanBase with new armorType + armorPieces[] system
2. **Phase 2**: Update EntityPirate constructor to set armorType = getLvl()
3. **Phase 3**: Update EntityIceWarrior to use same armor formulas (ice-themed textures)
4. **Phase 4**: Update EntityIceBoss to work with level system (setLevel(4) in constructor)
5. **Phase 5**: Verify EntityPirateArcher, EntityIceArcher, EntityPirateBoss remain unchanged (no level system)
6. **Phase 6**: Test client/server parity and NBT persistence