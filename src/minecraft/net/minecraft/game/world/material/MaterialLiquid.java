package net.minecraft.game.world.material;

import net.minecraft.game.MapColor;

public class MaterialLiquid extends Material {
	public MaterialLiquid(MapColor mapColor1) {
		super(mapColor1);
		this.setIsGroundCover();
		this.setNoPushMobility();
	}

	public boolean getIsLiquid() {
		return true;
	}

	public boolean getIsSolid() {
		return false;
	}

	public boolean isSolid() {
		return false;
	}
	
	public boolean blocksMovement() {
		return false;
	}
}
