package net.minecraft.client.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A JSON array (e.g. {@code [1, 2, 3]}). Wraps an {@link Iterable} snapshot into
 * a stable {@link List} so consumers can't mutate the source.
 */
public final class J_JsonArray extends J_JsonRootNode {
	private final List<J_JsonNode> elements;

	public J_JsonArray(Iterable<? extends J_JsonNode> elements) {
		this.elements = asList(elements);
	}

	public EnumJsonNodeType getType() {
		return EnumJsonNodeType.ARRAY;
	}

	public List<J_JsonNode> getElements() {
		return new ArrayList<J_JsonNode>(this.elements);
	}

	public String getText() {
		throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
	}

	public Map<J_JsonStringNode, J_JsonNode> getFields() {
		throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj != null && this.getClass() == obj.getClass()) {
			J_JsonArray other = (J_JsonArray) obj;
			return this.elements.equals(other.elements);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.elements.hashCode();
	}

	public String toString() {
		return "JsonArray elements:[" + this.elements + "]";
	}

	private static List<J_JsonNode> asList(Iterable<? extends J_JsonNode> elements) {
		return new J_JsonNodeList(elements);
	}
}
