package net.minecraft.client.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import net.minecraft.game.EnumMovingObjectType;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.IWorldAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.effect.EntityBubbleFX;
import net.minecraft.client.effect.EntityDiggingFX;
import net.minecraft.client.effect.EntityExplodeFX;
import net.minecraft.client.effect.EntityFlameFX;
import net.minecraft.client.effect.EntityLavaFX;
import net.minecraft.client.effect.EntitySmokeFX;
import net.minecraft.client.effect.EntitySplashFX;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityFootStepFX;
import net.minecraft.client.particle.EntityGlowdustFX;
import net.minecraft.client.particle.EntityHeartFX;
import net.minecraft.client.particle.EntityNoteFX;
import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.client.particle.EntitySlimeFX;
import net.minecraft.client.particle.EntitySnowShovelFX;
import net.minecraft.client.particle.EntityStatusEffectFX;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.entity.RenderManager;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.game.item.ItemRecord;

public class RenderGlobal implements IWorldAccess {
	public List<TileEntity> tileEntities = new ArrayList<TileEntity>();
	public World worldObj;
	public RenderEngine renderEngine;
	public List<WorldRenderer> worldRenderersToUpdate = new ArrayList<WorldRenderer>();
	private WorldRenderer[] sortedWorldRenderers;
	private WorldRenderer[] worldRenderers;
	private int renderChunksWide;
	private int renderChunksTall;
	private int renderChunksDeep;
	private int glRenderListBase;
	public Minecraft mc;
	public RenderBlocks globalRenderBlocks;
	private IntBuffer glOcclusionQueryBase;
	private boolean occlusionEnabled = false;
	private int cloudOffsetX = 0;
	private int starGLCallList;
	private int glSkyList;
	private int glSkyList2;
	private int minBlockX;
	private int minBlockY;
	private int minBlockZ;
	private int maxBlockX;
	private int maxBlockY;
	private int maxBlockZ;
	private int renderDistance = -1;
	private int renderEntitiesStartupCounter = 2;
	private int countEntitiesTotal;
	private int countEntitiesRendered;
	private int countEntitiesHidden;
	int[] dummyBuf50k = new int[50000];
	IntBuffer occlusionResult = GLAllocation.createDirectIntBuffer(64);
	private int renderersLoaded;
	private int renderersBeingClipped;
	private int renderersBeingOccluded;
	private int renderersBeingRendered;
	private int renderersSkippingRenderPass;
	private int worldRenderersCheckIndex;
	private IntBuffer glListBuffer = BufferUtils.createIntBuffer(65536);
	double prevSortX = -9999.0D;
	double prevSortY = -9999.0D;
	double prevSortZ = -9999.0D;
	public float damagePartialTime;
	int frustumCheckOffset = 0;
	double prevReposX;
	double prevReposY;
	double prevReposZ;
	private long lastMovedTime = System.currentTimeMillis();
	private long lastActionTime = System.currentTimeMillis();

	public RenderGlobal(Minecraft minecraft, RenderEngine renderengine) {
		this.mc = minecraft;
		this.renderEngine = renderengine;
		
		/*
		byte maxChunkDim = 65;
		byte maxChunkHeight = 16;
		*/
		// Try this thx Birevan
		byte maxChunkDim = 34;
		byte maxChunkHeight = 8;
		
		this.glRenderListBase = GLAllocation.generateDisplayLists(maxChunkDim * maxChunkDim * maxChunkHeight * 3);
		this.occlusionEnabled = minecraft.getOpenGlCapsChecker().checkARBOcclusion();
		if(this.occlusionEnabled) {
			this.occlusionResult.clear();
			this.glOcclusionQueryBase = GLAllocation.createDirectIntBuffer(maxChunkDim * maxChunkDim * maxChunkHeight);
			this.glOcclusionQueryBase.clear();
			this.glOcclusionQueryBase.position(0);
			this.glOcclusionQueryBase.limit(maxChunkDim * maxChunkDim * maxChunkHeight);
			ARBOcclusionQuery.glGenQueriesARB(this.glOcclusionQueryBase);
		}

		this.starGLCallList = GLAllocation.generateDisplayLists(3);
		GL11.glPushMatrix();
		GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
		this.renderStars();
		GL11.glEndList();
		GL11.glPopMatrix();
		Tessellator tessellator = Tessellator.instance;
		this.glSkyList = this.starGLCallList + 1;
		GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
		byte byte1 = 64;
		int i = 256 / byte1 + 2;
		float f = 16.0F;

		int k;
		int i1;
		for(k = -byte1 * i; k <= byte1 * i; k += byte1) {
			for(i1 = -byte1 * i; i1 <= byte1 * i; i1 += byte1) {
				tessellator.startDrawingQuads();
				tessellator.addVertex((double)(k + 0), (double)f, (double)(i1 + 0));
				tessellator.addVertex((double)(k + byte1), (double)f, (double)(i1 + 0));
				tessellator.addVertex((double)(k + byte1), (double)f, (double)(i1 + byte1));
				tessellator.addVertex((double)(k + 0), (double)f, (double)(i1 + byte1));
				tessellator.draw();
			}
		}

		GL11.glEndList();
		this.glSkyList2 = this.starGLCallList + 2;
		GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
		f = -16.0F;
		tessellator.startDrawingQuads();

		for(k = -byte1 * i; k <= byte1 * i; k += byte1) {
			for(i1 = -byte1 * i; i1 <= byte1 * i; i1 += byte1) {
				tessellator.addVertex((double)(k + byte1), (double)f, (double)(i1 + 0));
				tessellator.addVertex((double)(k + 0), (double)f, (double)(i1 + 0));
				tessellator.addVertex((double)(k + 0), (double)f, (double)(i1 + byte1));
				tessellator.addVertex((double)(k + byte1), (double)f, (double)(i1 + byte1));
			}
		}

		tessellator.draw();
		GL11.glEndList();
		this.renderEngine.updateDynamicTextures();
	}

	private void renderStars() {
		Random random = new Random(10842L);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		for(int i = 0; i < 1500; ++i) {
			double d = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d3 = (double)(0.25F + random.nextFloat() * 0.25F);
			double d4 = d * d + d1 * d1 + d2 * d2;
			if(d4 < 1.0D && d4 > 0.01D) {
				d4 = 1.0D / Math.sqrt(d4);
				d *= d4;
				d1 *= d4;
				d2 *= d4;
				double d5 = d * 100.0D;
				double d6 = d1 * 100.0D;
				double d7 = d2 * 100.0D;
				double d8 = Math.atan2(d, d2);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(d * d + d2 * d2), d1);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for(int j = 0; j < 4; ++j) {
					double d17 = 0.0D;
					double d18 = (double)((j & 2) - 1) * d3;
					double d19 = (double)((j + 1 & 2) - 1) * d3;
					double d20 = d18 * d16 - d19 * d15;
					double d21 = d19 * d16 + d18 * d15;
					double d22 = d20 * d12 + d17 * d13;
					double d23 = d17 * d12 - d20 * d13;
					double d24 = d23 * d9 - d21 * d10;
					double d25 = d21 * d9 + d23 * d10;
					tessellator.addVertex(d5 + d24, d6 + d22, d7 + d25);
				}
			}
		}

		tessellator.draw();
	}

	public void changeWorld(World world) {
		if(this.worldObj != null) {
			this.worldObj.removeWorldAccess(this);
		}

		this.prevSortX = -9999.0D;
		this.prevSortY = -9999.0D;
		this.prevSortZ = -9999.0D;
		RenderManager.instance.set(world);
		this.worldObj = world;
		this.globalRenderBlocks = new RenderBlocks(world);
		if(world != null) {
			world.addWorldAccess(this);
			this.loadRenderers();
		}

	}

	public void loadRenderers() {
		if(this.worldObj != null) {
			Block.leaves.setGraphicsLevel(GameSettingsValues.fancyGraphics);
			this.renderDistance = GameSettingsValues.renderDistance;
			int numBlocks;
		if(this.worldRenderers != null) {
				for(numBlocks = 0; numBlocks < this.worldRenderers.length; ++numBlocks) {
					this.worldRenderers[numBlocks].stopRendering();
			}
		}

			numBlocks = 64 << 3 - this.renderDistance;
			if(numBlocks > 400) {
				numBlocks = 400;
		}

			this.prevReposX = -9999.0D;
			this.prevReposY = -9999.0D;
			this.prevReposZ = -9999.0D;
			this.renderChunksWide = numBlocks / 16 + 1;
		// One renderer per 16-tall subchunk; a 256-block world needs all 16 stacked slabs.
		this.renderChunksTall = 16;
			this.renderChunksDeep = numBlocks / 16 + 1;
		this.worldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
		this.sortedWorldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
		int i2 = 0;
		int i3 = 0;
		this.minBlockX = 0;
		this.minBlockY = 0;
		this.minBlockZ = 0;
		this.maxBlockX = this.renderChunksWide;
		this.maxBlockY = this.renderChunksTall;
		this.maxBlockZ = this.renderChunksDeep;

		int i4;
			for(i4 = 0; i4 < this.worldRenderersToUpdate.size(); ++i4) {
				WorldRenderer wr = (WorldRenderer)this.worldRenderersToUpdate.get(i4);
				if(wr != null) {
					wr.needsUpdate = false;
				}
		}

			this.worldRenderersToUpdate.clear();
		this.tileEntities.clear();

		for(i4 = 0; i4 < this.renderChunksWide; ++i4) {
			for(int i5 = 0; i5 < this.renderChunksTall; ++i5) {
				for(int i6 = 0; i6 < this.renderChunksDeep; ++i6) {
						int wri = (i6 * this.renderChunksTall + i5) * this.renderChunksWide + i4;
						this.worldRenderers[wri] = new WorldRenderer(this.worldObj, this.tileEntities, i4 * 16, i5 * 16, i6 * 16, this.glRenderListBase + i2);
					if(this.occlusionEnabled) {
							this.worldRenderers[wri].glOcclusionQuery = this.glOcclusionQueryBase.get(i3);
					}

						this.worldRenderers[wri].isWaitingOnOcclusionQuery = false;
						this.worldRenderers[wri].isVisible = true;
						this.worldRenderers[wri].isInFrustum = false;
						this.worldRenderers[wri].chunkIndex = i3++;
						this.worldRenderers[wri].markDirty();
						this.sortedWorldRenderers[wri] = this.worldRenderers[wri];
						this.worldRenderersToUpdate.add(this.worldRenderers[wri]);
					i2 += 3;
				}
			}
		}

		if(this.worldObj != null) {
			EntityLiving entityLiving7 = this.mc.renderViewEntity;
				if (entityLiving7 == null) {
					entityLiving7 = this.mc.thePlayer;
				}

			if(entityLiving7 != null) {
				this.markRenderersForNewPosition(MathHelper.floor_double(entityLiving7.posX), MathHelper.floor_double(entityLiving7.posY), MathHelper.floor_double(entityLiving7.posZ));
				Arrays.sort(this.sortedWorldRenderers, new EntitySorter(entityLiving7));
			}
		}

		this.renderEntitiesStartupCounter = 2;
	}
	}

	public void renderEntities(Vec3D vec3d, ICamera icamera, float f) {
		if(this.renderEntitiesStartupCounter > 0) {
			--this.renderEntitiesStartupCounter;
		} else {
			TileEntityRenderer.instance.cacheActiveRenderInfo(this.worldObj, this.renderEngine, this.mc.fontRenderer, this.mc.renderViewEntity, f);
			RenderManager.instance.cacheActiveRenderInfo(this.worldObj, this.renderEngine, this.mc.fontRenderer, this.mc.renderViewEntity, this.mc.gameSettings, f);
			this.countEntitiesTotal = 0;
			this.countEntitiesRendered = 0;
			this.countEntitiesHidden = 0;
			EntityLiving entityliving = this.mc.renderViewEntity;
			RenderManager.renderPosX = entityliving.lastTickPosX + (entityliving.posX - entityliving.lastTickPosX) * (double)f;
			RenderManager.renderPosY = entityliving.lastTickPosY + (entityliving.posY - entityliving.lastTickPosY) * (double)f;
			RenderManager.renderPosZ = entityliving.lastTickPosZ + (entityliving.posZ - entityliving.lastTickPosZ) * (double)f;
			TileEntityRenderer.staticPlayerX = entityliving.lastTickPosX + (entityliving.posX - entityliving.lastTickPosX) * (double)f;
			TileEntityRenderer.staticPlayerY = entityliving.lastTickPosY + (entityliving.posY - entityliving.lastTickPosY) * (double)f;
			TileEntityRenderer.staticPlayerZ = entityliving.lastTickPosZ + (entityliving.posZ - entityliving.lastTickPosZ) * (double)f;
			this.mc.entityRenderer.enableLightmap((double)f);
			List<Entity> list5 = this.worldObj.getLoadedEntityList();
			this.countEntitiesTotal = list5.size();

			int k;
			Entity entity1;
			for(k = 0; k < this.worldObj.weatherEffects.size(); ++k) {
				entity1 = (Entity)this.worldObj.weatherEffects.get(k);
				++this.countEntitiesRendered;
				if(entity1.isInRangeToRenderVec3D(vec3d)) {
					RenderManager.instance.renderEntity(entity1, f);
				}
			}

			// PERRO

			for(k = 0; k < list5.size(); ++k) {
				entity1 = (Entity)list5.get(k);
				if(entity1.isInRangeToRenderVec3D(vec3d) && (entity1.ignoreFrustumCheck || icamera.isBoundingBoxInFrustum(entity1.boundingBox)) && (entity1 != this.mc.renderViewEntity || GameSettingsValues.thirdPersonView || this.mc.renderViewEntity.isPlayerSleeping()) && this.worldObj.blockExists(MathHelper.floor_double(entity1.posX), 0, MathHelper.floor_double(entity1.posZ))) {
						++this.countEntitiesRendered;
					RenderManager.instance.renderEntity(entity1, f);
				}
			}

			RenderHelper.enableStandardItemLighting();

			for(k = 0; k < this.tileEntities.size(); ++k) {
				TileEntityRenderer.instance.renderTileEntity((TileEntity)this.tileEntities.get(k), f);
			}

			this.mc.entityRenderer.disableLightmap((double)f);
		}
	}

	public String getDebugInfoRenders() {
		return "C: " + this.renderersBeingRendered + "/" + this.renderersLoaded + ". F: " + this.renderersBeingClipped + ", O: " + this.renderersBeingOccluded + ", E: " + this.renderersSkippingRenderPass;
	}

	public String getDebugInfoEntities() {
		return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ". B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered);
	}

	private void markRenderersForNewPosition(int x, int y, int z) {
		x -= 8;
		y -= 8;
		z -= 8;
		this.minBlockX = Integer.MAX_VALUE;
		this.minBlockY = Integer.MAX_VALUE;
		this.minBlockZ = Integer.MAX_VALUE;
		this.maxBlockX = Integer.MIN_VALUE;
		this.maxBlockY = Integer.MIN_VALUE;
		this.maxBlockZ = Integer.MIN_VALUE;
		int blocksWide = this.renderChunksWide * 16;
		int blocksWide2 = blocksWide / 2;

		for(int ix = 0; ix < this.renderChunksWide; ++ix) {
			int blockX = ix * 16;
			int blockXAbs = blockX + blocksWide2 - x;
			if(blockXAbs < 0) {
				blockXAbs -= blocksWide - 1;
			}

			blockXAbs /= blocksWide;
			blockX -= blockXAbs * blocksWide;
			if(blockX < this.minBlockX) {
				this.minBlockX = blockX;
			}

			if(blockX > this.maxBlockX) {
				this.maxBlockX = blockX;
			}

			for(int iz = 0; iz < this.renderChunksDeep; ++iz) {
				int blockZ = iz * 16;
				int blockZAbs = blockZ + blocksWide2 - z;
				if(blockZAbs < 0) {
					blockZAbs -= blocksWide - 1;
				}

				blockZAbs /= blocksWide;
				blockZ -= blockZAbs * blocksWide;
				if(blockZ < this.minBlockZ) {
					this.minBlockZ = blockZ;
				}

				if(blockZ > this.maxBlockZ) {
					this.maxBlockZ = blockZ;
				}

				for(int iy = 0; iy < this.renderChunksTall; ++iy) {
					int blockY = iy * 16;
					if(blockY < this.minBlockY) {
						this.minBlockY = blockY;
					}

					if(blockY > this.maxBlockY) {
						this.maxBlockY = blockY;
					}

					WorldRenderer worldrenderer = this.worldRenderers[(iz * this.renderChunksTall + iy) * this.renderChunksWide + ix];
					boolean wasNeedingUpdate = worldrenderer.needsUpdate;
					worldrenderer.setPosition(blockX, blockY, blockZ);
					if(!wasNeedingUpdate && worldrenderer.needsUpdate) {
						this.worldRenderersToUpdate.add(worldrenderer);
					}
				}
			}
		}

	}

	public int sortAndRender(EntityLiving player, int renderPass, double partialTicks) {
		if(this.worldRenderersToUpdate.size() < 10) {
		for(int i5 = 0; i5 < 10; ++i5) {
			this.worldRenderersCheckIndex = (this.worldRenderersCheckIndex + 1) % this.worldRenderers.length;
			WorldRenderer worldRenderer6 = this.worldRenderers[this.worldRenderersCheckIndex];
				if(worldRenderer6.needsUpdate && !this.worldRenderersToUpdate.contains(worldRenderer6)) {
					this.worldRenderersToUpdate.add(worldRenderer6);
				}
			}
		}

		if(GameSettingsValues.renderDistance != this.renderDistance) {
			this.loadRenderers();
		}

		if(renderPass == 0) {
			this.renderersLoaded = 0;
			this.renderersBeingClipped = 0;
			this.renderersBeingOccluded = 0;
			this.renderersBeingRendered = 0;
			this.renderersSkippingRenderPass = 0;
		}

		double d39 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double d40 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double partialZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		double dSortX = player.posX - this.prevSortX;
		double dSortY = player.posY - this.prevSortY;
		double dSortZ = player.posZ - this.prevSortZ;
		double distSqSort = dSortX * dSortX + dSortY * dSortY + dSortZ * dSortZ;
		int num;
		if(distSqSort > 16.0D) {
			this.prevSortX = player.posX;
			this.prevSortY = player.posY;
			this.prevSortZ = player.posZ;
			num = 0;
			double ocReq = player.posX - this.prevReposX;
			double lastIndex = player.posY - this.prevReposY;
			double stepNum = player.posZ - this.prevReposZ;
			double switchStep = ocReq * ocReq + lastIndex * lastIndex + stepNum * stepNum;
			if(switchStep > (double)(num * num) + 16.0D) {
				this.prevReposX = player.posX;
				this.prevReposY = player.posY;
				this.prevReposZ = player.posZ;
				this.markRenderersForNewPosition(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
			}

			Arrays.sort(this.sortedWorldRenderers, new EntitySorter(player));
			int sumTX = (int)player.posX;
			int sumTY = (int)player.posZ;
			short sumTZ = 2000;
			if(Math.abs(sumTX - WorldRenderer.globalChunkOffsetX) > sumTZ || Math.abs(sumTY - WorldRenderer.globalChunkOffsetZ) > sumTZ) {
				WorldRenderer.globalChunkOffsetX = sumTX;
				WorldRenderer.globalChunkOffsetZ = sumTY;
				this.loadRenderers();
			}
		}

		RenderHelper.disableStandardItemLighting();
		
		if(GameSettingsValues.ofSmoothFps && renderPass == 0) {
			GL11.glFinish();
		}

		byte b41 = 0;
		if(this.occlusionEnabled && GameSettingsValues.advancedOpengl && !GameSettingsValues.anaglyph && renderPass == 0) {
			byte firstIndex = 0;
			byte b43 = 20;
			this.checkOcclusionQueryResult(firstIndex, b43, player.posX, player.posY, player.posZ);

			int endIndex;
			for(endIndex = firstIndex; endIndex < b43; ++endIndex) {
				this.sortedWorldRenderers[endIndex].isVisible = true;
			}

			num = b41 + this.renderSortedRenderers(firstIndex, b43, renderPass, partialTicks);
			endIndex = b43;
			int i44 = 0;
			byte step = 30;

			int startIndex;
			for(int i45 = this.renderChunksWide / 2; endIndex < this.sortedWorldRenderers.length; num += this.renderSortedRenderers(startIndex, endIndex, renderPass, partialTicks)) {
				startIndex = endIndex;
				if(i44 < i45) {
					++i44;
				} else {
					--i44;
				}

				endIndex += i44 * step;
				if(endIndex <= startIndex) {
					endIndex = startIndex + 10;
				}

				if(endIndex > this.sortedWorldRenderers.length) {
					endIndex = this.sortedWorldRenderers.length;
				}

				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glDisable(GL11.GL_FOG);
				GL11.glColorMask(false, false, false, false);
				GL11.glDepthMask(false);
				this.checkOcclusionQueryResult(startIndex, endIndex, player.posX, player.posY, player.posZ);
				GL11.glPushMatrix();
				float sumTX = 0.0F;
				float sumTY = 0.0F;
				float sumTZ = 0.0F;

				for(int k = startIndex; k < endIndex; ++k) {
					WorldRenderer wr = this.sortedWorldRenderers[k];
					if(wr.skipAllRenderPasses()) {
						wr.isInFrustum = false;
					} else if(wr.isUpdating) {
						wr.isVisible = true;
					} else if(wr.isInFrustum) {
						if(wr.isInFrustum && !wr.isWaitingOnOcclusionQuery) {
							float bbX;
							float bbY;
							float bbZ;
							float tX;
							if(wr.isVisibleFromPosition) {
								bbX = Math.abs((float)(wr.visibleFromX - player.posX));
								bbY = Math.abs((float)(wr.visibleFromY - player.posY));
								bbZ = Math.abs((float)(wr.visibleFromZ - player.posZ));
								tX = bbX + bbY + bbZ;
								if((double)tX < 10.0D + (double)k / 1000.0D) {
									wr.isVisible = true;
									continue;
								}

								wr.isVisibleFromPosition = false;
						}

							bbX = (float)((double)wr.posXMinus - d39);
							bbY = (float)((double)wr.posYMinus - d40);
							bbZ = (float)((double)wr.posZMinus - partialZ);
							tX = bbX - sumTX;
							float tY = bbY - sumTY;
							float tZ = bbZ - sumTZ;
							if(tX != 0.0F || tY != 0.0F || tZ != 0.0F) {
								GL11.glTranslatef(tX, tY, tZ);
								sumTX += tX;
								sumTY += tY;
								sumTZ += tZ;
								}

							ARBOcclusionQuery.glBeginQueryARB(GL15.GL_SAMPLES_PASSED, wr.glOcclusionQuery);
							wr.callOcclusionQueryList();
								ARBOcclusionQuery.glEndQueryARB(GL15.GL_SAMPLES_PASSED);
							wr.isWaitingOnOcclusionQuery = true;
						}
					}
				}

				GL11.glPopMatrix();
					GL11.glColorMask(true, true, true, true);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_FOG);
			}
		} else {
			num = b41 + this.renderSortedRenderers(0, this.sortedWorldRenderers.length, renderPass, partialTicks);
		}

		return num;
	}

	private void checkOcclusionQueryResult(int startIndex, int endIndex, double px, double py, double pz) {
		for(int k = startIndex; k < endIndex; ++k) {
			WorldRenderer wr = this.sortedWorldRenderers[k];
			if(wr.isWaitingOnOcclusionQuery) {
				this.occlusionResult.clear();
				ARBOcclusionQuery.glGetQueryObjectuARB(wr.glOcclusionQuery, GL15.GL_QUERY_RESULT_AVAILABLE, this.occlusionResult);
				if(this.occlusionResult.get(0) != 0) {
					wr.isWaitingOnOcclusionQuery = false;
					this.occlusionResult.clear();
					ARBOcclusionQuery.glGetQueryObjectuARB(wr.glOcclusionQuery, GL15.GL_QUERY_RESULT, this.occlusionResult);
					boolean wasVisible = wr.isVisible;
					wr.isVisible = this.occlusionResult.get(0) > 0;
					if(wasVisible && wr.isVisible) {
						wr.isVisibleFromPosition = true;
						wr.visibleFromX = px;
						wr.visibleFromY = py;
						wr.visibleFromZ = pz;
					}
				}
			}
		}

	}

	private int renderSortedRenderers(int startIndex, int endIndex, int renderPass, double partialTicks) {
		this.glListBuffer.clear();
		int l = 0;

		for(int entityliving = startIndex; entityliving < endIndex; ++entityliving) {
			WorldRenderer renderer = this.sortedWorldRenderers[entityliving];
			if(renderPass == 0) {
				++this.renderersLoaded;
				if(renderer.skipRenderPass[renderPass]) {
					++this.renderersSkippingRenderPass;
				} else if(!renderer.isInFrustum) {
					++this.renderersBeingClipped;
				} else if(this.occlusionEnabled && !renderer.isVisible) {
					++this.renderersBeingOccluded;
				} else {
					++this.renderersBeingRendered;
				}
			}

			if(!renderer.skipRenderPass[renderPass] && renderer.isInFrustum && (!this.occlusionEnabled || renderer.isVisible)) {
				int glCallList = renderer.getGLCallListForPass(renderPass);
				if(glCallList >= 0) {
					this.glListBuffer.put(glCallList);
					++l;
				}
		}
		}

		this.glListBuffer.flip();
		EntityLiving entityLiving14 = this.mc.renderViewEntity;
		double d15 = entityLiving14.lastTickPosX + (entityLiving14.posX - entityLiving14.lastTickPosX) * partialTicks - (double)WorldRenderer.globalChunkOffsetX;
		double partialY = entityLiving14.lastTickPosY + (entityLiving14.posY - entityLiving14.lastTickPosY) * partialTicks;
		double partialZ = entityLiving14.lastTickPosZ + (entityLiving14.posZ - entityLiving14.lastTickPosZ) * partialTicks - (double)WorldRenderer.globalChunkOffsetZ;
		this.mc.entityRenderer.enableLightmap(partialTicks);
		GL11.glTranslatef((float)(-d15), (float)(-partialY), (float)(-partialZ));
		GL11.glCallLists(this.glListBuffer);
		GL11.glTranslatef((float)d15, (float)partialY, (float)partialZ);
		this.mc.entityRenderer.disableLightmap(partialTicks);
		return l;
		}

	public void renderAllRenderLists(int renderPass, double partialTicks) {
	}

	public void updateClouds() {
		++this.cloudOffsetX;
	}

	public void renderSky(float f1) {
		if(!this.mc.theWorld.worldProvider.isNether) {
			// ## SKY ##	
		
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Vec3D vec3D2 = this.worldObj.getSkyColor(this.mc.renderViewEntity, f1);
			float f3 = (float)vec3D2.xCoord;
			float f4 = (float)vec3D2.yCoord;
			float f5 = (float)vec3D2.zCoord;
			float f7;
			float f8;
			if(GameSettingsValues.anaglyph) {
				float f6 = (f3 * 30.0F + f4 * 59.0F + f5 * 11.0F) / 100.0F;
				f7 = (f3 * 30.0F + f4 * 70.0F) / 100.0F;
				f8 = (f3 * 30.0F + f5 * 70.0F) / 100.0F;
				f3 = f6;
				f4 = f7;
				f5 = f8;
			}

			GL11.glColor3f(f3, f4, f5);
			Tessellator tessellator17 = Tessellator.instance;
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glColor3f(f3, f4, f5);
			GL11.glCallList(this.glSkyList);

			GL11.glDisable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderHelper.disableStandardItemLighting();

			float f9;
			float f10;
			float f11;
			float f12;

			if(GameSettingsValues.sunriseSunsetColors) {
			float[] f18 = this.worldObj.worldProvider.calcSunriseSunsetColors(this.worldObj.getCelestialAngle(f1), f1);
			if(f18 != null) {
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				GL11.glPushMatrix();
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				f8 = this.worldObj.getCelestialAngle(f1);
				GL11.glRotatef(f8 > 0.5F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				f9 = f18[0];
				f10 = f18[1];
				f11 = f18[2];
				float f14;
					if(GameSettingsValues.anaglyph) {
					f12 = (f9 * 30.0F + f10 * 59.0F + f11 * 11.0F) / 100.0F;
					float f13 = (f9 * 30.0F + f10 * 70.0F) / 100.0F;
					f14 = (f9 * 30.0F + f11 * 70.0F) / 100.0F;
					f9 = f12;
					f10 = f13;
					f11 = f14;
				}

				tessellator17.startDrawing(6);
				tessellator17.setColorRGBA_F(f9, f10, f11, f18[3]);
				tessellator17.addVertex(0.0D, 100.0D, 0.0D);
				byte b19 = 16;
				tessellator17.setColorRGBA_F(f18[0], f18[1], f18[2], 0.0F);

				for(int i20 = 0; i20 <= b19; ++i20) {
					f14 = (float)i20 * (float)Math.PI * 2.0F / (float)b19;
					float f15 = MathHelper.sin(f14);
					float f16 = MathHelper.cos(f14);
					tessellator17.addVertex((double)(f15 * 120.0F), (double)(f16 * 120.0F), (double)(-f16 * 40.0F * f18[3]));
				}

				tessellator17.draw();
				GL11.glPopMatrix();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
			}

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glPushMatrix();
			f7 = 1.0F - this.worldObj.getRainStrength(f1);
			f8 = 0.0F;
			f9 = 0.0F;
			f10 = 0.0F;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, f7);
			GL11.glTranslatef(f8, f9, f10);
			GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(this.worldObj.getCelestialAngle(f1) * 360.0F, 1.0F, 0.0F, 0.0F);
			f11 = 30.0F;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/terrain/sun.png"));
			tessellator17.startDrawingQuads();
			tessellator17.addVertexWithUV((double)(-f11), 100.0D, (double)(-f11), 0.0D, 0.0D);
			tessellator17.addVertexWithUV((double)f11, 100.0D, (double)(-f11), 1.0D, 0.0D);
			tessellator17.addVertexWithUV((double)f11, 100.0D, (double)f11, 1.0D, 1.0D);
			tessellator17.addVertexWithUV((double)(-f11), 100.0D, (double)f11, 0.0D, 1.0D);
			tessellator17.draw();
			f11 = 20.0F;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/terrain/moon.png"));
			if(this.worldObj.worldInfo.isBloodMoon()) {
				GL11.glColor4f(1.0F, 0.0F, 0.0F, 1.0f);
				f11 = 50.0F;
			}
			tessellator17.startDrawingQuads();
			tessellator17.addVertexWithUV((double)(-f11), -100.0D, (double)f11, 1.0D, 1.0D);
			tessellator17.addVertexWithUV((double)f11, -100.0D, (double)f11, 0.0D, 1.0D);
			tessellator17.addVertexWithUV((double)f11, -100.0D, (double)(-f11), 0.0D, 0.0D);
			tessellator17.addVertexWithUV((double)(-f11), -100.0D, (double)(-f11), 1.0D, 0.0D);
			tessellator17.draw();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			f12 = this.worldObj.getStarBrightness(f1) * f7;
			if(f12 > 0.0F) {
				GL11.glColor4f(f12, f12, f12, f12);
				GL11.glCallList(this.starGLCallList);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glPopMatrix();
			if(this.worldObj.worldProvider.func_28112_c()) {
				GL11.glColor3f(f3 * 0.2F + 0.04F, f4 * 0.2F + 0.04F, f5 * 0.6F + 0.1F);
			} else {
				GL11.glColor3f(f3, f4, f5);
			}

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glCallList(this.glSkyList2);
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
		}
	}

	public void renderClouds(float f1) {
		if(!this.mc.theWorld.worldProvider.isNether) {
			if(GameSettingsValues.fancyGraphics) {
				this.renderCloudsFancy(f1);
			} else {
				GL11.glDisable(GL11.GL_CULL_FACE);
				float f2 = (float)(this.mc.renderViewEntity.lastTickPosY + (this.mc.renderViewEntity.posY - this.mc.renderViewEntity.lastTickPosY) * (double)f1);
				byte b3 = 32;
				int i4 = 256 / b3;
				Tessellator tessellator5 = Tessellator.instance;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/environment/clouds.png"));
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				Vec3D vec3D6 = this.worldObj.getCloudColor(f1);
				float f7 = (float)vec3D6.xCoord;
				float f8 = (float)vec3D6.yCoord;
				float f9 = (float)vec3D6.zCoord;
				float f10;
					if(GameSettingsValues.anaglyph) {
					f10 = (f7 * 30.0F + f8 * 59.0F + f9 * 11.0F) / 100.0F;
					float f11 = (f7 * 30.0F + f8 * 70.0F) / 100.0F;
					float f12 = (f7 * 30.0F + f9 * 70.0F) / 100.0F;
					f7 = f10;
					f8 = f11;
					f9 = f12;
				}

				f10 = 4.8828125E-4F;
				double d22 = this.mc.renderViewEntity.prevPosX + (this.mc.renderViewEntity.posX - this.mc.renderViewEntity.prevPosX) * (double)f1 + (double)(((float)this.cloudOffsetX + f1) * 0.03F);
				double d13 = this.mc.renderViewEntity.prevPosZ + (this.mc.renderViewEntity.posZ - this.mc.renderViewEntity.prevPosZ) * (double)f1;
				int i15 = MathHelper.floor_double(d22 / 2048.0D);
				int i16 = MathHelper.floor_double(d13 / 2048.0D);
				d22 -= (double)(i15 * 2048);
				d13 -= (double)(i16 * 2048);
				float f17 = this.worldObj.worldProvider.getCloudHeight() - f2 + 0.33F;
				float f18 = (float)(d22 * (double)f10);
				float f19 = (float)(d13 * (double)f10);
				tessellator5.startDrawingQuads();
				tessellator5.setColorRGBA_F(f7, f8, f9, 0.8F);

				for(int i20 = -b3 * i4; i20 < b3 * i4; i20 += b3) {
					for(int i21 = -b3 * i4; i21 < b3 * i4; i21 += b3) {
						tessellator5.addVertexWithUV((double)(i20 + 0), (double)f17, (double)(i21 + b3), (double)((float)(i20 + 0) * f10 + f18), (double)((float)(i21 + b3) * f10 + f19));
						tessellator5.addVertexWithUV((double)(i20 + b3), (double)f17, (double)(i21 + b3), (double)((float)(i20 + b3) * f10 + f18), (double)((float)(i21 + b3) * f10 + f19));
						tessellator5.addVertexWithUV((double)(i20 + b3), (double)f17, (double)(i21 + 0), (double)((float)(i20 + b3) * f10 + f18), (double)((float)(i21 + 0) * f10 + f19));
						tessellator5.addVertexWithUV((double)(i20 + 0), (double)f17, (double)(i21 + 0), (double)((float)(i20 + 0) * f10 + f18), (double)((float)(i21 + 0) * f10 + f19));
					}
				}

				tessellator5.draw();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
		}
	}

	public boolean clipRenderersByFrustrum(double d1, double d3, double d5, float f7) {
		return false;
	}

	public void renderCloudsFancy(float f) {
		GL11.glDisable(GL11.GL_CULL_FACE);
		float f1 = (float)(this.mc.renderViewEntity.lastTickPosY + (this.mc.renderViewEntity.posY - this.mc.renderViewEntity.lastTickPosY) * (double)f);
		Tessellator tessellator = Tessellator.instance;
		float f2 = 12.0F;
		float f3 = 4.0F;
		
		double d = (this.mc.renderViewEntity.prevPosX + (this.mc.renderViewEntity.posX - this.mc.renderViewEntity.prevPosX) * (double)f + (double)(((float)this.cloudOffsetX + f) * 0.03F)) / (double)f2;
		double d1 = (this.mc.renderViewEntity.prevPosZ + (this.mc.renderViewEntity.posZ - this.mc.renderViewEntity.prevPosZ) * (double)f) / (double)f2 + (double)0.33F;
		float f4 = this.worldObj.worldProvider.getCloudHeight() - f1 + 0.33F;
		int i = MathHelper.floor_double(d / 2048.0D);
		int j = MathHelper.floor_double(d1 / 2048.0D);
		d -= (double)(i * 2048);
		d1 -= (double)(j * 2048);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/environment/clouds.png"));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Vec3D vec3d = this.worldObj.getCloudColor(f1);
		float f5 = (float)vec3d.xCoord;
		float f6 = (float)vec3d.yCoord;
		float f7 = (float)vec3d.zCoord;
		float f9;
		float f11;
		float f13;
		if(GameSettingsValues.anaglyph) {
			f9 = (f5 * 30.0F + f6 * 59.0F + f7 * 11.0F) / 100.0F;
			f11 = (f5 * 30.0F + f6 * 70.0F) / 100.0F;
			f13 = (f5 * 30.0F + f7 * 70.0F) / 100.0F;
			f5 = f9;
			f6 = f11;
			f7 = f13;
		}

		f9 = (float)(d * 0.0D);
		f11 = (float)(d1 * 0.0D);
		f13 = 0.00390625F;
		f9 = (float)MathHelper.floor_double(d) * f13;
		f11 = (float)MathHelper.floor_double(d1) * f13;
		float f14 = (float)(d - (double)MathHelper.floor_double(d));
		float f15 = (float)(d1 - (double)MathHelper.floor_double(d1));
		byte k = 8;
		byte byte0 = 3;
		float f16 = 9.765625E-4F;
		GL11.glScalef(f2, 1.0F, f2);

		for(int l = 0; l < 2; ++l) {
			if(l == 0) {
				GL11.glColorMask(false, false, false, false);
			} else if(GameSettingsValues.anaglyph) {
				if(EntityRenderer.anaglyphField == 0) {
					GL11.glColorMask(false, true, true, true);
				} else {
					GL11.glColorMask(true, false, false, true);
				}
			} else {
				GL11.glColorMask(true, true, true, true);
			}

			double dd = 0.02D;

			for(int i1 = -byte0 + 1; i1 <= byte0; ++i1) {
				for(int j1 = -byte0 + 1; j1 <= byte0; ++j1) {
					tessellator.startDrawingQuads();
					float f17 = (float)(i1 * k);
					float f18 = (float)(j1 * k);
					float f19 = f17 - f14;
					float f20 = f18 - f15;
					tessellator.setColorRGBA_F(f5 * 0.9F, f6 * 0.9F, f7 * 0.9F, 0.8F);
					int j2;
					if(i1 > -1) {
						tessellator.setNormal(-1.0F, 0.0F, 0.0F);

						for(j2 = 0; j2 < k; ++j2) {
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 0.0F), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)k), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 0.0F), (double)(f4 + f3) - dd, (double)(f20 + (float)k), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 0.0F), (double)(f4 + f3) - dd, (double)(f20 + 0.0F), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 0.0F), (double)(f4 + 0.0F) + dd, (double)(f20 + 0.0F), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
					}
					}

					if(i1 <= 1) {
						tessellator.setNormal(1.0F, 0.0F, 0.0F);

						for(j2 = 0; j2 < k; ++j2) {
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 1.0F - f16), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)k), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 1.0F - f16), (double)(f4 + f3) - dd, (double)(f20 + (float)k), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 1.0F - f16), (double)(f4 + f3) - dd, (double)(f20 + 0.0F), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)j2 + 1.0F - f16), (double)(f4 + 0.0F) + dd, (double)(f20 + 0.0F), (double)((f17 + (float)j2 + 0.5F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
						}
					}

					tessellator.setColorRGBA_F(f5 * 0.8F, f6 * 0.8F, f7 * 0.8F, 0.8F);
					if(j1 > -1) {
						tessellator.setNormal(0.0F, 0.0F, -1.0F);

						for(j2 = 0; j2 < k; ++j2) {
							tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + f3) - dd, (double)(f20 + (float)j2 + 0.0F), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + f3) - dd, (double)(f20 + (float)j2 + 0.0F), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)j2 + 0.0F), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)j2 + 0.0F), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
						}
					}

					if(j1 <= 1) {
						tessellator.setNormal(0.0F, 0.0F, 1.0F);

						for(j2 = 0; j2 < k; ++j2) {
							tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + f3) - dd, (double)(f20 + (float)j2 + 1.0F - f16), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + f3) - dd, (double)(f20 + (float)j2 + 1.0F - f16), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)j2 + 1.0F - f16), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
							tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + 0.0F) + dd, (double)(f20 + (float)j2 + 1.0F - f16), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)j2 + 0.5F) * f13 + f11));
						}
					}

					if(f4 > -f3 - 1.0F) {
						tessellator.setColorRGBA_F(f5 * 0.7F, f6 * 0.7F, f7 * 0.7F, 0.8F);
						tessellator.setNormal(0.0F, -1.0F, 0.0F);
						tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + 0.0F), (double)(f20 + (float)k), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + 0.0F), (double)(f20 + (float)k), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + 0.0F), (double)(f20 + 0.0F), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + 0.0F), (double)(f20 + 0.0F), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
					}

					if(f4 <= f3 + 1.0F) {
						tessellator.setColorRGBA_F(f5, f6, f7, 0.8F);
						tessellator.setNormal(0.0F, 1.0F, 0.0F);
						tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + f3 - f16), (double)(f20 + (float)k), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + f3 - f16), (double)(f20 + (float)k), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + (float)k) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + (float)k), (double)(f4 + f3 - f16), (double)(f20 + 0.0F), (double)((f17 + (float)k) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
						tessellator.addVertexWithUV((double)(f19 + 0.0F), (double)(f4 + f3 - f16), (double)(f20 + 0.0F), (double)((f17 + 0.0F) * f13 + f9), (double)((f18 + 0.0F) * f13 + f11));
					}

					tessellator.draw();
				}
			}
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public boolean updateRenderers(EntityLiving entityliving, boolean flag) {
		if(this.worldRenderersToUpdate.size() <= 0) {
			return false;
		} else {
			int num = 0;
			int maxNum = GameSettingsValues.ofChunkUpdates;
			if(GameSettingsValues.ofChunkUpdatesDynamic && !this.isMoving(entityliving)) {
				maxNum *= 3;
			}

			byte NOT_IN_FRUSTRUM_MUL = 4;
			int numValid = 0;
			WorldRenderer wrBest = null;
			float distSqBest = Float.MAX_VALUE;
			int indexBest = -1;

			int dstIndex;
			for(dstIndex = 0; dstIndex < this.worldRenderersToUpdate.size(); ++dstIndex) {
				WorldRenderer i = (WorldRenderer)this.worldRenderersToUpdate.get(dstIndex);
				if(i != null) {
					++numValid;
					if(!i.needsUpdate) {
						this.worldRenderersToUpdate.set(dstIndex, (WorldRenderer)null);
					} else {
						float wr = i.distanceToEntitySquared(entityliving);
						if(wr <= 256.0F && this.isActingNow()) {
							i.updateRenderer();
							i.needsUpdate = false;
							this.worldRenderersToUpdate.set(dstIndex, (WorldRenderer)null);
							++num;
						} else {
							if(wr > 256.0F && num >= maxNum) {
								break;
							}
	
							if(!i.isInFrustum) {
								wr *= (float)NOT_IN_FRUSTRUM_MUL;
							}
	
							if(wrBest == null) {
								wrBest = i;
								distSqBest = wr;
								indexBest = dstIndex;
							} else if(wr < distSqBest) {
								wrBest = i;
								distSqBest = wr;
								indexBest = dstIndex;
								}
							}
						}
					}
				}
			
			int i16;
			if(wrBest != null) {
				wrBest.updateRenderer();
				wrBest.needsUpdate = false;
				this.worldRenderersToUpdate.set(indexBest, (WorldRenderer)null);
				++num;
				float f15 = distSqBest / 5.0F;

				for(i16 = 0; i16 < this.worldRenderersToUpdate.size() && num < maxNum; ++i16) {
					WorldRenderer worldRenderer17 = (WorldRenderer)this.worldRenderersToUpdate.get(i16);
					if(worldRenderer17 != null) {
						float distSq = worldRenderer17.distanceToEntitySquared(entityliving);
						if(!worldRenderer17.isInFrustum) {
							distSq *= (float)NOT_IN_FRUSTRUM_MUL;
				}

						float diffDistSq = Math.abs(distSq - distSqBest);
						if(diffDistSq < f15) {
							worldRenderer17.updateRenderer();
							worldRenderer17.needsUpdate = false;
							this.worldRenderersToUpdate.set(i16, (WorldRenderer)null);
							++num;
					}
				}
			}
					}

			if(numValid == 0) {
				this.worldRenderersToUpdate.clear();
			}

			if(this.worldRenderersToUpdate.size() > 100 && numValid < this.worldRenderersToUpdate.size() * 4 / 5) {
				dstIndex = 0;

				for(i16 = 0; i16 < this.worldRenderersToUpdate.size(); ++i16) {
					WorldRenderer object18 = this.worldRenderersToUpdate.get(i16);
					if(object18 != null) {
						if(i16 != dstIndex)  {
							this.worldRenderersToUpdate.set(dstIndex, object18);
						}
						++dstIndex;
					}
						}

				for(i16 = this.worldRenderersToUpdate.size() - 1; i16 >= dstIndex; --i16) {
					this.worldRenderersToUpdate.remove(i16);
				}
			}

			return true;
		}
	}

	public void drawBlockBreaking(EntityPlayer entityPlayer1, MovingObjectPosition movingObjectPosition2, int i3, ItemStack itemStack4, float f5) {
		Tessellator tessellator6 = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin((float)System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
		int i8;
		if(i3 == 0) {
			if(this.damagePartialTime > 0.0F) {
				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
				int i7 = this.renderEngine.getTexture("/terrain.png");
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, i7);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
				GL11.glPushMatrix();
				i8 = this.worldObj.getBlockId(movingObjectPosition2.blockX, movingObjectPosition2.blockY, movingObjectPosition2.blockZ);
				Block block9 = i8 > 0 ? Block.blocksList[i8] : null;
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glPolygonOffset(-3.0F, -3.0F);
				GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
				double d10 = entityPlayer1.lastTickPosX + (entityPlayer1.posX - entityPlayer1.lastTickPosX) * (double)f5;
				double d12 = entityPlayer1.lastTickPosY + (entityPlayer1.posY - entityPlayer1.lastTickPosY) * (double)f5;
				double d14 = entityPlayer1.lastTickPosZ + (entityPlayer1.posZ - entityPlayer1.lastTickPosZ) * (double)f5;
				if(block9 == null) {
					block9 = Block.stone;
				}

				GL11.glEnable(GL11.GL_ALPHA_TEST);
				tessellator6.startDrawingQuads();
				tessellator6.setTranslation(-d10, -d12, -d14);
				tessellator6.disableColor();
				this.globalRenderBlocks.renderBlockUsingTexture(block9, movingObjectPosition2.blockX, movingObjectPosition2.blockY, movingObjectPosition2.blockZ, 240 + (int)(this.damagePartialTime * 10.0F));
				tessellator6.draw();
				tessellator6.setTranslation(0.0D, 0.0D, 0.0D);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glPolygonOffset(0.0F, 0.0F);
				GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glDepthMask(true);
				GL11.glPopMatrix();
			}
		} else if(itemStack4 != null) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float f16 = MathHelper.sin((float)System.currentTimeMillis() / 100.0F) * 0.2F + 0.8F;
			GL11.glColor4f(f16, f16, f16, MathHelper.sin((float)System.currentTimeMillis() / 200.0F) * 0.2F + 0.5F);
			i8 = this.renderEngine.getTexture("/terrain.png");
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, i8);
			if(movingObjectPosition2.sideHit == 0) {
			}

			if(movingObjectPosition2.sideHit == 1) {
			}

			if(movingObjectPosition2.sideHit == 2) {
			}

			if(movingObjectPosition2.sideHit == 3) {
			}

			if(movingObjectPosition2.sideHit == 4) {
			}

			if(movingObjectPosition2.sideHit == 5) {
			}
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}

	public void drawSelectionBox(EntityPlayer entityPlayer1, MovingObjectPosition movingObjectPosition2, int i3, ItemStack itemStack4, float f5) {
		if(i3 == 0 && movingObjectPosition2.typeOfHit == EnumMovingObjectType.TILE) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
			GL11.glLineWidth(2.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(false);
			float f6 = 0.002F;
			int i7 = this.worldObj.getBlockId(movingObjectPosition2.blockX, movingObjectPosition2.blockY, movingObjectPosition2.blockZ);
			if(i7 > 0) {
				Block.blocksList[i7].setBlockBoundsBasedOnState(this.worldObj, movingObjectPosition2.blockX, movingObjectPosition2.blockY, movingObjectPosition2.blockZ);
				double d8 = entityPlayer1.lastTickPosX + (entityPlayer1.posX - entityPlayer1.lastTickPosX) * (double)f5;
				double d10 = entityPlayer1.lastTickPosY + (entityPlayer1.posY - entityPlayer1.lastTickPosY) * (double)f5;
				double d12 = entityPlayer1.lastTickPosZ + (entityPlayer1.posZ - entityPlayer1.lastTickPosZ) * (double)f5;
				this.drawOutlinedBoundingBox(Block.blocksList[i7].getSelectedBoundingBoxFromPool(this.worldObj, movingObjectPosition2.blockX, movingObjectPosition2.blockY, movingObjectPosition2.blockZ).expand((double)f6, (double)f6, (double)f6).getOffsetBoundingBox(-d8, -d10, -d12));
			}

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
		}

	}

	private void drawOutlinedBoundingBox(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(3);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.draw();
		tessellator.startDrawing(3);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		tessellator.draw();
		tessellator.startDrawing(1);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		tessellator.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		tessellator.draw();
	}

	public void markBlocksForUpdate(int par1, int par2, int par3, int par4, int par5, int par6) {
		int i = MathHelper.bucketInt(par1, 16);
		int j = MathHelper.bucketInt(par2, 16);
		int k = MathHelper.bucketInt(par3, 16);
		int l = MathHelper.bucketInt(par4, 16);
		int i1 = MathHelper.bucketInt(par5, 16);
		int j1 = MathHelper.bucketInt(par6, 16);

		for(int k1 = i; k1 <= l; ++k1) {
			int l1 = k1 % this.renderChunksWide;
			if(l1 < 0) {
				l1 += this.renderChunksWide;
			}

			for(int i2 = j; i2 <= i1; ++i2) {
				// The Y loop index is already a 16-block section number; clamp it into the
				// renderer column instead of wrapping via % so updates at y >= 128 invalidate
				// the correct upper renderer rather than wrapping to the bottom one.
				int j2 = i2;
				if(j2 < 0) {
					j2 = 0;
				} else if(j2 >= this.renderChunksTall) {
					j2 = this.renderChunksTall - 1;
				}

				for(int k2 = k; k2 <= j1; ++k2) {
					int l2 = k2 % this.renderChunksDeep;
					if(l2 < 0) {
						l2 += this.renderChunksDeep;
					}

					int i3 = (l2 * this.renderChunksTall + j2) * this.renderChunksWide + l1;
					WorldRenderer worldrenderer = this.worldRenderers[i3];
					if(!worldrenderer.needsUpdate) {
						this.worldRenderersToUpdate.add(worldrenderer);
						worldrenderer.markDirty();
					}
				}
			}
		}

	}

	public void markBlockNeedsUpdate(int i1, int i2, int i3) {
		this.markBlocksForUpdate(i1 - 1, i2 - 1, i3 - 1, i1 + 1, i2 + 1, i3 + 1);
	}

	public void markBlockRangeNeedsUpdate(int i1, int i2, int i3, int i4, int i5, int i6) {
		this.markBlocksForUpdate(i1 - 1, i2 - 1, i3 - 1, i4 + 1, i5 + 1, i6 + 1);
	}

	public void clipRenderersByFrustum(ICamera par1ICamera, float par2) {
		for(int i = 0; i < this.worldRenderers.length; ++i) {
			if(!this.worldRenderers[i].skipAllRenderPasses()) {
				this.worldRenderers[i].updateInFrustum(par1ICamera);
			}
		}

		++this.frustumCheckOffset;
	}

	public void playRecord(String string1, int i2, int i3, int i4) {
		if(string1 != null) {
			this.mc.ingameGUI.setRecordPlayingMessage("C418 - " + string1);
		}

		this.mc.sndManager.playStreaming(string1, (float)i2, (float)i3, (float)i4, 1.0F, 1.0F);
	}

	public void playSound(String string1, double d2, double d4, double d6, float f8, float f9) {
		float f10 = 16.0F;
		if(f8 > 1.0F) {
			f10 *= f8;
		}

		if(this.mc.renderViewEntity.getDistanceSq(d2, d4, d6) < (double)(f10 * f10)) {
			this.mc.sndManager.playSound(string1, (float)d2, (float)d4, (float)d6, f8, f9);
		}
	}
	
	public void showString(String s) {
		if (s != null) {
			this.mc.ingameGUI.showString(s);
		}
	}

	public void showChatMessage(String s) {
		if (s != null) {
			this.mc.ingameGUI.addChatMessage(s);
		}
	}

	public void spawnParticle(String particle, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
		if(this.mc != null && this.mc.renderViewEntity != null && this.mc.effectRenderer != null) {
			double dX = this.mc.renderViewEntity.posX - posX;
			double dY = this.mc.renderViewEntity.posY - posY;
			double dZ = this.mc.renderViewEntity.posZ - posZ;
			double dist = 16.0D;
			if(dX * dX + dY * dY + dZ * dZ <= dist * dist) {
				if(particle.equals("bubble")) {
					this.mc.effectRenderer.addEffect(new EntityBubbleFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("smoke")) {
					this.mc.effectRenderer.addEffect(new EntitySmokeFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("note")) {
					this.mc.effectRenderer.addEffect(new EntityNoteFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("portal")) {
					this.mc.effectRenderer.addEffect(new EntityPortalFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("explode")) {
					this.mc.effectRenderer.addEffect(new EntityExplodeFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("flame")) {
					this.mc.effectRenderer.addEffect(new EntityFlameFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("lava")) {
					this.mc.effectRenderer.addEffect(new EntityLavaFX(this.worldObj, posX, posY, posZ));
				} else if(particle.equals("footstep")) {
					this.mc.effectRenderer.addEffect(new EntityFootStepFX(this.renderEngine, this.worldObj, posX, posY, posZ));
				} else if(particle.equals("splash")) {
					this.mc.effectRenderer.addEffect(new EntitySplashFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("largesmoke")) {
					this.mc.effectRenderer.addEffect(new EntitySmokeFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ, 2.5F));
				} else if(particle.equals("reddust")) {
					this.mc.effectRenderer.addEffect(new EntityReddustFX(this.worldObj, posX, posY, posZ, (float)motionX, (float)motionY, (float)motionZ));
				} else if(particle.equals("glowdust")) {
					this.mc.effectRenderer.addEffect(new EntityGlowdustFX(this.worldObj, posX, posY, posZ));
				} else if(particle.equals("snowballpoof")) {
					this.mc.effectRenderer.addEffect(new EntitySlimeFX(this.worldObj, posX, posY, posZ, Item.snowball));
				} else if(particle.equals("crackedpebble")) {
					this.mc.effectRenderer.addEffect(new EntitySlimeFX(this.worldObj, posX, posY, posZ, Item.pebble));
				} else if(particle.equals("crackedpotion")) {
					this.mc.effectRenderer.addEffect(new EntitySlimeFX(this.worldObj, posX, posY, posZ, Item.potionEmpty));
				} else if(particle.equals("snowshovel")) {
					this.mc.effectRenderer.addEffect(new EntitySnowShovelFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if(particle.equals("slime")) {
					this.mc.effectRenderer.addEffect(new EntitySlimeFX(this.worldObj, posX, posY, posZ, Item.slimeBall));
				} else if(particle.equals("heart")) {
					this.mc.effectRenderer.addEffect(new EntityHeartFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ));
				} else if (particle.equals("status_effect")) {
					EntityStatusEffectFX entityFX = new EntityStatusEffectFX(this.worldObj, posX, posY, posZ, 0, 0, 0);
					entityFX.setParticleColor((float)motionX, (float)motionY, (float)motionZ);
					this.mc.effectRenderer.addEffect(entityFX);
				} else {
					int i24;
					if(particle.startsWith("iconcrack_")) {
						i24 = Integer.parseInt(particle.substring(particle.indexOf("_") + 1));
						this.mc.effectRenderer.addEffect(new EntityBreakingFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ, Item.itemsList[i24]));
					} else if(particle.startsWith("tilecrack_")) {
						i24 = Integer.parseInt(particle.substring(particle.indexOf("_") + 1));
						this.mc.effectRenderer.addEffect(new EntityDiggingFX(this.worldObj, posX, posY, posZ, motionX, motionY, motionZ, Block.blocksList[i24], 0, 0));
					}
				}

			}
		}
	}

	public void obtainEntitySkin(Entity entity1) {
		entity1.updateClNORMAL();
		if(entity1.skinUrl != null) {
			this.renderEngine.obtainImageData(entity1.skinUrl, new ImageBufferDownload());
		}

		if(entity1.clNORMALUrl != null) {
			this.renderEngine.obtainImageData(entity1.clNORMALUrl, new ImageBufferDownload());
		}

	}

	public void releaseEntitySkin(Entity entity1) {
		if(entity1.skinUrl != null) {
			this.renderEngine.releaseImageData(entity1.skinUrl);
		}

		if(entity1.clNORMALUrl != null) {
			this.renderEngine.releaseImageData(entity1.clNORMALUrl);
		}

	}

	public void updateAllRenderers() {
		if(this.worldRenderers != null) {
			for(int i = 0; i < this.worldRenderers.length; ++i) {
				if(this.worldRenderers[i].isChunkLit && !this.worldRenderers[i].needsUpdate) {
					this.worldRenderersToUpdate.add(this.worldRenderers[i]);
					this.worldRenderers[i].markDirty();
				}
			}

		}
	}

	public void setAllRenderesVisible() {
		if(this.worldRenderers != null) {
			for(int i = 0; i < this.worldRenderers.length; ++i) {
				this.worldRenderers[i].isVisible = true;
		}

	}
	}

	public void doNothingWithTileEntity(int i1, int i2, int i3, TileEntity tileEntity4) {
	}

	public void deleteDisplayLists() {
		GLAllocation.deleteDisplayLists(this.glRenderListBase);
	}

	public void playAuxSFX(EntityPlayer entityPlayer1, int i2, int i3, int i4, int i5, int i6) {
		Random random7 = this.worldObj.rand;
		int i16;
		switch(i2) {
		case 1000:
			this.worldObj.playSoundEffect((double)i3, (double)i4, (double)i5, "random.click", 1.0F, 1.0F);
			break;
		case 1001:
			this.worldObj.playSoundEffect((double)i3, (double)i4, (double)i5, "random.click", 1.0F, 1.2F);
			break;
		case 1002:
			this.worldObj.playSoundEffect((double)i3, (double)i4, (double)i5, "random.bow", 1.0F, 1.2F);
			break;
		case 1003:
			if(Math.random() < 0.5D) {
				this.worldObj.playSoundEffect((double)i3 + 0.5D, (double)i4 + 0.5D, (double)i5 + 0.5D, "random.door_open", 1.0F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			} else {
				this.worldObj.playSoundEffect((double)i3 + 0.5D, (double)i4 + 0.5D, (double)i5 + 0.5D, "random.door_close", 1.0F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}
			break;
		case 1004:
			this.worldObj.playSoundEffect((double)((float)i3 + 0.5F), (double)((float)i4 + 0.5F), (double)((float)i5 + 0.5F), "random.fizz", 0.5F, 2.6F + (random7.nextFloat() - random7.nextFloat()) * 0.8F);
			break;
		case 1005:
			if(Item.itemsList[i6] instanceof ItemRecord) {
				this.worldObj.playRecord(((ItemRecord)Item.itemsList[i6]).recordName, i3, i4, i5);
			} else {
				this.worldObj.playRecord((String)null, i3, i4, i5);
			}
			break;
		case 2000:
			int i8 = i6 % 3 - 1;
			int i9 = i6 / 3 % 3 - 1;
			double d10 = (double)i3 + (double)i8 * 0.6D + 0.5D;
			double d12 = (double)i4 + 0.5D;
			double d14 = (double)i5 + (double)i9 * 0.6D + 0.5D;

			for(i16 = 0; i16 < 10; ++i16) {
				double d31 = random7.nextDouble() * 0.2D + 0.01D;
				double d19 = d10 + (double)i8 * 0.01D + (random7.nextDouble() - 0.5D) * (double)i9 * 0.5D;
				double d21 = d12 + (random7.nextDouble() - 0.5D) * 0.5D;
				double d23 = d14 + (double)i9 * 0.01D + (random7.nextDouble() - 0.5D) * (double)i8 * 0.5D;
				double d25 = (double)i8 * d31 + random7.nextGaussian() * 0.01D;
				double d27 = -0.03D + random7.nextGaussian() * 0.01D;
				double d29 = (double)i9 * d31 + random7.nextGaussian() * 0.01D;
				this.spawnParticle("smoke", d19, d21, d23, d25, d27, d29);
			}

			return;
		case 2001:
			i16 = i6 & 255;
			if(i16 > 0) {
				Block block17 = Block.blocksList[i16];
				this.mc.sndManager.playSound(block17.stepSound.getBreakSound(), (float)i3 + 0.5F, (float)i4 + 0.5F, (float)i5 + 0.5F, (block17.stepSound.getVolume() + 1.0F) / 2.0F, block17.stepSound.getPitch() * 0.8F);
			}

			this.mc.effectRenderer.addBlockDestroyEffects(i3, i4, i5, i6 & 255, i6 >> 8 & 255);
		}

	}

	public boolean isMoving(EntityLiving entityliving) {
		boolean moving = this.isMovingNow(entityliving);
		if(moving) {
			this.lastMovedTime = System.currentTimeMillis();
			return true;
		} else {
			return System.currentTimeMillis() - this.lastMovedTime < 2000L;
		}
	}

	private boolean isMovingNow(EntityLiving entityliving) {
		double maxDiff = 0.001D;
		return entityliving.isJumping ? true : (entityliving.isSneaking() ? true : ((double)entityliving.prevSwingProgress > maxDiff ? true : (this.mc.mouseHelper.deltaX != 0 ? true : (this.mc.mouseHelper.deltaY != 0 ? true : (Math.abs(entityliving.posX - entityliving.prevPosX) > maxDiff ? true : (Math.abs(entityliving.posY - entityliving.prevPosY) > maxDiff ? true : Math.abs(entityliving.posZ - entityliving.prevPosZ) > maxDiff))))));
	}

	public boolean isActing() {
		boolean acting = this.isActingNow();
		if(acting) {
			this.lastActionTime = System.currentTimeMillis();
			return true;
		} else {
			return System.currentTimeMillis() - this.lastActionTime < 500L;
		}
	}
	
	public boolean isActingNow() {
		return Mouse.isButtonDown(0) ? true : Mouse.isButtonDown(1);
		}

	public int renderAllSortedRenderers(int renderPass, double partialTicks) {
		return this.renderSortedRenderers(0, this.sortedWorldRenderers.length, renderPass, partialTicks);
	}
}
