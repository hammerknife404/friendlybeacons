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

/** Radius selection and tier 5 bonus rules, shared by the server logic and the client panel. */
public final class BeaconSettings {
    private BeaconSettings() {}

    // Menu button ids. 0-3 are the BeaconTargets checkboxes.
    public static final int BUTTON_RADIUS = 4;
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

    // Tier 5 bonus: BONUS_BOTH_II, BONUS_PRIMARY_III, or 1..n for a third power from BONUS_EFFECTS.
    // Saved beacons store the index, so new effects go at the end.
    public static final int BONUS_BOTH_II = 0;
    public static final int BONUS_PRIMARY_III = -1;
    public static final List<Holder<MobEffect>> BONUS_EFFECTS = Stream.concat(
            BeaconBlockEntity.BEACON_EFFECTS.stream().flatMap(List::stream),
            Stream.of(MobEffects.GLOWING)).toList();

    @Nullable
    public static Holder<MobEffect> bonusEffect(int bonus) {
        return bonus >= 1 && bonus <= BONUS_EFFECTS.size() ? BONUS_EFFECTS.get(bonus - 1) : null;
    }

    /** True if the bonus would add nothing on top of the current powers, or isn't allowed for them. */
    public static boolean isRedundant(int bonus, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
        if (primary == null) return true;
        if (bonus == BONUS_BOTH_II) return Objects.equals(primary, secondary);
        if (bonus == BONUS_PRIMARY_III) {
            return !FriendlyBeaconsConfig.levelIIIEnabled() || !FriendlyBeaconsConfig.effectEnabled(primary) || isResistanceIIIBlocked(primary);
        }
        Holder<MobEffect> effect = bonusEffect(bonus);
        return effect == null || !FriendlyBeaconsConfig.effectEnabled(effect) || effect.equals(primary) || effect.equals(secondary);
    }

    /** Next third-power bonus after the given one, skipping effects the powers already cover. */
    public static int nextEffectBonus(int bonus, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
        int count = BONUS_EFFECTS.size();
        for (int step = 1; step <= count; step++) {
            int candidate = Math.floorMod(bonus - 1 + step, count) + 1;
            if (!isRedundant(candidate, primary, secondary)) return candidate;
        }
        return bonus;
    }

    /** True if the primary is Resistance and the server config doesn't allow it at level III. */
    public static boolean isResistanceIIIBlocked(Holder<MobEffect> primary) {
        return primary.equals(MobEffects.DAMAGE_RESISTANCE) && !FriendlyBeaconsConfig.resistanceIIIAllowed();
    }

    /** BONUS_BOTH_II and BONUS_PRIMARY_III, the bonuses that raise power levels instead of adding one. */
    public static boolean isLevelBonus(int bonus) {
        return bonus == BONUS_BOTH_II || bonus == BONUS_PRIMARY_III;
    }

    public static boolean isValidBonus(int bonus) {
        return isLevelBonus(bonus) || bonusEffect(bonus) != null;
    }

    public record Power(Holder<MobEffect> effect, int amplifier) {}

    /**
     * Effects a beacon gives with these levels, powers and tier 5 bonus. Tier 1-4 rules match
     * vanilla, minus effects turned off in the config. Used by the server to apply effects and by
     * the client to preview them.
     */
    public static List<Power> powers(int levels, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, int bonus) {
        List<Power> powers = new ArrayList<>();
        if (levels <= 0 || primary == null) return powers;
        boolean tier5 = levels >= 5;
        // A saved III falls back to the default bonus if the config no longer allows it.
        if (bonus == BONUS_PRIMARY_III && isRedundant(bonus, primary, secondary)) bonus = BONUS_BOTH_II;
        boolean bothII = tier5 && bonus == BONUS_BOTH_II;
        boolean primaryIII = tier5 && bonus == BONUS_PRIMARY_III;
        int primaryLevel = primaryIII ? 2 : (levels >= 4 && primary.equals(secondary)) || bothII ? 1 : 0;
        powers.add(new Power(primary, primaryLevel));
        if (levels >= 4 && secondary != null && !primary.equals(secondary)) {
            powers.add(new Power(secondary, bothII ? 1 : 0));
        }
        if (tier5 && !isRedundant(bonus, primary, secondary)) {
            Holder<MobEffect> extra = bonusEffect(bonus);
            if (extra != null) powers.add(new Power(extra, 0));
        }
        powers.removeIf(power -> !FriendlyBeaconsConfig.effectEnabled(power.effect()));
        return powers;
    }
}
