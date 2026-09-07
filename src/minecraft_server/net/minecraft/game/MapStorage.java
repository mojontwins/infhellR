package net.minecraft.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.game.world.chunk.CompressedStreamTools;
import net.minecraft.game.world.chunk.loader.ISaveHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;

public class MapStorage {
	private ISaveHandler saveHandler;
	private Map<String,MapDataBase> loadedDataMap = new HashMap<String,MapDataBase>();
	private List<MapDataBase> loadedDataList = new ArrayList<MapDataBase>();
	private Map<String,Short> idCounts = new HashMap<String,Short>();

	public MapStorage(ISaveHandler iSaveHandler1) {
		this.saveHandler = iSaveHandler1;
		this.loadIdCounts();
	}

	public MapDataBase loadData(Class<?> class1, String string2) {
		MapDataBase mapDataBase3 = (MapDataBase)this.loadedDataMap.get(string2);
		if(mapDataBase3 != null) {
			return mapDataBase3;
		} else {
			if(this.saveHandler != null) {
				try {
					File file4 = this.saveHandler.getMapFileFromName(string2);
					if(file4 != null && file4.exists()) {
						try {
							mapDataBase3 = (MapDataBase)class1.getConstructor(new Class[]{String.class}).newInstance(new Object[]{string2});
						} catch (Exception exception7) {
							throw new RuntimeException("Failed to instantiate " + class1.toString(), exception7);
						}

						FileInputStream fileInputStream5 = new FileInputStream(file4);
						NBTTagCompound nBTTagCompound6 = CompressedStreamTools.readCompressed(fileInputStream5);
						fileInputStream5.close();
						mapDataBase3.readFromNBT(nBTTagCompound6.getCompoundTag("data"));
					}
				} catch (Exception exception8) {
					exception8.printStackTrace();
				}
			}

			if(mapDataBase3 != null) {
				this.loadedDataMap.put(string2, mapDataBase3);
				this.loadedDataList.add(mapDataBase3);
			}

			return mapDataBase3;
		}
	}

	public void setData(String string1, MapDataBase mapDataBase2) {
		if(mapDataBase2 == null) {
			throw new RuntimeException("Can\'t set null data");
		} else {
			if(this.loadedDataMap.containsKey(string1)) {
				this.loadedDataList.remove(this.loadedDataMap.remove(string1));
			}

			this.loadedDataMap.put(string1, mapDataBase2);
			this.loadedDataList.add(mapDataBase2);
		}
	}

	public void saveAllData() {
		for(int i1 = 0; i1 < this.loadedDataList.size(); ++i1) {
			MapDataBase mapDataBase2 = (MapDataBase)this.loadedDataList.get(i1);
			if(mapDataBase2.isDirty()) {
				this.saveData(mapDataBase2);
				mapDataBase2.setDirty(false);
			}
		}

	}

	private void saveData(MapDataBase mapDataBase1) {
		if(this.saveHandler != null) {
			try {
				File file2 = this.saveHandler.getMapFileFromName(mapDataBase1.name);
				if(file2 != null) {
					NBTTagCompound nBTTagCompound3 = new NBTTagCompound();
					mapDataBase1.writeToNBT(nBTTagCompound3);
					NBTTagCompound nBTTagCompound4 = new NBTTagCompound();
					nBTTagCompound4.setCompoundTag("data", nBTTagCompound3);
					FileOutputStream fileOutputStream5 = new FileOutputStream(file2);
					CompressedStreamTools.writeCompressed(nBTTagCompound4, fileOutputStream5);
					fileOutputStream5.close();
				}
			} catch (Exception exception6) {
				exception6.printStackTrace();
			}

		}
	}

	private void loadIdCounts() {
		try {
			this.idCounts.clear();
			if(this.saveHandler == null) {
				return;
			}

			File file1 = this.saveHandler.getMapFileFromName("idcounts");
			if(file1 != null && file1.exists()) {
				DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file1));
				NBTTagCompound nBTTagCompound3 = CompressedStreamTools.readNBT(dataInputStream2);
				dataInputStream2.close();
				Iterator<NBTBase> iterator4 = nBTTagCompound3.func_28110_c().iterator();

				while(iterator4.hasNext()) {
					NBTBase nBTBase5 = (NBTBase)iterator4.next();
					if(nBTBase5 instanceof NBTTagShort) {
						NBTTagShort nBTTagShort6 = (NBTTagShort)nBTBase5;
						String string7 = nBTTagShort6.getKey();
						short s8 = nBTTagShort6.shortValue;
						this.idCounts.put(string7, s8);
					}
				}
			}
		} catch (Exception exception9) {
			exception9.printStackTrace();
		}

	}

	public int getUniqueDataId(String string1) {
		Short short2 = (Short)this.idCounts.get(string1);
		if(short2 == null) {
			short2 = (short)0;
		} else {
			short2 = (short)(short2.shortValue() + 1);
		}

		this.idCounts.put(string1, short2);
		if(this.saveHandler == null) {
			return short2.shortValue();
		} else {
			try {
				File file3 = this.saveHandler.getMapFileFromName("idcounts");
				if(file3 != null) {
					NBTTagCompound nBTTagCompound4 = new NBTTagCompound();
					Iterator<String> iterator5 = this.idCounts.keySet().iterator();

					while(iterator5.hasNext()) {
						String string6 = (String)iterator5.next();
						short s7 = ((Short)this.idCounts.get(string6)).shortValue();
						nBTTagCompound4.setShort(string6, s7);
					}

					DataOutputStream dataOutputStream9 = new DataOutputStream(new FileOutputStream(file3));
					CompressedStreamTools.writeNBT(nBTTagCompound4, dataOutputStream9);
					dataOutputStream9.close();
				}
			} catch (Exception exception8) {
				exception8.printStackTrace();
			}

			return short2.shortValue();
		}
	}
}
