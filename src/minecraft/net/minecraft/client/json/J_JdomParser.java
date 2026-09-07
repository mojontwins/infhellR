package net.minecraft.client.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * DOM-style JSON parser. Parses JSON text into a tree of {@link J_JsonNode} objects.
 */
public final class J_JdomParser {
	public J_JsonRootNode parseFromReader(Reader reader) throws IOException, J_InvalidSyntaxException {
		J_JsonListenerToJdomAdapter adapter = new J_JsonListenerToJdomAdapter();
		(new J_SajParser()).parse(reader, adapter);
		return adapter.getResult();
	}

	public J_JsonRootNode parse(String text) throws J_InvalidSyntaxException {
		try {
			return this.parseFromReader(new StringReader(text));
		} catch (IOException e) {
			throw new RuntimeException("Coding failure in Argo: StringReader threw IOException", e);
		}
	}

	public J_JsonRootNode parseFromInputStream(InputStream input) throws J_InvalidSyntaxException {
		Reader reader = null;
		try {
			reader = new InputStreamReader(input);
			return this.parseFromReader(reader);
		} catch (IOException e) {
			throw new RuntimeException("Coding failure in Argo: InputStreamReader threw IOException", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
