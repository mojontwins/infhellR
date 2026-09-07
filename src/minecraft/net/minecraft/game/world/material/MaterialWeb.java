package net.minecraft.game.world.material;

import net.minecraft.game.MapColor;

public class MaterialWeb extends Material {

	public MaterialWeb(MapColor mapColor1) {
		super(mapColor1);
	}

	public boolean blocksMovement() {
		return false;
	}
}
