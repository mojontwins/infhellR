package net.minecraft.game.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.game.entity.monster.EntityTFRedcap;
import net.minecraft.game.entity.monster.EntityTFSwarmSpider;
import net.minecraft.game.entity.monster.EntityTFWraith;
import net.minecraft.game.entity.monster.EntityHauntedCow;
import net.minecraft.game.entity.monster.EntitySecretBoss;
import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.entity.misc.EntityMovingPiston;
import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.entity.animal.EntityCatBlack;
import net.minecraft.game.entity.animal.EntityCatRed;
import net.minecraft.game.entity.animal.EntityCatSiamese;
import net.minecraft.game.entity.misc.EntityTriton;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.amazonvillage.EntityAmazon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.animal.EntityChicken;
import net.minecraft.game.entity.animal.EntityChickenBlack;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.animal.EntityCow;
import net.minecraft.game.entity.animal.EntityMooshroom;
import net.minecraft.game.entity.animal.EntityPig;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.animal.EntitySquid;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.human.EntityAlphaWitch;
import net.minecraft.game.entity.human.EntityCowman;
import net.minecraft.game.entity.human.EntityIceArcher;
import net.minecraft.game.entity.human.EntityIceBoss;
import net.minecraft.game.entity.human.EntityIceWarrior;
import net.minecraft.game.entity.human.EntityPigman;
import net.minecraft.game.entity.human.EntityPirate;
import net.minecraft.game.entity.human.EntityPirateArcher;
import net.minecraft.game.entity.human.EntityPirateBoss;
import net.minecraft.game.entity.misc.EntityBoat;
import net.minecraft.game.entity.misc.EntityFallingSand;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.entity.misc.EntityTNTPrimed;
import net.minecraft.game.entity.monster.EntityBuilderZombie;
import net.minecraft.game.entity.monster.EntityCityHusk;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityDrowned;
import net.minecraft.game.entity.monster.EntityElementalCreeper;
import net.minecraft.game.entity.monster.EntityExplodingZombie;
import net.minecraft.game.entity.monster.EntityGhast;
import net.minecraft.game.entity.monster.EntityGiantZombie;
import net.minecraft.game.entity.monster.EntityHusk;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntityPigZombie;
import net.minecraft.game.entity.monster.EntityPigZombieVolcanoes;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityToxicZombie;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityPebble;
import net.minecraft.game.entity.projectile.EntitySnowball;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;

public class EntityList {
	private static Map<String,Class<?>> stringToClassMapping = new HashMap<String,Class<?>>();
	private static Map<Class<?>,String> classToStringMapping = new HashMap<Class<?>,String>();
	private static Map<Integer,Class<?>> IDtoClassMapping = new HashMap<Integer,Class<?>>();
	private static Map<Class<?>,Integer> classToIDMapping = new HashMap<Class<?>,Integer>();

	private static void addMapping(Class<?> class0, String string1, int i2) {
		stringToClassMapping.put(string1, class0);
		classToStringMapping.put(class0, string1);
		IDtoClassMapping.put(i2, class0);
		classToIDMapping.put(class0, i2);
	}

	public static Entity createEntityByName(String string0, World world1) {
		Entity entity2 = null;

		try {
			Class<?> class3 = (Class<?>)stringToClassMapping.get(string0);
			if(class3 != null) {
				entity2 = (Entity)class3.getConstructor(new Class[]{World.class}).newInstance(new Object[]{world1});
			}
		} catch (Exception exception4) {
			exception4.printStackTrace();
		}

		return entity2;
	}

	public static Entity createEntityFromNBT(NBTTagCompound nBTTagCompound0, World world1) {
		Entity entity2 = null;

		try {
			Class<?> class3 = (Class<?>)stringToClassMapping.get(nBTTagCompound0.getString("id"));
			if(class3 != null) {
				entity2 = (Entity)class3.getConstructor(new Class[]{World.class}).newInstance(new Object[]{world1});
			}
		} catch (Exception exception4) {
			exception4.printStackTrace();
		}

		if(entity2 != null) {
			entity2.readFromNBT(nBTTagCompound0);
		} else {
			System.out.println("Skipping Entity with id " + nBTTagCompound0.getString("id"));
		}

		return entity2;
	}

	public static Entity createEntityByID(int i0, World world1) {
		Entity entity2 = null;

		try {
			Class<?> class3 = (Class<?>)IDtoClassMapping.get(i0);
			if(class3 != null) {
				entity2 = (Entity)class3.getConstructor(new Class[]{World.class}).newInstance(new Object[]{world1});
			}
		} catch (Exception exception4) {
			exception4.printStackTrace();
		}

		if(entity2 == null) {
			System.out.println("Skipping Entity with id " + i0);
		}

		return entity2;
	}

	public static int getEntityID(Entity entity0) {
		return ((Integer)classToIDMapping.get(entity0.getClass())).intValue();
	}

	public static String getEntityString(Entity entity0) {
		return (String)classToStringMapping.get(entity0.getClass());
	}
	
	public static List<String> getAllEntityStrings() {
		List<String> result = new ArrayList<String>();
		Iterator<String> it = stringToClassMapping.keySet().iterator();
		while(it.hasNext()) result.add(it.next());
		return result;
	}

	static {
		addMapping(EntityArrow.class, "Arrow", 10);
		addMapping(EntitySnowball.class, "Snowball", 11);
		addMapping(EntityItem.class, "Item", 1);
		addMapping(EntityPainting.class, "Painting", 9);
		addMapping(EntityLiving.class, "Mob", 48);
		addMapping(EntityMob.class, "Monster", 49);
		addMapping(EntityCreeper.class, "Creeper", 50);
		addMapping(EntitySkeleton.class, "Skeleton", 51);
		addMapping(EntitySpider.class, "Spider", 52);
		addMapping(EntityGiantZombie.class, "Giant", 53);
		addMapping(EntityZombie.class, "Zombie", 54);
		addMapping(EntitySlime.class, "Slime", 55);
		addMapping(EntityGhast.class, "Ghast", 56);
		addMapping(EntityPigZombie.class, "PigZombie", 57);
		addMapping(EntityPig.class, "Pig", 90);
		addMapping(EntitySheep.class, "Sheep", 91);
		addMapping(EntityCow.class, "Cow", 92);
		addMapping(EntityChicken.class, "Chicken", 93);
		addMapping(EntitySquid.class, "Squid", 94);
		addMapping(EntityWolf.class, "Wolf", 95);
		addMapping(EntityTNTPrimed.class, "PrimedTnt", 20);
		addMapping(EntityFallingSand.class, "FallingSand", 21);
		addMapping(EntityMinecart.class, "Minecart", 40);
		addMapping(EntityBoat.class, "Boat", 41);
		
		// Mine
		addMapping(EntityMeatBlock.class, "MeatBlock", 100);
		addMapping(EntityPebble.class, "Pebble", 101);
		addMapping(EntityIceSkeleton.class, "IceSkeleton", 102);
		addMapping(EntityHusk.class, "Husk", 103);
		addMapping(EntityDrowned.class, "Drowned", 104);
		addMapping(EntityZombieAlex.class, "ZombieAlex", 105);
		addMapping(EntityCityHusk.class, "CityHusk", 106);
		addMapping(EntityToxicZombie.class, "ToxicZombie", 107);
		addMapping(EntityExplodingZombie.class, "ExplodingZombie", 108);
		addMapping(EntityBuilderZombie.class, "BuilderZombie", 109);
		addMapping(EntityElementalCreeper.class, "ElementalCreeper", 110);
		addMapping(EntityChickenBlack.class, "ChickenBlack", 111);
		addMapping(EntityAlphaWitch.class, "AlphaWitch", 112);
		addMapping(EntityThrowablePotion.class, "ThrowablePotion", 113);
		addMapping(EntityColdCow.class, "ColdCow", 114);
		addMapping(EntityBetaOcelot.class, "BetaOcelot", 115);
		addMapping(EntityCatBlack.class, "BlackCat", 116);
		addMapping(EntityCatRed.class, "RedCat", 117);
		addMapping(EntityCatSiamese.class, "SiameseCat", 118);
		addMapping(EntityAmazon.class, "Amazon", 120);
		
		// Vanilla Release
		addMapping(EntityMooshroom.class, "MooshroomCow", 96);
		
		// Twilight Forest
		addMapping(EntityTFRedcap.class, "Redcap", 30);
		addMapping(EntityTFSwarmSpider.class, "SwarmSpider", 31);
		addMapping(EntityTFWraith.class, "TwilightWraith", 32);
		
		// Better Dungeons
		addMapping(EntityPirate.class, "Pirate", 70);
		addMapping(EntityPirateArcher.class, "PirateArcher", 71);
		addMapping(EntityPirateBoss.class, "PirateBoss", 72);
		addMapping(EntitySecretBoss.class, "SlimeBoss", 73);
		
		// Classic pistons
		addMapping(EntityMovingPiston.class, "MovingPiston", 119);
		
		// More stuff
		addMapping(EntityTriton.class, "Triton", 120);
		addMapping(EntityHauntedCow.class, "HauntedCow", 121);
		
		// Ice palace
		addMapping(EntityIceWarrior.class, "IceWarrior", 122);
		addMapping(EntityIceArcher.class, "IceArcher", 123);
		addMapping(EntityIceBoss.class, "IceBoss", 124);
		
		addMapping(EntityPigZombieVolcanoes.class, "PigZombieVolcanoes", 125);
		
		// Trading
		addMapping(EntityPigman.class, "Pigman", 126);
		addMapping(EntityCowman.class, "Cowman", 127);
		
		// MoCreatures
		addMapping(EntityGoat.class, "Goat", 150);
		
	}
}
