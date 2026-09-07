package net.minecraft.client.gui.container;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.game.container.ContainerWorkbench;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.item.recipe.CraftingManager;
import net.minecraft.game.item.recipe.IRecipe;
import net.minecraft.game.world.World;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.RecipeSorterForGUI;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;

public class GuiCrafting extends GuiContainer {
	/*
	 * Crafting inventory adapted from Fycraft's mod for a1.2.6
	 */

	List<IRecipe> cm;
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
	
	public GuiCrafting(InventoryPlayer inventoryPlayer1, World world2, int i3, int i4, int i5) {
		super(new ContainerWorkbench(inventoryPlayer1, world2, i3, i4, i5));
		this.cm = CraftingManager.getInstance().getRecipeList();
		Collections.sort(this.cm, new RecipeSorterForGUI());
		this.buttonLeft = new GuiButton(0, 0, 0, 40, 20, "<--");
		this.buttonRight = new GuiButton(1, 0, 0, 40, 20, "-->");
	}

	public void onGuiClosed() {
		super.onGuiClosed();
		this.inventorySlots.onCraftGuiClosed(this.mc.thePlayer);
	}

	protected void drawGuiContainerForegroundLayer() {
		this.fontRenderer.drawString("Crafting", 28, 6, 4210752);
		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
	}

	protected void drawGuiContainerBackgroundLayer(int x, int y, float f1) {
		int j = (this.width - this.xSize) / 2;
		int k = (this.height - this.ySize) / 2;
		int i;
		if(!mc.thePlayer.enableCraftingGuide) {
			i = this.mc.renderEngine.getTexture("/gui/crafting.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.renderEngine.bindTexture(i);
			this.drawTexturedModalRect(j, k, 0, 0, this.xSize, this.ySize);
		} else {
			i = mc.renderEngine.getTexture("/gui/craftingpanel.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(i);
			this.craftpanelx = this.craftPanelActive ? j - 86 : j - 6;
			this.craftpanely = k + 4;
			this.drawTexturedModalRect(this.craftpanelx, this.craftpanely, 0, 0, 86, 158);
			this.craftpanelwidth = this.craftpanelx + 86;
			this.craftpanelheight = this.craftpanely + 158;
			i = mc.renderEngine.getTexture("/gui/crafting.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(i);
			this.drawTexturedModalRect(j, k, 0, 0, this.xSize, this.ySize);
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

				for(Iterator<IRecipe> recipe = this.cm.iterator(); recipe.hasNext(); ++slotNum) {
					IRecipe craftingRecipe = recipe.next();
					ItemStack itemStack = craftingRecipe.getRecipeOutput ();
					// Item item = Item.itemsList[craftingRecipe.getRecipeOutputItemID()];
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
					IRecipe craftingRecipe = this.cm.get(this.clickedSlot);
					ItemStack [] recipeItems = craftingRecipe.getRecipeItems();
					ItemStack itemStackOutput = craftingRecipe.getRecipeOutput ();
					
					if (recipeItems == null ) {
						System.out.println ("RecipeItems is null !?=");
					} else {
						for(int var24 = 0; var24 < craftingRecipe.getHeight(); ++var24) {
							for(int var25 = 0; var25 < craftingRecipe.getWidth(); ++var25) {
								GL11.glEnable(GL11.GL_BLEND);
								GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.55F);
								ItemStack itemStack = recipeItems[var24 * craftingRecipe.getWidth() + var25];
								if (itemStack != null) {
									itemRenderer.renderItemIntoGUI(this.fontRenderer, mc.renderEngine, itemStack, (this.width - this.xSize) / 2 + 30 + var25 * 18, (this.height - this.ySize) / 2 + 17 + var24 * 18);
								}
							}
						}
					}
					
					itemRenderer.renderItemIntoGUI(this.fontRenderer, mc.renderEngine, itemStackOutput, (this.width - this.xSize) / 2 + 124 , (this.height - this.ySize) / 2 + 35);
					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, mc.renderEngine, itemStackOutput, (this.width - this.xSize) / 2 + 124 , (this.height - this.ySize) / 2 + 35);

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
			}
		}
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
		} /*else if(k == 0 && mc.thePlayer.inventory.itemStack == null && this.fc_isShiftPressed()) {
			InventoryPlayer playerInventory = mc.thePlayer.inventory;
			Slot slot = this.getSlotAtPosition(i, j);
			if(slot != null) {
				ItemStack stack = slot.getStack();
				if(stack != null) {
					Object destInventory = null;
					if(slot.getInventory() == ((ContainerWorkbench)this.inventorySlots).craftMatrix) {
						destInventory = playerInventory;
					} else if(slot.getInventory() == playerInventory) {
						destInventory = ((ContainerWorkbench)this.inventorySlots).craftMatrix;
					} else {
						destInventory = playerInventory;
					}

					int size;
					for(size = 0; size < ((IInventory)destInventory).getSizeInventory(); ++size) {
						ItemStack n = ((IInventory)destInventory).getStackInSlot(size);
						int s = this.fc_getMergeCount(stack, n);
						if(s > 0) {
							n.stackSize += s;
							stack.stackSize -= s;
							if(stack.stackSize < 1) {
								slot.putStack((ItemStack)null);
								slot.onSlotChanged();
								return;
							}
						}
					}

					size = ((IInventory)destInventory).getSizeInventory();
					if(destInventory == playerInventory) {
						size = playerInventory.mainInventory.length;
					}

					for(int var13 = 0; var13 < size; ++var13) {
						ItemStack var14 = ((IInventory)destInventory).getStackInSlot(var13);
						if(var14 == null) {
							((IInventory)destInventory).setInventorySlotContents(var13, stack);
							slot.putStack((ItemStack)null);
							slot.onSlotChanged();
							return;
						}
					}

					if(stack.stackSize > 0) {
						mc.thePlayer.inventory.itemStack = stack;
						slot.putStack((ItemStack)null);
						slot.onSlotChanged();
					}

				}
			}
		} */else {
			super.mouseClicked(i, j, k);
		}
	}
}
