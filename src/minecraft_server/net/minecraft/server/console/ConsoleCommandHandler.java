package net.minecraft.server.console;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayerMP;
import net.minecraft.server.ICommandListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;
import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.human.EntityAlphaWitch;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet4UpdateTime;

public class ConsoleCommandHandler {
	private static Logger minecraftLogger = Logger.getLogger("Minecraft");
	private MinecraftServer minecraftServer;

	public ConsoleCommandHandler(MinecraftServer minecraftServer1) {
		this.minecraftServer = minecraftServer1;
	}

	public void handleCommand(ServerCommand serverCommand1) {
		String command = serverCommand1.command;
		ICommandListener commandListener = serverCommand1.commandListener;
		String username = commandListener.getUsername();
		ServerConfigurationManager serverConfigurationManager = this.minecraftServer.configManager;
		if(!command.toLowerCase().startsWith("help") && !command.toLowerCase().startsWith("?")) {
			if(command.toLowerCase().startsWith("list")) {
				commandListener.log("Connected players: " + serverConfigurationManager.getPlayerList());
			} else if(command.toLowerCase().startsWith("stop")) {
				this.sendNoticeToOps(username, "Stopping the server..");
				this.minecraftServer.initiateShutdown();
			} else {
				int i6;
				WorldServer worldServer;
				if(command.toLowerCase().startsWith("save-all")) {
					this.sendNoticeToOps(username, "Forcing save..");
					if(serverConfigurationManager != null) {
						serverConfigurationManager.savePlayerStates();
					}

					for(i6 = 0; i6 < this.minecraftServer.worldMngr.length; ++i6) {
						worldServer = this.minecraftServer.worldMngr[i6];
						worldServer.saveWorld(true, (IProgressUpdate)null);
					}

					this.sendNoticeToOps(username, "Save complete.");
				} else if(command.toLowerCase().startsWith("save-off")) {
					this.sendNoticeToOps(username, "Disabling level saving..");

					for(i6 = 0; i6 < this.minecraftServer.worldMngr.length; ++i6) {
						worldServer = this.minecraftServer.worldMngr[i6];
						worldServer.levelSaving = true;
					}
				} else if(command.toLowerCase().startsWith("save-on")) {
					this.sendNoticeToOps(username, "Enabling level saving..");

					for(i6 = 0; i6 < this.minecraftServer.worldMngr.length; ++i6) {
						worldServer = this.minecraftServer.worldMngr[i6];
						worldServer.levelSaving = false;
					}
				} else if(command.toLowerCase().startsWith("snow")) {
					worldServer = this.minecraftServer.worldMngr[0];
					worldServer.commandSetSnow();
				} else if(command.toLowerCase().startsWith("rain")) {
					worldServer = this.minecraftServer.worldMngr[0];
					worldServer.commandSetRain();
				} else if(command.toLowerCase().startsWith("thunder")) {
					worldServer = this.minecraftServer.worldMngr[0];
					worldServer.commandSetThunder();
				} else {
					String argument;
					if(command.toLowerCase().startsWith("op ")) {
						argument = command.substring(command.indexOf(" ")).trim();
						serverConfigurationManager.opPlayer(argument);
						this.sendNoticeToOps(username, "Opping " + argument);
						serverConfigurationManager.sendChatMessageToPlayer(argument, "\u00a7eYou are now op!");
					} else if(command.toLowerCase().startsWith("deop ")) {
						argument = command.substring(command.indexOf(" ")).trim();
						serverConfigurationManager.deopPlayer(argument);
						serverConfigurationManager.sendChatMessageToPlayer(argument, "\u00a7eYou are no longer op!");
						this.sendNoticeToOps(username, "De-opping " + argument);
					} else if(command.toLowerCase().startsWith("ban-ip ")) {
						argument = command.substring(command.indexOf(" ")).trim();
						serverConfigurationManager.banIP(argument);
						this.sendNoticeToOps(username, "Banning ip " + argument);
					} else if(command.toLowerCase().startsWith("pardon-ip ")) {
						argument = command.substring(command.indexOf(" ")).trim();
						serverConfigurationManager.pardonIP(argument);
						this.sendNoticeToOps(username, "Pardoning ip " + argument);
					} else if(command.toLowerCase().startsWith("time set ")) {
						boolean timeChanged = false;
						argument = command.substring(command.lastIndexOf(" ")).trim();
						int timeToSet = 0;
						if ("day".equals(argument)) {
							timeToSet = 1000; timeChanged = true;
						} else if ("night".equals(argument)) {
							timeToSet = 14000; timeChanged = true;
						} else if (argument != null && !"".equals(argument)) {
							try {
								timeToSet = Integer.parseInt(argument);
								if (timeToSet >= 0 && timeToSet < 24000) {
									timeChanged = true;
								}
							} catch (Exception e) {
							}
						}
						if (timeChanged) {
							worldServer = this.minecraftServer.worldMngr[0];
							long worldTime = worldServer.getWorldTime();
							long timeBaseDay = worldTime / 24000L * 24000L;
							long remaining = worldTime % 24000L;
							if (timeToSet < remaining) {
								timeBaseDay += 24000;
							}
							worldServer.setWorldTime(timeBaseDay + timeToSet);
							serverConfigurationManager.sendPacketToAllPlayers(new Packet4UpdateTime(worldServer.getWorldTime()));
						} else {
							commandListener.log("Syntax error, use time set [night|day|0-23999].");
						}	
					} else if (command.toLowerCase().startsWith("summon ")) {
						worldServer = this.minecraftServer.worldMngr[0];
						argument = command.substring(command.indexOf(" ")).trim();
						boolean spawned = false;
						EntityLiving entity = (EntityLiving) EntityList.createEntityByName(argument, worldServer);
						System.out.println (">" + entity);
						if (entity != null) {
							EntityPlayerMP playerEntity = serverConfigurationManager.getPlayerEntity(username);
							int x = (int)playerEntity.posX + worldServer.rand.nextInt(8) - 4;
							int y = (int)playerEntity.posY + worldServer.rand.nextInt(4) + 1;
							int z = (int)playerEntity.posZ + worldServer.rand.nextInt(8) - 4;
							commandListener.log ("Attempting to spawn @ " + x + " " + y + " " + z);
							entity.setLocationAndAngles((double)x, (double)y, (double)z, worldServer.rand.nextFloat() * 360.0F, 0.0F);
							
							// TODO
							//if(entity instanceof EntityTrader) ((EntityTrader)entity).fillTradingRecipeList(this.worldMngr, false);
							if(entity instanceof EntityAlphaWitch) ((EntityAlphaWitch)entity).fillInventory();

							commandListener.log("Spawned " + argument + " @ " + x + " " + y + " " + z);
							worldServer.spawnEntityInWorld(entity);
							spawned = true;
						}
						
						if (!spawned) {
							commandListener.log("Could not spawn " + argument + ".");
						} 
					} else {
						EntityPlayerMP playerEntity;
						if (command.toLowerCase().startsWith("gamemode ")) {
							argument = command.substring(command.indexOf(" ")).trim ();
							playerEntity = serverConfigurationManager.getPlayerEntity(username);
							if ("creative".equals(argument) || "1".equals(argument)) {
								playerEntity.setCreativeMode (true);
								this.sendNoticeToOps(username, "Game mode changed to creative for " + username);
							} else if ("survival".equals(argument) || "0".equals(argument)) {
								playerEntity.setCreativeMode (false);
								this.sendNoticeToOps(username, "Game mode changed to survival for " + username);
							} else {
								//TODO
							}
						} else if(command.toLowerCase().startsWith("ban ")) {
							argument = command.substring(command.indexOf(" ")).trim();
							serverConfigurationManager.banPlayer(argument);
							this.sendNoticeToOps(username, "Banning " + argument);
							playerEntity = serverConfigurationManager.getPlayerEntity(argument);
							if(playerEntity != null) {
								playerEntity.playerNetServerHandler.kickPlayer("Banned by admin");
							}
						} else if(command.toLowerCase().startsWith("pardon ")) {
							argument = command.substring(command.indexOf(" ")).trim();
							serverConfigurationManager.pardonPlayer(argument);
							this.sendNoticeToOps(username, "Pardoning " + argument);
						} else {
							int i8;
							if(command.toLowerCase().startsWith("kick ")) {
								argument = command.substring(command.indexOf(" ")).trim();
								playerEntity = null;

								for(i8 = 0; i8 < serverConfigurationManager.playerEntities.size(); ++i8) {
									EntityPlayerMP entityPlayerMP9 = (EntityPlayerMP)serverConfigurationManager.playerEntities.get(i8);
									if(entityPlayerMP9.username.equalsIgnoreCase(argument)) {
										playerEntity = entityPlayerMP9;
									}
								}

								if(playerEntity != null) {
									playerEntity.playerNetServerHandler.kickPlayer("Kicked by admin");
									this.sendNoticeToOps(username, "Kicking " + playerEntity.username);
								} else {
									commandListener.log("Can\'t find user " + argument + ". No kick.");
								}
							} else {
								EntityPlayerMP entityPlayerMP15;
								String[] string18;
								if(command.toLowerCase().startsWith("tp ")) {
									string18 = command.split(" ");
									if(string18.length == 3) {
										playerEntity = serverConfigurationManager.getPlayerEntity(string18[1]);
										entityPlayerMP15 = serverConfigurationManager.getPlayerEntity(string18[2]);
										if(playerEntity == null) {
											commandListener.log("Can\'t find user " + string18[1] + ". No tp.");
										} else if(entityPlayerMP15 == null) {
											commandListener.log("Can\'t find user " + string18[2] + ". No tp.");
										} else if(playerEntity.dimension != entityPlayerMP15.dimension) {
											commandListener.log("User " + string18[1] + " and " + string18[2] + " are in different dimensions. No tp.");
										} else {
											playerEntity.playerNetServerHandler.teleportTo(entityPlayerMP15.posX, entityPlayerMP15.posY, entityPlayerMP15.posZ, entityPlayerMP15.rotationYaw, entityPlayerMP15.rotationPitch);
											this.sendNoticeToOps(username, "Teleporting " + string18[1] + " to " + string18[2] + ".");
										}
									} else {
										commandListener.log("Syntax error, please provice a source and a target.");
									}
								} else {
									String string16;
									int i17;
									if(command.toLowerCase().startsWith("give ")) {
										string18 = command.split(" ");
										if(string18.length != 3 && string18.length != 4) {
											return;
										}

										string16 = string18[1];
										entityPlayerMP15 = serverConfigurationManager.getPlayerEntity(string16);
										if(entityPlayerMP15 != null) {
											try {
												i17 = Integer.parseInt(string18[2]);
												if(Item.itemsList[i17] != null) {
													this.sendNoticeToOps(username, "Giving " + entityPlayerMP15.username + " some " + i17);
													int i10 = 1;
													if(string18.length > 3) {
														i10 = this.tryParse(string18[3], 1);
													}

													if(i10 < 1) {
														i10 = 1;
													}

													if(i10 > 64) {
														i10 = 64;
													}

													entityPlayerMP15.dropPlayerItem(new ItemStack(i17, i10, 0));
												} else {
													commandListener.log("There\'s no item with id " + i17);
												}
											} catch (NumberFormatException numberFormatException11) {
												commandListener.log("There\'s no item with id " + string18[2]);
											}
										} else {
											commandListener.log("Can\'t find user " + string16);
										}
									} else if(command.toLowerCase().startsWith("time ")) {
										string18 = command.split(" ");
										if(string18.length != 3) {
											return;
										}

										string16 = string18[1];

										try {
											i8 = Integer.parseInt(string18[2]);
											WorldServer worldServer19;
											if("add".equalsIgnoreCase(string16)) {
												for(i17 = 0; i17 < this.minecraftServer.worldMngr.length; ++i17) {
													worldServer19 = this.minecraftServer.worldMngr[i17];
													worldServer19.s_func_32005_b(worldServer19.getWorldTime() + (long)i8);
												}

												this.sendNoticeToOps(username, "Added " + i8 + " to time");
											} else if("set".equalsIgnoreCase(string16)) {
												for(i17 = 0; i17 < this.minecraftServer.worldMngr.length; ++i17) {
													worldServer19 = this.minecraftServer.worldMngr[i17];
													worldServer19.s_func_32005_b((long)i8);
												}

												this.sendNoticeToOps(username, "Set time to " + i8);
											} else {
												commandListener.log("Unknown method, use either \"add\" or \"set\"");
											}
										} catch (NumberFormatException numberFormatException12) {
											commandListener.log("Unable to convert time value, " + string18[2]);
										}
									} else if(command.toLowerCase().startsWith("say ")) {
										command = command.substring(command.indexOf(" ")).trim();
										minecraftLogger.info("[" + username + "] " + command);
										serverConfigurationManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7d[Server] " + command));
									} else if(command.toLowerCase().startsWith("tell ")) {
										string18 = command.split(" ");
										if(string18.length >= 3) {
											command = command.substring(command.indexOf(" ")).trim();
											command = command.substring(command.indexOf(" ")).trim();
											minecraftLogger.info("[" + username + "->" + string18[1] + "] " + command);
											command = "\u00a77" + username + " whispers " + command;
											minecraftLogger.info(command);
											if(!serverConfigurationManager.sendPacketToPlayer(string18[1], new Packet3Chat(command))) {
												commandListener.log("There\'s no player by that name online.");
											}
										}
									} else if(command.toLowerCase().startsWith("whitelist ")) {
										this.handleWhitelist(username, command, commandListener);
									} else {
										minecraftLogger.info("Unknown console command. Type \"help\" for help.");
									}
								}
							}
						}
					}
				}
			}
		} else {
			this.printHelp(commandListener);
		}

	}

	private void handleWhitelist(String string1, String string2, ICommandListener iCommandListener3) {
		String[] string4 = string2.split(" ");
		if(string4.length >= 2) {
			String string5 = string4[1].toLowerCase();
			if("on".equals(string5)) {
				this.sendNoticeToOps(string1, "Turned on white-listing");
				this.minecraftServer.propertyManagerObj.setProperty("white-list", true);
			} else if("off".equals(string5)) {
				this.sendNoticeToOps(string1, "Turned off white-listing");
				this.minecraftServer.propertyManagerObj.setProperty("white-list", false);
			} else if("list".equals(string5)) {
				Set<String> set6 = this.minecraftServer.configManager.getWhiteListedIPs();
				String string7 = "";

				String string9;
				for(Iterator<String> iterator8 = set6.iterator(); iterator8.hasNext(); string7 = string7 + string9 + " ") {
					string9 = (String)iterator8.next();
				}

				iCommandListener3.log("White-listed players: " + string7);
			} else {
				String string10;
				if("add".equals(string5) && string4.length == 3) {
					string10 = string4[2].toLowerCase();
					this.minecraftServer.configManager.addToWhiteList(string10);
					this.sendNoticeToOps(string1, "Added " + string10 + " to white-list");
				} else if("remove".equals(string5) && string4.length == 3) {
					string10 = string4[2].toLowerCase();
					this.minecraftServer.configManager.removeFromWhiteList(string10);
					this.sendNoticeToOps(string1, "Removed " + string10 + " from white-list");
				} else if("reload".equals(string5)) {
					this.minecraftServer.configManager.reloadWhiteList();
					this.sendNoticeToOps(string1, "Reloaded white-list from file");
				}
			}

		}
	}

	private void printHelp(ICommandListener iCommandListener1) {
		iCommandListener1.log("To run the server without a gui, start it like this:");
		iCommandListener1.log("   java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui");
		iCommandListener1.log("Console commands:");
		iCommandListener1.log("   help  or  ?               shows this message");
		iCommandListener1.log("   kick <player>             removes a player from the server");
		iCommandListener1.log("   ban <player>              bans a player from the server");
		iCommandListener1.log("   pardon <player>           pardons a banned player so that they can connect again");
		iCommandListener1.log("   ban-ip <ip>               bans an IP address from the server");
		iCommandListener1.log("   pardon-ip <ip>            pardons a banned IP address so that they can connect again");
		iCommandListener1.log("   op <player>               turns a player into an op");
		iCommandListener1.log("   deop <player>             removes op status from a player");
		iCommandListener1.log("   tp <player1> <player2>    moves one player to the same location as another player");
		iCommandListener1.log("   give <player> <id> [num]  gives a player a resource");
		iCommandListener1.log("   tell <player> <message>   sends a private message to a player");
		iCommandListener1.log("   stop                      gracefully stops the server");
		iCommandListener1.log("   save-all                  forces a server-wide level save");
		iCommandListener1.log("   save-off                  disables terrain saving (useful for backup scripts)");
		iCommandListener1.log("   save-on                   re-enables terrain saving");
		iCommandListener1.log("   list                      lists all currently connected players");
		iCommandListener1.log("   say <message>             broadcasts a message to all players");
		iCommandListener1.log("   time <add|set> <amount>   adds to or sets the world time (0-24000)");
	}

	private void sendNoticeToOps(String string1, String string2) {
		String string3 = string1 + ": " + string2;
		this.minecraftServer.configManager.sendChatMessageToAllOps("\u00a77(" + string3 + ")");
		minecraftLogger.info(string3);
	}

	private int tryParse(String string1, int i2) {
		try {
			return Integer.parseInt(string1);
		} catch (NumberFormatException numberFormatException4) {
			return i2;
		}
	}
}
