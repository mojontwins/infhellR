package net.minecraft.client;

public class UnexpectedThrowable {
	public final String description;
	public final Throwable exception;

	public UnexpectedThrowable(String par1Description, Throwable par2Throwable) {
		this.description = par1Description;
		this.exception = par2Throwable;
	}
}
