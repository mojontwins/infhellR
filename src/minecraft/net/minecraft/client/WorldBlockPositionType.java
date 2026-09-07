package net.minecraft.client;

class WorldBlockPositionType {
	int posX;
	int posY;
	int posZ;
	int field_1206_d;
	int blockID;
	int metadata;
	final WorldClient field_1203_g;

	public WorldBlockPositionType(WorldClient worldClient, int x, int y, int z, int blockId, int meta) {
		this.field_1203_g = worldClient;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.field_1206_d = 80;
		this.blockID = blockId;
		this.metadata = meta;
	}
}
