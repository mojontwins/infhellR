package net.minecraft.client.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.game.item.Item;

public class TextureWatchFX extends TextureFX {
	private Minecraft mc;
	private int[] watchIconImageData = new int[256];
	private int[] dialImageData = new int[256];
	private double lastAngle;
	private double smoothDelta;

	public TextureWatchFX(Minecraft minecraft) {
		super(Item.pocketSundial.getIconFromDamage(0));
		this.mc = minecraft;
		this.tileImage = 1;

		try {
			BufferedImage itemsTexture = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
			int srcX = this.iconIndex % 16 * 16;
			int srcY = this.iconIndex / 16 * 16;
			itemsTexture.getRGB(srcX, srcY, 16, 16, this.watchIconImageData, 0, 16);
			BufferedImage dialTexture = ImageIO.read(Minecraft.class.getResource("/misc/dial.png"));
			dialTexture.getRGB(0, 0, 16, 16, this.dialImageData, 0, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void onTick() {
		double targetAngle = 0.0D;
		if(this.mc.theWorld != null && this.mc.thePlayer != null) {
			float celestialAngle = this.mc.theWorld.getCelestialAngle(1.0F);
			targetAngle = (double)(-celestialAngle * (float)Math.PI * 2.0F);
			if(this.mc.theWorld.worldProvider.isNether) {
				targetAngle = Math.random() * (double)(float)Math.PI * 2.0D;
			}
		}

		double rawDelta;
		for(rawDelta = targetAngle - this.lastAngle; rawDelta < -3.141592653589793D; rawDelta += Math.PI * 2D) {
		}

		while(rawDelta >= Math.PI) {
			rawDelta -= Math.PI * 2D;
		}

		if(rawDelta < -1.0D) {
			rawDelta = -1.0D;
		}

		if(rawDelta > 1.0D) {
			rawDelta = 1.0D;
		}

		this.smoothDelta += rawDelta * 0.1D;
		this.smoothDelta *= 0.8D;
		this.lastAngle += this.smoothDelta;
		double sinAngle = Math.sin(this.lastAngle);
		double cosAngle = Math.cos(this.lastAngle);

		for(int i = 0; i < 256; ++i) {
			int alpha = this.watchIconImageData[i] >> 24 & 255;
			int red = this.watchIconImageData[i] >> 16 & 255;
			int green = this.watchIconImageData[i] >> 8 & 255;
			int blue = this.watchIconImageData[i] >> 0 & 255;
			if(red == blue && green == 0 && blue > 0) {
				double dx = -((double)(i % 16) / 15.0D - 0.5D);
				double dy = (double)(i / 16) / 15.0D - 0.5D;
				int origRed = red;
				int dialX = (int)((dx * cosAngle + dy * sinAngle + 0.5D) * 16.0D);
				int dialY = (int)((dy * cosAngle - dx * sinAngle + 0.5D) * 16.0D);
				int dialIdx = (dialX & 15) + (dialY & 15) * 16;
				alpha = this.dialImageData[dialIdx] >> 24 & 255;
				red = (this.dialImageData[dialIdx] >> 16 & 255) * origRed / 255;
				green = (this.dialImageData[dialIdx] >> 8 & 255) * origRed / 255;
				blue = (this.dialImageData[dialIdx] >> 0 & 255) * origRed / 255;
			}

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
