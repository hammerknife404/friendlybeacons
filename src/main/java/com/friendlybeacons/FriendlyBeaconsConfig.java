package com.friendlybeacons;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/** Server config: per-tier radius, the hidden 5th tier, the level III bonus and which effects can be picked. */
public final class FriendlyBeaconsConfig {
    private static final List<Integer> DEFAULT_RADII = List.of(28, 40, 52, 64, 76);

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> TIER_RADIUS;
    private static final ModConfigSpec.BooleanValue ENABLE_TIER_5;
    private static final ModConfigSpec.BooleanValue ENABLE_LEVEL_III;
    private static final ModConfigSpec.BooleanValue ALLOW_RESISTANCE_III;
    /** Config section holding one toggle per selectable effect, keyed by {@link #effectKey}. */
    public static final String EFFECTS_SECTION = "effects";
    private static final Map<String, ModConfigSpec.BooleanValue> EFFECTS = new LinkedHashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TIER_RADIUS = builder
                .comment("Maximum effect radius in blocks for pyramid tiers 1-5. Vanilla is [20, 30, 40, 50] (tier 5 does not exist in vanilla).")
                .defineList("tierRadius", DEFAULT_RADII, () -> 50,
                        o -> o instanceof Number n && n.intValue() >= 1 && n.intValue() <= 256);
        ENABLE_TIER_5 = builder
                .comment("Allow a 5th pyramid layer (11x11) that unlocks the tier 5 radius and bonus.")
                .define("enableTier5", true);
        ENABLE_LEVEL_III = builder
                .comment("Allow the tier 5 bonus that raises the primary power to level III. Resistance also needs allowResistanceIII.")
                .define("enableLevelIII", true);
        ALLOW_RESISTANCE_III = builder
                .comment("Allow the tier 5 \"primary at III\" bonus to pick Resistance (60% less damage).")
                .define("allowResistanceIII", false);
        builder.comment("Effects beacons can give. A turned off effect can't be picked, and beacons that already have it stop giving it.")
                .push(EFFECTS_SECTION);
        for (Holder<MobEffect> effect : BeaconSettings.BONUS_EFFECTS) {
            String key = effectKey(effect);
            EFFECTS.put(key, builder.translation(effect.value().getDescriptionId()).define(key, true));
        }
        builder.pop();
        SPEC = builder.build();
    }

    private FriendlyBeaconsConfig() {}

    public static boolean tier5Enabled() {
        return SPEC.isLoaded() ? ENABLE_TIER_5.get() : true;
    }

    public static boolean levelIIIEnabled() {
        return SPEC.isLoaded() ? ENABLE_LEVEL_III.get() : true;
    }

    public static boolean resistanceIIIAllowed() {
        return SPEC.isLoaded() && ALLOW_RESISTANCE_III.get();
    }

    public static boolean effectEnabled(Holder<MobEffect> effect) {
        ModConfigSpec.BooleanValue value = EFFECTS.get(effectKey(effect));
        return !SPEC.isLoaded() || value == null || value.get();
    }

    /** Config key for an effect: its registry path, e.g. "jump_boost". */
    public static String effectKey(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(key -> key.location().getPath()).orElse("");
    }

    @Nullable
    public static Holder<MobEffect> effectForKey(String key) {
        return BeaconSettings.BONUS_EFFECTS.stream().filter(effect -> effectKey(effect).equals(key)).findFirst().orElse(null);
    }

    /** Radius in blocks for a pyramid tier (1-5). Missing list entries fall back to the defaults. */
    public static int radius(int tier) {
        List<? extends Integer> radii = SPEC.isLoaded() ? TIER_RADIUS.get() : DEFAULT_RADII;
        int index = Math.clamp(tier, 1, 5) - 1;
        Number value = index < radii.size() ? radii.get(index) : DEFAULT_RADII.get(index);
        return value.intValue();
    }
}
