package net.minecraft.client.json;

/**
 * Factory for common JSON node selectors used to navigate into JSON trees.
 */
public final class J_JsonNodeSelectors {
	public static J_JsonNodeSelector asString(Object... path) {
		return buildPath(path, J_JsonStringNodeSelector.create());
	}

	public static J_JsonNodeSelector asNumber(Object... path) {
		return buildPath(path, J_JsonNumberNodeSelector.create());
	}

	public static J_JsonNodeSelector asArray(Object... path) {
		return buildPath(path, J_JsonArrayNodeSelector.create());
	}

	public static J_JsonNodeSelector asObject(Object... path) {
		return buildPath(path, J_JsonObjectNodeSelector.create());
	}

	public static J_JsonNodeSelector fieldSelector(String fieldName) {
		return new J_JsonFieldNodeSelector(J_JsonNodeFactories.stringNode(fieldName)).asSelector();
	}

	public static J_JsonNodeSelector arrayElement(int index) {
		return new J_JsonElementNodeSelector(index).asSelector();
	}

	private static J_JsonNodeSelector buildPath(Object[] path, J_JsonNodeSelector terminal) {
		J_JsonNodeSelector selector = terminal;
		for (int i = path.length - 1; i >= 0; i--) {
			if (path[i] instanceof Integer) {
				selector = chain(arrayElement((Integer) path[i]), selector);
			} else if (path[i] instanceof String) {
				selector = chain(fieldSelector((String) path[i]), selector);
			} else {
				throw new IllegalArgumentException("Path element [" + path[i] + "] must be Integer or String, got " + path[i].getClass().getName());
			}
		}
		return selector;
	}

	private static J_JsonNodeSelector chain(J_JsonNodeSelector a, J_JsonNodeSelector b) {
		return new J_JsonNodeSelector(new J_ChainedFunctor(a, b));
	}
}
