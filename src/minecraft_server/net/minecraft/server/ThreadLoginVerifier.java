package net.minecraft.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import net.minecraft.network.packet.Packet1Login;

/**
 * Background thread that verifies a connecting player's session against
 * minecraft.net's session server. This is the legacy server-side component
 * of Mojang's session authentication: when a player connects, their client
 * proves to the session server that it owns the account, and the server
 * then asks the session server whether the player is who they say they are.
 */
class ThreadLoginVerifier extends Thread {

	/** The login packet submitted by the connecting player. */
	final Packet1Login loginPacket;

	/** The login handler that will receive the verified login result. */
	final NetLoginHandler loginHandler;

	/**
	 * Creates the verification thread.
	 *
	 * @param netLoginHandler1  the login handler that initiated verification
	 * @param packet1Login2     the login packet containing username
	 */
	ThreadLoginVerifier(NetLoginHandler netLoginHandler1, Packet1Login packet1Login2) {
		this.loginHandler = netLoginHandler1;
		this.loginPacket = packet1Login2;
	}

	/**
	 * Queries the Mojang session server to verify the player's session.
	 * If the response is "YES", the login packet is forwarded to the login
	 * handler to complete authentication. Otherwise, the player is kicked.
	 */
	public void run() {
		try {
			String serverId = NetLoginHandler.getServerId(this.loginHandler);
			URL url = new URL("http://www.minecraft.net/game/checkserver.jsp?user="
					+ URLEncoder.encode(this.loginPacket.username, "UTF-8")
					+ "&serverId="
					+ URLEncoder.encode(serverId, "UTF-8"));

			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String response = reader.readLine();
			reader.close();
			if (response.equals("YES")) {
				NetLoginHandler.setLoginPacket(this.loginHandler, this.loginPacket);
			} else {
				this.loginHandler.kickUser("Failed to verify username!");
			}
		} catch (Exception e) {
			this.loginHandler.kickUser("Failed to verify username! [internal error " + e + "]");
			e.printStackTrace();
		}
	}
}