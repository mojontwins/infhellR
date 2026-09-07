package net.minecraft.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.render.texture.TextureOffset;

public abstract class ModelBase {
	public float swingProgress;
	public boolean isRiding = false;
	public List<ModelRenderer> boxList = new ArrayList<ModelRenderer>();
	public boolean isChild = true;
	private Map<String,TextureOffset> modelTextureMap = new HashMap<String,TextureOffset>();
	public int textureWidth = 64;
	public int textureHeight = 32;

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
	}

	public void setLivingAnimations(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTick) {
	}

	protected void setTextureOffset(String partName, int x, int y) {
		this.modelTextureMap.put(partName, new TextureOffset(x, y));
	}

	public TextureOffset getTextureOffset(String partName) {
		return (TextureOffset)this.modelTextureMap.get(partName);
	}
}