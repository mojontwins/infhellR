package net.minecraft.game.command;

import net.minecraft.game.world.terrain.generate.WorldGenAcacia;
import net.minecraft.game.world.terrain.generate.WorldGenCypress;
import net.minecraft.game.world.terrain.generate.WorldGenFir;
import net.minecraft.game.world.terrain.generate.WorldGenMangrove;
import net.minecraft.game.world.terrain.generate.WorldGenNylium;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPalmTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPineTree;
import net.minecraft.game.world.terrain.generate.WorldGenWillow;

import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class CommandTree extends CommandBase {
	
	// Will only work as single player command (for debugging)
	public boolean shouldList() {
		return false;
	}

	public CommandTree() {
	}

	@Override
	public String getString() {
		return "tree";
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		int x = 0, y = 0, z = 0;
		String treeShape = "";
		String treeVariant = "";
		
		BlockPos soilBlockPos = this.theCommandSender.getMouseOverCoordinates();
		if(soilBlockPos != null) {
			x = soilBlockPos.x;
			y = soilBlockPos.y + 1;
			z = soilBlockPos.z;
		}
		
		if (idx >= 4) {
			x = Integer.parseInt(tokens[1]);
			z = Integer.parseInt(tokens[2]);
			y = theWorld.getLandSurfaceHeightValue(x, z);
			
			treeShape = tokens[3];
			if(idx > 4) treeVariant = tokens[4];
		} else {
			treeShape = tokens[1];
			if(idx > 2) treeVariant = tokens[2];
		}
		
		// This may look cheesy but may improve in the future (NOT)
		WorldGenerator treeGen = null;
		
		if("fir".equals(treeShape)) {
			if("big".equals(treeVariant)) {
				treeGen = new WorldGenFir(8 + theWorld.rand.nextInt(6), true);
			} else {
				treeGen = new WorldGenFir(4 + theWorld.rand.nextInt(4), false);
			}
		} else if("pine".equals(treeShape)) {
			treeGen = new WorldGenPineTree(8 + theWorld.rand.nextInt(6), true);
		} else if("mangrove".equals(treeShape)) {
			treeGen = new WorldGenMangrove(true);
			y = 63;
		} else if("nylium".equals(treeShape)) {
			treeGen = new WorldGenNylium(true);
		} else if("acacia".equals(treeShape)) {
			treeGen = new WorldGenAcacia(true);
		} else if("willow".equals(treeShape)) {
			treeGen = new WorldGenWillow(4 + theWorld.rand.nextInt(4), true);
		} else if("cypress".equals(treeShape)) {
			treeGen = new WorldGenCypress(5 + theWorld.rand.nextInt(5), true);
		} else if("palm".equals(treeShape)) {
			treeGen = new WorldGenPalmTree(true);
		}  else if("compare".equals(treeShape)) {
		}
		
		if(treeGen != null) {
			treeGen.generate(theWorld, theWorld.rand, x, y, z);
		}
		
		return 0;
	}

	@Override
	public String getHelp() {
		return "[debug command]";
	}

}
