package net.minecraft.client.render.texture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.game.GameSettingsValues;

public class TexturePackList {
	private List<TexturePackBase> availableTexturePacks = new ArrayList<TexturePackBase>();
	private TexturePackBase defaultTexturePack = new TexturePackDefault();
	public TexturePackBase selectedTexturePack;
	private Map<String, TexturePackBase> texturePackCache = new HashMap<String, TexturePackBase>();
	private Minecraft mc;
	private File texturePackDir;
	private String currentTexturePack;

	public TexturePackList(Minecraft minecraft, File minecraftDir) {
		this.mc = minecraft;
		this.texturePackDir = new File(minecraftDir, "texturepacks");
		if(!this.texturePackDir.exists()) {
			this.texturePackDir.mkdirs();
		}

		this.currentTexturePack = GameSettingsValues.skin;
		this.updateAvaliableTexturePacks();
		this.selectedTexturePack.readZipFile();
	}

	public boolean setTexturePack(TexturePackBase texturePack) {
		if(texturePack == this.selectedTexturePack) {
			return false;
		} else {
			this.selectedTexturePack.closeTexturePackFile();
			this.currentTexturePack = texturePack.texturePackFileName;
			this.selectedTexturePack = texturePack;
			GameSettingsValues.skin = this.currentTexturePack;
			this.mc.gameSettings.saveOptions();
			this.selectedTexturePack.readZipFile();
			return true;
		}
	}

	public void updateAvaliableTexturePacks() {
		ArrayList<TexturePackBase> newPackList = new ArrayList<TexturePackBase>();
		this.selectedTexturePack = null;
		newPackList.add(this.defaultTexturePack);
		if(this.texturePackDir.exists() && this.texturePackDir.isDirectory()) {
			File[] files = this.texturePackDir.listFiles();
			File[] fileArr = files;
			int fileCount = files.length;

			for(int i = 0; i < fileCount; ++i) {
				File file = fileArr[i];
				if(file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
					String cacheKey = file.getName() + ":" + file.length() + ":" + file.lastModified();

					try {
						if(!this.texturePackCache.containsKey(cacheKey)) {
							TexturePackCustom customPack = new TexturePackCustom(file);
							customPack.texturePackHash = cacheKey;
							this.texturePackCache.put(cacheKey, customPack);
							customPack.readTexturePackInfo(this.mc);
						}

						TexturePackBase pack = this.texturePackCache.get(cacheKey);
						if(pack.texturePackFileName.equals(this.currentTexturePack)) {
							this.selectedTexturePack = pack;
						}

						newPackList.add(pack);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		if(this.selectedTexturePack == null) {
			this.selectedTexturePack = this.defaultTexturePack;
		}

		this.availableTexturePacks.removeAll(newPackList);
		Iterator<TexturePackBase> iter = this.availableTexturePacks.iterator();

		while(iter.hasNext()) {
			TexturePackBase pack = iter.next();
			pack.closeTexturePack(this.mc);
			this.texturePackCache.remove(pack.texturePackHash);
		}

		this.availableTexturePacks = newPackList;
	}

	public List<TexturePackBase> availableTexturePacks() {
		return new ArrayList<TexturePackBase>(this.availableTexturePacks);
	}
}
