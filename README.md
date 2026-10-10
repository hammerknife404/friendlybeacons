# Friendly Beacons

A NeoForge mod for Minecraft 1.21.1 that lets beacon effects apply to mobs, not just players.

## Features

- Adds an **Affects** panel to the left of the beacon screen with four checkboxes: Passive, Friendly, Hostile and Player.
- Player is selected by default, so beacons behave exactly like vanilla until you change them.
- Changes apply right away. No payment item or Done click is needed, and the beacon's powers are left alone.
- Each beacon saves its own setting. A broken and re-placed beacon goes back to Player only.
- Range, duration and amplifier match vanilla.

## Categories

| Checkbox | Affects                                                                                      |
|----------|----------------------------------------------------------------------------------------------|
| Passive  | Animals, villagers, fish, bats, untamed wolves, bees and every other non-hostile mob          |
| Friendly | Tamed mobs with an owner (wolves, cats, parrots, horses, llamas…), iron and snow golems, allays |
| Hostile  | Every mob vanilla treats as hostile: zombies, creepers, slimes, ghasts, endermen, piglins…    |
| Player   | Players                                                                                      |

Armor stands are never affected.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.257 or newer

## Installation

Put `friendlybeacons-<version>.jar` in the `mods/` folder. Install it on both the client and the server: the server applies the effects and the client shows the checkboxes.

## How it works

- The setting is stored as a NeoForge data attachment on the beacon block entity.
- It syncs to the client through an extra data slot on the beacon menu.
- Clicking a checkbox sends Minecraft's built-in container button click packet, so the mod adds no custom networking.
- When only Player is selected, the vanilla effect code runs unchanged. Otherwise a copy of it runs that targets every selected category.

## Building

Requires JDK 21. If it isn't installed, Gradle downloads it automatically.

```sh
./gradlew build      # jar is written to build/libs/
./gradlew runClient  # launch a dev client with the mod
```

## License

MIT
