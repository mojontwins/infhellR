package net.minecraft.client.json;

/**
 * Base exception thrown when a JSON node does not match a selector.
 */
public class J_JsonNodeDoesNotMatchJsonNodeSelectorException extends IllegalArgumentException {
	private static final long serialVersionUID = -726288864488212032L;

	J_JsonNodeDoesNotMatchJsonNodeSelectorException(String message) {
		super(message);
	}
}
