package net.minecraft.game.achievements;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.block.Block;

public class AchievementList {
	public static int minDisplayColumn;
	public static int minDisplayRow;
	public static int maxDisplayColumn;
	public static int maxDisplayRow;
	public static List<Achievement> achievementList = new ArrayList<Achievement>();
	public static Achievement openInventory = (new Achievement(0, "openInventory",   0,  0, Item.book, (Achievement)null)).setIndependent().registerAchievement();
	public static Achievement mineWood = (new Achievement(1, "mineWood",             2,  1, Block.wood, openInventory)).registerAchievement();
	public static Achievement buildWorkBench = (new Achievement(2, "buildWorkBench", 4, -1, Block.workbench, mineWood)).registerAchievement();
	public static Achievement buildPickaxe = (new Achievement(3, "buildPickaxe",     4,  2, Item.pickaxeWood, buildWorkBench)).registerAchievement();
	public static Achievement buildFurnace = (new Achievement(4, "buildFurnace",     3,  4, Block.stoneOvenActive, buildPickaxe)).registerAchievement();
	public static Achievement acquireIron = (new Achievement(5, "acquireIron",       1,  4, Item.ingotIron, buildFurnace)).registerAchievement();
	public static Achievement buildHoe = (new Achievement(6, "buildHoe",             2, -3, Item.hoeWood, buildWorkBench)).registerAchievement();
	public static Achievement makeBread = (new Achievement(7, "makeBread",          -1, -3, Item.bread, buildHoe)).registerAchievement();
	public static Achievement bakeCake = (new Achievement(8, "bakeCake",             0, -5, Item.cake, buildHoe)).registerAchievement();
	public static Achievement buildBetterPickaxe = (new Achievement(9, "buildBetterPickaxe", 6, 2, Item.pickaxeStone, buildPickaxe)).registerAchievement();
	public static Achievement cookFish = (new Achievement(10, "cookFish",            2,  6, Item.fishCooked, buildFurnace)).registerAchievement();
	public static Achievement onARail = (new Achievement(11, "onARail",              2,  3, Block.rail, acquireIron)).setSpecial().registerAchievement();
	public static Achievement buildSword = (new Achievement(12, "buildSword",        6, -1, Item.swordWood, buildWorkBench)).registerAchievement();
	public static Achievement killEnemy = (new Achievement(13, "killEnemy",          8, -1, Item.bone, buildSword)).registerAchievement();
	public static Achievement killCow = (new Achievement(14, "killCow",              7, -3, Item.leather, buildSword)).registerAchievement();
	public static Achievement flyPig = (new Achievement(15, "flyPig",                8, -4, Item.saddle, killCow)).setSpecial().registerAchievement();
	
	public static Achievement getStatus = (new Achievement(16, "getStatus",         -2,  0, Item.potionEmpty, (Achievement) null)).setIndependent().registerAchievement();
	public static Achievement diamonds = (new Achievement(17, "diamonds",           -1,  5, Item.diamond, acquireIron)).registerAchievement();
	
	public static Achievement currency = (new Achievement(18, "currency",           -1,  6, Item.ruby, acquireIron)).registerAchievement();	
	public static Achievement hireAmazon = (new Achievement(19, "hireAmazon",       -2,  6, Item.amazonFace, currency)).registerAchievement();
	public static Achievement bootsOfLeather = (new Achievement(20, "bootsOfLeather",8, -3, Item.bootsLeather, killCow)).setSpecial().registerAchievement();
	public static Achievement bangBang = (new Achievement(21, "bangBang",            9, -1, Item.slingshot, killEnemy)).registerAchievement();
	public static Achievement slimeBoss = (new Achievement(22, "slimeBoss",          9,  0, Item.slimeBossFace, killEnemy)).setSpecial().registerAchievement();
	public static Achievement chestRobber = (new Achievement(23, "chestRobber",     -2,  2, Block.chest, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	
	public static Achievement gotDizzy = (new Achievement(24, "gotDizzy",           -4,  7, Item.dizzy, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	public static Achievement cryingObsidian = (new Achievement(25, "cryingObsidian", -4, 5, Block.cryingObsidian, diamonds)).registerAchievement();
	public static Achievement usedCryingObsidianWand = (new Achievement(26, "usedCryingObsidianWand", -6, 5, Item.cryingObsidianWand, cryingObsidian)).setSpecial().registerAchievement();
	
	public static Achievement pirateKill = (new Achievement(27, "pirateKill",        7,  1, Item.pirateFace, buildSword)).registerAchievement();
	public static Achievement pirateDress = (new Achievement(28, "pirateDress",     10,  1, Item.helmetPirate, pirateKill)).registerAchievement();
	public static Achievement pirateKing = (new Achievement(29, "pirateKing",        8,  3, Item.pirateSigil, pirateKill)).registerAchievement();
	public static Achievement pirateCrown = (new Achievement(30, "pirateCrown",      8,  5, Item.pirateCrown, pirateKing)).setSpecial().registerAchievement();

	public static Achievement gotChilly = (new Achievement(31, "gotChilly",         -3, -2, Block.ice, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	public static Achievement warmed = (new Achievement(32, "warmed",               -5, -2, Block.fire, gotChilly)).registerAchievement();
	public static Achievement iceKiller = (new Achievement(33, "iceKiller",         -4, -4, Item.bone, gotChilly)).registerAchievement();
	public static Achievement dressedInRags = (new Achievement(34, "dressedInRags", -6, -4, Item.plateRags, iceKiller)).setSpecial().registerAchievement();
	public static Achievement warmedByOven = (new Achievement(35, "warmedByOven",   -5, -1, Block.stoneOvenActive, gotChilly)).registerAchievement();
	
	public static Achievement robbedAmazon = (new Achievement(36, "robbedAmazon",    1, -4, Block.chest, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	public static Achievement robbedWitch = (new Achievement(37, "robbedWitch",      3, -4, Block.chest, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	
	public static Achievement iceBoss = (new Achievement(38, "iceBoss",             -2, -5, Item.iceFace, (Achievement) null)).setSpecial().setIndependent().registerAchievement();
	
	public static Achievement zombie = (new Achievement(39, "zombie",                6, -5, Item.zombieFace, buildSword)).setSpecial().registerAchievement();
	public static Achievement zombieMeat = (new Achievement(40, "zombieMeat",        8, -6, Item.rottenFlesh, zombie)).registerAchievement();
	public static Achievement meatBlock = (new Achievement(41, "meatBlock",         10, -6, Block.fleshBlock, zombieMeat)).registerAchievement();
	
	//public static Achievement lightningRod = (new Achievement(43, "lightningRod",    0,  3, Block.blockSteel, acquireIron)).registerAchievement();
	
	public static Achievement gotCompass = (new Achievement(44, "gotCompass",        1,  8, Item.compass, acquireIron)).registerAchievement();
	public static Achievement gotRecoveryCompass = (new Achievement(45, "gotRecoveryCompass", 1, 9, Item.recoveryCompass, acquireIron)).registerAchievement();
	
	public static Achievement bucketMilkCow = (new Achievement(46, "bucketMilkCow", -1,  8, Item.bucketMilk, acquireIron)).registerAchievement();
	public static Achievement bucketMilkGoat = (new Achievement(47, "bucketMilkGoat", -1,  9, Item.bucketMilk, acquireIron)).registerAchievement();
	public static Achievement bucketMilkCursed = (new Achievement(48, "bucketMilkCursed", -1, 10, Item.bucketMilk, acquireIron)).registerAchievement();
	
	public static Achievement cursedCow = (new Achievement(49, "cursedCow", 6, 4, Item.cursedCow, buildPickaxe)).registerAchievement();
	
	public static void func_27374_a() {
	}

	static {
		System.out.println(achievementList.size() + " achievements");
	}
}
