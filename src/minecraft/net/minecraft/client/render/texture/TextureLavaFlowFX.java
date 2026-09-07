package net.minecraft.client.render.texture;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.block.Block;

public class TextureLavaFlowFX extends TextureFX {
	protected float[] currentLavaData = new float[256];
	protected float[] nextLavaData = new float[256];
	protected float[] lavaVelocity = new float[256];
	protected float[] lavaMomentum = new float[256];
	int flowTickCounter = 0;

	public TextureLavaFlowFX() {
		super(Block.lavaMoving.blockIndexInTexture + 1);
		this.tileSize = 2;
	}

	public void onTick() {
		++this.flowTickCounter;

		int y;
		float average;
		int xOffset;
		int yOffset;
		//int sampleX;
		//int sampleY;
		int red;
		int green;
		int blue;
		for(int x = 0; x < 16; ++x) {
			for(y = 0; y < 16; ++y) {
				average = 0.0F;
				int xSin = (int)(MathHelper.sin((float)y * (float)Math.PI * 2.0F / 16.0F) * 1.2F);
				int ySin = (int)(MathHelper.sin((float)x * (float)Math.PI * 2.0F / 16.0F) * 1.2F);

				for(int nx = x - 1; nx <= x + 1; ++nx) {
					for(int ny = y - 1; ny <= y + 1; ++ny) {
						xOffset = nx + xSin & 15;
						yOffset = ny + ySin & 15;
						average += this.currentLavaData[xOffset + yOffset * 16];
					}
				}

				this.nextLavaData[x + y * 16] = average / 10.0F + (this.lavaVelocity[(x + 0 & 15) + (y + 0 & 15) * 16] + this.lavaVelocity[(x + 1 & 15) + (y + 0 & 15) * 16] + this.lavaVelocity[(x + 1 & 15) + (y + 1 & 15) * 16] + this.lavaVelocity[(x + 0 & 15) + (y + 1 & 15) * 16]) / 4.0F * 0.8F;
				this.lavaVelocity[x + y * 16] += this.lavaMomentum[x + y * 16] * 0.01F;
				if(this.lavaVelocity[x + y * 16] < 0.0F) {
					this.lavaVelocity[x + y * 16] = 0.0F;
				}

				this.lavaMomentum[x + y * 16] -= 0.06F;
				if(Math.random() < 0.005D) {
					this.lavaMomentum[x + y * 16] = 1.5F;
				}
			}
		}

		float[] tempData = this.nextLavaData;
		this.nextLavaData = this.currentLavaData;
		this.currentLavaData = tempData;

		for(y = 0; y < 256; ++y) {
			average = this.currentLavaData[y - this.flowTickCounter / 3 * 16 & 255] * 2.0F;
			if(average > 1.0F) {
				average = 1.0F;
			}

			if(average < 0.0F) {
				average = 0.0F;
			}

			red = (int)(average * 100.0F + 155.0F);
			green = (int)(average * average * 255.0F);
			blue = (int)(average * average * average * average * 128.0F);
			if(this.anaglyphEnabled) {
				xOffset = (red * 30 + green * 59 + blue * 11) / 100;
				yOffset = (red * 30 + green * 70) / 100;
				int anaglyphB = (red * 30 + blue * 70) / 100;
				red = xOffset;
				green = yOffset;
				blue = anaglyphB;
			}

			this.imageData[y * 4 + 0] = (byte)red;
			this.imageData[y * 4 + 1] = (byte)green;
			this.imageData[y * 4 + 2] = (byte)blue;
			this.imageData[y * 4 + 3] = -1;
		}

	}
}
