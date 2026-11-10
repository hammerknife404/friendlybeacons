# Configuration

`config/friendlybeacons-server.toml` in the server (or game) folder, created on first launch. A copy in the world's `serverconfig/` folder overrides it for that world.

| Option | Default | Description |
|---|---|---|
| `tierRadius` | `[28, 40, 52, 64, 76]` | Maximum radius in blocks for tiers 1–5. Vanilla is `[20, 30, 40, 50]`. |
| `enableTier5` | `true` | Allow the 5th pyramid layer. |

## Effect toggles

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
