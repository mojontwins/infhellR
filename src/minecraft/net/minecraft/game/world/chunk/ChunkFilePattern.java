package net.minecraft.game.world.chunk;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkFilePattern implements FilenameFilter {
	public static final Pattern filenameRegexp = Pattern.compile("c\\.(-?[0-9a-z]+)\\.(-?[0-9a-z]+)\\.dat");

	public ChunkFilePattern() {
	}

	public boolean accept(File file1, String string2) {
		Matcher matcher3 = filenameRegexp.matcher(string2);
		return matcher3.matches();
	}

}
