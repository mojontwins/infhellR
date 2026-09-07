package net.minecraft.client;

import java.net.ConnectException;
import java.net.UnknownHostException;

import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.network.packet.Packet2Handshake;

public class ThreadConnectToServer extends Thread {
	final Minecraft mc;
	final String hostName;
	final int port;
	final GuiConnecting connectingGui;

	public ThreadConnectToServer(GuiConnecting connectingGui, Minecraft mc, String hostName, int port) {
		this.connectingGui = connectingGui;
		this.mc = mc;
		this.hostName = hostName;
		this.port = port;
	}

	public void run() {
		try {
			GuiConnecting.setNetClientHandler(this.connectingGui, new NetClientHandler(this.mc, this.hostName, this.port));
			if (GuiConnecting.isCancelled(this.connectingGui)) {
				return;
			}
			GuiConnecting.getNetClientHandler(this.connectingGui).addToSendQueue(new Packet2Handshake(this.mc.session.username));
		} catch (UnknownHostException e) {
			if (GuiConnecting.isCancelled(this.connectingGui)) {
				return;
			}
			this.mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]{"Unknown host '" + this.hostName + "'"}));
		} catch (ConnectException e) {
			if (GuiConnecting.isCancelled(this.connectingGui)) {
				return;
			}
			this.mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]{e.getMessage()}));
		} catch (Exception e) {
			if (GuiConnecting.isCancelled(this.connectingGui)) {
				return;
			}
			e.printStackTrace();
			this.mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]{e.toString()}));
		}
	}
}

