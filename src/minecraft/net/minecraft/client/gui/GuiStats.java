package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StatFileWriter;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.game.StringTranslate;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.item.Item;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.RenderItem;

public class GuiStats extends GuiScreen {
	private static RenderItem renderItem = new RenderItem();
	protected GuiScreen parentGui;
	protected String statsTitle = "Select world";
	private GuiSlotStatsGeneral slotGeneral;
	private GuiSlotStatsItem slotItem;
	private GuiSlotStatsBlock slotBlock;
	private StatFileWriter statFileWriter;
	private GuiSlot selectedSlot = null;

	public GuiStats(GuiScreen parentGui, StatFileWriter statFileWriter) {
		this.parentGui = parentGui;
		this.statFileWriter = statFileWriter;
	}

	public void initGui() {
		this.statsTitle = StatCollector.translateToLocal("gui.stats");
		this.slotGeneral = new GuiSlotStatsGeneral(this);
		this.slotGeneral.registerScrollButtons(this.controlList, 1, 1);
		this.slotItem = new GuiSlotStatsItem(this);
		this.slotItem.registerScrollButtons(this.controlList, 1, 1);
		this.slotBlock = new GuiSlotStatsBlock(this);
		this.slotBlock.registerScrollButtons(this.controlList, 1, 1);
		this.selectedSlot = this.slotGeneral;
		this.addHeaderButtons();
	}

	public void addHeaderButtons() {
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, translator.translateKey("gui.done")));
		this.controlList.add(new GuiButton(1, this.width / 2 - 154, this.height - 52, 100, 20, translator.translateKey("stat.generalButton")));
		GuiButton blocksButton;
		this.controlList.add(blocksButton = new GuiButton(2, this.width / 2 - 46, this.height - 52, 100, 20, translator.translateKey("stat.blocksButton")));
		GuiButton itemsButton;
		this.controlList.add(itemsButton = new GuiButton(3, this.width / 2 + 62, this.height - 52, 100, 20, translator.translateKey("stat.itemsButton")));
		if(this.slotBlock.getSize() == 0) {
			blocksButton.enabled = false;
		}

		if(this.slotItem.getSize() == 0) {
			itemsButton.enabled = false;
		}

	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id == 0) {
				this.mc.displayGuiScreen(this.parentGui);
			} else if(guiButton.id == 1) {
				this.selectedSlot = this.slotGeneral;
			} else if(guiButton.id == 3) {
				this.selectedSlot = this.slotItem;
			} else if(guiButton.id == 2) {
				this.selectedSlot = this.slotBlock;
			} else {
				this.selectedSlot.actionPerformed(guiButton);
			}

		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.selectedSlot.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, this.statsTitle, this.width / 2, 20, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawItemSprite(int x, int y, int itemId) {
		this.drawButtonBackground(x + 1, y + 1);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glPushMatrix();
		GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		renderItem.drawItemIntoGui(this.fontRenderer, this.mc.renderEngine, itemId, 0, Item.itemsList[itemId].getIconFromDamage(0), x + 2, y + 2);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	private void drawButtonBackground(int x, int y) {
		this.drawSprite(x, y, 0, 0);
	}

	private void drawSprite(int x, int y, int u, int v) {
		int texture = this.mc.renderEngine.getTexture("/gui/slot.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + 18), (double)this.zLevel, (double)((float)(u + 0) * 0.0078125F), (double)((float)(v + 18) * 0.0078125F));
		tessellator.addVertexWithUV((double)(x + 18), (double)(y + 18), (double)this.zLevel, (double)((float)(u + 18) * 0.0078125F), (double)((float)(v + 18) * 0.0078125F));
		tessellator.addVertexWithUV((double)(x + 18), (double)(y + 0), (double)this.zLevel, (double)((float)(u + 18) * 0.0078125F), (double)((float)(v + 0) * 0.0078125F));
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)this.zLevel, (double)((float)(u + 0) * 0.0078125F), (double)((float)(v + 0) * 0.0078125F));
		tessellator.draw();
	}

	static Minecraft getMinecraft(GuiStats guiStats) {
		return guiStats.mc;
	}

	static FontRenderer getFontRenderer(GuiStats guiStats) {
		return guiStats.fontRenderer;
	}

	public static StatFileWriter getStatsFileWriter(GuiStats guiStats) {
		return guiStats.statFileWriter;
	}

	static void drawSprite(GuiStats guiStats, int x, int y, int u, int v) {
		guiStats.drawSprite(x, y, u, v);
	}

	static void drawGradientRect(GuiStats guiStats, int x1, int y1, int x2, int y2, int startColor, int endColor) {
		guiStats.drawGradientRect(x1, y1, x2, y2, startColor, endColor);
	}

	static void drawItemSprite(GuiStats guiStats, int x, int y, int itemId) {
		guiStats.drawItemSprite(x, y, itemId);
	}
}
