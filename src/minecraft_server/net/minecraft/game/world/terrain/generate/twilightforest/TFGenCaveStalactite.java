package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.World;

public class TFGenCaveStalactite extends TFGenerator {
	public double size;
	public int bType;
	public boolean hang;
	public int dir;

	// Overcomplicated this 'cause my floor may be way bumpier than TF's.
	// Also changed stuff to make it a bit faster.
	
	public TFGenCaveStalactite(int blockType, double sizeFactor, boolean down) {
		this.bType = blockType;
		this.size = sizeFactor;
		this.hang = down;
		this.dir = this.hang ? -1 : 1;
	}

	public static TFGenCaveStalactite makeRandomOreStalactite(Random rand, int caveSize) {
		if(caveSize >= 3 || caveSize >= 2 && rand.nextInt(5) == 0) {
			switch(rand.nextInt(6)) {
			case 0:	return new TFGenCaveStalactite(Block.oreDiamond.blockID, rand.nextDouble() * 0.5D, true);
			case 1:	return new TFGenCaveStalactite(Block.oreRuby.blockID, rand.nextDouble() * 0.5D, true);
			case 2: return new TFGenCaveStalactite(Block.oreEmerald.blockID, rand.nextDouble() * 0.5D, true);
			}
		}

		if(caveSize >= 2 || caveSize >= 1 && rand.nextInt(5) == 0) {
			switch(rand.nextInt(6)) {
			case 0: return new TFGenCaveStalactite(Block.oreGold.blockID, rand.nextDouble() * 0.6D, true);
			case 1:
			case 2:	return new TFGenCaveStalactite(Block.oreRedstone.blockID, rand.nextDouble() * 0.8D, true);
			}
		}

		switch(rand.nextInt(5)) {
		case 0:
		case 1: return new TFGenCaveStalactite(Block.oreIron.blockID, rand.nextDouble() * 0.7D, true);
		case 2:
		case 3: return new TFGenCaveStalactite(Block.oreCoal.blockID, rand.nextDouble() * 0.8D, true);
		}
		
		return new TFGenCaveStalactite(Block.glowStone.blockID, rand.nextDouble() * 0.5D, true);
	}

	public boolean generate(World world, Random random, int x, int y, int z) {
		// When called during overworld generation, y = 65 and terrain may get under water (<64).
		// This method finds the starting point of the stalag[ti|mi]te and its length.
		
		this.worldObj = world;
		int ceiling = world.getWorldHeight() + 1;
		int floor = -1;

		int length;
		Material material;
		
		// My floor may be bumpy or be underwater. Thus, make sure we start at a proper height.
		// Depending on what we find at x, y, z, we go up or down.
		if(world.isBlockOpaqueCube(x, y, z)) {
			// We found a solid block. Let's rise until we find air
			while(y < world.getWorldHeight() && !world.isAirBlock(x, y ++, z));
		} else {
			// We found a non solid block. Let's sink until we find solid beneath
			while(y > 1 && !world.isBlockOpaqueCube(x, y - 1, z)) { y --; }
		}
			
		// At this point we are in the lowest possible spot where there's non solid over solid.
		
		// Let's now find the ceiling:
		for(length = y; length < world.getWorldHeight(); ++length) {
			material = this.worldObj.getBlockMaterial(x, length, z);
			if(material != Material.air && material != Material.water && material != Material.ice) {
				
				// Abort this estalag[ti|mi]te if we hit a solid not of this kind:
				if(material != Material.grass && material != Material.rock && material != Material.sand && material != Material.ground) {
					return false;
				}

				ceiling = length;
				break;
			}
		}
		
		// Ceiling contains the "y" of the first valid solid block.

		if(ceiling == 129) {
			return false;
		} else {
			// Extend down to find a proper length that fits.
			
			// That "32" is valid for the original Twilight Forest, but may serve our purposes in this port
			for(length = y; length > 32; --length) {
				material = this.worldObj.getBlockMaterial(x, length, z);
				
				if(material != Material.air && material != Material.water && material != Material.ice) {
					// For stalagmites (from floor up), a floor which is not grass, rock, ground, sand, etc.
					if(material != Material.grass && material != Material.rock && !this.hang && material != Material.ground && material != Material.sand && material != Material.lava) {
						return false;
					}

					floor = length;
					break;
				}
			}

			//ceiling -= 2; if(ceiling < floor) return false;
			
			length = (int)((double)(ceiling - floor) * this.size);
			
			if(this.bType == Block.oreDiamond.blockID && length > 4) {
				length = 4;
			}

			if(this.bType == Block.oreGold.blockID && length > 6) {
				length = 6;
			}

			if(length > 8 && (this.bType == Block.oreRuby.blockID || this.bType == Block.oreEmerald.blockID || this.bType == Block.oreRedstone.blockID || this.bType == Block.oreIron.blockID || this.bType == Block.glowStone.blockID)) {
				length = 8;
			}
			
			return this.makeSpike(random, x, this.hang ? ceiling : floor, z, length);
		}
	}

	public boolean makeSpike(Random random, int x, int y, int z, int length) {
		int dw = (int)((double)length / 4.5D);

		for(int dx = -dw; dx <= dw; ++dx) {
			for(int dz = -dw; dz <= dw; ++dz) {
				int ax = Math.abs(dx);
				int az = Math.abs(dz);
				int dist = (int)((double)Math.max(ax, az) + (double)Math.min(ax, az) * 0.5D);
				int dl = 0;
				if(dist == 0) {
					dl = length;
				}

				if(dist > 0) {
					dl = random.nextInt((int)((double)length / ((double)dist + 0.25D)));
				}

				// Real height. "y + 8" is a good place to start. This cap will prevent spikes going outside of the hill.
				int height = this.worldObj.getWorldHeight();
				if(this.hang) { 
					while(this.worldObj.getBlockId(x + dx, height, z + dz) == 0) height --;
				}
				
				for(int dy = 0; dy != dl * this.dir; dy += this.dir) {
					if(y + dy <= height) this.putBlock(x + dx, y + dy, z + dz, this.bType, false);
				}
			}
		}

		return true;
	}

}
