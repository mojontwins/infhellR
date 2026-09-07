package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

public class PanelCrashReport extends Panel {
	private static final long serialVersionUID = 1L;

	public PanelCrashReport(UnexpectedThrowable unexpectedThrowable) {
		this.setBackground(new Color(3028036));
		this.setLayout(new BorderLayout());
		StringWriter stringWriter = new StringWriter();
		unexpectedThrowable.exception.printStackTrace(new PrintWriter(stringWriter));
		String exceptionStackTrace = stringWriter.toString();
		String systemInfo = "";
		String glVendor = "";

		try {
			systemInfo = systemInfo + "Generated " + (new SimpleDateFormat()).format(new Date()) + "\n";
			systemInfo = systemInfo + "\n";
			systemInfo = systemInfo + "Minecraft: Minecraft Beta 1.7.3\n";
			systemInfo = systemInfo + "OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + "\n";
			systemInfo = systemInfo + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "\n";
			systemInfo = systemInfo + "VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + "\n";
			systemInfo = systemInfo + "LWJGL: " + Sys.getVersion() + "\n";
			glVendor = GL11.glGetString(GL11.GL_VENDOR);
			systemInfo = systemInfo + "OpenGL: " + GL11.glGetString(GL11.GL_RENDERER) + " version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR) + "\n";
		} catch (Throwable t) {
			systemInfo = systemInfo + "[failed to get system properties (" + t + ")]\n";
		}

		systemInfo = systemInfo + "\n";
		systemInfo = systemInfo + exceptionStackTrace;
		String errorReport = "";
		errorReport = errorReport + "\n";
		errorReport = errorReport + "\n";
		if(exceptionStackTrace.contains("Pixel format not accelerated")) {
			errorReport = errorReport + "      Bad video card drivers!      \n";
			errorReport = errorReport + "      -----------------------      \n";
			errorReport = errorReport + "\n";
			errorReport = errorReport + "Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n";
			errorReport = errorReport + "This can usually be fixed by updating the video card drivers.\n";
			if(glVendor.toLowerCase().contains("nvidia")) {
				errorReport = errorReport + "\n";
				errorReport = errorReport + "You might be able to find drivers for your video card here:\n";
				errorReport = errorReport + "  http://www.nvidia.com/\n";
			} else if(glVendor.toLowerCase().contains("ati")) {
				errorReport = errorReport + "\n";
				errorReport = errorReport + "You might be able to find drivers for your video card here:\n";
				errorReport = errorReport + "  http://www.amd.com/\n";
			}
		} else {
			errorReport = errorReport + "      Minecraft has crashed!      \n";
			errorReport = errorReport + "      ----------------------      \n";
			errorReport = errorReport + "\n";
			errorReport = errorReport + "Minecraft has stopped running because it encountered a problem.\n";
			errorReport = errorReport + "\n";
			errorReport = errorReport + "If you wish to report this, please copy this entire text and email it to support@mojang.com.\n";
			errorReport = errorReport + "Please include a description of what you did when the error occured.\n";
		}

		errorReport = errorReport + "\n";
		errorReport = errorReport + "\n";
		errorReport = errorReport + "\n";
		errorReport = errorReport + "--- BEGIN ERROR REPORT " + Integer.toHexString(errorReport.hashCode()) + " --------\n";
		errorReport = errorReport + systemInfo;
		errorReport = errorReport + "--- END ERROR REPORT " + Integer.toHexString(errorReport.hashCode()) + " ----------\n";
		errorReport = errorReport + "\n";
		errorReport = errorReport + "\n";
		TextArea textArea = new TextArea(errorReport, 0, 0, 1);
		textArea.setFont(new Font("Monospaced", 0, 12));
		this.add(new CanvasMojangLogo(), "North");
		this.add(new CanvasCrashReport(80), "East");
		this.add(new CanvasCrashReport(80), "West");
		this.add(new CanvasCrashReport(100), "South");
		this.add(textArea, "Center");
	}
}
