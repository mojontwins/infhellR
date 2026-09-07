package net.minecraft.client.json;

/**
 * A selector for JSON object nodes. Checks that the node is an OBJECT and extracts the field map.
 */
final class J_JsonObjectNodeSelector {
	public static J_JsonNodeSelector create() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonObject;
			}
			public Object apply(Object obj) {
				return ((J_JsonObject) obj).getFields();
			}
			public String description() {
				return "an object";
			}
		});
	}
}
