# Friendly Beacons

A NeoForge mod for Minecraft 1.21.1 that lets beacon effects apply to mobs, not just players, with a bigger and adjustable radius and a hidden 5th pyramid tier.

- An **Affects** panel on the beacon screen picks who gets the powers: Passive, Friendly, Hostile and Player mobs. Player is selected by default, so beacons behave exactly like vanilla until you change them.
- A **Powers** preview shows what the beacon will give before you pay.
- A **Radius** button keeps a beacon off a mob farm next door. The default reach is larger: 28 / 40 / 52 / 64 blocks for tiers 1–4 (vanilla is 20 / 30 / 40 / 50).
- A **tier 5**: an 11×11 fifth layer under the pyramid unlocks a 76-block radius, a bonus (both powers at II, or a third power) and **Glowing** for the mob types you pick. Glowing is handy during a raid.
- Server admins can turn each beacon effect off for the whole server, in game or in the config file.
- Effect duration and the tier 1–4 power rules match vanilla.

**Requires:** Minecraft 1.21.1, NeoForge 21.1.257 or newer. Put `friendlybeacons-<version>.jar` in the `mods/` folder, on both the client and the server: the server applies the effects and the client shows the checkboxes. A client without the mod can't join a server that has it.

**Compatibility:** with [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) installed, its item list and bookmarks keep clear of the side panel.

## Pages

- [Beacon Screen](Beacon-Screen): the Affects panel, who each category covers, the Powers preview and the Radius button.
- [Tier 5](Tier-5): the fifth layer, its bonus and Glowing.
- [Configuration](Configuration): radii, tier 5 and server-wide effect toggles.
- [How It Works](How-It-Works): what's stored on beacons and how the client and server talk.
- [Building and Releasing](Building-and-Releasing): building from source, CI and releases.
