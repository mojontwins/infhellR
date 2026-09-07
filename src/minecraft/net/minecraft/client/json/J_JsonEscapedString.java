package net.minecraft.client.json;

/**
 * Escapes a Java string for use inside a JSON string value.
 * Handles: backslash, double-quote, backspace, form-feed, newline, carriage-return, tab.
 */
final class J_JsonEscapedString {
	private final String escaped;

	J_JsonEscapedString(String raw) {
		this.escaped = raw
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\b", "\\b")
			.replace("\f", "\\f")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

	@Override
	public String toString() {
		return this.escaped;
	}
}
