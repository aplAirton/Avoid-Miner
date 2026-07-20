package com.airton.avoidminer.network;

import com.airton.avoidminer.event.ResonantMiningManager;
import com.airton.avoidminer.item.RangeCardItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
        registrar.playToServer(
                RangeCardUpdatePayload.TYPE,
                RangeCardUpdatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        handleRangeCardUpdate(player, payload);
                    }
                }));
    }

    private static void handleRangeCardUpdate(ServerPlayer player, RangeCardUpdatePayload payload) {
        ItemStack stack = ItemStack.EMPTY;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack h = player.getItemInHand(hand);
            if (h.getItem() instanceof RangeCardItem) {
                stack = h;
                break;
            }
        }
        if (stack.isEmpty()) return;

        if (payload.action() == RangeCardUpdatePayload.CLEAR) {
            RangeCardItem.clearData(stack);
        } else if (payload.action() == RangeCardUpdatePayload.SAVE) {
            RangeCardItem.setAllCoords(stack,
                    payload.x1(), payload.y1(), payload.z1(),
                    payload.x2(), payload.y2(), payload.z2());
        }
    }
}
