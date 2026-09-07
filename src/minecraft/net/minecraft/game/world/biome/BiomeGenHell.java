package net.minecraft.game.world.biome;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.monster.EntityGhast;
import net.minecraft.game.entity.monster.EntityPigZombie;


public class BiomeGenHell extends BiomeGenBase {
	public BiomeGenHell() {
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityGhast.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityPigZombie.class, 10));
	}
}
