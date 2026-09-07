package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.effect.Particle;

public class GuiParticle extends Gui {
	private List<Particle> particles = new ArrayList<Particle>();
	private Minecraft mc;

	public GuiParticle(Minecraft minecraft) {
		this.mc = minecraft;
	}

	public void update() {
		for(int i = 0; i < this.particles.size(); ++i) {
			Particle particle = (Particle)this.particles.get(i);
			particle.preUpdate();
			particle.update(this);
			if(particle.isDead) {
				this.particles.remove(i--);
			}
		}

	}

	public void draw(float partialTicks) {
		this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/gui/particles.png"));

		for(int i = 0; i < this.particles.size(); ++i) {
			Particle particle = (Particle)this.particles.get(i);
			int renderX = (int)(particle.prevPosX + (particle.posX - particle.prevPosX) * (double)partialTicks - 4.0D);
			int renderY = (int)(particle.prevPosY + (particle.posY - particle.prevPosY) * (double)partialTicks - 4.0D);
			float alpha = (float)(particle.prevTintAlpha + (particle.tintAlpha - particle.prevTintAlpha) * (double)partialTicks);
			float red = (float)(particle.prevTintRed + (particle.tintRed - particle.prevTintRed) * (double)partialTicks);
			float green = (float)(particle.prevTintGreen + (particle.tintGreen - particle.prevTintGreen) * (double)partialTicks);
			float blue = (float)(particle.prevTintBlue + (particle.tintBlue - particle.prevTintBlue) * (double)partialTicks);
			GL11.glColor4f(red, green, blue, alpha);
			this.drawTexturedModalRect(renderX, renderY, 40, 0, 8, 8);
		}

	}
}
