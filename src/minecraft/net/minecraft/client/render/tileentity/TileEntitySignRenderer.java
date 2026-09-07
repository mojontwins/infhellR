package net.minecraft.client.render.tileentity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.SignModel;

public class TileEntitySignRenderer extends TileEntitySpecialRenderer {
	private SignModel signModel = new SignModel();

	public void renderTileEntitySignAt(TileEntitySign tileEntitySign, double x, double y, double z, float partialTicks) {
		Block block = tileEntitySign.getBlockType();
		GL11.glPushMatrix();
		float scale = 0.6666667F;
		float yRotation;
		if(block == Block.signPost) {
			GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * scale, (float)z + 0.5F);
			yRotation = (float)(tileEntitySign.getBlockMetadata() * 360) / 16.0F;
			GL11.glRotatef(-yRotation, 0.0F, 1.0F, 0.0F);
			this.signModel.signStick.showModel = true;
		} else {
			int blockMeta = tileEntitySign.getBlockMetadata();
			yRotation = 0.0F;
			if(blockMeta == 2) {
				yRotation = 180.0F;
			}

			if(blockMeta == 4) {
				yRotation = 90.0F;
			}

			if(blockMeta == 5) {
				yRotation = -90.0F;
			}

			GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * scale, (float)z + 0.5F);
			GL11.glRotatef(-yRotation, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
			this.signModel.signStick.showModel = false;
		}

		this.bindTextureByName("/item/sign.png");
		GL11.glPushMatrix();
		GL11.glScalef(scale, -scale, -scale);
		this.signModel.func_887_a();
		GL11.glPopMatrix();
		FontRenderer fontRenderer = this.getFontRenderer();
		float textScale = 0.016666668F * scale;
		GL11.glTranslatef(0.0F, 0.5F * scale, 0.07F * scale);
		GL11.glScalef(textScale, -textScale, textScale);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F * textScale);
		GL11.glDepthMask(false);
		int color = 0;

		for(int i = 0; i < tileEntitySign.signText.length; ++i) {
			String text = tileEntitySign.signText[i];
			if(i == tileEntitySign.lineBeingEdited) {
				text = "> " + text + " <";
				fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, i * 10 - tileEntitySign.signText.length * 5, color);
			} else {
				fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, i * 10 - tileEntitySign.signText.length * 5, color);
			}
		}

		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		this.renderTileEntitySignAt((TileEntitySign)tileEntity, x, y, z, partialTicks);
	}
}
