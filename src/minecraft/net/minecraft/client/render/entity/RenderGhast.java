package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelGhast;
import net.minecraft.game.entity.monster.EntityGhast;

public class RenderGhast extends RenderLiving {
	public RenderGhast() {
		super(new ModelGhast(), 0.5F);
	}

	protected void scaleGhast(EntityGhast ghast, float partialTicks) {
		float attackProgress = ((float)ghast.prevAttackCounter + (float)(ghast.attackCounter - ghast.prevAttackCounter) * partialTicks) / 20.0F;
		if(attackProgress < 0.0F) {
			attackProgress = 0.0F;
		}

		attackProgress = 1.0F / (attackProgress * attackProgress * attackProgress * attackProgress * attackProgress * 2.0F + 1.0F);
		float scaleY = (8.0F + attackProgress) / 2.0F;
		float scaleXZ = (8.0F + 1.0F / attackProgress) / 2.0F;
		GL11.glScalef(scaleXZ, scaleY, scaleXZ);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.scaleGhast((EntityGhast)entity, partialTicks);
	}
}
