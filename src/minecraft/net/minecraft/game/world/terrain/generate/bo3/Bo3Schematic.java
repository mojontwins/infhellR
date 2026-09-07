package net.minecraft.game.world.terrain.generate.bo3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockState;
import net.minecraft.game.world.World;

public class Bo3Schematic {
	public static final boolean infHell = true;
	
	/*
	 * List of blocks to paint to the world
	 */
	private List<BlockState> blocks = new ArrayList<BlockState> ();
	
	/*
	 * List of checks "is block at (x, y, z) = ...? before spawning
	 */
	private List<BlockState> blockChecks = new ArrayList<BlockState> ();
	
	/*
	 * List of blocks this structure can overwrite
	 */
	private List<BlockState> sourceBlocks = new ArrayList<BlockState> ();
	
	/*
	 * 0 - 0 degrees   X, Z
	 * 1 - 90 degrees  Z, -X
	 * 2 - 180 degrees -X, -Z
	 * 3 - 270 degrees -Z, X
	 */
	private int orthoAngle; 
	
	/*
	 * Height to add to Y0
	 */
	private int spawnHeightOffset = 0;
	
	/*
	 * If != 0, random to add to Y0 
	 */
	private int spawnHeightVariance = 0;
	
	/*
	 * Custom : force this meta for leaves
	 */
	private int leavesMeta = 0;

	private boolean extendBottom = false;
	
	public String category;
	public String name;

	private boolean debug = false;
	
	public Bo3Schematic() {
	}
	
	public int rotateX(int x, int z) {
		switch(this.orthoAngle) {
		case 0:
		default:
			return x;
		case 1:
			return z;
		case 2:
			return -x;
		case 3:
			return -z;
		}
	}
	
	public int rotateZ(int x, int z) {
		switch(this.orthoAngle) {
		case 0:
		default:
			return z;
		case 1:
			return -x;
		case 2: 
			return -z;
		case 3:
			return x;
		}
	}
	
	public void addBlockState(BlockState blockState) {
		this.blocks.add(blockState);
	}
	
	public void addBlockCheck(BlockState blockState) {
		this.blockChecks.add(blockState);
	}
	
	public void render(World world, Random rand, int x0, int y0, int z0) {
		int x, y, z;
		int blockID, meta;
		
		y0 += this.spawnHeightOffset;
		if(this.spawnHeightVariance > 0) y0 += rand.nextInt(this.spawnHeightVariance);
		
		for(BlockState blockState : this.blocks) {
			x = this.rotateX(blockState.getX(), blockState.getZ());
			y = blockState.getY();
			z = this.rotateZ(blockState.getX(), blockState.getZ());
			
			blockID = blockState.getBlock().blockID;
			meta = (blockID == Block.leaves.blockID) ? this.leavesMeta : blockState.getMetadata();
			
			world.setBlockAndMetadata(x0 + x, y0 + y, z0 + z, blockID, meta);
			
			if(this.extendBottom && y <= 2) {
				while((y0 + y) > 0 && !world.isBlockOpaqueCube(x0 + x, y0 + y - 1, z0 + z)) { world.setBlockAndMetadata(x0 + x, y0 + y, z0 + z, blockID, meta); y --; }
			}
		}
	}
	
	public boolean checkCanGrowOn(World world, int x0, int y0, int z0) {
		BlockState blockState = world.getBlockStateAt(x0, y0, z0);
		if(blockState == null || blockState.getBlock() == null) return false;
		
		return blockState.getBlock().canGrowPlants();		
	}
	
	public boolean canSpawnHere(World world, int x0, int y0, int z0) {
		// Do some checks:
		int x, y, z;
		int xx, zz;
		boolean result;
		
		// New logic
		// 1.- If there are block checks, and one success -> go.
		// 2.- If no block checks, run checkCanGrownOn
		
		if(this.debug) System.out.println (this.toString() + " BlockChecks size = " + this.blockChecks.size());
		
		if(this.blockChecks.size() == 0) {
			// 1.- checkCanGrowOwn (x0, y0 - 1, z0);
			
			if(!this.checkCanGrowOn(world, x0, y0 - 1, z0)) {
				if(this.debug) System.out.println (this.toString() + " failed checkCanGrowOn " + x0 + " " + (y0 - 1) + " " + z0 + " >> " + world.getBlockStateAt(x0, y0 - 1, z0));
				return false;
			}
		} else {
			// 2.- Any blockChecks (if any) succeed;
		
			result = false;
			for(BlockState blockCheck : this.blockChecks) {
				xx = blockCheck.getX();
				zz = blockCheck.getZ();
				
				x = this.rotateX(xx, zz) + x0;
				y = blockCheck.getY() + y0;
				z = this.rotateZ(xx, zz) + z0;
				
				BlockState blockState = world.getBlockStateAt(x, y, z);
				if(blockState.equals(blockCheck)) {
					result = true; 
					break;
				}
			}
			
			// If none of the checks succeeded, can't spawn
			if(!result) {
				if(this.debug) System.out.println (this.toString() + " failed blockChecks");
				return false;
			}
		}
		
		// 3.- Iterates the block list and makes sure all blocks will replace blocks in sourceBlocks
		
		for(BlockState block : this.blocks) {
			xx = block.getX();
			zz = block.getZ();
			
			x = this.rotateX(xx, zz) + x0;
			y = block.getY() + y0;
			z = this.rotateZ(xx, zz) + z0;
			
			BlockState blockState = world.getBlockStateAt(x, y, z);
			
			result = false;
			for(BlockState sourceBlock : this.sourceBlocks) {
				if(blockState.equals(sourceBlock)) {
					result = true;
					break;
				}
			}
			
			// If existing block was none of the blocks listed, can't spawn
			if(!result) {
				String blockName = "Air";
				try {
					blockName = blockState.getBlock().getBlockName();
				} catch (Exception e) {}
				if(this.debug) System.out.println (this.toString() + " failed sourceBlock, found " + blockName + " @ " + x + " " + y + " " + z);
				return false;
			}
		}
		
		if(this.debug) System.out.println (this.toString() + " success @ " + x0 + " " + y0 + " " + z0);
		
		return true;
	}

	public List<BlockState> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<BlockState> blocks) {
		this.blocks = blocks;
	}

	public int getOrthoAngle() {
		return orthoAngle;
	}

	public void setOrthoAngle(int orthoAngle) {
		this.orthoAngle = orthoAngle;
	}

	public List<BlockState> getBlockChecks() {
		return blockChecks;
	}

	public void setBlockChecks(List<BlockState> blockChecks) {
		this.blockChecks = blockChecks;
	}

	public Bo3Schematic fromFile(Path path) {
		InputStream inputStream = null; 
		
		try {
			BufferedReader reader = Files.newBufferedReader(path);
			
			// This only understands a subset of the BO3 stuff.
			
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();

				// 1st parse for "Block" or "BlockCheck"
				if (line.startsWith("block(")) {
					
					// Block(x,y,z,blockRepresentation)
					
					String arguments[] = this.parseFrom(line, "block(", ")");
					
					int blockID = this.translateBlock(arguments[3]);
					
					this.addBlockState(new BlockState(
							blockID, 
							this.translateMeta(arguments[3], blockID), 
							Integer.parseInt(arguments[0]), 
							Integer.parseInt(arguments[1]), 
							Integer.parseInt(arguments[2])
					));
				} else if (line.startsWith("blockcheck(")) {
					
					// BlockCheck(x,y,z,blockRepresentation1, ...)
					
					String arguments[] = this.parseFrom(line, "blockcheck(", ")");
					
					for(int i = 3; i < arguments.length; i ++) {
						int blockID = this.translateBlock(arguments[i]);
						
						this.addBlockCheck(new BlockState(
								blockID, 
								this.translateMeta(arguments[i], blockID), 
								Integer.parseInt(arguments[0]), 
								Integer.parseInt(arguments[1]), 
								Integer.parseInt(arguments[2])
						));
					}
				} 
			}
			
			// The rest of things come as properties
			inputStream = Files.newInputStream(path);
			Properties properties = new Properties();
			properties.load(inputStream);
			
			// Do things
			this.populateSourceBlocks((String) properties.get("SourceBlocks"));
			this.spawnHeightOffset = Integer.parseInt(properties.getProperty("SpawnHeightOffset"));
			this.spawnHeightVariance = Integer.parseInt(properties.getProperty("SpawnHeightVariance"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(inputStream != null)	try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}

	private void populateSourceBlocks(String list) {
		String[] blockRepresentations = list.split(",");
		
		for (String blockRepresentation : blockRepresentations) {
			int blockID = this.translateBlock(blockRepresentation);
			this.sourceBlocks.add(new BlockState(blockID, this.translateMeta(blockRepresentation, blockID)));
		}
	}

	private String[] parseFrom(String line, String prefix, String suffix) {
		String strippedLine = line.substring(prefix.length(), line.length() - suffix.length());
		return strippedLine.split(",");
	}
	
	private int translateBlock(String blockRepresentation) {
		if (blockRepresentation == null) return 0;
		
		int index = blockRepresentation.indexOf(':');
		if (index > 0) {
			blockRepresentation = blockRepresentation.substring(0, index);
		}
		
		return Bo3BlockTranslationTable.getId(blockRepresentation.toUpperCase());
	}
	
	private int translateMeta(String blockRepresentation, int blockID) {
		if (blockRepresentation == null) return 0;
		
		int index = blockRepresentation.indexOf(':');
		if (index < 0) return 0;
		
		int meta = 0; 
		
		try {
			meta = Integer.parseInt(blockRepresentation.substring(index + 1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(infHell) {
			// Do some infHell translation
			
			if(blockID == Block.wood.blockID){
				int orientation = meta >> 4;
				if(orientation == 4) meta = 12;
				else if(orientation == 8) meta = 4;
				else meta = 0;
			} else if(blockID == Block.leaves.blockID) {
				meta = this.leavesMeta;
			} else if(
					blockID == Block.stone.blockID ||
					blockID == Block.dirt.blockID
			) {
				meta = 0;
			}
		}
		
		return meta;
	}

	public List<BlockState> getSourceBlocks() {
		return sourceBlocks;
	}

	public void setSourceBlocks(List<BlockState> sourceBlocks) {
		this.sourceBlocks = sourceBlocks;
	}

	public Bo3Schematic withCategoryAndName(String category, String name) {
		this.name = name;
		this.category = category;
		return this;
	}
	
	public Bo3Schematic withLeavesMeta(int meta) {
		this.leavesMeta = meta;
		return this;
	}
	
	public String toString() {
		return this.category + ":" + this.name;
	}

	public void setExtendBottom(boolean extendBottom) {
		this.extendBottom = extendBottom;
	}
}

