package net.minecraft.client;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.render.Tessellator;

/**
 * Full-screen loading/progress overlay rendered during world transitions.
 *
 * <p>Implements {@link IProgressUpdate} so it can be passed directly to save/load
 * operations. Displays:</p>
 * <ul>
 *   <li>A tiled background texture (dirt pattern).</li>
 *   <li>The "loading" headline string (e.g. "Loading terrain").</li>
 *   <li>A secondary message string (e.g. "Building terrain").</li>
 *   <li>A progress bar when {@link #setLoadingProgress} is called with {@code progress >= 0}.</li>
 * </ul>
 *
 * <p>The progress bar is split into two quads: the base bar in dark grey and the
 * filled portion in green, updating at most once per 20 ms to avoid flooding the
 * display swap.</p>
 */
public class LoadingScreenRenderer implements IProgressUpdate {
    /** Secondary/detailed message (e.g. "Building terrain"). */
    private String loadingString = "";
    /** Reference to the client instance (used for display dimensions and fonts). */
    private Minecraft minecraft;
    /** Primary headline message displayed above the progress bar. */
    private String currentlyDisplayedText = "";
    /** Timestamp of the last progress bar update (ms) — throttled to 20 ms. */
    private long timeElapsed = System.currentTimeMillis();
    /** True when the current operation is a world save (changes bar display behavior). */
    private boolean isSaving = false;

    public LoadingScreenRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    /**
     * Sets the loading headline message and resets the save flag.
     *
     * @param text the message to display (e.g. "Loading world")
     */
    public void printText(String text) {
        this.isSaving = false;
        this.setTextAndResetMatrix(text);
    }

    /**
     * Switches to "saving" mode and preserves the current message.
     *
     * @param text ignored — the message was already set by {@link #printText}
     */
    public void displaySavingString(String text) {
        this.isSaving = true;
        this.setTextAndResetMatrix(this.currentlyDisplayedText);
    }

    /**
     * Sets the current text and rebuilds the orthographic projection matrix
     * so that pixel-space coordinates work for the loading screen.
     *
     * @param text the headline message
     */
    private void setTextAndResetMatrix(String text) {
        if (!this.minecraft.running) {
            if (!this.isSaving) {
                throw new MinecraftError();
            }
        } else {
            this.currentlyDisplayedText = text;
            ScaledResolution res = new ScaledResolution(this.minecraft.gameSettings, this.minecraft.displayWidth, this.minecraft.displayHeight);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, res.scaledWidthD, res.scaledHeightD, 0.0D, 100.0D, 300.0D);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -200.0F);
        }
    }

    /**
     * Displays a secondary loading message and resets the elapsed timer.
     *
     * @param text the secondary message
     */
    public void displayLoadingString(String text) {
        if (!this.minecraft.running) {
            if (!this.isSaving) {
                throw new MinecraftError();
            }
        } else {
            this.timeElapsed = 0L;
            this.loadingString = text;
            this.setLoadingProgress(-1);
            this.timeElapsed = 0L;
        }
    }

    /**
     * Draws the progress bar if enough time has elapsed since the last draw.
     *
     * @param progress value from 0 (empty) to 100 (full), or -1 to suppress the bar
     */
    public void setLoadingProgress(int progress) {
        if (!this.minecraft.running) {
            if (!this.isSaving) {
                throw new MinecraftError();
            }
        } else {
            long now = System.currentTimeMillis();
            if (now - this.timeElapsed < 20L) {
                return;  // throttle: draw at most every 20 ms
            }
            this.timeElapsed = now;

            ScaledResolution res = new ScaledResolution(this.minecraft.gameSettings, this.minecraft.displayWidth, this.minecraft.displayHeight);
            int screenWidth = res.getScaledWidth();
            int screenHeight = res.getScaledHeight();
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, res.scaledWidthD, res.scaledHeightD, 0.0D, 100.0D, 300.0D);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -200.0F);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Tiled background texture.
            Tessellator tess = Tessellator.instance;
            int bgTex = this.minecraft.renderEngine.getTexture("/gui/background.png");
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, bgTex);
            float texScale = 32.0F;
            tess.startDrawingQuads();
            tess.setColorOpaque_I(4210752);
            tess.addVertexWithUV(0.0D, screenHeight, 0.0D, 0.0D, screenHeight / texScale);
            tess.addVertexWithUV(screenWidth, screenHeight, 0.0D, screenWidth / texScale, screenHeight / texScale);
            tess.addVertexWithUV(screenWidth, 0.0D, 0.0D, screenWidth / texScale, 0.0D);
            tess.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
            tess.draw();

            // Progress bar (background + filled portion).
            if (progress >= 0) {
                byte barWidth = 100;
                byte barHeight = 2;
                int barX = screenWidth / 2 - barWidth / 2;
                int barY = screenHeight / 2 + 16;
                GL11.glDisable(GL11.GL_TEXTURE_2D);

                // Background bar (dark grey).
                tess.startDrawingQuads();
                tess.setColorOpaque_I(8421504);
                tess.addVertex(barX,              barY,              0.0D);
                tess.addVertex(barX,              barY + barHeight,  0.0D);
                tess.addVertex(barX + barWidth,   barY + barHeight,  0.0D);
                tess.addVertex(barX + barWidth,   barY,              0.0D);
                tess.draw();

                // Filled portion (green).
                tess.startDrawingQuads();
                tess.setColorOpaque_I(8454016);
                tess.addVertex(barX,              barY,              0.0D);
                tess.addVertex(barX,              barY + barHeight,  0.0D);
                tess.addVertex(barX + progress,   barY + barHeight,  0.0D);
                tess.addVertex(barX + progress,   barY,              0.0D);
                tess.draw();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            // Loading messages.
            this.minecraft.fontRenderer.drawStringWithShadow(
                this.currentlyDisplayedText,
                (screenWidth - this.minecraft.fontRenderer.getStringWidth(this.currentlyDisplayedText)) / 2,
                screenHeight / 2 - 4 - 16,
                0xFFFFFF);
            this.minecraft.fontRenderer.drawStringWithShadow(
                this.loadingString,
                (screenWidth - this.minecraft.fontRenderer.getStringWidth(this.loadingString)) / 2,
                screenHeight / 2 - 4 + 8,
                0xFFFFFF);

            Display.update();
            Thread.yield();
        }
    }
}
