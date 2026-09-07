package net.minecraft.client.json;

/**
 * Builds a single field (name + value) inside a JSON object.
 * Used by {@link J_JsonObjectNodeBuilder} to assemble its key/value map.
 */
final class J_JsonFieldBuilder {
	private J_JsonNodeBuilder nameBuilder;
	private J_JsonNodeBuilder valueBuilder;

	static J_JsonFieldBuilder create() {
		return new J_JsonFieldBuilder();
	}

	J_JsonFieldBuilder withName(J_JsonNodeBuilder nameBuilder) {
		this.nameBuilder = nameBuilder;
		return this;
	}

	J_JsonFieldBuilder withValue(J_JsonNodeBuilder valueBuilder) {
		this.valueBuilder = valueBuilder;
		return this;
	}

	J_JsonStringNode getName() {
		return (J_JsonStringNode) this.nameBuilder.build();
	}

	J_JsonNode getValue() {
		return this.valueBuilder.build();
	}
}
