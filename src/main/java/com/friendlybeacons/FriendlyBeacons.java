package com.friendlybeacons;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FriendlyBeacons.MOD_ID)
public class FriendlyBeacons {
    public static final String MOD_ID = "friendlybeacons";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    /** Bitmask of {@link BeaconTargets} flags, stored on each beacon block entity. */
    public static final Supplier<AttachmentType<Integer>> BEACON_TARGETS = intAttachment("beacon_targets", BeaconTargets.DEFAULT);

    /** Selected radius tier, or {@link BeaconSettings#RADIUS_MAX}. */
    public static final Supplier<AttachmentType<Integer>> BEACON_RADIUS = intAttachment("beacon_radius", BeaconSettings.RADIUS_MAX);

    /** Tier 5 bonus, see {@link BeaconSettings}. */
    public static final Supplier<AttachmentType<Integer>> BEACON_BONUS = intAttachment("beacon_bonus", BeaconSettings.BONUS_BOTH_II);

    private static Supplier<AttachmentType<Integer>> intAttachment(String name, int defaultValue) {
        return ATTACHMENTS.register(name, () -> AttachmentType.builder(() -> defaultValue).serialize(Codec.INT).build());
    }

    public FriendlyBeacons(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, FriendlyBeaconsConfig.SPEC);
    }
}
