package net.minecraft.client.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.game.physics.Vec3i;
import net.minecraft.game.world.chunk.ChunkCoordinates;

public class TextureCompassFX extends TextureFX {
	protected Minecraft mc;
	private int[] compassIconImageData = new int[256];
	private double currentAngle;
	private double angleDelta;

	public TextureCompassFX(Minecraft minecraft, int iconIndex) {
		super(iconIndex);
		this.mc = minecraft;
		this.tileImage = 1;

		try {
			BufferedImage itemsTexture = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
			int srcX = this.iconIndex % 16 * 16;
			int srcY = this.iconIndex / 16 * 16;
			itemsTexture.getRGB(srcX, srcY, 16, 16, this.compassIconImageData, 0, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void onTick() {
		for(int i = 0; i < 256; ++i) {
			int alpha = this.compassIconImageData[i] >> 24 & 255;
			int red = this.compassIconImageData[i] >> 16 & 255;
			int green = this.compassIconImageData[i] >> 8 & 255;
			int blue = this.compassIconImageData[i] >> 0 & 255;
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

		double targetAngle = 0.0D;
		if(this.mc.theWorld != null && this.mc.thePlayer != null) {
			ChunkCoordinates target = this.pointCompassAt();
			if(target == null) target = this.mc.theWorld.getSpawnPoint();
			double dx = (double)target.posX - this.mc.thePlayer.posX;
			double dz = (double)target.posZ - this.mc.thePlayer.posZ;
			targetAngle = (double)(this.mc.thePlayer.rotationYaw - 90.0F) * Math.PI / 180.0D - Math.atan2(dz, dx);
			if(this.mc.theWorld.worldProvider.isNether) {
				targetAngle = Math.random() * (double)(float)Math.PI * 2.0D;
			}
		}

		double rawDelta;
		for(rawDelta = targetAngle - this.currentAngle; rawDelta < -3.141592653589793D; rawDelta += Math.PI * 2D) {
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

		this.angleDelta += rawDelta * 0.1D;
		this.angleDelta *= 0.8D;
		this.currentAngle += this.angleDelta;
		double sinAngle = Math.sin(this.currentAngle);
		double cosAngle = Math.cos(this.currentAngle);

		Vec3i color1 = this.getColor1();
		Vec3i color2 = this.getColor2();

		int needleX;
		int needleY;
		int pixelIdx;
		int red;
		int green;
		int blue;
		int rTmp;
		int gTmp;
		int bTmp;
		short alpha;
		for(needleX = -4; needleX <= 4; ++needleX) {
			needleY = (int)(8.5D + cosAngle * (double)needleX * 0.3D);
			pixelIdx = (int)(7.5D - sinAngle * (double)needleX * 0.3D * 0.5D);
			int destIdx = pixelIdx * 16 + needleY;
			red = color1.r;
			green = color1.g;
			blue = color1.b;
			alpha = 255;
			if(this.anaglyphEnabled) {
				rTmp = (red * 30 + green * 59 + blue * 11) / 100;
				gTmp = (red * 30 + green * 70) / 100;
				bTmp = (red * 30 + blue * 70) / 100;
				red = rTmp;
				green = gTmp;
				blue = bTmp;
			}

			this.imageData[destIdx * 4 + 0] = (byte)red;
			this.imageData[destIdx * 4 + 1] = (byte)green;
			this.imageData[destIdx * 4 + 2] = (byte)blue;
			this.imageData[destIdx * 4 + 3] = (byte)alpha;
		}

		for(needleX = -8; needleX <= 16; ++needleX) {
			needleY = (int)(8.5D + sinAngle * (double)needleX * 0.3D);
			pixelIdx = (int)(7.5D + cosAngle * (double)needleX * 0.3D * 0.5D);
			int destIdx = pixelIdx * 16 + needleY;
			red = needleX >= 0 ? color2.r : color1.r;
			green = needleX >= 0 ? color2.g : color1.g;
			blue = needleX >= 0 ? color2.b : color1.b;
			alpha = 255;
			if(this.anaglyphEnabled) {
				rTmp = (red * 30 + green * 59 + blue * 11) / 100;
				gTmp = (red * 30 + green * 70) / 100;
				bTmp = (red * 30 + blue * 70) / 100;
				red = rTmp;
				green = gTmp;
				blue = bTmp;
			}

			this.imageData[destIdx * 4 + 0] = (byte)red;
			this.imageData[destIdx * 4 + 1] = (byte)green;
			this.imageData[destIdx * 4 + 2] = (byte)blue;
			this.imageData[destIdx * 4 + 3] = (byte)alpha;
		}

	}

	protected Vec3i getColor2() {
		return new Vec3i(100, 100, 100);
	}

	protected Vec3i getColor1() {
		return new Vec3i(255, 20, 20);
	}

	protected ChunkCoordinates pointCompassAt() {
		return this.mc.thePlayer.getPlayerSpawnCoordinate();
	}
}
