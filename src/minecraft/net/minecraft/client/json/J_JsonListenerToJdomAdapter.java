package net.minecraft.client.json;

/**
 * Converts SAX-style parse events from {@link J_SajParser} into a tree of JSON nodes.
 * Implements {@link J_JsonListener} and assembles the node hierarchy on a stack.
 */
final class J_JsonListenerToJdomAdapter implements J_JsonListener {
	private final java.util.Stack<J_NodeContainer> containers = new java.util.Stack<J_NodeContainer>();
	private J_JsonNodeBuilder rootBuilder;

	J_JsonRootNode getResult() {
		return (J_JsonRootNode) this.rootBuilder.build();
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startArray() {
		J_JsonArrayNodeBuilder builder = J_JsonNodeBuilders.arrayNode();
		addChild(builder);
		this.containers.push(new J_ArrayNodeContainer(this, builder));
	}

	public void endArray() {
		this.containers.pop();
	}

	public void startObject() {
		J_JsonObjectNodeBuilder builder = J_JsonNodeBuilders.objectNode();
		addChild(builder);
		this.containers.push(new J_ObjectNodeContainer(this, builder));
	}

	public void endObject() {
		this.containers.pop();
	}

	public void visitFieldName(String name) {
		J_JsonFieldBuilder field = J_JsonFieldBuilder.create()
			.withName(J_JsonNodeBuilders.stringNode(name));
		((J_NodeContainer) this.containers.peek()).visitField(field);
		this.containers.push(new J_FieldNodeContainer(this, field));
	}

	public void endField() {
		this.containers.pop();
	}

	public void visitNumericValue(String value) {
		addChild(J_JsonNodeBuilders.numberNode(value));
	}

	public void visitFalse() {
		addChild(J_JsonNodeBuilders.falseNode());
	}

	public void visitStringValue(String value) {
		addChild(J_JsonNodeBuilders.stringNode(value));
	}

	public void visitTrue() {
		addChild(J_JsonNodeBuilders.trueNode());
	}

	public void visitNull() {
		addChild(J_JsonNodeBuilders.nullNode());
	}

	private void addChild(J_JsonNodeBuilder node) {
		if (this.rootBuilder == null) {
			this.rootBuilder = node;
		} else {
			((J_NodeContainer) this.containers.peek()).visitNode(node);
		}
	}
}
