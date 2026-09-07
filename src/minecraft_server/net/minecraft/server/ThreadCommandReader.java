package net.minecraft.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Background thread that reads commands from the server's standard input.
 * Runs continuously until the server stops, forwarding each line as a command
 * to the MinecraftServer instance.
 */
public class ThreadCommandReader extends Thread {

	/** Reference to the owning MinecraftServer. */
	final MinecraftServer mcServer;

	/**
	 * Creates the thread and binds it to the given server instance.
	 *
	 * @param minecraftServer1  the MinecraftServer that will process commands
	 */
	public ThreadCommandReader(MinecraftServer minecraftServer1) {
		this.mcServer = minecraftServer1;
	}

	/**
	 * Reads lines from stdin and submits them as commands.
	 * The loop exits when the server has stopped or stdin is closed.
	 */
	public void run() {
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		try {
			while (!this.mcServer.serverStopped && MinecraftServer.isServerRunning(this.mcServer) && (line = reader1.readLine()) != null) {
				this.mcServer.addCommand(line, this.mcServer);
			}
		} catch (IOException exception4) {
			exception4.printStackTrace();
		}
	}
}