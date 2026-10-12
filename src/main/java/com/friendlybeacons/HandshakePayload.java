package com.friendlybeacons;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Never sent. Registering it as a required payload makes NeoForge refuse clients without the mod at login,
 * instead of letting them join and crash on the beacon menu's extra data slots.
 */
public record HandshakePayload() implements CustomPacketPayload {
    public static final Type<HandshakePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FriendlyBeacons.MOD_ID, "handshake"));
    public static final StreamCodec<ByteBuf, HandshakePayload> STREAM_CODEC = StreamCodec.unit(new HandshakePayload());

    @Override
    public Type<HandshakePayload> type() {
        return TYPE;
    }
}
