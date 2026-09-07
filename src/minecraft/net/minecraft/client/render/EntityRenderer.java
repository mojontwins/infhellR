package net.minecraft.client.render;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import net.minecraft.game.entity.status.Status;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.MouseFilter;
import net.minecraft.client.controller.PlayerControllerTest;
import net.minecraft.client.effect.EffectRenderer;
import net.minecraft.client.effect.EntityRainFX;
import net.minecraft.client.effect.EntitySmokeFX;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.render.camera.ClippingHelperImpl;
import net.minecraft.client.render.camera.Frustrum;

/**
 * Per-frame camera & render orchestrator: mouse look, FOV, head-bob, hurt/portal
 * spin, third-person camera, fog & lightmap handling, and the full world render
 * pipeline (sky, chunks, entities, particles, weather, clouds, hand).
 *
 * <p>Optimisation notes:
 * <ul>
 *   <li>All per-frame terrain queries for the weather grid (top-of-terrain height,
 *       biome) are cached per chunk cell and only rebuilt on movement or block
 *       changes (see {@link #weatherTopSolidY}/{@link #weatherColumnBiome}).</li>
 *   <li>Rain is emitted as two texture batches (rain/snow passes) so the tessellator
 *       is never flushed for individual biome transitions, and the translation is
 *       set once per batch instead of per column.</li>
 *   <li>Rain-droplet particles are hard-capped so rain can't saturate the particle
 *       layer (each droplet is collision-checked and drawn every frame).</li>
 * </ul>
 */
public class EntityRenderer {
	public static boolean anaglyphEnable = false;
	public static int anaglyphField;
	/** The Minecraft instance whose frame pipeline this renderer drives. */
	private Minecraft mc;
	/** Far clip-plane distance, derived from the render distance setting. */
	private float farPlaneDistance = 0.0F;
	/** Renders the player's held item / hand in first-person view. */
	public ItemRenderer itemRenderer;
	/** Incremented once per frame; drives animation phases and smooth-cam. */
	private int rendererUpdateCount;
	/** The entity currently aimed at by the crosshair (null if none). */
	private Entity pointedEntity = null;
	/** Exponential-average filters for the mouse look (smooth camera). */
	private MouseFilter mouseFilterXAxis = new MouseFilter();
	private MouseFilter mouseFilterYAxis = new MouseFilter();
	/** Third-person camera distance (m): current value and smoothed copy. */
	private float thirdPersonDistance = 4.0F;
	private float thirdPersonDistanceTemp = 4.0F;
	/** Debug (spectator) camera yaw, pitch and FOV, with previous-frame copies. */
	private float debugCamYaw = 0.0F;
	private float prevDebugCamYaw = 0.0F;
	private float debugCamPitch = 0.0F;
	private float prevDebugCamPitch = 0.0F;
	/** Smooth-camera accumulators and filter state. */
	private float smoothCamYaw;
	private float smoothCamPitch;
	private float smoothCamFilterX;
	private float smoothCamFilterY;
	private float smoothCamPartialTicks;
	private float debugCamFOV = 0.0F;
	private float prevDebugCamFOV = 0.0F;
	/** Camera roll (Z-axis tilt) and its previous value (for interpolation). */
	private float camRoll = 0.0F;
	private float prevCamRoll = 0.0F;
	/** GL texture id of the 16x16 lightmap, re-uploaded whenever it changes. */
	public int lightmapTexture;
	/** Latest lightmap texel colors (16x16 ARGB ints). */
	private int[] lightmapColors;
	/** True while the camera is inside a cloud layer (uses EXP fog then). */
	private boolean cloudFog = false;
	/** Special zoom (binoculars, map view): zoom factor and aim offset. */
	private double cameraZoom = 1.0D;
	private double cameraYaw = 0.0D;
	private double cameraPitch = 0.0D;
	/** Timestamps for the FPS limiter and the in-game-pause detection. */
	private long prevFrameTime = System.currentTimeMillis();
	private long renderEndNanoTime = 0L;
	/** Set when the lightmap texels must be recomputed before the next frame. */
	private boolean lightmapUpdateNeeded = false;
	private Random random = new Random();
	/** Cadence counter for the ambient rain sound. */
	private int rainSoundCounter = 0;
	/** Reusable FloatBuffer receiving the fog color for glFog. */
	FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
	/** Current, view-dependent fog color (RGB). */
	float fogColorRed;
	float fogColorGreen;
	float fogColorBlue;
	/** Smoothed background-dim factor applied to the fog colors (prev/current). */
	private float prevFogBrightness;
	private float fogBrightness;
	/** Unit-circle direction LUT (32x32 grid, centered) for the rain/snow streak tilt. */
	float columnDirX[] = new float[1024];
	float columnDirZ[] = new float[1024];

	/* ---- Weather grid column cache (W1) ---- */
	/** Render radius of the weather streak grid, in blocks. */
	private static final int WEATHER_RADIUS = 12;
	/** Cache window size: one chunk cell (16 blocks) plus the radius on each side. */
	private static final int WEATHER_CACHE_SIZE = WEATHER_RADIUS * 2 + 16;
	/** World the cache was built for (rebuilds when the world changes). */
	private World weatherCacheWorld;
	/** Player chunk-cell the cache is anchored to. */
	private int weatherCacheCellX = Integer.MIN_VALUE;
	private int weatherCacheCellZ = Integer.MIN_VALUE;
	/** Per-column top-of-terrain height, indexed by cache cell (or -1 if void). */
	private int[] weatherTopSolidY;
	/** Per-column biome, indexed by cache cell. */
	private BiomeGenBase[] weatherColumnBiome;
	/** Set by RenderGlobal whenever blocks change so the weather cache rebuilds. */
	boolean weatherCacheDirty = false;
	/**
	 * Hard cap on live rain-droplet particles (W3). Every droplet is collision-checked
	 * and drawn each frame, so without a cap sustained rain saturates the effect layer
	 * and tanks FPS.
	 */
	private static final int MAX_RAIN_PARTICLES = 400;

	public EntityRenderer(Minecraft minecraft) {
		this.mc = minecraft;
		this.itemRenderer = new ItemRenderer(minecraft);
		this.lightmapTexture = minecraft.renderEngine.allocateAndSetupTexture(new BufferedImage(16, 16, 1));
		this.lightmapColors = new int[256];

		// Pre-compute the unit-circle direction LUT used to tilt each weather column's
		// streak away from the camera, so streaks always lean outward over the grid.
		int idx = 0;
		for(int x = -16; x < 16; ++x) {
			for(int z = -16; z < 16; ++z) {
				float distance = MathHelper.sqrt_float((float)(x * x) + (z * z));
				this.columnDirX[idx] = -((float)x) / distance;
				this.columnDirZ[idx] = ((float)z) / distance;
				++idx;
			}
		}
	}

	/**
	 * Per-frame bookkeeping: advances animations, smooths the fog brightness, ticks
	 * the held item and spawns rain droplets around the player.
	 */
	public void updateRenderer() {
		this.lightmapUpdateNeeded = true;
		this.prevFogBrightness = this.fogBrightness;
		this.thirdPersonDistanceTemp = this.thirdPersonDistance;
		this.prevDebugCamYaw = this.debugCamYaw;
		this.prevDebugCamPitch = this.debugCamPitch;
		this.prevDebugCamFOV = this.debugCamFOV;
		this.prevCamRoll = this.camRoll;

		if(GameSettingsValues.smoothCamera) {
			float smoothing = GameSettingsValues.mouseSensitivity * 0.6F + 0.2F;
			float smoothingPower = smoothing * smoothing * smoothing * 8.0F;
			this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * smoothingPower);
			this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * smoothingPower);
			this.smoothCamPartialTicks = 0.0F;
			this.smoothCamYaw = 0.0F;
			this.smoothCamPitch = 0.0F;
		}

		if(this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}

		// Blend the light brightness at the player's position into a smoothed "fog
		// brightness" so the scene dims gradually between day and night.
		float playerBrightness = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(this.mc.renderViewEntity.posX), MathHelper.floor_double(this.mc.renderViewEntity.posY), MathHelper.floor_double(this.mc.renderViewEntity.posZ));
		float fogWeight = (float)(3 - GameSettingsValues.renderDistance) / 3.0F;
		float targetBrightness = playerBrightness * (1.0F - fogWeight) + fogWeight;
		this.fogBrightness += (targetBrightness - this.fogBrightness) * 0.1F;
		++this.rendererUpdateCount;
		this.itemRenderer.updateEquippedItem();
		this.addRainParticles();
	}

	/** Picks the block/entity under the crosshair and stores it in mc.objectMouseOver. */
	public void getMouseOver(float renderPartialTicks) {
		if(this.mc.renderViewEntity != null) {
			if(this.mc.theWorld != null) {
				double reachDistance = (double)this.mc.playerController.getBlockReachDistance();
				this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(reachDistance, renderPartialTicks);
				double bestDistance = reachDistance;
				Vec3D eyePosition = this.mc.renderViewEntity.getPosition(renderPartialTicks);
				if(this.mc.objectMouseOver != null) {
					bestDistance = this.mc.objectMouseOver.hitVec.distanceTo(eyePosition);
				}

				if(this.mc.playerController instanceof PlayerControllerTest) {
					reachDistance = 32.0D;
					bestDistance = 32.0D;
				} else {
					if(bestDistance > 3.0D) {
						bestDistance = 3.0D;
					}

					reachDistance = bestDistance;
				}

				// Sweep the view ray against the bounding boxes of all nearby entities,
				// keeping the closest hit inside the reach distance.
				Vec3D lookVector = this.mc.renderViewEntity.getLook(renderPartialTicks);
				Vec3D lookEndPoint = eyePosition.addVector(lookVector.xCoord * reachDistance, lookVector.yCoord * reachDistance, lookVector.zCoord * reachDistance);
				this.pointedEntity = null;
				float collisionMargin = 1.0F;
				List<Entity> entityList = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(lookVector.xCoord * reachDistance, lookVector.yCoord * reachDistance, lookVector.zCoord * reachDistance).expand((double)collisionMargin, (double)collisionMargin, (double)collisionMargin));
				double closestDistance = 0.0D;

				for(int entityIndex = 0; entityIndex < entityList.size(); ++entityIndex) {
					Entity entity = (Entity)entityList.get(entityIndex);
					if(entity.canBeCollidedWith()) {
						float entityCollisionMargin = entity.getCollisionBorderSize();
						AxisAlignedBB expandedBox = entity.boundingBox.expand((double)entityCollisionMargin, (double)entityCollisionMargin, (double)entityCollisionMargin);
						MovingObjectPosition rayHit = expandedBox.raytrace(eyePosition, lookEndPoint);
						if(expandedBox.isVecInside(eyePosition)) {
							if(0.0D < closestDistance || closestDistance == 0.0D) {
								this.pointedEntity = entity;
								closestDistance = 0.0D;
							}
						} else if(rayHit != null) {
							double hitDistance = eyePosition.distanceTo(rayHit.hitVec);
							if(hitDistance < closestDistance || closestDistance == 0.0D) {
								this.pointedEntity = entity;
								closestDistance = hitDistance;
							}
						}
					}
				}

				if(this.pointedEntity != null && !(this.mc.playerController instanceof PlayerControllerTest)) {
					this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity);
				}

			}
		}
	}

	/** Computes the base field-of-view (deg), modified by water, death-cam and the FOV setting. */
	private float getFOVModifier(float renderPartialTicks) {
		EntityLiving viewEntity = this.mc.renderViewEntity;
		float fov = 70.0F;
		if(viewEntity.isInsideOfMaterial(Material.water)) {
			if(this.mc.thePlayer.divingHelmetOn()) {
				fov = 50.0F;
			} else {
				fov = 60.0F;
			}
		}

		if(viewEntity.health <= 0) {
			// Widen the view progressively while the death animation plays.
			float deathFade = (float)viewEntity.deathTime + renderPartialTicks;
			fov /= (1.0F - 500.0F / (deathFade + 500.0F)) * 2.0F + 1.0F;
		}

		return fov + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * renderPartialTicks + GameSettingsValues.FOV;
	}

	/** Tilts and zooms the camera for the red-screen hurt / death effect. */
	private void hurtCameraEffect(float renderPartialTicks) {
		EntityLiving viewEntity = this.mc.renderViewEntity;

		// Creative players are exempt from the hit wobble.
		if(viewEntity instanceof EntityPlayer && ((EntityPlayer)viewEntity).isCreative) return;

		float hurtRemaining = (float)viewEntity.hurtTime - renderPartialTicks;
		float deathFade;
		if(viewEntity.health <= 0) {
			deathFade = (float)viewEntity.deathTime + renderPartialTicks;
			GL11.glRotatef(40.0F - 8000.0F / (deathFade + 200.0F), 0.0F, 0.0F, 1.0F);
		}

		if(hurtRemaining >= 0.0F) {
			// Rock the view in the direction the player was attacked from.
			hurtRemaining /= (float)viewEntity.maxHurtTime;
			hurtRemaining = MathHelper.sin(hurtRemaining * hurtRemaining * hurtRemaining * hurtRemaining * (float)Math.PI);
			float attackYaw = viewEntity.attackedAtYaw;
			GL11.glRotatef(-attackYaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-hurtRemaining * 14.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(attackYaw, 0.0F, 1.0F, 0.0F);
		}
	}

	/** Applies the walking head-bob (amplified when the player is crippled). */
	private void setupViewBobbing(float renderPartialTicks) {
		if(this.mc.renderViewEntity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)this.mc.renderViewEntity;

			// Bob harder when crippled (low health, non-creative).
			float crippleEnhancer = !player.isCreative && player.health < 5 ? 3.0F : 1.0F;

			float walkDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
			float bobPhase = -(player.distanceWalkedModified + walkDelta * renderPartialTicks);
			float bobHeight = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * renderPartialTicks;
			float cameraTilt = player.cameraPitch + (player.field_9328_R - player.cameraPitch) * renderPartialTicks;
			GL11.glTranslatef(MathHelper.sin(bobPhase * (float)Math.PI) * bobHeight * 0.5F, -Math.abs(MathHelper.cos(bobPhase * (float)Math.PI) * bobHeight * crippleEnhancer), 0.0F);
			GL11.glRotatef(MathHelper.sin(bobPhase * (float)Math.PI) * bobHeight * crippleEnhancer * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(Math.abs(MathHelper.cos(bobPhase * (float)Math.PI - 0.2F) * bobHeight * crippleEnhancer) * 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(cameraTilt, 1.0F, 0.0F, 0.0F);
		}
	}

	/** Positions and orients the camera relative to the render view entity. */
	private void orientCamera(float renderPartialTicks) {
		EntityLiving viewEntity = this.mc.renderViewEntity;
		float eyeHeight = viewEntity.yOffset - 1.62F;
		double camPosX = viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * (double)renderPartialTicks;
		double camPosY = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * (double)renderPartialTicks - (double)eyeHeight;
		double camPosZ = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * (double)renderPartialTicks;
		GL11.glRotatef(this.prevCamRoll + (this.camRoll - this.prevCamRoll) * renderPartialTicks, 0.0F, 0.0F, 1.0F);
		if(viewEntity.isPlayerSleeping()) {
			// Sleeping: drop the camera to bed level, facing into the pillow.
			eyeHeight = (float)((double)eyeHeight + 1.0D);
			GL11.glTranslatef(0.0F, 0.3F, 0.0F);
			if(!GameSettingsValues.debugCamEnable) {
				int blockId = this.mc.theWorld.getBlockId(MathHelper.floor_double(viewEntity.posX), MathHelper.floor_double(viewEntity.posY), MathHelper.floor_double(viewEntity.posZ));
				if(blockId == Block.blockBed.blockID) {
					int metadata = this.mc.theWorld.getBlockMetadata(MathHelper.floor_double(viewEntity.posX), MathHelper.floor_double(viewEntity.posY), MathHelper.floor_double(viewEntity.posZ));
					int direction = metadata & 3;
					GL11.glRotatef((float)(direction * 90), 0.0F, 1.0F, 0.0F);
				}

				GL11.glRotatef(viewEntity.prevRotationYaw + (viewEntity.rotationYaw - viewEntity.prevRotationYaw) * renderPartialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
				GL11.glRotatef(viewEntity.prevRotationPitch + (viewEntity.rotationPitch - viewEntity.prevRotationPitch) * renderPartialTicks, -1.0F, 0.0F, 0.0F);
			}
		} else if(GameSettingsValues.thirdPersonView) {
			double thirdPersonDist = (double)(this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * renderPartialTicks);
			float camPitch;
			float camYaw;
			if(GameSettingsValues.debugCamEnable) {
				// Free-roaming debug camera.
				camYaw = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * renderPartialTicks;
				camPitch = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * renderPartialTicks;
				GL11.glTranslatef(0.0F, 0.0F, (float)(-thirdPersonDist));
				GL11.glRotatef(camPitch, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(camYaw, 0.0F, 1.0F, 0.0F);
			} else {
				// Third person: place the camera behind the player, sliding it forward
				// whenever an obstruction is hit so it never clips into walls.
				camYaw = viewEntity.rotationYaw;
				camPitch = viewEntity.rotationPitch;
				double backwardsX = (double)(-MathHelper.sin(camYaw / 180.0F * (float)Math.PI) * MathHelper.cos(camPitch / 180.0F * (float)Math.PI)) * thirdPersonDist;
				double backwardsY = (double)(-MathHelper.sin(camPitch / 180.0F * (float)Math.PI)) * thirdPersonDist;
				double backwardsZ = (double)(MathHelper.cos(camYaw / 180.0F * (float)Math.PI) * MathHelper.cos(camPitch / 180.0F * (float)Math.PI)) * thirdPersonDist;

				// Probe eight thin rays around the eye to approximate the camera's width.
				for(int sampleIndex = 0; sampleIndex < 8; ++sampleIndex) {
					float sampleX = (float)((sampleIndex & 1) * 2 - 1);
					float sampleY = (float)((sampleIndex >> 1 & 1) * 2 - 1);
					float sampleZ = (float)((sampleIndex >> 2 & 1) * 2 - 1);
					sampleX *= 0.1F;
					sampleY *= 0.1F;
					sampleZ *= 0.1F;
					MovingObjectPosition obstruction = this.mc.theWorld.rayTraceBlocks(Vec3D.createVector(camPosX + (double)sampleX, camPosY + (double)sampleY, camPosZ + (double)sampleZ), Vec3D.createVector(camPosX - backwardsX + (double)sampleX + (double)sampleZ, camPosY - backwardsY + (double)sampleY, camPosZ - backwardsZ + (double)sampleZ));
					if(obstruction != null) {
						double obstructionDistance = obstruction.hitVec.distanceTo(Vec3D.createVector(camPosX, camPosY, camPosZ));
						if(obstructionDistance < thirdPersonDist) {
							thirdPersonDist = obstructionDistance;
						}
					}
				}

				GL11.glRotatef(viewEntity.rotationPitch - camPitch, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(viewEntity.rotationYaw - camYaw, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, (float)(-thirdPersonDist));
				GL11.glRotatef(camYaw - viewEntity.rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(camPitch - viewEntity.rotationPitch, 1.0F, 0.0F, 0.0F);
			}
		} else {
			GL11.glTranslatef(0.0F, 0.0F, -0.1F);
		}

		if(!GameSettingsValues.debugCamEnable) {
			GL11.glRotatef(viewEntity.prevRotationPitch + (viewEntity.rotationPitch - viewEntity.prevRotationPitch) * renderPartialTicks, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(viewEntity.prevRotationYaw + (viewEntity.rotationYaw - viewEntity.prevRotationYaw) * renderPartialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
		}

		GL11.glTranslatef(0.0F, eyeHeight, 0.0F);
		camPosX = viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * (double)renderPartialTicks;
		camPosY = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * (double)renderPartialTicks - (double)eyeHeight;
		camPosZ = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * (double)renderPartialTicks;
		this.cloudFog = this.mc.renderGlobal.clipRenderersByFrustrum(camPosX, camPosY, camPosZ, renderPartialTicks);
	}

	/** Sets the projection & model-view matrices for one eye (anaglyph) pass. */
	private void setupCameraTransform(float renderPartialTicks, int eye) {
		this.farPlaneDistance = (float)(256 >> GameSettingsValues.renderDistance);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		float eyeSeparation = 0.07F;
		if(GameSettingsValues.anaglyph) {
			GL11.glTranslatef((float)(-(eye * 2 - 1)) * eyeSeparation, 0.0F, 0.0F);
		}

		if(this.cameraZoom != 1.0D) {
			GL11.glTranslatef((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
			GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
			GLU.gluPerspective(this.getFOVModifier(renderPartialTicks), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
		} else {
			GLU.gluPerspective(this.getFOVModifier(renderPartialTicks), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
		}

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		if(GameSettingsValues.anaglyph) {
			GL11.glTranslatef((float)(eye * 2 - 1) * 0.1F, 0.0F, 0.0F);
		}

		this.hurtCameraEffect(renderPartialTicks);
		if(GameSettingsValues.viewBobbing) {
			this.setupViewBobbing(renderPartialTicks);
		}

		// Portal / dizzy status spins the camera about the view axis.
		float portalTime = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * renderPartialTicks;
		if(portalTime > 0.0F) {
			float portalScale = 5.0F / (portalTime * portalTime + 5.0F) - portalTime * 0.04F;
			portalScale *= portalScale;
			float spinMultiplier = 20.0F;

			// Dizzy players get a gentler spin.
			if(this.mc.thePlayer.isStatusActive(Status.statusDizzy)) spinMultiplier = 7.0F;

			GL11.glRotatef(((float)this.rendererUpdateCount + renderPartialTicks) * spinMultiplier, 0.0F, 1.0F, 1.0F);
			GL11.glScalef(1.0F / portalScale, 1.0F, 1.0F);
			GL11.glRotatef(-((float)this.rendererUpdateCount + renderPartialTicks) * spinMultiplier, 0.0F, 1.0F, 1.0F);
		}

		this.orientCamera(renderPartialTicks);
	}

	/** Renders the first-person hand, held item and the fire/water overlay effects. */
	private void renderHand(float renderPartialTicks, int eye) {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		if(GameSettingsValues.anaglyph) {
			GL11.glTranslatef((float)(eye * 2 - 1) * 0.1F, 0.0F, 0.0F);
		}

		GL11.glPushMatrix();
		this.hurtCameraEffect(renderPartialTicks);
		if(GameSettingsValues.viewBobbing) {
			this.setupViewBobbing(renderPartialTicks);
		}

		// First-person held item (skipped for third person, sleeping and hidden GUI).
		if(!GameSettingsValues.thirdPersonView && !this.mc.renderViewEntity.isPlayerSleeping() && !GameSettingsValues.hideGUI) {
			this.enableLightmap((double)renderPartialTicks);
			this.itemRenderer.renderItemInFirstPerson(renderPartialTicks);
			this.disableLightmap((double)renderPartialTicks);
		}

		GL11.glPopMatrix();
		if(!GameSettingsValues.thirdPersonView && !this.mc.renderViewEntity.isPlayerSleeping()) {
			// Fire / suffocation / water overlay full-screen effects.
			this.itemRenderer.renderOverlays(renderPartialTicks);
			this.hurtCameraEffect(renderPartialTicks);
		}

		if(GameSettingsValues.viewBobbing) {
			this.setupViewBobbing(renderPartialTicks);
		}

	}

	/** Detaches the lightmap texture unit for regular (unlit) rendering. */
	public void disableLightmap(double partialTicks) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/** Switches in the lightmap texture unit and positions it over the geometry. */
	public void enableLightmap(double partialTicks) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		float textureScale = 0.00390625F;
		GL11.glScalef(textureScale, textureScale, textureScale);
		GL11.glTranslatef(8.0F, 8.0F, 8.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.mc.renderEngine.bindTexture(this.lightmapTexture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/** Re-generates the 16x16 lightmap texels from the current world lighting. */
	private void updateLightmap() {
		World world = this.mc.theWorld;
		if(world != null) {
			this.lightmapColors = world.worldProvider.updateLightmap(this.mc.thePlayer, GameSettingsValues.gammaSetting);

			this.mc.renderEngine.createTextureFromBytes(this.lightmapColors, 16, 16, this.lightmapTexture);
			this.lightmapUpdateNeeded = false;
		}
	}

	/**
	 * Top-level per-frame entry point: updates the lightmap, applies mouse look and
	 * the FPS limiter, renders the 3D world and then overlays the HUD / GUI.
	 */
	public void updateCameraAndRender(float renderPartialTicks) {
		if(this.lightmapUpdateNeeded) {
			this.updateLightmap();
		}
		if(!Display.isActive()) {
			// Game window lost focus: pause after half a second.
			if(System.currentTimeMillis() - this.prevFrameTime > 500L) {
				this.mc.displayInGameMenu();
			}
		} else {
			this.prevFrameTime = System.currentTimeMillis();
		}

		// Apply mouse look (with optional smoothing) to the player.
		if(this.mc.inGameHasFocus) {
			this.mc.mouseHelper.mouseXYChange();
			float mouseSmoothing = GameSettingsValues.mouseSensitivity * 0.6F + 0.2F;
			float smoothingPower = mouseSmoothing * mouseSmoothing * mouseSmoothing * 8.0F;
			float yawDelta = (float)this.mc.mouseHelper.deltaX * smoothingPower;
			float pitchDelta = (float)this.mc.mouseHelper.deltaY * smoothingPower;
			byte invertSign = 1;
			if(GameSettingsValues.invertMouse) {
				invertSign = -1;
			}

			if(GameSettingsValues.smoothCamera) {
				this.smoothCamYaw += yawDelta;
				this.smoothCamPitch += pitchDelta;
				float elapsedTicks = renderPartialTicks - this.smoothCamPartialTicks;
				this.smoothCamPartialTicks = renderPartialTicks;
				yawDelta = this.smoothCamFilterX * elapsedTicks;
				pitchDelta = this.smoothCamFilterY * elapsedTicks;
				this.mc.thePlayer.setAngles(yawDelta, pitchDelta * (float)invertSign);
			} else {
				this.mc.thePlayer.setAngles(yawDelta, pitchDelta * (float)invertSign);
			}
		}

		if(!this.mc.skipRenderWorld) {
			anaglyphEnable = GameSettingsValues.anaglyph;
			ScaledResolution scaledResolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			int scaledWidth = scaledResolution.getScaledWidth();
			int scaledHeight = scaledResolution.getScaledHeight();
			int mouseX = Mouse.getX() * scaledWidth / this.mc.displayWidth;
			int mouseY = scaledHeight - Mouse.getY() * scaledHeight / this.mc.displayHeight - 1;

			// FPS limiter: 0 = unlimited, 1 = 120, 2 = 40.
			short frameLimitTarget = 200;
			if(GameSettingsValues.limitFramerate == 1) {
				frameLimitTarget = 120;
			}

			if(GameSettingsValues.limitFramerate == 2) {
				frameLimitTarget = 40;
			}

			long sleepMillis;
			if(this.mc.theWorld != null) {
				if(GameSettingsValues.limitFramerate == 0) {
					this.renderWorld(renderPartialTicks, 0L);
				} else {
					this.renderWorld(renderPartialTicks, this.renderEndNanoTime + (long)(1000000000 / frameLimitTarget));
				}

				if(GameSettingsValues.limitFramerate == 2) {
					sleepMillis = (this.renderEndNanoTime + (long)(1000000000 / frameLimitTarget) - System.nanoTime()) / 1000000L;
					if(sleepMillis > 0L && sleepMillis < 500L) {
						try {
							Thread.sleep(sleepMillis);
						} catch (InterruptedException interrupt) {
							interrupt.printStackTrace();
						}
					}
				}

				this.renderEndNanoTime = System.nanoTime();
				if(!GameSettingsValues.hideGUI || this.mc.currentScreen != null) {
					this.mc.ingameGUI.renderGameOverlay(renderPartialTicks, this.mc.currentScreen != null, mouseX, mouseY);
				}
			} else {
				GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				this.setupOverlayRendering();
				if(GameSettingsValues.limitFramerate == 2) {
					sleepMillis = (this.renderEndNanoTime + (long)(1000000000 / frameLimitTarget) - System.nanoTime()) / 1000000L;
					if(sleepMillis < 0L) {
						sleepMillis += 10L;
					}

					if(sleepMillis > 0L && sleepMillis < 500L) {
						try {
							Thread.sleep(sleepMillis);
						} catch (InterruptedException interrupt) {
							interrupt.printStackTrace();
						}
					}
				}

				this.renderEndNanoTime = System.nanoTime();
			}

			if(this.mc.currentScreen != null) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				this.mc.currentScreen.drawScreen(mouseX, mouseY, renderPartialTicks);
				if(this.mc.currentScreen != null && this.mc.currentScreen.guiParticles != null) {
					this.mc.currentScreen.guiParticles.draw(renderPartialTicks);
				}
			}

		}
	}

	/**
	 * Renders the chunk world (pass 0) plus entities, particles, water, weather,
	 * clouds and the hand, once per anaglyph eye.
	 *
	 * @param frameDeadlineNano nanoTime budget within which chunk recompiles must
	 *                          finish (0 = unlimited).
	 */
	public void renderWorld(float renderPartialTicks, long frameDeadlineNano) {
		if(this.lightmapUpdateNeeded) {
			this.updateLightmap();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		if(this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}

		this.getMouseOver(renderPartialTicks);
		EntityLiving viewEntity = this.mc.renderViewEntity;
		RenderGlobal renderGlobal = this.mc.renderGlobal;
		EffectRenderer effectRenderer = this.mc.effectRenderer;
		double camPosX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * (double)renderPartialTicks;
		double camPosY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * (double)renderPartialTicks;
		double camPosZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * (double)renderPartialTicks;

		// Render once per anaglyph eye (red field 1, cyan field 0); a single pass otherwise.
		for(int eyePass = 0; eyePass < 2; ++eyePass) {
			if(GameSettingsValues.anaglyph) {
				anaglyphField = eyePass;
				if(anaglyphField == 0) {
					GL11.glColorMask(false, true, true, false);
				} else {
					GL11.glColorMask(true, false, false, false);
				}
			}

			GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
			this.updateFogColor(renderPartialTicks);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			this.setupCameraTransform(renderPartialTicks, eyePass);
			ClippingHelperImpl.getInstance();
			if(GameSettingsValues.renderDistance < 2) {
				this.setupFog(-1, renderPartialTicks);
				renderGlobal.renderSky(renderPartialTicks);
			}

			GL11.glEnable(GL11.GL_FOG);
			this.setupFog(1, renderPartialTicks);
			if(GameSettingsValues.ambientOcclusion) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
			}

			Frustrum frustum = new Frustrum();
			frustum.setPosition(camPosX, camPosY, camPosZ);
			this.mc.renderGlobal.clipRenderersByFrustum(frustum, renderPartialTicks);

			// Budget chunk recompiles against the remaining frame time (unlimited when 0).
			if(eyePass == 0) {
				while(!this.mc.renderGlobal.updateRenderers(viewEntity, false) && frameDeadlineNano != 0L) {
					long sleepMillis = frameDeadlineNano - System.nanoTime();
					if(sleepMillis < 0L || sleepMillis > 1000000000L) {
						break;
					}
				}
			}

			this.setupFog(0, renderPartialTicks);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
			RenderHelper.disableStandardItemLighting();
			renderGlobal.sortAndRender(viewEntity, 0, (double)renderPartialTicks);
			GL11.glShadeModel(GL11.GL_FLAT);
			EntityPlayer player;
			RenderHelper.enableStandardItemLighting();

			// Entities (interpolated positions).
			renderGlobal.renderEntities(viewEntity.getPosition(renderPartialTicks), frustum, renderPartialTicks);

			// Lit particles (fire, glowstone, etc.).
			this.enableLightmap((double)renderPartialTicks);
			effectRenderer.func_1187_b(viewEntity, renderPartialTicks);
			RenderHelper.disableStandardItemLighting();

			// Normal particles.
			this.setupFog(0, renderPartialTicks);
			effectRenderer.renderParticles(viewEntity, renderPartialTicks);
			this.disableLightmap((double)renderPartialTicks);

			if(this.mc.objectMouseOver != null && viewEntity.isInsideOfMaterial(Material.water) && viewEntity instanceof EntityPlayer) {
				player = (EntityPlayer)viewEntity;
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				renderGlobal.drawBlockBreaking(player, this.mc.objectMouseOver, 0, player.inventory.getCurrentItem(), renderPartialTicks);
				renderGlobal.drawSelectionBox(player, this.mc.objectMouseOver, 0, player.inventory.getCurrentItem(), renderPartialTicks);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
			}

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(true);
			this.setupFog(0, renderPartialTicks);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));

			// Water: a colour-masked hidden pass writes to the depth buffer, then the
			// blended pass is drawn on top.
			if(GameSettingsValues.fancyGraphics || GameSettingsValues.clearWaters) {
				if(GameSettingsValues.ambientOcclusion) {
					GL11.glShadeModel(GL11.GL_SMOOTH);
				}

				GL11.glColorMask(false, false, false, false);
				int waterRenderers = renderGlobal.renderAllSortedRenderers(1, (double)renderPartialTicks);
				if(GameSettingsValues.anaglyph) {
					if(anaglyphField == 0) {
						GL11.glColorMask(false, true, true, true);
					} else {
						GL11.glColorMask(true, false, false, true);
					}
				} else {
					GL11.glColorMask(true, true, true, true);
				}

				if(waterRenderers > 0) {
					renderGlobal.renderAllSortedRenderers(1, (double)renderPartialTicks);
				}

				GL11.glShadeModel(GL11.GL_FLAT);
			} else {
				renderGlobal.renderAllSortedRenderers(1, (double)renderPartialTicks);
			}

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			if(this.cameraZoom == 1.0D && viewEntity instanceof EntityPlayer && this.mc.objectMouseOver != null && !viewEntity.isInsideOfMaterial(Material.water)) {
				player = (EntityPlayer)viewEntity;
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				renderGlobal.drawBlockBreaking(player, this.mc.objectMouseOver, 0, player.inventory.getCurrentItem(), renderPartialTicks);
				renderGlobal.drawSelectionBox(player, this.mc.objectMouseOver, 0, player.inventory.getCurrentItem(), renderPartialTicks);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
			}

			// Weather layer (rain/snow streaks).
			if(
					this.mc.theWorld.rainingStrength > 0.0F ||
					this.mc.theWorld.snowingStrength > 0.0F
			) {
				this.renderWeather(renderPartialTicks);
			}

			GL11.glDisable(GL11.GL_FOG);
			if(this.pointedEntity == null) {
				// (Intentional legacy no-op reserved for the removed FOV-on-entity hook.)
			}

			// Cloud layer.
			GL11.glPushMatrix();
			this.setupFog(0, renderPartialTicks);
			GL11.glEnable(GL11.GL_FOG);
			renderGlobal.renderClouds(renderPartialTicks);
			GL11.glDisable(GL11.GL_FOG);
			this.setupFog(1, renderPartialTicks);
			GL11.glPopMatrix();

			if(this.cameraZoom == 1.0D) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				this.renderHand(renderPartialTicks, eyePass);
			}

			if(!GameSettingsValues.anaglyph) {
				return;
			}
		}

		GL11.glColorMask(true, true, true, false);
	}

	/**
	 * Spawns the falling rain droplets (and lava steam) as particles around the
	 * player. The rate scales with rain strength but is hard-capped at
	 * {@link #MAX_RAIN_PARTICLES}: every droplet is collision-checked and drawn each
	 * frame, and the spawn loop otherwise scales with FPS, flooding the effect layer.
	 */
	private void addRainParticles() {
		World world = this.mc.theWorld;

		float rainStrength = this.mc.theWorld.getRainStrength(1.0F);
		// Low-detail settings halve the droplet density.
		if(!GameSettingsValues.fancyGraphics && !GameSettingsValues.clearWaters) {
			rainStrength /= 2.0F;
		}

		if(rainStrength != 0.0F) {
			this.random.setSeed((long)this.rendererUpdateCount * 312987231L);
			EntityLiving viewEntity = this.mc.renderViewEntity;
			int playerX = MathHelper.floor_double(viewEntity.posX);
			int playerY = MathHelper.floor_double(viewEntity.posY);
			int playerZ = MathHelper.floor_double(viewEntity.posZ);
			byte spreadRadius = 10;
			// Fallback sound position: the player's eye, used when the particle cap
			// prevents any droplet from spawning nearby this frame.
			double rainSoundX = viewEntity.posX;
			double rainSoundY = viewEntity.posY + 1.0D;
			double rainSoundZ = viewEntity.posZ;
			int spawnedDrops = 0;

			int maxSpawns = (int)(100.0F * rainStrength * rainStrength);
			for(int spawnAttempt = 0; spawnAttempt < maxSpawns; ++spawnAttempt) {
				// W3: stop spawning once the live droplet count hits its cap.
				if(this.mc.effectRenderer.getParticleCount(0) >= MAX_RAIN_PARTICLES) {
					break;
				}

				int spawnX = playerX + this.random.nextInt(spreadRadius) - this.random.nextInt(spreadRadius);
				int spawnZ = playerZ + this.random.nextInt(spreadRadius) - this.random.nextInt(spreadRadius);

				BiomeGenBase biome = world.getBiomeGenAt(spawnX, spawnZ);
				int particleType = Weather.particleDecide(biome, world);
				if(particleType != Weather.RAIN) continue;

				int topSolidY = world.findTopSolidBlockUsingBlockMaterial(spawnX, spawnZ);
				int groundBlockId = world.getBlockId(spawnX, topSolidY - 1, spawnZ);
				if(topSolidY <= playerY + spreadRadius && topSolidY >= playerY - spreadRadius) {
					float offsetX = this.random.nextFloat();
					float offsetZ = this.random.nextFloat();
					if(groundBlockId > 0) {
						if(Block.blocksList[groundBlockId].blockMaterial == Material.lava) {
							// Steam rises where rain hits lava.
							this.mc.effectRenderer.addEffect(new EntitySmokeFX(world, (double)((float)spawnX + offsetX), (double)((float)topSolidY + 0.1F) - Block.blocksList[groundBlockId].minY, (double)((float)spawnZ + offsetZ), 0.0D, 0.0D, 0.0D));
						} else {
							++spawnedDrops;
							if(this.random.nextInt(spawnedDrops) == 0) {
								// Remember a random splash position for the rain sound.
								rainSoundX = (double)((float)spawnX + offsetX);
								rainSoundY = (double)((float)topSolidY + 0.1F) - Block.blocksList[groundBlockId].minY;
								rainSoundZ = (double)((float)spawnZ + offsetZ);
							}

							this.mc.effectRenderer.addEffect(new EntityRainFX(world, (double)((float)spawnX + offsetX), (double)((float)topSolidY + 0.1F) - Block.blocksList[groundBlockId].minY, (double)((float)spawnZ + offsetZ)));
						}
					}
				}
			}

			// Ambient rain sound: plays a few times while it is raining. It no longer
			// requires a droplet to have spawned, so the ambience persists once the
			// particle cap is reached.
			if(rainStrength > 0.0F && this.random.nextInt(3) < this.rainSoundCounter++) {
				this.rainSoundCounter = 0;
				if(rainSoundY > viewEntity.posY + 1.0D && world.findTopSolidBlockUsingBlockMaterial(MathHelper.floor_double(viewEntity.posX), MathHelper.floor_double(viewEntity.posZ)) > MathHelper.floor_double(viewEntity.posY)) {
					this.mc.theWorld.playSoundEffect(rainSoundX, rainSoundY, rainSoundZ, "ambient.weather.rain", 0.1F, 0.5F);
				} else {
					this.mc.theWorld.playSoundEffect(rainSoundX, rainSoundY, rainSoundZ, "ambient.weather.rain", 0.2F, 1.0F);
				}
			}

		}
	}

	/**
	 * Draws the full-screen rain/snow streak layer around the player.
	 *
	 * The terrain data for each column (top-of-terrain height and biome) is fetched
	 * from the cached column grid (see {@link #rebuildWeatherCache()}) instead of
	 * re-scanning the world, and the layer is emitted as exactly two tessellator
	 * batches (rain, then snow) so the texture never flushes per biome boundary.
	 */
	protected void renderWeather(float renderPartialTicks) {

		this.enableLightmap((double)renderPartialTicks);

		EntityLiving viewEntity = this.mc.renderViewEntity;
		World world = this.mc.theWorld;
		float rainStrength = world.getRainStrength(renderPartialTicks);
		float snowStrength = world.getSnowStrength(renderPartialTicks);

		// (Early return keeps the lightmap enabled, matching the original behaviour.)
		if(rainStrength <= 0.0F && snowStrength <= 0.0F) return;

		// Player block coordinates.
		int playerX = MathHelper.floor_double(viewEntity.posX);
		int playerY = MathHelper.floor_double(viewEntity.posY);
		int playerZ = MathHelper.floor_double(viewEntity.posZ);

		// Interpolated camera position; the streak layer is centered on it. All quads
		// of a pass share the same offset, so it is set once per batch.
		double interpolatedX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * (double)renderPartialTicks;
		double interpolatedY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * (double)renderPartialTicks;
		double interpolatedZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * (double)renderPartialTicks;

		// ---- W1: terrain column cache ---------------------------------------------
		// Top-of-terrain height and biome are static per column, so they are cached
		// per chunk cell and only rebuilt when the player crosses a cell boundary,
		// the world changes, or RenderGlobal flags a block change (weatherCacheDirty).
		int cellX = playerX >> 4;
		int cellZ = playerZ >> 4;
		if(cellX != this.weatherCacheCellX || cellZ != this.weatherCacheCellZ || this.mc.theWorld != this.weatherCacheWorld || this.weatherCacheDirty) {
			this.weatherCacheCellX = cellX;
			this.weatherCacheCellZ = cellZ;
			this.rebuildWeatherCache();
		}

		int cacheOriginX = (cellX << 4) - WEATHER_RADIUS;
		int cacheOriginZ = (cellZ << 4) - WEATHER_RADIUS;

		// Prepare the tessellator & GL state.
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);

		// Animation phase in ticks, used by the snow drift.
		float framePhase = (float)rendererUpdateCount + renderPartialTicks;

		// The weather layer is drawn as one batch per texture: two full grid passes
		// (rain columns, then snow columns) instead of switching textures mid-pass.
		// A column's droplets only depend on its own deterministic seed, so splitting
		// the passes does not change any column's appearance.
		boolean rainBatchOpen = false;
		boolean snowBatchOpen = false;

		// -- Rain pass --
		for(int x = playerX - WEATHER_RADIUS; x <= playerX + WEATHER_RADIUS; ++x) {
			for(int z = playerZ - WEATHER_RADIUS; z <= playerZ + WEATHER_RADIUS; ++z) {
				int index = (z - cacheOriginZ) * WEATHER_CACHE_SIZE + (x - cacheOriginX);
				BiomeGenBase biome = this.weatherColumnBiome[index];

				// The weather strength/season check is cheap; only the terrain scan was cached.
				int particleType = Weather.particleDecide(biome, world);
				if(particleType != Weather.RAIN) continue;

				int y = this.weatherTopSolidY[index];
				if(y < 0) {
					y = 0;
				}

				int y1 = playerY - WEATHER_RADIUS;
				int y2 = playerY + WEATHER_RADIUS;
				if(y1 < y) {
					y1 = y;
				}

				if(y2 < y) {
					y2 = y;
				}

				// No vertical span to draw (e.g. deep underground): skip the column.
				if(y1 == y2) continue;

				if(!rainBatchOpen) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/environment/rain.png"));
					tessellator.startDrawingQuads();
					tessellator.setTranslation(-interpolatedX, -interpolatedY, -interpolatedZ);
					rainBatchOpen = true;
				}

				// Deterministic per-column seed: the same droplets fall every frame.
				this.random.setSeed(x * x * 3121 + x * 0x2b24abb ^ z * z * 0x66397 + z * 13761);
				// Vertical scroll phase of this column's streaks, slowed by its height hash.
				float fallPhase = (((float)(rendererUpdateCount + x * x * 3121 + x * 0x2b24abb + z * z * 0x66397 + z * 13761 & 0x1f) + renderPartialTicks) / 32F) * (3F + this.random.nextFloat());
				// Tilt direction for this column from the pre-computed direction LUT.
				int dirIndex = ((z - playerZ) + 16) * 32 + ((x - playerX) + 16);
				float distanceX = this.columnDirX[dirIndex] * 0.5F;
				float distanceZ = this.columnDirZ[dirIndex] * 0.5F;
				// Radial distance from the camera, used to fade streaks near the grid edge.
				double offsetX = (double)((float)x + 0.5F) - viewEntity.posX;
				double offsetZ = (double)((float)z + 0.5F) - viewEntity.posZ;
				float hypotenuse = MathHelper.sqrt_float((float)(offsetX * offsetX + offsetZ * offsetZ)) / (float)WEATHER_RADIUS;

				tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x, y, z, 0));
				tessellator.setColorRGBA_F(1, 1, 1, ((1.0F - hypotenuse * hypotenuse) * 0.5F + 0.5F) * rainStrength);

				this.renderWeatherColumn(x, z, y1, y2, distanceX, distanceZ, 0.0F, 1.0F, ((float)y1) / 4F + fallPhase, ((float)y2) / 4F + fallPhase);
			}
		}

		if(rainBatchOpen) {
			tessellator.draw();
		}

		// -- Snow pass --
		for(int x = playerX - WEATHER_RADIUS; x <= playerX + WEATHER_RADIUS; ++x) {
			for(int z = playerZ - WEATHER_RADIUS; z <= playerZ + WEATHER_RADIUS; ++z) {
				int index = (z - cacheOriginZ) * WEATHER_CACHE_SIZE + (x - cacheOriginX);
				BiomeGenBase biome = this.weatherColumnBiome[index];
				int particleType = Weather.particleDecide(biome, world);
				if(particleType != Weather.SNOW) continue;

				int y = this.weatherTopSolidY[index];
				if(y < 0) {
					y = 0;
				}

				int y1 = playerY - WEATHER_RADIUS;
				int y2 = playerY + WEATHER_RADIUS;
				if(y1 < y) {
					y1 = y;
				}

				if(y2 < y) {
					y2 = y;
				}

				if(y1 == y2) continue;

				if(!snowBatchOpen) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/environment/snow.png"));
					tessellator.startDrawingQuads();
					tessellator.setTranslation(-interpolatedX, -interpolatedY, -interpolatedZ);
					snowBatchOpen = true;
				}

				// Sometimes (in cold biomes during winter) rain is substituted for snow.
				if(snowStrength < rainStrength) snowStrength = rainStrength;

				this.random.setSeed(x * x * 3121 + x * 0x2b24abb ^ z * z * 0x66397 + z * 13761);
				// Slow vertical scroll plus two pseudo-random horizontal drifts for this column.
				float snowScroll = ((float)(rendererUpdateCount & 0x1ff) + renderPartialTicks) / 512F;
				float driftU = this.random.nextFloat() + framePhase * 0.01F * (float)this.random.nextGaussian();
				float driftV = this.random.nextFloat() + framePhase * (float)this.random.nextGaussian() * 0.001F;
				int dirIndex = ((z - playerZ) + 16) * 32 + ((x - playerX) + 16);
				float distanceX = this.columnDirX[dirIndex] * 0.5F;
				float distanceZ = this.columnDirZ[dirIndex] * 0.5F;
				double offsetX = (double)((float)x + 0.5F) - viewEntity.posX;
				double offsetZ = (double)((float)z + 0.5F) - viewEntity.posZ;
				float hypotenuse = MathHelper.sqrt_float((float)(offsetX * offsetX + offsetZ * offsetZ)) / (float)WEATHER_RADIUS;

				tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x, y, z, 0));
				tessellator.setColorRGBA_F(1, 1, 1, ((1.0F - hypotenuse * hypotenuse) * 0.3F + 0.5F) * snowStrength);

				this.renderWeatherColumn(x, z, y1, y2, distanceX, distanceZ, 0.0F + driftU, 1.0F + driftU, ((float)y1) / 4F + snowScroll + driftV, ((float)y2) / 4F + snowScroll + driftV);
			}
		}

		if(snowBatchOpen) {
			tessellator.draw();
		}

		// Restore the translation and GL state for the rest of the frame.
		tessellator.setTranslation(0.0D, 0.0D, 0.0D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		this.disableLightmap((double)renderPartialTicks);

	}

	/** Emits one textured streak quad for a weather column at the given UVs. */
	private void renderWeatherColumn(int x, int z, int yLow, int yHigh, float distanceX, float distanceZ, float uMin, float uMax, float vMin, float vMax) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.addVertexWithUV((double)((float)x - distanceX) + 0.5D, yLow, (double)((float)z - distanceZ) + 0.5D, uMin, vMin);
		tessellator.addVertexWithUV((double)((float)x + distanceX) + 0.5D, yLow, (double)((float)z + distanceZ) + 0.5D, uMax, vMin);
		tessellator.addVertexWithUV((double)((float)x + distanceX) + 0.5D, yHigh, (double)((float)z + distanceZ) + 0.5D, uMax, vMax);
		tessellator.addVertexWithUV((double)((float)x - distanceX) + 0.5D, yHigh, (double)((float)z - distanceZ) + 0.5D, uMin, vMax);
	}

	/**
	 * Rebuffers the weather column cache for the player's current chunk cell: terrain
	 * height and biome for every column in the (cell + radius) window.
	 */
	private void rebuildWeatherCache() {
		if(this.weatherTopSolidY == null || this.weatherTopSolidY.length != WEATHER_CACHE_SIZE * WEATHER_CACHE_SIZE) {
			this.weatherTopSolidY = new int[WEATHER_CACHE_SIZE * WEATHER_CACHE_SIZE];
		}
		if(this.weatherColumnBiome == null || this.weatherColumnBiome.length != WEATHER_CACHE_SIZE * WEATHER_CACHE_SIZE) {
			this.weatherColumnBiome = new BiomeGenBase[WEATHER_CACHE_SIZE * WEATHER_CACHE_SIZE];
		}

		World world = this.mc.theWorld;
		this.weatherCacheWorld = world;
		int cacheOriginX = (this.weatherCacheCellX << 4) - WEATHER_RADIUS;
		int cacheOriginZ = (this.weatherCacheCellZ << 4) - WEATHER_RADIUS;
		for(int z = 0; z < WEATHER_CACHE_SIZE; ++z) {
			for(int x = 0; x < WEATHER_CACHE_SIZE; ++x) {
				int cacheIndex = z * WEATHER_CACHE_SIZE + x;
				this.weatherColumnBiome[cacheIndex] = world.getBiomeGenAt(cacheOriginX + x, cacheOriginZ + z);
				this.weatherTopSolidY[cacheIndex] = world.findTopSolidBlockUsingBlockMaterial(cacheOriginX + x, cacheOriginZ + z);
			}
		}
		this.weatherCacheDirty = false;
	}

	/** Sets up an orthographic projection for 2D HUD / GUI rendering. */
	public void setupOverlayRendering() {
		ScaledResolution scaledResolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, scaledResolution.scaledWidthD, scaledResolution.scaledHeightD, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}

	/** Recomputes the fog colour from sky/water/lava/status effects for this frame. */
	private void updateFogColor(float renderPartialTicks) {
		World world = this.mc.theWorld;
		EntityLiving viewEntity = this.mc.renderViewEntity;
		// Blend the horizon/sky tint into the fog depending on the render distance.
		float fogWeight = 1.0F / (float)(4 - GameSettingsValues.renderDistance);
		fogWeight = 1.0F - (float)Math.pow((double)fogWeight, 0.25D);
		Vec3D skyColor = world.getSkyColor(this.mc.renderViewEntity, renderPartialTicks);
		float skyRed = (float)skyColor.xCoord;
		float skyGreen = (float)skyColor.yCoord;
		float skyBlue = (float)skyColor.zCoord;
		Vec3D fogColorSource = world.getFogColor(renderPartialTicks);
		this.fogColorRed = (float)fogColorSource.xCoord;
		this.fogColorGreen = (float)fogColorSource.yCoord;
		this.fogColorBlue = (float)fogColorSource.zCoord;
		this.fogColorRed += (skyRed - this.fogColorRed) * fogWeight;
		this.fogColorGreen += (skyGreen - this.fogColorGreen) * fogWeight;
		this.fogColorBlue += (skyBlue - this.fogColorBlue) * fogWeight;

		// Rain and thunder darken the fog.
		float rainStrength = world.getRainStrength(renderPartialTicks);
		float darkenAmount;
		float darkenFactor;
		if(rainStrength > 0.0F) {
			darkenAmount = 1.0F - rainStrength * 0.5F;
			darkenFactor = 1.0F - rainStrength * 0.4F;
			this.fogColorRed *= darkenAmount;
			this.fogColorGreen *= darkenAmount;
			this.fogColorBlue *= darkenFactor;
		}

		darkenAmount = world.getWeightedThunderStrength(renderPartialTicks);
		if(darkenAmount > 0.0F) {
			darkenFactor = 1.0F - darkenAmount * 0.5F;
			this.fogColorRed *= darkenFactor;
			this.fogColorGreen *= darkenFactor;
			this.fogColorBlue *= darkenFactor;
		}

		// In-cloud / underwater / lava overrides.
		if(this.cloudFog) {
			Vec3D cloudColor = world.getCloudColor(renderPartialTicks);
			this.fogColorRed = (float)cloudColor.xCoord;
			this.fogColorGreen = (float)cloudColor.yCoord;
			this.fogColorBlue = (float)cloudColor.zCoord;
		} else if(viewEntity.isInsideOfMaterial(Material.water)) {
			this.fogColorRed = 0.02F;
			this.fogColorGreen = 0.02F;
			this.fogColorBlue = 0.2F;
		} else if(viewEntity.isInsideOfMaterial(Material.lava)) {
			this.fogColorRed = 0.6F;
			this.fogColorGreen = 0.1F;
			this.fogColorBlue = 0.0F;
		}

		// Blindness darkens the fog to black as the effect wears off (ported from r1.2.5).
		double blindnessFactor = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * (double)renderPartialTicks;
		if(viewEntity.isStatusActive(Status.statusBlind)) {
			int blindDuration = viewEntity.getActiveStatusEffect(Status.statusBlind).duration;
			if(blindDuration < 20) {
				blindnessFactor *= (double)(1.0F - (float)blindDuration / 20.0F);
			} else {
				blindnessFactor = 0.0D;
			}
		}

		if(blindnessFactor < 1.0D) {
			if(blindnessFactor < 0.0D) {
				blindnessFactor = 0.0D;
			}

			blindnessFactor *= blindnessFactor;
			this.fogColorRed = (float)((double)this.fogColorRed * blindnessFactor);
			this.fogColorGreen = (float)((double)this.fogColorGreen * blindnessFactor);
			this.fogColorBlue = (float)((double)this.fogColorBlue * blindnessFactor);
		}

		// Apply the smoothed day/night background-dim factor.
		darkenFactor = this.prevFogBrightness + (this.fogBrightness - this.prevFogBrightness) * renderPartialTicks;
		this.fogColorRed *= darkenFactor;
		this.fogColorGreen *= darkenFactor;
		this.fogColorBlue *= darkenFactor;

		// Diving helmet: brighten the scene like night vision (ported from r1.5.2).
		if(((EntityPlayer)viewEntity).divingHelmetOn()) {
			float nightVisionBlend = 0.5F;

			float brightestChannel = 1.0F / this.fogColorRed;
			if(brightestChannel > 1.0F / this.fogColorGreen) brightestChannel = 1.0F / this.fogColorGreen;
			if(brightestChannel > 1.0F / this.fogColorBlue) brightestChannel = 1.0F / this.fogColorBlue;

			this.fogColorRed   = this.fogColorRed   * (1.0F - nightVisionBlend) + this.fogColorRed   * brightestChannel * nightVisionBlend;
			this.fogColorGreen = this.fogColorGreen * (1.0F - nightVisionBlend) + this.fogColorGreen * brightestChannel * nightVisionBlend;
			this.fogColorBlue  = this.fogColorBlue  * (1.0F - nightVisionBlend) + this.fogColorBlue  * brightestChannel * nightVisionBlend;
		}

		if(GameSettingsValues.anaglyph) {
			// Anaglyph: remap to luminance-weighted channels so the red/cyan split stays coherent.
			float grey = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
			float redChannel = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
			float blueChannel = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
			this.fogColorRed = grey;
			this.fogColorGreen = redChannel;
			this.fogColorBlue = blueChannel;
		}

		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
	}

	/**
	 * Configures GL fog mode, density and range.
	 *
	 * @param fogType -1 = sky-only fog, 0 = world fog, 1 = close-range world fog.
	 */
	private void setupFog(int fogType, float renderPartialTicks) {
		EntityLiving viewEntity = this.mc.renderViewEntity;

		GL11.glFog(GL11.GL_FOG_COLOR, this.fillFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
		GL11.glNormal3f(0.0F, -1.0F, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		// Blindness: short, close fog that expands as the effect wears off (ported from r1.2.5).
		if(viewEntity.isStatusActive(Status.statusBlind)) {
			float blindFogDistance = 5.0F;
			int blindDuration = viewEntity.getActiveStatusEffect(Status.statusBlind).duration;
			if(blindDuration < 20) {
				blindFogDistance = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)blindDuration / 20.0F);
			}

			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			if(fogType < 0) {
				GL11.glFogf(GL11.GL_FOG_START, 0.0F);
				GL11.glFogf(GL11.GL_FOG_END, blindFogDistance * 0.8F);
			} else {
				GL11.glFogf(GL11.GL_FOG_START, blindFogDistance * 0.25F);
				GL11.glFogf(GL11.GL_FOG_END, blindFogDistance);
			}

			if(GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(34138, 34139);
			}
		} else {

			// Dense EXP fog for clouds, water and lava.
			if(this.cloudFog) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				if(GameSettingsValues.anaglyph) {
					// (Anaglyph fog tweak reserved; density identical for both eyes.)
				}
			} else if(viewEntity.isInsideOfMaterial(Material.water) && !((EntityPlayer)viewEntity).divingHelmetOn()) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				if(GameSettingsValues.anaglyph) {
				}
			} else if(viewEntity.isInsideOfMaterial(Material.lava)) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
				if(GameSettingsValues.anaglyph) {
				}
			} else {
				// World fog: linear from a quarter of the far plane out.
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
				GL11.glFogf(GL11.GL_FOG_START, this.farPlaneDistance * 0.25F);
				GL11.glFogf(GL11.GL_FOG_END, this.farPlaneDistance);
				if(fogType < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0.0F);
					GL11.glFogf(GL11.GL_FOG_END, this.farPlaneDistance * 0.8F);
				}

				if(GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}

				if(this.mc.theWorld.worldProvider.isNether) {
					GL11.glFogf(GL11.GL_FOG_START, 0.0F);
				}
			}
		}

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
	}

	/** Packs the given RGBA values into the reusable fog-color FloatBuffer. */
	private FloatBuffer fillFogColorBuffer(float red, float green, float blue, float alpha) {
		this.fogColorBuffer.clear();
		this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
		this.fogColorBuffer.flip();
		return this.fogColorBuffer;
	}
}