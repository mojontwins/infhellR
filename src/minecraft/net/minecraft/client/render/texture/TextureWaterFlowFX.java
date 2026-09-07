package net.minecraft.client.render.texture;

import net.minecraft.game.world.block.Block;

public class TextureWaterFlowFX extends TextureFX {
	protected float[] currentWaterData = new float[256];
	protected float[] nextWaterData = new float[256];
	protected float[] waterVelocity = new float[256];
	protected float[] waterMomentum = new float[256];
	private int flowTickCounter = 0;

	public TextureWaterFlowFX() {
		super(Block.waterMoving.blockIndexInTexture + 1);
		this.tileSize = 2;
	}

	public void onTick() {
		++this.flowTickCounter;

		int x;
		int y;
		float average;
		int red;
		int green;
		for(x = 0; x < 16; ++x) {
			for(y = 0; y < 16; ++y) {
				average = 0.0F;

				for(int neighborY = y - 2; neighborY <= y; ++neighborY) {
					red = x & 15;
					green = neighborY & 15;
					average += this.currentWaterData[red + green * 16];
				}

				this.nextWaterData[x + y * 16] = average / 3.2F + this.waterVelocity[x + y * 16] * 0.8F;
			}
		}

		for(x = 0; x < 16; ++x) {
			for(y = 0; y < 16; ++y) {
				this.waterVelocity[x + y * 16] += this.waterMomentum[x + y * 16] * 0.05F;
				if(this.waterVelocity[x + y * 16] < 0.0F) {
					this.waterVelocity[x + y * 16] = 0.0F;
				}

				this.waterMomentum[x + y * 16] -= 0.3F;
				if(Math.random() < 0.2D) {
					this.waterMomentum[x + y * 16] = 0.5F;
				}
			}
		}

		float[] tempData = this.nextWaterData;
		this.nextWaterData = this.currentWaterData;
		this.currentWaterData = tempData;

		for(y = 0; y < 256; ++y) {
			average = this.currentWaterData[y - this.flowTickCounter * 16 & 255];
			if(average > 1.0F) {
				average = 1.0F;
			}

			if(average < 0.0F) {
				average = 0.0F;
			}

			float intensity = average * average;
			red = (int)(32.0F + intensity * 32.0F);
			green = (int)(50.0F + intensity * 64.0F);
			int blue = 255;
			int alpha = (int)(146.0F + intensity * 50.0F);
			if(this.anaglyphEnabled) {
				int anaglyphR = (red * 30 + green * 59 + blue * 11) / 100;
				int anaglyphG = (red * 30 + green * 70) / 100;
				int anaglyphB = (red * 30 + blue * 70) / 100;
				red = anaglyphR;
				green = anaglyphG;
				blue = anaglyphB;
			}

			this.imageData[y * 4 + 0] = (byte)red;
			this.imageData[y * 4 + 1] = (byte)green;
			this.imageData[y * 4 + 2] = (byte)blue;
			this.imageData[y * 4 + 3] = (byte)alpha;
		}

	}
}
