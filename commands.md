I want to (re)use the same nested commands system for three tasks:

- Single player command (SPC).
- Command blocks (CB).
- Server commands via chat or directly (SMP).

In SPC mode, commands won't accept a player target parameter and will only aply on "self". CB should accept @a and @p commands. @a should have a range, a command block shouldn't affect a player that's more than 16 blocks away. CMP should accept @a, @p and usernames and @a should be global.

As command blocks are driven by tile entities, there's a "world" object reference so the list of players for @a and @p should be easily retrievable using the method I've added to World.

SPC should ignore the player completely and always execute commands directly.

Ideas:

- Chat, if SP it will call the parser in "executle directly" mode.
- Chat, if SMP it will send the commands to the server.
- Server will call the parser with the commands received in "parse target" mode with distance = 0.
- CBs will call the parser with the command in "parse target" mode with distance = 16.

Interfaces:

- Ahora: SMP: Minecraft.java -> GuiChat(this) -> new SinglePlayerCommands(this.mc).

Si hago CommandsParser como estático, necesitaría llamar al parser con esta información:

- Command string.
- World.
- Players list.

- En SP, world es this.mc.theWorld, playersList = null.
- En SMP, world es el mundo del jugador que envió el chat, playersList se obtiene del server.
- En CB, world es el mundo del tile entity, playersList se obtiene de world.findPlayers(chunkCoordinates, 16, null);

# Command blocks

1.- Command is executed while block is being powered, when ticked.
2.- If command in block fails (returns 0), command is stopped until block is reset (i.e. it's not powered).
3.- If command in block succeeds (returns 1), it will power connected blocks.

A command fails if the outter, latest command returns 0.

You can force a 0 with "/stop".

# Variables

Support for $A variables with A = 0..255. Make a method to parse a lvalue and another to parse a rvalue. so something like $A = $B + 1 works.

# Expresions

Suppor for simple expresions. For example integrate https://github.com/javalc6/Simple-Expression-Parser . Using {expresion} should solve the expresion and return the results.

# Relative values

Support for ~([+|-]\d+)

# Chain?

Work this way:

1.- A command block emits executed on completion.
	- Via notify to surrounding blocks that haven't executed this tick!

2.- A command block is triggered by:
	- redstone signal
	- surrounding command block has emitted execution (bit 2 set).

3.- If redstone signal, run if not blocked.

4.- If /stop, block until no redstone signal.

5.- Loop / non loop blocks, will run while redstone signal (unless they fail / are stopped) / once.

# Add a command to modify creature attributes

Such as:

* max life (also sets life?)
* life
* attack strength
* speed

```
	/setattribute <id> <attribute> <value>
```

# Easy conditions:

```
	/if { command } ...
```

If `command` yields zero, the current execution exits with 0.

```
	{/if {/summon Gianta}} {/summon Zombie}
	      \_____________/
	             \ This will fail
	                        \_____________/
	                               \ So this will be never executed             
```

## checks

```
	/tileIs <x> <y> <z> <id:meta>
```

Returns 1 if tile @ (x, y, z) == id:meta.

```
	/playerHas <@p|player_name> <id:meta>
```

## flags

* [X] Lvalue / Rvalue parser
* [X] /let lvalue rvalue
* [X] $... yields rvalue
* [X] save flags with world!

## comparisons 

* [ ] /eq A B -> 1 ok, 0 false
* [ ] /ne A B
* [ ] /lt A B
* [ ] /ge A B

# Doc to export

# How commands work

Commands are a list of numbers. This may sound stupid, but it's important to understand this. Commands have a result. The result of a list of numbers is the last number. If you have:

```
	1 3 4 7
```

The result is 7.

Those list of numbers can be enclosed in curly brackets. Each enclosed section has its own result, which is, again, the last number, so if you have:

```
	{1 3 4 7}
```

The result is still 7. And you may have:

```
	{1 3} {4 7}
```

Which will get reduced to

```
	3 7
```

And finally, it will yield just "7".

You can nest commands.

```
	{1 {3 5}}
```

As {3 5} yields 5, this will become

```
	{1 5}
```

which will yield "5".

If the parser finds a 0, the current execution stops and returns 0. So 

```
	{1 3 4 0 {1 3}}
```

Will yield 0, and {1 3} won't ever be looked at.

This is nonsense, what's this good for? Well, instead of numbers you can have actual commands that do stuff and yield numbers. Actual commands that take parameters. And you know what? Those parameters can be the result of other commands. Does this make sense now?

# Some simple commands


# Some simple commands in sequence


# Some sinple nested commands


# Some commands executed conditionally


# Storing and comparing values


# How do I run these commands?