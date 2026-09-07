package net.minecraft.client.json;

/**
 * A predicate that can check a node type and extract a typed value.
 */
interface J_Functor {
	boolean checkType(Object obj);

	Object apply(Object obj);

	String description();
}
