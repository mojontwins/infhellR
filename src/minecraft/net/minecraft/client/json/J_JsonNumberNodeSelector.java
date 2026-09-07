package net.minecraft.client.json;

/**
 * A selector for JSON number nodes. Checks that the node is a NUMBER and extracts the text value.
 */
public class J_JsonNumberNodeSelector {
	public static J_JsonNodeSelector create() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonNumberNode;
			}
			public Object apply(Object obj) {
				return ((J_JsonNumberNode) obj).getText();
			}
			public String description() {
				return "a numeric value";
			}
		});
	}
}
