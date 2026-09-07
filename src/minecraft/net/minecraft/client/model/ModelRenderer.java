package net.minecraft.client.model;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.GLAllocation;
import net.minecraft.client.render.Tessellator;

public class ModelRenderer {
	public int textureWidth;
	public int textureHeight;
	private int textureOffsetX;
	private int textureOffsetY;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	private boolean compiled;
	private int displayList;
	public boolean mirror;
	public boolean showModel;
	public boolean isHidden;
	public List<ModelBox> cubeList;
	public List<ModelRenderer> childModels;
	public float translateX = 0.0F;
	public float translateY = 0.0F;
	public float translateZ = 0.0F;

	public ModelRenderer() {
		this.textureWidth = 64;
		this.textureHeight = 32;
		this.compiled = false;
		this.displayList = 0;
		this.mirror = false;
		this.showModel = true;
		this.isHidden = false;
		this.cubeList = new ArrayList<ModelBox>();
		this.setTextureSize(this.textureWidth, this.textureHeight);
	}

	public ModelRenderer(int texOffsetX, int texOffsetY) {
		this();
		this.setTextureOffset(texOffsetX, texOffsetY);
	}

	public void addChild(ModelRenderer child) {
		if(this.childModels == null) {
			this.childModels = new ArrayList<ModelRenderer>();
		}

		this.childModels.add(child);
	}

	public ModelRenderer setTextureOffset(int texOffsetX, int texOffsetY) {
		this.textureOffsetX = texOffsetX;
		this.textureOffsetY = texOffsetY;
		return this;
	}

	public ModelRenderer addBox(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ) {
		this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, posX, posY, posZ, sizeX, sizeY, sizeZ, 0.0F));
		return this;
		}

	public void addBox(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float expand) {
		this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, posX, posY, posZ, sizeX, sizeY, sizeZ, expand));
	}

	public void setRotationPoint(float posX, float posY, float posZ) {
		this.rotationPointX = posX;
		this.rotationPointY = posY;
		this.rotationPointZ = posZ;
	}

	public void render(float scale) {
		if(!this.isHidden) {
			if(this.showModel) {
				if(!this.compiled) {
					this.compileDisplayList(scale);
				}

				GL11.glTranslatef(this.translateX, this.translateY, this.translateZ);

				int i;
				if(this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
					if(this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
						GL11.glCallList(this.displayList);
						if(this.childModels != null) {
							for(i = 0; i < this.childModels.size(); ++i) {
								((ModelRenderer)this.childModels.get(i)).render(scale);
							}
						}
					} else {
						GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
						GL11.glCallList(this.displayList);
						if(this.childModels != null) {
							for(i = 0; i < this.childModels.size(); ++i) {
								((ModelRenderer)this.childModels.get(i)).render(scale);
							}
						}

						GL11.glTranslatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
					}
				} else {
					GL11.glPushMatrix();
					GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
					if(this.rotateAngleZ != 0.0F) {
						GL11.glRotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
					}

					if(this.rotateAngleY != 0.0F) {
						GL11.glRotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
					}

					if(this.rotateAngleX != 0.0F) {
						GL11.glRotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
					}

					GL11.glCallList(this.displayList);
					if(this.childModels != null) {
						for(i = 0; i < this.childModels.size(); ++i) {
							((ModelRenderer)this.childModels.get(i)).render(scale);
						}
					}

					GL11.glPopMatrix();
				}

				GL11.glTranslatef(-this.translateX, -this.translateY, -this.translateZ);
			}
		}
	}

	public void renderWithRotation(float scale) {
		if(!this.isHidden) {
			if(this.showModel) {
				if(!this.compiled) {
					this.compileDisplayList(scale);
				}

				GL11.glPushMatrix();
				GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
				if(this.rotateAngleY != 0.0F) {
					GL11.glRotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
				}

				if(this.rotateAngleX != 0.0F) {
					GL11.glRotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
				}

				if(this.rotateAngleZ != 0.0F) {
					GL11.glRotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
				}

				GL11.glCallList(this.displayList);
				GL11.glPopMatrix();
			}
		}
	}

	public void postRender(float scale) {
		if(!this.isHidden) {
			if(this.showModel) {
				if(!this.compiled) {
					this.compileDisplayList(scale);
				}

				if(this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
					if(this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
						GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
					}
				} else {
					GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
					if(this.rotateAngleZ != 0.0F) {
						GL11.glRotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
					}

					if(this.rotateAngleY != 0.0F) {
						GL11.glRotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
					}

					if(this.rotateAngleX != 0.0F) {
						GL11.glRotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
					}
				}

			}
		}
	}

	private void compileDisplayList(float scale) {
		this.displayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(this.displayList, GL11.GL_COMPILE);
		Tessellator tessellator = Tessellator.instance;

		for(int i = 0; i < this.cubeList.size(); ++i) {
			((ModelBox)this.cubeList.get(i)).render(tessellator, scale);
		}

		GL11.glEndList();
		this.compiled = true;
	}

	public ModelRenderer setTextureSize(int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		return this;
	}
}