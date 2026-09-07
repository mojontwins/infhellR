package net.minecraft.client.json;

/**
 * Thrown when the JSON parser encounters invalid syntax.
 * Reports the line and column of the error position.
 */
public final class J_InvalidSyntaxException extends Exception {
	private static final long serialVersionUID = -4322294217994740516L;
	private final int column;
	private final int line;

	J_InvalidSyntaxException(String message, J_ThingWithPosition pos) {
		super("At line " + pos.getLine() + ", column " + pos.getColumn() + ":  " + message);
		this.column = pos.getColumn();
		this.line = pos.getLine();
	}

	J_InvalidSyntaxException(String message, Throwable cause, J_ThingWithPosition pos) {
		super("At line " + pos.getLine() + ", column " + pos.getColumn() + ":  " + message, cause);
		this.column = pos.getColumn();
		this.line = pos.getLine();
	}

	public int getColumn() {
		return this.column;
	}

	public int getLine() {
		return this.line;
	}
}
