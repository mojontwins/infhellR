package net.minecraft.client.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.client.gui.container.GuiContainer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.container.ContainerTrader;
import net.minecraft.game.world.World;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.game.trading.TradingRecipeList;
import net.minecraft.game.trading.TradingRecipe;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.Currency;

public class GuiTrading extends GuiContainer {
	public TradingRecipeList tradingRecipeList;
	public ITrader entityTrader;
	
	private GuiButtonTrading nextRecipeButton;
	private GuiButtonTrading previousRecipeButton;
	
	private int currentRecipeIndex = 0;
	
	public GuiTrading(EntityPlayer entityPlayer, ITrader entityTrader, World world) {
		super(new ContainerTrader(entityPlayer.inventory, entityTrader, world));
		this.entityTrader = entityTrader;

		this.tradingRecipeList = entityTrader.getRecipes(entityPlayer);
	}
		
	@Override
	public void initGui() {
		super.initGui();
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.controlList.add(this.nextRecipeButton = new GuiButtonTrading(1, guiLeft + 147, guiTop + 23, true));
		this.controlList.add(this.previousRecipeButton = new GuiButtonTrading(2, guiLeft + 17, guiTop + 23, false));
		this.nextRecipeButton.enabled = false;
		this.previousRecipeButton.enabled = false;
	}
		
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		this.inventorySlots.onCraftGuiClosed(this.mc.thePlayer);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		if(this.tradingRecipeList != null) {
			if(this.tradingRecipeList.get(currentRecipeIndex).isCurrencyChange()) {
				this.fontRenderer.drawCenteredString("Currency change [" + (int)(100 * this.tradingRecipeList.get(currentRecipeIndex).getChangeFactor()) + "%]", this.xSize / 2, 6, 0x404040);
			} else {
				this.fontRenderer.drawCenteredString(this.entityTrader.getTraderName() + " trading", this.xSize / 2, 6, 0x404040);
			}
		}
		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(int x, int y, float renderPartialTick) {
		int texture = this.mc.renderEngine.getTexture("/gui/trading.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		if(this.tradingRecipeList != null) {
			this.nextRecipeButton.enabled = this.currentRecipeIndex < this.tradingRecipeList.size() - 1;
			this.previousRecipeButton.enabled = this.currentRecipeIndex > 0;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guiButton) {
		boolean recipeChangeRequest = false;
		
		if(guiButton.id == 1) {
			this.currentRecipeIndex++;
			recipeChangeRequest = true;
		} else if(guiButton.id == 2) {
			this.currentRecipeIndex--;
			recipeChangeRequest = true;
		}
		
		if(recipeChangeRequest) {
			((ContainerTrader)this.inventorySlots).setCurrentRecipeIndex(this.currentRecipeIndex);
			
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			
			try {
				dataStream.writeInt(this.currentRecipeIndex);
				this.mc.getSendQueue().addToSendQueue(new Packet250CustomPayload("MC|TrSel", byteStream.toByteArray()));
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float renderPartialTick) {
		super.drawScreen(mouseX, mouseY, renderPartialTick);
		
		if(this.tradingRecipeList != null && !this.tradingRecipeList.isEmpty()) {
			int guiLeft = (this.width - this.xSize) / 2;
			int guiTop = (this.height - this.ySize) / 2;
			
			int recipeIndex = this.currentRecipeIndex;
			TradingRecipe recipe = this.tradingRecipeList.get(recipeIndex);

			GL11.glPushMatrix();
			GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glTranslatef(0, 0, 32.0F);
			
			if(recipe.isCurrencyChange()) {
				itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, Currency.getItemStack(this.entityTrader.getCurrency()), guiLeft + 36, guiTop + 24);
				itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, Currency.getItemStack(Currency.getOtherCurrency(this.entityTrader.getCurrency())), guiLeft + 120, guiTop + 24);
			} else {
				itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStack1(), guiLeft + 36, guiTop + 24);
				itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStack1(), guiLeft + 36, guiTop + 24);
				
				if(recipe.getItemStack2() != null) {
					itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStack2(), guiLeft + 62, guiTop + 24);
					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStack2(), guiLeft + 62, guiTop + 24);
				}
				
				itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStackResult(), guiLeft + 120, guiTop + 24);
				itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, recipe.getItemStackResult(), guiLeft + 120, guiTop + 24);

				ItemStack hoveredItem = null;
				
				if(this.mouseOverIconAt(mouseX, mouseY, guiLeft + 36, guiTop + 24)) {
					hoveredItem = recipe.getItemStack1();
				} else if(this.mouseOverIconAt(mouseX, mouseY, guiLeft + 62, guiTop + 24)) {
					hoveredItem = recipe.getItemStack2();
				} else if(this.mouseOverIconAt(mouseX, mouseY, guiLeft + 120, guiTop + 24)) {
					hoveredItem = recipe.getItemStackResult();
				}

				if(hoveredItem != null) {
					String info = ("" + StringTranslate.getInstance().translateNamedKey(hoveredItem.getItemName())).trim();
					if(info.length() > 0) {
						int labelX = mouseX + 12;
						int labelY = mouseY - 12;
						int strLength = this.fontRenderer.getStringWidth(info);
						this.drawGradientRect(labelX - 3, labelY - 3, labelX + strLength + 3, labelY + 8 + 3, 0xC0000000, 0xC0000000);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glDisable(GL11.GL_DEPTH_TEST);
						this.fontRenderer.drawStringWithShadow(info, labelX, labelY, 0xFFFFFF);
						GL11.glEnable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
					}
				}
			}
				
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}
	
	public boolean mouseOverIconAt(int mouseX, int mouseY, int iconX, int iconY) {
		return mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16;
	}

	public ITrader getEntityTrader() {
		return this.entityTrader;
	}
}
