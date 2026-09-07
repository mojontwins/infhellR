package net.minecraft.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ThreadDownloadResources extends Thread {
	public File resourcesFolder;
	private Minecraft mc;
	private boolean closing = false;

	public ThreadDownloadResources(File baseDir, Minecraft mc) {
		this.mc = mc;
		this.setName("Resource download thread");
		this.setDaemon(true);
		this.resourcesFolder = new File(baseDir, "resources/");
		if (!this.resourcesFolder.exists() && !this.resourcesFolder.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + this.resourcesFolder);
		}
	}

	public void run() {
		try {
			URL baseUrl = new URL("http://s3.amazonaws.com/MinecraftResources/");
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(baseUrl.openStream());
			NodeList entries = doc.getElementsByTagName("Contents");

			for (int pass = 0; pass < 2; ++pass) {
				for (int i = 0; i < entries.getLength(); ++i) {
					Node node = entries.item(i);
					if (node.getNodeType() == 1) {
						Element entry = (Element) node;
						String key = ((Element) entry.getElementsByTagName("Key").item(0)).getChildNodes().item(0).getNodeValue();
						long size = Long.parseLong(((Element) entry.getElementsByTagName("Size").item(0)).getChildNodes().item(0).getNodeValue());
						if (size > 0L) {
							this.downloadAndInstallResource(baseUrl, key, size, pass);
							if (this.closing) return;
						}
					}
				}
			}
		} catch (Exception e) {
			this.loadResource(this.resourcesFolder, "");
			e.printStackTrace();
		}
	}

	public void reloadResources() {
		this.loadResource(this.resourcesFolder, "");
	}

	private void loadResource(File dir, String prefix) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; ++i) {
			if (files[i].isDirectory()) {
				this.loadResource(files[i], prefix + files[i].getName() + "/");
			} else {
				try {
					this.mc.installResource(prefix + files[i].getName(), files[i]);
				} catch (Exception e) {
					System.out.println("Failed to add " + prefix + files[i].getName());
				}
			}
		}
	}

	private void downloadAndInstallResource(URL baseUrl, String key, long size, int category) {
		try {
			int slash = key.indexOf("/");
			String categoryStr = key.substring(0, slash);
			if (!categoryStr.equals("sound") && !categoryStr.equals("newsound")) {
				if (category != 1) return;
			} else if (category != 0) return;

			File target = new File(this.resourcesFolder, key);
			if (!target.exists() || target.length() != size) {
				target.getParentFile().mkdirs();
				String urlPath = key.replaceAll(" ", "%20");
				this.downloadResource(new URL(baseUrl, urlPath), target, size);
				if (this.closing) return;
			}
			this.mc.installResource(key, target);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void downloadResource(URL url, File target, long size) throws IOException {
		byte[] buf = new byte[4096];
		DataInputStream in = new DataInputStream(url.openStream());
		DataOutputStream out = new DataOutputStream(new FileOutputStream(target));
		try {
			int read;
			while ((read = in.read(buf)) >= 0) {
				if (this.closing) break;
				out.write(buf, 0, read);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	public void closeMinecraft() {
		this.closing = true;
	}
}
