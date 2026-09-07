package net.minecraft.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.container.ContainerDispenser;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.world.block.tileentity.TileEntityDispenser;
import net.minecraft.client.gui.container.GuiContainer;

public class GuiDispenser extends GuiContainer {
	public GuiDispenser(InventoryPlayer inventoryPlayer, TileEntityDispenser tileEntityDispenser) {
		super(new ContainerDispenser(inventoryPlayer, tileEntityDispenser));
	}

	protected void drawGuiContainerForegroundLayer() {
		this.fontRenderer.drawString("Dispenser", 60, 6, 4210752);
		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
	}

	protected void drawGuiContainerBackgroundLayer(int x, int y, float partialTicks) {
		int bgTexture = this.mc.renderEngine.getTexture("/gui/trap.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(bgTexture);
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
	}
}
