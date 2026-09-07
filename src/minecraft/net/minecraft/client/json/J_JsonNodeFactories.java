package net.minecraft.client.json;

import java.util.Arrays;
import java.util.Map;

/**
 * Factory methods for all node types.
 * Used both by the parser ({@link J_JsonNodeFactories#trueNode()} etc.) and
 * by direct programmatic construction ({@link J_JsonNodeFactories#array(Iterable)},
 * {@link J_JsonNodeFactories#object(Map)}).
 */
public final class J_JsonNodeFactories {
	public static J_JsonNode nullNode() {
		return J_JsonConstants.NULL;
	}

	public static J_JsonNode trueNode() {
		return J_JsonConstants.TRUE;
	}

	public static J_JsonNode falseNode() {
		return J_JsonConstants.FALSE;
	}

	public static J_JsonStringNode stringNode(String value) {
		return new J_JsonStringNode(value);
	}

	public static J_JsonNode numberNode(String value) {
		return new J_JsonNumberNode(value);
	}

	public static J_JsonRootNode array(Iterable<? extends J_JsonNode> elements) {
		return new J_JsonArray(elements);
	}

	public static J_JsonRootNode buildPath(J_JsonNode... elements) {
		return array(Arrays.asList(elements));
	}

	public static J_JsonRootNode object(Map<J_JsonStringNode, J_JsonNode> fields) {
		return new J_JsonObject(fields);
	}
}
