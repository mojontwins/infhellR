package net.minecraft.client.render.texture;

import java.util.Random;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.block.Block;

public class TexturePortalFX extends TextureFX {
	private int portalTickCounter = 0;
	private byte[][] portalTextureData = new byte[32][1024];

	public TexturePortalFX() {
		super(Block.portal.blockIndexInTexture);
		Random random = new Random(100L);

		for(int layer = 0; layer < 32; ++layer) {
			for(int texY = 0; texY < 16; ++texY) {
				for(int texX = 0; texX < 16; ++texX) {
					float brightness = 0.0F;

					int band;
					for(band = 0; band < 2; ++band) {
						float bandY = (float)(band * 8);
						float bandX = (float)(band * 8);
						float dx = ((float)texY - bandY) / 16.0F * 2.0F;
						float dy = ((float)texX - bandX) / 16.0F * 2.0F;
						if(dx < -1.0F) {
							dx += 2.0F;
						}

						if(dx >= 1.0F) {
							dx -= 2.0F;
						}

						if(dy < -1.0F) {
							dy += 2.0F;
						}

						if(dy >= 1.0F) {
							dy -= 2.0F;
						}

						float distSq = dx * dx + dy * dy;
						float angle = (float)Math.atan2((double)dy, (double)dx) + ((float)layer / 32.0F * (float)Math.PI * 2.0F - distSq * 10.0F + (float)(band * 2)) * (float)(band * 2 - 1);
						angle = (MathHelper.sin(angle) + 1.0F) / 2.0F;
						angle /= distSq + 1.0F;
						brightness += angle * 0.5F;
					}

					brightness += random.nextFloat() * 0.1F;
					band = (int)(brightness * 100.0F + 155.0F);
					int green = (int)(brightness * brightness * 200.0F + 55.0F);
					int blue = (int)(brightness * brightness * brightness * brightness * 255.0F);
					int alpha = (int)(brightness * 100.0F + 155.0F);
					int pixelIdx = texX * 16 + texY;
					this.portalTextureData[layer][pixelIdx * 4 + 0] = (byte)green;
					this.portalTextureData[layer][pixelIdx * 4 + 1] = (byte)blue;
					this.portalTextureData[layer][pixelIdx * 4 + 2] = (byte)band;
					this.portalTextureData[layer][pixelIdx * 4 + 3] = (byte)alpha;
				}
			}
		}

	}

	public void onTick() {
		++this.portalTickCounter;
		byte[] textureLayer = this.portalTextureData[this.portalTickCounter & 31];

		for(int i = 0; i < 256; ++i) {
			int red = textureLayer[i * 4 + 0] & 255;
			int green = textureLayer[i * 4 + 1] & 255;
			int blue = textureLayer[i * 4 + 2] & 255;
			int alpha = textureLayer[i * 4 + 3] & 255;
			if(this.anaglyphEnabled) {
				int anaglyphR = (red * 30 + green * 59 + blue * 11) / 100;
				int anaglyphG = (red * 30 + green * 70) / 100;
				int anaglyphB = (red * 30 + blue * 70) / 100;
				red = anaglyphR;
				green = anaglyphG;
				blue = anaglyphB;
			}

			this.imageData[i * 4 + 0] = (byte)red;
			this.imageData[i * 4 + 1] = (byte)green;
			this.imageData[i * 4 + 2] = (byte)blue;
			this.imageData[i * 4 + 3] = (byte)alpha;
		}

	}
}
