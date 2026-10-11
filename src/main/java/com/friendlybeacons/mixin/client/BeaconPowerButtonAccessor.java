package com.friendlybeacons.mixin.client;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** What a vanilla power button picks, for the Powers panel hover preview. */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton")
public interface BeaconPowerButtonAccessor {
    @Accessor("isPrimary")
    boolean friendlybeacons$isPrimary();

    @Accessor("effect")
    Holder<MobEffect> friendlybeacons$getEffect();
}
