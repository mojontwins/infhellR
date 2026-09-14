package net.minecraft.game.entity.human;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.game.world.World;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.TradingRecipe;
import net.minecraft.game.trading.TradingRecipeList;
import net.minecraft.game.trading.Currency;
import net.minecraft.game.trading.TradingRecipePresetList;

public abstract class EntityTrader extends EntityZombie implements ITrader, ISentient {
	public TradingRecipeList tradingRecipeList;
	public EntityPlayer tradingWith;
	
	public int traderType;
	protected int angerLevel = 0;
	protected boolean angryAtPlayer = false;
	public ItemStack defaultHeldItem = null;
	
	public EntityTrader(World world) {
		super(world);
		this.traderType = world.rand.nextInt(TradingRecipePresetList.TRADER_TYPES);
		
		// Add armor
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
		
		if(this.rand.nextInt(3) == 0) this.inventory.setArmorItemInSlot(3, new ItemStack(Item.helmetLeather));
		this.fillTradingRecipeList(world, false);
		
		this.health = 20; 	
		this.attackStrength = 4;
	}
	
	@Override
	public String getEntityTexture() {
		return this.texture;
	}

	public void setCustomer(EntityPlayer entityPlayer) {
		this.tradingWith = entityPlayer;
	}
	
	public EntityPlayer getCustomer() {
		return this.tradingWith;
	}
	
	public TradingRecipeList getRecipes(EntityPlayer entityPlayer) {
		if(this.tradingRecipeList == null) {
			this.fillTradingRecipeList(this.worldObj, false);
		}
		
		return this.tradingRecipeList;
	}
	
	public void setRecipes(TradingRecipeList tradingRecipeList) {
		this.tradingRecipeList = tradingRecipeList;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.angerLevel > 0) this.angerLevel --;
		if(this.angerLevel == 0) this.angryAtPlayer = false;
	}
	
	public boolean isTrading() {
		return this.tradingWith != null;
	}
	
	// Only spawns on city chunks
	public boolean isUrban() {
		return true;
	}
	
	public abstract Currency getCurrency();
	
	public void useRecipe(TradingRecipe par1MerchantRecipe) {
		// TODO
	}
	
	public void fillTradingRecipeList(World world, boolean specialTrader) {
		// TODO : `specialTrader` will equal true for riders - those have special & expensive items to trade
		
		this.tradingRecipeList = TradingRecipePresetList.getNewTradingRecipeList(world.rand, specialTrader, this.getCurrency(), this.traderType);
	}
	
	// tradingRecipeList must be saved & loaded
	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);	
		nBTTagCompound1.setTag("TradingRecipeList", this.tradingRecipeList.writeToNBT(new NBTTagList()));
		nBTTagCompound1.setShort("Anger", (short)this.angerLevel);
		nBTTagCompound1.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		nBTTagCompound1.setInteger("TraderType", this.traderType);
		nBTTagCompound1.setInteger("DefaultHeldItemID", this.defaultHeldItem.itemID);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.tradingRecipeList = new TradingRecipeList();
		this.tradingRecipeList.readFromNBT(nBTTagCompound1.getTagList("TradingRecipeList"));
		this.angerLevel = nBTTagCompound1.getShort("Anger");
		this.angryAtPlayer = nBTTagCompound1.getBoolean("AntryAtPlayer");
		this.traderType = nBTTagCompound1.getInteger("TraderType");
		this.defaultHeldItem = new ItemStack(nBTTagCompound1.getInteger("DefaultHeldItemID"));
	}
	
	@Override
	public boolean interact(EntityPlayer entityPlayer) {
		if(this.isEntityAlive() && !this.isTrading() && !this.angryAtPlayer) {
			if (!this.worldObj.isRemote) {
				this.tradingWith = entityPlayer;
				entityPlayer.displayGUITrading(entityPlayer.inventory, this);
			}
			return true;
		} else return super.interact(entityPlayer);
	}

	@Override
	public void onLivingUpdate() {
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
		int y = MathHelper.floor_double(this.boundingBox.minY);
		if(y < 60) return false;
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
	
	public String getTraderName() {
		return TradingRecipePresetList.traderName[traderType];
	}
	
	@Override
	public void onDeath(Entity entity) {		
		if(this.scoreValue > 0 && entity != null) {
			entity.addToPlayerScore(this, this.scoreValue);
		}

		this.setEntityDead();
		if(entity instanceof EntityPlayer)  this.dropFewItems();

	}
}
