package net.minecraft.client.json;

/**
 * A selector for JSON array nodes. Checks that the node is an ARRAY and extracts the element list.
 */
final class J_JsonArrayNodeSelector {
	public static J_JsonNodeSelector create() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonArray;
			}
			public Object apply(Object obj) {
				return ((J_JsonArray) obj).getElements();
			}
			public String description() {
				return "an array";
			}
		});
	}
}
