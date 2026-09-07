package net.minecraft.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.GameSettings;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.render.GLAllocation;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.ChatAllowedCharacters;

public class FontRenderer {
	public static final int FONT_HEIGHT = 8;
	private int[] charWidth = new int[256];
	public int fontTextureName = 0;
	private int fontDisplayLists;
	private IntBuffer buffer = GLAllocation.createDirectIntBuffer(1024);

	public FontRenderer(GameSettings settings, String texturePath, RenderEngine renderEngine) {
		BufferedImage fontImage;
		try {
			fontImage = ImageIO.read(RenderEngine.class.getResourceAsStream(texturePath));
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		int imageWidth = fontImage.getWidth();
		int imageHeight = fontImage.getHeight();
		int[] imagePixels = new int[imageWidth * imageHeight];
		fontImage.getRGB(0, 0, imageWidth, imageHeight, imagePixels, 0, imageWidth);

		int charX;
		int charY;
		int col;
		int row;
		int pixelX;
		int pixelY;
		int charIndex;
		for(charIndex = 0; charIndex < 256; ++charIndex) {
			charX = charIndex % 16;
			charY = charIndex / 16;

			for(col = 7; col >= 0; --col) {
				pixelX = charX * 8 + col;
				boolean columnHasPixels = true;

				for(row = 0; row < 8 && columnHasPixels; ++row) {
					pixelY = (charY * 8 + row) * imageWidth;
					pixelY = imagePixels[pixelX + pixelY] & 255;
					if(pixelY > 0) {
						columnHasPixels = false;
					}
				}

				if(!columnHasPixels) {
					break;
				}
			}

			if(charIndex == 32) {
				col = 2;
			}

			this.charWidth[charIndex] = col + 2;
		}

		this.fontTextureName = renderEngine.allocateAndSetupTexture(fontImage);
		this.fontDisplayLists = GLAllocation.generateDisplayLists(288);
		Tessellator tessellator = Tessellator.instance;

		for(charIndex = 0; charIndex < 256; ++charIndex) {
			GL11.glNewList(this.fontDisplayLists + charIndex, GL11.GL_COMPILE);
			tessellator.startDrawingQuads();
			int u = charIndex % 16 * 8;
			int v = charIndex / 16 * 8;
			float charHeight = 7.99F;
			float uOffset = 0.0F;
			float vOffset = 0.0F;
			tessellator.addVertexWithUV(0.0D, (double)(0.0F + charHeight), 0.0D, (double)((float)u / 128.0F + uOffset), (double)(((float)v + charHeight) / 128.0F + vOffset));
			tessellator.addVertexWithUV((double)(0.0F + charHeight), (double)(0.0F + charHeight), 0.0D, (double)(((float)u + charHeight) / 128.0F + uOffset), (double)(((float)v + charHeight) / 128.0F + vOffset));
			tessellator.addVertexWithUV((double)(0.0F + charHeight), 0.0D, 0.0D, (double)(((float)u + charHeight) / 128.0F + uOffset), (double)((float)v / 128.0F + vOffset));
			tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, (double)((float)u / 128.0F + uOffset), (double)((float)v / 128.0F + vOffset));
			tessellator.draw();
			GL11.glTranslatef((float)this.charWidth[charIndex], 0.0F, 0.0F);
			GL11.glEndList();
		}

		for(charIndex = 0; charIndex < 32; ++charIndex) {
			int u = (charIndex >> 3 & 1) * 85;
			int red = (charIndex >> 2 & 1) * 170 + u;
			int green = (charIndex >> 1 & 1) * 170 + u;
			int blue = (charIndex >> 0 & 1) * 170 + u;
			if(charIndex == 6) {
				red += 85;
			}

			boolean isColorShifted = charIndex >= 16;
			if(GameSettingsValues.anaglyph) {
				int newRed = (red * 30 + green * 59 + blue * 11) / 100;
				int newGreen = (red * 30 + green * 70) / 100;
				int newBlue = (red * 30 + blue * 70) / 100;
				red = newRed;
				green = newGreen;
				blue = newBlue;
			}

			if(isColorShifted) {
				red /= 4;
				green /= 4;
				blue /= 4;
			}

			GL11.glNewList(this.fontDisplayLists + 256 + charIndex, GL11.GL_COMPILE);
			GL11.glColor3f((float)red / 255.0F, (float)green / 255.0F, (float)blue / 255.0F);
			GL11.glEndList();
		}

	}

	public void drawStringWithShadow(String text, int x, int y, int color) {
		this.renderString(text, x + 1, y + 1, color, true);
		this.drawString(text, x, y, color);
	}
	
	public void drawCenteredString(String string, int x, int y, int c) {
		this.renderString(string, x - this.getStringWidth(string) / 2, y, c, false);
	}

	public void drawString(String text, int x, int y, int color) {
		this.renderString(text, x, y, color, false);
	}

	public void renderString(String text, int x, int y, int color, boolean hasShadow) {
		if(text != null) {
			int alpha;
			if(hasShadow) {
				alpha = color & 0xFF000000;
				color = (color & 16579836) >> 2;
				color += alpha;
			}

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.fontTextureName);
			float red = (float)(color >> 16 & 255) / 255.0F;
			float green = (float)(color >> 8 & 255) / 255.0F;
			float blue = (float)(color & 255) / 255.0F;
			float alphaF = (float)(color >> 24 & 255) / 255.0F;
			if(alphaF == 0.0F) {
				alphaF = 1.0F;
			}

			GL11.glColor4f(red, green, blue, alphaF);
			this.buffer.clear();
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x, (float)y, 0.0F);

			for(alpha = 0; alpha < text.length(); ++alpha) {
				int charIndex;
				for(; text.length() > alpha + 1 && text.charAt(alpha) == 167; alpha += 2) {
					charIndex = "0123456789abcdef".indexOf(text.toLowerCase().charAt(alpha + 1));
					if(charIndex < 0 || charIndex > 15) {
						charIndex = 15;
					}

					this.buffer.put(this.fontDisplayLists + 256 + charIndex + (hasShadow ? 16 : 0));
					if(this.buffer.remaining() == 0) {
						this.buffer.flip();
						GL11.glCallLists(this.buffer);
						this.buffer.clear();
					}
				}

				if(alpha < text.length()) {
					charIndex = ChatAllowedCharacters.allowedCharacters.indexOf(text.charAt(alpha));
					if(charIndex >= 0) {
						this.buffer.put(this.fontDisplayLists + charIndex + 32);
					}
				}

				if(this.buffer.remaining() == 0) {
					this.buffer.flip();
					GL11.glCallLists(this.buffer);
					this.buffer.clear();
				}
			}

			this.buffer.flip();
			GL11.glCallLists(this.buffer);
			GL11.glPopMatrix();
		}
	}

	public int getStringWidth(String text) {
		if(text == null) {
			return 0;
		} else {
			int width = 0;

			for(int i = 0; i < text.length(); ++i) {
				if(text.charAt(i) == 167) {
					++i;
				} else {
					int charWidth = ChatAllowedCharacters.allowedCharacters.indexOf(text.charAt(i));
					if(charWidth >= 0) {
						width += this.charWidth[charWidth + 32];
					}
				}
			}

			return width;
		}
	}

	public void func_27278_a(String text, int x, int y, int maxWidth, int color) {
		String[] lines = text.split("\n");
		if(lines.length > 1) {
			for(int i = 0; i < lines.length; ++i) {
				this.func_27278_a(lines[i], x, y, maxWidth, color);
				y += this.func_27277_a(lines[i], maxWidth);
			}

		} else {
			String[] words = text.split(" ");
			int wordIndex = 0;

			while(wordIndex < words.length) {
				String currentLine;
				for(currentLine = words[wordIndex++] + " "; wordIndex < words.length && this.getStringWidth(currentLine + words[wordIndex]) < maxWidth; currentLine = currentLine + words[wordIndex++] + " ") {
				}

				int charIndex;
				for(; this.getStringWidth(currentLine) > maxWidth; currentLine = currentLine.substring(charIndex)) {
					for(charIndex = 0; this.getStringWidth(currentLine.substring(0, charIndex + 1)) <= maxWidth; ++charIndex) {
					}

					if(currentLine.substring(0, charIndex).trim().length() > 0) {
						this.drawString(currentLine.substring(0, charIndex), x, y, color);
						y += 8;
					}
				}

				if(currentLine.trim().length() > 0) {
					this.drawString(currentLine, x, y, color);
					y += 8;
				}
			}

		}
	}

	public int func_27277_a(String text, int maxWidth) {
		String[] lines = text.split("\n");
		int totalHeight;
		if(lines.length > 1) {
			totalHeight = 0;

			for(int i = 0; i < lines.length; ++i) {
				totalHeight += this.func_27277_a(lines[i], maxWidth);
			}

			return totalHeight;
		} else {
			String[] words = text.split(" ");
			totalHeight = 0;
			int lineHeight = 0;

			while(totalHeight < words.length) {
				String currentLine;
				for(currentLine = words[totalHeight++] + " "; totalHeight < words.length && this.getStringWidth(currentLine + words[totalHeight]) < maxWidth; currentLine = currentLine + words[totalHeight++] + " ") {
				}

				for(int charIndex = 0; this.getStringWidth(currentLine) > maxWidth; currentLine = currentLine.substring(charIndex)) {
					for(charIndex = 0; this.getStringWidth(currentLine.substring(0, charIndex + 1)) <= maxWidth; ++charIndex) {
					}

					if(currentLine.substring(0, charIndex).trim().length() > 0) {
						lineHeight += 8;
					}
				}

				if(currentLine.trim().length() > 0) {
					lineHeight += 8;
				}
			}

			if(lineHeight < 8) {
				lineHeight += 8;
			}

			return lineHeight;
		}
	}
}
