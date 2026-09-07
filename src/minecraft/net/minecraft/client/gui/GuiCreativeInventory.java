package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemBlock;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockDoor;
import net.minecraft.game.world.block.BlockSign;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;
import net.minecraft.game.world.block.BlockBigFlower;
import net.minecraft.game.world.block.BlockBigMushroom;
import net.minecraft.game.world.block.BlockCoral;
import net.minecraft.game.world.block.BlockStep;

public class GuiCreativeInventory extends GuiScreen {
	
	public List<ItemStack> itemsList = new ArrayList<ItemStack>();
	private static RenderItem itemRenderer = new RenderItem();
	public static int firstElement = 0;
	public int lastElement;
	public int selectedItem = -1;
	public int selectedItemX = 0, selectedItemY = 0;
	public int lastPossibleFirstElement;
	private Minecraft mc;
	
	public GuiCreativeInventory (Minecraft mc) {
		this.mc = mc;
		
		for (int i = 0; i < Block.blocksList.length; i++) {
			Block block = Block.blocksList[i];
			if (block != null) {
				if(Item.itemsList[i] == null) continue;
				if(!(Item.itemsList[i] instanceof ItemBlock)) continue;
				
				if(block.softLocked()) continue;
				
				if(block instanceof BlockDoor) continue;
				if(block == Block.stoneOvenActive) continue;
				if(block == Block.oreRedstoneGlowing) continue;
				if(block == Block.torchRedstoneActive) continue;
				if(block == Block.redstoneWire) continue;
				if(block == Block.redstoneRepeaterActive) continue;
				if(block == Block.redstoneRepeaterIdle) continue;
				if(block == Block.portal) continue;
				if(block instanceof BlockSign) continue;
				if(block == Block.classicPiston || block == Block.classicStickyPiston) continue;
				
				if(block == Block.cloth || block == Block.stainedTerracotta) {
					for (int j = 0; j < 16; j++) {
						this.itemsList.add(new ItemStack (block.blockID, 1, j));
					}
				} else if(block instanceof BlockStep) {
					for (int j = 0; j < 4; j++) {
						this.itemsList.add(new ItemStack (Block.stairSingle, 1, j));
					}
				} else if(block instanceof BlockCoral) {
					for (int j = 0; j < 3; j++) {
						this.itemsList.add(new ItemStack (Block.coral, 1, 8 | j));
					}
				} else if(block instanceof BlockBigFlower) {
					for (int j = 0; j < 3; j++) {
						this.itemsList.add(new ItemStack (Block.bigFlower, 1, j));
					}
				} else if(block == Block.grass) {
					for (int j = 0; j < 2; j++) {
						this.itemsList.add(new ItemStack (Block.grass, 1, j));
					}
				} else if(block == Block.ice) {
					for (int j = 0; j < 2; j++) {
						this.itemsList.add(new ItemStack (Block.ice, 1, j));
					}
				} else if(block instanceof BlockBigMushroom) {
					this.itemsList.add(new ItemStack (block, 1, 14));
				} else {
					this.itemsList.add(new ItemStack (block.blockID, 1, 0));
				}
			}
		}
		
		for (int i = 256; i < Item.itemsList.length; i++) {
			Item item = Item.itemsList[i];
			if (item != null && item.isShowInCreative()) {
				if(item.softLocked()) continue;
				
				if (item == Item.dyePowder) {
					for (int j = 0; j < 16; j++) {
						this.itemsList.add(new ItemStack (Item.dyePowder, 1, j));
					}
				} else {
					this.itemsList.add(new ItemStack (item.shiftedIndex));
				}
			}
		}
		
		this.calculatePaging();
		this.lastPossibleFirstElement = 9 * (this.itemsList.size() / 9 - 4);
	}
	
	private void calculatePaging() {		
		this.lastElement = GuiCreativeInventory.firstElement + 44; 
		if (this.lastElement > this.itemsList.size() - 1) this.lastElement = this.itemsList.size() - 1; 
	}
	
	public void updateSelectedItem(int mouseX, int mouseY) {		
		if (mouseX >= this.width / 2 - 108 && mouseX <= this.width / 2 + 107 &&
			mouseY >= this.height / 2 - 70 && mouseY <= this.height / 2 + 49) {
			int x = (mouseX - (this.width / 2 - 108)) / 24;
			int y = (mouseY - (this.height / 2 - 70)) / 24;
			this.selectedItem = GuiCreativeInventory.firstElement + x + y * 9;
			this.selectedItemX = x * 24 + this.width / 2 - 108;
			this.selectedItemY = y * 24 + this.height / 2 - 70;
		} else {
			this.selectedItem = -1;
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float renderPartialTick) {
		this.calculatePaging();
		this.updateSelectedItem(mouseX, mouseY);
		
		int x0 = this.width / 2 - 118; 
		int y0 = this.height / 2 - 100;
		this.drawGradientRect(x0, y0, x0 + 235, y0 + 179, 0x90000000, 0xC00000FF);
		this.drawCenteredString(this.fontRenderer, "Select a block or item", this.width / 2, y0 + 10, 0xFFFFFF);
		this.drawCenteredString(this.fontRenderer, "Showing " + (1 + GuiCreativeInventory.firstElement) + "-" + (1 + this.lastElement) + " of " + this.itemsList.size(), this.width/2, y0 + 160, 0xFFFFFF);
		
		RenderEngine renderEngine = this.mc.renderEngine;
		
		if (this.selectedItem != -1) {
			this.drawRect(this.selectedItemX, this.selectedItemY, this.selectedItemX + 23, this.selectedItemY + 23, 0x80CCCCCC);
		}
		
		int showingIndex = 0;
		for (int i = GuiCreativeInventory.firstElement; i <= this.lastElement; i++) {			
			int x = x0 + 10 + 24 * (showingIndex % 9) + 4;
			int y = y0 + 30 + 24 * (showingIndex / 9) + 4;
			
			GL11.glPushMatrix();
			GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glTranslatef(0, 0, 32.0F);
			
			itemRenderer.renderItemIntoGUI(this.fontRenderer, renderEngine, this.itemsList.get(i), x, y);
			
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
			
			++showingIndex;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			if(this.selectedItem != -1 && this.selectedItem < this.itemsList.size()) {
				String info = this.itemsList.get(this.selectedItem).getItem().shiftedIndex + ":" + this.itemsList.get(this.selectedItem).itemDamage;
				this.drawCenteredString(this.fontRenderer, info, mouseX, mouseY + 10, 0xFFFFFF);
			}
		}
		
		super.drawScreen(mouseX, mouseY, renderPartialTick);
	}
	
	public void handleMouseInput() {
		super.handleMouseInput();
		int wheel;
		if (0 != (wheel = Mouse.getEventDWheel())) {			
			if (wheel > 0) {
				this.scrollUp();	
			} else {
				this.scrollDown();
			}
		}
	}
	
	private void scrollUp () {
		if (GuiCreativeInventory.firstElement > 0) GuiCreativeInventory.firstElement = ((GuiCreativeInventory.firstElement / 9) - 1) * 9; 
	}
	
	private void scrollDown () {
		if (GuiCreativeInventory.firstElement < this.lastPossibleFirstElement) GuiCreativeInventory.firstElement = ((GuiCreativeInventory.firstElement / 9) + 1) * 9;
	}
	
	protected void mouseClicked (int x, int y, int b) {		
		if (b == 0) {
			if (this.selectedItem != -1) {
				if(this.selectedItem < this.itemsList.size()) {
					this.mc.playerController.sendSetItemStack(this.itemsList.get(this.selectedItem).getItem(), this.itemsList.get(this.selectedItem).itemDamage);
				}
			}
		}
	}
	
	protected void keyTyped(char c, int key) {
		if (key == 1) {
			this.mc.displayGuiScreen((GuiScreen)null);
		} else if (key == Keyboard.KEY_UP) {
			this.scrollUp();
		} else if (key == Keyboard.KEY_DOWN) {
			this.scrollDown();
		} else {
			for(int i = 0; i < 9; ++i) {
				if(Keyboard.getEventKey() == Keyboard.KEY_1 + i) {
					this.mc.thePlayer.inventory.currentItem = i;
				}
			}
		}
	}
	
	public boolean doesGuiPauseGame() {
		return false;
	}
}
