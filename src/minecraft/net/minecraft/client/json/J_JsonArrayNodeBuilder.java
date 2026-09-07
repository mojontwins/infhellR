package net.minecraft.client.json;

import java.util.LinkedList;
import java.util.List;

/**
 * Builds an array JSON node.
 */
public final class J_JsonArrayNodeBuilder implements J_JsonNodeBuilder {
	private final List<J_JsonNodeBuilder> elements = new LinkedList<J_JsonNodeBuilder>();

	public J_JsonArrayNodeBuilder add(J_JsonNodeBuilder element) {
		this.elements.add(element);
		return this;
	}

	public J_JsonRootNode build() {
		LinkedList<J_JsonNode> nodes = new LinkedList<J_JsonNode>();
		for (J_JsonNodeBuilder element : this.elements) {
			nodes.add(element.build());
		}
		return J_JsonNodeFactories.array(nodes);
	}
}
