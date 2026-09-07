package net.minecraft.client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5String {
	private String prefix;

	public MD5String(String prefix) {
		this.prefix = prefix;
	}

	public String getMD5String(String input) {
		try {
			String combined = this.prefix + input;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(combined.getBytes(), 0, combined.length());
			return (new BigInteger(1, md.digest())).toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
