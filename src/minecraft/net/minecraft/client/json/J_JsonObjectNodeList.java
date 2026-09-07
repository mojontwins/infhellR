package net.minecraft.client.json;

import java.util.HashMap;

/**
 * Materialises the {@link J_JsonObjectNodeBuilder} field list into a {@link HashMap}
 * of {@code field name -> value} for use by {@link J_JsonObject}.
 */
class J_JsonObjectNodeList extends HashMap<J_JsonStringNode, J_JsonNode> {
	private static final long serialVersionUID = 6952261357576353857L;

	J_JsonObjectNodeList(J_JsonObjectNodeBuilder builder) {
		for (J_JsonFieldBuilder field : J_JsonObjectNodeBuilder.getFields(builder)) {
			this.put(field.getName(), field.getValue());
		}
	}
}
