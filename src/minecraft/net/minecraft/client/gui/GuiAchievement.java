package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;
import net.minecraft.game.achievements.Achievement;
import net.minecraft.game.achievements.StatCollector;

public class GuiAchievement extends Gui {
	private Minecraft theGame;
	private int achievementWindowWidth;
	private int achievementWindowHeight;
	private String achievementGetLocalText;
	private String achievementStatName;
	private Achievement theAchievement;
	private long achievementTime;
	private RenderItem itemRender;
	private boolean haveAchievement;

	public GuiAchievement(Minecraft minecraft) {
		this.theGame = minecraft;
		this.itemRender = new RenderItem();
	}

	public void queueTakenAchievement(Achievement achievement) {
		this.achievementGetLocalText = StatCollector.translateToLocal("achievement.get");
		this.achievementStatName = achievement.statName;
		this.achievementTime = System.currentTimeMillis();
		this.theAchievement = achievement;
		this.haveAchievement = false;
	}

	public void queueAchievementInformation(Achievement achievement) {
		this.achievementGetLocalText = achievement.statName;
		this.achievementStatName = achievement.getDescription();
		this.achievementTime = System.currentTimeMillis() - 2500L;
		this.theAchievement = achievement;
		this.haveAchievement = true;
	}

	private void updateAchievementWindowScale() {
		GL11.glViewport(0, 0, this.theGame.displayWidth, this.theGame.displayHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.achievementWindowWidth = this.theGame.displayWidth;
		this.achievementWindowHeight = this.theGame.displayHeight;
		ScaledResolution scaledResolution = new ScaledResolution(this.theGame.gameSettings, this.theGame.displayWidth, this.theGame.displayHeight);
		this.achievementWindowWidth = scaledResolution.getScaledWidth();
		this.achievementWindowHeight = scaledResolution.getScaledHeight();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)this.achievementWindowWidth, (double)this.achievementWindowHeight, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}

	public void updateAchievementWindow() {
		/*
		if(Minecraft.hasPaidCheckTime > 0L) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			RenderHelper.disableStandardItemLighting();
			this.updateAchievementWindowScale();
			String string1 = "Minecraft Beta 1.7.3   Unlicensed Copy :(";
			String string2 = "(Or logged in from another location)";
			String string3 = "Purchase at minecraft.net";
			this.theGame.fontRenderer.drawStringWithShadow(string1, 2, 2, 0xFFFFFF);
			this.theGame.fontRenderer.drawStringWithShadow(string2, 2, 11, 0xFFFFFF);
			this.theGame.fontRenderer.drawStringWithShadow(string3, 2, 20, 0xFFFFFF);
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		*/

		if(this.theAchievement != null && this.achievementTime != 0L) {
			double progress = (double)(System.currentTimeMillis() - this.achievementTime) / 3000.0D;
			if(this.haveAchievement || progress >= 0.0D && progress <= 1.0D) {
				this.updateAchievementWindowScale();
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDepthMask(false);
				double slideOffset = progress * 2.0D;
				if(slideOffset > 1.0D) {
					slideOffset = 2.0D - slideOffset;
				}

				slideOffset *= 4.0D;
				slideOffset = 1.0D - slideOffset;
				if(slideOffset < 0.0D) {
					slideOffset = 0.0D;
				}

				slideOffset *= slideOffset;
				slideOffset *= slideOffset;
				int x = this.achievementWindowWidth - 160;
				int y = 0 - (int)(slideOffset * 36.0D);
				int bgTexture = this.theGame.renderEngine.getTexture("/achievement/bg.png");
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, bgTexture);
				GL11.glDisable(GL11.GL_LIGHTING);
				this.drawTexturedModalRect(x, y, 96, 202, 160, 32);
				if(this.haveAchievement) {
					this.theGame.fontRenderer.func_27278_a(this.achievementStatName, x + 30, y + 7, 120, -1);
				} else {
					this.theGame.fontRenderer.drawString(this.achievementGetLocalText, x + 30, y + 7, -256);
					this.theGame.fontRenderer.drawString(this.achievementStatName, x + 30, y + 18, -1);
				}

				GL11.glPushMatrix();
				GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
				RenderHelper.enableStandardItemLighting();
				GL11.glPopMatrix();
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_COLOR_MATERIAL);
				GL11.glEnable(GL11.GL_LIGHTING);
				this.itemRender.renderItemIntoGUI(this.theGame.fontRenderer, this.theGame.renderEngine, this.theAchievement.theItemStack, x + 8, y + 8);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			} else {
				this.achievementTime = 0L;
			}
		}
	}
}
