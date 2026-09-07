package net.minecraft.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public final class GameWindowListener extends WindowAdapter {
	final Minecraft mc;
	final Thread mcThread;

	public GameWindowListener(Minecraft mc, Thread mcThread) {
		this.mc = mc;
		this.mcThread = mcThread;
	}

	public void windowClosing(WindowEvent e) {
		this.mc.shutdown();
		try {
			this.mcThread.join();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}
}
