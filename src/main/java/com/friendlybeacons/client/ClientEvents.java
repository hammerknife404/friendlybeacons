package com.friendlybeacons.client;

import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.FriendlyBeaconsConfig;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = FriendlyBeacons.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof BeaconScreen screen)) return;
        Tier5Row tier5 = null;
        if (FriendlyBeaconsConfig.tier5Enabled()) {
            tier5 = new Tier5Row(screen);
            event.addListener(tier5);
        }
        // Left of the beacon GUI; clamped on-screen for very small windows.
        int x = Math.max(2, screen.getGuiLeft() - BeaconTargetsPanel.WIDTH - 2);
        event.addListener(new BeaconTargetsPanel(x, screen.getGuiTop(), screen, tier5));
    }
}
