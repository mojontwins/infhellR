package net.minecraft.client;

import java.awt.Canvas;
import java.awt.Dimension;

class CanvasCrashReport extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1045303830132238555L;

	public CanvasCrashReport(int size) {
		this.setPreferredSize(new Dimension(size, size));
		this.setMinimumSize(new Dimension(size, size));
	}
}
