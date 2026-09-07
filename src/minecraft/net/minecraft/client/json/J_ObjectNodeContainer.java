package net.minecraft.client.json;

/**
 * Object container: adds fields to the object builder.
 */
class J_ObjectNodeContainer implements J_NodeContainer {
	final J_JsonObjectNodeBuilder objectBuilder;
	final J_JsonListenerToJdomAdapter adapter;

	J_ObjectNodeContainer(J_JsonListenerToJdomAdapter adapter, J_JsonObjectNodeBuilder objectBuilder) {
		this.adapter = adapter;
		this.objectBuilder = objectBuilder;
	}

	public void visitNode(J_JsonNodeBuilder node) {
		throw new RuntimeException("Coding failure in Argo:  Attempt to add a node to an object.");
	}

	public void visitField(J_JsonFieldBuilder field) {
		this.objectBuilder.addField(field);
	}
}
