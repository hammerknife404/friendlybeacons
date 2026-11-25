package com.friendlybeacons.mixin.client;

import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.client.Tier5Row;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconConfirmButton")
public abstract class BeaconConfirmButtonMixin {
    /** Sends the picked tier 5 bonus right before the vanilla Done packet. */
    @Inject(method = "onPress", at = @At("HEAD"))
    private void friendlybeacons$sendBonus(CallbackInfo ci) {
        Tier5Row.onConfirm();
    }

    /** Creative beacons need no payment item, so Done only needs a primary power. */
    @Inject(method = "updateStatus", at = @At("TAIL"))
    private void friendlybeacons$creativeNeedsNoPayment(int beaconTier, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof BeaconScreen screen
                && screen.getMenu() instanceof BeaconTargetsHolder settings && settings.friendlybeacons$isCreative()) {
            ((AbstractWidget) (Object) this).active = ((BeaconScreenAccessor) screen).friendlybeacons$getPrimary() != null;
        }
    }
}
