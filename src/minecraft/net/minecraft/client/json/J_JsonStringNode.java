package net.minecraft.client.json;

import java.util.List;
import java.util.Map;

/**
 * A JSON string literal (the leaf key/value type).
 * Comparable so that {@link J_JsonStringNode} keys can be sorted for stable output.
 */
public final class J_JsonStringNode extends J_JsonNode implements Comparable<Object> {
	private final String text;

	J_JsonStringNode(String text) {
		if (text == null) {
			throw new NullPointerException("Attempt to construct a JsonString with a null value.");
		} else {
			this.text = text;
		}
	}

	public EnumJsonNodeType getType() {
		return EnumJsonNodeType.STRING;
	}

	public String getText() {
		return this.text;
	}

	public Map<J_JsonStringNode, J_JsonNode> getFields() {
		throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
	}

	public List<J_JsonNode> getElements() {
		throw new IllegalStateException("Attempt to get elements on a JsonNode without elements.");
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj != null && this.getClass() == obj.getClass()) {
			J_JsonStringNode other = (J_JsonStringNode) obj;
			return this.text.equals(other.text);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.text.hashCode();
	}

	public String toString() {
		return "JsonStringNode value:[" + this.text + "]";
	}

	public int compareTo(J_JsonStringNode other) {
		return this.text.compareTo(other.text);
	}

	public int compareTo(Object obj) {
		return this.compareTo((J_JsonStringNode) obj);
	}
}
