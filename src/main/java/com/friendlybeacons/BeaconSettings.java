package com.friendlybeacons;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;

/** Radius selection, tier 5 bonus and effect toggle rules, shared by the server logic and the client panel. */
public final class BeaconSettings {
    private BeaconSettings() {}

    // Menu button ids. 0-3 are the BeaconTargets checkboxes.
    public static final int BUTTON_RADIUS = 4;
    public static final int BUTTON_GLOW = 5;
    /** Sets the tier 5 bonus to (id - BUTTON_SET_BONUS). Sent just before the vanilla Done packet. */
    public static final int BUTTON_SET_BONUS = 100;

    /** Stored radius selection meaning "largest radius this pyramid allows". */
    public static final int RADIUS_MAX = 0;

    /** Pyramid tier whose radius is used, given the beacon's levels and the stored selection. */
    public static int radiusTier(int levels, int selection) {
        return selection == RADIUS_MAX ? levels : Math.min(selection, levels);
    }

    /** Selection after one click: the next tier, wrapping to 1; the top tier is stored as RADIUS_MAX. */
    public static int nextRadiusSelection(int levels, int selection) {
        if (levels <= 0) return selection;
        int next = radiusTier(levels, selection) % levels + 1;
        return next == levels ? RADIUS_MAX : next;
    }

    public static int radiusBlocks(int levels, int selection) {
        return levels > 0 ? FriendlyBeaconsConfig.radius(radiusTier(levels, selection)) : 0;
    }

    // Tier 5 bonus: BONUS_BOTH_II, or 1..n for a third power from BONUS_EFFECTS.
    public static final int BONUS_BOTH_II = 0;
    public static final List<Holder<MobEffect>> BONUS_EFFECTS =
            BeaconBlockEntity.BEACON_EFFECTS.stream().flatMap(List::stream).toList();

    /** Given by every tier 5 beacon unless its Glowing checkbox is off. */
    public static final Holder<MobEffect> GLOW_EFFECT = MobEffects.GLOWING;

    /** Effects admins can turn off server-wide: the vanilla beacon powers plus tier 5 Glowing. */
    public static final List<Holder<MobEffect>> TOGGLEABLE_EFFECTS =
            Stream.concat(BONUS_EFFECTS.stream(), Stream.of(GLOW_EFFECT)).toList();

    /** Config and command name of an effect, e.g. "jump_boost". */
    public static String effectName(Holder<MobEffect> effect) {
        return effect.unwrapKey().orElseThrow().location().getPath();
    }

    /** Bit of an effect in a disabled-effects mask, indexed by TOGGLEABLE_EFFECTS. */
    public static int effectBit(Holder<MobEffect> effect) {
        int index = TOGGLEABLE_EFFECTS.indexOf(effect);
        return index < 0 ? 0 : 1 << index;
    }

    public static boolean isDisabled(@Nullable Holder<MobEffect> effect, int disabled) {
        return effect != null && (disabled & effectBit(effect)) != 0;
    }

    @Nullable
    public static Holder<MobEffect> bonusEffect(int bonus) {
        return bonus >= 1 && bonus <= BONUS_EFFECTS.size() ? BONUS_EFFECTS.get(bonus - 1) : null;
    }

    /** True if the bonus would add nothing on top of the current powers, or its effect is disabled. */
    public static boolean isRedundant(int bonus, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, int disabled) {
        if (primary == null) return true;
        if (bonus == BONUS_BOTH_II) return Objects.equals(primary, secondary);
        Holder<MobEffect> effect = bonusEffect(bonus);
        return effect == null || effect.equals(primary) || effect.equals(secondary) || isDisabled(effect, disabled);
    }

    /** Next third-power bonus after the given one, skipping effects the powers already cover and disabled ones. */
    public static int nextEffectBonus(int bonus, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, int disabled) {
        int count = BONUS_EFFECTS.size();
        for (int step = 1; step <= count; step++) {
            int candidate = Math.floorMod(bonus - 1 + step, count) + 1;
            if (!isRedundant(candidate, primary, secondary, disabled)) return candidate;
        }
        return bonus;
    }

    public static boolean isValidBonus(int bonus) {
        return bonus == BONUS_BOTH_II || bonusEffect(bonus) != null;
    }

    public record Power(Holder<MobEffect> effect, int amplifier) {}

    /**
     * Effects a beacon gives with these levels, powers, tier 5 bonus and Glowing checkbox, minus
     * the effects disabled server-wide. Tier 1-4 rules match vanilla. Used by the server to apply
     * effects and by the client to preview them.
     */
    public static List<Power> powers(int levels, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary,
            int bonus, boolean glow, int disabled) {
        List<Power> powers = new ArrayList<>();
        if (levels <= 0 || primary == null) return powers;
        boolean tier5 = levels >= 5;
        boolean bothII = tier5 && bonus == BONUS_BOTH_II;
        powers.add(new Power(primary, (levels >= 4 && primary.equals(secondary)) || bothII ? 1 : 0));
        if (levels >= 4 && secondary != null && !primary.equals(secondary)) {
            powers.add(new Power(secondary, bothII ? 1 : 0));
        }
        if (tier5 && !isRedundant(bonus, primary, secondary, disabled)) {
            Holder<MobEffect> extra = bonusEffect(bonus);
            if (extra != null) powers.add(new Power(extra, 0));
        }
        if (tier5 && glow) powers.add(new Power(GLOW_EFFECT, 0));
        powers.removeIf(power -> isDisabled(power.effect(), disabled));
        return powers;
    }
}
