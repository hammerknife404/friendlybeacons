package com.friendlybeacons.client;

import com.friendlybeacons.FriendlyBeacons;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client entrypoint: adds the config screen, with effect icons on the effect toggles, to the Mods list. */
@Mod(value = FriendlyBeacons.MOD_ID, dist = Dist.CLIENT)
public class FriendlyBeaconsClient {
    public FriendlyBeaconsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, new EffectToggleFilter()));
    }
}
