package net.minecraft.client.render.entity;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.entity.misc.EntityItem;

public class RenderItem extends Render {
	private RenderBlocks renderBlocks = new RenderBlocks();
	private Random random = new Random();
	public boolean colorItemFromDamage = true;

	public RenderItem() {
		this.shadowSize = 0.15F;
		this.shadowOpaque = 0.75F;
	}

	public void doRenderItem(EntityItem itemEntity, double x, double y, double z, float yaw, float partialTicks) {
		this.random.setSeed(187L);
		ItemStack itemStack = itemEntity.item;
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		float bobOffset = MathHelper.sin(((float)itemEntity.age + partialTicks) / 10.0F + itemEntity.baseRenderingAngle) * 0.1F + 0.1F;
		float rotationAngle = (((float)itemEntity.age + partialTicks) / 20.0F + itemEntity.baseRenderingAngle) * 57.295776F;
		byte stackCount = 1;
		if(itemEntity.item.stackSize > 1) {
			stackCount = 2;
		}

		if(itemEntity.item.stackSize > 5) {
			stackCount = 3;
		}

		if(itemEntity.item.stackSize > 20) {
			stackCount = 4;
		}

		GL11.glTranslatef((float)x, (float)y + bobOffset, (float)z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		float scale;
		float scaleY;
		float scaleZ;
		if(itemStack.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemStack.itemID].getRenderType())) {
			GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
			this.loadTexture("/terrain.png");
			float blockScale = 0.25F;
			if(!Block.blocksList[itemStack.itemID].renderAsNormalBlock() && itemStack.itemID != Block.stairSingle.blockID && Block.blocksList[itemStack.itemID].getRenderType() != 16) {
				blockScale = 0.5F;
			}

			GL11.glScalef(blockScale, blockScale, blockScale);

			for(int i = 0; i < stackCount; ++i) {
				GL11.glPushMatrix();
				if(i > 0) {
					scale = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
					scaleY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
					scaleZ = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / blockScale;
					GL11.glTranslatef(scale, scaleY, scaleZ);
				}

				this.renderBlocks.renderBlockOnInventory(Block.blocksList[itemStack.itemID], itemStack.getItemDamage(), 1.0F);
				GL11.glPopMatrix();
			}
		} else {
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			int iconIndex = itemStack.getIconIndex();
			if(itemStack.itemID < 256) {
				this.loadTexture("/terrain.png");
			} else {
				this.loadTexture("/gui/items.png");
			}

			Tessellator tessellator = Tessellator.instance;
			TextureAtlas itemAtlas = itemStack.itemID < 256 ? TextureAtlas.TERRAIN : TextureAtlas.ITEMS;
			AtlasTexel.calc(iconIndex, itemAtlas);
			float texU1 = TexelScale.u(itemAtlas, (float)AtlasTexel.u);
			float texU2 = TexelScale.u(itemAtlas, (float)(AtlasTexel.u + TextureAtlas.TILE));
			float texV1 = TexelScale.v(itemAtlas, (float)AtlasTexel.v);
			float texV2 = TexelScale.v(itemAtlas, (float)(AtlasTexel.v + TextureAtlas.TILE));
			float width = 1.0F;
			float height = 0.5F;
			float depth = 0.25F;
			int color;
			float brightness;
			if(this.colorItemFromDamage) {
				color = Item.itemsList[itemStack.itemID].getColorFromDamage(itemStack.getItemDamage());

				float red = (float)(color >> 16 & 255) / 255.0F;
				float green = (float)(color >> 8 & 255) / 255.0F;
				float blue = (float)(color & 255) / 255.0F;
				brightness = 1.0F;

				GL11.glColor4f(red * brightness, green * brightness, blue * brightness, 1.0F);
				tessellator.setColorOpaque_F(red * brightness, green * brightness, blue * brightness);
			}

			for(int i = 0; i < stackCount; ++i) {
				GL11.glPushMatrix();
				if(i > 0) {
					scale = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
					scaleY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
					scaleZ = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
					GL11.glTranslatef(scale, scaleY, scaleZ);
				}

				GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				tessellator.startDrawingQuads();

				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV((double)(0.0F - height), (double)(0.0F - depth), 0.0D, (double)texU1, (double)texV2);
				tessellator.addVertexWithUV((double)(width - height), (double)(0.0F - depth), 0.0D, (double)texU2, (double)texV2);
				tessellator.addVertexWithUV((double)(width - height), (double)(1.0F - depth), 0.0D, (double)texU2, (double)texV1);
				tessellator.addVertexWithUV((double)(0.0F - height), (double)(1.0F - depth), 0.0D, (double)texU1, (double)texV1);
				tessellator.draw();
				GL11.glPopMatrix();
			}
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public void drawItemIntoGui(FontRenderer fontRenderer, RenderEngine renderEngine, int itemId, int damage, int iconIndex, int x, int y) {
		if(itemId < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemId].getRenderType())) {
			GL11.glDisable(GL11.GL_LIGHTING);   // add this at start of 3D block path
			renderEngine.bindTexture(renderEngine.getTexture("/terrain.png"));
			Block block = Block.blocksList[itemId];
			GL11.glPushMatrix();
			GL11.glTranslatef((float)(x - 2), (float)(y + 3), -3.0F);
			GL11.glScalef(10.0F, 10.0F, 10.0F);
			GL11.glTranslatef(1.0F, 0.5F, 1.0F);
			GL11.glScalef(1.0F, 1.0F, -1.0F);
			GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			int color = Item.itemsList[itemId].getColorFromDamage(damage);
			float red = (float)(color >> 16 & 255) / 255.0F;
			float green = (float)(color >> 8 & 255) / 255.0F;
			float blue = (float)(color & 255) / 255.0F;
			if(this.colorItemFromDamage) {
				GL11.glColor4f(red, green, blue, 1.0F);
			}

			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			this.renderBlocks.useInventoryTint = this.colorItemFromDamage;
			this.renderBlocks.renderBlockOnInventory(block, damage, 1.0F);
			this.renderBlocks.useInventoryTint = true;
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
		} else if(iconIndex >= 0) {
			GL11.glDisable(GL11.GL_LIGHTING);
			if(itemId < 256) {
				renderEngine.bindTexture(renderEngine.getTexture("/terrain.png"));
			} else {
				renderEngine.bindTexture(renderEngine.getTexture("/gui/items.png"));
			}

			int color = Item.itemsList[itemId].getColorFromDamage(damage);
			float red  = (float)(color >> 16 & 255) / 255.0F;
			float green = (float)(color >> 8 & 255) / 255.0F;
			float blue = (float)(color & 255) / 255.0F;
			if(this.colorItemFromDamage) {
				GL11.glColor4f(red, green, blue, 1.0F);
			}

			TextureAtlas iconAtlas = itemId < 256 ? TextureAtlas.TERRAIN : TextureAtlas.ITEMS;
			AtlasTexel.calc(iconIndex, iconAtlas);
			this.renderTexturedQuad(x, y, AtlasTexel.u, AtlasTexel.v, 16, 16, iconAtlas);
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public void renderItemIntoGUI(FontRenderer fr, RenderEngine re, ItemStack stack, int x, int y) {
		if(stack != null) {
			this.drawItemIntoGui(fr, re, stack.itemID, stack.getItemDamage(), stack.getIconIndex(), x, y);
		}
	}

	public void renderItemOverlayIntoGUI(FontRenderer fr, RenderEngine re, ItemStack stack, int x, int y) {
		if(stack != null) {
			if(stack.itemID < 256) {
				Block block = Block.blocksList[stack.itemID];
				String overlay;
				if(block != null && (overlay = block.inventoryOverlayString(stack.itemDamage)) != null) {
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					fr.drawStringWithShadow(overlay, x, y, 0xFFFFFF);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}

			if(stack.stackSize > 1) {
				String count = "" + stack.stackSize;
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				fr.drawStringWithShadow(count, x + 19 - 2 - fr.getStringWidth(count), y + 6 + 3, 0xFFFFFF);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}

			if(stack.isItemDamaged()) {
				int durability = (int)Math.round(13.0D - (double)stack.getItemDamageForDisplay() * 13.0D / (double)stack.getMaxDamage());
				int red = (int)Math.round(255.0D - (double)stack.getItemDamageForDisplay() * 255.0D / (double)stack.getMaxDamage());
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				Tessellator tessellator = Tessellator.instance;
				int barColor = 255 - red << 16 | red << 8;
				int barBgColor = (255 - red) / 4 << 16 | 16128;
				this.renderQuad(tessellator, x + 2, y + 13, 13, 2, 0);
				this.renderQuad(tessellator, x + 2, y + 13, 12, 1, barBgColor);
				this.renderQuad(tessellator, x + 2, y + 13, durability, 1, barColor);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}

		}
	}

	private void renderQuad(Tessellator tessellator, int x, int y, int width, int height, int color) {
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(color);
		tessellator.addVertex((double)(x + 0), (double)(y + 0), 0.0D);
		tessellator.addVertex((double)(x + 0), (double)(y + height), 0.0D);
		tessellator.addVertex((double)(x + width), (double)(y + height), 0.0D);
		tessellator.addVertex((double)(x + width), (double)(y + 0), 0.0D);
		tessellator.draw();
	}

	public void renderTexturedQuad(int x, int y, int texX, int texY, int width, int height, TextureAtlas atlas) {
		float z = 0.0F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), (double)z, (double)TexelScale.u(atlas, (float)(texX + 0)), (double)TexelScale.v(atlas, (float)(texY + height)));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + height), (double)z, (double)TexelScale.u(atlas, (float)(texX + width)), (double)TexelScale.v(atlas, (float)(texY + height)));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), (double)z, (double)TexelScale.u(atlas, (float)(texX + width)), (double)TexelScale.v(atlas, (float)(texY + 0)));
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)z, (double)TexelScale.u(atlas, (float)(texX + 0)), (double)TexelScale.v(atlas, (float)(texY + 0)));
		tessellator.draw();
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderItem((EntityItem)entity, x, y, z, yaw, partialTicks);
	}
}
