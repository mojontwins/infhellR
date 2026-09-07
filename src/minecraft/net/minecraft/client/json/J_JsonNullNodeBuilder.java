package net.minecraft.client.json;

/**
 * Builds a "null" JSON literal.
 */
final class J_JsonNullNodeBuilder implements J_JsonNodeBuilder {
	public J_JsonNode build() {
		return J_JsonNodeFactories.nullNode();
	}
}
