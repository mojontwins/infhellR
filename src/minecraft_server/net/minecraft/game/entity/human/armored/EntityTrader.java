package net.minecraft.game.entity.human.armored;

import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.trading.Currency;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.TradingRecipe;
import net.minecraft.game.trading.TradingRecipeList;
import net.minecraft.game.trading.TradingRecipePresetList;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Base for the humanoid traders (pigmen, cowmen). A trader carries a set of
 * trading recipes, faces and stops moving while a customer has the trade screen
 * open, and only turns hostile once angered.
 */
public abstract class EntityTrader extends EntityArmoredMob implements ITrader, ISentient {
	public TradingRecipeList tradingRecipeList;
	public EntityPlayer tradingWith;

	// Texture variations. TODO: this block is duplicated from EntityZombie and
	// is planned to move to a shared EntityLiving once all mobs support skins.
	protected String texturePrefix;

	public int traderType;
	protected int angerLevel = 0;
	protected boolean angryAtPlayer = false;
	public ItemStack defaultHeldItem = null;

	public EntityTrader(World world) {
		super(world);
		this.traderType = world.rand.nextInt(TradingRecipePresetList.TRADER_TYPES);

		// Give the trader his profession's armour and default held item.
		switch(this.traderType) {
			case TradingRecipePresetList.TRADER_BLACKSMITH:
				this.inventory.setArmorItemInSlot(2, new ItemStack(Item.plateSteel));
				this.defaultHeldItem = new ItemStack(Item.swordStone);
				break;
			case TradingRecipePresetList.TRADER_BUTCHER:
				this.inventory.setArmorItemInSlot(2, new ItemStack(Item.plateChain));
				this.defaultHeldItem = new ItemStack(Item.porkRaw);
				break;
			case TradingRecipePresetList.TRADER_FARMER:
				this.defaultHeldItem = new ItemStack(Item.hoeStone);
				break;
		}

		if(this.rand.nextInt(3) == 0) {
			this.inventory.setArmorItemInSlot(3, new ItemStack(Item.helmetLeather));
		}

		this.fillTradingRecipeList(world, false);

		this.health = 20;
		this.attackStrength = 4;
	}

	protected int getMaxTextureVariations() {
		return 1;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte)(1 + this.rand.nextInt(this.getMaxTextureVariations())));
	}

	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(Datawatchers.DW_TYPE, variation);
	}

	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE);
	}

	@Override
	public String getEntityTexture() {
		if(this.getMaxTextureVariations() > 1) {
			return "/mob/" + this.texturePrefix + this.getTextureVariation() + ".png";
		}
		return this.texture;
	}

	public void setCustomer(EntityPlayer player) {
		this.tradingWith = player;
	}

	public EntityPlayer getCustomer() {
		return this.tradingWith;
	}

	public TradingRecipeList getRecipes(EntityPlayer player) {
		if(this.tradingRecipeList == null) {
			this.fillTradingRecipeList(this.worldObj, false);
		}

		return this.tradingRecipeList;
	}

	public void setRecipes(TradingRecipeList recipes) {
		this.tradingRecipeList = recipes;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// Anger fades with time.
		if(this.angerLevel > 0) {
			--this.angerLevel;
		}
		if(this.angerLevel == 0) {
			this.angryAtPlayer = false;
		}
	}

	public boolean isTrading() {
		return this.tradingWith != null;
	}

	/** Traders only spawn inside city chunks. */
	public boolean isUrban() {
		return true;
	}

	public abstract Currency getCurrency();

	public void useRecipe(TradingRecipe recipe) {
		// TODO
	}

	public void fillTradingRecipeList(World world, boolean specialTrader) {
		// `specialTrader` is true for riders, which trade special, expensive items.
		this.tradingRecipeList = TradingRecipePresetList.getNewTradingRecipeList(world.rand, specialTrader, this.getCurrency(), this.traderType);
	}

	// The trading recipe list must be saved and loaded.
	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setTag("TradingRecipeList", this.tradingRecipeList.writeToNBT(new NBTTagList()));
		tag.setShort("Anger", (short)this.angerLevel);
		tag.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		tag.setInteger("TraderType", this.traderType);
		tag.setInteger("DefaultHeldItemID", this.defaultHeldItem.itemID);
		tag.setByte("TextureVariation", this.getTextureVariation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.tradingRecipeList = new TradingRecipeList();
		this.tradingRecipeList.readFromNBT(tag.getTagList("TradingRecipeList"));
		this.angerLevel = tag.getShort("Anger");
		this.angryAtPlayer = tag.getBoolean("AngryAtPlayer");
		this.traderType = tag.getInteger("TraderType");
		this.defaultHeldItem = new ItemStack(tag.getInteger("DefaultHeldItemID"));
		this.setTextureVariation(tag.hasKey("TextureVariation") ? tag.getByte("TextureVariation") : (byte)1);
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if(this.isEntityAlive() && !this.isTrading() && !this.angryAtPlayer) {
			if(!this.worldObj.isRemote) {
				this.tradingWith = player;
				player.displayGUITrading(player.inventory, this);
			}
			return true;
		} else {
			return super.interact(player);
		}
	}

	@Override
	public void onLivingUpdate() {
		// Look at the customer while trading.
		if(this.isTrading()) {
			this.faceEntity(this.tradingWith, 10, 0);
		}
		super.onLivingUpdate();
	}

	@Override
	protected boolean isMovementCeased() {
		return this.isTrading();
	}

	@Override
	public boolean getCanSpawnHere() {
		int surfaceY = MathHelper.floor_double(this.boundingBox.minY);
		if(surfaceY < 60) {
			return false;
		}

		return this.worldObj.checkIfAABBIsClear(this.boundingBox)
				&& this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0
				&& !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}

	public String getTraderName() {
		return TradingRecipePresetList.traderName[this.traderType];
	}

	@Override
	public void onDeath(Entity source) {
		if(this.scoreValue > 0 && source != null) {
			source.addToPlayerScore(this, this.scoreValue);
		}

		this.setEntityDead();
		if(source instanceof EntityPlayer) {
			this.dropFewItems();
		}
	}
}
