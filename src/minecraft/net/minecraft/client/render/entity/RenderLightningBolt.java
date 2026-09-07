package net.minecraft.client.render.entity;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.entity.EntityLightningBolt;

public class RenderLightningBolt extends Render {
	public void doRenderLightningBolt(EntityLightningBolt bolt, double x, double y, double z, float yaw, float partialTicks) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		double[] offsetX = new double[8];
		double[] offsetZ = new double[8];
		double accumX = 0.0D;
		double accumZ = 0.0D;
		Random random = new Random(bolt.boltVertex);

		for(int i = 7; i >= 0; --i) {
			offsetX[i] = accumX;
			offsetZ[i] = accumZ;
			accumX += (double)(random.nextInt(11) - 5);
			accumZ += (double)(random.nextInt(11) - 5);
		}

		for(int pass = 0; pass < 4; ++pass) {
			Random passRandom = new Random(bolt.boltVertex);

			for(int subPass = 0; subPass < 3; ++subPass) {
				int topSegment = 7;
				int bottomSegment = 0;
				if(subPass > 0) {
					topSegment = 7 - subPass;
				}

				if(subPass > 0) {
					bottomSegment = topSegment - 2;
				}

				double segDX = offsetX[topSegment] - accumX;
				double segDZ = offsetZ[topSegment] - accumZ;

				for(int seg = topSegment; seg >= bottomSegment; --seg) {
					double prevSegDX = segDX;
					double prevSegDZ = segDZ;
					if(subPass == 0) {
						segDX += (double)(passRandom.nextInt(11) - 5);
						segDZ += (double)(passRandom.nextInt(11) - 5);
					} else {
						segDX += (double)(passRandom.nextInt(31) - 15);
						segDZ += (double)(passRandom.nextInt(31) - 15);
					}

					tessellator.startDrawing(5);
					float brightness = 0.5F;
					tessellator.setColorRGBA_F(0.9F * brightness, 0.9F * brightness, 1.0F * brightness, 0.3F);
					double halfWidth = 0.1D + (double)pass * 0.2D;
					if(subPass == 0) {
						halfWidth *= (double)seg * 0.1D + 1.0D;
					}

					double halfWidthBottom = 0.1D + (double)pass * 0.2D;
					if(subPass == 0) {
						halfWidthBottom *= (double)(seg - 1) * 0.1D + 1.0D;
					}

					for(int side = 0; side < 5; ++side) {
						double vx1 = x + 0.5D - halfWidth;
						double vz1 = z + 0.5D - halfWidth;
						if(side == 1 || side == 2) {
							vx1 += halfWidth * 2.0D;
						}

						if(side == 2 || side == 3) {
							vz1 += halfWidth * 2.0D;
						}

						double vx2 = x + 0.5D - halfWidthBottom;
						double vz2 = z + 0.5D - halfWidthBottom;
						if(side == 1 || side == 2) {
							vx2 += halfWidthBottom * 2.0D;
						}

						if(side == 2 || side == 3) {
							vz2 += halfWidthBottom * 2.0D;
						}

						tessellator.addVertex(vx2 + segDX, y + (double)(seg * 16), vz2 + segDZ);
						tessellator.addVertex(vx1 + prevSegDX, y + (double)((seg + 1) * 16), vz1 + prevSegDZ);
					}

					tessellator.draw();
				}
			}
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderLightningBolt((EntityLightningBolt)entity, x, y, z, yaw, partialTicks);
	}
}
