package net.minecraft.client.gui.container;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.game.StringTranslate;
import net.minecraft.game.container.Container;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.container.Slot;
import net.minecraft.game.item.ItemStack;
import net.minecraft.client.GameSettingsKeys;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.RenderItem;

public abstract class GuiContainer extends GuiScreen {
	protected static RenderItem itemRenderer = new RenderItem();
	public int xSize = 176;
	public int ySize = 166;
	public Container inventorySlots;
	protected int guiLeft;
	protected int guiTop;
	
	public GuiContainer(Container container1) {
		this.inventorySlots = container1;
	}

	public void initGui() {
		super.initGui();
		this.mc.thePlayer.craftingInventory = this.inventorySlots;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
	}

	public void drawScreen(int x, int y, float renderPartialTicks) {
		this.drawDefaultBackground();
		int i4 = (this.width - this.xSize) / 2;
		int i5 = (this.height - this.ySize) / 2;
		this.drawGuiContainerBackgroundLayer(x, y, renderPartialTicks);
		GL11.glPushMatrix();
		GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glTranslatef((float)i4, (float)i5, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Slot slot6 = null;
		short s7 = 240;
		short s8 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) s7 / 1.0F, (float) s8 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int i9;
		int i10;
		for(int i7 = 0; i7 < this.inventorySlots.inventorySlots.size(); ++i7) {
			Slot slot8 = (Slot)this.inventorySlots.inventorySlots.get(i7);
			this.drawSlotInventory(slot8);
			if(this.getIsMouseOverSlot(slot8, x, y)) {
				slot6 = slot8;
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				i9 = slot8.xDisplayPosition;
				i10 = slot8.yDisplayPosition;
				this.drawGradientRect(i9, i10, i9 + 16, i10 + 16, -2130706433, -2130706433);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
		}

		InventoryPlayer inventoryPlayer12 = this.mc.thePlayer.inventory;
		if(inventoryPlayer12.getItemStack() != null) {
			GL11.glTranslatef(0.0F, 0.0F, 32.0F);
			itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, inventoryPlayer12.getItemStack(), x - i4 - 8, y - i5 - 8);
			itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, inventoryPlayer12.getItemStack(), x - i4 - 8, y - i5 - 8);
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		this.drawGuiContainerForegroundLayer();
		if(inventoryPlayer12.getItemStack() == null && slot6 != null && slot6.getHasStack()) {
			String string13 = ("" + StringTranslate.getInstance().translateNamedKey(slot6.getStack().getItemName())).trim();
			if(string13.length() > 0) {
				i9 = x - i4 + 12;
				i10 = y - i5 - 12;
				int i11 = this.fontRenderer.getStringWidth(string13);
				this.drawGradientRect(i9 - 3, i10 - 3, i9 + i11 + 3, i10 + 8 + 3, -1073741824, -1073741824);
				this.fontRenderer.drawStringWithShadow(string13, i9, i10, -1);
			}
		}

		GL11.glPopMatrix();
		super.drawScreen(x, y, renderPartialTicks);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	protected void func_74190_a(String par1Str, int par2, int par3) {
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		int var4 = this.fontRenderer.getStringWidth(par1Str);
		int var5 = par2 + 12;
		int var6 = par3 - 12;
		byte var8 = 8;
		this.zLevel = 300.0F;
		//itemRenderer.zLevel = 300.0F;
		int var9 = -267386864;
		this.drawGradientRect(var5 - 3, var6 - 4, var5 + var4 + 3, var6 - 3, var9, var9);
		this.drawGradientRect(var5 - 3, var6 + var8 + 3, var5 + var4 + 3, var6 + var8 + 4, var9, var9);
		this.drawGradientRect(var5 - 3, var6 - 3, var5 + var4 + 3, var6 + var8 + 3, var9, var9);
		this.drawGradientRect(var5 - 4, var6 - 3, var5 - 3, var6 + var8 + 3, var9, var9);
		this.drawGradientRect(var5 + var4 + 3, var6 - 3, var5 + var4 + 4, var6 + var8 + 3, var9, var9);
		int var10 = 1347420415;
		int var11 = (var10 & 16711422) >> 1 | var10 & -16777216;
		this.drawGradientRect(var5 - 3, var6 - 3 + 1, var5 - 3 + 1, var6 + var8 + 3 - 1, var10, var11);
		this.drawGradientRect(var5 + var4 + 2, var6 - 3 + 1, var5 + var4 + 3, var6 + var8 + 3 - 1, var10, var11);
		this.drawGradientRect(var5 - 3, var6 - 3, var5 + var4 + 3, var6 - 3 + 1, var10, var10);
		this.drawGradientRect(var5 - 3, var6 + var8 + 2, var5 + var4 + 3, var6 + var8 + 3, var11, var11);
		this.fontRenderer.drawStringWithShadow(par1Str, var5, var6, -1);
		this.zLevel = 0.0F;
		//itemRenderer.zLevel = 0.0F;
	}

	protected void drawGuiContainerForegroundLayer() {
	}

	protected abstract void drawGuiContainerBackgroundLayer(int x, int y, float f1);

	private void drawSlotInventory(Slot slot1) {
		int i2 = slot1.xDisplayPosition;
		int i3 = slot1.yDisplayPosition;
		ItemStack itemStack4 = slot1.getStack();
		if(itemStack4 == null) {
			int i5 = slot1.getBackgroundIconIndex();
			if(i5 >= 0) {
				GL11.glDisable(GL11.GL_LIGHTING);
				this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/gui/items.png"));
				this.drawTexturedModalRect(i2, i3, i5 % 16 * 16, i5 / 16 * 16, 16, 16);
				GL11.glEnable(GL11.GL_LIGHTING);
				return;
			}
		}

		itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, itemStack4, i2, i3);
		itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, itemStack4, i2, i3);
	}

	protected Slot getSlotAtPosition(int i1, int i2) {
		for(int i3 = 0; i3 < this.inventorySlots.inventorySlots.size(); ++i3) {
			Slot slot4 = (Slot)this.inventorySlots.inventorySlots.get(i3);
			if(this.getIsMouseOverSlot(slot4, i1, i2)) {
				return slot4;
			}
		}

		return null;
	}
	
	protected boolean func_74188_c(int par1, int par2, int par3, int par4, int par5, int par6) {
		int var7 = this.guiLeft;
		int var8 = this.guiTop;
		par5 -= var7;
		par6 -= var8;
		return par5 >= par1 - 1 && par5 < par1 + par3 + 1 && par6 >= par2 - 1 && par6 < par2 + par4 + 1;
	}

	private boolean getIsMouseOverSlot(Slot slot1, int i2, int i3) {
		int i4 = (this.width - this.xSize) / 2;
		int i5 = (this.height - this.ySize) / 2;
		i2 -= i4;
		i3 -= i5;
		return i2 >= slot1.xDisplayPosition - 1 && i2 < slot1.xDisplayPosition + 16 + 1 && i3 >= slot1.yDisplayPosition - 1 && i3 < slot1.yDisplayPosition + 16 + 1;
	}

	protected void mouseClicked(int i1, int i2, int i3) {
		super.mouseClicked(i1, i2, i3);
		if(i3 == 0 || i3 == 1) {
			Slot slot4 = this.getSlotAtPosition(i1, i2);
			int i5 = (this.width - this.xSize) / 2;
			int i6 = (this.height - this.ySize) / 2;
			boolean z7 = i1 < i5 || i2 < i6 || i1 >= i5 + this.xSize || i2 >= i6 + this.ySize;
			int i8 = -1;
			if(slot4 != null) {
				i8 = slot4.id;
			}

			if(z7) {
				i8 = -999;
			}

			if(i8 != -1) {
				boolean z9 = i8 != -999 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
				//this.mc.playerController.windowClick(this.inventorySlots.windowId, i8, i3, z9, this.mc.thePlayer);
				this.handleMouseClick(slot4, i8, i3, z9);
			}
		}

	}
	
	protected void handleMouseClick(Slot slot, int slotId, int button, boolean shift) {
		this.mc.playerController.windowClick(this.inventorySlots.windowId, slotId, button, shift, this.mc.thePlayer);
	}

	protected void mouseMovedOrUp(int i1, int i2, int i3) {
		if(i3 == 0) {
			;
		}

	}

	protected void keyTyped(char c1, int i2) {
		if(i2 == 1 || i2 == GameSettingsKeys.keyBindInventory.keyCode) {
			this.mc.thePlayer.closeScreen();
		}

	}

	public void onGuiClosed() {
		if(this.mc.thePlayer != null) {
			this.mc.playerController.onCraftGuiClosed(this.inventorySlots.windowId, this.mc.thePlayer);
		}
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public void updateScreen() {
		super.updateScreen();
		if(!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
			this.mc.thePlayer.closeScreen();
		}

	}

	protected boolean fc_isShiftPressed() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

	protected boolean fc_canStacksMerge(ItemStack stack1, ItemStack stack2) {
		return stack1 != null && stack2 != null ? (stack1.itemID == stack2.itemID && stack1.itemDamage == stack2.itemDamage ? stack1.stackSize + stack2.stackSize <= stack1.getItem().getItemStackLimit() : false) : false;
	}

	protected int fc_getMergeCount(ItemStack stack1, ItemStack stack2) {
		if(stack1 != null && stack2 != null) {
			if(stack1.itemID == stack2.itemID && stack1.itemDamage == stack2.itemDamage) {
				int count = stack1.getItem().getItemStackLimit() - (stack1.stackSize + stack2.stackSize);
				return count >= 0 ? stack1.stackSize : stack1.stackSize + count;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
