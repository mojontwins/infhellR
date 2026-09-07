package net.minecraft.client.json;

import java.util.List;
import java.util.Map;

/**
 * Singleton node for {@code null}, {@code true}, {@code false} JSON literals.
 */
final class J_JsonConstants extends J_JsonNode {
	static final J_JsonConstants NULL = new J_JsonConstants(EnumJsonNodeType.NULL);
	static final J_JsonConstants TRUE = new J_JsonConstants(EnumJsonNodeType.TRUE);
	static final J_JsonConstants FALSE = new J_JsonConstants(EnumJsonNodeType.FALSE);

	private final EnumJsonNodeType jsonNodeType;

	private J_JsonConstants(EnumJsonNodeType jsonNodeType) {
		this.jsonNodeType = jsonNodeType;
	}

	public EnumJsonNodeType getType() {
		return this.jsonNodeType;
	}

	public String getText() {
		throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
	}

	public Map<J_JsonStringNode, J_JsonNode> getFields() {
		throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
	}

	public List<J_JsonNode> getElements() {
		throw new IllegalStateException("Attempt to get elements on a JsonNode without elements.");
	}
}
