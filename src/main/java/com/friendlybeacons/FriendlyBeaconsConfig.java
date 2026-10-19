package com.friendlybeacons;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config: per-tier radius, the hidden 5th tier and server-wide effect toggles. */
public final class FriendlyBeaconsConfig {
    private static final List<Integer> DEFAULT_RADII = List.of(28, 40, 52, 64, 76);

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> TIER_RADIUS;
    private static final ModConfigSpec.BooleanValue ENABLE_TIER_5;
    private static final Map<Holder<MobEffect>, ModConfigSpec.BooleanValue> EFFECTS = new LinkedHashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TIER_RADIUS = builder
                .comment("Maximum effect radius in blocks for pyramid tiers 1-5. Vanilla is [20, 30, 40, 50] (tier 5 does not exist in vanilla).")
                .defineList("tierRadius", DEFAULT_RADII, () -> 50,
                        o -> o instanceof Number n && n.intValue() >= 1 && n.intValue() <= 256);
        ENABLE_TIER_5 = builder
                .comment("Allow a 5th pyramid layer (11x11) that unlocks the tier 5 radius and bonus.")
                .define("enableTier5", true);
        builder.comment("Turn beacon effects on or off for every beacon on the server. A disabled effect can't be picked",
                        "and existing beacons stop giving it. Ops can also change these in game with /friendlybeacons effect.",
                        "glowing is the tier 5 Glowing effect.")
                .push("effects");
        for (Holder<MobEffect> effect : BeaconSettings.TOGGLEABLE_EFFECTS) {
            EFFECTS.put(effect, builder
                    .translation(effect.value().getDescriptionId())
                    .define(BeaconSettings.effectName(effect), true));
        }
        builder.pop();
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

    public static boolean effectEnabled(Holder<MobEffect> effect) {
        ModConfigSpec.BooleanValue value = EFFECTS.get(effect);
        return value == null || !SPEC.isLoaded() || value.get();
    }

    /** Disabled effects as a mask of {@link BeaconSettings#effectBit} bits. */
    public static int disabledEffects() {
        int mask = 0;
        for (Holder<MobEffect> effect : EFFECTS.keySet()) {
            if (!effectEnabled(effect)) mask |= BeaconSettings.effectBit(effect);
        }
        return mask;
    }

    /** Changes an effect toggle and writes the config file. Server side only. */
    public static void setEffectEnabled(Holder<MobEffect> effect, boolean enabled) {
        ModConfigSpec.BooleanValue value = EFFECTS.get(effect);
        value.set(enabled);
        value.save();
    }
}
