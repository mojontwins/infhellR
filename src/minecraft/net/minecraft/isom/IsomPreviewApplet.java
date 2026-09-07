package net.minecraft.isom;

import java.applet.Applet;
import java.awt.BorderLayout;

import net.minecraft.client.Minecraft;

public class IsomPreviewApplet extends Applet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6215140273671254963L;
	private CanvasIsomPreview a;

	public IsomPreviewApplet(Minecraft mc) {
		this.setLayout(new BorderLayout());
		this.a = new CanvasIsomPreview(mc);
		this.add(this.a, "Center");
	}

	public void start() {
		this.a.func_1272_b();
	}

	public void stop() {
		this.a.exit();
	}
}
