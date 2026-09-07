package net.minecraft.game.world.material;

import net.minecraft.game.MapColor;

public class MaterialTransparent extends Material {
	public MaterialTransparent(MapColor mapColor1) {
		super(mapColor1);
		this.setIsGroundCover();
	}

	public boolean isSolid() {
		return false;
	}

	public boolean getCanBlockGrass() {
		return false;
	}

	public boolean getIsSolid() {
		return false;
	}
	
	public boolean blocksMovement() {
		return false;
	}
}
