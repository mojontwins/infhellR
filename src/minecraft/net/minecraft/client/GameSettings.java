package net.minecraft.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import net.minecraft.game.GameSettingsValues;
import net.minecraft.game.StringTranslate;
import net.minecraft.game.achievements.StatCollector;
import net.minecraft.client.render.GraphicsModeSorter;

/**
 * Persisted game options manager. Reads and writes the {@code options.txt} file in the
 * Minecraft data directory, exposing typed getters and setters that delegate to the
 * shared {@link net.minecraft.game.GameSettingsValues} static values block.
 *
 * <p>Options are divided into three categories:</p>
 * <ul>
 *   <li><b>Float options</b> — volume, FOV, gamma, sensitivity (see {@link #setOptionFloatValue}).</li>
 *   <li><b>Boolean options</b> — toggles like invert mouse, view bobbing (see {@link #getOptionOrdinalValue}).</li>
 *   <li><b>Enum options</b> — render distance, difficulty, graphics quality, etc. (see
 *       {@link #setOptionValue} and {@link #getKeyBinding}).</li>
 * </ul>
 *
 * <p>Note: the actual current values live in {@link net.minecraft.game.GameSettingsValues}
 * (a static singleton). This class is the persistence layer that maps the text file
 * onto those static fields.</p>
 */
public class GameSettings {

    /** Local reference to the Minecraft instance, used to refresh renderers on option changes. */
    protected Minecraft minecraft;

    /** The {@code options.txt} file inside the Minecraft data directory. */
    private File optionsFile;

    /** Populated at class-load time with all available fullscreen display modes. */
    public static final ArrayList<String> AVAILABLE_DISPLAY_MODES = new ArrayList<String>();

    /** Localized display strings for the four render-distance choices. */
    public static final String[] RENDER_DISTANCES = new String[]{
        "options.renderDistance.far",
        "options.renderDistance.normal",
        "options.renderDistance.short",
        "options.renderDistance.tiny"
    };

    /** Localized display strings for the four difficulty levels. */
    public static final String[] DIFFICULTIES = new String[]{
        "options.difficulty.peaceful",
        "options.difficulty.easy",
        "options.difficulty.normal",
        "options.difficulty.hard"
    };

    /** Localized display strings for the four GUI scale options. */
    public static final String[] GUI_SCALES = new String[]{
        "options.guiScale.auto",
        "options.guiScale.small",
        "options.guiScale.normal",
        "options.guiScale.large"
    };

    /** Localized display strings for the three FPS limit modes. */
    public static final String[] LIMIT_FRAMERATES = new String[]{
        "performance.max",
        "performance.balanced",
        "performance.powersaver"
    };

    /**
     * Constructs a new {@code GameSettings} that will read/write to
     * {@code <minecraftDir>/options.txt}.
     *
     * @param minecraft     the client instance (used to trigger renderer rebuilds on option changes)
     * @param minecraftDir  the Minecraft data directory
     */
    public GameSettings(Minecraft minecraft, File minecraftDir) {
        this.minecraft = minecraft;
        this.optionsFile = new File(minecraftDir, "options.txt");
        this.loadOptions();
    }

    /** Default constructor — creates an unlinked instance. */
    public GameSettings() {
    }

    /**
     * Returns the localized description of the key bound to the given option index.
     *
     * @param optionIndex index into {@link GameSettingsKeys#keyBindings}
     * @return the translated key description string
     */
    public String getKeyBindingDescription(int optionIndex) {
        StringTranslate translator = StringTranslate.getInstance();
        return translator.translateKey(GameSettingsKeys.keyBindings[optionIndex].keyDescription);
    }

    /**
     * Returns the keyboard key name (e.g. "LEFT", "F") bound to the given option.
     *
     * @param optionIndex index into {@link GameSettingsKeys#keyBindings}
     * @return the key name from LWJGL's keyboard map
     */
    public String getOptionDisplayString(int optionIndex) {
        return Keyboard.getKeyName(GameSettingsKeys.keyBindings[optionIndex].keyCode);
    }

    /**
     * Binds a new key code to the option at the given index and persists the change.
     *
     * @param optionIndex index into {@link GameSettingsKeys#keyBindings}
     * @param keyCode     the new LWJGL key code
     */
    public void setKeyBinding(int optionIndex, int keyCode) {
        GameSettingsKeys.keyBindings[optionIndex].keyCode = keyCode;
        this.saveOptions();
    }

    /**
     * Sets a floating-point option value.
     *
     * @param option the option to change
     * @param value  the new value, typically in the range [0, 1]
     */
    public void setOptionFloatValue(EnumOptions option, float value) {
        if (option == EnumOptions.MUSIC) {
            GameSettingsValues.musicVolume = value;
            this.minecraft.sndManager.onSoundOptionsChanged();
        }

        if (option == EnumOptions.SOUND) {
            GameSettingsValues.soundVolume = value;
            this.minecraft.sndManager.onSoundOptionsChanged();
        }

        if (option == EnumOptions.SENSITIVITY) {
            GameSettingsValues.mouseSensitivity = value;
        }

        if (option == EnumOptions.FOV) {
            GameSettingsValues.FOV = (int)(value * 60);
        }

        if (option == EnumOptions.GAMMA) {
            GameSettingsValues.gammaSetting = value;
        }

        if (option == EnumOptions.AO_LEVEL) {
            GameSettingsValues.ofAoLevel = value;
            GameSettingsValues.ambientOcclusion = GameSettingsValues.ofAoLevel > 0.0F;
            this.minecraft.renderGlobal.loadRenderers();
        }
    }

    /**
     * Increments or decrements an enum/ordinal option by {@code delta}.
     *
     * @param option the option to change
     * @param delta  +1 to cycle forward, -1 to cycle backward
     */
    public void setOptionValue(EnumOptions option, int delta) {
        if (option == EnumOptions.INVERT_MOUSE) {
            GameSettingsValues.invertMouse = !GameSettingsValues.invertMouse;
        }

        if (option == EnumOptions.RENDER_DISTANCE) {
            GameSettingsValues.renderDistance = (GameSettingsValues.renderDistance + delta) & 3;
        }

        if (option == EnumOptions.GUI_SCALE) {
            GameSettingsValues.guiScale = (GameSettingsValues.guiScale + delta) & 3;
        }

        if (option == EnumOptions.VIEW_BOBBING) {
            GameSettingsValues.viewBobbing = !GameSettingsValues.viewBobbing;
        }

        if (option == EnumOptions.ADVANCED_OPENGL) {
            GameSettingsValues.advancedOpengl = !GameSettingsValues.advancedOpengl;
            this.minecraft.renderGlobal.loadRenderers();
        }

        if (option == EnumOptions.USE_VBO) {
            GameSettingsValues.useVbo = !GameSettingsValues.useVbo;
        }

        if (option == EnumOptions.ANAGLYPH) {
            GameSettingsValues.anaglyph = !GameSettingsValues.anaglyph;
            this.minecraft.renderEngine.refreshTextures();
        }

        if (option == EnumOptions.CLEAR_WATERS) {
            GameSettingsValues.clearWaters = !GameSettingsValues.clearWaters;
            this.minecraft.renderGlobal.loadRenderers();
        }

        if (option == EnumOptions.FRAMERATE_LIMIT) {
            GameSettingsValues.limitFramerate = (GameSettingsValues.limitFramerate + delta + 3) % 3;
        }

        if (option == EnumOptions.DIFFICULTY) {
            GameSettingsValues.difficulty = (GameSettingsValues.difficulty + delta) & 3;
        }

        if (option == EnumOptions.GRAPHICS) {
            GameSettingsValues.fancyGraphics = !GameSettingsValues.fancyGraphics;
            this.minecraft.renderGlobal.loadRenderers();
        }

        if (option == EnumOptions.HAND) {
            GameSettingsValues.retardedArm = !GameSettingsValues.retardedArm;
        }

        if (option == EnumOptions.THREADED_LIGHT) {
            GameSettingsValues.threadedLighting = !GameSettingsValues.threadedLighting;
        }

        if (option == EnumOptions.AMBIENT_OCCLUSION) {
            GameSettingsValues.ambientOcclusion = !GameSettingsValues.ambientOcclusion;
            this.minecraft.renderGlobal.loadRenderers();
        }

        if (option == EnumOptions.IS_CREATIVE) {
            GameSettingsValues.isCreative = !GameSettingsValues.isCreative;
        }

        if (option == EnumOptions.ENABLE_CHEATS) {
            GameSettingsValues.enableCheats = !GameSettingsValues.enableCheats;
        }

        if (option == EnumOptions.DEAD_MAN_CHEST) {
            GameSettingsValues.deadManChest = !GameSettingsValues.deadManChest;
        }

        if (option == EnumOptions.CRAFT_GUIDE) {
            GameSettingsValues.craftGuide = !GameSettingsValues.craftGuide;
        }

        if (option == EnumOptions.COLOURED_ATHMOSPHERICS) {
            GameSettingsValues.colouredAthmospherics = !GameSettingsValues.colouredAthmospherics;
        }

        if (option == EnumOptions.DISPLAY_MODES) {
            int index = AVAILABLE_DISPLAY_MODES.indexOf(GameSettingsValues.displayMode);
            index++;
            if (index >= AVAILABLE_DISPLAY_MODES.size()) index = 0;
            GameSettingsValues.displayMode = AVAILABLE_DISPLAY_MODES.get(index);
        }

        if (option == EnumOptions.MELTBUILD) {
            GameSettingsValues.meltBuild = !GameSettingsValues.meltBuild;
        }

        if (option == EnumOptions.MIPMAP_LEVEL) {
            GameSettingsValues.ofMipmapLevel++;
            if (GameSettingsValues.ofMipmapLevel > 4) {
                GameSettingsValues.ofMipmapLevel = 0;
            }
            this.minecraft.renderEngine.refreshTextures();
        }

        if (option == EnumOptions.MIPMAP_TYPE) {
            GameSettingsValues.ofMipmapLinear = !GameSettingsValues.ofMipmapLinear;
            this.minecraft.renderEngine.refreshTextures();
        }

        this.saveOptions();
    }

    /**
     * Returns the current floating-point value for the given option.
     *
     * @param option the option to query
     * @return the current value, or 0.0 if the option has no float value
     */
    public float getOptionFloatValue(EnumOptions option) {
        if (option == EnumOptions.MUSIC) return GameSettingsValues.musicVolume;
        if (option == EnumOptions.SOUND) return GameSettingsValues.soundVolume;
        if (option == EnumOptions.SENSITIVITY) return GameSettingsValues.mouseSensitivity;
        if (option == EnumOptions.FOV) return (float)GameSettingsValues.FOV / 60.0F;
        if (option == EnumOptions.GAMMA) return GameSettingsValues.gammaSetting;
        if (option == EnumOptions.AO_LEVEL) return GameSettingsValues.ofAoLevel;
        return 0.0F;
    }

    /**
     * Returns the current boolean toggle state for the given option.
     *
     * @param option the boolean option to query
     * @return true if the option is currently enabled
     */
    public boolean getOptionOrdinalValue(EnumOptions option) {
        switch (option) {
        case INVERT_MOUSE:       return GameSettingsValues.invertMouse;
        case VIEW_BOBBING:       return GameSettingsValues.viewBobbing;
        case CLEAR_WATERS:       return GameSettingsValues.clearWaters;
        case ADVANCED_OPENGL:    return GameSettingsValues.advancedOpengl;
        case USE_VBO:            return GameSettingsValues.useVbo;
        case AMBIENT_OCCLUSION:  return GameSettingsValues.ambientOcclusion;
        case COLOURED_ATHMOSPHERICS: return GameSettingsValues.colouredAthmospherics;
        case MELTBUILD:          return GameSettingsValues.meltBuild;
        case HAND:               return GameSettingsValues.retardedArm;
        case THREADED_LIGHT:     return GameSettingsValues.threadedLighting;
        default:                return false;
        }
    }

    /**
     * Returns the fully formatted display string for the given option, including
     * the current value. Used by the options GUI to render each line.
     *
     * @param option the option to format
     * @return a string like "Render Distance: Far" or "Gamma: 1.50"
     */
    public String getKeyBinding(EnumOptions option) {
        StringTranslate translator = StringTranslate.getInstance();
        String label = translator.translateKey(option.getLocalizedKey()) + ": ";

        if (option.isFloat()) {
            float value = this.getOptionFloatValue(option);
            if (option == EnumOptions.SENSITIVITY) {
                if (value == 0.0F) {
                    return label + translator.translateKey("options.sensitivity.min");
                } else if (value == 1.0F) {
                    return label + translator.translateKey("options.sensitivity.max");
                } else {
                    return label + (int)(value * 200.0F) + "%";
                }
            } else if (option == EnumOptions.GAMMA) {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                return label + df.format(value);
            } else if (option == EnumOptions.FOV) {
                return label + (70 + (int)(value * 60));
            } else if (option == EnumOptions.AO_LEVEL) {
                if (value == 0.0F) {
                    return label + translator.translateKey("options.off");
                } else if (value == 1.0F) {
                    return label + "Full";
                } else {
                    return label + (int)(value * 100.0F) + "%";
                }
            } else {
                if (value == 0.0F) {
                    return label + translator.translateKey("options.off");
                } else {
                    return label + (int)(value * 100.0F) + "%";
                }
            }
        } else if (option.isBoolean()) {
            boolean enabled = this.getOptionOrdinalValue(option);
            return enabled ? label + translator.translateKey("options.on") : label + translator.translateKey("options.off");
        } else {
            switch (option) {
                case RENDER_DISTANCE:    return label + translator.translateKey(RENDER_DISTANCES[GameSettingsValues.renderDistance]);
                case DIFFICULTY:         return label + translator.translateKey(DIFFICULTIES[GameSettingsValues.difficulty]);
                case GUI_SCALE:          return label + translator.translateKey(GUI_SCALES[GameSettingsValues.guiScale]);
                case FRAMERATE_LIMIT:   return label + StatCollector.translateToLocal(LIMIT_FRAMERATES[GameSettingsValues.limitFramerate]);
                case GRAPHICS:           return (GameSettingsValues.fancyGraphics
                                               ? label + translator.translateKey("options.graphics.fancy")
                                               : label + translator.translateKey("options.graphics.fast"));
                case HAND:               return label + (GameSettingsValues.retardedArm
                                               ? translator.translateKey("options.yes")
                                               : translator.translateKey("options.no"));
                case THREADED_LIGHT:     return label + (GameSettingsValues.threadedLighting
                                               ? translator.translateKey("options.on")
                                               : translator.translateKey("options.off"));
                case IS_CREATIVE:        return label + (GameSettingsValues.isCreative
                                               ? translator.translateKey("options.creative")
                                               : translator.translateKey("options.survival"));
                case ENABLE_CHEATS:      return label + (GameSettingsValues.enableCheats
                                               ? translator.translateKey("options.yes")
                                               : translator.translateKey("options.no"));
                case DEAD_MAN_CHEST:     return label + (GameSettingsValues.deadManChest
                                               ? translator.translateKey("options.yes")
                                               : translator.translateKey("options.no"));
                case CRAFT_GUIDE:        return label + (GameSettingsValues.craftGuide
                                               ? translator.translateKey("options.yes")
                                               : translator.translateKey("options.no"));
                case COLOURED_ATHMOSPHERICS: return label + (GameSettingsValues.colouredAthmospherics
                                               ? translator.translateKey("options.on")
                                               : translator.translateKey("options.off"));
                case CLEAR_WATERS:       return label + (GameSettingsValues.clearWaters
                                               ? translator.translateKey("options.on")
                                               : translator.translateKey("options.off"));
                case DISPLAY_MODES:      return label + GameSettingsValues.displayMode;
                case MIPMAP_LEVEL:       return label + GameSettingsValues.ofMipmapLevel;
                case MIPMAP_TYPE:        return GameSettingsValues.ofMipmapLinear ? label + "Linear" : label + "Nearest";
                default:                 return label;
            }
        }
    }

    /**
     * Loads all persisted options from {@code options.txt}.
     * Each line is in the form {@code key:value}. Unknown keys are silently skipped.
     */
    public void loadOptions() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(this.optionsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(":");
                    String key = parts[0];
                    String value = parts[1];

                    switch (key) {
                        case "music":          GameSettingsValues.musicVolume = this.parseFloatOrBoolean(value); break;
                        case "sound":          GameSettingsValues.soundVolume = this.parseFloatOrBoolean(value); break;
                        case "mouseSensitivity": GameSettingsValues.mouseSensitivity = this.parseFloatOrBoolean(value); break;
                        case "invertYMouse":   GameSettingsValues.invertMouse = value.equals("true"); break;
                        case "viewDistance":   GameSettingsValues.renderDistance = Integer.parseInt(value); break;
                        case "guiScale":       GameSettingsValues.guiScale = Integer.parseInt(value); break;
                        case "bobView":        GameSettingsValues.viewBobbing = value.equals("true"); break;
                        case "anaglyph3d":     GameSettingsValues.anaglyph = value.equals("true"); break;
                        case "clearWaters":   GameSettingsValues.clearWaters = value.equals("true"); break;
                        case "advancedOpengl": GameSettingsValues.advancedOpengl = value.equals("true"); break;
                        case "useVbo":         GameSettingsValues.useVbo = value.equals("true"); break;
                        case "fpsLimit":       GameSettingsValues.limitFramerate = Integer.parseInt(value); break;
                        case "difficulty":     GameSettingsValues.difficulty = Integer.parseInt(value); break;
                        case "fancyGraphics": GameSettingsValues.fancyGraphics = value.equals("true"); break;
                        case "hand":           GameSettingsValues.retardedArm = value.equals("true"); break;
                        case "threadedLighting": GameSettingsValues.threadedLighting = value.equals("true"); break;
                        case "ao":
                            GameSettingsValues.ambientOcclusion = value.equals("true");
                            GameSettingsValues.ofAoLevel = GameSettingsValues.ambientOcclusion ? 1.0F : 0.0F;
                            break;
                        case "skin":           GameSettingsValues.skin = value; break;
                        case "lastServer":     GameSettingsValues.lastServer = value; break;
                        case "FOV":            GameSettingsValues.FOV = Integer.parseInt(value); break;
                        case "gammaSetting":   GameSettingsValues.gammaSetting = this.parseFloatOrBoolean(value); break;
                        case "colouredAthmospherics": GameSettingsValues.colouredAthmospherics = value.equals("true"); break;
                        case "displayMode":    GameSettingsValues.displayMode = value; break;
                        case "meltBuild":      GameSettingsValues.meltBuild = value.equals("true"); break;
                        case "ofMipmapLevel":
                            GameSettingsValues.ofMipmapLevel = Integer.valueOf(value).intValue();
                            GameSettingsValues.ofMipmapLevel = Math.max(0, Math.min(4, GameSettingsValues.ofMipmapLevel));
                            break;
                        case "ofMipmapLinear": GameSettingsValues.ofMipmapLinear = Boolean.valueOf(value).booleanValue(); break;
                        default:
                            // Key-binding entries: "key_<description>:<code>"
                            if (key.startsWith("key_")) {
                                for (int i = 0; i < GameSettingsKeys.keyBindings.length; ++i) {
                                    if (key.equals("key_" + GameSettingsKeys.keyBindings[i].keyDescription)) {
                                        GameSettingsKeys.keyBindings[i].keyCode = Integer.parseInt(value);
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Skipping bad option line: " + line);
                }
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Failed to load options");
            e.printStackTrace();
        }
    }

    /**
     * Parses a string as either a float, or the booleans "true"/"false".
     *
     * @param value the string to parse
     * @return 1.0 for "true", 0.0 for "false", otherwise the parsed float
     */
    private float parseFloatOrBoolean(String value) {
        if (value.equals("true")) return 1.0F;
        if (value.equals("false")) return 0.0F;
        return Float.parseFloat(value);
    }

    /**
     * Persists all current option values to {@code options.txt}.
     * Each value is written as {@code key:value}, one per line.
     */
    public void saveOptions() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.optionsFile));
            writer.println("music:" + GameSettingsValues.musicVolume);
            writer.println("sound:" + GameSettingsValues.soundVolume);
            writer.println("invertYMouse:" + GameSettingsValues.invertMouse);
            writer.println("mouseSensitivity:" + GameSettingsValues.mouseSensitivity);
            writer.println("viewDistance:" + GameSettingsValues.renderDistance);
            writer.println("guiScale:" + GameSettingsValues.guiScale);
            writer.println("bobView:" + GameSettingsValues.viewBobbing);
            writer.println("anaglyph3d:" + GameSettingsValues.anaglyph);
            writer.println("advancedOpengl:" + GameSettingsValues.advancedOpengl);
            writer.println("useVbo:" + GameSettingsValues.useVbo);
            writer.println("fpsLimit:" + GameSettingsValues.limitFramerate);
            writer.println("difficulty:" + GameSettingsValues.difficulty);
            writer.println("fancyGraphics:" + GameSettingsValues.fancyGraphics);
            writer.println("hand:" + GameSettingsValues.retardedArm);
            writer.println("threadedLighting:" + GameSettingsValues.threadedLighting);
            writer.println("clearWaters:" + GameSettingsValues.clearWaters);
            writer.println("ao:" + GameSettingsValues.ambientOcclusion);
            writer.println("skin:" + GameSettingsValues.skin);
            writer.println("lastServer:" + GameSettingsValues.lastServer);
            writer.println("FOV:" + GameSettingsValues.FOV);
            writer.println("gammaSetting:" + GameSettingsValues.gammaSetting);
            writer.println("colouredAthmospherics:" + GameSettingsValues.colouredAthmospherics);
            writer.println("displayMode:" + GameSettingsValues.displayMode);
            writer.println("meltBuild:" + GameSettingsValues.meltBuild);

            for (int i = 0; i < GameSettingsKeys.keyBindings.length; ++i) {
                writer.println("key_" + GameSettingsKeys.keyBindings[i].keyDescription + ":" + GameSettingsKeys.keyBindings[i].keyCode);
            }
            writer.println("ofMipmapLevel:" + GameSettingsValues.ofMipmapLevel);
            writer.println("ofMipmapLinear:" + GameSettingsValues.ofMipmapLinear);
            writer.println("ofAoLevel:" + GameSettingsValues.ofAoLevel);

            writer.close();
        } catch (Exception e) {
            System.out.println("Failed to save options");
            e.printStackTrace();
        }
    }

    /**
     * Static initializer: enumerates all available fullscreen display modes for the
     * current desktop resolution and populates {@link #AVAILABLE_DISPLAY_MODES}.
     */
    static {
        DisplayMode currentMode = Display.getDisplayMode();
        ArrayList<DisplayMode> resolutions = new ArrayList<DisplayMode>();
        AVAILABLE_DISPLAY_MODES.add(GameSettingsValues.DEFAULT_DISPLAY_STRING);

        try {
            DisplayMode[] availableModes = Display.getAvailableDisplayModes();
            for (DisplayMode mode : availableModes) {
                resolutions.add(mode);
            }
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        for (DisplayMode mode : resolutions) {
            if (mode.getBitsPerPixel() == currentMode.getBitsPerPixel()
                    && mode.getFrequency() == currentMode.getFrequency()) {
                AVAILABLE_DISPLAY_MODES.add(
                    mode.getWidth() + "x" + mode.getHeight() + "x"
                    + mode.getBitsPerPixel() + " " + mode.getFrequency() + "Hz");
            }
        }

        Collections.sort(AVAILABLE_DISPLAY_MODES, new GraphicsModeSorter());
    }
}
