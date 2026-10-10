package com.friendlybeacons.mixin.client;

import com.friendlybeacons.client.Tier5Row;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends the picked tier 5 bonus right before the vanilla Done packet. */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconConfirmButton")
public abstract class BeaconConfirmButtonMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void friendlybeacons$sendBonus(CallbackInfo ci) {
        Tier5Row.onConfirm();
    }
}
