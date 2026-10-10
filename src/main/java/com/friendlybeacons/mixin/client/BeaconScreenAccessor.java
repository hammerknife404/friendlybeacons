package com.friendlybeacons.mixin.client;

import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The powers currently picked on the screen (not yet confirmed with Done). */
@Mixin(BeaconScreen.class)
public interface BeaconScreenAccessor {
    @Accessor("primary")
    Holder<MobEffect> friendlybeacons$getPrimary();

    @Accessor("secondary")
    Holder<MobEffect> friendlybeacons$getSecondary();
}
