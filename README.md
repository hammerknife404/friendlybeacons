# Friendly Beacons

A NeoForge mod for Minecraft 1.21.1 that lets beacon effects apply to mobs, not just players, with a bigger and adjustable radius and a hidden 5th pyramid tier.

## Features

- Adds an **Affects** panel to the left of the beacon screen with four checkboxes: Passive, Friendly, Hostile and Player.
- Player is selected by default, so beacons behave exactly like vanilla until you change them.
- Changes apply right away. No payment item or Done click is needed, and the beacon's powers are left alone.
- A **Powers** section under the Affects panel lists the effects the beacon will give, with their levels. It updates as you pick powers. Hover a power button to preview the result: effects the beacon doesn't give yet show in green, and ones it would stop giving are struck through.
- A **Radius** button cycles through the radius of each tier your pyramid has reached, so you can keep a beacon off a mob farm next door.
- Larger default reach: 28 / 40 / 52 / 64 blocks for tiers 1–4 (vanilla is 20 / 30 / 40 / 50).
- A **tier 5**: add an 11×11 fifth layer under the pyramid to unlock a 76-block radius and a bonus (see below).
- A **Glowing** checkbox (tier 5 only) makes everything the beacon affects glow through walls. Handy during a raid.
- Server admins can turn each beacon effect off for the whole server, in game or in the config file.
- Each beacon saves its own settings. A broken and re-placed beacon goes back to Player only and the largest radius.
- Effect duration and the tier 1–4 power rules match vanilla.

## Categories

| Checkbox | Affects                                                                                      |
|----------|----------------------------------------------------------------------------------------------|
| Passive  | Animals, villagers, fish, bats, untamed wolves, bees and every other non-hostile mob          |
| Friendly | Tamed mobs with an owner (wolves, cats, parrots, horses, llamas…), iron and snow golems, allays |
| Hostile  | Every mob vanilla treats as hostile: zombies, creepers, slimes, ghasts, endermen, piglins…    |
| Player   | Players                                                                                      |

Armor stands are never affected.

## Tier 5

A fifth pyramid layer (11×11, 285 blocks in total) unlocks a 76-block radius step and a bonus.

The beacon screen shows a tier 5 row under the Secondary Power column, with a 5-layer pyramid icon and two buttons. It stays greyed out until the fifth layer is built.

- **II**: the primary and secondary powers both at level II (e.g. Haste II + Regeneration II). The button shows both power icons.
- **Third power** (marked with a green **+**): a third power at level I. Click it again while it's selected to step to the next effect. It skips effects your powers already cover.

Like vanilla powers, the pick only applies when you press Done with a payment item.

A tier 5 beacon also gives **Glowing** to everything it affects, on top of its powers. The **Glowing** checkbox under the Radius button turns it off for that beacon. It works straight away and needs no payment item, so you can leave it off and switch it on when a raid starts. Glowing follows the Affects checkboxes, so with Player ticked, players glow too.

## Configuration

`config/friendlybeacons-server.toml` in the server (or game) folder, created on first launch. A copy in the world's `serverconfig/` folder overrides it for that world.

| Option        | Default                | Description                                                  |
|---------------|------------------------|--------------------------------------------------------------|
| `tierRadius`  | `[28, 40, 52, 64, 76]` | Maximum radius in blocks for tiers 1–5. Vanilla is `[20, 30, 40, 50]`. |
| `enableTier5` | `true`                 | Allow the 5th pyramid layer.                                 |

### Effect toggles

The `[effects]` section has one `true`/`false` switch per effect: `speed`, `haste`, `resistance`, `jump_boost`, `strength`, `regeneration` and `glowing` (the tier 5 Glowing). All default to `true`. A disabled effect is greyed out on the beacon screen, can't be the tier 5 third power, and existing beacons stop giving it.

```toml
[effects]
	speed = true
	haste = true
	resistance = false
	jump_boost = true
	strength = false
	regeneration = true
	glowing = true
```

Ops (permission level 2) can change them in game without a restart:

- `/friendlybeacons effects` lists every effect and whether it's on.
- `/friendlybeacons effect <name> <true|false>` turns one on or off and saves the config file.

In singleplayer, the mod list's **Config** button opens the same settings.

## Compatibility

With [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) installed, its item list and bookmarks keep clear of the side panel.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.257 or newer

## Installation

Put `friendlybeacons-<version>.jar` in the `mods/` folder. Install it on both the client and the server: the server applies the effects and the client shows the checkboxes. A client without the mod can't join a server that has it.

## How it works

- The settings are stored as NeoForge data attachments on the beacon block entity.
- They sync to the client through extra data slots on the beacon menu.
- Clicking a checkbox, the Radius button or Done (for the tier 5 bonus) sends Minecraft's built-in container button click packet, so the mod adds no custom packets.
- The mod registers one required network channel that is never used. It makes NeoForge turn away clients without the mod at login, instead of letting them crash when they open a beacon.
- The vanilla effect code is replaced with a copy that handles the target categories, the radius and the tier 5 bonus.
- Tier 5 works by letting the vanilla pyramid check look at a fifth layer. Vanilla code already handles a level above 4.

## Building

Requires JDK 21. If it isn't installed, Gradle downloads it automatically.

```sh
./gradlew build      # jar is written to build/libs/
./gradlew runClient  # launch a dev client with the mod
```

## License

MIT
