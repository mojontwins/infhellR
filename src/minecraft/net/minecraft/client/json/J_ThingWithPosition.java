package net.minecraft.client.json;

/**
 * Tracks the current column and line number in a reader.
 * Used by {@link J_InvalidSyntaxException} to report parse error positions.
 */
interface J_ThingWithPosition {
	int getColumn();

	int getLine();
}
