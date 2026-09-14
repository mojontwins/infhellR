# Feature Chances

## Biome Distribution

Source: `src/minecraft/biomelut.png` (64x64 = 4096 pixels)

| Biome | LUT Share |
|---|---:|
| Cold Forest | 14.01% |
| Tundra | 13.92% |
| Forest | 10.57% |
| Taiga | 9.77% |
| Thick Forest | 8.64% |
| Glacier | 6.52% |
| Plains | 6.01% |
| Cold Plains | 4.39% |
| Taiga Border | 3.64% |
| Hot Forest | 3.64% |
| Savanna | 3.59% |
| Willow Forest | 2.78% |
| Mesa | 2.59% |
| Tundra Flat | 2.05% |
| Desert Outskirts | 1.68% |
| Cold Rocky | 1.66% |
| Mesa Surroundings | 1.44% |
| Rocky | 1.07% |
| Desert With Grass | 0.81% |
| Desert | 0.73% |
| Rocky Desert | 0.49% |

## Feature Rarity

Base chance = `getSpawnChance() / 4096` (from `Feature.MAXCHANCE`).

Effective chance = base chance x biome probability.

| Feature | Base Chance | Biome % | Effective Chance | 1 per X chunks |
|---|---:|---:|---:|---:|
| FeatureStoneArch | 500 | 1.07% | 0.131% | 1 per 763 |
| FeatureVolcano | 5 | 3.66% | 0.004% | 1 per 22,370 |
| FeatureAmazonVillage | 6 | 10.57% | 0.015% | 1 per 6,458 |
| FeatureHollowHill | 6 | 10.57% | 0.015% | 1 per 6,458 |
| FeatureWreck | 20 | 6.52% | 0.032% | 1 per 3,142 |
| FeatureIcePalace | 4 | 20.43% | 0.020% | 1 per 5,011 |
| FeatureFossil | 22 | 0.73% | 0.004% | 1 per 25,420 |
| FeatureSlimeBossLair | 1 | 63.60% | 0.016% | 1 per 6,440 |
| FeatureShip | 20 | 36.40% | 0.178% | 1 per 563 |
| FeatureOceanRuins | 5 | 36.40% | 0.044% | 1 per 2,251 |
| FeatureBigShip | 2 | 36.40% | 0.018% | 1 per 5,626 |

## Notes

- Ocean fraction is estimated at 36.4% since ocean is determined by terrain height, not the LUT.
- Mangrove is a special biome override and does not appear in `biomelut.png`.
- Actual spacing also depends on `minimumSeparation()` between features.