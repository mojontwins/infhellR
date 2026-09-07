package net.minecraft.game.achievements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.item.recipe.CraftingManager;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.recipe.FurnaceRecipes;
import net.minecraft.game.item.recipe.IRecipe;

public class StatList {
	protected static Map<Integer,StatBase> oneShotStats = new HashMap<Integer,StatBase>();
	public static List<StatBase> allStats = new ArrayList<StatBase>();
	public static List<StatBase> generalStats = new ArrayList<StatBase>();
	public static List<StatBase> itemStats = new ArrayList<StatBase>();
	public static List<StatBase> objectMineStats = new ArrayList<StatBase>();
	public static StatBase startGameStat = (new StatBasic(1000, StatCollector.translateToLocal("stat.startGame"))).initIndependentStat().registerStat();
	public static StatBase createWorldStat = (new StatBasic(1001, StatCollector.translateToLocal("stat.createWorld"))).initIndependentStat().registerStat();
	public static StatBase loadWorldStat = (new StatBasic(1002, StatCollector.translateToLocal("stat.loadWorld"))).initIndependentStat().registerStat();
	public static StatBase joinMultiplayerStat = (new StatBasic(1003, StatCollector.translateToLocal("stat.joinMultiplayer"))).initIndependentStat().registerStat();
	public static StatBase leaveGameStat = (new StatBasic(1004, StatCollector.translateToLocal("stat.leaveGame"))).initIndependentStat().registerStat();
	public static StatBase minutesPlayedStat = (new StatBasic(1100, StatCollector.translateToLocal("stat.playOneMinute"), StatBase.timeStatType)).initIndependentStat().registerStat();
	public static StatBase distanceWalkedStat = (new StatBasic(2000, StatCollector.translateToLocal("stat.walkOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceSwumStat = (new StatBasic(2001, StatCollector.translateToLocal("stat.swimOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceFallenStat = (new StatBasic(2002, StatCollector.translateToLocal("stat.fallOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceClimbedStat = (new StatBasic(2003, StatCollector.translateToLocal("stat.climbOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceFlownStat = (new StatBasic(2004, StatCollector.translateToLocal("stat.flyOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceDoveStat = (new StatBasic(2005, StatCollector.translateToLocal("stat.diveOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceByMinecartStat = (new StatBasic(2006, StatCollector.translateToLocal("stat.minecartOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceByBoatStat = (new StatBasic(2007, StatCollector.translateToLocal("stat.boatOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase distanceByPigStat = (new StatBasic(2008, StatCollector.translateToLocal("stat.pigOneCm"), StatBase.distanceStatType)).initIndependentStat().registerStat();
	public static StatBase jumpStat = (new StatBasic(2010, StatCollector.translateToLocal("stat.jump"))).initIndependentStat().registerStat();
	public static StatBase dropStat = (new StatBasic(2011, StatCollector.translateToLocal("stat.drop"))).initIndependentStat().registerStat();
	public static StatBase damageDealtStat = (new StatBasic(2020, StatCollector.translateToLocal("stat.damageDealt"))).registerStat();
	public static StatBase damageTakenStat = (new StatBasic(2021, StatCollector.translateToLocal("stat.damageTaken"))).registerStat();
	public static StatBase deathsStat = (new StatBasic(2022, StatCollector.translateToLocal("stat.deaths"))).registerStat();
	public static StatBase mobKillsStat = (new StatBasic(2023, StatCollector.translateToLocal("stat.mobKills"))).registerStat();
	public static StatBase playerKillsStat = (new StatBasic(2024, StatCollector.translateToLocal("stat.playerKills"))).registerStat();
	public static StatBase fishCaughtStat = (new StatBasic(2025, StatCollector.translateToLocal("stat.fishCaught"))).registerStat();
	public static StatBase[] mineBlockStatArray = func_25153_a("stat.mineBlock", 16777216);
	public static StatBase[] objectCraftStats;
	public static StatBase[] objectUseStats;
	public static StatBase[] objectBreakStats;
	private static boolean blockStatsInitialized;
	private static boolean itemStatsInitialized;

	public static void preInit() {
	}

	public static void initBreakableStats() {
		objectUseStats = initUsableStats(objectUseStats, "stat.useItem", 16908288, 0, Block.blocksList.length);
		objectBreakStats = initBreakStats(objectBreakStats, "stat.breakItem", 16973824, 0, Block.blocksList.length);
		blockStatsInitialized = true;
		initCraftableStats();
	}

	public static void initStats() {
		objectUseStats = initUsableStats(objectUseStats, "stat.useItem", 16908288, Block.blocksList.length, 32000);
		objectBreakStats = initBreakStats(objectBreakStats, "stat.breakItem", 16973824, Block.blocksList.length, 32000);
		itemStatsInitialized = true;
		initCraftableStats();
	}

	public static void initCraftableStats() {
		if(blockStatsInitialized && itemStatsInitialized) {
			HashSet<Integer> hashSet0 = new HashSet<Integer>();
			Iterator<IRecipe> iterator1 = CraftingManager.getInstance().getRecipeList().iterator();

			while(iterator1.hasNext()) {
				IRecipe iRecipe2 = (IRecipe)iterator1.next();
				hashSet0.add(iRecipe2.getRecipeOutput().itemID);
			}

			Iterator<ItemStack> iterator2 = FurnaceRecipes.smelting().getSmeltingList().values().iterator();

			while(iterator2.hasNext()) {
				ItemStack itemStack4 = (ItemStack)iterator2.next();
				hashSet0.add(itemStack4.itemID);
			}

			objectCraftStats = new StatBase[32000];
			Iterator<Integer>iterator3 = hashSet0.iterator();

			while(iterator3.hasNext()) {
				Integer integer5 = (Integer)iterator3.next();
				if(Item.itemsList[integer5.intValue()] != null) {
					String string3 = StatCollector.translateToLocalFormatted("stat.craftItem", new Object[]{Item.itemsList[integer5.intValue()].getStatName()});
					objectCraftStats[integer5.intValue()] = (new StatCrafting(16842752 + integer5.intValue(), string3, integer5.intValue())).registerStat();
				}
			}

			replaceAllSimilarBlocks(objectCraftStats);
		}
	}

	private static StatBase[] func_25153_a(String string0, int i1) {
		StatBase[] statBase2 = new StatBase[256];

		for(int i3 = 0; i3 < 256; ++i3) {
			if(Block.blocksList[i3] != null && Block.blocksList[i3].getEnableStats()) {
				String string4 = StatCollector.translateToLocalFormatted(string0, new Object[]{Block.blocksList[i3].translateBlockName()});
				statBase2[i3] = (new StatCrafting(i1 + i3, string4, i3)).registerStat();
				objectMineStats.add((StatCrafting)statBase2[i3]);
			}
		}

		replaceAllSimilarBlocks(statBase2);
		return statBase2;
	}

	private static StatBase[] initUsableStats(StatBase[] statBase0, String string1, int i2, int i3, int i4) {
		if(statBase0 == null) {
			statBase0 = new StatBase[32000];
		}

		for(int i5 = i3; i5 < i4; ++i5) {
			if(Item.itemsList[i5] != null) {
				String string6 = StatCollector.translateToLocalFormatted(string1, new Object[]{Item.itemsList[i5].getStatName()});
				statBase0[i5] = (new StatCrafting(i2 + i5, string6, i5)).registerStat();
				if(i5 >= Block.blocksList.length) {
					itemStats.add((StatCrafting)statBase0[i5]);
				}
			}
		}

		replaceAllSimilarBlocks(statBase0);
		return statBase0;
	}

	private static StatBase[] initBreakStats(StatBase[] statBase0, String string1, int i2, int i3, int i4) {
		if(statBase0 == null) {
			statBase0 = new StatBase[32000];
		}

		for(int i5 = i3; i5 < i4; ++i5) {
			if(Item.itemsList[i5] != null && Item.itemsList[i5].isDamageable()) {
				String string6 = StatCollector.translateToLocalFormatted(string1, new Object[]{Item.itemsList[i5].getStatName()});
				statBase0[i5] = (new StatCrafting(i2 + i5, string6, i5)).registerStat();
			}
		}

		replaceAllSimilarBlocks(statBase0);
		return statBase0;
	}

	private static void replaceAllSimilarBlocks(StatBase[] statBase0) {
		replaceSimilarBlocks(statBase0, Block.waterStill.blockID, Block.waterMoving.blockID);
		replaceSimilarBlocks(statBase0, Block.lavaStill.blockID, Block.lavaStill.blockID);
		replaceSimilarBlocks(statBase0, Block.pumpkinLantern.blockID, Block.pumpkin.blockID);
		replaceSimilarBlocks(statBase0, Block.stoneOvenActive.blockID, Block.stoneOvenIdle.blockID);
		replaceSimilarBlocks(statBase0, Block.oreRedstoneGlowing.blockID, Block.oreRedstone.blockID);
		replaceSimilarBlocks(statBase0, Block.redstoneRepeaterActive.blockID, Block.redstoneRepeaterIdle.blockID);
		replaceSimilarBlocks(statBase0, Block.torchRedstoneActive.blockID, Block.torchRedstoneIdle.blockID);
		replaceSimilarBlocks(statBase0, Block.mushroomRed.blockID, Block.mushroomBrown.blockID);
		replaceSimilarBlocks(statBase0, Block.stairDouble.blockID, Block.stairSingle.blockID);
		replaceSimilarBlocks(statBase0, Block.grass.blockID, Block.dirt.blockID);
		replaceSimilarBlocks(statBase0, Block.tilledField.blockID, Block.dirt.blockID);
	}

	private static void replaceSimilarBlocks(StatBase[] statBase0, int i1, int i2) {
		if(statBase0[i1] != null && statBase0[i2] == null) {
			statBase0[i2] = statBase0[i1];
		} else {
			allStats.remove(statBase0[i1]);
			objectMineStats.remove(statBase0[i1]);
			generalStats.remove(statBase0[i1]);
			statBase0[i1] = statBase0[i2];
		}
	}

	public static StatBase getOneShotStat(int i0) {
		return (StatBase)oneShotStats.get(i0);
	}

	static {
		AchievementList.func_27374_a();
		blockStatsInitialized = false;
		itemStatsInitialized = false;
	}
}
