package net.minecraft.client.json;

/**
 * A selector for JSON string nodes. Checks that the node is a STRING and extracts the text value.
 */
final class J_JsonStringNodeSelector {
	public static J_JsonNodeSelector create() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonStringNode;
			}
			public Object apply(Object obj) {
				return ((J_JsonStringNode) obj).getText();
			}
			public String description() {
				return "a string value";
			}
		});
	}
}
