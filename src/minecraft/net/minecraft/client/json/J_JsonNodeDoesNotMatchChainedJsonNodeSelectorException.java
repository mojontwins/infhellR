package net.minecraft.client.json;

import java.util.LinkedList;
import java.util.List;

/**
 * Exception thrown when a chained selector (two selectors applied sequentially) fails.
 * Tracks both the failed node and the path of selectors that were attempted.
 */
public final class J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException extends J_JsonNodeDoesNotMatchJsonNodeSelectorException {
	private static final long serialVersionUID = 7218739566926966671L;
	private final J_Functor failedFunctor;
	private final List<J_JsonNodeSelector> path;

	J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException(J_Functor failedFunctor, List<J_JsonNodeSelector> path) {
		super("Failed to match JSON node at [" + buildPathString(path) + "]");
		this.failedFunctor = failedFunctor;
		this.path = path;
	}

	static J_JsonNodeDoesNotMatchJsonNodeSelectorException create(J_Functor functor) {
		return new J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException(
			functor, new LinkedList<J_JsonNodeSelector>());
	}

	static J_JsonNodeDoesNotMatchJsonNodeSelectorException createWithFirst(
			J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex, J_JsonNodeSelector first) {
		List<J_JsonNodeSelector> newPath = new LinkedList<J_JsonNodeSelector>(ex.path);
		newPath.add(0, first);
		return new J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException(ex.failedFunctor, newPath);
	}

	static J_JsonNodeDoesNotMatchJsonNodeSelectorException createWithSecond(
			J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex, J_JsonNodeSelector first) {
		List<J_JsonNodeSelector> newPath = new LinkedList<J_JsonNodeSelector>(ex.path);
		newPath.add(first);
		return new J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException(ex.failedFunctor, newPath);
	}

	private static String buildPathString(List<J_JsonNodeSelector> path) {
		StringBuilder sb = new StringBuilder();
		for (int i = path.size() - 1; i >= 0; i--) {
			sb.append(path.get(i).toString());
			if (i > 0) sb.append(".");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "JsonNodeDoesNotMatchJsonNodeSelectorException{failedFunctor=" + this.failedFunctor + ", path=" + this.path + "}";
	}
}
