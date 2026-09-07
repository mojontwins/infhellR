package net.minecraft.client.json;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A {@link PushbackReader} that tracks column/line position for error reporting.
 */
final class J_PositionTrackingPushbackReader implements J_ThingWithPosition {
	private final PushbackReader reader;
	private int column = 0;
	private int line = 1;
	private boolean sawCarriageReturn = false;

	public J_PositionTrackingPushbackReader(Reader reader) {
		this.reader = new PushbackReader(reader);
	}

	public void unreadChar(char ch) throws IOException {
		--this.column;
		if (this.column < 0) {
			this.column = 0;
		}
		this.reader.unread(ch);
	}

	public void unreadChars(char[] chars) {
		this.column -= chars.length;
		if (this.column < 0) {
			this.column = 0;
		}
	}

	public int readChar() throws IOException {
		int ch = this.reader.read();
		this.trackPosition(ch);
		return ch;
	}

	public int readChars(char[] buf) throws IOException {
		int nread = this.reader.read(buf);
		for (int i = 0; i < buf.length; i++) {
			this.trackPosition(buf[i]);
		}
		return nread;
	}

	private void trackPosition(int ch) {
		if (ch == 13) {
			this.column = 0;
			++this.line;
			this.sawCarriageReturn = true;
		} else if (ch == 10 && !this.sawCarriageReturn) {
			this.column = 0;
			++this.line;
		} else {
			++this.column;
		}
		this.sawCarriageReturn = false;
	}

	public int getColumn() {
		return this.column;
	}

	public int getLine() {
		return this.line;
	}
}
