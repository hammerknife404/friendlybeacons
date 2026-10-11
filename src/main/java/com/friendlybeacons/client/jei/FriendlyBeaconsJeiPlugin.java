package com.friendlybeacons.client.jei;

import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.client.BeaconTargetsPanel;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

/** Tells JEI about the beacon side panel so its item list and bookmarks stay clear of it. Only loaded when JEI is installed. */
@JeiPlugin
public class FriendlyBeaconsJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FriendlyBeacons.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(BeaconScreen.class, new IGuiContainerHandler<BeaconScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(BeaconScreen screen) {
                return screen.children().stream()
                        .filter(BeaconTargetsPanel.class::isInstance)
                        .map(BeaconTargetsPanel.class::cast)
                        .map(panel -> new Rect2i(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight()))
                        .toList();
            }
        });
    }
}
