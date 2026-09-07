package net.minecraft.client;

/**
 * Enumeration of all configurable in-game options displayed in the options GUI.
 *
 * <p>Each constant carries three pieces of metadata:</p>
 * <ul>
 *   <li>{@code localizedKey} — the localization key used to look up the displayed label.</li>
 *   <li>{@code isFloat} — true for slider options (volume, sensitivity, gamma, FOV).</li>
 *   <li>{@code isBoolean} — true for on/off toggle options.</li>
 * </ul>
 *
 * <p>Options that are neither float nor boolean are ordinal/cycle options (render distance,
 * difficulty, graphics quality).</p>
 */
public enum EnumOptions {

    MUSIC("options.music", true, false),
    SOUND("options.sound", true, false),
    INVERT_MOUSE("options.invertMouse", false, true),
    SENSITIVITY("options.sensitivity", true, false),
    RENDER_DISTANCE("options.renderDistance", false, false),
    VIEW_BOBBING("options.viewBobbing", false, true),
    ANAGLYPH("options.anaglyph", false, true),
    ADVANCED_OPENGL("options.advancedOpengl", false, true),
    USE_VBO("options.useVbo", false, true),
    FRAMERATE_LIMIT("options.framerateLimit", false, false),
    DIFFICULTY("options.difficulty", false, false),
    GRAPHICS("options.graphics", false, false),
    AMBIENT_OCCLUSION("options.ao", false, true),
    GUI_SCALE("options.guiScale", false, false),

    IS_CREATIVE("options.isCreative", false, false),
    ENABLE_CHEATS("options.enableCheats", false, false),
    DEAD_MAN_CHEST("options.deadManChest", false, false),
    CRAFT_GUIDE("options.craftingGuide", false, false),

    CLEAR_WATERS("options.clearWaters", false, false),
    FOV("options.fov", true, false),
    GAMMA("options.gamma", true, false),
    COLOURED_ATHMOSPHERICS("options.colouredAthmospherics", false, false),
    DISPLAY_MODES("options.displayModes", false, false),
    MELTBUILD("options.meltBuild", false, true),
    HAND("options.hand", false, true),
    THREADED_LIGHT("options.threaded_light", false, true),

    MIPMAP_LEVEL("Mipmap Level", false, false),
    MIPMAP_TYPE("Mipmap Type", false, false),
    AO_LEVEL("Smooth Lighting", true, false),
    ;

    /** True if this option is a float slider (volume, sensitivity, gamma, FOV). */
    private final boolean isFloat;
    /** True if this option is a boolean on/off toggle. */
    private final boolean isBoolean;
    /** Localization key for the human-readable option label. */
    private final String localizedKey;

    EnumOptions(String caption, boolean floatOption, boolean booleanOption) {
        this.localizedKey = caption;
        this.isFloat = floatOption;
        this.isBoolean = booleanOption;
    }

    /**
     * Looks up an {@code EnumOptions} by its ordinal position in the enum.
     *
     * @param ordinal the ordinal to find
     * @return the matching option, or {@code null} if out of range
     */
    public static EnumOptions getEnumOptions(int ordinal) {
        for (EnumOptions opt : values()) {
            if (opt.getOrdinal() == ordinal) {
                return opt;
            }
        }
        return null;
    }

    /** @return true if this is a float slider option. */
    public boolean isFloat() {
        return this.isFloat;
    }

    /** @return true if this is a boolean toggle option. */
    public boolean isBoolean() {
        return this.isBoolean;
    }

    /** @return the enum ordinal value. */
    public int getOrdinal() {
        return this.ordinal();
    }

    /** @return the localization key for the option's display label. */
    public String getLocalizedKey() {
        return this.localizedKey;
    }
}
