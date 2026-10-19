package com.friendlybeacons;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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

    /** 1 if the beacon gives Glowing at tier 5, 0 if its Glowing checkbox is off. */
    public static final Supplier<AttachmentType<Integer>> BEACON_GLOW = intAttachment("beacon_glow", 1);

    private static Supplier<AttachmentType<Integer>> intAttachment(String name, int defaultValue) {
        return ATTACHMENTS.register(name, () -> AttachmentType.builder(() -> defaultValue).serialize(Codec.INT).build());
    }

    public FriendlyBeacons(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, FriendlyBeaconsConfig.SPEC);
        modBus.addListener(FriendlyBeacons::registerPayloads);
        NeoForge.EVENT_BUS.addListener(FriendlyBeaconsCommand::register);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(HandshakePayload.TYPE, HandshakePayload.STREAM_CODEC, (payload, context) -> {});
    }
}
