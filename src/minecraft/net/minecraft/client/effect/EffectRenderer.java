package net.minecraft.client.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;

public class EffectRenderer {
	protected World worldObj;
	@SuppressWarnings("unchecked")
	private List<EntityFX>[] fxLayers = new List[4];
	private RenderEngine renderer;
	private Random rand = new Random();

	public EffectRenderer(World world, RenderEngine renderEngine) {
		if(world != null) {
			this.worldObj = world;
		}

		this.renderer = renderEngine;

		for(int layer = 0; layer < 4; ++layer) {
			this.fxLayers[layer] = new ArrayList<EntityFX>();
		}

	}

	public void addEffect(EntityFX effect) {
		int layer = effect.getFXLayer();
		if(this.fxLayers[layer].size() >= 4000) {
			this.fxLayers[layer].remove(0);
		}

		this.fxLayers[layer].add(effect);
	}

	public void updateEffects() {
		for(int layer = 0; layer < 4; ++layer) {
			for(int index = 0; index < this.fxLayers[layer].size(); ++index) {
				EntityFX effect = (EntityFX)this.fxLayers[layer].get(index);
				effect.onUpdate();
				if(effect.isDead) {
					this.fxLayers[layer].remove(index--);
				}
			}
		}

	}

	public void renderParticles(Entity entity, float partialTicks) {
		float cosYaw = MathHelper.cos(entity.rotationYaw * (float)Math.PI / 180.0F);
		float sinYaw = MathHelper.sin(entity.rotationYaw * (float)Math.PI / 180.0F);
		float cosPitchNegSinYaw = -sinYaw * MathHelper.sin(entity.rotationPitch * (float)Math.PI / 180.0F);
		float cosPitchCosYaw = cosYaw * MathHelper.sin(entity.rotationPitch * (float)Math.PI / 180.0F);
		float cosPitch = MathHelper.cos(entity.rotationPitch * (float)Math.PI / 180.0F);
		EntityFX.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		EntityFX.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		EntityFX.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

		for(int layer = 0; layer < 3; ++layer) {
			if(this.fxLayers[layer].size() != 0) {
				int textureId = 0;
				if(layer == 0) {
					textureId = this.renderer.getTexture("/particles.png");
				}

				if(layer == 1) {
					textureId = this.renderer.getTexture("/terrain.png");
				}

				if(layer == 2) {
					textureId = this.renderer.getTexture("/gui/items.png");
				}

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
				Tessellator tessellator = Tessellator.instance;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				tessellator.startDrawingQuads();

				for(int index = 0; index < this.fxLayers[layer].size(); ++index) {
					EntityFX effect = (EntityFX)this.fxLayers[layer].get(index);
					tessellator.setBrightness(effect.getBrightnessForRender(partialTicks));
					effect.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
				}

				tessellator.draw();
			}
		}

	}

	public void func_1187_b(Entity entity, float partialTicks) {
		byte layer = 3;
		if(this.fxLayers[layer].size() != 0) {
			Tessellator tessellator = Tessellator.instance;

			for(int index = 0; index < this.fxLayers[layer].size(); ++index) {
				EntityFX effect = (EntityFX)this.fxLayers[layer].get(index);
				tessellator.setBrightness(effect.getBrightnessForRender(partialTicks));
				effect.renderParticle(tessellator, partialTicks, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
			}

		}
	}

	public void clearEffects(World world) {
		this.worldObj = world;

		for(int layer = 0; layer < 4; ++layer) {
			this.fxLayers[layer].clear();
		}

	}

	public void addBlockDestroyEffects(int x, int y, int z, int blockId, int metadata) {
		if(blockId != 0) {
			Block block = Block.blocksList[blockId];
			byte gridSize = 4;

			for(int ix = 0; ix < gridSize; ++ix) {
				for(int iy = 0; iy < gridSize; ++iy) {
					for(int iz = 0; iz < gridSize; ++iz) {
						double px = (double)x + ((double)ix + 0.5D) / (double)gridSize;
						double py = (double)y + ((double)iy + 0.5D) / (double)gridSize;
						double pz = (double)z + ((double)iz + 0.5D) / (double)gridSize;
						int side = this.rand.nextInt(6);
						this.addEffect((new EntityDiggingFX(this.worldObj, px, py, pz, px - (double)x - 0.5D, py - (double)y - 0.5D, pz - (double)z - 0.5D, block, side, metadata)).func_4041_a(x, y, z));
					}
				}
			}

		}
	}

	public void addBlockHitEffects(int x, int y, int z, int side) {
		int blockId = this.worldObj.getBlockId(x, y, z);
		if(blockId != 0) {
			Block block = Block.blocksList[blockId];
			float inset = 0.1F;
			double px = (double)x + this.rand.nextDouble() * (block.maxX - block.minX - (double)(inset * 2.0F)) + (double)inset + block.minX;
			double py = (double)y + this.rand.nextDouble() * (block.maxY - block.minY - (double)(inset * 2.0F)) + (double)inset + block.minY;
			double pz = (double)z + this.rand.nextDouble() * (block.maxZ - block.minZ - (double)(inset * 2.0F)) + (double)inset + block.minZ;
			if(side == 0) {
				py = (double)y + block.minY - (double)inset;
			}

			if(side == 1) {
				py = (double)y + block.maxY + (double)inset;
			}

			if(side == 2) {
				pz = (double)z + block.minZ - (double)inset;
			}

			if(side == 3) {
				pz = (double)z + block.maxZ + (double)inset;
			}

			if(side == 4) {
				px = (double)x + block.minX - (double)inset;
			}

			if(side == 5) {
				px = (double)x + block.maxX + (double)inset;
			}

			this.addEffect((new EntityDiggingFX(this.worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, side, this.worldObj.getBlockMetadata(x, y, z))).func_4041_a(x, y, z).multiplyVelocity(0.2F).func_405_d(0.6F));
		}
	}

	public String getStatistics() {
		return "" + (this.fxLayers[0].size() + this.fxLayers[1].size() + this.fxLayers[2].size());
	}

	/**
	 * Returns the number of active particles on the given layer (0-3).
	 * Used by the weather renderer to cap how many rain drops are spawned per frame.
	 */
	public int getParticleCount(int layer) {
		return this.fxLayers[layer].size();
	}
}
