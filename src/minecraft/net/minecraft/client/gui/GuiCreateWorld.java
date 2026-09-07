package net.minecraft.client.gui;

import java.util.Random;

import org.lwjgl.input.Keyboard;

import net.minecraft.game.MathHelper;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.game.ChatAllowedCharacters;

import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.WorldType;
import net.minecraft.game.world.chunk.loader.ISaveFormat;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.controller.PlayerControllerSP;

public class GuiCreateWorld extends GuiScreen {
	private static final String[] chunkChanceStrings = new String [] { "selectWorld.cityChunkChanceLow", "selectWorld.cityChunkChanceMid", "selectWorld.cityChunkChanceHigh" };
	private GuiScreen parentGuiScreen;
	private GuiTextField textboxWorldName;
	private GuiTextField textboxSeed;
	private String folderName;
	private String gameMode = "survival";
	private boolean generateStructures = true;
	private boolean generateCities = true;
	private boolean enableCheats = false;
	private boolean craftGuide = true;
	private boolean deadManChest = false;
	private int cityChunkChance = 1;
	private boolean createClicked;
	private boolean moreOptions;
	private GuiButton gameModeButton;
	private GuiButton moreWorldOptions;
	private GuiButton generateStructuresButton;
	private GuiButton generateCitiesButton;
	private GuiButton worldTypeButton;
	private GuiButton enableCheatsButton;
	private GuiButton craftingGuideButton;
	private GuiButton deadManChestButton;
	private GuiButton cityChunkChanceButton;
	private String gameModeDescriptionLine1;
	private String gameModeDescriptionLine2;
	private String seed;
	private String localizedNewWorldText;
	private int worldType = 0;
	
	private float cityChance = 0.0F;
	private static final float[] cityChanceValues = new float [] { 0.6F, 0.1F, -0.4F };

	public GuiCreateWorld(GuiScreen parentGuiScreen) {
		this.parentGuiScreen = parentGuiScreen;
		this.seed = "";
		this.localizedNewWorldText = StatCollector.translateToLocal("selectWorld.newWorld");
	}

	public void updateScreen() {
		this.textboxWorldName.updateCursorCounter();
		this.textboxSeed.updateCursorCounter();
	}

	public void initGui() {
		StringTranslate translator = StringTranslate.getInstance();
		Keyboard.enableRepeatEvents(true);
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, translator.translateKey("selectWorld.create")));
		this.controlList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, translator.translateKey("gui.cancel")));
		this.controlList.add(this.gameModeButton = new GuiButton(2, this.width / 2 - 75, 100, 150, 20, translator.translateKey("selectWorld.gameMode")));
		this.controlList.add(this.moreWorldOptions = new GuiButton(3, this.width / 2 - 75, 180, 150, 20, translator.translateKey("selectWorld.moreWorldOptions")));
		
		this.controlList.add(this.generateStructuresButton = new GuiButton(4, this.width / 2 - 155, 100, 150, 20, translator.translateKey("selectWorld.mapFeatures")));
		this.generateStructuresButton.drawButton = false;
		this.generateStructuresButton.toolTip = "Generate big, special structures around the world.";
				
		this.controlList.add(this.worldTypeButton = new GuiButton(5, this.width / 2 + 5, 100, 150, 20, translator.translateKey("selectWorld.mapType")));
		this.worldTypeButton.drawButton = false;
		this.worldTypeButton.toolTip = "Default: normal world generation.\nFlat: flat terrain with no mountains nor oceans.\nSky: Terrain based on individual floating islands.\nCold: Lower overall temperature generates colder biomes.";
		
		this.controlList.add(this.enableCheatsButton = new GuiButton(6, this.width / 2 - 155, 150, 150, 20, translator.translateKey("selectWorld.enableCheats")));
		this.enableCheatsButton.drawButton = false;
		this.enableCheatsButton.toolTip = "Let's you execute commands in the chat box.\nType /help for a list of commands.";
		
		this.controlList.add(this.craftingGuideButton = new GuiButton(7, this.width / 2 + 5, 150, 150, 20, translator.translateKey("selectWorld.craftingGuide")));
		this.craftingGuideButton.drawButton = false;
		this.craftingGuideButton.toolTip = "Adds a crafting gide to the crafting table\nand furnace screens.";
		
		this.controlList.add(this.generateCitiesButton = new GuiButton(8, this.width / 2 - 155, 125, 75, 20, translator.translateKey("selectWorld.generateCities")));
		this.generateCitiesButton.drawButton = false;
		this.generateCitiesButton.toolTip = "Whether to generate cities or not.";
		
		this.controlList.add(this.deadManChestButton = new GuiButton(9, this.width / 2 + 5, 125, 150, 20, translator.translateKey("selectWorld.deadManChest")));
		this.deadManChestButton.drawButton = false;
		this.deadManChestButton.toolTip = "Put all your items in a chest\n at the coordinates you died.";
		
		this.controlList.add(this.cityChunkChanceButton = new GuiButton (10, this.width / 2 - 155+75, 125, 75, 20, translator.translateKey("selectWorld.cityChunkChanceMid")));
		this.cityChunkChanceButton.drawButton = false;
		this.cityChunkChanceButton.toolTip = "Chance of spawning cities in a chunk.\nNo effect if cities are disabled.";
		
		this.textboxWorldName = new GuiTextField(this, this.fontRenderer, this.width / 2 - 100, 60, 200, 20, translator.translateKey("selectWorld.newWorld"));
		this.textboxWorldName.isFocused = true;
		this.textboxWorldName.setText(this.localizedNewWorldText);
		this.textboxSeed = new GuiTextField(this, this.fontRenderer, this.width / 2 - 100, 60, 200, 20, "");
		this.textboxSeed.setText(this.seed);
		
		this.makeUseableName();
		this.updateCaptions();
	}

	private void makeUseableName() {
		this.folderName = this.textboxWorldName.getText().trim();
		char[] allowedChars = ChatAllowedCharacters.allowedCharactersArray;
		int allowedLen = allowedChars.length;

		for(int i = 0; i < allowedLen; ++i) {
			char c = allowedChars[i];
			this.folderName = this.folderName.replace(c, '_');
		}

		if(MathHelper.stringNullOrLengthZero(this.folderName)) {
			this.folderName = "World";
		}

		this.folderName = func_25097_a(this.mc.getSaveLoader(), this.folderName);
	}

	private void updateCaptions() {
		StringTranslate translator = StringTranslate.getInstance();
		this.gameModeButton.displayString = translator.translateKey("selectWorld.gameMode") + " " + translator.translateKey("selectWorld.gameMode." + this.gameMode);
		this.gameModeDescriptionLine1 = translator.translateKey("selectWorld.gameMode." + this.gameMode + ".line1");
		this.gameModeDescriptionLine2 = translator.translateKey("selectWorld.gameMode." + this.gameMode + ".line2");
		
		this.generateStructuresButton.displayString = translator.translateKey("selectWorld.mapFeatures") + ": " + (this.generateStructures ? translator.translateKey("options.on") : translator.translateKey("options.off"));
		this.enableCheatsButton.displayString = translator.translateKey("selectWorld.enableCheats") + ": " + (this.enableCheats ? translator.translateKey("options.on") : translator.translateKey("options.off"));
		this.craftingGuideButton.displayString = translator.translateKey("selectWorld.craftingGuide") + ": " + (this.craftGuide ? translator.translateKey("options.on") : translator.translateKey("options.off"));
		this.generateCitiesButton.displayString = translator.translateKey("selectWorld.generateCities") + ": " + (this.generateCities ? translator.translateKey("options.on") : translator.translateKey("options.off"));
		this.cityChunkChanceButton.displayString = translator.translateKey(chunkChanceStrings[this.cityChunkChance]);
		this.deadManChestButton.displayString = translator.translateKey("selectWorld.deadManChest") + ": " + (this.deadManChest ? translator.translateKey("options.on") : translator.translateKey("options.off"));
		
		this.worldTypeButton.displayString = translator.translateKey("selectWorld.mapType") + ": " + translator.translateKey(WorldType.worldTypes[this.worldType].getTranslateName());
	}

	public static String func_25097_a(ISaveFormat saveFormat, String folderName) {
		for(folderName = folderName.replaceAll("[\\./\"]|COM", "_"); saveFormat.getWorldInfo(folderName) != null; folderName = folderName + "-") {
		}

		return folderName;
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton buttonPressed) {
		if(buttonPressed.enabled && buttonPressed.drawButton) {
			if(buttonPressed.id == 1) {
				this.mc.displayGuiScreen(this.parentGuiScreen);
			} else if(buttonPressed.id == 0) {
				this.mc.displayGuiScreen((GuiScreen)null);
				if(this.createClicked) {
					return;
				}

				this.createClicked = true;
				long seedLong = (new Random()).nextLong();
				String seedText = this.textboxSeed.getText();
				if(!MathHelper.stringNullOrLengthZero(seedText)) {
					try {
						long parsedSeed = Long.parseLong(seedText);
						if(parsedSeed != 0L) {
							seedLong = parsedSeed;
						}
					} catch (NumberFormatException numberFormatException) {
						seedLong = (long)seedText.hashCode();
					}
				}

				GameSettingsValues.isCreative = this.gameMode.equals("creative");
				GameSettingsValues.enableCheats = this.enableCheats;
				GameSettingsValues.deadManChest = this.deadManChest;
				GameSettingsValues.craftGuide = this.craftGuide;
				
				WorldType.worldTypes[this.worldType].onGUICreateWorldPress();
				this.mc.playerController = new PlayerControllerSP(this.mc);
				this.mc.startWorld(this.folderName, this.textboxWorldName.getText(), new WorldSettings(
						seedLong, 
						GameSettingsValues.isCreative ? 1 : 0, 
						this.generateStructures, 
						false,
						this.generateCities,
						this.cityChance,
						WorldType.worldTypes[this.worldType])
					);
				this.mc.displayGuiScreen((GuiScreen)null);
				
			} else if(buttonPressed.id == 3) {
				this.moreOptions = !this.moreOptions;
				this.gameModeButton.drawButton = !this.moreOptions;
				this.generateStructuresButton.drawButton = this.moreOptions;
				this.generateCitiesButton.drawButton = this.moreOptions;
				this.cityChunkChanceButton.drawButton = this.moreOptions;
				this.worldTypeButton.drawButton = this.moreOptions;
				this.enableCheatsButton.drawButton = this.moreOptions;
				this.deadManChestButton.drawButton = this.moreOptions;
				this.craftingGuideButton.drawButton = this.moreOptions;
				StringTranslate translator;
				if(this.moreOptions) {
					translator = StringTranslate.getInstance();
					this.moreWorldOptions.displayString = translator.translateKey("gui.done");
				} else {
					translator = StringTranslate.getInstance();
					this.moreWorldOptions.displayString = translator.translateKey("selectWorld.moreWorldOptions");
				}
			} else if(buttonPressed.id == 2) {
				if(this.gameMode.equals("survival")) {
					this.gameMode = "creative";
					this.updateCaptions();
				} else {
					this.gameMode = "survival";
					this.updateCaptions();
				}

				this.updateCaptions();
			} else if(buttonPressed.id == 4) {
				this.generateStructures = !this.generateStructures;
				this.updateCaptions();
			} else if(buttonPressed.id == 5) {
				++this.worldType;
				if(this.worldType >= WorldType.worldTypes.length) {
					this.worldType = 0;
				}

				while(WorldType.worldTypes[this.worldType] == null || !WorldType.worldTypes[this.worldType].getCanBeCreated()) {
					++this.worldType;
					if(this.worldType >= WorldType.worldTypes.length) {
						this.worldType = 0;
					}
				}
 
				this.updateCaptions();
			} else if(buttonPressed.id == 6) {
				this.enableCheats = !this.enableCheats;
				this.updateCaptions(); 
			} else if(buttonPressed.id == 7) {
				this.craftGuide = !this.craftGuide;
				this.updateCaptions();
			} else if(buttonPressed.id == 8) {
				this.generateCities = !this.generateCities;
				this.updateCaptions();
			} else if(buttonPressed.id == 9) {
				this.deadManChest = !this.deadManChest;
				this.updateCaptions();
			} else if(buttonPressed.id == 10) {
				this.cityChunkChance++; if (this.cityChunkChance == 3) this.cityChunkChance = 0;
				this.cityChance = GuiCreateWorld.cityChanceValues[this.cityChunkChance];
				this.updateCaptions();
			}
		}

	}

	protected void keyTyped(char c, int keyCode) {
		if(this.textboxWorldName.isFocused && !this.moreOptions) {
			this.textboxWorldName.textboxKeyTyped(c, keyCode);
			this.localizedNewWorldText = this.textboxWorldName.getText();
		} else if(this.textboxSeed.isFocused && this.moreOptions) {
			this.textboxSeed.textboxKeyTyped(c, keyCode);
			this.seed = this.textboxSeed.getText();
		}

		if(c == 13) {
			this.actionPerformed((GuiButton)this.controlList.get(0));
		}

		((GuiButton)this.controlList.get(0)).enabled = this.textboxWorldName.getText().length() > 0;
		this.makeUseableName();
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if(!this.moreOptions) {
			this.textboxWorldName.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			this.textboxSeed.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		StringTranslate translator = StringTranslate.getInstance();
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.create"), this.width / 2, 20, 0xFFFFFF);
		if(!this.moreOptions) {
			this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.enterName"), this.width / 2, 47, 10526880);
			this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.resultFolder") + " " + this.folderName, this.width / 2, 85, 10526880);
			this.textboxWorldName.drawTextBox();
			this.drawCenteredString(this.fontRenderer, this.gameModeDescriptionLine1, this.width / 2, 122, 10526880);
			this.drawCenteredString(this.fontRenderer, this.gameModeDescriptionLine2, this.width / 2, 134, 10526880);
		} else {
			this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.enterSeed"), this.width / 2, 47, 10526880);
			this.drawCenteredString(this.fontRenderer, translator.translateKey("selectWorld.seedInfo"), this.width / 2, 85, 10526880);
			this.textboxSeed.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
