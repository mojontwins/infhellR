package net.minecraft.client.json;

/**
 * Serialises a JSON node tree back to a string.
 */
public interface J_JsonFormatter {
	String format(J_JsonRootNode root);
}
