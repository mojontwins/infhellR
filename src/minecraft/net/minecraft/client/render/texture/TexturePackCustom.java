package net.minecraft.client.render.texture;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class TexturePackCustom extends TexturePackBase {
	private ZipFile texturePackZipFile;
	private int textureId = -1;
	private BufferedImage texturePackThumbnail;
	private File texturePackFile;

	public TexturePackCustom(File file) {
		this.texturePackFileName = file.getName();
		this.texturePackFile = file;
	}

	private String truncateString(String text) {
		if(text != null && text.length() > 34) {
			text = text.substring(0, 34);
		}

		return text;
	}

	public void readTexturePackInfo(Minecraft minecraft) throws IOException {
		ZipFile zipFile = null;
		InputStream inputStream = null;

		try {
			zipFile = new ZipFile(this.texturePackFile);

			try {
				inputStream = zipFile.getInputStream(zipFile.getEntry("pack.txt"));
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				this.firstDescriptionLine = this.truncateString(reader.readLine());
				this.secondDescriptionLine = this.truncateString(reader.readLine());
				reader.close();
				inputStream.close();
			} catch (Exception ignored) {
			}

			try {
				inputStream = zipFile.getInputStream(zipFile.getEntry("pack.png"));
				this.texturePackThumbnail = ImageIO.read(inputStream);
				inputStream.close();
			} catch (Exception ignored) {
			}

			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(inputStream != null) inputStream.close();
			} catch (Exception ignored) {
			}

			try {
				if(zipFile != null) zipFile.close();
			} catch (Exception ignored) {
			}

		}

	}

	public void closeTexturePack(Minecraft minecraft) {
		if(this.texturePackThumbnail != null) {
			minecraft.renderEngine.deleteTexture(this.textureId);
		}

		this.closeTexturePackFile();
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

	public void readZipFile() {
		try {
			this.texturePackZipFile = new ZipFile(this.texturePackFile);
		} catch (Exception e) {
		}

	}

	public void closeTexturePackFile() {
		try {
			if(this.texturePackZipFile != null) {
				this.texturePackZipFile.close();
			}
		} catch (Exception e) {
		}

		this.texturePackZipFile = null;
	}

	public InputStream getResourceAsStream(String resourcePath) {
		try {
			ZipEntry entry = this.texturePackZipFile.getEntry(resourcePath.substring(1));
			if(entry != null) {
				return this.texturePackZipFile.getInputStream(entry);
			}
		} catch (Exception e) {
		}

		return TexturePackBase.class.getResourceAsStream(resourcePath);
	}
}
