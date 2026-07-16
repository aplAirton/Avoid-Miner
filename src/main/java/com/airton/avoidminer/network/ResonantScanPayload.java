package com.airton.avoidminer.network;

import com.airton.avoidminer.AvoidMiner;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ResonantScanPayload(List<Target> targets, int durationTicks) implements CustomPacketPayload {
    public static final int MAX_TARGETS = 2048;
    public static final Type<ResonantScanPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "resonant_scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResonantScanPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ResonantScanPayload::write, ResonantScanPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        int size = Math.min(MAX_TARGETS, targets.size());
        buffer.writeVarInt(durationTicks);
        buffer.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            Target target = targets.get(i);
            buffer.writeBlockPos(target.pos());
            buffer.writeByte(target.category());
        }
    }

    private static ResonantScanPayload read(RegistryFriendlyByteBuf buffer) {
        int duration = Math.clamp(buffer.readVarInt(), 1, 72000);
        int size = Math.clamp(buffer.readVarInt(), 0, MAX_TARGETS);
        List<Target> targets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            targets.add(new Target(buffer.readBlockPos(), buffer.readByte()));
        }
        return new ResonantScanPayload(List.copyOf(targets), duration);
    }

    @Override
    public Type<ResonantScanPayload> type() {
        return TYPE;
    }

    public record Target(BlockPos pos, byte category) {}
}
