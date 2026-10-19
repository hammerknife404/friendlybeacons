package com.friendlybeacons.mixin.client;

import com.friendlybeacons.FriendlyBeaconsConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Greys out vanilla power buttons for effects turned off in the config. */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton")
public abstract class BeaconPowerButtonMixin {
    @Inject(method = "updateStatus", at = @At("TAIL"))
    private void friendlybeacons$disableTurnedOffEffects(int beaconTier, CallbackInfo ci) {
        if (!FriendlyBeaconsConfig.effectEnabled(((BeaconPowerButtonAccessor) this).friendlybeacons$getEffect())) {
            ((AbstractWidget) (Object) this).active = false;
        }
    }
}
