package net.minecraft.client.render.tileentity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.block.tileentity.TileEntityPiston;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.RenderEngine;

public class TileEntityRenderer {
	private Map<Class<?>, TileEntitySpecialRenderer> specialRendererMap = new HashMap<Class<?>, TileEntitySpecialRenderer>();
	public static TileEntityRenderer instance = new TileEntityRenderer();
	private FontRenderer fontRenderer;
	public static double staticPlayerX;
	public static double staticPlayerY;
	public static double staticPlayerZ;
	public RenderEngine renderEngine;
	public World worldObj;
	public EntityLiving entityLivingPlayer;
	public float playerYaw;
	public float playerPitch;
	public double playerX;
	public double playerY;
	public double playerZ;

	private TileEntityRenderer() {
		this.specialRendererMap.put(TileEntitySign.class, new TileEntitySignRenderer());
		this.specialRendererMap.put(TileEntityMobSpawner.class, new TileEntityMobSpawnerRenderer());
		this.specialRendererMap.put(TileEntityMobSpawnerOneshot.class, new TileEntityMobSpawnerOneshotRenderer());
		this.specialRendererMap.put(TileEntityPiston.class, new TileEntityRendererPiston());
		Iterator<TileEntitySpecialRenderer> iter = this.specialRendererMap.values().iterator();

		while(iter.hasNext()) {
			TileEntitySpecialRenderer renderer = iter.next();
			renderer.setTileEntityRenderer(this);
		}

	}

	public TileEntitySpecialRenderer getSpecialRendererForClass(Class<?> clazz) {
		TileEntitySpecialRenderer renderer = this.specialRendererMap.get(clazz);
		if(renderer == null && clazz != TileEntity.class) {
			renderer = this.getSpecialRendererForClass(clazz.getSuperclass());
			this.specialRendererMap.put(clazz, renderer);
		}

		return renderer;
	}

	public boolean hasSpecialRenderer(TileEntity tileEntity) {
		return this.getSpecialRendererForEntity(tileEntity) != null;
	}

	public TileEntitySpecialRenderer getSpecialRendererForEntity(TileEntity tileEntity) {
		return tileEntity == null ? null : this.getSpecialRendererForClass(tileEntity.getClass());
	}

	public void cacheActiveRenderInfo(World world, RenderEngine renderEngine, FontRenderer fontRenderer, EntityLiving entityLiving, float partialTicks) {
		if(this.worldObj != world) {
			this.cacheSpecialRenderInfo(world);
		}

		this.renderEngine = renderEngine;
		this.entityLivingPlayer = entityLiving;
		this.fontRenderer = fontRenderer;
		this.playerYaw = entityLiving.prevRotationYaw + (entityLiving.rotationYaw - entityLiving.prevRotationYaw) * partialTicks;
		this.playerPitch = entityLiving.prevRotationPitch + (entityLiving.rotationPitch - entityLiving.prevRotationPitch) * partialTicks;
		this.playerX = entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * (double)partialTicks;
		this.playerY = entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * (double)partialTicks;
		this.playerZ = entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * (double)partialTicks;
	}

	public void renderTileEntity(TileEntity tileEntity, float partialTicks) {
		if(tileEntity.getDistanceFrom(this.playerX, this.playerY, this.playerZ) < 4096.0D) {
			int lightValue = this.worldObj.getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0);
			int skyLight = lightValue % 65536;
			int blockLight = lightValue / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight / 1.0F, (float)blockLight / 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.renderTileEntityAt(tileEntity, (double)tileEntity.xCoord - staticPlayerX, (double)tileEntity.yCoord - staticPlayerY, (double)tileEntity.zCoord - staticPlayerZ, partialTicks);
		}

	}

	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		TileEntitySpecialRenderer renderer = this.getSpecialRendererForEntity(tileEntity);
		if(renderer != null) {
			renderer.renderTileEntityAt(tileEntity, x, y, z, partialTicks);
		}

	}

	public void cacheSpecialRenderInfo(World world) {
		this.worldObj = world;
		Iterator<TileEntitySpecialRenderer> iter = this.specialRendererMap.values().iterator();

		while(iter.hasNext()) {
			TileEntitySpecialRenderer renderer = iter.next();
			if(renderer != null) {
				renderer.cacheSpecialRenderInfo(world);
			}
		}

	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}
}
