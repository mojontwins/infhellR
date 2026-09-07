package net.minecraft.client.json;

/**
 * A selector that wraps a functor and exposes the {@link #matches(Object)} / {@link #select(Object)} API.
 * Multiple selectors can be chained via {@link #andThen(J_JsonNodeSelector)}.
 */
public final class J_JsonNodeSelector {
	private final J_Functor functor;

	J_JsonNodeSelector(J_Functor functor) {
		this.functor = functor;
	}

	public boolean matches(Object obj) {
		return this.functor.checkType(obj);
	}

	public Object select(Object obj) {
		return this.functor.apply(obj);
	}

	public J_JsonNodeSelector andThen(J_JsonNodeSelector next) {
		return new J_JsonNodeSelector(new J_ChainedFunctor(this, next));
	}

	String selectFirst() {
		return this.functor.description();
	}

	@Override
	public String toString() {
		return this.functor.description();
	}
}
