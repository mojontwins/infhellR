package net.minecraft.client.model;

import net.minecraft.client.render.Tessellator;

public class ModelBox {
	private PositionTextureVertex[] vertexPositions;
	private TexturedQuad[] quadList;
	public final float posX1;
	public final float posY1;
	public final float posZ1;
	public final float posX2;
	public final float posY2;
	public final float posZ2;
	public String name;

	public ModelBox(ModelRenderer owner, int texOffsetX, int texOffsetY, float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float expand) {
		this.posX1 = posX;
		this.posY1 = posY;
		this.posZ1 = posZ;
		this.posX2 = posX + (float)sizeX;
		this.posY2 = posY + (float)sizeY;
		this.posZ2 = posZ + (float)sizeZ;
		this.vertexPositions = new PositionTextureVertex[8];
		this.quadList = new TexturedQuad[6];
		float posX2 = posX + (float)sizeX;
		float posY2 = posY + (float)sizeY;
		float posZ2 = posZ + (float)sizeZ;
		posX -= expand;
		posY -= expand;
		posZ -= expand;
		posX2 += expand;
		posY2 += expand;
		posZ2 += expand;
		if(owner.mirror) {
			float temp = posX2;
			posX2 = posX;
			posX = temp;
		}

		PositionTextureVertex v0 = new PositionTextureVertex(posX, posY, posZ, 0.0F, 0.0F);
		PositionTextureVertex v1 = new PositionTextureVertex(posX2, posY, posZ, 0.0F, 8.0F);
		PositionTextureVertex v2 = new PositionTextureVertex(posX2, posY2, posZ, 8.0F, 8.0F);
		PositionTextureVertex v3 = new PositionTextureVertex(posX, posY2, posZ, 8.0F, 0.0F);
		PositionTextureVertex v4 = new PositionTextureVertex(posX, posY, posZ2, 0.0F, 0.0F);
		PositionTextureVertex v5 = new PositionTextureVertex(posX2, posY, posZ2, 0.0F, 8.0F);
		PositionTextureVertex v6 = new PositionTextureVertex(posX2, posY2, posZ2, 8.0F, 8.0F);
		PositionTextureVertex v7 = new PositionTextureVertex(posX, posY2, posZ2, 8.0F, 0.0F);
		this.vertexPositions[0] = v0;
		this.vertexPositions[1] = v1;
		this.vertexPositions[2] = v2;
		this.vertexPositions[3] = v3;
		this.vertexPositions[4] = v4;
		this.vertexPositions[5] = v5;
		this.vertexPositions[6] = v6;
		this.vertexPositions[7] = v7;
		this.quadList[0] = new TexturedQuad(new PositionTextureVertex[]{v5, v1, v2, v6}, texOffsetX + sizeZ + sizeX, texOffsetY + sizeZ, texOffsetX + sizeZ + sizeX + sizeZ, texOffsetY + sizeZ + sizeY, owner.textureWidth, owner.textureHeight);
		this.quadList[1] = new TexturedQuad(new PositionTextureVertex[]{v0, v4, v7, v3}, texOffsetX, texOffsetY + sizeZ, texOffsetX + sizeZ, texOffsetY + sizeZ + sizeY, owner.textureWidth, owner.textureHeight);
		this.quadList[2] = new TexturedQuad(new PositionTextureVertex[]{v5, v4, v0, v1}, texOffsetX + sizeZ, texOffsetY, texOffsetX + sizeZ + sizeX, texOffsetY + sizeZ, owner.textureWidth, owner.textureHeight);
		this.quadList[3] = new TexturedQuad(new PositionTextureVertex[]{v2, v3, v7, v6}, texOffsetX + sizeZ + sizeX, texOffsetY + sizeZ, texOffsetX + sizeZ + sizeX + sizeX, texOffsetY, owner.textureWidth, owner.textureHeight);
		this.quadList[4] = new TexturedQuad(new PositionTextureVertex[]{v1, v0, v3, v2}, texOffsetX + sizeZ, texOffsetY + sizeZ, texOffsetX + sizeZ + sizeX, texOffsetY + sizeZ + sizeY, owner.textureWidth, owner.textureHeight);
		this.quadList[5] = new TexturedQuad(new PositionTextureVertex[]{v4, v5, v6, v7}, texOffsetX + sizeZ + sizeX + sizeZ, texOffsetY + sizeZ, texOffsetX + sizeZ + sizeX + sizeZ + sizeX, texOffsetY + sizeZ + sizeY, owner.textureWidth, owner.textureHeight);
		if(owner.mirror) {
			for(int i = 0; i < this.quadList.length; ++i) {
				this.quadList[i].flipFace();
			}
		}

	}

	public void render(Tessellator tessellator, float scale) {
		for(int i = 0; i < this.quadList.length; ++i) {
			this.quadList[i].draw(tessellator, scale);
		}

	}

	public ModelBox setName(String name) {
		this.name = name;
		return this;
	}
}
