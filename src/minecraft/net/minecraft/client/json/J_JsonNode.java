package net.minecraft.client.json;

import java.util.List;
import java.util.Map;

/**
 * Base class for all JSON node types (string, number, array, object, true, false, null).
 * Source: Mojang's "Argo" lightweight JSON library (originally derived from public domain code).
 * Lives in the client because Mojang embedded the whole library here.
 */
public abstract class J_JsonNode {
	public abstract EnumJsonNodeType getType();

	public abstract String getText();

	public abstract Map<J_JsonStringNode, J_JsonNode> getFields();

	public abstract List<J_JsonNode> getElements();

	public final String getStringValue(Object... pathElements) {
		return (String) this.wrapExceptionsFor(J_JsonNodeSelectors.asString(pathElements), this, pathElements);
	}

	public final String getNumberValue(Object... pathElements) {
		return (String) this.wrapExceptionsFor(J_JsonNodeSelectors.asNumber(pathElements), this, pathElements);
	}

	@SuppressWarnings("unchecked")
	public final List<J_JsonNode> getArrayNode(Object... pathElements) {
		return (List<J_JsonNode>) this.wrapExceptionsFor(J_JsonNodeSelectors.asArray(pathElements), this, pathElements);
	}

	public J_JsonObject getObjectNode(final Object... pathElements) {
		return (J_JsonObject) wrapExceptionsFor(J_JsonNodeSelectors.asObject(pathElements), this, pathElements);
	}

	private Object wrapExceptionsFor(J_JsonNodeSelector selector, J_JsonNode node, Object[] pathElements) {
		try {
			return selector.select(node);
		} catch (J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex) {
			throw J_JsonNodeDoesNotMatchPathElementsException.createWithPath(ex, pathElements, J_JsonNodeFactories.buildPath(new J_JsonNode[]{node}));
		}
	}
}
