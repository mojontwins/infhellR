package net.minecraft.client;

import org.lwjgl.input.Keyboard;

/**
 * Static registry of all keyboard and mouse key bindings used by the game.
 *
 * <p>The individual bindings are separate static fields so they can be referenced
 * directly by the options GUI without going through the array. The {@link #keyBindings}
 * array provides ordered iteration for the options screen.</p>
 *
 * <p>Note: key codes are LWJGL constants (e.g. {@code 17} = W, {@code 30} = A, etc.).
 * Some bindings also have gamepad alternatives tracked separately.</p>
 */
public class GameSettingsKeys {

    public static KeyBinding keyBindForward   = new KeyBinding("key.forward", 17);
    public static KeyBinding keyBindLeft      = new KeyBinding("key.left", 30);
    public static KeyBinding keyBindBack       = new KeyBinding("key.back", 31);
    public static KeyBinding keyBindRight      = new KeyBinding("key.right", 32);
    public static KeyBinding keyBindJump      = new KeyBinding("key.jump", 57);
    public static KeyBinding keyBindInventory  = new KeyBinding("key.inventory", 18);
    public static KeyBinding keyBindDrop       = new KeyBinding("key.drop", 16);
    public static KeyBinding keyBindChat       = new KeyBinding("key.chat", 20);
    public static KeyBinding keyBindToggleFog   = new KeyBinding("key.fog", 33);
    public static KeyBinding keyBindSneak     = new KeyBinding("key.sneak", 42);
    public static KeyBinding keyBindCreative   = new KeyBinding("key.creativeInventory", Keyboard.KEY_C);

    public static KeyBinding keyBindMapZoom   = new KeyBinding("key.mapZoom", Keyboard.KEY_Z);
    public static KeyBinding keyBindMapMenu   = new KeyBinding("key.mapMenu", Keyboard.KEY_M);

    /** Ordered array of all key bindings, used for the options GUI. */
    public static KeyBinding[] keyBindings = new KeyBinding[]{
        keyBindForward, keyBindLeft, keyBindBack, keyBindRight,
        keyBindJump, keyBindSneak,
        keyBindDrop, keyBindInventory,
        keyBindChat, keyBindCreative,
        keyBindMapZoom, keyBindMapMenu
    };
}
