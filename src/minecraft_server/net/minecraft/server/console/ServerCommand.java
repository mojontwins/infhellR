package net.minecraft.server.console;

import net.minecraft.server.ICommandListener;

public class ServerCommand {
	public final String command;
	public final ICommandListener commandListener;

	public ServerCommand(String string1, ICommandListener iCommandListener2) {
		this.command = string1;
		this.commandListener = iCommandListener2;
	}
}
