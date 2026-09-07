package net.minecraft.client.render.entity;

import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.client.render.atlas.TexelScale;

public class RenderBlockHollowLog {
	/*
	 * Texture lookup:
	 * ti_0 = log_inside
	 * ti_1 = log_end
	 * ti_2 = log_side
	 */
	public static boolean renderBlock (IBlockAccess blockAccess, Block block, int meta, float x, float y, float z, int ti_0, int ti_1, int ti_2) {
		// Facing north/south
		if(meta == 4 || meta == 5) return renderMeta2(blockAccess, block, x, y, z, ti_0, ti_1, ti_2);

		// Facing east/west
		if(meta == 2 || meta == 3) return renderMeta4(blockAccess, block, x, y, z, ti_0, ti_1, ti_2);
		
		return renderMeta0(blockAccess, block, x, y, z, ti_0, ti_1, ti_2);
	}
	

	public static boolean renderMeta0 (IBlockAccess blockAccess, Block block, float x, float y, float z, int ti_0, int ti_1, int ti_2) {
		Tessellator tessellator = Tessellator.instance;
		float x1, y1, z1, x2, y2, z2;
		float u1, v1, u2, v2;

		// Texture #0
		AtlasTexel.calc(ti_0, TextureAtlas.TERRAIN);
		float t0_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t0_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #1
		AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
		float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #2
		AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
		float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Cube #0

		x1 = x + 0.1250F;
		y1 = y + 0.0000F;
		z1 = z + 0.1250F;
		x2 = x + 0.8750F;
		y2 = y + 1.0000F;
		z2 = z + 0.8750F;

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.5F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z, 1.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.8F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.8F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y2, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		// Cube #1

		x1 = x + 0.0000F;
		y1 = y + 0.0000F;
		z1 = z + 0.0000F;
		x2 = x + 1.0000F;
		y2 = y + 1.0000F;
		z2 = z + 1.0000F;

		setLightValue(tessellator, blockAccess, block, x + 1, y, z, 1.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x - 1, y, z, 0.5F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u1, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z - 1, 0.8F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z + 1, 0.8F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y2, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y + 1, z, 0.6F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x, y - 1, z, 0.6F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);

		return true;
	}


	public static boolean renderMeta2 (IBlockAccess blockAccess, Block block, float x, float y, float z, int ti_0, int ti_1, int ti_2) {
		Tessellator tessellator = Tessellator.instance;
		float x1, y1, z1, x2, y2, z2;
		float u1, v1, u2, v2;

		// Texture #0
		AtlasTexel.calc(ti_0, TextureAtlas.TERRAIN);
		float t0_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t0_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #1
		AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
		float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #2
		AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
		float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Cube #0

		x1 = x + 0.0000F;
		y1 = y + 0.1250F;
		z1 = z + 0.1250F;
		x2 = x + 1.0000F;
		y2 = y + 0.8750F;
		z2 = z + 0.8750F;

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.5F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z, 1.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.8F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y1, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.8F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		// Cube #1

		x1 = x + 0.0000F;
		y1 = y + 0.0000F;
		z1 = z + 0.0000F;
		x2 = x + 1.0000F;
		y2 = y + 1.0000F;
		z2 = z + 1.0000F;

		setLightValue(tessellator, blockAccess, block, x, y + 1, z, 1.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y - 1, z, 0.5F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y1, z2, u1, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z - 1, 0.8F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z + 1, 0.8F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x + 1, y, z, 0.6F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x - 1, y, z, 0.6F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		return true;
	}

	public static boolean renderMeta4 (IBlockAccess blockAccess, Block block, float x, float y, float z, int ti_0, int ti_1, int ti_2) {
		Tessellator tessellator = Tessellator.instance;
		float x1, y1, z1, x2, y2, z2;
		float u1, v1, u2, v2;

		// Texture #0
		AtlasTexel.calc(ti_0, TextureAtlas.TERRAIN);
		float t0_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t0_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #1
		AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
		float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #2
		AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
		float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Cube #0

		x1 = x + 0.8750F;
		y1 = y + 0.1250F;
		z1 = z + 1.0000F;
		x2 = x + 0.1250F;
		y2 = y + 0.8750F;
		z2 = z + 0.0000F;

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.5F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u2, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z, 1.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y1, z2, u1, v1);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.6F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u1, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z, 0.6F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);

		// Cube #1

		x1 = x + 1.0000F;
		y1 = y + 0.0000F;
		z1 = z + 1.0000F;
		x2 = x + 0.0000F;
		y2 = y + 1.0000F;
		z2 = z + 0.0000F;

		setLightValue(tessellator, blockAccess, block, x, y + 1, z, 1.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u2, v1);

		setLightValue(tessellator, blockAccess, block, x, y - 1, z, 0.5F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);

		setLightValue(tessellator, blockAccess, block, x + 1, y, z, 0.6F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		setLightValue(tessellator, blockAccess, block, x - 1, y, z, 0.6F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z + 1, 0.8F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);

		setLightValue(tessellator, blockAccess, block, x, y, z - 1, 0.8F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u1, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u2, v1);

		return true;
	}
	
	public static boolean renderAsItem (IBlockAccess blockAccess, Block block, int ti_0, int ti_1, int ti_2, Tessellator tessellator, float blockBrightness) {
		float x1, y1, z1, x2, y2, z2;
		float u1, v1, u2, v2;

		// Texture #0
		AtlasTexel.calc(ti_0, TextureAtlas.TERRAIN);
		float t0_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t0_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #1
		AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
		float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Texture #2
		AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
		float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float) AtlasTexel.u);
		float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float) AtlasTexel.v);

		// Cube #0

		x1 = 0.1250F;
		y1 = 0.0000F;
		z1 = 0.1250F;
		x2 = 0.8750F;
		y2 = 1.0000F;
		z2 = 0.8750F;

		//setLightValueItem(tessellator, block, 0.5F, blockBrightness);
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v2);

		//setLightValueItem(tessellator, block, 1.0F, blockBrightness);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);	
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x1, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

		//setLightValueItem(tessellator, block, 0.8F, blockBrightness);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);

		//setLightValueItem(tessellator, block, 0.8F, blockBrightness);
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		u1 = t0_u + TextureAtlas.TERRAIN.uPixels[2];
		v1 = t0_v + TextureAtlas.TERRAIN.vPixels[2];
		u2 = t0_u + TextureAtlas.TERRAIN.uPixels[14];
		v2 = t0_v + TextureAtlas.TERRAIN.vPixels[14];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y2, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		// Cube #1

		x1 = 0.0000F;
		y1 = 0.0000F;
		z1 = 0.0000F;
		x2 = 1.0000F;
		y2 = 1.0000F;
		z2 = 1.0000F;

		//setLightValueItem(tessellator, block, 1.0F, blockBrightness);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

		//setLightValueItem(tessellator, block, 0.5F, blockBrightness);
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z2, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u1, v1);

		//setLightValueItem(tessellator, block, 0.8F, blockBrightness);
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y1, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
		tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

		//setLightValueItem(tessellator, block, 0.8F, blockBrightness);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		u1 = t2_u + 0.0F;
		v1 = t2_v + 0.0F;
		u2 = t2_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t2_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y2, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z2, u1, v2);

		//setLightValueItem(tessellator, block, 0.6F, blockBrightness);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x1, y2, z1, u1, v2);
		tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
		tessellator.addVertexWithUV(x2, y2, z2, u2, v1);
		tessellator.addVertexWithUV(x2, y2, z1, u2, v2);

		//setLightValueItem(tessellator, block, 0.6F, blockBrightness);
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		u1 = t1_u + 0.0F;
		v1 = t1_v + 0.0F;
		u2 = t1_u + TextureAtlas.TERRAIN.uPixels[16];
		v2 = t1_v + TextureAtlas.TERRAIN.vPixels[16];
		tessellator.addVertexWithUV(x2, y1, z2, u1, v1);
		tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
		tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
		tessellator.addVertexWithUV(x2, y1, z1, u1, v2);

		return true;
	}

	public static void setLightValue (Tessellator tessellator, IBlockAccess blockAccess, Block block, float x, float y, float z, float factor) {
		/*
		float f;
		if (blockAccess == null) f = factor; else f = block.getBlockBrightness(blockAccess, (int) x, (int) y, (int) z) * factor;
		if (Block.lightValue[block.blockID] > 0) f = factor;
		tessellator.setColorOpaque_F(f, f, f);
		*/
		
		tessellator.setBrightness(block.getMixedBrightnessForBlock(blockAccess, (int)x, (int)y, (int)z));
		tessellator.setColorOpaque_F(factor, factor, factor);
	}
	
	public static void setLightValueItem (Tessellator tessellator, Block block, float factor, float blockBrightness) {
		float f;
		f = factor * blockBrightness;
		if (Block.lightValue[block.blockID] > 0) f = factor;
		tessellator.setColorOpaque_F(f, f, f);
	}
}
