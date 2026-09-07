package net.minecraft.client;

/**
 * A key-binding descriptor used to store the human-readable description and the
 * current LWJGL key code for one input action.
 */
public class KeyBinding {
    /** Human-readable label for this key (shown in the options GUI). */
    public String keyDescription;
    /** Current LWJGL key code (e.g. {@code Keyboard.KEY_W}). */
    public int keyCode;

    public KeyBinding(String keyDescription, int keyCode) {
        this.keyDescription = keyDescription;
        this.keyCode = keyCode;
    }
}
