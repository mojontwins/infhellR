package net.minecraft.client.json;

/**
 * Array container: adds nodes directly to the array builder.
 */
class J_ArrayNodeContainer implements J_NodeContainer {
	final J_JsonArrayNodeBuilder arrayBuilder;
	final J_JsonListenerToJdomAdapter adapter;

	J_ArrayNodeContainer(J_JsonListenerToJdomAdapter adapter, J_JsonArrayNodeBuilder arrayBuilder) {
		this.adapter = adapter;
		this.arrayBuilder = arrayBuilder;
	}

	public void visitNode(J_JsonNodeBuilder node) {
		this.arrayBuilder.add(node);
	}

	public void visitField(J_JsonFieldBuilder field) {
		throw new RuntimeException("Coding failure in Argo:  Attempt to add a field to an array.");
	}
}
