package net.minecraft.game.world.chunk;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkFolderPattern implements FileFilter {
	public static final Pattern folderRegexp = Pattern.compile("[0-9a-z]|([0-9a-z][0-9a-z])");

	public ChunkFolderPattern() {
	}

	public boolean accept(File file1) {
		if(file1.isDirectory()) {
			Matcher matcher2 = folderRegexp.matcher(file1.getName());
			return matcher2.matches();
		} else {
			return false;
		}
	}

}
