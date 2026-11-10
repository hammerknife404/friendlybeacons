# How It Works

- The settings are stored as NeoForge data attachments on the beacon block entity.
- They sync to the client through extra data slots on the beacon menu.
- Clicking a checkbox (Affects or Glowing), the Radius button or Done (for the tier 5 bonus) sends Minecraft's built-in container button click packet, so the mod adds no custom packets.
- The mod registers one required network channel that is never used. It makes NeoForge turn away clients without the mod at login, instead of letting them crash when they open a beacon.
- The vanilla effect code is replaced with a copy that handles the target categories, the radius and the tier 5 bonus.
- Tier 5 works by letting the vanilla pyramid check look at a fifth layer. Vanilla code already handles a level above 4.
