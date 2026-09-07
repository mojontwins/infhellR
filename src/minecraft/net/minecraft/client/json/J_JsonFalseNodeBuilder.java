package net.minecraft.client.json;

/**
 * Builds a "false" JSON literal.
 */
final class J_JsonFalseNodeBuilder implements J_JsonNodeBuilder {
	public J_JsonNode build() {
		return J_JsonNodeFactories.falseNode();
	}
}
