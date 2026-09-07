package net.minecraft.client.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JSON object (e.g. {@code {"a": 1, "b": 2}}). Wraps a {@link Map} snapshot so
 * that mutations to the original don't leak into the node.
 */
public final class J_JsonObject extends J_JsonRootNode {
	private final Map<J_JsonStringNode, J_JsonNode> fields;

	public J_JsonObject(Map<J_JsonStringNode, J_JsonNode> fields) {
		this.fields = new HashMap<J_JsonStringNode, J_JsonNode>(fields);
	}

	public Map<J_JsonStringNode, J_JsonNode> getFields() {
		return new HashMap<J_JsonStringNode, J_JsonNode>(this.fields);
	}

	public EnumJsonNodeType getType() {
		return EnumJsonNodeType.OBJECT;
	}

	public String getText() {
		throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
	}

	public List<J_JsonNode> getElements() {
		throw new IllegalStateException("Attempt to get elements on a JsonNode without elements.");
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj != null && this.getClass() == obj.getClass()) {
			J_JsonObject other = (J_JsonObject) obj;
			return this.fields.equals(other.fields);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.fields.hashCode();
	}

	public String toString() {
		return "JsonObject fields:[" + this.fields + "]";
	}
}
