package net.minecraft.client.render;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

public class RenderList {
	protected int posXMinus;
	protected int posYMinus;
	protected int posZMinus;
	protected float dXf;
	protected float dYf;
	protected float dZf;
	protected IntBuffer intBuffer = GLAllocation.createDirectIntBuffer(65536);
	protected boolean positionUpdated = false;
	protected boolean justFlipped = false;

	public void updatePosition(int i1, int i2, int i3, double d4, double d6, double d8) {
		this.positionUpdated = true;
		this.intBuffer.clear();
		this.posXMinus = i1;
		this.posYMinus = i2;
		this.posZMinus = i3;
		this.dXf = (float)d4;
		this.dYf = (float)d6;
		this.dZf = (float)d8;
	}

	public boolean positionEquals(int i1, int i2, int i3) {
		return !this.positionUpdated ? false : i1 == this.posXMinus && i2 == this.posYMinus && i3 == this.posZMinus;
	}

	public void addCallListToIntBuffer(int i1) {
		this.intBuffer.put(i1);
		if(this.intBuffer.remaining() == 0) {
			this.flip();
		}

	}

	public void flip() {
		if(this.positionUpdated) {
			if(!this.justFlipped) {
				this.intBuffer.flip();
				this.justFlipped = true;
			}

			if(this.intBuffer.remaining() > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float)this.posXMinus - this.dXf, (float)this.posYMinus - this.dYf, (float)this.posZMinus - this.dZf);
				GL11.glCallLists(this.intBuffer);
				GL11.glPopMatrix();
			}

		}
	}

	public void func_859_b() {
		this.positionUpdated = false;
		this.justFlipped = false;
	}
}
