package net.minecraft.client.render.entity;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.item.ItemArmor;

public class RenderPlayer extends RenderLiving {
	private ModelBiped modelBipedMain = (ModelBiped)this.mainModel;
	private ModelBiped modelArmorChestplate = new ModelBiped(1.0F);
	private ModelBiped modelArmor = new ModelBiped(0.5F);
	private static final String[] armorFilenamePrefix = new String[]{"cloth", "chain", "iron", "diamond", "gold", "pirate", "rags"};

	public RenderPlayer() {
		super(new ModelBiped(0.0F), 0.5F);
	}

	protected boolean setArmorModel(EntityPlayer player, int pass, float partialTicks) {
		if(pass < 4) {
			ItemStack armorStack = player.inventory.armorItemInSlot(3 - pass);
			if(armorStack != null) {
				Item item = armorStack.getItem();
				if(item instanceof ItemArmor) {
					ItemArmor armor = (ItemArmor)item;
					this.loadTexture("/armor/" + armorFilenamePrefix[armor.renderIndex] + "_" + (pass == 2 ? 2 : 1) + ".png");

					ModelBiped armorModel = pass == 2 ? this.modelArmor : this.modelArmorChestplate;

					armorModel.bipedHead.showModel = pass == 0;
					armorModel.bipedHeadwear.showModel = pass == 0;
					armorModel.bipedBody.showModel = pass == 1 || pass == 2;
					armorModel.bipedRightArm.showModel = pass == 1;
					armorModel.bipedLeftArm.showModel = pass == 1;
					armorModel.bipedRightLeg.showModel = pass == 2 || pass == 3;
					armorModel.bipedLeftLeg.showModel = pass == 2 || pass == 3;
					this.setRenderPassModel(armorModel);

					return true;
				}
			}
		}

		return false;
	}

	public void renderPlayer(EntityPlayer player, double x, double y, double z, float yaw, float partialTicks) {
		ItemStack heldItem = player.inventory.getCurrentItem();
		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = heldItem != null;
		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = player.isSneaking();
		double yOffset = y - (double)player.yOffset;
		if(player.isSneaking() && !(player instanceof EntityPlayerSP)) {
			yOffset -= 0.125D;
		}

		super.doRenderLiving(player, x, yOffset, z, yaw, partialTicks);
		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = false;
	}

	protected void renderName(EntityPlayer player, double x, double y, double z) {
		if(Minecraft.isGuiEnabled() && player != this.renderManager.livingPlayer) {
			float textScale = 1.6F;
			float inverseTextScale = 0.016666668F * textScale;
			float distance = player.getDistanceToEntity(this.renderManager.livingPlayer);
			float maxDist = player.isSneaking() ? 32.0F : 64.0F;
			if(distance < maxDist) {
				String username = player.username;
				if(!player.isSneaking()) {
					if(player.isPlayerSleeping()) {
						this.renderLivingLabel(player, username, x, y - 1.5D, z, 64);
					} else {
						this.renderLivingLabel(player, username, x, y, z, 64);
					}
				} else {
					FontRenderer fontRenderer = this.getFontRendererFromRenderManager();
					GL11.glPushMatrix();
					GL11.glTranslatef((float)x + 0.0F, (float)y + 2.3F, (float)z);
					GL11.glNormal3f(0.0F, 1.0F, 0.0F);
					GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
					GL11.glScalef(-inverseTextScale, -inverseTextScale, inverseTextScale);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glTranslatef(0.0F, 0.25F / inverseTextScale, 0.0F);
					GL11.glDepthMask(false);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					Tessellator tessellator = Tessellator.instance;
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					tessellator.startDrawingQuads();
					int textWidth = fontRenderer.getStringWidth(username) / 2;
					tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
					tessellator.addVertex((double)(-textWidth - 1), -1.0D, 0.0D);
					tessellator.addVertex((double)(-textWidth - 1), 8.0D, 0.0D);
					tessellator.addVertex((double)(textWidth + 1), 8.0D, 0.0D);
					tessellator.addVertex((double)(textWidth + 1), -1.0D, 0.0D);
					tessellator.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glDepthMask(true);
					fontRenderer.drawString(username, -fontRenderer.getStringWidth(username) / 2, 0, 553648127);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glPopMatrix();
				}
			}
		}

	}

	protected void renderSpecials(EntityPlayer player, float partialTicks) {
		ItemStack helmet = player.inventory.armorItemInSlot(3);
		if(helmet != null && helmet.getItem().shiftedIndex < 256) {
			GL11.glPushMatrix();
			this.modelBipedMain.bipedHead.postRender(0.0625F);
			if(RenderBlocks.renderItemIn3d(Block.blocksList[helmet.itemID].getRenderType())) {
				float scale = 0.625F;
				GL11.glTranslatef(0.0F, -0.25F, 0.0F);
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
			}

			this.renderManager.itemRenderer.renderItem(player, helmet);
			GL11.glPopMatrix();
		}

		float scale;
		if(player.username.equals("deadmau5") && this.loadDownloadableImageTexture(player.skinUrl, null)) {
			for(int i = 0; i < 2; ++i) {
				scale = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks - (player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks);
				float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
				GL11.glPushMatrix();
				GL11.glRotatef(scale, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
				GL11.glTranslatef(0.0F, -0.375F, 0.0F);
				GL11.glRotatef(-pitch, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-scale, 0.0F, 1.0F, 0.0F);
				float earScale = 1.3333334F;
				GL11.glScalef(earScale, earScale, earScale);
				this.modelBipedMain.renderEars(0.0625F);
				GL11.glPopMatrix();
			}
		}

		if(this.loadDownloadableImageTexture(player.playerClNORMALUrl, null)) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 0.0F, 0.125F);
			double dx = player.altPrevPosX + (player.altPosX - player.altPrevPosX) * (double)partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks);
			double dy = player.altPrevPosY + (player.altPosY - player.altPrevPosY) * (double)partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks);
			double dz = player.altPrevPosZ + (player.prevPosZ - player.altPrevPosZ) * (double)partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks);
			float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
			double sinYaw = (double)MathHelper.sin(yaw * (float)Math.PI / 180.0F);
			double cosYaw = (double)(-MathHelper.cos(yaw * (float)Math.PI / 180.0F));
			float bobY = (float)dy * 10.0F;
			if(bobY < -6.0F) {
				bobY = -6.0F;
			}

			if(bobY > 32.0F) {
				bobY = 32.0F;
			}

			float bobX = (float)(dx * sinYaw + dz * cosYaw) * 100.0F;
			float bobZ = (float)(dx * cosYaw - dz * sinYaw) * 100.0F;
			if(bobX < 0.0F) {
				bobX = 0.0F;
			}

			float headTilt = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
			bobY += MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * headTilt;
			if(player.isSneaking()) {
				bobY += 25.0F;
			}

			GL11.glRotatef(6.0F + bobX / 2.0F + bobY, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(bobZ / 2.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(-bobZ / 2.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			this.modelBipedMain.renderCloak(0.0625F);
			GL11.glPopMatrix();
		}

		ItemStack heldItem = player.inventory.getCurrentItem();
		if(heldItem != null) {
			GL11.glPushMatrix();
			this.modelBipedMain.bipedRightArm.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
			if(player.fishEntity != null) {
				heldItem = new ItemStack(Item.stick);
			}

			if(heldItem.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[heldItem.itemID].getRenderType())) {
				scale = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				scale *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
			} else if(Item.itemsList[heldItem.itemID].isFull3D()) {
				scale = 0.625F;
				if(Item.itemsList[heldItem.itemID].shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}

				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				scale = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(scale, scale, scale);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			this.renderManager.itemRenderer.renderItem(player, heldItem);
			GL11.glPopMatrix();
		}

	}

	protected void renderPlayerScale(EntityPlayer player, float partialTicks) {
		float scale = 0.9375F;
		GL11.glScalef(scale, scale, scale);
	}

	public void drawFirstPersonHand() {
		this.modelBipedMain.swingProgress = 0.0F;
		this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		this.modelBipedMain.bipedRightArm.render(0.0625F);
	}

	protected void renderPlayerSleep(EntityPlayer player, double x, double y, double z) {
		if(player.isEntityAlive() && player.isPlayerSleeping()) {
			super.renderLivingAt(player, x + (double)player.bedAdjustPosX, y + (double)player.bedAdjustPosY, z + (double)player.bedAdjustPosZ);
		} else {
			super.renderLivingAt(player, x, y, z);
		}

	}

	protected void rotatePlayer(EntityPlayer player, float limbSwingTime, float renderYawOffset, float partialTicks) {
		if(player.isEntityAlive() && player.isPlayerSleeping()) {
			GL11.glRotatef(player.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(this.getDeathMaxRotation(player), 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
		} else {
			super.rotateCorpse(player, limbSwingTime, renderYawOffset, partialTicks);
		}

	}

	protected void passSpecialRender(EntityLiving entity, double x, double y, double z) {
		this.renderName((EntityPlayer)entity, x, y, z);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.renderPlayerScale((EntityPlayer)entity, partialTicks);
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.setArmorModel((EntityPlayer)entity, pass, partialTicks);
	}

	protected void renderEquippedItems(EntityLiving entity, float partialTicks) {
		this.renderSpecials((EntityPlayer)entity, partialTicks);
	}

	protected void rotateCorpse(EntityLiving entity, float limbSwingTime, float renderYawOffset, float partialTicks) {
		this.rotatePlayer((EntityPlayer)entity, limbSwingTime, renderYawOffset, partialTicks);
	}

	protected void renderLivingAt(EntityLiving entity, double x, double y, double z) {
		this.renderPlayerSleep((EntityPlayer)entity, x, y, z);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderPlayer((EntityPlayer)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderPlayer((EntityPlayer)entity, x, y, z, yaw, partialTicks);
	}
}
