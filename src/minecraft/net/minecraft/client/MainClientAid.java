package net.minecraft.client;


public class MainClientAid {
	public static Minecraft minecraft;
	
	public static void setMinecraft(Minecraft mc) {
		minecraft = mc;
	}

	public static Minecraft getMinecraft() {
		return minecraft;
	}
}