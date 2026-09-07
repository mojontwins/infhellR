package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

public abstract class Render {
	protected RenderManager renderManager;
	private ModelBase modelBase = new ModelBiped();
	protected RenderBlocks renderBlocks = new RenderBlocks();
	protected float shadowSize = 0.0F;
	protected float shadowOpaque = 1.0F;

	public abstract void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks);

	protected void loadTexture(String texturePath) {
		RenderEngine renderEngine = this.renderManager.renderEngine;
		renderEngine.bindTexture(renderEngine.getTexture(texturePath));
	}

	protected boolean loadDownloadableImageTexture(String url, String textureName) {
		RenderEngine renderEngine = this.renderManager.renderEngine;
		int textureId = renderEngine.getTextureForDownloadableImage(url, textureName);
		if(textureId >= 0) {
			renderEngine.bindTexture(textureId);
			return true;
		} else {
			return false;
		}
	}

	private void renderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks) {
		GL11.glDisable(GL11.GL_LIGHTING);
		int fireTexIndex = Block.fire.blockIndexInTexture;
		AtlasTexel.calc(fireTexIndex, TextureAtlas.TERRAIN);
		int fireX = AtlasTexel.u;
		int fireY = AtlasTexel.v;
		float texU = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX);
		float texU2 = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX + TextureAtlas.TERRAIN.tileSpan);
		float texV = TexelScale.v(TextureAtlas.TERRAIN, (float)fireY);
		float texV2 = TexelScale.v(TextureAtlas.TERRAIN, (float)fireY + TextureAtlas.TERRAIN.tileSpan);

		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		float scale = entity.width * 1.4F;
		GL11.glScalef(scale, scale, scale);
		this.loadTexture("/terrain.png");

		Tessellator tessellator = Tessellator.instance;
		float width = 0.5F;
		float zOffset = 0.0F;
		float height = entity.height / scale;
		float yBase = (float)(entity.posY - entity.boundingBox.minY);

		GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(0.0F, 0.0F, -0.3F + (float)((int)height) * 0.02F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		float depth = 0.0F;
		int layer = 0;
		tessellator.startDrawingQuads();

		while(height > 0.0F) {
			if(layer % 2 == 0) {
				texU = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX);
				texU2 = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX + TextureAtlas.TERRAIN.tileSpan);
				texV = TexelScale.v(TextureAtlas.TERRAIN, (float)fireY);
				texV2 = TexelScale.v(TextureAtlas.TERRAIN, (float)fireY + TextureAtlas.TERRAIN.tileSpan);
			} else {
				texU = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX);
				texU2 = TexelScale.u(TextureAtlas.TERRAIN, (float)fireX + TextureAtlas.TERRAIN.tileSpan);
				texV = TexelScale.v(TextureAtlas.TERRAIN, (float)(fireY + TextureAtlas.TILE));
				texV2 = TexelScale.v(TextureAtlas.TERRAIN, (float)(fireY + TextureAtlas.TILE) + TextureAtlas.TERRAIN.tileSpan);
			}

			if(layer / 2 % 2 == 0) {
				float swap = texU2;
				texU2 = texU;
				texU = swap;
			}

			tessellator.addVertexWithUV((double)(width - zOffset), (double)(0.0F - yBase), (double)depth, (double)texU2, (double)texV2);
			tessellator.addVertexWithUV((double)(-width - zOffset), (double)(0.0F - yBase), (double)depth, (double)texU, (double)texV2);
			tessellator.addVertexWithUV((double)(-width - zOffset), (double)(1.4F - yBase), (double)depth, (double)texU, (double)texV);
			tessellator.addVertexWithUV((double)(width - zOffset), (double)(1.4F - yBase), (double)depth, (double)texU2, (double)texV);
			height -= 0.45F;
			yBase -= 0.45F;
			width *= 0.9F;
			depth += 0.03F;
			++layer;
		}

		tessellator.draw();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	private void renderShadow(Entity entity, double x, double y, double z, float shadowAlpha, float partialTicks) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderEngine renderEngine = this.renderManager.renderEngine;
		renderEngine.bindTexture(renderEngine.getTexture("%clamp%/misc/shadow.png"));
		World world = this.getWorldFromRenderManager();
		GL11.glDepthMask(false);
		float shadowRadius = this.shadowSize;
		double prevX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double prevY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks + (double)entity.getShadowSize();
		double prevZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		int minBlockX = MathHelper.floor_double(prevX - (double)shadowRadius);
		int maxBlockX = MathHelper.floor_double(prevX + (double)shadowRadius);
		int minBlockY = MathHelper.floor_double(prevY - (double)shadowRadius);
		int maxBlockY = MathHelper.floor_double(prevY);
		int minBlockZ = MathHelper.floor_double(prevZ - (double)shadowRadius);
		int maxBlockZ = MathHelper.floor_double(prevZ + (double)shadowRadius);
		double dx = x - prevX;
		double dy = y - prevY;
		double dz = z - prevZ;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		for(int bx = minBlockX; bx <= maxBlockX; ++bx) {
			for(int by = minBlockY; by <= maxBlockY; ++by) {
				for(int bz = minBlockZ; bz <= maxBlockZ; ++bz) {
					int blockId = world.getBlockId(bx, by - 1, bz);
					if(blockId > 0 && world.getBlockLightValue(bx, by, bz) > 3) {
						this.renderShadowOnBlock(Block.blocksList[blockId], x, y + (double)entity.getShadowSize(), z, bx, by, bz, shadowAlpha, shadowRadius, dx, dy + (double)entity.getShadowSize(), dz);
					}
				}
			}
		}

		tessellator.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
	}

	private World getWorldFromRenderManager() {
		return this.renderManager.worldObj;
	}

	private void renderShadowOnBlock(Block block, double x, double y, double z, int bx, int by, int bz, float shadowAlpha, float shadowRadius, double dx, double dy, double dz) {
		Tessellator tessellator = Tessellator.instance;
		if(block.renderAsNormalBlock()) {
			double alpha = ((double)shadowAlpha - (y - ((double)by + dy)) / 2.0D) * 0.5D * (double)this.getWorldFromRenderManager().getLightBrightness(bx, by, bz);
			if(alpha >= 0.0D) {
				if(alpha > 1.0D) {
					alpha = 1.0D;
				}

				tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, (float)alpha);
				double minX = (double)bx + block.minX + dx;
				double maxX = (double)bx + block.maxX + dx;
				double surfaceY = (double)by + block.minY + dy + 0.015625D;
				double minZ = (double)bz + block.minZ + dz;
				double maxZ = (double)bz + block.maxZ + dz;
				float u1 = (float)((x - minX) / 2.0D / (double)shadowRadius + 0.5D);
				float u2 = (float)((x - maxX) / 2.0D / (double)shadowRadius + 0.5D);
				float v1 = (float)((z - minZ) / 2.0D / (double)shadowRadius + 0.5D);
				float v2 = (float)((z - maxZ) / 2.0D / (double)shadowRadius + 0.5D);
				tessellator.addVertexWithUV(minX, surfaceY, minZ, (double)u1, (double)v1);
				tessellator.addVertexWithUV(minX, surfaceY, maxZ, (double)u1, (double)v2);
				tessellator.addVertexWithUV(maxX, surfaceY, maxZ, (double)u2, (double)v2);
				tessellator.addVertexWithUV(maxX, surfaceY, minZ, (double)u2, (double)v1);
			}
		}
	}

	public static void renderOffsetAABB(AxisAlignedBB aabb, double x, double y, double z) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tessellator = Tessellator.instance;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tessellator.startDrawingQuads();
		tessellator.setTranslation(x, y, z);
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void renderAABB(AxisAlignedBB aabb) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		tessellator.draw();
	}

	public void setRenderManager(RenderManager renderManager) {
		this.renderManager = renderManager;
	}

	public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		if((GameSettingsValues.fancyGraphics || GameSettingsValues.clearWaters) && this.shadowSize > 0.0F) {
			double distance = this.renderManager.getDistanceToCamera(entity.posX, entity.posY, entity.posZ);
			float shadowAlpha = (float)((1.0D - distance / 256.0D) * (double)this.shadowOpaque);
			if(shadowAlpha > 0.0F) {
				this.renderShadow(entity, x, y, z, shadowAlpha, partialTicks);
			}
		}

		if(entity.isBurning()) {
			this.renderEntityOnFire(entity, x, y, z, partialTicks);
		}

	}

	public FontRenderer getFontRendererFromRenderManager() {
		return this.renderManager.getFontRenderer();
	}

	public ModelBase getModelBase() {
		return modelBase;
	}

	public void setModelBase(ModelBase modelBase) {
		this.modelBase = modelBase;
	}

	public RenderBlocks getRenderBlocks() {
		return renderBlocks;
	}

	public void setRenderBlocks(RenderBlocks renderBlocks) {
		this.renderBlocks = renderBlocks;
	}
}
