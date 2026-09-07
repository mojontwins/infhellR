package net.minecraft.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.Tessellator;

/**
 * Abstract base class for scrollable lists in the GUI system.
 *
 * <p>Handles all scroll mechanics including mouse-wheel input, click-drag scrolling,
 * and the scrollbar thumb. Subclasses implement {@link #drawSlot} to render each row
 * and optionally override {@link #drawHeader} and {@link #drawFooter} for custom
 * overlays above and below the list.</p>
 *
 * <p>Template methods for subclasses:</p>
 * <ul>
 *   <li>{@link #getSize()} — number of elements</li>
 *   <li>{@link #elementClicked(int, boolean)} — handle element click/double-click</li>
 *   <li>{@link #isSelected(int)} — true if element is selected (tinted)</li>
 *   <li>{@link #drawBackground()} — draw behind the list</li>
 *   <li>{@link #drawSlot(int, int, int, int, Tessellator)} — draw a single row</li>
 * </ul>
 */
public abstract class GuiSlot {
    private final Minecraft minecraft;
    private final int width;
    private final int height;
    protected final int top;
    protected final int bottom;
    private final int right;
    private final int left;
    /** Row height in pixels. */
    protected final int rowHeight;
    private int scrollUpButtonID;
    private int scrollDownButtonID;
    private float initialClickY = -2.0F;
    private float scrollMultiplier;
    private float amountScrolled;
    private int selectedElement = -1;
    private long lastClicked = 0L;
    /** When false, the selection highlight is not drawn. */
    private boolean renderSelectionBox = true;
    /** When true, a header overlay is drawn above the list. */
    private boolean renderHeader = false;
    /** Height in pixels of the header overlay (used to offset scroll). */
    private int headerHeight = 0;

    public GuiSlot(Minecraft minecraft, int width, int height, int top, int bottom, int rowHeight) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.rowHeight = rowHeight;
        this.left = 0;
        this.right = width;
    }

    /**
     * Controls whether the selection highlight is rendered behind selected items.
     *
     * @param visible true to draw selection box, false to hide
     */
    public void setRenderSelectionBox(boolean visible) {
        this.renderSelectionBox = visible;
    }

    /**
     * Enables or disables the header overlay and sets its height.
     *
     * @param render true to draw header, false to disable
     * @param headerHeightPixels height of the header in pixels
     */
    protected void setRenderHeader(boolean render, int headerHeightPixels) {
        this.renderHeader = render;
        this.headerHeight = headerHeightPixels;
        if (!render) {
            this.headerHeight = 0;
        }
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight() {
        return this.getSize() * this.rowHeight + this.headerHeight;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int index, int x, int y, int height, Tessellator tessellator);

    /**
     * Draws an overlay above the scroll list (between the top edge and the first visible row).
     * Default implementation is empty. Override to draw custom headers.
     *
     * @param listX left edge of the list area
     * @param listY top edge of the first visible row (including scroll offset)
     * @param tessellator the active tessellator
     */
    protected void drawHeader(int listX, int listY, Tessellator tessellator) {
    }

    /**
     * Draws an overlay below the scroll list. Default implementation is empty.
     * Override to draw custom footers.
     *
     * @param mouseX current mouse X
     * @param mouseY current mouse Y
     */
    protected void drawFooter(int mouseX, int mouseY) {
    }

    /**
     * Hit-tests the list at the given mouse position.
     *
     * @param mouseX mouse X in screen pixels
     * @param mouseY mouse Y in screen pixels
     * @return the list index under the mouse, or -1 if outside the list area
     */
    public int getSlotAtMouse(int mouseX, int mouseY) {
        int listLeft = this.width / 2 - 110;
        int listRight = this.width / 2 + 110;
        int listY = mouseY - this.top - this.headerHeight + (int) this.amountScrolled - 4;
        int listIndex = listY / this.rowHeight;
        return mouseX >= listLeft && mouseX <= listRight && listIndex >= 0 && listY >= 0 && listIndex < this.getSize() ? listIndex : -1;
    }

    public void registerScrollButtons(List<GuiButton> list, int upButtonId, int downButtonId) {
        this.scrollUpButtonID = upButtonId;
        this.scrollDownButtonID = downButtonId;
    }

    private void clampScrollAmount() {
        int maxScroll = this.getContentHeight() - (this.bottom - this.top - 4);
        if (maxScroll < 0) {
            maxScroll /= 2;
        }
        if (this.amountScrolled < 0.0F) {
            this.amountScrolled = 0.0F;
        }
        if (this.amountScrolled > (float) maxScroll) {
            this.amountScrolled = (float) maxScroll;
        }
    }

    public void actionPerformed(GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.scrollUpButtonID) {
                this.amountScrolled -= (float) (this.rowHeight * 2 / 3);
                this.initialClickY = -2.0F;
                this.clampScrollAmount();
            } else if (guiButton.id == this.scrollDownButtonID) {
                this.amountScrolled += (float) (this.rowHeight * 2 / 3);
                this.initialClickY = -2.0F;
                this.clampScrollAmount();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground();
        int listSize = this.getSize();
        int scrollBarLeft = this.width / 2 + 124;
        int scrollBarRight = scrollBarLeft + 6;
        int maxScroll;
        if (Mouse.isButtonDown(0)) {
            if (this.initialClickY == -1.0F) {
                boolean canScroll = true;
                if (mouseY >= this.top && mouseY <= this.bottom) {
                    int scrollLeft = this.width / 2 - 110;
                    int scrollRight = this.width / 2 + 110;
                    int listY = mouseY - this.top - this.headerHeight + (int) this.amountScrolled - 4;
                    int listIndex = listY / this.rowHeight;
                    if (mouseX >= scrollLeft && mouseX <= scrollRight && listIndex >= 0 && listY >= 0 && listIndex < listSize) {
                        boolean isDoubleClick = listIndex == this.selectedElement && System.currentTimeMillis() - this.lastClicked < 250L;
                        this.elementClicked(listIndex, isDoubleClick);
                        this.selectedElement = listIndex;
                        this.lastClicked = System.currentTimeMillis();
                    } else if (mouseX >= scrollLeft && mouseX <= scrollRight && listY < 0) {
                        this.drawFooter(mouseX - scrollLeft, mouseY - this.top + (int) this.amountScrolled - 4);
                        canScroll = false;
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        this.scrollMultiplier = -1.0F;
                        maxScroll = this.getContentHeight() - (this.bottom - this.top - 4);
                        if (maxScroll < 1) {
                            maxScroll = 1;
                        }
                        int scrollThumbHeight = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getContentHeight());
                        if (scrollThumbHeight < 32) {
                            scrollThumbHeight = 32;
                        }
                        if (scrollThumbHeight > this.bottom - this.top - 8) {
                            scrollThumbHeight = this.bottom - this.top - 8;
                        }
                        this.scrollMultiplier /= (float) (this.bottom - this.top - scrollThumbHeight) / (float) maxScroll;
                    } else {
                        this.scrollMultiplier = 1.0F;
                    }

                    if (canScroll) {
                        this.initialClickY = (float) mouseY;
                    } else {
                        this.initialClickY = -2.0F;
                    }
                } else {
                    this.initialClickY = -2.0F;
                }
            } else if (this.initialClickY >= 0.0F) {
                this.amountScrolled -= ((float) mouseY - this.initialClickY) * this.scrollMultiplier;
                this.initialClickY = (float) mouseY;
            }
        } else {
            this.initialClickY = -1.0F;
        }

        this.clampScrollAmount();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.renderEngine.getTexture("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float texScale = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(2105376);
        tessellator.addVertexWithUV((double) this.left, (double) this.bottom, 0.0D, (double) ((float) this.left / texScale), (double) ((float) (this.bottom + (int) this.amountScrolled) / texScale));
        tessellator.addVertexWithUV((double) this.right, (double) this.bottom, 0.0D, (double) ((float) this.right / texScale), (double) ((float) (this.bottom + (int) this.amountScrolled) / texScale));
        tessellator.addVertexWithUV((double) this.right, (double) this.top, 0.0D, (double) ((float) this.right / texScale), (double) ((float) (this.top + (int) this.amountScrolled) / texScale));
        tessellator.addVertexWithUV((double) this.left, (double) this.top, 0.0D, (double) ((float) this.left / texScale), (double) ((float) (this.top + (int) this.amountScrolled) / texScale));
        tessellator.draw();
        int listX = this.width / 2 - 92 - 16;
        int listY = this.top + 4 - (int) this.amountScrolled;
        if (this.renderHeader) {
            this.drawHeader(listX, listY, tessellator);
        }

        int slotY;
        for (maxScroll = 0; maxScroll < listSize; ++maxScroll) {
            int rowY = listY + maxScroll * this.rowHeight + this.headerHeight;
            int rowInnerHeight = this.rowHeight - 4;
            if (rowY <= this.bottom && rowY + rowInnerHeight >= this.top) {
                if (this.renderSelectionBox && this.isSelected(maxScroll)) {
                    int selectionLeft = this.width / 2 - 110;
                    int selectionRight = this.width / 2 + 110;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    tessellator.setColorOpaque_I(8421504);
                    tessellator.addVertexWithUV((double) selectionLeft, (double) (rowY + rowInnerHeight + 2), 0.0D, 0.0D, 1.0D);
                    tessellator.addVertexWithUV((double) selectionRight, (double) (rowY + rowInnerHeight + 2), 0.0D, 1.0D, 1.0D);
                    tessellator.addVertexWithUV((double) selectionRight, (double) (rowY - 2), 0.0D, 1.0D, 0.0D);
                    tessellator.addVertexWithUV((double) selectionLeft, (double) (rowY - 2), 0.0D, 0.0D, 0.0D);
                    tessellator.setColorOpaque_I(0);
                    tessellator.addVertexWithUV((double) (selectionLeft + 1), (double) (rowY + rowInnerHeight + 1), 0.0D, 0.0D, 1.0D);
                    tessellator.addVertexWithUV((double) (selectionRight - 1), (double) (rowY + rowInnerHeight + 1), 0.0D, 1.0D, 1.0D);
                    tessellator.addVertexWithUV((double) (selectionRight - 1), (double) (rowY - 1), 0.0D, 1.0D, 0.0D);
                    tessellator.addVertexWithUV((double) (selectionLeft + 1), (double) (rowY - 1), 0.0D, 0.0D, 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
                this.drawSlot(maxScroll, listX, rowY, rowInnerHeight, tessellator);
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        byte overlaySize = 4;
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.height, 255, 255);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double) this.left, (double) (this.top + overlaySize), 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double) this.right, (double) (this.top + overlaySize), 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double) this.right, (double) this.top, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double) this.left, (double) this.top, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double) this.left, (double) this.bottom, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double) this.right, (double) this.bottom, 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double) this.right, (double) (this.bottom - overlaySize), 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double) this.left, (double) (this.bottom - overlaySize), 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        slotY = this.getContentHeight() - (this.bottom - this.top - 4);
        if (slotY > 0) {
            int scrollThumbHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            if (scrollThumbHeight < 32) {
                scrollThumbHeight = 32;
            }
            if (scrollThumbHeight > this.bottom - this.top - 8) {
                scrollThumbHeight = this.bottom - this.top - 8;
            }
            int scrollThumbY = (int) this.amountScrolled * (this.bottom - this.top - scrollThumbHeight) / slotY + this.top;
            if (scrollThumbY < this.top) {
                scrollThumbY = this.top;
            }
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(0, 255);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) this.bottom, 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double) scrollBarRight, (double) this.bottom, 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double) scrollBarRight, (double) this.top, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) this.top, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(8421504, 255);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) (scrollThumbY + scrollThumbHeight), 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double) scrollBarRight, (double) (scrollThumbY + scrollThumbHeight), 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double) scrollBarRight, (double) scrollThumbY, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) scrollThumbY, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(12632256, 255);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) (scrollThumbY + scrollThumbHeight - 1), 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double) (scrollBarRight - 1), (double) (scrollThumbY + scrollThumbHeight - 1), 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double) (scrollBarRight - 1), (double) scrollThumbY, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double) scrollBarLeft, (double) scrollThumbY, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
        }

        this.drawFooter(mouseX, mouseY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void overlayBackground(int y1, int y2, int alpha1, int alpha2) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.renderEngine.getTexture("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float texScale = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(4210752, alpha2);
        tessellator.addVertexWithUV(0.0D, (double) y2, 0.0D, 0.0D, (double) ((float) y2 / texScale));
        tessellator.addVertexWithUV((double) this.width, (double) y2, 0.0D, (double) ((float) this.width / texScale), (double) ((float) y2 / texScale));
        tessellator.setColorRGBA_I(4210752, alpha1);
        tessellator.addVertexWithUV((double) this.width, (double) y1, 0.0D, (double) ((float) this.width / texScale), (double) ((float) y1 / texScale));
        tessellator.addVertexWithUV(0.0D, (double) y1, 0.0D, 0.0D, (double) ((float) y1 / texScale));
        tessellator.draw();
    }
}
