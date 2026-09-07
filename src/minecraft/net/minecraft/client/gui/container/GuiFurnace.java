package net.minecraft.client.gui.container;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.game.container.ContainerFurnace;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.item.recipe.FurnaceRecipe;
import net.minecraft.game.item.recipe.FurnaceRecipeSorterForGUI;
import net.minecraft.game.item.recipe.FurnaceRecipes;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityFurnace;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;

public class GuiFurnace extends GuiContainer {
	/*
	 * Furnace recipes inspired from Fycraft's mod for 1.2.6
	 */
	List<FurnaceRecipe> cm;
	GuiButton buttonLeft;
	GuiButton buttonRight;
	int craftpanelx;
	int craftpanely;
	int craftpanelwidth;
	int craftpanelheight;
	boolean craftPanelActive = false;
	int highlightedSlot = -1;
	int clickedSlot = -1;
	int startPage = 0;	
	private static RenderItem itemRenderer = new RenderItem();
	
	private TileEntityFurnace furnaceInventory;

	public GuiFurnace(InventoryPlayer inventoryPlayer1, TileEntityFurnace tileEntityFurnace2) {
		super(new ContainerFurnace(inventoryPlayer1, tileEntityFurnace2));
		this.furnaceInventory = tileEntityFurnace2;
		this.cm = FurnaceRecipes.smelting().getSmeltingsAsList();
		Collections.sort(this.cm, new FurnaceRecipeSorterForGUI());
		this.buttonLeft = new GuiButton(0, 0, 0, 40, 20, "<--");
		this.buttonRight = new GuiButton(1, 0, 0, 40, 20, "-->");
	}

	protected void drawGuiContainerForegroundLayer() {
		this.fontRenderer.drawString("Furnace", 60, 6, 4210752);
		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
	}

	protected void drawGuiContainerBackgroundLayer(int x, int y, float partialTicks) {
		int x0 = (this.width - this.xSize) / 2;
		int y0 = (this.height - this.ySize) / 2;
		
		if(!mc.thePlayer.enableCraftingGuide) {
			int bgTexI = this.mc.renderEngine.getTexture("/gui/furnace.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.renderEngine.bindTexture(bgTexI);
			this.drawTexturedModalRect(x0, y0, 0, 0, this.xSize, this.ySize);
		} else {
			int bgTexI = mc.renderEngine.getTexture("/gui/craftingpanel.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(bgTexI);
			this.craftpanelx = this.craftPanelActive ? x0 - 86 : x0 - 6;
			this.craftpanely = y0 + 4;
			this.drawTexturedModalRect(this.craftpanelx, this.craftpanely, 0, 0, 86, 158);
			this.craftpanelwidth = this.craftpanelx + 86;
			this.craftpanelheight = this.craftpanely + 158;
			bgTexI = mc.renderEngine.getTexture("/gui/furnace.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(bgTexI);
			this.drawTexturedModalRect(x0, y0, 0, 0, this.xSize, this.ySize);
			
			if(!this.craftPanelActive) {
				this.craftpanelwidth = this.craftpanelx + 6;
			} else {
				int mouseX = Mouse.getX() * this.width / mc.displayWidth;
				int mouseY = this.height - Mouse.getY() * this.height / mc.displayHeight - 1;
				int tileX = (mouseX - this.craftpanelx - 8) / 18;
				int tileY = (mouseY - this.craftpanely - 8) / 18;
				byte var21 = 4;
				byte var22 = 7;
				int tilesPerPage = var21 * var22;
				int maxPages = this.cm.size() / tilesPerPage;
				this.highlightedSlot = -1;
				if(mouseX >= this.craftpanelx + 8 && mouseX < this.craftpanelx + 8 + var21 * 18 && mouseY >= this.craftpanely + 8 && mouseY < this.craftpanely + 8 + var22 * 18) {
					this.highlightedSlot = tileX + tileY * var21;
				}
				
				int currentx = 9;
				int currenty = 9;
				int currentPage = 0;
				if(this.startPage < 0) {
					this.startPage = 0;
				}

				if(this.startPage > maxPages) {
					this.startPage = maxPages;
				}
				
				GL11.glPushMatrix();
				GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
				RenderHelper.enableStandardItemLighting();
				GL11.glPopMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable('\u803a');
				int slotNum = 0;

				for(Iterator<FurnaceRecipe> recipe = this.cm.iterator(); recipe.hasNext(); ++slotNum) {
					FurnaceRecipe furnaceRecipe = recipe.next();
					ItemStack itemStack = furnaceRecipe.outputItemStack;
					if(currentPage == this.startPage) {
						if(slotNum == this.clickedSlot) {
							this.drawGradientRect(this.craftpanelx + currentx, this.craftpanely + currenty, this.craftpanelx + currentx + 16, this.craftpanely + currenty + 16, -1716846593, -1716846593);
						}

						itemRenderer.renderItemIntoGUI(this.fontRenderer, mc.renderEngine, itemStack, this.craftpanelx + currentx, this.craftpanely + currenty);
					}

					if(slotNum == this.highlightedSlot) {
						this.drawGradientRect(this.craftpanelx + currentx, this.craftpanely + currenty, this.craftpanelx + currentx + 16, this.craftpanely + currenty + 16, 1157627903, 1157627903);
					}

					currentx += 18;
					if(currentx + 18 > this.craftpanelwidth - this.craftpanelx) {
						currentx = 9;
						currenty += 18;
					}

					if(currenty + 38 > this.craftpanelheight - this.craftpanely) {
						++currentPage;
						currenty = 9;
					}
				}

				if(this.highlightedSlot >= 0) {
					this.highlightedSlot += this.startPage * var21 * var22;
				}

				itemRenderer.colorItemFromDamage = false;
				if(this.clickedSlot >= 0) {
					FurnaceRecipe furnaceRecipe = this.cm.get(this.clickedSlot);
					int recipeItemId = furnaceRecipe.inputId;
					ItemStack itemStackOutput = furnaceRecipe.outputItemStack;
					
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.55F);
					ItemStack itemStack = new ItemStack(recipeItemId);
					if (itemStack != null) {
						itemRenderer.renderItemIntoGUI(
								this.fontRenderer, 
								mc.renderEngine, 
								itemStack, 
								(this.width - this.xSize) / 2 + 56, 
								(this.height - this.ySize) / 2 + 53
							);
					}
					
					itemRenderer.renderItemIntoGUI(this.fontRenderer, mc.renderEngine, itemStackOutput, (this.width - this.xSize) / 2 + 56 , (this.height - this.ySize) / 2 + 17);
					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, mc.renderEngine, itemStackOutput, (this.width - this.xSize) / 2 + 56 , (this.height - this.ySize) / 2 + 17);

					GL11.glDisable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				}

				RenderHelper.disableStandardItemLighting();
				GL11.glDisable('\u803a');
				itemRenderer.colorItemFromDamage = true;
				this.buttonLeft.xPosition = this.craftpanelx + 19;
				this.buttonLeft.yPosition = this.craftpanelheight - 19;
				this.buttonLeft.width = 24;
				this.buttonLeft.height = 12;
				this.buttonRight.xPosition = this.craftpanelx + 45;
				this.buttonRight.yPosition = this.craftpanelheight - 19;
				this.buttonRight.width = 24;
				this.buttonRight.height = 12;
				
				bgTexI = mc.renderEngine.getTexture("/gui/furnace.png");
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				mc.renderEngine.bindTexture(bgTexI);
			}					
		}
			
		int progress;
		if(this.furnaceInventory.isBurning()) {
			progress = this.furnaceInventory.getBurnTimeRemainingScaled(12);
			this.drawTexturedModalRect(x0 + 56, y0 + 36 + 12 - progress, 176, 12 - progress, 14, progress + 2);
		}

		progress = this.furnaceInventory.getCookProgressScaled(24);
		this.drawTexturedModalRect(x0 + 79, y0 + 34, 176, 14, progress + 1, 16);
	}
	
	private void handleCraftPanelInput(int x, int y, int button) {
		if(!this.craftPanelActive) {
			this.craftPanelActive = true;
		} else {
			if(this.highlightedSlot >= 0 && this.highlightedSlot < this.cm.size()) {
				this.clickedSlot = this.highlightedSlot;
			}

			if(this.buttonLeft.mousePressed(mc, x, y)) {
				--this.startPage;
			}

			if(this.buttonRight.mousePressed(mc, x, y)) {
				++this.startPage;
			}

			if(x - this.craftpanelx < 6 || x - this.craftpanelx > 80) {
				this.craftPanelActive = false;
			}

		}
	}
	
	protected void mouseClicked(int i, int j, int k) {		
		if(i >= this.craftpanelx && i < this.craftpanelwidth && j >= this.craftpanely && j < this.craftpanelheight) {
			this.handleCraftPanelInput(i, j, k);
		} else {
			super.mouseClicked(i, j, k);
		}
	}
}
