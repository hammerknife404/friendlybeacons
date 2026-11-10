# Beacon Screen

## Affects

The mod adds an **Affects** panel to the left of the beacon screen with four checkboxes: Passive, Friendly, Hostile and Player.

- Player is selected by default, so beacons behave exactly like vanilla until you change them.
- Changes apply right away. No payment item or Done click is needed, and the beacon's powers are left alone.

| Checkbox | Affects |
|---|---|
| Passive | Animals, villagers, fish, bats, untamed wolves, bees and every other non-hostile mob |
| Friendly | Tamed mobs with an owner (wolves, cats, parrots, horses, llamas…), iron and snow golems, allays |
| Hostile | Every mob vanilla treats as hostile: zombies, creepers, slimes, ghasts, endermen, piglins… |
| Player | Players |

Armor stands are never affected.

A tier 5 beacon has a second column of checkboxes on the right of the panel, for [Glowing](Tier-5#glowing).

## Powers

A **Powers** section under the Affects panel lists the effects the beacon will give, with their levels. It updates as you pick powers. Hover a power button to preview the result: effects the beacon doesn't give yet show in green, and ones it would stop giving are struck through.

Effects a server admin has turned off are greyed out (see [Configuration](Configuration#effect-toggles)).

## Radius

A **Radius** button cycles through the radius of each tier your pyramid has reached, so you can keep a beacon off a mob farm next door. The default radii are 28 / 40 / 52 / 64 blocks for tiers 1–4 and 76 for [tier 5](Tier-5) (vanilla is 20 / 30 / 40 / 50); servers can change them in the [config](Configuration).

## Saved settings

Each beacon saves its own settings. A broken and re-placed beacon goes back to Player only, Glowing on hostile mobs only, and the largest radius.
