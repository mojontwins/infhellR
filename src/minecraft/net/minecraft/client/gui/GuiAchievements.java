package net.minecraft.client.gui;

import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.Achievement;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.GameSettingsKeys;
import net.minecraft.client.StatFileWriter;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;

public class GuiAchievements extends GuiScreen {
	private static final int guiMapTop = AchievementList.minDisplayColumn * 24 - 112;
	private static final int guiMapLeft = AchievementList.minDisplayRow * 24 - 112;
	private static final int guiMapBottom = AchievementList.maxDisplayColumn * 24 - 77;
	private static final int guiMapRight = AchievementList.maxDisplayRow * 24 - 77;
	protected int achievementsPaneWidth = 256;
	protected int achievementsPaneHeight = 202;
	protected int mouseX = 0;
	protected int mouseY = 0;
	protected double field_27116_m;
	protected double field_27115_n;
	protected double guiMapX;
	protected double guiMapY;
	protected double field_27112_q;
	protected double field_27111_r;
	private int isMouseButtonDown = 0;
	private StatFileWriter statFileWriter;

	public GuiAchievements(StatFileWriter statFileWriter) {
		this.statFileWriter = statFileWriter;
		short width = 141;
		short height = 141;
		this.field_27116_m = this.guiMapX = this.field_27112_q = (double)(AchievementList.openInventory.displayColumn * 24 - width / 2 - 12);
		this.field_27115_n = this.guiMapY = this.field_27111_r = (double)(AchievementList.openInventory.displayRow * 24 - height / 2);
	}

	public void initGui() {
		this.controlList.clear();
		this.controlList.add(new GuiSmallButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, StatCollector.translateToLocal("gui.done")));
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 1) {
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		}

		super.actionPerformed(guiButton);
	}

	protected void keyTyped(char c, int keyCode) {
		if(keyCode == GameSettingsKeys.keyBindInventory.keyCode) {
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		} else {
			super.keyTyped(c, keyCode);
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(Mouse.isButtonDown(0)) {
			int paneLeft = (this.width - this.achievementsPaneWidth) / 2;
			int paneTop = (this.height - this.achievementsPaneHeight) / 2;
			int contentLeft = paneLeft + 8;
			int contentTop = paneTop + 17;
			if((this.isMouseButtonDown == 0 || this.isMouseButtonDown == 1) && mouseX >= contentLeft && mouseX < contentLeft + 224 && mouseY >= contentTop && mouseY < contentTop + 155) {
				if(this.isMouseButtonDown == 0) {
					this.isMouseButtonDown = 1;
				} else {
					this.guiMapX -= (double)(mouseX - this.mouseX);
					this.guiMapY -= (double)(mouseY - this.mouseY);
					this.field_27112_q = this.field_27116_m = this.guiMapX;
					this.field_27111_r = this.field_27115_n = this.guiMapY;
				}

				this.mouseX = mouseX;
				this.mouseY = mouseY;
			}

			if(this.field_27112_q < (double)guiMapTop) {
				this.field_27112_q = (double)guiMapTop;
			}

			if(this.field_27111_r < (double)guiMapLeft) {
				this.field_27111_r = (double)guiMapLeft;
			}

			if(this.field_27112_q >= (double)guiMapBottom) {
				this.field_27112_q = (double)(guiMapBottom - 1);
			}

			if(this.field_27111_r >= (double)guiMapRight) {
				this.field_27111_r = (double)(guiMapRight - 1);
			}
		} else {
			this.isMouseButtonDown = 0;
		}

		this.drawDefaultBackground();
		this.func_27109_b(mouseX, mouseY, partialTicks);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		this.func_27110_k();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public void updateScreen() {
		this.field_27116_m = this.guiMapX;
		this.field_27115_n = this.guiMapY;
		double deltaX = this.field_27112_q - this.guiMapX;
		double deltaY = this.field_27111_r - this.guiMapY;
		if(deltaX * deltaX + deltaY * deltaY < 4.0D) {
			this.guiMapX += deltaX;
			this.guiMapY += deltaY;
		} else {
			this.guiMapX += deltaX * 0.85D;
			this.guiMapY += deltaY * 0.85D;
		}

	}

	protected void func_27110_k() {
		int x = (this.width - this.achievementsPaneWidth) / 2;
		int y = (this.height - this.achievementsPaneHeight) / 2;
		this.fontRenderer.drawString("Achievements", x + 15, y + 5, 4210752);
	}

	protected void func_27109_b(int mouseX, int mouseY, float partialTicks) {
		int renderX = MathHelper.floor_double(this.field_27116_m + (this.guiMapX - this.field_27116_m) * (double)partialTicks);
		int renderY = MathHelper.floor_double(this.field_27115_n + (this.guiMapY - this.field_27115_n) * (double)partialTicks);
		if(renderX < guiMapTop) {
			renderX = guiMapTop;
		}

		if(renderY < guiMapLeft) {
			renderY = guiMapLeft;
		}

		if(renderX >= guiMapBottom) {
			renderX = guiMapBottom - 1;
		}

		if(renderY >= guiMapRight) {
			renderY = guiMapRight - 1;
		}

		int terrainTexture = this.mc.renderEngine.getTexture("/terrain.png");
		int achievementTexture = this.mc.renderEngine.getTexture("/achievement/bg.png");
		int paneLeft = (this.width - this.achievementsPaneWidth) / 2;
		int paneTop = (this.height - this.achievementsPaneHeight) / 2;
		int contentLeft = paneLeft + 16;
		int contentTop = paneTop + 17;
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_GEQUAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		this.mc.renderEngine.bindTexture(terrainTexture);
		int chunkX = renderX + 288 >> 4;
		int chunkY = renderY + 288 >> 4;
		int tileX = (renderX + 288) % 16;
		int tileY = (renderY + 288) % 16;
		Random random = new Random();

		for(int row = 0; row * 16 - tileY < 155; ++row) {
			float brightness = 0.6F - (float)(chunkY + row) / 25.0F * 0.3F;
			GL11.glColor4f(brightness, brightness, brightness, 1.0F);

			for(int col = 0; col * 16 - tileX < 224; ++col) {
				random.setSeed((long)(1234 + chunkX + col));
				random.nextInt();
				int blockValue = random.nextInt(1 + chunkY + row) + (chunkY + row) / 2;
				int textureIndex = Block.sand.blockIndexInTexture;
				if(blockValue <= 37 && chunkY + row != 35) {
					if(blockValue == 22) {
						if(random.nextInt(2) == 0) {
							textureIndex = Block.oreDiamond.blockIndexInTexture;
						} else {
							textureIndex = Block.oreRedstone.blockIndexInTexture;
						}
					} else if(blockValue == 10) {
						textureIndex = Block.oreIron.blockIndexInTexture;
					} else if(blockValue == 8) {
						textureIndex = Block.oreCoal.blockIndexInTexture;
					} else if(blockValue > 4) {
						textureIndex = Block.stone.blockIndexInTexture;
					} else if(blockValue > 0) {
						textureIndex = Block.dirt.blockIndexInTexture;
					}
				} else {
					textureIndex = Block.bedrock.blockIndexInTexture;
				}

				this.drawTexturedModalRect(contentLeft + col * 16 - tileX, contentTop + row * 16 - tileY, textureIndex % 16 << 4, textureIndex >> 4 << 4, 16, 16);
			}
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		int screenX;
		int screenY;
		int color;
		int pulseAlpha;
		for(chunkX = 0; chunkX < AchievementList.achievementList.size(); ++chunkX) {
			Achievement achievement = (Achievement)AchievementList.achievementList.get(chunkX);
			if(achievement.parentAchievement != null) {
				int achX = achievement.displayColumn * 24 - renderX + 11 + contentLeft;
				int achY = achievement.displayRow * 24 - renderY + 11 + contentTop;
				int parentX = achievement.parentAchievement.displayColumn * 24 - renderX + 11 + contentLeft;
				int parentY = achievement.parentAchievement.displayRow * 24 - renderY + 11 + contentTop;
				boolean isUnlocked = this.statFileWriter.hasAchievementUnlocked(achievement);
				boolean canUnlock = this.statFileWriter.canUnlockAchievement(achievement);
				pulseAlpha = Math.sin((double)(System.currentTimeMillis() % 600L) / 600.0D * Math.PI * 2.0D) > 0.6D ? 255 : 130;
				if(isUnlocked) {
					color = -9408400;
				} else if(canUnlock) {
					color = 65280 + (pulseAlpha << 24);
				} else {
					color = 0xFF000000;
				}

			this.drawVerticalAchievementConnector(achX, parentX, achY, color);
			this.drawHorizontalAchievementConnector(parentX, achY, parentY, color);
			}
		}

		Achievement hoveredAchievement = null;
		RenderItem renderItem = new RenderItem();
		GL11.glPushMatrix();
		GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		int nameplateX;
		int nameplateY;
		for(chunkY = 0; chunkY < AchievementList.achievementList.size(); ++chunkY) {
			Achievement achievement = (Achievement)AchievementList.achievementList.get(chunkY);
			screenX = achievement.displayColumn * 24 - renderX;
			screenY = achievement.displayRow * 24 - renderY;
			if(screenX >= -24 && screenY >= -24 && screenX <= 224 && screenY <= 155) {
				float alpha;
				if(this.statFileWriter.hasAchievementUnlocked(achievement)) {
					alpha = 1.0F;
					GL11.glColor4f(alpha, alpha, alpha, 1.0F);
				} else if(this.statFileWriter.canUnlockAchievement(achievement)) {
					alpha = Math.sin((double)(System.currentTimeMillis() % 600L) / 600.0D * Math.PI * 2.0D) < 0.6D ? 0.6F : 0.8F;
					GL11.glColor4f(alpha, alpha, alpha, 1.0F);
				} else {
					alpha = 0.3F;
					GL11.glColor4f(alpha, alpha, alpha, 1.0F);
				}

				this.mc.renderEngine.bindTexture(achievementTexture);
				nameplateX = contentLeft + screenX;
				nameplateY = contentTop + screenY;
				if(achievement.getSpecial()) {
					this.drawTexturedModalRect(nameplateX - 2, nameplateY - 2, 26, 202, 26, 26);
				} else {
					this.drawTexturedModalRect(nameplateX - 2, nameplateY - 2, 0, 202, 26, 26);
				}

				if(!this.statFileWriter.canUnlockAchievement(achievement)) {
					float darken = 0.1F;
					GL11.glColor4f(darken, darken, darken, 1.0F);
					renderItem.colorItemFromDamage = false;
				}

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_CULL_FACE);
				renderItem.renderItemIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, achievement.theItemStack, nameplateX + 3, nameplateY + 3);
				GL11.glDisable(GL11.GL_LIGHTING);
				if(!this.statFileWriter.canUnlockAchievement(achievement)) {
					renderItem.colorItemFromDamage = true;
				}

				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				if(mouseX >= contentLeft && mouseY >= contentTop && mouseX < contentLeft + 224 && mouseY < contentTop + 155 && mouseX >= nameplateX && mouseX <= nameplateX + 22 && mouseY >= nameplateY && mouseY <= nameplateY + 22) {
					hoveredAchievement = achievement;
				}
			}
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(achievementTexture);
		this.drawTexturedModalRect(paneLeft, paneTop, 0, 0, this.achievementsPaneWidth, this.achievementsPaneHeight);
		GL11.glPopMatrix();
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if(hoveredAchievement != null) {
			String statName = hoveredAchievement.statName;
			String description = hoveredAchievement.getDescription();
			int tooltipX = mouseX + 12;
			int tooltipY = mouseY - 4;
			if(this.statFileWriter.canUnlockAchievement(hoveredAchievement)) {
				int maxWidth = Math.max(this.fontRenderer.getStringWidth(statName), 120);
				int descHeight = this.fontRenderer.func_27277_a(description, maxWidth);
				if(this.statFileWriter.hasAchievementUnlocked(hoveredAchievement)) {
					descHeight += 12;
				}

				this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY + descHeight + 3 + 12, -1073741824, -1073741824);
				this.fontRenderer.func_27278_a(description, tooltipX, tooltipY + 12, maxWidth, -6250336);
				if(this.statFileWriter.hasAchievementUnlocked(hoveredAchievement)) {
					this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("achievement.taken"), tooltipX, tooltipY + descHeight + 4, -7302913);
				}
			} else {
				int maxWidth = Math.max(this.fontRenderer.getStringWidth(statName), 120);
				String requires = StatCollector.translateToLocalFormatted("achievement.requires", new Object[]{hoveredAchievement.parentAchievement.statName});
				int descHeight = this.fontRenderer.func_27277_a(requires, maxWidth);
				this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY + descHeight + 12 + 3, -1073741824, -1073741824);
				this.fontRenderer.func_27278_a(requires, tooltipX, tooltipY + 12, maxWidth, -9416624);
			}

			this.fontRenderer.drawStringWithShadow(statName, tooltipX, tooltipY, this.statFileWriter.canUnlockAchievement(hoveredAchievement) ? (hoveredAchievement.getSpecial() ? -128 : -1) : (hoveredAchievement.getSpecial() ? -8355776 : -8355712));
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
	}

	public boolean doesGuiPauseGame() {
		return true;
	}
}
