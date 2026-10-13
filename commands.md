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

# Commands & Command Blocks — A Player's Guide

Welcome! This guide will teach you how to use commands and Command Blocks to do cool
things in your world. No programming knowledge needed — we'll start from zero and build
up, one idea at a time, with plenty of examples you can try right away.

---

## 1. Your first command

Press the chat key (the same one you use to talk) and type:

```
/time set day
```

Press Enter. The sun jumps to morning. That's it — you just ran a command.

In single player, the chat box is your command console. You can type commands with or
without the leading `/`; both `/time set day` and `time set day` work the same.

Here are a few more to try:

```
/time set night        make it night
/gamemode creative     switch to creative mode
/gamemode survival     switch back to survival
/give stone 64         drop 64 stone at your feet
/summon Zombie         spawn a zombie nearby
```

If you ever forget what's available, type `/help` to see every command, or
`/help <command>` (for example `/help give`) to see how one command works.

---

## 2. Commands give back numbers

Here's the secret that makes everything else possible: **every command produces a number
when it runs.** That number is called its *result*.

- `/give stone 64` returns the block ID of stone.
- `/summon Zombie` returns the ID of the zombie it created.
- `/playerHas @p stone` returns `1` if you have stone, `0` if you don't.

Most of the time you don't care about the result — you just want the command to *do* its
thing. But the result is what lets us *combine* commands, which is where the fun begins.

---

## 3. Combining commands with `{ }`

You can put a command inside curly brackets `{ }`. The bracketed part runs first, and its
result is dropped into place where the brackets were.

A simple example. This:

```
{ 1 3 4 7 }
```

just becomes `7`. Not very exciting. But now imagine the brackets contain a real command:

```
{ /summon Zombie }
```

This runs `/summon Zombie`, which returns the zombie's ID, and that ID is dropped in
place. So you could write:

```
/setHeldItem { /summon Zombie } stone
```

This summons a zombie, then gives *that exact zombie* a stone to hold — because the
`{ /summon Zombie }` part is replaced by the zombie's ID, which `/setHeldItem` then uses.

That's the whole trick: **a command's result can be another command's argument.**

You can nest brackets inside brackets too:

```
{ 1 { 3 5 } }
```

The inner `{ 3 5 }` becomes `5`, so this becomes `{ 1 5 }`, which becomes `5`.

---

## 4. Zero means "stop"

There's one special number: **0**. If a command ever produces `0`, everything stops right
there and the whole thing fails.

Try this:

```
{ 1 3 4 0 { 1 3 } }
```

The `0` stops everything, so `{ 1 3 }` never runs, and the result is `0`.

This is how we make *decisions*. A command that "fails" returns `0`, and we can use that
to stop a chain of commands in its tracks.

---

## 5. Making decisions with `/if`

The `/if` command runs whatever is inside its brackets, and if that comes out as `0`, it
fails and stops everything. Otherwise it carries on.

Here's a real example. Give yourself 64 stone, but only if you already have some stone:

```
/if { /playerHas @p stone } { /give @p stone 64 }
```

Let's read it step by step:

1. `{ /playerHas @p stone }` runs first. It returns `1` if you have stone, `0` if not.
2. `/if` checks that result. If it's `0`, everything stops — you get nothing.
3. If it's `1`, the second part `{ /give @p stone 64 }` runs and drops 64 stone.

You can also force a failure on purpose with `/stop`, which always returns `0`:

```
/if { /playerHas @p stone } { /give @p stone 64 } /stop
```

---

## 6. Remembering things with variables

Sometimes you want a command to remember a number for later. The game gives you 256
variables, named `$0` through `$255`, and they're saved with your world.

To store a value, use `/let`:

```
/let $0 10
```

This stores `10` in variable `$0`. To read it back, just use `$0` anywhere a number would
go:

```
/give stone $0
```

gives you 10 stone (because `$0` is `10`).

Variables are great for counters. Here's a classic: a button that counts how many times
it's been pressed. Put this in a Command Block:

```
/let $0 { $0 1 }
```

Wait, what? Let's unpack it. `{ $0 1 }` is a list of two numbers, and a list's result is
its *last* number — so it's just `1`. That means this stores `1` in `$0` every time. Not a
counter yet!

To actually count, we'd want to add 1 to the current value. The command system doesn't do
math on its own, but you can chain results. For now, just remember: **`/let` stores, `$n`
reads.**

---

## 7. Command Blocks: automation

So far you've been typing commands by hand. Command Blocks let you run commands
automatically, triggered by redstone.

### Getting one

Command Blocks are block ID **251**. They're unbreakable, and you can grab one from the
creative inventory (the "Blocks" tab).

### Setting one up

1. Place the block.
2. Right-click it to open its screen.
3. Type your command in the big box.
4. Press **Done**.

Now power it with redstone (a lever, button, or pressure plate) and it will run your
command.

### Loop mode

The **Loop** button toggles between two behaviours:

- **"Run only once"** — runs once each time it gets powered.
- **"Run while powered"** — keeps re-running as long as it's powered.

### Chaining blocks

When a Command Block *succeeds* (returns a positive number), it triggers the Command
Blocks touching it (above, below, and on all four sides). When it *fails* (returns `0`),
it doesn't. This lets you build sequences of blocks that run one after another.

---

## 8. A worked example: a key-locked door

Let's build something real: a door that only opens if you're holding a specific item.

**What you need:** a Command Block, a lever, and a door.

1. Place the Command Block next to your door.
2. Right-click it and type:

```
/if { /playerHas @p stone } { /setTile ~ ~ ~ door }
```

3. Press **Done**, then power it with a lever.

Here's what happens when you flip the lever:

1. `{ /playerHas @p stone }` checks if the nearest player has stone. Returns `1` or `0`.
2. If `0`, `/if` fails and nothing happens — the door stays shut.
3. If `1`, `{ /setTile ~ ~ ~ door }` runs and places a door right at the Command Block's
   position.

The `~` means "right here" — the Command Block's own position. So the door appears where
the block is.

Try changing `stone` to something else (like `diamond`) and see how the door only opens
for players holding that item.

---

## 9. Another example: a teleport pad

Let's make a pressure plate that teleports you somewhere.

1. Place a Command Block under a pressure plate.
2. Right-click it and type:

```
/tp @p 100 64 100
```

3. Press **Done**.

Now step on the plate. The nearest player (`@p`) is teleported to coordinates
`100, 64, 100`.

Want to teleport *everyone* nearby instead? Use `@a`:

```
/tp @a 100 64 100
```

And if you want to teleport relative to where the block is, use `~`:

```
/tp @p ~ ~+10 ~
```

This sends the player 10 blocks straight up.

---

## 10. A weather machine

Command Blocks can control the weather. Put these in three blocks wired to three buttons:

```
/rain
/thunder
/snow
```

Each one toggles its weather on or off. Add a fourth for a blood moon:

```
/nextMoonBad
```

---

## 11. Putting it all together

The real power comes from combining everything you've learned. Here's a more advanced
recipe — a block that gives you a reward, but only once:

1. Place a Command Block, set it to **"Run only once"**.
2. Type:

```
/if { /playerHas @p stone } { /give @p diamond 1 } /stop
```

3. Power it with a button.

The first time you press it, you get a diamond. The `/stop` at the end makes the block
fail, so it won't chain to anything else.

---

## 12. Tips and tricks

- **`/help`** lists every command; **`/help <command>`** explains one.
- **`~`** means "relative to here" — `~`, `~+5`, `~-5`.
- **`@p`** is the nearest player, **`@a`** is everyone (within 16 blocks of a Command
  Block).
- **`$0`–`$255`** are your saved variables; use `/let` to set them.
- **`{ }`** runs a command and drops its result in place.
- **`0`** stops everything; use `/stop` to force it.
- **`/if { ... }`** runs the rest only if the bracketed part succeeds.

---

## 13. Where to go next

- Experiment! Change the numbers and items in the examples above and see what happens.
- Use `/help` to discover commands you haven't tried yet.
- Chain Command Blocks together to build bigger machines — a block that succeeds will
  trigger the blocks touching it.

Have fun, and happy building!
