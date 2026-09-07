package net.minecraft.client.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A {@link J_JsonFormatter} that produces compact (non-indented) JSON output.
 */
public final class J_CompactJsonFormatter implements J_JsonFormatter {
	public String format(J_JsonRootNode root) {
		StringBuilder sb = new StringBuilder();
		formatNode(root, sb);
		return sb.toString();
	}

	public void formatTo(J_JsonRootNode root, Writer writer) throws IOException {
		formatNode(root, writer);
	}

	private void formatNode(J_JsonNode node, Appendable out) throws IOException {
		switch (node.getType()) {
			case ARRAY:
				out.append('[');
				Iterator<J_JsonNode> it = node.getElements().iterator();
				boolean first = true;
				while (it.hasNext()) {
					if (!first) out.append(',');
					first = false;
					formatNode(it.next(), out);
				}
				out.append(']');
				break;
			case OBJECT:
				out.append('{');
				Iterator<J_JsonStringNode> keys = new TreeSet<J_JsonStringNode>(node.getFields().keySet()).iterator();
				first = true;
				while (keys.hasNext()) {
					if (!first) out.append(',');
					first = false;
					J_JsonStringNode key = keys.next();
					out.append('"').append(new J_JsonEscapedString(key.getText()).toString()).append('"');
					out.append(':');
					formatNode(node.getFields().get(key), out);
				}
				out.append('}');
				break;
			case STRING:
				out.append('"').append(new J_JsonEscapedString(node.getText()).toString()).append('"');
				break;
			case NUMBER:
				out.append(node.getText());
				break;
			case FALSE:
				out.append("false");
				break;
			case TRUE:
				out.append("true");
				break;
			case NULL:
				out.append("null");
				break;
			default:
				throw new RuntimeException("Coding failure in Argo: Unknown node type [" + node.getType() + "].");
		}
	}

	private void formatNode(J_JsonNode node, StringBuilder sb) {
		try {
			formatNode(node, (Appendable) sb);
		} catch (IOException e) {
			throw new RuntimeException("Coding failure in Argo: StringBuilder threw IOException", e);
		}
	}
}
