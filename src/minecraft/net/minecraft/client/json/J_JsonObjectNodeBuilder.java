package net.minecraft.client.json;

import java.util.LinkedList;
import java.util.List;

/**
 * Builds an object JSON node (a list of fields).
 */
public final class J_JsonObjectNodeBuilder implements J_JsonNodeBuilder {
	private final List<J_JsonFieldBuilder> fields = new LinkedList<J_JsonFieldBuilder>();

	public J_JsonObjectNodeBuilder addField(J_JsonFieldBuilder field) {
		this.fields.add(field);
		return this;
	}

	public J_JsonRootNode build() {
		return J_JsonNodeFactories.object(new J_JsonObjectNodeList(this));
	}

	static List<J_JsonFieldBuilder> getFields(J_JsonObjectNodeBuilder builder) {
		return builder.fields;
	}
}
