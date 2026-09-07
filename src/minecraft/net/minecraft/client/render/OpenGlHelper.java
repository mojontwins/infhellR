package net.minecraft.client.render;

import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

public class OpenGlHelper {
	public static int defaultTexUnit;
	public static int lightmapTexUnit;
	private static boolean useMultitextureARB = false;

	public static void initializeTextures() {
		useMultitextureARB = GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13;
		if(useMultitextureARB) {
			defaultTexUnit = GL13.GL_TEXTURE0;
			lightmapTexUnit = GL13.GL_TEXTURE1;
		} else {
			defaultTexUnit = GL13.GL_TEXTURE0;
			lightmapTexUnit = GL13.GL_TEXTURE1;
		}

	}

	public static void setActiveTexture(int i0) {
		if(useMultitextureARB) {
			ARBMultitexture.glActiveTextureARB(i0);
			// FIX FOR INTEL CARDS: Keep client texture state synced on older systems
			ARBMultitexture.glClientActiveTextureARB(i0); 
		} else {
			GL13.glActiveTexture(i0);
			// FIX FOR INTEL CARDS: Keep client texture state synced on older systems
			GL13.glClientActiveTexture(i0); 
		}

	}

	public static void setClientActiveTexture(int i0) {
		if(useMultitextureARB) {
			ARBMultitexture.glClientActiveTextureARB(i0);
		} else {
			GL13.glClientActiveTexture(i0);
		}

	}

	public static void setLightmapTextureCoords(int i0, float f1, float f2) {
		if(useMultitextureARB) {
			ARBMultitexture.glMultiTexCoord2fARB(i0, f1, f2);
		} else {
			GL13.glMultiTexCoord2f(i0, f1, f2);
		}

	}
}
