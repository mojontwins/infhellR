package net.minecraft.client.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.Tessellator;

/**
 * Base class for all in-game and menu screens (GUI overlays).
 *
 * <p>Screens are pushed onto a stack in the client and rendered on top of the world
 * (or as the full background when no world is loaded). Input events are dispatched
 * from {@link Minecraft#runTick} when a screen is active.</p>
 *
 * <p>The screen coordinate system uses the GUI's own pixel dimensions
 * ({@link #width}, {@link #height}), not the display resolution. The
 * {@link ScaledResolution} class converts between the two.</p>
 *
 * <p>Subclasses override:</p>
 * <ul>
 *   <li>{@link #initGui()} — populate buttons/controls when the screen opens.</li>
 *   <li>{@link #drawScreen(int, int, float)} — draw the screen content.</li>
 *   <li>{@link #actionPerformed(GuiButton)} — handle button clicks.</li>
 *   <li>{@link #updateScreen()} — animate/tick the screen.</li>
 *   <li>{@link #keyTyped(char, int)} — handle keyboard input.</li>
 * </ul>
 */
public class GuiScreen extends Gui {
    /** Reference to the client instance. */
    protected Minecraft mc;
    /** Screen width in GUI pixels (may be scaled). */
    public int width;
    /** Screen height in GUI pixels (may be scaled). */
    public int height;
    /** All interactive controls currently on this screen. */
    protected List<GuiButton> controlList = new ArrayList<GuiButton>();
    /** When true, keyboard input is forwarded to the active screen even during gameplay. */
    public boolean allowUserInput = false;
    /** Font renderer for drawing text on this screen. */
    protected FontRenderer fontRenderer;
    /** Particle system for background effects (chat, etc.). */
    public GuiParticle guiParticles;
    /** The button currently being held down by the mouse (for drag-release detection). */
    private GuiButton selectedButton = null;
    /** The button currently under the mouse cursor. */
    private GuiButton hoveredButton = null;
    /** Last mouse X position, used for tooltip delay. */
    int lastMouseX = 0;
    /** Last mouse Y position, used for tooltip delay. */
    int lastMouseY = 0;
    /** Timestamp (from {@code System.currentTimeMillis()}) when the mouse last moved. */
    private long mouseStillTime;
    /** True once the tooltip delay has elapsed and the tooltip should be shown. */
    private boolean showingTooltip;

    /**
     * Renders the screen and all its controls.
     *
     * @param mouseX current mouse X position (screen pixels)
     * @param mouseY current mouse Y position (screen pixels)
     * @param renderPartialTicks partial tick for smooth animations
     */
    public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
        GuiButton lastHoveredButton = this.hoveredButton;
        this.hoveredButton = null;

        // Draw all controls and track which one the mouse is over.
        for (int i = 0; i < this.controlList.size(); ++i) {
            GuiButton control = this.controlList.get(i);
            control.drawButton(this.mc, mouseX, mouseY);

            if (control.drawButton
                    && mouseX >= control.xPosition && mouseX <= control.xPosition + control.width
                    && mouseY >= control.yPosition && mouseY <= control.yPosition + control.height) {
                this.hoveredButton = control;
            }
        }

        if (lastHoveredButton != this.hoveredButton) {
            this.showingTooltip = false;
        }

        if (this.showingTooltip && this.hoveredButton != null && this.hoveredButton.toolTip != null) {
            String[] lines = this.hoveredButton.toolTip.split("\n");
            int maxTextWidth = 0;
            for (String line : lines) {
                int tw = fontRenderer.getStringWidth(line);
                if (tw > maxTextWidth) maxTextWidth = tw;
            }

            int x1 = mouseX - 2 - maxTextWidth / 2;
            if (x1 < 0) x1 = 0;

            int y1 = mouseY - 2;
            if (y1 < 0) y1 = 0;

            int x2 = x1 + maxTextWidth + 4;
            if (x2 > this.width) x1 = this.width - maxTextWidth - 4;

            int y2 = y1 + 2 + 10 * lines.length;
            if (y2 > this.height) y1 = this.height - 2 - 10 * lines.length;

            this.drawGradientRect(x1, y1, x2, y2, -536870912, -536870912);

            int y = y1 + 2;
            for (String line : lines) {
                this.drawString(fontRenderer, line, x1 + 2, y, 0xFFFFFF);
                y += 10;
            }
        } else {
            if (Math.abs(this.lastMouseX - mouseX) < 16 && Math.abs(this.lastMouseY - mouseY) < 16) {
                if (System.currentTimeMillis() >= this.mouseStillTime + 600) {
                    this.showingTooltip = true;
                }
            } else {
                this.lastMouseX = mouseX;
                this.lastMouseY = mouseY;
                this.mouseStillTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * Handles a key press. Default action: Escape closes the screen.
     *
     * @param c     the typed character
     * @param keyCode the LWJGL key code
     */
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == 1) {  // Escape
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }

    /**
     * Returns the clipboard text, or {@code null} if unavailable.
     *
     * @return clipboard contents, or null
     */
    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Handles a mouse button press. Left-clicks all buttons and triggers
     * {@link #actionPerformed} for the first hit.
     *
     * @param mouseX     mouse X in screen pixels
     * @param mouseY     mouse Y in screen pixels
     * @param mouseButton 0 = left, 1 = right, 2 = middle
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.showingTooltip = false;
        this.mouseStillTime = System.currentTimeMillis();

        if (mouseButton == 0) {
            for (int i = 0; i < this.controlList.size(); ++i) {
                GuiButton button = this.controlList.get(i);
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    this.selectedButton = button;
                    this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
                    this.actionPerformed(button);
                }
            }
        }
    }

    /**
     * Called when a held mouse button is released. Triggers {@link GuiButton#mouseReleased}.
     *
     * @param mouseX     mouse X in screen pixels
     * @param mouseY     mouse Y in screen pixels
     * @param mouseButton 0 = left, 1 = right, 2 = middle
     */
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton) {
        if (this.selectedButton != null && mouseButton == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    /**
     * Called when a button is clicked. Override to handle button actions.
     *
     * @param button the button that was clicked
     */
    protected void actionPerformed(GuiButton button) {
    }

    /**
     * Initializes the screen, called by {@link #setWorldAndResolution}.
     * Populate {@link #controlList} with buttons and other controls here.
     */
    public void initGui() {
    }

    /**
     * Sets the world reference and screen resolution, then rebuilds the screen.
     *
     * @param minecraft the client instance
     * @param width    screen width in GUI pixels
     * @param height   screen height in GUI pixels
     */
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        this.guiParticles = new GuiParticle(minecraft);
        this.mc = minecraft;
        this.fontRenderer = minecraft.fontRenderer;
        this.width = width;
        this.height = height;
        this.controlList.clear();
        this.initGui();
    }

    /**
     * Polls all pending input events and dispatches them.
     */
    public void handleInput() {
        while (Mouse.next()) {
            this.handleMouseInput();
        }
        while (Keyboard.next()) {
            this.handleKeyboardInput();
        }
    }

    /**
     * Dispatches a mouse event from the global mouse queue.
     */
    public void handleMouseInput() {
        int mouseX;
        int mouseY;
        if (Mouse.getEventButtonState()) {
            mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
        } else {
            mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.mouseMovedOrUp(mouseX, mouseY, Mouse.getEventButton());
        }
    }

    /**
     * Dispatches a keyboard event from the global keyboard queue.
     */
    public void handleKeyboardInput() {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
                this.mc.toggleFullscreen();
                return;
            }
            this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }
    }

    /**
     * Called once per frame to tick screen animations.
     */
    public void updateScreen() {
    }

    /** Called when the screen is closed. Use to restore game state. */
    public void onGuiClosed() {
    }

    /**
     * Draws the world background (blurred terrain or dirt texture) behind the GUI.
     */
    public void drawDefaultBackground() {
        this.drawWorldBackground(0);
    }

    /**
     * Draws either the blurred world background (when a world is loaded) or the
     * static dirt-texture background.
     *
     * @param parallaxOffset vertical scroll offset for the background texture
     */
    public void drawWorldBackground(int parallaxOffset) {
        if (this.mc.theWorld != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.drawBackground(parallaxOffset);
        }
    }

    /**
     * Draws the static dirt-texture background shown on the main menu and
     * other menus without a loaded world.
     *
     * @param parallaxOffset vertical scroll offset for the background texture
     */
    public void drawBackground(int parallaxOffset) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float texScale = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(4210752);
        tessellator.addVertexWithUV(0.0D, this.height, 0.0D, 0.0D, this.height / texScale + parallaxOffset);
        tessellator.addVertexWithUV(this.width, this.height, 0.0D, this.width / texScale, this.height / texScale + parallaxOffset);
        tessellator.addVertexWithUV(this.width, 0.0D, 0.0D, this.width / texScale, parallaxOffset);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, parallaxOffset);
        tessellator.draw();
    }

    /**
     * Returns true if this screen pauses the game while it is open.
     *
     * @return true if the game should pause
     */
    public boolean doesGuiPauseGame() {
        return true;
    }

    /**
     * Called when the player confirms or cancels world deletion.
     *
     * @param confirmed true if confirmed
     * @param worldIndex index of the world to delete
     */
    public void deleteWorld(boolean confirmed, int worldIndex) {
    }

    /** Advances focus to the next text field. */
    public void selectNextField() {
    }
}
