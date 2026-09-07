package net.minecraft.game.world.schematic;

import java.io.DataInputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class Schematic {
	
	// Schematic file format loader
	
	private byte[] blocks;
	private byte[] data;
	
	private int width, height, length;
	
	private boolean vanilla;
	
	public Schematic(String pathspec) {
		
		Class<?> classLoader = this.getClass();
		URL resource = classLoader.getResource(pathspec);	
		
		if(resource == null) {
			System.out.println ("File " + pathspec + " not found!");
		} else {
			this.loadSchematic(resource);
        }
		
		// Vanilla schematics (not exported from ingame) need conversion
		
		if(this.vanilla) {
			System.out.println ("Schematic " + pathspec + " is vanilla. Converting.");
			VanillaConverter.convert(this);
		}
	}
	
	public void loadSchematic(URL path) {
		DataInputStream dataInputStream = null; 
		
		try {
			dataInputStream =  new DataInputStream(new GZIPInputStream(path.openStream()));
			
			NBTTagCompound nBTTagCompound = (NBTTagCompound) NBTBase.readTag(dataInputStream);
			
			this.height = nBTTagCompound.getShort("Height");
			this.length = nBTTagCompound.getShort("Length");
			this.width = nBTTagCompound.getShort("Width");
			
			this.vanilla = true;
			
			if(nBTTagCompound.hasKey("Client")) {
				String client = nBTTagCompound.getString("Client");
				if(client.equals("Infhell")) this.vanilla = false;
				if(client.equals("Infdev+")) this.vanilla = false;
			}
			
			if(height == 0 || width == 0 || length == 0) {
				throw new Exception ("Wrong dimensions!");
			}
			
			this.blocks = nBTTagCompound.getByteArray("Blocks");
			if(blocks.length != width * height * length) {
				throw new Exception ("Blocks array size doesn't match dimensions!");
			}
			
			this.data = nBTTagCompound.getByteArray("Data");
			if(data.length != width * height * length) {
				throw new Exception ("Data array size doesn't match dimensions!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getBlocks() {
		return blocks;
	}

	public void setBlocks(byte[] blocks) {
		this.blocks = blocks;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isVanilla() {
		return vanilla;
	}

	public void setVanilla(boolean vanilla) {
		this.vanilla = vanilla;
	}
	
	public void free() {
		// May help with GC in some cases
		this.blocks = null;
		this.data = null;
		this.width = 0;
		this.height = 0;
		this.length = 0;
		this.vanilla = true;
	}
}
