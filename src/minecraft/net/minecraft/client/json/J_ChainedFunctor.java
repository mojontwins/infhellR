package net.minecraft.client.json;

/**
 * A selector that chains two functors: applies the first, then the second on its result.
 */
final class J_ChainedFunctor implements J_Functor {
	private final J_JsonNodeSelector first;
	private final J_JsonNodeSelector second;

	J_ChainedFunctor(J_JsonNodeSelector first, J_JsonNodeSelector second) {
		this.first = first;
		this.second = second;
	}

	public boolean checkType(Object obj) {
		return this.first.matches(obj) && this.second.matches(this.first.select(obj));
	}

	public Object apply(Object obj) {
		Object result;
		try {
			result = this.first.select(obj);
		} catch (J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex) {
			throw J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException.createWithFirst(ex, this.first);
		}
		try {
			return this.second.select(result);
		} catch (J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException ex) {
			throw J_JsonNodeDoesNotMatchChainedJsonNodeSelectorException.createWithSecond(ex, this.first);
		}
	}

	public String description() {
		return this.second.selectFirst();
	}
}
