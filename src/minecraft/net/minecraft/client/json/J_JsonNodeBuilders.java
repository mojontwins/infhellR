package net.minecraft.client.json;

/**
 * Factory methods for all node builder types.
 */
public final class J_JsonNodeBuilders {
	public static J_JsonNodeBuilder nullNode() {
		return new J_JsonNullNodeBuilder();
	}

	public static J_JsonNodeBuilder trueNode() {
		return new J_JsonTrueNodeBuilder();
	}

	public static J_JsonNodeBuilder falseNode() {
		return new J_JsonFalseNodeBuilder();
	}

	public static J_JsonNodeBuilder numberNode(String value) {
		return new J_JsonNumberNodeBuilder(value);
	}

	public static J_JsonStringNodeBuilder stringNode(String value) {
		return new J_JsonStringNodeBuilder(value);
	}

	public static J_JsonObjectNodeBuilder objectNode() {
		return new J_JsonObjectNodeBuilder();
	}

	public static J_JsonArrayNodeBuilder arrayNode() {
		return new J_JsonArrayNodeBuilder();
	}
}
