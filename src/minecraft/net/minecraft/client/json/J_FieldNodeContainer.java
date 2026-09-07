package net.minecraft.client.json;

/**
 * Field container: stores the field name and awaits the value node.
 */
class J_FieldNodeContainer implements J_NodeContainer {
	final J_JsonFieldBuilder field;
	final J_JsonListenerToJdomAdapter adapter;

	J_FieldNodeContainer(J_JsonListenerToJdomAdapter adapter, J_JsonFieldBuilder field) {
		this.adapter = adapter;
		this.field = field;
	}

	public void visitNode(J_JsonNodeBuilder node) {
		this.field.withValue(node);
	}

	public void visitField(J_JsonFieldBuilder field) {
		throw new RuntimeException("Coding failure in Argo:  Attempt to add a field to a field.");
	}
}
