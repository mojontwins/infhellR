package net.minecraft.client.render.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.entity.monster.EntityTFRedcap;
import net.minecraft.game.entity.monster.EntityTFWraith;
import net.minecraft.client.model.ModelTFRedcap;
import net.minecraft.game.entity.monster.EntityHauntedCow;
import net.minecraft.client.model.ModelHauntedCow;
import net.minecraft.client.model.ModelArcher;
import net.minecraft.client.model.ModelHuman;
import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.client.model.ModelGoat;
import net.minecraft.game.entity.misc.EntityMovingPiston;
import net.minecraft.client.render.MovingPistonRenderer;
import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.entity.animal.EntityCatBlack;
import net.minecraft.game.entity.animal.EntityCatRed;
import net.minecraft.game.entity.animal.EntityCatSiamese;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.game.entity.monster.EntityIceBall;
import net.minecraft.client.model.ModelIceBoss;
import net.minecraft.game.entity.misc.EntityTriton;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.amazonvillage.EntityAmazon;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelSheep1;
import net.minecraft.client.model.ModelSheep2;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.render.ItemRenderer;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.game.entity.EntityFish;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.entity.animal.EntityChicken;
import net.minecraft.game.entity.animal.EntityCow;
import net.minecraft.game.entity.animal.EntityMooshroom;
import net.minecraft.game.entity.animal.EntityPig;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.animal.EntitySquid;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.human.EntityAlphaWitch;
import net.minecraft.game.entity.human.EntityCowman;
import net.minecraft.game.entity.human.EntityIceArcher;
import net.minecraft.game.entity.human.EntityIceBoss;
import net.minecraft.game.entity.human.EntityIceWarrior;
import net.minecraft.game.entity.human.EntityPigman;
import net.minecraft.game.entity.human.EntityPirate;
import net.minecraft.game.entity.human.EntityPirateArcher;
import net.minecraft.game.entity.human.EntityPirateBoss;
import net.minecraft.game.entity.misc.EntityBoat;
import net.minecraft.game.entity.misc.EntityFallingSand;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.entity.misc.EntityTNTPrimed;
import net.minecraft.game.entity.monster.EntityBuilderZombie;
import net.minecraft.game.entity.monster.EntityCityHusk;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityDrowned;
import net.minecraft.game.entity.monster.EntityExplodingZombie;
import net.minecraft.game.entity.monster.EntityGhast;
import net.minecraft.game.entity.monster.EntityGiantZombie;
import net.minecraft.game.entity.monster.EntityHusk;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityToxicZombie;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityEgg;
import net.minecraft.game.entity.projectile.EntityFireball;
import net.minecraft.game.entity.projectile.EntityPebble;
import net.minecraft.game.entity.projectile.EntitySnowball;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;

public class RenderManager {
	private Map<Class<?>, Render> entityRenderMap = new HashMap<Class<?>, Render>();
	public static RenderManager instance = new RenderManager();
	private FontRenderer fontRenderer;
	public static double renderPosX;
	public static double renderPosY;
	public static double renderPosZ;
	public RenderEngine renderEngine;
	public ItemRenderer itemRenderer;
	public World worldObj;
	public EntityLiving livingPlayer;
	public float playerViewY;
	public float playerViewX;
	public GameSettings options;
	public double viewerPosX;
	public double viewerPosY;
	public double viewerPosZ;

	private RenderManager() {
		this.entityRenderMap.put(EntitySpider.class, new RenderSpider());
		this.entityRenderMap.put(EntityPig.class, new RenderPig(new ModelPig(), new ModelPig(0.5F), 0.7F));
		this.entityRenderMap.put(EntitySheep.class, new RenderSheep(new ModelSheep2(), new ModelSheep1(), 0.7F));
		this.entityRenderMap.put(EntityCow.class, new RenderCow(new ModelCow(), 0.7F));
		this.entityRenderMap.put(EntityWolf.class, new RenderWolf(new ModelWolf(), 0.5F));
		this.entityRenderMap.put(EntityChicken.class, new RenderChicken(new ModelChicken(), 0.3F));
		this.entityRenderMap.put(EntityCreeper.class, new RenderCreeper());
		this.entityRenderMap.put(EntitySkeleton.class, new RenderBiped(new ModelSkeleton(), 0.5F));
		this.entityRenderMap.put(EntityZombie.class, new RenderBiped(new ModelZombie(), 0.5F));
		this.entityRenderMap.put(EntitySlime.class, new RenderSlime(new ModelSlime(16), new ModelSlime(0), 0.25F));
		this.entityRenderMap.put(EntityPlayer.class, new RenderPlayer());
		this.entityRenderMap.put(EntityGiantZombie.class, new RenderGiantZombie(new ModelZombie(), 0.5F, 6.0F));
		this.entityRenderMap.put(EntityGhast.class, new RenderGhast());
		this.entityRenderMap.put(EntitySquid.class, new RenderSquid(new ModelSquid(), 0.7F));
		this.entityRenderMap.put(EntityLiving.class, new RenderLiving(new ModelBiped(), 0.5F));
		this.entityRenderMap.put(Entity.class, new RenderEntity());
		this.entityRenderMap.put(EntityPainting.class, new RenderPainting());
		this.entityRenderMap.put(EntityArrow.class, new RenderArrow());
		this.entityRenderMap.put(EntitySnowball.class, new RenderSnowball(Item.snowball.getIconFromDamage(0)));
		this.entityRenderMap.put(EntityEgg.class, new RenderSnowball(Item.egg.getIconFromDamage(0)));
		this.entityRenderMap.put(EntityFireball.class, new RenderFireball());
		this.entityRenderMap.put(EntityIceBall.class, new RenderIceBall());
		this.entityRenderMap.put(EntityItem.class, new RenderItem());
		this.entityRenderMap.put(EntityTNTPrimed.class, new RenderTNTPrimed());
		this.entityRenderMap.put(EntityFallingSand.class, new RenderFallingSand());
		this.entityRenderMap.put(EntityMinecart.class, new RenderMinecart());
		this.entityRenderMap.put(EntityBoat.class, new RenderBoat());
		this.entityRenderMap.put(EntityFish.class, new RenderFish());
		this.entityRenderMap.put(EntityLightningBolt.class, new RenderLightningBolt());

		// Release vanilla
		this.entityRenderMap.put(EntityMooshroom.class, new RenderMooshroom(new ModelCow(), 0.7F));

		// Mine
		this.entityRenderMap.put(EntityPebble.class, new RenderSnowball(Item.pebble.getIconFromDamage(0)));
		this.entityRenderMap.put(EntityThrowablePotion.class, new RenderThrowablePotion());
		this.entityRenderMap.put(EntityIceSkeleton.class, new RenderBiped(new ModelSkeleton(), 0.5F));
		this.entityRenderMap.put(EntityZombieAlex.class, new RenderZombie(new ModelZombie(), 0.5F, "zombie_alex"));
		this.entityRenderMap.put(EntityDrowned.class, new RenderZombie(new ModelZombie(), 0.5F, "drowned"));
		this.entityRenderMap.put(EntityHusk.class, new RenderZombie(new ModelZombie(), 0.5F, "husk"));
		this.entityRenderMap.put(EntityCityHusk.class, new RenderZombie(new ModelZombie(), 0.5F, "zombie"));
		this.entityRenderMap.put(EntityToxicZombie.class, new RenderZombie(new ModelZombie(), 0.5F, "zombie"));
		this.entityRenderMap.put(EntityBuilderZombie.class, new RenderZombie(new ModelZombie(), 0.5F, "zombie"));
		this.entityRenderMap.put(EntityExplodingZombie.class, new RenderExplodingZombie(new ModelZombie(), 0.5F));
		this.entityRenderMap.put(EntityAlphaWitch.class, new RenderWitch());
		this.entityRenderMap.put(EntityBetaOcelot.class, new RenderOcelot(new ModelOcelot(), 0.5F));
		this.entityRenderMap.put(EntityCatBlack.class, new RenderOcelot(new ModelOcelot(), 0.5F));
		this.entityRenderMap.put(EntityCatRed.class, new RenderOcelot(new ModelOcelot(), 0.5F));
		this.entityRenderMap.put(EntityCatSiamese.class, new RenderOcelot(new ModelOcelot(), 0.5F));
		this.entityRenderMap.put(EntityAmazon.class, new RenderAmazon());

		// Twilight Forest
		this.entityRenderMap.put(EntityTFRedcap.class, new RenderBiped(new ModelTFRedcap(), 0.625F));
		this.entityRenderMap.put(EntityTFWraith.class, new RenderTFWraith(new ModelZombie(), 0.5F));

		// Better Dungeons
		this.entityRenderMap.put(EntityPirate.class, new RenderHuman(new ModelHuman(), 0.5F));
		this.entityRenderMap.put(EntityPirateArcher.class, new RenderBiped(new ModelArcher(), 0.5F));
		this.entityRenderMap.put(EntityPirateBoss.class, new RenderBiped(new ModelArcher(), 0.5F));

		// Classic pistons
		this.entityRenderMap.put(EntityMovingPiston.class, new MovingPistonRenderer());

		// More stuff
		this.entityRenderMap.put(EntityTriton.class, new RenderTriton());
		this.entityRenderMap.put(EntityHauntedCow.class, new RenderHauntedCow(new ModelHauntedCow(), 0.7F));

		// Ice palace
		this.entityRenderMap.put(EntityIceWarrior.class, new RenderHuman(new ModelHuman(), 0.5F));
		this.entityRenderMap.put(EntityIceArcher.class, new RenderBiped(new ModelArcher(), 0.5F));
		this.entityRenderMap.put(EntityIceBoss.class, new RenderIceBoss(new ModelIceBoss(), 0.5F));

		// Traders
		this.entityRenderMap.put(EntityPigman.class, new RenderBiped(new ModelBiped(), 0.5F));
		this.entityRenderMap.put(EntityCowman.class, new RenderBiped(new ModelBiped(), 0.5F));

		// Mo Creatures
		this.entityRenderMap.put(EntityGoat.class, new RenderGoat(new ModelGoat(), 0.7F));

		Iterator<Render> iter = this.entityRenderMap.values().iterator();

		while(iter.hasNext()) {
			Render render = iter.next();
			render.setRenderManager(this);
		}

	}

	public Render getEntityClassRenderObject(Class<?> clazz) {
		Render render = this.entityRenderMap.get(clazz);
		if(render == null && clazz != Entity.class) {
			render = this.getEntityClassRenderObject(clazz.getSuperclass());
			this.entityRenderMap.put(clazz, render);
		}

		return render;
	}

	public Render getEntityRenderObject(Entity entity) {
		return this.getEntityClassRenderObject(entity.getClass());
	}

	public void cacheActiveRenderInfo(World world, RenderEngine renderEngine, FontRenderer fontRenderer, EntityLiving entityLiving, GameSettings gameSettings, float partialTicks) {
		this.worldObj = world;
		this.renderEngine = renderEngine;
		this.options = gameSettings;
		this.livingPlayer = entityLiving;
		this.fontRenderer = fontRenderer;
		if(entityLiving.isPlayerSleeping()) {
			int blockId = world.getBlockId(MathHelper.floor_double(entityLiving.posX), MathHelper.floor_double(entityLiving.posY), MathHelper.floor_double(entityLiving.posZ));
			if(blockId == Block.blockBed.blockID) {
				int blockMeta = world.getBlockMetadata(MathHelper.floor_double(entityLiving.posX), MathHelper.floor_double(entityLiving.posY), MathHelper.floor_double(entityLiving.posZ));
				int orientation = blockMeta & 3;
				this.playerViewY = (float)(orientation * 90 + 180);
				this.playerViewX = 0.0F;
			}
		} else {
			this.playerViewY = entityLiving.prevRotationYaw + (entityLiving.rotationYaw - entityLiving.prevRotationYaw) * partialTicks;
			this.playerViewX = entityLiving.prevRotationPitch + (entityLiving.rotationPitch - entityLiving.prevRotationPitch) * partialTicks;
		}

		this.viewerPosX = entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * (double)partialTicks;
		this.viewerPosY = entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * (double)partialTicks;
		this.viewerPosZ = entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * (double)partialTicks;
	}

	public void renderEntity(Entity entity, float partialTicks) {
		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
		int lightValue = entity.getBrightnessForRender(partialTicks);
		if(entity.isBurning()) {
			lightValue = 15728880;
		}

		int skyLight = lightValue % 65536;
		int blockLight = lightValue / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight / 1.0F, (float)blockLight / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderEntityWithPosYaw(entity, x - renderPosX, y - renderPosY, z - renderPosZ, yaw, partialTicks);
	}

	public void renderEntityWithPosYaw(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		Render render = this.getEntityRenderObject(entity);
		if(render != null) {
			render.doRender(entity, x, y, z, yaw, partialTicks);
			render.doRenderShadowAndFire(entity, x, y, z, yaw, partialTicks);
		}

	}

	public void set(World world) {
		this.worldObj = world;
	}

	public double getDistanceToCamera(double x, double y, double z) {
		double dx = x - this.viewerPosX;
		double dy = y - this.viewerPosY;
		double dz = z - this.viewerPosZ;
		return dx * dx + dy * dy + dz * dz;
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}
}
