package net.minecraft.game.world.block.tileentity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;

public class TileEntity {
	private static Map<String, Class<?>> nameToClassMap = new HashMap<String, Class<?>>();
	private static Map<Class<?>, String> classToNameMap = new HashMap<Class<?>, String>();
	public World worldObj;
	public int xCoord;
	public int yCoord;
	public int zCoord;
	protected boolean tileEntityInvalid;
	private String ownerEntityType = null;

	@SuppressWarnings("unlikely-arg-type")
	private static void addMapping(Class<?> class0, String string1) {
		if(classToNameMap.containsKey(string1)) {
			throw new IllegalArgumentException("Duplicate id: " + string1);
		} else {
			nameToClassMap.put(string1, class0);
			classToNameMap.put(class0, string1);
		}
	}

	public void readFromNBT(NBTTagCompound nBTTagCompound1) {
		this.xCoord = nBTTagCompound1.getInteger("x");
		this.yCoord = nBTTagCompound1.getInteger("y");
		this.zCoord = nBTTagCompound1.getInteger("z");
		if(nBTTagCompound1.hasKey("OwnerEntityType")) {
			this.ownerEntityType = nBTTagCompound1.getString("OwnerEntityType");
		}
	}

	public void writeToNBT(NBTTagCompound nBTTagCompound1) {
		String string2 = (String)classToNameMap.get(this.getClass());
		if(string2 == null) {
			throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
		} else {
			nBTTagCompound1.setString("id", string2);
			nBTTagCompound1.setInteger("x", this.xCoord);
			nBTTagCompound1.setInteger("y", this.yCoord);
			nBTTagCompound1.setInteger("z", this.zCoord);
			if(this.ownerEntityType != null) {
				nBTTagCompound1.setString("OwnerEntityType", this.ownerEntityType);
			}
		}
	}

	public void updateEntity() {
	}

	public static TileEntity createAndLoadEntity(NBTTagCompound nBTTagCompound0) {
		TileEntity tileEntity1 = null;

		try {
			Class<?> class2 = (Class<?>)nameToClassMap.get(nBTTagCompound0.getString("id"));
			if(class2 != null) {
				tileEntity1 = (TileEntity)class2.newInstance();
			}
		} catch (Exception exception3) {
			exception3.printStackTrace();
		}

		if(tileEntity1 != null) {
			tileEntity1.readFromNBT(nBTTagCompound0);
		} else {
			System.out.println("Skipping TileEntity with id " + nBTTagCompound0.getString("id"));
		}

		return tileEntity1;
	}

	public int getBlockMetadata() {
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
	}

	public void onInventoryChanged() {
		if(this.worldObj != null) {
			this.worldObj.updateTileEntityChunkAndDoNothing(this.xCoord, this.yCoord, this.zCoord, this);
		}

	}

	public Packet getDescriptionPacket() {
		return null;
	}
	
	public double getDistanceFrom(double d1, double d3, double d5) {
		double d7 = (double)this.xCoord + 0.5D - d1;
		double d9 = (double)this.yCoord + 0.5D - d3;
		double d11 = (double)this.zCoord + 0.5D - d5;
		return d7 * d7 + d9 * d9 + d11 * d11;
	}

	public Block getBlockType() {
		return Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)];
	}

	public boolean isInvalid() {
		return this.tileEntityInvalid;
	}

	public void invalidate() {
		this.tileEntityInvalid = true;
	}

	public void validate() {
		this.tileEntityInvalid = false;
	}

	public void setOwnerEntityType(String ownerEntityType) {
		this.ownerEntityType = ownerEntityType;
	}

	static {
		addMapping(TileEntityFurnace.class, "Furnace");
		addMapping(TileEntityChest.class, "Chest");
		addMapping(TileEntityRecordPlayer.class, "RecordPlayer");
		addMapping(TileEntityDispenser.class, "Trap");
		addMapping(TileEntitySign.class, "Sign");
		addMapping(TileEntityMobSpawner.class, "MobSpawner");
		addMapping(TileEntityNote.class, "Music");
		addMapping(TileEntityPiston.class, "Piston");
		addMapping(TileEntityMobSpawnerOneshot.class, "MobSpawnerOneshot");
		addMapping(TileEntityCommandBlock.class, "CommandBlock");
	}
	
	public String getOwnerEntityType() {
		return ownerEntityType;
	}
}
