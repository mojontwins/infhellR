package net.minecraft.game.world.chunk.loader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.chunk.ChunkFile;
import net.minecraft.game.world.chunk.ChunkFilePattern;
import net.minecraft.game.world.chunk.ChunkFolderPattern;

public class SaveConverterMcRegion extends SaveFormatOld {
	public SaveConverterMcRegion(File file1) {
		super(file1);
	}

	public String getFormatName() {
		return "Scaevolus\' McRegion";
	}

	public List<SaveFormatComparator> getSaveList() {
		ArrayList<SaveFormatComparator> arrayList1 = new ArrayList<SaveFormatComparator>();
		File[] file2 = this.savesDirectory.listFiles();
		File[] file3 = file2;
		int i4 = file2.length;

		for(int i5 = 0; i5 < i4; ++i5) {
			File file6 = file3[i5];
			if(file6.isDirectory()) {
				String string7 = file6.getName();
				WorldInfo worldInfo8 = this.getWorldInfo(string7);
				if(worldInfo8 != null) {
					boolean z9 = worldInfo8.getSaveVersion() != 19132;
					String string10 = worldInfo8.getWorldName();
					if(string10 == null || MathHelper.stringNullOrLengthZero(string10)) {
						string10 = string7;
					}

					arrayList1.add(new SaveFormatComparator(string7, string10, worldInfo8.getLastTimePlayed(), worldInfo8.getSizeOnDisk(), z9));
				}
			}
		}

		return arrayList1;
	}

	public void flushCache() {
		RegionFileCache.closeRegionFiles();
	}

	public ISaveHandler getSaveLoader(String string1, boolean z2) {
		return new SaveOldDir(this.savesDirectory, string1, z2);
	}

	public boolean isOldMapFormat(String string1) {
		WorldInfo worldInfo2 = this.getWorldInfo(string1);
		return worldInfo2 != null && worldInfo2.getSaveVersion() == 0;
	}

	public boolean converMapToMCRegion(String levelName, IProgressUpdate progress) {
		progress.setLoadingProgress(0);
		ArrayList<ChunkFile> chunkFilesSurface = new ArrayList<ChunkFile>();
		ArrayList<File> chunkFoldersSurface = new ArrayList<File>();
		ArrayList<ChunkFile> chunkFilesNether = new ArrayList<ChunkFile>();
		ArrayList<File> chunkFoldersNether = new ArrayList<File>();

		File baseDir = new File(this.savesDirectory, levelName);
		File netherDir = new File(baseDir, "DIM-1");

		System.out.println("Scanning folders...");

		this.scanFolder(baseDir, chunkFilesSurface, chunkFoldersSurface);
		if(netherDir.exists()) {
			this.scanFolder(netherDir, chunkFilesNether, chunkFoldersNether);
		}

		int totalConversions = chunkFilesSurface.size() + chunkFilesNether.size() + chunkFoldersSurface.size() + chunkFoldersNether.size();
		System.out.println("Total conversion count is " + totalConversions);

		this.convertToMcr(baseDir, chunkFilesSurface, 0, totalConversions, progress);
		this.convertToMcr(netherDir, chunkFilesNether, chunkFilesSurface.size(), totalConversions, progress);
		
		WorldInfo worldInfo = this.getWorldInfo(levelName);
		worldInfo.setSaveVersion(19132);
		
		ISaveHandler iSaveHandler11 = this.getSaveLoader(levelName, false);
		iSaveHandler11.saveWorldInfo(worldInfo);
		this.deleteOldFolders(chunkFoldersSurface, chunkFilesSurface.size() + chunkFilesNether.size(), totalConversions, progress);
		if(netherDir.exists()) {
			this.deleteOldFolders(chunkFoldersNether, chunkFilesSurface.size() + chunkFilesNether.size() + chunkFoldersSurface.size(), totalConversions, progress);
		}

		return true;
	}

	private void scanFolder(File dir, ArrayList<ChunkFile> listChunks, ArrayList<File> listFolders) {
		ChunkFolderPattern folderPattern = new ChunkFolderPattern();
		ChunkFilePattern filePattern = new ChunkFilePattern();
		
		File[] folders = dir.listFiles(folderPattern);
		File[] foldersC = folders;
		int numFolders = folders.length;

		for(int i = 0; i < numFolders; ++i) {
			File folder = foldersC[i];
			listFolders.add(folder);
			File[] subfolders = folder.listFiles(folderPattern);
			File[] subfoldersC = subfolders;
			int i13 = subfolders.length;

			for(int j = 0; j < i13; ++j) {
				File subfolder = subfoldersC[j];
				File[] files = subfolder.listFiles(filePattern);
				File[] filesC = files;
				int numFiles = files.length;

				for(int k = 0; k < numFiles; ++k) {
					File file = filesC[k];
					listChunks.add(new ChunkFile(file));
				}
			}
		}

	}

	private void convertToMcr(File dir, ArrayList<ChunkFile> chunkFiles, int curConversion, int totalConversion, IProgressUpdate progress) {
		Collections.sort(chunkFiles);
		byte[] buffer = new byte[4096];
		Iterator<ChunkFile> it = chunkFiles.iterator();

		while(it.hasNext()) {
			ChunkFile chunkFile = (ChunkFile)it.next();
			int chunkX = chunkFile.getXpos();
			int chunkZ = chunkFile.getZpos();

			RegionFile mcr = RegionFileCache.getRegionFile(dir, chunkX, chunkZ);

			if(!mcr.isChunkSaved(chunkX & 31, chunkZ & 31)) {
				try {
					DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(chunkFile.getFile())));
					DataOutputStream dos = mcr.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
					
					int size;
					while((size = dis.read(buffer)) != -1) {
						dos.write(buffer, 0, size);
					}

					dos.close();
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			++curConversion;
			int percent = (int)Math.round(100.0D * (double)curConversion / (double)totalConversion);
			progress.setLoadingProgress(percent);
		}

		RegionFileCache.closeRegionFiles();
	}

	private void deleteOldFolders(ArrayList<File> arrayList1, int i2, int i3, IProgressUpdate iProgressUpdate4) {
		Iterator<File> iterator5 = arrayList1.iterator();

		while(iterator5.hasNext()) {
			File file6 = (File)iterator5.next();
			File[] file7 = file6.listFiles();
			deleteRecursively(file7);
			file6.delete();
			++i2;
			int i8 = (int)Math.round(100.0D * (double)i2 / (double)i3);
			iProgressUpdate4.setLoadingProgress(i8);
		}

	}
}
