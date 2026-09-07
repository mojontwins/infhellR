package net.minecraft.client.json;

/**
 * A selector for a named field in a JSON object.
 */
final class J_JsonFieldNodeSelector {
	private final J_JsonStringNode fieldName;

	public J_JsonFieldNodeSelector(J_JsonStringNode fieldName) {
		this.fieldName = fieldName;
	}

	public J_JsonNodeSelector asSelector() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonObject
					&& ((J_JsonObject) obj).getFields().containsKey(fieldName);
			}
			public Object apply(Object obj) {
				return ((J_JsonObject) obj).getFields().get(fieldName);
			}
			public String description() {
				return "field [\"" + fieldName.getText() + "\"]";
			}
		});
	}
}
