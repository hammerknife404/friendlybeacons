package com.friendlybeacons.mixin;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.FriendlyBeaconsConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
    /** Checks a 5th pyramid layer (11x11) when tier 5 is enabled. */
    @ModifyConstant(method = "updateBase", constant = @Constant(intValue = 4))
    private static int friendlybeacons$maxPyramidLayers(int layers) {
        return FriendlyBeaconsConfig.tier5Enabled() ? 5 : layers;
    }

    /**
     * Replaces applyEffects to support target categories, the configurable radius and the
     * tier 5 bonus. Duration and the tier 1-4 power rules match vanilla.
     */
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void friendlybeacons$applyEffects(Level level, BlockPos pos, int beaconLevel,
            @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, CallbackInfo ci) {
        if (level.isClientSide || primary == null) return;
        if (!(level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon)) return;
        ci.cancel();
        int mask = beacon.getData(FriendlyBeacons.BEACON_TARGETS);
        if (mask == 0) return;

        List<BeaconSettings.Power> powers = BeaconSettings.powers(beaconLevel, primary, secondary,
                beacon.getData(FriendlyBeacons.BEACON_BONUS));

        double range = BeaconSettings.radiusBlocks(beaconLevel, beacon.getData(FriendlyBeacons.BEACON_RADIUS));
        int duration = (9 + beaconLevel * 2) * 20;
        AABB area = new AABB(pos).inflate(range).expandTowards(0.0, level.getHeight(), 0.0);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> BeaconTargets.matches(e, mask))) {
            for (BeaconSettings.Power power : powers) {
                entity.addEffect(new MobEffectInstance(power.effect(), duration, power.amplifier(), true, true));
            }
        }
    }
}
