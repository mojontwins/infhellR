package net.minecraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.minecraft.game.Seasons;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.IArmoredMob;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemArmor;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IWorldAccess;
import net.minecraft.game.world.SpawnerAnimals;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.bo3.WorldGenBo3Tree;

// This is not used anymore. There's more beefy stuff @ net.minecraft.game.command

public class SinglePlayerCommands {
	
	/*
	 * Implements a simple command parser launched from the chat console, easily hookable:
	 * 
	 * 1. In Minecraft.java, there's code which prevents the chat console from appearing
	 *    if you are not playing SMP, remove it and change for something else like I did,
	 *    (I have PlayerEntity.enableCheats). Also pass an instance of the Minecraft object
	 *    to GuiChat, for example:
	 *    
	 *    if((this.isRemote() || this.thePlayer.enableCheats) && Keyboard.getEventKey() == this.options.keyBindChat.keyCode) {
	 *        this.displayGuiScreen(new GuiChat(this));
	 *    }
	 *    
	 *    This will make the chat console pop when the bound key is pressed.
	 *    
	 * 2. In GuiChat.java, change the constructor to get the Minecraft instance & store it.
	 *    Also create an instance of this class:
	 * 
	 *    public GuiChat(Minecraft mc) {
	 *          this.mc = mc;
	 *          this.singlePlayerCommands = new SinglePlayerCommands(this.mc);
	 *    }
	 *    
	 * 3. Also in GuiChat.java, add the hook to this code in the keyTyped method when newline
	 *    is detected, running the actual chat if in SMP, or calling this parser:
	 *    
	 *    protected void keyTyped(char character, int key) {
	 *    [...]
	 *    if(key == 28) {
	 *        String string3 = this.message.trim();
	 *        if(string3.length() > 0) {
	 *            if (this.mc.isRemote()) {
	 *                this.mc.thePlayer.sendChatMessage(this.message.trim());
	 *            } else {
	 *                this.singlePlayerCommands.executeCommand(this.message.trim());
	 *            }
	 *        }
	 *        
	 * This code includes a minimal parser which understands gamemode / time set / tp.
	 * It should be easy to adapt it to fit your needs.
	 * 
	 * Enjoy!
	 * 
	 * by na_th_an
	 * Use freely, credit if you wish. Make mods.
	 */
	
	private Minecraft mc;
	private WorldGenBo3Tree bo3Tree = new WorldGenBo3Tree(true);
	
	public SinglePlayerCommands(Minecraft mc) {
		this.mc = mc;
	}
	
	public Minecraft getMinecraft() {
		return this.mc;
	}
	
	public List<String> tokenizeWithBraces(String s) {
		// Cheap
		s = s + " ";
		
		// Tokenizes a space separated string of tokens with 1 level of curly brazes
		ArrayList<String> result = new ArrayList<String> ();
		
		char charArray[] = s.toCharArray();
		
		// Number of nested braces. Will be "in braces" until this is 0.
		int braces = 0;
		
		// Current token
		String curToken = "";
		
		for(int i = 0; i < charArray.length; i ++) {
			boolean addWord = false;
			char c = charArray[i];

			if (c == ' ' || c == '\t' || c == '\n') {
				if(braces == 0) {
					addWord = true;
				} else {
					curToken += String.valueOf(c);
				}
			} else {
				curToken += String.valueOf(c);
			
				if (c == '{') {
					braces ++;
				} else if (c == '}') {
					if(braces > 0) braces --;
					if(braces == 0) addWord = true;
				}
			} 
			
			if(addWord) {
				if(curToken.length() > 0) {
					result.add(curToken);
					curToken = "";
				}
			}
			
		}
		
		return result;
	}
	
	public int toIntWithDefault(String s, int defVal) {
		int res = defVal;
		try {
			res = Integer.parseInt(s);
		} catch (Exception e) {
			
		}
		return res;
	}
	
	public int executeCommand(String command) {
		List<String> tokensRaw = tokenizeWithBraces(command.trim());
		
		command = "";

		for(int i = 0; i < tokensRaw.size(); i ++) {
			if(i > 0) command += " ";
			String token = tokensRaw.get(i);
			if(token.indexOf("{") >= 0) {
				command = command + this.executeCommand(token.substring(1, token.length() - 1).trim());
			} else {
				command = command + token;
			}
		}
		
		StringTokenizer tokenizer = new StringTokenizer(command);
		int res = 0;
		
		int numTokens = tokenizer.countTokens();
		if (numTokens == 0) return res;
		
		String[] tokens = new String [numTokens];
		int idx = 0;
		while (tokenizer.hasMoreTokens()) {
			tokens [idx++] = tokenizer.nextToken();
		}
		
		if (idx > 0) {
			String cmd = tokens [0];
			if ("/gamemode".equals(cmd)) {
				if (idx > 1) {
					String gameMode = tokens [1];
					if ("0".equals(gameMode) || "survival".equals(gameMode)) {
						if (this.mc.thePlayer.isCreative) this.mc.ingameGUI.addChatMessage("Game mode changed to survival");
						this.mc.thePlayer.isCreative = false;
						this.mc.thePlayer.isFlying = false;
					} else if ("1".equals(gameMode) || "creative".equals(gameMode)) {
						if (!this.mc.thePlayer.isCreative) this.mc.ingameGUI.addChatMessage("Game mode changed to creative");
						this.mc.thePlayer.isCreative = true;
					}
				}
			} else if ("/time".equals(cmd)) {
				if (idx > 2 && "set".equals(tokens [1])) {
					int timeSet = -1;
					if ("night".equals(tokens [2])) {
						timeSet = 14000;
					} else if ("day".equals(tokens [2])) {
						timeSet = 1000;
					} else {
						timeSet = this.toIntWithDefault(tokens [2], -1);
					}
					if(timeSet > 0) {
						long timeBaseDay = this.mc.theWorld.getWorldTime() / 24000L * 24000L;
						long elapsedDay = this.mc.theWorld.getWorldTime() % 24000L;
						if (timeSet > elapsedDay) timeBaseDay += 24000L;
						this.mc.theWorld.setWorldTime(timeBaseDay + timeSet);
						this.mc.ingameGUI.addChatMessage("Time set to " + timeSet);
					} else {
						this.mc.ingameGUI.addChatMessage("Wrong time!");
					}
				}
			} else if ("/tp".equals(cmd)) {
				if (idx > 3) {
					double x = this.mc.thePlayer.posX;
					double y = this.mc.thePlayer.posY;
					double z = this.mc.thePlayer.posZ;
					
					try {
						x = Double.parseDouble(tokens [1]);
					} catch (Exception e) { }
					
					try {
						y = Double.parseDouble(tokens [2]);
					} catch (Exception e) { }
					
					try {
						z = Double.parseDouble(tokens [3]);
					} catch (Exception e) { }
					
					this.mc.thePlayer.setPosition(x, y, z);
					this.mc.ingameGUI.addChatMessage("Teleporting to " + x + " " + y + " " + z);
				}
			} else if ("/summon".equals(cmd)) {
				if (idx > 1) {
					boolean spawned = false;
					EntityLiving entity = (EntityLiving) EntityList.createEntityByName(tokens [1], this.mc.theWorld);
					System.out.println (">" + entity);
					if (entity != null) {
						int x = (int)this.mc.thePlayer.posX + this.mc.theWorld.rand.nextInt(8) - 4;
						int y = (int)this.mc.thePlayer.posY + this.mc.theWorld.rand.nextInt(4) + 1;
						int z = (int)this.mc.thePlayer.posZ + this.mc.theWorld.rand.nextInt(8) - 4;
					
						if(idx > 4) {
							x = this.toIntWithDefault(tokens[2], x);
							y = this.toIntWithDefault(tokens[3], y);
							z = this.toIntWithDefault(tokens[4], z);
						}
						
						System.out.println ("Attempting to spawn @ " + x + " " + y + " " + z);
						entity.setLocationAndAngles((double)x, (double)y, (double)z, this.mc.theWorld.rand.nextFloat() * 360.0F, 0.0F);
						
						SpawnerAnimals.creatureSpecificInit(entity, this.mc.theWorld, x, y, z);
						
						if(idx == 3 && entity instanceof IMobWithLevel) {
							try {
								int level = Integer.parseInt(tokens[2]);
								if(level < ((IMobWithLevel)entity).getMaxLevel()) ((IMobWithLevel)entity).setLevel(level);
							} catch (Exception e) { }
						}
						
						this.mc.ingameGUI.addChatMessage("Spawned " + tokens [1] + " @ " + x + " " + y + " " + z + " id=" + entity.entityId);
						this.mc.theWorld.spawnEntityInWorld(entity);
						spawned = true;
						res = entity.entityId;
					}
					
					if (!spawned) {
						this.mc.ingameGUI.addChatMessage("Could not spawn " + tokens [1] + ".");
					} 
				} else {
					this.mc.ingameGUI.addChatMessage("/summon <Entity> [<x> <y> <z>]");
				}
				
			} else if ("/setHeldItem".equals(cmd)) {
				if(idx > 2) {
					int entityId = this.toIntWithDefault(tokens[1], -1);
					int itemId = this.toIntWithDefault(tokens[2], -1);
					
					if(entityId >= 0 && itemId >= 0) {
						Item item = Item.itemsList[itemId];
						if (item != null) {
							Entity entity = this.mc.theWorld.getEntityById(entityId);
							if(entity instanceof EntityLiving) {
								((EntityLiving) entity).setHeldItem(new ItemStack(item));
							}
						}
					}
					res = entityId;
				} else {
					this.mc.ingameGUI.addChatMessage("/setHeldItem <entityId> <itemId>");
				}
			
			} else if ("/setArmor".equals(cmd)) {
				if(idx > 3) {
					int entityId = this.toIntWithDefault(tokens[1], -1);
					int armorType = ItemArmor.toArmorType(tokens[2]);
					int itemId = ItemArmor.toItemId(armorType, tokens[3]);
					
					if(entityId >= 0 && armorType >= 0 && itemId >= 0) {
						Item item = Item.itemsList[itemId];
						if(item != null) {
							Entity entity = this.mc.theWorld.getEntityById(entityId);
							if(entity instanceof IArmoredMob) {
								((IArmoredMob)entity).setArmor(armorType, new ItemStack(item));
							}
						}
					}
					res = entityId;
				} else {
					this.mc.ingameGUI.addChatMessage("/setArmor <entityId> <armorType> <itemId>");
				}
				
			} else if ("/nextMoonBad".equals(cmd)) {
				this.mc.theWorld.nextMoonBad = true;
				this.mc.ingameGUI.addChatMessage("Next moon will be blood moon");
			} else if ("/snow".equals(cmd)) {
				this.mc.theWorld.worldInfo.setSnowingTime(0);
			} else if ("/rain".equals(cmd)) {
				this.mc.theWorld.worldInfo.setRainTime(0);
			} else if ("/thunder".equals(cmd)) {
				this.mc.theWorld.worldInfo.setThunderTime(0);
			} else if ("/setDay".equals(cmd)) {

				try {
					int day = Integer.parseInt(tokens[1]);
					int dayWithinYear = day % (4 * Seasons.SEASON_DURATION);
					long currentYear = this.mc.theWorld.getWorldTime() / (4 * Seasons.SEASON_DURATION);
					int currentDayWithinYear = (int)(this.mc.theWorld.getWorldTime() % (4 * Seasons.SEASON_DURATION));
					if(currentDayWithinYear > dayWithinYear) currentYear ++;
					
					this.mc.theWorld.setWorldTime(currentYear * (4 * Seasons.SEASON_DURATION) + dayWithinYear);
					
					Seasons.dayOfTheYear = dayWithinYear;
					Seasons.updateSeasonCounters();
					
					for(int i5 = 0; i5 < this.mc.theWorld.worldAccesses.size(); ++i5) {
						((IWorldAccess)this.mc.theWorld.worldAccesses).updateAllRenderers();
					}
					
					this.mc.ingameGUI.addChatMessage("Day set to " + dayWithinYear + ": " + Seasons.seasonNames[Seasons.currentSeason] + ", day " + Seasons.dayOfTheSeason);
				} catch (Exception e) {
					this.mc.ingameGUI.addChatMessage("Wrong day");
				}

			} else if ("/give".equals(cmd)) {
				// /give id quantity
				if(idx > 2) {
					// Just an id or id:damage ?
					int dotdot = tokens[1].indexOf(':');
					int blockID = 0; 
					int metadata = 0;
					int quantity = 0;
					
					try {
						if(dotdot >= 0) {
							blockID = Integer.parseInt(tokens[1].substring(0, dotdot));
							metadata = Integer.parseInt(tokens[1].substring(dotdot + 1));
						} else {
							blockID = Integer.parseInt(tokens[1]);
						}
						
						quantity = Integer.parseInt(tokens[2]);
											
						float f6 = 0.7F;
						double d7 = (double)(this.mc.theWorld.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
						double d9 = (double)(this.mc.theWorld.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
						double d11 = (double)(this.mc.theWorld.rand.nextFloat() * f6) + (double)(1.0F - f6) * 0.5D;
						EntityItem entityItem13 = new EntityItem(this.mc.theWorld, this.mc.thePlayer.posX + d7, this.mc.thePlayer.posY + d9, this.mc.thePlayer.posZ + d11, new ItemStack(blockID, quantity, metadata));
						entityItem13.delayBeforeCanPickup = 10;
						this.mc.theWorld.spawnEntityInWorld(entityItem13);
					} catch (Exception e) { }

				}
			} else if ("/bo3tree".equals(cmd)) {
				try {
					String treeShape = null;
					int x = 0, y = 0, z = 0;
					
					if (idx == 4) {
						x = Integer.parseInt(tokens[1]);
						z = Integer.parseInt(tokens[2]);
						y = this.mc.theWorld.getLandSurfaceHeightValue(x, z);
						
						treeShape = tokens[3];
					} else if (idx == 2) {
						x = this.mc.objectMouseOver.blockX;
						y = this.mc.objectMouseOver.blockY + 1;
						z = this.mc.objectMouseOver.blockZ;
						
						treeShape = tokens[1];
					}
					
					if (treeShape != null) {
						bo3Tree.setTreeName(treeShape);
						
						if(bo3Tree.generate(this.mc.theWorld, this.mc.theWorld.rand, x, y, z)) {
							this.mc.ingameGUI.addChatMessage("Spawned Bo3 tree " + treeShape + " @ " + x + " " + y + "  " + z);
						} else {
							this.mc.ingameGUI.addChatMessage("Could not spawn " + treeShape); 
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("/urban".equals(cmd)) {
				int chunkX = (int)this.mc.thePlayer.posX / 16;
				int chunkZ = (int)this.mc.thePlayer.posZ / 16;
				Chunk chunk = this.mc.theWorld.getChunkFromChunkCoords(chunkX, chunkZ);
				chunk.needsSaving(true);
				chunk.isUrbanChunk = true;
				chunk.hasBuilding = true;
				chunk.hasRoad = true;
				this.mc.ingameGUI.addChatMessage("chunk " + chunkX + " " + chunkZ + " set as urban"); 
			}
		}
		
		return res;
	}
	
	public void printMessage(String message) {
		this.mc.ingameGUI.addChatMessage(message);
	}
}
