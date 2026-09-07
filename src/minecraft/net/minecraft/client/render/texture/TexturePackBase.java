package net.minecraft.client.render.texture;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;

public abstract class TexturePackBase {
	public String texturePackFileName;
	public String firstDescriptionLine;
	public String secondDescriptionLine;
	public String texturePackHash;

	public void readZipFile() {
	}

	public void closeTexturePackFile() {
	}

	public void readTexturePackInfo(Minecraft minecraft) throws IOException {
	}

	public void closeTexturePack(Minecraft minecraft) {
	}

	public void bindThumbnailTexture(Minecraft minecraft) {
	}

	public InputStream getResourceAsStream(String resourcePath) {
		return TexturePackBase.class.getResourceAsStream(resourcePath);
	}
}
