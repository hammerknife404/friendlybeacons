package com.friendlybeacons;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config: per-tier radius and the hidden 5th tier. */
public final class FriendlyBeaconsConfig {
    private static final List<Integer> DEFAULT_RADII = List.of(28, 40, 52, 64, 76);

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> TIER_RADIUS;
    private static final ModConfigSpec.BooleanValue ENABLE_TIER_5;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TIER_RADIUS = builder
                .comment("Maximum effect radius in blocks for pyramid tiers 1-5. Vanilla is [20, 30, 40, 50] (tier 5 does not exist in vanilla).")
                .defineList("tierRadius", DEFAULT_RADII, () -> 50,
                        o -> o instanceof Number n && n.intValue() >= 1 && n.intValue() <= 256);
        ENABLE_TIER_5 = builder
                .comment("Allow a 5th pyramid layer (11x11) that unlocks the tier 5 radius and bonus.")
                .define("enableTier5", true);
        SPEC = builder.build();
    }

    private FriendlyBeaconsConfig() {}

    public static boolean tier5Enabled() {
        return SPEC.isLoaded() ? ENABLE_TIER_5.get() : true;
    }

    /** Radius in blocks for a pyramid tier (1-5). Missing list entries fall back to the defaults. */
    public static int radius(int tier) {
        List<? extends Integer> radii = SPEC.isLoaded() ? TIER_RADIUS.get() : DEFAULT_RADII;
        int index = Math.clamp(tier, 1, 5) - 1;
        Number value = index < radii.size() ? radii.get(index) : DEFAULT_RADII.get(index);
        return value.intValue();
    }
}
