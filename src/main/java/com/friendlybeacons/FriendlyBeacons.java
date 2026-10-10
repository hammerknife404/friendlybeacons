package com.friendlybeacons;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FriendlyBeacons.MOD_ID)
public class FriendlyBeacons {
    public static final String MOD_ID = "friendlybeacons";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    /** Bitmask of {@link BeaconTargets} flags, stored on each beacon block entity. */
    public static final Supplier<AttachmentType<Integer>> BEACON_TARGETS = ATTACHMENTS.register(
            "beacon_targets",
            () -> AttachmentType.builder(() -> BeaconTargets.DEFAULT).serialize(Codec.INT).build());

    public FriendlyBeacons(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
