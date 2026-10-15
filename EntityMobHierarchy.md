# EntityMob Hierarchy Redesign

## Overview

This document outlines a major redesign of the `EntityMob` class hierarchy to improve modularity, separation of concerns, and maintainability. The redesign uses standard Java inheritance to organize entity types.

## Current Structure (Pre-Redesign)

```
EntityMob (base)
├─ EntityHumanBase (human-like entities with level system)
│   ├─ EntityPirate
│   ├─ EntityIceWarrior
│   ├─ EntityIceBoss
│   └─ EntityTrader (abstract, also extends EntityZombie)
│       ├─ EntityCowman
│       └─ EntityPigman
├─ EntityMob (directly, no level system)
│   ├─ EntityPirateArcher
│   │   └─ EntityIceArcher
│   └─ EntityPirateBoss
└─ EntityArmoredMob
    └─ EntityAlphaWitch
```

### Observations

- **EntityHumanBase**: Has level system inherited from `EntityMobWithLevel` (stores level in DataWatcher DWO #16)
- **EntityPirateArcher / EntityIceArcher / EntityPirateBoss**: Extend `EntityMob` directly, no level system
- **EntityTrader / EntityCowman / EntityPigman**: Different hierarchy (extends EntityZombie)
- **Artifact**: Armor logic scattered in EntityHumanBase `updateArmor()` method with incorrect health comparisons

## Proposed Changes

### 1. Core Fields in EntityHumanBase

```java
public class EntityHumanBase extends EntityMobWithLevel implements IMobWithLevel {
    public int armorType = 0;              // 0=none, 1=leather, 2=chain, 3=iron, 4=diamond
    public boolean[] armorPieces = new boolean[4]; // [boots, helmet, legs, chestplate]
    
    // New armor types enum for clarity
    public enum ArmorType {
        NONE(0, 20, "/armor/cloth"),
        LEATHER(1, 25, "/armor/leather"),
        CHAIN(2, 30, "/armor/chain"),
        IRON(3, 35, "/armor/iron"),
        DIAMOND(4, 40, "/armor/diamond");
        
        public final int typeId;
        public final int maxHealth;
        public final String textureBase;
        
        ArmorType(int typeId, int maxHealth, String textureBase) {
            this.typeId = typeId;
            this.maxHealth = maxHealth;
            this.textureBase = textureBase;
        }
    }
}
```

### 2. Updated getFullHealth() Formula

```java
@Override
public int getFullHealth() {
    // New formula: 20 + 5 * armorType
    return 20 + this.armorType * 5;
}
```

### 3. Fixed updateArmor() Logic (Deterministic from Health %)

```java
public void updateArmor() {
    float healthRatio = (float) this.health / this.getFullHealth();
    
    // Set armor pieces based on health percentage thresholds
    // [0] = boots, [1] = helmet, [2] = legs, [3] = chestplate
    this.armorPieces[0] = healthRatio > 0.8f;  // boots at > 80%
    this.armorPieces[1] = healthRatio > 0.6f;  // helmet at > 60%
    this.armorPieces[2] = healthRatio > 0.4f;    // legs at > 40%
    this.armorPieces[3] = healthRatio > 0.2f;  // chestplate at > 20%
}
```

### 4. Updated setLevel() for Armor Type

```java
@Override
public void setLevel(int level) {
    this.setLvl(level);
    this.armorType = level;  // armorType = level
    this.health = this.getFullHealth();
    this.configureAttributesBasedOnLevel();
}
```

### 5. NBT Serialization Updates

```java
@Override
public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
    super.writeEntityToNBT(nbttagcompound);
    nbttagcompound.setInteger("level", this.getLvl());
    nbttagcompound.setInteger("armorType", this.armorType);
    nbttagcompound.writeBooleanArray("armorPieces", this.armorPieces);
}

@Override
public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    super.readEntityFromNBT(nbttagcompound);
    this.setLvl(nbttagcompound.getInteger("level"));
    this.armorType = nbttagcompound.getInteger("armorType");
    this.armorPieces = nbttagcompound.getBooleanArray("armorPieces");
    if (this.armorPieces == null) {
        this.armorPieces = new boolean[] {true, true, true, true};
    }
}
```

## Entity-Specific Changes

### EntityPirate
- No changes needed (already extends EntityHumanBase)
- Armor type automatically set via level

### EntityIceWarrior
- No changes needed (already extends EntityHumanBase)
- Override `setArmorTexture()` for ice-themed textures
- Override `getFullHealth()` if 100 HP needed (IceBoss pattern)

### EntityIceBoss
```java
public EntityIceBoss(World world) {
    super(world);
    this.setLevel(4);  // diamond armor visually
    this.health = 100; // fixed, not 40
    this.setHeldItem(new ItemStack(Block.ice));
}

@Override
public int getFullHealth() {
    return 100; // override for boss
}
```

### EntityPirateArcher (No Changes)
- Extends EntityMob directly, no level system
- No armor modifications needed
- Keep as-is: bow attacks, no armor

### EntityIceArcher (No Changes)
- Extends EntityPirateArcher → EntityMob
- No level system
- Ice-themed arrows, no armor system needed

### EntityPirateBoss (No Changes)
- Extends EntityMob directly, no level system
- Fixed 100 HP, teleportation, invisibility mechanics
- No armor modifications needed

## Implementation Checklist

### Phase 1: EntityHumanBase Refactor
- [ ] Add `armorType` field (int 0-4)
- [ ] Add `armorPieces[]` field (boolean[4])
- [ ] Add `ArmorType` enum
- [ ] Update `getFullHealth()` to use `20 + armorType * 5`
- [ ] Fix `updateArmor()` with correct health percentage logic
- [ ] Update `setLevel()` to set `armorType = level`
- [ ] Update NBT serialization

### Phase 2: Entity Updates
- [ ] Verify EntityPirate: already uses level system
- [ ] Verify EntityIceWarrior: inherits correctly
- [ ] Update EntityIceBoss: ensure fixed 100 HP doesn't break armor logic
- [ ] Verify EntityPirateArcher: no changes needed (no level system)
- [ ] Verify EntityIceArcher: no changes needed (no level system)

### Phase 3: Rendering Support
- [ ] Update RenderHuman to check `armorPieces[]` array
- [ ] Ensure client-side armor piece visibility matches server health state

### Phase 4: Testing
- [ ] Test level 0 pirate: 20 HP, no armor pieces
- [ ] Test level 4 pirate: 40 HP, all armor pieces
- [ ] Test armor removal at 80%/60%/40%/20% health
- [ ] Test NBT load/save with armor pieces
- [ ] Verify client/server parity

## Health Thresholds (Deterministic)

| Health Range | Armor Pieces Equipped |
|--------------|----------------------|
| 80-100% | All 4 (boots, helmet, legs, chestplate) |
| 60-79% | 3 pieces (helmet, legs, chestplate) |
| 40-59% | 2 pieces (legs, chestplate) |
| 20-39% | 1 piece (chestplate only) |
| 0-19% | None |

## Benefits

- **Deterministic armor**: Armor state is purely a function of health ratio
- **No NBT bloat**: Don't need to save armor state separately
- **Consistent behavior**: All EntityHumanBase subclasses behave identically
- **No breaking changes**: EntityPirateArcher, EntityIceArcher, EntityPirateBoss unchanged
- **Predictable**: Level 0 = no armor, Level 4 = full armor, health-based removal