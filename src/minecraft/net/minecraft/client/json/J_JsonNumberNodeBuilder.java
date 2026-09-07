package net.minecraft.client.json;

/**
 * Builds a number JSON node.
 */
final class J_JsonNumberNodeBuilder implements J_JsonNodeBuilder {
	private final J_JsonNode node;

	J_JsonNumberNodeBuilder(String text) {
		this.node = J_JsonNodeFactories.numberNode(text);
	}

	public J_JsonNode build() {
		return this.node;
	}
}
