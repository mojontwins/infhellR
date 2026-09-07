package net.minecraft.client.render.texture;

import net.minecraft.game.world.block.Block;

public class TextureFlamesFX extends TextureFX {
	protected float[] currentFlameData = new float[320];
	protected float[] nextFlameData = new float[320];

	public TextureFlamesFX(int textureIndex) {
		super(Block.fire.blockIndexInTexture + textureIndex * 16);
	}

	public void onTick() {
		int row;
		float average;
		int red;
		int green;
		int blue;
		for(int x = 0; x < 16; ++x) {
			for(row = 0; row < 20; ++row) {
				int neighborCount = 18;
				average = this.currentFlameData[x + (row + 1) % 20 * 16] * (float)neighborCount;

				for(red = x - 1; red <= x + 1; ++red) {
					for(blue = row; blue <= row + 1; ++blue) {
						if(red >= 0 && blue >= 0 && red < 16 && blue < 20) {
							average += this.currentFlameData[red + blue * 16];
						}

						++neighborCount;
					}
				}

				this.nextFlameData[x + row * 16] = average / ((float)neighborCount * 1.06F);
				if(row >= 19) {
					this.nextFlameData[x + row * 16] = (float)(Math.random() * Math.random() * Math.random() * 4.0D + Math.random() * (double)0.1F + (double)0.2F);
				}
			}
		}

		float[] tempData = this.nextFlameData;
		this.nextFlameData = this.currentFlameData;
		this.currentFlameData = tempData;

		for(row = 0; row < 256; ++row) {
			float intensity = this.currentFlameData[row] * 1.8F;
			if(intensity > 1.0F) {
				intensity = 1.0F;
			}

			if(intensity < 0.0F) {
				intensity = 0.0F;
			}

			red = (int)(intensity * 155.0F + 100.0F);
			green = (int)(intensity * intensity * 255.0F);
			blue = (int)(intensity * intensity * intensity * intensity * intensity * intensity * intensity * intensity * intensity * intensity * 255.0F);
			short alpha = 255;
			if(intensity < 0.5F) {
				alpha = 0;
			}

			average = (intensity - 0.5F) * 2.0F;
			if(this.anaglyphEnabled) {
				int anaglyphR = (red * 30 + green * 59 + blue * 11) / 100;
				int anaglyphG = (red * 30 + green * 70) / 100;
				int anaglyphB = (red * 30 + blue * 70) / 100;
				red = anaglyphR;
				green = anaglyphG;
				blue = anaglyphB;
			}

			this.imageData[row * 4 + 0] = (byte)red;
			this.imageData[row * 4 + 1] = (byte)green;
			this.imageData[row * 4 + 2] = (byte)blue;
			this.imageData[row * 4 + 3] = (byte)alpha;
		}

	}
}
