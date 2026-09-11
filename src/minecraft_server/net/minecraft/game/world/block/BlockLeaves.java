package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;

public class BlockLeaves extends BlockLeavesBase {

	// Leaves are special a bit cumbersome, mostly I have planed this mod for 0 hours before starting.
	// Bits 0-2 are used to colorize the block.
	// Bit 3 is, like in vanilla, the "don't decay" flag. When you PLACE leaves, they don't decay.
	// Bits 7-4 are used to decide which tree these leaves are from, so the right sapling can drop.
	
	public static int OAK = 0;
	public static int BAOBAB = 0x10;
	public static int CYPRESS = 0x20;
	public static int FIR = 0x30;
	public static int JUNGLE = 0x40;
	public static int MANGROVE = 0x50;
	public static int TAIGA = 0x60;
	public static int FANCYFIR = 0x70;
	public static int WILLOW = 0x80;
	public static int SHRUB = 0x90;

	/** Metadata bit a removed log stamps onto a leaf to make it re-verify its trunk next tick. */
	public static final int DECAY_CHECK_BIT = 8;
	
	private int leafTexIndex;

	// --- Decay search geometry -------------------------------------------------
	// The 9x9x9 search box sits inside an 11x11x11 padded grid. The one-cell pad
	// lets every +-1 neighbour stride land on a real slot, so the inner flood-fill
	// loop needs no bounds checks.
	private static final int RADIUS = 4;                 // connectivity distance to a log
	private static final int SPAN = RADIUS * 2 + 1;      // 9 cells per axis
	private static final int GRID = SPAN + 2;            // 11 (padded dimension)
	private static final int GRID_AREA = GRID * GRID;    // 121
	private static final int HALF = GRID >> 1;           // 5 (centre offset)
	private static final int CENTER = HALF * GRID_AREA + HALF * GRID + HALF; // 665
	private static final int CELL_COUNT = SPAN * SPAN * SPAN; // 729

	/** The six +-1 neighbour strides, in x/y/z pairs. */
	private static final int[] NEIGHBOR_STRIDES = { GRID_AREA, -GRID_AREA, GRID, -GRID, 1, -1 };

	// Precomputed per-probe-cell tables: the block offset (dx/dy/dz) and the flat
	// grid cursor for each of the 729 cells, in scan order (x-major, z-minor).
	private static final int[] PROBE_DX = new int[CELL_COUNT];
	private static final int[] PROBE_DY = new int[CELL_COUNT];
	private static final int[] PROBE_DZ = new int[CELL_COUNT];
	private static final int[] PROBE_CURSOR = new int[CELL_COUNT];

	static {
		int i = 0;
		for(int dx = -RADIUS; dx <= RADIUS; ++dx) {
			for(int dy = -RADIUS; dy <= RADIUS; ++dy) {
				for(int dz = -RADIUS; dz <= RADIUS; ++dz) {
					PROBE_DX[i] = dx;
					PROBE_DY[i] = dy;
					PROBE_DZ[i] = dz;
					PROBE_CURSOR[i] = (dx + HALF) * GRID_AREA + (dy + HALF) * GRID + (dz + HALF);
					++i;
				}
			}
		}
	}

	/** Scratch grid reused across ticks (the block is a singleton, so one array serves). */
	private final int[] adjacency = new int[GRID * GRID * GRID];

	// Change to fancy colors for fancy trees and use metadata 1..7 (0 means "biome
	// controlled")
	public static int[] fixedColors = { 
			0x5BFB3B, // Normal neon green
			0xF6F535, // Yellower for arid biomes
			0x9BE658, // Average normal / yellowish, dying Oaks
			0x5BFB3B, 0x5BFB3B, 0x5BFB3B, 0x5BFB3B, 
			0x5BFB3B // This will NEVER be used, it means "Seasonal
					 // colorizer"
	};

	protected BlockLeaves(int id, int blockIndex) {
		super(id, blockIndex, Material.leaves, false);
		this.leafTexIndex = blockIndex;
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		// Small optimization: When replaced with leaves or wood, surrounding leaves are
		// NOT affected
		if (blockID == Block.wood.blockID || blockID == Block.leaves.blockID)
			return;
		
		this.onBlockRemovalDo(world, x, y, z);
	}

	public int quantityDropped(Random Random) {
		return Random.nextInt(20) != 0 ? 0 : 1;
	}

	public int idDropped(int i, Random Random) {
		return Random.nextInt(50) == 0 ? Item.appleRed.shiftedIndex : Block.sapling.blockID;
	}

	protected int damageDropped(int meta) {
		// Damage dropped is used in saplings. The top 4 bits in the meta byte
		// are used to represent tree types.
		return meta & 0xf0;
	}
	
	public boolean isOpaqueCube() {
		return !this.graphicsLevel;
	}

	public void setGraphicsLevel(boolean flag) {
		this.graphicsLevel = flag;
		this.blockIndexInTexture = this.leafTexIndex + (flag ? 0 : 1);
	}

	public void onEntityWalking(World world, int i, int j, int k, Entity entity) {
		super.onEntityWalking(world, i, j, k, entity);
	}

	public void onBlockRemoval(World world, int x, int y, int z) {
		int blockID = world.getBlockId(x, y, z);

		// Small optimization: When replaced with leaves or wood, surrounding leaves are
		// NOT affected
		if (blockID == Block.wood.blockID || blockID == Block.leaves.blockID)
			return;
		
		this.onBlockRemovalDo(world, x, y, z);
	}

	public void onBlockRemovalDo(World world, int x, int y, int z) {
		byte radius = 1;
		int range = radius + 1;

		if (world.checkChunksExist(x - range, y - range, z - range, x + range, y + range, z + range)) {
			for (int xx = -radius; xx <= radius; ++xx) {
				for (int yy = -radius; yy <= radius; ++yy) {
					for (int zz = -radius; zz <= radius; ++zz) {
						int i2 = world.getBlockId(x + xx, y + yy, z + zz);
						if (i2 == this.blockID) {
							int j2 = world.getBlockMetadata(x + xx, y + yy, z + zz);
							world.setBlockMetadata(x + xx, y + yy, z + zz, j2 | DECAY_CHECK_BIT);
						}
					}
				}
			}
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int metadata = world.getBlockMetadata(x, y, z);

			// Only a leaf a removed log has flagged needs re-checking; the rest stay put.
			if ((metadata & DECAY_CHECK_BIT) == 0) {
				return;
			}

			// Wait for every corner chunk so a half-loaded world never drops a leaf the
			// generator hasn't finished. The box is one larger than the search radius.
			int extent = RADIUS + 1;
			if (!world.checkChunksExist(x - extent, y - extent, z - extent, x + extent, y + extent, z + extent)) {
				return;
			}

			// Classify the neighbourhood: 0 = log, -2 = leaves, -1 = anything else.
			// Any wood-material block counts as a log (hollow/chipped logs included),
			// and every BlockLeaves subtype counts as leaves.
			for (int i = 0; i < CELL_COUNT; ++i) {
				Block block = Block.blocksList[world.getBlockId(x + PROBE_DX[i], y + PROBE_DY[i], z + PROBE_DZ[i])];
				int cursor = PROBE_CURSOR[i];

				if (block != null && block.blockMaterial == Material.wood) {
					adjacency[cursor] = 0;
				} else if (block instanceof BlockLeaves) {
					adjacency[cursor] = -2;
				} else {
					adjacency[cursor] = -1;
				}
			}

			// Flood from each log (distance 0) outward through leaves, at most RADIUS steps.
			for (int distance = 1; distance <= RADIUS; ++distance) {
				for (int i = 0; i < CELL_COUNT; ++i) {
					int cursor = PROBE_CURSOR[i];
					if (adjacency[cursor] == distance - 1) {
						for (int stride : NEIGHBOR_STRIDES) {
							int neighbor = cursor + stride;
							if (adjacency[neighbor] == -2) {
								adjacency[neighbor] = distance;
							}
						}
					}
				}
			}

			if (adjacency[CENTER] >= 0) {
				// Still tethered to a log: keep the leaves and clear the re-check mark.
				world.setBlockMetadata(x, y, z, metadata & ~DECAY_CHECK_BIT);
			} else {
				this.removeLeaves(world, x, y, z);
			}
		}
	}

	/** Drops the leaf's usual drops and replaces it with air. */
	private void removeLeaves(World world, int x, int y, int z) {
		this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
		world.setBlockWithNotify(x, y, z, 0);
	}

	// Not as complex as a colorizer, but allows for some freedom!
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return this.getRenderColor(blockAccess.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta) {
		meta &= 7;
		if (meta == 7)
			return Seasons.getLeavesColorForToday();
		return BlockLeaves.fixedColors[meta];
	}

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 2; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}
    
	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if (entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == Item.shears.shiftedIndex) {
			this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.leaves, 1, world.getBlockMetadata(x, y, z)));
			world.setBlockWithNotify(x, y, z, 0);
			world.playSoundEffect((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, this.stepSound.getStepSound(), (this.stepSound.getVolume() + 1.0F) / 8.0F, this.stepSound.getPitch() * 0.5F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public int getEncouragementToFire() {
		return 30;
	}

	@Override
	public int getAbilityToCatchFire() {
		return 60;
	}
}
