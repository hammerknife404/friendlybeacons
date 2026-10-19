package com.friendlybeacons.client;

import com.friendlybeacons.FriendlyBeacons;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Adds a Config button to the mod list, for editing the server config (effect toggles included) in singleplayer. */
@Mod(value = FriendlyBeacons.MOD_ID, dist = Dist.CLIENT)
public class FriendlyBeaconsClient {
    public FriendlyBeaconsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
