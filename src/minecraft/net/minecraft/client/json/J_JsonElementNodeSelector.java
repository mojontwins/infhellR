package net.minecraft.client.json;

/**
 * A selector for an element at a specific index in a JSON array.
 */
final class J_JsonElementNodeSelector {
	private final int index;

	public J_JsonElementNodeSelector(int index) {
		this.index = index;
	}

	public J_JsonNodeSelector asSelector() {
		return new J_JsonNodeSelector(new J_Functor() {
			public boolean checkType(Object obj) {
				return obj instanceof J_JsonArray
					&& ((J_JsonArray) obj).getElements().size() > index;
			}
			public Object apply(Object obj) {
				return ((J_JsonArray) obj).getElements().get(index);
			}
			public String description() {
				return "element at index [" + index + "]";
			}
		});
	}
}
