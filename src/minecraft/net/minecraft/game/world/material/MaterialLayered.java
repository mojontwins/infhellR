package net.minecraft.game.world.material;

import net.minecraft.game.MapColor;

public class MaterialLayered extends Material {

	public MaterialLayered(MapColor mapColor1) {
		super(mapColor1);
	}

	@Override
	public boolean isSolid() {
		return false;
	}
	
	@Override
	public boolean getIsSolid() {
		return false;
	}
}
