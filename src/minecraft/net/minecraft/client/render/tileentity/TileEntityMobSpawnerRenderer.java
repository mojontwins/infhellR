package net.minecraft.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.client.render.entity.RenderManager;

public class TileEntityMobSpawnerRenderer extends TileEntitySpecialRenderer {
	private Map<String, Entity> entityCache = new HashMap<String, Entity>();

	public void renderTileEntityMobSpawner(TileEntityMobSpawner spawner, double x, double y, double z, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y, (float)z + 0.5F);
		Entity entity = this.entityCache.get(spawner.getMobID());
		if(entity == null) {
			entity = EntityList.createEntityByName(spawner.getMobID(), null);
			this.entityCache.put(spawner.getMobID(), entity);
		}

		if(entity != null) {
			entity.setWorld(spawner.worldObj);
			float scale = 0.4375F;
			GL11.glTranslatef(0.0F, 0.4F, 0.0F);
			GL11.glRotatef((float)(spawner.prevYaw + (spawner.yaw - spawner.prevYaw) * (double)partialTicks) * 10.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.4F, 0.0F);
			GL11.glScalef(scale, scale, scale);
			entity.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
			RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
		}

		GL11.glPopMatrix();
	}

	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		this.renderTileEntityMobSpawner((TileEntityMobSpawner)tileEntity, x, y, z, partialTicks);
	}
}
