package net.minecraft.client.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import net.minecraft.game.MathHelper;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.Version;
import net.minecraft.client.effect.LogoEffectRandomizer;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;

public class GuiMainMenu extends GuiScreen {
	private static final Random rand = new Random();
	String[] logoBlockLayers = new String[]{
			" *** *   * **** *  * **** *    *    * ",
			"  *  **  * *    *  * *    *    *    * ",
			"  *  * * * ***  **** ***  *    *    * ",
			"  *  *  ** *    *  * *    *    *      ",
			" *** *   * *    *  * **** **** **** * "
	};
	private LogoEffectRandomizer[][] logoEffects;
	private float updateCounter = 0.0F;
	private String splashText = "missingno";
	private GuiButton multiplayerButton;
	
	private int bgMax = 8;
	private boolean bgStatic;
	private long bgTicks;
	private int bgNow;
	private int bgNext;
	private float bgAlpha = 0.0F;

	public GuiMainMenu() {
		try {
			ArrayList<String> splashes = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(GuiMainMenu.class.getResourceAsStream("/title/splashes.txt"), Charset.forName("UTF-8")));
			String line = "";

			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					splashes.add(line);
				}
			}

			this.splashText = (String)splashes.get(rand.nextInt(splashes.size()));
		} catch (Exception e) {
		}

		this.updateCounter = rand.nextFloat();
	}

	public void updateScreen() {
		++this.updateCounter;
		if(this.logoEffects != null) {
			for(int i = 0; i < this.logoEffects.length; ++i) {
				for(int j = 0; j < this.logoEffects[i].length; ++j) {
					this.logoEffects[i][j].updateLogoEffects();
				}
			}
		}
		
	}

	protected void keyTyped(char c, int keyCode) {
	}

	public void initGui() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if(calendar.get(2) + 1 == 11 && calendar.get(5) == 9) {
			this.splashText = "Happy Birthday, ez!";
		} else if(calendar.get(2) + 1 == 6 && calendar.get(5) == 1) {
			this.splashText = "Trans Rights are Human Rights!";
		} else if(calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
			this.splashText = "Happy Hollidays!";
		} else if(calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
			this.splashText = "Happy new year!";
		}

		StringTranslate translator = StringTranslate.getInstance();
		int buttonY = this.height / 4 + 48;
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, buttonY, translator.translateKey("menu.singleplayer")));
		this.controlList.add(this.multiplayerButton = new GuiButton(2, this.width / 2 - 100, buttonY + 24, translator.translateKey("menu.multiplayer")));
		this.controlList.add(new GuiButton(3, this.width / 2 - 100, buttonY + 48, translator.translateKey("menu.mods")));
		
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, buttonY + 72 + 12, 98, 20, translator.translateKey("menu.options")));
		this.controlList.add(new GuiButton(4, this.width / 2 + 2, buttonY + 72 + 12, 98, 20, translator.translateKey("menu.quit")));
	
		this.controlList.add(new GuiButton(5, this.width / 2 + 104, buttonY + 72 + 12, 20, 20, translator.translateKey("menu.credits")));	
		
		if(this.mc.session == null) {
			this.multiplayerButton.enabled = false;
		}

		this.bgNow = this.mc.renderEngine.getTexture("/title/backgrounds/ts" + GuiMainMenu.rand.nextInt(this.bgMax) + ".jpg");
		this.bgTicks = System.currentTimeMillis();
		this.bgStatic = true;
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.id == 0) {
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
		}

		if(guiButton.id == 1) {
			this.mc.displayGuiScreen(new GuiSelectWorld(this));
		}

		if(guiButton.id == 2) {
			this.mc.displayGuiScreen(new GuiMultiplayer(this));
		}

		if(guiButton.id == 3) {
			this.mc.displayGuiScreen(new GuiTexturePacks(this));
		}

		if(guiButton.id == 4) {
			this.mc.shutdown();
		}

		if(guiButton.id == 5) {
			this.mc.displayGuiScreen(new GuiCredits(this));
		}
	}
	
	public void drawBackground(int what) {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		
		Tessellator tessellator = Tessellator.instance;
		float bgWidth = 1920.0F;
		float bgHeight = 1080.0F;
		float bgRatio = bgWidth / bgHeight;
		float scrRatio = (float)this.width / (float)this.height;
		float stretchRatio = this.width / bgWidth / (this.height / bgHeight);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.bgNow);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, 1.0f);
		if(scrRatio < bgRatio) {
			tessellator.addVertexWithUV(0.0, this.height, 0.0, 0.5f - stretchRatio / 2.0f, 1.0);
            tessellator.addVertexWithUV(this.width, this.height, 0.0, 0.5f + stretchRatio / 2.0f, 1.0);
            tessellator.addVertexWithUV(this.width, 0.0, 0.0, 0.5f + stretchRatio / 2.0f, 0.0);
            tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.5f - stretchRatio / 2.0f, 0.0);
		} else {
			tessellator.addVertexWithUV(0.0, this.height, 0.0, 0.0, 0.5f + 0.5f / stretchRatio);
            tessellator.addVertexWithUV(this.width, this.height, 0.0, 1.0, 0.5f + 0.5f / stretchRatio);
            tessellator.addVertexWithUV(this.width, 0.0, 0.0, 1.0, 0.5f - 0.5f / stretchRatio);
            tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.5f - 0.5f / stretchRatio);
		}
		tessellator.draw();
		
		if(this.bgAlpha > 0) { 
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.bgNext);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, this.bgAlpha);
			if(scrRatio < bgRatio) {
				tessellator.addVertexWithUV(0.0, this.height, 0.0, 0.5f - stretchRatio / 2.0f, 1.0);
	            tessellator.addVertexWithUV(this.width, this.height, 0.0, 0.5f + stretchRatio / 2.0f, 1.0);
	            tessellator.addVertexWithUV(this.width, 0.0, 0.0, 0.5f + stretchRatio / 2.0f, 0.0);
	            tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.5f - stretchRatio / 2.0f, 0.0);
			} else {
				tessellator.addVertexWithUV(0.0, this.height, 0.0, 0.0, 0.5f + 0.5f / stretchRatio);
	            tessellator.addVertexWithUV(this.width, this.height, 0.0, 1.0, 0.5f + 0.5f / stretchRatio);
	            tessellator.addVertexWithUV(this.width, 0.0, 0.0, 1.0, 0.5f - 0.5f / stretchRatio);
	            tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.5f - 0.5f / stretchRatio);
			}
			tessellator.draw();
		}
		
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void drawScreen(int mouseX, int mouseY, float renderPartialTick) {
		this.drawDefaultBackground();
		Tessellator tessellator = Tessellator.instance;
		
		this.drawLogo(renderPartialTick);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				
		tessellator.setColorOpaque_I(0xFFFFFF);
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
		GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		float splashScale = 1.8F - MathHelper.abs(MathHelper.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
		splashScale = splashScale * 100.0F / (float)(this.fontRenderer.getStringWidth(this.splashText) + 32);
		GL11.glScalef(splashScale, splashScale, splashScale);
		this.drawCenteredString(this.fontRenderer, this.splashText, 0, -8, 16776960);
		GL11.glPopMatrix();
		this.drawString(this.fontRenderer, "Minecraft " + Version.getVersion() + " " + Version.getDate(), 2, 2, 0xffffff);
		String copyrightText = "Copyright Mojang Specifications. Do not distribute.";
		this.drawString(this.fontRenderer, copyrightText, this.width - this.fontRenderer.getStringWidth(copyrightText) - 2, this.height - 10, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, renderPartialTick);
		
		long now = System.currentTimeMillis();
		if(this.bgStatic) {
			if(now > this.bgTicks + 20000) {
				this.bgStatic = false;
				this.bgTicks = now;
				this.bgNext = this.mc.renderEngine.getTexture("/title/backgrounds/ts" + GuiMainMenu.rand.nextInt(this.bgMax) + ".jpg");
			}
			this.bgAlpha = 0;
		} else {
			if(now > this.bgTicks + 1000) {
				this.bgStatic = true;
				this.bgTicks = now;
				this.bgNow = this.bgNext;
			} else {
				this.bgAlpha = (float)(now - bgTicks) / 1000.0F;
			}
		}
	}

	private void drawLogo(float renderPartialTick) {
		int viewportHeight;
		if(this.logoEffects == null) {
			this.logoEffects = new LogoEffectRandomizer[this.logoBlockLayers[0].length()][this.logoBlockLayers.length];

			for(int i = 0; i < this.logoEffects.length; ++i) {
				for(viewportHeight = 0; viewportHeight < this.logoEffects[i].length; ++viewportHeight) {
					this.logoEffects[i][viewportHeight] = new LogoEffectRandomizer(this, i, viewportHeight);
				}
			}
		}

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		ScaledResolution scaledRes = new ScaledResolution(this.mc.gameSettings, mc.displayWidth, this.mc.displayHeight);
		viewportHeight = 120 * scaledRes.scaleFactor;
		GLU.gluPerspective(70.0F, (float)this.mc.displayWidth / (float)viewportHeight, 0.05F, 100.0F);
		GL11.glViewport(0, this.mc.displayHeight - viewportHeight, this.mc.displayWidth, viewportHeight);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glDepthMask(true);

		for(int pass = 0; pass < 3; ++pass) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.4F, 0.6F, -12.0F);
			if(pass == 0) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glTranslatef(0.0F, -0.4F, 0.0F);
				GL11.glScalef(0.98F, 1.0F, 1.0F);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}

			if(pass == 1) {
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			}

			if(pass == 2) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
			}

			GL11.glScalef(1.0F, -1.0F, 1.0F);
			GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(0.89F, 1.0F, 0.4F);
			GL11.glTranslatef((float)(-this.logoBlockLayers[0].length()) * 0.5F, (float)(-this.logoBlockLayers.length) * 0.5F, 0.0F);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
			if(pass == 0) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/title/black.png"));
			}

			RenderBlocks renderBlocks = new RenderBlocks();

			for(int row = 0; row < this.logoBlockLayers.length; ++row) {
				for(int col = 0; col < this.logoBlockLayers[row].length(); ++col) {
					char c = this.logoBlockLayers[row].charAt(col);
					if(c != 32) {
						GL11.glPushMatrix();
						LogoEffectRandomizer effect = this.logoEffects[col][row];
						float heightF = (float)(effect.prevHeight + (effect.height - effect.prevHeight) * (double)renderPartialTick);
						float scaleX = 1.0F;
						float scaleY = 1.0F;
						float rotation = 0.0F;
						if(pass == 0) {
							scaleX = heightF * 0.04F + 1.0F;
							scaleY = 1.0F / scaleX;
							heightF = 0.0F;
						}

						GL11.glTranslatef((float)col, (float)row, heightF);
						GL11.glScalef(scaleX, scaleX, scaleX);
						GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						renderBlocks.renderBlockAsItem(pass == 0 ? Block.fire : Block.stone, scaleY);
						GL11.glPopMatrix();
					}
				}
			}

			GL11.glPopMatrix();
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	static public Random getRandom() {
		return rand;
	}

	public float getUpdateCounter() {
		return updateCounter;
	}

	public void setUpdateCounter(float updateCounter) {
		this.updateCounter = updateCounter;
	}
}
