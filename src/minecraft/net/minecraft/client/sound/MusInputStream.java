package net.minecraft.client.sound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class MusInputStream extends InputStream {
	private int decryptionKey;
	private InputStream wrappedStream;
	byte[] buffer;
	final CodecMus codec;

	public MusInputStream(CodecMus parentCodec, URL url, InputStream wrappedStream) {
		this.codec = parentCodec;
		this.buffer = new byte[1];
		this.wrappedStream = wrappedStream;
		String path = url.getPath();
		path = path.substring(path.lastIndexOf("/") + 1);
		this.decryptionKey = path.hashCode();
	}

	public int read() throws IOException {
		int bytesRead = this.read(this.buffer, 0, 1);
		return bytesRead < 0 ? bytesRead : this.buffer[0];
	}

	public int read(byte[] buffer, int offset, int maxBytes) throws IOException {
		maxBytes = this.wrappedStream.read(buffer, offset, maxBytes);

		for(int i = 0; i < maxBytes; ++i) {
			byte decryptedByte = buffer[offset + i] = (byte)(buffer[offset + i] ^ this.decryptionKey >> 8);
			this.decryptionKey = this.decryptionKey * 498729871 + 85731 * decryptedByte;
		}

		return maxBytes;
	}
}
