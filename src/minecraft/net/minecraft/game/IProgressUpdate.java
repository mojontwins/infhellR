package net.minecraft.game;

public interface IProgressUpdate {
	void displaySavingString(String string1);

	void displayLoadingString(String string1);

	void setLoadingProgress(int i1);
}
