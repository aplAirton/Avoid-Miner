package com.airton.avoidminer.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                ResonantScanPayload.TYPE, ResonantScanPayload.STREAM_CODEC);
    }
}
