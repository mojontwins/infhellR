package net.minecraft.client.json;

/**
 * Exception thrown when a selector path fails to resolve against a JSON tree.
 * Wraps a {@link J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException} and adds
 * the full path that was attempted.
 */
public final class J_JsonNodeDoesNotMatchPathElementsException extends J_JsonNodeDoesNotMatchJsonNodeSelectorException {
	private static final long serialVersionUID = 1048493850865530802L;
	private static final J_JsonFormatter FORMATTER = new J_CompactJsonFormatter();

	static J_JsonNodeDoesNotMatchPathElementsException createWithPath(
			J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex,
			Object[] pathElements,
			J_JsonRootNode rootNode) {
		return new J_JsonNodeDoesNotMatchPathElementsException(ex, pathElements, rootNode);
	}

	private J_JsonNodeDoesNotMatchPathElementsException(
			J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException inner,
			Object[] pathElements,
			J_JsonRootNode rootNode) {
		super(buildMessage(inner, pathElements, rootNode));
	}

	private static String buildMessage(J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException inner,
			Object[] pathElements, J_JsonRootNode rootNode) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object p : pathElements) {
			if (!first) sb.append(".");
			first = false;
			if (p instanceof String) {
				sb.append("\"").append(p).append("\"");
			} else {
				sb.append(p);
			}
		}
		return "Failed to find " + inner.getMessage() + " while resolving [" + sb + "] in " + FORMATTER.format(rootNode) + ".";
	}
}
