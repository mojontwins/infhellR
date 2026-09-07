package net.minecraft.client.json;

/**
 * A container of nodes being built. Arrays and objects both contain nodes,
 * while fields contain a name node and a value node.
 */
interface J_NodeContainer {
	void visitNode(J_JsonNodeBuilder node);

	void visitField(J_JsonFieldBuilder field);
}
