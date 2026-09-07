package net.minecraft.client;

import java.awt.Component;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import net.minecraft.client.render.GLAllocation;

/**
 * Wraps the LWJGL mouse input and provides cursor grab/ungrab and delta accumulation.
 *
 * <p>When the cursor is grabbed (via {@link #grabMouseCursor}), LWJGL captures all
 * mouse movement and hides the OS cursor. The accumulated {@link #deltaX}/{@link #deltaY}
 * deltas are read each frame to drive the player's view rotation.</p>
 */
public class MouseHelper {
    /** The AWT component whose coordinate space is used for ungrabbed cursor positioning. */
    private Component windowComponent;
    /** The OS cursor handle used for custom cursors. */
    private Cursor cursor;
    /** Accumulated mouse X delta since the last frame (set by {@link #mouseXYChange}). */
    public int deltaX;
    /** Accumulated mouse Y delta since the last frame (set by {@link #mouseXYChange}). */
    public int deltaY;

    public MouseHelper(Component component) {
        this.windowComponent = component;
        // Create a 32x32 transparent cursor to hide the OS cursor when grabbed.
        IntBuffer zeroBuf = GLAllocation.createDirectIntBuffer(1);
        zeroBuf.put(0);
        zeroBuf.flip();
        IntBuffer imageBuf = GLAllocation.createDirectIntBuffer(1024);

        try {
            this.cursor = new Cursor(32, 32, 16, 16, 1, imageBuf, zeroBuf);
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    /** Captures the mouse and hides the OS cursor (enables look-around in-game). */
    public void grabMouseCursor() {
        Mouse.setGrabbed(true);
        this.deltaX = 0;
        this.deltaY = 0;
    }

    /**
     * Releases the mouse grab and re-centers the OS cursor at the center of the window.
     * Call this when returning control to the OS (e.g. opening a GUI).
     */
    public void ungrabMouseCursor() {
        Mouse.setCursorPosition(this.windowComponent.getWidth() / 2, this.windowComponent.getHeight() / 2);
        Mouse.setGrabbed(false);
    }

    /**
     * Reads and accumulates the current frame's mouse movement deltas.
     * Call once per frame before processing input.
     */
    public void mouseXYChange() {
        this.deltaX = Mouse.getDX();
        this.deltaY = Mouse.getDY();
    }

    /** @return the OS cursor handle, or {@code null} if construction failed. */
    public Cursor getCursor() {
        return this.cursor;
    }

    /** Sets a new OS cursor handle. */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
