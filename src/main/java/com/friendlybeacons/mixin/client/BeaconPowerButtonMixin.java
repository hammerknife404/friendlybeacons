package com.friendlybeacons.mixin.client;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargetsHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Greys out power buttons for effects disabled server-wide. Covers the tier 4 upgrade button through its super call. */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton")
public abstract class BeaconPowerButtonMixin {
    @Shadow
    private Holder<MobEffect> effect;

    @Inject(method = "updateStatus", at = @At("TAIL"))
    private void friendlybeacons$disableTurnedOffEffects(int beaconTier, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof BeaconTargetsHolder settings
                && BeaconSettings.isDisabled(effect, settings.friendlybeacons$getDisabledEffects())) {
            ((AbstractWidget) (Object) this).active = false;
        }
    }
}
