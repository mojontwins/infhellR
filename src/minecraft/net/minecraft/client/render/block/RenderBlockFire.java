package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;

/**
 * Block render handler for the fire block (BlockRenderType FIRE).
 *
 * <p>Emits the flame as vertical billboards rather than solid faces. Two layouts
 * are used depending on what is below the cell:
 * <ul>
 *   <li><b>Grounded flame</b> - a solid or flammable block is below: two layers
 *       of crossed quads are drawn, each layer being two crossing diagonal
 *       boards along x and two along z. The <i>interior</i> layer spans about
 *       0.2..0.8 of the cell; the <i>outer</i> layer is made of slivers at the
 *       cell edges (0.0..0.1 and 0.9..1.0).</li>
 *   <li><b>Floating flame</b> - nothing solid/flammable below: four vertical
 *       quads are drawn on the sides where a neighbour can catch fire, inset by
 *       0.2 and raised 0.0625 above the base. If the cell above can catch fire,
 *       a full-cell pair of crossing diagonal boards is added on top.</li>
 * </ul>
 *
 * <p>Quirks:
 * <ul>
 *   <li>Every quad is emitted twice with reversed winding so the flame is
 *       visible from both sides.</li>
 *   <li>The two rows of the fire atlas tile (tileV and tileV+16) are alternated
 *       between the crossing boards of each layer, giving a two-frame look.
 *       The floating side quads always use the first row.</li>
 *   <li>Parity tests ((x + y + z &amp; 1) and (x/2 + y/2 + z/2 &amp; 1)) flip the
 *       crossing axis and the horizontal texture direction so adjacent flames
 *       look different.</li>
 *   <li>Always returns true - the fire block always produces custom geometry.</li>
 * </ul>
 */
public final class RenderBlockFire implements BlockRenderHandler {
	/**
	 * Emits the fire block's quads into the currently active tessellator.
	 * The caller (RenderBlocks) owns the tessellator start/draw lifecycle.
	 *
	 * @param renderBlocks the active block renderer (blockAccess, overrideBlockTexture)
	 * @param block the fire block being rendered
	 * @param x the block's x coordinate
	 * @param y the block's y coordinate
	 * @param z the block's z coordinate
	 * @return always true (the fire block always produces geometry)
	 */
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {

		Tessellator tessellator = Tessellator.instance;
		int texId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		float height = 1.4F;
		if(!renderBlocks.blockAccess.isBlockNormalCube(x, y - 1, z) && !Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x, y - 1, z)) {
			float edge = 0.2F;
			float ground = 0.0625F;
			if((x + y + z & 1) == 1) {
				uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
				uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
				vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(tileV + TextureAtlas.TILE));
				vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan + (float)TextureAtlas.TILE);
			}

			if((x / 2 + y / 2 + z / 2 & 1) == 1) {
				double tempU = uHi;
				uHi = uLo;
				uLo = tempU;
			}

			if(Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x - 1, y, z)) {
				tessellator.addVertexWithUV((double)((float)x + edge), (double)((float)y + height + ground), (double)(z + 1), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 1), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)((float)x + edge), (double)((float)y + height + ground), (double)(z + 0), uLo, vLo);
				tessellator.addVertexWithUV((double)((float)x + edge), (double)((float)y + height + ground), (double)(z + 0), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 1), uHi, vHi);
				tessellator.addVertexWithUV((double)((float)x + edge), (double)((float)y + height + ground), (double)(z + 1), uHi, vLo);
			}

			if(Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x + 1, y, z)) {
				tessellator.addVertexWithUV((double)((float)(x + 1) - edge), (double)((float)y + height + ground), (double)(z + 0), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 1 - 0), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 1 - 0), (double)((float)(y + 0) + ground), (double)(z + 1), uHi, vHi);
				tessellator.addVertexWithUV((double)((float)(x + 1) - edge), (double)((float)y + height + ground), (double)(z + 1), uHi, vLo);
				tessellator.addVertexWithUV((double)((float)(x + 1) - edge), (double)((float)y + height + ground), (double)(z + 1), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 1 - 0), (double)((float)(y + 0) + ground), (double)(z + 1), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 1 - 0), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)((float)(x + 1) - edge), (double)((float)y + height + ground), (double)(z + 0), uLo, vLo);
			}

			if(Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x, y, z - 1)) {
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height + ground), (double)((float)z + edge), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 0), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height + ground), (double)((float)z + edge), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height + ground), (double)((float)z + edge), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 0) + ground), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 0), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height + ground), (double)((float)z + edge), uHi, vLo);
			}

			if(Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x, y, z + 1)) {
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height + ground), (double)((float)(z + 1) - edge), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 0) + ground), (double)(z + 1 - 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 1 - 0), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height + ground), (double)((float)(z + 1) - edge), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height + ground), (double)((float)(z + 1) - edge), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 0) + ground), (double)(z + 1 - 0), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 0) + ground), (double)(z + 1 - 0), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height + ground), (double)((float)(z + 1) - edge), uLo, vLo);
			}

			if(Block.fire.canBlockCatchFire(renderBlocks.blockAccess, x, y + 1, z)) {
				double xBottom1 = (double)x + 0.5D + 0.5D;
				double xBottom2 = (double)x + 0.5D - 0.5D;
				double zBottom2 = (double)z + 0.5D + 0.5D;
				double zBottom1 = (double)z + 0.5D - 0.5D;
				double xTop1 = (double)x + 0.5D - 0.5D;
				double xTop2 = (double)x + 0.5D + 0.5D;
				double zTop2 = (double)z + 0.5D - 0.5D;
				double zTop1 = (double)z + 0.5D + 0.5D;
				uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
				uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
				vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
				vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
				++y;
				height = -0.2F;
				if((x + y + z & 1) == 0) {
					tessellator.addVertexWithUV(xTop1, (double)((float)y + height), (double)(z + 0), uHi, vLo);
					tessellator.addVertexWithUV(xBottom1, (double)(y + 0), (double)(z + 0), uHi, vHi);
					tessellator.addVertexWithUV(xBottom1, (double)(y + 0), (double)(z + 1), uLo, vHi);
					tessellator.addVertexWithUV(xTop1, (double)((float)y + height), (double)(z + 1), uLo, vLo);
					uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
					uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
					vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(tileV + TextureAtlas.TILE));
					vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan + (float)TextureAtlas.TILE);
					tessellator.addVertexWithUV(xTop2, (double)((float)y + height), (double)(z + 1), uHi, vLo);
					tessellator.addVertexWithUV(xBottom2, (double)(y + 0), (double)(z + 1), uHi, vHi);
					tessellator.addVertexWithUV(xBottom2, (double)(y + 0), (double)(z + 0), uLo, vHi);
					tessellator.addVertexWithUV(xTop2, (double)((float)y + height), (double)(z + 0), uLo, vLo);
				} else {
					tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), zTop1, uHi, vLo);
					tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), zBottom1, uHi, vHi);
					tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), zBottom1, uLo, vHi);
					tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), zTop1, uLo, vLo);
					uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
					uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
					vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(tileV + TextureAtlas.TILE));
					vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan + (float)TextureAtlas.TILE);
					tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), zTop2, uHi, vLo);
					tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), zBottom2, uHi, vHi);
					tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), zBottom2, uLo, vHi);
					tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), zTop2, uLo, vLo);
				}
			}
		} else {
			double innerXBottom1 = (double)x + 0.5D + 0.2D;
			double innerXBottom2 = (double)x + 0.5D - 0.2D;
			double innerZBottom2 = (double)z + 0.5D + 0.2D;
			double innerZBottom1 = (double)z + 0.5D - 0.2D;
			double innerXTop1 = (double)x + 0.5D - 0.3D;
			double innerXTop2 = (double)x + 0.5D + 0.3D;
			double innerZTop2 = (double)z + 0.5D - 0.3D;
			double innerZTop1 = (double)z + 0.5D + 0.3D;
			tessellator.addVertexWithUV(innerXTop1, (double)((float)y + height), (double)(z + 1), uHi, vLo);
			tessellator.addVertexWithUV(innerXBottom1, (double)(y + 0), (double)(z + 1), uHi, vHi);
			tessellator.addVertexWithUV(innerXBottom1, (double)(y + 0), (double)(z + 0), uLo, vHi);
			tessellator.addVertexWithUV(innerXTop1, (double)((float)y + height), (double)(z + 0), uLo, vLo);
			tessellator.addVertexWithUV(innerXTop2, (double)((float)y + height), (double)(z + 0), uHi, vLo);
			tessellator.addVertexWithUV(innerXBottom2, (double)(y + 0), (double)(z + 0), uHi, vHi);
			tessellator.addVertexWithUV(innerXBottom2, (double)(y + 0), (double)(z + 1), uLo, vHi);
			tessellator.addVertexWithUV(innerXTop2, (double)((float)y + height), (double)(z + 1), uLo, vLo);
			uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
			uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
			vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(tileV + TextureAtlas.TILE));
			vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan + (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), innerZTop1, uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), innerZBottom1, uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), innerZBottom1, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), innerZTop1, uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), innerZTop2, uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), innerZBottom2, uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), innerZBottom2, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), innerZTop2, uLo, vLo);
			double outerXBottom1 = (double)x + 0.5D - 0.5D;
			double outerXBottom2 = (double)x + 0.5D + 0.5D;
			double outerZBottom2 = (double)z + 0.5D - 0.5D;
			double outerZBottom1 = (double)z + 0.5D + 0.5D;
			double outerXTop1 = (double)x + 0.5D - 0.4D;
			double outerXTop2 = (double)x + 0.5D + 0.4D;
			double outerZTop2 = (double)z + 0.5D - 0.4D;
			double outerZTop1 = (double)z + 0.5D + 0.4D;
			tessellator.addVertexWithUV(outerXTop1, (double)((float)y + height), (double)(z + 0), uLo, vLo);
			tessellator.addVertexWithUV(outerXBottom1, (double)(y + 0), (double)(z + 0), uLo, vHi);
			tessellator.addVertexWithUV(outerXBottom1, (double)(y + 0), (double)(z + 1), uHi, vHi);
			tessellator.addVertexWithUV(outerXTop1, (double)((float)y + height), (double)(z + 1), uHi, vLo);
			tessellator.addVertexWithUV(outerXTop2, (double)((float)y + height), (double)(z + 1), uLo, vLo);
			tessellator.addVertexWithUV(outerXBottom2, (double)(y + 0), (double)(z + 1), uLo, vHi);
			tessellator.addVertexWithUV(outerXBottom2, (double)(y + 0), (double)(z + 0), uHi, vHi);
			tessellator.addVertexWithUV(outerXTop2, (double)((float)y + height), (double)(z + 0), uHi, vLo);
			uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
			uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
			vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
			vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
			tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), outerZTop1, uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), outerZBottom1, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), outerZBottom1, uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), outerZTop1, uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + height), outerZTop2, uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), outerZBottom2, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), outerZBottom2, uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + height), outerZTop2, uHi, vLo);
		}

		return true;
	
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
