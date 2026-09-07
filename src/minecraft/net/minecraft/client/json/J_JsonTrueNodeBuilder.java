package net.minecraft.client.json;

/**
 * Builds a "true" JSON literal.
 */
final class J_JsonTrueNodeBuilder implements J_JsonNodeBuilder {
	public J_JsonNode build() {
		return J_JsonNodeFactories.trueNode();
	}
}
