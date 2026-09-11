package net.minecraft.game.world.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EnumMobType;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemBlock;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.item.ItemBlockWithSubtypes;
import net.minecraft.game.item.ItemCloth;
import net.minecraft.game.item.ItemLayeredSand;
import net.minecraft.game.item.ItemLeaves;
import net.minecraft.game.item.ItemLilypad;
import net.minecraft.game.item.ItemPiston;
import net.minecraft.game.item.ItemPumpkin;
import net.minecraft.game.item.ItemSlab;
import net.minecraft.game.item.ItemTerracotta;



public class Block {
	
	// Step sounds
	
	public static final StepSound soundPowderFootstep = new StepSound("stone", 1.0F, 1.0F);
	public static final StepSound soundWoodFootstep = new StepSound("wood", 1.0F, 1.0F);
	public static final StepSound soundGravelFootstep = new StepSound("gravel", 1.0F, 1.0F);
	public static final StepSound soundGrassFootstep = new StepSound("grass", 1.0F, 1.0F);
	public static final StepSound soundStoneFootstep = new StepSound("stone", 1.0F, 1.0F);
	public static final StepSound soundMetalFootstep = new StepSound("stone", 1.0F, 1.5F);
	public static final StepSound soundGlassFootstep = new StepSoundStone("stone", 1.0F, 1.0F);
	public static final StepSound soundClothFootstep = new StepSound("cloth", 1.0F, 1.0F);
	public static final StepSound soundSandFootstep = new StepSoundSand("sand", 1.0F, 1.0F);
	public static final StepSound soundSlimeFootstep = new StepSoundSlime("slime", 1.0F, 1.0F);
	
	// Block arrays
	
	public static final Block[] blocksList = new Block[256];
	public static final boolean[] tickOnLoad = new boolean[256];
	public static final boolean[] opaqueCubeLookup = new boolean[256];
	public static final boolean[] isBlockContainer = new boolean[256];
	public static final int[] lightOpacity = new int[256];
	public static final boolean[] canBlockGrass = new boolean[256];
	public static final int[] lightValue = new int[256];
	public static final boolean[] requiresSelfNotify = new boolean[256];
	public static boolean[] useNeighborBrightness = new boolean[256];
	
	// Blocks
	
	public static final Block stone = (new BlockStone(1, 1)).setHardness(1.5F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("stone").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final BlockGrass grass = (BlockGrass)(new BlockGrass(2)).setHardness(0.6F).setStepSound(soundGrassFootstep).setBlockName("grass");
	public static final Block dirt = (new BlockDirt(3, 2)).setHardness(0.5F).setStepSound(soundGravelFootstep).setBlockName("dirt");
	public static final Block cobblestone = (new BlockCobblestone(4, 16)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("stonebrick").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final Block planks = (new BlockPlanks(5, 4, Material.wood)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundWoodFootstep).setBlockName("wood").setRequiresSelfNotify().setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final Block sapling = (new BlockSapling(6, 15)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("sapling").setRequiresSelfNotify();
	public static final Block bedrock = (new Block(7, 17, Material.rock)).setBlockUnbreakable().setResistance(6000000.0F).setStepSound(soundStoneFootstep).setBlockName("bedrock").disableStats().setCreativeTab(CreativeTabs.tabBlock);
	public static final Block waterMoving = (new BlockFlowing(8, Material.water)).setHardness(100.0F).setLightOpacity(3).setBlockName("water").disableStats().setRequiresSelfNotify();
	public static final Block waterStill = (new BlockStationary(9, Material.water)).setHardness(100.0F).setLightOpacity(3).setBlockName("water").disableStats().setRequiresSelfNotify();
	public static final Block lavaMoving = (new BlockFlowing(10, Material.lava)).setHardness(0.0F).setLightValue(1.0F).setLightOpacity(255).setBlockName("lava").disableStats().setRequiresSelfNotify();
	public static final Block lavaStill = (new BlockStationary(11, Material.lava)).setHardness(100.0F).setLightValue(1.0F).setLightOpacity(255).setBlockName("lava").disableStats().setRequiresSelfNotify();
	public static final Block sand = (new BlockSand(12, 18)).setHardness(0.5F).setStepSound(soundSandFootstep).setBlockName("sand");
	public static final Block gravel = (new BlockGravel(13, 19)).setHardness(0.6F).setStepSound(soundGravelFootstep).setBlockName("gravel").setIsUrban(true);
	public static final Block oreGold = (new BlockOre(14, 32)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreGold");
	public static final Block oreIron = (new BlockOre(15, 33)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreIron");
	public static final Block oreCoal = (new BlockOre(16, 34)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreCoal");
	public static final Block wood = (new BlockLog(17)).setHardness(2.0F).setStepSound(soundWoodFootstep).setBlockName("log").setRequiresSelfNotify();
	public static final BlockLeaves leaves = (BlockLeaves)(new BlockLeaves(18, 52)).setHardness(0.2F).setLightOpacity(1).setStepSound(soundGrassFootstep).setBlockName("leaves").disableStats().setRequiresSelfNotify();
	public static final Block sponge = (new BlockSponge(19)).setHardness(0.6F).setStepSound(soundGrassFootstep).setBlockName("sponge");
	public static final BlockGlass glass = (BlockGlass) new BlockGlass(20, 49, Material.glass, false).setHardness(0.3F).setStepSound(soundGlassFootstep).setBlockName("glass").setIsUrban(true);
	public static final Block oreLapis = (new BlockOre(21, 160)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreLapis");
	public static final Block blockLapis = (new Block(22, 144, Material.rock)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("blockLapis").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block dispenser = (new BlockDispenser(23)).setHardness(3.5F).setStepSound(soundStoneFootstep).setBlockName("dispenser").setRequiresSelfNotify();
	public static final Block sandStone = (new BlockSandStone(24)).setStepSound(soundStoneFootstep).setHardness(0.8F).setBlockName("sandStone").setIsUrban(true);
	public static final Block musicBlock = (new BlockNote(25)).setHardness(0.8F).setBlockName("musicBlock").setRequiresSelfNotify();
	public static final Block blockBed = (new BlockBed(26)).setHardness(0.2F).setBlockName("bed").disableStats().setRequiresSelfNotify();
	public static final Block railPowered = (new BlockRail(27, 179, true)).setHardness(0.7F).setStepSound(soundMetalFootstep).setBlockName("goldenRail").setRequiresSelfNotify();
	public static final Block railDetector = (new BlockDetectorRail(28, 195)).setHardness(0.7F).setStepSound(soundMetalFootstep).setBlockName("detectorRail").setRequiresSelfNotify();
	public static final Block pistonStickyBase = (new BlockPistonBase(29, 106, true)).setBlockName("pistonStickyBase").setRequiresSelfNotify();
	public static final Block web = (new BlockWeb(30, 11)).setLightOpacity(1).setHardness(4.0F).setBlockName("web");
	public static final BlockTallGrass tallGrass = (BlockTallGrass)(new BlockTallGrass(31, 16*13+13)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("tallgrass");
	public static final BlockDeadBush deadBush = (BlockDeadBush)(new BlockDeadBush(32, 16*13+10)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("deadbush");
	public static final Block pistonBase = (new BlockPistonBase(33, 107, false)).setBlockName("pistonBase").setRequiresSelfNotify();
	public static final BlockPistonExtension pistonExtension = (BlockPistonExtension)(new BlockPistonExtension(34, 107)).setRequiresSelfNotify();
	public static final Block cloth = (new BlockCloth()).setHardness(0.8F).setStepSound(soundClothFootstep).setBlockName("cloth").setRequiresSelfNotify();
	public static final BlockPistonMoving pistonMoving = new BlockPistonMoving(36);
	public static final BlockFlower plantYellow = (BlockFlower)(new BlockFlower(37, 13)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("flower");
	public static final BlockFlower plantRed = (BlockFlower)(new BlockFlower(38, 12)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("rose");
	public static final BlockFlower mushroomBrown = (BlockFlower)(new BlockMushroom(39, 29)).setHardness(0.0F).setStepSound(soundGrassFootstep).setLightValue(0.125F).setBlockName("mushroom");
	public static final BlockFlower mushroomRed = (BlockFlower)(new BlockMushroom(40, 28)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("mushroom");
	public static final Block blockGold = (new BlockOreStorage(41, 23)).setHardness(3.0F).setResistance(10.0F).setStepSound(soundMetalFootstep).setBlockName("blockGold");
	public static final Block blockSteel = (new BlockOreStorage(42, 22)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundMetalFootstep).setBlockName("blockIron");
	public static final Block stairDouble = (new BlockStep(43, true)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("stoneSlab").setIsUrban(true);
	public static final Block stairSingle = (new BlockStep(44, false)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("stoneSlab").setIsUrban(true);
	public static final Block brick = (new Block(45, 7, Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("brick").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final Block tnt = (new BlockTNT(46, 8)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("tnt");
	public static final Block bookShelf = (new BlockBookshelf(47, 35)).setHardness(1.5F).setStepSound(soundWoodFootstep).setBlockName("bookshelf");
	public static final Block cobblestoneMossy = (new BlockCobblestone(48, 36)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("stoneMoss").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final Block obsidian = (new BlockObsidian(49, 37)).setHardness(10.0F).setResistance(2000.0F).setStepSound(soundStoneFootstep).setBlockName("obsidian").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	public static final Block torchWood = (new BlockTorch(50, 80)).setHardness(0.0F).setLightValue(0.9375F).setStepSound(soundWoodFootstep).setBlockName("torch").setRequiresSelfNotify();
	public static final BlockFire fire = (BlockFire)(new BlockFire(51, 31)).setHardness(0.0F).setLightValue(1.0F).setStepSound(soundWoodFootstep).setBlockName("fire").disableStats().setRequiresSelfNotify();
	public static final Block mobSpawner = (new BlockMobSpawner(52, 65, false)).setHardness(5.0F).setStepSound(soundMetalFootstep).setBlockName("mobSpawner").disableStats();
	public static final Block stairCompactPlanks = (new BlockStairs(53, planks)).setBlockName("stairsWood").setRequiresSelfNotify();
	public static final Block chest = (new BlockChest(54)).setHardness(2.5F).setStepSound(soundWoodFootstep).setBlockName("chest").setRequiresSelfNotify();
	public static final Block redstoneWire = (new BlockRedstoneWire(55, 164)).setHardness(0.0F).setStepSound(soundPowderFootstep).setBlockName("redstoneDust").disableStats().setRequiresSelfNotify();
	public static final Block oreDiamond = (new BlockOre(56, 50)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreDiamond");
	public static final Block blockDiamond = (new BlockOreStorage(57, 24)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundMetalFootstep).setBlockName("blockDiamond");
	public static final Block workbench = (new BlockWorkbench(58)).setHardness(2.5F).setStepSound(soundWoodFootstep).setBlockName("workbench");
	public static final Block crops = (new BlockCrops(59, 88)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("crops").disableStats().setRequiresSelfNotify();
	public static final Block tilledField = (new BlockFarmland(60)).setHardness(0.6F).setStepSound(soundGravelFootstep).setBlockName("farmland");
	public static final Block stoneOvenIdle = (new BlockFurnace(61, false)).setHardness(3.5F).setStepSound(soundStoneFootstep).setBlockName("furnace").setRequiresSelfNotify().setCreativeTab(CreativeTabs.tabDeco);
	public static final Block stoneOvenActive = (new BlockFurnace(62, true)).setHardness(3.5F).setStepSound(soundStoneFootstep).setLightValue(0.875F).setBlockName("furnaceOn").setRequiresSelfNotify();
	public static final Block signPost = (new BlockSign(63, TileEntitySign.class, true)).setHardness(1.0F).setStepSound(soundWoodFootstep).setBlockName("sign").disableStats().setRequiresSelfNotify();
	public static final Block doorWood = (new BlockDoor(64, Material.wood)).setHardness(3.0F).setStepSound(soundWoodFootstep).setBlockName("doorWood").disableStats().setRequiresSelfNotify();
	public static final Block ladder = (new BlockLadder(65, 83)).setHardness(0.4F).setStepSound(soundWoodFootstep).setBlockName("ladder").setRequiresSelfNotify();
	public static final Block rail = (new BlockRail(66, 128, false)).setHardness(0.7F).setStepSound(soundMetalFootstep).setBlockName("rail").setRequiresSelfNotify().setIsUrban(true);
	public static final Block stairCompactCobblestone = (new BlockStairs(67, cobblestone)).setBlockName("stairsStone").setRequiresSelfNotify();
	public static final Block signWall = (new BlockSign(68, TileEntitySign.class, false)).setHardness(1.0F).setStepSound(soundWoodFootstep).setBlockName("sign").disableStats().setRequiresSelfNotify();
	public static final Block lever = (new BlockLever(69, 96)).setHardness(0.5F).setStepSound(soundWoodFootstep).setBlockName("lever").setRequiresSelfNotify();
	public static final Block pressurePlateStone = (new BlockPressurePlate(70, stone.blockIndexInTexture, EnumMobType.mobs, Material.rock)).setHardness(0.5F).setStepSound(soundStoneFootstep).setBlockName("pressurePlate").setRequiresSelfNotify();
	public static final Block doorSteel = (new BlockDoor(71, Material.iron)).setHardness(5.0F).setStepSound(soundMetalFootstep).setBlockName("doorIron").disableStats().setRequiresSelfNotify();
	public static final Block pressurePlatePlanks = (new BlockPressurePlate(72, planks.blockIndexInTexture, EnumMobType.everything, Material.wood)).setHardness(0.5F).setStepSound(soundWoodFootstep).setBlockName("pressurePlate").setRequiresSelfNotify();
	public static final Block oreRedstone = (new BlockRedstoneOre(73, 51, false, 0)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreRedstone").setRequiresSelfNotify();
	public static final Block oreRedstoneGlowing = (new BlockRedstoneOre(74, 51, true, 0)).setLightValue(0.625F).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreRedstone").setRequiresSelfNotify();
	public static final Block torchRedstoneIdle = (new BlockRedstoneTorch(75, 115, false)).setHardness(0.0F).setStepSound(soundWoodFootstep).setBlockName("notGate").setRequiresSelfNotify();
	public static final Block torchRedstoneActive = (new BlockRedstoneTorch(76, 99, true)).setHardness(0.0F).setLightValue(0.5F).setStepSound(soundWoodFootstep).setBlockName("notGate").setRequiresSelfNotify().setCreativeTab(CreativeTabs.tabRedstone);
	public static final Block button = (new BlockButton(77, stone.blockIndexInTexture)).setHardness(0.5F).setStepSound(soundStoneFootstep).setBlockName("button").setRequiresSelfNotify();
	public static final Block snow = (new BlockSnow(78, 66)).setHardness(0.1F).setStepSound(soundClothFootstep).setBlockName("snow");
	public static final Block ice = (new BlockIce(79, 67)).setHardness(0.5F).setLightOpacity(3).setStepSound(soundGlassFootstep).setBlockName("ice");
	public static final Block blockSnow = (new BlockSnowBlock(80, 66)).setHardness(0.2F).setStepSound(soundClothFootstep).setBlockName("snow");
	public static final Block cactus = (new BlockCactus(81, 70)).setHardness(0.4F).setStepSound(soundClothFootstep).setBlockName("cactus");
	public static final Block blockClay = (new BlockClay(82, 72)).setHardness(0.6F).setStepSound(soundGravelFootstep).setBlockName("clay");
	public static final Block reed = (new BlockReed(83, 73)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("reeds").disableStats();
	public static final Block jukebox = (new BlockJukeBox(84, 74)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("jukebox").setRequiresSelfNotify();
	public static final Block fence = (new BlockFence(85, 4)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundWoodFootstep).setBlockName("fence").setRequiresSelfNotify();
	public static final Block pumpkin = (new BlockPumpkin(86, 102, false)).setHardness(1.0F).setStepSound(soundWoodFootstep).setBlockName("pumpkin").setRequiresSelfNotify();
	public static final Block bloodStone = (new BlockNetherrack(87, 103)).setHardness(0.4F).setStepSound(soundStoneFootstep).setBlockName("hellrock");
	public static final Block slowSand = (new BlockSoulSand(88, 104)).setHardness(0.5F).setStepSound(soundSandFootstep).setBlockName("hellsand");
	
	// Reinstated for b1.6: glowStone is glass
	//public static final Block glowStone = (new BlockGlowStone(89, 105, Material.rock)).setHardness(0.3F).setStepSound(soundGlassFootstep).setLightValue(1.0F).setBlockName("lightgem");
	public static final Block glowStone = (new BlockGlowStone(89, 105, Material.glass)).setHardness(0.3F).setStepSound(soundGlassFootstep).setLightValue(1.0F).setBlockName("lightgem");
	
	public static final BlockPortal portal = (BlockPortal)(new BlockPortal(90, 14)).setHardness(-1.0F).setStepSound(soundGlassFootstep).setLightValue(0.75F).setBlockName("portal");
	public static final Block pumpkinLantern = (new BlockPumpkin(91, 102, true)).setHardness(1.0F).setStepSound(soundWoodFootstep).setLightValue(1.0F).setBlockName("litpumpkin").setRequiresSelfNotify();
	public static final Block cake = (new BlockCake(92, 121)).setHardness(0.5F).setStepSound(soundClothFootstep).setBlockName("cake").disableStats().setRequiresSelfNotify();
	public static final Block redstoneRepeaterIdle = (new BlockRedstoneRepeater(93, false)).setHardness(0.0F).setStepSound(soundWoodFootstep).setBlockName("diode").disableStats().setRequiresSelfNotify();
	public static final Block redstoneRepeaterActive = (new BlockRedstoneRepeater(94, true)).setHardness(0.0F).setLightValue(0.625F).setStepSound(soundWoodFootstep).setBlockName("diode").disableStats().setRequiresSelfNotify();
	public static final Block lockedChest = (new BlockLockedChest(95)).setHardness(0.0F).setLightValue(1.0F).setStepSound(soundWoodFootstep).setBlockName("lockedchest").setTickOnLoad(true).setRequiresSelfNotify();
	public static final Block trapdoor = (new BlockTrapDoor(96, Material.wood)).setHardness(3.0F).setStepSound(soundWoodFootstep).setBlockName("trapdoor").disableStats().setRequiresSelfNotify();

	// From r1.2.5 / misc
	public static final Block stoneBricks = (new BlockStoneBrick(98, Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setIsUrban(true).setBlockName("stoneBricks").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block mushroomCapBrown = (new BlockBigMushroom(99, 1)).setHardness(0.2F).setStepSound(soundWoodFootstep).setBlockName("mushroomCapBrown");
	public static final Block mushroomCapRed = (new BlockBigMushroom(100, 0)).setHardness(0.2F).setStepSound(soundWoodFootstep).setBlockName("mushroomCapRed");
	public static final Block fenceIron = (new BlockPane(101, 85, 85, Material.iron, true)).setHardness(5.0F).setResistance(10.0F).setStepSound(soundMetalFootstep).setBlockName("fenceIron");
	public static final Block thinGlass = (new BlockPane(102, 49, 148, Material.glass, false)).setHardness(0.3F).setStepSound(soundGlassFootstep).setBlockName("thinGlass");
	public static final Block thinPlanks = (new BlockPane(103, 4, 4, Material.wood, true)).setHardness(1.5F).setStepSound(soundWoodFootstep).setBlockName("thinPlanks");
	public static final Block shoji = (new BlockPane(104, 166, 4, Material.wood, true)).setHardness(0.5F).setStepSound(soundWoodFootstep).setBlockName("shoji");
	// 105
	public static final Block vine = (new BlockVine(106, 13*16 + 12)).setHardness(0.2F).setStepSound(soundGrassFootstep).setBlockName("vine");
	// 107
	public static final Block stairsBrick = (new BlockStairs(108, brick)).setBlockName("stairsBrick").setLightOpacity(0).setRequiresSelfNotify();
	public static final Block stairsStoneBrickSmooth = (new BlockStairs(109, stoneBricks)).setBlockName("stairsStoneBrickSmooth").setLightOpacity(0).setRequiresSelfNotify();
	public static final Block mycelium = (new BlockMycelium(110)).setHardness(0.6F).setStepSound(soundGrassFootstep).setBlockName("mycelium");
	public static final Block lilyPad = (BlockLilypad)(new BlockLilypad(111, 13*16 + 11)).setHardness(0.3F).setStepSound(soundGrassFootstep).setBlockName("lilypad");	
	// 112
	// 113
	public static final Block cryingObsidian = (new BlockCryingObsidian(114, 116)).setHardness(10.0F).setResistance(2000.0F).setStepSound(soundStoneFootstep).setBlockName("cryingObsidian");
	
	// Originally with id 128 in 1.3.2, remapped to 125 which won't be used as I encode wood slabs correctly.
	public static final Block stairsSandStone = (new BlockStairs(125, sandStone)).setBlockName("stairsSandStone").setLightOpacity(0).setRequiresSelfNotify();
	
	// Custom *new* blocks ID >= 128
	public static final Block ironWall = (new BlockPane(128, 14*16 + 12, 85, Material.iron, true)).setHardness(2.5F).setResistance(5.0F).setStepSound(soundMetalFootstep).setBlockName("wireWall");
	public static final Block woodenSpikes = (new BlockWoodenSpikes(129, 14*16 + 11)).setHardness(2.0F).setResistance(1.0F).setStepSound(soundWoodFootstep).setBlockName("woodenSpikes");
	public static final Block streetLantern = (new BlockStreetLantern(130, 14*16 + 9, false)).setHardness(0.5F).setStepSound(soundMetalFootstep).setLightValue(0.875F).setIsUrban(true).setBlockName("streetLantern");
	public static final Block streetLanternBroken = (new BlockStreetLantern(131, 14*16 + 9, true)).setHardness(0.5F).setStepSound(soundMetalFootstep).setIsUrban(true).setBlockName("streetLanternBroken");
	public static final Block streetLanternFence = (new BlockFence(132, 14*16 + 7, Material.iron)).setHardness(2.0F).setResistance(5.0F).setStepSound(soundMetalFootstep).setBlockName("ironFence").setCreativeTab(CreativeTabs.tabDeco);
	public static final Block barbedWire = (new BlockBarbedWire(133, 14*16 + 6)).setHardness(3.0F).setResistance(8.0F).setStepSound(soundMetalFootstep).setBlockName("barbedWire");
	public static final Block layeredSand = (new BlockLayeredSand(134, 18)).setHardness(0.2F).setStepSound(soundSandFootstep).setBlockName("layeredSand");
	public static final Block hollowLog = (new BlockHollowLog(135, 20)).setHardness(1.0F).setResistance(1.0F).setStepSound(soundWoodFootstep).setBlockName("hollowLog");
	public static final Block dirtPath = (new BlockDirtPath(136, 13*16 + 6)).setHardness(0.6F).setStepSound(soundGrassFootstep).setBlockName("dirtPath");
	public static final Block blockCoal = (new Block(137, 14*16 + 4, Material.rock)).setHardness(1.5F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("blockCoal");
	public static final Block regolith = (new BlockRegolith(138, 13*16 + 5)).setHardness(0.7F).setStepSound(soundGravelFootstep).setBlockName("regolith");
	public static final Block glowshroom = (new BlockMushroom(139, 13*16 + 4)).setHardness(0.0F).setStepSound(soundGrassFootstep).setLightValue(0.5F).setBlockName("glowshroom");
	public static final Block oreRuby = (new BlockOre(140, 14*16 + 3)).setHardness(2.5F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("oreRuby");
	public static final Block oreEmerald = (new BlockOre(141, 13*16 + 3)).setHardness(2.5F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("oreEmerald");
	public static final Block seaweed = (new BlockSeaweed(142, 15*16 + 12)).setHardness(0.2F).setLightOpacity(3).setStepSound(soundGrassFootstep).setBlockName("seaWeed");
	public static final BlockFlower paeonia = (BlockFlower)(new BlockFlower(143, 14*16 + 2)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("paeonia");
	public static final BlockFlower blueFlower = (BlockFlower)(new BlockFlower(144, 14*16 + 0)).setHardness(0.0F).setStepSound(soundGrassFootstep).setBlockName("blueFlower");
	public static final Block coral = (new BlockCoral(145, 12 * 16 + 10)).setHardness(0.5F).setStepSound(soundGrassFootstep).setLightOpacity(3).setLightValue(0.675F).setBlockName("coral");
	public static final Block mushroomCapGreen = (new BlockBigMushroom(146, 2)).setHardness(0.2F).setStepSound(soundWoodFootstep).setLightValue(0.675F).setBlockName("bigMushroomGreen");
	public static final Block shinyGlass = (new Block(147, 12 * 16 + 8, Material.glass)).setHardness(0.3F).setStepSound(soundGlassFootstep).setIsUrban(true).setLightValue(1.0F).setBlockName("shinyGlass").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block chippedWood = (new BlockChippedWood(148, 13 * 16 + 9)).setHardness(1.8F).setStepSound(soundWoodFootstep).setBlockName("chippedWood");
	public static final Block driedKelpBlock = (new Block(149, 12 * 16 + 7, Material.grass)).setHardness(0.5F).setStepSound(soundGrassFootstep).setBlockName("driedKelp").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block mazeStone = (new BlockTFMazestone(150, 14*16 + 5)).setHardness(15.0F).setResistance(5.0F).setStepSound(soundMetalFootstep).setBlockName("mazeStone");
	public static final Block mobSpawnerOneshot = (new BlockMobSpawner(151, 243 /*65*/, true)).setHardness(5.0F).setStepSound(soundMetalFootstep).setBlockName("oneShotSpawner");
	public static final Block slimeBlock = (new BlockSlime(152, 12 * 16 + 6, Material.slime)).setHardness(0.2F).setStepSound(soundSlimeFootstep).setSliperiness(0.8F).setBlockName("slimeBlock");
	public static final Block adobe = (new BlockAdobe(153, 12 * 16 + 5)).setHardness(0.7F).setStepSound(soundGrassFootstep).setBlockName("adobe");
	public static final Block bigFlower = (new BlockBigFlower(154, 11 * 16 + 13, Material.leaves)).setHardness(0.5F).setStepSound(soundWoodFootstep).setBlockName("bigFlower");
	public static final Block fleshBlock = (new BlockMeat(155, 103, Material.flesh)).setHardness(0.5F).setStepSound(soundSandFootstep).setBlockName("fleshBlock").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block hayStack = (new BlockHaystack(156, 11 * 16 + 11)).setHardness(0.5F).setStepSound(soundGrassFootstep).setBlockName("hayStack");
	public static final Block oreGlow = (new BlockRedstoneOre(157, 11 * 16 + 10, false, 1)).setHardness(1.5F).setLightValue(0.375F).setStepSound(soundStoneFootstep).setBlockName("oreGlow");
	public static final Block oreGlowGlowing = (new BlockRedstoneOre(158, 11 * 16 + 10, true, 1)).setHardness(1.5F).setLightValue(1.0F).setStepSound(soundStoneFootstep).setBlockName("oreGlowGlowing");
	
	// The Ocean Update
	public static final Block oreCopper = (new BlockOre(159, 11*16+8)).setHardness(3.0F).setResistance(5.0F).setStepSound(soundStoneFootstep).setBlockName("oreCopper");
	public static final Block blockCopper = (new BlockOreStorage(160, 11*16+4)).setHardness(3.0F).setResistance(10.0F).setStepSound(soundMetalFootstep).setBlockName("blockCopper");
	public static final Block divingHelmet = (new BlockDivingHelmet(161, 11*16+4)).setHardness(2.0F).setStepSound(soundMetalFootstep).setBlockName("divingHelmet");
	public static final Block caveVine = (new BlockCaveVine(162)).setHardness(0.2F).setStepSound(soundGrassFootstep).setBlockName("vine");
	
	// The World Overhaul Update	
	public static final Block chain = (new BlockChain(163, 13 * 16, Material.iron)).setHardness(1.0F).setResistance(5.0F).setStepSound(soundMetalFootstep).setBlockName("chain");
	public static final Block terracotta = (new BlockTerracotta(164, 11 * 16 /*18*/, Material.clay, false)).setHardness(0.8F).setResistance(3.0F).setStepSound(soundStoneFootstep).setBlockName("terracotta");
	public static final Block stainedTerracotta = (new BlockTerracotta(165, 11 * 16 /*18*/, Material.clay, true)).setHardness(0.8F).setResistance(3.0F).setStepSound(soundStoneFootstep).setBlockName("terracotta");
	
	public static final Block packedIce = (new BlockIcePacked(166, 67)).setHardness(0.5F).setLightOpacity(1).setStepSound(soundGlassFootstep).setBlockName("packedIce");
	public static final Block boneBlock = (new BlockBone(167)).setHardness(1.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("boneBlock");
	public static final Block leafPile = (new BlockLeafPile(168)).setHardness(0.1F).setResistance(0.1F).setStepSound(soundGrassFootstep).setBlockName("leafPile");
	
	// City overhaul update
	public static final Block cementPowder = new BlockCementPowder(169, 11 * 16 + 1).setHardness(0.8F).setStepSound(soundGravelFootstep).setBlockName("cementPowder").setIsUrban(true);
	public static final Block cement = new BlockCement(170, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(soundStoneFootstep).setBlockName("cement").setIsUrban(true).setCreativeTab(CreativeTabs.tabBlock);
	
	// Moss update
	public static final Block surfaceMoss = (new BlockSurfaceMoss(171, 12 * 16 + 4)).setHardness(0.1F).setResistance(0.1F).setStepSound(soundGrassFootstep).setBlockName("surfaceMoss");
	
	// Special
	public static final Block structureVoid = new Block(217, 49, Material.air);
	
	// Command block
	public static final Block blockRedstone = new BlockPoweredOreStorage(250, 162).setHardness(0.5F).setResistance(2.0F).setStepSound(soundStoneFootstep).setBlockName("redstoneBlock").setCreativeTab(CreativeTabs.tabBlock);
	public static final Block blockCommand = new BlockCommandBlock(251).setBlockUnbreakable().setResistance(6000000.0F).setStepSound(soundStoneFootstep).setBlockName("command").disableStats().setCreativeTab(CreativeTabs.tabBlock);
	
	// Pistons - sorry, different IDs
	public static final Block classicPistonBase = (new PistonBase(252, 22, false)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setBlockName("piston");
	public static final Block classicPiston = (new Piston(253, 22, false)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setBlockName("piston");
	public static final Block classicStickyPistonBase = (new PistonBase(254, 23, true)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setBlockName("pistonSticky");
	public static final Block classicStickyPiston = (new Piston(255, 23, true)).setLightOpacity(0).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setBlockName("pistonSticky");

	public int blockIndexInTexture;
	public final int blockID;
	public float blockHardness;
	protected float blockResistance;
	protected boolean blockConstructorCalled;
	protected boolean enableStats;
	public double minX;
	public double minY;
	public double minZ;
	public double maxX;
	public double maxY;
	public double maxZ;
	public StepSound stepSound;
	public float blockParticleGravity;
	public final Material blockMaterial;
	public float slipperiness;
	private String blockName;
	public boolean isUrban = false;
	
	public CreativeTabs displayOnCreativeTab;

	protected Block(int i1, Material material2) {
		this.blockConstructorCalled = true;
		this.enableStats = true;
		this.stepSound = soundPowderFootstep;
		this.blockParticleGravity = 1.0F;
		this.slipperiness = 0.6F;
		if(blocksList[i1] != null) {
			throw new IllegalArgumentException("Slot " + i1 + " is already occupied by " + blocksList[i1] + " when adding " + this);
		} else {
			this.blockMaterial = material2;
			blocksList[i1] = this;
			this.blockID = i1;
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			opaqueCubeLookup[i1] = this.isOpaqueCube();
			lightOpacity[i1] = this.isOpaqueCube() ? 255 : 0;
			canBlockGrass[i1] = !material2.getCanBlockGrass();
			isBlockContainer[i1] = false;
		}
	}

	protected Block setRequiresSelfNotify() {
		requiresSelfNotify[this.blockID] = true;
		return this;
	}

	protected void initializeBlock() {
	}

	public int getEncouragementToFire() {
		return 0;
	}

	public int getAbilityToCatchFire() {
		return 0;
	}

	protected Block(int i1, int i2, Material material3) {
		this(i1, material3);
		this.blockIndexInTexture = i2;
	}

	protected Block setStepSound(StepSound stepSound1) {
		this.stepSound = stepSound1;
		return this;
	}

	protected Block setLightOpacity(int i1) {
		lightOpacity[this.blockID] = i1;
		return this;
	}

	protected Block setLightValue(float f1) {
		lightValue[this.blockID] = (int)(15.0F * f1);
		return this;
	}

	protected Block setResistance(float f1) {
		this.blockResistance = f1 * 3.0F;
		return this;
	}

	public boolean renderAsNormalBlock() {
		return true;
	}

	public int getRenderType() {
		return 0;
	}

	protected Block setHardness(float f1) {
		this.blockHardness = f1;
		if(this.blockResistance < f1 * 5.0F) {
			this.blockResistance = f1 * 5.0F;
		}

		return this;
	}

	protected Block setBlockUnbreakable() {
		this.setHardness(-1.0F);
		return this;
	}

	public float getHardness() {
		return this.blockHardness;
	}

	protected Block setTickOnLoad(boolean z1) {
		tickOnLoad[this.blockID] = z1;
		return this;
	}

	public void setBlockBounds(float f1, float f2, float f3, float f4, float f5, float f6) {
		this.minX = (double)f1;
		this.minY = (double)f2;
		this.minZ = (double)f3;
		this.maxX = (double)f4;
		this.maxY = (double)f5;
		this.maxZ = (double)f6;
	}

	public float getBlockBrightness(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return iBlockAccess1.getBrightness(i2, i3, i4, lightValue[this.blockID]);
	}

	public int getMixedBrightnessForBlock(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return iBlockAccess1.getLightBrightnessForSkyBlocks(i2, i3, i4, lightValue[this.blockID]);
	}

	public boolean shouldSideBeRendered(IBlockAccess iBlockAccess1, int i2, int i3, int i4, int i5) {
		return i5 == 0 && this.minY > 0.0D ? true : (i5 == 1 && this.maxY < 1.0D ? true : (i5 == 2 && this.minZ > 0.0D ? true : (i5 == 3 && this.maxZ < 1.0D ? true : (i5 == 4 && this.minX > 0.0D ? true : (i5 == 5 && this.maxX < 1.0D ? true : !iBlockAccess1.isBlockOpaqueCube(i2, i3, i4))))));
	}

	public boolean getIsBlockSolid(IBlockAccess iBlockAccess1, int i2, int i3, int i4, int i5) {
		return iBlockAccess1.getBlockMaterial(i2, i3, i4).isSolid();
	}

	public int getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side) {
		return this.getBlockTextureFromSideAndMetadata(side, iBlockAccess.getBlockMetadata(x, y, z));
	}

	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return this.getBlockTextureFromSide(side);
	}

	public int getBlockTextureFromSide(int side) {
		return this.blockIndexInTexture;
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		return AxisAlignedBB.getBoundingBoxFromPool((double)i2 + this.minX, (double)i3 + this.minY, (double)i4 + this.minZ, (double)i2 + this.maxX, (double)i3 + this.maxY, (double)i4 + this.maxZ);
	}
	
	public void getCollidingBoundingBoxes(World world1, int i2, int i3, int i4, AxisAlignedBB axisAlignedBB5, ArrayList<AxisAlignedBB> arrayList6, Entity entity) {
		this.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
	}

	public void getCollidingBoundingBoxes(World world1, int i2, int i3, int i4, AxisAlignedBB axisAlignedBB5, ArrayList<AxisAlignedBB> arrayList6) {
		AxisAlignedBB axisAlignedBB7 = this.getCollisionBoundingBoxFromPool(world1, i2, i3, i4);
		if(axisAlignedBB7 != null && axisAlignedBB5.intersectsWith(axisAlignedBB7)) {
			arrayList6.add(axisAlignedBB7);
		}

	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		return AxisAlignedBB.getBoundingBoxFromPool((double)i2 + this.minX, (double)i3 + this.minY, (double)i4 + this.minZ, (double)i2 + this.maxX, (double)i3 + this.maxY, (double)i4 + this.maxZ);
	}

	public boolean isOpaqueCube() {
		return true;
	}

	public boolean canCollideCheck(int i1, boolean z2) {
		return this.isCollidable();
	}

	public boolean isCollidable() {
		return true;
	}

	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
	}

	public void randomDisplayTick(World world1, int i2, int i3, int i4, Random random5) {
	}

	public void onBlockDestroyedByPlayer(World world1, int i2, int i3, int i4, int i5) {
	}

	public void onNeighborBlockChange(World world1, int i2, int i3, int i4, int i5) {
	}

	public int tickRate() {
		return 10;
	}

	public void onBlockAdded(World world1, int i2, int i3, int i4) {
	}

	public void onBlockRemoval(World world1, int i2, int i3, int i4) {
	}

	public int quantityDropped(Random random1) {
		return 1;
	}

	public int idDropped(int meta, Random random2) {
		return this.blockID;
	}

	public float blockStrength(EntityPlayer entityPlayer1, int metadata) {
		return this.blockHardness < 0.0F ? 0.0F : (!entityPlayer1.canHarvestBlock(this, metadata) ? 1.0F / this.blockHardness / 100.0F : entityPlayer1.getCurrentPlayerStrVsBlock(this, this.damageDropped(metadata)) / this.blockHardness / 30.0F);
	}

	public void dropBlockAsItem(World world, int x, int y, int z, int meta) {
		this.dropBlockAsItemWithChance(world, x, y, z, meta, 1.0F);
	}

	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance) {
		if(!world.isRemote) {
			int i7 = this.quantityDropped(world.rand);

			for(int i8 = 0; i8 < i7; ++i8) {
				if(world.rand.nextFloat() <= chance) {
					int i9 = this.idDropped(meta, world.rand);
					if(i9 > 0) {
						this.dropBlockAsItem_do(world, x, y, z, new ItemStack(i9, 1, this.damageDropped(meta)));
					}
				}
			}

		}
	}

	protected void dropBlockAsItem_do(World world, int x, int y, int z, ItemStack itemStack) {
		if(!world.isRemote) {
			float f6 = 0.7F;
			double d7 = (double)(world.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
			double d9 = (double)(world.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
			double d11 = (double)(world.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
			EntityItem entityItem13 = new EntityItem(world, (double)x + d7, (double)y + d9, (double)z + d11, itemStack);
			entityItem13.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityItem13);
		}
	}

	protected int damageDropped(int i1) {
		return 0;
	}

	public float getExplosionResistance(Entity entity1) {
		return this.blockResistance / 5.0F;
	}

	public MovingObjectPosition collisionRayTrace(World world1, int i2, int i3, int i4, Vec3D vec3D5, Vec3D vec3D6) {
		this.setBlockBoundsBasedOnState(world1, i2, i3, i4);
		vec3D5 = vec3D5.addVector((double)(-i2), (double)(-i3), (double)(-i4));
		vec3D6 = vec3D6.addVector((double)(-i2), (double)(-i3), (double)(-i4));
		Vec3D vec3D7 = vec3D5.getIntermediateWithXValue(vec3D6, this.minX);
		Vec3D vec3D8 = vec3D5.getIntermediateWithXValue(vec3D6, this.maxX);
		Vec3D vec3D9 = vec3D5.getIntermediateWithYValue(vec3D6, this.minY);
		Vec3D vec3D10 = vec3D5.getIntermediateWithYValue(vec3D6, this.maxY);
		Vec3D vec3D11 = vec3D5.getIntermediateWithZValue(vec3D6, this.minZ);
		Vec3D vec3D12 = vec3D5.getIntermediateWithZValue(vec3D6, this.maxZ);
		if(!this.isVecInsideYZBounds(vec3D7)) {
			vec3D7 = null;
		}

		if(!this.isVecInsideYZBounds(vec3D8)) {
			vec3D8 = null;
		}

		if(!this.isVecInsideXZBounds(vec3D9)) {
			vec3D9 = null;
		}

		if(!this.isVecInsideXZBounds(vec3D10)) {
			vec3D10 = null;
		}

		if(!this.isVecInsideXYBounds(vec3D11)) {
			vec3D11 = null;
		}

		if(!this.isVecInsideXYBounds(vec3D12)) {
			vec3D12 = null;
		}

		Vec3D vec3D13 = null;
		if(vec3D7 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D7) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D7;
		}

		if(vec3D8 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D8) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D8;
		}

		if(vec3D9 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D9) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D9;
		}

		if(vec3D10 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D10) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D10;
		}

		if(vec3D11 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D11) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D11;
		}

		if(vec3D12 != null && (vec3D13 == null || vec3D5.distanceTo(vec3D12) < vec3D5.distanceTo(vec3D13))) {
			vec3D13 = vec3D12;
		}

		if(vec3D13 == null) {
			return null;
		} else {
			byte b14 = -1;
			if(vec3D13 == vec3D7) {
				b14 = 4;
			}

			if(vec3D13 == vec3D8) {
				b14 = 5;
			}

			if(vec3D13 == vec3D9) {
				b14 = 0;
			}

			if(vec3D13 == vec3D10) {
				b14 = 1;
			}

			if(vec3D13 == vec3D11) {
				b14 = 2;
			}

			if(vec3D13 == vec3D12) {
				b14 = 3;
			}

			return new MovingObjectPosition(i2, i3, i4, b14, vec3D13.addVector((double)i2, (double)i3, (double)i4));
		}
	}

	private boolean isVecInsideYZBounds(Vec3D vec3D1) {
		return vec3D1 == null ? false : vec3D1.yCoord >= this.minY && vec3D1.yCoord <= this.maxY && vec3D1.zCoord >= this.minZ && vec3D1.zCoord <= this.maxZ;
	}

	private boolean isVecInsideXZBounds(Vec3D vec3D1) {
		return vec3D1 == null ? false : vec3D1.xCoord >= this.minX && vec3D1.xCoord <= this.maxX && vec3D1.zCoord >= this.minZ && vec3D1.zCoord <= this.maxZ;
	}

	private boolean isVecInsideXYBounds(Vec3D vec3D1) {
		return vec3D1 == null ? false : vec3D1.xCoord >= this.minX && vec3D1.xCoord <= this.maxX && vec3D1.yCoord >= this.minY && vec3D1.yCoord <= this.maxY;
	}

	public void onBlockDestroyedByExplosion(World world1, int i2, int i3, int i4) {
	}

	public int getRenderBlockPass() {
		return 0;
	}

	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side, ItemStack itemStack) {
		return this.canPlaceBlockOnSide(world, x, y, z, side);
	}
	
	public boolean canPlaceBlockOnSide(World world1, int i2, int i3, int i4, int i5) {
		return this.canPlaceBlockAt(world1, i2, i3, i4);
	}

	public boolean canPlaceBlockAt(World world1, int i2, int i3, int i4) {
		int i5 = world1.getBlockId(i2, i3, i4);
		return i5 == 0 || blocksList[i5].blockMaterial.getIsGroundCover() || (blocksList[i5] instanceof BlockFlower);
	}

	public boolean blockActivated(World world1, int i2, int i3, int i4, EntityPlayer entityPlayer5) {
		return false;
	}

	public void onEntityWalking(World world1, int i2, int i3, int i4, Entity entity5) {
	}

	public void onBlockPlaced(World world, int x, int y, int z, int face) {
	}
	
	public void onBlockPlaced(World world, int x, int y, int z, int face, float xWithinFace, float yWithinFace, float zWithinFace) {
		this.onBlockPlaced (world, x, y, z, face);
	}

	public void onBlockClicked(World world1, int i2, int i3, int i4, EntityPlayer entityPlayer5) {
	}

	public void velocityToAddToEntity(World world1, int i2, int i3, int i4, Entity entity5, Vec3D vec3D6) {
	}

	public void setBlockBoundsBasedOnState(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
	}

	public int getRenderColor(int i1) {
		return 0xFFFFFF;
	}

	public int colorMultiplier(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return 0xFFFFFF;
	}

	public boolean isPoweringTo(World iBlockAccess1, int i2, int i3, int i4, int i5) {
		return false;
	}

	public boolean canProvidePower() {
		return false;
	}

	public void onEntityCollidedWithBlock(World world1, int i2, int i3, int i4, Entity entity5) {
	}

	public boolean isIndirectlyPoweringTo(World world1, int i2, int i3, int i4, int i5) {
		return false;
	}

	public void setBlockBoundsForItemRender() {
	}

	public void harvestBlock(World world1, EntityPlayer entityPlayer2, int i3, int i4, int i5, int i6) {
		entityPlayer2.addStat(StatList.mineBlockStatArray[this.blockID], 1);
		this.dropBlockAsItem(world1, i3, i4, i5, i6);
	}
	
	public void silkTouchBlock (World var1, int var2, int var3, int var4, int var5) {
		this.dropBlockAsItem_do(var1, var2, var3, var4, new ItemStack(this.blockID, 1, this.damageDropped(var5)));
	}

	public boolean canBlockStay(World world1, int x, int y, int z) {
		return true;
	}
	
	public boolean canBlockStay(World world1, int x, int y, int z, int metadata) {
		return this.canBlockStay(world1, x, y, z);
	}

	public void onBlockPlacedBy(World world1, int i2, int i3, int i4, EntityLiving entityLiving5) {
	}

	public Block setBlockName(String string1) {
		this.blockName = "tile." + string1;
		return this;
	}

	public String translateBlockName() {
		return StatCollector.translateToLocal(this.getBlockName() + ".name");
	}

	public String getBlockName() {
		return this.blockName;
	}

	public void playBlock(World world1, int i2, int i3, int i4, int i5, int i6) {
	}

	public boolean getEnableStats() {
		return this.enableStats;
	}

	protected Block disableStats() {
		this.enableStats = false;
		return this;
	}

	public int getMobilityFlag() {
		return this.blockMaterial.getMaterialMobility();
	}
	
	public float getAmbientOcclusionLightValue(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return iBlockAccess1.isBlockNormalCube(i2, i3, i4) ? 0.2F : 1.0F;
	}
	
	public boolean canGrowPlants() {
		return false;
	}
	
	public boolean canGrowMushrooms() {
		return false;
	}

	/** Whether surface moss ({@link BlockSurfaceMoss}) can grow on this block. */
	public boolean canGrowMoss() {
		return false;
	}

	public boolean softLocked() {
		return false;
	}
	
	// This is to fix wooden slabs being Material.rock!
	public Material getBlockMaterialBasedOnmetaData(int meta) {
		return this.blockMaterial;
	}
	
	public Block setSliperiness(float slipperiness) {
		this.slipperiness = slipperiness;
		return this;
	}
	
	public Block setIsUrban(boolean isUrban) {
		this.isUrban = isUrban;
		return this;
	}
	
	public boolean isClimbable() {
		return false;
	}
	
	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		return !this.blockMaterial.blocksMovement();
	}
	
	// r1.3+ creative
	
	public Block setCreativeTab(CreativeTabs creativeTab) {
		this.displayOnCreativeTab = creativeTab;
		return this;
	}
	
	public CreativeTabs getCreativeTab() {
		return this.displayOnCreativeTab;
	}
	
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
	}
	
	@Override
	public String toString() {
		return this.blockName;
	}
	
	// 
	
	static {
		Item.itemsList[cloth.blockID] = (new ItemCloth(cloth.blockID - 256)).setItemName("cloth");
		Item.itemsList[stairSingle.blockID] = (new ItemSlab(stairSingle.blockID - 256)).setItemName("stoneSlab");
		Item.itemsList[leaves.blockID] = (new ItemLeaves(leaves.blockID - 256)).setItemName("leaves");
		Item.itemsList[pistonBase.blockID] = new ItemPiston(pistonBase.blockID - 256);
		Item.itemsList[pistonStickyBase.blockID] = new ItemPiston(pistonStickyBase.blockID - 256);
		Item.itemsList[lilyPad.blockID] = new ItemLilypad(lilyPad.blockID - 256);
		Item.itemsList[layeredSand.blockID] = new ItemLayeredSand(layeredSand.blockID - 256);
		Item.itemsList[pumpkin.blockID] = new ItemPumpkin(pumpkin.blockID - 256);
		Item.itemsList[stainedTerracotta.blockID] = (new ItemTerracotta(stainedTerracotta.blockID - 256)).setItemName("terracotta");
		
		for(int id = 0; id < 256; ++id) {
			if(blocksList[id] instanceof IBlockWithSubtypes) {
				// Automaticly assign the special ItemBlockWithSubTypes to
				// blocks that implement the IBlockWithSubtypes interface

				Item.itemsList[id] = new ItemBlockWithSubtypes((IBlockWithSubtypes)blocksList[id], id);
				blocksList[id].initializeBlock();

			} else if(blocksList[id] != null) {
				if(Item.itemsList[id] == null) {
					Item.itemsList[id] = new ItemBlock(id - 256);
					blocksList[id].initializeBlock();
				}

				boolean z1 = false;
				if(id > 0 && blocksList[id].getRenderType() == 10) {
					z1 = true;
				}

				if(id > 0 && blocksList[id] instanceof BlockStep) {
					z1 = true;
				}

				if(id == tilledField.blockID) {
					z1 = true;
				}

				if(canBlockGrass[id]) {
					z1 = true;
				}

				useNeighborBrightness[id] = z1;
			}
		}

		canBlockGrass[0] = true;
		StatList.initBreakableStats();
	}

	public static int encode(Block block, int meta) {
		return (block.blockID & 0xFF) | (meta << 8);
	}

	public static byte decodeID(int encodedIdMeta) {
		return (byte) (encodedIdMeta & 0xFF);
	}
	
	public static byte decodeMeta (int encodedIdMeta) {
		return (byte) (encodedIdMeta >> 8);
	}

	public String inventoryOverlayString(int meta) {
		return null;
	}
}
