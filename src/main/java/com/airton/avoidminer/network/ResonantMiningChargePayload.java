package com.airton.avoidminer.network;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ResonantMiningChargePayload(byte action) implements CustomPacketPayload {
    public static final byte START = 0;
    public static final byte RELEASE = 1;
    public static final byte CANCEL = 2;

    public static final Type<ResonantMiningChargePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "resonant_mining_charge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResonantMiningChargePayload> STREAM_CODEC =
            CustomPacketPayload.codec(ResonantMiningChargePayload::write, ResonantMiningChargePayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeByte(action);
    }

    private static ResonantMiningChargePayload read(RegistryFriendlyByteBuf buffer) {
        return new ResonantMiningChargePayload((byte) Math.clamp(buffer.readByte(), START, CANCEL));
    }

    @Override
    public Type<ResonantMiningChargePayload> type() {
        return TYPE;
    }
}
