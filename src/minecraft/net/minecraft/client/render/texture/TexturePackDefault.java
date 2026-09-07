package net.minecraft.client.render.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class TexturePackDefault extends TexturePackBase {
	private int textureId = -1;
	private BufferedImage texturePackThumbnail;

	public TexturePackDefault() {
		this.texturePackFileName = "Default";
		this.firstDescriptionLine = "The default look of Minecraft";

		try {
			this.texturePackThumbnail = ImageIO.read(TexturePackDefault.class.getResource("/pack.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void closeTexturePack(Minecraft minecraft) {
		if(this.texturePackThumbnail != null) {
			minecraft.renderEngine.deleteTexture(this.textureId);
		}

	}

	public void bindThumbnailTexture(Minecraft minecraft) {
		if(this.texturePackThumbnail != null && this.textureId < 0) {
			this.textureId = minecraft.renderEngine.allocateAndSetupTexture(this.texturePackThumbnail);
		}

		if(this.texturePackThumbnail != null) {
			minecraft.renderEngine.bindTexture(this.textureId);
		} else {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.renderEngine.getTexture("/gui/unknown_pack.png"));
		}

	}
}
