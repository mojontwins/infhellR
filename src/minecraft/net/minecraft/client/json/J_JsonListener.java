package net.minecraft.client.json;

/**
 * SAX-style listener interface for JSON parsing events.
 * Events fire in document order from {@link J_SajParser}.
 */
public interface J_JsonListener {
	void startDocument();

	void endDocument();

	void startArray();

	void endArray();

	void startObject();

	void endObject();

	void visitFieldName(String name);

	void endField();

	void visitStringValue(String value);

	void visitNumericValue(String value);

	void visitFalse();

	void visitTrue();

	void visitNull();
}
