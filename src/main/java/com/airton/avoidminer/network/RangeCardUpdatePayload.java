package com.airton.avoidminer.network;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RangeCardUpdatePayload(byte action, int x1, int y1, int z1, int x2, int y2, int z2)
        implements CustomPacketPayload {

    public static final byte SAVE = 0;
    public static final byte CLEAR = 1;

    public static final Type<RangeCardUpdatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "range_card_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RangeCardUpdatePayload> STREAM_CODEC =
            CustomPacketPayload.codec(RangeCardUpdatePayload::write, RangeCardUpdatePayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(action);
        buf.writeInt(x1);
        buf.writeInt(y1);
        buf.writeInt(z1);
        buf.writeInt(x2);
        buf.writeInt(y2);
        buf.writeInt(z2);
    }

    private static RangeCardUpdatePayload read(RegistryFriendlyByteBuf buf) {
        return new RangeCardUpdatePayload(buf.readByte(),
                buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public Type<RangeCardUpdatePayload> type() {
        return TYPE;
    }
}
