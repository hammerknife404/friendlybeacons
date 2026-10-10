package com.friendlybeacons.mixin;

import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.FriendlyBeacons;
import java.util.List;
import java.util.Objects;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
    /**
     * Leaves vanilla alone when the beacon is set to players only; otherwise replaces
     * applyEffects with a copy that targets every selected category.
     */
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void friendlybeacons$applyEffects(Level level, BlockPos pos, int beaconLevel,
            @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, CallbackInfo ci) {
        if (level.isClientSide || primary == null) return;
        if (!(level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon)) return;
        int mask = beacon.getData(FriendlyBeacons.BEACON_TARGETS);
        if (mask == BeaconTargets.DEFAULT) return;
        ci.cancel();
        if (mask == 0) return;

        // Same range, duration and amplifier as vanilla.
        double range = beaconLevel * 10 + 10;
        int amplifier = beaconLevel >= 4 && Objects.equals(primary, secondary) ? 1 : 0;
        int duration = (9 + beaconLevel * 2) * 20;
        AABB area = new AABB(pos).inflate(range).expandTowards(0.0, level.getHeight(), 0.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> BeaconTargets.matches(entity, mask));

        for (LivingEntity entity : targets) {
            entity.addEffect(new MobEffectInstance(primary, duration, amplifier, true, true));
        }
        if (beaconLevel >= 4 && secondary != null && !Objects.equals(primary, secondary)) {
            for (LivingEntity entity : targets) {
                entity.addEffect(new MobEffectInstance(secondary, duration, 0, true, true));
            }
        }
    }
}
