package net.minecraft.client.json;

import java.util.ArrayList;

/**
 * A fixed {@link ArrayList} that copies elements from an {@link Iterable} at construction time.
 * Used internally by {@link J_JsonArray} to snapshot the source.
 */
final class J_JsonNodeList extends ArrayList<J_JsonNode> {
	private static final long serialVersionUID = -3886041211225729981L;

	J_JsonNodeList(Iterable<? extends J_JsonNode> source) {
		for (J_JsonNode node : source) {
			this.add(node);
		}
	}
}
