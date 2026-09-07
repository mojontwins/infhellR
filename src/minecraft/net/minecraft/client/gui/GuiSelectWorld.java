package net.minecraft.client.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import net.minecraft.game.MathHelper;
import net.minecraft.game.StringTranslate;

import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.chunk.loader.ISaveFormat;
import net.minecraft.game.world.chunk.loader.SaveFormatComparator;
import net.minecraft.client.controller.PlayerControllerSP;

public class GuiSelectWorld extends GuiScreen {
	private final DateFormat dateFormatter = new SimpleDateFormat();
	protected GuiScreen parentScreen;
	protected String screenTitle = "Select world";
	private boolean selected = false;
	private int selectedWorld;
	private List<SaveFormatComparator> saveList;
	private GuiWorldSlot worldSlotContainer;
	private String localizedWorldText;
	private String localizedMustConvertText;
	private boolean deleting;
	private GuiButton buttonRename;
	private GuiButton buttonSelect;
	private GuiButton buttonDelete;

	public GuiSelectWorld(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		this.screenTitle = translator.translateKey("selectWorld.title");
		this.localizedWorldText = translator.translateKey("selectWorld.world");
		this.localizedMustConvertText = translator.translateKey("selectWorld.conversion");
		this.loadSaves();
		this.worldSlotContainer = new GuiWorldSlot(this);
		this.worldSlotContainer.registerScrollButtons(this.controlList, 4, 5);
		this.initButtons();
	}

	private void loadSaves() {
		ISaveFormat saveLoader = this.mc.getSaveLoader();
		this.saveList = saveLoader.getSaveList();
		Collections.sort(this.saveList);
		this.selectedWorld = -1;
	}

	protected String getSaveFileName(int index) {
		return ((SaveFormatComparator)this.saveList.get(index)).getFileName();
	}

	protected String getSaveName(int index) {
		String name = ((SaveFormatComparator)this.saveList.get(index)).getDisplayName();
		if(name == null || MathHelper.stringNullOrLengthZero(name)) {
			StringTranslate translator = StringTranslate.getInstance();
			name = translator.translateKey("selectWorld.world") + " " + (index + 1);
		}

		return name;
	}

	public void initButtons() {
		StringTranslate translator = StringTranslate.getInstance();
		this.controlList.add(this.buttonSelect = new GuiButton(1, this.width / 2 - 154, this.height - 52, 150, 20, translator.translateKey("selectWorld.select")));
		this.controlList.add(this.buttonRename = new GuiButton(6, this.width / 2 - 154, this.height - 28, 70, 20, translator.translateKey("selectWorld.rename")));
		this.controlList.add(this.buttonDelete = new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, translator.translateKey("selectWorld.delete")));
		this.controlList.add(new GuiButton(3, this.width / 2 + 4, this.height - 52, 150, 20, translator.translateKey("selectWorld.create")));
		this.controlList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, translator.translateKey("gui.cancel")));
		this.buttonSelect.enabled = false;
		this.buttonRename.enabled = false;
		this.buttonDelete.enabled = false;
	}

	protected void actionPerformed(GuiButton guiButton) {
		if(guiButton.enabled) {
			if(guiButton.id == 2) {
				String name = this.getSaveName(this.selectedWorld);
				if(name != null) {
					this.deleting = true;
					StringTranslate translator = StringTranslate.getInstance();
					String question = translator.translateKey("selectWorld.deleteQuestion");
					String warning = "\'" + name + "\' " + translator.translateKey("selectWorld.deleteWarning");
					String confirmText = translator.translateKey("selectWorld.deleteButton");
					String cancelText = translator.translateKey("gui.cancel");
					GuiYesNo confirmScreen = new GuiYesNo(this, question, warning, confirmText, cancelText, this.selectedWorld);
					this.mc.displayGuiScreen(confirmScreen);
				}
			} else if(guiButton.id == 1) {
				this.selectWorld(this.selectedWorld);
			} else if(guiButton.id == 3) {
				this.mc.displayGuiScreen(new GuiCreateWorld(this));
			} else if(guiButton.id == 6) {
				this.mc.displayGuiScreen(new GuiRenameWorld(this, this.getSaveFileName(this.selectedWorld)));
			} else if(guiButton.id == 0) {
				this.mc.displayGuiScreen(this.parentScreen);
			} else {
				this.worldSlotContainer.actionPerformed(guiButton);
			}

		}
	}

	public void selectWorld(int index) {
		this.mc.displayGuiScreen((GuiScreen)null);
		if(!this.selected) {
			this.selected = true;
			this.mc.playerController = new PlayerControllerSP(this.mc);
			String fileName = this.getSaveFileName(index);
			if(fileName == null) {
				fileName = "World" + index;
			}

			this.mc.startWorld(fileName, this.getSaveName(index), (WorldSettings)null);
			this.mc.displayGuiScreen((GuiScreen)null);
		}
	}

	public void deleteWorld(boolean confirmed, int index) {
		if(this.deleting) {
			this.deleting = false;
			if(confirmed) {
				ISaveFormat saveLoader = this.mc.getSaveLoader();
				saveLoader.flushCache();
				saveLoader.deleteWorldDirectory(this.getSaveFileName(index));
				this.loadSaves();
			}

			this.mc.displayGuiScreen(this);
		}

	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.worldSlotContainer.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	static List<SaveFormatComparator> getSize(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.saveList;
	}

	static int onElementSelected(GuiSelectWorld guiSelectWorld, int index) {
		return guiSelectWorld.selectedWorld = index;
	}

	static int getSelectedWorld(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.selectedWorld;
	}

	static GuiButton getSelectButton(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.buttonSelect;
	}

	static GuiButton getRenameButton(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.buttonRename;
	}

	static GuiButton getDeleteButton(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.buttonDelete;
	}

	static String func_22087_f(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.localizedWorldText;
	}

	static DateFormat getDateFormatter(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.dateFormatter;
	}

	static String func_22088_h(GuiSelectWorld guiSelectWorld) {
		return guiSelectWorld.localizedMustConvertText;
	}
}
