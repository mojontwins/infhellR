package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.EntityMeatBlock;

public class RenderEntityMeatBlock extends Render {

	public RenderEntityMeatBlock() {
	}

	@Override
	public void doRender(Entity entity1, double d2, double d4, double d6, float f8, float f9) {
		this.doRenderBlockEntity((EntityMeatBlock)entity1, d2, d4, d6, f8, f9);
	}

	private void doRenderBlockEntity(EntityMeatBlock entity1, double d2, double d4, double d6, float f8, float f9) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)d2, (float)d4, (float)d6);
		this.loadTexture("/terrain.png");
		Block block10 = Block.blocksList[entity1.blockID];
		if(block10 == null) block10 = Block.stone;
		
		// tint
		byte rb = (byte) (0xff - (entity1.meatDuration * 0x33 / EntityMeatBlock.MAXDURATION));
		GL11.glColor4b(rb, (byte)0xff, rb, (byte)0xff);
		
		this.renderBlocks.renderBlockOnInventory(block10, 0, entity1.getEntityBrightness(f9));
		
		GL11.glPopMatrix();
	}

}
