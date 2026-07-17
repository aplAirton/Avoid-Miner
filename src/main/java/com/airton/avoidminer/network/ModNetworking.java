package com.airton.avoidminer.network;

import com.airton.avoidminer.event.ResonantMiningManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(ResonantScanPayload.TYPE, ResonantScanPayload.STREAM_CODEC);
        registrar.playToServer(
                ResonantMiningChargePayload.TYPE,
                ResonantMiningChargePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        ResonantMiningManager.handleChargeAction(player, payload.action());
                    }
                }));
    }
}
