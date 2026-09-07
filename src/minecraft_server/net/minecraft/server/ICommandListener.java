package net.minecraft.server;

/** Listener interface for server commands.
 * Receives log messages and provides the command sender's username. */
public interface ICommandListener {

	/** Records a log message from the command sender. */
	void log(String message);

	/** Returns the username of the command sender. */
	String getUsername();
}