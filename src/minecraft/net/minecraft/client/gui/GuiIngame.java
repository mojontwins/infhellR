package net.minecraft.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.EntityClientPlayerMP;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.game.entity.status.Status;

import net.minecraft.game.StringTranslate;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.game.Seasons;
import net.minecraft.game.Version;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.client.render.entity.RenderItem;

public class GuiIngame extends Gui {
	private static RenderItem itemRenderer = new RenderItem();
	private List<ChatLine> chatMessageList = new ArrayList<ChatLine>();
	private Random rand = new Random();
	private Minecraft mc;
	public String field_933_a = null;
	private int updateCounter = 0;
	private String onScreenMessage = "";
	private int onScreenMessageTimeout = 0;
	private boolean fancyText = false;
	public float damageGuiPartialTime;
	float prevVignetteBrightness = 1.0F;

	public GuiIngame(Minecraft minecraft) {
		this.mc = minecraft;
	}

	public void renderGameOverlay(float partialTicks, boolean alwaysRenderHealth, int crosshairX, int crosshairY) {
		ScaledResolution scaledRes = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		int scaledWidth = scaledRes.getScaledWidth();
		int scaledHeight = scaledRes.getScaledHeight();
		FontRenderer font = this.mc.fontRenderer;
		this.mc.entityRenderer.setupOverlayRendering();
		GL11.glEnable(GL11.GL_BLEND);
		
		float brightness = this.mc.thePlayer.getEntityBrightness(partialTicks);
		
		if(Minecraft.isFancyGraphicsEnabled()) {
			this.renderVignette(brightness, scaledWidth, scaledHeight);
		}
		
		if(this.mc.thePlayer.freezeLevel > 0.0D) this.renderFreezeFrame(brightness, scaledWidth, scaledHeight);

		ItemStack helmet = this.mc.thePlayer.inventory.armorItemInSlot(3);
		if(!GameSettingsValues.thirdPersonView && helmet != null) {
			if(helmet.itemID == Block.pumpkin.blockID) {
				this.renderPumpkinBlur(scaledWidth, scaledHeight);
			} else if(helmet.itemID == Block.divingHelmet.blockID) {
				this.renderDivingHelmetBlur(scaledWidth, scaledHeight);
			}
		}

		if(!this.mc.thePlayer.isStatusActive(Status.statusDizzy)) {
			float portalTime = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;
			if(portalTime > 0.0F) {
				this.renderPortalOverlay(portalTime, scaledWidth, scaledHeight);
			}
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/gui.png"));
		InventoryPlayer inventory = this.mc.thePlayer.inventory;
		this.zLevel = -90.0F;
		this.drawTexturedModalRect(scaledWidth / 2 - 91, scaledHeight - 22, 0, 0, 182, 22);
		this.drawTexturedModalRect(scaledWidth / 2 - 91 - 1 + inventory.currentItem * 20, scaledHeight - 22 - 1, 0, 22, 24, 22);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/icons.png"));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
		this.drawTexturedModalRect(scaledWidth / 2 - 7, scaledHeight / 2 - 7, 0, 0, 16, 16);
		GL11.glDisable(GL11.GL_BLEND);
		boolean isHurtFlash = this.mc.thePlayer.hurtResistantTime / 3 % 2 == 1;
		if(this.mc.thePlayer.hurtResistantTime < 10) {
			isHurtFlash = false;
		}

		//int healthBarY;
		int armorBarX;
		int armorBarY;
		
		if (!this.mc.thePlayer.isCreative) {
			int poisonTicks = this.mc.thePlayer.isStatusActive(Status.statusPoisoned) ? 9 : 0;
			int currentHealth = this.mc.thePlayer.health;
			int prevHealth = this.mc.thePlayer.prevHealth;
			this.rand.setSeed((long)(this.updateCounter * 312871));

			if(this.mc.playerController.shouldDrawHUD()) {
				int armorValue = this.mc.thePlayer.getPlayerArmorValue();
	
				int heartX;
				for(heartX = 0; heartX < 10; ++heartX) {
					int heartY = scaledHeight - 32;
					if(armorValue > 0) {
						int armorIconX = scaledWidth / 2 + 91 - heartX * 8 - 9;
						if(heartX * 2 + 1 < armorValue) {
							this.drawTexturedModalRect(armorIconX, heartY, 34, 9, 9, 9);
						}
	
						if(heartX * 2 + 1 == armorValue) {
							this.drawTexturedModalRect(armorIconX, heartY, 25, 9, 9, 9);
						}
	
						if(heartX * 2 + 1 > armorValue) {
							this.drawTexturedModalRect(armorIconX, heartY, 16, 9, 9, 9);
						}
					}
	
					byte hurtOffset = 0;
					if(isHurtFlash) {
						hurtOffset = 1;
					}
	
					int heartCenterX = scaledWidth / 2 - 91 + heartX * 8;
					if(currentHealth <= 4) {
						heartY += this.rand.nextInt(2);
					}
	
					this.drawTexturedModalRect(heartCenterX, heartY, 16 + hurtOffset * 9, 0, 9, 9);
					if(isHurtFlash) {
						if(heartX * 2 + 1 < prevHealth) {
							this.drawTexturedModalRect(heartCenterX, heartY, 70, 0, 9, 9);
						}
	
						if(heartX * 2 + 1 == prevHealth) {
							this.drawTexturedModalRect(heartCenterX, heartY, 79, 0, 9, 9);
						}
					}
	
					if(heartX * 2 + 1 < currentHealth) {
						this.drawTexturedModalRect(heartCenterX, heartY, 52, poisonTicks, 9, 9);
					}
	
					if(heartX * 2 + 1 == currentHealth) {
						this.drawTexturedModalRect(heartCenterX, heartY, 61, poisonTicks, 9, 9);
					}
				}
	
				if(this.mc.thePlayer.isInsideOfMaterial(Material.water)) {
					int airDrain = (int)Math.ceil((double)(this.mc.thePlayer.getAir() - 2) * 10.0D / 300.0D);
					int airLeft = (int)Math.ceil((double)this.mc.thePlayer.getAir() * 10.0D / 300.0D) - airDrain;
	
					for(int i = 0; i < airDrain + airLeft; ++i) {
						if(i < airDrain) {
							this.drawTexturedModalRect(scaledWidth / 2 - 91 + i * 8, scaledHeight - 32 - 9, 16, 18, 9, 9);
						} else {
							this.drawTexturedModalRect(scaledWidth / 2 - 91 + i * 8, scaledHeight - 32 - 9, 25, 18, 9, 9);
						}
					}
				}
			}
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glPushMatrix();
		GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();

		for(armorBarX = 0; armorBarX < 9; ++armorBarX) {
			armorBarY = scaledWidth / 2 - 90 + armorBarX * 20 + 2;
			int hotbarY = scaledHeight - 16 - 3;
			this.renderInventorySlot(armorBarX, armorBarY, hotbarY, partialTicks);
		}

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		if(this.mc.thePlayer.getSleepTimer() > 0) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			int sleepTimer = this.mc.thePlayer.getSleepTimer();
			float sleepAlpha = (float)sleepTimer / 100.0F;
			if(sleepAlpha > 1.0F) {
				sleepAlpha = 1.0F - (float)(sleepTimer - 100) / 10.0F;
			}

			int sleepColor = (int)(220.0F * sleepAlpha) << 24 | 1052704;
			this.drawRect(0, 0, scaledWidth, scaledHeight, sleepColor);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		String memoryText;
		if(GameSettingsValues.showDebugInfo) {
			GL11.glPushMatrix();
			if(Minecraft.hasPaidCheckTime > 0L) {
				GL11.glTranslatef(0.0F, 32.0F, 0.0F);
			}

			font.drawStringWithShadow("Minecraft " + Version.getVersion() + " (" + this.mc.debug + ")", 2, 2, 0xFFFFFF);
			font.drawStringWithShadow(this.mc.debugInfoRenders(), 2, 12, 0xFFFFFF);
			font.drawStringWithShadow(this.mc.getEntityDebug(), 2, 22, 0xFFFFFF);
			font.drawStringWithShadow(this.mc.debugInfoEntities(), 2, 32, 0xFFFFFF);
			long maxMemory = Runtime.getRuntime().maxMemory();
			long totalMemory = Runtime.getRuntime().totalMemory();
			long freeMemory = Runtime.getRuntime().freeMemory();
			long usedMemory = totalMemory - freeMemory;
			memoryText = "Used: " + usedMemory * 100L / maxMemory + "% (" + usedMemory / 1024L / 1024L + "MB) of " + maxMemory / 1024L / 1024L + "MB";
			this.drawString(font, memoryText, scaledWidth - font.getStringWidth(memoryText) - 2, 2, 14737632);
			memoryText = "Allocated: " + totalMemory * 100L / maxMemory + "% (" + totalMemory / 1024L / 1024L + "MB)";
			this.drawString(font, memoryText, scaledWidth - font.getStringWidth(memoryText) - 2, 12, 14737632);
			
			font.drawStringWithShadow("Pos: " + (int)this.mc.thePlayer.posX + ", " + (int)this.mc.thePlayer.posY + ", " + (int)this.mc.thePlayer.posZ + " [" + (int)this.mc.thePlayer.rotationYaw + "]", 2, 42, 0xFFFFFF);
			
			float timeAdjusted = (float) (this.mc.theWorld.worldInfo.getWorldTime() % 24000);
			font.drawStringWithShadow("Time: " + this.twoDigits((int)((timeAdjusted / 1000.0F) + 6) % 24) + ":" + this.twoDigits((int)((timeAdjusted % 1000.0F) * 60 / 1000)), 2, 52, 0xFFFFFF);
			
			String seedText = "Seed: " + this.mc.theWorld.getRandomSeed();
			this.drawString(font, seedText, scaledWidth - font.getStringWidth(seedText) - 2, 22, 14737632);
			
			if (!this.mc.theWorld.isRemote) {
				String biomeText = "Biome: " + this.mc.theWorld.getBiomeGenAt((int)this.mc.thePlayer.posX, (int)this.mc.thePlayer.posZ).biomeName;
				this.drawString(font, biomeText, scaledWidth - font.getStringWidth(biomeText) - 2, 32, 14737632);
			}
			
			String seasonText = Seasons.getStringForGui() +  ", F: " + this.mc.thePlayer.freezeLevel;
			this.drawString(font, seasonText, scaledWidth - font.getStringWidth(seasonText) - 2, 42, 14737632);
						
			if(this.mc.thePlayer instanceof EntityClientPlayerMP) {
				String netText = "Net - R: " + 
						String.format("%1$07d", this.mc.getSendQueue().getNetworkManager().getNumReadPackets()) + " W: " + 
						String.format("%1$07d", this.mc.getSendQueue().getNetworkManager().getNumChunkDataPackets());
				this.drawString(font, netText, scaledWidth - font.getStringWidth(netText) - 2, 52, 14737632);
			}
				
			GL11.glPopMatrix();
		} else {
			font.drawStringWithShadow("Minecraft " + Version.getVersion(), 2, 2, 0xFFFFFF);
		}

		if(this.onScreenMessageTimeout > 0) {
			float msgFade = (float)this.onScreenMessageTimeout - partialTicks;
			int msgAlpha = (int)(msgFade * 256.0F / 20.0F);
			if(msgAlpha > 255) {
				msgAlpha = 255;
			}

			if(msgAlpha > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float)(scaledWidth / 2), (float)(scaledHeight - 48), 0.0F);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				int msgColor = 0xFFFFFF;
				if(this.fancyText) {
					msgColor = Color.HSBtoRGB(msgFade / 50.0F, 0.7F, 0.6F) & 0xFFFFFF;
				}

				font.drawString(this.onScreenMessage, -font.getStringWidth(this.onScreenMessage) / 2, -4, msgColor + (msgAlpha << 24));
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
		}

		byte chatLines = 10;
		boolean chatVisible = false;
		if(this.mc.currentScreen instanceof GuiChat) {
			chatLines = 20;
			chatVisible = true;
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, (float)(scaledHeight - 48), 0.0F);

		for(armorBarY = 0; armorBarY < this.chatMessageList.size() && armorBarY < chatLines; ++armorBarY) {
			if(((ChatLine)this.chatMessageList.get(armorBarY)).updateCounter < 200 || chatVisible) {
				double age = (double)((ChatLine)this.chatMessageList.get(armorBarY)).updateCounter / 200.0D;
				age = 1.0D - age;
				age *= 10.0D;
				if(age < 0.0D) {
					age = 0.0D;
				}

				if(age > 1.0D) {
					age = 1.0D;
				}

				age *= age;
				int chatAlpha = (int)(255.0D * age);
				if(chatVisible) {
					chatAlpha = 255;
				}

				if(chatAlpha > 0) {
					byte textX = 2;
					int textY = -armorBarY * 9;
					memoryText = ((ChatLine)this.chatMessageList.get(armorBarY)).message;
					this.drawRect(textX, textY - 1, textX + 320, textY + 8, chatAlpha / 2 << 24);
					GL11.glEnable(GL11.GL_BLEND);
					font.drawStringWithShadow(memoryText, textX, textY, 0xFFFFFF + (chatAlpha << 24));
				}
			}
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void renderPumpkinBlur(int screenWidth, int screenHeight) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("%blur%/misc/pumpkinblur.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, (double)screenHeight, -90.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, (double)screenHeight, -90.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderDivingHelmetBlur(int screenWidth, int screenHeight) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/misc/divinghelmetblur.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, (double)screenHeight, -90.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, (double)screenHeight, -90.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	private void renderVignette(float brightness, int screenWidth, int screenHeight) {
		brightness = 1.0F - brightness;
		if(brightness < 0.0F) {
			brightness = 0.0F;
		}

		if(brightness > 1.0F) {
			brightness = 1.0F;
		}

		this.prevVignetteBrightness = (float)((double)this.prevVignetteBrightness + (double)(brightness - this.prevVignetteBrightness) * 0.01D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
		GL11.glColor4f(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1.0F);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("%blur%/misc/vignette.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, (double)screenHeight, -90.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, (double)screenHeight, -90.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void renderFreezeFrame(float brightness, int screenWidth, int screenHeight) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		float alpha = (float)this.mc.thePlayer.freezeLevel / 256.0F;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha/2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/seasons/frozen.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, (double)screenHeight, -90.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, (double)screenHeight, -90.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void renderPortalOverlay(float portalTime, int screenWidth, int screenHeight) {
		if(portalTime < 1.0F) {
			portalTime *= portalTime;
			portalTime *= portalTime;
			portalTime = portalTime * 0.8F + 0.2F;
		}

		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, portalTime);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
		AtlasTexel.calc(Block.portal.blockIndexInTexture, TextureAtlas.TERRAIN);
		float u1 = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
		float v1 = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);
		float u2 = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u + 16.0F);
		float v2 = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v + 16.0F);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, (double)screenHeight, -90.0D, (double)u1, (double)v2);
		tessellator.addVertexWithUV((double)screenWidth, (double)screenHeight, -90.0D, (double)u2, (double)v2);
		tessellator.addVertexWithUV((double)screenWidth, 0.0D, -90.0D, (double)u2, (double)v1);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, (double)u1, (double)v1);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderInventorySlot(int slotIndex, int slotX, int slotY, float partialTicks) {
		ItemStack stack = this.mc.thePlayer.inventory.mainInventory[slotIndex];
		if(stack != null) {
			float animOffset = (float)stack.animationsToGo - partialTicks;
			if(animOffset > 0.0F) {
				GL11.glPushMatrix();
				float scale = 1.0F + animOffset / 5.0F;
				GL11.glTranslatef((float)(slotX + 8), (float)(slotY + 12), 0.0F);
				GL11.glScalef(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
				GL11.glTranslatef((float)(-(slotX + 8)), (float)(-(slotY + 12)), 0.0F);
			}

			itemRenderer.renderItemIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, stack, slotX, slotY);
			if(animOffset > 0.0F) {
				GL11.glPopMatrix();
			}

			itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, stack, slotX, slotY);
		}
	}

	public void updateTick() {
		if(this.onScreenMessageTimeout > 0) {
			--this.onScreenMessageTimeout;
		}

		++this.updateCounter;

		for(int i = 0; i < this.chatMessageList.size(); ++i) {
			++((ChatLine)this.chatMessageList.get(i)).updateCounter;
		}

	}

	public void clearChatMessages() {
		this.chatMessageList.clear();
	}

	public void addChatMessage(String message) {
		while(this.mc.fontRenderer.getStringWidth(message) > 320) {
			int cutIndex;
			for(cutIndex = 1; cutIndex < message.length() && this.mc.fontRenderer.getStringWidth(message.substring(0, cutIndex + 1)) <= 320; ++cutIndex) {
			}

			this.addChatMessage(message.substring(0, cutIndex));
			message = message.substring(cutIndex);
		}

		this.chatMessageList.add(0, new ChatLine(message));

		while(this.chatMessageList.size() > 50) {
			this.chatMessageList.remove(this.chatMessageList.size() - 1);
		}

	}

	public void setRecordPlayingMessage(String recordName) {
		this.onScreenMessage = "Now playing: " + recordName;
		this.onScreenMessageTimeout = 60;
		this.fancyText = true;
	}
	
	public void showString (String s) {
		this.onScreenMessage = s;
		this.onScreenMessageTimeout = 60;
		this.fancyText = true;
	}
	
	public void addChatMessageTranslate(String messageKey) {
		StringTranslate translator = StringTranslate.getInstance();
		String translated = translator.translateKey(messageKey);
		this.addChatMessage(translated);
	}
}
