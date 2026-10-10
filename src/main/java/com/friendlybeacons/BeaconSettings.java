package com.friendlybeacons;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
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

    // Tier 5 bonus: BONUS_BOTH_II, or 1..n for a third power from BONUS_EFFECTS.
    public static final int BONUS_BOTH_II = 0;
    public static final List<Holder<MobEffect>> BONUS_EFFECTS =
            BeaconBlockEntity.BEACON_EFFECTS.stream().flatMap(List::stream).toList();

    @Nullable
    public static Holder<MobEffect> bonusEffect(int bonus) {
        return bonus >= 1 && bonus <= BONUS_EFFECTS.size() ? BONUS_EFFECTS.get(bonus - 1) : null;
    }

    /** True if the bonus would add nothing on top of the current powers. */
    public static boolean isRedundant(int bonus, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
        if (primary == null) return true;
        if (bonus == BONUS_BOTH_II) return Objects.equals(primary, secondary);
        Holder<MobEffect> effect = bonusEffect(bonus);
        return effect == null || effect.equals(primary) || effect.equals(secondary);
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

    public static boolean isValidBonus(int bonus) {
        return bonus == BONUS_BOTH_II || bonusEffect(bonus) != null;
    }
}
