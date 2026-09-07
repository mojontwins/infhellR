package net.minecraft.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Captures the current framebuffer and saves it as a PNG file.
 *
 * <p>The screenshot is saved to {@code <minecraftDir>/screenshots/} with a filename
 * in the format {@code yyyy-MM-dd_HH.mm.ss.png}. If a file with that name already
 * exists, numeric suffixes are appended (e.g. {@code _2.png}, {@code _3.png}).</p>
 *
 * <p>Pixel data is read via {@code glReadPixels} (bottom-up, RGB), then flipped
 * vertically to correct for OpenGL's coordinate system and written into an ARGB
 * {@link BufferedImage} for encoding.</p>
 */
public class ScreenShotHelper {
    /** Date formatter used to generate screenshot filenames. */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    /** Reusable pixel buffer for {@code glReadPixels}. Sized lazily to the required dimensions. */
    private static ByteBuffer pixelBuffer;
    /** Temporary byte array holding raw RGB data before conversion. */
    private static byte[] pixelData;
    /** ARGB int array used to construct the {@link BufferedImage}. */
    private static int[] imageData;

    /**
     * Reads the current framebuffer, flips it vertically, and saves it as a PNG.
     *
     * @param minecraftDir the Minecraft data directory (screenshots are saved under {@code screenshots/})
     * @param width       framebuffer width in pixels
     * @param height      framebuffer height in pixels
     * @return a status message, e.g. "Saved screenshot as 2024-01-15_14.30.00.png"
     */
    public static String saveScreenshot(File minecraftDir, int width, int height) {
        try {
            File screenshotsDir = new File(minecraftDir, "screenshots");
            screenshotsDir.mkdir();

            // Lazily allocate or resize the pixel buffer to fit width * height pixels (RGB = 3 bytes each).
            int pixelCount = width * height;
            if (pixelBuffer == null || pixelBuffer.capacity() < pixelCount * 3) {
                pixelBuffer = BufferUtils.createByteBuffer(pixelCount * 3);
            }

            if (imageData == null || imageData.length < pixelCount) {
                pixelData = new byte[pixelCount * 3];
                imageData = new int[pixelCount];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
            pixelBuffer.clear();

            String timestamp = dateFormat.format(new Date());

            // Find the first unused filename (append _2, _3, ... if necessary).
            File outputFile;
            int suffix = 1;
            while ((outputFile = new File(screenshotsDir, timestamp + (suffix == 1 ? "" : "_" + suffix) + ".png")).exists()) {
                suffix++;
            }

            // Read pixels from the buffer into the byte array.
            pixelBuffer.get(pixelData);

            // Flip vertically: OpenGL reads bottom-to-top, but BufferedImage expects top-to-bottom.
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int flippedIndex = x + (height - y - 1) * width;
                    int srcIndex = flippedIndex * 3;
                    int r = pixelData[srcIndex + 0] & 255;
                    int g = pixelData[srcIndex + 1] & 255;
                    int b = pixelData[srcIndex + 2] & 255;
                    int argb = 0xFF000000 | (r << 16) | (g << 8) | b;
                    imageData[x + y * width] = argb;
                }
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, imageData, 0, width);
            ImageIO.write(image, "png", outputFile);
            return "Saved screenshot as " + outputFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to save: " + e;
        }
    }
}
