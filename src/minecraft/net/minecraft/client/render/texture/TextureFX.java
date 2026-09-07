package net.minecraft.client.render.texture;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.RenderEngine;

public class TextureFX {
	public byte[] imageData = new byte[1024];
	public int iconIndex;
	public boolean anaglyphEnabled = false;
	public int textureId = 0;
	public int tileSize = 1;
	public int tileImage = 0;

	public TextureFX(int iconIndex) {
		this.iconIndex = iconIndex;
	}

	public void onTick() {
	}

	public void bindImage(RenderEngine renderEngine) {
		if(this.tileImage == 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderEngine.getTexture("/terrain.png"));
		} else if(this.tileImage == 1) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderEngine.getTexture("/gui/items.png"));
		}

	}
}
