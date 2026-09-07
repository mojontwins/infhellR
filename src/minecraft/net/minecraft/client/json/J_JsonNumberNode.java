package net.minecraft.client.json;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A JSON number literal (e.g. {@code 42}, {@code -3.14e0}). Validated against the
 * JSON number specification on construction.
 */
final class J_JsonNumberNode extends J_JsonNode {
	private static final Pattern NUMBER_PATTERN = Pattern.compile("(-?)(0|([1-9]([0-9]*)))(\\.[0-9]+)?((e|E)(\\+|-)?[0-9]+)?");
	private final String text;

	J_JsonNumberNode(String text) {
		if (text == null) {
			throw new NullPointerException("Attempt to construct a JsonNumber with a null value.");
		} else if (!NUMBER_PATTERN.matcher(text).matches()) {
			throw new IllegalArgumentException("Attempt to construct a JsonNumber with a String [" + text + "] that does not match the JSON number specification.");
		} else {
			this.text = text;
		}
	}

	public EnumJsonNodeType getType() {
		return EnumJsonNodeType.NUMBER;
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
			J_JsonNumberNode other = (J_JsonNumberNode) obj;
			return this.text.equals(other.text);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.text.hashCode();
	}

	public String toString() {
		return "JsonNumberNode value:[" + this.text + "]";
	}
}
