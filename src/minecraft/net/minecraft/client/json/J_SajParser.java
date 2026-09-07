package net.minecraft.client.json;

import java.io.IOException;
import java.io.Reader;

/**
 * SAX-style JSON parser. Reads a {@link Reader} and fires events on a {@link J_JsonListener}.
 * Tracks position for error reporting via {@link J_PositionTrackingPushbackReader}.
 */
public final class J_SajParser {
	public void parse(Reader reader, J_JsonListener listener) throws IOException, J_InvalidSyntaxException {
		J_PositionTrackingPushbackReader ptr = new J_PositionTrackingPushbackReader(reader);
		char ch = (char) ptr.readChar();

		switch (ch) {
			case '[':
				ptr.unreadChar(ch);
				listener.startDocument();
				parseArray(ptr, listener);
				break;
			case '{':
				ptr.unreadChar(ch);
				listener.startDocument();
				parseObject(ptr, listener);
				break;
			default:
				throw new J_InvalidSyntaxException("Expected [ or { but got [" + ch + "].", ptr);
		}

		int trailing = skipWhitespace(ptr);
		if (trailing != -1) {
			throw new J_InvalidSyntaxException("Got unexpected trailing character [" + (char) trailing + "].", ptr);
		}
		listener.endDocument();
	}

	private void parseArray(J_PositionTrackingPushbackReader ptr, J_JsonListener listener) throws IOException, J_InvalidSyntaxException {
		char ch = (char) skipWhitespace(ptr);
		if (ch != '[') {
			throw new J_InvalidSyntaxException("Expected [ to start array, got [" + ch + "].", ptr);
		}
		listener.startArray();

		ch = (char) skipWhitespace(ptr);
		ptr.unreadChar(ch);
		if (ch != ']') {
			parseValue(ptr, listener);
		}

		boolean done = false;
		while (!done) {
			char buf = (char) skipWhitespace(ptr);
			switch (buf) {
				case ',':
					parseValue(ptr, listener);
					break;
				case ']':
					done = true;
					break;
				default:
					throw new J_InvalidSyntaxException("Expected , or ] but got [" + buf + "].", ptr);
			}
		}
		listener.endArray();
	}

	private void parseObject(J_PositionTrackingPushbackReader ptr, J_JsonListener listener) throws IOException, J_InvalidSyntaxException {
		char ch = (char) skipWhitespace(ptr);
		if (ch != '{') {
			throw new J_InvalidSyntaxException("Expected { to start object, got [" + ch + "].", ptr);
		}
		listener.startObject();

		ch = (char) skipWhitespace(ptr);
		ptr.unreadChar(ch);
		if (ch != '}') {
			parseField(ptr, listener);
		}

		boolean done = false;
		while (!done) {
			char buf = (char) skipWhitespace(ptr);
			switch (buf) {
				case ',':
					parseField(ptr, listener);
					break;
				case '}':
					done = true;
					break;
				default:
					throw new J_InvalidSyntaxException("Expected , or } but got [" + buf + "].", ptr);
			}
		}
		listener.endObject();
	}

	private void parseField(J_PositionTrackingPushbackReader ptr, J_JsonListener listener) throws IOException, J_InvalidSyntaxException {
		char ch = (char) skipWhitespace(ptr);
		if (ch != '"') {
			throw new J_InvalidSyntaxException("Expected field name to start with \", got [" + ch + "].", ptr);
		}
		ptr.unreadChar(ch);
		listener.visitFieldName(parseString(ptr));

		ch = (char) skipWhitespace(ptr);
		if (ch != ':') {
			throw new J_InvalidSyntaxException("Expected : after field name, got [" + ch + "].", ptr);
		}
		parseValue(ptr, listener);
		listener.endField();
	}

	private void parseValue(J_PositionTrackingPushbackReader ptr, J_JsonListener listener) throws IOException, J_InvalidSyntaxException {
		char ch = (char) skipWhitespace(ptr);
		switch (ch) {
			case '"':
				ptr.unreadChar(ch);
				listener.visitStringValue(parseString(ptr));
				break;
			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				ptr.unreadChar(ch);
				listener.visitNumericValue(parseNumber(ptr));
				break;
			case '[':
				ptr.unreadChar(ch);
				parseArray(ptr, listener);
				break;
			case '{':
				ptr.unreadChar(ch);
				parseObject(ptr, listener);
				break;
			case 'f':
				char[] falseBuf = new char[4];
				int nread = ptr.readChars(falseBuf);
				if (nread == 4 && falseBuf[0] == 'a' && falseBuf[1] == 'l' && falseBuf[2] == 's' && falseBuf[3] == 'e') {
					listener.visitFalse();
					break;
				}
				ptr.unreadChars(falseBuf);
				throw new J_InvalidSyntaxException("Expected 'false', got [" + new String(falseBuf, 0, nread) + "].", ptr);
			case 'n':
				char[] nullBuf = new char[3];
				nread = ptr.readChars(nullBuf);
				if (nread == 3 && nullBuf[0] == 'u' && nullBuf[1] == 'l' && nullBuf[2] == 'l') {
					listener.visitNull();
					break;
				}
				ptr.unreadChars(nullBuf);
				throw new J_InvalidSyntaxException("Expected 'null', got [" + new String(nullBuf, 0, nread) + "].", ptr);
			case 't':
				char[] trueBuf = new char[3];
				nread = ptr.readChars(trueBuf);
				if (nread == 3 && trueBuf[0] == 'r' && trueBuf[1] == 'u' && trueBuf[2] == 'e') {
					listener.visitTrue();
					break;
				}
				ptr.unreadChars(trueBuf);
				throw new J_InvalidSyntaxException("Expected 'true', got [" + new String(trueBuf, 0, nread) + "].", ptr);
			default:
				throw new J_InvalidSyntaxException("Invalid character at start of value [" + ch + "].", ptr);
		}
	}

	private String parseNumber(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch == '-') {
			sb.append('-');
		} else {
			ptr.unreadChar(ch);
		}
		sb.append(parseDigits(ptr));
		return sb.toString();
	}

	private String parseDigits(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch == '0') {
			sb.append('0');
			sb.append(parseFraction(ptr));
			sb.append(parseExponent(ptr));
		} else {
			ptr.unreadChar(ch);
			sb.append(parseNonZeroDigit(ptr));
			sb.append(parseDigitsFraction(ptr));
			sb.append(parseFraction(ptr));
			sb.append(parseExponent(ptr));
		}
		return sb.toString();
	}

	private char parseNonZeroDigit(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		char ch = (char) ptr.readChar();
		if (ch >= '1' && ch <= '9') {
			return ch;
		}
		throw new J_InvalidSyntaxException("Expected a digit 1-9, got [" + ch + "].", ptr);
	}

	private String parseDigitsFraction(J_PositionTrackingPushbackReader ptr) throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean done = false;
		while (!done) {
			char ch = (char) ptr.readChar();
			if (ch >= '0' && ch <= '9') {
				sb.append(ch);
			} else {
				done = true;
				ptr.unreadChar(ch);
			}
		}
		return sb.toString();
	}

	private char parseFractionDigit(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		char ch = (char) ptr.readChar();
		if (ch >= '0' && ch <= '9') {
			return ch;
		}
		throw new J_InvalidSyntaxException("Expected a digit, got [" + ch + "].", ptr);
	}

	private String parseFraction(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch == '.') {
			sb.append('.');
			sb.append(parseFractionDigit(ptr));
			sb.append(parseDigitsFraction(ptr));
		} else {
			ptr.unreadChar(ch);
		}
		return sb.toString();
	}

	private String parseExponent(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch == 'e' || ch == 'E') {
			sb.append('E');
			sb.append(parseExponentSign(ptr));
			sb.append(parseFractionDigit(ptr));
			sb.append(parseDigitsFraction(ptr));
		} else {
			ptr.unreadChar(ch);
		}
		return sb.toString();
	}

	private String parseExponentSign(J_PositionTrackingPushbackReader ptr) throws IOException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch == '+' || ch == '-') {
			sb.append(ch);
		} else {
			ptr.unreadChar(ch);
		}
		return sb.toString();
	}

	private String parseString(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		char ch = (char) ptr.readChar();
		if (ch != '"') {
			throw new J_InvalidSyntaxException("Expected opening quote, got [" + ch + "].", ptr);
		}

		boolean done = false;
		while (!done) {
			char ch2 = (char) ptr.readChar();
			switch (ch2) {
				case '"':
					done = true;
					break;
				case '\\':
					sb.append(parseEscape(ptr));
					break;
				default:
					sb.append(ch2);
					break;
			}
		}
		return sb.toString();
	}

	private char parseEscape(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		char ch = (char) ptr.readChar();
		switch (ch) {
			case '"':  return '"';
			case '/':  return '/';
			case '\\': return '\\';
			case 'b':  return '\b';
			case 'f':  return '\f';
			case 'n':  return '\n';
			case 'r':  return '\r';
			case 't':  return '\t';
			case 'u':  return (char) parseHex(ptr);
			default:
				throw new J_InvalidSyntaxException("Unrecognised escape character [" + ch + "].", ptr);
		}
	}

	private int parseHex(J_PositionTrackingPushbackReader ptr) throws IOException, J_InvalidSyntaxException {
		char[] buf = new char[4];
		int nread = ptr.readChars(buf);
		if (nread != 4) {
			throw new J_InvalidSyntaxException("Expected 4 hex digits, got only " + nread + ".", ptr);
		}
		try {
			return Integer.parseInt(new String(buf), 16);
		} catch (NumberFormatException e) {
			ptr.unreadChars(buf);
			throw new J_InvalidSyntaxException("Invalid hex sequence [" + new String(buf) + "].", e, ptr);
		}
	}

	private int skipWhitespace(J_PositionTrackingPushbackReader ptr) throws IOException {
		while (true) {
			int ch = ptr.readChar();
			switch (ch) {
				case 9:  // tab
				case 10: // newline
				case 13: // carriage return
				case 32: // space
					continue;
				default:
					return ch;
			}
		}
	}
}
