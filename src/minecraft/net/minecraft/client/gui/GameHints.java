package net.minecraft.client.gui;

import java.util.Random;

public class GameHints {
	public static Random rand = new Random(System.currentTimeMillis());
	
	public static final String[] hints = new String[] {
		"Walk better on snow with leather boots!",
		"Some trees drop apples!",
		"Golden tools do something special!",
		"Winners never kill creepers!",
		"Give valuable gems to Amazons and hire them!",
		"Coral can only be replanted in water",
		"Copper is used to create the diving helmet",
		"Explore the oceans with a diving helmet!",
		"Blocks of coal will burn forever",
		"Ice skeleton rags are good to keep you warm",
		"Pirates will accept you if you wear their clothes",
		"Never trust an ice warrior!",
		"Glaciers may hide icebound ships",
		"Can you find a Monolith?",
		"a block of rotten flesh will attract zombies",
		"Hit crying obsidian with a compass to set a new spawn point",
		"Use a crying obsidian wand to teleport to your spawn point",
		"Two crying obsidian, one diamond and two sticks for a wand",
		"During winter you may freeze if you don’t have a covered fire place",
		"Place upside down slabs and stairs!",
		"Make paths on grass right-clicking with a shovel!",
		"Use pebbles on grass...",
		"peel the bark (and get it!) from trunks right-clicking with an axe",
		"Beware of mobs when they are armored, they are more dangerous!",
		"Nasty mobs lurk in the mangrove swamps... Can you find the witch?",
		"Be sure to adopt a cat. With fresh fish.",
		"Yup, you also got (prettier than vanilla) mineshafts!",
		"Cities are infested with zombies. Beware of poisonous zombies.",
		"You can throw potions you steal from witches! Look for the witch manor!",
		"Can you find the huge underground lair of the Slime Boss?",
		"Slime-like stone brick monuments in the surface give a hint.",
		"Amazons can be hired for gems! But don’t steal from them, silly!",
		"Remember good ol' Hippoplatimus pistons?",
		"The only 100% safe way to kill cows is with a sword!",
		"Give animal names with a tag so they never despawn. Can you find a tag?",
		"Goats will eat anything.",
		"You can sprint, but only on max heath!",
		"Player will be crippled on low health",
		"It's a good idea to stay away from cities at first",
		"The dark forest is incredibly dangerous!",
		"Monsters will attack animals. Be careful.",
		"Plain beds won't skip the night. Enchanted beds do!",
		"Craft a Lunar Wand and right-click a bed to enchant it.",
		"Enchanted beds set your spawn when you sleep or wake up in them."
	};
	
	public static String getRandomHint() {
		return hints[rand.nextInt(hints.length)];
	}
}
