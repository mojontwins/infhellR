package net.minecraft.game.world.chunk.loader;

public class SaveFormatComparator implements Comparable<Object> {
	private final String fileName;
	private final String displayName;
	private final long lastTimePlayed;
	private final long sizeOnDisk;
	private final boolean requiresConversion;

	public SaveFormatComparator(String string1, String string2, long j3, long j5, boolean z7) {
		this.fileName = string1;
		this.displayName = string2;
		this.lastTimePlayed = j3;
		this.sizeOnDisk = j5;
		this.requiresConversion = z7;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public long getSizeOnDisk() {
		return this.sizeOnDisk;
	}

	public boolean getRequiresConversion() {
		return this.requiresConversion;
	}

	public long getLastTimePlayed() {
		return this.lastTimePlayed;
	}

	public int compareTo(SaveFormatComparator saveFormatComparator1) {
		return this.lastTimePlayed < saveFormatComparator1.lastTimePlayed ? 1 : (this.lastTimePlayed > saveFormatComparator1.lastTimePlayed ? -1 : this.fileName.compareTo(saveFormatComparator1.fileName));
	}

	public int compareTo(Object object1) {
		return this.compareTo((SaveFormatComparator)object1);
	}
}
