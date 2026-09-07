package net.minecraft.client.json;

/**
 * Builds a string JSON node.
 */
public final class J_JsonStringNodeBuilder implements J_JsonNodeBuilder {
	private final String text;

	J_JsonStringNodeBuilder(String text) {
		this.text = text;
	}

	public J_JsonStringNode build() {
		return J_JsonNodeFactories.stringNode(this.text);
	}
}
