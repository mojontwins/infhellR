package net.minecraft.client.model;

import net.minecraft.game.physics.Vec3D;
import net.minecraft.client.render.Tessellator;

public class TexturedQuad {
	public PositionTextureVertex[] vertexPositions;
	public int nVertices;
	private boolean invertNormal;

	public TexturedQuad(PositionTextureVertex[] vertices) {
		this.nVertices = 0;
		this.invertNormal = false;
		this.vertexPositions = vertices;
		this.nVertices = vertices.length;
	}

	public TexturedQuad(PositionTextureVertex[] vertices, int texU1, int texV1, int texU2, int texV2) {
		this(vertices, texU1, texV1, texU2, texV2, 64, 32);
	}

	public TexturedQuad(PositionTextureVertex[] vertices, int texU1, int texV1, int texU2, int texV2, float textureWidth, float textureHeight){
		this(vertices);
		float uOffset = 0.0015625F;
		float vOffset = 0.003125F;
		vertices[0] = vertices[0].setTexturePosition((float)texU2 / textureWidth - uOffset, (float)texV1 / textureHeight + vOffset);
		vertices[1] = vertices[1].setTexturePosition((float)texU1 / textureWidth + uOffset, (float)texV1 / textureHeight + vOffset);
		vertices[2] = vertices[2].setTexturePosition((float)texU1 / textureWidth + uOffset, (float)texV2 / textureHeight - vOffset);
		vertices[3] = vertices[3].setTexturePosition((float)texU2 / textureWidth - uOffset, (float)texV2 / textureHeight - vOffset);
	}

	public void flipFace() {
		PositionTextureVertex[] flipped = new PositionTextureVertex[this.vertexPositions.length];

		for(int i = 0; i < this.vertexPositions.length; ++i) {
			flipped[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
		}

		this.vertexPositions = flipped;
	}

	public void draw(Tessellator tessellator, float scale) {
		Vec3D vec3D = this.vertexPositions[1].vector3D.subtract(this.vertexPositions[0].vector3D);
		Vec3D vec3D2 = this.vertexPositions[1].vector3D.subtract(this.vertexPositions[2].vector3D);
		Vec3D normal = vec3D2.crossProduct(vec3D).normalize();
		tessellator.startDrawingQuads();
		if(this.invertNormal) {
			tessellator.setNormal(-((float)normal.xCoord), -((float)normal.yCoord), -((float)normal.zCoord));
		} else {
			tessellator.setNormal((float)normal.xCoord, (float)normal.yCoord, (float)normal.zCoord);
		}

		for(int i = 0; i < 4; ++i) {
			PositionTextureVertex vertex = this.vertexPositions[i];
			tessellator.addVertexWithUV((double)((float)vertex.vector3D.xCoord * scale), (double)((float)vertex.vector3D.yCoord * scale), (double)((float)vertex.vector3D.zCoord * scale), (double)vertex.texturePositionX, (double)vertex.texturePositionY);
		}

		tessellator.draw();
	}
}