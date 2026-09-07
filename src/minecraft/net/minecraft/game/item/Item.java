package net.minecraft.game.item;

import java.util.List;
import java.util.Random;

import net.minecraft.game.entity.status.Status;
import net.minecraft.game.worldedit.ItemMagicWand;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EnumAction;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class Item {
	protected static Random rand = new Random();
	public static Item[] itemsList = new Item[32000];
	public static Item shovelSteel = (new ItemSpade(0, EnumToolMaterial.IRON, false)).setIconCoord(2, 5).setItemName("shovelIron");
	public static Item pickaxeSteel = (new ItemPickaxe(1, EnumToolMaterial.IRON, false)).setIconCoord(2, 6).setItemName("pickaxeIron");
	public static Item axeSteel = (new ItemAxe(2, EnumToolMaterial.IRON, false)).setIconCoord(2, 7).setItemName("hatchetIron");
	public static Item flintAndSteel = (new ItemFlintAndSteel(3)).setIconCoord(5, 0).setItemName("flintAndSteel");
	public static Item appleRed = (new ItemFood(4, 4, false, false)).setIconCoord(10, 0).setItemName("apple");
	public static Item bow = (new ItemBow(5)).setIconCoord(5, 1).setItemName("bow");
	public static Item arrow = (new Item(6)).setIconCoord(5, 2).setItemName("arrow").setCreativeTab(CreativeTabs.tabCombat);
	public static Item coal = (new ItemCoal(7)).setIconCoord(7, 0).setItemName("coal");
	public static Item diamond = (new Item(8)).setIconCoord(7, 3).setItemName("diamond").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item ingotIron = (new Item(9)).setIconCoord(7, 1).setItemName("ingotIron").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item ingotGold = (new ItemGolden(10)).setIconCoord(7, 2).setItemName("ingotGold").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item swordSteel = (new ItemSword(11, EnumToolMaterial.IRON, false)).setIconCoord(2, 4).setItemName("swordIron");
	public static Item swordWood = (new ItemSword(12, EnumToolMaterial.WOOD, false)).setIconCoord(0, 4).setItemName("swordWood");
	public static Item shovelWood = (new ItemSpade(13, EnumToolMaterial.WOOD, false)).setIconCoord(0, 5).setItemName("shovelWood");
	public static Item pickaxeWood = (new ItemPickaxe(14, EnumToolMaterial.WOOD, false)).setIconCoord(0, 6).setItemName("pickaxeWood");
	public static Item axeWood = (new ItemAxe(15, EnumToolMaterial.WOOD, false)).setIconCoord(0, 7).setItemName("hatchetWood");
	public static Item swordStone = (new ItemSword(16, EnumToolMaterial.STONE, false)).setIconCoord(1, 4).setItemName("swordStone");
	public static Item shovelStone = (new ItemSpade(17, EnumToolMaterial.STONE, false)).setIconCoord(1, 5).setItemName("shovelStone");
	public static Item pickaxeStone = (new ItemPickaxe(18, EnumToolMaterial.STONE, false)).setIconCoord(1, 6).setItemName("pickaxeStone");
	public static Item axeStone = (new ItemAxe(19, EnumToolMaterial.STONE, false)).setIconCoord(1, 7).setItemName("hatchetStone");
	public static Item swordDiamond = (new ItemSword(20, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 4).setItemName("swordDiamond");
	public static Item shovelDiamond = (new ItemSpade(21, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 5).setItemName("shovelDiamond");
	public static Item pickaxeDiamond = (new ItemPickaxe(22, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 6).setItemName("pickaxeDiamond");
	public static Item axeDiamond = (new ItemAxe(23, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 7).setItemName("hatchetDiamond");
	public static Item stick = (new Item(24)).setIconCoord(5, 3).setFull3D().setItemName("stick").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item bowlEmpty = (new Item(25)).setIconCoord(7, 4).setItemName("bowl").setCreativeTab(CreativeTabs.tabFood);
	public static Item bowlSoup = (new ItemSoup(26, 10)).setIconCoord(8, 4).setItemName("mushroomStew");
	public static Item swordGold = (new ItemSword(27, EnumToolMaterial.GOLD, true)).setIconCoord(4, 4).setItemName("swordGold");
	public static Item shovelGold = (new ItemSpade(28, EnumToolMaterial.GOLD, true)).setIconCoord(4, 5).setItemName("shovelGold");
	public static Item pickaxeGold = (new ItemPickaxe(29, EnumToolMaterial.GOLD, true)).setIconCoord(4, 6).setItemName("pickaxeGold");
	public static Item axeGold = (new ItemAxe(30, EnumToolMaterial.GOLD, true)).setIconCoord(4, 7).setItemName("hatchetGold");
	public static Item silk = (new Item(31)).setIconCoord(8, 0).setItemName("string").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item feather = (new Item(32)).setIconCoord(8, 1).setItemName("feather").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item gunpowder = (new Item(33)).setIconCoord(8, 2).setItemName("sulphur").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item hoeWood = (new ItemHoe(34, EnumToolMaterial.WOOD)).setIconCoord(0, 8).setItemName("hoeWood");
	public static Item hoeStone = (new ItemHoe(35, EnumToolMaterial.STONE)).setIconCoord(1, 8).setItemName("hoeStone");
	public static Item hoeSteel = (new ItemHoe(36, EnumToolMaterial.IRON)).setIconCoord(2, 8).setItemName("hoeIron");
	public static Item hoeDiamond = (new ItemHoe(37, EnumToolMaterial.EMERALD)).setIconCoord(3, 8).setItemName("hoeDiamond");
	public static Item hoeGold = (new ItemHoe(38, EnumToolMaterial.GOLD)).setIconCoord(4, 8).setItemName("hoeGold");
	public static Item seeds = (new ItemSeeds(39, Block.crops.blockID)).setIconCoord(9, 0).setItemName("seeds");
	public static Item wheat = (new Item(40)).setIconCoord(9, 1).setItemName("wheat").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item bread = (new ItemFood(41, 5, false, false)).setIconCoord(9, 2).setItemName("bread");
	public static Item helmetLeather = (new ItemArmor(42, 0, 0, 0)).setIconCoord(0, 0).setItemName("helmetCloth");
	public static Item plateLeather = (new ItemArmor(43, 0, 0, 1)).setIconCoord(0, 1).setItemName("chestplateCloth");
	public static Item legsLeather = (new ItemArmor(44, 0, 0, 2)).setIconCoord(0, 2).setItemName("leggingsCloth");
	public static Item bootsLeather = (new ItemArmor(45, 0, 0, 3)).setIconCoord(0, 3).setItemName("bootsCloth");
	public static Item helmetChain = (new ItemArmor(46, 1, 1, 0)).setIconCoord(1, 0).setItemName("helmetChain");
	public static Item plateChain = (new ItemArmor(47, 1, 1, 1)).setIconCoord(1, 1).setItemName("chestplateChain");
	public static Item legsChain = (new ItemArmor(48, 1, 1, 2)).setIconCoord(1, 2).setItemName("leggingsChain");
	public static Item bootsChain = (new ItemArmor(49, 1, 1, 3)).setIconCoord(1, 3).setItemName("bootsChain");
	public static Item helmetSteel = (new ItemArmor(50, 2, 2, 0)).setIconCoord(2, 0).setItemName("helmetIron");
	public static Item plateSteel = (new ItemArmor(51, 2, 2, 1)).setIconCoord(2, 1).setItemName("chestplateIron");
	public static Item legsSteel = (new ItemArmor(52, 2, 2, 2)).setIconCoord(2, 2).setItemName("leggingsIron");
	public static Item bootsSteel = (new ItemArmor(53, 2, 2, 3)).setIconCoord(2, 3).setItemName("bootsIron");
	public static Item helmetDiamond = (new ItemArmor(54, 3, 3, 0)).setIconCoord(3, 0).setItemName("helmetDiamond");
	public static Item plateDiamond = (new ItemArmor(55, 3, 3, 1)).setIconCoord(3, 1).setItemName("chestplateDiamond");
	public static Item legsDiamond = (new ItemArmor(56, 3, 3, 2)).setIconCoord(3, 2).setItemName("leggingsDiamond");
	public static Item bootsDiamond = (new ItemArmor(57, 3, 3, 3)).setIconCoord(3, 3).setItemName("bootsDiamond");
	public static Item helmetGold = (new ItemArmor(58, 1, 4, 0)).setIconCoord(4, 0).setItemName("helmetGold");
	public static Item plateGold = (new ItemArmor(59, 1, 4, 1)).setIconCoord(4, 1).setItemName("chestplateGold");
	public static Item legsGold = (new ItemArmor(60, 1, 4, 2)).setIconCoord(4, 2).setItemName("leggingsGold");
	public static Item bootsGold = (new ItemArmor(61, 1, 4, 3)).setIconCoord(4, 3).setItemName("bootsGold");
	public static Item flint = (new Item(62)).setIconCoord(6, 0).setItemName("flint").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item porkRaw = (new ItemFood(63, 3, true, false)).setIconCoord(7, 5).setItemName("porkchopRaw");
	public static Item porkCooked = (new ItemFood(64, 8, true, false)).setIconCoord(8, 5).setItemName("porkchopCooked");
	public static Item painting = (new ItemPainting(65)).setIconCoord(10, 1).setItemName("painting");
	public static Item appleGold = (new ItemFood(66, 42, false, true)).setIconCoord(11, 0).setItemName("appleGold");
	public static Item sign = (new ItemSign(67)).setIconCoord(10, 2).setItemName("sign");
	public static Item doorWood = (new ItemDoor(68, Material.wood)).setIconCoord(11, 2).setItemName("doorWood");
	public static Item bucketEmpty = (new ItemBucket(69, 0)).setIconCoord(10, 4).setItemName("bucket");
	public static Item bucketWater = (new ItemBucket(70, Block.waterMoving.blockID)).setIconCoord(11, 4).setItemName("bucketWater").setContainerItem(bucketEmpty);
	public static Item bucketLava = (new ItemBucket(71, Block.lavaMoving.blockID)).setIconCoord(12, 4).setItemName("bucketLava").setContainerItem(bucketEmpty);
	public static Item minecartEmpty = (new ItemMinecart(72, 0)).setIconCoord(7, 8).setItemName("minecart");
	public static Item saddle = (new ItemSaddle(73)).setIconCoord(8, 6).setItemName("saddle");
	public static Item doorSteel = (new ItemDoor(74, Material.iron)).setIconCoord(12, 2).setItemName("doorIron");
	public static Item redstone = (new ItemRedstone(75)).setIconCoord(8, 3).setItemName("redstone");
	public static Item snowball = (new ItemSnowball(76)).setIconCoord(14, 0).setItemName("snowball");
	public static Item boat = (new ItemBoat(77)).setIconCoord(8, 8).setItemName("boat");
	public static Item leather = (new Item(78)).setIconCoord(7, 6).setItemName("leather").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item bucketMilk = (new ItemBucketMilk(79)).setIconCoord(13, 4).setItemName("milk").setContainerItem(bucketEmpty);
	public static Item brick = (new Item(80)).setIconCoord(6, 1).setItemName("brick").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item clay = (new Item(81)).setIconCoord(9, 3).setItemName("clay").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item reed = (new ItemReed(82, Block.reed)).setIconCoord(11, 1).setItemName("reeds").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item paper = (new Item(83)).setIconCoord(10, 3).setItemName("paper").setCreativeTab(CreativeTabs.tabMisc);
	public static Item book = (new Item(84)).setIconCoord(11, 3).setItemName("book").setCreativeTab(CreativeTabs.tabMisc);
	public static Item slimeBall = (new Item(85)).setIconCoord(14, 1).setItemName("slimeball").setCreativeTab(CreativeTabs.tabMisc);
	public static Item minecartCrate = (new ItemMinecart(86, 1)).setIconCoord(7, 9).setItemName("minecartChest");
	public static Item minecartPowered = (new ItemMinecart(87, 2)).setIconCoord(7, 10).setItemName("minecartFurnace");
	public static Item egg = (new ItemEgg(88)).setIconCoord(12, 0).setItemName("egg");
	public static Item compass = (new Item(89)).setIconCoord(6, 3).setItemName("compass").setCreativeTab(CreativeTabs.tabTools);
	public static Item fishingRod = (new ItemFishingRod(90)).setIconCoord(5, 4).setItemName("fishingRod");
	public static Item pocketSundial = (new Item(91)).setIconCoord(6, 4).setItemName("clock").setCreativeTab(CreativeTabs.tabTools);
	public static Item lightStoneDust = (new Item(92)).setIconCoord(9, 4).setItemName("yellowDust").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item fishRaw = (new ItemFood(93, 2, false, true)).setIconCoord(9, 5).setItemName("fishRaw");
	public static Item fishCooked = (new ItemFood(94, 5, false, true)).setIconCoord(10, 5).setItemName("fishCooked");
	public static Item dyePowder = (new ItemDye(95)).setIconCoord(14, 4).setItemName("dyePowder");
	public static Item bone = (new Item(96)).setIconCoord(12, 1).setItemName("bone").setFull3D().setCreativeTab(CreativeTabs.tabMisc);
	public static Item sugar = (new Item(97)).setIconCoord(13, 0).setItemName("sugar").setFull3D().setCreativeTab(CreativeTabs.tabMaterials);
	public static Item cake = (new ItemReed(98, Block.cake)).setMaxStackSize(1).setIconCoord(13, 1).setItemName("cake").setCreativeTab(CreativeTabs.tabFood);
	public static Item bed = (new ItemBed(99)).setMaxStackSize(1).setIconCoord(13, 2).setItemName("bed");
	public static Item redstoneRepeater = (new ItemReed(100, Block.redstoneRepeaterIdle)).setIconCoord(6, 5).setItemName("diode").setCreativeTab(CreativeTabs.tabRedstone);
	public static Item cookie = (new ItemCookie(101, 1, false, 8)).setIconCoord(12, 5).setItemName("cookie");
	public static ItemMap mapItem = (ItemMap)(new ItemMap(102)).setIconCoord(12, 3).setItemName("map");
	public static ItemShears shears = (ItemShears)(new ItemShears(103)).setIconCoord(13, 5).setItemName("shears");
	
	public static Item beefRaw = (new ItemFood(107, 3, true, false)).setIconCoord(9, 6).setItemName("beefRaw");
	public static Item beefCooked = (new ItemFood(108, 8, true, false)).setIconCoord(10, 6).setItemName("beefCooked");
	
	public static Item record13 = (new ItemRecord(2000, "13")).setIconCoord(0, 15).setItemName("record");
	public static Item recordCat = (new ItemRecord(2001, "cat")).setIconCoord(1, 15).setItemName("record");

	// Custom items
	public static Item chickenRaw = (ItemFood)(new ItemFood(109, 2, true, true)).setIconIndex(121).setStatusEffect(Status.statusPoisoned, 50, 1).setItemName("chickenRaw");
	public static Item chickenCooked = (ItemFood)(new ItemFood(110, 6, true, true)).setIconIndex(122).setItemName("chickenCooked");
	public static Item emerald = (new Item(132)).setIconIndex(11*16 + 10).setItemName("emerald").setCreativeTab(CreativeTabs.tabMaterials);
	
	public static Item ironWire = (new Item(1000)).setIconIndex(14*16 + 14).setItemName("ironWire").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item pebble = (new Item(1001)).setIconIndex(14*16 + 15).setItemName("pebble").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item slingshot = (new ItemSlingshot(1002)).setIconIndex(14*16 + 13).setItemName("slingshot");
	public static Item ruby = (new Item(1003)).setIconIndex(10*16 + 10).setItemName("ruby").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item kelp = (new Item(1004)).setIconIndex(14*16 + 12).setItemName("kelp").setCreativeTab(CreativeTabs.tabFood);
	public static Item driedKelp =  (new ItemFood(1005, 2, false, true)).setIconIndex(14*16 + 11).setItemName("driedKelp");
	public static Item pirateSigil = (new Item(1006)).setIconIndex(14*16 + 10).setItemName("pirateSigil");
	public static Item helmetPirate = (new ItemArmor(1007, 1, ItemArmor.PIRATE, 0)).setIconIndex(14*16 + 9).setItemName("helmetPirate");
	public static Item platePirate = (new ItemArmor(1008, 1, ItemArmor.PIRATE, 1)).setIconIndex(14*16 + 8).setItemName("platePirate");
	public static Item legsPirate = (new ItemArmor(1009, 1, ItemArmor.PIRATE, 2)).setIconIndex(14*16 + 7).setItemName("legsPirate");
	public static Item bootsPirate = (new ItemArmor(1010, 1, ItemArmor.PIRATE, 3)).setIconIndex(14*16 + 6).setItemName("bootsPirate");
	
	public static Item pumpkinPie = (new ItemFood(1011, 6, false, false)).setIconIndex(14*16 + 5).setItemName("pumpkinPie");
	public static Item friedEgg = (new ItemFood(1013, 5, false, true)).setIconIndex(14*16 + 4).setItemName("friedEgg");
	public static Item rottenFlesh = (new ItemFood(1015, 2, true, false)).setIconIndex(5*16 + 11).setStatusEffect(Status.statusDizzy, 200, 1).setItemName("rottenFlesh");
	
	public static Item helmetRags = (new ItemArmor(1016, 1, ItemArmor.RAGS, 0)).setIconIndex(13*16 + 9).setItemName("helmetRags");
	public static Item plateRags = (new ItemArmor(1017, 1, ItemArmor.RAGS, 1)).setIconIndex(13*16 + 8).setItemName("plateRags");
	public static Item legsRags = (new ItemArmor(1018, 1, ItemArmor.RAGS, 2)).setIconIndex(13*16 + 7).setItemName("legsRags");
	
	public static Item amazonFace = new Item(1019).setIconIndex(14*16 + 2).setShowInCreative(false);
	public static Item dizzy = new Item(1020).setIconIndex(14*16).setShowInCreative(false);
	public static Item cryingObsidianWand = (new ItemCryingObsidianWand(1021)).setIconIndex(13*16 + 15).setItemName("cryingObsidianWand");
	public static Item pirateFace = new Item(1022).setIconIndex(13*16 + 14).setShowInCreative(false);
	public static Item pirateCrown = new Item(1023).setIconIndex(13*16 + 13).setItemName("pirateCrown");
	public static Item mail = new Item(1024).setIconIndex(12*16 + 15).setItemName("mail").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item iceFace = new Item(1025).setIconIndex(13*16 + 12).setShowInCreative(false);
	public static Item zombieFace = new Item(1026).setIconIndex(13*16 + 6).setShowInCreative(false);
	public static Item straw = new Item(1027).setIconIndex(13*16 + 11).setItemName("straw").setCreativeTab(CreativeTabs.tabMisc);
	public static Item recoveryCompass = (new Item(1028)).setIconCoord(5, 13).setItemName("recoveryCompass").setCreativeTab(CreativeTabs.tabTools);
	public static Item obsidianShard = new Item(1029).setIconCoord(4, 13).setItemName("obsidianShard").setCreativeTab(CreativeTabs.tabMaterials);
	public static Item cursedCow = new Item(1030).setIconCoord(3, 13).setItemName("cursedCow").setShowInCreative(false);
	public static Item nametagSimple = new ItemNametagSimple(1031).setIconCoord(2,13).setItemName("nametag").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);
	public static Item slimeBossFace = new Item(1032).setIconCoord(1, 13).setItemName("slimeBoss").setShowInCreative(false);
	
	public static Item potionEmpty = new ItemPotion(3000, 0xFFFFFF, ItemPotion.EMPTY).setItemName("potionEmpty");
	public static Item potionPoison = new ItemPotion(3001, 0x30FF30, ItemPotion.POISON).setItemName("potionPoison");
	public static Item potionSlowness = new ItemPotion(3002, 0xA7E8EF, ItemPotion.SLOWNESS).setItemName("potionSlowness");
	public static Item potionAutoHealing = new ItemPotion(3003, 0xFF8989, ItemPotion.AUTOHEALING).setItemName("potionAutoHealing");
	public static Item potionInstantDamage = new ItemPotion(3004, 0xFFFFFF, ItemPotion.INSTANTDAMAGE).setIconIndex(14*16 + 3).setItemName("potionInstantDamage");
	public static Item potionDizzy = new ItemPotion(3005, 0x98A739, ItemPotion.DIZZY).setStatusTime(300).setItemName("potionDizziness");
	
	public static Item ingotCopper = (new Item(401)).setIconIndex(14*16 + 1).setItemName("ingotCopper").setCreativeTab(CreativeTabs.tabMaterials);
	
	public static Item maceDiamond = (new ItemLongSword(420, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 9).setItemName("maceDiamond");
	public static Item hammerDiamond = (new ItemHammer(421, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 10).setItemName("hammerDiamond");
	public static Item battleDiamond = (new ItemBattleAxe(422, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 11).setItemName("battleAxeDiamond");
	public static Item knifeDiamond = (new ItemKnife(423, EnumToolMaterial.EMERALD, false)).setIconCoord(3, 12).setItemName("knifeDiamond");
	
	public static Item maceGold = (new ItemLongSword(424, EnumToolMaterial.GOLD, true)).setIconCoord(4, 9).setItemName("maceGold");
	public static Item hammerGold = (new ItemHammer(425, EnumToolMaterial.GOLD, true)).setIconCoord(4, 10).setItemName("hammerGold");
	public static Item battleGold = (new ItemBattleAxe(426, EnumToolMaterial.GOLD, true)).setIconCoord(4, 11).setItemName("battleAxeGold");
	public static Item knifeGold = (new ItemKnife(427, EnumToolMaterial.GOLD, true)).setIconCoord(4, 12).setItemName("knifeGold");
	
	public static Item maceSteel = (new ItemLongSword(428, EnumToolMaterial.IRON, false)).setIconCoord(2, 9).setItemName("maceSteel");
	public static Item hammerSteel = (new ItemHammer(429, EnumToolMaterial.IRON, false)).setIconCoord(2, 10).setItemName("hammerSteel");
	public static Item battleSteel = (new ItemBattleAxe(430, EnumToolMaterial.IRON, false)).setIconCoord(2, 11).setItemName("battleAxeSteel");
	public static Item knifeSteel = (new ItemKnife(431, EnumToolMaterial.IRON, false)).setIconCoord(2, 12).setItemName("knifeSteel");
	
	public static Item maceStone = (new ItemLongSword(432, EnumToolMaterial.STONE, false)).setIconCoord(1, 9).setItemName("maceStone");
	public static Item hammerStone = (new ItemHammer(433, EnumToolMaterial.STONE, false)).setIconCoord(1, 10).setItemName("hammerStone");
	public static Item battleStone = (new ItemBattleAxe(434, EnumToolMaterial.STONE, false)).setIconCoord(1, 11).setItemName("battleAxeStone");
	public static Item knifeStone = (new ItemKnife(435, EnumToolMaterial.STONE, false)).setIconCoord(1, 12).setItemName("knifeStone");
	
	public static Item battleWood = (new ItemBattleAxe(436, EnumToolMaterial.WOOD, false)).setIconCoord(0, 11).setItemName("battleAxeWood");
	public static Item knifeWood = (new ItemKnife(437, EnumToolMaterial.WOOD, false)).setIconCoord(0, 12).setItemName("knifeWood");
	
	// Wand
	
	public static Item magicWand = (new ItemMagicWand(999-256)).setIconCoord(8, 12).setItemName("magic wand");
	
	public final int shiftedIndex;
	protected int maxStackSize = 64;
	protected int maxDamage = 0;
	public int iconIndex;
	protected boolean bFull3D = false;
	protected boolean hasSubtypes = false;
	private Item containerItem = null;
	private String itemName;

	public boolean silkTouch = false;
		
	protected Status status = null;
	public int statusTime;
	public int statusAmplifier;
	
	protected boolean showInCreative = true;
	
	public CreativeTabs displayOnCreativeTab;
	
	protected static final int armorPieceForTier [][] = new int[][] {
		{ Item.helmetLeather.shiftedIndex, Item.plateLeather.shiftedIndex, Item.legsLeather.shiftedIndex, Item.bootsLeather.shiftedIndex }, 
		{ Item.helmetChain.shiftedIndex, Item.plateChain.shiftedIndex, Item.legsChain.shiftedIndex, Item.bootsChain.shiftedIndex },
		{ Item.helmetGold.shiftedIndex, Item.plateGold.shiftedIndex, Item.legsGold.shiftedIndex, Item.bootsGold.shiftedIndex },
		{ Item.helmetSteel.shiftedIndex, Item.plateSteel.shiftedIndex, Item.legsSteel.shiftedIndex, Item.bootsSteel.shiftedIndex },
		{ Item.helmetDiamond.shiftedIndex, Item.plateDiamond.shiftedIndex, Item.legsDiamond.shiftedIndex, Item.bootsDiamond.shiftedIndex }
	};
	
	protected Item(int i1) {
		this.shiftedIndex = 256 + i1;
		if(itemsList[256 + i1] != null) {
			System.out.println("CONFLICT @ " + i1);
		}

		itemsList[256 + i1] = this;
	}
	
	public boolean isShowInCreative() {
		return this.showInCreative;
	}
	
	public Item setShowInCreative(boolean showInCreative) {
		this.showInCreative = showInCreative;
		return this;
	}
	
	public Item setStatusEffect(Status status, int statusTime, int statusAmplifier) {
		this.status = status;
		this.statusTime = statusTime;
		this.statusAmplifier = statusAmplifier;
		return this;
	}
	
	public Item setStatusTime(int statusTime) {
		this.statusTime = statusTime;
		return this;
	}

	public Item setIconIndex(int i1) {
		this.iconIndex = i1;
		return this;
	}

	public Item setMaxStackSize(int i1) {
		this.maxStackSize = i1;
		return this;
	}

	public Item setIconCoord(int i1, int i2) {
		this.iconIndex = i1 + i2 * 16;
		return this;
	}

	public int getIconFromDamage(int i1) {
		return this.iconIndex;
	}

	public final int getIconIndex(ItemStack itemStack1) {
		return this.getIconFromDamage(itemStack1.getItemDamage());
	}

	public boolean onItemUse(ItemStack itemStack1, EntityPlayer entityPlayer2, World world3, int i4, int i5, int i6, int i7) {
		return false;
	}

	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int face, float xWithinFace, float yWithinFace, float zWithinFace) {
		return this.onItemUse(itemStack, entityPlayer, world, x, y, z, face);
	}
	
	public float getStrVsBlock(ItemStack itemStack1, Block block2) {
		return 1.0F;
	}

	public ItemStack onItemRightClick(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		return itemStack1;
	}

	public ItemStack onFoodEaten(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		return itemStack1;
	}

	public int getItemStackLimit() {
		return this.maxStackSize;
	}

	public int getPlacedBlockMetadata(int i1) {
		return 0;
	}

	public boolean getHasSubtypes() {
		return this.hasSubtypes;
	}

	protected Item setHasSubtypes(boolean z1) {
		this.hasSubtypes = z1;
		return this;
	}

	public int getMaxDamage() {
		return this.maxDamage;
	}

	protected Item setMaxDamage(int i1) {
		this.maxDamage = i1;
		return this;
	}

	public boolean isDamageable() {
		return this.maxDamage > 0 && !this.hasSubtypes;
	}

	public boolean hitEntity(ItemStack itemStack1, EntityLiving entityLiving2, EntityLiving entityLiving3) {
		return false;
	}

	public boolean onBlockDestroyed(ItemStack itemStack1, int i2, int i3, int i4, int i5, EntityLiving entityLiving6) {
		return false;
	}

	public int getDamageVsEntity(Entity entity1) {
		return 1;
	}

	/*
	 * Add knock back when hitting an entity.
	 */
	public float getExtraKnockbackVsEntity(Entity entity) {
		return 0.0F;
	}

	/*
	 * Default swinging speed = 6, less is faster. 
	 */
	public int getSwingSpeed() {
		return 6;
	}

	public boolean canHarvestBlock(Block block1) {
		return false;
	}

	public void saddleEntity(ItemStack itemStack1, EntityLiving entityLiving2) {
	}

	public Item setFull3D() {
		this.bFull3D = true;
		return this;
	}

	public boolean isFull3D() {
		return this.bFull3D;
	}

	public boolean shouldRotateAroundWhenRendering() {
		return false;
	}

	public Item setItemName(String string1) {
		this.itemName = "item." + string1;
		return this;
	}

	public String getItemName() {
		return this.itemName;
	}

	public String getItemNameIS(ItemStack itemStack1) {
		return this.itemName;
	}

	public Item setContainerItem(Item item1) {
		if(this.maxStackSize > 1) {
			throw new IllegalArgumentException("Max stack size must be 1 for items with crafting results");
		} else {
			this.containerItem = item1;
			return this;
		}
	}

	public Item getContainerItem() {
		return this.containerItem;
	}

	public boolean hasContainerItem() {
		return this.containerItem != null;
	}

	public String getStatName() {
		return StatCollector.translateToLocal(this.getItemName() + ".name");
	}

	public int getColorFromDamage(int i1) {
		return 0xFFFFFF;
	}

	public void onUpdate(ItemStack itemStack1, World world2, Entity entity3, int i4, boolean z5) {
	}

	public void onCreated(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
	}

	public boolean hasContents() {
		return false;
	}
	
	public boolean softLocked() {
		return false;
	}
	
	// r1.0+ use item
	
	public EnumAction getItemUseAction(ItemStack itemStack1) {
		return EnumAction.none;
	}

	public int getMaxItemUseDuration(ItemStack itemStack1) {
		return 0;
	}

	public void onPlayerStoppedUsing(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3, int i4) {
	}
	
	public void onUsingItemTick(ItemStack stack, EntityPlayer player, int count) {
	}

	// r1.3+ creative
	
	public Item setCreativeTab(CreativeTabs creativeTab) {
		this.displayOnCreativeTab = creativeTab;
		return this;
	}
	
	public CreativeTabs getCreativeTab() {
		return this.displayOnCreativeTab;
	}
	
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
	}
	
	// 

	static {
		StatList.initStats();
	}
}



