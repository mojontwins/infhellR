package net.minecraft.game.world;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;

public abstract class WorldProvider {
	public World worldObj;
	public WorldChunkManager worldChunkMgr;
	public boolean isNether = false;
	public boolean isHellWorld = false;
	public boolean hasNoSky = false;
	public float[] lightBrightnessTable = new float[16];
	public int worldType = 0;
	private float[] colorsSunriseSunset = new float[4];

	public final void registerWorld(World world1) {
		this.worldObj = world1;
		this.registerWorldChunkManager();
		this.generateLightBrightnessTable();
	}

	protected void generateLightBrightnessTable() {
		float f1 = 0.05F;

		for(int i2 = 0; i2 <= 15; ++i2) {
			float f3 = 1.0F - (float)i2 / 15.0F;
			this.lightBrightnessTable[i2] = (1.0F - f3) / (f3 * 3.0F + 1.0F) * (1.0F - f1) + f1;
		}

	}

	protected void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManager(this.worldObj);
	}

	public IChunkProvider getChunkProvider() {
		/*
		if(this.worldObj.worldInfo.getTerrainType() == WorldType.SKY) {
			return new ChunkProviderSky(this.worldObj, this.worldObj.getRandomSeed());
		} else {
			return new ChunkProviderGenerate(this.worldObj, this.worldObj.getRandomSeed());
		}
		*/
		return this.worldObj.worldInfo.getTerrainType().getChunkGenerator(worldObj);
	}

	public boolean canCoordinateBeSpawn(int i1, int i2) {
		int i3 = this.worldObj.getFirstUncoveredBlock(i1, i2);
		return i3 == Block.sand.blockID;
	}

	/*
	public float calculateCelestialAngle(long j1, float f3) {
		int i4 = (int)(j1 % 24000L);
		float f5 = ((float)i4 + f3) / 24000.0F - 0.25F;
		if(f5 < 0.0F) {
			++f5;
		}

		if(f5 > 1.0F) {
			--f5;
		}

		float f6 = f5;
		f5 = 1.0F - (float)((Math.cos((double)f5 * Math.PI) + 1.0D) / 2.0D);
		f5 = f6 + (f5 - f6) / 3.0F;
		return f5;
	}
	*/
	
	public float calculateCelestialAngle(long worldTime, float renderPartialTick) {
		// Thanks for the pointers jonk!
		float tickWithinCycle = (int)(worldTime % 24000L) + renderPartialTick;
		
		boolean isDay = tickWithinCycle < Seasons.dayLengthTicks;
		
		float partProgress = isDay ? 
				(float)tickWithinCycle / (float)Seasons.dayLengthTicks
			:
				(float)(tickWithinCycle - Seasons.dayLengthTicks) / (float)Seasons.nightLengthTicks
		;
				
		float dayProgress = isDay ? 
				partProgress / 2.0F
			:
				0.5F + partProgress / 2.0F
		;
		
		dayProgress -= 0.25F;
		
		if(dayProgress < 0.0F) dayProgress ++;
		if(dayProgress > 1.0F) dayProgress --;
		
		float f2 = dayProgress;
		dayProgress = 1.0F - (float)((Math.cos((double)dayProgress * Math.PI) + 1.0D) / 2D);
		dayProgress = f2 + (dayProgress - f2) / 3F;
		
		return dayProgress;
	}

	public float[] calcSunriseSunsetColors(float f1, float f2) {
		float f3 = 0.4F;
		float f4 = MathHelper.cos(f1 * (float)Math.PI * 2.0F) - 0.0F;
		float f5 = -0.0F;
		if(f4 >= f5 - f3 && f4 <= f5 + f3) {
			float f6 = (f4 - f5) / f3 * 0.5F + 0.5F;
			float f7 = 1.0F - (1.0F - MathHelper.sin(f6 * (float)Math.PI)) * 0.99F;
			f7 *= f7;
			this.colorsSunriseSunset[0] = f6 * 0.3F + 0.7F;
			this.colorsSunriseSunset[1] = f6 * f6 * 0.7F + 0.2F;
			this.colorsSunriseSunset[2] = f6 * f6 * 0.0F + 0.2F;
			this.colorsSunriseSunset[3] = f7;
			return this.colorsSunriseSunset;
		} else {
			return null;
		}
	}

	public Vec3D getFogColor(float f1, float f2, boolean bloodMoon, boolean colouredAthmospherics) {
		float f3 = MathHelper.cos(f1 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		float f4, f5, f6;
		
		if(colouredAthmospherics) {
			int fogColor = Seasons.getFogColorForToday();
			f4 = (float)(fogColor >> 16 & 255L) / 255.0F;
			f5 = (float)(fogColor >> 8 & 255L) / 255.0F;
			f6 = (float)(fogColor & 255L) / 255.0F;

			f4 *= f3 * 0.90F + 0.06F;
			f5 *= f3 * 0.90F + 0.10F;
			f6 *= f3 * 0.80F + 0.20F;

		} else {
			f4 = 0.7529412F;
			f5 = 0.84705883F;
			f6 = 1.0F;
			// RGB is = 192, 216, 255 = 0xC0D8FF
		
			f4 *= f3 * 0.94F + 0.06F;
			f5 *= f3 * 0.94F + 0.06F;
			f6 *= f3 * 0.91F + 0.09F;
			
		}
		
		// Red tint if bloodMoon
		if (bloodMoon) {
			f4 += 0.50F * (1.0F - f3); if(f4 > 1.0F) f4 = 1.0F;
		}
		
		return Vec3D.createVector((double)f4, (double)f5, (double)f6);
	}

	public boolean canRespawnHere() {
		return true;
	}

	public static WorldProvider getProviderForDimension(int i0) {
		return (WorldProvider)(i0 == -1 ? new WorldProviderHell() : (i0 == 0 ? new WorldProviderSurface() : (i0 == 1 ? new WorldProviderSky() : null)));
	}

	public float getCloudHeight() {
		return 108.0F;
	}

	public boolean func_28112_c() {
		return false;
	}

	public int[] updateLightmap(EntityPlayer thePlayer, float gammaSetting) {
		int[] lightmapColors = new int[256];
		
		World world1 = this.worldObj;
		float sb = world1.getSunBrightness(1.0F);		
		for(int i2 = 0; i2 < 256; ++i2) {
			float f3 = sb * 0.95F + 0.05F;
			float f4 = world1.worldProvider.lightBrightnessTable[i2 / 16] * f3;
			float f5 = world1.worldProvider.lightBrightnessTable[i2 % 16]/* * (this.torchFlickerX * 0.1F + 1.5F)*/;
			if(world1.lightningFlash > 0) {
				f4 = world1.worldProvider.lightBrightnessTable[i2 / 16];
			}

			float f6 = f4 * (sb * 0.65F + 0.35F);
			float f12 = f6 + f5;

			f12 = f12 * 0.96F + 0.03F;

			if(world1.worldProvider.worldType == 1) {
				f12 = 0.22F + f5 * 0.75F;
			}

			// Set up night vision with code somewhat stolen from r1.5.2!
				if(thePlayer.divingHelmetOn() && thePlayer.isInsideOfMaterial(Material.water)) {
				float nVB = 0.5F;					
				float fNV = 1.0F / f12;					
				f12 = f12 * (1.0F - nVB) + f12 * fNV * nVB;
			}
			
			if(f12 > 1.0F) {
				f12 = 1.0F;
			}
	
			float f15 = gammaSetting - 0.55F;
			float f16 = 1.0F - f12;
	
			f16 = 1.0F - f16 * f16 * f16 * f16;				
			f12 = f12 * (1.0F - f15) + f16 * f15;
			f12 = f12 * 0.96F + 0.03F;

			if(f12 > 1.0F) {
				f12 = 1.0F;
			}

			if(f12 < 0.0F) {
				f12 = 0.0F;
			}

			short s19 = 255;
			int i20 = (int)(f12 * 255.0F);
			lightmapColors[i2] = s19 << 24 | i20 << 16 | i20 << 8 | i20;
		}

		
		return lightmapColors;
	}
}
