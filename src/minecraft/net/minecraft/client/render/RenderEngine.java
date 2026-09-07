package net.minecraft.client.render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.GameSettings;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.render.texture.ImageBuffer;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.client.render.texture.TextureFX;
import net.minecraft.client.render.texture.TexturePackBase;
import net.minecraft.client.render.texture.TexturePackDefault;
import net.minecraft.client.render.texture.TexturePackList;

public class RenderEngine {
	public static boolean useMipmaps = false;
	private HashMap<String,Integer> textureMap = new HashMap<String,Integer>();
	private HashMap<String,Object> textureContentsMap = new HashMap<String, Object>();
	private HashMap<Integer,BufferedImage> textureNameToImageMap = new HashMap<Integer,BufferedImage>();
	private IntBuffer singleIntBuffer = GLAllocation.createDirectIntBuffer(1);
	//private ByteBuffer imageData = GLAllocation.createDirectByteBuffer(1048576);
	private ByteBuffer imageData = GLAllocation.createDirectByteBuffer(1920*1080*4);
	private List<TextureFX> textureList = new ArrayList<TextureFX>();
	private Map<String,ThreadDownloadImageData> urlToImageDataMap = new HashMap<String,ThreadDownloadImageData>();
	private GameSettings options;
	private boolean clampTexture = false;
	private boolean blurTexture = false;
	private TexturePackList texturePack;
	private BufferedImage missingTextureImage = new BufferedImage(64, 64, 2);

	// Optifine 
	private int terrainTextureId = -1;
	private int guiItemsTextureId = -1;
	private Map<Integer, Dimension> textureDimensionsMap = new HashMap<Integer, Dimension>();
	private Map<String, byte[]> textureDataMap = new HashMap<String, byte[]>();
	private int tickCounter = 0;
	private ByteBuffer[] mipImageDatas;
	private boolean dynamicTexturesUpdated = false;
	
	public RenderEngine(TexturePackList texturePackList1, GameSettings gameSettings2) {
		this.allocateImageData(TextureAtlas.TERRAIN.width);
		this.textureList = new ArrayList<TextureFX>();
		this.urlToImageDataMap = new HashMap<String, ThreadDownloadImageData>();
		this.clampTexture = false;
		this.blurTexture = false;
		this.missingTextureImage = new BufferedImage(64, 64, 2);
		
		this.texturePack = texturePackList1;
		this.options = gameSettings2;
		Graphics graphics3 = this.missingTextureImage.getGraphics();
		graphics3.setColor(Color.WHITE);
		graphics3.fillRect(0, 0, 64, 64);
		graphics3.setColor(Color.BLACK);
		graphics3.drawString("missingtex", 1, 10);
		graphics3.dispose();
	}

	public int[] getTextureContents(String string1) {
		TexturePackBase texturePackBase2 = this.texturePack.selectedTexturePack;
		int[] i3 = (int[])this.textureContentsMap.get(string1);
		if(i3 != null) {
			return i3;
		} else {

			try {
				if(string1.startsWith("##")) {
					i3 = this.getImageContentsAndAllocate(this.unwrapImageByColumns(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(2)))));
				} else if(string1.startsWith("%clamp%")) {
					this.clampTexture = true;
					i3 = this.getImageContentsAndAllocate(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(7))));
					this.clampTexture = false;
				} else if(string1.startsWith("%blur%")) {
					this.blurTexture = true;
					i3 = this.getImageContentsAndAllocate(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(6))));
					this.blurTexture = false;
				} else {
					InputStream inputStream7 = texturePackBase2.getResourceAsStream(string1);
					if(inputStream7 == null) {
						i3 = this.getImageContentsAndAllocate(this.missingTextureImage);
					} else {
						i3 = this.getImageContentsAndAllocate(this.readTextureImage(inputStream7));
					}
				}

				this.textureContentsMap.put(string1, i3);
				return i3;
			} catch (IOException iOException5) {
				iOException5.printStackTrace();
				int[] i4 = this.getImageContentsAndAllocate(this.missingTextureImage);
				this.textureContentsMap.put(string1, i4);
				return i4;
			}
		}
	}

	private int[] getImageContentsAndAllocate(BufferedImage bufferedImage1) {
		int i2 = bufferedImage1.getWidth();
		int i3 = bufferedImage1.getHeight();
		int[] i4 = new int[i2 * i3];
		bufferedImage1.getRGB(0, 0, i2, i3, i4, 0, i2);
		return i4;
	}

	private int[] getImageContents(BufferedImage bufferedImage1, int[] i2) {
		int i3 = bufferedImage1.getWidth();
		int i4 = bufferedImage1.getHeight();
		bufferedImage1.getRGB(0, 0, i3, i4, i2, 0, i3);
		return i2;
	}

	public int getTexture(String string1) {
		TexturePackBase texturePackBase2 = this.texturePack.selectedTexturePack;
		Integer integer3 = (Integer)this.textureMap.get(string1);
		if(integer3 != null) {
			return integer3.intValue();
		} else {
			try {
				this.singleIntBuffer.clear();
				GLAllocation.generateTextureNames(this.singleIntBuffer);
				int i6 = this.singleIntBuffer.get(0);
				if(string1.startsWith("##")) {
					this.setupTexture(this.unwrapImageByColumns(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(2)))), i6);
				} else if(string1.startsWith("%clamp%")) {
					this.clampTexture = true;
					this.setupTexture(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(7))), i6);
					this.clampTexture = false;
				} else if(string1.startsWith("%blur%")) {
					this.blurTexture = true;
					this.setupTexture(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(6))), i6);
					this.blurTexture = false;
				} else if(string1.startsWith("%blurclamp%")) {
					this.blurTexture = true;
					this.clampTexture = true;
					this.setupTexture(this.readTextureImage(texturePackBase2.getResourceAsStream(string1.substring(11))), i6);
					this.blurTexture = false;
					this.clampTexture = false;
				} else {
					if(string1.equals("/terrain.png")) {
						this.terrainTextureId = i6;
					}

					if(string1.equals("/gui/items.png")) {
						this.guiItemsTextureId = i6;
					}
					
					InputStream inputStream7 = texturePackBase2.getResourceAsStream(string1);
					if(inputStream7 == null) {
						this.setupTexture(this.missingTextureImage, i6);
					} else {
						this.setupTexture(this.readTextureImage(inputStream7), i6);
					}
				}

				this.textureMap.put(string1, i6);
				return i6;
			} catch (IOException iOException5) {
				iOException5.printStackTrace();
				GLAllocation.generateTextureNames(this.singleIntBuffer);
				int i4 = this.singleIntBuffer.get(0);
				this.setupTexture(this.missingTextureImage, i4);
				this.textureMap.put(string1, i4);
				return i4;
			}
		}
	}

	private BufferedImage unwrapImageByColumns(BufferedImage bufferedImage1) {
		int i2 = bufferedImage1.getWidth() / 16;
		BufferedImage bufferedImage3 = new BufferedImage(16, bufferedImage1.getHeight() * i2, 2);
		Graphics graphics4 = bufferedImage3.getGraphics();

		for(int i5 = 0; i5 < i2; ++i5) {
			graphics4.drawImage(bufferedImage1, -i5 * 16, i5 * bufferedImage1.getHeight(), (ImageObserver)null);
		}

		graphics4.dispose();
		return bufferedImage3;
	}

	public int allocateAndSetupTexture(BufferedImage bufferedImage1) {
		this.singleIntBuffer.clear();
		GLAllocation.generateTextureNames(this.singleIntBuffer);
		int i2 = this.singleIntBuffer.get(0);
		this.setupTexture(bufferedImage1, i2);
		this.textureNameToImageMap.put(i2, bufferedImage1);
		return i2;
	}

	public void setupTexture(BufferedImage bufferedImage1, int i2) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, i2);
		
		useMipmaps = GameSettingsValues.ofMipmapLevel > 0;
		int width;
		int height;
		
		if(useMipmaps && i2 != this.guiItemsTextureId) {
			int mipmapType = (GameSettingsValues.ofMipmapLinear ? 9986 : 9984);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmapType);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			if(GLContext.getCapabilities().OpenGL12) {
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
				int mipmapLevel = GameSettingsValues.ofMipmapLevel;
				if(mipmapLevel >= 4) {
					int ai = Math.min(bufferedImage1.getWidth(), bufferedImage1.getHeight());
					mipmapLevel = this.getMaxMipmapLevel(ai) - 4;
					if(mipmapLevel < 0) {
						mipmapLevel = 0;
					}
				}

				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapLevel);
			}
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		if(this.blurTexture) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		if(this.clampTexture) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}

		width = bufferedImage1.getWidth();
		height = bufferedImage1.getHeight();
		this.setTextureDimension(i2, new Dimension(width, height));
		int[] i5 = new int[width * height];
		byte[] b6 = new byte[width * height * 4];
		bufferedImage1.getRGB(0, 0, width, height, i5, 0, width);

		int i7;
		int i8;
		int i9;
		int i10;
		int i11;
		int i12;
		int i13;
		int i14;
		for(i7 = 0; i7 < i5.length; ++i7) {
			i8 = i5[i7] >> 24 & 255;
			i9 = i5[i7] >> 16 & 255;
			i10 = i5[i7] >> 8 & 255;
			i11 = i5[i7] & 255;
			if(this.options != null && GameSettingsValues.anaglyph) {
				i12 = (i9 * 30 + i10 * 59 + i11 * 11) / 100;
				i13 = (i9 * 30 + i10 * 70) / 100;
				i14 = (i9 * 30 + i11 * 70) / 100;
				i9 = i12;
				i10 = i13;
				i11 = i14;
			}

			if(i8 == 0) { i9 = i10 = i11 = 255; }
			
			b6[i7 * 4 + 0] = (byte)i9;
			b6[i7 * 4 + 1] = (byte)i10;
			b6[i7 * 4 + 2] = (byte)i11;
			b6[i7 * 4 + 3] = (byte)i8;
		}

		this.checkImageDataSize(width);
		this.imageData.clear();
		this.imageData.put(b6);
		this.imageData.position(0).limit(b6.length);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageData);
		if(useMipmaps) {
			this.generateMipMaps(this.imageData, width, height);
		}

	}
	

	private void generateMipMaps(ByteBuffer data, int width, int height) {
		ByteBuffer parMipData = data;

		for(int level = 1; level <= 16; ++level) {
			int parWidth = width >> level - 1;
			int mipWidth = width >> level;
			int mipHeight = height >> level;
			if(mipWidth <= 0 || mipHeight <= 0) {
				break;
			}

			ByteBuffer mipData = this.mipImageDatas[level - 1];

			for(int mipX = 0; mipX < mipWidth; ++mipX) {
				for(int mipY = 0; mipY < mipHeight; ++mipY) {
					int p1 = parMipData.getInt((mipX * 2 + 0 + (mipY * 2 + 0) * parWidth) * 4);
					int p2 = parMipData.getInt((mipX * 2 + 1 + (mipY * 2 + 0) * parWidth) * 4);
					int p3 = parMipData.getInt((mipX * 2 + 1 + (mipY * 2 + 1) * parWidth) * 4);
					int p4 = parMipData.getInt((mipX * 2 + 0 + (mipY * 2 + 1) * parWidth) * 4);
					int pixel = this.weightedAverageColor(p1, p2, p3, p4);
					mipData.putInt((mipX + mipY * mipWidth) * 4, pixel);
				}
			}

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA, mipWidth, mipHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, mipData);
			parMipData = mipData;
		}

	}

	public void createTextureFromBytes(int[] i1, int i2, int i3, int i4) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, i4);
		if(useMipmaps) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		if(this.blurTexture) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		if(this.clampTexture) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}

		byte[] b5 = new byte[i2 * i3 * 4];

		for(int i6 = 0; i6 < i1.length; ++i6) {
			int i7 = i1[i6] >> 24 & 255;
			int i8 = i1[i6] >> 16 & 255;
			int i9 = i1[i6] >> 8 & 255;
			int i10 = i1[i6] & 255;
			if(this.options != null && GameSettingsValues.anaglyph) {
				int i11 = (i8 * 30 + i9 * 59 + i10 * 11) / 100;
				int i12 = (i8 * 30 + i9 * 70) / 100;
				int i13 = (i8 * 30 + i10 * 70) / 100;
				i8 = i11;
				i9 = i12;
				i10 = i13;
			}

			b5[i6 * 4 + 0] = (byte)i8;
			b5[i6 * 4 + 1] = (byte)i9;
			b5[i6 * 4 + 2] = (byte)i10;
			b5[i6 * 4 + 3] = (byte)i7;
		}

		this.imageData.clear();
		this.imageData.put(b5);
		this.imageData.position(0).limit(b5.length);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, i2, i3, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageData);
	}

	public void deleteTexture(int i1) {
		this.textureNameToImageMap.remove(i1);
		this.singleIntBuffer.clear();
		this.singleIntBuffer.put(i1);
		this.singleIntBuffer.flip();
		GL11.glDeleteTextures(this.singleIntBuffer);
	}

	public int getTextureForDownloadableImage(String string1, String string2) {
		ThreadDownloadImageData threadDownloadImageData3 = (ThreadDownloadImageData)this.urlToImageDataMap.get(string1);
		if(threadDownloadImageData3 != null && threadDownloadImageData3.image != null && !threadDownloadImageData3.textureSetupComplete) {
			if(threadDownloadImageData3.textureName < 0) {
				threadDownloadImageData3.textureName = this.allocateAndSetupTexture(threadDownloadImageData3.image);
			} else {
				this.setupTexture(threadDownloadImageData3.image, threadDownloadImageData3.textureName);
			}

			threadDownloadImageData3.textureSetupComplete = true;
		}

		return threadDownloadImageData3 != null && threadDownloadImageData3.textureName >= 0 ? threadDownloadImageData3.textureName : (string2 == null ? -1 : this.getTexture(string2));
	}

	public ThreadDownloadImageData obtainImageData(String string1, ImageBuffer imageBuffer2) {
		ThreadDownloadImageData threadDownloadImageData3 = (ThreadDownloadImageData)this.urlToImageDataMap.get(string1);
		if(threadDownloadImageData3 == null) {
			this.urlToImageDataMap.put(string1, new ThreadDownloadImageData(string1, imageBuffer2));
		} else {
			++threadDownloadImageData3.referenceCount;
		}

		return threadDownloadImageData3;
	}

	public void releaseImageData(String string1) {
		ThreadDownloadImageData threadDownloadImageData2 = (ThreadDownloadImageData)this.urlToImageDataMap.get(string1);
		if(threadDownloadImageData2 != null) {
			--threadDownloadImageData2.referenceCount;
			if(threadDownloadImageData2.referenceCount == 0) {
				if(threadDownloadImageData2.textureName >= 0) {
					this.deleteTexture(threadDownloadImageData2.textureName);
				}

				this.urlToImageDataMap.remove(string1);
			}
		}

	}

	public void registerTextureFX(TextureFX texturefx) {
		for(int i = 0; i < this.textureList.size(); ++i) {
			TextureFX tex = (TextureFX)this.textureList.get(i);
			if(tex.tileImage == texturefx.tileImage && tex.iconIndex == texturefx.iconIndex) {
				this.textureList.remove(i);
				--i;
			}
		}

		this.textureList.add(texturefx);
		texturefx.onTick();
		
		this.dynamicTexturesUpdated = false;
	}
	

	private void generateMipMapsSub(int xOffset, int yOffset, int width, int height, ByteBuffer data, int numTiles, boolean fastColor) {
		ByteBuffer parMipData = data;

		for(int level = 1; level <= 16; ++level) {
			int parWidth = width >> level - 1;
			int mipWidth = width >> level;
			int mipHeight = height >> level;
			int xMipOffset = xOffset >> level;
			int yMipOffset = yOffset >> level;
			if(mipWidth <= 0 || mipHeight <= 0) {
				break;
			}

			ByteBuffer mipData = this.mipImageDatas[level - 1];

			int ix;
			int iy;
			int dx;
			int dy;
			for(ix = 0; ix < mipWidth; ++ix) {
				for(iy = 0; iy < mipHeight; ++iy) {
					dx = parMipData.getInt((ix * 2 + 0 + (iy * 2 + 0) * parWidth) * 4);
					dy = parMipData.getInt((ix * 2 + 1 + (iy * 2 + 0) * parWidth) * 4);
					int p3 = parMipData.getInt((ix * 2 + 1 + (iy * 2 + 1) * parWidth) * 4);
					int p4 = parMipData.getInt((ix * 2 + 0 + (iy * 2 + 1) * parWidth) * 4);
					int pixel;
					if(fastColor) {
						pixel = this.averageColor(this.averageColor(dx, dy), this.averageColor(p3, p4));
					} else {
						pixel = this.weightedAverageColor(dx, dy, p3, p4);
					}

					mipData.putInt((ix + iy * mipWidth) * 4, pixel);
				}
			}

			for(ix = 0; ix < numTiles; ++ix) {
				for(iy = 0; iy < numTiles; ++iy) {
					dx = ix * mipWidth;
					dy = iy * mipHeight;
					GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xMipOffset + dx, yMipOffset + dy, mipWidth, mipHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, mipData);
				}
			}

			parMipData = mipData;
		}

	}

	public void updateDynamicTextures() {
		this.checkHdTextures();
		++this.tickCounter;
		this.terrainTextureId = this.getTexture("/terrain.png");
		this.guiItemsTextureId = this.getTexture("/gui/items.png");

		int i;
		TextureFX texturefx1;
		for(i = 0; i < this.textureList.size(); ++i) {
			texturefx1 = (TextureFX)this.textureList.get(i);
			texturefx1.anaglyphEnabled = GameSettingsValues.anaglyph;
			if(!texturefx1.getClass().getName().equals("ModTextureStatic") || !this.dynamicTexturesUpdated) {
				int i13;
				if(texturefx1.tileImage == 0) {
					i13 = this.terrainTextureId;
				} else {
					i13 = this.guiItemsTextureId;
				}

				Dimension dim = this.getTextureDimensions(i13);
				if(dim == null) {
					throw new IllegalArgumentException("Unknown dimensions for texture id: " + i13);
				}

				int tileWidth = dim.width / 16;
				int tileHeight = dim.height / 16;
				this.checkImageDataSize(dim.width);
				this.imageData.limit(0);
				boolean customOk = this.updateCustomTexture(texturefx1, this.imageData, dim.width / 16);
				if(!customOk || this.imageData.limit() > 0) {
					boolean fastColor;
					if(this.imageData.limit() <= 0) {
						fastColor = this.updateDefaultTexture(texturefx1, this.imageData, dim.width / 16);
						if(fastColor && this.imageData.limit() <= 0) {
							continue;
						}
					}

					if(this.imageData.limit() <= 0) {			
						texturefx1.onTick();
							
						if(texturefx1.imageData == null) {
							continue;
						}

						int i14 = tileWidth * tileHeight * 4;
						if(texturefx1.imageData.length == i14) {
							this.imageData.clear();
							this.imageData.put(texturefx1.imageData);
							this.imageData.position(0).limit(texturefx1.imageData.length);
						} else {
							this.copyScaled(texturefx1.imageData, this.imageData, tileWidth);
						}
					}

					texturefx1.bindImage(this);
					fastColor = this.scalesWithFastColor(texturefx1);

					for(int ix = 0; ix < texturefx1.tileSize; ++ix) {
						for(int iy = 0; iy < texturefx1.tileSize; ++iy) {
							int xOffset = texturefx1.iconIndex % 16 * tileWidth + ix * tileWidth;
							int yOffset = texturefx1.iconIndex / 16 * tileHeight + iy * tileHeight;
							GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, xOffset, yOffset, tileWidth, tileHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageData);
							if(useMipmaps && ix == 0 && iy == 0) {
								this.generateMipMapsSub(xOffset, yOffset, tileWidth, tileHeight, this.imageData, texturefx1.tileSize, fastColor);
							}
						}
					}
				}
			}
		}

		this.dynamicTexturesUpdated = true;

		for(i = 0; i < this.textureList.size(); ++i) {
			texturefx1 = (TextureFX)this.textureList.get(i);
			if(texturefx1.textureId > 0) {
				this.imageData.clear();
				this.imageData.put(texturefx1.imageData);
				this.imageData.position(0).limit(texturefx1.imageData.length);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturefx1.textureId);
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageData);
				if(useMipmaps) {
					this.generateMipMapsSub(0, 0, 16, 16, this.imageData, texturefx1.tileSize, false);
				}
			}
		}

	}

	private int averageColor(int i1, int i2) {
		int i3 = (i1 & 0xFF000000) >> 24 & 255;
		int i4 = (i2 & 0xFF000000) >> 24 & 255;
		return (i3 + i4 >> 1 << 24) + ((i1 & 16711422) + (i2 & 16711422) >> 1);
	}

	private int weightedAverageColor(int c1, int c2, int c3, int c4) {
		int cx1 = this.weightedAverageColor(c1, c2);
		int cx2 = this.weightedAverageColor(c3, c4);
		int cx = this.weightedAverageColor(cx1, cx2);
		return cx;
	}
	
	private int weightedAverageColor(int c1, int c2) {
		int a1 = (c1 & 0xFF000000) >> 24 & 255;
		int a2 = (c2 & 0xFF000000) >> 24 & 255;
		int ax = (a1 + a2) / 2;
		if(a1 == 0 && a2 == 0) {
			a1 = 1;
			a2 = 1;
		} else {
			if(a1 == 0) {
				c1 = c2;
				ax /= 2;
			}

			if(a2 == 0) {
				c2 = c1;
				ax /= 2;
			}
		}

		int r1 = (c1 >> 16 & 255) * a1;
		int g1 = (c1 >> 8 & 255) * a1;
		int b1 = (c1 & 255) * a1;
		int r2 = (c2 >> 16 & 255) * a2;
		int g2 = (c2 >> 8 & 255) * a2;
		int b2 = (c2 & 255) * a2;
		int rx = (r1 + r2) / (a1 + a2);
		int gx = (g1 + g2) / (a1 + a2);
		int bx = (b1 + b2) / (a1 + a2);
		return ax << 24 | rx << 16 | gx << 8 | bx;
	}

	public void refreshTextures() {
		this.textureDataMap.clear();
		this.dynamicTexturesUpdated = false;
		
		TexturePackBase texturePackBase1 = this.texturePack.selectedTexturePack;
		Iterator<Integer> iterator2 = this.textureNameToImageMap.keySet().iterator();

		BufferedImage bufferedImage4;
		while(iterator2.hasNext()) {
			int i3 = ((Integer)iterator2.next()).intValue();
			bufferedImage4 = (BufferedImage)this.textureNameToImageMap.get(i3);
			this.setupTexture(bufferedImage4, i3);
		}

		ThreadDownloadImageData threadDownloadImageData8;
		Iterator<ThreadDownloadImageData> iterator3;
		for(iterator3 = this.urlToImageDataMap.values().iterator(); iterator3.hasNext(); threadDownloadImageData8.textureSetupComplete = false) {
			threadDownloadImageData8 = (ThreadDownloadImageData)iterator3.next();
		}

		Iterator<String> iterator4 = this.textureMap.keySet().iterator();

		String string9;
		while(iterator4.hasNext()) {
			string9 = (String)iterator4.next();

			try {
				if(string9.startsWith("##")) {
					bufferedImage4 = this.unwrapImageByColumns(this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(2))));
				} else if(string9.startsWith("%clamp%")) {
					this.clampTexture = true;
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(7)));
				} else if(string9.startsWith("%blur%")) {
					this.blurTexture = true;
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(6)));
				} else if(string9.startsWith("%blurclamp%")) {
					this.blurTexture = true;
					this.clampTexture = true;
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(11)));
				} else {
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9));
				}

				int i5 = ((Integer)this.textureMap.get(string9)).intValue();
				this.setupTexture(bufferedImage4, i5);
				this.blurTexture = false;
				this.clampTexture = false;
			} catch (IOException iOException7) {
				iOException7.printStackTrace();
			}
		}

		Iterator<String> iterator5 = this.textureContentsMap.keySet().iterator();

		while(iterator5.hasNext()) {
			string9 = (String)iterator5.next();

			try {
				if(string9.startsWith("##")) {
					bufferedImage4 = this.unwrapImageByColumns(this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(2))));
				} else if(string9.startsWith("%clamp%")) {
					this.clampTexture = true;
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(7)));
				} else if(string9.startsWith("%blur%")) {
					this.blurTexture = true;
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9.substring(6)));
				} else {
					bufferedImage4 = this.readTextureImage(texturePackBase1.getResourceAsStream(string9));
				}

				this.getImageContents(bufferedImage4, (int[])this.textureContentsMap.get(string9));
				this.blurTexture = false;
				this.clampTexture = false;
			} catch (IOException iOException6) {
				iOException6.printStackTrace();
			}
		}

	}
	private BufferedImage readTextureImage(InputStream inputstream) throws IOException {
		BufferedImage bufferedimage = ImageIO.read(inputstream);
		inputstream.close();
		return bufferedimage;
	}

	public void bindTexture(int i) {
		if(i >= 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, i);
		}
	}

	private void setTextureDimension(int id, Dimension dim) {
		this.textureDimensionsMap.put(new Integer(id), dim);
		if(id == this.terrainTextureId) {
			this.updateDinamicTextures(0, dim);
		}

		if(id == this.guiItemsTextureId) {
			this.updateDinamicTextures(1, dim);
		}

	}

	private Dimension getTextureDimensions(int id) {
		return (Dimension)this.textureDimensionsMap.get(new Integer(id));
	}

	private void updateDinamicTextures(int texNum, Dimension dim) {
		this.checkHdTextures();

		/*
		for(int i = 0; i < this.textureList.size(); ++i) {
			TextureFX tex = (TextureFX)this.textureList.get(i);
			if(tex.tileImage == texNum && tex instanceof TextureHDFX) {
				TextureHDFX texHD = (TextureHDFX)tex;
				texHD.setTexturePackBase(this.texturePack.selectedTexturePack);
				texHD.setTileWidth(dim.width / 16);
				texHD.onTick();
			}
		}
		*/

	}

	public boolean updateCustomTexture(TextureFX texturefx, ByteBuffer imgData, int tileWidth) {
		return false;
		/*
		return 
				texturefx.iconIndex == Block.waterStill.blockIndexInTexture ? 
						(Config.isGeneratedWater() ? 
								false 
							: 
								this.updateCustomTexture(texturefx, "/custom_water_still.png", imgData, tileWidth, Config.isAnimatedWater(), 1)
						) 
					: 
						(texturefx.iconIndex == Block.waterStill.blockIndexInTexture + 1 ? 
								(Config.isGeneratedWater() ? 
										false 
									: 
										this.updateCustomTexture(texturefx, "/custom_water_flowing.png", imgData, tileWidth, Config.isAnimatedWater(), 1)
								) 
							: 
								(texturefx.iconIndex == Block.lavaStill.blockIndexInTexture ? 
										(Config.isGeneratedLava() ? 
												false 
											: 
												this.updateCustomTexture(texturefx, "/custom_lava_still.png", imgData, tileWidth, Config.isAnimatedLava(), 1)
										) 
									: 
										(texturefx.iconIndex == Block.lavaStill.blockIndexInTexture + 1 ? 
												(Config.isGeneratedLava() ? 
														false 
													: 
														this.updateCustomTexture(texturefx, "/custom_lava_flowing.png", imgData, tileWidth, Config.isAnimatedLava(), 1)
												) 
											: 
												(texturefx.iconIndex == Block.portal.blockIndexInTexture ? 
														this.updateCustomTexture(texturefx, "/custom_portal.png", imgData, tileWidth, Config.isAnimatedPortal(), 1) 
													: 
														(texturefx.iconIndex == Block.fire.blockIndexInTexture ? 
																this.updateCustomTexture(texturefx, "/custom_fire_n_s.png", imgData, tileWidth, Config.isAnimatedFire(), 1) 
															: 
																(texturefx.iconIndex == Block.fire.blockIndexInTexture + 16 ? 
																		this.updateCustomTexture(texturefx, "/custom_fire_e_w.png", imgData, tileWidth, Config.isAnimatedFire(), 1)
																	: 
																		false
																)
														)
												)
										)
								)
						);
		*/
	}

	private boolean updateDefaultTexture(TextureFX texturefx, ByteBuffer imgData, int tileWidth) {
		return this.texturePack.selectedTexturePack instanceof TexturePackDefault ? false
				: (texturefx.iconIndex == Block.waterStill.blockIndexInTexture
						? (this.updateDefaultTexture(texturefx, imgData, tileWidth, false, 1))
						: (texturefx.iconIndex == Block.waterStill.blockIndexInTexture + 1
								? (this.updateDefaultTexture(texturefx, imgData, tileWidth, false, 1))
								: (texturefx.iconIndex == Block.lavaStill.blockIndexInTexture
										? (this.updateDefaultTexture(texturefx, imgData, tileWidth, false, 1))
										: (texturefx.iconIndex == Block.lavaStill.blockIndexInTexture + 1
												? (this.updateDefaultTexture(texturefx, imgData, tileWidth, false, 3))
												: false))));
	}

	private boolean updateDefaultTexture(TextureFX texturefx, ByteBuffer imgData, int tileWidth, boolean scrolling, int scrollDiv) {
		int iconIndex = texturefx.iconIndex;
		if(!scrolling && this.dynamicTexturesUpdated) {
			return true;
		} else {
			byte[] tileData = this.getTerrainIconData(iconIndex, tileWidth);
			if(tileData == null) {
				return false;
			} else {
				imgData.clear();
				int imgLen = tileData.length;
				if(scrolling) {
					int movNum = tileWidth - this.tickCounter / scrollDiv % tileWidth;
					int offset = movNum * tileWidth * 4;
					imgData.put(tileData, offset, imgLen - offset);
					imgData.put(tileData, 0, offset);
				} else {
					imgData.put(tileData, 0, imgLen);
				}

				imgData.position(0).limit(imgLen);
				return true;
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean updateCustomTexture(TextureFX texturefx, String imagePath, ByteBuffer imgData, int tileWidth, boolean animated, int animDiv) {
		byte[] imageBytes = this.getCustomTextureData(imagePath, tileWidth);
		if(imageBytes == null) {
			return false;
		} else if(!animated && this.dynamicTexturesUpdated) {
			return true;
		} else {
			int imgLen = tileWidth * tileWidth * 4;
			int imgCount = imageBytes.length / imgLen;
			int imgNum = this.tickCounter / animDiv % imgCount;
			int offset = 0;
			if(animated) {
				offset = imgLen * imgNum;
			}

			imgData.clear();
			imgData.put(imageBytes, offset, imgLen);
			imgData.position(0).limit(imgLen);
			return true;
		}
	}

	private byte[] getTerrainIconData(int tileNum, int tileWidth) {
		String tileIdStr = "Tile-" + tileNum;
		byte[] tileData = this.getCustomTextureData(tileIdStr, tileWidth);
		if(tileData != null) {
			return tileData;
		} else {
			byte[] terrainData = this.getCustomTextureData("/terrain.png", tileWidth * 16);
			if(terrainData == null) {
				return null;
			} else {
				tileData = new byte[tileWidth * tileWidth * 4];
				int tx = tileNum % 16;
				int ty = tileNum / 16;
				int xMin = tx * tileWidth;
				int yMin = ty * tileWidth;
				for(int y = 0; y < tileWidth; ++y) {
					int ys = yMin + y;

					for(int x = 0; x < tileWidth; ++x) {
						int xs = xMin + x;
						int posSrc = 4 * (xs + ys * tileWidth * 16);
						int posDst = 4 * (x + y * tileWidth);
						tileData[posDst + 0] = terrainData[posSrc + 0];
						tileData[posDst + 1] = terrainData[posSrc + 1];
						tileData[posDst + 2] = terrainData[posSrc + 2];
						tileData[posDst + 3] = terrainData[posSrc + 3];
					}
				}

				this.setCustomTextureData(tileIdStr, tileData);
				return tileData;
			}
		}
	}

	public byte[] getCustomTextureData(String imagePath, int tileWidth) {
		byte[] imageBytes = (byte[])((byte[])this.textureDataMap.get(imagePath));
		if(imageBytes == null) {
			if(this.textureDataMap.containsKey(imagePath)) {
				return null;
			}

			imageBytes = this.loadImage(imagePath, tileWidth);
			this.textureDataMap.put(imagePath, imageBytes);
		}

		return imageBytes;
	}

	private void setCustomTextureData(String imagePath, byte[] data) {
		this.textureDataMap.put(imagePath, data);
	}

	private byte[] loadImage(String name, int targetWidth) {
		try {
			TexturePackBase e = this.texturePack.selectedTexturePack;
			if(e == null) {
				return null;
			} else {
				InputStream in = e.getResourceAsStream(name);
				if(in == null) {
					return null;
				} else {
					BufferedImage image = this.readTextureImage(in);
					if(image == null) {
						return null;
					} else {
						if(targetWidth > 0 && image.getWidth() != targetWidth) {
							double width = (double)(image.getHeight() / image.getWidth());
							int ai = (int)((double)targetWidth * width);
							image = scaleBufferedImage(image, targetWidth, ai);
						}

						int i19 = image.getWidth();
						int height = image.getHeight();
						int[] i20 = new int[i19 * height];
						byte[] byteBuf = new byte[i19 * height * 4];
						image.getRGB(0, 0, i19, height, i20, 0, i19);

						for(int l = 0; l < i20.length; ++l) {
							int alpha = i20[l] >> 24 & 255;
							int red = i20[l] >> 16 & 255;
							int green = i20[l] >> 8 & 255;
							int blue = i20[l] & 255;
							if(this.options != null && GameSettingsValues.anaglyph) {
								int j3 = (red * 30 + green * 59 + blue * 11) / 100;
								int l3 = (red * 30 + green * 70) / 100;
								int j4 = (red * 30 + blue * 70) / 100;
								red = j3;
								green = l3;
								blue = j4;
							}

							byteBuf[l * 4 + 0] = (byte)red;
							byteBuf[l * 4 + 1] = (byte)green;
							byteBuf[l * 4 + 2] = (byte)blue;
							byteBuf[l * 4 + 3] = (byte)alpha;
						}

						return byteBuf;
					}
				}
			}
		} catch (Exception exception18) {
			exception18.printStackTrace();
			return null;
		}
	}

	public static BufferedImage scaleBufferedImage(BufferedImage image, int width, int height) {
		BufferedImage scaledImage = new BufferedImage(width, height, 2);
		Graphics2D gr = scaledImage.createGraphics();
		gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gr.drawImage(image, 0, 0, width, height, (ImageObserver)null);
		return scaledImage;
	}

	private void checkImageDataSize(int width) {
		if(this.imageData != null) {
			int len = width * width * 4;
			if(this.imageData.capacity() >= len) {
				return;
			}
		}

		this.allocateImageData(width);
	}

	private void allocateImageData(int width) {
		int imgLen = width * width * 4;
		this.imageData = GLAllocation.createDirectByteBuffer(imgLen);
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();

		for(int mipWidth = width / 2; mipWidth > 0; mipWidth /= 2) {
			int mipLen = mipWidth * mipWidth * 4;
			ByteBuffer buf = GLAllocation.createDirectByteBuffer(mipLen);
			list.add(buf);
		}

		this.mipImageDatas = (ByteBuffer[])((ByteBuffer[])list.toArray(new ByteBuffer[list.size()]));
	}

	public void checkHdTextures() {
		// No, sorry
	}

	private int getMaxMipmapLevel(int size) {
		int level;
		for(level = 0; size > 0; ++level) {
			size /= 2;
		}

		return level - 1;
	}

	private void copyScaled(byte[] buf, ByteBuffer dstBuf, int dstWidth) {
		int srcWidth = (int)Math.sqrt((double)(buf.length / 4));
		int scale = dstWidth / srcWidth;
		byte[] buf4 = new byte[4];
		dstBuf.clear();
		if(scale > 1) {
			for(int y = 0; y < srcWidth; ++y) {
				int yMul = y * srcWidth;
				int ty = y * scale;
				int tyMul = ty * dstWidth;

				for(int x = 0; x < srcWidth; ++x) {
					int srcPos = (x + yMul) * 4;
					buf4[0] = buf[srcPos];
					buf4[1] = buf[srcPos + 1];
					buf4[2] = buf[srcPos + 2];
					buf4[3] = buf[srcPos + 3];
					int tx = x * scale;
					int dstPosBase = tx + tyMul;

					for(int tdy = 0; tdy < scale; ++tdy) {
						int dstPosY = dstPosBase + tdy * dstWidth;
						dstBuf.position(dstPosY * 4);

						for(int tdx = 0; tdx < scale; ++tdx) {
							dstBuf.put(buf4);
						}
					}
				}
			}
		}

		dstBuf.position(0).limit(dstWidth * dstWidth * 4);
	}

	private boolean scalesWithFastColor(TextureFX texturefx) {
		return !texturefx.getClass().getName().equals("ModTextureStatic");
	}
}
