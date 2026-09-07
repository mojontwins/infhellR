package net.minecraft.client.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

/* 
 * Absolute minimum to read a png from this jar: the biome map
 */

public class RenderEngineMin {
	private HashMap<String,Object> textureContentsMap = new HashMap<String, Object>();
	
	public RenderEngineMin() {
		// TODO Auto-generated constructor stub
	}

	public int[] getTextureContents(String string1) {
		int[] i3 = (int[])this.textureContentsMap.get(string1);
		if(i3 != null) {
			return i3;
		} else {
			try {
				InputStream inputStream7 = RenderEngineMin.class.getResourceAsStream(string1);
				if(inputStream7 == null) {
					throw new Exception();
				} else {
					i3 = this.getImageContentsAndAllocate(this.readTextureImage(inputStream7));
				}

				this.textureContentsMap.put(string1, i3);
				return i3;
			} catch (Exception e) {
				e.printStackTrace();
				return new int[4096];
			}
		}
	}

	private BufferedImage readTextureImage(InputStream inputStream1) throws IOException {
		BufferedImage bufferedImage2 = ImageIO.read(inputStream1);
		inputStream1.close();
		return bufferedImage2;
	}
	
	private int[] getImageContentsAndAllocate(BufferedImage bufferedImage1) {
		int i2 = bufferedImage1.getWidth();
		int i3 = bufferedImage1.getHeight();
		int[] i4 = new int[i2 * i3];
		bufferedImage1.getRGB(0, 0, i2, i3, i4, 0, i2);
		return i4;
	}
}
