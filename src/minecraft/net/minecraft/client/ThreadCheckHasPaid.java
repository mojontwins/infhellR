package net.minecraft.client;

import java.net.HttpURLConnection;
import java.net.URL;


public class ThreadCheckHasPaid extends Thread {
	final Minecraft mc;

	public ThreadCheckHasPaid(Minecraft mc) {
		this.mc = mc;
	}

	public void run() {
		try {
			HttpURLConnection conn = (HttpURLConnection) (new URL("https://login.minecraft.net/session?name=" + this.mc.session.username + "&session=" + this.mc.session.sessionId)).openConnection();
			conn.connect();
			if (conn.getResponseCode() == 400) {
				Minecraft.hasPaidCheckTime = System.currentTimeMillis();
			}
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
