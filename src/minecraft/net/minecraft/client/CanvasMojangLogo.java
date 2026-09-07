package net.minecraft.client;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import javax.imageio.ImageIO;

class CanvasMojangLogo extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4159804082218237188L;
	private BufferedImage logo;

	public CanvasMojangLogo() {
		try {
			this.logo = ImageIO.read(PanelCrashReport.class.getResource("/gui/logo.png"));
		} catch (IOException ignored) {
		}
		byte size = 100;
		this.setPreferredSize(new Dimension(size, size));
		this.setMinimumSize(new Dimension(size, size));
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(this.logo, this.getWidth() / 2 - this.logo.getWidth() / 2, 32, (ImageObserver) null);
	}
}
